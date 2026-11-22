// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/users/user_follow.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteUserFollowLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteUserFollowLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M18.973 18.593C18.767 17.069 17.502 16.05 16 16H8a3.2 3.2 0 0 0-1.168.236c-.474.2-.892.524-1.207.931C4.719 18.339 5.081 19.591 5 21a1 1 0 0 1-.102.44c-.402.824-1.663.684-1.872-.211L3 21v-2c.072-2.838 2.162-4.928 5-5h8c1.002.025 1.959.289 2.796.854 1.392.941 2.162 2.484 2.204 4.146v2a1 1 0 0 1-.102.44c-.473.967-1.838.599-1.898-.44-.046-.801.08-1.612-.027-2.407m.321-7.387 2.125-2.12c.485-.423 1.238-.379 1.577.215.231.406.131.864-.164 1.201L20 13.328c-.432.377-.982.376-1.413-.001l-1.415-1.414c-.133-.152-.208-.256-.26-.454a1.01 1.01 0 0 1 .968-1.254c.278 0 .502.114.707.293Zm-2.396-3.198a5 5 0 0 1-1.362 2.528 5 5 0 0 1-2.528 1.362c-3.502.716-6.622-2.404-5.906-5.906a5 5 0 0 1 1.362-2.528 5 5 0 0 1 2.528-1.362c3.502-.716 6.622 2.404 5.906 5.906m-7.791-.21a3 3 0 0 0 1.06 1.577c2.262 1.748 5.482-.419 4.726-3.173a3 3 0 0 0-1.06-1.577c-2.262-1.748-5.482.419-4.726 3.173").toNodes(),
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
