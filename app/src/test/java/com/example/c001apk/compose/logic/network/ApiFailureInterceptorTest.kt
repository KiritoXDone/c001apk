package com.example.c001apk.compose.logic.network

import java.util.concurrent.TimeUnit
import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ApiFailureInterceptorTest {
    @Test
    fun captchaResponseWithHttp403KeepsItsCaptchaClassification() {
        val failure = assertThrows(ApiFailure::class.java) {
            ApiFailureInterceptor().intercept(chain(403, """{"messageStatus":"err_request_captcha_v2"}"""))
        }
        assertEquals(ApiFailureKind.CAPTCHA, failure.kind)
    }

    @Test
    fun unrelatedHttpErrorRemainsHttpFailure() {
        val failure = assertThrows(ApiFailure::class.java) {
            ApiFailureInterceptor().intercept(chain(503, """{"messageStatus":"err_request_captcha_v2"}"""))
        }
        assertEquals(ApiFailureKind.HTTP, failure.kind)
    }

    @Test
    fun authenticationResponseWithHttp403KeepsItsAuthenticationClassification() {
        val failure = assertThrows(ApiFailure::class.java) {
            ApiFailureInterceptor().intercept(chain(403, """{"messageStatus":"err_request_login"}"""))
        }
        assertEquals(ApiFailureKind.AUTHENTICATION, failure.kind)
    }

    private fun chain(code: Int, body: String): Interceptor.Chain {
        val request = Request.Builder().url("https://api.coolapk.com/v6/feed/detail?id=1").build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("test")
            .body(body.toResponseBody("application/json".toMediaType()))
            .build()
        return object : Interceptor.Chain {
            override fun request(): Request = request
            override fun proceed(request: Request): Response = response
            override fun connection(): Connection? = null
            override fun call(): Call = error("Not used")
            override fun connectTimeoutMillis(): Int = 0
            override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
            override fun readTimeoutMillis(): Int = 0
            override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
            override fun writeTimeoutMillis(): Int = 0
            override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
        }
    }
}
