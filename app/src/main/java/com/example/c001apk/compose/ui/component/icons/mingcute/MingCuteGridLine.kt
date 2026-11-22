// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/system/grid.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteGridLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteGridLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M9.114 13.007c1.088.099 1.846.901 1.886 1.993v4a2.1 2.1 0 0 1-.376 1.162c-.38.531-.979.814-1.624.838H5a2.1 2.1 0 0 1-1.162-.376c-.531-.38-.814-.979-.838-1.624v-4c.042-1.135.865-1.958 2-2h4Zm-4.108 1.999v3.988h3.988v-3.988Zm14.108-1.999c1.088.099 1.846.901 1.886 1.993v4a2.1 2.1 0 0 1-.376 1.162c-.38.531-.979.814-1.624.838h-4a2.1 2.1 0 0 1-1.162-.376c-.531-.38-.814-.979-.838-1.624v-4c.042-1.135.865-1.958 2-2h4Zm-4.108 1.999v3.988h3.988v-3.988ZM9.114 3.007C10.202 3.106 10.96 3.908 11 5v4a2.1 2.1 0 0 1-.376 1.162c-.38.531-.979.814-1.624.838H5a2.1 2.1 0 0 1-1.162-.376C3.307 10.244 3.024 9.645 3 9V5c.042-1.135.865-1.958 2-2h4ZM5.006 5.006v3.988h3.988V5.006Zm14.108-1.999C20.202 3.106 20.96 3.908 21 5v4a2.1 2.1 0 0 1-.376 1.162c-.38.531-.979.814-1.624.838h-4a2.1 2.1 0 0 1-1.162-.376c-.531-.38-.814-.979-.838-1.624V5c.042-1.135.865-1.958 2-2h4Zm-4.108 1.999v3.988h3.988V5.006Z").toNodes(),
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
