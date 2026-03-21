package com.soraiyu.foxappmemo.ui.screen.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soraiyu.foxappmemo.data.entity.AppEntity
import com.soraiyu.foxappmemo.data.entity.AppStatus
import com.soraiyu.foxappmemo.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditUiState(
    val isLoading: Boolean = true,
    val packageName: String = "",
    val appName: String = "",
    val status: AppStatus = AppStatus.TRYING,
    val rating: Int? = null,
    val memo: String = "",
    val tagInput: String = "",
    val tags: List<String> = emptyList(),
    val installDate: Long? = null,
    val uninstallDate: Long? = null,
    val lastUsedDate: Long? = null,
    val isSaved: Boolean = false,
    val error: String? = null,
    /** Set when editing an existing entry */
    val isEditMode: Boolean = false,
)

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val repository: AppRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val editPackageName: String? = savedStateHandle["packageName"]

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    init {
        if (editPackageName != null) {
            loadApp(editPackageName)
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadApp(packageName: String) {
        viewModelScope.launch {
            val appWithTags = repository.getAppWithTags(packageName)
            if (appWithTags != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isEditMode = true,
                        packageName = appWithTags.app.packageName,
                        appName = appWithTags.app.appName,
                        status = AppStatus.fromLabel(appWithTags.app.status),
                        rating = appWithTags.app.rating,
                        memo = appWithTags.app.memo ?: "",
                        tags = appWithTags.tags.map { tag -> tag.name },
                        installDate = appWithTags.app.installDate,
                        uninstallDate = appWithTags.app.uninstallDate,
                        lastUsedDate = appWithTags.app.lastUsedDate,
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun setPackageName(value: String) = _uiState.update { it.copy(packageName = value) }
    fun setAppName(value: String) = _uiState.update { it.copy(appName = value) }
    fun setStatus(value: AppStatus) = _uiState.update { it.copy(status = value) }
    fun setRating(value: Int?) = _uiState.update { it.copy(rating = value) }
    fun setMemo(value: String) = _uiState.update { it.copy(memo = value) }
    fun setTagInput(value: String) = _uiState.update { it.copy(tagInput = value) }

    fun addTag(name: String = _uiState.value.tagInput) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty() && trimmed !in _uiState.value.tags) {
            _uiState.update {
                it.copy(
                    tags = it.tags + trimmed,
                    tagInput = "",
                )
            }
        }
    }

    fun removeTag(name: String) {
        _uiState.update { it.copy(tags = it.tags - name) }
    }

    /** Pre-fill fields from a Play Store share URL text */
    fun applySharedText(text: String) {
        // Play Store URLs: https://play.google.com/store/apps/details?id=<packageName>
        val regex = Regex("""id=([A-Za-z][A-Za-z0-9_]*(\.([A-Za-z][A-Za-z0-9_]*))*)""")
        val match = regex.find(text)
        if (match != null) {
            val pkg = match.groupValues[1]
            _uiState.update {
                it.copy(
                    packageName = pkg,
                    appName = if (it.appName.isBlank()) pkg else it.appName,
                )
            }
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.packageName.isBlank()) {
            _uiState.update { it.copy(error = "Package name is required") }
            return
        }
        if (state.appName.isBlank()) {
            _uiState.update { it.copy(error = "App name is required") }
            return
        }
        viewModelScope.launch {
            try {
                val app = AppEntity(
                    packageName = state.packageName.trim(),
                    appName = state.appName.trim(),
                    status = state.status.label,
                    rating = state.rating,
                    memo = state.memo.trim().ifEmpty { null },
                    installDate = state.installDate,
                    uninstallDate = state.uninstallDate,
                    lastUsedDate = state.lastUsedDate,
                )
                repository.saveApp(app, state.tags)
                _uiState.update { it.copy(isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Save failed") }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
