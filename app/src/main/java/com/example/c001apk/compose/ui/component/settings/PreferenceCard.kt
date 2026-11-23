package com.example.c001apk.compose.ui.component.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val LocalPreferenceInGroup = staticCompositionLocalOf { false }
private val LocalPreferenceHorizontalPadding = staticCompositionLocalOf { 12.dp }
private val LocalPreferenceSpacing = staticCompositionLocalOf { 12.dp }

/** 统一卡片外边距：设置首页 12dp/8dp，子页与关于页 16dp/12dp。 */
@Composable
fun PreferenceSpacing(
    horizontal: Dp,
    spacing: Dp,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalPreferenceHorizontalPadding provides horizontal,
        LocalPreferenceSpacing provides spacing,
        content = content,
    )
}

/** One native card owns the shape and clipping for all of its preference rows. */
@Composable
fun PreferenceGroup(
    title: String? = null,
    modifier: Modifier = Modifier,
    containerColor: Color = MiuixTheme.colorScheme.surfaceContainer,
    cardModifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth()) {
        title?.let { SmallTitle(text = it) }
        Card(
            Modifier.fillMaxWidth()
                .padding(horizontal = LocalPreferenceHorizontalPadding.current)
                .padding(bottom = LocalPreferenceSpacing.current)
                .then(cardModifier),
            colors = CardDefaults.defaultColors(containerColor),
        ) {
            CompositionLocalProvider(LocalPreferenceInGroup provides true) { content() }
        }
    }
}

@Composable
fun PreferenceCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MiuixTheme.colorScheme.surfaceContainer,
    cardModifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (LocalPreferenceInGroup.current) Column(modifier.fillMaxWidth(), content = content)
    else PreferenceGroup(modifier = modifier, containerColor = containerColor, cardModifier = cardModifier, content = content)
}
