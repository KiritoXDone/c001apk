// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/regular/files/copy.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteCopyLine: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteCopyLine",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M7.771 16.974c-.895-.209-1.035-1.47-.211-1.872A1 1 0 0 1 8 15h2c.383.022.725.206.898.56.339.693-.169 1.398-.898 1.44H8Zm0-4c-.895-.209-1.035-1.47-.211-1.872A1 1 0 0 1 8 11h4c.46.026.865.305.974.771.15.645-.344 1.193-.974 1.229H8ZM17 18v2c-.026.641-.293 1.229-.819 1.614-.351.257-.751.369-1.181.386H5c-.535-.022-1.03-.202-1.414-.586S3.022 20.535 3 20V8c.022-.535.202-1.03.586-1.414S4.465 6.022 5 6h2V4c.026-.641.293-1.229.819-1.614C8.17 2.129 8.57 2.017 9 2h10c.535.022 1.03.202 1.414.586S20.978 3.465 21 4v12c-.022.535-.202 1.03-.586 1.414s-.879.564-1.414.586Zm0-2h2V4H9v2h6c1.146.046 1.954.854 2 2ZM5 8v12h10V8Z").toNodes(),
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
