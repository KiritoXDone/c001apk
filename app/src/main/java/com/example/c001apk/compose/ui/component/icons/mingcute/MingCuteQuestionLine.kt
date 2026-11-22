// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/system/question.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteQuestionLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteQuestionLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M11.741 17.966a1 1 0 0 1-.607-.466c-.439-.76.29-1.69 1.127-1.466.254.068.475.238.607.466.439.76-.29 1.69-1.127 1.466m-.734-4.334c.077-.829.598-1.495 1.328-1.873.44-.227.792-.355 1.06-.804.745-1.248-.43-2.778-1.827-2.395-.618.17-1.09.709-1.176 1.343-.038.284-.018.528-.186.779a1.01 1.01 0 0 1-1.584.101c-.459-.525-.195-1.555.066-2.128.738-1.625 2.61-2.532 4.338-2.006 2.257.687 3.336 3.361 2.053 5.387a3.64 3.64 0 0 1-1.359 1.279c-.18.095-.503.183-.646.348-.104.12-.062.156-.074.337-.024.36-.178.68-.5.866a1 1 0 0 1-.759.1c-.619-.166-.787-.765-.734-1.334m.251 8.34c-1.75-.119-3.446-.731-4.895-1.712C1.054 16.668.538 9.075 5.166 4.701a9.8 9.8 0 0 1 3.148-1.999 10.2 10.2 0 0 1 4.428-.674c1.75.119 3.446.731 4.895 1.712 5.309 3.592 5.825 11.185 1.197 15.559a9.8 9.8 0 0 1-3.148 1.999 10.2 10.2 0 0 1-4.428.674m.116-17.947a8 8 0 0 0-4.377 1.733C2.03 9.744 3.51 17.709 9.585 19.629a8.006 8.006 0 0 0 7.418-1.387c4.967-3.985 3.487-11.951-2.588-13.871a8.1 8.1 0 0 0-3.041-.346").toNodes(),
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
