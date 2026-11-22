// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/editing/font_size.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteFontSizeLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteFontSizeLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M20.095 12.595c.323-.612 1.085-.829 1.612-.302.124.124.213.279.259.448L22 13v6c-.072 1.095-1.396 1.369-1.905.405-2.681 1.597-6.097-.248-6.097-3.405a4.01 4.01 0 0 1 1.556-3.168A4 4 0 0 1 18 11.998c.75 0 1.454.215 2.095.597m-2.368 1.426c-.43.058-.833.26-1.14.566-1.345 1.345-.195 3.647 1.686 3.392.43-.058.833-.26 1.14-.566 1.345-1.345.195-3.647-1.686-3.392M5.015 16l-1.063 3.306c-.073.185-.131.3-.281.435a1 1 0 0 1-.72.258c-.678-.034-1.059-.684-.903-1.305L6.415 5.109c.259-.66.859-1.117 1.585-1.117s1.326.457 1.585 1.117l4.367 13.585c.184.734-.375 1.454-1.163 1.283a1 1 0 0 1-.741-.671L10.985 16Zm.643-2h4.684L8 6.712Z").toNodes(),
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
