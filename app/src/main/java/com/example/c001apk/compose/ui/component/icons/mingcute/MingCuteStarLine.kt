// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/shapes/star.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteStarLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteStarLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("m12 19.093-5.18 2.282-.423.087-.509-.117-.411-.299-.269-.448-.048-.429.57-5.632-3.772-4.221-.213-.375-.046-.521.157-.483.344-.394.393-.179 5.532-1.198 2.849-4.891.291-.319.481-.204h.508l.481.204.291.319 2.849 4.891 5.533 1.198.393.179.343.394.157.483-.046.52-.213.376-3.772 4.221.57 5.632-.048.429-.269.448-.411.299-.509.117-.423-.087Zm7.621-9.069-4.594-.995c-.356-.117-.44-.178-.661-.481L12 4.487 9.634 8.549c-.221.302-.305.363-.661.48l-4.594.995 3.132 3.505c.219.303.251.403.252.777l-.473 4.677 4.301-1.896c.357-.114.461-.114.818 0l4.301 1.896-.473-4.677c.001-.374.033-.473.252-.777Z").toNodes(),
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
