package com.example.c001apk.compose.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.logic.model.HomeMenu
import com.example.c001apk.compose.logic.repository.HomeMenuRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class HomeViewModel
@Inject
constructor(
    private val homeMenuRepo: HomeMenuRepo,
) : ViewModel() {

    /**
     * Normalization stays in memory: writing position back on every collection would erase the
     * order the user saved. Unknown and FOLLOW rows are ignored, missing tabs are appended.
     */
    val homeMenus: StateFlow<List<HomeMenu>?> =
        homeMenuRepo.loadAllListFlow()
            .map { stored -> normalizeHomeMenus(stored) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    private val _saveError = MutableStateFlow(false)
    val saveError: StateFlow<Boolean> = _saveError.asStateFlow()

    /** Persists order and enabled flags in one write and only leaves the editor once it landed. */
    fun saveMenus(menus: List<HomeMenu>, onSaved: () -> Unit) {
        if (_saving.value) return
        val sanitized = sanitizeHomeMenus(menus)
        if (sanitized.isEmpty()) return

        _saving.value = true
        _saveError.value = false
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { homeMenuRepo.upsertList(sanitized) }
                onSaved()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _saveError.value = true
            } finally {
                _saving.value = false
            }
        }
    }
}
