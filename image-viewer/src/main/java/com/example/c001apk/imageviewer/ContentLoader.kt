package com.example.c001apk.imageviewer

import android.content.Context
import android.graphics.RectF
import android.view.View

interface ContentLoader {
    val displayRect: RectF

    fun init(context: Context, originUrl: String, targetUrl: String?)
    fun providerView(): View
    fun providerRealView(): View
    fun onDisplayRectChanged(onChange: () -> Unit) = Unit
    fun ownsVerticalDrag(isDown: Boolean): Boolean
    fun dragging(scale: Float)
    fun beginBackToMin(isResetSize: Boolean)
    fun backToNormal()
    fun loadAnimFinish()
    fun isLongImage(width: Int, height: Int): Boolean
    fun onTapCallback(onTapCallback: OnTapCallback)
    fun onLongTapCallback(onLongTapCallback: OnLongTapCallback)
    fun pageChange(isHidden: Boolean)
    fun dispose() = Unit
}
