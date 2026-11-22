// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/design/shadow.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteShadowLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteShadowLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M18.809 19.325a10 10 0 0 1-3.791 2.212 10.1 10.1 0 0 1-4.563.345 10 10 0 0 1-3.503-1.247C1.179 17.254.289 9.233 5.192 4.675a10 10 0 0 1 3.79-2.212 10.1 10.1 0 0 1 4.563-.345 10 10 0 0 1 3.503 1.247c5.773 3.381 6.663 11.402 1.761 15.96M18.928 16H12c-.543-.036-1-.428-1-1s.457-.964 1-1h7.743l.189-1H12c-.543-.036-1-.428-1-1s.457-.964 1-1h7.932l-.189-1H12c-.543-.036-1-.428-1-1s.457-.964 1-1h6.928a11 11 0 0 0-.708-1.029L18 7h-6c-.755-.05-1.269-.802-.866-1.5.088-.152.214-.278.366-.366.175-.101.302-.121.5-.134h3.865c-1.197-.643-2.502-.999-3.865-.999-6.681 0-10.43 7.783-6.242 13.002a8.05 8.05 0 0 0 3.266 2.425 8 8 0 0 0 2.976.571c1.363 0 2.668-.356 3.865-.999H12c-.198-.013-.325-.033-.5-.134a1 1 0 0 1-.466-.607c-.176-.656.327-1.217.966-1.259h6l.22.029c.267-.348.479-.654.708-1.029").toNodes(),
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
