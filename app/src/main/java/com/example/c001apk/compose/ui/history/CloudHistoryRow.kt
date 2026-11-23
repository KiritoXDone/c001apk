package com.example.c001apk.compose.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.model.CloudHistoryEntry
import com.example.c001apk.compose.ui.component.CoilLoader
import com.example.c001apk.compose.util.DateUtils.fromToday
import com.example.c001apk.compose.util.Utils.richToString
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Recent
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun CloudHistoryRow(
    entry: CloudHistoryEntry,
    onOpenLink: (String, String?) -> Unit,
    onLongClick: (() -> Unit)? = null,
    onAvatarClick: (() -> Unit)? = null,
    endActions: (@Composable RowScope.() -> Unit)? = null,
) {
    val title = remember(entry.title, entry.typeName) {
        entry.title.orEmpty().richToString().ifBlank { entry.typeName.orEmpty().ifBlank { "浏览记录" } }
    }
    val summary = remember(entry.description) { entry.description.orEmpty().richToString() }
    BasicComponent(
        modifier = if (onLongClick != null) Modifier.combinedClickable(
            onClick = { onOpenLink(entry.url.orEmpty(), entry.title) }, onLongClick = onLongClick,
        ) else Modifier,
        onClick = if (onLongClick == null) ({ onOpenLink(entry.url.orEmpty(), entry.title) }) else null,
        endActions = endActions,
        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        startAction = {
            Box(
                Modifier.size(44.dp).squircleClip(8.dp).background(MiuixTheme.colorScheme.secondaryContainer)
                    .then(if (onAvatarClick != null) Modifier.clickable(onClick = onAvatarClick) else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                if (entry.logo.isNullOrBlank()) Icon(MiuixIcons.Recent, null, Modifier.size(24.dp),
                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                else CoilLoader(url = entry.logo, modifier = Modifier.fillMaxSize())
            }
        },
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, modifier = Modifier.weight(1f), style = MiuixTheme.textStyles.headline2,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            entry.dateline?.takeIf { it > 0 }?.let {
                Text(fromToday(it.coerceAtMost(System.currentTimeMillis() / 1000)),
                    modifier = Modifier.widthIn(max = 96.dp), style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (summary.isNotBlank()) Text(summary, modifier = Modifier.padding(top = 4.dp),
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
