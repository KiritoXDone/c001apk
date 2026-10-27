package com.example.c001apk.compose.ui.event

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.logic.model.EventDetailResponse
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.CoilLoader
import com.example.c001apk.compose.ui.component.MiuixHeader as TopAppBar
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.cards.LoadingCard
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.ui.component.TopBarScrollEffect
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Alarm
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.Community
import top.yukonga.miuix.kmp.icon.extended.Help
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Promotions
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val EVENT_RULE_URL = "/page?url=V8_GOODS_ZHONGCE_README"
private const val EVENT_RULE_TITLE = "酷品-众测规则"

@Composable
fun EventScreen(
    id: String,
    onBackClick: () -> Unit,
    onViewUser: (String) -> Unit,
    onOpenTab: (String, String) -> Unit,
    onRegister: (String) -> Unit,
) {
    val viewModel =
        hiltViewModel<EventViewModel, EventViewModel.ViewModelFactory>(key = id) { factory ->
            factory.create(id)
        }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                windowInsets =
                    WindowInsets.systemBars.only(WindowInsetsSides.Start + WindowInsetsSides.Top),
                navigationIcon = { BackButton(onBackClick) },
                title = { Text("酷安众测") },
            )
        },
    ) { paddingValues ->
        when (val state = viewModel.eventState) {
            LoadingState.Loading,
            LoadingState.Empty,
            is LoadingState.Error -> {
                Box(
                    modifier =
                        Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding()),
                ) {
                    LoadingCard(
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 16.dp),
                        state = state,
                        onClick =
                            if (state is LoadingState.Loading) null else viewModel::fetchEvent,
                    )
                }
            }

            is LoadingState.Success ->
                EventContent(
                    event = state.response,
                    paddingValues = paddingValues,
                    onViewUser = onViewUser,
                    onOpenTab = onOpenTab,
                    onRegister = onRegister,
                )
        }
    }
}

@Composable
private fun EventContent(
    event: EventDetailResponse.Data,
    paddingValues: PaddingValues,
    onViewUser: (String) -> Unit,
    onOpenTab: (String, String) -> Unit,
    onRegister: (String) -> Unit,
) {
    val now = System.currentTimeMillis() / 1000
    val registrationOpen =
        event.actionUrl?.isNotBlank() == true &&
            event.stageStatus != 2 &&
            event.stageStatus != 3 &&
            (event.registrationStart == null || now >= event.registrationStart) &&
            (event.registrationEnd == null || now <= event.registrationEnd)
    val statusText =
        when {
            event.stageStatus == 3 || (event.eventEnd != null && now > event.eventEnd) -> "活动已结束"
            event.registrationStart != null && now < event.registrationStart -> "报名未开始"
            event.stageStatus == 2 -> "活动进行中"
            event.registrationEnd != null && now > event.registrationEnd -> "报名已结束"
            registrationOpen -> "立即报名"
            else -> "暂不可报名"
        }

    val topBarListState = rememberLazyListState()
    TopBarScrollEffect { topBarListState.animateScrollToItem(0) }
    LazyColumn(
        state = topBarListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding() + 16.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "header") {
            Column {
                CoilLoader(
                    url = event.logo,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1080f / 608f),
                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = event.title.orEmpty(),
                        style = MiuixTheme.textStyles.title3,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Alarm,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = registrationPeriod(event),
                            style = MiuixTheme.textStyles.body1,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = registrationOpen,
                        onClick =
                            rememberHapticClick {
                                event.actionUrl?.let(onRegister)
                            },
                        colors =
                            if (registrationOpen) ButtonDefaults.buttonColors()
                            else
                                ButtonDefaults.buttonColors(
                                    disabledColor = MiuixTheme.colorScheme.errorContainer,
                                    disabledContentColor = MiuixTheme.colorScheme.onErrorContainer,
                                ),
                    ) {
                        Text(statusText)
                    }
                    HorizontalDivider()
                    Row(
                        modifier =
                            Modifier.fillMaxWidth()
                                .clickable(
                                    onClick =
                                        rememberHapticClick {
                                            onOpenTab(EVENT_RULE_URL, EVENT_RULE_TITLE)
                                        },
                                )
                                .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Help,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = "众测须知",
                            style = MiuixTheme.textStyles.headline2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
        }

        event.noticeRule
            ?.takeIf { it.isNotBlank() }
            ?.let { notice ->
                item(key = "notice") {
                    EventSection(title = "报名须知", icon = MiuixIcons.Info) {
                        Text(
                            text = notice.trim(),
                            style = MiuixTheme.textStyles.main,
                            lineHeight = MiuixTheme.textStyles.main.lineHeight * 1.25f,
                        )
                    }
                }
            }

        event.sponsorUser
            ?.takeIf { it.isNotEmpty() }
            ?.let { sponsors ->
                item(key = "sponsors") {
                    EventSection(title = "主办方", icon = MiuixIcons.Community) {
                        sponsors.forEachIndexed { index, sponsor ->
                            SponsorRow(sponsor = sponsor, onViewUser = onViewUser)
                            if (index != sponsors.lastIndex) HorizontalDivider()
                        }
                    }
                }
            }

        event.sponsorPrize
            ?.takeIf { it.isNotEmpty() }
            ?.let { prizes ->
                item(key = "prizes") {
                    EventSection(title = "活动奖品", icon = MiuixIcons.Promotions) {
                        prizes.forEachIndexed { index, prize ->
                            PrizeRow(prize)
                            if (index != prizes.lastIndex) HorizontalDivider()
                        }
                    }
                }
            }

        event.tabList
            ?.takeIf { it.isNotEmpty() }
            ?.let { tabs ->
                item(key = "tabs") {
                    EventSection(title = "相关内容") {
                        tabs.forEachIndexed { index, tab ->
                            val title = tab.title.orEmpty()
                            Row(
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .clickable(
                                            enabled = !tab.url.isNullOrBlank(),
                                            onClick =
                                                rememberHapticClick {
                                                    tab.url?.let { onOpenTab(it, title) }
                                                },
                                        )
                                        .padding(vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = title,
                                    style = MiuixTheme.textStyles.subtitle,
                                    modifier = Modifier.weight(1f),
                                )
                                Icon(
                                    imageVector = MiuixIcons.ChevronForward,
                                    contentDescription = null,
                                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                            }
                            if (index != tabs.lastIndex) HorizontalDivider()
                        }
                    }
                }
            }
    }
}

@Composable
private fun EventSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        shape = RoundedCornerShape(8.dp),
        color = MiuixTheme.colorScheme.surfaceContainer,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.size(21.dp),
                    )
                }
                Text(
                    text = title,
                    style = MiuixTheme.textStyles.subtitle,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun SponsorRow(
    sponsor: EventDetailResponse.SponsorUser,
    onViewUser: (String) -> Unit,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clickable(
                    enabled = !sponsor.uid.isNullOrBlank(),
                    onClick = rememberHapticClick { sponsor.uid?.let(onViewUser) },
                )
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CoilLoader(
            url = sponsor.userAvatar,
            modifier = Modifier.size(48.dp).clip(CircleShape),
        )
        Text(
            text = sponsor.displayUsername.orEmpty(),
            style = MiuixTheme.textStyles.subtitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = MiuixIcons.ChevronForward,
            contentDescription = null,
            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun PrizeRow(prize: EventDetailResponse.SponsorPrize) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CoilLoader(
            url = prize.logo,
            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(6.dp)),
        )
        Text(
            text = prize.title.orEmpty(),
            style = MiuixTheme.textStyles.subtitle,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun registrationPeriod(event: EventDetailResponse.Data): String {
    val start = event.registrationStart?.formatEventDate()
    val end = event.registrationEnd?.formatEventDate()
    return if (start != null && end != null) "报名时间：$start 至 $end" else "报名时间待定"
}

private fun Long.formatEventDate(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date(this * 1000))
