package com.example.c001apk.compose.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.logic.repository.UserPreferencesRepository
import com.example.c001apk.compose.ui.component.MiuixRootPopupHost
import com.example.c001apk.compose.ui.component.currentWindowWidthSizeClass
import com.example.c001apk.compose.ui.search.SearchViewModel
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import com.example.c001apk.compose.util.CookieUtil.apiVersion
import com.example.c001apk.compose.util.CookieUtil.contentScale
import com.example.c001apk.compose.util.CookieUtil.customHaptics
import com.example.c001apk.compose.util.CookieUtil.fontScale
import com.example.c001apk.compose.util.CookieUtil.hapticFeedback
import com.example.c001apk.compose.util.CookieUtil.hapticStrength
import com.example.c001apk.compose.util.CookieUtil.imageFilter
import com.example.c001apk.compose.util.CookieUtil.imageQuality
import com.example.c001apk.compose.util.CookieUtil.isDarkMode
import com.example.c001apk.compose.util.CookieUtil.materialYou
import com.example.c001apk.compose.util.CookieUtil.openInBrowser
import com.example.c001apk.compose.util.CookieUtil.paletteStyle
import com.example.c001apk.compose.util.CookieUtil.pureBlack
import com.example.c001apk.compose.util.CookieUtil.recordHistory
import com.example.c001apk.compose.util.CookieUtil.reverseHapticEffects
import com.example.c001apk.compose.util.CookieUtil.sdkInt
import com.example.c001apk.compose.util.CookieUtil.seedColor
import com.example.c001apk.compose.util.CookieUtil.showEmoji
import com.example.c001apk.compose.util.CookieUtil.showSquare
import com.example.c001apk.compose.util.CookieUtil.szlmId
import com.example.c001apk.compose.util.CookieUtil.themeType
import com.example.c001apk.compose.util.CookieUtil.userAgent
import com.example.c001apk.compose.util.CookieUtil.versionCode
import com.example.c001apk.compose.util.CookieUtil.versionName
import com.example.c001apk.compose.util.CookieUtil.xAppDevice
import com.example.c001apk.compose.util.makeToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Created by bggRGjQaUbCoE on 2024/5/29 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var navigator: AppNavigator
    private val viewModel by viewModels<MainViewModel>()
    private val searchViewModel by viewModels<SearchViewModel>()
    private var stableStatusBarInset: Int? = null
    private val pendingLinks = androidx.compose.runtime.mutableStateListOf<String>()

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        enqueueIntent(intent)
    }

    private fun enqueueIntent(intent: Intent) {
        val url =
            when (intent.action) {
                Intent.ACTION_VIEW -> intent.dataString
                Intent.ACTION_SEND ->
                    com.example.c001apk.compose.util.CoolapkLink.fromSharedText(
                        intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty(),
                    )
                else -> null
            } ?: return
        if (pendingLinks.lastOrNull() != url && pendingLinks.size < 16) pendingLinks.add(url)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArrayList("pending_links", ArrayList(pendingLinks))
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) enqueueIntent(intent)
        else pendingLinks.addAll(savedInstanceState.getStringArrayList("pending_links").orEmpty())
        enableEdgeToEdge()
        setupStableStatusBarInsets()

        // Restore the local account before navigation; network registration never holds the splash.
        val contentView = findViewById<View>(android.R.id.content)
        contentView.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (viewModel.startup.preparingAccount) return false
                    contentView.viewTreeObserver.removeOnPreDrawListener(this)
                    return true
                }
            },
        )

        val initialPreferences = runBlocking {
            userPreferencesRepository.data.first()
        }

        setContent {
            navigator = rememberAppNavigator()

            val userPreferences by
                userPreferencesRepository.data.collectAsStateWithLifecycle(
                    initialValue = initialPreferences,
                    lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current,
                )

            val preferences =
                if (userPreferences == null) {
                    return@setContent
                } else {
                    checkNotNull(userPreferences)
                }

            val currentId by rememberUpdatedState(preferences.szlmId)
            LaunchedEffect(viewModel) {
                lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.RESUMED) {
                    viewModel.startup.identityFailures.collect {
                        if (currentId.isBlank()) {
                            makeToast("数盟 ID 获取失败，请在设置中手动填写")
                        }
                    }
                }
            }

            LaunchedEffect(
                preferences.accountRevision,
                preferences.checkCount,
                preferences.checkCountPeriod,
            ) {
                if (preferences.checkCount && preferences.isLogin) {
                    lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.RESUMED) {
                        while (isActive) {
                            viewModel.refreshNotificationCounts()
                            delay(preferences.checkCountPeriod.coerceIn(1, 60) * 60_000L)
                        }
                    }
                }
            }
            CompositionLocalProvider(
                LocalUserPreferences provides preferences,
            ) {
                LaunchedEffect(preferences.xAppDevice) {
                    if (preferences.xAppDevice.isEmpty()) viewModel.regenerateParams()
                }

                com.example.c001apk.compose.util.CookieUtil.publishAccount(preferences)
                szlmId = preferences.szlmId
                xAppDevice = preferences.xAppDevice
                userAgent = preferences.userAgent
                sdkInt = preferences.sdkInt
                versionName = preferences.versionName
                versionCode = preferences.versionCode
                apiVersion = preferences.apiVersion
                imageQuality = preferences.imageQuality
                showEmoji = preferences.showEmoji
                showSquare = preferences.showSquare
                openInBrowser = preferences.openInBrowser
                imageFilter = preferences.imageFilter
                recordHistory = preferences.recordHistory
                materialYou = preferences.materialYou
                isDarkMode = preferences.isDarkMode()
                themeType = preferences.themeType
                paletteStyle = preferences.paletteStyle
                seedColor = preferences.seedColor
                pureBlack = preferences.pureBlack
                fontScale = preferences.fontScale
                contentScale = preferences.contentScale
                hapticFeedback = preferences.hapticFeedback
                hapticStrength = preferences.hapticStrength
                customHaptics = preferences.customHaptics
                reverseHapticEffects = preferences.reverseHapticEffects

                C001apkComposeTheme(
                    darkTheme = preferences.isDarkMode(),
                    themeType = preferences.themeType,
                    seedColor = preferences.seedColor,
                    materialYou = preferences.materialYou,
                    pureBlack = preferences.pureBlack,
                    paletteStyle = preferences.paletteStyle,
                    fontScale = preferences.fontScale,
                    contentScale = preferences.contentScale,
                ) {
                    MiuixRootPopupHost {
                        Surface(
                            modifier =
                                Modifier.fillMaxSize().background(MiuixTheme.colorScheme.background),
                        ) {
                            if (!viewModel.startup.preparingAccount) {
                                com.example.c001apk.compose.ui.update.AppUpdateHost(navigator)
                                MainNavigation(
                                    navigator = navigator,
                                    badge = viewModel.badge,
                                    resetBadge = viewModel::resetBadge,
                                    widthSizeClass = currentWindowWidthSizeClass(),
                                )
                            }
                        }
                    }
                }
            }

            LaunchedEffect(navigator, viewModel.startup.preparingAccount, pendingLinks.size) {
                if (!viewModel.startup.preparingAccount) {
                    androidx.compose.runtime.withFrameNanos {}
                    while (pendingLinks.isNotEmpty()) {
                        val url = pendingLinks.first()
                        navigator.onOpenLink(
                            context = this@MainActivity,
                            url = url,
                            onSearch = searchViewModel::saveHistory,
                        )
                        pendingLinks.removeAt(0)
                        androidx.compose.runtime.withFrameNanos {}
                    }
                }
            }
        }
    }

    private fun setupStableStatusBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
            val currentTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val stableTop =
                stableStatusBarInset ?: currentTop.takeIf { it > 0 } ?: fallbackStatusBarHeight()
            if (stableStatusBarInset == null && stableTop > 0) {
                stableStatusBarInset = stableTop
            }
            if (stableTop == 0) {
                return@setOnApplyWindowInsetsListener insets
            }
            WindowInsetsCompat.Builder(insets)
                .setInsets(WindowInsetsCompat.Type.statusBars(), Insets.of(0, stableTop, 0, 0))
                .build()
        }
    }

    private fun fallbackStatusBarHeight(): Int {
        val resId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resId > 0) resources.getDimensionPixelSize(resId) else 0
    }
}
