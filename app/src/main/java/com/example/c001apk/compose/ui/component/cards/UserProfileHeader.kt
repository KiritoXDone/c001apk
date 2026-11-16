package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.ImageView
import com.example.c001apk.compose.ui.ffflist.FFFListType
import com.example.c001apk.compose.util.DateUtils.fromToday
import com.example.c001apk.compose.util.Utils.richToString
import com.example.c001apk.compose.util.copyText
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Email
import top.yukonga.miuix.kmp.squircle.squircleBackground
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal object UserProfileDefaults {
    val CoverHeight = 232.dp
    val AvatarSize = 84.dp
    val AvatarBorder = 4.dp
    val AvatarOverlap = 52.dp
    val ContentPadding = 20.dp
    val SectionSpacing = 12.dp
}

@Composable
fun UserProfileHeader(
    data: HomeFeedResponse.Data,
    coverHeight: Dp = UserProfileDefaults.CoverHeight,
    onFollow: (String, Int) -> Unit,
    onLocalFollow: () -> Unit,
    onPMUser: (String, String) -> Unit,
    onViewFFFList: (String, String) -> Unit,
) {
    val context = LocalContext.current
    val colors = MiuixTheme.colorScheme
    val prefs = LocalUserPreferences.current
    val locallyFollowed = prefs.localFollows.any { it.uid == data.uid }
    val ownProfile = prefs.isLogin && prefs.uid == data.uid
    val followed = if (prefs.isLogin) data.isFollow == 1 else locallyFollowed

    Box(Modifier.fillMaxWidth().testTag("user_profile_header")) {
        Box(
            Modifier.fillMaxWidth().height(coverHeight).background(colors.secondaryContainer),
        ) {
            if (!data.cover.isNullOrBlank()) {
                ImageView(
                    Modifier.fillMaxSize().semantics { contentDescription = "查看用户封面" },
                    url = data.cover,
                )
            }
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.62f),
                        0.42f to Color.Transparent,
                        0.62f to Color.Transparent,
                        1f to colors.surface,
                    ),
                ),
            )
        }
        Column(
            Modifier.fillMaxWidth().padding(
                start = UserProfileDefaults.ContentPadding,
                end = UserProfileDefaults.ContentPadding,
                top = coverHeight - UserProfileDefaults.AvatarOverlap,
            ),
        ) {
            Box(
                Modifier.size(UserProfileDefaults.AvatarSize + UserProfileDefaults.AvatarBorder * 2)
                    .squircleBackground(colors.surface, 28.dp)
                    .padding(UserProfileDefaults.AvatarBorder)
                    .squircleClip(24.dp)
                    .squircleBackground(colors.secondaryContainer, 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(data.username.orEmpty().take(1), style = MiuixTheme.textStyles.title1)
                if (!data.userAvatar.isNullOrBlank()) {
                    ImageView(
                        Modifier.fillMaxSize().semantics { contentDescription = "查看用户头像" },
                        url = data.userAvatar,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProfileCopyText(
                    text = data.username.orEmpty(),
                    onClick = { context.copyText(data.username.orEmpty()) },
                    modifier = Modifier.weight(1f, fill = false).testTag("user_copy_name"),
                    textStyle = MiuixTheme.textStyles.title2.copy(fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
                    maxLines = 2,
                )
                data.level?.let { level ->
                    Text(
                        "Lv.$level",
                        modifier = Modifier.squircleBackground(colors.tertiaryContainer, 6.dp)
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        color = colors.onTertiaryContainer,
                        maxLines = 1,
                    )
                }
            }
            ProfileCopyText(
                text = buildString {
                    append("UID ${data.uid.orEmpty()}")
                    if ((data.logintime ?: 0) > 0) append(" · ${fromToday(data.logintime ?: 0)}活跃")
                },
                onClick = { context.copyText(data.uid.orEmpty()) },
                modifier = Modifier.padding(top = 6.dp).testTag("user_copy_uid"),
                textStyle = MiuixTheme.textStyles.footnote1.copy(fontSize = 12.sp),
                color = colors.onSurfaceVariantSummary,
            )
            if (!data.bio.isNullOrBlank()) {
                ProfileCopyText(
                    text = data.bio.richToString(),
                    onClick = { context.copyText(data.bio) },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp).testTag("user_copy_bio"),
                    textStyle = MiuixTheme.textStyles.body2.copy(fontSize = 14.sp, lineHeight = 21.sp),
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp, bottom = if (ownProfile) 0.dp else 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                UserStatistic("动态", (data.feed?.id ?: 0).toString(), Modifier.weight(1f))
                UserStatistic("获赞", (data.beLikeNum ?: 0).toString(), Modifier.weight(1f))
                UserStatistic("关注", (data.follow ?: 0).toString(), Modifier.weight(1f)) {
                    onViewFFFList(data.uid.orEmpty(), FFFListType.USER_FOLLOW.name)
                }
                UserStatistic("粉丝", (data.fans ?: 0).toString(), Modifier.weight(1f)) {
                    onViewFFFList(data.uid.orEmpty(), FFFListType.FAN.name)
                }
            }
            if (!ownProfile) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        modifier = Modifier.weight(1f).testTag("user_follow"),
                        onClick = {
                            if (prefs.isLogin) onFollow(data.uid.orEmpty(), data.isFollow ?: 0)
                            else onLocalFollow()
                        },
                        colors = if (followed) ButtonDefaults.buttonColors() else ButtonDefaults.buttonColorsPrimary(),
                    ) {
                        if (!followed) {
                            Icon(MiuixIcons.Add, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            if (!prefs.isLogin) {
                                if (locallyFollowed) "已本地关注" else "本地关注"
                            } else if (data.isFollow == 1) "已关注" else "关注",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Button(
                        modifier = Modifier.weight(1f).testTag("user_private_message"),
                        onClick = { onPMUser(data.uid.orEmpty(), data.username.orEmpty()) },
                        enabled = prefs.isLogin,
                    ) {
                        Icon(MiuixIcons.Email, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("私信", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
                if (prefs.isLogin && prefs.quietFollow) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        text = if (locallyFollowed) "已悄悄关注" else "悄悄关注",
                        onClick = onLocalFollow,
                        modifier = Modifier.fillMaxWidth().testTag("user_quiet_follow"),
                    )
                }
            }
        }
    }
}

@Composable
private fun UserStatistic(
    label: String,
    value: String,
    modifier: Modifier,
    onClick: (() -> Unit)? = null,
) {
    val content: @Composable () -> Unit = {
        Row(
            Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary, maxLines = 1)
        }
    }
    if (onClick == null) {
        Box(modifier) { content() }
    } else {
        Card(
            modifier = modifier.testTag("user_stat_$label"),
            colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(color = Color.Transparent),
            showIndication = true,
            onClick = onClick,
        ) { content() }
    }
}

@Composable
private fun ProfileCopyText(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle,
    color: Color = MiuixTheme.colorScheme.onSurface,
    maxLines: Int = Int.MAX_VALUE,
) {
    Card(
        modifier = modifier,
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(color = Color.Transparent),
        showIndication = true,
        onClick = onClick,
    ) {
        Text(text, style = textStyle, color = color, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
    }
}
