package com.example.c001apk.compose.util

import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.settings.AppLanguage
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/** Permission is requested only for an explicit gallery save on Android 7–9. */
internal object GalleryPermission {
    private val pending = ConcurrentHashMap<String, () -> Unit>()

    fun granted(context: Context): Boolean =
        Build.VERSION.SDK_INT >= 29 ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ) == PackageManager.PERMISSION_GRANTED

    fun request(context: Context, onGranted: () -> Unit) {
        if (granted(context)) {
            onGranted()
            return
        }
        val key = UUID.randomUUID().toString()
        pending[key] = onGranted
        try {
            context.startActivity(
                Intent(context, GalleryPermissionActivity::class.java)
                    .putExtra("request", key)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        } catch (error: Exception) {
            pending.remove(key)
            context.makeToast(context.getString(R.string.image_permission_request_failed))
        }
    }

    fun contains(key: String?) = key != null && pending.containsKey(key)

    fun finish(key: String?, granted: Boolean) {
        val action = key?.let(pending::remove)
        if (granted) action?.invoke()
    }
}

/** No content UI; the system permission dialog appears over the current image. */
class GalleryPermissionActivity : ComponentActivity() {
    private val permission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            GalleryPermission.finish(intent.getStringExtra("request"), it)
            if (!it) makeToast(getString(R.string.image_permission_denied))
            finish()
        }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLanguage.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val key = intent.getStringExtra("request")
        when {
            !GalleryPermission.contains(key) -> finish()
            GalleryPermission.granted(this) -> {
                GalleryPermission.finish(key, true)
                finish()
            }
            savedInstanceState == null ->
                permission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    override fun onDestroy() {
        if (isFinishing) GalleryPermission.finish(intent.getStringExtra("request"), false)
        super.onDestroy()
    }
}
