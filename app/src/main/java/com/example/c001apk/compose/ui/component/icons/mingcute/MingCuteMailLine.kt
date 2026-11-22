// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/communication/mail.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteMailLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteMailLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M22 17.04c-.067 1.67-1.29 2.893-2.96 2.96H4.96c-1.67-.067-2.893-1.29-2.96-2.96V6.96a3.1 3.1 0 0 1 .332-1.34C2.849 4.623 3.854 4.045 4.96 4h14.08c1.67.067 2.893 1.29 2.96 2.96ZM4 9.456v7.504c.025.622.418 1.015 1.04 1.04h13.92c.336-.014.641-.132.845-.412.138-.188.186-.398.195-.628V9.456l-6.28 4.536c-1.102.721-2.338.721-3.44 0Zm.004-2.464 7.374 5.325c.41.269.834.269 1.244 0l7.374-5.325c-.051-.603-.428-.968-1.036-.992H5.04c-.608.024-.985.389-1.036.992").toNodes(),
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
