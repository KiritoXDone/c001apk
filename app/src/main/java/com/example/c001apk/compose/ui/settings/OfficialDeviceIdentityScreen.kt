package com.example.c001apk.compose.ui.settings

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.logic.repository.UserPreferencesRepository
import com.example.c001apk.compose.logic.smid.OfficialDeviceIdentity
import com.example.c001apk.compose.ui.component.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.*

@HiltViewModel
class OfficialDeviceIdentityViewModel
@Inject
constructor(private val preferences: UserPreferencesRepository) : ViewModel() {
    var pending by mutableStateOf<String?>(null)
        private set

    var error by mutableStateOf<String?>(null)
    var saved by mutableStateOf(false)
        private set

    var saving by mutableStateOf(false)
        private set

    fun recognize(text: String): Boolean {
        saved = false
        pending = null
        return try {
            pending = OfficialDeviceIdentity.parse(text)
            error = null
            true
        } catch (e: IllegalArgumentException) {
            error = e.message
            false
        }
    }

    fun save() {
        val id = pending ?: return
        if (saving) return
        saving = true
        viewModelScope.launch {
            try {
                preferences.setSZLMId(id)
                pending = null
                saved = true
                error = null
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                error = "保存失败，请重试"
            } finally {
                saving = false
            }
        }
    }
}

@Composable
fun OfficialDeviceIdentityScreen(onBack: () -> Unit) {
    val model = hiltViewModel<OfficialDeviceIdentityViewModel>()
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    fun recognize(text: String) {
        input = if (model.recognize(text)) "" else text.take(32768)
    }
    MiuixScaffold(
        topBar = { MiuixTopBar(title = "导入官方设备标识", navigationIcon = { BackButton(onBack) }) },
    ) { padding ->
        Column(
            Modifier.fillMaxSize()
                .verticalScroll(rememberTopBarScrollState())
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("1. 打开官方酷安的“检查日志和测试”页。\n2. 在同一页面累计点击“复制 OAID”5 次。\n3. 返回此处粘贴完整内容，识别后保存。")
            Button(
                onClick = {
                    try {
                        context.startActivity(
                            Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("coolmarket://www.coolapk.com/native/testSettings"),
                                )
                                .setPackage("com.coolapk.market"),
                        )
                    } catch (_: Exception) {
                        model.error = "无法打开官方酷安，请确认已安装并启用支持此入口的版本"
                    }
                },
            ) {
                Text("打开酷安 OAID 界面")
            }
            Text("仅识别 ShuzlmID（数盟 ID），忽略 OAID、AndroidID 和 UUID。其他设备参数与账号保持原值。")
            TextField(
                value = input,
                onValueChange = { recognize(it) },
                label = "粘贴官方复制的完整文本",
                modifier = Modifier.fillMaxWidth(),
                enabled = !model.saving,
                minLines = 2,
                maxLines = 4,
            )
            if (model.pending != null) Text("已识别数盟 ID，复制内容已隐藏。")
            if (model.saved) Text("已保存，返回内容页后刷新即可。")
            model.error?.let { Text(it) }
            Button(
                onClick = {
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val text =
                        clipboard.primaryClip
                            ?.getItemAt(0)
                            ?.coerceToText(context)
                            ?.toString()
                            .orEmpty()
                    if (text.isBlank()) model.error = "剪贴板没有文本" else recognize(text)
                },
                enabled = !model.saving,
            ) {
                Text("粘贴并识别")
            }
            Button(onClick = model::save, enabled = model.pending != null && !model.saving) {
                Text(if (model.saving) "正在保存…" else "保存并应用")
            }
        }
    }
}
