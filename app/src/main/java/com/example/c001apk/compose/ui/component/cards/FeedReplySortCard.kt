package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.c001apk.compose.ui.component.rememberHapticClick
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.SinkFeedback

@Composable
fun FeedReplySortCard(
    modifier: Modifier = Modifier,
    replyCount: String,
    selected: Int = 0,
    updateSortReply: (Int) -> Unit,
) {
    Row(
        modifier.fillMaxWidth().background(MiuixTheme.colorScheme.surface).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier.weight(1f).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.label_comment), style = MiuixTheme.textStyles.subtitle, fontWeight = FontWeight.SemiBold)
            Text(
                replyCount,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
        Row(Modifier.selectableGroup()) {
            listOf(R.string.theme_type_default, R.string.label_latest, R.string.label_hot, R.string.label_author).forEachIndexed { index, labelRes ->
                val isSelected = index == selected
                Box(
                    modifier =
                        Modifier.selectable(
                                selected = isSelected,
                                interactionSource = null,
                                indication = SinkFeedback(),
                                role = Role.RadioButton,
                                onClick =
                                    rememberHapticClick {
                                        if (!isSelected) updateSortReply(index)
                                    },
                            )
                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                            .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(labelRes),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color =
                            if (isSelected) MiuixTheme.colorScheme.primary
                            else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
