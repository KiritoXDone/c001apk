package com.example.c001apk.imageviewer

import android.net.Uri
import android.view.View
import java.io.File

class NativeImageLoadFactory : ImageViewLoadFactory {
    override fun showImage(view: View, uri: Uri, isPreview: Boolean) {
        if (view is NativeImageView) {
            uri.path?.let { view.load(File(it), isPreview) }
        }
    }

    override fun newContentLoader(): NativeImageContentLoader = NativeImageContentLoader()
}
