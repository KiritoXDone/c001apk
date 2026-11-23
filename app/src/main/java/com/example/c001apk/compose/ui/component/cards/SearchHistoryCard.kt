package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.theme.cardBg
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Created by bggRGjQaUbCoE on 2024/6/16 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchHistoryCard(
    modifier: Modifier = Modifier,
    data: String,
    onSearch: () -> Unit,
    onDelete: () -> Unit,
) {

    Text(
        modifier =
            modifier
                .clip(RoundedCornerShape(20.dp))
                .background(cardBg())
                .combinedClickable(
                    onClick = onSearch,
                    onLongClick = onDelete,
                    onLongClickLabel = stringResource(R.string.search_delete_history),
                    role = Role.Button,
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
        text = data,
        style = MiuixTheme.textStyles.body1,
    )
}
