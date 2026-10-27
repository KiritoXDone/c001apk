package com.example.c001apk.compose.ui.message

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.MiuixAlertDialog as AlertDialog
import com.example.c001apk.compose.ui.component.MiuixDialogButton as TextButton
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.cards.MessageFFFCard
import com.example.c001apk.compose.ui.component.cards.MessageHeaderCard
import com.example.c001apk.compose.ui.component.cards.MessageListCard
import com.example.c001apk.compose.ui.component.cards.MessageWidgetCard
import com.example.c001apk.compose.ui.component.cards.iconList
import com.example.c001apk.compose.ui.component.cards.titleList
import com.example.c001apk.compose.ui.component.icons.FlutterProfileIcons
import com.example.c001apk.compose.ui.component.TopBarScrollEffect
import com.example.c001apk.compose.ui.notification.NoticeType
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.makeToast
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Text

/** Created by bggRGjQaUbCoE on 2024/6/2 */
@Composable
fun MessageScreen(
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onLogin: () -> Unit,
    onSettingsClick: () -> Unit,
    onAccountsClick: () -> Unit,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onViewFFFList: (String?, String) -> Unit,
    onReport: (String, ReportType) -> Unit,
    onViewNotice: (String) -> Unit,
    onViewHistory: (String) -> Unit,
) {

    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val prefs = LocalUserPreferences.current
    val listState = rememberLazyListState()
    val viewModel =
        hiltViewModel<MessageViewModel, MessageViewModel.ViewModelFactory> { factory ->
            factory.create(url = "/v6/notification/list")
        }
    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        if (prefs.isLogin) viewModel.refreshSilently()
    }
    var showInboxDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            com.example.c001apk.compose.ui.component.MiuixTopBar(
                title = "我的",
                actions = {
                    top.yukonga.miuix.kmp.basic.IconButton(
                        onClick = onAccountsClick,
                    ) {
                        top.yukonga.miuix.kmp.basic.Icon(
                            FlutterProfileIcons.Account,
                            contentDescription = "账号",
                        )
                    }
                    top.yukonga.miuix.kmp.basic.IconButton(onClick = onSettingsClick) {
                        top.yukonga.miuix.kmp.basic.Icon(
                            FlutterProfileIcons.Settings,
                            contentDescription = "设置",
                        )
                    }
                },
            )
        },
        contentWindowInsets =
            com.example.c001apk.compose.ui.component.MiuixScaffoldDefaults.contentWindowInsets
                .exclude(WindowInsets.navigationBars),
    ) { paddingValues ->
        Column(
            Modifier.fillMaxSize()
                .padding(
                    start = paddingValues.calculateLeftPadding(layoutDirection),
                    end = paddingValues.calculateRightPadding(layoutDirection),
                ),
        ) {
            top.yukonga.miuix.kmp.basic.PullToRefresh(
                modifier = Modifier.fillMaxSize(),
                isRefreshing = viewModel.isRefreshing,
                onRefresh = viewModel::refresh,
                contentPadding = PaddingValues(top = paddingValues.calculateTopPadding() + 12.dp),
                refreshTexts = listOf("下拉刷新", "释放刷新", "正在刷新", "刷新完成"),
            ) {
                BoxWithConstraints(Modifier.fillMaxSize()) {
                    val sideInset = ((maxWidth - 800.dp) / 2).coerceAtLeast(0.dp)
                    TopBarScrollEffect { listState.animateScrollToItem(0) }
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding =
                            PaddingValues(
                                start = sideInset,
                                end = sideInset,
                                top = paddingValues.calculateTopPadding() + 12.dp,
                                bottom =
                                    bottomPadding + paddingValues.calculateBottomPadding() + 24.dp,
                            ),
                    ) {
                        item(key = "profile") {
                            MessageHeaderCard(
                                modifier =
                                    Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp),
                                isLogin = prefs.isLogin,
                                userAvatar = prefs.userAvatar,
                                userName = prefs.username,
                                level = prefs.level,
                                experience = prefs.experience,
                                nextLevelExperience = prefs.nextLevelExperience,
                                onLogin = onLogin,
                                onViewUser = onViewUser,
                            )
                        }
                        if (prefs.isLogin) {
                            item(key = "fff") {
                                MessageFFFCard(
                                    modifier =
                                        Modifier.padding(horizontal = 12.dp)
                                            .padding(bottom = 12.dp),
                                    fffList = viewModel.fffList,
                                    onViewFFFList = onViewFFFList,
                                )
                            }
                        }
                        item(key = "toolsTitle") { ProfileSectionTitle("常用功能") }
                        item(key = "widget") {
                            MessageWidgetCard(
                                modifier =
                                    Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp),
                                onViewFFFList = onViewFFFList,
                                onViewHistory = onViewHistory,
                            )
                        }
                        item(key = "messagesTitle") { ProfileSectionTitle("消息") }
                        items(5, key = { "message_$it" }) { index ->
                            MessageListCard(
                                modifier =
                                    Modifier.padding(horizontal = 12.dp)
                                        .padding(bottom = if (index == 4) 12.dp else 0.dp),
                                imageVector = iconList[index],
                                title = titleList[index],
                                count = viewModel.badgeList.getOrNull(index),
                                first = index == 0,
                                last = index == 4,
                                onViewNotice = {
                                    if (!prefs.isLogin) onLogin()
                                    else if (index == 4) showInboxDialog = true
                                    else {
                                        viewModel.clearBadge(index)
                                        onViewNotice(NoticeType.entries[index].name)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    when {
        showInboxDialog -> {
            AlertDialog(
                onDismissRequest = { showInboxDialog = false },
                title = { Text("私信与信息") },
                text = {
                    Column {
                        BasicComponent(
                            title = "私信",
                            onClick = {
                                showInboxDialog = false
                                viewModel.clearBadge(4)
                                onViewNotice(NoticeType.MESSAGE.name)
                            },
                        )
                        BasicComponent(
                            title = "酷安小秘书",
                            summary =
                                if (com.example.c001apk.compose.util.CookieUtil.notification > 0)
                                    "${com.example.c001apk.compose.util.CookieUtil.notification} 条系统通知"
                                else "系统通知与信息",
                            onClick = {
                                showInboxDialog = false
                                com.example.c001apk.compose.util.CookieUtil.notification = 0
                                onViewNotice(NoticeType.NOTIFICATION.name)
                            },
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showInboxDialog = false }) { Text("取消") }
                },
            )
        }
    }

    viewModel.toastText?.let {
        context.makeToast(it)
        viewModel.resetToastText()
    }
}

@Composable
private fun ProfileSectionTitle(title: String) {
    Text(
        title,
        modifier = Modifier.padding(start = 28.dp, top = 6.dp, bottom = 10.dp),
        style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.footnote1,
        color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurfaceVariantSummary,
    )
}
