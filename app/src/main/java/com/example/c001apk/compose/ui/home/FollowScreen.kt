package com.example.c001apk.compose.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.FollowType
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.*
import com.example.c001apk.compose.ui.follow.FollowSettingsViewModel
import com.example.c001apk.compose.ui.follow.LocalFollowViewModel
import com.example.c001apk.compose.ui.follow.LocalFollowHeader
import com.example.c001apk.compose.ui.home.feed.HomeFeedScreen
import com.example.c001apk.compose.util.ReportType
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.icon.extended.ChevronForward

@Composable
fun FollowScreen(
    bottomPadding: Dp,
    refreshState: Boolean,
    resetRefreshState: () -> Unit,
    onSearch: () -> Unit,
    onLogin: () -> Unit,
    onManage: () -> Unit,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onReport: (String, ReportType) -> Unit,
) {
    val prefs = LocalUserPreferences.current
    val settings = hiltViewModel<FollowSettingsViewModel>()
    val labels = mapOf(1 to "用户关注", 2 to "话题关注", 3 to "数码关注", 4 to "应用关注", 0 to "全部关注", 5 to "悄悄关注")
    val local = !prefs.isLogin || prefs.followSelection == 5
    val label = if (!prefs.isLogin) "本地关注" else labels[prefs.followSelection] ?: "用户关注"
    var menu by remember { mutableStateOf(false) }
    MiuixScaffold(
        topBar = {
            MiuixHeader(
                centerTitle = true,
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Box {
                            Row(
                                Modifier.clickable(enabled = prefs.isLogin, role = Role.Button) { menu = true }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(label)
                                if (prefs.isLogin) Icon(MiuixIcons.ChevronForward, null,
                                    Modifier.size(12.dp).rotate(90f))
                            }
                            MiuixMenu(expanded = menu, onDismissRequest = { menu = false }, contentScale = 1f) {
                                labels.forEach { (value, title) ->
                                    BasicComponent(
                                        title = title,
                                        summary = if (prefs.followSelection == value) "当前分类" else null,
                                        onClick = {
                                            menu = false
                                            settings.select(value)
                                        },
                                    )
                                }
                            }
                        }
                    }
                },
                actions = { IconButton(onClick = onSearch) { Icon(MiuixIcons.Search, "搜索") } },
            )
        },
    ) { padding ->
        if (local) {
            val model = hiltViewModel<LocalFollowViewModel>()
            val localHeader: @Composable () -> Unit = {
                LocalFollowHeader(warning = model.warning, onManage = onManage, onRetry = model::loadMore)
            }
            if (prefs.localFollows.isEmpty()) {
                Column(
                    Modifier.fillMaxSize()
                        .verticalScroll(rememberTopBarScrollState())
                        .padding(
                            top = padding.calculateTopPadding(),
                            bottom = bottomPadding + padding.calculateBottomPadding(),
                        ),
                ) {
                    localHeader()
                    Text(
                        if (prefs.isLogin) "在设置中开启悄悄关注后，长按用户主页的关注按钮即可添加。"
                        else "在用户主页点击“本地关注”，即可在这里查看动态。",
                        Modifier.padding(horizontal = 28.dp, vertical = 12.dp),
                        style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.body2,
                        color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            } else {
                CommonScreen(
                    viewModel = model,
                    refreshState = refreshState,
                    resetRefreshState = resetRefreshState,
                    paddingValues =
                        PaddingValues(
                            top = padding.calculateTopPadding(),
                            bottom = bottomPadding + padding.calculateBottomPadding(),
                        ),
                    needTopPadding = true,
                    listHeader = localHeader,
                    onViewUser = onViewUser,
                    onViewFeed = onViewFeed,
                    onOpenLink = onOpenLink,
                    onCopyText = onCopyText,
                    onReport = onReport,
                )
            }
        } else
            key(prefs.followSelection) {
                HomeFeedScreen(
                    paddingValues =
                        PaddingValues(
                            top = padding.calculateTopPadding(),
                            bottom = bottomPadding + padding.calculateBottomPadding(),
                        ),
                    type = TabType.FOLLOW,
                    followType = FollowType.forNumber(prefs.followSelection) ?: FollowType.USER,
                    refreshState = refreshState,
                    resetRefreshState = resetRefreshState,
                    onViewUser = onViewUser,
                    onViewFeed = onViewFeed,
                    onOpenLink = onOpenLink,
                    onCopyText = onCopyText,
                    onReport = onReport,
                )
            }
    }
}
