package com.example.c001apk.imageviewer

import android.net.Uri
import android.view.View

interface ImageViewLoadFactory {
    fun showImage(view: View, uri: Uri, isPreview: Boolean)
    fun newContentLoader(): ContentLoader
}
