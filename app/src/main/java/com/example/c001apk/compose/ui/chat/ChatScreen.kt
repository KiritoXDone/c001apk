package com.example.c001apk.compose.ui.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.res.ResourcesCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.c001apk.compose.R
import com.example.c001apk.compose.constant.Constants.EMPTY_STRING
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.ChatEditText
import com.example.c001apk.compose.ui.component.MiuixMenu as DropdownMenu
import com.example.c001apk.compose.ui.component.MiuixMenuItem as DropdownMenuItem
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.MiuixScaffoldDefaults as ScaffoldDefaults
import com.example.c001apk.compose.ui.component.MiuixTopBar as TopAppBar
import com.example.c001apk.compose.ui.component.MoreMenuButton
import com.example.c001apk.compose.ui.component.cards.CardIndicator
import com.example.c001apk.compose.ui.component.cards.ChatLeftCard
import com.example.c001apk.compose.ui.component.cards.ChatRightCard
import com.example.c001apk.compose.ui.component.cards.ChatTimeCard
import com.example.c001apk.compose.ui.component.cards.LoadingCard
import com.example.c001apk.compose.ui.component.icons.Backspace
import com.example.c001apk.compose.ui.component.icons.EmojiEmotions
import com.example.c001apk.compose.ui.component.TopBarScrollEffect
import com.example.c001apk.compose.ui.theme.cardBg
import com.example.c001apk.compose.util.CookieUtil
import com.example.c001apk.compose.util.EmojiUtils
import com.example.c001apk.compose.util.EmojiUtils.coolBList
import com.example.c001apk.compose.util.EmojiUtils.emojiList
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.copyText
import com.example.c001apk.compose.util.makeToast
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card as ElevatedCard
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.VerticalDivider
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Image
import top.yukonga.miuix.kmp.icon.extended.Send
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
    var showFeedPicker by remember { mutableStateOf(false) }
    var retryDelivery by remember { mutableStateOf<ChatViewModel.Delivery?>(null) }
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
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
                    com.example.c001apk.compose.ui.component.MiuixActionButton(
                        onClick = {
                            onClearFocus()
                            showFeedPicker = true
                        },
                    ) {
                        Text("发动态")
                    }
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
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.End,
                    ) {
                        if (delivery.preview != null)
                            coil.compose.AsyncImage(
                                model = delivery.preview,
                                contentDescription = "待发送图片",
                                modifier = Modifier.size(120.dp),
                            )
                        else if (delivery.picture.isNotEmpty()) Text("[图片]")
                        if (delivery.text.isNotBlank()) Text(delivery.text)
                        Text(
                            if (delivery.sending) "发送中…" else delivery.error.orEmpty(),
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                        if (!delivery.sending)
                            Row {
                                com.example.c001apk.compose.ui.component.MiuixActionButton(
                                    onClick = {
                                        if (delivery.unconfirmed) retryDelivery = delivery
                                        else viewModel.retry(delivery)
                                    },
                                ) {
                                    Text("重试")
                                }
                                com.example.c001apk.compose.ui.component.MiuixActionButton(
                                    onClick = { viewModel.discard(delivery) },
                                ) {
                                    Text("移除")
                                }
                            }
                    }
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

    if (showFeedPicker)
        ChatFeedPicker(
            onDismiss = { showFeedPicker = false },
            onSelected = { feedId ->
                showFeedPicker = false
                viewModel.onSendMessage(
                    uid,
                    com.example.c001apk.compose.util.ShareContent.link(
                        com.example.c001apk.compose.util.ShareType.FEED,
                        feedId,
                    ),
                    "",
                )
            },
        )
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

    when {
        showDialog -> {
            Dialog(onDismissRequest = { showDialog = false }) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 28.dp,
                    colors =
                        CardDefaults.defaultColors()
                            .copy(color = MiuixTheme.colorScheme.surfaceContainerHigh),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    ) {
                        Text(
                            text = "删除",
                            modifier =
                                Modifier.fillMaxWidth()
                                    .clickable {
                                        showDialog = false
                                        viewModel.onDeleteMsg()
                                    }
                                    .padding(horizontal = 24.dp, vertical = 14.dp),
                            style = MiuixTheme.textStyles.headline2,
                        )
                        if (viewModel.pic.isNotEmpty()) {
                            top.yukonga.miuix.kmp.basic.BasicComponent(
                                title = "重新加载图片",
                                onClick = {
                                    showDialog = false
                                    viewModel.retryImage(viewModel.deleteId)
                                },
                            )
                        }
                        if (viewModel.pic.isEmpty()) {
                            Text(
                                text = "复制",
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .clickable {
                                            showDialog = false
                                            context.copyText(viewModel.message)
                                        }
                                        .padding(horizontal = 24.dp, vertical = 14.dp),
                                style = MiuixTheme.textStyles.headline2,
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
fun ChatBottom(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
    onPickImage: () -> Unit,
    onPickOtherImage: () -> Unit,
    onSendMessage: (String) -> Unit,
    clearText: Boolean,
    resetClear: () -> Unit,
    clearFocus: Boolean,
    resetFocus: () -> Unit,
    onClearFocus: () -> Unit,
) {
    val recentList by
        viewModel.recentEmojiData.collectAsStateWithLifecycle(initialValue = emptyList())
    var shouldShowSendBtn by remember { mutableStateOf(false) }
    var showEmojiPanel by remember { mutableStateOf(false) }
    var clickedEmoji by remember { mutableStateOf<String?>(null) }
    var textInput by remember { mutableStateOf(EMPTY_STRING) }
    var pagerState: PagerState
    val context = LocalContext.current

    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier =
                    Modifier.size(48.dp).clickable {
                        onClearFocus()
                        showEmojiPanel = !showEmojiPanel
                    },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    imageVector = MiuixIcons.EmojiEmotions,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantSummary),
                )
            }
            ChatEditText(
                modifier =
                    Modifier.weight(1f)
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MiuixTheme.colorScheme.surface)
                        .padding(horizontal = 6.dp),
                showSendBtn = { shouldShowSendBtn = it },
                updateInputText = {
                    textInput = it
                },
                clearText = clearText,
                resetClear = resetClear,
                clearFocus = clearFocus,
                resetFocus = resetFocus,
                clickedEmoji = clickedEmoji,
                resetEmoji = {
                    clickedEmoji = null
                },
                onCloseEmojiPanel = {
                    showEmojiPanel = false
                },
            )
            if (!shouldShowSendBtn) {
                Row {
                    Box(
                        modifier =
                            Modifier.size(48.dp).clickable {
                                onPickImage()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            imageVector = MiuixIcons.Image,
                            contentDescription = null,
                            colorFilter =
                                ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantSummary),
                        )
                    }
                    Box(
                        modifier =
                            Modifier.size(48.dp).clickable {
                                onPickOtherImage()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter =
                                androidx.compose.ui.res.painterResource(
                                    id = R.drawable.outline_note_alt_24,
                                ),
                            contentDescription = null,
                            colorFilter =
                                ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantSummary),
                        )
                    }
                }
            } else {
                Box(
                    modifier =
                        Modifier.size(48.dp).clickable {
                            onSendMessage(textInput)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        imageVector = MiuixIcons.Send,
                        contentDescription = null,
                        colorFilter =
                            ColorFilter.tint(MiuixTheme.colorScheme.onSurfaceVariantSummary),
                    )
                }
            }
        }
        if (showEmojiPanel) {
            pagerState =
                rememberPagerState(
                    initialPage = if (recentList.isEmpty()) 1 else 0,
                    pageCount = { 3 },
                )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().padding(10.dp),
            ) { index ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    val pagerState1 = rememberPagerState {
                        when (index) {
                            0 -> 1
                            1 -> 4
                            2 -> 2
                            else -> 0
                        }
                    }
                    HorizontalPager(
                        state = pagerState1,
                        modifier = Modifier.fillMaxWidth(),
                    ) { index0 ->
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            items(28) {
                                val emojiName =
                                    when (index) {
                                        0 -> recentList.getOrNull(it)?.data
                                        1 -> emojiList.getOrNull(index0)?.getOrNull(it)?.first
                                        2 -> coolBList.getOrNull(index0)?.getOrNull(it)?.first
                                        else -> null
                                    }
                                val emojiRes =
                                    when (index) {
                                        0 ->
                                            emojiName?.let { name ->
                                                EmojiUtils.emojiMap.getValue(name)
                                            }

                                        1 -> emojiList.getOrNull(index0)?.getOrNull(it)?.second
                                        2 -> coolBList.getOrNull(index0)?.getOrNull(it)?.second
                                        else -> null
                                    }
                                Box(
                                    modifier =
                                        Modifier.size(48.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                emojiName?.let { name ->
                                                    clickedEmoji = name
                                                    if (pagerState.currentPage != 0) {
                                                        viewModel.updateRecentEmoji(
                                                            name,
                                                            recentList.size,
                                                            recentList.lastOrNull()?.data,
                                                        )
                                                    }
                                                }
                                                if (it == 27) clickedEmoji = "[c001apk]"
                                            },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (emojiRes != null) {
                                        Image(
                                            painter =
                                                rememberDrawablePainter(
                                                    drawable =
                                                        ResourcesCompat.getDrawable(
                                                            context.resources,
                                                            emojiRes,
                                                            context.theme,
                                                        ),
                                                ),
                                            contentDescription = null,
                                            modifier = Modifier.size(28.dp),
                                        )
                                    } else if (it == 27) {
                                        Icon(
                                            imageVector = MiuixIcons.Backspace,
                                            contentDescription = null,
                                            modifier = Modifier.size(28.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                    CardIndicator(
                        modifier =
                            Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp),
                        pagerState = pagerState1,
                        defColor = MiuixTheme.colorScheme.primary.copy(alpha = 0.3f),
                    )
                }
            }
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            ) {
                TextIndicator(
                    index = 0,
                    title = "最近",
                    pagerState = pagerState,
                )
                VerticalDivider()
                TextIndicator(
                    index = 1,
                    title = "默认",
                    pagerState = pagerState,
                )
                VerticalDivider()
                TextIndicator(
                    index = 2,
                    title = "酷币",
                    pagerState = pagerState,
                )
            }
        }
    }
}

@Composable
fun RowScope.TextIndicator(
    modifier: Modifier = Modifier,
    index: Int,
    title: String,
    pagerState: PagerState,
) {
    val scope = rememberCoroutineScope()
    Text(
        text = title,
        modifier =
            modifier
                .weight(1f)
                .background(
                    if (pagerState.currentPage == index) MiuixTheme.colorScheme.primary
                    else Color.Transparent,
                )
                .clickable {
                    scope.launch {
                        pagerState.scrollToPage(index)
                    }
                }
                .padding(vertical = 10.dp),
        color =
            if (pagerState.currentPage == index) MiuixTheme.colorScheme.onPrimary
            else MiuixTheme.colorScheme.onSurfaceVariantSummary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MiuixTheme.textStyles.headline2,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun LoadingDialog() {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false),
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = RoundedCornerShape(20.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
