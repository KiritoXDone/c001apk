package com.example.c001apk.compose.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import com.example.c001apk.compose.BuildConfig
import com.example.c001apk.compose.R
import com.example.c001apk.compose.constant.Constants.URL_PROJECT
import com.example.c001apk.compose.constant.Constants.URL_SOURCE_CODE
import com.example.c001apk.compose.constant.Constants.URL_SOURCE_CODE_FORK
import com.example.c001apk.compose.ui.component.FlowLightBackground
import com.example.c001apk.compose.ui.component.settings.BasicListItem
import com.example.c001apk.compose.ui.component.settings.PreferenceGroup
import com.example.c001apk.compose.ui.component.settings.PreferenceSpacing
import com.example.c001apk.compose.util.openInBrowser
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.WorldClock
import top.yukonga.miuix.kmp.icon.glass.FolderOutline
import top.yukonga.miuix.kmp.icon.glass.HtmlViewer
import top.yukonga.miuix.kmp.icon.glass.OpenWithOtherApp
import top.yukonga.miuix.kmp.icon.glass.Tags
import top.yukonga.miuix.kmp.material.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Created by bggRGjQaUbCoE on 2024/6/2 */
@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    onLicenseClick: () -> Unit,
) {
    val context = LocalContext.current
    val colors = MiuixTheme.colorScheme
    val isDarkTheme = colors.surface.luminance() < 0.5f
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val backdrop = if (isRuntimeShaderSupported()) rememberLayerBackdrop() else null
    val cardModifier =
        if (backdrop != null) {
            Modifier.squircleClip(16.dp).textureBlur(
                backdrop = backdrop,
                shape = RoundedCornerShape(16.dp),
                blurRadius = 25f,
                colors =
                    BlurDefaults.blurColors(
                        blendColors =
                            listOf(
                                BlendColorEntry(color = colors.surface.copy(alpha = 0.8f)),
                            ),
                    ),
            )
        } else {
            Modifier
        }
    val cardColor = if (backdrop != null) Color.Transparent else colors.surfaceContainer

    FlowLightBackground(
        modifier = Modifier.fillMaxSize(),
        isAnimated = lifecycleState == Lifecycle.State.RESUMED,
        backgroundModifier = backdrop?.let { Modifier.layerBackdrop(it) } ?: Modifier,
    ) {
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                        ),
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(144.dp))
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier =
                    Modifier.size(100.dp).aboutForeground(backdrop = backdrop, isDarkTheme = isDarkTheme),
                colorFilter = ColorFilter.tint(colors.onBackground),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(id = R.string.app_name),
                modifier =
                    Modifier.aboutForeground(
                        backdrop = backdrop,
                        isDarkTheme = isDarkTheme,
                        shape = RoundedCornerShape(16.dp),
                    ),
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = "v${BuildConfig.VERSION_NAME}",
                fontSize = 14.sp,
                color = colors.onSurfaceVariantSummary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { context.openInBrowser(URL_PROJECT) },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(MiuixIcons.Glass.OpenWithOtherApp, stringResource(R.string.settings_about_project_home), Modifier.size(24.dp))
                }
                IconButton(
                    onClick = onLicenseClick,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(MiuixIcons.Glass.HtmlViewer, stringResource(R.string.settings_about_license), Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.height(70.dp))
            PreferenceSpacing(horizontal = 0.dp, spacing = 12.dp) {
                PreferenceGroup(containerColor = cardColor, cardModifier = cardModifier) {
                    com.example.c001apk.compose.ui.update.AppUpdateSettings()
                }
                PreferenceGroup(containerColor = cardColor, cardModifier = cardModifier) {
                    BasicListItem(
                        leadingImageVector = MiuixIcons.WorldClock,
                        headlineText = stringResource(R.string.settings_about_project_home),
                        supportingText = "KiritoXDone/c001apk",
                        onClick = { context.openInBrowser(URL_PROJECT) },
                    )
                    BasicListItem(
                        leadingImageVector = MiuixIcons.Glass.FolderOutline,
                        headlineText = stringResource(R.string.settings_about_source),
                        supportingText = URL_SOURCE_CODE,
                        onClick = { context.openInBrowser(URL_SOURCE_CODE) },
                    )
                    BasicListItem(
                        leadingImageVector = MiuixIcons.Glass.Tags,
                        headlineText = stringResource(R.string.settings_about_baseline),
                        supportingText = URL_SOURCE_CODE_FORK,
                        onClick = { context.openInBrowser(URL_SOURCE_CODE_FORK) },
                    )
                    BasicListItem(
                        leadingImageVector = MiuixIcons.Info,
                        headlineText = stringResource(R.string.settings_about_license),
                        onClick = onLicenseClick,
                    )
                }
            }
        }
        IconButton(
            onClick = onBackClick,
            modifier =
                Modifier.windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Start,
                    ),
                )
                    .padding(16.dp)
                    .size(40.dp),
        ) {
            Icon(
                MiuixIcons.Back,
                contentDescription = stringResource(R.string.action_back),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

private fun Modifier.aboutForeground(
    backdrop: LayerBackdrop?,
    isDarkTheme: Boolean,
    shape: Shape = RectangleShape,
): Modifier {
    if (backdrop == null) return this
    return textureBlur(
        backdrop = backdrop,
        shape = shape,
        blurRadius = 150f,
        colors = BlurColors(blendColors = if (isDarkTheme) LogoDark else LogoLight),
        contentBlendMode = BlendMode.DstIn,
    )
}

private val LogoLight =
    listOf(
        BlendColorEntry(Color(0xCC4A4A4A), BlurBlendMode.ColorBurn),
        BlendColorEntry(Color(0xFF4F4F4F), BlurBlendMode.LinearLight),
        BlendColorEntry(Color(0xFF1AF200), BlurBlendMode.Lab),
    )

private val LogoDark =
    listOf(
        BlendColorEntry(Color(0xE6A1A1A1), BlurBlendMode.ColorDodge),
        BlendColorEntry(Color(0x4DE6E6E6), BlurBlendMode.LinearLight),
        BlendColorEntry(Color(0xFF1AF500), BlurBlendMode.Lab),
    )
