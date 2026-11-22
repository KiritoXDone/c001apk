// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/system/lock.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteLockLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteLockLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M13 15.727V17l-.034.259c-.226.842-1.396.996-1.832.241-.101-.175-.121-.302-.134-.5v-1.273c-.895-.569-1.287-1.617-.802-2.609A2.016 2.016 0 0 1 12 11.993a2.02 2.02 0 0 1 1.802 1.125c.485.992.093 2.04-.802 2.609M6 8c.026-.614.085-1.194.269-1.784.911-2.932 3.932-4.71 6.939-4.095a6 6 0 0 1 3.035 1.636C17.432 4.945 17.93 6.347 18 8c.196 0 1.087-.04 1.458.046.9.21 1.49.995 1.542 1.897v10.114c-.063 1.1-.843 1.88-1.943 1.943H4.943c-.445-.026-.846-.153-1.198-.435-.47-.377-.711-.915-.745-1.508V9.943c.052-.902.642-1.687 1.542-1.897C4.913 7.96 5.804 8 6 8m-.994 2.006v9.988h13.988v-9.988ZM8.001 8h7.998c-.018-.361-.009-.454-.082-.807a4 4 0 0 0-1.089-2.021c-1.882-1.881-5.076-1.414-6.345.922-.326.601-.448 1.23-.482 1.906").toNodes(),
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
