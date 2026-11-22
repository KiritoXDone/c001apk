// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/editing/sort_ascending.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteSortAscendingLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteSortAscendingLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M3.741 19.966C2.643 19.672 2.796 18.079 4 18h9c.543.036 1 .428 1 1s-.457.964-1 1H4ZM17 6.586l-1.121 1.121c-.149.131-.254.207-.449.259-.842.226-1.561-.71-1.124-1.466l.158-.207 2.829-2.828c.204-.179.429-.293.707-.293s.503.114.707.293l2.829 2.828.158.207c.437.756-.282 1.692-1.124 1.466-.195-.052-.3-.128-.449-.259L19 6.586V19c-.036.543-.428 1-1 1s-.964-.457-1-1Zm-13.259 6.38C2.643 12.672 2.796 11.079 4 11h9c.543.036 1 .428 1 1s-.457.964-1 1H4Zm0-7c-.978-.262-.978-1.67 0-1.932L4 4h7c.451.03.844.288.966.741.176.656-.327 1.217-.966 1.259H4Z").toNodes(),
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
