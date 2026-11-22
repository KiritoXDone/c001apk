// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/weather/moon.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteMoonLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteMoonLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M10.625 20.896a9 9 0 0 1-3.67-1.443 9.06 9.06 0 0 1-3.259-3.982 9 9 0 0 1-.42-1.26c-.164-.651-.327-1.384.419-1.736.195-.092.419-.118.63-.074.274.057.521.206.787.294l.607.167c.428.094.842.137 1.281.138a6.03 6.03 0 0 0 4.777-2.369 6.03 6.03 0 0 0 1.188-4.282 6 6 0 0 0-.269-1.237c-.188-.568-.537-1.046-.105-1.603a1 1 0 0 1 .331-.276c.198-.103.411-.127.63-.097 1.457.2 3.024.979 4.153 1.905 5.506 4.515 3.804 13.395-2.988 15.542a9.1 9.1 0 0 1-4.092.313m-5.001-6.015a7.1 7.1 0 0 0 1.849 2.457 7 7 0 0 0 3.887 1.633c4.849.439 8.686-4.14 7.387-8.84a7 7 0 0 0-1.409-2.658 7.1 7.1 0 0 0-2.457-1.849c.089.683.148 1.343.092 2.032-.294 3.636-3.074 6.653-6.675 7.239-.901.147-1.774.104-2.674-.014").toNodes(),
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
