package com.example.c001apk.imageviewer

import android.net.Uri
import java.io.File

interface ImageFileLoader {
    fun loadImage(requestId: Int, uri: Uri, onlyRetrieveFromCache: Boolean, callback: Callback)
    fun cancel(requestId: Int)

    interface Callback {
        fun onStart()
        fun onProgress(progress: Int)
        fun onSuccess(image: File)
        fun onFail(error: Exception)
    }
}
