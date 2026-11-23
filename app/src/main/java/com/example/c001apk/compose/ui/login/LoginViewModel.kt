package com.example.c001apk.compose.ui.login

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.c001apk.compose.logic.network.ApiFailures
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.settings.AppLanguage
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.constant.Constants.EMPTY_STRING
import com.example.c001apk.compose.logic.model.LoginResponse
import com.example.c001apk.compose.logic.repository.NetworkRepo
import com.example.c001apk.compose.util.CookieUtil.SESSID
import com.example.c001apk.compose.util.CookieUtil.isGetCaptcha
import com.example.c001apk.compose.util.CookieUtil.isGetLoginParam
import com.example.c001apk.compose.util.CookieUtil.isPreGetLoginParam
import com.example.c001apk.compose.util.CookieUtil.isTryLogin
import com.example.c001apk.compose.util.createRequestHash
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

/** Created by bggRGjQaUbCoE on 2024/6/10 */
@HiltViewModel
class LoginViewModel
@Inject
constructor(
    private val networkRepo: NetworkRepo,
    private val accounts: com.example.c001apk.compose.logic.accounts.AccountStore,
) : ViewModel() {

    private val accountRevision =
        com.example.c001apk.compose.util.CookieUtil.accountSession.revision

    var requestHash by mutableStateOf(EMPTY_STRING)
    var captchaImg by mutableStateOf<Bitmap?>(null)
        private set

    var toastText by mutableStateOf<String?>(null)
        private set

    private val urlPreGetParam = "/auth/login?type=mobile"
    private val urlGetParam = "/auth/loginByCoolApk"

    init {
        isPreGetLoginParam = true
        viewModelScope.launch(Dispatchers.IO) {
            onGetLoginParam(urlPreGetParam)
        }
    }

    private suspend fun onGetLoginParam(url: String) {
        networkRepo.getLoginParam(url).collect { result ->
            val response = result.getOrNull()
            if (response != null) {
                if (url == urlGetParam) {
                    response.body()?.string()?.let {
                        requestHash = Jsoup.parse(it).createRequestHash()
                    }
                }
                try {
                    val session = response.headers().values("Set-Cookie")[0]
                    SESSID = session.substring(0, session.indexOf(";"))
                } catch (e: Exception) {
                    e.printStackTrace()
                    toastText = AppLanguage.getString(R.string.error_cookie)
                    return@collect
                }
                if (url == urlPreGetParam) {
                    isGetLoginParam = true
                    onGetLoginParam(urlGetParam)
                }
            } else {
                toastText = ApiFailures.safeMessage(result.exceptionOrNull())
                result.exceptionOrNull()?.printStackTrace()
            }
        }
    }

    fun onGetCaptcha() {
        isGetCaptcha = true
        val timeStamp = System.currentTimeMillis().toString()
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getCaptcha("/auth/showCaptchaImage?$timeStamp").collect { result ->
                val response = result.getOrNull()
                if (response != null) {
                    captchaImg = BitmapFactory.decodeStream(response.body()?.byteStream())
                } else {
                    toastText = ApiFailures.safeMessage(result.exceptionOrNull())
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }
    }

    var loginData = HashMap<String, String?>()

    fun onLogin() {
        isTryLogin = true
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.tryLogin(loginData).collect { result ->
                val response = result.getOrNull()
                response?.body()?.let {
                    val login: LoginResponse =
                        Gson()
                            .fromJson(
                                response.body()?.string(),
                                LoginResponse::class.java,
                            )
                    if (login.status == 1) {
                        val cookies = response.headers().values("Set-Cookie")
                        val uid =
                            cookies
                                .find { it.startsWith("uid=") }
                                ?.split(";")
                                ?.getOrNull(0)
                                ?.replace("uid=", EMPTY_STRING)
                                ?.trim()
                        val username =
                            cookies
                                .find { it.startsWith("username=") }
                                ?.split(";")
                                ?.getOrNull(0)
                                ?.replace("username=", EMPTY_STRING)
                                ?.trim()
                        val token =
                            cookies
                                .findLast { it.startsWith("token=") }
                                ?.split(";")
                                ?.getOrNull(0)
                                ?.replace("token=", EMPTY_STRING)
                                ?.trim()
                        if (
                            !uid.isNullOrEmpty() &&
                                !username.isNullOrEmpty() &&
                                !token.isNullOrEmpty()
                        ) {
                            try {
                                accounts.saveLogin(
                                    com.example.c001apk.compose.logic.accounts.SavedAccount(
                                        uid,
                                        username,
                                        token,
                                    ),
                                    accountRevision,
                                )
                                loginData.clear()
                            } catch (e: kotlinx.coroutines.CancellationException) {
                                throw e
                            } catch (_: Exception) {
                                toastText = AppLanguage.getString(R.string.error_login_save)
                            }
                        }
                    } else {
                        login.message?.let {
                            toastText = login.message
                            when (login.message) {
                                "图形验证码不能为空",
                                "图形验证码错误" -> onGetCaptcha()

                                "密码错误" -> if (captchaImg != null) onGetCaptcha()
                            }
                        }
                    }
                }
            }
        }
    }

    fun reset() {
        toastText = null
    }
}
