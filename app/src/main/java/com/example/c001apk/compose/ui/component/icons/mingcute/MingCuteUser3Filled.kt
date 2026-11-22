// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/filled/users/user_3.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteUser3Filled: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteUser3Filled",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M12.79 13.025a12.3 12.3 0 0 1 3.606.779c1.904.726 4.977 2.658 4.571 5.104-.131.79-.664 1.414-1.326 1.83-.949.597-2.11.836-3.2.997-1.156.171-2.327.232-3.494.256-1.793.036-3.611.007-5.388-.256-1.381-.204-3.192-.585-4.079-1.787-1.005-1.362-.278-2.97.769-4.046 2.098-2.155 5.616-3.062 8.541-2.877m-.279-10.999a5 5 0 0 1 2.669 1.116 5 5 0 0 1 1.595 2.371c1.06 3.412-1.737 6.821-5.286 6.461a5 5 0 0 1-2.669-1.116 5 5 0 0 1-1.595-2.371c-1.06-3.412 1.737-6.821 5.286-6.461").toNodes(),
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
