package com.example.c001apk.compose.ui.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.c001apk.compose.ui.main.AppNavigator
import com.example.c001apk.compose.ui.main.AppDestination
import com.example.c001apk.compose.logic.update.AppUpdates
import com.example.c001apk.compose.ui.component.*
import com.example.c001apk.compose.ui.component.settings.*
import com.example.c001apk.compose.util.makeToast
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Update

@HiltViewModel class AppUpdateViewModel @Inject constructor(val updates: AppUpdates) : ViewModel()

private fun Context.openUpdate(url: String, fallback: String = url) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: Exception) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fallback)))
        } catch (_: Exception) {
            makeToast("无法打开发布页，请检查浏览器设置")
        }
    }
}

@Composable
fun AppUpdateHost(navigator: AppNavigator, model: AppUpdateViewModel = hiltViewModel()) {
    val state by model.updates.state.collectAsStateWithLifecycle()
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) { model.updates.coldStart() }
    // Queue an automatic result until the home page is in the foreground; verification activities
    // pause this host.
    if (
        !lifecycle.isAtLeast(Lifecycle.State.RESUMED) ||
            (!state.manual && navigator.currentDestination != AppDestination.Main)
    )
        return
    val release = state.release
    if (release != null)
        MiuixAlertDialog(
            onDismissRequest = { model.updates.dismiss() },
            title = { Text("发现新版本 ${release.version}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (release.publishedAt.isNotEmpty())
                        Text("发布于 ${release.publishedAt.replace('T', ' ').removeSuffix("Z")}")
                    RichPostText(
                        text = release.notes.ifBlank { "该版本未提供更新说明。" },
                        explicit = true,
                        coolapk = false,
                        selectable = true,
                        onOpenLink = { url, _ -> context.openUpdate(url) },
                    )
                    Button(onClick = { context.openUpdate(release.page) }) { Text("查看发布页") }
                }
            },
            dismissButton = {
                MiuixDialogButton(onClick = { model.updates.dismiss() }) { Text("稍后") }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiuixDialogButton(
                        onClick = { model.updates.dismiss(skip = true) },
                        modifier = Modifier.weight(1f),
                        primary = false,
                    ) {
                        Text("跳过")
                    }
                    MiuixDialogButton(
                        onClick = {
                            context.openUpdate(release.download ?: release.page, release.page)
                            model.updates.dismiss()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("更新")
                    }
                }
            },
        )
    else
        state.message?.let { message ->
            MiuixAlertDialog(
                onDismissRequest = { model.updates.dismiss() },
                title = { Text("检查 c001apk 更新") },
                text = { Text(message) },
                confirmButton = {
                    MiuixDialogButton(onClick = { model.updates.dismiss() }) { Text("知道了") }
                },
            )
        }
}

@Composable
fun AppUpdateSettings(model: AppUpdateViewModel = hiltViewModel()) {
    val prefs by model.updates.preferences.collectAsStateWithLifecycle()
    val state by model.updates.state.collectAsStateWithLifecycle()
    SwitchListItem(
        value = prefs.automatic,
        leadingImageVector = MiuixIcons.Update,
        headlineText = "自动检查更新",
        supportingText = "每次启动检查一次正式版本",
        onValueChanged = model.updates::setAutomatic,
    )
    BasicListItem(
        headlineText = if (state.checking) "正在检查更新…" else "检查更新",
        leadingImageVector = MiuixIcons.Update,
        enabled = !state.checking,
        onClick = { model.updates.check() },
    )
}
