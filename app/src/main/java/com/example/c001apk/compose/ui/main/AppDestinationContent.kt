package com.example.c001apk.compose.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.c001apk.compose.constant.Constants.URL_LOGIN
import com.example.c001apk.compose.ui.app.AppScreen
import com.example.c001apk.compose.ui.appupdate.AppUpdateScreen
import com.example.c001apk.compose.ui.blacklist.BlackListScreen
import com.example.c001apk.compose.ui.carousel.CarouselScreen
import com.example.c001apk.compose.ui.chat.ChatScreen
import com.example.c001apk.compose.ui.collection.CollectionScreen
import com.example.c001apk.compose.ui.component.WindowWidthSizeClass
import com.example.c001apk.compose.ui.component.TopBarScrollPage
import com.example.c001apk.compose.ui.coolpic.CoolPicScreen
import com.example.c001apk.compose.ui.dyh.DyhScreen
import com.example.c001apk.compose.ui.event.EventScreen
import com.example.c001apk.compose.ui.feed.FeedScreen
import com.example.c001apk.compose.ui.ffflist.FFFListScreen
import com.example.c001apk.compose.ui.ffflist.FFFListType
import com.example.c001apk.compose.ui.history.HistoryScreen
import com.example.c001apk.compose.ui.home.HomeTabEditorScreen
import com.example.c001apk.compose.ui.login.LoginScreen
import com.example.c001apk.compose.ui.notification.NoticeScreen
import com.example.c001apk.compose.ui.others.CopyTextScreen
import com.example.c001apk.compose.ui.search.SearchResultScreen
import com.example.c001apk.compose.ui.search.SearchScreen
import com.example.c001apk.compose.ui.settings.AboutScreen
import com.example.c001apk.compose.ui.settings.LicenseScreen
import com.example.c001apk.compose.ui.settings.ParamsScreen
import com.example.c001apk.compose.ui.settings.SettingsScreen
import com.example.c001apk.compose.ui.topic.TopicScreen
import com.example.c001apk.compose.ui.user.UserScreen
import com.example.c001apk.compose.ui.webview.WebViewScreen
import com.example.c001apk.compose.util.CookieUtil
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.openInBrowser
import kotlin.math.max
import kotlin.math.min

@Composable
internal fun AppDestinationContent(
    route: AppDestination,
    navigator: AppNavigator,
    mainPageState: MainPageState,
    onMainPageSelected: (Int) -> Unit,
    badge: Int,
    widthSizeClass: WindowWidthSizeClass,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onReport: (String, ReportType) -> Unit,
) {
    val context = LocalContext.current
    when (route) {
        AppDestination.Main -> {
            MainScreen(
                pageState = mainPageState,
                onSelect = onMainPageSelected,
                badge = badge,
                widthSizeClass = widthSizeClass,
                onSettingsClick = { navigator.navigate(AppDestination.Settings()) },
                onAccountsClick = { navigator.navigate(AppDestination.Accounts) },
                onLocalFollowsClick = { navigator.navigate(AppDestination.LocalFollows) },
                onViewUser = navigator::navigateToUser,
                onViewFeed = onViewFeed,
                onSearch = {
                    navigator.navigateToSearch(null, null, null)
                },
                onOpenLink = onOpenLink,
                onCopyText = navigator::navigateToCopyText,
                onViewApp = navigator::navigateToApp,
                onLogin = {
                    navigator.navigate(AppDestination.Login)
                },
                onCheckUpdate = { data ->
                    navigator.navigate(AppDestination.Updates(data.toList()))
                },
                onViewFFFList = { viewUid, viewType ->
                    navigator.navigateToFFFList(viewUid, viewType, null, null)
                },
                onReport = onReport,
                onViewNotice = navigator::navigateToNotice,
                onViewHistory = navigator::navigateToHistory,
            )
        }

        is AppDestination.Settings -> {
            // Restore old saved account-category routes directly into account management.
            if (route.section == "Accounts") {
                com.example.c001apk.compose.ui.accounts.AccountsScreen(
                    onBack = { navigator.popBackStack() },
                    onLogin = { navigator.navigate(AppDestination.Login) },
                )
            } else SettingsScreen(
                section = route.section,
                onSectionClick = { section ->
                    navigator.navigate(AppDestination.Settings(section))
                },
                onBackClick = { navigator.popBackStack() },
                onAccountsClick = { navigator.navigate(AppDestination.Accounts) },
                onLocalFollowsClick = { navigator.navigate(AppDestination.LocalFollows) },
                onHomeTabsClick = { navigator.navigate(AppDestination.TabEditor) },
                onParamsClick = { navigator.navigate(AppDestination.Params) },
                onDeviceImport = { navigator.navigate(AppDestination.DeviceImport) },
                onAboutClick = { navigator.navigate(AppDestination.About) },
                onViewBlackList = navigator::navigateToBlackList,
            )
        }

        AppDestination.LocalFollows -> {
            com.example.c001apk.compose.ui.follow.LocalFollowListScreen(
                onBack = { navigator.popBackStack() },
                onUser = navigator::navigateToUser,
            )
        }

        AppDestination.Accounts -> {
            com.example.c001apk.compose.ui.accounts.AccountsScreen(
                onBack = { navigator.popBackStack() },
                onLogin = { navigator.navigate(AppDestination.Login) },
            )
        }

        AppDestination.TabEditor -> {
            HomeTabEditorScreen(onBackClick = { navigator.popBackStack() })
        }

        AppDestination.DeviceImport -> {
            com.example.c001apk.compose.ui.settings.OfficialDeviceIdentityScreen(
                onBack = { navigator.popBackStack() },
            )
        }

        AppDestination.Params -> {
            ParamsScreen(
                onBackClick = navigator::popBackStack,
            )
        }

        AppDestination.About -> {
            AboutScreen(
                onBackClick = navigator::popBackStack,
                onLicenseClick = {
                    navigator.navigate(AppDestination.License)
                },
            )
        }

        AppDestination.License -> {
            LicenseScreen(
                onBackClick = navigator::popBackStack,
            )
        }

        is AppDestination.Feed -> {
            val id = route.id
            val isViewReply = route.isViewReply
            FeedScreen(
                onBackClick = navigator::popBackStack,
                id = id,
                isViewReply = isViewReply,
                onViewUser = navigator::navigateToUser,
                onViewFeed = onViewFeed,
                onOpenLink = onOpenLink,
                onCopyText = navigator::navigateToCopyText,
                onReport = onReport,
            )
        }

        is AppDestination.User -> {
            val uid = route.uid
            UserScreen(
                uid = uid,
                onBackClick = navigator::popBackStack,
                onViewUser = navigator::navigateToUser,
                onViewFeed = onViewFeed,
                onOpenLink = onOpenLink,
                onCopyText = navigator::navigateToCopyText,
                onSearch = { title, pageType, pageParam ->
                    navigator.navigateToSearch(title, pageType, pageParam)
                },
                onViewFFFList = { viewUid, viewType ->
                    navigator.navigateToFFFList(viewUid, viewType, null, null)
                },
                onReport = onReport,
                onPMUser = { viewUid, viewUsername ->
                    navigator.navigateToChat(
                        "${
                            min(
                                viewUid.toLongOrNull() ?: 0,
                                CookieUtil.uid.toLongOrNull() ?: 0,
                            )
                        }_${
                            max(
                                viewUid.toLongOrNull() ?: 0,
                                CookieUtil.uid.toLongOrNull() ?: 0,
                            )
                        }",
                        viewUid,
                        viewUsername,
                    )
                },
            )
        }

        is AppDestination.Search -> {
            val title = route.title
            val pageType = route.pageType
            val pageParam = route.pageParam
            TopBarScrollPage(active = false) {
                SearchScreen(
                    onBackClick = navigator::popBackStack,
                    title = title,
                    onSearch = { keyword ->
                        navigator.navigateToSearchResult(keyword, title, pageType, pageParam)
                    },
                )
            }
        }

        is AppDestination.SearchResult -> {
            var initialPage by rememberSaveable { mutableIntStateOf(0) }
            val keyword = route.keyword
            val title = route.title
            val pageType = route.pageType
            val pageParam = route.pageParam
            TopBarScrollPage(active = false) {
                SearchResultScreen(
                    onBackClick = navigator::popBackStack,
                    keyword = keyword,
                    title = title,
                    pageType = pageType,
                    pageParam = pageParam,
                    onViewUser = navigator::navigateToUser,
                    onViewFeed = onViewFeed,
                    onOpenLink = onOpenLink,
                    onCopyText = navigator::navigateToCopyText,
                    initialPage = initialPage,
                    updateInitPage = { index ->
                        initialPage = index
                    },
                    onReport = onReport,
                )
            }
        }

        is AppDestination.CopyText -> {
            val text = route.text.orEmpty()
            CopyTextScreen(text = text)
        }

        is AppDestination.Topic -> {
            val tag = route.tag
            val id = route.id
            TopicScreen(
                onBackClick = navigator::popBackStack,
                tag = tag,
                id = id,
                onViewUser = navigator::navigateToUser,
                onViewFeed = onViewFeed,
                onOpenLink = onOpenLink,
                onCopyText = navigator::navigateToCopyText,
                onSearch = { title, pageType, pageParam ->
                    navigator.navigateToSearch(title, pageType, pageParam)
                },
                onReport = onReport,
            )
        }

        is AppDestination.WebView -> {
            val url = route.url
            val isLogin = route.isLogin
            WebViewScreen(
                onBackClick = {
                    if (navigator.currentDestination == route) {
                        navigator.popBackStack()
                    }
                },
                url = url,
                isLogin = isLogin,
            )
        }

        is AppDestination.App -> {
            val packageName = route.packageName
            AppScreen(
                onBackClick = navigator::popBackStack,
                packageName = packageName,
                onViewUser = navigator::navigateToUser,
                onViewFeed = onViewFeed,
                onOpenLink = onOpenLink,
                onCopyText = navigator::navigateToCopyText,
                onSearch = { title, pageType, pageParam ->
                    navigator.navigateToSearch(title, pageType, pageParam)
                },
                onReport = onReport,
            )
        }

        AppDestination.Login -> {
            LoginScreen(
                onBackClick = {
                    if (navigator.currentDestination == AppDestination.Login) {
                        navigator.popBackStack()
                    }
                },
                onWebLogin = {
                    navigator.navigateToWebView(url = URL_LOGIN, isLogin = true)
                },
            )
        }

        is AppDestination.Carousel -> {
            val url = route.url
            val title = route.title
            CarouselScreen(
                onBackClick = navigator::popBackStack,
                url = url,
                title = title,
                onViewUser = navigator::navigateToUser,
                onViewFeed = onViewFeed,
                onOpenLink = onOpenLink,
                onCopyText = navigator::navigateToCopyText,
                onReport = onReport,
            )
        }

        is AppDestination.Event -> {
            val id = route.id
            EventScreen(
                id = id,
                onBackClick = navigator::popBackStack,
                onViewUser = navigator::navigateToUser,
                onOpenTab = onOpenLink,
                onRegister = context::openInBrowser,
            )
        }

        is AppDestination.Updates -> {
            val data = route.items
            AppUpdateScreen(
                onBackClick = navigator::popBackStack,
                data = data,
                onViewApp = navigator::navigateToApp,
            )
        }

        is AppDestination.FFFList -> {
            val uid = route.uid
            val type = route.type
            val id = route.id
            val title = route.title
            FFFListScreen(
                onBackClick = navigator::popBackStack,
                id = id,
                title = title,
                uid = uid,
                type = type,
                onViewUser = navigator::navigateToUser,
                onViewFeed = onViewFeed,
                onOpenLink = onOpenLink,
                onCopyText = navigator::navigateToCopyText,
                onReport = onReport,
                onViewFFFList = navigator::navigateToFFFList,
            )
        }

        is AppDestination.Dyh -> {
            val id = route.id
            val title = route.title
            DyhScreen(
                onBackClick = navigator::popBackStack,
                id = id,
                title = title,
                onViewUser = navigator::navigateToUser,
                onViewFeed = onViewFeed,
                onOpenLink = onOpenLink,
                onCopyText = navigator::navigateToCopyText,
                onReport = onReport,
            )
        }

        is AppDestination.CoolPic -> {
            val title = route.title
            CoolPicScreen(
                onBackClick = navigator::popBackStack,
                title = title,
                onViewUser = navigator::navigateToUser,
                onViewFeed = onViewFeed,
                onOpenLink = onOpenLink,
                onCopyText = navigator::navigateToCopyText,
                onReport = onReport,
            )
        }

        is AppDestination.Notice -> {
            val type = route.type
            NoticeScreen(
                onBackClick = navigator::popBackStack,
                type = type,
                onViewUser = navigator::navigateToUser,
                onViewFeed = onViewFeed,
                onOpenLink = onOpenLink,
                onCopyText = navigator::navigateToCopyText,
                onReport = onReport,
                onViewChat = navigator::navigateToChat,
            )
        }

        is AppDestination.BlackList -> {
            val type = route.type
            BlackListScreen(
                onBackClick = navigator::popBackStack,
                type = type,
                onViewUser = navigator::navigateToUser,
                onViewTopic = { tag ->
                    navigator.navigateToTopic(tag, null)
                },
            )
        }

        is AppDestination.History -> {
            val type = route.type
            HistoryScreen(
                onBackClick = navigator::popBackStack,
                type = type,
                onViewUser = navigator::navigateToUser,
                onReport = onReport,
                onOpenLink = onOpenLink,
                onViewFeed = onViewFeed,
                onCopyText = navigator::navigateToCopyText,
            )
        }

        is AppDestination.Chat -> {
            val ukey = route.ukey
            val uid = route.uid
            val username = route.username
            ChatScreen(
                onBackClick = navigator::popBackStack,
                ukey = ukey,
                uid = uid,
                username = username,
                onViewUser = navigator::navigateToUser,
                onReport = onReport,
                onOpenLink = onOpenLink,
            )
        }

        is AppDestination.Collection -> {
            val id = route.id
            CollectionScreen(
                onBackClick = navigator::popBackStack,
                id = id,
                onViewUser = navigator::navigateToUser,
                onViewFeed = onViewFeed,
                onOpenLink = onOpenLink,
                onCopyText = navigator::navigateToCopyText,
                onReport = onReport,
            )
        }
        AppDestination.DetailPlaceholder -> Unit
    }
}
