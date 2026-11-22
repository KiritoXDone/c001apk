// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/buildings/home_4.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteHome4Line: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteHome4Line",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M21 19.057c-.063 1.1-.843 1.88-1.943 1.943h-5.09l-.436-.118-.413-.413-.118-.436V14.04c-.02-.497-.321-.939-.836-1.024-.649-.106-1.139.406-1.164 1.024v5.993l-.118.436-.413.413-.436.118h-5.09l-.401-.046c-.9-.21-1.49-.995-1.542-1.897V9.442c.019-.331.053-.535.205-.839s.296-.454.548-.668l7.099-5.324c.749-.489 1.547-.489 2.296 0l7.099 5.324c.252.214.396.364.548.668.152.303.186.508.205.839ZM15 19l3.994-.006V9.503L12 4.26 5.006 9.503v9.491L9 19v-5.04l.036-.445c.423-2.608 3.827-3.419 5.387-1.289.376.513.552 1.103.577 1.734Z").toNodes(),
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
