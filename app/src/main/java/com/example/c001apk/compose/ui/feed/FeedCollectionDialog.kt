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
import com.example.c001apk.compose.ui.component.MiuixDialogButton
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.glass.GlassDialog
import top.yukonga.miuix.kmp.glass.GlassDialogButton
import top.yukonga.miuix.kmp.glass.GlassDialogButtonRole
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun FeedCollectionDialog(collections: FeedCollections) {
    if (!collections.visible) return
    var create by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var isOpen by remember { mutableStateOf(false) }
    GlassDialog(
        show = true,
        onDismissRequest = collections::dismiss,
        backdrop = null,
        stroke = null,
        containerColor = MiuixTheme.colorScheme.surface.copy(alpha = 1f),
    ) {
        Column(Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.collection_save_to), style = MiuixTheme.textStyles.title3, modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {
                        title = ""
                        isOpen = false
                        create = true
                    },
                    enabled = !collections.busy && collections.error == null,
                    minHeight = 35.dp,
                    minWidth = 35.dp,
                ) {
                    Icon(
                        MiuixIcons.Add,
                        contentDescription = stringResource(R.string.collection_create),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            LazyColumn(Modifier.weight(1f, fill = false).fillMaxWidth()) {
                items(collections.items, key = { it.id.orEmpty() }) { collection ->
                    CheckboxPreference(
                        title = collection.title.orEmpty().ifBlank { stringResource(R.string.collection_default) },
                        checked = collection.id in collections.selected,
                        enabled = !collections.busy,
                        insideMargin = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
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
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassDialogButton(
                    onClick = collections::dismiss,
                    enabled = !collections.saving && !collections.creating,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.action_cancel), style = MiuixTheme.textStyles.button)
                }
                GlassDialogButton(
                    onClick = collections::save,
                    enabled =
                        !collections.busy &&
                            collections.error == null &&
                            collections.items.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    role = GlassDialogButtonRole.Primary,
                ) {
                    Text(stringResource(if (collections.saving) R.string.status_saving else R.string.action_save), style = MiuixTheme.textStyles.button)
                }
            }
        }
    }
    if (create) {
        MiuixAlertDialog(
            onDismissRequest = { if (!collections.creating) create = false },
            title = { Text(stringResource(R.string.collection_create)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        label = stringResource(R.string.collection_name),
                        useLabelAsPlaceholder = true,
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
                MiuixDialogButton(
                    onClick = { collections.create(title, isOpen) { create = false } },
                    enabled = !collections.busy && title.isNotBlank(),
                ) {
                    Text(stringResource(if (collections.creating) R.string.status_creating else R.string.action_create))
                }
            },
        )
    }
}
