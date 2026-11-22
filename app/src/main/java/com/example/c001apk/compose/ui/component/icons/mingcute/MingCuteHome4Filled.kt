// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/filled/buildings/home_4.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteHome4Filled: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteHome4Filled",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M12.538 2.323c.274.077.428.168.662.327l7 5.25c.505.411.775.949.8 1.6V19c-.03.748-.403 1.426-1.081 1.777-.291.151-.594.21-.919.223h-3.9c-.368-.019-.701-.173-.912-.485A1.16 1.16 0 0 1 14 19.9V15c-.045-1.105-.861-2-2-2s-1.955.895-2 2v4.9a1.16 1.16 0 0 1-.188.615c-.211.312-.544.466-.912.485H5a2.1 2.1 0 0 1-.919-.223C3.403 20.426 3.03 19.748 3 19V9.5a2.09 2.09 0 0 1 .8-1.6l7-5.25c.518-.352 1.123-.498 1.738-.327").toNodes(),
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
