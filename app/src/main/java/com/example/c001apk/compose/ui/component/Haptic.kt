package com.example.c001apk.compose.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.model.HapticStrength
import com.example.c001apk.compose.logic.model.TopBarStyle
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.util.performConfiguredHapticFeedback
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.glass.More as GlassMore

@Composable
fun rememberHapticClick(
    type: HapticFeedbackType = HapticFeedbackType.TextHandleMove,
    strength: HapticStrength? = null,
    onClick: () -> Unit,
): () -> Unit {
    val prefs = LocalUserPreferences.current
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val hapticStrength = strength ?: prefs.hapticStrength
    return remember(
        context,
        prefs.hapticFeedback,
        prefs.customHaptics,
        prefs.reverseHapticEffects,
        hapticStrength,
        hapticFeedback,
        type,
        onClick,
    ) {
        {
            if (prefs.hapticFeedback) {
                context.performConfiguredHapticFeedback(
                    fallback = { hapticFeedback.performHapticFeedback(type) },
                    enabled = prefs.hapticFeedback,
                    strength = hapticStrength,
                    customHaptics = prefs.customHaptics,
                    reverseEffects = prefs.reverseHapticEffects,
                )
            }
            onClick()
        }
    }
}

@Composable
fun MoreMenuButton(onClick: () -> Unit) {
    MiuixToolbarIconButton(onClick = rememberHapticClick(onClick = onClick)) {
        Icon(
            imageVector = if (LocalUserPreferences.current.topBarStyle == TopBarStyle.OS4)
                MiuixIcons.Glass.GlassMore else MiuixIcons.More,
            contentDescription = "更多操作",
            modifier = Modifier.size(21.dp),
        )
    }
}

/** Routes feedback from miuix controls through the application's existing preference. */
@Composable
fun rememberConfiguredHapticFeedback(): androidx.compose.ui.hapticfeedback.HapticFeedback {
    val prefs = LocalUserPreferences.current
    val context = LocalContext.current
    val system = LocalHapticFeedback.current
    return remember(
        context,
        system,
        prefs.hapticFeedback,
        prefs.customHaptics,
        prefs.hapticStrength,
        prefs.reverseHapticEffects,
    ) {
        object : androidx.compose.ui.hapticfeedback.HapticFeedback {
            override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
                context.performConfiguredHapticFeedback(
                    fallback = { system.performHapticFeedback(hapticFeedbackType) },
                    enabled = prefs.hapticFeedback,
                    strength = prefs.hapticStrength,
                    customHaptics = prefs.customHaptics,
                    reverseEffects = prefs.reverseHapticEffects,
                )
            }
        }
    }
}
