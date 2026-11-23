package com.example.c001apk.compose.ui.feed

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.settings.AppLanguage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.logic.export.FeedExportService
import com.example.c001apk.compose.logic.model.FeedArticleContentBean
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.component.*
import com.example.c001apk.compose.util.makeToast
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import top.yukonga.miuix.kmp.basic.*

@HiltViewModel
class FeedExportViewModel @Inject constructor(private val exporter: FeedExportService) :
    ViewModel() {
    var busy by mutableStateOf(false)
        private set

    var prepared by mutableStateOf<FeedExportService.Prepared?>(null)
        private set

    var error by mutableStateOf<String?>(null)
    var awaitingFile by mutableStateOf(false)
    private var job: Job? = null

    fun prepare(
        feed: HomeFeedResponse.Data,
        article: List<FeedArticleContentBean>?,
        format: FeedExportService.Format,
    ) {
        if (busy) return
        busy = true
        prepared = null
        error = null
        job = viewModelScope.launch {
            try {
                prepared = exporter.prepare(feed, article, format)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                error = AppLanguage.getString(R.string.export_prepare_failed)
            } finally {
                busy = false
            }
        }
    }

    fun cancel() {
        job?.cancel()
        prepared = null
        busy = false
        awaitingFile = false
    }

    fun save(context: Context, uri: Uri?) {
        awaitingFile = false
        if (uri == null) {
            prepared = null
            return
        }
        val file =
            prepared
                ?: run {
                    error = AppLanguage.getString(R.string.export_interrupted)
                    return
                }
        prepared = null
        busy = true
        job = viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    context.contentResolver
                        .openOutputStream(uri, "wt")
                        ?.bufferedWriter(Charsets.UTF_8)
                        ?.use { it.write(file.content) } ?: error("Cannot open document")
                }
                context.makeToast(context.getString(R.string.export_saved, file.name))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                error = AppLanguage.getString(R.string.export_save_failed)
            } finally {
                busy = false
            }
        }
    }
}

@Composable
internal fun FeedExportDialogs(
    feed: HomeFeedResponse.Data,
    article: List<FeedArticleContentBean>?,
    model: FeedExportViewModel,
    choose: Boolean,
    onChooseChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val markdown =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/markdown")) {
            model.save(context, it)
        }
    val html =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/html")) {
            model.save(context, it)
        }
    fun save() {
        model.prepared?.let {
            model.awaitingFile = true
            if (it.format == FeedExportService.Format.Html) html.launch(it.name)
            else markdown.launch(it.name)
        }
    }
    LaunchedEffect(model.prepared) {
        if (model.prepared?.warnings?.isEmpty() == true && !model.awaitingFile) save()
    }
    if (choose) {
        FeedExportFormatDialog(
            onDismiss = { onChooseChange(false) },
            onSelect = { format ->
                onChooseChange(false)
                model.prepare(feed, article, format)
            },
        )
    }
    if (model.busy)
        MiuixAlertDialog(
            onDismissRequest = model::cancel,
            title = { Text(stringResource(R.string.export_preparing)) },
            text = { Text(stringResource(R.string.export_preparing_summary)) },
            confirmButton = { MiuixDialogButton(onClick = model::cancel, primary = false) { Text(stringResource(R.string.action_cancel)) } },
        )
    val prepared = model.prepared
    if (prepared != null && prepared.warnings.isNotEmpty() && !model.awaitingFile)
        MiuixAlertDialog(
            onDismissRequest = model::cancel,
            title = { Text(stringResource(R.string.export_partial)) },
            text = { Text(stringResource(R.string.export_partial_summary, prepared.warnings.joinToString("\n\n"))) },
            dismissButton = { MiuixDialogButton(onClick = model::cancel) { Text(stringResource(R.string.action_cancel)) } },
            confirmButton = { MiuixDialogButton(onClick = ::save) { Text(stringResource(R.string.export_continue)) } },
        )
    model.error?.let { message ->
        MiuixAlertDialog(
            onDismissRequest = { model.error = null },
            title = { Text(stringResource(R.string.export_failed)) },
            text = { Text(message) },
            confirmButton = { MiuixDialogButton(onClick = { model.error = null }, primary = false) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}

@Composable
internal fun FeedExportMenuItem(enabled: Boolean, onDismissMenu: () -> Unit, onChoose: () -> Unit) {
    MiuixMenuItem(
        text = { Text(stringResource(R.string.export_content)) },
        enabled = enabled,
        onClick = {
            onDismissMenu()
            onChoose()
        },
    )
}

@Composable
internal fun FeedExportFormatDialog(onDismiss: () -> Unit, onSelect: (FeedExportService.Format) -> Unit) {
    MiuixAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.export_content)) },
        text = {
            val close = checkNotNull(LocalDialogClose.current)
            Column {
                BasicComponent(
                    title = "Markdown",
                    summary = stringResource(R.string.export_markdown_summary),
                    onClick = { close { onSelect(FeedExportService.Format.Markdown) } },
                )
                BasicComponent(
                    title = stringResource(R.string.export_html),
                    summary = stringResource(R.string.export_html_summary),
                    onClick = { close { onSelect(FeedExportService.Format.Html) } },
                )
            }
        },
        confirmButton = { MiuixDialogButton(onClick = onDismiss, primary = false) { Text(stringResource(R.string.action_cancel)) } },
    )
}
