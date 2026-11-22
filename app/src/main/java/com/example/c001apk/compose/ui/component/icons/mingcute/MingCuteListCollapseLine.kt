// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/editing/list_collapse.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteListCollapseLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteListCollapseLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M18.223 14.976c.247.138.984 1.242 1.16 1.499.231.338 1.572 2.329 1.485 2.687-.107.438-.877.361-1.2.379-.538.029-4.092.13-4.428-.159-.256-.22-.057-.512.065-.742l.189-.346a24 24 0 0 1 1.7-2.617c.14-.188.365-.563.575-.688a.46.46 0 0 1 .454-.013m-5.964 2.058c1.098.294.945 1.887-.259 1.966H4c-.543-.036-1-.428-1-1s.457-.964 1-1h8Zm0-6c1.098.294.945 1.887-.259 1.966H4c-.543-.036-1-.428-1-1s.457-.964 1-1h8Zm5.964-6.058c.247.138.984 1.242 1.16 1.499.231.339 1.572 2.329 1.485 2.687-.107.438-.877.362-1.2.379-.538.03-4.092.13-4.428-.159-.256-.22-.057-.512.065-.742l.189-.346a24 24 0 0 1 1.7-2.616c.14-.188.365-.564.575-.689a.46.46 0 0 1 .454-.013m-5.964.058c1.098.294.945 1.887-.259 1.966H4c-.543-.036-1-.428-1-1s.457-.964 1-1h8Z").toNodes(),
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
