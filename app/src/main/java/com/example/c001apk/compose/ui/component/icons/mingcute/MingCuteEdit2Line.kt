// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/editing/edit_2.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteEdit2Line: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteEdit2Line",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M3.738 20.967c-.454-.122-.712-.516-.742-.966v-5.243c.015-.43.137-.84.395-1.189l.191-.225L13.896 3.03c.69-.638 1.714-.781 2.515-.25l.313.25 4.243 4.243c.771.834.771 1.994 0 2.828l-2.433 2.433-7.881 7.881c-.396.37-.872.567-1.414.586H3.996Zm1.263-6.207-.005 4.241 4.241-.005 7.176-7.169-4.243-4.242Zm8.583-8.59 4.243 4.243 1.714-1.726-4.231-4.231Z").toNodes(),
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
