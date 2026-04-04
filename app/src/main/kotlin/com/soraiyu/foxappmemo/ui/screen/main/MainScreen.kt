package com.soraiyu.foxappmemo.ui.screen.main

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soraiyu.foxappmemo.R
import com.soraiyu.foxappmemo.data.entity.AppRating
import com.soraiyu.foxappmemo.data.entity.AppStatus
import com.soraiyu.foxappmemo.data.entity.AppWithTags
import com.soraiyu.foxappmemo.ui.component.AppListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    onNavigateToAddEdit: (packageName: String?) -> Unit,
    onNavigateToInstalledApps: () -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val context = LocalContext.current

    val deleteMsg = stringResource(R.string.deleted)
    val exportSavedMsg = stringResource(R.string.export_saved)
    val exportFailedMsg = stringResource(R.string.export_failed)

    var pendingDeletePackage by remember { mutableStateOf<String?>(null) }

    // Holds the JSON string captured at the moment the SAF file picker is opened.
    // Cleared after the picker returns (whether the user saved or cancelled).
    var capturedExportJson by remember { mutableStateOf<String?>(null) }

    // SAF launcher: opens the system file picker so the user can choose where to
    // save the JSON export. The callback receives the URI chosen by the user, or
    // null if the picker was dismissed.
    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        val json = capturedExportJson
        capturedExportJson = null
        if (uri != null && json != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(json.toByteArray(Charsets.UTF_8))
                    }
                    snackbarHostState.showSnackbar(exportSavedMsg)
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar(exportFailedMsg.format(e.message))
                }
            }
        }
        // Always clear the pending JSON so a subsequent export attempt works.
        viewModel.clearExportJson()
    }

    // When the ViewModel has prepared the JSON, capture it and open the SAF file picker.
    LaunchedEffect(uiState.exportJson) {
        val json = uiState.exportJson
        if (json != null) {
            capturedExportJson = json
            createDocumentLauncher.launch("foxappmemo-export.json")
        }
    }

    // Delete confirmation dialog
    pendingDeletePackage?.let { pkg ->
        AlertDialog(
            onDismissRequest = { pendingDeletePackage = null },
            title = { Text(stringResource(R.string.delete_app)) },
            text = { Text(stringResource(R.string.delete_app_confirm, pkg)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteApp(pkg)
                    pendingDeletePackage = null
                    scope.launch { snackbarHostState.showSnackbar(deleteMsg) }
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletePackage = null }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            FilterPanel(
                uiState = uiState,
                onToggleStatus = viewModel::toggleStatus,
                onToggleTag = viewModel::toggleTag,
                onToggleRating = viewModel::toggleRating,
                onClearFilters = viewModel::clearFilters,
            )
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                actions = {
                    IconButton(onClick = onNavigateToInstalledApps) {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = stringResource(R.string.installed_apps))
                    }
                    IconButton(onClick = {
                        scope.launch { scaffoldState.bottomSheetState.expand() }
                    }) {
                        Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.filter))
                    }
                    IconButton(onClick = { viewModel.exportToJson() }) {
                        Icon(Icons.Default.IosShare, contentDescription = stringResource(R.string.export_json))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            OutlinedTextField(
                value = uiState.filter.query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.search_apps)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(R.string.search_apps),
                    )
                },
                trailingIcon = {
                    if (uiState.filter.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setQuery("") }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.clear_search),
                            )
                        }
                    }
                },
                singleLine = true,
            )

            if (uiState.apps.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (uiState.filter.query.isNotEmpty() ||
                            uiState.filter.selectedStatuses.isNotEmpty() ||
                            uiState.filter.selectedTagIds.isNotEmpty() ||
                            uiState.filter.selectedRatings.isNotEmpty()
                        ) {
                            stringResource(R.string.no_apps_match_filters)
                        } else {
                            stringResource(R.string.no_apps)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(
                        items = uiState.apps,
                        key = { it.app.packageName },
                    ) { appWithTags ->
                        SwipeToDismissAppItem(
                            appWithTags = appWithTags,
                            onDismiss = { pendingDeletePackage = appWithTags.app.packageName },
                            onClick = { onNavigateToAddEdit(appWithTags.app.packageName) },
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.BottomEnd,
        ) {
            FloatingActionButton(
                onClick = { onNavigateToAddEdit(null) },
                modifier = Modifier.padding(16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_app))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissAppItem(
    appWithTags: AppWithTags,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDismiss()
                false // don't auto-remove, wait for confirmation
            } else {
                false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        },
        enableDismissFromStartToEnd = false,
    ) {
        AppListItem(appWithTags = appWithTags, onClick = onClick)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterPanel(
    uiState: MainUiState,
    onToggleStatus: (AppStatus) -> Unit,
    onToggleTag: (Long) -> Unit,
    onToggleRating: (Int) -> Unit,
    onClearFilters: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.filter_status), style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppStatus.entries.forEach { status ->
                FilterChip(
                    selected = status in uiState.filter.selectedStatuses,
                    onClick = { onToggleStatus(status) },
                    label = { Text(stringResource(status.labelResId)) },
                )
            }
        }

        if (uiState.allTags.isNotEmpty()) {
            Text(stringResource(R.string.filter_tags), style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.allTags.forEach { tag ->
                    FilterChip(
                        selected = tag.id in uiState.filter.selectedTagIds,
                        onClick = { onToggleTag(tag.id) },
                        label = { Text(tag.name) },
                    )
                }
            }
        }

        Text(stringResource(R.string.filter_rating), style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppRating.entries.forEach { r ->
                FilterChip(
                    selected = r.value in uiState.filter.selectedRatings,
                    onClick = { onToggleRating(r.value) },
                    label = { Text(stringResource(r.labelResId)) },
                )
            }
        }

        TextButton(onClick = onClearFilters) {
            Text(stringResource(R.string.clear_filters))
        }
    }
}
