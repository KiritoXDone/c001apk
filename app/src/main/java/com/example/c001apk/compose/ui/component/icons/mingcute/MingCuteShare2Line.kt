// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/system/share_2.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteShare2Line: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteShare2Line",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("m16.139 8.08-4.407 2.31c.345 1.168.346 2.051.001 3.219l4.406 2.31c1.928-1.702 4.963-.932 5.715 1.57a3.512 3.512 0 0 1-.416 2.919 3.53 3.53 0 0 1-1.927 1.446c-2.033.611-4.166-.737-4.476-2.838-.07-.473-.026-.915.077-1.377l-4.395-2.304a5 5 0 0 1-2.571 1.535 5.1 5.1 0 0 1-2.292 0 5.03 5.03 0 0 1-2.391-1.333C.712 12.785 2.065 8.018 5.854 7.13a5.04 5.04 0 0 1 2.832.16 5 5 0 0 1 2.031 1.374l4.395-2.304c-.103-.461-.147-.903-.077-1.376.25-1.695 1.753-2.987 3.465-2.987.509 0 1.015.112 1.477.326a3.53 3.53 0 0 1 1.461 1.269 3.5 3.5 0 0 1 .565 1.908 3.5 3.5 0 0 1-.565 1.908c-1.194 1.837-3.683 2.1-5.299.672m2.138 8.939c-.35.052-.675.232-.907.499-.909 1.044-.012 2.664 1.353 2.463.35-.052.675-.232.907-.499.909-1.044.012-2.664-1.353-2.463M6.649 9.022a3 3 0 0 0-1.524.638c-2.339 1.876-.751 5.663 2.226 5.318a3 3 0 0 0 1.524-.638c2.339-1.876.751-5.663-2.226-5.318m11.628-5.003c-.35.052-.675.232-.907.499-.909 1.044-.012 2.664 1.353 2.463.35-.052.675-.232.907-.499.909-1.044.012-2.664-1.353-2.463").toNodes(),
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
