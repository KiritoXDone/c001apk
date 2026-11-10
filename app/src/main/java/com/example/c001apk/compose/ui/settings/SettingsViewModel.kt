package com.example.c001apk.compose.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.FollowType
import com.example.c001apk.compose.ThemeMode
import com.example.c001apk.compose.ThemeType
import com.example.c001apk.compose.logic.model.HapticStrength
import com.example.c001apk.compose.logic.repository.UserPreferencesRepository
import com.example.c001apk.compose.logic.smid.DigitalUnionRepository
import com.example.c001apk.compose.ui.base.PrefsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/** Created by bggRGjQaUbCoE on 2024/5/31 */
@HiltViewModel
class SettingsViewModel
@Inject
constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val digitalUnion: DigitalUnionRepository,
) : PrefsViewModel(userPreferencesRepository) {

    fun setPredictiveBack(value: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setPredictiveBack(value) }
    }

    var savingNavigationOrder by mutableStateOf(false)
        private set
    var navigationOrderError by mutableStateOf<String?>(null)
        private set

    fun setMainNavigationOrder(value: List<Int>, onSaved: () -> Unit) {
        if (savingNavigationOrder) return
        savingNavigationOrder = true
        navigationOrderError = null
        viewModelScope.launch {
            try {
                userPreferencesRepository.setMainNavigationOrder(value)
                onSaved()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                navigationOrderError = "排序保存失败，请重试"
            } finally {
                savingNavigationOrder = false
            }
        }
    }

    fun setHideHomePublishButton(value: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setHideHomePublishButton(value) }
    }

    fun setDefaultSearchOrder(value: String) {
        viewModelScope.launch { userPreferencesRepository.setDefaultSearchOrder(value) }
    }

    fun setReadingMode(value: Int) {
        viewModelScope.launch { userPreferencesRepository.setReadingMode(value) }
    }

    fun setFloatingNavigationBar(value: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setFloatingNavigationBar(value) }
    }

    fun setBlurBars(value: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setBlurBars(value) }
    }

    fun setCollapseTabsOnScroll(value: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setCollapseTabsOnScroll(value) }
    }

    var generatingId by mutableStateOf(false)
        private set

    var idError by mutableStateOf<String?>(null)
        private set

    fun generateSZLMId() {
        if (generatingId) return
        generatingId = true
        idError = null
        viewModelScope.launch {
            try {
                digitalUnion.ensureId(force = true).onFailure { idError = it.message }
            } finally {
                generatingId = false
            }
        }
    }

    fun setDarkTheme(value: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(value)
        }
    }

    fun setMaterialYou(value: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setMaterialYou(value)
        }
    }

    fun setPureBlack(value: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setPureBlack(value)
        }
    }

    fun setFontScale(value: Float) {
        viewModelScope.launch {
            userPreferencesRepository.setFontScale(value)
        }
    }

    fun setContentScale(value: Float) {
        viewModelScope.launch {
            userPreferencesRepository.setContentScale(value)
        }
    }

    fun setSZLMId(value: String, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                require(com.example.c001apk.compose.logic.smid.OfficialDeviceIdentity.valid(value))
                userPreferencesRepository.setSZLMId(value)
                idError = null
                onSaved()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                idError = "数盟 ID 格式不正确或保存失败，请检查后重试"
            }
        }
    }

    fun setImageQuality(value: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setImageQuality(value)
        }
    }

    fun setImageFilter(value: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setImageFilter(value)
        }
    }

    fun setOpenInBrowser(value: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setOpenInBrowser(value)
        }
    }

    fun setShowHomeCarousel(value: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setShowHomeCarousel(value) }
    }

    fun setShowHomeChannels(value: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setShowHomeChannels(value) }
    }

    fun setShowSquare(value: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setShowSquare(value)
        }
    }

    fun setRecordHistory(value: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setRecordHistory(value)
        }
    }

    fun setShowEmoji(value: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setShowEmoji(value)
        }
    }

    fun setCheckUpdate(value: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setCheckUpdate(value)
        }
    }

    fun setCheckCount(value: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setCheckCount(value)
        }
    }

    fun setVersionName(value: String) {
        viewModelScope.launch {
            userPreferencesRepository.setVersionName(value)
        }
    }

    fun setApiVersion(value: String) {
        viewModelScope.launch {
            userPreferencesRepository.setApiVersion(value)
        }
    }

    fun setVersionCode(value: String) {
        viewModelScope.launch {
            userPreferencesRepository.setVersionCode(value)
        }
    }

    fun setManufacturer(value: String) {
        viewModelScope.launch {
            userPreferencesRepository.setManufacturer(value)
        }
    }

    fun setBrand(value: String) {
        viewModelScope.launch {
            userPreferencesRepository.setBrand(value)
        }
    }

    fun setModel(value: String) {
        viewModelScope.launch {
            userPreferencesRepository.setModel(value)
        }
    }

    fun setBuildNumber(value: String) {
        viewModelScope.launch {
            userPreferencesRepository.setBuildNumber(value)
        }
    }

    fun setSdkInt(value: String) {
        viewModelScope.launch {
            userPreferencesRepository.setSdkInt(value)
        }
    }

    fun setAndroidVersion(value: String) {
        viewModelScope.launch {
            userPreferencesRepository.setAndroidVersion(value)
        }
    }

    fun setUserAgent(value: String) {
        viewModelScope.launch {
            userPreferencesRepository.setUserAgent(value)
        }
    }

    fun setXAppDevice(value: String) {
        viewModelScope.launch {
            userPreferencesRepository.setXAppDevice(value)
        }
    }

    fun setXAppToken(value: String) {
        viewModelScope.launch {
            userPreferencesRepository.setXAppToken(value)
        }
    }

    fun setFollowType(value: FollowType) {
        viewModelScope.launch {
            userPreferencesRepository.setFollowType(value)
        }
    }

    fun setThemeType(value: ThemeType) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeType(value)
        }
    }

    fun setSeedColor(value: String) {
        viewModelScope.launch {
            userPreferencesRepository.setSeedColor(value)
        }
    }

    fun setPaletteStyle(value: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setPaletteStyle(value)
        }
    }

    fun setHapticFeedback(value: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setHapticFeedback(value)
        }
    }

    fun setHapticStrength(value: HapticStrength) {
        viewModelScope.launch {
            userPreferencesRepository.setHapticStrength(value)
        }
    }

    fun setCustomHaptics(value: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setCustomHaptics(value)
        }
    }

    fun setReverseHapticEffects(value: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setReverseHapticEffects(value)
        }
    }
}
