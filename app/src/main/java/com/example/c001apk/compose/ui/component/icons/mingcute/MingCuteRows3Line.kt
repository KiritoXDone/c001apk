// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/design/rows_3.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteRows3Line: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteRows3Line",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M19.458 13.046c.9.21 1.49.995 1.542 1.897v4.114c-.043.745-.435 1.411-1.118 1.745-.301.147-.499.179-.825.198H4.943c-.745-.043-1.411-.435-1.745-1.118-.147-.301-.179-.499-.198-.825v-4.114c.063-1.1.843-1.88 1.943-1.943h14.114Zm-14.452 1.96v3.988h13.988v-3.988Zm14.452-11.96c.9.21 1.49.995 1.542 1.897v4.114c-.043.745-.435 1.411-1.118 1.745-.301.147-.499.179-.825.198H4.943c-.745-.043-1.411-.435-1.745-1.118-.147-.301-.179-.499-.198-.825V4.943c.063-1.1.843-1.88 1.943-1.943h14.114ZM5.006 5.006v3.988h13.988V5.006Z").toNodes(),
            pathFillType = PathFillType.NonZero,
            fill = SolidColor(Color(0xFF000000)),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter,
            strokeLineMiter = 4f,
        )
    }.build()
}
