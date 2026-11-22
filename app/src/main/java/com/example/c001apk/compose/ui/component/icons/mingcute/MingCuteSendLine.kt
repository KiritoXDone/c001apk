// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/communication/send.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteSendLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteSendLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M19.49 3.538c.757.161 1.147.927.926 1.647l-5.647 15.611c-.259.604-.896.972-1.547.763a1.32 1.32 0 0 1-.71-.562l-3.487-6.022-6.022-3.487c-.906-.598-.796-1.829.201-2.257l15.611-5.647c.222-.068.446-.095.675-.046M5.399 10.564l4.195 2.428 3.699-3.699c.409-.359 1.01-.404 1.414 0s.359 1.005 0 1.414l-3.699 3.7 2.428 4.194 4.555-12.592Z").toNodes(),
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
