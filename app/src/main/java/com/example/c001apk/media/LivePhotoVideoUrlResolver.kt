package com.example.c001apk.media

/** Resolves a short-lived video URL for a live photo whose JPEG has no embedded MP4. */
fun interface LivePhotoVideoUrlResolver {
    /** This method is always called from a background thread. */
    fun resolve(imageUrl: String): String?
}
