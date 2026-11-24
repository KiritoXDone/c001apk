package com.example.c001apk.compose.logic.network

import androidx.annotation.StringRes
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.settings.AppLanguage
import com.example.c001apk.compose.util.CoolapkRequestPolicy
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

enum class ApiFailureKind {
    HTTP,
    BUSINESS,
    AUTHENTICATION,
    CAPTCHA,
    RATE_LIMIT,
    RISK_CONTROL,
    CLIENT_UPGRADE,
    INVALID_RESPONSE,
}

class ApiFailure(
    val kind: ApiFailureKind,
    message: String,
    val expiredSession: Boolean = false,
    @param:StringRes val userMessageRes: Int? = null,
    val messageArgs: List<Any> = emptyList(),
) : IOException(message)

object ApiFailures {
    fun fromBody(body: JsonObject): ApiFailure? {
        fun text(key: String) = body.get(key)?.takeUnless { it.isJsonNull }?.asString.orEmpty()
        val marker = text("messageStatus").lowercase()
        val message = text("message")
        val clues = "$marker $message ${text("error")}".lowercase()
        val status = text("status").toIntOrNull()
        val code = text("code").toIntOrNull()
        val failed =
            marker.startsWith("err_") ||
                status?.let { it !in listOf(0, 1) } == true ||
                code?.let { it !in listOf(0, 1, 200) } == true ||
                text("error") !in listOf("", "0", "false") ||
                (body.get("data")?.isJsonNull != false &&
                    message.isNotEmpty() &&
                    code == null &&
                    status == null)
        if (!failed) return null
        return when {
            status == -10 || "网络环境" in clues ->
                ApiFailure(ApiFailureKind.RISK_CONTROL, "Network environment rejected", userMessageRes = R.string.api_risk_control)
            marker == "err_request_need_upgrade_new_version" ->
                ApiFailure(ApiFailureKind.CLIENT_UPGRADE, "Client protocol rejected", userMessageRes = R.string.api_upgrade)
            "captcha" in clues || "验证码" in clues ->
                ApiFailure(ApiFailureKind.CAPTCHA, "Human verification required", userMessageRes = R.string.api_captcha)
            "login" in clues || "auth" in clues || "登录" in clues || code == 401 ->
                ApiFailure(ApiFailureKind.AUTHENTICATION, "Authentication required", message == "登录信息有误", R.string.api_login)
            "rate_limit" in clues || "频繁" in clues || code == 429 ->
                ApiFailure(ApiFailureKind.RATE_LIMIT, "Too many requests", userMessageRes = R.string.api_rate_limit)
            status == -2 && ("应用不存在" in message || "未上架" in message || "已下架" in message) ->
                ApiFailure(ApiFailureKind.BUSINESS, "App not listed", userMessageRes = R.string.api_app_unavailable)
            else -> ApiFailure(
                ApiFailureKind.BUSINESS,
                message.ifBlank { "Request rejected" },
                userMessageRes = if (message.isBlank()) R.string.api_business else null,
            )
        }
    }

    fun safeMessage(error: Throwable?): String =
        safeMessage(error) { resId, formatArgs ->
            AppLanguage.getString(resId, *formatArgs.toTypedArray())
        }

    fun safeMessage(error: Throwable?, resolveString: (Int, List<Any>) -> String): String =
        when (error) {
            is ApiFailure -> error.userMessageRes?.let {
                resolveString(it, error.messageArgs)
            } ?: error.message.orEmpty()
            is java.net.SocketTimeoutException -> resolveString(R.string.api_timeout, emptyList())
            is javax.net.ssl.SSLException -> resolveString(R.string.api_certificate, emptyList())
            is IOException -> resolveString(R.string.api_connection, emptyList())
            else -> resolveString(R.string.api_invalid_response, emptyList())
        }
}

class ApiFailureInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (
            !CoolapkRequestPolicy.isApi(response.request.url) &&
                !CoolapkRequestPolicy.isPublicFeed(response.request)
        )
            return response
        if (response.code !in 200..399) {
            val accessFailure = if (response.code in listOf(400, 401, 403)) {
                runCatching {
                    val body = JsonParser.parseString(response.peekBody(1024 * 1024).string()).asJsonObject
                    ApiFailures.fromBody(body)?.takeIf {
                        it.kind == ApiFailureKind.CAPTCHA || it.kind == ApiFailureKind.AUTHENTICATION
                    }
                }.getOrNull()
            } else null
            response.close()
            throw accessFailure ?: ApiFailure(
                ApiFailureKind.HTTP,
                "HTTP ${response.code}",
                userMessageRes = R.string.api_http,
                messageArgs = listOf(response.code),
            )
        }
        // 二进制图片与下载接口不参与 JSON 响应校验。
        val type = response.body?.contentType()?.subtype.orEmpty()
        if (!type.contains("json") && !type.contains("text")) return response
        val body =
            runCatching {
                JsonParser.parseString(response.peekBody(1024 * 1024).string()).asJsonObject
            }
                .getOrNull() ?: return response
        if (
            body.get("messageStatus")?.let {
                !it.isJsonNull && it.asString == "err_request_captcha"
            } == true
        )
            return response
        val failure = runCatching { ApiFailures.fromBody(body) }.getOrNull()
        if (failure != null) {
            response.close()
            throw failure
        }
        return response
    }
}
