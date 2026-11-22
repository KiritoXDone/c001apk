package com.example.c001apk.compose.debug

import com.example.c001apk.compose.logic.update.AppUpdates
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppDebugEntryPoint {
    fun appUpdates(): AppUpdates
}
