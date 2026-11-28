package com.example.c001apk.compose.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.model.FeedAiSummaryState
import com.example.c001apk.compose.ui.component.AiSparkleIcon
import com.example.c001apk.compose.ui.component.MiuixNavigationSheet
import com.example.c001apk.compose.ui.component.RichPostText
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Close
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun FeedAiSummarySheet(
    state: FeedAiSummaryState,
    onRetry: () -> Unit,
    onCopyText: (String?) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    MiuixNavigationSheet(onDismissRequest = onDismiss) { dismiss ->
        val colors = MiuixTheme.colorScheme
        val modelName =
            when (state) {
                FeedAiSummaryState.Hidden -> null
                is FeedAiSummaryState.Idle -> state.modelName
                is FeedAiSummaryState.Loading -> state.modelName
                is FeedAiSummaryState.Success -> state.modelName
                is FeedAiSummaryState.Error -> state.modelName
            }
        val copyableText =
            when (state) {
                is FeedAiSummaryState.Success -> state.text
                is FeedAiSummaryState.Loading -> state.text.takeIf(String::isNotBlank)
                else -> null
            }

        Column(Modifier.fillMaxSize().background(colors.surface)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = AiSparkleIcon,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.feed_ai_summary_title),
                    style = MiuixTheme.textStyles.body1,
                    fontWeight = FontWeight.SemiBold,
                )
                if (!modelName.isNullOrBlank()) {
                    Text(
                        text = modelName,
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariantSummary,
                    )
                }
                Spacer(Modifier.weight(1f))
                if (!copyableText.isNullOrBlank()) {
                    Text(
                        text = stringResource(R.string.action_copy),
                        style = MiuixTheme.textStyles.footnote1,
                        color = colors.primary,
                        modifier =
                            Modifier.clickable(
                                    role = Role.Button,
                                    onClickLabel = stringResource(R.string.feed_ai_summary_copy),
                                    onClick = { onCopyText(copyableText) },
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                    )
                }
                IconButton(onClick = dismiss) {
                    Icon(
                        imageVector = MiuixIcons.Basic.Close,
                        contentDescription = stringResource(R.string.feed_ai_summary_close_sheet),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            HorizontalDivider()

            val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            Column(
                modifier =
                    Modifier.weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp + navBottom),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when (state) {
                    FeedAiSummaryState.Hidden,
                    is FeedAiSummaryState.Idle -> Unit

                    is FeedAiSummaryState.Loading -> {
                        if (state.text.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    InfiniteProgressIndicator(Modifier.size(18.dp))
                                    Text(
                                        text = stringResource(R.string.feed_ai_summary_generating),
                                        style = MiuixTheme.textStyles.body2,
                                        color = colors.onSurfaceVariantSummary,
                                    )
                                }
                            }
                        } else {
                            RichPostText(
                                text = state.text,
                                textSize = 15f,
                                lineSpacingMultiplier = 1.45f,
                                explicit = true,
                                coolapk = false,
                                selectable = true,
                                onOpenLink = { url, title ->
                                    dismiss()
                                    onOpenLink(url, title)
                                },
                            )
                        }
                    }

                    is FeedAiSummaryState.Success -> {
                        RichPostText(
                            text = state.text,
                            textSize = 15f,
                            lineSpacingMultiplier = 1.45f,
                            explicit = true,
                            coolapk = false,
                            selectable = true,
                            onOpenLink = { url, title ->
                                dismiss()
                                onOpenLink(url, title)
                            },
                        )
                    }

                    is FeedAiSummaryState.Error -> {
                        Text(
                            text = state.message,
                            style = MiuixTheme.textStyles.body2,
                            color = colors.error,
                        )
                        TextButton(
                            text = stringResource(R.string.action_retry),
                            onClick = onRetry,
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                            cornerRadius = 12.dp,
                            minHeight = 44.dp,
                            insideMargin = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.feed_ai_summary_disclaimer),
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariantSummary,
                )
            }
        }
    }
}
