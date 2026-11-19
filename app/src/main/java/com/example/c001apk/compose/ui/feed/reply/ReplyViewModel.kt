package com.example.c001apk.compose.ui.feed.reply

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.model.OSSUploadPrepareModel
import com.example.c001apk.compose.logic.model.OSSUploadPrepareResponse
import com.example.c001apk.compose.logic.model.StringEntity
import com.example.c001apk.compose.logic.model.VOTE_COMMENT_TYPE
import com.example.c001apk.compose.logic.network.ApiFailures
import com.example.c001apk.compose.logic.repository.NetworkRepo
import com.example.c001apk.compose.logic.repository.RecentEmojiRepo
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReplyViewModel @Inject constructor(
    private val recentEmojiRepo: RecentEmojiRepo,
    private val networkRepo: NetworkRepo
) : ViewModel() {

    val recentList = recentEmojiRepo.loadAllListFlow()

    var isInit = true
    var type: String? = null
    var rid: String? = null
    internal var voteCommentTarget: VoteCommentTarget? = null

    var responseData: HomeFeedResponse.Data? = null
    var toastText = MutableStateFlow<String?>(null)
        private set

    fun resetToast() {
        toastText.value = null
    }

    var replyAndFeedData = HashMap<String, String>()
    fun onPostReply() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postReply(replyAndFeedData, rid.toString(), type.toString())
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data != null) {
                            responseData = response.data
                            postFinished.value = true
                        } else {
                            toastText.value = response.message
                            if (response.messageStatus == "err_request_captcha") {
                                onGetValidateCaptcha()
                            }
                        }
                    } else {
                        toastText.value = result.exceptionOrNull()?.message ?: "response is null"
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    lateinit var requestValidateData: HashMap<String, String?>
    fun onPostRequestValidate() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postRequestValidate(requestValidateData)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data != null) {
                            toastText.value = response.data.count
                            if (response.data.count == "验证通过") {
                                postCurrentDraft()
                            }
                        } else if (response.message != null) {
                            toastText.value = response.message
                            if (response.message == "请输入正确的图形验证码") {
                                onGetValidateCaptcha()
                            }
                        }
                    } else {
                        toastText.value = result.exceptionOrNull()?.message ?: "response is null"
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }


    var captchaImg = MutableStateFlow<Bitmap?>(null)
        private set

    fun resetCaptcha() {
        captchaImg.value = null
    }

    private fun onGetValidateCaptcha() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getValidateCaptcha("/v6/account/captchaImage?${System.currentTimeMillis() / 1000}&w=270=&h=113")
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        val responseBody = response.body()
                        val bitmap = BitmapFactory.decodeStream(responseBody?.byteStream())
                        captchaImg.value = bitmap
                    } else {
                        toastText.value = result.exceptionOrNull()?.message ?: "response is null"
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    var postFinished = MutableStateFlow(false)
    private var createFeedJob: Job? = null

    fun postCurrentDraft() {
        when (type) {
            "createFeed", VOTE_COMMENT_TYPE -> onPostCreateFeed()
            else -> onPostReply()
        }
    }

    @Synchronized
    private fun onPostCreateFeed() {
        if (postFinished.value || createFeedJob?.isActive == true) return
        val voteTarget = voteCommentTarget
        if (type == VOTE_COMMENT_TYPE && voteTarget?.isCurrentAccount() != true) {
            toastText.value = "账号已切换，请重新打开投票"
            return
        }
        val draft = HashMap(replyAndFeedData)
        createFeedJob = viewModelScope.launch(Dispatchers.IO) {
            val submission = if (voteTarget == null) {
                networkRepo.postCreateFeed(draft)
            } else {
                networkRepo.postVoteComment(
                    voteId = voteTarget.voteId,
                    message = draft["message"].orEmpty(),
                    pictureUrls = draft["pic"].orEmpty(),
                    anonymous = voteTarget.anonymous,
                    accountRevision = voteTarget.accountRevision,
                )
            }
            submission
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (!response.data?.id.isNullOrBlank() && (response.error ?: 0) == 0) {
                            postFinished.value = true
                        } else {
                            toastText.value = response.message ?: "发布失败，请重试"
                            if (response.messageStatus == "err_request_captcha") {
                                onGetValidateCaptcha()
                            }
                        }
                    } else {
                        toastText.value = (result.exceptionOrNull() as? Exception)
                            ?.let(ApiFailures::safeMessage) ?: "发布失败，请重试"
                    }
                }
        }
    }

    var size = 0
    var last: String? = null
    fun updateRecentEmoji(data: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (recentEmojiRepo.checkEmoji(data)) {
                recentEmojiRepo.updateEmoji(data)
            } else {
                if (size == 27)
                    last?.let {
                        recentEmojiRepo.updateEmoji(it, data)
                    }
                else
                    recentEmojiRepo.insertEmoji(StringEntity(data))
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            recentEmojiRepo.deleteAll()
        }
    }

    var uploadImage = MutableStateFlow<OSSUploadPrepareResponse.Data?>(null)
        private set

    fun resetUpload() {
        uploadImage.value = null
    }

    fun onPostOSSUploadPrepare(imageList: List<OSSUploadPrepareModel>) {
        val ossUploadPrepareData = hashMapOf(
            "uploadBucket" to "image",
            "uploadDir" to "feed",
            "is_anonymous" to "0",
            "uploadFileList" to Gson().toJson(imageList),
            "toUid" to ""
        )
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postOSSUploadPrepare(ossUploadPrepareData)
                .collect { result ->
                    val data = result.getOrNull()
                    if (data != null) {
                        if (data.message != null) {
                            toastText.value = data.message
                        } else if (data.data != null) {
                            uploadImage.value = data.data
                        }
                    } else {
                        toastText.value = result.exceptionOrNull()?.message ?: "response is null"
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

}
