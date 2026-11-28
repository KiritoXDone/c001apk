package com.example.c001apk.compose.ui.feed

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.model.FeedAiSummaryState
import com.example.c001apk.compose.logic.model.FeedAiSummaryTrigger
import com.example.c001apk.compose.ui.component.AiSparkleIcon
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ExpandMore
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FeedAiSummaryCard(
    feedId: String,
    state: FeedAiSummaryState,
    onCardClick: () -> Unit,
    onRetry: () -> Unit,
    onCopyText: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val plan =
        when (state) {
            FeedAiSummaryState.Hidden -> return
            is FeedAiSummaryState.Idle -> state.plan
            is FeedAiSummaryState.Loading -> state.plan
            is FeedAiSummaryState.Success -> state.plan
            is FeedAiSummaryState.Error -> state.plan
        }
    val modelName =
        when (state) {
            FeedAiSummaryState.Hidden -> return
            is FeedAiSummaryState.Idle -> state.modelName
            is FeedAiSummaryState.Loading -> state.modelName
            is FeedAiSummaryState.Success -> state.modelName
            is FeedAiSummaryState.Error -> state.modelName
        }
    val colors = MiuixTheme.colorScheme
    val isModalMode = plan.trigger == FeedAiSummaryTrigger.ClickModal
    val isWholeCardClickable =
        isModalMode || state is FeedAiSummaryState.Idle || state is FeedAiSummaryState.Error
    val wholeCardClickLabel =
        when {
            state is FeedAiSummaryState.Success && isModalMode ->
                stringResource(R.string.feed_ai_summary_tap_to_view)
            state is FeedAiSummaryState.Error ->
                stringResource(R.string.action_retry)
            else ->
                stringResource(R.string.feed_ai_summary_tap_to_fetch)
        }
    var expanded by rememberSaveable(feedId) { mutableStateOf(true) }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .squircleClip(16.dp)
                .background(colors.surfaceContainerHigh)
                .then(
                    if (isWholeCardClickable) {
                        Modifier.clickable(
                            role = Role.Button,
                            onClickLabel = wholeCardClickLabel,
                            onClick = onCardClick,
                        )
                    } else {
                        Modifier
                    },
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("feed_ai_summary_card"),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val showInlineControls = !isModalMode && state is FeedAiSummaryState.Success
        val toggleLabel =
            stringResource(
                if (expanded) R.string.feed_ai_summary_collapse
                else R.string.feed_ai_summary_expand,
            )
        Row(
            modifier =
                Modifier.fillMaxWidth().then(
                    if (showInlineControls) {
                        Modifier.clickable(
                            role = Role.Button,
                            onClickLabel = toggleLabel,
                            onClick = { expanded = !expanded },
                        )
                    } else {
                        Modifier
                    },
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = AiSparkleIcon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = stringResource(R.string.feed_ai_summary_title),
                style = MiuixTheme.textStyles.footnote1,
                fontWeight = FontWeight.Medium,
                color = colors.primary,
            )
            Spacer(Modifier.weight(1f))
            if (!modelName.isNullOrBlank()) {
                Text(
                    text = modelName,
                    fontSize = 11.sp,
                    color = colors.onSurfaceVariantSummary,
                )
            }
            if (showInlineControls) {
                val summaryText = (state as FeedAiSummaryState.Success).text
                if (expanded) {
                    Text(
                        text = stringResource(R.string.action_copy),
                        fontSize = 11.sp,
                        color = colors.onSurfaceVariantSummary,
                        modifier =
                            Modifier.clickable(
                                    role = Role.Button,
                                    onClickLabel = stringResource(R.string.feed_ai_summary_copy),
                                    onClick = { onCopyText(summaryText) },
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                    )
                }
                Icon(
                    imageVector = MiuixIcons.ExpandMore,
                    contentDescription = toggleLabel,
                    tint = colors.onSurfaceVariantActions,
                    modifier = Modifier.size(16.dp).rotate(if (expanded) 180f else 0f),
                )
            }
        }

        when (state) {
            FeedAiSummaryState.Hidden -> Unit

            is FeedAiSummaryState.Idle -> {
                Text(
                    text = stringResource(R.string.feed_ai_summary_tap_to_fetch),
                    style = MiuixTheme.textStyles.body2,
                    color = colors.primary,
                    modifier = Modifier.testTag("feed_ai_summary_trigger"),
                )
            }

            is FeedAiSummaryState.Loading -> {
                if (isModalMode || state.text.isEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        InfiniteProgressIndicator(Modifier.size(14.dp))
                        Text(
                            text =
                                stringResource(
                                    if (state.isPreparingContext) R.string.feed_ai_summary_preparing
                                    else R.string.feed_ai_summary_generating,
                                ),
                            style = MiuixTheme.textStyles.body2,
                            color = colors.onSurfaceVariantSummary,
                        )
                    }
                } else {
                    Text(
                        text = state.text,
                        style = MiuixTheme.textStyles.body2,
                        lineHeight = 22.sp,
                        color = colors.onSurface,
                        modifier = Modifier.fillMaxWidth().testTag("feed_ai_summary_text"),
                    )
                }
            }

            is FeedAiSummaryState.Success -> {
                if (isModalMode) {
                    Text(
                        text = stringResource(R.string.feed_ai_summary_tap_to_view),
                        style = MiuixTheme.textStyles.body2,
                        color = colors.primary,
                        modifier = Modifier.testTag("feed_ai_summary_trigger"),
                    )
                } else if (expanded) {
                    Text(
                        text = state.text,
                        style = MiuixTheme.textStyles.body2,
                        lineHeight = 22.sp,
                        color = colors.onSurface,
                        modifier =
                            Modifier.fillMaxWidth()
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = { onCopyText(state.text) },
                                    onLongClickLabel = stringResource(R.string.feed_ai_summary_copy),
                                )
                                .testTag("feed_ai_summary_text"),
                    )
                }
            }

            is FeedAiSummaryState.Error -> {
                Text(
                    text = state.message,
                    style = MiuixTheme.textStyles.footnote1,
                    color = colors.error,
                )
                Row(
                    modifier =
                        Modifier.clickable(
                                role = Role.Button,
                                onClickLabel = stringResource(R.string.action_retry),
                                onClick = onRetry,
                            )
                            .padding(vertical = 2.dp)
                            .testTag("feed_ai_summary_retry"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = MiuixIcons.Refresh,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = stringResource(R.string.action_retry),
                        style = MiuixTheme.textStyles.footnote1,
                        color = colors.primary,
                    )
                }
            }
        }

        if (!showInlineControls || expanded) {
            Text(
                text = stringResource(R.string.feed_ai_summary_disclaimer),
                fontSize = 11.sp,
                color = colors.onSurfaceVariantSummary,
            )
        }
    }
}
