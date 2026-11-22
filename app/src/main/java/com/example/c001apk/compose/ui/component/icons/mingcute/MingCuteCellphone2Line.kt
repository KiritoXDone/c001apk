// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/devices/cellphone_2.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteCellphone2Line: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteCellphone2Line",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("m12.475 16.064.418.216c.985.72.742 2.274-.418 2.656l-.475.077-.475-.077-.418-.216c-.985-.72-.742-2.274.418-2.656l.475-.077Zm1.284-11.03c.842.226.996 1.396.241 1.832-.175.101-.302.121-.5.134h-3c-.451-.03-.844-.288-.966-.741-.176-.656.327-1.217.966-1.259h3Zm2.726-2.998c1.469.238 2.456 1.466 2.515 2.924v14.08c-.067 1.67-1.29 2.893-2.96 2.96H7.96a3.1 3.1 0 0 1-1.34-.332c-.997-.517-1.575-1.522-1.62-2.628V4.96C5.067 3.29 6.29 2.067 7.96 2h8.08ZM8.04 4c-.622.025-1.015.418-1.04 1.04v13.92c.009.23.057.44.195.628.204.28.509.398.845.412h7.92c.177-.007.339-.032.498-.114.358-.185.526-.537.542-.926V5.04c-.009-.23-.057-.44-.195-.628-.204-.28-.509-.398-.845-.412Z").toNodes(),
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
