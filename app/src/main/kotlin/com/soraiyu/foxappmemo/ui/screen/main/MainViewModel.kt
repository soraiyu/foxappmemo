package com.soraiyu.foxappmemo.ui.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soraiyu.foxappmemo.data.entity.AppStatus
import com.soraiyu.foxappmemo.data.entity.AppWithTags
import com.soraiyu.foxappmemo.data.entity.TagEntity
import com.soraiyu.foxappmemo.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class FilterState(
    val query: String = "",
    val selectedStatuses: Set<AppStatus> = emptySet(),
    val selectedTagIds: Set<Long> = emptySet(),
    val selectedRatings: Set<Int> = emptySet(),
)

data class MainUiState(
    val apps: List<AppWithTags> = emptyList(),
    val allTags: List<TagEntity> = emptyList(),
    val filter: FilterState = FilterState(),
    val exportJson: String? = null,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AppRepository,
) : ViewModel() {

    private val _filter = MutableStateFlow(FilterState())
    private val _exportJson = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MainUiState> = combine(
        repository.getAllAppsWithTags(),
        repository.getAllTags(),
        _filter,
        _exportJson,
    ) { apps, tags, filter, exportJson ->
        val filtered = apps.filter { appWithTags ->
            val app = appWithTags.app
            val matchesQuery = filter.query.isBlank() ||
                app.appName.contains(filter.query, ignoreCase = true) ||
                app.packageName.contains(filter.query, ignoreCase = true)
            val matchesStatus = filter.selectedStatuses.isEmpty() ||
                filter.selectedStatuses.any { it.label == app.status }
            val matchesTags = filter.selectedTagIds.isEmpty() ||
                appWithTags.tags.any { it.id in filter.selectedTagIds }
            val matchesRating = filter.selectedRatings.isEmpty() ||
                app.rating in filter.selectedRatings
            matchesQuery && matchesStatus && matchesTags && matchesRating
        }
        MainUiState(
            apps = filtered,
            allTags = tags,
            filter = filter,
            exportJson = exportJson,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState(),
    )

    fun setQuery(query: String) {
        _filter.value = _filter.value.copy(query = query)
    }

    fun toggleStatus(status: AppStatus) {
        val current = _filter.value.selectedStatuses
        _filter.value = _filter.value.copy(
            selectedStatuses = if (status in current) current - status else current + status,
        )
    }

    fun toggleTag(tagId: Long) {
        val current = _filter.value.selectedTagIds
        _filter.value = _filter.value.copy(
            selectedTagIds = if (tagId in current) current - tagId else current + tagId,
        )
    }

    fun toggleRating(rating: Int) {
        val current = _filter.value.selectedRatings
        _filter.value = _filter.value.copy(
            selectedRatings = if (rating in current) current - rating else current + rating,
        )
    }

    fun clearFilters() {
        _filter.value = FilterState()
    }

    fun deleteApp(packageName: String) {
        viewModelScope.launch {
            repository.deleteApp(packageName)
        }
    }

    fun exportToJson() {
        viewModelScope.launch {
            val apps = repository.getAllAppsWithTags().first()
            val json = Json { prettyPrint = true }
            _exportJson.value = json.encodeToString(apps)
        }
    }

    fun clearExportJson() {
        _exportJson.value = null
    }
}
