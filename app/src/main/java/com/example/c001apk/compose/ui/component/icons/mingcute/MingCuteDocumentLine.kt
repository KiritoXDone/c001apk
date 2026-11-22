// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/files/document.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteDocumentLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteDocumentLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M8.741 14.966c-.842-.226-.996-1.396-.241-1.832.175-.101.302-.121.5-.134h3c.451.03.844.288.966.741.176.656-.327 1.217-.966 1.259H9Zm0-5c-.978-.262-.978-1.67 0-1.932L9 8h6c.451.03.844.288.966.741.176.656-.327 1.217-.966 1.259H9ZM20 20.057c-.063 1.1-.843 1.88-1.943 1.943H5.943c-.593-.034-1.131-.275-1.508-.745-.282-.352-.409-.753-.435-1.198V3.943c.034-.593.275-1.131.745-1.508.352-.282.753-.409 1.198-.435h12.114c1.1.063 1.88.843 1.943 1.943ZM6.006 4.006v15.988h11.988V4.006Z").toNodes(),
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
