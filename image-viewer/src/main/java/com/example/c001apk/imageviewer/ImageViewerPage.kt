package com.example.c001apk.imageviewer

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

internal class ImageViewerPage(
    context: Context,
    private val image: ImageViewer.Image,
    private val renderer: ImageViewLoadFactory,
    private val imageLoader: ImageFileLoader,
    private val autoLoadOriginal: Boolean,
    @StringRes private val retryLabel: Int,
    private val onImageChanged: () -> Unit,
    private val onTap: () -> Unit,
    private val onLongTap: ((() -> Unit)?) -> Unit,
    canDismiss: () -> Boolean,
    onDrag: (Float, Float) -> Unit,
    onRelease: (Boolean) -> Unit,
) : FrameLayout(context) {
    private val content = renderer.newContentLoader()
    private val originalUrl = image.originalUrl?.takeUnless { it == image.previewUrl }
    private val state = ImageLoadState(originalUrl != null)
    private val handler = Handler(Looper.getMainLooper())
    private val requestId = nextRequestId.incrementAndGet()
    private val progress = ImageLoadingIndicator(context)
    private val retry = Button(context)
    private var disposed = false
    private var readyForPlayback = false
    private var isPageVisible = false
    private var retryOriginal = false

    val displayRect get() = content.displayRect

    init {
        content.init(context, image.previewUrl, originalUrl)
        content.onDisplayRectChanged(onImageChanged)
        val gestureLayout = ImageGestureLayout(
            context,
            canDismiss = { canDismiss() && !content.ownsVerticalDrag(isDown = true) },
            onDrag = onDrag,
            onRelease = onRelease,
        )
        val fill = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        gestureLayout.addView(content.providerView(), LayoutParams(fill))
        gestureLayout.setOnClickListener { onTap() }
        addView(gestureLayout, fill)
        content.onTapCallback { _, _, _ -> onTap() }
        content.onLongTapCallback { _, _, _ -> showImageMenu() }
        progress.isVisible = false
        addView(progress, LayoutParams(dp(50), dp(50), Gravity.CENTER))
        retry.setText(retryLabel)
        retry.setOnClickListener {
            if (retryOriginal) loadOriginal(cacheOnly = false) else loadPreview(cacheOnly = false)
        }
        retry.isVisible = false
        addView(retry, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER))
        loadPreview(cacheOnly = true)
    }

    fun setPlaybackReady() {
        if (readyForPlayback) return
        readyForPlayback = true
        content.loadAnimFinish()
    }

    fun setPageVisible(visible: Boolean) {
        if (visible == isPageVisible) return
        isPageVisible = visible
        content.pageChange(!visible)
    }

    fun updateDrag(scale: Float) = content.dragging(scale)

    fun cancelDrag() = content.backToNormal()

    fun beginClose() = content.beginBackToMin(isResetSize = false)

    fun dispose() {
        if (disposed) return
        disposed = true
        setPageVisible(false)
        imageLoader.cancel(requestId)
        content.dispose()
    }

    private fun showImageMenu() {
        val loadOriginalAction: (() -> Unit)? = if (!autoLoadOriginal && state.showTargetAction) {
            { if (!disposed) loadOriginal(cacheOnly = false) }
        } else {
            null
        }
        onLongTap(loadOriginalAction)
    }

    private fun loadPreview(cacheOnly: Boolean) {
        retry.isVisible = false
        request(image.previewUrl, cacheOnly, onSuccess = { file, width, height ->
            if (state.acceptPreview()) {
                show(file, width, height, isPreview = originalUrl != null)
                loadOriginal(cacheOnly = !autoLoadOriginal)
            }
        }, onFailure = {
            when {
                cacheOnly -> loadPreview(cacheOnly = false)
                originalUrl != null && autoLoadOriginal -> loadOriginal(cacheOnly = false)
                else -> {
                    retryOriginal = false
                    retry.isVisible = true
                }
            }
        })
    }

    private fun loadOriginal(cacheOnly: Boolean) {
        val url = originalUrl ?: return
        if (!state.beginTarget()) return
        if (!cacheOnly) retry.isVisible = false
        request(url, cacheOnly, onSuccess = { file, width, height ->
            state.acceptTarget()
            show(file, width, height, isPreview = false)
        }, onFailure = {
            state.targetFailed()
            if (!cacheOnly) {
                retryOriginal = true
                retry.isVisible = true
            }
        })
    }

    private fun show(file: File, width: Int, height: Int, isPreview: Boolean) {
        if (width > 0 && height > 0) content.isLongImage(width, height)
        renderer.showImage(content.providerRealView(), Uri.fromFile(file), isPreview)
        retry.isVisible = false
        onImageChanged()
    }

    private fun request(
        url: String,
        cacheOnly: Boolean,
        onSuccess: (File, Int, Int) -> Unit,
        onFailure: () -> Unit,
    ) {
        val uri = Uri.parse(url).let { if (it.scheme == null) Uri.fromFile(File(url)) else it }
        imageLoader.loadImage(requestId, uri, cacheOnly, object : ImageFileLoader.Callback {
            override fun onStart() {
                if (!cacheOnly) updateProgress(0)
            }

            override fun onProgress(progress: Int) {
                if (!cacheOnly) updateProgress(progress)
            }

            override fun onSuccess(image: File) {
                imageInfoExecutor.execute {
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    val (width, height) = try {
                        BitmapFactory.decodeFile(image.absolutePath, options)
                        BitmapUtil.getAdjustSize(image.absolutePath, options)
                    } catch (_: RuntimeException) {
                        arrayOf(-1, -1)
                    }
                    handler.post {
                        if (disposed) return@post
                        progress.isVisible = false
                        onSuccess(image, width, height)
                    }
                }
            }

            override fun onFail(error: Exception) {
                handler.post {
                    if (disposed) return@post
                    progress.isVisible = false
                    onFailure()
                }
            }

            private fun updateProgress(percent: Int) {
                handler.post {
                    if (disposed) return@post
                    progress.setProgress(percent)
                    progress.isVisible = true
                }
            }
        })
    }

    private fun dp(value: Int): Int = (resources.displayMetrics.density * value + 0.5f).toInt()

    private companion object {
        val nextRequestId = AtomicInteger()
        val imageInfoExecutor = Executors.newFixedThreadPool(2)
    }
}
