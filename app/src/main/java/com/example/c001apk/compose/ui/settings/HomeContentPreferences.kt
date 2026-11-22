package com.example.c001apk.compose.ui.settings

import androidx.compose.runtime.Composable
import com.example.c001apk.compose.ui.component.settings.BasicListItem
import com.example.c001apk.compose.ui.component.settings.PreferenceGroup
import com.example.c001apk.compose.ui.component.settings.SwitchListItem
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteDocumentLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteEmojiLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteGridLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCutePhotoAlbumLine

@Composable
internal fun HomeContentPreferences(
    showCarousel: Boolean,
    showChannels: Boolean,
    showTopics: Boolean,
    showEmoji: Boolean,
    onHomeTabsClick: () -> Unit,
    onShowCarousel: (Boolean) -> Unit,
    onShowChannels: (Boolean) -> Unit,
    onShowTopics: (Boolean) -> Unit,
    onShowEmoji: (Boolean) -> Unit,
) {
    PreferenceGroup(title = "首页内容") {
        BasicListItem(
            leadingImageVector = MingCuteGridLine,
            headlineText = "首页板块",
            supportingText = "选择并排序首页显示的标签页",
            onClick = onHomeTabsClick,
        )
        SwitchListItem(
            value = showCarousel,
            leadingImageVector = MingCutePhotoAlbumLine,
            headlineText = "显示轮播横幅",
            supportingText = "首页动态顶部的大图推荐和活动横幅",
            onValueChanged = onShowCarousel,
        )
        SwitchListItem(
            value = showChannels,
            leadingImageVector = MingCuteGridLine,
            headlineText = "显示频道快捷入口",
            supportingText = "首页动态中的值得看、热闻、活动等入口",
            onValueChanged = onShowChannels,
        )
        SwitchListItem(
            value = showTopics,
            leadingImageVector = MingCuteDocumentLine,
            headlineText = "显示推荐话题与数码入口",
            supportingText = "首页动态中的机型、话题等横向或网格入口",
            onValueChanged = onShowTopics,
        )
        SwitchListItem(
            value = showEmoji,
            leadingImageVector = MingCuteEmojiLine,
            headlineText = "显示酷安表情",
            supportingText = "将普通文本中的 [doge] 等表情代码显示为图片",
            onValueChanged = onShowEmoji,
        )
    }
}
