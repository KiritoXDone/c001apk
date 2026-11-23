package com.example.c001apk.compose.ui.feed.reply

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.EditText
import android.widget.LinearLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.c001apk.compose.R
import com.example.c001apk.compose.ui.theme.C001apkViewTheme
import com.example.c001apk.compose.ui.theme.DefaultFontScale
import com.example.c001apk.compose.ui.theme.miuixViewColors
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Close
import top.yukonga.miuix.kmp.icon.extended.Share
import top.yukonga.miuix.kmp.icon.glass.LockOutline
import top.yukonga.miuix.kmp.icon.glass.Send
import top.yukonga.miuix.kmp.squircle.addSquircleRect
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal object ReplyEditorDefaults {
    val CardRadius = 16.dp
    val CardPadding = 16.dp
    val ChipIconSize = 16.dp
    val ChipSpacing = 6.dp
    val ChipRadius = 18.dp
    val ToolHeight = 36.dp
    val SendSize = 40.dp
    const val ThumbnailSize = 72
    const val ThumbnailRadius = 16
}

// 保留 Android Editable、富文本 span 和现有 IME 行为。
class ReplyInputCardLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {
    init {
        orientation = VERTICAL
        val inset = (ReplyEditorDefaults.CardPadding.value * resources.displayMetrics.density).toInt()
        setPadding(inset, inset, inset, inset)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setCardColor(miuixViewColors(context).surfaceContainerHighest.toArgb())
        val editor = (0 until childCount).map(::getChildAt).filterIsInstance<EditText>().first()
        editor.setTextSize(TypedValue.COMPLEX_UNIT_PX, editor.textSize * DefaultFontScale)
    }

    fun setCardColor(cardColor: Int) {
        background = object : Drawable() {
            private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = cardColor }
            private val path = Path()

            override fun draw(canvas: Canvas) {
                path.reset()
                path.addSquircleRect(
                    bounds.width().toFloat(),
                    bounds.height().toFloat(),
                    ReplyEditorDefaults.CardRadius.value * resources.displayMetrics.density,
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

class ReplyVisibilityChipView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    AbstractComposeView(context, attrs) {
    var isChecked by mutableStateOf(false)
    var text: CharSequence by mutableStateOf("")
    var isReply by mutableStateOf(false)

    @Composable
    override fun Content() {
        C001apkViewTheme {
            Surface(
                onClick = {
                    isChecked = !isChecked
                    performClick()
                },
                modifier = Modifier.testTag("reply_visibility").semantics {
                    role = Role.Checkbox
                    toggleableState = ToggleableState(isChecked)
                },
                shape = RoundedCornerShape(ReplyEditorDefaults.ChipRadius),
                color = if (isChecked) MiuixTheme.colorScheme.tertiaryContainer
                    else MiuixTheme.colorScheme.surfaceContainerHighest,
                contentColor = if (isChecked) MiuixTheme.colorScheme.onTertiaryContainer
                    else MiuixTheme.colorScheme.onSurfaceVariantSummary,
            ) {
                Row(
                    modifier = Modifier.heightIn(min = 32.dp).padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ReplyEditorDefaults.ChipSpacing),
                ) {
                    Icon(
                        if (isReply) MiuixIcons.Share else MiuixIcons.Glass.LockOutline,
                        null,
                        Modifier.size(ReplyEditorDefaults.ChipIconSize),
                    )
                    Text(text.toString(), fontSize = 13.sp, maxLines = 1)
                }
            }
        }
    }
}

class ReplyPublishButtonView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    AbstractComposeView(context, attrs) {
    var canPublish by mutableStateOf(false)
    var actionLabel by mutableStateOf("发布动态")

    @Composable
    override fun Content() {
        C001apkViewTheme {
            Button(
                onClick = { performClick() },
                modifier = Modifier.size(ReplyEditorDefaults.SendSize).clip(CircleShape)
                    .testTag("reply_publish"),
                enabled = canPublish,
                colors = ButtonDefaults.buttonColorsPrimary(),
                minWidth = ReplyEditorDefaults.SendSize,
                minHeight = ReplyEditorDefaults.SendSize,
                insideMargin = PaddingValues(0.dp),
                cornerRadius = ReplyEditorDefaults.SendSize / 2,
            ) {
                Icon(
                    MiuixIcons.Glass.Send,
                    actionLabel,
                    Modifier.size(24.dp),
                )
            }
        }
    }
}

class ReplyToolChipView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    AbstractComposeView(context, attrs) {
    private var text by mutableStateOf("")
    private var iconResource by mutableIntStateOf(0)

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ReplyToolChipView)
        iconResource = attributes.getResourceId(R.styleable.ReplyToolChipView_android_src, 0)
        text = attributes.getText(R.styleable.ReplyToolChipView_android_text).toString()
        attributes.recycle()
    }

    @Composable
    override fun Content() {
        C001apkViewTheme {
            Button(
                onClick = { performClick() },
                modifier = Modifier.testTag("reply_tool_$text"),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MiuixTheme.colorScheme.onSurface,
                ),
                cornerRadius = ReplyEditorDefaults.ChipRadius,
                minHeight = ReplyEditorDefaults.ToolHeight,
                insideMargin = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ReplyEditorDefaults.ChipSpacing),
                ) {
                    Icon(painterResource(iconResource), null, Modifier.size(ReplyEditorDefaults.ChipIconSize))
                    Text(text, fontSize = 13.sp, maxLines = 1)
                }
            }
        }
    }
}

class ReplyAttachmentDeleteView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    AbstractComposeView(context, attrs) {
    lateinit var onRemove: () -> Unit

    @Composable
    override fun Content() {
        C001apkViewTheme {
            IconButton(
                onClick = { onRemove() },
                modifier = Modifier.size(24.dp).clip(CircleShape).testTag("reply_remove_attachment"),
                backgroundColor = Color.Black.copy(alpha = 0.55f),
                cornerRadius = 12.dp,
                minWidth = 24.dp,
                minHeight = 24.dp,
            ) {
                Icon(
                    MiuixIcons.Basic.Close,
                    "删除图片",
                    Modifier.size(14.dp),
                    tint = Color.White,
                )
            }
        }
    }
}
