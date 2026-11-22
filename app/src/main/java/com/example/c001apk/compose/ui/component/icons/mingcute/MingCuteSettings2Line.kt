// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/system/settings_2.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteSettings2Line: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteSettings2Line",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M18 19v1l-.034.259c-.226.842-1.396.996-1.832.241-.23-.398-.134-1.148-.134-1.5H4c-.543-.036-1-.428-1-1s.457-.964 1-1h12v-1l.034-.259c.226-.842 1.396-.996 1.832-.241.23.398.134 1.148.134 1.5h2c.198.013.325.033.5.134.755.436.601 1.606-.241 1.832L20 19ZM8 13c0 .352.096 1.102-.134 1.5-.436.755-1.606.601-1.832-.241L6 14v-1H4c-.198-.013-.325-.033-.5-.134-.755-.436-.601-1.606.241-1.832L4 11h2v-1l.034-.259c.226-.842 1.396-.996 1.832-.241.23.398.134 1.148.134 1.5h12c.543.036 1 .428 1 1s-.457.964-1 1Zm10-6v1l-.034.259c-.226.842-1.396.996-1.832.241C15.904 8.102 16 7.352 16 7H4c-.543-.036-1-.428-1-1s.457-.964 1-1h12c0-.352-.096-1.102.134-1.5.436-.755 1.606-.601 1.832.241L18 4v1h2c.198.013.325.033.5.134.755.436.601 1.606-.241 1.832L20 7Z").toNodes(),
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
