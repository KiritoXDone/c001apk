// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/system/add_square.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteAddSquareLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteAddSquareLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M13 13v3c-.05.755-.802 1.269-1.5.866a1 1 0 0 1-.366-.366c-.101-.175-.121-.302-.134-.5v-3H8c-.755-.05-1.269-.802-.866-1.5.088-.152.214-.278.366-.366.175-.101.302-.121.5-.134h3V8c.05-.755.802-1.269 1.5-.866.152.088.278.214.366.366.101.175.121.302.134.5v3h3c.755.05 1.269.802.866 1.5a1 1 0 0 1-.366.366c-.175.101-.302.121-.5.134Zm8 6.057c-.063 1.1-.843 1.88-1.943 1.943H4.943c-.445-.026-.846-.153-1.198-.435-.47-.377-.711-.915-.745-1.508V4.943c.026-.445.153-.846.435-1.198.377-.47.915-.711 1.508-.745h14.114c.593.034 1.131.275 1.508.745.282.352.409.753.435 1.198ZM5.006 5.006v13.988h13.988V5.006Z").toNodes(),
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
