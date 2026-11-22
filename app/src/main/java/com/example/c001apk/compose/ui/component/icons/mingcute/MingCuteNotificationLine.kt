// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/media/notification.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteNotificationLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteNotificationLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M8.128 18H4.162c-.73-.031-1.263-.714-1.047-1.433l.063-.159 1.699-3.398c.055-.121.088-.2.108-.328L5 12.489V9c.009-.449.04-.84.118-1.282.583-3.276 3.605-5.764 6.929-5.717 3.319.047 6.248 2.416 6.835 5.717.078.442.109.833.118 1.282v3.489l.015.193c.02.128.053.207.108.328l1.699 3.398.063.159c.216.719-.316 1.402-1.047 1.433h-3.966c-1.148 3.988-6.596 3.988-7.744 0m2.142 0c.89 1.338 2.57 1.338 3.46 0Zm-4.652-2h12.764l-1.065-2.13a3.2 3.2 0 0 1-.29-.957L17 12.528V9.024a5.5 5.5 0 0 0-.201-1.428c-1.203-4.126-6.756-4.887-9.038-1.248-.509.813-.738 1.723-.761 2.676v3.504l-.027.385a3.2 3.2 0 0 1-.29.957Z").toNodes(),
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
