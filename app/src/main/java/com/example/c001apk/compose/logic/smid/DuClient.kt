package com.example.c001apk.compose.logic.smid

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaDrm
import android.net.ConnectivityManager
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.telephony.TelephonyManager
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.zip.Deflater

/**
 * Active registration path from People-11/shuzilm_id sample/DuClient.kt, revision 03118bc4.
 * Only package/configuration wiring and unused analysis helpers/comments are omitted.
 * Network scheduling, timeouts, payload fields and response rules follow that reference.
 */
internal object DuClient {
    private const val API_KEY = "11e7b222083a4b732b4b14811f6fc05995a01415eb"
    private const val PKG = "com.coolapk.market"
    private val WIDEVINE = UUID.fromString("edef8ba9-79d6-4ace-a3c8-27dcd51d21ed")

    private val HOSTS = listOf("https://auni.telecome.cn", "https://global-auni.checktrustworthiness.com")

    fun query(context: Context, previousId: String? = null): String? = runCatching {
        val payload = buildPayload(context.applicationContext, previousId).toString()
        val path = "/a/mdna/report?v=8.7&t=m&p=${DuCodec.md5Upper("$PKG.37be8a1ee106979a")}" +
                "&r=${UUID.randomUUID().toString().replace("-", "")}&n=0&l=2&h=0"
        val body = DuCodec.xor(deflate(payload.toByteArray()))
        val pool = Executors.newFixedThreadPool(HOSTS.size)
        try {
            pool.invokeAny(HOSTS.map { host -> Callable { report(host + path, body) ?: error("no id from $host") } })
        } finally {
            pool.shutdownNow()
        }
    }.getOrNull()

    private fun report(url: String, body: ByteArray): String? {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 30000
            readTimeout = 30000
            doOutput = true
            setRequestProperty("Content-Length", body.size.toString())
        }
        try {
            conn.outputStream.use { it.write(body) }
            if (conn.responseCode != 200) return null
            val reply = JSONObject(String(DuCodec.xor(conn.inputStream.use { it.readBytes() })))
            if (reply.optString("err") != "0") return null
            return reply.optString("n_a").ifEmpty { reply.optString("cdd") }.takeIf { it.isNotEmpty() && !it.all { c -> c == '0' } }
        } finally {
            conn.disconnect()
        }
    }

    private fun deflate(data: ByteArray): ByteArray {
        val d = Deflater() // zlib framing, same as libz compress()
        d.setInput(data)
        d.finish()
        val out = ByteArrayOutputStream()
        val buf = ByteArray(4096)
        while (!d.finished()) out.write(buf, 0, d.deflate(buf))
        d.end()
        return out.toByteArray()
    }

    @SuppressLint("HardwareIds")
    @Suppress("UNUSED_VARIABLE") // t0, pkgInfo, net and tm feed the commented-out fields
    private fun buildPayload(ctx: Context, previousId: String?): JSONObject {
        val t0 = SystemClock.elapsedRealtime()
        val androidId = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID).orEmpty()
        val pkgInfo = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
        val nowMs = System.currentTimeMillis()
        val nsec = (nowMs % 1000 * 1_000_000 + SystemClock.elapsedRealtimeNanos() % 1_000_000).coerceAtLeast(1)
        val time = "${nowMs / 1000}.$nsec"
        val p = DuCodec.param(time)
        val net = networkCaps(ctx)
        val tm = ctx.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val sha1 = DuCodec.hashHexUpper("SHA-1", androidId)
        return JSONObject().apply {
            put("foO", DuCodec.enc(DuCodec.dropChar(androidId, p), p)) // ANDROID_ID, mode 10 with sfOo
            put("sfOo", DuCodec.checksum(androidId, p))
            put("P1J", DuCodec.fakeMac(sha1)) // libdu's stand-in MAC from SHA-1(ANDROID_ID); later reports send the reply's o_a
            put("AYk", DuCodec.fakeMac(sha1))
            widevineId()?.let { put("6yY", it) }
            previousId?.let { put("ubF", it) } // the last id (libdu pref n_a)
            put("dLH", DuCodec.enc(bootUuid(ctx), p)) // libdu pref boot_uuid: random, once per install (mIS is the kernel's boot_id)

            put("75c", API_KEY)
            put("DUv", PKG)
            put("3mS", "v9.0.1")
            put("AAA", "v1.0")
            put("BBB", "v1.0")
            put("UOv", DuCodec.enc(bootTime(), p))
            put("2cO", time) // the report's time; its last 3 digits are also the encoding parameter p

            put("R37", Build.MANUFACTURER)
            put("R3d", Build.MODEL)
            put("R41", Build.VERSION.RELEASE)
            put("SAN", Build.VERSION.SDK_INT)








        }
    }

    private fun widevineId(): String? = runCatching {
        val drm = MediaDrm(WIDEVINE)
        try {
            drm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID).joinToString("") { "%02x".format(it) }
        } finally {
            if (Build.VERSION.SDK_INT >= 28) drm.close() else @Suppress("DEPRECATION") drm.release()
        }
    }.getOrNull()

    private fun bootTime(): String {
        val ns = System.currentTimeMillis() * 1_000_000 - SystemClock.elapsedRealtimeNanos()
        return "${ns / 1_000_000_000}.${ns % 1_000_000_000}"
    }

    private fun bootUuid(ctx: Context): String = ctx.getSharedPreferences("du_client", Context.MODE_PRIVATE).let { sp ->
        sp.getString("boot_uuid", null) ?: UUID.randomUUID().toString().also { sp.edit().putString("boot_uuid", it).apply() }
    }

    private fun networkCaps(ctx: Context) = runCatching {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.getNetworkCapabilities(cm.activeNetwork)
    }.getOrNull()
}
