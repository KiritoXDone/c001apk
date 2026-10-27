package com.example.c001apk.compose.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.example.c001apk.compose.BuildConfig
import com.example.c001apk.compose.R
import com.example.c001apk.compose.constant.Constants.URL_PROJECT
import com.example.c001apk.compose.constant.Constants.URL_SOURCE_CODE
import com.example.c001apk.compose.constant.Constants.URL_SOURCE_CODE_FORK
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.MiuixTopBar as TopAppBar
import com.example.c001apk.compose.ui.component.rememberTopBarScrollState
import com.example.c001apk.compose.ui.component.settings.BasicListItem
import com.example.c001apk.compose.ui.component.settings.PreferenceGroup
import com.example.c001apk.compose.util.openInBrowser
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.All
import top.yukonga.miuix.kmp.icon.extended.Create
import top.yukonga.miuix.kmp.icon.extended.File
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Share

/** Created by bggRGjQaUbCoE on 2024/6/2 */
@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    onLicenseClick: () -> Unit,
) {

    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton { onBackClick() }
                },
                title = "关于",
            )
        },
    ) { paddingValues ->
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberTopBarScrollState()).padding(paddingValues),
        ) {
            Box(
                Modifier.fillMaxWidth().height(125.dp).align(Alignment.CenterHorizontally),
            ) {
                val drawable =
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.mipmap.ic_launcher,
                        context.theme,
                    )
                Image(
                    painter = rememberDrawablePainter(drawable),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            PreferenceGroup {
                BasicListItem(
                    leadingImageVector = MiuixIcons.All,
                    headlineText = stringResource(id = R.string.app_name),
                    supportingText = "第三方酷安客户端",
                )
                BasicListItem(
                    leadingImageVector = MiuixIcons.Info,
                    headlineText = "版本",
                    supportingText = "${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})",
                )
                com.example.c001apk.compose.ui.update.AppUpdateSettings(compact = true)
            }
            PreferenceGroup(title = "开源") {
                BasicListItem(
                    leadingImageVector = MiuixIcons.Create,
                    headlineText = "项目主页",
                    supportingText = "KiritoXDone/c001apk",
                    onClick = { context.openInBrowser(URL_PROJECT) },
                )
                BasicListItem(
                    leadingImageVector = MiuixIcons.Create,
                    headlineText = "原项目地址",
                    supportingText = URL_SOURCE_CODE,
                ) {
                    context.openInBrowser(URL_SOURCE_CODE)
                }
                BasicListItem(
                    leadingImageVector = MiuixIcons.Share,
                    headlineText = "Compose 基线项目",
                    supportingText = URL_SOURCE_CODE_FORK,
                ) {
                    context.openInBrowser(URL_SOURCE_CODE_FORK)
                }
                BasicListItem(
                    leadingImageVector = MiuixIcons.File,
                    headlineText = "开源许可证",
                ) {
                    onLicenseClick()
                }
            }
        }
    }
}
