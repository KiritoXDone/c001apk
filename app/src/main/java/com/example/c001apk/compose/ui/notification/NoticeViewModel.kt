package com.example.c001apk.compose.ui.notification

import com.example.c001apk.compose.logic.filter.ContentFilterStore
import com.example.c001apk.compose.logic.network.ApiFailures
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.constant.Constants.EMPTY_STRING
import com.example.c001apk.compose.logic.repository.BlackListRepo
import com.example.c001apk.compose.logic.repository.NetworkRepo
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.base.BaseViewModel
import com.example.c001apk.compose.ui.ffflist.FFFContentViewModel.ActionType
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Created by bggRGjQaUbCoE on 2024/6/13 */
@HiltViewModel(assistedFactory = NoticeViewModel.ViewModelFactory::class)
class NoticeViewModel
@AssistedInject
constructor(
    @Assisted val url: String,
    networkRepo: NetworkRepo,
    blackListRepo: BlackListRepo,
    contentFilters: ContentFilterStore,
) : BaseViewModel(networkRepo, blackListRepo, contentFilters) {

    @AssistedFactory
    interface ViewModelFactory {
        fun create(url: String): NoticeViewModel
    }

    init {
        fetchData()
    }

    override suspend fun customFetchData() = networkRepo.getMessage(url, page, lastItem)

    var isTop = 0
    lateinit var ukey: String

    private val busy = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

    fun onHandleMessage(actionType: ActionType) {
        val targetKey = ukey
        val wasTop = isTop
        if (!busy.add(targetKey)) return
        val url =
            when (actionType) {
                ActionType.TOP -> "/v6/message/${if (wasTop == 1) "removeTop" else "addTop"}"
                ActionType.DELETE -> "/v6/message/deleteChat"
                ActionType.DELETE_ALL -> EMPTY_STRING
            }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                networkRepo.deleteMessage(url, targetKey, null).collect { result ->
                    val data = result.getOrNull()
                    if (data != null) {
                        if (!data.message.isNullOrEmpty()) {
                            toastText = data.message
                        } else if (!data.data?.count.isNullOrBlank()) {
                            var response =
                                (loadingState as? LoadingState.Success)?.response.orEmpty()
                            response =
                                when (actionType) {
                                    ActionType.TOP -> {
                                        if (wasTop == 0) {
                                            val actionItem = response.find { it.ukey == targetKey }
                                            response.toMutableList().also {
                                                it.remove(actionItem)
                                                actionItem?.let { item ->
                                                    item.isTop = 1
                                                    it.add(0, item)
                                                }
                                            }
                                        } else {
                                            response.map {
                                                if (it.ukey == targetKey) it.copy(isTop = 0) else it
                                            }
                                        }
                                    }

                                    ActionType.DELETE -> response.filterNot { it.ukey == targetKey }
                                    ActionType.DELETE_ALL -> response
                                }
                            loadingState = LoadingState.Success(response)
                            toastText = data.data.count
                        }
                    } else {
                        toastText = ApiFailures.safeMessage(result.exceptionOrNull())
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
            } finally {
                busy.remove(targetKey)
            }
        }
    }

    fun deleteNotice(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postDelete("/v6/notification/delete", id).collect { result ->
                val data = result.getOrNull()
                val error = data?.message
                if (!error.isNullOrBlank()) toastText = error
                else if (data?.data?.count?.contains("成功") == true) {
                    (loadingState as? LoadingState.Success)?.let { current ->
                        loadingState =
                            LoadingState.Success(current.response.filterNot { it.id == id })
                    }
                    toastText = data.data.count
                } else {
                    toastText = ApiFailures.safeMessage(result.exceptionOrNull())
                }
            }
        }
    }

    fun resetUnRead(ukey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            var response = (loadingState as LoadingState.Success).response
            response = response.map { item ->
                if (item.ukey == ukey) item.copy(unreadNum = 0) else item
            }
            loadingState = LoadingState.Success(response)
        }
    }
}
