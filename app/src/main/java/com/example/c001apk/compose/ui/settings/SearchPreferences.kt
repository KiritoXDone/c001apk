package com.example.c001apk.compose.ui.settings

import androidx.compose.runtime.Composable
import com.example.c001apk.compose.ui.component.settings.DropdownListItem
import com.example.c001apk.compose.ui.component.settings.PreferenceGroup
import com.example.c001apk.compose.ui.component.settings.SelectionItem
import com.example.c001apk.compose.ui.search.SearchOrderType

@Composable
internal fun SearchPreferences(value: String, onValueChanged: (String) -> Unit) {
    PreferenceGroup(title = "搜索") {
        DropdownListItem(
            value = SearchOrderType.entries.firstOrNull { it.name == value }?.name ?: SearchOrderType.COMPREHENSIVE.name,
            headlineText = "默认搜索排序",
            selections = SearchOrderType.entries.map { SelectionItem(it.title, it.name) },
            onValueChanged = { _, type -> onValueChanged(type) },
        )
    }
}
