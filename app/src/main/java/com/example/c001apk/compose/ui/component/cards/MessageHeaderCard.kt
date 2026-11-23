package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.component.CoilLoader
import com.example.c001apk.compose.util.CookieUtil
import com.example.c001apk.compose.util.decodeCookieValue
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.ProgressIndicatorDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.ChevronForward
import top.yukonga.miuix.kmp.icon.glass.ContactsCircle
import top.yukonga.miuix.kmp.squircle.squircleBackground
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MessageHeaderCard(
    modifier: Modifier = Modifier,
    isLogin: Boolean,
    userAvatar: String,
    userName: String,
    onLogin: () -> Unit,
    onViewUser: (String) -> Unit,
) {
    val headerDescription = stringResource(if (isLogin) R.string.profile_view_own else R.string.profile_login)
    Card(
        modifier = modifier.fillMaxWidth().testTag("profile_header").semantics(mergeDescendants = true) {
            role = Role.Button
            contentDescription = headerDescription
        },
        colors = CardDefaults.defaultColors(
            color = Color.Transparent,
            contentColor = MiuixTheme.colorScheme.onSurface,
        ),
        onClick = { if (isLogin) onViewUser(CookieUtil.uid) else onLogin() },
        showIndication = true,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = ProfileLayoutDefaults.HeaderInset)
                .padding(top = ProfileLayoutDefaults.HeaderTopSpacing),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ProfileLayoutDefaults.HeaderContentSpacing),
        ) {
            Box(
                Modifier.size(ProfileLayoutDefaults.AvatarSize)
                    .squircleBackground(
                        MiuixTheme.colorScheme.secondaryContainer,
                        ProfileLayoutDefaults.AvatarCornerRadius,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    MiuixIcons.Glass.ContactsCircle,
                    null,
                    Modifier.size(32.dp),
                    tint = MiuixTheme.colorScheme.onSecondaryContainer,
                )
                if (isLogin)
                    CoilLoader(
                        url = userAvatar,
                        modifier = Modifier.fillMaxSize()
                            .squircleClip(ProfileLayoutDefaults.AvatarCornerRadius),
                    )
            }
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ProfileLayoutDefaults.HeaderTextSpacing),
            ) {
                Text(
                    if (isLogin) decodeCookieValue(userName) else stringResource(R.string.profile_login),
                    fontSize = ProfileLayoutDefaults.UserNameFontSize,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(if (isLogin) R.string.profile_view_own else R.string.profile_login_summary),
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Icon(
                        MiuixIcons.Glass.ChevronForward,
                        null,
                        Modifier.size(16.dp),
                        tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }
    }
}

@Composable
fun MessageLevelCard(
    level: String,
    experience: String,
    nextLevelExperience: String,
    modifier: Modifier = Modifier,
) {
    val currentExperience = experience.toLongOrNull() ?: 0L
    val requiredExperience = nextLevelExperience.toLongOrNull() ?: 0L
    val nextLevel = level.toIntOrNull()?.plus(1)
    Card(
        modifier = modifier.fillMaxWidth().testTag("profile_level"),
        colors = CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.tertiaryContainer,
            contentColor = MiuixTheme.colorScheme.onTertiaryContainer,
        ),
    ) {
        Column(Modifier.padding(ProfileLayoutDefaults.ContentPadding)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ProfileLayoutDefaults.ItemSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Lv.${level.ifBlank { "0" }}", fontWeight = FontWeight.SemiBold)
                Text(
                    if (requiredExperience > currentExperience && nextLevel != null) {
                        stringResource(R.string.profile_experience_remaining, requiredExperience - currentExperience, nextLevel)
                    } else {
                        stringResource(R.string.profile_experience, experience.ifBlank { "0" }, nextLevelExperience.ifBlank { "0" })
                    },
                    modifier = Modifier.weight(1f),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = (currentExperience.toFloat() / requiredExperience.coerceAtLeast(1L))
                    .coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth(),
                colors = ProgressIndicatorDefaults.progressIndicatorColors(
                    backgroundColor = MiuixTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.22f),
                ),
                height = 6.dp,
            )
        }
    }
}
