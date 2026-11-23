package com.example.c001apk.compose.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
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
import top.yukonga.miuix.kmp.icon.basic.Search
import top.yukonga.miuix.kmp.icon.glass.ChevronForward
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
    val labels = mapOf(
        1 to R.string.follow_users,
        2 to R.string.follow_topics,
        3 to R.string.follow_products,
        4 to R.string.follow_apps,
        0 to R.string.follow_all,
        5 to R.string.user_quiet_follow,
    )
    val local = !prefs.isLogin || prefs.followSelection == 5
    val label = stringResource(if (!prefs.isLogin) R.string.follow_local_title else labels[prefs.followSelection] ?: R.string.follow_users)
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
                                if (prefs.isLogin) Icon(MiuixIcons.Glass.ChevronForward, null,
                                    Modifier.size(12.dp).rotate(90f))
                            }
                            MiuixMenu(expanded = menu, onDismissRequest = { menu = false }, contentScale = 1f) {
                                labels.forEach { (value, title) ->
                                    MiuixMenuItem(
                                        text = {
                                            Column {
                                                Text(stringResource(title))
                                                if (prefs.followSelection == value) {
                                                    Text(
                                                        stringResource(R.string.follow_current_category),
                                                        style = MiuixTheme.textStyles.body2,
                                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                    )
                                                }
                                            }
                                        },
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
                actions = { MiuixToolbarIconButton(onClick = onSearch) { Icon(MiuixIcons.Basic.Search, stringResource(R.string.search_hint)) } },
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
                        stringResource(if (prefs.isLogin) R.string.follow_quiet_hint else R.string.follow_local_hint),
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
