package com.example.c001apk.compose.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import com.example.c001apk.compose.ui.component.BackButton
import top.yukonga.miuix.kmp.basic.IconButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBarDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun SearchResultHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onBackClick: () -> Unit,
    subtitle: String,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var editing by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val windowFocused = LocalWindowInfo.current.isWindowFocused
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) editing = false
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(windowFocused) {
        if (!windowFocused) editing = false
    }
    LaunchedEffect(editing) {
        if (editing) focusRequester.requestFocus()
    }
    val titleStyle = MiuixTheme.textStyles.title3.copy(
        color = MiuixTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
    )

    fun submit(value: String) {
        if (value.isBlank()) return
        editing = false
        focusManager.clearFocus()
        keyboardController?.hide()
        onSearch(value)
    }

    Column(
        Modifier.fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars.union(WindowInsets.displayCutout)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .heightIn(min = TopAppBarDefaults.CollapsedHeight)
                .padding(
                    start = TopAppBarDefaults.NavigationIconPadding,
                    end = TopAppBarDefaults.ActionIconPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onBackClick)
            // 普通标题不持有输入连接，只有显式点击后才挂载编辑器。
            if (!editing) {
                Box(
                    Modifier.weight(1f)
                        .heightIn(min = TopAppBarDefaults.SmallTopAppBarCenterHeight)
                        .clickable { editing = true }
                        .semantics { contentDescription = "搜索关键词，点击编辑" },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(query.ifEmpty { "搜索" }, style = titleStyle, maxLines = 1)
                }
            } else BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f)
                    .focusRequester(focusRequester)
                    .semantics { contentDescription = "搜索关键词，点击编辑" },
                textStyle = titleStyle,
                singleLine = true,
                cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { submit(query) }),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .heightIn(min = TopAppBarDefaults.SmallTopAppBarCenterHeight),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (query.isEmpty()) Text("搜索", style = titleStyle)
                        innerTextField()
                    }
                },
            )
            Row(
                modifier = Modifier.widthIn(min = IconButtonDefaults.MinWidth),
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                modifier = Modifier.padding(bottom = TopAppBarDefaults.SubtitleBottomPadding),
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}
