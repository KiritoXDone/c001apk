// Copyright 2025, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.basic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * A [FloatingActionButton] component with Miuix style.
 *
 * @param onClick The callback when the [FloatingActionButton] is clicked.
 * @param modifier The modifier to be applied to the [FloatingActionButton].
 * @param shape The shape of the [FloatingActionButton].
 * @param containerColor The color of the [FloatingActionButton].
 * @param shadowElevation The shadow elevation of the [FloatingActionButton].
 * @param minWidth The minimum width of the [FloatingActionButton].
 * @param minHeight The minimum height of the [FloatingActionButton].
 * @param content The [Composable] content of the [FloatingActionButton].
 */
@Composable
@NonRestartableComposable
fun FloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    containerColor: Color = MiuixTheme.colorScheme.primary,
    shadowElevation: Dp = FloatingActionButtonDefaults.ShadowElevation,
    minWidth: Dp = FloatingActionButtonDefaults.MinWidth,
    minHeight: Dp = FloatingActionButtonDefaults.MinHeight,
    content: @Composable () -> Unit,
) {
    // 合成实际底色后，以白图标 3:1 的对比度边界区分深浅容器。
    val backgroundLuminance = containerColor.compositeOver(MiuixTheme.colorScheme.background).luminance()
    val contentColor = if (backgroundLuminance <= 0.3f) Color.White else Color.Black
    Surface(
        onClick = onClick,
        modifier = modifier
            .semantics { role = Role.Button },
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = shadowElevation,
    ) {
        Box(
            modifier = Modifier.defaultMinSize(
                minWidth = minWidth,
                minHeight = minHeight,
            ),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

/** Contains default values used by [FloatingActionButton]. */
object FloatingActionButtonDefaults {
    /** The default minimum width of the [FloatingActionButton]. */
    val MinWidth = 60.dp

    /** The default minimum height of the [FloatingActionButton]. */
    val MinHeight = 60.dp

    /** The default shadow elevation of the [FloatingActionButton]. */
    val ShadowElevation = 4.dp
}
