package com.example.c001apk.compose.ui.message

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import com.example.c001apk.compose.ui.component.pullToRefreshTexts
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.cards.MessageFFFCard
import com.example.c001apk.compose.ui.component.cards.MessageHeaderCard
import com.example.c001apk.compose.ui.component.cards.MessageLevelCard
import com.example.c001apk.compose.ui.component.cards.MessageWidgetCard
import com.example.c001apk.compose.ui.component.cards.ProfileLayoutDefaults
import com.example.c001apk.compose.ui.component.TopBarScrollEffect
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.makeToast
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.ContactsBook
import top.yukonga.miuix.kmp.icon.glass.SettingsOutline

@Composable
fun MessageScreen(
    bottomPadding: Dp = 0.dp,
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
    val navigationInset = with(LocalDensity.current) {
        WindowInsets.navigationBars.getBottom(this).toDp()
    }
    val pageBottomSpacing = when {
        navigationInset > 0.dp -> 12.dp
        bottomPadding > 0.dp -> 24.dp
        else -> 20.dp
    }
    val viewModel =
        hiltViewModel<MessageViewModel, MessageViewModel.ViewModelFactory> { factory ->
            factory.create(url = "/v6/notification/list")
        }
    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        if (prefs.isLogin) viewModel.refreshSilently()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            com.example.c001apk.compose.ui.component.MiuixTopBar(
                title = stringResource(R.string.profile),
                actions = {
                    com.example.c001apk.compose.ui.component.MiuixToolbarIconButton(
                        onClick = onAccountsClick,
                    ) {
                        top.yukonga.miuix.kmp.basic.Icon(
                            MiuixIcons.Glass.ContactsBook,
                            contentDescription = stringResource(R.string.login_account),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    com.example.c001apk.compose.ui.component.MiuixToolbarIconButton(onClick = onSettingsClick) {
                        top.yukonga.miuix.kmp.basic.Icon(
                            MiuixIcons.Glass.SettingsOutline,
                            contentDescription = stringResource(R.string.settings),
                            modifier = Modifier.size(24.dp),
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
                contentPadding = PaddingValues(top = paddingValues.calculateTopPadding() + 10.dp),
                refreshTexts = pullToRefreshTexts(),
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
                                top = paddingValues.calculateTopPadding() + 10.dp,
                                bottom = maxOf(
                                    bottomPadding,
                                    paddingValues.calculateBottomPadding(),
                                    navigationInset,
                                ) + pageBottomSpacing,
                            ),
                    ) {
                        item(key = "profile") {
                            MessageHeaderCard(
                                modifier =
                                    Modifier.padding(horizontal = ProfileLayoutDefaults.HorizontalMargin)
                                        .padding(bottom = ProfileLayoutDefaults.SectionSpacing),
                                isLogin = prefs.isLogin,
                                userAvatar = prefs.userAvatar,
                                userName = prefs.username,
                                onLogin = onLogin,
                                onViewUser = onViewUser,
                            )
                        }
                        if (prefs.isLogin) {
                            item(key = "fff") {
                                MessageFFFCard(
                                    modifier =
                                        Modifier.padding(horizontal = ProfileLayoutDefaults.HorizontalMargin)
                                            .padding(bottom = ProfileLayoutDefaults.SectionSpacing),
                                    fffList = viewModel.fffList,
                                    onViewFFFList = onViewFFFList,
                                )
                            }
                            item(key = "level") {
                                MessageLevelCard(
                                    modifier = Modifier.padding(horizontal = ProfileLayoutDefaults.HorizontalMargin)
                                        .padding(bottom = ProfileLayoutDefaults.SectionSpacing),
                                    level = prefs.level,
                                    experience = prefs.experience,
                                    nextLevelExperience = prefs.nextLevelExperience,
                                )
                            }
                        }
                        item(key = "messages") {
                            MessageSection(
                                modifier =
                                    Modifier.padding(horizontal = ProfileLayoutDefaults.HorizontalMargin)
                                        .padding(bottom = ProfileLayoutDefaults.SectionSpacing),
                                counts = viewModel.badgeCounts,
                                onViewNotice = { type ->
                                    if (!prefs.isLogin) onLogin()
                                    else {
                                        viewModel.clearBadge(type)
                                        onViewNotice(type.name)
                                    }
                                },
                            )
                        }
                        item(key = "toolsTitle") { ProfileSectionTitle(stringResource(R.string.profile_tools)) }
                        item(key = "widget") {
                            MessageWidgetCard(
                                modifier = Modifier.padding(horizontal = ProfileLayoutDefaults.HorizontalMargin),
                                onViewFFFList = onViewFFFList,
                                onViewHistory = onViewHistory,
                            )
                        }
                    }
                }
            }
        }
    }

    viewModel.toastText?.let {
        context.makeToast(it)
        viewModel.resetToastText()
    }
}

@Composable
private fun ProfileSectionTitle(title: String) {
    SmallTitle(
        title,
        modifier = Modifier.padding(bottom = ProfileLayoutDefaults.SectionSpacing),
        textColor = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.primary,
        insideMargin = PaddingValues(horizontal = 28.dp, vertical = 4.dp),
    )
}
