// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/system/more_2.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteMore2Line: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteMore2Line",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("m12.475 16.564.418.216c.985.72.742 2.274-.418 2.656l-.475.077-.475-.077-.418-.216c-.985-.72-.742-2.274.418-2.656l.475-.077Zm0-6 .418.216c.985.72.742 2.274-.418 2.656l-.475.077-.475-.077-.418-.216c-.985-.72-.742-2.274.418-2.656l.475-.077Zm0-6 .418.216c.985.72.742 2.274-.418 2.656L12 7.513l-.475-.077-.418-.216c-.985-.72-.742-2.274.418-2.656L12 4.487Z").toNodes(),
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
