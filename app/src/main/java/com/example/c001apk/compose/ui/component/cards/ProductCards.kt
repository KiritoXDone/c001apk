package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.component.CoilLoader
import com.example.c001apk.compose.ui.component.MiuixActionButton as TextButton
import top.yukonga.miuix.kmp.basic.Button as OutlinedButton
import top.yukonga.miuix.kmp.basic.Card as Surface
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Check
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.glass.ChevronForward
import top.yukonga.miuix.kmp.icon.glass.SettingsOutline
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ProductConfigListCard(
    data: HomeFeedResponse.Data,
    configRows: List<HomeFeedResponse.ProductConfig>,
    selectedCompareIds: Set<String>,
    onViewConfig: (HomeFeedResponse.ProductConfig) -> Unit,
    onToggleCompare: (HomeFeedResponse.ProductConfig) -> Unit,
    onShowComparison: () -> Unit,
    onShowGlobalComparison: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Row(
                modifier =
                    Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = data.title.orEmpty(),
                    modifier = Modifier.weight(1f),
                    style = MiuixTheme.textStyles.subtitle,
                    fontWeight = FontWeight.SemiBold,
                )
                TextButton(onClick = onShowComparison) {
                    Text(stringResource(R.string.product_compare))
                    Icon(
                        imageVector = MiuixIcons.Glass.ChevronForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            configRows.forEachIndexed { index, config ->
                if (index > 0) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
                ProductConfigRow(
                    config = config,
                    selectedForCompare = config.id in selectedCompareIds,
                    onViewConfig = onViewConfig,
                    onToggleCompare = onToggleCompare,
                )
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            TextButton(
                onClick = onShowGlobalComparison,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            ) {
                Text(stringResource(R.string.product_compare_cross, selectedCompareIds.size))
                Icon(
                    imageVector = MiuixIcons.Glass.ChevronForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun ProductConfigRow(
    config: HomeFeedResponse.ProductConfig,
    selectedForCompare: Boolean,
    onViewConfig: (HomeFeedResponse.ProductConfig) -> Unit,
    onToggleCompare: (HomeFeedResponse.ProductConfig) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = config.title.orEmpty(),
                style = MiuixTheme.textStyles.subtitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = config.price.asPrice(),
                style = MiuixTheme.textStyles.main,
                color =
                    if (config.price.isPositivePrice()) {
                        MiuixTheme.colorScheme.error
                    } else {
                        MiuixTheme.colorScheme.onSurfaceVariantSummary
                    },
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (!config.id.isNullOrBlank()) {
                OutlinedButton(
                    modifier = Modifier.height(36.dp),
                    insideMargin = PaddingValues(horizontal = 10.dp),
                    onClick = { onViewConfig(config) },
                ) {
                    Icon(
                        MiuixIcons.Glass.SettingsOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.params_title), style = MiuixTheme.textStyles.footnote1)
                }
            }
            if (!config.id.isNullOrBlank()) {
                OutlinedButton(
                    modifier = Modifier.height(36.dp),
                    insideMargin = PaddingValues(horizontal = 10.dp),
                    onClick = { onToggleCompare(config) },
                ) {
                    Icon(
                        if (selectedForCompare) MiuixIcons.Basic.Check else MiuixIcons.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        stringResource(if (selectedForCompare) R.string.product_compare_added else R.string.product_compare_action),
                        style = MiuixTheme.textStyles.footnote1,
                    )
                }
            }
        }
    }
}

@Composable
fun ProductListCard(
    data: HomeFeedResponse.Data,
    onOpenLink: (String, String?) -> Unit,
    selectedCompareIds: Set<String>,
    onToggleCompare: (HomeFeedResponse.Entities) -> Unit,
    modifier: Modifier = Modifier,
) {
    val products = data.entities.orEmpty()
    if (products.isEmpty()) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            TitleCard(
                modifier = Modifier.padding(bottom = 8.dp),
                url = data.url.orEmpty(),
                title = data.title.orEmpty(),
                onOpenLink = onOpenLink,
            )
            products.forEachIndexed { index, product ->
                if (index > 0) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
                ProductListRow(
                    product = product,
                    selectedForCompare = product.configId in selectedCompareIds,
                    onOpenLink = onOpenLink,
                    onToggleCompare = onToggleCompare,
                )
            }
        }
    }
}

@Composable
private fun ProductListRow(
    product: HomeFeedResponse.Entities,
    selectedForCompare: Boolean,
    onOpenLink: (String, String?) -> Unit,
    onToggleCompare: (HomeFeedResponse.Entities) -> Unit,
) {
    val description =
        product.configName?.takeIf { it.isNotBlank() }
            ?: product.productSpecs.orEmpty().filter { it.isNotBlank() }.take(2).joinToString(" · ")
    val price = productPrice(product)

    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .clickable(enabled = !product.url.isNullOrBlank()) {
                    onOpenLink(product.url.orEmpty(), product.title)
                }
                .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoilLoader(
            url = product.logo,
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
        )
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(
                text = product.title.orEmpty(),
                style = MiuixTheme.textStyles.headline2,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (description.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = description,
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = price,
                    modifier = Modifier.weight(1f),
                    style = MiuixTheme.textStyles.body1,
                    color =
                        if (!product.priceMin.isPositivePrice() && !product.priceMax.isPositivePrice()) {
                            MiuixTheme.colorScheme.onSurfaceVariantSummary
                        } else {
                            MiuixTheme.colorScheme.error
                        },
                )
                if (!product.configId.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = { onToggleCompare(product) },
                        modifier = Modifier.height(32.dp),
                        insideMargin = PaddingValues(horizontal = 9.dp),
                    ) {
                        Icon(
                            if (selectedForCompare) MiuixIcons.Basic.Check else MiuixIcons.Add,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(stringResource(if (selectedForCompare) R.string.product_compare_added else R.string.product_compare_action))
                    }
                }
            }
        }
    }
}

@Composable
private fun productPrice(product: HomeFeedResponse.Entities): String {
    val min = product.priceMin.takeIf { it.isPositivePrice() }
    val max = product.priceMax.takeIf { it.isPositivePrice() }
    if (min == null && max == null) return stringResource(R.string.product_no_price)

    val currency = product.priceCurrency?.takeIf { it.isNotBlank() } ?: "¥"
    return when {
        min == null -> "$currency$max"
        max == null || min == max -> "$currency$min"
        else -> "$currency$min - $currency$max"
    }
}

@Composable
private fun String?.asPrice(): String = takeIf { it.isPositivePrice() }?.let { "¥$it" } ?: stringResource(R.string.product_no_price)

private fun String?.isPositivePrice(): Boolean =
    !isNullOrBlank() && (toBigDecimalOrNull()?.signum() ?: 0) > 0
