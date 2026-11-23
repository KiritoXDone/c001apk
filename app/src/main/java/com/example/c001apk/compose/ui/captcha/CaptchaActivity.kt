package com.example.c001apk.compose.ui.captcha

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import org.json.JSONObject
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.settings.AppLanguage
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.example.c001apk.compose.logic.network.CaptchaCoordinator
import com.example.c001apk.compose.ui.component.*
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import top.yukonga.miuix.kmp.basic.Text

class CaptchaActivity : ComponentActivity() {
    private var requestId: String? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLanguage.wrap(newBase))
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestId = intent.getStringExtra("request")
        val pending =
            CaptchaCoordinator.pending(requestId)
                ?: run {
                    finish()
                    return
                }
        pending.result.whenComplete { _, _ -> runOnUiThread { if (!isDestroyed) finish() } }
        setContent {
            C001apkComposeTheme {
                var reload by remember { mutableIntStateOf(0) }
                MiuixScaffold(
                    topBar = {
                        MiuixTopBar(
                            title = stringResource(R.string.captcha_title),
                            navigationIcon = { BackButton { finish() } },
                            actions = {
                                MiuixActionButton(onClick = { reload++ }) { Text(stringResource(R.string.action_reload)) }
                            },
                        )
                    },
                ) { padding ->
                    if (!WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
                        Text(stringResource(R.string.captcha_update_webview), Modifier.padding(padding))
                    } else
                        key(reload) {
                            AndroidView(
                                modifier = Modifier.fillMaxSize().padding(padding),
                                factory = { context ->
                                    WebView(context).apply {
                                        settings.javaScriptEnabled = true
                                        settings.allowFileAccess = false
                                        settings.allowContentAccess = false
                                        settings.javaScriptCanOpenWindowsAutomatically = false
                                        settings.mixedContentMode =
                                            WebSettings.MIXED_CONTENT_NEVER_ALLOW
                                        webViewClient =
                                            object : WebViewClient() {
                                                override fun shouldOverrideUrlLoading(
                                                    view: WebView?,
                                                    request: WebResourceRequest?,
                                                ) = request?.isForMainFrame == true
                                            }
                                        WebViewCompat.addWebMessageListener(
                                            this,
                                            "CaptchaResult",
                                            setOf("https://captcha.c001apk.invalid"),
                                        ) { _, message, _, isMainFrame, _ ->
                                            if (isMainFrame)
                                                pending.challenge
                                                    .proof(message.data.orEmpty())
                                                    ?.let {
                                                        CaptchaCoordinator.complete(pending.id, it)
                                                    }
                                        }
                                        loadDataWithBaseURL(
                                            "https://captcha.c001apk.invalid/",
                                            html(pending.challenge.id),
                                            "text/html",
                                            "UTF-8",
                                            null,
                                        )
                                    }
                                },
                                onRelease = {
                                    it.stopLoading()
                                    it.destroy()
                                },
                            )
                        }
                }
            }
        }
    }

    override fun onDestroy() {
        if (!isChangingConfigurations) requestId?.let { CaptchaCoordinator.complete(it, null) }
        super.onDestroy()
    }

    private fun html(id: String): String {
        val language = if (resources.configuration.locales[0].language == "en") "en" else "zh-CN"
        fun message(resource: Int): String = JSONObject.quote(getString(resource))
        return """
        <!doctype html><html lang="$language"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
        <style>:root{color-scheme:light dark}body{font:16px sans-serif;margin:24px;background:Canvas;color:CanvasText}p{line-height:1.6}</style>
        <p id="status">${TextUtils.htmlEncode(getString(R.string.captcha_loading))}</p><div id="captcha"></div><script>
        function statusText(t){document.getElementById('status').textContent=t;}
        var timeout=setTimeout(function(){statusText(${message(R.string.captcha_timeout)});},15000);
        var script=document.createElement('script');script.src='https://cstaticdun.126.net/load.min.js';
        script.onerror=function(){clearTimeout(timeout);statusText(${message(R.string.captcha_load_failed)});};
        script.onload=function(){try{initNECaptcha({captchaId:${JSONObject.quote(id)},element:'#captcha',protocol:'https',mode:'embed',apiVersion:2,
        width:Math.min(320,window.innerWidth-48)+'px',lang:'$language',onVerify:function(err,data){
        if(!err&&data&&data.validate){window.CaptchaResult.postMessage(data.validate);}else{statusText(${message(R.string.captcha_failed)});}}},
        function(){clearTimeout(timeout);statusText(${message(R.string.captcha_hint)});},function(){clearTimeout(timeout);statusText(${message(R.string.captcha_init_failed)});});
        }catch(e){clearTimeout(timeout);statusText(${message(R.string.captcha_init_failed)});}};document.head.appendChild(script);
        </script></html>
        """.trimIndent()
    }
}
