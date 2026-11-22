package com.example.c001apk.compose.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.c001apk.compose.R
import com.example.c001apk.compose.constant.Constants.EMPTY_STRING
import com.example.c001apk.compose.logic.model.StringEntity
import com.example.c001apk.compose.ui.component.ChatEditText
import com.example.c001apk.compose.ui.component.cards.CardIndicator
import com.example.c001apk.compose.util.EmojiUtils
import com.example.c001apk.compose.util.EmojiUtils.pickerCategories
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.extended.Image
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteDeleteBackLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteEmojiLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCutePhotoAlbumLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteSendLine

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
    ChatComposerContent(
        modifier = modifier,
        recentList = recentList,
        onUpdateRecentEmoji = { name ->
            viewModel.updateRecentEmoji(name, recentList.size, recentList.lastOrNull()?.data)
        },
        onPickImage = onPickImage,
        onPickOtherImage = onPickOtherImage,
        onSendMessage = onSendMessage,
        clearText = clearText,
        resetClear = resetClear,
        clearFocus = clearFocus,
        resetFocus = resetFocus,
        onClearFocus = onClearFocus,
    )
}

@Composable
internal fun ChatComposerContent(
    modifier: Modifier = Modifier,
    recentList: List<StringEntity>,
    onUpdateRecentEmoji: (String) -> Unit,
    onPickImage: () -> Unit,
    onPickOtherImage: () -> Unit,
    onSendMessage: (String) -> Unit,
    clearText: Boolean,
    resetClear: () -> Unit,
    clearFocus: Boolean,
    resetFocus: () -> Unit,
    onClearFocus: () -> Unit,
) {
    var shouldShowSendBtn by remember { mutableStateOf(false) }
    var showEmojiPanel by remember { mutableStateOf(false) }
    var clickedEmoji by remember { mutableStateOf<String?>(null) }
    var textInput by remember { mutableStateOf(EMPTY_STRING) }
    var pagerState: PagerState
    val context = LocalContext.current

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChatComposerIcon(
                icon = MingCuteEmojiLine,
                description = if (showEmojiPanel) "收起表情" else "选择表情",
                selected = showEmojiPanel,
                onClick = {
                    onClearFocus()
                    showEmojiPanel = !showEmojiPanel
                },
            )
            ChatEditText(
                modifier =
                    Modifier.weight(1f)
                        .testTag("chat_composer_input")
                        .squircleSurface(MiuixTheme.colorScheme.surface, 20.dp)
                        .heightIn(min = 44.dp)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
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
                    ChatComposerIcon(MingCutePhotoAlbumLine, "选择图片", onClick = onPickImage)
                    IconButton(
                        onClick = onPickOtherImage,
                        modifier = Modifier.size(44.dp),
                        backgroundColor = Color.Transparent,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_note_alt_24),
                            contentDescription = "从文件选择图片",
                            modifier = Modifier.size(22.dp),
                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            } else {
                ChatComposerIcon(
                    icon = MingCuteSendLine,
                    description = "发送",
                    primary = true,
                    onClick = { onSendMessage(textInput) },
                )
            }
        }
        if (showEmojiPanel) {
            pagerState =
                rememberPagerState(
                    initialPage = if (recentList.isEmpty()) 1 else 0,
                    pageCount = { pickerCategories.size + 1 },
                )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().padding(10.dp),
            ) { index ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    val pages = if (index == 0) listOf(recentList.take(27).mapNotNull { recent ->
                        EmojiUtils.emojiMap[recent.data]?.let { recent.data to it }
                    }) else pickerCategories[index - 1].pages
                    val pagerState1 = rememberPagerState { pages.size }
                    HorizontalPager(
                        state = pagerState1,
                        modifier = Modifier.fillMaxWidth(),
                    ) { index0 ->
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            items(28) {
                                val emoji = pages.getOrNull(index0)?.getOrNull(it)
                                val emojiName = emoji?.first
                                val emojiRes = emoji?.second
                                Box(
                                    modifier =
                                        Modifier.size(48.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable(enabled = emojiName != null || it == 27) {
                                                emojiName?.let { name ->
                                                    clickedEmoji = name
                                                    if (pagerState.currentPage != 0) {
                                                        onUpdateRecentEmoji(name)
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
                                            contentDescription = emojiName,
                                            modifier = Modifier.size(28.dp),
                                        )
                                    } else if (it == 27) {
                                        Icon(
                                            imageVector = MingCuteDeleteBackLine,
                                            contentDescription = "删除表情",
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
                        defColor = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.65f),
                    )
                }
            }
            HorizontalDivider()
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
                (listOf("最近") + pickerCategories.map { it.title }).forEachIndexed { index, title ->
                    TextIndicator(index = index, title = title, pagerState = pagerState)
                }
            }
        }
    }
}

@Composable
private fun ChatComposerIcon(
    icon: ImageVector,
    description: String,
    selected: Boolean = false,
    primary: Boolean = false,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        backgroundColor = when {
            primary -> MiuixTheme.colorScheme.primary
            selected -> MiuixTheme.colorScheme.primaryContainer
            else -> Color.Transparent
        },
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            modifier = Modifier.size(22.dp),
            tint = when {
                primary -> MiuixTheme.colorScheme.onPrimary
                selected -> MiuixTheme.colorScheme.onPrimaryContainer
                else -> MiuixTheme.colorScheme.onSurfaceVariantSummary
            },
        )
    }
}

@Composable
private fun RowScope.TextIndicator(
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
                .padding(horizontal = 2.dp)
                .squircleSurface(
                    if (pagerState.currentPage == index) MiuixTheme.colorScheme.primaryContainer
                    else Color.Transparent,
                    12.dp,
                )
                .clickable(role = Role.Tab) {
                    scope.launch {
                        pagerState.scrollToPage(index)
                    }
                }
                .semantics { selected = pagerState.currentPage == index }
                .padding(vertical = 8.dp),
        color =
            if (pagerState.currentPage == index) MiuixTheme.colorScheme.onPrimaryContainer
            else MiuixTheme.colorScheme.onSurfaceVariantSummary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MiuixTheme.textStyles.headline2,
        textAlign = TextAlign.Center,
    )
}
