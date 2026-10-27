package com.example.c001apk.compose.ui.webview

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.R
import com.example.c001apk.compose.constant.Constants.EMPTY_STRING
import com.example.c001apk.compose.constant.Constants.WEB_LOGIN_FAILED
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.MiuixHeader as TopAppBar
import com.example.c001apk.compose.ui.component.MiuixMenu as DropdownMenu
import com.example.c001apk.compose.ui.component.MiuixMenuItem as DropdownMenuItem
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.MoreMenuButton
import com.example.c001apk.compose.ui.component.TopBarScrollEffect
import com.example.c001apk.compose.ui.component.WebView
import com.example.c001apk.compose.util.makeToast
import com.example.c001apk.compose.util.openInBrowser
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.SnackbarResult
import top.yukonga.miuix.kmp.basic.Text

/** Created by bggRGjQaUbCoE on 2024/6/9 */
enum class ActionType {
    REFRESH,
    COPY,
    OPEN,
    CLEAN,
    SCROLL_TO_TOP,
    NONE,
}

@Composable
fun WebViewScreen(
    onBackClick: () -> Unit,
    url: String,
    isLogin: Boolean = false,
) {

    val viewModel = hiltViewModel<WebViewViewModel>(key = url)
    val prefs = LocalUserPreferences.current
    val initialAccountRevision = remember { prefs.accountRevision }
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    var title by remember { mutableStateOf(EMPTY_STRING) }
    var dropdownMenuExpanded by remember { mutableStateOf(false) }

    var progress by remember { mutableFloatStateOf(0.0f) }
    val animatedProgress by
        animateFloatAsState(
            targetValue = progress,
            animationSpec =
                top.yukonga.miuix.kmp.anim.folmeSpring(damping = 0.95f, response = 0.4f),
            label = EMPTY_STRING,
        )
    val snackbarHostState = remember(::SnackbarHostState)
    val scope = rememberCoroutineScope()

    var actionType by remember { mutableStateOf(ActionType.NONE) }
    var onBack by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        blurBars = false,
        topBar = {
            TopAppBar(
                windowInsets =
                    WindowInsets.systemBars.only(WindowInsetsSides.Start + WindowInsetsSides.Top),
                navigationIcon = {
                    BackButton { onBackClick() }
                },
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                actions = {
                    Box {
                        MoreMenuButton { dropdownMenuExpanded = true }
                        DropdownMenu(
                            expanded = dropdownMenuExpanded,
                            onDismissRequest = { dropdownMenuExpanded = false },
                        ) {
                            listOf(
                                    stringResource(id = R.string.menu_refresh),
                                    "复制",
                                    stringResource(id = R.string.menu_open_in_browser),
                                    stringResource(id = R.string.menu_clean_caches),
                                )
                                .forEachIndexed { index, menu ->
                                    DropdownMenuItem(
                                        text = { Text(menu) },
                                        onClick = {
                                            dropdownMenuExpanded = false
                                            actionType =
                                                when (index) {
                                                    0 -> ActionType.REFRESH
                                                    1 -> ActionType.COPY
                                                    2 -> ActionType.OPEN
                                                    3 -> ActionType.CLEAN
                                                    else -> ActionType.NONE
                                                }
                                        },
                                    )
                                }
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        TopBarScrollEffect { actionType = ActionType.SCROLL_TO_TOP }
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding(),
                        start = paddingValues.calculateLeftPadding(layoutDirection),
                    ),
        ) {
            WebView(
                url = url,
                isLogin = isLogin,
                onFinishLogin = { cookie ->
                    if (cookie.isNotEmpty()) {
                        val split = cookie.split(";")
                        val uid =
                            split
                                .find { it.contains("uid=") }
                                ?.replace("uid=", EMPTY_STRING)
                                ?.trim()
                        val username =
                            split
                                .find { it.contains("username=") }
                                ?.replace("username=", EMPTY_STRING)
                                ?.trim()
                        val token =
                            split
                                .find { it.contains("token=") }
                                ?.replace("token=", EMPTY_STRING)
                                ?.trim()
                        if (
                            !uid.isNullOrEmpty() &&
                                !username.isNullOrEmpty() &&
                                !token.isNullOrEmpty()
                        ) {

                            viewModel.setIsLogin(uid, username, token)
                        } else {
                            context.makeToast(WEB_LOGIN_FAILED)
                        }
                    } else {
                        context.makeToast(WEB_LOGIN_FAILED)
                    }
                },
                actionType = actionType,
                resetActionType = {
                    actionType = ActionType.NONE
                },
                onFinish = onBackClick,
                onBack = onBack,
                onBackReset = {
                    onBack = false
                },
                onUpdateProgress = { progress = it },
                onUpdateTitle = { title = it },
                onShowSnackbar = { requestUrl ->
                    scope.launch {
                        val result =
                            snackbarHostState.showSnackbar(
                                message = "当前网页将要打开外部链接，是否打开",
                                actionLabel = "打开",
                                withDismissAction = true,
                            )
                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                context.openInBrowser(requestUrl)
                            }

                            SnackbarResult.Dismissed -> {}
                        }
                    }
                },
            )
            // Overlay progress without changing the webpage's viewport on every load.
            AnimatedVisibility(
                visible = progress < 1.0f,
                modifier = Modifier.align(Alignment.TopCenter),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = animatedProgress,
                )
            }
        }
    }

    LaunchedEffect(isLogin, prefs.accountRevision) {
        if (isLogin && prefs.accountRevision != initialAccountRevision) onBackClick()
    }

    viewModel.error?.let { Text(it) }

    BackHandler {
        onBack = true
    }
}
