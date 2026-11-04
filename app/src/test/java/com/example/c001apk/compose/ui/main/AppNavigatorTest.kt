package com.example.c001apk.compose.ui.main

import com.example.c001apk.compose.logic.model.UpdateCheckItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import top.yukonga.miuix.kmp.nav.core.NavController
import top.yukonga.miuix.kmp.nav.core.navBackStackOf

class AppNavigatorTest {
    private fun navigator(root: AppDestination = AppDestination.Main) =
        AppNavigator(NavController(navBackStackOf(AppNavEntry(root))))

    @Test
    fun allDestinationsRoundTripWithStableVisitIdsAndUnescapedArguments() {
        val special = "中文 / null ?q=a&b=%23 #片段 user_tag:%s%d C++ 100% %20 %"
        val destinations = listOf(
            AppDestination.Main, AppDestination.DetailPlaceholder, AppDestination.Settings(special),
            AppDestination.LocalFollows, AppDestination.Accounts, AppDestination.TabEditor, AppDestination.NavigationOrder,
            AppDestination.DeviceImport, AppDestination.Params, AppDestination.About, AppDestination.License,
            AppDestination.ContentFilters, AppDestination.ContentRule(null), AppDestination.ContentRule(special),
            AppDestination.TwilightRules,
            AppDestination.Feed("123", true), AppDestination.User("456"),
            AppDestination.Search(null, special, "null"), AppDestination.SearchResult(special, null, "null", special),
            AppDestination.CopyText(special), AppDestination.Topic(special, null),
            AppDestination.WebView("https://example.com/a/b?q=$special", true), AppDestination.App("com.example.test"),
            AppDestination.Login, AppDestination.Carousel(special, special), AppDestination.Event("12"),
            AppDestination.Updates(listOf(UpdateCheckItem("packageName", special))),
            AppDestination.FFFList(null, special, "null", null), AppDestination.Dyh("12", special),
            AppDestination.CoolPic(special), AppDestination.Notice(special), AppDestination.BlackList(special),
            AppDestination.History(special), AppDestination.Chat("1_2", "2", special), AppDestination.Collection("12"),
        )
        val visits = destinations.map { AppNavEntry(it) }
        val restored = Json.decodeFromString<List<AppNavEntry>>(Json.encodeToString(visits))
        assertEquals(visits, restored)
        assertEquals(visits.size, restored.map { it.id }.toSet().size)
    }

    @Test
    fun repeatedVisitsHaveDifferentStateButDoubleTapDoesNotPushTwice() {
        val nav = navigator()
        nav.navigateToFeed("12")
        val firstVisit = nav.currentEntry
        nav.navigateToFeed("12")
        assertEquals(2, nav.controller.backStack.size)
        nav.navigateToUser("34")
        nav.navigateToFeed("12")
        assertEquals(firstVisit.destination, nav.currentDestination)
        assertNotEquals(firstVisit.id, nav.currentEntry.id)
    }

    @Test
    fun popKeepsRootAndAccountResetReplacesItsIdentity() {
        val nav = navigator()
        val originalRoot = nav.currentEntry.id
        assertFalse(nav.popBackStack())
        nav.navigate(AppDestination.Settings("account"))
        nav.navigate(AppDestination.Accounts)
        assertTrue(nav.popBackStack())
        assertEquals(AppDestination.Settings("account"), nav.currentDestination)
        nav.reset()
        assertEquals(AppDestination.Main, nav.currentDestination)
        assertNotEquals(originalRoot, nav.currentEntry.id)
        assertFalse(nav.canGoBack)
    }

    @Test
    fun detailReplacementKeepsItsPlaceholderAndOneBackClosesIt() {
        val nav = navigator(AppDestination.DetailPlaceholder)
        val root = nav.currentEntry
        nav.navigateToFeed("12")
        nav.replace(AppDestination.Feed("34", true))
        assertEquals(2, nav.controller.backStack.size)
        assertEquals(root, nav.controller.backStack.first())
        assertEquals(AppDestination.Feed("34", true), nav.currentDestination)
        assertTrue(nav.popBackStack())
        assertEquals(root, nav.currentEntry)
    }

    @Test
    fun readingHistoryPreservesPreviousVisitAndBoundsOldEntries() {
        val nav = navigator(AppDestination.DetailPlaceholder)
        val root = nav.currentEntry
        nav.navigateReading(AppDestination.Feed("1", false))
        val first = nav.currentEntry
        nav.navigateReading(AppDestination.Feed("2", false))
        nav.popBackStack()
        assertEquals(first, nav.currentEntry)
        for (id in 2..12) nav.navigateReading(AppDestination.Feed(id.toString(), false))
        assertEquals(9, nav.controller.backStack.size)
        assertEquals(root, nav.controller.backStack.first())
        assertEquals(AppDestination.Feed("5", false), (nav.controller.backStack[1] as AppNavEntry).destination)
        nav.navigateReading(AppDestination.Feed("12", false))
        assertEquals(9, nav.controller.backStack.size)
        repeat(8) { assertTrue(nav.popBackStack()) }
        assertEquals(root, nav.currentEntry)
    }

    @Test
    fun editedResultQueryReturnsToItsSearchEntryWithoutReplacingVisitIdentity() {
        val nav = navigator()
        nav.navigateToSearch("THANOX", "tag", "THANOX")
        val searchId = nav.currentEntry.id
        nav.updateSearchInput(searchId, "后台")
        nav.navigateToSearchResult("后台", "THANOX", "tag", "THANOX")
        val resultId = nav.currentEntry.id
        nav.updateSearchInput(resultId, "后台运行 C++ & 100%")
        assertEquals(resultId, nav.currentEntry.id)
        nav.popBackStack()
        assertEquals(searchId, nav.currentEntry.id)
        assertEquals("后台运行 C++ & 100%", (nav.currentDestination as AppDestination.Search).query)
        val restored = Json.decodeFromString<AppNavEntry>(Json.encodeToString(nav.currentEntry))
        assertEquals(nav.currentEntry, restored)
    }

    @Test
    fun searchDraftClearingAndHistorySelectionUseTheSameState() {
        val nav = navigator()
        nav.navigateToSearch(null, null, null)
        val searchId = nav.currentEntry.id
        nav.updateSearchInput(searchId, "历史关键词")
        nav.navigateToSearchResult("历史关键词", null, null, null)
        nav.updateSearchInput(nav.currentEntry.id, "")
        nav.popBackStack()
        assertEquals("", (nav.currentDestination as AppDestination.Search).query)
        nav.updateSearchInput(searchId, "下一次搜索")
        assertEquals("下一次搜索", (nav.currentDestination as AppDestination.Search).query)
    }

    @Test
    fun resultUpdatesDoNotLeakAcrossScopesOrAfterNavigationReset() {
        val nav = navigator()
        nav.navigateToSearch("话题 A", "tag", "A")
        nav.updateSearchInput(nav.currentEntry.id, "原关键词")
        nav.navigateToSearchResult("其他搜索", "话题 B", "tag", "B")
        val resultId = nav.currentEntry.id
        nav.updateSearchInput(resultId, "不能覆盖 A")
        nav.popBackStack()
        assertEquals("原关键词", (nav.currentDestination as AppDestination.Search).query)
        nav.reset()
        nav.updateSearchInput(resultId, "已过期的回调")
        assertEquals(AppDestination.Main, nav.currentDestination)
    }
}
