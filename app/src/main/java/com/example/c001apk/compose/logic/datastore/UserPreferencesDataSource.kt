package com.example.c001apk.compose.logic.datastore

import androidx.datastore.core.DataStore
import com.example.c001apk.compose.FollowType
import com.example.c001apk.compose.ThemeMode
import com.example.c001apk.compose.ThemeType
import com.example.c001apk.compose.logic.model.HapticStrength
import com.example.c001apk.compose.logic.model.MainNavigationOrder
import com.example.c001apk.compose.logic.smid.protocolVersion
import com.example.c001apk.compose.logic.smid.withProtocolVersion
import com.example.c001apk.compose.logic.smid.withCurrentDevice
import com.example.c001apk.compose.logic.smid.withDigitalUnionId
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserPreferencesDataSource
@Inject
constructor(
    private val userPreferences: DataStore<UserPreferencesCompat>,
) {
    val data
        get() = userPreferences.data

    suspend fun setPredictiveBack(value: Boolean) = userPreferences.updateData {
        it.copy(predictiveBack = value)
    }

    suspend fun setMainNavigationOrder(value: List<Int>) = userPreferences.updateData {
        it.copy(mainNavigationOrder = MainNavigationOrder.normalize(value))
    }

    suspend fun setHideHomePublishButton(value: Boolean) = userPreferences.updateData { it.copy(hideHomePublishButton = value) }

    suspend fun setReadingMode(value: Int) = userPreferences.updateData {
        it.copy(readingMode = value.coerceIn(0, 2))
    }

    suspend fun setFloatingNavigationBar(value: Boolean) = userPreferences.updateData {
        it.copy(floatingNavigationBar = value)
    }

    suspend fun setBlurBars(value: Boolean) = userPreferences.updateData {
        it.copy(blurBars = value)
    }

    suspend fun setCollapseTabsOnScroll(value: Boolean) = userPreferences.updateData {
        it.copy(collapseTabsOnScroll = value)
    }

    suspend fun setHideTradeLinkHint(value: Boolean) = userPreferences.updateData {
        it.copy(hideTradeLinkHint = value)
    }

    suspend fun setThemeMode(value: ThemeMode) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(themeMode = value) }
        }

    suspend fun setMaterialYou(value: Boolean) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(materialYou = value) }
        }

    suspend fun setPureBlack(value: Boolean) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(pureBlack = value) }
        }

    suspend fun setFontScale(value: Float) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(fontScale = value) }
        }

    suspend fun setContentScale(value: Float) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(contentScale = value) }
        }

    suspend fun setSZLMId(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData {
                it.withDigitalUnionId(value.trim()).copy(deviceIdentityRevision = it.deviceIdentityRevision + 1, digitalUnionRefreshPending = false)
            }
        }

    suspend fun saveGeneratedIdentity(value: String, expected: UserPreferencesCompat): UserPreferencesCompat? {
        var applied = false
        val saved = userPreferences.updateData { current ->
            if (com.example.c001apk.compose.logic.smid.identityUnchanged(expected, current)) {
                applied = true
                current.withDigitalUnionId(value.trim())
                    .copy(deviceIdentityRevision = current.deviceIdentityRevision + 1, digitalUnionRefreshPending = false)
            } else current
        }
        return saved.takeIf { applied }
    }

    suspend fun useCurrentDevice() = userPreferences.updateData {
        it.withCurrentDevice().copy(deviceIdentityRevision = it.deviceIdentityRevision + 1)
    }

    suspend fun setImageQuality(value: Int) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(imageQuality = value) }
        }

    suspend fun setImageFilter(value: Boolean) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(imageFilter = value) }
        }

    suspend fun setOpenInBrowser(value: Boolean) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(openInBrowser = value) }
        }

    suspend fun setShowHomeCarousel(value: Boolean) = userPreferences.updateData {
        it.copy(showHomeCarousel = value)
    }

    suspend fun setShowHomeChannels(value: Boolean) = userPreferences.updateData {
        it.copy(showHomeChannels = value)
    }

    suspend fun setShowSquare(value: Boolean) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(showSquare = value) }
        }

    suspend fun setRecordHistory(value: Boolean) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(recordHistory = value) }
        }

    suspend fun setShowEmoji(value: Boolean) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(showEmoji = value) }
        }

    suspend fun setCheckUpdate(value: Boolean) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(checkUpdate = value) }
        }

    suspend fun setCheckCount(value: Boolean) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(checkCount = value) }
        }

    suspend fun setVersionName(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.withProtocolVersion(it.protocolVersion().copy(name = value)) }
        }

    suspend fun setApiVersion(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.withProtocolVersion(it.protocolVersion().copy(api = value)) }
        }

    suspend fun setVersionCode(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.withProtocolVersion(it.protocolVersion().copy(code = value)) }
        }

    suspend fun setManufacturer(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(manufacturer = value) }
        }

    suspend fun setBrand(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(brand = value) }
        }

    suspend fun setModel(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(model = value) }
        }

    suspend fun setBuildNumber(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(buildNumber = value) }
        }

    suspend fun setSdkInt(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(sdkInt = value) }
        }

    suspend fun setAndroidVersion(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(androidVersion = value) }
        }

    suspend fun setUserAgent(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(userAgent = value) }
        }

    suspend fun setXAppDevice(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(xAppDevice = value, deviceIdentityRevision = it.deviceIdentityRevision + 1) }
        }

    suspend fun setXAppToken(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(xAppToken = value) }
        }

    suspend fun setIsLogin(value: Boolean) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(isLogin = value) }
        }

    suspend fun setUserAvatar(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(userAvatar = value) }
        }

    suspend fun setUsername(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(username = value) }
        }

    suspend fun setLevel(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(level = value) }
        }

    suspend fun setExperience(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(experience = value) }
        }

    suspend fun setNextLevelExperience(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(nextLevelExperience = value) }
        }

    suspend fun setUid(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(uid = value) }
        }

    suspend fun setToken(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(token = value) }
        }

    suspend fun setFollowType(value: FollowType) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(followType = value) }
        }

    suspend fun setRecentIds(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(recentIds = value) }
        }

    suspend fun setCheckCountPeriod(value: Int) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(checkCountPeriod = value) }
        }

    suspend fun setInstallTime(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(installTime = value) }
        }

    suspend fun setThemeType(value: ThemeType) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(themeType = value) }
        }

    suspend fun setSeedColor(value: String) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(seedColor = value) }
        }

    suspend fun setPaletteStyle(value: Int) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(paletteStyle = value) }
        }

    suspend fun setHapticFeedback(value: Boolean) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(hapticFeedback = value) }
        }

    suspend fun setHapticStrength(value: HapticStrength) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(hapticStrength = value) }
        }

    suspend fun setCustomHaptics(value: Boolean) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(customHaptics = value) }
        }

    suspend fun setReverseHapticEffects(value: Boolean) =
        withContext(Dispatchers.IO) {
            userPreferences.updateData { it.copy(reverseHapticEffects = value) }
        }
}
