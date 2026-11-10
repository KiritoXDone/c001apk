package com.example.c001apk.compose.logic.repository

import com.example.c001apk.compose.FollowType
import com.example.c001apk.compose.ThemeMode
import com.example.c001apk.compose.ThemeType
import com.example.c001apk.compose.logic.datastore.UserPreferencesDataSource
import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.example.c001apk.compose.logic.model.HapticStrength
import com.example.c001apk.compose.util.CookieUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository
@Inject
constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
) {
    private val identityWrites = Mutex()
    val data
        get() = userPreferencesDataSource.data

    suspend fun setPredictiveBack(value: Boolean) = userPreferencesDataSource.setPredictiveBack(value)

    suspend fun setMainNavigationOrder(value: List<Int>) = userPreferencesDataSource.setMainNavigationOrder(value)

    suspend fun setHideHomePublishButton(value: Boolean) = userPreferencesDataSource.setHideHomePublishButton(value)

    suspend fun setDefaultSearchOrder(value: String) = userPreferencesDataSource.setDefaultSearchOrder(value)

    suspend fun setReadingMode(value: Int) = userPreferencesDataSource.setReadingMode(value)

    suspend fun setFloatingNavigationBar(value: Boolean) =
        userPreferencesDataSource.setFloatingNavigationBar(value)

    suspend fun setBlurBars(value: Boolean) = userPreferencesDataSource.setBlurBars(value)

    suspend fun setCollapseTabsOnScroll(value: Boolean) =
        userPreferencesDataSource.setCollapseTabsOnScroll(value)

    suspend fun setHideTradeLinkHint(value: Boolean) =
        userPreferencesDataSource.setHideTradeLinkHint(value)

    suspend fun setThemeMode(value: ThemeMode) = userPreferencesDataSource.setThemeMode(value)

    suspend fun setMaterialYou(value: Boolean) = userPreferencesDataSource.setMaterialYou(value)

    suspend fun setPureBlack(value: Boolean) = userPreferencesDataSource.setPureBlack(value)

    suspend fun setFontScale(value: Float) = userPreferencesDataSource.setFontScale(value)

    suspend fun setContentScale(value: Float) = userPreferencesDataSource.setContentScale(value)

    private fun publishIdentity(it: UserPreferencesCompat) {
        CookieUtil.szlmId = it.szlmId
        CookieUtil.xAppDevice = it.xAppDevice
        CookieUtil.userAgent = it.userAgent
        CookieUtil.versionName = it.versionName
        CookieUtil.versionCode = it.versionCode
        CookieUtil.apiVersion = it.apiVersion
        CookieUtil.sdkInt = it.sdkInt
    }

    suspend fun setSZLMId(value: String) = identityWrites.withLock {
        userPreferencesDataSource.setSZLMId(value).also(::publishIdentity)
    }

    suspend fun saveGeneratedIdentity(
        value: String,
        expected: UserPreferencesCompat,
    ): Boolean = identityWrites.withLock {
        val saved = userPreferencesDataSource.saveGeneratedIdentity(value, expected) ?: return@withLock false
        publishIdentity(saved)
        true
    }

    suspend fun useCurrentDevice() = identityWrites.withLock {
        userPreferencesDataSource.useCurrentDevice().also(::publishIdentity)
    }

    suspend fun setImageQuality(value: Int) = userPreferencesDataSource.setImageQuality(value)

    suspend fun setImageFilter(value: Boolean) = userPreferencesDataSource.setImageFilter(value)

    suspend fun setOpenInBrowser(value: Boolean) = userPreferencesDataSource.setOpenInBrowser(value)

    suspend fun setShowHomeCarousel(value: Boolean) = userPreferencesDataSource.setShowHomeCarousel(value)

    suspend fun setShowHomeChannels(value: Boolean) = userPreferencesDataSource.setShowHomeChannels(value)

    suspend fun setShowSquare(value: Boolean) = userPreferencesDataSource.setShowSquare(value)

    suspend fun setRecordHistory(value: Boolean) = userPreferencesDataSource.setRecordHistory(value)

    suspend fun setShowEmoji(value: Boolean) = userPreferencesDataSource.setShowEmoji(value)

    suspend fun setCheckUpdate(value: Boolean) = userPreferencesDataSource.setCheckUpdate(value)

    suspend fun setCheckCount(value: Boolean) = userPreferencesDataSource.setCheckCount(value)

    suspend fun setVersionName(value: String) = userPreferencesDataSource.setVersionName(value)

    suspend fun setApiVersion(value: String) = userPreferencesDataSource.setApiVersion(value)

    suspend fun setVersionCode(value: String) = userPreferencesDataSource.setVersionCode(value)

    suspend fun setManufacturer(value: String) = userPreferencesDataSource.setManufacturer(value)

    suspend fun setBrand(value: String) = userPreferencesDataSource.setBrand(value)

    suspend fun setModel(value: String) = userPreferencesDataSource.setModel(value)

    suspend fun setBuildNumber(value: String) = userPreferencesDataSource.setBuildNumber(value)

    suspend fun setSdkInt(value: String) = userPreferencesDataSource.setSdkInt(value)

    suspend fun setAndroidVersion(value: String) =
        userPreferencesDataSource.setAndroidVersion(value)

    suspend fun setUserAgent(value: String) = userPreferencesDataSource.setUserAgent(value)

    suspend fun setXAppDevice(value: String) = identityWrites.withLock {
        userPreferencesDataSource.setXAppDevice(value).also(::publishIdentity)
    }

    suspend fun setXAppToken(value: String) = userPreferencesDataSource.setXAppToken(value)

    suspend fun setIsLogin(value: Boolean) = userPreferencesDataSource.setIsLogin(value)

    suspend fun setUserAvatar(value: String) = userPreferencesDataSource.setUserAvatar(value)

    suspend fun setUsername(value: String) = userPreferencesDataSource.setUsername(value)

    suspend fun setLevel(value: String) = userPreferencesDataSource.setLevel(value)

    suspend fun setExperience(value: String) = userPreferencesDataSource.setExperience(value)

    suspend fun setNextLevelExperience(value: String) =
        userPreferencesDataSource.setNextLevelExperience(value)

    suspend fun setUid(value: String) = userPreferencesDataSource.setUid(value)

    suspend fun setToken(value: String) = userPreferencesDataSource.setToken(value)

    suspend fun setFollowType(value: FollowType) = userPreferencesDataSource.setFollowType(value)

    suspend fun setRecentIds(value: String) = userPreferencesDataSource.setRecentIds(value)

    suspend fun setCheckCountPeriod(value: Int) =
        userPreferencesDataSource.setCheckCountPeriod(value)

    suspend fun setInstallTime(value: String) = userPreferencesDataSource.setInstallTime(value)

    suspend fun setThemeType(value: ThemeType) = userPreferencesDataSource.setThemeType(value)

    suspend fun setSeedColor(value: String) = userPreferencesDataSource.setSeedColor(value)

    suspend fun setPaletteStyle(value: Int) = userPreferencesDataSource.setPaletteStyle(value)

    suspend fun setHapticFeedback(value: Boolean) =
        userPreferencesDataSource.setHapticFeedback(value)

    suspend fun setHapticStrength(value: HapticStrength) =
        userPreferencesDataSource.setHapticStrength(value)

    suspend fun setCustomHaptics(value: Boolean) = userPreferencesDataSource.setCustomHaptics(value)

    suspend fun setReverseHapticEffects(value: Boolean) =
        userPreferencesDataSource.setReverseHapticEffects(value)
}
