package com.rtneg.foxappmemo.ui.screen.installedapps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rtneg.foxappmemo.data.repository.InstalledAppInfo
import com.rtneg.foxappmemo.data.repository.InstalledAppsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class InstalledAppsUiState(
    val isLoading: Boolean = true,
    /** Visible (already-filtered) list of apps. */
    val apps: List<InstalledAppInfo> = emptyList(),
    val query: String = "",
    val showSystemApps: Boolean = false,
)

@HiltViewModel
class InstalledAppsViewModel @Inject constructor(
    private val installedAppsRepository: InstalledAppsRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _showSystemApps = MutableStateFlow(false)

    val uiState: StateFlow<InstalledAppsUiState> = combine(
        installedAppsRepository.allApps,
        installedAppsRepository.isLoading,
        _query,
        _showSystemApps,
    ) { allApps, loading, query, showSystem ->
        val filtered = allApps
            .filter { app -> showSystem || !app.isSystemApp }
            .filter { app ->
                query.isBlank() ||
                    app.appName.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)
            }
        InstalledAppsUiState(
            isLoading = loading,
            apps = filtered,
            query = query,
            showSystemApps = showSystem,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InstalledAppsUiState(),
    )

    fun setQuery(query: String) = _query.update { query }

    fun setShowSystemApps(show: Boolean) = _showSystemApps.update { show }
}
