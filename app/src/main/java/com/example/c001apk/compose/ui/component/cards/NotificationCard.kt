package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.component.CoilLoader
import com.example.c001apk.compose.ui.component.LinkText
import com.example.c001apk.compose.ui.component.MiuixMenuItem as DropdownMenuItem
import com.example.c001apk.compose.ui.component.MiuixMoreMenu
import com.example.c001apk.compose.ui.theme.cardBg
import com.example.c001apk.compose.util.DateUtils
import com.example.c001apk.compose.util.ReportType
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Created by bggRGjQaUbCoE on 2024/6/11 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationCard(
    modifier: Modifier = Modifier,
    data: HomeFeedResponse.Data,
    onViewUser: (String) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onReport: ((String, ReportType) -> Unit)? = null,
    onDeleteNotice: ((String) -> Unit)? = null,
) {

    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    val isFollow by lazy { data.type == "contacts_follow" }

    ConstraintLayout(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .squircleSurface(cardBg(), 16.dp)
                .combinedClickable(
                    onClick = {
                        if (isFollow) {
                            onOpenLink(data.url.orEmpty(), null)
                        } else {
                            val doc: Document = Jsoup.parse(data.note.orEmpty())
                            val links: Elements = doc.select("a[href]")
                            onOpenLink(
                                links.getOrNull(0)?.attr("href").orEmpty(),
                                null,
                            )
                        }
                    },
                    onLongClick = {
                        onDeleteNotice?.let { it(data.id.orEmpty()) }
                    },
                ),
    ) {
        val (avatar, username, expand, message, dateLine) = createRefs()

        CoilLoader(
            url = data.fromUserAvatar,
            modifier =
                Modifier.padding(start = 10.dp, top = 10.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .constrainAs(avatar) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                    }
                    .clickable {
                        onViewUser(data.fromuid.orEmpty())
                    },
        )

        Text(
            text = data.fromusername.orEmpty(),
            modifier =
                Modifier.padding(start = 10.dp, top = 10.dp, end = 10.dp).constrainAs(username) {
                    start.linkTo(avatar.end)
                    top.linkTo(parent.top)
                    end.linkTo(expand.start)
                    width = Dimension.fillToConstraints
                },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MiuixTheme.textStyles.headline2,
        )

        LinkText(
            text = data.note.orEmpty(),
            modifier =
                Modifier.padding(start = 10.dp, top = 5.dp, end = 10.dp).constrainAs(message) {
                    start.linkTo(avatar.end)
                    top.linkTo(username.bottom)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
            lineSpacingMultiplier = 1.2f,
            onOpenLink = if (isFollow) null else onOpenLink,
        )

        Text(
            text = DateUtils.fromToday(data.dateline ?: 0),
            modifier =
                Modifier.padding(start = 10.dp, top = 10.dp, bottom = 10.dp).constrainAs(dateLine) {
                    start.linkTo(avatar.end)
                    top.linkTo(message.bottom)
                },
            style = MiuixTheme.textStyles.body2.copy(fontSize = 14.sp),
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )

        MiuixMoreMenu(
            expanded = dropdownMenuExpanded,
            onExpandedChange = { dropdownMenuExpanded = it },
            modifier =
                Modifier.constrainAs(expand) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                },
        ) {
            listOf(stringResource(R.string.menu_block), stringResource(id = R.string.menu_report)).forEachIndexed { index, menu ->
                DropdownMenuItem(
                    text = { Text(menu) },
                    onClick = {
                        dropdownMenuExpanded = false
                        when (index) {
                            1 -> onReport?.let { it(data.fromuid.orEmpty(), ReportType.USER) }
                        }
                    },
                )
            }
        }
    }
}
