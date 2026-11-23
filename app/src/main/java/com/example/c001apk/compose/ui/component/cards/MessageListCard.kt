package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
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
    Card(
        modifier = modifier.testTag("profile_message_$title").semantics(mergeDescendants = true) {
            role = Role.Button
            contentDescription = description
        },
        colors = CardDefaults.defaultColors(color = Color.Transparent),
        onClick = onViewNotice,
        showIndication = true,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = ProfileLayoutDefaults.ContentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ProfileLayoutDefaults.MessageContentSpacing),
        ) {
            Box {
                Box(
                    modifier = Modifier.size(ProfileLayoutDefaults.MessageCircleSize)
                        .background(MiuixTheme.colorScheme.secondaryContainer, CircleShape),
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
