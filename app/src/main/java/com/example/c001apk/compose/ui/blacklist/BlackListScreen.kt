package com.example.c001apk.compose.ui.blacklist

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.text.style.TextAlign
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.c001apk.compose.logic.model.StringEntity
import com.example.c001apk.compose.ui.component.*
import com.example.c001apk.compose.util.makeToast
import com.google.gson.Gson
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import top.yukonga.miuix.kmp.basic.Text

/** Created by bggRGjQaUbCoE on 2024/6/16 */
enum class BlackListType { USER, TOPIC }

@Composable
fun BlackListScreen(
    onBackClick: () -> Unit,
    type: String,
    onViewUser: (String) -> Unit,
    onViewTopic: (String) -> Unit,
) {
    val listType = remember(type) { BlackListType.valueOf(type) }
    val viewModel = hiltViewModel<BlackListViewModel, BlackListViewModel.ViewModelFactory>(key = type) { factory ->
        factory.create(listType)
    }
    val entries by viewModel.blackList.collectAsStateWithLifecycle(initialValue = null)
    val blackList = entries.orEmpty()
    val context = LocalContext.current
    var query by rememberSaveable(type) { mutableStateOf("") }
    var menu by remember(type) { mutableStateOf(false) }
    var showClearDialog by remember(type) { mutableStateOf(false) }

    val backupSAFLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/json"),
        ) backup@{ uri ->
            if (uri == null) return@backup
            try {
                val output = context.contentResolver.openOutputStream(uri)
                    ?: throw IOException("Cannot create export file")
                output.use { it.write(Gson().toJson(blackList.map { entry -> entry.data }).toByteArray()) }
                context.makeToast(context.getString(R.string.blocklist_exported))
            } catch (e: Exception) {
                context.makeToast(context.getString(R.string.export_failed))
            }
        }

    fun onBackup() {
        if (blackList.isEmpty()) {
            context.makeToast(context.getString(R.string.blocklist_empty))
        } else {
            try {
                val date =
                    SimpleDateFormat(
                            "yyyy-MM-dd_HH.mm.ss",
                            Locale.getDefault(),
                        )
                        .format(Date())
                backupSAFLauncher.launch("${type.lowercase()}_blacklist_$date.json")
            } catch (e: Exception) {
                context.makeToast(context.getString(R.string.export_failed))
                e.printStackTrace()
            }
        }
    }

    val restoreSAFLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) restore@{ uri ->
            if (uri == null) return@restore
            try {
                val string =
                    context.contentResolver.openInputStream(uri)?.reader().use { it?.readText() }
                        ?: throw IOException("Backup file was damaged")
                val dataList: List<String> =
                    Gson().fromJson(string, Array<String>::class.java).toList()
                val currentList: List<String> = blackList.map { it.data }
                val newList: List<String> =
                    if (currentList.isEmpty()) dataList else dataList.filter { it !in currentList }
                if (newList.isNotEmpty()) viewModel.insertList(newList.map { StringEntity(it) })
                context.makeToast(context.getString(R.string.blocklist_imported))
            } catch (e: Exception) {
                context.makeToast(context.getString(R.string.blocklist_import_failed))
                e.printStackTrace()
            }
        }

    fun onRestore() {
        try {
            restoreSAFLauncher.launch("application/json")
        } catch (e: Exception) {
            context.makeToast(context.getString(R.string.blocklist_import_failed))
            e.printStackTrace()
        }
    }

    MiuixScaffold(
        topBar = {
            MiuixTopBar(
                title = stringResource(if (listType == BlackListType.USER) R.string.settings_user_blocklist else R.string.settings_topic_blocklist),
                navigationIcon = { BackButton(onBackClick) },
                actions = {
                    MiuixMoreMenu(expanded = menu, onExpandedChange = { menu = it }) {
                        MiuixMenuItem(text = { Text(stringResource(R.string.action_import)) }, onClick = { menu = false; onRestore() })
                        MiuixMenuItem(text = { Text(stringResource(R.string.action_export)) }, enabled = blackList.isNotEmpty(),
                            onClick = { menu = false; onBackup() })
                        MiuixMenuItem(text = { Text(stringResource(R.string.action_clear_all)) }, enabled = blackList.isNotEmpty(),
                            onClick = { menu = false; showClearDialog = true })
                    }
                },
            )
        },
    ) { padding ->
        BlackListContent(
            type = listType, entries = entries, query = query, padding = padding,
            onQueryChange = { if (listType != BlackListType.USER || it.isDigitsOnly()) query = it },
            onAdd = {
                val value = query.trim()
                if (value.isNotEmpty()) {
                    viewModel.save(value)
                    query = ""
                }
            },
            onOpen = { if (listType == BlackListType.USER) onViewUser(it) else onViewTopic(it) },
            onRemove = viewModel::delete,
        )
    }
    if (showClearDialog) {
        MiuixAlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(if (listType == BlackListType.USER) R.string.blocklist_clear_users else R.string.blocklist_clear_topics),
                Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            confirmButton = {
                MiuixDialogButton(onClick = { showClearDialog = false; viewModel.clearAll() }, destructive = true) { Text(stringResource(R.string.action_clear)) }
            },
            dismissButton = { MiuixDialogButton(onClick = { showClearDialog = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
    val toastText = viewModel.toastText
    LaunchedEffect(toastText) {
        toastText?.let {
            context.makeToast(it)
            viewModel.reset()
        }
    }
}
