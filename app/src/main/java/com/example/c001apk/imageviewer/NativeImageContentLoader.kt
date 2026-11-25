package com.example.c001apk.imageviewer

import android.content.Context
import android.graphics.RectF

class NativeImageContentLoader : ContentLoader {
    private lateinit var view: NativeImageView

    override val displayRect: RectF get() = view.displayRect

    override fun init(context: Context, originUrl: String, targetUrl: String?) {
        view = NativeImageView(context).apply { setPageVisible(false) }
    }

    override fun providerView(): NativeImageView = view

    override fun providerRealView(): NativeImageView = view

    override fun onDisplayRectChanged(onChange: () -> Unit) {
        view.setDisplayRectListener(onChange)
    }

    override fun ownsVerticalDrag(isDown: Boolean): Boolean = view.ownsVerticalDrag(isDown)

    override fun dragging(scale: Float) = Unit

    override fun beginBackToMin(isResetSize: Boolean) {
        view.freezeForClose()
        if (isResetSize && !view.isReadMode) view.zoomTo(view.fitScale)
    }

    override fun backToNormal() {
        view.setTilingEnabled(true)
    }

    override fun loadAnimFinish() {
        view.setTilingEnabled(true)
    }

    override fun isLongImage(width: Int, height: Int): Boolean {
        val windowWidth = view.width.takeIf { it > 0 } ?: view.resources.displayMetrics.widthPixels
        val useReadingMode = height > width * 40f / 9f || (width > height * 8f && width > windowWidth * 2f)
        view.isReadMode = useReadingMode
        return useReadingMode
    }

    override fun onTapCallback(onTapCallback: OnTapCallback) {
        view.tapCallback = onTapCallback
    }

    override fun onLongTapCallback(onLongTapCallback: OnLongTapCallback) {
        view.longTapCallback = onLongTapCallback
    }

    override fun pageChange(isHidden: Boolean) {
        view.setPageVisible(!isHidden)
    }

    override fun dispose() {
        view.dispose()
    }
}
