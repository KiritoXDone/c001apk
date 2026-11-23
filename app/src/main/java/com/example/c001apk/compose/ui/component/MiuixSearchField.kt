package com.example.c001apk.compose.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.withResumed
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TextFieldDefaults
import top.yukonga.miuix.kmp.glass.GlassIconButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Search
import top.yukonga.miuix.kmp.icon.basic.SearchCleanup
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MiuixSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    enabled: Boolean = true,
    shouldAutoFocus: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
) {
    if (shouldAutoFocus) {
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        var hasRequestedInitialFocus by rememberSaveable { mutableStateOf(false) }
        val currentOnExpandedChange by rememberUpdatedState(onExpandedChange)
        LaunchedEffect(lifecycle) {
            // 首次可交互时展开，避开导航清焦点；从结果页返回时不重复弹出输入法。
            if (!hasRequestedInitialFocus) {
                lifecycle.withResumed {
                    currentOnExpandedChange(true)
                    hasRequestedInitialFocus = true
                }
            }
        }
    }
    InputField(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier,
        label = label,
        enabled = enabled,
        leadingIcon = { SearchFieldIcon() },
        trailingIcon = { SearchFieldClearButton(query, enabled, onQueryChange) },
        interactionSource = interactionSource,
    )
}

@Composable
fun MiuixSearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    textStyle: TextStyle = MiuixTheme.textStyles.main,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val backgroundColor = MiuixTheme.colorScheme.surfaceContainerHigh
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.heightIn(min = 45.dp),
        insideMargin = DpSize(0.dp, 10.dp),
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = backgroundColor,
            labelColor = MiuixTheme.colorScheme.onSurfaceContainerHigh,
            borderColor = backgroundColor,
        ),
        cornerRadius = 24.dp,
        label = label,
        useLabelAsPlaceholder = true,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        leadingIcon = { SearchFieldIcon() },
        trailingIcon = { SearchFieldClearButton(value, true, onValueChange) },
        singleLine = true,
        cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
    )
}

@Composable
private fun SearchFieldIcon() {
    Icon(
        imageVector = MiuixIcons.Basic.Search,
        contentDescription = null,
        modifier = Modifier.padding(start = 16.dp, end = 8.dp).size(28.dp)
            .testTag("miuix_search_icon"),
        tint = MiuixTheme.colorScheme.onSurfaceContainerHigh,
    )
}

@Composable
private fun SearchFieldClearButton(value: String, enabled: Boolean, onValueChange: (String) -> Unit) {
    AnimatedVisibility(visible = value.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
        GlassIconButton(
            onClick = { onValueChange("") },
            modifier = Modifier.padding(end = 4.dp),
            enabled = enabled,
            backdrop = null,
            inheritTopBarMaterial = false,
            fill = MiuixTheme.colorScheme.surfaceContainerHigh.copy(alpha = 1f),
            size = 36.dp,
            stroke = null,
            shadow = null,
        ) {
            Icon(
                imageVector = MiuixIcons.Basic.SearchCleanup,
                contentDescription = "清空搜索",
                modifier = Modifier.size(24.dp),
                tint = MiuixTheme.colorScheme.onSurfaceContainerHigh,
            )
        }
    }
}
