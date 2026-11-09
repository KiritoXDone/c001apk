package com.example.c001apk.compose.util

import android.graphics.drawable.Drawable
import android.widget.ImageView
import coil.load
import coil.request.ImageRequest
import com.example.c001apk.compose.BuildConfig
import com.example.c001apk.compose.R
import com.example.c001apk.compose.view.ImageFailureDrawable

private class ImageBinding(val key: Any, var open: () -> Unit) {
    var failed = false
    var retry: () -> Unit = {}
}

/** Recomposition must not cancel and enqueue an unchanged image request. */
fun ImageView.bindRecoverableImage(
    url: String,
    key: Any,
    onOpen: () -> Unit,
    onSuccess: (Drawable) -> Unit = {},
    configure: ImageRequest.Builder.() -> Unit = {},
) {
    val previous = getTag(R.id.image_request_binding) as? ImageBinding
    if (previous?.key == key) {
        previous.open = onOpen
        return
    }
    val binding = ImageBinding(key, onOpen)
    setTag(R.id.image_request_binding, binding)
    binding.retry = {
        binding.failed = false
        contentDescription = "正在加载图片"
        load(url) {
            configure()
            error(ImageFailureDrawable(context))
            fallback(ImageFailureDrawable(context))
            listener(
                onSuccess = { _, result ->
                    contentDescription = "查看图片"
                    onSuccess(result.drawable)
                },
                onError = { _, result ->
                    binding.failed = true
                    contentDescription = "图片加载失败，点按重试"
                    logImageFailure(result.throwable)
                },
            )
        }
    }
    setOnClickListener { if (binding.failed) binding.retry() else binding.open() }
    binding.retry()
}

fun logImageFailure(error: Throwable) {
    if (!BuildConfig.DEBUG) return
    val reason =
        when (error) {
            is coil.network.HttpException -> "HTTP ${error.response.code}"
            else -> error.javaClass.simpleName
        }
    android.util.Log.d("ImageLoad", "Image request failed: $reason")
}
