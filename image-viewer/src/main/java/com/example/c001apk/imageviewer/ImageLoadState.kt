package com.example.c001apk.imageviewer

/** Tracks preview/target callbacks on the UI thread, including out-of-order completions. */
internal class ImageLoadState(private val hasDistinctTarget: Boolean) {
    var targetLoaded = false
        private set

    private var previewLoaded = false
    private var targetLoading = false

    val showTargetAction: Boolean
        get() = hasDistinctTarget && previewLoaded && !targetLoaded

    fun acceptPreview(): Boolean {
        if (targetLoaded) return false
        previewLoaded = true
        if (!hasDistinctTarget) targetLoaded = true
        return true
    }

    fun beginTarget(): Boolean {
        if (!hasDistinctTarget || targetLoading || targetLoaded) return false
        targetLoading = true
        return true
    }

    fun acceptTarget() {
        targetLoading = false
        targetLoaded = true
    }

    fun targetFailed() {
        targetLoading = false
    }
}
