package com.example.c001apk.compose.ui.feed.reply

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.model.RecentAtUser
import com.example.c001apk.compose.logic.state.FooterState
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.component.CoilLoader
import com.example.c001apk.compose.ui.component.MiuixActionButton
import com.example.c001apk.compose.ui.component.cards.AppCard
import com.example.c001apk.compose.ui.component.cards.AppCardType
import com.example.c001apk.compose.ui.component.cards.LoadingCard
import com.example.c001apk.compose.ui.ffflist.FFFContentViewModel
import com.example.c001apk.compose.ui.theme.cardBg
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal data class AtUser(val username: String, val avatar: String)

internal fun HomeFeedResponse.Data.asAtUser(): AtUser? {
    val contacts = entityType == "contacts"
    val name = if (contacts) {
        userInfo?.username ?: fUserInfo?.username ?: username
    } else {
        username ?: userInfo?.username ?: fUserInfo?.username
    }
    if (name.isNullOrBlank()) return null
    val avatar = if (contacts) {
        userInfo?.userAvatar ?: fUserInfo?.userAvatar ?: userAvatar
    } else {
        userAvatar ?: userInfo?.userAvatar ?: fUserInfo?.userAvatar
    }
    return AtUser(name, avatar.orEmpty())
}

@Composable
internal fun FollowUserList(
    uid: String?,
    recentUsers: List<RecentAtUser>,
    onSelectUser: (AtUser) -> Unit,
) {
    val viewModel =
        hiltViewModel<FFFContentViewModel, FFFContentViewModel.ViewModelFactory>(
            key = "at_follow_user_$uid",
        ) { factory ->
            factory.create(
                url = "/v6/user/followList",
                uid = uid,
                id = null,
                showDefault = null,
            )
        }
    val state = viewModel.loadingState
    val recentNames = recentUsers.mapTo(mutableSetOf()) { it.username }
    val followedUsers = (state as? LoadingState.Success)?.response.orEmpty().filter {
        val user = it.asAtUser()
        it.entityType in listOf("user", "contacts") && user != null && user.username !in recentNames
    }
    val footerState = viewModel.footerState
    val showFollowSection = recentUsers.isEmpty() || state !is LoadingState.Success ||
        followedUsers.isNotEmpty() || !viewModel.stopAutomaticLoading || footerState is FooterState.Error

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 6.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (recentUsers.isNotEmpty()) {
            item(key = "recent_title") { UserSectionTitle("最近 @") }
            items(recentUsers, key = { "recent_user:${it.username}" }) { user ->
                RecentAtUserRow(user) { onSelectUser(AtUser(user.username, user.avatar)) }
            }
        }
        if (showFollowSection) {
            item(key = "follow_title") { UserSectionTitle("关注用户") }
            when (state) {
                is LoadingState.Success -> {
                    itemsIndexed(followedUsers) { index, item ->
                        val user = item.asAtUser()
                        val cardType = if (item.entityType == "contacts") AppCardType.CONTACTS else AppCardType.USER
                        AppCard(
                            data = item,
                            onOpenLink = { _, _ -> user?.let(onSelectUser) },
                            appCardType = cardType,
                            onViewUser = { user?.let(onSelectUser) },
                            onFollowUser = null,
                        )
                        if (index == followedUsers.lastIndex && !viewModel.stopAutomaticLoading) {
                            LaunchedEffect(state.response.size) { viewModel.loadMore() }
                        }
                    }
                    if (followedUsers.isEmpty() && viewModel.stopAutomaticLoading && footerState !is FooterState.Error) {
                        item(key = "follow_empty") { LoadingCard(state = LoadingState.Empty) }
                    }
                    if (followedUsers.isEmpty() && !viewModel.stopAutomaticLoading && footerState == FooterState.Success) {
                        item(key = "follow_more") {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                MiuixActionButton(onClick = viewModel::loadMore) { Text("加载更多") }
                            }
                        }
                    }
                    item(key = "follow_footer") {
                        LoadingCard(
                            state = footerState,
                            onClick = if (footerState is FooterState.Error) viewModel::loadMore else null,
                        )
                    }
                }
                else -> item(key = "follow_state") {
                    LoadingCard(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        state = state,
                        onClick = if (state is LoadingState.Loading) null else viewModel::loadMore,
                    )
                }
            }
        }
    }
}

@Composable
private fun UserSectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        style = MiuixTheme.textStyles.headline2.copy(fontWeight = FontWeight.Medium),
    )
}

@Composable
private fun RecentAtUserRow(user: RecentAtUser, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
            .squircleClip(16.dp).background(cardBg()).clickable(onClick = onClick).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val avatarModifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp))
        if (user.avatar.isBlank()) {
            Box(avatarModifier.background(MiuixTheme.colorScheme.surfaceContainer))
        } else {
            CoilLoader(modifier = avatarModifier, url = user.avatar)
        }
        Text(
            text = user.username,
            modifier = Modifier.padding(start = 10.dp),
            style = MiuixTheme.textStyles.body1,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
