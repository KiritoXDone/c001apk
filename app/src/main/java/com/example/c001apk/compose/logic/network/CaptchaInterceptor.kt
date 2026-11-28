package com.example.c001apk.compose.logic.network

import com.example.c001apk.compose.logic.network.session.ApiSessions
import com.example.c001apk.compose.logic.network.session.RequestSession
import com.example.c001apk.compose.util.CoolapkRequestPolicy
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.*

class CaptchaInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val epoch = CaptchaCoordinator.epoch.get()
        val sentValidation = ApiSessions.store.validation()
        val response = chain.proceed(request)
        if (
            !CoolapkRequestPolicy.isApi(response.request.url) ||
                request.tag(SkipCaptcha::class.java) != null ||
                response.body?.contentType()?.subtype.equals("event-stream", ignoreCase = true)
        )
            return response
        val challenge = CaptchaChallenge.from(json(response)) ?: return response
        val session = request.tag(RequestSession::class.java) ?: return response
        if (!session.isCurrent()) return response
        val currentValidation = ApiSessions.store.validation()
        if (
            challenge.field.isEmpty() &&
                currentValidation != null &&
                currentValidation != sentValidation
        ) {
            response.close()
            return chain.proceed(request)
        }
        if (epoch != CaptchaCoordinator.epoch.get()) return response
        val context = ApiSessions.store.capture()
        if (challenge.field.isEmpty()) ApiSessions.store.clearValidation()
        val proof =
            CaptchaCoordinator.request(challenge) {
                session.isCurrent() && !chain.call().isCanceled()
            } ?: return response
        if (!session.isCurrent() || chain.call().isCanceled()) return response
        val retry = request.newBuilder()
        if (challenge.field.isEmpty()) retry.tag(CaptchaProof::class.java, CaptchaProof(proof))
        else
            when (val body = request.body) {
                is FormBody -> {
                    val form = FormBody.Builder()
                    for (i in 0 until body.size) if (body.name(i) != challenge.field)
                        form.addEncoded(body.encodedName(i), body.encodedValue(i))
                    retry.method(request.method, form.add(challenge.field, proof).build())
                }
                is MultipartBody -> {
                    if (body.isOneShot()) return response
                    val multipart = MultipartBody.Builder().setType(body.type)
                    body.parts
                        .filterNot {
                            it.headers
                                ?.get("Content-Disposition")
                                ?.contains("name=\"${challenge.field}\"") == true
                        }
                        .forEach(multipart::addPart)
                    retry.method(
                        request.method,
                        multipart.addFormDataPart(challenge.field, proof).build(),
                    )
                }
                else ->
                    retry.url(
                        request.url.newBuilder().setQueryParameter(challenge.field, proof).build(),
                    )
            }
        // Exactly one replay after an explicit rejection; transport errors never trigger write
        // retries.
        response.close()
        val verified = chain.proceed(retry.build())
        if (challenge.field.isEmpty()) {
            val body = json(verified)
            val acceptedSession =
                verified.request
                    .tag(
                        com.example.c001apk.compose.logic.network.session.AcceptedApiSession::class
                            .java,
                    )
                    ?.hash
            if (
                verified.isSuccessful &&
                    runCatching { accepted(body) }.getOrDefault(false) &&
                    session.isCurrent() &&
                    acceptedSession != null
            )
                ApiSessions.store.acceptValidation(
                    CaptchaProof(proof).encoded,
                    context,
                    acceptedSession,
                )
            else ApiSessions.store.clearValidation()
        }
        return verified
    }

    private fun json(response: Response): JsonObject? = runCatching {
        JsonParser.parseString(response.peekBody(1024 * 1024).string()).asJsonObject
    }
        .getOrNull()

    private fun accepted(body: JsonObject?): Boolean =
        body != null &&
            body.has("data") &&
            body.get("messageStatus")?.asString?.startsWith("err_") != true &&
            body.get("status")?.let { it.isJsonNull || it.asInt in listOf(0, 1) } != false &&
            body.get("code")?.let { it.isJsonNull || it.asInt in listOf(0, 1, 200) } != false &&
            body.get("error")?.let {
                it.isJsonNull || it.toString() in listOf("0", "false", "\"\"")
            } != false
}
