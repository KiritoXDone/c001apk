package com.example.c001apk.compose.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.c001apk.compose.R
import com.example.c001apk.compose.ui.component.AiSparkleIcon
import com.example.c001apk.compose.ui.component.settings.PreferenceGroup
import com.example.c001apk.compose.ui.component.settings.SwitchListItem
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.Document
import top.yukonga.miuix.kmp.icon.glass.Layers

@Composable
internal fun AiSummaryPreferences(
    enableAiSummary: Boolean,
    useOfficialAiAssistant: Boolean,
    showRawAiSummaryModal: Boolean,
    onEnableAiSummary: (Boolean) -> Unit,
    onUseOfficialAiAssistant: (Boolean) -> Unit,
    onShowRawAiSummaryModal: (Boolean) -> Unit,
) {
    PreferenceGroup(title = stringResource(R.string.settings_group_ai_summary)) {
        SwitchListItem(
            value = enableAiSummary,
            leadingImageVector = AiSparkleIcon,
            headlineText = stringResource(R.string.settings_ai_summary_enable),
            supportingText = stringResource(R.string.settings_ai_summary_enable_summary),
            onValueChanged = onEnableAiSummary,
        )
        if (enableAiSummary) {
            SwitchListItem(
                value = useOfficialAiAssistant,
                leadingImageVector = MiuixIcons.Glass.Document,
                headlineText = stringResource(R.string.settings_ai_summary_official),
                supportingText = stringResource(R.string.settings_ai_summary_official_summary),
                onValueChanged = onUseOfficialAiAssistant,
            )
            if (useOfficialAiAssistant) {
                SwitchListItem(
                    value = showRawAiSummaryModal,
                    leadingImageVector = MiuixIcons.Glass.Layers,
                    headlineText = stringResource(R.string.settings_ai_summary_raw_modal),
                    supportingText = stringResource(R.string.settings_ai_summary_raw_modal_summary),
                    onValueChanged = onShowRawAiSummaryModal,
                )
            }
        }
    }
}
