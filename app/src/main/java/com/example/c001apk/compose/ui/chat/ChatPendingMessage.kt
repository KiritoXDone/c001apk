package com.example.c001apk.compose.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.c001apk.compose.ui.component.MiuixActionButton
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun ChatPendingMessage(
    delivery: ChatViewModel.Delivery,
    onRetry: () -> Unit,
    onDiscard: () -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val maxBubbleWidth = (maxWidth - 80.dp).coerceAtLeast(0.dp).coerceAtMost(420.dp)
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Column(
                modifier = Modifier.widthIn(max = maxBubbleWidth)
                    .squircleSurface(MiuixTheme.colorScheme.primaryContainer, 20.dp)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.End,
            ) {
                if (delivery.preview != null) {
                    AsyncImage(
                        model = delivery.preview,
                        contentDescription = stringResource(R.string.image_pending),
                        modifier = Modifier.size(140.dp).squircleClip(16.dp),
                    )
                } else if (delivery.picture.isNotEmpty()) {
                    Text(stringResource(R.string.image_placeholder), color = MiuixTheme.colorScheme.onPrimaryContainer)
                }
                if (delivery.text.isNotBlank()) {
                    Text(delivery.text, color = MiuixTheme.colorScheme.onPrimaryContainer)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (delivery.sending) CircularProgressIndicator(modifier = Modifier.size(14.dp))
                    Text(
                        text = if (delivery.sending) stringResource(R.string.status_sending) else delivery.error.orEmpty(),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        style = MiuixTheme.textStyles.body2,
                    )
                }
            }
            if (!delivery.sending) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                    MiuixActionButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
                    MiuixActionButton(onClick = onDiscard) { Text(stringResource(R.string.action_remove)) }
                }
            }
        }
    }
}
