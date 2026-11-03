package com.example.c001apk.compose.logic.repository

import com.example.c001apk.compose.constant.Constants.LOADING_FAILED
import com.example.c001apk.compose.di.AccountService
import com.example.c001apk.compose.di.Api1Service
import com.example.c001apk.compose.di.Api1ServiceNoRedirect
import com.example.c001apk.compose.di.Api2Service
import com.example.c001apk.compose.logic.model.AccountCollection
import com.example.c001apk.compose.logic.model.FeedContentResponse
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.model.ProductConfigData
import com.example.c001apk.compose.logic.model.configSections
import com.example.c001apk.compose.logic.network.ApiService
import com.example.c001apk.compose.logic.network.feedSearchUrl
import com.example.c001apk.compose.logic.network.feedSearchScope
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.util.CookieUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/** Created by bggRGjQaUbCoE on 2024/6/3 */
@Singleton
class NetworkRepo
@Inject
constructor(
    @param:Api1Service private val apiService: ApiService,
    @param:Api1ServiceNoRedirect private val apiServiceNoRedirect: ApiService,
    @param:Api2Service private val api2Service: ApiService,
    @param:AccountService private val accountService: ApiService,
) {

    suspend fun getHomeFeed(
        page: Int,
        firstLaunch: Int,
        installTime: String,
        firstItem: String?,
        lastItem: String?,
    ) = flowList {
        api2Service.getHomeFeed(page, firstLaunch, installTime, firstItem, lastItem).await()
    }

    suspend fun getFeedContent(url: String, verifyCaptcha: Boolean = false) = flowData {
        val id = ("https://api.coolapk.com" + url).toHttpUrl().queryParameter("id")
        try {
            apiService
                .getFeedContent(
                    url,
                    if (verifyCaptcha) null
                    else com.example.c001apk.compose.logic.network.SkipCaptcha(),
                )
                .await()
        } catch (failure: com.example.c001apk.compose.logic.network.ApiFailure) {
            if (
                verifyCaptcha ||
                    id?.matches(Regex("^[0-9]+$")) != true ||
                    failure.kind !in
                        listOf(
                            com.example.c001apk.compose.logic.network.ApiFailureKind.CAPTCHA,
                            com.example.c001apk.compose.logic.network.ApiFailureKind.AUTHENTICATION,
                        )
            )
                throw failure
            val public =
                try {
                    apiService.getFeedContent("https://www.coolapk.com/feed/$id").await()
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    throw com.example.c001apk.compose.logic.network.ApiFailure(
                        failure.kind,
                        "公开分享页暂不提供正文，点击重试以完成验证，或检查登录与设备标识",
                    )
                }
            public.copy(data = public.data?.copy(userAction = null), isPublic = true)
        }
    }

    suspend fun getFeedContentReply(
        id: String,
        listType: String,
        page: Int,
        firstItem: String?,
        lastItem: String?,
        discussMode: Int,
        feedType: String,
        blockStatus: Int,
        fromFeedAuthor: Int,
        verifyCaptcha: Boolean = true,
    ) = flowList {
        api2Service
            .getFeedContentReply(
                id,
                listType,
                page,
                firstItem,
                lastItem,
                discussMode,
                feedType,
                blockStatus,
                fromFeedAuthor,
                if (verifyCaptcha) null
                else com.example.c001apk.compose.logic.network.SkipCaptcha(),
            )
            .await()
    }

    suspend fun getSearch(
        type: String,
        feedType: String,
        sort: String,
        keyWord: String,
        pageType: String?,
        pageParam: String?,
        page: Int,
        lastItem: String?,
    ) = flowList {
        apiService
            .getSearch(
                type,
                feedType,
                sort,
                keyWord,
                pageType,
                pageParam,
                page,
                lastItem,
            )
            .await()
    }

    suspend fun getFeedSearch(
        keyword: String,
        feedType: String,
        sort: String,
        isStrict: Boolean,
        title: String,
        pageType: String?,
        pageParam: String?,
        page: Int,
        lastItem: String?,
    ) = flowList {
        val scope = feedSearchScope(pageType, pageParam)
        if (scope != null) {
            apiService.getSearch(
                "feed", feedType, sort, keyword, scope.type, scope.parameter, page, lastItem,
                isStrict = if (isStrict) 1 else 0,
            ).await()
        } else {
            apiService.getDataList(
                feedSearchUrl(keyword, sort, isStrict, feedType),
                title, null, lastItem, page,
            ).await()
        }
    }

    suspend fun getReply2Reply(id: String, page: Int, lastItem: String?) = flowList {
        apiService.getReply2Reply(id, page, lastItem).await()
    }

    suspend fun getTopicLayout(url: String, tag: String?, id: String?) = flowData {
        api2Service.getTopicLayout(url, tag, id).await()
    }

    suspend fun getProductLayout(id: String) = fire {
        Result.success(apiService.getProductLayout(id).await())
    }

    suspend fun getProductConfig(id: String) = fire {
        val response = apiService.getProductConfig(id).await()
        val sections = response.configSections()
        when {
            sections.isNotEmpty() ->
                Result.success(
                    ProductConfigData(
                        title = response.data?.title.orEmpty(),
                        sections = sections,
                    ),
                )
            !response.message.isNullOrBlank() ->
                Result.failure(
                    IllegalStateException(mapServerMessage(response.message)),
                )
            else -> Result.failure(IllegalStateException(LOADING_FAILED))
        }
    }

    suspend fun getUserSpace(uid: String) = flowData {
        apiService.getUserSpace(uid).await()
    }

    suspend fun getUserFeed(uid: String, page: Int, lastItem: String?, includeTop: Boolean = true) =
        flowList {
            apiService.getUserFeed(uid, page, lastItem, if (includeTop) 1 else 0).await()
        }

    suspend fun getAppInfo(id: String) = flowData {
        apiService.getAppInfo(id).await()
    }

    suspend fun getAppDownloadLink(pn: String, aid: String, vc: String) = fire {
        val appResponse = apiServiceNoRedirect.getAppDownloadLink(pn, aid, vc).response()
        Result.success(appResponse.headers()["Location"])
    }

    suspend fun getAppsUpdate(pkgs: String) = flowUpdateList {
        val multipartBody = MultipartBody.Part.createFormData("pkgs", pkgs)
        val responseBody = apiService.getAppsUpdate(multipartBody).await()
        parseAppsUpdate(responseBody.string())
    }

    suspend fun getProfile(uid: String) = fire {
        Result.success(api2Service.getProfile(uid).await())
    }

    suspend fun getFollowList(
        url: String,
        uid: String?,
        id: String?,
        showDefault: Int?,
        page: Int,
        lastItem: String?,
    ) = flowList {
        apiService.getFollowList(url, uid, id, showDefault, page, lastItem).await()
    }

    suspend fun postLike(url: String, id: String) = fire {
        Result.success(apiService.postLike(url, id).await())
    }

    suspend fun checkLoginInfo() = fire {
        Result.success(apiService.checkLoginInfo().response())
    }

    suspend fun getLoginParam(url: String) = fire {
        Result.success(accountService.getLoginParam(url).response())
    }

    suspend fun tryLogin(data: HashMap<String, String?>) = fire {
        Result.success(accountService.tryLogin(data).response())
    }

    suspend fun getCaptcha(url: String) = fire {
        Result.success(accountService.getCaptcha(url).response())
    }

    suspend fun getValidateCaptcha(url: String) = fire {
        Result.success(apiService.getValidateCaptcha(url).response())
    }

    suspend fun postReply(data: HashMap<String, String>, id: String, type: String) = fire {
        Result.success(apiService.postReply(data, id, type).await())
    }

    suspend fun getDataList(
        url: String,
        title: String,
        subTitle: String?,
        lastItem: String?,
        page: Int,
    ) = flowList {
        api2Service.getDataList(url, title, subTitle, lastItem, page).await()
    }

    suspend fun getEventDetail(id: String) = flow {
        val state =
            try {
                val response = api2Service.getEventDetail(id).await()
                when {
                    !response.message.isNullOrEmpty() ->
                        LoadingState.Error(mapServerMessage(response.message))

                    response.data != null -> LoadingState.Success(response.data)
                    else -> LoadingState.Error(LOADING_FAILED)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                LoadingState.Error(mapErrorMessage(e))
            }
        emit(state)
    }
        .flowOn(Dispatchers.IO)

    suspend fun getDyhDetail(dyhId: String, type: String, page: Int, lastItem: String?) = flowList {
        apiService.getDyhDetail(dyhId, type, page, lastItem).await()
    }

    suspend fun getCoolPic(tag: String, type: String, page: Int, lastItem: String?) = flowList {
        apiService.getCoolPic(tag, type, page, lastItem).await()
    }

    suspend fun getMessage(url: String, page: Int, lastItem: String?) = flowList {
        apiService.getMessage(url, page, lastItem).await()
    }

    suspend fun postFollowUnFollow(url: String, uid: String) = fire {
        Result.success(apiService.postFollowUnFollow(url, uid).await())
    }

    suspend fun postCreateFeed(data: HashMap<String, String>) = fire {
        Result.success(apiService.postCreateFeed(data).await())
    }

    suspend fun postRequestValidate(data: HashMap<String, String?>) = fire {
        Result.success(apiService.postRequestValidate(data).await())
    }

    suspend fun getVoteComment(
        fid: String,
        extraKey: String,
        page: Int,
        firstItem: String?,
        lastItem: String?,
    ) = fire {
        Result.success(apiService.getVoteComment(fid, extraKey, page, firstItem, lastItem).await())
    }

    suspend fun getAnswerList(
        id: String,
        sort: String,
        page: Int,
        firstItem: String?,
        lastItem: String?,
    ) = fire {
        Result.success(apiService.getAnswerList(id, sort, page, firstItem, lastItem).await())
    }

    suspend fun getProductList(url: String) = flowList {
        apiService.getProductList(url).await()
    }

    suspend fun getCollectionList(
        url: String,
        uid: String?,
        id: String?,
        showDefault: Int,
        page: Int,
        lastItem: String?,
    ) = fire {
        Result.success(
            apiService.getCollectionList(url, uid, id, showDefault, page, lastItem).await(),
        )
    }

    suspend fun getFeedCollections(feedId: String, page: Int, lastItem: String?) =
        apiService
            .getFeedCollections(feedId = feedId, page = page, lastItem = lastItem)
            .await()
            .requireData()

    suspend fun saveFeedCollections(feedId: String, ids: Set<String>, cancelIds: Set<String>) =
        apiService
            .saveFeedCollections(ids.joinToString(","), cancelIds.joinToString(","), feedId)
            .await()
            .requireData()

    suspend fun createCollection(
        title: String,
        isOpen: Boolean,
    ): AccountCollection {
        val text = "text/plain; charset=utf-8".toMediaType()
        return apiService
            .createCollection(
                title.toRequestBody(text),
                (if (isOpen) "1" else "0").toRequestBody(text),
                "".toRequestBody(text),
                "".toRequestBody(text),
                "".toRequestBody(text),
            )
            .await()
            .requireData()
    }

    suspend fun postDelete(url: String, id: String) = fire {
        Result.success(apiService.postDelete(url, id).await())
    }

    suspend fun postFollow(data: HashMap<String, String>) = fire {
        Result.success(apiService.postFollow(data).await())
    }

    suspend fun getFollow(url: String, tag: String?, id: String?) = fire {
        Result.success(apiService.getFollow(url, tag, id).await())
    }

    suspend fun postOSSUploadPrepare(data: HashMap<String, String>) = fire {
        Result.success(apiService.postOSSUploadPrepare(data).await())
    }

    suspend fun getSearchTag(
        query: String,
        page: Int,
        recentIds: String?,
        firstItem: String?,
        lastItem: String?,
    ) = fire {
        Result.success(apiService.getSearchTag(query, page, recentIds, firstItem, lastItem).await())
    }

    suspend fun loadShareUrl(url: String) = fire {
        Result.success(apiService.loadShareUrl(url).await())
    }

    suspend fun messageOperation(
        url: String,
        ukey: String?,
        uid: String?,
        page: Int?,
        firstItem: String?,
        lastItem: String?,
    ) = flowList {
        apiService.messageOperation(url, ukey, uid, page, firstItem, lastItem).await()
    }

    suspend fun sendMessage(uid: String, message: MultipartBody.Part, pic: MultipartBody.Part) =
        fire {
            val body = apiService.sendMessage(uid, message, pic).await().use { it.string() }
            val root = com.google.gson.JsonParser.parseString(body).asJsonObject
            if (root.get("data")?.isJsonObject == true)
                root.add("data", com.google.gson.JsonArray().apply { add(root.get("data")) })
            Result.success(Gson().fromJson(root, HomeFeedResponse::class.java))
        }

    suspend fun deleteMessage(url: String, ukey: String, id: String?) = fire {
        Result.success(apiService.deleteMessage(url, ukey, id).await())
    }

    suspend fun getImageUrl(id: String) = fire {
        val response = apiServiceNoRedirect.getImageUrl(id).response()
        val location = response.headers()["Location"]
        Result.success(
            location?.takeIf {
                runCatching { it.toHttpUrl().scheme in listOf("http", "https") }.getOrDefault(false)
            },
        )
    }

    suspend fun checkCount() = fire {
        Result.success(apiService.checkCount().await())
    }

    suspend fun postDelete(url: String, data: Map<String, String>?) = fire {
        Result.success(apiService.postDelete(url, data).await())
    }

    private suspend fun <T> Call<T>.await(): T {
        val revision = CookieUtil.accountSession.revision
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { cancel() }
            enqueue(
                object : Callback<T> {
                    override fun onResponse(call: Call<T>, response: Response<T>) {
                        if (!continuation.isActive) {
                            (response.body() as? okhttp3.ResponseBody)?.close()
                            return
                        }
                        if (revision != CookieUtil.accountSession.revision) {
                            (response.body() as? okhttp3.ResponseBody)?.close()
                            continuation.cancel(CancellationException("账号已切换"))
                            return
                        }
                        val body = response.body()
                        if (body != null) continuation.resume(body)
                        else
                            continuation.resumeWithException(
                                RuntimeException("response body is null"),
                            )
                    }

                    override fun onFailure(call: Call<T>, t: Throwable) {
                        if (continuation.isActive) continuation.resumeWithException(t)
                    }
                },
            )
        }
    }

    private suspend fun <T> Call<T>.response(): Response<T> {
        val revision = CookieUtil.accountSession.revision
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { cancel() }
            enqueue(
                object : Callback<T> {
                    override fun onResponse(call: Call<T>, response: Response<T>) {
                        if (!continuation.isActive) {
                            (response.body() as? okhttp3.ResponseBody)?.close()
                            return
                        }
                        if (revision != CookieUtil.accountSession.revision) {
                            (response.body() as? okhttp3.ResponseBody)?.close()
                            continuation.cancel(CancellationException("账号已切换"))
                            return
                        }
                        continuation.resume(response)
                    }

                    override fun onFailure(call: Call<T>, t: Throwable) {
                        if (continuation.isActive) continuation.resumeWithException(t)
                    }
                },
            )
        }
    }

    private fun <T> fire(block: suspend () -> Result<T>) = flow {
        val result =
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        emit(result)
    }
        .flowOn(Dispatchers.IO)

    private fun flowList(block: suspend () -> HomeFeedResponse) = flow {
        val result =
            try {
                val response = block()
                if (!response.message.isNullOrEmpty()) {
                    LoadingState.Error(mapServerMessage(response.message))
                } else if (!response.data.isNullOrEmpty()) {
                    LoadingState.Success(response.data)
                } else if (response.data?.isEmpty() == true) {
                    LoadingState.Empty
                } else {
                    LoadingState.Error(LOADING_FAILED)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                LoadingState.Error(mapErrorMessage(e))
            }
        emit(result)
    }
        .flowOn(Dispatchers.IO)

    private fun flowUpdateList(block: suspend () -> List<HomeFeedResponse.Data>) = flow {
        val result =
            try {
                val response = block()
                if (response.isNotEmpty()) {
                    LoadingState.Success(response)
                } else {
                    LoadingState.Empty
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                LoadingState.Error(mapErrorMessage(e))
            }
        emit(result)
    }
        .flowOn(Dispatchers.IO)

    private fun parseAppsUpdate(raw: String): List<HomeFeedResponse.Data> {
        val payload = raw.trim()
        if (payload.isEmpty()) return emptyList()
        val gson = Gson()
        val listType = object : TypeToken<List<HomeFeedResponse.Data>>() {}.type
        return if (payload.startsWith("[")) {
            gson.fromJson(payload, listType) ?: emptyList()
        } else {
            val rootType = object : TypeToken<HomeFeedResponse>() {}.type
            val response: HomeFeedResponse? = gson.fromJson(payload, rootType)
            response?.data ?: emptyList()
        }
    }

    private fun flowData(block: suspend () -> FeedContentResponse) = flow {
        val result =
            try {
                val response = block()
                if (!response.message.isNullOrEmpty()) {
                    LoadingState.Error(mapServerMessage(response.message))
                } else if (response.data != null) {
                    LoadingState.Success(response.data, isPublic = response.isPublic)
                } else {
                    LoadingState.Error(LOADING_FAILED)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                LoadingState.Error(mapErrorMessage(e))
            }
        emit(result)
    }
        .flowOn(Dispatchers.IO)

    private fun mapServerMessage(message: String?): String {
        val normalized = message?.trim().orEmpty()
        if (normalized.equals("timeout", ignoreCase = true)) {
            return "超时"
        }
        return normalized.ifEmpty { LOADING_FAILED }
    }

    private fun mapErrorMessage(e: Exception): String =
        com.example.c001apk.compose.logic.network.ApiFailures.safeMessage(e)
}
