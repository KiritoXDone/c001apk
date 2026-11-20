package com.example.c001apk.compose.ui.settings

import androidx.compose.runtime.Composable
import com.example.c001apk.compose.logic.model.BottomBarStyle
import com.example.c001apk.compose.logic.model.TopBarStyle
import com.example.c001apk.compose.ui.component.settings.DropdownListItem
import com.example.c001apk.compose.ui.component.settings.SelectionItem

private val topBarStyles = listOf(
    SelectionItem("经典", TopBarStyle.Classic),
    SelectionItem("经典轮廓", TopBarStyle.ClassicContour),
    SelectionItem("OS4", TopBarStyle.OS4),
)

private val bottomBarStyles = listOf(
    SelectionItem("经典", BottomBarStyle.Classic),
    SelectionItem("悬浮", BottomBarStyle.Floating),
    SelectionItem("OS4", BottomBarStyle.OS4),
)

@Composable
internal fun BarStylePreferences(
    topBarStyle: TopBarStyle,
    bottomBarStyle: BottomBarStyle,
    onTopBarStyleChange: (TopBarStyle) -> Unit,
    onBottomBarStyleChange: (BottomBarStyle) -> Unit,
) {
    DropdownListItem(
        value = topBarStyle,
        headlineText = "顶栏样式",
        selections = topBarStyles,
        onValueChanged = { _, value -> onTopBarStyleChange(value) },
        useWindowPopup = true,
    )
    DropdownListItem(
        value = bottomBarStyle,
        headlineText = "底栏样式",
        selections = bottomBarStyles,
        onValueChanged = { _, value -> onBottomBarStyleChange(value) },
        useWindowPopup = true,
    )
}
