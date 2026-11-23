// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/editing/translate.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteTranslateLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteTranslateLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M18 19v1l-.034.259c-.226.842-1.396.996-1.832.241-.23-.398-.134-1.148-.134-1.5h-1.5a1.6 1.6 0 0 1-.886-.289c-.39-.286-.595-.736-.614-1.211v-3c.028-.693.484-1.32 1.183-1.462L14.5 13H16c0-.352-.096-1.102.134-1.5.436-.755 1.606-.601 1.832.241L18 12v1h1.5l.317.038c.699.142 1.155.769 1.183 1.462v3c-.019.475-.224.925-.614 1.211A1.6 1.6 0 0 1 19.5 19Zm-3-4v2h1v-2Zm3 0v2h1v-2Zm-7.5 3.134c.755.436.601 1.606-.241 1.832L10 20H7c-1.697-.051-2.949-1.303-3-3v-1l.034-.259c.196-.73 1.139-.983 1.673-.448.72.719-.207 2.054.835 2.593.159.082.321.107.498.114H10c.198.013.325.033.5.134M5 7h4c.543.036 1 .428 1 1s-.457.964-1 1H5v2h5c.639.042 1.142.603.966 1.259-.122.453-.515.711-.966.741H4c-.36-.024-.68-.178-.866-.5-.101-.175-.121-.302-.134-.5V4c.013-.198.033-.325.134-.5.186-.322.506-.476.866-.5h5.5c.451.03.844.288.966.741.176.656-.327 1.217-.966 1.259H5Zm12.984-.164c-.085-.515-.527-.816-1.024-.836H14c-.755-.05-1.269-.802-.866-1.5.186-.322.506-.476.866-.5h3c1.697.051 2.949 1.303 3 3v2c-.013.198-.033.325-.134.5-.436.755-1.606.601-1.832-.241L18 9V7.04Z").toNodes(),
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
