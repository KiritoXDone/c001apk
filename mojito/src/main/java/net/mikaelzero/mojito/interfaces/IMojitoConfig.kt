package net.mikaelzero.mojito.interfaces

import android.content.Context

/**
 * @Author:         MikaelZero
 * @CreateDate:     2020/6/14 8:54 PM
 * @Description:
 */
interface IMojitoConfig {
    fun wrapPreviewContext(context: Context): Context = context
    fun duration(): Long
    fun maxTransYRatio(): Float
    fun errorDrawableResId(): Int
    fun transparentNavigationBar():Boolean
}
