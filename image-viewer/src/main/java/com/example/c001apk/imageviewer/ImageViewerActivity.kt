package com.example.c001apk.imageviewer

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import java.lang.ref.WeakReference
import kotlin.math.min

class ImageViewerActivity : AppCompatActivity() {
    private lateinit var request: ImageViewer.Request
    private lateinit var root: FrameLayout
    private lateinit var backdrop: View
    private lateinit var pager: ViewPager2
    private lateinit var indicator: LinearLayout
    private val pages = mutableMapOf<Int, ImageViewerPage>()
    private var selectedPage = 0
    private var resumed = false
    private var opened = false
    private var closing = false
    private var transition: ValueAnimator? = null
    private var thumbnailOverlay: View? = null
    private val statusBars = WindowInsetsCompat.Type.statusBars()
    private val updateSystemBarsRunnable = Runnable { updateSystemBars() }
    private var statusBarHeightPx = 0
    private var statusBarHidden = false

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ImageViewer.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pending = lastCustomNonConfigurationInstance as? ImageViewer.Request
            ?: ImageViewer.takeRequest(intent.getStringExtra(ImageViewer.EXTRA_REQUEST))
        if (pending == null) {
            finish()
            return
        }
        request = pending
        selectedPage = (savedInstanceState?.getInt(STATE_PAGE) ?: request.startingPage)
            .coerceIn(request.images.indices)

        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (request.enableHdr && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            window.colorMode = ActivityInfo.COLOR_MODE_HDR
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        @Suppress("DEPRECATION")
        window.navigationBarColor = Color.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        root = FrameLayout(this).apply { clipChildren = false }
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val measuredHeightPx = maxOf(
                insets.getInsets(statusBars).top,
                insets.getInsetsIgnoringVisibility(statusBars).top,
            )
            // 部分系统隐藏状态栏时连稳定 inset 也会归零，继续使用已知的高度。
            if (measuredHeightPx > 0 && measuredHeightPx != statusBarHeightPx &&
                (statusBarHeightPx == 0 || insets.isVisible(statusBars))
            ) {
                statusBarHeightPx = measuredHeightPx
                scheduleSystemBarsUpdate()
            }
            insets
        }
        backdrop = View(this).apply {
            setBackgroundColor(Color.BLACK)
            alpha = 0f
        }
        root.addView(backdrop, matchParent())
        pager = ViewPager2(this).apply {
            isUserInputEnabled = false
            alpha = 0f
            adapter = PageAdapter()
            setCurrentItem(selectedPage, false)
        }
        root.addView(pager, matchParent())
        indicator = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            alpha = 0f
            repeat(request.images.size.takeIf { it > 1 } ?: 0) {
                addView(View(context).apply {
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(Color.WHITE)
                    }
                    importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                }, LinearLayout.LayoutParams(dp(5), dp(5)).apply {
                    marginStart = dp(3)
                    marginEnd = dp(3)
                })
            }
        }
        root.addView(indicator, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            dp(36),
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
        ).apply { bottomMargin = dp(16) })
        setContentView(root)
        selectPage(selectedPage)
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                selectPage(position)
            }
        })
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = closeViewer()
        })
        ImageViewer.currentActivity = WeakReference(this)
        root.doOnPreDraw { if (!closing) openViewer() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_PAGE, selectedPage)
        super.onSaveInstanceState(outState)
    }

    @Suppress("DEPRECATION")
    override fun onRetainCustomNonConfigurationInstance(): Any? =
        if (::request.isInitialized) request else null

    override fun onResume() {
        super.onResume()
        resumed = true
        updatePageVisibility()
    }

    override fun onPause() {
        resumed = false
        updatePageVisibility()
        super.onPause()
    }

    override fun onDestroy() {
        if (::root.isInitialized) root.removeCallbacks(updateSystemBarsRunnable)
        transition?.removeAllListeners()
        transition?.removeAllUpdateListeners()
        transition?.cancel()
        pages.values.forEach(ImageViewerPage::dispose)
        pages.clear()
        if (ImageViewer.currentActivity?.get() === this) ImageViewer.currentActivity = null
        if (::request.isInitialized && isFinishing) request.onClosed()
        super.onDestroy()
    }

    private fun selectPage(position: Int) {
        selectedPage = position
        request.onPageSelected(position)
        for (index in 0 until indicator.childCount) {
            indicator.getChildAt(index).alpha = if (index == position) 1f else 0.42f
        }
        updatePageVisibility()
        updateSystemBars()
    }

    private fun updatePageVisibility() {
        pages.forEach { (position, page) -> page.setPageVisible(resumed && position == selectedPage) }
    }

    private fun scheduleSystemBarsUpdate() {
        if (closing) return
        root.removeCallbacks(updateSystemBarsRunnable)
        root.postOnAnimation(updateSystemBarsRunnable)
    }

    private fun updateSystemBars() {
        if (closing || statusBarHeightPx == 0) return
        val rect = pages[selectedPage]?.displayRect
        val shouldHide = rect != null && !rect.isEmpty && rect.top < statusBarHeightPx
        if (shouldHide == statusBarHidden) return
        statusBarHidden = shouldHide
        val controller = WindowCompat.getInsetsController(window, root)
        if (shouldHide) controller.hide(statusBars) else controller.show(statusBars)
    }

    private fun openViewer() {
        val thumbnail = request.thumbnailAt(selectedPage)
        val bounds = thumbnailBounds(thumbnail)
        val overlay = if (thumbnail != null && bounds != null && thumbnail.drawable != null) {
            object : View(this) {
                override fun onDraw(canvas: Canvas) {
                    thumbnail.draw(canvas)
                }
            }.apply {
                pivotX = 0f
                pivotY = 0f
                translationX = bounds.left
                translationY = bounds.top
                root.addView(this, FrameLayout.LayoutParams(thumbnail.width, thumbnail.height))
            }.also { thumbnailOverlay = it }
        } else {
            null
        }
        val target = thumbnail?.takeIf { overlay != null }?.let(::fitThumbnail)
        animate(OPEN_DURATION_MS, onFrame = { progress ->
            backdrop.alpha = progress
            pager.alpha = progress
            if (overlay != null && bounds != null && target != null) {
                overlay.translationX = lerp(bounds.left, target.left, progress)
                overlay.translationY = lerp(bounds.top, target.top, progress)
                overlay.scaleX = lerp(1f, target.width() / bounds.width(), progress)
                overlay.scaleY = lerp(1f, target.height() / bounds.height(), progress)
                overlay.alpha = 1f - progress
            }
        }, onEnd = {
            removeThumbnailOverlay()
            opened = true
            pager.isUserInputEnabled = true
            indicator.alpha = 1f
            pages.values.forEach(ImageViewerPage::setPlaybackReady)
            updateSystemBars()
            request.onOpened()
        })
    }

    private fun dragViewer(dx: Float, dy: Float) {
        val progress = (dy / root.height.coerceAtLeast(1)).coerceIn(0f, 1f)
        pager.translationX = dx * 0.75f
        pager.translationY = dy
        pager.scaleX = 1f - 0.65f * progress
        pager.scaleY = pager.scaleX
        backdrop.alpha = 1f - progress
        indicator.alpha = (1f - progress * 3f).coerceAtLeast(0f)
        pages[selectedPage]?.updateDrag(pager.scaleX)
    }

    private fun releaseDrag(dismiss: Boolean) {
        if (dismiss) {
            closeViewer()
            return
        }
        val from = snapshot()
        val to = Transform()
        animate(RESET_DURATION_MS, { applyTransform(from, to, it) }) {
            pages[selectedPage]?.cancelDrag()
        }
    }

    private fun closeViewer() {
        if (closing) return
        closing = true
        root.removeCallbacks(updateSystemBarsRunnable)
        pager.isUserInputEnabled = false
        removeThumbnailOverlay()
        val page = pages[selectedPage]
        page?.beginClose()
        val from = snapshot()
        val thumbnail = thumbnailBounds(request.thumbnailAt(selectedPage))
        val imageBounds = page?.displayRect?.takeUnless(RectF::isEmpty)
        val to = if (thumbnail != null && imageBounds != null) {
            val scale = min(thumbnail.width() / imageBounds.width(), thumbnail.height() / imageBounds.height())
                .coerceIn(0.02f, 1f)
            Transform(
                x = thumbnail.centerX() - root.width / 2f - (imageBounds.centerX() - root.width / 2f) * scale,
                y = thumbnail.centerY() - root.height / 2f - (imageBounds.centerY() - root.height / 2f) * scale,
                scale = scale,
                contentAlpha = 0f,
                backdropAlpha = 0f,
                indicatorAlpha = 0f,
            )
        } else {
            from.copy(scale = from.scale * 0.94f, contentAlpha = 0f, backdropAlpha = 0f, indicatorAlpha = 0f)
        }
        if (statusBarHidden) {
            // 退出转场还在遮挡底页时恢复状态栏，让底页先收到稳定的 inset。
            WindowCompat.getInsetsController(window, root).show(statusBars)
            statusBarHidden = false
        }
        animate(CLOSE_DURATION_MS, { applyTransform(from, to, it) }) {
            root.doOnPreDraw {
                root.postOnAnimation {
                    finish()
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                }
            }
            root.invalidate()
        }
    }

    private fun removeThumbnailOverlay() {
        thumbnailOverlay?.let(root::removeView)
        thumbnailOverlay = null
    }

    private fun thumbnailBounds(thumbnail: ImageView?): RectF? {
        if (thumbnail == null || !thumbnail.isAttachedToWindow || !thumbnail.isShown ||
            thumbnail.width <= 0 || thumbnail.height <= 0
        ) return null
        val position = IntArray(2)
        val origin = IntArray(2)
        thumbnail.getLocationOnScreen(position)
        root.getLocationOnScreen(origin)
        val left = (position[0] - origin[0]).toFloat()
        val top = (position[1] - origin[1]).toFloat()
        val rect = RectF(left, top, left + thumbnail.width, top + thumbnail.height)
        return rect.takeIf { RectF.intersects(it, RectF(0f, 0f, root.width.toFloat(), root.height.toFloat())) }
    }

    private fun fitThumbnail(thumbnail: ImageView): RectF {
        val width = thumbnail.drawable.intrinsicWidth.takeIf { it > 0 } ?: thumbnail.width
        val height = thumbnail.drawable.intrinsicHeight.takeIf { it > 0 } ?: thumbnail.height
        val scale = min(root.width.toFloat() / width, root.height.toFloat() / height)
        val left = (root.width - width * scale) / 2f
        val top = (root.height - height * scale) / 2f
        return RectF(left, top, root.width - left, root.height - top)
    }

    private fun snapshot() = Transform(
        x = pager.translationX,
        y = pager.translationY,
        scale = pager.scaleX,
        contentAlpha = pager.alpha,
        backdropAlpha = backdrop.alpha,
        indicatorAlpha = indicator.alpha,
    )

    private fun applyTransform(from: Transform, to: Transform, progress: Float) {
        pager.translationX = lerp(from.x, to.x, progress)
        pager.translationY = lerp(from.y, to.y, progress)
        pager.scaleX = lerp(from.scale, to.scale, progress)
        pager.scaleY = pager.scaleX
        pager.alpha = lerp(from.contentAlpha, to.contentAlpha, progress)
        backdrop.alpha = lerp(from.backdropAlpha, to.backdropAlpha, progress)
        indicator.alpha = lerp(from.indicatorAlpha, to.indicatorAlpha, progress)
        pages[selectedPage]?.updateDrag(pager.scaleX)
    }

    private fun animate(durationMs: Long, onFrame: (Float) -> Unit, onEnd: () -> Unit) {
        transition?.removeAllListeners()
        transition?.cancel()
        transition = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = durationMs
            interpolator = DecelerateInterpolator()
            addUpdateListener { onFrame(it.animatedValue as Float) }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    transition = null
                    onEnd()
                }
            })
            start()
        }
    }

    private fun matchParent() = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT,
    )

    private fun dp(value: Int): Int = (resources.displayMetrics.density * value + 0.5f).toInt()

    private inner class PageAdapter : RecyclerView.Adapter<PageHolder>() {
        override fun getItemCount(): Int = request.images.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageHolder =
            PageHolder(FrameLayout(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            })

        override fun onBindViewHolder(holder: PageHolder, position: Int) {
            onViewRecycled(holder)
            val page = ImageViewerPage(
                context = this@ImageViewerActivity,
                image = request.images[position],
                renderer = request.rendererAt(position),
                imageLoader = ImageViewer.imageLoader(),
                autoLoadOriginal = request.autoLoadOriginal,
                retryLabel = request.retryLabel,
                onImageChanged = {
                    if (position == selectedPage) scheduleSystemBarsUpdate()
                },
                onTap = { if (position == selectedPage && opened) closeViewer() },
                onLongTap = { loadOriginal ->
                    if (position == selectedPage && opened && !closing) {
                        request.onLongClick(this@ImageViewerActivity, position, loadOriginal)
                    }
                },
                canDismiss = {
                    position == selectedPage && opened && !closing && transition == null &&
                        pager.scrollState == ViewPager2.SCROLL_STATE_IDLE
                },
                onDrag = ::dragViewer,
                onRelease = ::releaseDrag,
            )
            holder.frame.addView(page, matchParent())
            holder.boundPosition = position
            holder.page = page
            pages[position] = page
            page.setPageVisible(resumed && selectedPage == position)
            if (opened) page.setPlaybackReady()
        }

        override fun onViewRecycled(holder: PageHolder) {
            val page = holder.page ?: return
            if (pages[holder.boundPosition] === page) pages.remove(holder.boundPosition)
            page.dispose()
            holder.frame.removeView(page)
            holder.page = null
        }
    }

    private class PageHolder(val frame: FrameLayout) : RecyclerView.ViewHolder(frame) {
        var boundPosition = RecyclerView.NO_POSITION
        var page: ImageViewerPage? = null
    }

    private data class Transform(
        val x: Float = 0f,
        val y: Float = 0f,
        val scale: Float = 1f,
        val contentAlpha: Float = 1f,
        val backdropAlpha: Float = 1f,
        val indicatorAlpha: Float = 1f,
    )

    private companion object {
        const val STATE_PAGE = "selected_page"
        const val OPEN_DURATION_MS = 250L
        const val CLOSE_DURATION_MS = 220L
        const val RESET_DURATION_MS = 180L

        fun lerp(start: Float, end: Float, fraction: Float) = start + (end - start) * fraction
    }
}
