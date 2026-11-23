package com.example.c001apk.compose.ui.main

import com.example.c001apk.compose.R
import android.content.Context
import com.example.c001apk.compose.constant.Constants.PREFIX_CAROUSEL
import com.example.c001apk.compose.constant.Constants.PREFIX_CAROUSEL1
import com.example.c001apk.compose.ui.ffflist.FFFListType
import com.example.c001apk.compose.util.CookieUtil
import com.example.c001apk.compose.util.CookieUtil.openInBrowser
import com.example.c001apk.compose.util.makeToast
import com.example.c001apk.compose.util.openInBrowser

fun AppNavigator.onOpenLink(
    context: Context,
    url: String,
    title: String? = null,
    onSearch: ((String) -> Unit)? = null,
    onViewFeed: ((String, Boolean) -> Unit)? = null,
) {
    val link = com.example.c001apk.compose.util.CoolapkLink.parse(url)
    val parts = link?.segments.orEmpty()
    val target = parts.getOrNull(1).orEmpty()
    fun unsupported() {
        context.makeToast(context.getString(R.string.link_unsupported))
    }
    when {
        link == null -> {
            if (com.example.c001apk.compose.util.CoolapkLink.external(url)) {
                if (openInBrowser) context.openInBrowser(url) else navigateToWebView(url)
            } else unsupported()
        }
        parts.firstOrNull() == "feed" -> {
            if (parts.size != 2 || !target.matches(Regex("[0-9]+"))) unsupported()
            else if (onViewFeed != null) onViewFeed(target, link.query.containsKey("rid"))
            else navigateToFeed(target, link.query.containsKey("rid"))
        }
        parts.firstOrNull() == "event" -> {
            if (parts.size == 2 && target.matches(Regex("[0-9]+"))) navigateToEvent(target)
            else unsupported()
        }
        parts.firstOrNull() == "u" ->
            if (target.isNotBlank()) navigateToUser(target) else unsupported()
        parts.firstOrNull() == "t" -> {
            if (target.isBlank()) unsupported()
            else if (link.query["type"] == "8") navigateToCoolPic(target)
            else navigateToTopic(tag = target, id = null)
        }
        parts.firstOrNull() == "product" ->
            if (target.isNotBlank()) navigateToTopic(tag = null, id = target) else unsupported()
        parts.firstOrNull() in listOf("apk", "game") ->
            if (target.isNotBlank()) navigateToApp(target) else unsupported()
        link.path.startsWith(PREFIX_CAROUSEL) ->
            link.query["url"]?.let { navigateToCarousel(it, title.orEmpty()) } ?: unsupported()
        link.path.startsWith(PREFIX_CAROUSEL1) ->
            navigateToCarousel(link.path.removePrefix("#"), title.orEmpty())
        parts.firstOrNull() == "user" && target == "myFollowList" ->
            navigateToFFFList(CookieUtil.uid, FFFListType.USER_FOLLOW.name, null, null)
        parts.firstOrNull() == "dyh" ->
            if (target.isNotBlank()) navigateToDyh(target, title.orEmpty()) else unsupported()
        parts.firstOrNull() == "collection" ->
            if (target.isNotBlank()) navigateToCollection(target) else unsupported()
        parts.firstOrNull() == "search" ||
            (parts.firstOrNull() == "com.coolapk.market" && target == "search") -> {
            val keyword = link.query["searchValue"].orEmpty()
            if (keyword.isBlank()) navigateToSearch(null, null, null)
            else {
                onSearch?.invoke(keyword)
                navigateToSearchResult(keyword, null, null, null)
            }
        }
        com.example.c001apk.compose.util.CoolapkLink.external(url) -> {
            if (openInBrowser) context.openInBrowser(url) else navigateToWebView(url)
        }
        else -> unsupported()
    }
}


fun AppNavigator.navigateToCopyText(text: String?) = navigate(AppDestination.CopyText(text))
fun AppNavigator.navigateToFeed(id: String, isViewReply: Boolean = false) =
    navigate(AppDestination.Feed(id, isViewReply))
fun AppNavigator.navigateToUser(uid: String) = navigate(AppDestination.User(uid))
fun AppNavigator.navigateToTopic(tag: String?, id: String?) = navigate(AppDestination.Topic(tag, id))
fun AppNavigator.navigateToWebView(url: String, isLogin: Boolean = false) =
    navigate(AppDestination.WebView(url, isLogin))
fun AppNavigator.navigateToSearch(title: String?, pageType: String?, pageParam: String?) =
    navigate(AppDestination.Search(title, pageType, pageParam))
fun AppNavigator.navigateToSearchResult(
    keyword: String, title: String?, pageType: String?, pageParam: String?,
) = navigate(AppDestination.SearchResult(keyword, title, pageType, pageParam))
fun AppNavigator.navigateToApp(packageName: String) = navigate(AppDestination.App(packageName))
fun AppNavigator.navigateToCarousel(url: String, title: String) = navigate(AppDestination.Carousel(url, title))
fun AppNavigator.navigateToEvent(id: String) = navigate(AppDestination.Event(id))
fun AppNavigator.navigateToFFFList(uid: String?, type: String, id: String?, title: String?) =
    navigate(AppDestination.FFFList(uid, type, id, title))
fun AppNavigator.navigateToDyh(id: String, title: String) = navigate(AppDestination.Dyh(id, title))
fun AppNavigator.navigateToCoolPic(title: String) = navigate(AppDestination.CoolPic(title))
fun AppNavigator.navigateToNotice(type: String) = navigate(AppDestination.Notice(type))
fun AppNavigator.navigateToBlackList(type: String) = navigate(AppDestination.BlackList(type))
fun AppNavigator.navigateToHistory(type: String) = navigate(AppDestination.History(type))
fun AppNavigator.navigateToChat(ukey: String, uid: String, username: String) =
    navigate(AppDestination.Chat(ukey, uid, username))
fun AppNavigator.navigateToCollection(id: String) = navigate(AppDestination.Collection(id))
