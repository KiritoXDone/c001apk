package com.example.c001apk.compose.ui.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AiSparkleIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "AiSparkle",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(19f, 9f)
            lineToRelative(1.25f, -2.75f)
            lineTo(23f, 5f)
            lineToRelative(-2.75f, -1.25f)
            lineTo(19f, 1f)
            lineToRelative(-1.25f, 2.75f)
            lineTo(15f, 5f)
            lineToRelative(2.75f, 1.25f)
            lineTo(19f, 9f)
            close()
            moveTo(11.5f, 9.5f)
            lineTo(9f, 4f)
            lineTo(6.5f, 9.5f)
            lineTo(1f, 12f)
            lineToRelative(5.5f, 2.5f)
            lineTo(9f, 20f)
            lineToRelative(2.5f, -5.5f)
            lineTo(17f, 12f)
            lineToRelative(-5.5f, -2.5f)
            close()
            moveTo(19f, 15f)
            lineToRelative(-1.25f, 2.75f)
            lineTo(15f, 19f)
            lineToRelative(2.75f, 1.25f)
            lineTo(19f, 23f)
            lineToRelative(1.25f, -2.75f)
            lineTo(23f, 19f)
            lineToRelative(-2.75f, -1.25f)
            lineTo(19f, 15f)
            close()
        }
    }.build()
}
