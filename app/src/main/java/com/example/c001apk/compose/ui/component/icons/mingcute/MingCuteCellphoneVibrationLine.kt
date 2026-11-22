// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/devices/cellphone_vibration.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteCellphoneVibrationLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteCellphoneVibrationLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M8.308 15.333a1 1 0 0 1-.104-.759c.224-.854 1.4-.992 1.838-.243.133.228.17.503.103.758-.224.855-1.4.993-1.837.244m1.061 6.387c-.377-.099-.572-.231-.857-.481L2.76 15.488c-.727-.832-.727-1.903 0-2.734l9.994-9.994c.523-.457 1.191-.662 1.877-.482.378.099.572.232.857.482l5.752 5.751c.727.832.727 1.902 0 2.734l-9.994 9.994c-.523.457-1.191.661-1.877.481m-5.136-7.599 5.646 5.645 9.888-9.888-5.646-5.646Zm16.511 1.155c.778.209.947 1.104.448 1.673l-4.242 4.243c-.41.359-1.01.404-1.414 0-.405-.404-.359-1.005 0-1.415l4.242-4.242c.271-.238.607-.355.966-.259M8.016 2.548c.779.209.948 1.104.448 1.673L4.222 8.464c-.41.359-1.01.404-1.414 0-.405-.404-.359-1.005 0-1.414L7.05 2.807c.272-.238.607-.355.966-.259").toNodes(),
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
