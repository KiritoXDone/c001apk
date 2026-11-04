package com.example.c001apk.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.PlaybackException
import net.mikaelzero.mojito.interfaces.OnMojitoViewCallback
import net.mikaelzero.mojito.loader.ContentLoader
import net.mikaelzero.mojito.loader.OnLongTapCallback
import net.mikaelzero.mojito.loader.OnTapCallback
import net.mikaelzero.mojito.view.sketch.core.SketchImageView
import net.mikaelzero.mojito.view.sketch.core.zoom.ImageZoomer
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.concurrent.Executors

internal class MediaContentLoader(
    private val delegate: ContentLoader,
    private val playbackSession: MojitoMediaPlaybackSession,
    private val imageUrl: String,
    private val videoUrlResolver: LivePhotoVideoUrlResolver?,
    private val mediaHint: RichMediaHint,
    override val pagePosition: Int,
) : ContentLoader, DefaultLifecycleObserver, PlaybackTarget {

    private lateinit var context: Context
    private lateinit var root: FrameLayout
    private lateinit var textureView: TextureView
    private lateinit var exitPreviewView: ImageView
    private lateinit var hdrBadge: TextView
    private lateinit var livePhotoButton: TextView
    private lateinit var audioButton: ImageButton
    private lateinit var livePhotoMenu: LinearLayout
    private lateinit var primaryMenuAction: TextView
    private val executor = Executors.newSingleThreadExecutor()
    private var extractedVideo: File? = null
    private var lifecycleVisible = false
    private var pageSelected = false
    private var pendingPlayRequest = false
    private lateinit var soundPreferences: LivePhotoSoundPreferences
    private var muted: Boolean
        get() = soundPreferences.muted
        set(value) { soundPreferences.muted = value }
    private var livePhotoEnabled = true
    private var playing = false
    private var playbackFinished = false
    private var hasUltraHdr = false
    private var liveControlsRequested = false
    private var loadAnimationFinished = false
    private var videoWidth = 0
    private var videoHeight = 0
    private var pixelWidthHeightRatio = 1f
    private var bindGeneration = 0
    private var disposed = false
    private var boundImage: File? = null
    private var boundMediaInfo: EmbeddedMediaInfo? = null
    private var imageZoomer: ImageZoomer? = null
    private var exitTransitionActive = false
    private var exitTransitionGeneration = 0
    private var exitPreviewGeneration = 0
    private var exitPreviewReady = false
    private var exitPreviewBitmap: Bitmap? = null
    private var usingExitPreview = false
    private val matrixChangeListener = ImageZoomer.OnMatrixChangeListener {
        positionVideoLayer()
        positionControls()
    }

    override val displayRect: RectF
        get() = delegate.displayRect

    override fun init(
        context: Context,
        originUrl: String,
        targetUrl: String?,
        onMojitoViewCallback: OnMojitoViewCallback?,
    ) {
        this.context = context
        soundPreferences = LivePhotoSoundPreferences(context)
        playbackSession.attach(context)
        playbackSession.register(this)
        delegate.init(context, originUrl, targetUrl, onMojitoViewCallback)
        logState("init")

        root = FrameLayout(context).apply {
            addView(
                delegate.providerView(),
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                ),
            )
        }
        textureView = TextureView(context).apply {
            visibility = View.GONE
            alpha = 0f
            isClickable = false
            isOpaque = false
            addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                updateVideoTransform()
            }
        }
        root.addView(
            textureView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        exitPreviewView = ImageView(context).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            visibility = View.GONE
            isClickable = false
        }
        root.addView(
            exitPreviewView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )

        hdrBadge = TextView(context).apply {
            text = "HDR"
            setTextColor(Color.WHITE)
            textSize = 12f
            gravity = Gravity.CENTER
            typeface = Typeface.create("sans-serif-condensed", Typeface.NORMAL)
            setPadding(4.dp(context), 1.dp(context), 4.dp(context), 1.dp(context))
            minHeight = 20.dp(context)
            background = GradientDrawable().apply {
                setColor(Color.argb(110, 0, 0, 0))
                cornerRadius = 4.dp(context).toFloat()
            }
            visibility = View.GONE
        }

        livePhotoButton = TextView(context).apply {
            minWidth = 55.dp(context)
            minHeight = 24.dp(context)
            background = ContextCompat.getDrawable(context, R.drawable.ic_live_photo_on_big)
            visibility = View.GONE
            setOnClickListener { toggleLivePhotoMenu() }
        }
        audioButton = ImageButton(context).apply {
            imageTintList = null
            background = null
            setPadding(0, 0, 0, 0)
            visibility = View.GONE
            setOnClickListener {
                muted = !muted
                playbackSession.setMuted(this@MediaContentLoader, muted)
                updateAudioButton()
            }
        }
        updateAudioButton()
        primaryMenuAction = createMenuAction {
            setLivePhotoEnabled(!livePhotoEnabled)
        }
        val replayMenuAction = createMenuAction {
            setLivePhotoMenuVisible(false)
            pendingPlayRequest = true
            startPlayback()
        }.apply {
            text = "重新播放"
            setTextViewIcon(R.drawable.ic_livephoto_replay)
        }
        livePhotoMenu = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            elevation = 8.dp(context).toFloat()
            setBackgroundResource(R.drawable.bg_live_photo_menu)
            addView(
                primaryMenuAction,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ),
            )
            addView(
                View(context).apply {
                    setBackgroundColor(
                        ContextCompat.getColor(context, R.color.live_photo_menu_divider_color),
                    )
                },
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1.dp(context)),
            )
            addView(
                replayMenuAction,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ),
            )
        }
        updateLivePhotoControls()
        root.addView(
            hdrBadge,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ),
        )
        root.addView(
            livePhotoButton,
            FrameLayout.LayoutParams(
                55.dp(context),
                24.dp(context),
            ),
        )
        root.addView(
            audioButton,
            FrameLayout.LayoutParams(24.dp(context), 24.dp(context)),
        )
        root.addView(
            livePhotoMenu,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ),
        )
        applyMediaHint()
        root.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            positionControls()
            positionVideoLayer()
            updateVideoTransform()
        }

        imageZoomer = (delegate.providerRealView() as? SketchImageView)?.zoomer
        imageZoomer?.addOnMatrixChangeListener(matrixChangeListener)
        (context as? LifecycleOwner)?.lifecycle?.addObserver(this)
    }

    override fun providerView(): View = root

    override fun providerRealView(): View = delegate.providerRealView()

    fun bindExitPreview(file: File) {
        val generation = ++exitPreviewGeneration
        executor.execute {
            val bitmap = decodeExitPreview(file)?.also(Bitmap::prepareToDraw)
            root.post {
                if (disposed || generation != exitPreviewGeneration) {
                    bitmap?.recycle()
                    return@post
                }
                val previousBitmap = exitPreviewBitmap
                exitPreviewBitmap = bitmap
                exitPreviewView.setImageBitmap(bitmap)
                exitPreviewReady = bitmap != null
                if (previousBitmap !== bitmap) previousBitmap?.recycle()
            }
        }
    }

    fun bind(file: File, info: EmbeddedMediaInfo) {
        boundImage = file
        boundMediaInfo = info
        val isLivePhoto = mediaHint == RichMediaHint.LIVE_PHOTO
        hasUltraHdr = mediaHint == RichMediaHint.ULTRA_HDR
        setControlsVisible(isLivePhoto)
        positionControls()
        if (hasUltraHdr) pauseSketchBlockRendering()
        if (!isLivePhoto) return
        val generation = ++bindGeneration
        logState(
            "bind embedded=${info.embeddedVideoLength} declared=${info.declaredVideoLength}",
        )
        executor.execute {
            val video = info.embeddedVideoLength?.let { extractVideo(file, it) }
                ?: resolveExternalVideo()
            root.post {
                if (generation == bindGeneration && video != null) {
                    extractedVideo = video
                    logState("video ready")
                    setControlsVisible(true)
                    positionControls()
                    if (livePhotoEnabled && canPlay()) {
                        startPlayback()
                    }
                }
            }
        }
    }

    val hasLivePhoto: Boolean
        get() = boundMediaInfo?.hasMotionPhoto == true

    fun exportLivePhoto(
        destination: File,
        onComplete: (Result<File>) -> Unit,
    ): Boolean {
        val source = boundImage ?: return false
        val info = boundMediaInfo?.takeIf { it.hasMotionPhoto } ?: return false
        if (disposed) return false
        return runCatching {
            executor.execute {
                val result = runCatching {
                    val video = if (info.embeddedVideoLength != null) {
                        null
                    } else {
                        extractedVideo ?: cachedExternalVideo() ?: resolveExternalVideo()
                    }
                    MotionPhotoExporter.export(
                        sourceImage = source,
                        video = video,
                        destination = destination,
                        presentationTimestampUs = info.presentationTimestampUs,
                    )
                }
                root.post { onComplete(result) }
            }
        }.isSuccess
    }

    private fun extractVideo(source: File, videoLength: Long): File? = runCatching {
        val directory = File(context.cacheDir, "motion-photo").apply { mkdirs() }
        val target = File(directory, "${source.nameWithoutExtension}-${source.length()}-$videoLength.mp4")
        if (target.isFile && target.length() == videoLength) return@runCatching target

        val temporary = File(directory, "${target.name}.part")
        RandomAccessFile(source, "r").use { input ->
            input.seek(source.length() - videoLength)
            FileOutputStream(temporary).use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var remaining = videoLength
                while (remaining > 0L) {
                    val read = input.read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
                    if (read < 0) break
                    output.write(buffer, 0, read)
                    remaining -= read
                }
                check(remaining == 0L)
            }
        }
        if (target.exists()) target.delete()
        check(temporary.renameTo(target))
        target
    }.onFailure {
        Log.w(TAG, "Unable to extract embedded live photo video", it)
    }.getOrNull()

    private fun resolveExternalVideo(): File? {
        cachedExternalVideo()?.let { return it }
        val resolver = videoUrlResolver ?: return null
        val resolvedUrl = runCatching { resolver.resolve(imageUrl) }
            .onFailure { Log.w(TAG, "Unable to resolve external live photo video", it) }
            .getOrNull()
            ?.takeIf { it.startsWith("https://") || it.startsWith("http://") }
            ?: return null
        return downloadExternalVideo(resolvedUrl)
    }

    private fun downloadExternalVideo(videoUrl: String): File? = runCatching {
        val directory = File(context.cacheDir, "live-photo-video").apply { mkdirs() }
        val target = File(directory, externalVideoFileName())
        if (target.isFile && isValidMp4(target)) return@runCatching target
        if (target.exists()) target.delete()

        val temporary = File(directory, "${target.name}.part")
        if (temporary.exists()) temporary.delete()
        val connection = URL(videoUrl).openConnection() as HttpURLConnection
        try {
            connection.instanceFollowRedirects = true
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000
            connection.setRequestProperty("Accept", "video/mp4,application/octet-stream")
            val responseCode = connection.responseCode
            check(responseCode in 200..299) { "HTTP $responseCode" }
            val contentType = connection.contentType?.substringBefore(';')?.lowercase()
            check(contentType == null || contentType.startsWith("video/") ||
                contentType == "application/octet-stream"
            ) { "Unexpected content type: $contentType" }
            val responseLength = connection.contentLengthLong
            check(responseLength <= MAX_EXTERNAL_VIDEO_BYTES || responseLength < 0L) {
                "External live photo video is too large: $responseLength"
            }
            connection.inputStream.use { input ->
                FileOutputStream(temporary).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var total = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        check(total <= MAX_EXTERNAL_VIDEO_BYTES) {
                            "External live photo video exceeds size limit"
                        }
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }
            check(isValidMp4(temporary)) { "Downloaded file is not an MP4" }
            check(temporary.renameTo(target)) { "Unable to persist live photo video" }
            target
        } finally {
            connection.disconnect()
            if (temporary.exists()) temporary.delete()
        }
    }.onFailure {
        Log.w(TAG, "Unable to download external live photo video", it)
    }.getOrNull()

    private fun cachedExternalVideo(): File? {
        val target = File(File(context.cacheDir, "live-photo-video"), externalVideoFileName())
        return target.takeIf { isValidMp4(it) }
    }

    private fun externalVideoFileName(): String = "${sha256(imageUrl)}.mp4"

    private fun isValidMp4(file: File): Boolean {
        if (!file.isFile || file.length() < 12L) return false
        return runCatching {
            RandomAccessFile(file, "r").use { input ->
                input.seek(4L)
                val type = ByteArray(4)
                input.readFully(type)
                String(type, StandardCharsets.US_ASCII) == "ftyp"
            }
        }.getOrDefault(false)
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8))
        .joinToString(separator = "") { "%02x".format(it) }

    private fun createMenuAction(onClick: () -> Unit): TextView = TextView(context).apply {
        setTextColor(ContextCompat.getColor(context, R.color.live_photo_menu_text_color))
        textSize = 13f
        gravity = Gravity.CENTER_VERTICAL
        typeface = Typeface.DEFAULT
        compoundDrawablePadding = 3.dp(context)
        setPadding(12.dp(context), 12.dp(context), 12.dp(context), 12.dp(context))
        TypedValue().also { value ->
            if (context.theme.resolveAttribute(
                    android.R.attr.selectableItemBackground,
                    value,
                    true,
                )
            ) {
                setBackgroundResource(value.resourceId)
            }
        }
        setOnClickListener { onClick() }
    }

    private fun TextView.setTextViewIcon(
        @DrawableRes iconRes: Int,
        size: Int = 18.dp(context),
    ) {
        val icon = ContextCompat.getDrawable(context, iconRes)?.mutate()?.apply {
            setBounds(0, 0, size, size)
            setTint(this@setTextViewIcon.currentTextColor)
        }
        setCompoundDrawablesRelative(icon, null, null, null)
    }

    private fun updateLivePhotoControls() {
        livePhotoButton.text = null
        livePhotoButton.setCompoundDrawablesRelative(null, null, null, null)
        livePhotoButton.setBackgroundResource(
            if (livePhotoEnabled) R.drawable.ic_live_photo_on_big
            else R.drawable.ic_live_photo_off_big,
        )
        primaryMenuAction.text = if (livePhotoEnabled) "关闭实况" else "开启实况"
        primaryMenuAction.setTextViewIcon(
            if (livePhotoEnabled) R.drawable.ic_live_photo_off2 else R.drawable.ic_live_photo2,
        )
        if (liveControlsRequested) positionControls() else audioButton.visibility = View.GONE
    }

    private fun toggleLivePhotoMenu() {
        setLivePhotoMenuVisible(livePhotoMenu.visibility != View.VISIBLE)
    }

    private fun setLivePhotoMenuVisible(visible: Boolean) {
        if (!::livePhotoMenu.isInitialized) return
        livePhotoMenu.animate().cancel()
        livePhotoMenu.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible) {
            updateLivePhotoControls()
            positionControls()
            livePhotoMenu.pivotX = livePhotoButton.x + livePhotoButton.width / 2f -
                livePhotoMenu.x
            livePhotoMenu.pivotY = livePhotoButton.y + livePhotoButton.height / 2f -
                livePhotoMenu.y
            livePhotoMenu.alpha = 0f
            livePhotoMenu.scaleX = MENU_INITIAL_SCALE
            livePhotoMenu.scaleY = MENU_INITIAL_SCALE
            livePhotoMenu.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(MENU_ANIMATION_DURATION_MS)
                .setInterpolator(LAYER_CROSSFADE_INTERPOLATOR)
                .start()
        } else {
            livePhotoMenu.alpha = 1f
            livePhotoMenu.scaleX = 1f
            livePhotoMenu.scaleY = 1f
        }
    }

    private fun setLivePhotoEnabled(enabled: Boolean) {
        setLivePhotoMenuVisible(false)
        if (livePhotoEnabled == enabled) return
        livePhotoEnabled = enabled
        updateLivePhotoControls()
        if (enabled) {
            pendingPlayRequest = true
            startPlayback()
        } else {
            pendingPlayRequest = false
            playbackSession.stop(this)
            showStaticLayer()
        }
    }

    private fun startPlayback() {
        val video = extractedVideo ?: run {
            logState("play waiting for extract")
            return
        }
        if (!canPlay()) {
            logState("play ignored")
            return
        }
        pendingPlayRequest = false
        playing = false
        playbackFinished = false
        positionVideoLayer()
        cancelLayerAnimations()
        delegate.providerView().alpha = 1f
        delegate.providerRealView().alpha = 1f
        textureView.visibility = View.VISIBLE
        textureView.alpha = 0f
        logState("play start")
        updateAudioButton()
        playbackSession.play(context, this, textureView, video, muted)
    }

    private fun canPlay(): Boolean =
        lifecycleVisible && pageSelected && loadAnimationFinished

    override fun onVideoFrameAvailable() {
        if (playbackFinished) return
        if (!lifecycleVisible || !pageSelected) {
            playbackSession.stop(this)
            return
        }
        playing = true
        root.setBackgroundColor(Color.BLACK)
        textureView.animate()
            .alpha(1f)
            .setDuration(LAYER_CROSSFADE_DURATION_MS)
            .setInterpolator(LAYER_CROSSFADE_INTERPOLATOR)
            .start()
    }

    override fun onVideoSizeChanged(width: Int, height: Int, pixelWidthHeightRatio: Float) {
        videoWidth = width
        videoHeight = height
        this.pixelWidthHeightRatio = pixelWidthHeightRatio
        updateVideoTransform()
    }

    override fun onPlaybackEnded() {
        playbackFinished = true
        pendingPlayRequest = false
        playing = false
        logState("play ended")
        fadeToStaticLayer()
    }

    override fun onPlaybackFailed(error: PlaybackException) {
        Log.w(TAG, "Live photo playback failed", error)
        showStaticLayer()
    }

    override fun onPlaybackDetached() = showStaticLayer()

    private fun showStaticLayer() {
        playing = false
        if (!::textureView.isInitialized) return
        cancelLayerAnimations()
        delegate.providerView().alpha = 1f
        delegate.providerRealView().alpha = 1f
        textureView.alpha = 0f
        textureView.visibility = View.GONE
        root.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun fadeToStaticLayer(clearVideoSurface: Boolean = true) {
        if (!::textureView.isInitialized) return
        cancelLayerAnimations()
        delegate.providerView().alpha = 1f
        delegate.providerRealView().alpha = 1f
        textureView.animate()
            .alpha(0f)
            .setDuration(LAYER_CROSSFADE_DURATION_MS)
            .setInterpolator(LAYER_CROSSFADE_INTERPOLATOR)
            .withEndAction {
                if (playbackFinished && !playing) {
                    if (clearVideoSurface) {
                        textureView.visibility = View.GONE
                        playbackSession.clearVideoSurface(this)
                    }
                    root.setBackgroundColor(Color.TRANSPARENT)
                }
            }
            .start()
    }

    private fun showExitPreview(): Boolean {
        if (!exitPreviewReady) return false
        cancelLayerAnimations()
        exitPreviewView.scaleType = ImageView.ScaleType.FIT_CENTER
        exitPreviewView.visibility = View.VISIBLE
        delegate.providerView().visibility = View.GONE
        textureView.alpha = 0f
        root.setBackgroundColor(Color.TRANSPARENT)
        usingExitPreview = true
        return true
    }

    private fun hideExitPreview() {
        if (!::exitPreviewView.isInitialized) return
        usingExitPreview = false
        exitPreviewView.visibility = View.GONE
        delegate.providerView().visibility = View.VISIBLE
    }

    private fun cancelLayerAnimations() {
        delegate.providerView().animate().cancel()
        textureView.animate().cancel()
    }

    private fun updateVideoTransform() {
        if (!::textureView.isInitialized) return
        val viewWidth = textureView.width.toFloat()
        val viewHeight = textureView.height.toFloat()
        val sourceWidth = videoWidth * pixelWidthHeightRatio
        val sourceHeight = videoHeight.toFloat()
        if (viewWidth <= 0f || viewHeight <= 0f || sourceWidth <= 0f || sourceHeight <= 0f) return

        val fitScale = minOf(viewWidth / sourceWidth, viewHeight / sourceHeight)
        val scaleX = sourceWidth * fitScale / viewWidth
        val scaleY = sourceHeight * fitScale / viewHeight
        textureView.setTransform(
            Matrix().apply {
                setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
            },
        )
    }

    private fun positionVideoLayer() {
        if (!::textureView.isInitialized) return
        val rect = delegate.displayRect
        if (rect.isEmpty) return
        val width = rect.width().toInt().coerceAtLeast(1)
        val height = rect.height().toInt().coerceAtLeast(1)
        val params = textureView.layoutParams as FrameLayout.LayoutParams
        if (params.width != width || params.height != height) {
            params.width = width
            params.height = height
            textureView.layoutParams = params
        }
        textureView.x = rect.left
        textureView.y = rect.top
    }

    private fun setControlsVisible(visible: Boolean) {
        if (!::livePhotoButton.isInitialized) return
        liveControlsRequested = visible
        if (!visible) {
            livePhotoButton.visibility = View.GONE
            audioButton.visibility = View.GONE
            setLivePhotoMenuVisible(false)
            return
        }
        livePhotoButton.visibility = View.INVISIBLE
        audioButton.visibility = View.INVISIBLE
        positionControls()
    }

    private fun applyMediaHint() {
        val isLivePhoto = mediaHint == RichMediaHint.LIVE_PHOTO
        hasUltraHdr = mediaHint == RichMediaHint.ULTRA_HDR
        hdrBadge.visibility = if (hasUltraHdr && !isLivePhoto) View.INVISIBLE else View.GONE
        setControlsVisible(isLivePhoto)
        positionControls()
    }

    private fun positionControls() {
        if (!::livePhotoButton.isInitialized) return
        val rect = delegate.displayRect
        if (rect.isEmpty) {
            hdrBadge.visibility = if (hasUltraHdr) View.INVISIBLE else View.GONE
            livePhotoButton.visibility = if (liveControlsRequested) View.INVISIBLE else View.GONE
            audioButton.visibility = if (liveControlsRequested && livePhotoEnabled) {
                View.INVISIBLE
            } else {
                View.GONE
            }
            return
        }
        if (hasUltraHdr) {
            hdrBadge.x = rect.left + 12.dp(context)
            hdrBadge.y = rect.top + 12.dp(context)
            hdrBadge.visibility = View.VISIBLE
        } else {
            hdrBadge.visibility = View.GONE
        }
        if (!liveControlsRequested) {
            livePhotoButton.visibility = View.GONE
            audioButton.visibility = View.GONE
            return
        }
        livePhotoButton.x = rect.left + 14.dp(context)
        livePhotoButton.y = rect.top + 8.dp(context)
        livePhotoMenu.x = rect.left + 10.dp(context)
        livePhotoMenu.y = livePhotoButton.y +
            livePhotoButton.height.coerceAtLeast(24.dp(context)) + 4.dp(context)
        audioButton.x = rect.right - audioButton.width - 12.dp(context)
        audioButton.y = rect.bottom - audioButton.height - 10.dp(context)
        livePhotoButton.visibility = View.VISIBLE
        audioButton.visibility = if (livePhotoEnabled) View.VISIBLE else View.GONE
    }

    private fun updateExitTransition(progress: Float) {
        val fraction = progress.coerceIn(0f, 1f)
        if (fraction > 0f && !exitTransitionActive) {
            exitTransitionActive = true
            setLivePhotoMenuVisible(false)
        }
        if (!exitTransitionActive) return
        cancelControlAnimations()
        livePhotoButton.alpha = 1f - fraction
        audioButton.alpha = 1f - fraction
        hdrBadge.alpha = 1f - fraction
    }

    private fun fadeControlsForExit() {
        exitTransitionActive = true
        setLivePhotoMenuVisible(false)
        listOf(livePhotoButton, audioButton, hdrBadge).forEach { control ->
            control.animate()
                .alpha(0f)
                .setDuration(EXIT_CONTROL_FADE_DURATION_MS)
                .setInterpolator(LAYER_CROSSFADE_INTERPOLATOR)
                .start()
        }
    }

    private fun cancelControlAnimations() {
        livePhotoButton.animate().cancel()
        audioButton.animate().cancel()
        hdrBadge.animate().cancel()
    }

    private fun restoreControlsAfterExit() {
        exitTransitionGeneration++
        exitTransitionActive = false
        cancelControlAnimations()
        livePhotoButton.alpha = 1f
        audioButton.alpha = 1f
        hdrBadge.alpha = 1f
        positionControls()
    }

    private fun restoreControlsWhenDragSettles() {
        restoreControlsWhenDragSettles(++exitTransitionGeneration)
    }

    private fun restoreControlsWhenDragSettles(generation: Int) {
        if (disposed || generation != exitTransitionGeneration) return
        val parentScale = (root.parent as? View)?.scaleX ?: 1f
        if (parentScale >= 0.995f) {
            restoreControlsAfterExit()
        } else {
            root.postOnAnimation { restoreControlsWhenDragSettles(generation) }
        }
    }

    private fun updateAudioButton() {
        audioButton.setImageResource(
            if (muted) R.drawable.ic_sound_mute else R.drawable.ic_sound,
        )
        audioButton.imageTintList = null
        audioButton.contentDescription = if (muted) "打开实况声音" else "关闭实况声音"
    }

    private fun pauseSketchBlockRendering() {
        (delegate.providerRealView() as? SketchImageView)
            ?.zoomer
            ?.blockDisplayer
            ?.setPause(true)
    }

    override fun dispatchTouchEvent(
        isDrag: Boolean,
        isActionUp: Boolean,
        isDown: Boolean,
        isHorizontal: Boolean,
    ): Boolean = delegate.dispatchTouchEvent(isDrag, isActionUp, isDown, isHorizontal)

    override fun dragging(width: Int, height: Int, ratio: Float) {
        delegate.dragging(width, height, ratio)
        val parentScale = (root.parent as? View)?.scaleX ?: 1f
        val progress = ((1f - parentScale) / EXIT_CONTROL_FADE_SCALE_RANGE).coerceIn(0f, 1f)
        updateExitTransition(progress)
    }

    override fun beginBackToMin(isResetSize: Boolean) {
        if (!isResetSize) {
            playbackSession.pause(this)
            pendingPlayRequest = false
            playbackFinished = true
            playing = false
            if (!showExitPreview()) {
                fadeToStaticLayer(clearVideoSurface = false)
            }
            exitTransitionGeneration++
            fadeControlsForExit()
        } else if (exitPreviewReady) {
            exitPreviewView.scaleType = ImageView.ScaleType.CENTER_CROP
        }
        if (!usingExitPreview) {
            delegate.beginBackToMin(isResetSize)
        }
    }

    override fun backToNormal() {
        delegate.backToNormal()
        restoreControlsWhenDragSettles()
    }

    override fun loadAnimFinish() {
        delegate.loadAnimFinish()
        loadAnimationFinished = true
        if (hasUltraHdr) pauseSketchBlockRendering()
        positionControls()
        if (livePhotoEnabled && extractedVideo != null && canPlay()) startPlayback()
    }

    override fun needReBuildSize(): Boolean = delegate.needReBuildSize()

    override fun useTransitionApi(): Boolean = delegate.useTransitionApi()

    override fun isLongImage(width: Int, height: Int): Boolean = delegate.isLongImage(width, height)

    override fun onTapCallback(onTapCallback: OnTapCallback) = delegate.onTapCallback(onTapCallback)

    override fun onLongTapCallback(onLongTapCallback: OnLongTapCallback) =
        delegate.onLongTapCallback(onLongTapCallback)

    override fun pageChange(isHidden: Boolean) {
        lifecycleVisible = !isHidden
        delegate.pageChange(isHidden)
        logState("lifecycle visible=${!isHidden}")
        if (isHidden) {
            setLivePhotoMenuVisible(false)
            playbackSession.stop(this)
            showStaticLayer()
        } else {
            hideExitPreview()
            if (livePhotoEnabled && extractedVideo != null && canPlay()) {
                setControlsVisible(true)
                positionControls()
                startPlayback()
            }
        }
    }

    override fun onPageSelectionChanged(selected: Boolean) {
        pageSelected = selected
        if (selected && ::audioButton.isInitialized) updateAudioButton()
        logState("selected=$selected")
        if (!selected) {
            setLivePhotoMenuVisible(false)
            playbackSession.stop(this)
            showStaticLayer()
        } else if (livePhotoEnabled && extractedVideo != null && canPlay()) {
            setControlsVisible(true)
            positionControls()
            startPlayback()
        }
    }

    internal fun dispose() {
        if (disposed) return
        disposed = true
        bindGeneration++
        exitPreviewGeneration++
        playbackSession.unregister(this)
        imageZoomer?.removeOnMatrixChangeListener(matrixChangeListener)
        imageZoomer = null
        if (::exitPreviewView.isInitialized) {
            exitPreviewView.setImageDrawable(null)
        }
        exitPreviewBitmap?.recycle()
        exitPreviewBitmap = null
        executor.shutdownNow()
        (context as? LifecycleOwner)?.lifecycle?.removeObserver(this)
    }

    override fun onDestroy(owner: LifecycleOwner) = dispose()

    private fun Int.dp(context: Context): Int =
        (this * context.resources.displayMetrics.density + 0.5f).toInt()

    private fun decodeExitPreview(file: File): Bitmap? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        check(bounds.outWidth > 0 && bounds.outHeight > 0)

        val metrics = context.resources.displayMetrics
        val fitScale = minOf(
            metrics.widthPixels / bounds.outWidth.toFloat(),
            metrics.heightPixels / bounds.outHeight.toFloat(),
            1f,
        )
        val targetWidth = (bounds.outWidth * fitScale).toInt().coerceAtLeast(1)
        val targetHeight = (bounds.outHeight * fitScale).toInt().coerceAtLeast(1)
        var sampleSize = 1
        while (bounds.outWidth / (sampleSize * 2) >= targetWidth &&
            bounds.outHeight / (sampleSize * 2) >= targetHeight
        ) {
            sampleSize *= 2
        }
        val decoded = checkNotNull(BitmapFactory.decodeFile(
            file.absolutePath,
            BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inMutable = true
            },
        ))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && decoded.hasGainmap()) {
            decoded.gainmap = null
        }
        if (decoded.width == targetWidth && decoded.height == targetHeight) {
            decoded
        } else {
            Bitmap.createScaledBitmap(decoded, targetWidth, targetHeight, true).also {
                decoded.recycle()
            }
        }
    }.onFailure {
        Log.w(TAG, "Unable to decode exit preview ${file.name}", it)
    }.getOrNull()

    private fun logState(event: String) {
        if (!BuildConfig.DEBUG) return
        Log.d(
            TAG,
            "page=$pagePosition $event selected=$pageSelected visible=$lifecycleVisible " +
                "loaded=$loadAnimationFinished extracted=${extractedVideo != null} " +
                "playing=$playing pending=$pendingPlayRequest",
        )
    }

    private companion object {
        const val TAG = "MojitoMedia"
        const val MAX_EXTERNAL_VIDEO_BYTES = 128L * 1024 * 1024
        const val LAYER_CROSSFADE_DURATION_MS = 250L
        const val MENU_ANIMATION_DURATION_MS = 160L
        const val MENU_INITIAL_SCALE = 0.88f
        const val EXIT_CONTROL_FADE_DURATION_MS = 120L
        const val EXIT_CONTROL_FADE_SCALE_RANGE = 0.08f
        val LAYER_CROSSFADE_INTERPOLATOR = PathInterpolator(0.4f, 0f, 0.2f, 1f)
    }
}
