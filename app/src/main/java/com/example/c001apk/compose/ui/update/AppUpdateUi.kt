package com.example.c001apk.compose.ui.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.c001apk.compose.R
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
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Update

@HiltViewModel class AppUpdateViewModel @Inject constructor(val updates: AppUpdates) : ViewModel()

private fun Context.openUpdate(url: String, fallback: String = url) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: Exception) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fallback)))
        } catch (_: Exception) {
            makeToast(getString(R.string.update_cannot_open))
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
    if (release != null) {
        var isMenuExpanded by remember { mutableStateOf(false) }
        val skippedMessage = stringResource(R.string.update_skipped, release.version)
        MiuixAlertDialog(
            onDismissRequest = { model.updates.dismiss() },
            title = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.update_new_version, release.version),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp),
                        textAlign = TextAlign.Center,
                    )
                    Box(Modifier.align(Alignment.CenterEnd)) {
                        IconButton(onClick = { isMenuExpanded = true }) {
                            Icon(
                                imageVector = MiuixIcons.More,
                                contentDescription = stringResource(R.string.update_more),
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        MiuixMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false },
                            alignment = PopupPositionProvider.Align.End,
                        ) {
                            MiuixMenuItem(
                                text = { Text(stringResource(R.string.update_view_page)) },
                                onClick = {
                                    isMenuExpanded = false
                                    context.openUpdate(release.page)
                                },
                            )
                            MiuixMenuItem(
                                text = { Text(stringResource(R.string.update_skip_version)) },
                                onClick = {
                                    isMenuExpanded = false
                                    model.updates.dismiss(skip = true)
                                    context.makeToast(skippedMessage)
                                },
                            )
                        }
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (release.publishedAt.isNotEmpty())
                        Text(
                            stringResource(
                                R.string.update_published_at,
                                release.publishedAt.replace('T', ' ').removeSuffix("Z"),
                            ),
                        )
                    RichPostText(
                        text = release.notes.ifBlank { stringResource(R.string.update_notes_empty) },
                        explicit = true,
                        coolapk = false,
                        selectable = true,
                        onOpenLink = { url, _ -> context.openUpdate(url) },
                    )
                }
            },
            dismissButton = {
                MiuixDialogButton(onClick = { model.updates.dismiss() }) { Text(stringResource(android.R.string.cancel)) }
            },
            confirmButton = {
                MiuixDialogButton(
                    onClick = {
                        context.openUpdate(release.download ?: release.page, release.page)
                        model.updates.dismiss()
                    },
                ) {
                    Text(stringResource(R.string.update_title))
                }
            },
        )
    } else
        state.message?.let { message ->
            MiuixAlertDialog(
                onDismissRequest = { model.updates.dismiss() },
                title = { Text(stringResource(R.string.update_check_title)) },
                text = { Text(message) },
                confirmButton = {
                    MiuixDialogButton(onClick = { model.updates.dismiss() }) { Text(stringResource(R.string.update_got_it)) }
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
        headlineText = stringResource(R.string.update_auto_check),
        supportingText = stringResource(R.string.update_auto_check_summary),
        onValueChanged = model.updates::setAutomatic,
    )
    BasicListItem(
        headlineText =
            if (state.checking) stringResource(R.string.update_checking)
            else stringResource(R.string.update_check),
        leadingImageVector = MiuixIcons.Update,
        enabled = !state.checking,
        onClick = { model.updates.check() },
    )
}
