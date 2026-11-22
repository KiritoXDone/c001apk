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
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.*
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R

@HiltViewModel
class OfficialDeviceIdentityViewModel
@Inject
constructor(
    private val preferences: UserPreferencesRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
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
                error = context.getString(R.string.settings_device_save_failed)
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
    val openFailedMessage = stringResource(R.string.settings_device_import_error)
    val clipboardEmptyMessage = stringResource(R.string.settings_device_clipboard_empty)
    fun recognize(text: String) {
        input = if (model.recognize(text)) "" else text.take(32768)
    }
    MiuixScaffold(
        topBar = { MiuixTopBar(
                title = stringResource(R.string.settings_device_import_title),
                navigationIcon = { BackButton(onBack) },
            ) },
    ) { padding ->
        Column(
            Modifier.fillMaxSize()
                .verticalScroll(rememberTopBarScrollState())
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.settings_device_import_steps))
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
                        model.error = openFailedMessage
                    }
                },
            ) {
                Text(stringResource(R.string.settings_device_import_open))
            }
            Text(stringResource(R.string.settings_device_import_note))
            TextField(
                value = input,
                onValueChange = { recognize(it) },
                label = stringResource(R.string.settings_device_import_label),
                modifier = Modifier.fillMaxWidth(),
                enabled = !model.saving,
                minLines = 2,
                maxLines = 4,
            )
            if (model.pending != null) Text(stringResource(R.string.settings_device_import_recognized))
            if (model.saved) Text(stringResource(R.string.settings_device_import_saved))
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
                    if (text.isBlank()) {
                        model.error = clipboardEmptyMessage
                    } else {
                        recognize(text)
                    }
                },
                enabled = !model.saving,
            ) {
                Text(stringResource(R.string.settings_device_import_paste))
            }
            Button(onClick = model::save, enabled = model.pending != null && !model.saving) {
                Text(if (model.saving) stringResource(R.string.settings_device_import_saving) else stringResource(R.string.settings_device_import_apply))
            }
        }
    }
}
