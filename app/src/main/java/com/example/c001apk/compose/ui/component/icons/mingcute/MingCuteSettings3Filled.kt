// SPDX-License-Identifier: Apache-2.0
// 来源：https://github.com/mingcute-design/mingcute-icons/blob/9dc7d3ad074fb16477096a6a8109a6467d0e2fe3/assets/svg/core/filled/system/settings_3.svg
package com.example.c001apk.compose.ui.component.icons.mingcute

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

val MingCuteSettings3Filled: ImageVector by lazy {
    ImageVector.Builder(
        name = "MingCuteSettings3Filled",
        defaultWidth = 24f.dp,
        defaultHeight = 24f.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString("M15.181 2.537c.473.083 1.105.412 1.526.637q.61.326 1.169.733c.441.32.874.631 1.021 1.179.091.337.021.662.025 1.005.008.579.149 1.154.439 1.658a3.4 3.4 0 0 0 1.217 1.21c.522.31.978.438 1.206 1.064.11.301.162.918.179 1.106l.037.87c0 .507-.049 1.22-.148 1.72a1.53 1.53 0 0 1-.673 1.004c-.316.204-.662.33-.965.562a3.3 3.3 0 0 0-.853.964 3.4 3.4 0 0 0-.439 1.658c-.004.343.066.668-.025 1.005a1.5 1.5 0 0 1-.26.529c-.183.235-.561.504-.761.65-.738.537-1.571 1.01-2.439 1.301-.811.272-1.126-.076-1.78-.443a3.4 3.4 0 0 0-1.657-.45c-.582 0-1.151.166-1.657.45-.654.367-.969.715-1.78.443-.868-.291-1.701-.764-2.439-1.301-.2-.145-.578-.415-.761-.65a1.5 1.5 0 0 1-.26-.529c-.091-.337-.021-.662-.025-1.005a3.4 3.4 0 0 0-.439-1.658 3.4 3.4 0 0 0-1.217-1.209c-.642-.381-1.109-.486-1.275-1.321C2.049 13.22 2 12.505 2 11.999s.049-1.221.147-1.72c.083-.415.317-.773.674-1.003.317-.204.662-.33.965-.563.343-.263.636-.589.853-.964.29-.504.431-1.079.439-1.658.005-.452-.082-.854.126-1.278.259-.527 1.016-.994 1.491-1.292q.588-.369 1.221-.653c.356-.159.751-.353 1.156-.352.518 0 .843.293 1.271.533a3.35 3.35 0 0 0 2.088.421 3.5 3.5 0 0 0 1.226-.421c.529-.296.867-.627 1.524-.512m-3.588 6.489a3 3 0 0 0-1.714.852c-2.018 2.017-.295 5.475 2.528 5.094a3 3 0 0 0 1.714-.852c2.018-2.017.295-5.475-2.528-5.094").toNodes(),
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
