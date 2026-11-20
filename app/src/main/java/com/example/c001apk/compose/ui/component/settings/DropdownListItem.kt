package com.example.c001apk.compose.ui.component.settings

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.glass.GlassDropdownPreference
import top.yukonga.miuix.kmp.glass.GlassPopupDefaults
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

data class SelectionItem<T>(val label: String, val value: T)

@Composable
fun <T> DropdownListItem(
    value: T?,
    leadingImageVector: ImageVector? = null,
    headlineText: String,
    selections: List<SelectionItem<T>>,
    onValueChanged: (index: Int, value: T) -> Unit,
    enabled: Boolean = true,
    useWindowPopup: Boolean = false,
) {
    val labels = selections.map { it.label }
    val selectedIndex = selections.indexOfFirst { it.value == value }
    val modifier = if (!enabled || selections.isEmpty()) {
        Modifier.semantics(mergeDescendants = true) { disabled() }
    } else {
        Modifier
    }
    val startAction: (@Composable () -> Unit)? = leadingImageVector?.let { icon ->
        {
            Icon(
                icon,
                null,
                Modifier.size(24.dp),
                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
    val onSelect: (Int) -> Unit = { index ->
        selections.getOrNull(index)?.let { onValueChanged(index, it.value) }
    }
    PreferenceCard {
        if (useWindowPopup) {
            WindowDropdownPreference(
                title = headlineText,
                items = labels,
                selectedIndex = selectedIndex,
                modifier = modifier,
                enabled = enabled,
                startAction = startAction,
                onSelectedIndexChange = onSelect,
            )
        } else {
            GlassDropdownPreference(
                title = headlineText,
                items = labels,
                selectedIndex = selectedIndex,
                modifier = modifier,
                backdrop = null,
                visuals = GlassPopupDefaults.visuals(
                    stroke = null,
                    shadow = null,
                    material = null,
                    containerColor = GlassPopupDefaults.containerColor().copy(alpha = 1f),
                ),
                enabled = enabled,
                startAction = startAction,
                onSelectedIndexChange = onSelect,
            )
        }
    }
}
