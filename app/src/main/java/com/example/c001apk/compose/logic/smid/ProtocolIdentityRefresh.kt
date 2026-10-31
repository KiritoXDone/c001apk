package com.example.c001apk.compose.logic.smid

import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map

/** Startup handles the first snapshot. Subsequent edits refresh in the main page's scope. */
internal suspend fun observeProtocolIdentityRefresh(
    preferences: Flow<UserPreferencesCompat>,
    refresh: suspend () -> Result<String>,
    onFailure: suspend () -> Unit,
    settleMillis: Long = 500,
) {
    preferences.map { if (it.digitalUnionRefreshPending) it.deviceIdentityRevision else null }
        .distinctUntilChanged()
        .drop(1)
        .collectLatest { revision ->
            if (revision != null) {
                delay(settleMillis)
                if (refresh().isFailure) onFailure()
            }
        }
}
