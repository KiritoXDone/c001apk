package com.example.c001apk.compose.ui.carousel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.c001apk.compose.logic.model.ProductCatalogHeader
import com.example.c001apk.compose.logic.model.TopicBean
import com.example.c001apk.compose.ui.component.CoilLoader
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun ProductCatalogHeader(
    header: ProductCatalogHeader,
    onOpenLink: (String, String?) -> Unit,
    onSort: (TopicBean) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        if (header.brands.isNotEmpty()) {
            var expanded by rememberSaveable { mutableStateOf(false) }
            BoxWithConstraints {
                val columns = (maxWidth / 64.dp).toInt().coerceAtLeast(1)
                val visible = if (expanded) header.brands else header.brands.take(columns * 2)
                Column {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(R.string.label_brand), modifier = Modifier.weight(1f), fontSize = 14.sp)
                        if (header.brands.size > columns * 2) {
                            Text(
                                stringResource(if (expanded) R.string.action_collapse else R.string.label_all),
                                modifier = Modifier.clickable(
                                    role = Role.Button,
                                    onClickLabel = stringResource(if (expanded) R.string.brand_collapse else R.string.brand_expand),
                                ) { expanded = !expanded }.padding(12.dp),
                                fontSize = 12.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                            )
                        }
                    }
                    visible.chunked(columns).forEach { row ->
                        Row(Modifier.fillMaxWidth()) {
                            row.forEach { brand ->
                                Column(
                                    Modifier.weight(1f).clickable(role = Role.Button) {
                                        onOpenLink(brand.page.url, brand.page.title)
                                    }.padding(horizontal = 4.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    CoilLoader(
                                        url = brand.logo,
                                        modifier = Modifier.size(width = 48.dp, height = 28.dp),
                                        contentScale = ContentScale.Fit,
                                    )
                                    Text(
                                        brand.page.title,
                                        modifier = Modifier.padding(top = 4.dp),
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
        if (header.sorts.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).selectableGroup(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                header.sorts.forEach { option ->
                    val selected = option.url == header.selectedSort
                    Box(
                        Modifier.selectable(selected, role = Role.RadioButton) {
                            if (!selected) onSort(option)
                        }.sizeIn(minWidth = 48.dp, minHeight = 36.dp).padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            option.title,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) MiuixTheme.colorScheme.primary
                                else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
        }
    }
}
