package com.example.c001apk.compose.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.widget.doAfterTextChanged
import com.example.c001apk.compose.ui.theme.C001apkViewTheme
import com.example.c001apk.compose.ui.theme.DefaultFontScale
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.squircle.addSquircleRect
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** The editor keeps Android Editable/span/IME support inside a squircle field. */
class MiuixInputLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {
    private var counter: TextView? = null

    init {
        orientation = VERTICAL
        val inset = (12 * resources.displayMetrics.density).toInt()
        setPadding(inset, inset, inset, inset)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val colors = com.example.c001apk.compose.ui.theme.miuixViewColors(context)
        setFieldColors(colors.primary.toArgb(), colors.surfaceContainer.toArgb())
        val editor =
            (0 until childCount).map(::getChildAt).filterIsInstance<EditText>().firstOrNull()
                ?: return
        editor.setTextSize(TypedValue.COMPLEX_UNIT_PX, editor.textSize * DefaultFontScale)
        if (editor.maxLines > 1) {
            counter =
                TextView(context).apply {
                    gravity = Gravity.END
                    textSize = 12f * DefaultFontScale
                }
            addView(counter)
            counter?.setTextColor(colors.primary.toArgb())
            (editor.layoutParams as? LayoutParams)?.let { params ->
                if (params.height == LayoutParams.MATCH_PARENT) {
                    params.height = 0
                    params.weight = 1f
                    editor.layoutParams = params
                }
            }
            editor.doAfterTextChanged { counter?.text = "${it?.length ?: 0} / 500" }
            counter?.text = "${editor.length()} / 500"
        }
    }

    fun setFieldColors(accent: Int, surface: Int) {
        counter?.setTextColor(accent)
        background =
            object : Drawable() {
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = surface }
                val path = Path()

                override fun draw(canvas: Canvas) {
                    path.reset()
                    path.addSquircleRect(
                        bounds.width().toFloat(),
                        bounds.height().toFloat(),
                        16 * resources.displayMetrics.density,
                    )
                    canvas.drawPath(path.asAndroidPath(), paint)
                }

                override fun setAlpha(alpha: Int) {
                    paint.alpha = alpha
                }

                override fun setColorFilter(colorFilter: ColorFilter?) {
                    paint.colorFilter = colorFilter
                }

                @Deprecated("Deprecated in Java")
                override fun getOpacity() = PixelFormat.TRANSLUCENT
            }
    }
}

class MiuixCheckboxView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    AbstractComposeView(context, attrs) {
    var isChecked by mutableStateOf(false)
    var text: CharSequence by mutableStateOf("")
    var compact by mutableStateOf(false)

    @Composable
    override fun Content() {
        C001apkViewTheme {
            Row(
                Modifier.toggleable(
                        value = isChecked,
                        role = Role.Checkbox,
                        onValueChange = {
                            isChecked = it
                            performClick()
                        },
                    )
                    .then(
                        if (compact) Modifier.heightIn(min = 40.dp).padding(horizontal = 6.dp)
                        else Modifier.padding(8.dp),
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(if (compact) 20.dp else 26.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Checkbox(
                        state = androidx.compose.ui.state.ToggleableState(isChecked),
                        onClick = null,
                        modifier =
                            Modifier.graphicsLayer {
                                scaleX = if (compact) 20f / 26f else 1f
                                scaleY = scaleX
                            },
                    )
                }
                Text(
                    text.toString(),
                    modifier = Modifier.padding(start = if (compact) 6.dp else 8.dp),
                    fontSize = if (compact) 12.sp else MiuixTheme.textStyles.main.fontSize,
                )
            }
        }
    }
}
