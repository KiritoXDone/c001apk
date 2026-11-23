package com.example.c001apk.compose.ui.topic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.c001apk.compose.ui.component.rememberHapticClick
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.SinkFeedback

@Composable
internal fun TopicSortOptions(
    options: List<TopicSortOption>,
    selected: ProductSortType,
    onSelect: (ProductSortType) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp).selectableGroup(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options
            .forEach { (sort, _) ->
                val active = selected == sort
                Box(
                    Modifier.selectable(
                            selected = active,
                            role = Role.RadioButton,
                            interactionSource = null,
                            indication = SinkFeedback(),
                            onClick = rememberHapticClick { if (!active) onSelect(sort) },
                        )
                        .sizeIn(minWidth = 48.dp, minHeight = 36.dp)
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(when (sort) {
                            ProductSortType.REPLY -> R.string.theme_type_default
                            ProductSortType.HOT -> R.string.search_order_hot
                            ProductSortType.DATELINE -> R.string.label_latest
                            ProductSortType.OWNER -> R.string.product_sort_owner
                            ProductSortType.GOOD -> R.string.product_sort_good
                            ProductSortType.BAD -> R.string.product_sort_bad
                        }),
                        fontSize = 13.sp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                        color =
                            if (active) MiuixTheme.colorScheme.primary
                            else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                    )
                }
            }
    }
}
