package com.example.c001apk.compose.ui.component

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.WindowManager
import androidx.activity.ComponentDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.theme.C001apkViewTheme
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

// View 编辑器使用独立窗口，尺寸与内容位置统一交给 Compose 测量。
fun createMiuixLoadingDialog(context: Context): ComponentDialog =
    ComponentDialog(context).apply {
        setCancelable(false)
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setGravity(Gravity.CENTER)
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.16f)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        }
        setContentView(
            ComposeView(context).apply {
                setContent {
                    C001apkViewTheme {
                        Card(cornerRadius = 20.dp, insideMargin = PaddingValues(0.dp)) {
                            Row(
                                modifier = Modifier.widthIn(min = 164.dp)
                                    .heightIn(min = 60.dp)
                                    .padding(horizontal = 18.dp, vertical = 16.dp)
                                    .semantics(mergeDescendants = true) {
                                        progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                            ) {
                                InfiniteProgressIndicator(
                                    size = 20.dp,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                                Text("正在发送…", style = MiuixTheme.textStyles.main)
                            }
                        }
                    }
                }
            },
        )
        setOnShowListener {
            window?.setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
            )
        }
    }
