package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.c001apk.compose.util.noRippleClickable
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.ChevronForward
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Created by bggRGjQaUbCoE on 2024/6/6 */
@Composable
fun TitleCard(
    modifier: Modifier = Modifier,
    url: String,
    title: String,
    onOpenLink: (String, String?) -> Unit,
) {

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .squircleClip(16.dp)
                .noRippleClickable(enabled = url.isNotEmpty()) {
                    onOpenLink(url, title)
                }
                .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
        )

        if (url.isNotEmpty()) {
            Icon(
                imageVector = MiuixIcons.Glass.ChevronForward,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.size(15.dp),
            )
        }
    }
}
