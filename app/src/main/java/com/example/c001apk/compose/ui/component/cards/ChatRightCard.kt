package com.example.c001apk.compose.ui.component.cards

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.c001apk.compose.logic.model.HomeFeedResponse

@Composable
fun ChatRightCard(
    modifier: Modifier = Modifier,
    data: HomeFeedResponse.Data,
    onGetImageUrl: (String) -> Unit,
    onLongClick: (String, String, String) -> Unit,
    onViewUser: (String) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onClearFocus: () -> Unit,
) {
    ChatMessageBubble(
        data = data,
        outgoing = true,
        modifier = modifier,
        onGetImageUrl = onGetImageUrl,
        onLongClick = onLongClick,
        onViewUser = onViewUser,
        onOpenLink = onOpenLink,
        onClearFocus = onClearFocus,
    )
}
