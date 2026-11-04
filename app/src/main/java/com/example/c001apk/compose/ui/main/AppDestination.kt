package com.example.c001apk.compose.ui.main

import com.example.c001apk.compose.logic.model.UpdateCheckItem
import java.util.UUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.yukonga.miuix.kmp.nav.core.NavKey

/** A visit has its own identity, even when the same destination occurs twice in the stack. */
@Serializable
data class AppNavEntry(
    val destination: AppDestination,
    val id: String = UUID.randomUUID().toString(),
) : NavKey

@Serializable
sealed interface AppDestination {
    @Serializable data object Main : AppDestination
    @Serializable data object DetailPlaceholder : AppDestination
    @Serializable data class Settings(val section: String? = null) : AppDestination
    @Serializable data object ContentFilters : AppDestination
    @Serializable data object TwilightRules : AppDestination
    @Serializable data class ContentRule(val id: String? = null) : AppDestination
    @Serializable data object LocalFollows : AppDestination
    @Serializable data object Accounts : AppDestination
    @Serializable data object TabEditor : AppDestination
    @Serializable data object NavigationOrder : AppDestination
    @Serializable data object DeviceImport : AppDestination
    @Serializable data object Params : AppDestination
    @Serializable data object About : AppDestination
    @Serializable data object License : AppDestination
    @Serializable data class Feed(val id: String, val isViewReply: Boolean = false) : AppDestination
    @Serializable data class User(val uid: String) : AppDestination
    @Serializable data class Search(
        val title: String?, val pageType: String?, val pageParam: String?,
        val query: String = "",
    ) : AppDestination
    @Serializable data class SearchResult(
        val keyword: String, val title: String?, val pageType: String?, val pageParam: String?,
    ) : AppDestination
    @Serializable data class CopyText(val text: String?) : AppDestination
    @Serializable data class Topic(val tag: String?, val id: String?) : AppDestination
    @Serializable data class WebView(val url: String, val isLogin: Boolean = false) : AppDestination
    @Serializable data class App(val packageName: String) : AppDestination
    @Serializable data object Login : AppDestination
    @Serializable data class Carousel(val url: String, val title: String) : AppDestination
    @Serializable data class Event(val id: String) : AppDestination
    @Serializable data class Updates(val items: List<UpdateCheckItem>) : AppDestination
    @Serializable data class FFFList(
        val uid: String?, @SerialName("contentType") val type: String, val id: String?, val title: String?,
    ) : AppDestination
    @Serializable data class Dyh(val id: String, val title: String) : AppDestination
    @Serializable data class CoolPic(val title: String) : AppDestination
    @Serializable data class Notice(@SerialName("contentType") val type: String) : AppDestination
    @Serializable data class BlackList(@SerialName("contentType") val type: String) : AppDestination
    @Serializable data class History(@SerialName("contentType") val type: String) : AppDestination
    @Serializable data class Chat(val ukey: String, val uid: String, val username: String) : AppDestination
    @Serializable data class Collection(val id: String) : AppDestination
}
