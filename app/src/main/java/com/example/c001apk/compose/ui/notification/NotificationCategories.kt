package com.example.c001apk.compose.ui.notification

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.ui.component.MiuixPagerTabRow
import com.example.c001apk.compose.ui.component.TopBarScrollPage

@Composable
internal fun NotificationCategories(
    pagerState: PagerState,
) {
    MiuixPagerTabRow(
        tabs = notificationCategories.map { stringResource(it.titleRes) },
        pagerState = pagerState,
    )
}

@Composable
internal fun NotificationPager(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    content: @Composable (NoticeType) -> Unit,
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        key = { notificationCategories[it].name },
    ) { index ->
        TopBarScrollPage(pagerState.currentPage == index) {
            content(notificationCategories[index])
        }
    }
}
