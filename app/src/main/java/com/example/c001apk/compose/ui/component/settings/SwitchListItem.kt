package com.example.c001apk.compose.ui.component.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import top.yukonga.miuix.kmp.preference.SwitchPreference

@Composable
fun SwitchListItem(
    value: Boolean,
    leadingImageVector: ImageVector? = null,
    headlineText: String,
    supportingText: String? = null,
    enabled: Boolean = true,
    onValueChanged: (value: Boolean) -> Unit,
) {
    PreferenceCard {
        SwitchPreference(
            enabled = enabled,
            checked = value,
            onCheckedChange = onValueChanged,
            title = headlineText,
            summary = supportingText,
            startAction = leadingImageVector?.let { icon ->
                { PreferenceLeadingIcon(icon) }
            },
        )
    }
}
