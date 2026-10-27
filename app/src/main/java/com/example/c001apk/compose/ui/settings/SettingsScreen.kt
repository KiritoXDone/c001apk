package com.example.c001apk.compose.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.BuildConfig
import com.example.c001apk.compose.R
import com.example.c001apk.compose.ThemeMode
import com.example.c001apk.compose.ThemeType
import com.example.c001apk.compose.logic.model.HapticStrength
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.blacklist.BlackListType
import com.example.c001apk.compose.ui.component.MiuixAlertDialog as AlertDialog
import com.example.c001apk.compose.ui.component.MiuixDialogButton as TextButton
import com.example.c001apk.compose.ui.component.MiuixScaffold as Scaffold
import com.example.c001apk.compose.ui.component.currentWindowSize
import com.example.c001apk.compose.ui.component.icons.EmojiEmotions
import com.example.c001apk.compose.ui.component.icons.Smartphone
import com.example.c001apk.compose.ui.component.icons.Swatch
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.ui.component.settings.BasicListItem
import com.example.c001apk.compose.ui.component.settings.DropdownListItem
import com.example.c001apk.compose.ui.component.settings.PreferenceGroup
import com.example.c001apk.compose.ui.component.settings.SelectionItem
import com.example.c001apk.compose.ui.component.settings.StateDropdownListItem
import com.example.c001apk.compose.ui.component.settings.SwitchListItem
import com.example.c001apk.compose.ui.component.TopBarScrollEffect
import com.example.c001apk.compose.util.CacheDataManager.clearAllCache
import com.example.c001apk.compose.util.CacheDataManager.getTotalCacheSize
import java.util.Formatter
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTopAppBar as TopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.All
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Blocklist
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Image
import top.yukonga.miuix.kmp.icon.extended.Notes
import top.yukonga.miuix.kmp.icon.extended.Photos
import top.yukonga.miuix.kmp.icon.extended.Recent
import top.yukonga.miuix.kmp.icon.extended.Rename
import top.yukonga.miuix.kmp.icon.extended.ScreenMirroring
import top.yukonga.miuix.kmp.icon.extended.SelectAll
import top.yukonga.miuix.kmp.icon.extended.Theme
import top.yukonga.miuix.kmp.icon.extended.Update
import top.yukonga.miuix.kmp.icon.extended.WorldClock
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle as PaletteStyle
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

private enum class SettingsSection(val title: String, val summary: String) {
    Appearance("外观与字号", "主题、字号、分栏与触感"),
    Content("关注与内容", "首页板块、本地关注和屏蔽"),
    Privacy("浏览与隐私", "图片、链接和浏览记录"),
    Storage("存储与更新", "图片缓存与应用更新"),
    Device("协议与设备", "数盟 ID、设备和接口参数"),
}

/** Created by bggRGjQaUbCoE on 2024/5/30 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    section: String?,
    onSectionClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onHomeTabsClick: () -> Unit,
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

    val imageQualityList by lazy { listOf("网络自适应", "原图", "普清") }
    val themeList by lazy { listOf("跟随系统", "总是开启", "总是关闭") }
    val hapticStrengthList by lazy { HapticStrength.options.map { it.label } }

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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                color = com.example.c001apk.compose.ui.component.LocalBarColor.current,
                title = if (wide) "设置" else activeSection?.title ?: "设置",
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(MiuixIcons.Back, contentDescription = "返回")
                    }
                },
            )
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
                        else Modifier.fillMaxSize(),
                    contentPadding = paddingValues,
                ) {
                    SettingsSection.entries.forEach { section ->
                        item(key = section.name) {
                            BasicListItem(
                                headlineText = section.title,
                                supportingText = section.summary,
                                holdDownState = wide && activeSection == section,
                                onClick = {
                                    if (wide) wideSection = section
                                    else onSectionClick(section.name)
                                },
                            )
                        }
                    }
                    item(key = "accounts") {
                        BasicListItem(
                            headlineText = "账号管理",
                            supportingText = "切换账号、添加账号与游客模式",
                            onClick = onAccountsClick,
                        )
                    }
                    item(key = "about") {
                        BasicListItem(
                            headlineText = "关于",
                            supportingText = BuildConfig.VERSION_NAME,
                            onClick = onAboutClick,
                        )
                    }
                }
            }
            if (activeSection != null) {
                val topBarListState = rememberLazyListState()
                TopBarScrollEffect { topBarListState.animateScrollToItem(0) }
                LazyColumn(
                    state = topBarListState,
                    modifier = Modifier.weight(1f).fillMaxHeight().scrollEndHaptic(),
                    contentPadding =
                        PaddingValues(
                            top = paddingValues.calculateTopPadding(),
                            bottom = 24.dp + paddingValues.calculateBottomPadding(),
                            start = paddingValues.calculateLeftPadding(layoutDirection),
                            end = paddingValues.calculateRightPadding(layoutDirection),
                        ),
                ) {
                    when (activeSection) {
                        SettingsSection.Appearance -> {
                            item(key = "Appearance_0") {
                                PreferenceGroup(title = "主题") {
                                    SwitchListItem(
                                        value = prefs.materialYou,
                                        leadingImageVector = MiuixIcons.Theme,
                                        headlineText = "系统主题色",
                                    ) {
                                        viewModel.setMaterialYou(it)
                                    }
                                    StateDropdownListItem(
                                        isEnable = !prefs.materialYou,
                                        leadingImageVector = MiuixIcons.Theme,
                                        headlineText = "主题颜色",
                                        value = prefs.themeType,
                                        selections =
                                            ThemeType.entries
                                                .toMutableList()
                                                .also { it.remove(ThemeType.UNRECOGNIZED) }
                                                .map {
                                                    SelectionItem(
                                                        stringResource(id = themeTypeLabelRes(it)),
                                                        it,
                                                    )
                                                },
                                        onValueChanged = { _, value ->
                                            if (value == ThemeType.Custom)
                                                showThemeTypeDialog = true
                                            viewModel.setThemeType(value)
                                        },
                                    )
                                    StateDropdownListItem(
                                        isEnable = !prefs.materialYou,
                                        leadingImageVector = MiuixIcons.Swatch,
                                        headlineText = "调色版风格",
                                        value = prefs.paletteStyle,
                                        selections =
                                            PaletteStyle.entries.mapIndexed { index, label ->
                                                SelectionItem(
                                                    stringResource(
                                                        id = paletteStyleLabelRes(label),
                                                    ),
                                                    index,
                                                )
                                            },
                                        onValueChanged = { index, _ ->
                                            viewModel.setPaletteStyle(index)
                                        },
                                    )
                                    DropdownListItem(
                                        leadingImageVector = MiuixIcons.Theme,
                                        headlineText = "深色主题",
                                        value = prefs.themeMode.name,
                                        selections =
                                            (0..2).map {
                                                SelectionItem(
                                                    themeList[it],
                                                    ThemeMode.entries[it].name,
                                                )
                                            },
                                        onValueChanged = { index, _ ->
                                            viewModel.setDarkTheme(ThemeMode.forNumber(index))
                                        },
                                    )
                                    SwitchListItem(
                                        value = prefs.pureBlack,
                                        leadingImageVector = MiuixIcons.Theme,
                                        headlineText = "纯黑主题",
                                    ) {
                                        viewModel.setPureBlack(it)
                                    }
                                }
                            }
                            item(key = "Appearance_1") {
                                PreferenceGroup(title = "显示") {
                                    SwitchListItem(
                                        value = prefs.floatingNavigationBar,
                                        leadingImageVector = MiuixIcons.ScreenMirroring,
                                        headlineText = "悬浮底栏",
                                        supportingText = "在手机布局中使用悬浮导航栏",
                                    ) {
                                        viewModel.setFloatingNavigationBar(it)
                                    }
                                    SwitchListItem(
                                        value = prefs.blurBars && Build.VERSION.SDK_INT >= 33,
                                        enabled = Build.VERSION.SDK_INT >= 33,
                                        leadingImageVector = MiuixIcons.Theme,
                                        headlineText = "模糊效果",
                                        supportingText =
                                            if (Build.VERSION.SDK_INT >= 33) "模糊顶部栏和底部栏后方的内容"
                                            else "需要 Android 13 及以上",
                                    ) {
                                        viewModel.setBlurBars(it)
                                    }
                                    SwitchListItem(
                                        value = prefs.collapseTabsOnScroll,
                                        leadingImageVector = MiuixIcons.ScreenMirroring,
                                        headlineText = "滚动时收起标签栏",
                                        supportingText = "上滑收起 Tab 栏，下滑重新展开",
                                    ) {
                                        viewModel.setCollapseTabsOnScroll(it)
                                    }
                                    DropdownListItem(
                                        value = prefs.readingMode,
                                        headlineText = "分栏阅读",
                                        selections =
                                            listOf(
                                                SelectionItem("自动适配大屏", 0),
                                                SelectionItem("开启（窗口足够宽时）", 1),
                                                SelectionItem("关闭", 2),
                                            ),
                                        onValueChanged = { _, value ->
                                            viewModel.setReadingMode(value)
                                        },
                                    )
                                    BasicListItem(
                                        leadingImageVector = MiuixIcons.Rename,
                                        headlineText = "字体大小",
                                        supportingText =
                                            "${Formatter().format("%.2f", prefs.fontScale)}x",
                                    ) {
                                        showFontScaleDialog = true
                                    }
                                    BasicListItem(
                                        leadingImageVector = MiuixIcons.ScreenMirroring,
                                        headlineText = "内容大小",
                                        supportingText =
                                            "${Formatter().format("%.2f", prefs.contentScale)}x",
                                    ) {
                                        showContentScaleDialog = true
                                    }
                                    SwitchListItem(
                                        value = prefs.imageFilter,
                                        leadingImageVector = MiuixIcons.Photos,
                                        headlineText = "压暗图片",
                                    ) {
                                        viewModel.setImageFilter(it)
                                    }
                                }
                            }
                            item(key = "Appearance_2") {
                                PreferenceGroup(title = "触感") {
                                    SwitchListItem(
                                        value = prefs.hapticFeedback,
                                        leadingImageVector = MiuixIcons.SelectAll,
                                        headlineText = "触感反馈",
                                    ) {
                                        viewModel.setHapticFeedback(it)
                                    }
                                    SwitchListItem(
                                        value = prefs.customHaptics,
                                        leadingImageVector = MiuixIcons.SelectAll,
                                        headlineText = "自定义触感",
                                        supportingText = "关闭时跟随系统触感设置",
                                    ) {
                                        viewModel.setCustomHaptics(it)
                                    }
                                    if (prefs.customHaptics) {
                                        BasicListItem(
                                            leadingImageVector = MiuixIcons.SelectAll,
                                            headlineText = "触感强度",
                                            supportingText = prefs.hapticStrength.label,
                                            onClick = showHapticStrengthDialogClick,
                                        )
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                            SwitchListItem(
                                                value = prefs.reverseHapticEffects,
                                                leadingImageVector = MiuixIcons.SelectAll,
                                                headlineText = "触感兼容模式",
                                                supportingText = "轻重反馈与预期相反时开启",
                                            ) {
                                                viewModel.setReverseHapticEffects(it)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        SettingsSection.Content -> {
                            item(key = "Content_0") {
                                PreferenceGroup(title = "首页内容") {
                                    BasicListItem(
                                        leadingImageVector = MiuixIcons.All,
                                        headlineText = "首页板块",
                                        supportingText = "选择首页显示的内容 Tab",
                                        onClick = onHomeTabsClick,
                                    )
                                    SwitchListItem(
                                        value = prefs.showSquare,
                                        leadingImageVector = MiuixIcons.Notes,
                                        headlineText = "头条显示广场",
                                    ) {
                                        viewModel.setShowSquare(it)
                                    }
                                    SwitchListItem(
                                        value = prefs.showEmoji,
                                        leadingImageVector = MiuixIcons.EmojiEmotions,
                                        headlineText = "显示表情",
                                    ) {
                                        viewModel.setShowEmoji(it)
                                    }
                                }
                            }
                            item(key = "Content_1") {
                                PreferenceGroup(title = "关注") {
                                    BasicListItem(
                                        headlineText = "本地 / 悄悄关注",
                                        supportingText = "管理本机名单与长按关注行为",
                                        leadingImageVector =
                                            com.example.c001apk.compose.ui.component.icons
                                                .FlutterProfileIcons
                                                .Favorite,
                                        onClick = onLocalFollowsClick,
                                    )
                                }
                            }
                            item(key = "Content_2") {
                                PreferenceGroup(title = "屏蔽") {
                                    BasicListItem(
                                        leadingImageVector = MiuixIcons.Blocklist,
                                        headlineText = "用户黑名单",
                                    ) {
                                        onViewBlackList(BlackListType.USER.name)
                                    }
                                    BasicListItem(
                                        leadingImageVector = MiuixIcons.Blocklist,
                                        headlineText = "话题黑名单",
                                    ) {
                                        onViewBlackList(BlackListType.TOPIC.name)
                                    }
                                }
                            }
                        }
                        SettingsSection.Privacy -> {
                            item(key = "Privacy_0") {
                                PreferenceGroup(title = "图片") {
                                    DropdownListItem(
                                        leadingImageVector = MiuixIcons.Image,
                                        headlineText = "图片画质",
                                        value = prefs.imageQuality,
                                        selections =
                                            imageQualityList.mapIndexed { index, label ->
                                                SelectionItem(label, index)
                                            },
                                        onValueChanged = { index, _ ->
                                            viewModel.setImageQuality(index)
                                        },
                                    )
                                }
                            }
                            item(key = "Privacy_1") {
                                PreferenceGroup(title = "浏览") {
                                    SwitchListItem(
                                        value = prefs.openInBrowser,
                                        leadingImageVector = MiuixIcons.WorldClock,
                                        headlineText = "使用外部浏览器打开链接",
                                    ) {
                                        viewModel.setOpenInBrowser(it)
                                    }
                                    SwitchListItem(
                                        value = prefs.recordHistory,
                                        leadingImageVector = MiuixIcons.Recent,
                                        headlineText = "记录浏览历史",
                                    ) {
                                        viewModel.setRecordHistory(it)
                                    }
                                }
                            }
                        }
                        SettingsSection.Storage -> {
                            item(key = "Storage_0") {
                                PreferenceGroup(title = "缓存与应用") {
                                    BasicListItem(
                                        leadingImageVector = MiuixIcons.Delete,
                                        headlineText = "清理缓存",
                                        supportingText = cacheSize,
                                    ) {
                                        cacheSize = getTotalCacheSize(context)
                                        showCleanCacheDialog = true
                                    }
                                    SwitchListItem(
                                        value = prefs.checkUpdate,
                                        leadingImageVector = MiuixIcons.Update,
                                        headlineText = "检查已安装应用更新",
                                    ) {
                                        viewModel.setCheckUpdate(it)
                                    }
                                }
                            }
                            item(key = "Storage_1") {
                                PreferenceGroup(title = "c001apk 更新") {
                                    com.example.c001apk.compose.ui.update.AppUpdateSettings()
                                }
                            }
                        }
                        SettingsSection.Device -> {
                            item(key = "Device_0") {
                                PreferenceGroup(title = "协议与设备") {
                                    BasicListItem(
                                        leadingImageVector = MiuixIcons.Smartphone,
                                        headlineText = "数字联盟ID",
                                        supportingText =
                                            if (prefs.szlmId.isBlank()) "尚未生成，点击获取" else "已保存，点击管理",
                                    ) {
                                        showSZLMIDDialog = true
                                    }
                                    BasicListItem(
                                        leadingImageVector = MiuixIcons.Smartphone,
                                        headlineText = "机型参数",
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
                    title = "自定义主题颜色",
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
                    title = "字体大小",
                    hint = "比例",
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
                    title = "内容缩放",
                    hint = "比例",
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
                    title = "触感强度",
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
            Text(text = "当前缓存大小: " + cacheSize)
        },
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "清理缓存",
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
        dialogButtonString = "取消",
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

private fun themeTypeLabelRes(themeType: ThemeType): Int =
    when (themeType) {
        ThemeType.Default -> R.string.theme_type_default
        ThemeType.Red -> R.string.theme_type_red
        ThemeType.Pink -> R.string.theme_type_pink
        ThemeType.Purple -> R.string.theme_type_purple
        ThemeType.Indigo -> R.string.theme_type_indigo
        ThemeType.Blue -> R.string.theme_type_blue
        ThemeType.LightBlue -> R.string.theme_type_light_blue
        ThemeType.Cyan -> R.string.theme_type_cyan
        ThemeType.Teal -> R.string.theme_type_teal
        ThemeType.Green -> R.string.theme_type_green
        ThemeType.LightGreen -> R.string.theme_type_light_green
        ThemeType.Lime -> R.string.theme_type_lime
        ThemeType.Yellow -> R.string.theme_type_yellow
        ThemeType.Amber -> R.string.theme_type_amber
        ThemeType.Orange -> R.string.theme_type_orange
        ThemeType.DeepOrange -> R.string.theme_type_deep_orange
        ThemeType.Brown -> R.string.theme_type_brown
        ThemeType.BlueGrey -> R.string.theme_type_blue_grey
        ThemeType.Sakura -> R.string.theme_type_sakura
        ThemeType.Custom -> R.string.theme_type_custom
        ThemeType.UNRECOGNIZED -> R.string.theme_type_default
    }

private fun paletteStyleLabelRes(style: PaletteStyle): Int =
    when (style) {
        PaletteStyle.TonalSpot -> R.string.palette_style_tonal_spot
        PaletteStyle.Neutral -> R.string.palette_style_neutral
        PaletteStyle.Vibrant -> R.string.palette_style_vibrant
        PaletteStyle.Expressive -> R.string.palette_style_expressive
        PaletteStyle.Rainbow -> R.string.palette_style_rainbow
        PaletteStyle.FruitSalad -> R.string.palette_style_fruit_salad
        PaletteStyle.Monochrome -> R.string.palette_style_monochrome
        PaletteStyle.Fidelity -> R.string.palette_style_fidelity
        PaletteStyle.Content -> R.string.palette_style_content
    }
