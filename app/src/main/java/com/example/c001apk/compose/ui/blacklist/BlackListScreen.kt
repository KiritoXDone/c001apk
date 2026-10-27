package com.example.c001apk.compose.ui.blacklist

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.c001apk.compose.constant.Constants.EMPTY_STRING
import com.example.c001apk.compose.logic.model.StringEntity
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.MiuixActionButton as TextButton
import com.example.c001apk.compose.ui.component.MiuixAlertDialog as AlertDialog
import com.example.c001apk.compose.ui.component.MiuixHeader as TopAppBar
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.cards.LoadingCard
import com.example.c001apk.compose.ui.component.cards.SearchHistoryCard
import com.example.c001apk.compose.ui.component.rememberTopBarScrollState
import com.example.c001apk.compose.util.makeToast
import com.google.gson.Gson
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Clear
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Download
import top.yukonga.miuix.kmp.icon.extended.UploadCloud
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Created by bggRGjQaUbCoE on 2024/6/16 */
enum class BlackListType {
    USER,
    TOPIC,
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BlackListScreen(
    onBackClick: () -> Unit,
    type: String,
    onViewUser: (String) -> Unit,
    onViewTopic: (String) -> Unit,
) {

    val viewModel =
        hiltViewModel<BlackListViewModel, BlackListViewModel.ViewModelFactory>(key = type) { factory
            ->
            factory.create(BlackListType.valueOf(type))
        }
    val blackList by viewModel.blackList.collectAsStateWithLifecycle(initialValue = emptyList())

    val context = LocalContext.current
    val focusRequest = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        try {
            focusRequest.requestFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var textInput by remember { mutableStateOf(TextFieldValue(text = EMPTY_STRING)) }
    val textStyle = MiuixTheme.textStyles.main
    var showClearDialog by remember { mutableStateOf(false) }

    val backupSAFLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/json"),
        ) backup@{ uri ->
            if (uri == null) return@backup
            context.contentResolver.openOutputStream(uri).use { output ->
                if (output == null) context.makeToast("导出失败")
                else output.write(Gson().toJson(blackList.map { it.data }).toByteArray())
            }
            context.makeToast("导出成功")
        }

    fun onBackup() {
        if (blackList.isEmpty()) {
            context.makeToast("黑名单为空")
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
                context.makeToast("导出失败")
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
                context.makeToast("导入成功")
            } catch (e: Exception) {
                context.makeToast(e.message ?: "导入失败")
                e.printStackTrace()
            }
        }

    fun onRestore() {
        try {
            restoreSAFLauncher.launch("application/json")
        } catch (e: Exception) {
            context.makeToast("导入失败")
            e.printStackTrace()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton { onBackClick() }
                },
                title = {
                    TextField(
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequest),
                        singleLine = true,
                        value = textInput,
                        onValueChange = {
                            when (type) {
                                BlackListType.USER.name -> {
                                    if (it.text.isDigitsOnly()) textInput = it
                                }

                                else -> {
                                    textInput = it
                                }
                            }
                        },
                        textStyle = textStyle.copy(fontSize = 18.sp),
                        label =
                            when (type) {
                                BlackListType.USER.name -> "UID"
                                BlackListType.TOPIC.name -> "话题"
                                else -> EMPTY_STRING
                            },
                        useLabelAsPlaceholder = true,
                        trailingIcon = {
                            AnimatedVisibility(
                                visible = textInput.text.isNotEmpty(),
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                IconButton(
                                    onClick = {
                                        textInput = TextFieldValue(EMPTY_STRING)
                                    },
                                ) {
                                    Icon(
                                        imageVector = MiuixIcons.Clear,
                                        contentDescription = null,
                                    )
                                }
                            }
                        },
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    when (type) {
                                        BlackListType.USER.name -> KeyboardType.Number
                                        BlackListType.TOPIC.name -> KeyboardType.Text
                                        else -> KeyboardType.Text
                                    },
                                imeAction = ImeAction.Done,
                            ),
                        keyboardActions =
                            KeyboardActions(
                                onDone = {
                                    if (textInput.text.trim().isNotEmpty()) {
                                        viewModel.save(textInput.text)
                                        textInput = TextFieldValue(EMPTY_STRING)
                                    }
                                },
                            ),
                    )
                },
                actions = {
                    Row {
                        IconButton(
                            onClick = {
                                onBackup()
                            },
                        ) {
                            Icon(imageVector = MiuixIcons.UploadCloud, contentDescription = null)
                        }
                        IconButton(
                            onClick = {
                                onRestore()
                            },
                        ) {
                            Icon(imageVector = MiuixIcons.Download, contentDescription = null)
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize().verticalScroll(rememberTopBarScrollState()).padding(paddingValues),
        ) {
            HorizontalDivider()

            androidx.compose.animation.AnimatedVisibility(
                visible = blackList.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text =
                            when (type) {
                                BlackListType.USER.name -> "用户"
                                BlackListType.TOPIC.name -> "话题"
                                else -> type
                            },
                        modifier = Modifier.weight(1f).padding(start = 10.dp),
                        style =
                            MiuixTheme.textStyles.headline2.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                    )

                    IconButton(
                        onClick = {
                            showClearDialog = true
                        },
                    ) {
                        Icon(imageVector = MiuixIcons.Delete, contentDescription = null)
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(horizontal = 10.dp),
            ) {
                blackList.forEach {
                    SearchHistoryCard(
                        data = it.data,
                        onSearch = {
                            when (type) {
                                BlackListType.USER.name -> onViewUser(it.data)
                                BlackListType.TOPIC.name -> onViewTopic(it.data)
                                else -> {}
                            }
                        },
                        onDelete = {
                            viewModel.delete(it.data)
                        },
                    )
                }
            }
        }

        if (blackList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                LoadingCard(state = LoadingState.Empty)
            }
        }
    }

    when {
        showClearDialog -> {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showClearDialog = false
                            viewModel.clearAll()
                        },
                    ) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showClearDialog = false
                        },
                    ) {
                        Text(text = stringResource(id = android.R.string.cancel))
                    }
                },
                title = { Text("确定清除全部黑名单？") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    viewModel.toastText?.let {
        viewModel.reset()
        context.makeToast(it)
    }
}
