package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import com.example.c001apk.compose.ui.ffflist.FFFListType
import com.example.c001apk.compose.ui.history.HistoryType
import com.example.c001apk.compose.util.CookieUtil
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteArrowLeftUpLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteClockLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteFolderLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteHeartLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteLocation2Line
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteStarLine

@Composable
fun MessageWidgetCard(
    modifier: Modifier = Modifier,
    onViewFFFList: (String?, String) -> Unit,
    onViewHistory: (String) -> Unit,
) {
    val tools = listOf(
        ProfileTool("本地收藏", MingCuteFolderLine, FFFListType.FAV),
        ProfileTool("浏览历史", MingCuteClockLine, FFFListType.HISTORY),
        ProfileTool("我的常去", MingCuteLocation2Line, FFFListType.RECENT),
        ProfileTool("我的收藏", MingCuteStarLine, FFFListType.COLLECTION),
        ProfileTool("我的赞", MingCuteHeartLine, FFFListType.LIKE),
        ProfileTool("我的回复", MingCuteArrowLeftUpLine, FFFListType.REPLY),
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ProfileLayoutDefaults.ItemSpacing),
    ) {
        tools.chunked(2).forEach { rowTools ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ProfileLayoutDefaults.ItemSpacing),
            ) {
                rowTools.forEach { tool ->
                    Card(Modifier.weight(1f)) {
                        BasicComponent(
                            modifier = Modifier.testTag("profile_tool_${tool.destination.name}"),
                            title = tool.title,
                            role = Role.Button,
                            startAction = {
                                Row {
                                    Icon(
                                        tool.icon,
                                        null,
                                        Modifier.size(ProfileLayoutDefaults.LeadingIconSize),
                                        tint = MiuixTheme.colorScheme.onSurface,
                                    )
                                    Spacer(Modifier.width(ProfileLayoutDefaults.LeadingIconSpacing))
                                }
                            },
                            onClick = {
                                when (tool.destination) {
                                    FFFListType.FAV -> onViewHistory(HistoryType.FAV.name)
                                    FFFListType.HISTORY -> onViewHistory(HistoryType.HISTORY.name)
                                    FFFListType.COLLECTION -> {
                                        if (CookieUtil.isLogin) onViewFFFList(null, tool.destination.name)
                                    }
                                    else -> {
                                        if (CookieUtil.isLogin) {
                                            onViewFFFList(CookieUtil.uid, tool.destination.name)
                                        }
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

private data class ProfileTool(
    val title: String,
    val icon: ImageVector,
    val destination: FFFListType,
)
