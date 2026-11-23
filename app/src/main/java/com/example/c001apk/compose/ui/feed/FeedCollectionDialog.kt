package com.example.c001apk.compose.ui.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.component.MiuixAlertDialog
import com.example.c001apk.compose.ui.component.MiuixDialogTitle
import com.example.c001apk.compose.ui.component.MiuixDialogButton
import top.yukonga.miuix.kmp.basic.*
import com.example.c001apk.compose.ui.component.LocalDialogClose
import top.yukonga.miuix.kmp.window.WindowDialog
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.preference.CheckboxLocation
import top.yukonga.miuix.kmp.squircle.squircleClip
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun FeedCollectionDialog(collections: FeedCollections) {
    var closing by remember { mutableStateOf(false) }
    var create by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var isOpen by remember { mutableStateOf(false) }
    WindowDialog(
        show = collections.visible && !closing,
        onDismissRequest = { if (!collections.saving && !collections.creating) closing = true },
        onDismissFinished = {
            if (closing) collections.dismiss()
            closing = false
        },
    ) {
        Column(Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
            MiuixDialogTitle(
                title = { Text(stringResource(R.string.collection_save_to)) },
                actions = {
                    IconButton(
                        onClick = {
                            title = ""
                            isOpen = false
                            create = true
                        },
                        enabled = !collections.busy && collections.error == null,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            MiuixIcons.Add,
                            contentDescription = stringResource(R.string.collection_create),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
            )
            Spacer(Modifier.height(12.dp))
            Card(Modifier.weight(1f, fill = false).fillMaxWidth()) {
            LazyColumn(Modifier.fillMaxWidth()) {
                items(collections.items, key = { it.id.orEmpty() }) { collection ->
                    CheckboxPreference(
                        title = collection.title.orEmpty().ifBlank { stringResource(R.string.collection_default) },
                        checked = collection.id in collections.selected,
                        enabled = !collections.busy,
                        checkboxLocation = CheckboxLocation.End,
                        modifier = Modifier.squircleClip(16.dp),
                        onCheckedChange = { checked ->
                            collection.id?.let { collections.toggle(it, checked) }
                        },
                    )
                }
                item {
                    if (collections.loading) {
                        Box(
                            Modifier.fillMaxWidth().padding(12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    } else if (collections.error != null) {
                        Column(
                            Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(collections.error.orEmpty(), color = MiuixTheme.colorScheme.error)
                            Button(
                                onClick = collections::loadMore,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(stringResource(R.string.action_retry))
                            }
                        }
                    } else if (collections.items.isEmpty()) {
                        Text(stringResource(R.string.collection_empty), modifier = Modifier.padding(vertical = 16.dp))
                    } else if (collections.hasMore) {
                        LaunchedEffect(collections.items.lastOrNull()?.id) {
                            collections.loadMore()
                        }
                    }
                }
            }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(
                    text = stringResource(R.string.action_cancel),
                    onClick = { closing = true },
                    enabled = !collections.saving && !collections.creating,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = stringResource(if (collections.saving) R.string.status_saving else R.string.action_save),
                    onClick = collections::save,
                    enabled = !collections.busy && collections.error == null && collections.items.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
    }
    LaunchedEffect(collections.visible) { if (!collections.visible) create = false }
    fun submit(close: (() -> Unit) -> Unit) {
        if (!collections.busy && title.isNotBlank()) {
            collections.create(title, isOpen) { close { create = false } }
        }
    }
    if (create) {
        MiuixAlertDialog(
            onDismissRequest = { create = false },
            dismissEnabled = !collections.creating,
            title = { Text(stringResource(R.string.collection_create)) },
            text = {
                val close = checkNotNull(LocalDialogClose.current)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        label = stringResource(R.string.collection_name),
                        useLabelAsPlaceholder = true,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submit(close) }),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SwitchPreference(
                        title = stringResource(R.string.collection_public),
                        summary = stringResource(R.string.collection_public_summary),
                        checked = isOpen,
                        enabled = !collections.creating,
                        onCheckedChange = { isOpen = it },
                        insideMargin = PaddingValues(0.dp),
                    )
                    collections.createError?.let { Text(it, color = MiuixTheme.colorScheme.error) }
                }
            },
            dismissButton = {
                MiuixDialogButton(onClick = { create = false }, enabled = !collections.creating) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            confirmButton = {
                val close = checkNotNull(LocalDialogClose.current)
                MiuixDialogButton(
                    onClick = { submit(close) },
                    dismissOnClick = false,
                    enabled = !collections.busy && title.isNotBlank(),
                ) {
                    Text(stringResource(if (collections.creating) R.string.status_creating else R.string.action_create))
                }
            },
        )
    }
}
