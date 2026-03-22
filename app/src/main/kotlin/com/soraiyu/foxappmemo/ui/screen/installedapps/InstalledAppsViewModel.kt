package com.soraiyu.foxappmemo.ui.screen.installedapps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Represents a single entry in the installed-apps list.
 *
 * @property packageName The Android package name (e.g. `com.example.app`).
 * @property appName     Human-readable label resolved via [PackageManager].
 * @property isSystemApp Whether the package carries the [ApplicationInfo.FLAG_SYSTEM] flag.
 */
data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
)

data class InstalledAppsUiState(
    val isLoading: Boolean = true,
    /** Visible (already-filtered) list of apps. */
    val apps: List<InstalledAppInfo> = emptyList(),
    val query: String = "",
    val showSystemApps: Boolean = false,
)

@HiltViewModel
class InstalledAppsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    /** Full unfiltered list loaded once from PackageManager. Filtering is applied reactively in the UI state via [combine]. */
    private val _allApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    private val _query = MutableStateFlow("")
    private val _showSystemApps = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(true)

    val uiState: StateFlow<InstalledAppsUiState> = combine(
        _allApps,
        _query,
        _showSystemApps,
        _isLoading,
    ) { allApps, query, showSystem, loading ->
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

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = appContext.packageManager
            @Suppress("DEPRECATION")
            val rawList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
            } else {
                pm.getInstalledApplications(0)
            }
            val apps = rawList
                .map { info ->
                    InstalledAppInfo(
                        packageName = info.packageName,
                        appName = pm.getApplicationLabel(info).toString(),
                        isSystemApp = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    )
                }
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.appName })

            _allApps.value = apps
            _isLoading.update { false }
        }
    }

    fun setQuery(query: String) = _query.update { query }

    fun setShowSystemApps(show: Boolean) = _showSystemApps.update { show }
}
