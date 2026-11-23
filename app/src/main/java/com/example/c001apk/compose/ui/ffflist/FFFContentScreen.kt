package com.example.c001apk.compose.ui.ffflist

import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import com.example.c001apk.compose.ui.component.LocalDialogClose
import com.example.c001apk.compose.ui.component.MiuixAlertDialog
import com.example.c001apk.compose.ui.component.MiuixDialogButton
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.squircle.squircleClip
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.constant.Constants.EMPTY_STRING
import com.example.c001apk.compose.ui.component.CommonScreen
import com.example.c001apk.compose.util.ReportType
import com.example.c001apk.compose.util.makeToast
import top.yukonga.miuix.kmp.basic.Text

/** Created by bggRGjQaUbCoE on 2024/6/12 */
@Composable
fun FFFContentScreen(
    uid: String?,
    id: String?,
    type: String,
    paddingValues: PaddingValues,
    refreshState: Boolean?,
    resetRefreshState: () -> Unit,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onReport: (String, ReportType) -> Unit,
    onViewFFFList: (String?, String, String?, String?) -> Unit,
) {

    val viewModel =
        hiltViewModel<FFFContentViewModel, FFFContentViewModel.ViewModelFactory>(
            key = id + uid + type,
        ) { factory ->
            factory.create(
                url =
                    when (type) {
                        FFFListType.FEED.name -> "/v6/user/feedList?showAnonymous=0&isIncludeTop=1"
                        FFFListType.FOLLOW.name,
                        FFFListType.USER_FOLLOW.name -> "/v6/user/followList"
                        FFFListType.APK.name -> "/v6/user/apkFollowList"
                        FFFListType.FAN.name -> "/v6/user/fansList"
                        FFFListType.RECENT.name -> "/v6/user/recentHistoryList"
                        FFFListType.LIKE.name -> "/v6/user/likeList"
                        FFFListType.REPLY.name -> "/v6/user/replyList"
                        FFFListType.REPLYME.name -> "/v6/user/replyToMeList"
                        FFFListType.COLLECTION.name -> "/v6/collection/list"
                        FFFListType.COLLECTION_ITEM.name -> "/v6/collection/itemList"
                        else -> EMPTY_STRING
                    },
                uid = uid,
                id = id,
                showDefault = if (type.contains("COLLECTION")) 0 else null,
            )
        }

    var showRecentDialog by remember { mutableStateOf(false) }

    CommonScreen(
        viewModel = viewModel,
        refreshState = refreshState,
        resetRefreshState = resetRefreshState,
        paddingValues = paddingValues,
        needTopPadding = true,
        onViewUser = onViewUser,
        onViewFeed = onViewFeed,
        onOpenLink = onOpenLink,
        onCopyText = onCopyText,
        onReport = onReport,
        onViewFFFList = onViewFFFList,
        onHandleRecent = { actionId, targetId, targetType, isTop ->
            viewModel.actionId = actionId
            viewModel.targetId = targetId
            viewModel.targetType = targetType
            viewModel.isTop = isTop
            showRecentDialog = true
        },
    )

    val context = LocalContext.current
    viewModel.toastText?.let {
        viewModel.resetToastText()
        context.makeToast(it)
    }

    if (showRecentDialog) {
        MiuixAlertDialog(
            onDismissRequest = { showRecentDialog = false },
            title = { Text(stringResource(R.string.profile_recent)) },
            text = {
                val close = checkNotNull(LocalDialogClose.current)
                Card {
                    listOf(
                        (if (viewModel.isTop == 1) R.string.action_unpin else R.string.action_pin) to FFFContentViewModel.ActionType.TOP,
                        R.string.menu_delete to FFFContentViewModel.ActionType.DELETE,
                        R.string.action_clear_all to FFFContentViewModel.ActionType.DELETE_ALL,
                    ).forEach { (label, action) ->
                        BasicComponent(
                            title = stringResource(label),
                            modifier = Modifier.squircleClip(16.dp),
                            onClick = { close { showRecentDialog = false; viewModel.onHandleRecentHistory(action) } },
                        )
                    }
                }
            },
            confirmButton = {
                MiuixDialogButton(onClick = { showRecentDialog = false }, primary = false) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}
