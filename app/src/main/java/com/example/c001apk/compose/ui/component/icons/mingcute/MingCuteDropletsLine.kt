// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/design/droplets.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteDropletsLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteDropletsLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M10.382 2.214 11 3zc.474-.278.762-.278 1.236 0L11 3l.618-.786A26 26 0 0 1 13.53 3.93c.133.131.418.385.529.584.423.757-.302 1.678-1.136 1.451-.372-.101-.62-.437-.879-.691L11 4.308C8.625 6.39 5.816 9.536 5.144 12.696c-.745 3.51 1.678 6.933 5.278 7.277a6.04 6.04 0 0 0 4.346-1.303q.443-.358.81-.792c.384-.452.625-.975 1.321-.866a1 1 0 0 1 .806.706c.16.544-.194.96-.52 1.356l-.418.471A8.05 8.05 0 0 1 11 22c-5.341.003-9.045-5.015-7.713-10.134.948-3.642 4.198-7.313 7.095-9.652m5.5 3L16.5 6zc.474-.278.762-.278 1.236 0L16.5 6l.618-.786c1.591 1.295 3.3 3.213 3.762 5.254C21.517 13.287 19.409 16 16.5 16s-5.017-2.713-4.38-5.532c.462-2.041 2.171-3.959 3.762-5.254m.618 2.117c-1.42 1.335-3.563 3.882-1.887 5.809.472.543 1.168.86 1.887.86s1.415-.317 1.887-.86c1.676-1.927-.467-4.474-1.887-5.809m.618-.545").toNodes(),
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
