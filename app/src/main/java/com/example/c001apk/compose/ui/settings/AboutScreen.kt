package com.example.c001apk.compose.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.c001apk.compose.BuildConfig
import com.example.c001apk.compose.R
import com.example.c001apk.compose.constant.Constants.URL_PROJECT
import com.example.c001apk.compose.constant.Constants.URL_SOURCE_CODE
import com.example.c001apk.compose.constant.Constants.URL_SOURCE_CODE_FORK
import com.example.c001apk.compose.ui.component.FlowLightBackground
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.MiuixToolbarIconButton
import com.example.c001apk.compose.ui.component.settings.BasicListItem
import com.example.c001apk.compose.ui.component.settings.PreferenceGroup
import com.example.c001apk.compose.util.openInBrowser
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteArrowLeftLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteFolderLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteGlobeLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteInformationLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteTagLine

/** Created by bggRGjQaUbCoE on 2024/6/2 */
@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    onLicenseClick: () -> Unit,
) {
    val context = LocalContext.current
    val colors = MiuixTheme.colorScheme
    val safeTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
    ) { paddingValues ->
        FlowLightBackground(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(144.dp))
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    colorFilter = ColorFilter.tint(colors.primary),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(id = R.string.app_name),
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    fontSize = 14.sp,
                    color = colors.onSurfaceVariantSummary,
                )
                Spacer(Modifier.height(70.dp))
                PreferenceGroup {
                    com.example.c001apk.compose.ui.update.AppUpdateSettings()
                }
                Spacer(Modifier.height(12.dp))
                PreferenceGroup {
                    BasicListItem(
                        leadingImageVector = MingCuteGlobeLine,
                        headlineText = "项目主页",
                        supportingText = "KiritoXDone/c001apk",
                        onClick = { context.openInBrowser(URL_PROJECT) },
                    )
                    BasicListItem(
                        leadingImageVector = MingCuteFolderLine,
                        headlineText = "原项目地址",
                        supportingText = URL_SOURCE_CODE,
                        onClick = { context.openInBrowser(URL_SOURCE_CODE) },
                    )
                    BasicListItem(
                        leadingImageVector = MingCuteTagLine,
                        headlineText = "Compose 基线项目",
                        supportingText = URL_SOURCE_CODE_FORK,
                        onClick = { context.openInBrowser(URL_SOURCE_CODE_FORK) },
                    )
                    BasicListItem(
                        leadingImageVector = MingCuteInformationLine,
                        headlineText = "开源许可证",
                        onClick = onLicenseClick,
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
            MiuixToolbarIconButton(
                onClick = onBackClick,
                modifier = Modifier.padding(start = 16.dp, top = safeTop + 16.dp),
            ) {
                Icon(
                    MingCuteArrowLeftLine,
                    contentDescription = "返回",
                    tint = colors.onSurface,
                )
            }
        }
    }
}
