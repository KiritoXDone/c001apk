package com.example.c001apk.compose.ui.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.model.TopBarStyle
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import top.yukonga.miuix.kmp.basic.DropdownDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.glass.GlassBottomSheet
import top.yukonga.miuix.kmp.glass.GlassPopupDefaults
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Check
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.MiuixIndication
import top.yukonga.miuix.kmp.window.WindowListPopup

@Composable
fun pullToRefreshTexts(): List<String> = listOf(
    stringResource(R.string.refresh_pull),
    stringResource(R.string.refresh_release),
    stringResource(R.string.refresh_loading),
    stringResource(R.string.refresh_complete),
)

@Composable
fun MiuixMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    alignment: PopupPositionProvider.Align = PopupPositionProvider.Align.End,
    onDismissFinished: (() -> Unit)? = null,
    contentScale: Float = 1f,
    enableWindowDim: Boolean = true,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density * contentScale, density.fontScale)) {
        if (LocalUserPreferences.current.topBarStyle == TopBarStyle.OS4) {
            MiuixGlassMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest,
                onDismissFinished = onDismissFinished,
                modifier = modifier,
                alignment = alignment,
                enableWindowDim = enableWindowDim,
                content = content,
            )
            return@CompositionLocalProvider
        }
        WindowListPopup(
            show = expanded,
            popupModifier = modifier,
            alignment = alignment,
            enableWindowDim = enableWindowDim,
            onDismissRequest = onDismissRequest,
            onDismissFinished = onDismissFinished,
        ) {
            val edgePadding = DropdownDefaults.FirstLastVerticalPadding - DropdownDefaults.MiddleVerticalPadding
            Box(Modifier.padding(vertical = edgePadding)) {
                ListPopupColumn { content() }
            }
        }
    }
}

@Composable
fun MiuixMoreMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var isHeldDown by remember { mutableStateOf(false) }
    if (expanded) SideEffect { isHeldDown = true }

    Box(modifier, contentAlignment = Alignment.Center) {
        Box(Modifier.padding(4.dp)) {
            IconButton(
                onClick = { onExpandedChange(true) },
                holdDownState = expanded || isHeldDown,
                minHeight = 35.dp,
                minWidth = 35.dp,
                backgroundColor = Color.Transparent,
            ) {
                Icon(
                    imageVector = MiuixIcons.More,
                    contentDescription = stringResource(R.string.update_more),
                    modifier = Modifier.size(17.5.dp),
                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            MiuixMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                onDismissFinished = { isHeldDown = false },
                alignment = PopupPositionProvider.Align.End,
                content = content,
            )
        }
    }
}

// 菜单行参考 Miuix DropdownImpl 和 GlassPopupItem（Apache-2.0，compose-miuix-ui contributors）。
// 保留内容插槽和长按。
@Composable
fun MiuixMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textColor: Color = Color.Unspecified,
    isSelected: Boolean? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val glassExpanded = LocalGlassMenuExpanded.current
    val isGlass = glassExpanded != null
    val colors = DropdownDefaults.dropdownColors()
    val enabledColor = textColor.takeOrElse { colors.contentColor }
    val color = when {
        !enabled && isGlass -> enabledColor.copy(alpha = 0.4f)
        !enabled -> MiuixTheme.colorScheme.disabledOnSecondaryVariant
        isSelected == true -> colors.selectedContentColor
        else -> enabledColor
    }
    val selectionSemantics = if (isSelected == null) {
        Modifier
    } else {
        Modifier.semantics { selected = isSelected }
    }
    val glassInset = if (isGlass) GlassPopupDefaults.ItemPressInset else 0.dp
    val rowShape = if (isGlass) {
        Modifier.heightIn(min = GlassPopupDefaults.ItemMinHeight)
            .padding(horizontal = glassInset)
            .clip(RoundedCornerShape(GlassPopupDefaults.ItemPressRadius))
    } else {
        Modifier
    }
    val iconSize = if (isGlass) GlassPopupDefaults.ItemIconSize else DropdownDefaults.IconMinSize
    val iconGap = if (isGlass) GlassPopupDefaults.ItemIconGap else DropdownDefaults.IconEndPadding
    val checkSize = if (isGlass) GlassPopupDefaults.ItemTickSize else DropdownDefaults.CheckIconSize
    val checkGap = if (isGlass) GlassPopupDefaults.ItemIconGap else DropdownDefaults.CheckIconStartPadding
    val textStyles = MiuixTheme.textStyles
    CompositionLocalProvider(LocalContentColor provides color) {
        MiuixTheme(
            textStyles = textStyles.copy(
                main = textStyles.main.copy(fontSize = textStyles.body1.fontSize, fontWeight = FontWeight.Medium),
            ),
        ) {
            val indication = if (isGlass) {
                val pressColor = GlassPopupDefaults.itemPressedColor()
                remember(pressColor) {
                    MiuixIndication(color = pressColor.copy(alpha = 1f), pressAlpha = pressColor.alpha)
                }
            } else {
                LocalIndication.current
            }
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .then(rowShape)
                    .then(selectionSemantics)
                    .combinedClickable(
                        interactionSource = null,
                        indication = indication,
                        enabled = enabled && glassExpanded != false,
                        role = if (isSelected == null) Role.Button else Role.RadioButton,
                        onClick = onClick,
                        onLongClick = onLongClick,
                        onLongClickLabel = onLongClickLabel,
                    )
                    .padding(
                        horizontal = if (isGlass) GlassPopupDefaults.ItemPaddingHorizontal - glassInset else DropdownDefaults.InsideHorizontalPadding,
                        vertical = if (isGlass) GlassPopupDefaults.ItemPaddingVertical else DropdownDefaults.MiddleVerticalPadding,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false).widthIn(max = DropdownDefaults.MaxItemTextWidth),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    leadingIcon?.let { icon ->
                        Box(
                            Modifier.sizeIn(minWidth = iconSize, minHeight = iconSize),
                            contentAlignment = Alignment.CenterStart,
                        ) { icon() }
                        Spacer(Modifier.width(iconGap))
                    }
                    Box { text() }
                }
                Spacer(Modifier.width(checkGap))
                when {
                    trailingIcon != null -> trailingIcon()
                    isSelected == true -> Icon(
                        imageVector = MiuixIcons.Basic.Check,
                        contentDescription = null,
                        modifier = Modifier.size(checkSize),
                        tint = if (enabled) colors.selectedIndicatorColor else color,
                    )
                    else -> Spacer(Modifier.size(checkSize))
                }
            }
        }
    }
}

@Composable
fun MiuixBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val contentDensity = LocalDensity.current
    GlassBottomSheet(
        show = true,
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        backdrop = null,
        stroke = null,
        containerColor = MiuixTheme.colorScheme.surface.copy(alpha = 1f),
        dragHandleColor = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.2f),
        insideMargin = DpSize(0.dp, 0.dp),
    ) {
        CompositionLocalProvider(LocalDensity provides contentDensity) {
            Box(
                Modifier.windowInsetsPadding(
                    WindowInsets.systemBars
                        .union(WindowInsets.displayCutout)
                        .only(WindowInsetsSides.Bottom),
                ),
            ) {
                content()
            }
        }
    }
}

object MiuixScaffoldDefaults {
    val contentWindowInsets: WindowInsets
        @Composable get() = WindowInsets.systemBars.union(WindowInsets.displayCutout)
}
