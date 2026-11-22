// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/system/delete_2.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteDelete2Line: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteDelete2Line",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M13.034 10.741c.226-.842 1.396-.996 1.832-.241.101.175.121.302.134.5v5c-.03.451-.288.844-.741.966-.656.176-1.217-.327-1.259-.966v-5Zm-4 0c.226-.842 1.396-.996 1.832-.241.101.175.121.302.134.5v5c-.03.451-.288.844-.741.966-.656.176-1.217-.327-1.259-.966v-5ZM19.936 7l-.816 12.238c-.079.749-.384 1.435-.94 1.955s-1.259.778-2.013.807H7.833c-1.058-.041-2.027-.57-2.563-1.499a3.1 3.1 0 0 1-.39-1.263L4.064 7c-.209-.016-.376-.025-.564-.134-.655-.378-.655-1.354 0-1.732.175-.101.302-.121.5-.134h3.279l.561-1.683A2.025 2.025 0 0 1 9.668 2h4.664a2.03 2.03 0 0 1 1.828 1.317L16.721 5H20c.198.013.325.033.5.134.655.378.655 1.354 0 1.732-.188.109-.355.118-.564.134M6.069 7l.802 12.028c.013.13.025.218.072.337.161.41.543.619.967.635h8.18c.322-.012.615-.121.82-.38.139-.175.196-.373.219-.592L17.931 7Zm3.656-2.995L9.387 5h5.226l-.338-.995Z").toNodes(),
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
