package com.example.c001apk.compose.ui.topic

import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.c001apk.compose.logic.model.ProductSpecItem
import com.example.c001apk.compose.logic.model.ProductSpecSection
import com.example.c001apk.compose.ui.component.CoilLoader
import com.example.c001apk.compose.ui.component.MiuixActionButton as TextButton
import com.example.c001apk.compose.ui.component.MiuixBottomSheet as ModalBottomSheet
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.VerticalDivider
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Close
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Share
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ProductConfigSheet(
    state: ProductConfigSheetState,
    topPadding: Dp,
    onDismiss: () -> Unit,
    onRemoveComparison: (String) -> Unit,
) {
    if (state is ProductConfigSheetState.Hidden) return
    if (state is ProductConfigSheetState.Comparison) {
        Dialog(
            onDismissRequest = onDismiss,
            properties =
                DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false,
                ),
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MiuixTheme.colorScheme.background,
            ) {
                ComparisonContent(
                    state = state,
                    onDismiss = onDismiss,
                    onRemoveComparison = onRemoveComparison,
                )
            }
        }
        return
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.padding(top = topPadding),
    ) {
        when (state) {
            ProductConfigSheetState.Hidden,
            is ProductConfigSheetState.Comparison -> Unit
            is ProductConfigSheetState.Loading -> LoadingContent(state.title)
            is ProductConfigSheetState.Error -> ErrorContent(state.message, onDismiss)
            is ProductConfigSheetState.Details -> DetailsContent(state.title, state.sections)
        }
    }
}

@Composable
private fun SheetTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        style = MiuixTheme.textStyles.title3,
        fontWeight = FontWeight.SemiBold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun LoadingContent(title: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SheetTitle(title.ifBlank { "产品参数" })
        Box(
            modifier = Modifier.fillMaxWidth().height(180.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun ErrorContent(message: String, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("加载失败", style = MiuixTheme.textStyles.title3)
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onDismiss) { Text("关闭") }
    }
}

@Composable
private fun DetailsContent(title: String, sections: List<ProductSpecSection>) {
    Column(modifier = Modifier.fillMaxSize()) {
        SheetTitle(title.ifBlank { "产品参数" })
        HorizontalDivider()
        if (sections.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text("暂无参数", color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                sections.forEach { section ->
                    item(key = "section_${section.title}") {
                        SectionHeader(section.title)
                    }
                    var previousGroup: String? = null
                    section.items.forEachIndexed { index, spec ->
                        if (!spec.group.isNullOrBlank() && spec.group != previousGroup) {
                            item(key = "section_${section.title}_group_${spec.group}_$index") {
                                SpecGroupHeader(spec.group.orEmpty())
                            }
                        }
                        item(key = "${section.title}_${spec.group}_${spec.name}_$index") {
                            SpecRow(spec.name, spec.value)
                        }
                        previousGroup = spec.group
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonContent(
    state: ProductConfigSheetState.Comparison,
    onDismiss: () -> Unit,
    onRemoveComparison: (String) -> Unit,
) {
    val context = LocalContext.current
    val horizontalState = rememberScrollState()
    var hideSame by rememberSaveable(state.mode) { mutableStateOf(false) }
    val sections =
        (if (state.columns.isEmpty()) {
                EMPTY_COMPARISON_SECTIONS
            } else {
                mergeSections(state.columns)
            })
            .map { section ->
                if (!hideSame) section
                else
                    section.copy(
                        items = section.items.filterNot { it.values.hasSameValues() },
                    )
            }
            .filter { it.items.isNotEmpty() }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onDismiss) {
                Icon(MiuixIcons.Basic.Close, contentDescription = "关闭")
            }
            Text(
                text = state.title,
                modifier = Modifier.weight(1f),
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            IconButton(
                enabled = state.columns.isNotEmpty(),
                onClick = {
                    val text = buildString {
                        appendLine(state.title)
                        state.columns.forEach { column ->
                            appendLine(column.selection.displayTitle(state.mode))
                        }
                    }
                        .trim()
                    val intent =
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                    context.startActivity(Intent.createChooser(intent, state.title))
                },
            ) {
                Icon(MiuixIcons.Share, contentDescription = "分享")
            }
        }
        HorizontalDivider()

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "comparison_header") {
                ComparisonHeader(
                    state = state,
                    hideSame = hideSame,
                    onHideSameChange = { hideSame = it },
                    horizontalState = horizontalState,
                    onAddComparison = onDismiss,
                    onRemoveComparison = onRemoveComparison,
                )
            }
            sections.forEach { section ->
                item(key = "compare_section_${section.title}") {
                    SectionHeader(section.title)
                }
                var previousGroup: String? = null
                section.items.forEachIndexed { index, comparisonItem ->
                    if (
                        !comparisonItem.group.isNullOrBlank() &&
                            comparisonItem.group != previousGroup
                    ) {
                        item(
                            key = "compare_${section.title}_group_${comparisonItem.group}_$index",
                        ) {
                            ComparisonGroupHeader(
                                title = comparisonItem.group.orEmpty(),
                                columnCount = state.columns.size,
                                horizontalState = horizontalState,
                                includeAddColumn =
                                    state.mode == ProductComparisonMode.CROSS_PRODUCT &&
                                        state.columns.size < MAX_GLOBAL_COMPARISON_COUNT,
                            )
                        }
                    }
                    item(
                        key =
                            "compare_${section.title}_${comparisonItem.group}_${comparisonItem.name}_$index",
                    ) {
                        ComparisonSpecRow(
                            item = comparisonItem,
                            horizontalState = horizontalState,
                            includeAddColumn =
                                state.mode == ProductComparisonMode.CROSS_PRODUCT &&
                                    state.columns.size < MAX_GLOBAL_COMPARISON_COUNT,
                        )
                    }
                    previousGroup = comparisonItem.group
                }
            }
        }
    }
}

@Composable
private fun ComparisonHeader(
    state: ProductConfigSheetState.Comparison,
    hideSame: Boolean,
    onHideSameChange: (Boolean) -> Unit,
    horizontalState: androidx.compose.foundation.ScrollState,
    onAddComparison: () -> Unit,
    onRemoveComparison: (String) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth().height(COMPARISON_HEADER_HEIGHT)) {
        Column(
            modifier = Modifier.width(PARAMETER_COLUMN_WIDTH).fillMaxHeight().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Switch(
                checked = hideSame,
                onCheckedChange = onHideSameChange,
                enabled = state.columns.size > 1,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "隐藏相同配置",
                style = MiuixTheme.textStyles.body1,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${state.columns.size}款配置对比",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
        VerticalDivider(
            modifier = Modifier.fillMaxHeight().width(1.dp),
            color = MiuixTheme.colorScheme.dividerLine,
        )
        Row(modifier = Modifier.horizontalScroll(horizontalState)) {
            state.columns.forEach { column ->
                ProductComparisonHeaderColumn(
                    column = column,
                    mode = state.mode,
                    onRemove =
                        if (state.mode == ProductComparisonMode.CROSS_PRODUCT) {
                            { column.selection.config.id?.let(onRemoveComparison) }
                        } else null,
                )
            }
            if (
                state.mode == ProductComparisonMode.CROSS_PRODUCT &&
                    state.columns.size < MAX_GLOBAL_COMPARISON_COUNT
            ) {
                Column(
                    modifier = Modifier.width(VALUE_COLUMN_WIDTH).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    TextButton(onClick = onAddComparison) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                MiuixIcons.Add,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                            )
                            Text("添加对比")
                        }
                    }
                }
            }
        }
    }
    HorizontalDivider()
}

@Composable
private fun ProductComparisonHeaderColumn(
    column: ProductComparisonColumn,
    mode: ProductComparisonMode,
    onRemove: (() -> Unit)?,
) {
    val config = column.selection.config
    Column(
        modifier =
            Modifier.width(VALUE_COLUMN_WIDTH)
                .fillMaxHeight()
                .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(72.dp)) {
            CoilLoader(
                url = column.selection.productLogo,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(72.dp).align(Alignment.Center),
            )
            onRemove?.let {
                IconButton(
                    onClick = it,
                    modifier = Modifier.size(28.dp).align(Alignment.TopEnd),
                ) {
                    Icon(
                        MiuixIcons.Basic.Close,
                        contentDescription = "移除",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        Text(
            text = column.selection.displayTitle(mode),
            style = MiuixTheme.textStyles.headline2,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (mode == ProductComparisonMode.CROSS_PRODUCT) {
            Text(
                text = config.title.orEmpty(),
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = config.price.asComparisonPrice(),
            style = MiuixTheme.textStyles.headline2,
            color =
                if (config.price.isPositivePrice()) MiuixTheme.colorScheme.error
                else MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun ComparisonSpecRow(
    item: ComparisonItem,
    horizontalState: androidx.compose.foundation.ScrollState,
    includeAddColumn: Boolean,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier =
                Modifier.width(PARAMETER_COLUMN_WIDTH)
                    .heightIn(min = 64.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = item.name,
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
        Row(modifier = Modifier.horizontalScroll(horizontalState)) {
            item.values.forEach { value ->
                Box(
                    modifier =
                        Modifier.width(VALUE_COLUMN_WIDTH)
                            .heightIn(min = 64.dp)
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(text = value, style = MiuixTheme.textStyles.body1)
                }
            }
            if (includeAddColumn) {
                Spacer(modifier = Modifier.width(VALUE_COLUMN_WIDTH))
            }
        }
    }
    HorizontalDivider()
}

@Composable
private fun ComparisonGroupHeader(
    title: String,
    columnCount: Int,
    horizontalState: androidx.compose.foundation.ScrollState,
    includeAddColumn: Boolean,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier =
                Modifier.width(PARAMETER_COLUMN_WIDTH).padding(horizontal = 16.dp, vertical = 9.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = title,
                style = MiuixTheme.textStyles.button,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Row(modifier = Modifier.horizontalScroll(horizontalState)) {
            Spacer(
                modifier =
                    Modifier.width(
                        VALUE_COLUMN_WIDTH *
                            (columnCount + if (includeAddColumn) 1 else 0).toFloat(),
                    ),
            )
        }
    }
    HorizontalDivider()
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        style = MiuixTheme.textStyles.subtitle,
        fontWeight = FontWeight.SemiBold,
        color = MiuixTheme.colorScheme.primary,
    )
}

@Composable
private fun SpecGroupHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        style = MiuixTheme.textStyles.button,
        fontWeight = FontWeight.SemiBold,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
    )
}

@Composable
private fun SpecRow(name: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = name,
            modifier = Modifier.weight(0.38f),
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Text(text = value, modifier = Modifier.weight(0.62f), style = MiuixTheme.textStyles.body1)
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
}

private data class ComparisonSection(
    val title: String,
    val items: List<ComparisonItem>,
)

private data class ComparisonItem(
    val name: String,
    val values: List<String>,
    val group: String? = null,
)

private val EMPTY_COMPARISON_SECTIONS =
    listOf(
        ComparisonSection(
            title = "重要参数",
            items = listOf("性能", "芯片", "RAM & ROM").map { ComparisonItem(it, emptyList()) },
        ),
        ComparisonSection(
            title = "屏幕",
            items = listOf("屏幕").map { ComparisonItem(it, emptyList()) },
        ),
        ComparisonSection(
            title = "影像",
            items =
                listOf(
                        "影像联名",
                        "后置主摄",
                        "后置长焦①",
                        "后置长焦②",
                        "后置超广角",
                        "前置主摄",
                        "前置副摄",
                    )
                    .map { ComparisonItem(it, emptyList()) },
        ),
        ComparisonSection(
            title = "充电与电池",
            items =
                listOf("电池容量", "有线充电", "无线充电").map {
                    ComparisonItem(it, emptyList())
                },
        ),
    )

private fun mergeSections(
    columns: List<ProductComparisonColumn>,
): List<ComparisonSection> {
    val sectionKeys = linkedSetOf<String>()
    columns.forEach { column ->
        column.sections.forEach { sectionKeys += it.title.normalizedSpecKey() }
    }
    val sectionMaps = columns.map { column ->
        column.sections.associateBy { it.title.normalizedSpecKey() }
    }

    return sectionKeys.mapNotNull { sectionKey ->
        val sections = sectionMaps.map { it[sectionKey] }
        val itemKeys = linkedSetOf<String>()
        sections.forEach { section ->
            section?.items?.forEach { itemKeys += it.comparisonKey() }
        }
        val itemMaps = sections.map { section ->
            section?.items.orEmpty().associateBy { it.comparisonKey() }
        }
        val items = itemKeys.map { itemKey ->
            val source = itemMaps.firstNotNullOfOrNull { it[itemKey] }
            ComparisonItem(
                name = source?.name.orEmpty(),
                values =
                    itemMaps.map { itemsByName ->
                        itemsByName[itemKey]?.value ?: "-"
                    },
                group = source?.group,
            )
        }
        items
            .takeIf { it.isNotEmpty() }
            ?.let {
                ComparisonSection(
                    title = sections.firstNotNullOfOrNull { it?.title }.orEmpty(),
                    items = it,
                )
            }
    }
}

private fun ProductSpecItem.comparisonKey(): String =
    "${group.orEmpty().normalizedSpecKey()}\u0000${name.normalizedSpecKey()}"

private fun List<String>.hasSameValues(): Boolean =
    size > 1 && map { it.normalizedSpecKey() }.distinct().size == 1

private fun com.example.c001apk.compose.logic.model.ProductComparisonSelection.displayTitle(
    mode: ProductComparisonMode,
): String =
    if (mode == ProductComparisonMode.CURRENT_PRODUCT) {
        config.title.orEmpty()
    } else {
        productTitle
    }

private fun String.normalizedSpecKey(): String = trim().replace(Regex("\\s+"), "").lowercase()

private fun String?.asComparisonPrice(): String =
    takeIf { it.isPositivePrice() }?.let { "¥$it" } ?: "暂无价格"

private fun String?.isPositivePrice(): Boolean =
    !isNullOrBlank() && (toBigDecimalOrNull()?.signum() ?: 0) > 0

private val PARAMETER_COLUMN_WIDTH = 96.dp
private val VALUE_COLUMN_WIDTH = 144.dp
private val COMPARISON_HEADER_HEIGHT = 220.dp
private const val MAX_GLOBAL_COMPARISON_COUNT = 10
