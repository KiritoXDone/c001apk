package com.example.c001apk.compose.debug

import android.app.Activity
import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.Process
import android.util.Base64
import java.lang.ref.WeakReference
import org.json.JSONObject

/**
 * Debug-only command channel. The provider is packaged from `src/debug` only and accepts
 * adb shell/root callers; every request and response is a base64 encoded JSON object.
 */
class AppDebugProvider : ContentProvider() {

    private var application: Application? = null
    private var resumedActivity = WeakReference<Activity>(null)

    override fun onCreate(): Boolean {
        val app = context?.applicationContext as? Application ?: return false
        application = app
        app.registerActivityLifecycleCallbacks(
            object : ActivityCallbacks() {
                override fun onActivityResumed(activity: Activity) {
                    resumedActivity = WeakReference(activity)
                }

                override fun onActivityDestroyed(activity: Activity) {
                    if (resumedActivity.get() === activity) resumedActivity = WeakReference(null)
                }
            },
        )
        return true
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle {
        val caller = Binder.getCallingUid()
        val allowed = caller == Process.ROOT_UID || caller == Process.SHELL_UID ||
            caller == Process.myUid()
        require(allowed) { "Debug channel rejects uid $caller" }
        val request = decodeRequest(arg)
        val response =
            try {
                DebugAutomation(requireNotNull(application), resumedActivity.get()).execute(request)
            } catch (error: Exception) {
                JSONObject()
                    .put("ok", false)
                    .put("error", error.message ?: error.javaClass.simpleName)
            }
        return Bundle().apply {
            putString("result", Base64.encodeToString(response.toString().toByteArray(), Base64.NO_WRAP))
        }
    }

    private fun decodeRequest(arg: String?): JSONObject {
        require(!arg.isNullOrBlank()) { "Debug request payload is missing" }
        val text = String(Base64.decode(arg, Base64.DEFAULT), Charsets.UTF_8)
        return JSONObject(text)
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = throw UnsupportedOperationException("Debug channel only supports call()")

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? =
        throw UnsupportedOperationException("Debug channel only supports call()")

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int =
        throw UnsupportedOperationException("Debug channel only supports call()")

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = throw UnsupportedOperationException("Debug channel only supports call()")
}
