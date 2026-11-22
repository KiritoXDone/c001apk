// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/users/user_forbid.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteUserForbidLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteUserForbidLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M17.459 21.966a4.003 4.003 0 0 1-3.318-2.903c-.757-2.757 1.57-5.411 4.4-5.029a4.003 4.003 0 0 1 3.318 2.903c.757 2.757-1.57 5.411-4.4 5.029m1.057-2.036-2.446-2.446c-.321 1.571.875 2.767 2.446 2.446m-1.032-3.86 2.446 2.446c.321-1.571-.875-2.767-2.446-2.446M11.002 15c-1.99.004-4.093.551-5.701 1.76-.397.298-1.54 1.307-1.254 1.922.108.233.399.385.613.491.691.34 1.484.481 2.238.59 1.107.16 2.23.213 3.348.231L11 20c.136 0 .687-.034.924.028.433.113.744.513.747.961s-.303.852-.734.971c-.236.064-.814.04-.938.04l-1.554-.025c-2.042-.072-6.15-.264-7.204-2.404-.746-1.515.302-3.087 1.424-4.061 1.981-1.721 4.747-2.506 7.333-2.51.303-.001.984.022 1.292.067.861.126 1.274 1.157.569 1.758-.387.33-.836.215-1.301.19Zm-.584-3.032a5.03 5.03 0 0 1-2.955-1.431A5.03 5.03 0 0 1 6.13 8.146c-.787-3.362 2.024-6.511 5.452-6.114a5.03 5.03 0 0 1 2.955 1.431 5.03 5.03 0 0 1 1.333 2.391c.787 3.362-2.024 6.511-5.452 6.114m.231-7.946a3 3 0 0 0-1.524.638c-2.339 1.876-.751 5.663 2.226 5.318a3 3 0 0 0 1.524-.638c2.339-1.876.751-5.663-2.226-5.318").toNodes(),
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
