package com.soraiyu.foxappmemo.ui.screen.main

import android.content.Intent
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
import androidx.compose.material3.Scaffold
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
import com.soraiyu.foxappmemo.data.entity.AppStatus
import com.soraiyu.foxappmemo.data.entity.AppWithTags
import com.soraiyu.foxappmemo.ui.component.AppListItem
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

    var pendingDeletePackage by remember { mutableStateOf<String?>(null) }

    // Handle export
    LaunchedEffect(uiState.exportJson) {
        val json = uiState.exportJson ?: return@LaunchedEffect
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_TEXT, json)
            putExtra(Intent.EXTRA_SUBJECT, "FoxAppMemo export")
        }
        context.startActivity(Intent.createChooser(intent, "Export JSON"))
        viewModel.clearExportJson()
    }

    // Delete confirmation dialog
    pendingDeletePackage?.let { pkg ->
        AlertDialog(
            onDismissRequest = { pendingDeletePackage = null },
            title = { Text("Delete App") },
            text = { Text("Remove \"$pkg\" from your memo list?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteApp(pkg)
                    pendingDeletePackage = null
                    scope.launch { snackbarHostState.showSnackbar("Deleted") }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletePackage = null }) { Text("Cancel") }
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
                onSetMinRating = viewModel::setMinRating,
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
                        Icon(Icons.Default.PhoneAndroid, contentDescription = "Installed Apps")
                    }
                    IconButton(onClick = {
                        scope.launch { scaffoldState.bottomSheetState.expand() }
                    }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    IconButton(onClick = { viewModel.exportToJson() }) {
                        Icon(Icons.Default.IosShare, contentDescription = "Export JSON")
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
                placeholder = { Text("Search apps…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.filter.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
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
                            uiState.filter.selectedTagIds.isNotEmpty()
                        ) {
                            "No apps match your filters"
                        } else {
                            "No apps recorded yet.\nTap + to add one!"
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
                Icon(Icons.Default.Add, contentDescription = "Add App")
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
                    contentDescription = "Delete",
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
    onSetMinRating: (Int?) -> Unit,
    onClearFilters: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Status", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppStatus.entries.forEach { status ->
                FilterChip(
                    selected = status in uiState.filter.selectedStatuses,
                    onClick = { onToggleStatus(status) },
                    label = { Text(status.label) },
                )
            }
        }

        if (uiState.allTags.isNotEmpty()) {
            Text("Tags", style = MaterialTheme.typography.titleSmall)
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

        Text("Min Rating", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = uiState.filter.minRating == null,
                onClick = { onSetMinRating(null) },
                label = { Text("Any") },
            )
            (1..5).forEach { r ->
                FilterChip(
                    selected = uiState.filter.minRating == r,
                    onClick = { onSetMinRating(r) },
                    label = { Text("$r★+") },
                )
            }
        }

        TextButton(onClick = onClearFilters) {
            Text("Clear Filters")
        }
    }
}
