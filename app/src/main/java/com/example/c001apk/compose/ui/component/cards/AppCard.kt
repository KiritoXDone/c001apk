package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.component.CoilLoader
import com.example.c001apk.compose.ui.component.MiuixActionButton as TextButton
import com.example.c001apk.compose.ui.theme.cardBg
import com.example.c001apk.compose.util.DateUtils.fromToday
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Created by bggRGjQaUbCoE on 2024/6/10 */
enum class AppCardType {
    APP,
    PRODUCT,
    TOPIC,
    USER,
    CONTACTS,
    RECENT,
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    data: HomeFeedResponse.Data,
    onOpenLink: (String, String?) -> Unit,
    appCardType: AppCardType,
    isHomeFeed: Boolean = false,
    onViewUser: (String) -> Unit,
    onFollowUser: ((String, Int) -> Unit)? = null,
    onHandleRecent: ((String, String, String, Int) -> Unit)? = null,
) {
    val canFollow = appCardType == AppCardType.USER && onFollowUser != null

    ConstraintLayout(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
                .squircleClip(16.dp)
                .background(
                    if (isHomeFeed) MiuixTheme.colorScheme.surface
                    else if (data.isTop == 1) MiuixTheme.colorScheme.primaryContainer else cardBg(),
                )
                .combinedClickable(
                    onClick = {
                        if (appCardType == AppCardType.CONTACTS)
                            onViewUser(data.userInfo?.uid ?: data.fUserInfo?.uid.orEmpty())
                        else
                            onOpenLink(
                                data.url.orEmpty(),
                                if (appCardType == AppCardType.USER)
                                    data.username
                                        ?: data.userInfo?.username
                                        ?: data.fUserInfo?.username
                                else data.title,
                            )
                    },
                    onLongClick =
                        if (appCardType == AppCardType.RECENT) {
                            {
                                onHandleRecent?.let {
                                    it(
                                        data.id.orEmpty(),
                                        data.targetId.orEmpty(),
                                        data.targetType.orEmpty(),
                                        data.isTop ?: 0,
                                    )
                                }
                            }
                        } else null,
                )
                .padding(10.dp),
    ) {
        val (logo, appName, commentNum, downCount, active, follow) = createRefs()

        CoilLoader(
            url =
                when (appCardType) {
                    AppCardType.APP,
                    AppCardType.PRODUCT,
                    AppCardType.TOPIC,
                    AppCardType.RECENT -> data.logo
                    AppCardType.USER -> data.userAvatar
                    AppCardType.CONTACTS -> data.userInfo?.userAvatar ?: data.fUserInfo?.userAvatar
                },
            modifier =
                Modifier.clip(RoundedCornerShape(8.dp)).aspectRatio(1f).constrainAs(logo) {
                    start.linkTo(parent.start)
                    top.linkTo(appName.top)
                    bottom.linkTo(commentNum.bottom)
                    height = Dimension.fillToConstraints
                },
        )

        Text(
            text =
                when (appCardType) {
                    AppCardType.APP,
                    AppCardType.PRODUCT,
                    AppCardType.TOPIC -> data.title.orEmpty()
                    AppCardType.USER -> data.username.orEmpty()
                    AppCardType.CONTACTS ->
                        data.userInfo?.username ?: data.fUserInfo?.username.orEmpty()

                    AppCardType.RECENT -> "${data.targetTypeTitle}: ${data.title}"
                },
            modifier =
                Modifier.padding(
                        start = 10.dp,
                        end = if (appCardType == AppCardType.USER) 10.dp else 0.dp,
                    )
                    .constrainAs(appName) {
                        start.linkTo(logo.end)
                        top.linkTo(parent.top)
                        end.linkTo(
                            if (canFollow) follow.start else parent.end,
                        )
                        width = Dimension.fillToConstraints
                    },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MiuixTheme.textStyles.headline2.copy(lineHeight = 18.sp),
        )

        Text(
            text =
                when (appCardType) {
                    AppCardType.APP -> stringResource(R.string.stat_discussions, data.commentnum.toString())
                    AppCardType.PRODUCT,
                    AppCardType.TOPIC -> stringResource(R.string.stat_popularity, data.hotNumTxt.toString())
                    AppCardType.USER -> stringResource(R.string.stat_following, data.follow.toString())
                    AppCardType.CONTACTS ->
                        stringResource(R.string.stat_following, (data.userInfo?.follow ?: data.fUserInfo?.follow).toString())
                    AppCardType.RECENT -> stringResource(R.string.stat_followed_by, data.followNum.toString())
                },
            modifier =
                Modifier.padding(start = 10.dp, top = 5.dp).constrainAs(commentNum) {
                    start.linkTo(logo.end)
                    top.linkTo(appName.bottom)
                },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MiuixTheme.textStyles.body1.copy(fontSize = 13.sp, lineHeight = 16.sp),
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )

        Text(
            text =
                when (appCardType) {
                    AppCardType.APP -> stringResource(R.string.stat_downloads, data.downCount.toString())
                    AppCardType.PRODUCT -> stringResource(R.string.stat_discussions, data.feedCommentNumTxt.toString())
                    AppCardType.TOPIC -> stringResource(R.string.stat_discussions, data.commentnumTxt.toString())
                    AppCardType.USER -> stringResource(R.string.stat_followers, data.fans.toString())
                    AppCardType.CONTACTS -> stringResource(R.string.stat_followers, (data.userInfo?.fans ?: data.fUserInfo?.fans).toString())
                    AppCardType.RECENT ->
                        when (data.targetType) {
                            "user" -> stringResource(R.string.stat_followers, data.fansNum.toString())
                            else -> stringResource(R.string.stat_discussions, data.commentNum.toString()) /*"apk","topic"*/
                        }
                },
            modifier =
                Modifier.padding(start = 10.dp, top = 5.dp).constrainAs(downCount) {
                    start.linkTo(commentNum.end)
                    top.linkTo(appName.bottom)
                },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MiuixTheme.textStyles.body1.copy(fontSize = 13.sp, lineHeight = 16.sp),
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )

        if (appCardType in listOf(AppCardType.USER, AppCardType.CONTACTS)) {
            Text(
                text =
                    if (appCardType == AppCardType.USER) stringResource(R.string.user_active_at, fromToday(data.logintime ?: 0))
                    else
                        stringResource(R.string.user_active_at, fromToday(data.userInfo?.logintime ?: data.fUserInfo?.logintime ?: 0)),
                modifier =
                    Modifier.padding(start = 10.dp, top = 5.dp).constrainAs(active) {
                        start.linkTo(downCount.end)
                        top.linkTo(appName.bottom)
                    },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style =
                    MiuixTheme.textStyles.body1.copy(
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                    ),
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )

            if (canFollow) {
                TextButton(
                    onClick = {
                        onFollowUser(data.uid.orEmpty(), data.isFollow ?: 0)
                    },
                    modifier =
                        Modifier.constrainAs(follow) {
                            top.linkTo(appName.top)
                            end.linkTo(parent.end)
                            bottom.linkTo(commentNum.bottom)
                            height = Dimension.fillToConstraints
                        },
                ) {
                    Text(
                        text = stringResource(if (data.isFollow == 1) R.string.menu_unfollow else R.string.menu_follow),
                        color =
                            if (data.isFollow == 1) MiuixTheme.colorScheme.onSurfaceVariantSummary
                            else MiuixTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
