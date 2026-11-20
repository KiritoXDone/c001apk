package com.example.c001apk.compose.logic.providable

import androidx.compose.runtime.compositionLocalOf
import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat

val LocalUserPreferences = compositionLocalOf { UserPreferencesCompat.default() }
