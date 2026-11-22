// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/business/filter.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteFilterLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteFilterLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M9 13.414 3.586 8l-.3-.388A2.1 2.1 0 0 1 3 6.586V4.5c.021-.529.277-1.025.745-1.292.236-.134.485-.197.755-.208h15c.156.006.322.027.471.076.621.205 1.003.786 1.029 1.424v2.086A2.12 2.12 0 0 1 20.414 8L15 13.414V21l-.032.252c-.165.635-.843.884-1.415.642l-3.724-1.861a1.7 1.7 0 0 1-.438-.337c-.255-.274-.376-.636-.391-1.005ZM5 5l.005 1.584.03.037 5.672 5.672c.189.215.274.422.293.707v5.382l2 1V13c.019-.285.104-.492.293-.707l5.672-5.672.03-.037L19 5Z").toNodes(),
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
