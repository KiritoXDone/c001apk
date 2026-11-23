package com.example.c001apk.compose.ui.chat

import com.example.c001apk.compose.logic.filter.ContentFilterStore
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.settings.AppLanguage
import com.example.c001apk.compose.logic.network.ApiFailures
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.model.StringEntity
import com.example.c001apk.compose.logic.repository.BlackListRepo
import com.example.c001apk.compose.logic.repository.NetworkRepo
import com.example.c001apk.compose.logic.repository.RecentEmojiRepo
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.base.BaseViewModel
import com.example.c001apk.compose.util.CookieUtil
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

/** Created by bggRGjQaUbCoE on 2024/6/19 */
@HiltViewModel(assistedFactory = ChatViewModel.ViewModelFactory::class)
class ChatViewModel
@AssistedInject
constructor(
    @Assisted val ukey: String,
    networkRepo: NetworkRepo,
    blackListRepo: BlackListRepo,
    contentFilters: ContentFilterStore,
    private val recentEmojiRepo: RecentEmojiRepo,
    private val imageUploader: ChatImageUploader,
) : BaseViewModel(networkRepo, blackListRepo, contentFilters) {

    @AssistedFactory
    interface ViewModelFactory {
        fun create(ukey: String): ChatViewModel
    }

    private val accountRevision = CookieUtil.accountSession.revision
    private val imageRequests = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

    init {
        fetchData()
    }

    override suspend fun customFetchData() =
        networkRepo.messageOperation("/v6/message/chat", ukey, null, page, firstItem, lastItem)

    override fun onBlockUser(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            blackListRepo.saveUid(uid)
        }
    }

    override fun handleResponse(
        response: List<HomeFeedResponse.Data>,
    ): List<HomeFeedResponse.Data> {
        return response.reversed()
    }

    fun onGetImageUrl(id: String) {
        if (!imageRequests.add(id) || accountRevision != CookieUtil.accountSession.revision) return
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getImageUrl(id).collect { result ->
                val imageUrl = result.getOrNull()
                if (!imageUrl.isNullOrEmpty()) {
                    var response = (loadingState as? LoadingState.Success)?.response.orEmpty()
                    response = response.map { item ->
                        if (item.id == id) item.copy(messagePic = imageUrl) else item
                    }
                    loadingState = LoadingState.Success(response)
                } else {
                    toastText = ApiFailures.safeMessage(result.exceptionOrNull())
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }
    }

    fun onDeleteMsg() {
        val targetId = deleteId
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.deleteMessage("/v6/message/delete", ukey, targetId).collect { result ->
                val data = result.getOrNull()
                if (data != null) {
                    if (!data.message.isNullOrEmpty()) {
                        toastText = data.message
                    } else if (data.data?.count != null) {
                        var response = (loadingState as? LoadingState.Success)?.response.orEmpty()
                        response = response.filterNot { it.id == targetId }
                        loadingState = LoadingState.Success(response)
                        toastText = data.data.count
                    }
                } else {
                    toastText = ApiFailures.safeMessage(result.exceptionOrNull())
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }
    }

    lateinit var deleteId: String
    lateinit var message: String
    lateinit var pic: String
    var scroll by mutableStateOf(false)
        private set

    fun reset() {
        scroll = false
    }

    class Delivery(
        val id: String = java.util.UUID.randomUUID().toString(),
        val uid: String,
        val text: String,
        var picture: String,
        val preview: Uri? = null,
    ) {
        var sending by mutableStateOf(true)
        var error by mutableStateOf<String?>(null)
        var unconfirmed by mutableStateOf(false)
    }

    val deliveries = mutableStateListOf<Delivery>()

    fun onSendMessage(uid: String, text: String, url: String, preview: Uri? = null) {
        if (
            accountRevision != CookieUtil.accountSession.revision ||
                (text.isBlank() && url.isBlank())
        )
            return
        val delivery = Delivery(uid = uid, text = text, picture = url, preview = preview)
        deliveries.add(0, delivery)
        scroll = true
        send(delivery)
    }

    fun onSendImage(uid: String, uri: Uri) {
        if (accountRevision != CookieUtil.accountSession.revision) return
        val delivery = Delivery(uid = uid, text = "", picture = "", preview = uri)
        deliveries.add(0, delivery)
        scroll = true
        send(delivery)
    }

    fun retry(delivery: Delivery) {
        if (!delivery.sending) send(delivery)
    }

    fun discard(delivery: Delivery) {
        if (!delivery.sending) deliveries.remove(delivery)
    }

    fun retryImage(id: String) {
        imageRequests.remove(id)
        onGetImageUrl(id)
    }

    private fun send(delivery: Delivery) {
        if (accountRevision != CookieUtil.accountSession.revision) return
        delivery.sending = true
        delivery.error = null
        viewModelScope.launch {
            var requestStarted = false
            try {
                if (delivery.preview != null && delivery.picture.isEmpty()) {
                    delivery.picture =
                        imageUploader.upload(delivery.preview, delivery.uid) {
                            accountRevision == CookieUtil.accountSession.revision
                        }
                }
                requestStarted = true
                val result =
                    networkRepo
                        .sendMessage(
                            delivery.uid,
                            MultipartBody.Part.createFormData("message", delivery.text),
                            MultipartBody.Part.createFormData("message_pic", delivery.picture),
                        )
                        .first()
                val data = result.getOrThrow()
                val rows =
                    data.data.orEmpty().filter {
                        it.entityType == "message" && !it.id.isNullOrBlank()
                    }
                if (rows.isEmpty()) throw java.io.IOException("Message delivery is unconfirmed")
                if (accountRevision != CookieUtil.accountSession.revision) return@launch
                val previous = (loadingState as? LoadingState.Success)?.response.orEmpty()
                loadingState =
                    LoadingState.Success(
                        (rows + previous).distinctBy { it.entityType to (it.id ?: it.entityId) },
                    )
                deliveries.remove(delivery)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                delivery.unconfirmed =
                    requestStarted &&
                        (e !is com.example.c001apk.compose.logic.network.ApiFailure ||
                            e.kind in
                                listOf(
                                    com.example.c001apk.compose.logic.network.ApiFailureKind.HTTP,
                                    com.example.c001apk.compose.logic.network.ApiFailureKind
                                        .INVALID_RESPONSE,
                                ))
                delivery.error =
                    if (delivery.unconfirmed) AppLanguage.getString(R.string.chat_delivery_unknown)
                    else com.example.c001apk.compose.logic.network.ApiFailures.safeMessage(e)
            } finally {
                delivery.sending = false
            }
        }
    }

    override fun handleLoadMore(response: List<HomeFeedResponse.Data>) = response.distinctBy {
        it.entityType to (it.id ?: it.entityId)
    }

    val recentEmojiData = recentEmojiRepo.loadAllListFlow()

    fun updateRecentEmoji(data: String, size: Int, last: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (recentEmojiRepo.checkEmoji(data)) {
                recentEmojiRepo.updateEmoji(data)
            } else {
                if (size == 27)
                    last?.let {
                        recentEmojiRepo.updateEmoji(it, data)
                    }
                else recentEmojiRepo.insertEmoji(StringEntity(data))
            }
        }
    }
}
