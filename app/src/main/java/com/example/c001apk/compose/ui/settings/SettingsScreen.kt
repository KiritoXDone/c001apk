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
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.blacklist.BlackListType
import com.example.c001apk.compose.ui.component.MiuixToolbarIconButton
import com.example.c001apk.compose.ui.component.MiuixAlertDialog as AlertDialog
import com.example.c001apk.compose.ui.component.MiuixDialogButton as TextButton
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.currentWindowSize
import com.example.c001apk.compose.ui.component.MiuixSearchField
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.ui.component.settings.BasicListItem
import com.example.c001apk.compose.ui.component.settings.PreferenceGroup
import com.example.c001apk.compose.ui.component.settings.SelectionItem
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteAddSquareLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteArrowLeftLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteCardSimLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteCellphone2Line
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteClockwiseLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteCloseLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteDelete2Line
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteEye2Line
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteFilterLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteFolderLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteGlobeLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteHeartLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteHistoryLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteInformationLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCutePaletteLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCutePhotoAlbumLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteSearchLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteTagLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteUser3Line
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteUserFollowLine
import com.example.c001apk.compose.ui.component.icons.mingcute.MingCuteUserForbidLine
import com.example.c001apk.compose.ui.component.settings.SelectionListItem
import com.example.c001apk.compose.ui.component.settings.SwitchListItem
import com.example.c001apk.compose.ui.component.TopBarScrollEffect
import com.example.c001apk.compose.util.CacheDataManager.clearAllCache
import com.example.c001apk.compose.util.CacheDataManager.getTotalCacheSize
import java.util.Formatter
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

private enum class SettingsSection(@param:StringRes val titleRes: Int, val icon: ImageVector) {
    Appearance(R.string.settings_section_appearance, MingCutePaletteLine),
    Content(R.string.settings_section_content, MingCuteHeartLine),
    Privacy(R.string.settings_section_privacy, MingCuteEye2Line),
    Storage(R.string.settings_section_storage, MingCuteFolderLine),
    Device(R.string.settings_section_device, MingCuteCellphone2Line),
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
                                Icon(MingCuteCloseLine, contentDescription = stringResource(R.string.settings_search_close))
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
                                Icon(MingCuteArrowLeftLine, contentDescription = stringResource(R.string.action_back))
                            }
                        },
                        actions = {
                            if (activeSection == null) {
                                MiuixToolbarIconButton(onClick = { searchExpanded = true }) {
                                    Icon(MingCuteSearchLine, contentDescription = stringResource(R.string.settings_search_open))
                                }
                            }
                        },
                    )
                }
            }
        },
    ) { paddingValues ->
        Row(Modifier.fillMaxSize()) {
            if (wide || activeSection == null) {
                val topBarListState = rememberLazyListState()
                TopBarScrollEffect { topBarListState.animateScrollToItem(0) }
                LazyColumn(
                    state = topBarListState,
                    modifier =
                        if (wide) Modifier.width(260.dp).fillMaxHeight()
                        else Modifier.fillMaxSize().nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                    contentPadding = paddingValues,
                ) {
                    val visibleSections =
                        SettingsSection.entries.filter { matchesSearch(sectionTitles.getValue(it)) }
                    visibleSections.forEach { section ->
                        item(key = section.name) {
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
                    if (matchesSearch(accountsLabel)) {
                        item(key = "accounts") {
                            BasicListItem(
                                headlineText = accountsLabel,
                                leadingImageVector = MingCuteUser3Line,
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
                                leadingImageVector = MingCuteInformationLine,
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
                            top = paddingValues.calculateTopPadding(),
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
                                        MingCuteAddSquareLine,
                                        stringResource(R.string.settings_hide_publish_button),
                                        stringResource(R.string.settings_hide_publish_button_summary),
                                    ) { viewModel.setHideHomePublishButton(it) }
                                }
                            }
                            item(key = "Content_1") {
                                PreferenceGroup(title = stringResource(R.string.settings_group_follow)) {
                                    BasicListItem(
                                        leadingImageVector = MingCuteUserFollowLine,
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
                                            leadingImageVector = MingCuteFilterLine,
                                            onClick = onContentFilters,
                                        )

                                    BasicListItem(
                                        leadingImageVector = MingCuteUserForbidLine,
                                        headlineText = stringResource(R.string.settings_user_blocklist),
                                    ) {
                                        onViewBlackList(BlackListType.USER.name)
                                    }
                                    BasicListItem(
                                        leadingImageVector = MingCuteTagLine,
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
                                        leadingImageVector = MingCutePhotoAlbumLine,
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
                                        leadingImageVector = MingCuteGlobeLine,
                                        headlineText = stringResource(R.string.settings_open_in_browser),
                                    ) {
                                        viewModel.setOpenInBrowser(it)
                                    }
                                    SwitchListItem(
                                        value = prefs.recordHistory,
                                        leadingImageVector = MingCuteHistoryLine,
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
                                        leadingImageVector = MingCuteDelete2Line,
                                        headlineText = stringResource(R.string.settings_clean_cache),
                                        supportingText = cacheSize,
                                    ) {
                                        cacheSize = getTotalCacheSize(context)
                                        showCleanCacheDialog = true
                                    }
                                    SwitchListItem(
                                        value = prefs.autoClearImageCache,
                                        leadingImageVector = MingCuteDelete2Line,
                                        headlineText = stringResource(R.string.settings_clear_cache_on_exit),
                                        supportingText = stringResource(R.string.settings_clear_cache_on_exit_summary),
                                    ) {
                                        viewModel.setAutoClearImageCache(it)
                                    }
                                    SwitchListItem(
                                        value = prefs.checkUpdate,
                                        leadingImageVector = MingCuteClockwiseLine,
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
                                        leadingImageVector = MingCuteCardSimLine,
                                        headlineText = stringResource(R.string.settings_szlm_id),
                                        supportingText =
                                            if (prefs.szlmId.isBlank()) stringResource(R.string.settings_szlm_id_empty)
                                            else stringResource(R.string.settings_szlm_id_saved),
                                    ) {
                                        showSZLMIDDialog = true
                                    }
                                    BasicListItem(
                                        leadingImageVector = MingCuteCellphone2Line,
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
                    onSave = { viewModel.setSZLMId(it) { showSZLMIDDialog = false } },
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
    val onSelect by androidx.compose.runtime.rememberUpdatedState(optionClicks)
    val entry =
        remember(list, selected) {
            top.yukonga.miuix.kmp.basic.DropdownEntry(
                list.mapIndexed { index, label ->
                    top.yukonga.miuix.kmp.basic.DropdownItem(
                        text = label,
                        selected = selected == index,
                        onClick = { onSelect[index]() },
                    )
                },
            )
        }
    top.yukonga.miuix.kmp.popup.WindowDropdownDialog(
        entry = entry,
        title = title,
        dialogButtonString = stringResource(android.R.string.cancel),
        show = true,
        onDismiss = onDismiss,
        onDismissFinished = onDismiss,
        dropdownColors = top.yukonga.miuix.kmp.basic.DropdownDefaults.dialogDropdownColors(),
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
    val focusRequest = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        try {
            focusRequest.requestFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
            TextButton(
                onClick = {
                    onDismiss()
                    setData(text.text)
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
            TextField(
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequest),
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
            TextButton(
                onClick = {
                    onDismiss()
                    setData(1.0f)
                },
            ) {
                Text(text = stringResource(id = R.string.menu_reset))
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
