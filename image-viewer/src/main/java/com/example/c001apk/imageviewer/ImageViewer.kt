package com.example.c001apk.imageviewer

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference
import java.util.UUID

object ImageViewer {
    private val requests = mutableMapOf<String, Request>()
    private lateinit var imageLoader: ImageFileLoader
    private var wrapContext: (Context) -> Context = { it }

    var currentActivity: WeakReference<ImageViewerActivity>? = null
        internal set

    fun initialize(imageLoader: ImageFileLoader, wrapContext: (Context) -> Context) {
        this.imageLoader = imageLoader
        this.wrapContext = wrapContext
    }

    fun open(context: Context, request: Request) {
        if (request.images.isEmpty()) return
        val activity = context.findActivity() ?: return
        if (activity.isFinishing || activity.isDestroyed) return
        val key = UUID.randomUUID().toString()
        requests[key] = request
        val intent = Intent(activity, ImageViewerActivity::class.java).putExtra(EXTRA_REQUEST, key)
        try {
            activity.startActivity(intent)
        } catch (error: RuntimeException) {
            requests.remove(key)
            throw error
        }
        @Suppress("DEPRECATION")
        activity.overridePendingTransition(0, 0)
    }

    internal fun takeRequest(key: String?): Request? = requests.remove(key)

    internal fun imageLoader(): ImageFileLoader = imageLoader

    internal fun wrap(context: Context): Context = wrapContext(context)

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    data class Image(
        val previewUrl: String,
        val originalUrl: String? = null,
    )

    class Request(
        val images: List<Image>,
        val rendererAt: (Int) -> ImageViewLoadFactory,
        @StringRes val retryLabel: Int,
        val startingPage: Int = 0,
        val autoLoadOriginal: Boolean = true,
        val enableHdr: Boolean = false,
        val thumbnailAt: (Int) -> ImageView? = { null },
        val onPageSelected: (Int) -> Unit = {},
        val onLongClick: (FragmentActivity, Int, (() -> Unit)?) -> Unit = { _, _, _ -> },
        val onOpened: () -> Unit = {},
        val onClosed: () -> Unit = {},
    )

    internal const val EXTRA_REQUEST = "image_viewer_request"
}
