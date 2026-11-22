// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/brands/layers.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteLayersLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteLayersLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M15 5.38c.331-.046.578-.077.914-.021a2.53 2.53 0 0 1 2.048 2.013c.056.302.038.717.038 1.008.331-.046.578-.077.914-.021 1.218.204 2.031 1.217 2.086 2.425v6.615c-.036.917-.427 1.767-1.161 2.336a3.1 3.1 0 0 1-1.212.56l-6.681 1.336c-1.36.208-2.653-.617-2.908-2.003C8.982 19.326 9 18.911 9 18.62c-.331.046-.578.077-.914.021a2.53 2.53 0 0 1-2.048-2.013C5.982 16.326 6 15.911 6 15.62c-.331.046-.578.077-.914.021-1.218-.204-2.031-1.217-2.086-2.425V6.601c.036-.917.427-1.767 1.161-2.336a3.1 3.1 0 0 1 1.212-.559l6.681-1.337c1.36-.208 2.653.617 2.908 2.003.056.302.038.717.038 1.008m2.196 5.201L17 9.6zl-5.354 1.07c-.267.065-.498.187-.658.416-.13.185-.175.389-.184.612v6.446c.011.199.074.375.257.478.124.07.248.068.386.049l6.515-1.303c.267-.065.498-.187.658-.416.13-.185.175-.389.184-.612v-6.446c-.021-.372-.274-.579-.643-.527ZM9 16.58v-3.979c.056-1.446.967-2.557 2.373-2.896L16 8.78v-.905c-.021-.372-.274-.579-.643-.527L8.842 8.651c-.317.077-.586.245-.735.545-.077.154-.1.312-.107.483v6.446c.036.642.525.55 1 .455m4-10.8v-.905c-.021-.372-.274-.579-.643-.527L5.842 5.651c-.317.077-.586.245-.735.545-.077.154-.1.312-.107.483v6.446c.036.642.525.55 1 .455V9.601c.056-1.446.967-2.557 2.373-2.895Z").toNodes(),
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
