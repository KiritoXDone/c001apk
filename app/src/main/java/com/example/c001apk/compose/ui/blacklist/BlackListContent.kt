package com.example.c001apk.compose.ui.blacklist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.model.StringEntity
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.component.GroupedListItem
import com.example.c001apk.compose.ui.component.TopBarScrollEffect
import com.example.c001apk.compose.ui.component.cards.LoadingCard
import com.example.c001apk.compose.ui.component.settings.PreferenceGroup
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Remove
import top.yukonga.miuix.kmp.icon.glass.ContactsCircle
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun BlackListContent(
    type: BlackListType,
    entries: List<StringEntity>?,
    query: String,
    padding: PaddingValues,
    onQueryChange: (String) -> Unit,
    onAdd: () -> Unit,
    onOpen: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    val users = type == BlackListType.USER
    val listState = rememberLazyListState()
    TopBarScrollEffect { listState.animateScrollToItem(0) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().consumeWindowInsets(padding).imePadding(),
        state = listState,
        contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 24.dp),
    ) {
        item(key = "add") {
            PreferenceGroup(title = stringResource(if (users) R.string.blocklist_add_user else R.string.blocklist_add_topic)) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.weight(1f),
                        label = stringResource(if (users) R.string.blocklist_user_hint else R.string.blocklist_topic_hint),
                        useLabelAsPlaceholder = true,
                        singleLine = true,
                        insideMargin = DpSize(12.dp, 12.dp),
                        textStyle = MiuixTheme.textStyles.body1,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (users) KeyboardType.Number else KeyboardType.Text,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { onAdd() }),
                    )
                    Button(onClick = onAdd, enabled = query.isNotBlank(),
                        colors = ButtonDefaults.buttonColorsPrimary(),
                        minWidth = 64.dp, minHeight = 48.dp,
                        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(stringResource(R.string.action_add))
                    }
                }
            }
            Text(stringResource(R.string.blocklist_local_notice), Modifier.padding(horizontal = 28.dp).padding(bottom = 16.dp),
                style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            if (entries != null) SmallTitle(stringResource(if (users) R.string.blocklist_users_count else R.string.blocklist_topics_count, entries.size))
        }
        if (entries == null) {
            item(key = "loading") { LoadingCard(state = LoadingState.Loading) }
        } else if (entries.isEmpty()) {
            item(key = "empty") {
                PreferenceGroup {
                    BasicComponent(
                        title = stringResource(if (users) R.string.blocklist_users_empty else R.string.blocklist_topics_empty),
                        summary = stringResource(R.string.blocklist_add_hint),
                    )
                }
            }
        } else {
            itemsIndexed(entries, key = { _, entry -> "entry:${entry.data}" }) { index, entry ->
                GroupedListItem(first = index == 0, last = index == entries.lastIndex) {
                    BasicComponent(
                        onClick = { onOpen(entry.data) },
                        startAction = {
                            Box(
                                Modifier.size(40.dp).squircleSurface(MiuixTheme.colorScheme.surfaceContainerHigh, 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (users) Icon(MiuixIcons.Glass.ContactsCircle, null, Modifier.size(24.dp))
                                else Text("#", style = MiuixTheme.textStyles.title3)
                            }
                        },
                        endActions = {
                            IconButton(onClick = { onRemove(entry.data) }) {
                                Icon(MiuixIcons.Remove, stringResource(if (users) R.string.blocklist_remove_user else R.string.blocklist_remove_topic, entry.data),
                                    Modifier.size(22.dp), tint = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                            }
                        },
                    ) {
                        Text(if (users) "UID ${entry.data}" else entry.data,
                            style = MiuixTheme.textStyles.headline2,
                            maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            item(key = "hint") {
                Text(stringResource(if (users) R.string.blocklist_view_user else R.string.blocklist_view_topic),
                    Modifier.padding(horizontal = 28.dp, vertical = 12.dp),
                    style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
        }
    }
}
