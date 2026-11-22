// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/communication/message_1.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteMessage1Line: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteMessage1Line",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M15.659 10.013C17.538 10.239 17.356 13 15.5 13c-1.506 0-2.069-2.044-.755-2.792.278-.158.596-.234.914-.195m-7 0c1.411.17 1.853 2.063.596 2.779a1.5 1.5 0 0 1-.914.195c-1.411-.17-1.853-2.063-.596-2.779.278-.158.596-.234.914-.195m10.421-7.01C20.747 3.115 21.949 4.316 22 6v10c-.015.505-.122 1.003-.374 1.446C21.08 18.411 20.1 18.967 19 19h-3.648c-.136.028.038-.025-.09.027l-2.569 1.713a1.3 1.3 0 0 1-1.386 0l-2.569-1.713-.042-.022L8.648 19H5c-.817-.025-1.609-.328-2.173-.935C2.294 17.493 2.023 16.778 2 16V6c.051-1.697 1.303-2.949 3-3h14ZM12 18.798l2.193-1.462.438-.217c.217-.078.442-.111.672-.119h3.657c.622-.025 1.015-.418 1.04-1.04V6.04c-.016-.389-.184-.741-.542-.926A1.14 1.14 0 0 0 18.96 5H5.04c-.622.025-1.015.418-1.04 1.04v9.92c.007.177.032.339.114.498.185.358.537.526.926.542h3.657c.403.014.767.124 1.11.336Z").toNodes(),
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
