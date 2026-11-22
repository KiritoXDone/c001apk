// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/files/search.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteSearchLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteSearchLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("m17.176 15.762 3.652 3.652c.359.41.405 1.01 0 1.414-.404.405-1.004.359-1.414 0l-3.652-3.652c-1.145.85-2.399 1.459-3.814 1.702C5.871 19.921.72 14.327 2.272 8.355a8.5 8.5 0 0 1 1.979-3.618A8.55 8.55 0 0 1 7.68 2.478c6.112-2.146 12.292 3.095 11.198 9.47-.243 1.415-.852 2.669-1.702 3.814M9.938 4.025a6.5 6.5 0 0 0-3.844 1.697 6.5 6.5 0 0 0-1.727 2.622c-1.558 4.436 2.021 9.03 6.695 8.631a6.5 6.5 0 0 0 3.844-1.697 6.5 6.5 0 0 0 1.727-2.622c1.558-4.436-2.021-9.03-6.695-8.631").toNodes(),
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
