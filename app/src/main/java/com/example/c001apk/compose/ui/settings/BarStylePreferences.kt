package com.example.c001apk.compose.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.model.BottomBarStyle
import com.example.c001apk.compose.logic.model.TopBarStyle
import com.example.c001apk.compose.ui.component.settings.SelectionItem
import com.example.c001apk.compose.ui.component.settings.SelectionListItem
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteLayoutBottomLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteLayoutTopLine

@Composable
private fun topBarStyleItems() =
    listOf(
        SelectionItem(stringResource(R.string.settings_bar_classic), TopBarStyle.Classic),
        SelectionItem(stringResource(R.string.settings_bar_classic_contour), TopBarStyle.ClassicContour),
        SelectionItem("OS4", TopBarStyle.OS4),
    )

@Composable
private fun bottomBarStyleItems() =
    listOf(
        SelectionItem(stringResource(R.string.settings_bar_classic), BottomBarStyle.Classic),
        SelectionItem(stringResource(R.string.settings_bar_floating), BottomBarStyle.Floating),
        SelectionItem("OS4", BottomBarStyle.OS4),
    )

@Composable
internal fun BarStylePreferences(
    topBarStyle: TopBarStyle,
    bottomBarStyle: BottomBarStyle,
    onTopBarStyleChange: (TopBarStyle) -> Unit,
    onBottomBarStyleChange: (BottomBarStyle) -> Unit,
) {
    SelectionListItem(
        leadingImageVector = MingCuteLayoutTopLine,
        title = stringResource(R.string.settings_top_bar_style),
        value = topBarStyle,
        selections = topBarStyleItems(),
        onSelected = onTopBarStyleChange,
    )
    SelectionListItem(
        leadingImageVector = MingCuteLayoutBottomLine,
        title = stringResource(R.string.settings_bottom_bar_style),
        value = bottomBarStyle,
        selections = bottomBarStyleItems(),
        onSelected = onBottomBarStyleChange,
    )
}
