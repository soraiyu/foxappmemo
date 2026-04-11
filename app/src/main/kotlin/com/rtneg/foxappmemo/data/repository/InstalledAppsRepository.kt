package com.rtneg.foxappmemo.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

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

/**
 * Application-scoped repository that loads the installed-apps list once in the
 * background and caches the result for the lifetime of the process.
 *
 * Loading is triggered immediately when Hilt creates the singleton (typically at
 * app startup via the [MainActivity] injection point), so the list is ready by
 * the time the user opens any screen that needs it.
 */
@Singleton
class InstalledAppsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _allApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    private val _isLoading = MutableStateFlow(true)

    /** Full sorted list of installed apps; empty until loading completes. */
    val allApps: StateFlow<List<InstalledAppInfo>> = _allApps.asStateFlow()

    /** `true` while the initial load is in progress. */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        repositoryScope.launch {
            try {
                val pm = context.packageManager
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
            } catch (_: Exception) {
                // Degrade gracefully — leave the list empty so the UI can show an
                // appropriate empty state rather than crashing the app.
            } finally {
                _isLoading.value = false
            }
        }
    }
}
