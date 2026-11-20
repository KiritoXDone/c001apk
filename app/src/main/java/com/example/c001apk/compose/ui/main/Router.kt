package com.example.c001apk.compose.ui.main

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.c001apk.compose.R
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ContactsCircle
import top.yukonga.miuix.kmp.icon.extended.FavoritesFill
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Settings

/** Created by bggRGjQaUbCoE on 2024/5/30 */
sealed class Router(
    val name: String,
    @param:StringRes val stringId: Int? = null,
    val unselectedIcon: ImageVector? = null,
    val selectedIcon: ImageVector? = null,
) {

    data object MAIN :
        Router(
            name = "MAIN",
        )

    data object HOME :
        Router(
            name = "HOME",
            stringId = R.string.home,
            unselectedIcon = MiuixIcons.Home,
            selectedIcon = MiuixIcons.Home,
        )

    data object FOLLOW :
        Router(
            name = "FOLLOW",
            stringId = R.string.follow,
            unselectedIcon = MiuixIcons.FavoritesFill,
            selectedIcon = MiuixIcons.FavoritesFill,
        )

    data object PROFILE :
        Router(
            name = "PROFILE",
            stringId = R.string.profile,
            unselectedIcon = MiuixIcons.ContactsCircle,
            selectedIcon = MiuixIcons.ContactsCircle,
        )

    data object SETTINGS :
        Router(
            name = "SETTINGS",
            stringId = R.string.settings,
            unselectedIcon = MiuixIcons.Settings,
            selectedIcon = MiuixIcons.Settings,
        )

    data object LOCAL_FOLLOWS : Router(name = "LOCAL_FOLLOWS")

    data object ACCOUNTS : Router(name = "ACCOUNTS")

    data object DEVICE_IMPORT : Router(name = "DEVICE_IMPORT")

    data object PARAMS : Router(name = "PARAMS")

    data object ABOUT : Router(name = "ABOUT")

    data object LICENSE : Router(name = "LICENSE")

    data object BLACKLIST : Router(name = "BLACKLIST")

    data object SEARCH : Router(name = "SEARCH")

    data object SEARCHRESULT : Router(name = "SEARCHRESULT")

    data object TAB : Router(name = "TAB")

    data object FEED : Router(name = "FEED")

    data object USER : Router(name = "USER")

    data object TOPIC : Router(name = "TOPIC")

    data object COPY : Router(name = "COPY")

    data object WEBVIEW : Router(name = "WEBVIEW")

    data object APP : Router(name = "APP")

    data object LOGIN : Router(name = "LOGIN")

    data object CAROUSEL : Router(name = "CAROUSEL")

    data object EVENT : Router(name = "EVENT")

    data object UPDATE : Router(name = "UPDATE")

    data object FFFLIST : Router(name = "FFFLIST")

    data object DYH : Router(name = "DYH")

    data object COOLPIC : Router(name = "COOLPIC")

    data object NOTICE : Router(name = "NOTICE")

    data object HISTORY : Router(name = "HISTORY")

    data object CHAT : Router(name = "CHAT")

    data object COLLECTION : Router(name = "COLLECTION")
}
