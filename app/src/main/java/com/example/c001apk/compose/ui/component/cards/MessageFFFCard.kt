package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.c001apk.compose.ui.ffflist.FFFListType
import com.example.c001apk.compose.util.CookieUtil
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MessageFFFCard(
    modifier: Modifier = Modifier,
    fffList: List<String>,
    onViewFFFList: (String, String) -> Unit,
) {
    val statistics = listOf(
        "动态" to FFFListType.FEED,
        "关注" to FFFListType.FOLLOW,
        "粉丝" to FFFListType.FAN,
    )
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = ProfileLayoutDefaults.HeaderInset),
        horizontalArrangement = Arrangement.spacedBy(ProfileLayoutDefaults.ItemSpacing),
    ) {
        statistics.forEachIndexed { index, (title, type) ->
            Card(
                modifier = Modifier.weight(1f).testTag("profile_statistic_${type.name}")
                    .semantics(mergeDescendants = true) { role = Role.Button },
                colors = CardDefaults.defaultColors(
                    color = Color.Transparent,
                    contentColor = MiuixTheme.colorScheme.onSurface,
                ),
                onClick = {
                    if (CookieUtil.isLogin) onViewFFFList(CookieUtil.uid, type.name)
                },
                showIndication = true,
            ) {
                Row(
                    modifier = Modifier.heightIn(min = ProfileLayoutDefaults.StatisticMinHeight),
                    horizontalArrangement = Arrangement.spacedBy(ProfileLayoutDefaults.StatisticSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = fffList.getOrNull(index) ?: "—",
                        modifier = Modifier.weight(1f, fill = false),
                        fontSize = ProfileLayoutDefaults.StatisticValueFontSize,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = title,
                        fontSize = ProfileLayoutDefaults.StatisticLabelFontSize,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
