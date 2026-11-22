package com.example.c001apk.compose.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
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
    PreferenceGroup(title = stringResource(R.string.settings_group_home_content)) {
        BasicListItem(
            leadingImageVector = MingCuteGridLine,
            headlineText = stringResource(R.string.settings_home_tabs),
            supportingText = stringResource(R.string.settings_home_tabs_summary),
            onClick = onHomeTabsClick,
        )
        SwitchListItem(
            value = showCarousel,
            leadingImageVector = MingCutePhotoAlbumLine,
            headlineText = stringResource(R.string.settings_show_carousel),
            supportingText = stringResource(R.string.settings_show_carousel_summary),
            onValueChanged = onShowCarousel,
        )
        SwitchListItem(
            value = showChannels,
            leadingImageVector = MingCuteGridLine,
            headlineText = stringResource(R.string.settings_show_channels),
            supportingText = stringResource(R.string.settings_show_channels_summary),
            onValueChanged = onShowChannels,
        )
        SwitchListItem(
            value = showTopics,
            leadingImageVector = MingCuteDocumentLine,
            headlineText = stringResource(R.string.settings_show_topics),
            supportingText = stringResource(R.string.settings_show_topics_summary),
            onValueChanged = onShowTopics,
        )
        SwitchListItem(
            value = showEmoji,
            leadingImageVector = MingCuteEmojiLine,
            headlineText = stringResource(R.string.settings_show_emoji),
            supportingText = stringResource(R.string.settings_show_emoji_summary),
            onValueChanged = onShowEmoji,
        )
    }
}
