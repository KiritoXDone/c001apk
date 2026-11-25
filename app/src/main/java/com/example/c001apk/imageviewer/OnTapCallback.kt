package com.example.c001apk.imageviewer

import android.view.View

/**
 * @Author:         MikaelZero
 * @CreateDate:     2020/6/15 10:35 AM
 * @Description:
 */
fun interface OnTapCallback {
    fun onTap(view: View,x:Float,y:Float)
}