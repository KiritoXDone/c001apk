package com.example.c001apk.compose.ui.component.cards

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.theme.MiuixTheme

enum class SkeletonType {
    FEED,
    DETAIL,
    REPLY,
}

@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    type: SkeletonType = SkeletonType.FEED,
) {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val visibility = animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 160, easing = LinearEasing),
        label = "skeleton-appearance",
    )
    val colors = MiuixTheme.colorScheme
    val baseColor = colors.onSurface.copy(alpha = 0.07f)
    val highlightColor = if (colors.surface.luminance() < 0.5f) {
        colors.onSurface.copy(alpha = 0.12f)
    } else {
        colors.surfaceContainer.copy(alpha = 0.8f)
    }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val progress = rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "skeleton-highlight",
    )
    val height = when (type) {
        SkeletonType.FEED -> 280.dp
        SkeletonType.DETAIL -> 380.dp
        SkeletonType.REPLY -> 132.dp
    }
    val horizontalPadding = when (type) {
        SkeletonType.FEED -> 16.dp
        SkeletonType.DETAIL -> FEED_CONTENT_HORIZONTAL_PADDING
        SkeletonType.REPLY -> LocalDetailReplyHorizontalPadding.current ?: FEED_REPLY_HORIZONTAL_PADDING
    }
    val authorInset = if (type == SkeletonType.DETAIL) {
        FEED_DETAIL_AUTHOR_HORIZONTAL_PADDING - horizontalPadding
    } else {
        0.dp
    }
    val verticalPadding = if (type == SkeletonType.REPLY) FEED_REPLY_VERTICAL_PADDING else 16.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("skeleton-${type.name.lowercase()}")
            .progressSemantics()
            .graphicsLayer { alpha = visibility.value },
        cornerRadius = if (type == SkeletonType.FEED) 16.dp else 20.dp,
        showIndication = false,
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                .drawWithCache {
                    val path = skeletonPath(type, isRtl, authorInset.toPx())
                    val brush = Brush.linearGradient(
                        colors = listOf(
                            highlightColor.copy(alpha = 0f),
                            highlightColor,
                            highlightColor.copy(alpha = 0f),
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width * 0.6f, size.height * 0.2f),
                    )
                    onDrawBehind {
                        drawPath(path, baseColor)
                        clipPath(path) {
                            // 动画状态只在绘制阶段读取，亮带不触发卡片重组或参与触摸。
                            val offset = progress.value * size.width * if (isRtl) -1f else 1f
                            translate(left = offset) {
                                drawRect(brush)
                            }
                        }
                    }
                },
        )
    }
}

private fun CacheDrawScope.skeletonPath(type: SkeletonType, isRtl: Boolean, authorInsetPx: Float): Path {
    val path = Path()
    fun block(x: Float, y: Float, width: Float, height: Float, radius: Float = 5.dp.toPx()) {
        val start = x.coerceAtLeast(0f)
        val blockWidth = (x + width).coerceAtMost(size.width) - start
        if (blockWidth <= 0f) return
        val left = if (isRtl) size.width - start - blockWidth else start
        path.addRoundRect(
            RoundRect(Rect(left, y, left + blockWidth, y + height), CornerRadius(radius)),
        )
    }

    val avatarSize = when (type) {
        SkeletonType.FEED -> 36.dp
        SkeletonType.DETAIL -> 48.dp
        SkeletonType.REPLY -> FEED_REPLY_AVATAR_SIZE
    }.toPx()
    val avatarGap = if (type == SkeletonType.REPLY) FEED_REPLY_AVATAR_GAP else 12.dp
    val headerStart = authorInsetPx + avatarSize + avatarGap.toPx()
    block(authorInsetPx, 0f, avatarSize, avatarSize, if (type == SkeletonType.DETAIL) 16.dp.toPx() else avatarSize / 2)
    block(
        headerStart,
        4.dp.toPx(),
        (if (type == SkeletonType.REPLY) 94.dp else 112.dp).toPx(),
        (if (type == SkeletonType.DETAIL) 14.dp else 10.dp).toPx(),
    )
    if (type != SkeletonType.REPLY) {
        block(headerStart, 30.dp.toPx(), (if (type == SkeletonType.DETAIL) 142.dp else 70.dp).toPx(), 8.dp.toPx())
    }

    val textStart = if (type == SkeletonType.REPLY) headerStart else 0f
    val textWidth = size.width - textStart
    val textTop = when (type) {
        SkeletonType.FEED -> 55.dp
        SkeletonType.DETAIL -> 70.dp
        SkeletonType.REPLY -> 26.dp
    }
    val lineCount = when (type) {
        SkeletonType.FEED -> 2
        SkeletonType.DETAIL -> 4
        SkeletonType.REPLY -> 2
    }
    repeat(lineCount) { index ->
        val widthFraction = when {
            index == lineCount - 1 -> 0.64f
            index == 1 -> 0.88f
            else -> 1f
        }
        block(
            textStart,
            (textTop + (if (type == SkeletonType.REPLY) 20.dp else 25.dp) * index).toPx(),
            textWidth * widthFraction,
            (if (type == SkeletonType.REPLY) 11.dp else 13.dp).toPx(),
        )
    }
    when (type) {
        SkeletonType.FEED -> block(0f, 125.dp.toPx(), size.width * 0.82f, 100.dp.toPx(), 10.dp.toPx())
        SkeletonType.DETAIL -> block(0f, 188.dp.toPx(), size.width, 132.dp.toPx(), 10.dp.toPx())
        SkeletonType.REPLY -> Unit
    }
    val footerY = size.height - 10.dp.toPx()
    block(textStart, footerY, textWidth * 0.2f, 8.dp.toPx())
    block(size.width - 30.dp.toPx(), footerY, 30.dp.toPx(), 8.dp.toPx())
    return path
}
