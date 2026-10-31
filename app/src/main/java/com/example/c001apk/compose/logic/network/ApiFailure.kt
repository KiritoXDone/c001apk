package com.example.c001apk.compose.logic.network

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

class ApiFailure(val kind: ApiFailureKind, message: String, val expiredSession: Boolean = false) :
    IOException(message)

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
                ApiFailure(ApiFailureKind.RISK_CONTROL, "服务端检测到网络环境异常，请检查代理、DNS 或网络过滤设置")
            marker == "err_request_need_upgrade_new_version" ->
                ApiFailure(ApiFailureKind.CLIENT_UPGRADE, "当前客户端协议未被服务端接受，请检查协议设置或更新客户端")
            "captcha" in clues || "验证码" in clues ->
                ApiFailure(ApiFailureKind.CAPTCHA, "请求需要人机验证，请完成验证后重试")
            "login" in clues || "auth" in clues || "登录" in clues || code == 401 ->
                ApiFailure(ApiFailureKind.AUTHENTICATION, "登录状态已失效或需要登录，请重新登录", message == "登录信息有误")
            "rate_limit" in clues || "频繁" in clues || code == 429 ->
                ApiFailure(ApiFailureKind.RATE_LIMIT, "请求过于频繁，请稍后重试")
            status == -2 && ("应用不存在" in message || "未上架" in message || "已下架" in message) ->
                ApiFailure(ApiFailureKind.BUSINESS, "该应用未在酷安上架")
            else -> ApiFailure(ApiFailureKind.BUSINESS, message.ifBlank { "服务器未能完成请求，请稍后重试" })
        }
    }

    fun safeMessage(error: Exception): String =
        when (error) {
            is ApiFailure -> error.message.orEmpty()
            is java.net.SocketTimeoutException -> "请求超时，请稍后重试"
            is javax.net.ssl.SSLException -> "服务器证书验证失败"
            is IOException -> "连接失败，请检查网络后重试"
            else -> "服务器响应格式异常，请稍后重试"
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
            response.close()
            throw ApiFailure(ApiFailureKind.HTTP, "请求失败（HTTP ${response.code}），请稍后重试")
        }
        // Binary image/download endpoints are intentionally outside JSON-envelope validation.
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
