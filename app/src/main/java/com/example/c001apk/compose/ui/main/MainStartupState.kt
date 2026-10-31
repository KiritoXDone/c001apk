package com.example.c001apk.compose.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

/** Only local account restoration holds the launch screen; device registration runs afterward. */
internal class MainStartupState {
    var preparingAccount by mutableStateOf(true)
        private set

    private val failures = Channel<Unit>(Channel.CONFLATED)
    val identityFailures = failures.receiveAsFlow()

    suspend fun reportIdentityFailure() { failures.send(Unit) }

    suspend fun prepare(
        initializeAccount: suspend () -> Unit,
        ensureId: suspend () -> Result<String>,
    ) {
        try {
            initializeAccount()
        } finally {
            preparingAccount = false
        }
        // Retain a failure until the UI resumes, then deliver it to one collector only.
        if (ensureId().isFailure) reportIdentityFailure()
    }
}
