package com.example.c001apk.compose.ui.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.R
import com.example.c001apk.compose.constant.Constants.EMPTY_STRING
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.MiuixMenu as DropdownMenu
import com.example.c001apk.compose.ui.component.MiuixMenuItem as DropdownMenuItem
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.MiuixScaffoldDefaults as ScaffoldDefaults
import com.example.c001apk.compose.ui.component.MiuixTopBar as TopAppBar
import com.example.c001apk.compose.ui.component.MoreMenuButton
import com.example.c001apk.compose.ui.component.TopBarScrollEffect
import com.example.c001apk.compose.ui.component.cards.ChatLeftCard
import com.example.c001apk.compose.ui.component.cards.ChatRightCard
import com.example.c001apk.compose.ui.component.cards.ChatTimeCard
import com.example.c001apk.compose.ui.component.cards.LoadingCard
import com.example.c001apk.compose.ui.theme.cardBg
import com.example.c001apk.compose.util.CookieUtil
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.copyText
import com.example.c001apk.compose.util.makeToast
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Text

/** Created by bggRGjQaUbCoE on 2024/6/19 */
@Composable
fun ChatScreen(
    onBackClick: () -> Unit,
    ukey: String,
    uid: String,
    username: String,
    onViewUser: (String) -> Unit,
    onReport: (String, ReportType) -> Unit,
    onOpenLink: (String, String?) -> Unit,
) {

    val viewModel =
        hiltViewModel<ChatViewModel, ChatViewModel.ViewModelFactory>(key = ukey) { factory ->
            factory.create(ukey = ukey)
        }

    val context = LocalContext.current
    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var retryDelivery by remember { mutableStateOf<ChatViewModel.Delivery?>(null) }
    val lazyListState = rememberLazyListState()
    var clearText by remember { mutableStateOf(false) }
    var clearFocus by remember { mutableStateOf(false) }
    val windowInsets = WindowInsets.navigationBars

    fun handlePickedImage(uri: android.net.Uri) {
        viewModel.onSendImage(uid, uri)
    }

    LaunchedEffect(key1 = viewModel.scroll) {
        if (viewModel.scroll) {
            clearText = true
            delay(150)
            lazyListState.scrollToItem(0)
            viewModel.reset()
        }
    }

    fun onClearFocus() {
        clearFocus = true
    }

    val pickContent =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { uri1 ->
                handlePickedImage(uri1)
            }
        }

    val pickDocument =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { uri1 ->
                handlePickedImage(uri1)
            }
        }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                windowInsets =
                    WindowInsets.systemBars.only(WindowInsetsSides.Start + WindowInsetsSides.Top),
                navigationIcon = {
                    BackButton {
                        clearFocus = true
                        onBackClick()
                    }
                },
                title = username,
                actions = {
                    Box {
                        MoreMenuButton { dropdownMenuExpanded = true }
                        DropdownMenu(
                            expanded = dropdownMenuExpanded,
                            onDismissRequest = { dropdownMenuExpanded = false },
                        ) {
                            listOf(
                                    stringResource(id = R.string.menu_check),
                                    "拉黑",
                                    stringResource(id = R.string.menu_report),
                                )
                                .forEachIndexed { index, menu ->
                                    DropdownMenuItem(
                                        text = { Text(text = menu) },
                                        onClick = {
                                            dropdownMenuExpanded = false
                                            when (index) {
                                                0 -> {
                                                    onClearFocus()
                                                    onViewUser(uid)
                                                }

                                                1 -> viewModel.onBlockUser(uid)
                                                2 -> {
                                                    onClearFocus()
                                                    onReport(uid, ReportType.USER)
                                                }
                                            }
                                        },
                                    )
                                }
                        }
                    }
                },
            )
        },
        contentWindowInsets =
            ScaffoldDefaults.contentWindowInsets
                .exclude(WindowInsets.navigationBars)
                .exclude(WindowInsets.ime),
    ) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .windowInsetsPadding(windowInsets.only(WindowInsetsSides.Start)),
        ) {
            TopBarScrollEffect {
                lazyListState.animateScrollToItem(
                    (lazyListState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0),
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxSize(),
                contentPadding = PaddingValues(top = paddingValues.calculateTopPadding()),
                reverseLayout = true,
                state = lazyListState,
            ) {
                itemsIndexed(viewModel.deliveries, key = { _, item -> "pending-${item.id}" }) {
                    _,
                    delivery ->
                    ChatPendingMessage(
                        delivery = delivery,
                        onRetry = {
                            if (delivery.unconfirmed) retryDelivery = delivery
                            else viewModel.retry(delivery)
                        },
                        onDiscard = { viewModel.discard(delivery) },
                    )
                }
                when (viewModel.loadingState) {
                    LoadingState.Loading,
                    LoadingState.Empty,
                    is LoadingState.Error -> {
                        item(key = "loadingState") {
                            Box(modifier = Modifier.fillParentMaxSize()) {
                                LoadingCard(
                                    modifier =
                                        Modifier.align(Alignment.Center)
                                            .padding(horizontal = 10.dp),
                                    state = viewModel.loadingState,
                                    onClick =
                                        if (viewModel.loadingState is LoadingState.Loading) null
                                        else viewModel::loadMore,
                                )
                            }
                        }
                    }

                    is LoadingState.Success -> {
                        itemsIndexed(
                            items = (viewModel.loadingState as LoadingState.Success).response,
                            key = { _, item -> item.entityId + item.dateline },
                        ) { index, item ->
                            when (item.entityType) {
                                "message" ->
                                    when (item.fromuid) {
                                        CookieUtil.uid ->
                                            ChatRightCard(
                                                data = item,
                                                onGetImageUrl = viewModel::onGetImageUrl,
                                                onLongClick = { id, msg, url ->
                                                    onClearFocus()
                                                    viewModel.deleteId = id
                                                    viewModel.message = msg
                                                    viewModel.pic = url
                                                    showDialog = true
                                                },
                                                onViewUser = { uid ->
                                                    onClearFocus()
                                                    onViewUser(uid)
                                                },
                                                onOpenLink = onOpenLink,
                                                onClearFocus = ::onClearFocus,
                                            )

                                        else ->
                                            ChatLeftCard(
                                                data = item,
                                                onGetImageUrl = viewModel::onGetImageUrl,
                                                onLongClick = { id, msg, url ->
                                                    onClearFocus()
                                                    viewModel.deleteId = id
                                                    viewModel.message = msg
                                                    viewModel.pic = url
                                                    showDialog = true
                                                },
                                                onViewUser = { uid ->
                                                    onClearFocus()
                                                    onViewUser(uid)
                                                },
                                                onOpenLink = onOpenLink,
                                                onClearFocus = ::onClearFocus,
                                            )
                                    }

                                "messageExtra" -> ChatTimeCard(title = item.title.orEmpty())
                            }

                            if (
                                index ==
                                    (viewModel.loadingState as LoadingState.Success)
                                        .response
                                        .lastIndex && !viewModel.isEnd
                            ) {
                                viewModel.loadMore()
                            }
                        }
                    }
                }
            }

            ChatBottom(
                modifier =
                    Modifier.fillMaxWidth()
                        .background(cardBg())
                        .padding(
                            bottom =
                                androidx.compose.animation.core
                                    .animateDpAsState(
                                        targetValue =
                                            with(
                                                androidx.compose.ui.platform.LocalDensity.current,
                                            ) {
                                                androidx.compose.foundation.layout.WindowInsets.ime
                                                    .getBottom(this)
                                                    .toDp()
                                            },
                                        animationSpec =
                                            androidx.compose.animation.core.spring(
                                                stiffness =
                                                    androidx.compose.animation.core.Spring
                                                        .StiffnessHigh,
                                                dampingRatio =
                                                    androidx.compose.animation.core.Spring
                                                        .DampingRatioNoBouncy,
                                            ),
                                        label = "ChatImeAnimation",
                                    )
                                    .value,
                        )
                        .windowInsetsPadding(
                            windowInsets.only(WindowInsetsSides.Start + WindowInsetsSides.Bottom),
                        ),
                onPickImage = {
                    onClearFocus()
                    pickContent.launch("image/*")
                },
                onPickOtherImage = {
                    onClearFocus()
                    pickDocument.launch(arrayOf("image/*"))
                },
                onSendMessage = {
                    viewModel.onSendMessage(uid, it, EMPTY_STRING)
                },
                clearFocus = clearFocus,
                clearText = clearText,
                resetClear = {
                    clearText = false
                },
                resetFocus = {
                    clearFocus = false
                },
                onClearFocus = ::onClearFocus,
                viewModel = viewModel,
            )
        }
    }

    retryDelivery?.let { delivery ->
        com.example.c001apk.compose.ui.component.MiuixAlertDialog(
            onDismissRequest = { retryDelivery = null },
            title = { Text("再次发送这条消息？") },
            text = { Text("上次发送结果未确认，重试可能重复发送。请先查看聊天记录。") },
            confirmButton = {
                com.example.c001apk.compose.ui.component.MiuixDialogButton(
                    onClick = {
                        retryDelivery = null
                        viewModel.retry(delivery)
                    },
                ) {
                    Text("再次发送")
                }
            },
            dismissButton = {
                com.example.c001apk.compose.ui.component.MiuixDialogButton(
                    onClick = { retryDelivery = null },
                ) {
                    Text("取消")
                }
            },
        )
    }

    if (showDialog) {
        com.example.c001apk.compose.ui.component.MiuixAlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("消息操作") },
            text = {
                Column {
                    top.yukonga.miuix.kmp.basic.BasicComponent(
                        title = "删除",
                        onClick = {
                            showDialog = false
                            viewModel.onDeleteMsg()
                        },
                    )
                    if (viewModel.pic.isNotEmpty()) {
                        top.yukonga.miuix.kmp.basic.BasicComponent(
                            title = "重新加载图片",
                            onClick = {
                                showDialog = false
                                viewModel.retryImage(viewModel.deleteId)
                            },
                        )
                    } else {
                        top.yukonga.miuix.kmp.basic.BasicComponent(
                            title = "复制",
                            onClick = {
                                showDialog = false
                                context.copyText(viewModel.message)
                            },
                        )
                    }
                }
            },
            confirmButton = {
                com.example.c001apk.compose.ui.component.MiuixDialogButton(
                    onClick = { showDialog = false },
                    primary = false,
                ) { Text("取消") }
            },
        )
    }

    viewModel.toastText?.let {
        context.makeToast(it)
        viewModel.resetToastText()
    }
}
