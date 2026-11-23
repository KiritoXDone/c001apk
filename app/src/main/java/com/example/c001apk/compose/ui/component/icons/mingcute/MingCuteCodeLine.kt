// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/development/code.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteCodeLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteCodeLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M14.486 3.144c.551.154.805.7.693 1.233l-4.431 15.787c-.181.514-.683.847-1.233.693-.551-.155-.805-.701-.693-1.233l4.431-15.788c.181-.513.683-.847 1.233-.692M20.329 12l-3.536-3.536c-.422-.481-.381-1.233.208-1.573.406-.235.867-.139 1.207.159l4.242 4.243c.179.204.293.429.293.707s-.114.503-.293.707l-4.242 4.243c-.205.179-.43.293-.708.293-.755 0-1.243-.846-.866-1.5l.159-.208ZM3.672 12l3.536 3.535.158.208c.378.654-.11 1.5-.866 1.5-.277 0-.503-.114-.707-.293l-4.242-4.243c-.179-.204-.293-.429-.293-.707s.114-.503.293-.707L5.793 7.05c.34-.298.801-.394 1.207-.159.589.34.63 1.092.208 1.573Z").toNodes(),
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
