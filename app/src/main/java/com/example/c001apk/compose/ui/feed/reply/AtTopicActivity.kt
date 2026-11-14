package com.example.c001apk.compose.ui.feed.reply

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.MiuixHeader as TopAppBar
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.search.SearchContentScreen
import com.example.c001apk.compose.ui.search.SearchFeedType
import com.example.c001apk.compose.ui.search.SearchOrderType
import com.example.c001apk.compose.ui.search.SearchType
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import com.example.c001apk.compose.util.CookieUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField as OutlinedTextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.theme.MiuixTheme

@AndroidEntryPoint
class AtTopicActivity : ComponentActivity() {
    private val viewModel: AtTopicViewModel by viewModels()
    private var selectingUser = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val type = intent.getStringExtra("type") ?: "user"
        setContent {
            C001apkComposeTheme(
                darkTheme = CookieUtil.isDarkMode,
                themeType = CookieUtil.themeType,
                seedColor = CookieUtil.seedColor,
                materialYou = CookieUtil.materialYou,
                pureBlack = CookieUtil.pureBlack,
                paletteStyle = CookieUtil.paletteStyle,
                fontScale = CookieUtil.fontScale,
                contentScale = CookieUtil.contentScale,
            ) {
                AtTopicScreen(
                    type = type,
                    viewModel = viewModel,
                    onBack = { finish() },
                    onSelect = ::finishWithSelection,
                    onSelectUser = ::selectUser,
                )
            }
        }
    }

    private fun selectUser(user: AtUser) {
        if (selectingUser) return
        selectingUser = true
        lifecycleScope.launch {
            try {
                viewModel.rememberUser(user.username, user.avatar)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.e("AtTopic", "Failed to save recent user", error)
                Toast.makeText(this@AtTopicActivity, "无法保存最近 @ 用户", Toast.LENGTH_SHORT).show()
            }
            finishWithSelection("@${user.username} ")
        }
    }

    private fun finishWithSelection(selected: String) {
        setResult(RESULT_OK, Intent().putExtra("data", selected))
        finish()
    }
}

@Composable
private fun AtTopicScreen(
    type: String,
    viewModel: AtTopicViewModel,
    onBack: () -> Unit,
    onSelect: (String) -> Unit,
    onSelectUser: (AtUser) -> Unit,
) {
    val isTopic = type == "topic"
    var keyword by rememberSaveable { mutableStateOf("") }
    val title = if (isTopic) "搜索话题" else "搜索用户"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton { onBack() } },
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            AtTopicSearchField(keyword, title) { keyword = it }

            if (keyword.isBlank()) {
                if (isTopic) {
                    Text(
                        text = "热门话题",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        style =
                            MiuixTheme.textStyles.headline2.copy(
                                fontWeight = FontWeight.Medium,
                            ),
                    )
                    HotTopicList(onSelect = onSelect)
                } else {
                    val recentUsers by viewModel.recentUsers.collectAsStateWithLifecycle(initialValue = emptyList())
                    FollowUserList(
                        uid = CookieUtil.uid,
                        recentUsers = recentUsers,
                        onSelectUser = onSelectUser,
                    )
                }
            } else {
                SearchContentScreen(
                    searchType = if (isTopic) SearchType.TOPIC else SearchType.USER,
                    keyword = keyword,
                    pageType = null,
                    pageParam = null,
                    refreshState = false,
                    resetRefreshState = {},
                    feedType = SearchFeedType.ALL,
                    orderType = SearchOrderType.DATELINE,
                    paddingValues = PaddingValues(0.dp),
                    onViewUser = {},
                    onViewFeed = { _, _ -> },
                    onOpenLink = { _, itemTitle ->
                        val targetTitle = itemTitle.orEmpty().trim()
                        if (targetTitle.isNotEmpty()) {
                            if (isTopic) onSelect("#$targetTitle# ")
                            else onSelectUser(AtUser(targetTitle, ""))
                        }
                    },
                    onCopyText = {},
                    updateInitPage = {},
                    onReport = { _, _ -> },
                    onSelectUser = if (isTopic) null else { item ->
                        item.asAtUser()?.let(onSelectUser)
                    },
                )
            }
        }
    }
}

@Composable
internal fun AtTopicSearchField(keyword: String, title: String, onKeywordChange: (String) -> Unit) {
    OutlinedTextField(
        value = keyword,
        onValueChange = onKeywordChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("at_search_field"),
        singleLine = true,
        label = title,
        useLabelAsPlaceholder = true,
        leadingIcon = {
            Icon(
                MiuixIcons.Search,
                contentDescription = null,
                modifier = Modifier.padding(start = 16.dp, end = 10.dp).size(20.dp)
                    .testTag("at_search_icon"),
            )
        },
    )
}

@Composable
private fun HotTopicList(
    onSelect: (String) -> Unit,
) {
    SearchContentScreen(
        searchType = SearchType.TOPIC,
        keyword = "热门",
        pageType = null,
        pageParam = null,
        refreshState = false,
        resetRefreshState = {},
        feedType = SearchFeedType.ALL,
        orderType = SearchOrderType.DATELINE,
        paddingValues = PaddingValues(0.dp),
        onViewUser = {},
        onViewFeed = { _, _ -> },
        onOpenLink = { _, itemTitle ->
            val targetTitle = itemTitle.orEmpty().trim()
            if (targetTitle.isNotEmpty()) {
                onSelect("#$targetTitle# ")
            }
        },
        onCopyText = {},
        updateInitPage = {},
        onReport = { _, _ -> },
    )
}
