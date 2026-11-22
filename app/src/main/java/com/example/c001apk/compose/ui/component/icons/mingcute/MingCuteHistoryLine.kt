// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/system/history.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteHistoryLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteHistoryLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M19.921 10.915c-.778-5.257-6.243-8.395-11.15-6.234a8 8 0 0 0-2.958 2.246A8.05 8.05 0 0 0 4.06 11.01c-.553 4.453 2.722 8.542 7.199 8.956a8.05 8.05 0 0 0 5.079-1.244l.549-.386c.104-.081.534-.493.77-.603.684-.32 1.485.239 1.42.992-.047.542-.551.865-.936 1.169l-.684.487c-1.625 1.072-3.565 1.614-5.505 1.619-5.084.014-9.296-3.885-9.889-8.882a9.8 9.8 0 0 1 .34-3.931c1.5-4.993 6.449-7.992 11.566-6.993a9.7 9.7 0 0 1 3.066 1.166 10.05 10.05 0 0 1 4.96 8.293 10 10 0 0 1-.165 2.193c-.133.724-.331 1.5-1.238 1.487-.308-.004-.594-.158-.802-.377l-2.252-2.587-.096-.131a1.1 1.1 0 0 1 .883-1.686l.321.031Zm-6.921.67 2.707 2.708c.359.409.404 1.01 0 1.414s-1.005.359-1.414 0l-3-3-.159-.207c-.101-.175-.121-.302-.134-.5V7c.03-.451.288-.845.741-.966.656-.176 1.217.327 1.259.966Z").toNodes(),
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
