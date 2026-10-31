package com.example.c001apk.compose.logic.smid

import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat

/** The comparison runs inside DataStore's atomic update, including same-value manual imports. */
internal fun identityUnchanged(expected: UserPreferencesCompat, current: UserPreferencesCompat) =
    expected.deviceIdentityRevision == current.deviceIdentityRevision &&
        expected.szlmId == current.szlmId && expected.xAppDevice == current.xAppDevice &&
        expected.manufacturer == current.manufacturer && expected.brand == current.brand &&
        expected.model == current.model && expected.buildNumber == current.buildNumber &&
        expected.androidVersion == current.androidVersion && expected.sdkInt == current.sdkInt &&
        expected.userAgent == current.userAgent && expected.protocolVersion() == current.protocolVersion()
