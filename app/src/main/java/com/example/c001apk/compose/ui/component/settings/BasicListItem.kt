package com.example.c001apk.compose.ui.component.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.squircle.squircleClip

@Composable
fun BasicListItem(
    modifier: Modifier = Modifier,
    headlineText: String? = null,
    supportingText: String? = null,
    leadingImageVector: ImageVector? = null,
    leadingPainter: Painter? = null,
    leadingText: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    holdDownState: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    if (leadingText != null && headlineText == null) {
        SmallTitle(text = leadingText, modifier = modifier)
        return
    }
    val leading: (@Composable () -> Unit)? =
        if (leadingImageVector != null || leadingPainter != null) {
            {
                if (leadingImageVector != null)
                    PreferenceLeadingIcon(leadingImageVector)
                else if (leadingPainter != null)
                    Image(
                        leadingPainter,
                        null,
                        modifier = Modifier.size(24.dp).squircleClip(6.dp),
                    )
            }
        } else null
    PreferenceCard(modifier) {
        if (onClick != null && trailingContent == null) {
            ArrowPreference(
                title = headlineText.orEmpty(),
                summary = supportingText,
                enabled = enabled,
                holdDownState = holdDownState,
                onClick = onClick,
                startAction = leading,
            )
        } else {
            BasicComponent(
                title = headlineText,
                summary = supportingText,
                enabled = enabled,
                holdDownState = holdDownState,
                onClick = onClick,
                startAction = leading,
                endActions = trailingContent?.let { trailing -> { trailing() } },
            )
        }
    }
}
