// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/body/eye_2.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteEye2Line: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteEye2Line",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M12.485 9.036c.618.1 1.196.398 1.638.841 2.047 2.046.245 5.55-2.608 5.087a3 3 0 0 1-1.638-.841c-2.047-2.046-.245-5.55 2.608-5.087m-.649 1.98c-1.286.211-.967 2.18.328 1.968 1.286-.211.967-2.18-.328-1.968m.915-5.986c2.861.228 5.585 1.746 7.529 3.82.739.788 1.726 2.006 1.726 3.15 0 1.266-1.269 2.724-2.121 3.551-2.457 2.387-5.917 3.876-9.371 3.332a11 11 0 0 1-3.333-1.12c-1.778-.92-3.65-2.437-4.666-4.191-.274-.472-.521-1.014-.521-1.572 0-1.144.987-2.362 1.726-3.15 2.286-2.439 5.641-4.091 9.031-3.82m-1.343 1.995c-1.157.092-2.28.457-3.307.989-1.436.743-3.031 1.997-3.854 3.417-.085.147-.241.385-.241.569s.156.422.241.569c.415.716 1.007 1.337 1.623 1.882 2.152 1.905 5.005 3.011 7.876 2.342 1.931-.449 3.725-1.572 5.074-3.011.281-.3 1.174-1.346 1.174-1.782 0-.524-1.15-1.774-1.502-2.116-1.86-1.805-4.447-3.07-7.084-2.859").toNodes(),
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
