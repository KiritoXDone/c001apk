// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/editing/text_2.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteText2Line: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteText2Line",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M13 6v13h1l.259.034c.842.226.996 1.396.241 1.832-.175.101-.302.121-.5.134h-4l-.259-.034c-.842-.226-.996-1.396-.241-1.832.398-.23 1.148-.134 1.5-.134V6H6v1c-.05.755-.802 1.269-1.5.866a1 1 0 0 1-.366-.366C4.033 7.325 4.013 7.198 4 7V5c.037-.571.429-.963 1-1h14l.259.034c.453.122.711.515.741.966v2c-.013.198-.033.325-.134.5a1 1 0 0 1-.607.466C18.603 8.142 18.042 7.639 18 7V6Z").toNodes(),
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
