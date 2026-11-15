package com.example.c001apk.compose.ui.feed

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.logic.export.FeedExportService
import com.example.c001apk.compose.logic.model.FeedArticleContentBean
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.component.MiuixToolbarIconButton
import com.example.c001apk.compose.ui.component.*
import com.example.c001apk.compose.util.makeToast
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Download

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
                error = "内容准备失败，请重试"
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
                    error = "导出已中断，请重新选择格式"
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
                context.makeToast("已保存 ${file.name}")
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                error = "文件保存失败，请检查所选位置后重试"
            } finally {
                busy = false
            }
        }
    }
}

@Composable
fun FeedExportAction(feed: HomeFeedResponse.Data, article: List<FeedArticleContentBean>?) {
    val model = hiltViewModel<FeedExportViewModel>(key = "export-${feed.id}")
    val context = LocalContext.current
    var choose by remember { mutableStateOf(false) }
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
    MiuixToolbarIconButton(onClick = { choose = true }, enabled = !model.busy && !model.awaitingFile) {
        Icon(MiuixIcons.Download, contentDescription = "保存内容")
    }
    if (choose)
        MiuixAlertDialog(
            onDismissRequest = { choose = false },
            title = { Text("保存内容") },
            text = {
                Column {
                    BasicComponent(
                        title = "Markdown",
                        summary = "可编辑的正文，保留在线图片链接",
                        onClick = {
                            choose = false
                            model.prepare(feed, article, FeedExportService.Format.Markdown)
                        },
                    )
                    BasicComponent(
                        title = "离线 HTML",
                        summary = "正文与图片保存在一个文件中",
                        onClick = {
                            choose = false
                            model.prepare(feed, article, FeedExportService.Format.Html)
                        },
                    )
                }
            },
            confirmButton = { MiuixDialogButton(onClick = { choose = false }, primary = false) { Text("取消") } },
        )
    if (model.busy)
        MiuixAlertDialog(
            onDismissRequest = model::cancel,
            title = { Text("正在准备文件") },
            text = { Text("正在整理正文、评论与图片…") },
            confirmButton = { MiuixDialogButton(onClick = model::cancel, primary = false) { Text("取消") } },
        )
    val prepared = model.prepared
    if (prepared != null && prepared.warnings.isNotEmpty() && !model.awaitingFile)
        MiuixAlertDialog(
            onDismissRequest = model::cancel,
            title = { Text("部分内容未能保存") },
            text = { Text(prepared.warnings.joinToString("\n\n") + "\n\n继续将保存已有内容，并在文件中注明缺失部分。") },
            dismissButton = { MiuixDialogButton(onClick = model::cancel) { Text("取消") } },
            confirmButton = { MiuixDialogButton(onClick = ::save) { Text("继续保存") } },
        )
    model.error?.let { message ->
        MiuixAlertDialog(
            onDismissRequest = { model.error = null },
            title = { Text("导出失败") },
            text = { Text(message) },
            confirmButton = { MiuixDialogButton(onClick = { model.error = null }) { Text("知道了") } },
        )
    }
}
