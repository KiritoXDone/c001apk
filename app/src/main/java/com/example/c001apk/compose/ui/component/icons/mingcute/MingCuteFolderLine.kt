// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/files/folder.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteFolderLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteFolderLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("m12.484 5.494.053.006h7.52c.445.026.846.153 1.198.435.47.377.711.915.745 1.508v11.614c-.034.593-.275 1.131-.745 1.508-.352.282-.753.409-1.198.435H3.943c-.326-.019-.524-.051-.825-.198-.683-.334-1.075-1-1.118-1.745V4.943c.063-1.1.843-1.88 1.943-1.943h5.633a2.04 2.04 0 0 1 1.47.707c.51.569.938 1.208 1.438 1.787m-2.968-.488h-5.51v13.988h15.988V7.506l-.051-.006h-7.519a2.04 2.04 0 0 1-1.47-.707c-.51-.569-.938-1.208-1.438-1.787").toNodes(),
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
