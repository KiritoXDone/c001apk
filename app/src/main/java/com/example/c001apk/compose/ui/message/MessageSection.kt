package com.example.c001apk.compose.ui.message

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.component.cards.MessageListCard
import com.example.c001apk.compose.ui.notification.NoticeType
import top.yukonga.miuix.kmp.basic.Card

@Composable
internal fun MessageSection(
    counts: Map<NoticeType, Int?>,
    onViewNotice: (NoticeType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            MessageEntry.entries.forEach { entry ->
                MessageListCard(
                    modifier = Modifier.weight(1f),
                    icon = { MessageEntryIcon(entry) },
                    title = entry.title,
                    count = entry.unreadCount(counts),
                    onViewNotice = { onViewNotice(entry.destination) },
                )
            }
        }
    }
}
