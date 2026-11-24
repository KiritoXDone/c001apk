package com.example.c001apk.compose.ui.component

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.withResumed
import top.yukonga.miuix.kmp.basic.Icon
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.IconButton
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
    val fieldInteraction = interactionSource ?: remember { MutableInteractionSource() }
    val isFocused by fieldInteraction.collectIsFocusedAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val currentOnExpandedChange by rememberUpdatedState(onExpandedChange)
    val currentOnQueryChange by rememberUpdatedState(onQueryChange)
    // Android 8 的输入框可能在清焦点后重新获得焦点。
    val isFieldEnabled = enabled && (expanded || Build.VERSION.SDK_INT !in 26..27)
    val expandOnTap = if (enabled && !isFieldEnabled) {
        Modifier.pointerInput(Unit) { detectTapGestures { currentOnExpandedChange(true) } }
    } else {
        Modifier
    }
    MiuixSearchTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.then(expandOnTap).focusRequester(focusRequester).onFocusChanged {
            if (it.isFocused) currentOnExpandedChange(true)
        },
        label = label.takeUnless { expanded || query.isNotEmpty() }.orEmpty(),
        enabled = isFieldEnabled,
        interactionSource = fieldInteraction,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
    )
    LaunchedEffect(expanded, enabled) {
        if (expanded && enabled) {
            focusRequester.requestFocus()
        } else if (isFocused) {
            delay(100)
            if (isFocused) {
                currentOnQueryChange("")
                focusManager.clearFocus()
            }
        }
    }
}

@Suppress("DEPRECATION")
@Composable
fun MiuixSearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    textStyle: TextStyle = MiuixTheme.textStyles.main,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    val backgroundColor = MiuixTheme.colorScheme.surfaceContainerHigh
    // 外部文本变化时把光标移到末尾，避免重建编辑器后从开头插入。
    var inputValue by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    if (inputValue.text != value) {
        inputValue = TextFieldValue(value, TextRange(value.length))
    }
    TextField(
        value = inputValue,
        onValueChange = {
            inputValue = it
            onValueChange(it.text)
        },
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
        enabled = enabled,
        interactionSource = interactionSource,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        leadingIcon = { SearchFieldIcon() },
        trailingIcon = {
            SearchFieldClearButton(value, enabled) {
                inputValue = TextFieldValue()
                onValueChange("")
            }
        },
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
private fun SearchFieldClearButton(value: String, enabled: Boolean, onClear: () -> Unit) {
    AnimatedVisibility(visible = value.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
        val fill = MiuixTheme.colorScheme.surfaceContainerHigh.copy(alpha = 1f)
        val icon: @Composable () -> Unit = {
            Icon(
                imageVector = MiuixIcons.Basic.SearchCleanup,
                contentDescription = stringResource(R.string.search_clear),
                modifier = Modifier.size(24.dp),
                tint = MiuixTheme.colorScheme.onSurfaceContainerHigh,
            )
        }
        val modifier = Modifier.padding(end = 4.dp)
        if (enabled) {
            GlassIconButton(
                onClick = onClear,
                modifier = modifier,
                backdrop = null,
                inheritTopBarMaterial = false,
                fill = fill,
                size = 36.dp,
                stroke = null,
                shadow = null,
                content = icon,
            )
        } else {
            IconButton(
                onClick = onClear,
                modifier = modifier,
                enabled = false,
                backgroundColor = fill,
                minHeight = 36.dp,
                minWidth = 36.dp,
                content = icon,
            )
        }
    }
}
