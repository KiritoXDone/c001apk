package com.example.c001apk.compose.ui.feed.reply

import androidx.lifecycle.ViewModel
import com.example.c001apk.compose.logic.repository.RecentAtUserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AtTopicViewModel @Inject constructor(
    private val recentAtUserRepo: RecentAtUserRepo,
) : ViewModel() {
    val recentUsers = recentAtUserRepo.loadAllListFlow()

    suspend fun rememberUser(username: String, avatar: String) {
        recentAtUserRepo.rememberUser(username, avatar)
    }
}
