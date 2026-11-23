package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.c001apk.compose.logic.model.FeedPriceDisplay
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.model.PurchasePriceInfo
import com.example.c001apk.compose.logic.model.TradeLink
import com.example.c001apk.compose.logic.model.asProductPrice
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Link
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun FeedProductInfo(
    price: FeedPriceDisplay?,
    link: TradeLink?,
    isFeedContent: Boolean,
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier,
    showLinkHint: Boolean = false,
    onDismissLinkHint: () -> Unit = {},
) {
    if (price == null && link == null) return
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        if (price != null) {
            FeedPriceRow(
                price,
                trailingContent = if (link != null) {
                    { TradeLinkIcon(link, isFeedContent, onOpenLink) }
                } else null,
            )
        } else if (link != null) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TradeLinkIcon(link, isFeedContent, onOpenLink)
            }
        }
        if (isFeedContent && link != null && showLinkHint) {
            Row(
                Modifier.fillMaxWidth().padding(top = 6.dp)
                    .border(1.dp, MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "点击图标即可跳转二手平台",
                    modifier = Modifier.weight(1f),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Text(
                    "不再显示",
                    modifier = Modifier.clickable(role = Role.Button, onClick = onDismissLinkHint)
                        .padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun TradeLinkIcon(link: TradeLink, clickable: Boolean, onOpenLink: (String) -> Unit) {
    val graphic: @Composable () -> Unit = {
        val size = if (clickable) 32.dp else 24.dp
        val description = if (clickable) link.label else link.previewLabel
        if (link.isXianyu) {
            Image(painterResource(R.drawable.ic_xianyu), description, Modifier.size(size))
        } else {
            Icon(MiuixIcons.Link, description, Modifier.size(size), tint = MiuixTheme.colorScheme.primary)
        }
    }
    if (clickable) IconButton(onClick = { onOpenLink(link.url) }, minWidth = 40.dp, minHeight = 40.dp) { graphic() }
    else graphic()
}

@Composable
fun FeedPriceRow(
    price: FeedPriceDisplay,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val inlineConfiguration = price.inlineConfiguration && price.configuration != null
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(price.label, style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            Text(
                price.amount,
                modifier = if (inlineConfiguration) Modifier.widthIn(max = 144.dp)
                    else Modifier.weight(1f, fill = trailingContent != null),
                style = MiuixTheme.textStyles.main.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                color = MiuixTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (inlineConfiguration) {
                Text(
                    price.configuration.orEmpty(),
                    modifier = Modifier.weight(1f),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            trailingContent?.invoke()
        }
        price.configuration?.takeUnless { inlineConfiguration }?.let {
            Text(it, style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        }
    }
}

@Composable
fun PurchasePriceSummaryCard(
    summary: PurchasePriceInfo,
    onViewFeed: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val amount = summary.finalPrice.asProductPrice() ?: return
    val feedId = summary.feedId?.takeIf { it.toLongOrNull()?.let { id -> id > 0 } == true }
    Card(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        onClick = feedId?.let { { onViewFeed(it, false) } },
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FeedPriceRow(FeedPriceDisplay("到手价参考", amount, null))
            summary.updateTime?.takeIf { it.isNotBlank() }?.let {
                Text("更新于 $it", style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
            if (feedId != null) Text("查看价格来源", style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.primary)
        }
    }
}
