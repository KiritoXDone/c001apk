package com.example.c001apk.compose.ui.feed

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.model.HomeFeedResponse.Vote
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun FeedVoteContent(
    vote: Vote?,
    title: String?,
    canVote: Boolean,
    submitting: Boolean,
    refreshing: Boolean,
    error: String?,
    onSubmit: (Set<String>, Boolean) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalDivider()
        Text(
            text = title ?: "投票选项",
            style = MiuixTheme.textStyles.headline2.copy(fontWeight = FontWeight.SemiBold),
        )
        val options = vote?.availableOptions.orEmpty()
        if (vote == null || options.isEmpty()) {
            VoteSummary("暂无投票选项")
            return@Column
        }

        val now by produceState(System.currentTimeMillis() / 1_000, vote.startTime, vote.endTime) {
            value = System.currentTimeMillis() / 1_000
            for (boundary in listOfNotNull(vote.startTime, vote.endTime).sorted()) {
                val remaining = boundary * 1_000 - System.currentTimeMillis()
                if (remaining > 0) delay(remaining)
                value = System.currentTimeMillis() / 1_000
            }
        }
        val closed = vote.isClosed(now)
        val upcoming = vote.isUpcoming(now)
        val votedIds = vote.selectedIds
        val showResults = closed || votedIds.isNotEmpty()
        val limits = vote.selectionRange
        val multiSelect = limits.last > 1
        val canSelect = canVote && !upcoming && !showResults && !vote.id.isNullOrBlank() &&
            !limits.isEmpty() && options.size >= limits.first
        var selection by rememberSaveable(vote.id) { mutableStateOf(arrayListOf<String>()) }
        var anonymous by rememberSaveable(vote.id) { mutableStateOf(false) }
        val selected = options.mapNotNull { it.id }.filter(selection::contains).toSet()

        if (showResults) {
            val totalSelections = options.sumOf { it.totalSelectNum?.coerceAtLeast(0) ?: 0L }
            for (option in options) {
                val count = option.totalSelectNum?.coerceAtLeast(0)
                val fraction = count?.let { if (totalSelections > 0) it.toFloat() / totalSelections else 0f }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(option.title.orEmpty(), Modifier.weight(1f))
                        if (option.id in votedIds) VoteSummary("已选")
                        if (count != null && fraction != null) {
                            VoteSummary("${(fraction * 100).roundToInt()}% · ${count} 票")
                        }
                    }
                    if (fraction != null) {
                        LinearProgressIndicator(progress = fraction, height = 4.dp)
                    }
                }
            }
        } else {
            if (canSelect && multiSelect) {
                VoteSummary(if (limits.first == limits.last) "选 ${limits.last} 项" else "选 ${limits.first}–${limits.last} 项")
            }
            Column(
                modifier = if (multiSelect) Modifier else Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (option in options) {
                    val optionId = option.id ?: continue
                    VoteChoice(
                        label = option.title.orEmpty(),
                        selected = optionId in selected,
                        multiSelect = multiSelect,
                        enabled = canSelect && !submitting && !refreshing &&
                            (!multiSelect || optionId in selected || selected.size < limits.last),
                        onToggle = {
                            selection = if (!multiSelect) arrayListOf(optionId)
                            else ArrayList(if (it) selected + optionId else selected - optionId)
                        },
                    )
                }
            }
        }

        val status = when {
            closed -> "投票已结束"
            upcoming -> "投票未开始"
            votedIds.isNotEmpty() -> "已投票"
            !canVote -> "登录后投票"
            !canSelect -> "投票暂不可用"
            else -> null
        }
        if (canSelect && vote.anonymousStatus == 1) {
            AnonymousVoteSwitch(
                checked = anonymous,
                enabled = !submitting && !refreshing,
                onCheckedChange = { anonymous = it },
            )
        }
        val participation = vote.totalVoteNum?.let { "${it} 人参与" }
        listOfNotNull(participation, status).joinToString(" · ")
            .takeIf(String::isNotEmpty)?.let { VoteSummary(it) }
        if (canSelect) {
            error?.let { Text(it, color = MiuixTheme.colorScheme.error) }
            TextButton(
                text = if (submitting) "正在投票…" else "投票",
                onClick = { onSubmit(selected, anonymous) },
                enabled = !submitting && !refreshing && selected.size in limits,
                colors = ButtonDefaults.textButtonColorsPrimary(),
                cornerRadius = 12.dp,
                minHeight = 44.dp,
                insideMargin = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        HorizontalDivider()
    }
}

@Composable
private fun VoteChoice(
    label: String,
    selected: Boolean,
    multiSelect: Boolean,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val colors = MiuixTheme.colorScheme
    val corner = 12.dp
    val row = Modifier.fillMaxWidth().heightIn(min = 48.dp).squircleClip(corner)
        .background(if (selected) colors.primary.copy(alpha = 0.09f) else colors.onSurface.copy(alpha = 0.025f))
        .squircleBorder(
            width = 1.dp,
            color = if (selected) colors.primary.copy(alpha = 0.8f) else colors.onSurface.copy(alpha = 0.17f),
            cornerRadius = corner,
        )
    val interaction = if (multiSelect) {
        row.toggleable(selected, enabled = enabled, role = Role.Checkbox, onValueChange = onToggle)
    } else {
        row.selectable(selected, enabled = enabled, role = Role.RadioButton) { onToggle(true) }
    }
    Row(
        modifier = interaction.padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            label,
            Modifier.weight(1f),
            color = if (enabled || selected) colors.onSurface else colors.onSurfaceVariantSummary,
        )
        VoteChoiceIndicator(checked = selected, multiSelect = multiSelect, enabled = enabled)
    }
}

@Composable
private fun VoteChoiceIndicator(checked: Boolean, multiSelect: Boolean, enabled: Boolean) {
    val colors = MiuixTheme.colorScheme
    val indicator = when {
        checked -> colors.primary
        enabled -> colors.onSurface.copy(alpha = 0.53f)
        else -> colors.onSurface.copy(alpha = 0.3f)
    }
    val checkColor = colors.onPrimary
    // 选择语义由整行提供，这里只绘制设计稿里的圆形单选与方形多选。
    Canvas(Modifier.size(20.dp)) {
        val stroke = 1.5.dp.toPx()
        val inset = stroke / 2
        if (multiSelect) {
            drawRoundRect(
                color = indicator,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke),
                cornerRadius = CornerRadius(4.dp.toPx()),
                style = if (checked) Fill else Stroke(stroke),
            )
            if (checked) {
                val check = Path().apply {
                    moveTo(size.width * 0.26f, size.height * 0.51f)
                    lineTo(size.width * 0.43f, size.height * 0.67f)
                    lineTo(size.width * 0.75f, size.height * 0.34f)
                }
                drawPath(check, checkColor, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        } else {
            drawCircle(indicator, radius = size.minDimension / 2 - inset, style = Stroke(stroke))
            if (checked) drawCircle(indicator, radius = size.minDimension * 0.25f)
        }
    }
}

@Composable
private fun AnonymousVoteSwitch(
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column {
        HorizontalDivider()
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .toggleable(checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
                .padding(horizontal = 2.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("匿名投票", Modifier.weight(1f), style = MiuixTheme.textStyles.body2)
            Switch(checked = checked, onCheckedChange = null, enabled = enabled)
        }
    }
}

@Composable
private fun VoteSummary(text: String) {
    Text(text, style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
}
