package com.example.c001apk.compose.ui.settings

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.model.HapticStrength
import com.example.c001apk.compose.logic.settings.AppLanguage
import com.example.c001apk.compose.logic.settings.AppLanguage.findActivity
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.blacklist.BlackListType
import com.example.c001apk.compose.ui.component.LocalDialogClose
import com.example.c001apk.compose.ui.component.MiuixToolbarIconButton
import com.example.c001apk.compose.ui.component.MiuixAlertDialog as AlertDialog
import com.example.c001apk.compose.ui.component.MiuixDialogButton as TextButton
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.currentWindowSize
import com.example.c001apk.compose.ui.component.MiuixSearchField
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.ui.component.settings.BasicListItem
import com.example.c001apk.compose.ui.component.settings.PreferenceGroup
import com.example.c001apk.compose.ui.component.settings.PreferenceSpacing
import com.example.c001apk.compose.ui.component.settings.SelectionItem
import com.example.c001apk.compose.ui.component.settings.SelectionListItem
import com.example.c001apk.compose.ui.component.settings.SwitchListItem
import com.example.c001apk.compose.ui.component.TopBarScrollEffect
import com.example.c001apk.compose.util.CacheDataManager.clearAllCache
import com.example.c001apk.compose.util.CacheDataManager.getTotalCacheSize
import java.util.Formatter
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Close
import top.yukonga.miuix.kmp.icon.basic.Search
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Blocklist
import top.yukonga.miuix.kmp.icon.extended.Carrier
import top.yukonga.miuix.kmp.icon.extended.Create
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Filter
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Photos
import top.yukonga.miuix.kmp.icon.extended.Recent
import top.yukonga.miuix.kmp.icon.extended.Show
import top.yukonga.miuix.kmp.icon.extended.Theme
import top.yukonga.miuix.kmp.icon.extended.Translate
import top.yukonga.miuix.kmp.icon.extended.Update
import top.yukonga.miuix.kmp.icon.extended.WorldClock
import top.yukonga.miuix.kmp.icon.glass.Contacts
import top.yukonga.miuix.kmp.icon.glass.ContactsCircle
import top.yukonga.miuix.kmp.icon.glass.FavoriteOutline
import top.yukonga.miuix.kmp.icon.glass.FolderOutline
import top.yukonga.miuix.kmp.icon.glass.MyDevice
import top.yukonga.miuix.kmp.icon.glass.Tags
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

private enum class SettingsSection(@param:StringRes val titleRes: Int, val icon: ImageVector) {
    Appearance(R.string.settings_section_appearance, MiuixIcons.Theme),
    Content(R.string.settings_section_content, MiuixIcons.Glass.FavoriteOutline),
    Privacy(R.string.settings_section_privacy, MiuixIcons.Show),
    Storage(R.string.settings_section_storage, MiuixIcons.Glass.FolderOutline),
    Device(R.string.settings_section_device, MiuixIcons.Glass.MyDevice),
}

/** Created by bggRGjQaUbCoE on 2024/5/30 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    section: String?,
    onSectionClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onHomeTabsClick: () -> Unit,
    onNavigationOrderClick: () -> Unit,
    onContentFilters: () -> Unit,
    onAccountsClick: () -> Unit,
    onLocalFollowsClick: () -> Unit,
    onParamsClick: () -> Unit,
    onDeviceImport: () -> Unit,
    onAboutClick: () -> Unit,
    onViewBlackList: (String) -> Unit,
) {

    val prefs = LocalUserPreferences.current
    val layoutDirection = LocalLayoutDirection.current
    val context = LocalContext.current
    val wide = currentWindowSize().width >= 840.dp
    val routeSection = SettingsSection.entries.firstOrNull { it.name == section }
    var wideSection by
        rememberSaveable(section) {
            mutableStateOf(routeSection ?: SettingsSection.Appearance)
        }
    // Narrow categories use the app navigation stack, including its predictive back preview.
    val activeSection = if (wide) wideSection else routeSection
    var searchExpanded by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val closeSearch = {
        searchExpanded = false
        searchQuery = ""
    }
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = searchExpanded,
        onBackCompleted = closeSearch,
    )
    val matchesSearch: (String) -> Boolean = { text ->
        searchQuery.isBlank() || text.contains(searchQuery.trim(), ignoreCase = true)
    }

    val topBarScrollBehavior = MiuixScrollBehavior()
    val imageQualityList =
        listOf(
            stringResource(R.string.settings_image_quality_adaptive),
            stringResource(R.string.settings_image_quality_original),
            stringResource(R.string.settings_image_quality_standard),
        )
    val hapticStrengthList = HapticStrength.options.map { hapticStrengthLabel(it) }

    var showSZLMIDDialog by remember { mutableStateOf(false) }
    var showFontScaleDialog by remember { mutableStateOf(false) }
    var showContentScaleDialog by remember { mutableStateOf(false) }
    var showCleanCacheDialog by remember { mutableStateOf(false) }
    var showThemeTypeDialog by remember { mutableStateOf(false) }
    var showHapticStrengthDialog by remember { mutableStateOf(false) }
    var cacheSize by remember { mutableStateOf(getTotalCacheSize(context)) }
    val showHapticStrengthDialogClick = rememberHapticClick {
        showHapticStrengthDialog = true
    }
    val sectionTitles = SettingsSection.entries.associateWith { stringResource(it.titleRes) }
    val languageLabel = stringResource(R.string.settings_language)
    var language by remember { mutableStateOf(AppLanguage.current(context)) }
    val languageItems =
        listOf(
            SelectionItem(stringResource(R.string.settings_theme_follow_system), AppLanguage.SYSTEM),
            SelectionItem(stringResource(R.string.language_chinese), AppLanguage.CHINESE),
            SelectionItem(stringResource(R.string.language_chinese_traditional), AppLanguage.CHINESE_TRADITIONAL),
            SelectionItem(stringResource(R.string.language_english), AppLanguage.ENGLISH),
            SelectionItem(stringResource(R.string.language_japanese), AppLanguage.JAPANESE),
            SelectionItem(stringResource(R.string.language_korean), AppLanguage.KOREAN),
        )
    val accountsLabel = stringResource(R.string.settings_accounts)
    val aboutLabel = stringResource(R.string.settings_about)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AnimatedContent(
                targetState = searchExpanded,
                transitionSpec = {
                    (fadeIn(tween(180, delayMillis = 60)) +
                        slideInVertically(tween(300, easing = FastOutSlowInEasing)) { it / 12 })
                        .togetherWith(
                            fadeOut(tween(120)) +
                                slideOutVertically(tween(300, easing = FastOutSlowInEasing)) { it / 12 },
                        )
                        .using(
                            SizeTransform(clip = false) { _, _ ->
                                tween(300, easing = FastOutSlowInEasing)
                            },
                        )
                },
                label = "settingsSearch",
            ) { expanded ->
                if (expanded) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(
                                WindowInsets.statusBars.union(WindowInsets.displayCutout),
                            )
                            .padding(bottom = 12.dp),
                    ) {
                    SearchBar(
                        inputField = {
                            MiuixSearchField(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                onSearch = {},
                                expanded = true,
                                onExpandedChange = { if (!it) closeSearch() },
                                modifier = Modifier.testTag("settings-search"),
                                label = "",
                                shouldAutoFocus = true,
                            )
                        },
                        onExpandedChange = { if (!it) closeSearch() },
                        expanded = true,
                        outsideEndAction = {
                            MiuixToolbarIconButton(onClick = closeSearch) {
                                Icon(MiuixIcons.Basic.Close, contentDescription = stringResource(R.string.settings_search_close))
                            }
                        },
                    ) {}
                    }
                } else {
                    TopAppBar(
                        color = com.example.c001apk.compose.ui.component.LocalBarColor.current,
                        title =
                            if (wide) stringResource(R.string.settings)
                            else activeSection?.let { stringResource(it.titleRes) }
                                ?: stringResource(R.string.settings),
                        largeTitle =
                            if (wide) stringResource(R.string.settings)
                            else activeSection?.let { stringResource(it.titleRes) }
                                ?: stringResource(R.string.settings),
                        scrollBehavior = topBarScrollBehavior,
                        navigationIcon = {
                            MiuixToolbarIconButton(onClick = onBackClick) {
                                Icon(MiuixIcons.Back, contentDescription = stringResource(R.string.action_back))
                            }
                        },
                        actions = {
                            if (activeSection == null) {
                                MiuixToolbarIconButton(onClick = { searchExpanded = true }) {
                                    Icon(MiuixIcons.Basic.Search, contentDescription = stringResource(R.string.settings_search_open))
                                }
                            }
                        },
                    )
                }
            }
        },
    ) { paddingValues ->
    PreferenceSpacing(
        horizontal = if (activeSection != null && !wide) 16.dp else 12.dp,
        spacing = if (activeSection != null && !wide) 12.dp else 8.dp,
    ) {
            Row(Modifier.fillMaxSize()) {
                if (wide || activeSection == null) {
                    val topBarListState = rememberLazyListState()
                    TopBarScrollEffect { topBarListState.animateScrollToItem(0) }
                    LazyColumn(
                        state = topBarListState,
                        modifier =
                            if (wide) Modifier.width(260.dp).fillMaxHeight()
                            else Modifier.fillMaxSize().nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                        contentPadding =
                            PaddingValues(
                                top = paddingValues.calculateTopPadding() + 10.dp,
                                bottom = paddingValues.calculateBottomPadding(),
                                start = paddingValues.calculateLeftPadding(layoutDirection),
                                end = paddingValues.calculateRightPadding(layoutDirection),
                            ),
                    ) {
                        val visibleSections =
                            SettingsSection.entries.filter { matchesSearch(sectionTitles.getValue(it)) }
                        visibleSections.forEach { section ->
                            item(key = section.name) {
                                if (section == SettingsSection.Appearance) {
                                    PreferenceGroup {
                                        BasicListItem(
                                            headlineText = sectionTitles.getValue(section),
                                            leadingImageVector = section.icon,
                                            holdDownState = wide && activeSection == section,
                                            onClick = {
                                                if (wide) wideSection = section
                                                else {
                                                    closeSearch()
                                                    onSectionClick(section.name)
                                                }
                                            },
                                        )
                                        SelectionListItem(
                                            leadingImageVector = MiuixIcons.Translate,
                                            title = languageLabel,
                                            value = language,
                                            selections = languageItems,
                                            onSelected = { tag ->
                                                language = tag
                                                AppLanguage.set(context, tag)
                                                context.findActivity()?.recreate()
                                            },
                                        )
                                    }
                                } else {
                                    BasicListItem(
                                        headlineText = sectionTitles.getValue(section),
                                        leadingImageVector = section.icon,
                                        holdDownState = wide && activeSection == section,
                                        onClick = {
                                            if (wide) wideSection = section
                                            else {
                                                closeSearch()
                                                onSectionClick(section.name)
                                            }
                                        },
                                    )
                                }
                            }
                        }
                        if (matchesSearch(accountsLabel)) {
                            item(key = "accounts") {
                                BasicListItem(
                                    headlineText = accountsLabel,
                                    leadingImageVector = MiuixIcons.Glass.ContactsCircle,
                                    onClick = {
                                        closeSearch()
                                        onAccountsClick()
                                    },
                                )
                            }
                        }
                        if (matchesSearch(aboutLabel)) {
                            item(key = "about") {
                                BasicListItem(
                                    headlineText = aboutLabel,
                                    leadingImageVector = MiuixIcons.Info,
                                    onClick = {
                                        closeSearch()
                                        onAboutClick()
                                    },
                                )
                            }
                        }
                        if (searchQuery.isNotBlank() && visibleSections.isEmpty() &&
                            !matchesSearch(accountsLabel) && !matchesSearch(aboutLabel)
                        ) {
                            item(key = "no-results") {
                                Text(
                                    text = stringResource(R.string.settings_search_empty),
                                    modifier = Modifier.fillMaxWidth().padding(28.dp),
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                            }
                        }
                    }
                }
                if (activeSection != null) {
                    val topBarListState = rememberLazyListState()
                    TopBarScrollEffect { topBarListState.animateScrollToItem(0) }
                    LazyColumn(
                        state = topBarListState,
                        modifier =
                            Modifier.weight(1f)
                                .fillMaxHeight()
                                .scrollEndHaptic()
                                .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                        contentPadding =
                            PaddingValues(
                                top = paddingValues.calculateTopPadding() + 10.dp,
                                bottom = 24.dp + paddingValues.calculateBottomPadding(),
                                start = paddingValues.calculateLeftPadding(layoutDirection),
                                end = paddingValues.calculateRightPadding(layoutDirection),
                            ),
                    ) {
                        when (activeSection) {
                            SettingsSection.Appearance -> appearancePreferences(
                                prefs = prefs,
                                viewModel = viewModel,
                                onCustomTheme = { showThemeTypeDialog = true },
                                onNavigationOrder = onNavigationOrderClick,
                                onFontSize = { showFontScaleDialog = true },
                                onContentScale = { showContentScaleDialog = true },
                                onHapticStrength = showHapticStrengthDialogClick,
                            )
                            SettingsSection.Content -> {
                                item(key = "Content_0") {
                                    HomeContentPreferences(
                                        showCarousel = prefs.showHomeCarousel,
                                        showChannels = prefs.showHomeChannels,
                                        showTopics = prefs.showSquare,
                                        showEmoji = prefs.showEmoji,
                                        onHomeTabsClick = onHomeTabsClick,
                                        onShowCarousel = viewModel::setShowHomeCarousel,
                                        onShowChannels = viewModel::setShowHomeChannels,
                                        onShowTopics = viewModel::setShowSquare,
                                        onShowEmoji = viewModel::setShowEmoji,
                                    )
                                }
                                item(key = "default_search") {
                                    SearchPreferences(prefs.defaultSearchOrder, viewModel::setDefaultSearchOrder)
                                }
                                item(key = "publish") {
                                    PreferenceGroup(title = stringResource(R.string.settings_group_publish)) {
                                        SwitchListItem(
                                            prefs.hideHomePublishButton,
                                            MiuixIcons.Create,
                                            stringResource(R.string.settings_hide_publish_button),
                                            stringResource(R.string.settings_hide_publish_button_summary),
                                        ) { viewModel.setHideHomePublishButton(it) }
                                    }
                                }
                                item(key = "Content_1") {
                                    PreferenceGroup(title = stringResource(R.string.settings_group_follow)) {
                                        BasicListItem(
                                            leadingImageVector = MiuixIcons.Glass.Contacts,
                                            headlineText = stringResource(R.string.settings_local_follow),
                                            supportingText = stringResource(R.string.settings_local_follow_summary),
                                            onClick = onLocalFollowsClick,
                                        )
                                    }
                                }
                                item(key = "Content_2") {
                                    PreferenceGroup(title = stringResource(R.string.settings_group_block)) {
                                        BasicListItem(
                                                headlineText = stringResource(R.string.settings_content_filter),
                                                supportingText = stringResource(R.string.settings_content_filter_summary),
                                                leadingImageVector = MiuixIcons.Filter,
                                                onClick = onContentFilters,
                                            )

                                        BasicListItem(
                                            leadingImageVector = MiuixIcons.Blocklist,
                                            headlineText = stringResource(R.string.settings_user_blocklist),
                                        ) {
                                            onViewBlackList(BlackListType.USER.name)
                                        }
                                        BasicListItem(
                                            leadingImageVector = MiuixIcons.Glass.Tags,
                                            headlineText = stringResource(R.string.settings_topic_blocklist),
                                        ) {
                                            onViewBlackList(BlackListType.TOPIC.name)
                                        }
                                    }
                                }
                            }
                            SettingsSection.Privacy -> {
                                item(key = "Privacy_0") {
                                    PreferenceGroup(title = stringResource(R.string.settings_group_image)) {
                                        SelectionListItem(
                                            leadingImageVector = MiuixIcons.Photos,
                                            title = stringResource(R.string.settings_image_quality),
                                            value = prefs.imageQuality,
                                            selections =
                                                imageQualityList.mapIndexed { index, label ->
                                                    SelectionItem(label, index)
                                                },
                                            onSelected = viewModel::setImageQuality,
                                        )
                                    }
                                }
                                item(key = "Privacy_1") {
                                    PreferenceGroup(title = stringResource(R.string.settings_group_browse)) {
                                        SwitchListItem(
                                            value = prefs.openInBrowser,
                                            leadingImageVector = MiuixIcons.WorldClock,
                                            headlineText = stringResource(R.string.settings_open_in_browser),
                                        ) {
                                            viewModel.setOpenInBrowser(it)
                                        }
                                        SwitchListItem(
                                            value = prefs.recordHistory,
                                            leadingImageVector = MiuixIcons.Recent,
                                            headlineText = stringResource(R.string.settings_record_history),
                                        ) {
                                            viewModel.setRecordHistory(it)
                                        }
                                    }
                                }
                            }
                            SettingsSection.Storage -> {
                                item(key = "Storage_0") {
                                    PreferenceGroup(title = stringResource(R.string.settings_group_cache)) {
                                        BasicListItem(
                                            leadingImageVector = MiuixIcons.Delete,
                                            headlineText = stringResource(R.string.settings_clean_cache),
                                            supportingText = cacheSize,
                                        ) {
                                            cacheSize = getTotalCacheSize(context)
                                            showCleanCacheDialog = true
                                        }
                                        SwitchListItem(
                                            value = prefs.autoClearImageCache,
                                            leadingImageVector = MiuixIcons.Delete,
                                            headlineText = stringResource(R.string.settings_clear_cache_on_exit),
                                            supportingText = stringResource(R.string.settings_clear_cache_on_exit_summary),
                                        ) {
                                            viewModel.setAutoClearImageCache(it)
                                        }
                                        SwitchListItem(
                                            value = prefs.checkUpdate,
                                            leadingImageVector = MiuixIcons.Update,
                                            headlineText = stringResource(R.string.settings_check_app_updates),
                                        ) {
                                            viewModel.setCheckUpdate(it)
                                        }
                                    }
                                }
                            }
                            SettingsSection.Device -> {
                                item(key = "Device_0") {
                                    PreferenceGroup(title = stringResource(R.string.settings_section_device)) {
                                        BasicListItem(
                                            leadingImageVector = MiuixIcons.Carrier,
                                            headlineText = stringResource(R.string.settings_szlm_id),
                                            supportingText =
                                                if (prefs.szlmId.isBlank()) stringResource(R.string.settings_szlm_id_empty)
                                                else stringResource(R.string.settings_szlm_id_saved),
                                        ) {
                                            showSZLMIDDialog = true
                                        }
                                        BasicListItem(
                                            leadingImageVector = MiuixIcons.Glass.MyDevice,
                                            headlineText = stringResource(R.string.settings_device_params),
                                        ) {
                                            onParamsClick()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }

        when {
            showThemeTypeDialog -> {
                EditTextDialog(
                    hint = "6650A4",
                    maxLength = 6,
                    data = prefs.seedColor,
                    title = stringResource(R.string.settings_custom_theme_color),
                    onDismiss = {
                        showThemeTypeDialog = false
                    },
                    setData = {
                        viewModel.setSeedColor(it)
                    },
                )
            }

            showSZLMIDDialog -> {
                DigitalUnionDialog(
                    id = prefs.szlmId,
                    generating = viewModel.generatingId,
                    error = viewModel.idError,
                    onGenerate = viewModel::generateSZLMId,
                    onImport = {
                        showSZLMIDDialog = false
                        onDeviceImport()
                    },
                    onDismiss = { showSZLMIDDialog = false },
                    onSave = viewModel::setSZLMId,
                )
            }

            showFontScaleDialog -> {
                SliderDialog(
                    data = prefs.fontScale,
                    title = stringResource(R.string.settings_font_scale),
                    hint = stringResource(R.string.settings_scale_hint),
                    onDismiss = {
                        showFontScaleDialog = false
                    },
                    setData = {
                        viewModel.setFontScale(it)
                    },
                )
            }

            showContentScaleDialog -> {
                SliderDialog(
                    data = prefs.contentScale,
                    title = stringResource(R.string.settings_content_scale),
                    hint = stringResource(R.string.settings_scale_hint),
                    onDismiss = {
                        showContentScaleDialog = false
                    },
                    setData = {
                        viewModel.setContentScale(it)
                    },
                )
            }

            showCleanCacheDialog -> {
                CleanCacheDialog(
                    cacheSize = cacheSize,
                    onDismiss = {
                        showCleanCacheDialog = false
                    },
                    onClean = {
                        clearAllCache(context)
                        cacheSize = "0 B"
                    },
                )
            }

            showHapticStrengthDialog -> {
                ListItemDialog(
                    title = stringResource(R.string.settings_haptic_strength),
                    list = hapticStrengthList,
                    selected = HapticStrength.options.indexOf(prefs.hapticStrength),
                    onDismiss = {
                        showHapticStrengthDialog = false
                    },
                    hapticStrengthForIndex = { HapticStrength.options[it] },
                    setData = {
                        showHapticStrengthDialog = false
                        viewModel.setHapticStrength(HapticStrength.options[it])
                    },
                )
            }
        }
    }
}

@Composable
fun CleanCacheDialog(
    cacheSize: String,
    onDismiss: () -> Unit,
    onClean: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                },
            ) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onClean()
                },
            ) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        text = {
            Text(text = stringResource(R.string.settings_cache_size, cacheSize))
        },
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.settings_clean_cache),
                textAlign = TextAlign.Center,
            )
        },
    )
}

@Composable
fun ListItemDialog(
    title: String,
    list: List<String>,
    selected: Int,
    onDismiss: () -> Unit,
    hapticStrengthForIndex: ((Int) -> HapticStrength)? = null,
    setData: (Int) -> Unit,
) {
    val optionClicks = list.mapIndexed { index, _ ->
        rememberHapticClick(strength = hapticStrengthForIndex?.invoke(index)) { setData(index) }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            val close = checkNotNull(LocalDialogClose.current)
            top.yukonga.miuix.kmp.basic.Card {
                list.forEachIndexed { index, label ->
                    top.yukonga.miuix.kmp.preference.RadioButtonPreference(
                        title = label,
                        selected = selected == index,
                        onClick = { close { optionClicks[index](); onDismiss() } },
                        radioButtonLocation = top.yukonga.miuix.kmp.preference.RadioButtonLocation.End,
                        modifier = Modifier.squircleClip(16.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, primary = false) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
fun EditTextDialog(
    data: String,
    title: String,
    onDismiss: () -> Unit,
    setData: (String) -> Unit,
    hint: String? = null,
    maxLength: Int? = null,
) {
    var text by remember {
        mutableStateOf(
            TextFieldValue(
                text = data,
                selection = TextRange(data.length),
            ),
        )
    }
    val canSave = text.text.isNotBlank() && (maxLength == null || text.text.length == maxLength)
    fun submit(close: (() -> Unit) -> Unit) {
        if (canSave) close { onDismiss(); setData(text.text) }
    }
    AlertDialog(
        onDismissRequest = { onDismiss() },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                },
            ) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        confirmButton = {
            val close = checkNotNull(LocalDialogClose.current)
            TextButton(onClick = { submit(close) }, enabled = canSave, dismissOnClick = false) {
                Text(stringResource(R.string.action_save))
            }
        },
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                textAlign = TextAlign.Center,
            )
        },
        text = {
            val close = checkNotNull(LocalDialogClose.current)
            val focusRequest = remember { FocusRequester() }
            var laidOut by remember { mutableStateOf(false) }
            val windowFocused = androidx.compose.ui.platform.LocalWindowInfo.current.isWindowFocused
            LaunchedEffect(laidOut, windowFocused) {
                if (laidOut && windowFocused) focusRequest.requestFocus()
            }
            TextField(
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequest).onGloballyPositioned { laidOut = true },
                value = text,
                onValueChange = {
                    if (maxLength != null) {
                        if (it.text.matches(Regex("^[0-9a-fA-F]{0,6}$"))) text = it
                    } else {
                        text = it
                    }
                },
                label = hint.orEmpty(),
                useLabelAsPlaceholder = true,
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { submit(close) }),
            )
        },
    )
}

@Composable
fun SliderDialog(
    data: Float,
    title: String,
    hint: String,
    onDismiss: () -> Unit,
    setData: (Float) -> Unit,
) {
    var progress by remember { mutableFloatStateOf(data) }
    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        titleActions = {
            val resetClick = rememberHapticClick { progress = 1.0f }
            top.yukonga.miuix.kmp.basic.IconButton(onClick = resetClick, modifier = Modifier.size(40.dp)) {
                Icon(MiuixIcons.Refresh, stringResource(R.string.menu_reset), Modifier.size(24.dp))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    setData(progress)
                },
            ) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                textAlign = TextAlign.Center,
            )
        },
        text = {
            Column(
                Modifier.fillMaxWidth(),
            ) {
                top.yukonga.miuix.kmp.preference.SliderPreference(
                    title = hint,
                    value = progress,
                    valueText = "${Formatter().format("%.2f", progress)}x",
                    onValueChange = { progress = it },
                    valueRange = 0.8f..1.3f,
                    insideMargin = PaddingValues(vertical = 12.dp),
                )
                Text(
                    text = "$hint: ${Formatter().format("%.2f", progress)}",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 15.sp * progress,
                    textAlign = TextAlign.Center,
                )
            }
        },
    )
}
