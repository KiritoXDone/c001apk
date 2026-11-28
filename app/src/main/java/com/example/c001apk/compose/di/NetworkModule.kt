package com.example.c001apk.compose.di

import com.example.c001apk.compose.logic.network.ApiService
import com.example.c001apk.compose.util.AddCookiesInterceptor
import com.example.c001apk.compose.util.CoolapkRequestPolicy
import com.example.c001apk.compose.util.LoginCookiesInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class Api1Service

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class Api1ServiceNoRedirect

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class Api1StreamService

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class PublicStreamClient

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class Api2Service

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class AccountService

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val API_BASE_URL = "https://api.coolapk.com"
    private const val API2_BASE_URL = "https://api2.coolapk.com"
    private const val ACCOUNT_BASE_URL = "https://account.coolapk.com"

    @Api1Service
    @Singleton
    @Provides
    fun provideApi1Service(@Api1Service retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Api1ServiceNoRedirect
    @Singleton
    @Provides
    fun provideApi1ServiceNo(@Api1ServiceNoRedirect retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Api1StreamService
    @Singleton
    @Provides
    fun provideApi1StreamService(@Api1StreamService retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Api2Service
    @Singleton
    @Provides
    fun provideApi2Service(@Api2Service retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @AccountService
    @Singleton
    @Provides
    fun provideAccountService(@AccountService retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Api1Service
    @Singleton
    @Provides
    fun provideApi1ServiceRetrofit(@Api1Service okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Api1ServiceNoRedirect
    @Singleton
    @Provides
    fun provideApi1ServiceNoRetrofit(@Api1ServiceNoRedirect okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Api1StreamService
    @Singleton
    @Provides
    fun provideApi1StreamServiceRetrofit(@Api1StreamService okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Api2Service
    @Singleton
    @Provides
    fun provideApi2ServiceRetrofit(@Api1Service okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(API2_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @AccountService
    @Singleton
    @Provides
    fun provideAccountServiceRetrofit(@AccountService okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ACCOUNT_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Api1Service
    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                com.example.c001apk.compose.logic.network.session.RequestSession.capture,
            )
            .addInterceptor(com.example.c001apk.compose.logic.network.ApiFailureInterceptor())
            .addInterceptor(com.example.c001apk.compose.logic.network.CaptchaInterceptor())
            .addNetworkInterceptor(AddCookiesInterceptor)
            .followRedirects(true)
            .build()
    }

    @Api1ServiceNoRedirect
    @Singleton
    @Provides
    fun provideNoOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                com.example.c001apk.compose.logic.network.session.RequestSession.capture,
            )
            .addInterceptor(com.example.c001apk.compose.logic.network.ApiFailureInterceptor())
            .addInterceptor(com.example.c001apk.compose.logic.network.CaptchaInterceptor())
            .addNetworkInterceptor(AddCookiesInterceptor)
            .followRedirects(false)
            .build()
    }

    @Api1StreamService
    @Singleton
    @Provides
    fun provideApi1StreamOkHttpClient(@Api1ServiceNoRedirect okHttpClient: OkHttpClient): OkHttpClient {
        // AI 流式推理耗时长于普通接口，复用无重定向客户端并放宽读取超时。
        return okHttpClient.newBuilder().readTimeout(60, TimeUnit.SECONDS).build()
    }

    @PublicStreamClient
    @Singleton
    @Provides
    fun providePublicStreamOkHttpClient(): OkHttpClient {
        // 第三方镜像摘要走纯净客户端，绝不附带酷安登录态、设备标识或签名头。
        return OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS).build()
    }

    @AccountService
    @Singleton
    @Provides
    fun provideAccountServiceOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(LoginCookiesInterceptor)
            .addNetworkInterceptor(CoolapkRequestPolicy.accountBoundary)
            .build()
    }
}
