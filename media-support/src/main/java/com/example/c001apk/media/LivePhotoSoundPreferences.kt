package com.example.c001apk.media

import android.content.Context

// 仅保存用户主动选择的实况声音偏好，不修改系统媒体音量。
internal class LivePhotoSoundPreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("live_photo_playback", Context.MODE_PRIVATE)

    var muted: Boolean
        get() = preferences.getBoolean("muted", true)
        set(value) { preferences.edit().putBoolean("muted", value).apply() }
}
