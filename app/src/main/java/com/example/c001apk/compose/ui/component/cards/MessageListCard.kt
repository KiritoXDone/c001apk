package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.R
import top.yukonga.miuix.kmp.basic.Badge
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MessageListCard(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    title: String,
    count: Int,
    onViewNotice: () -> Unit,
) {
    val description = if (count > 0) stringResource(R.string.unread_description, title, count.toString()) else title
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = modifier,
        colors = CardDefaults.defaultColors(color = Color.Transparent),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_message_$title")
                .semantics { contentDescription = description }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = onViewNotice,
                )
                .padding(vertical = ProfileLayoutDefaults.ContentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ProfileLayoutDefaults.MessageContentSpacing),
        ) {
            Box {
                Box(
                    modifier = Modifier.size(ProfileLayoutDefaults.MessageCircleSize)
                        .clip(CircleShape)
                        .background(MiuixTheme.colorScheme.secondaryContainer)
                        .indication(interactionSource, LocalIndication.current),
                    contentAlignment = Alignment.Center,
                ) { icon() }
                if (count > 0) {
                    Badge(Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-4).dp)) {
                        Text(if (count > 99) "99+" else count.toString())
                    }
                }
            }
            Text(
                title,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceContainer,
                maxLines = 1,
            )
        }
    }
}
