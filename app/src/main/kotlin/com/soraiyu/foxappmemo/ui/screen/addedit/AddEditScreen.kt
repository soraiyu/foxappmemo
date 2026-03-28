package com.soraiyu.foxappmemo.ui.screen.addedit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soraiyu.foxappmemo.data.entity.AppStatus
import com.soraiyu.foxappmemo.data.repository.InstalledAppInfo
import com.soraiyu.foxappmemo.ui.component.AppIcon
import com.soraiyu.foxappmemo.ui.component.RatingSelector

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditScreen(
    onNavigateBack: () -> Unit,
    sharedText: String? = null,
    viewModel: AddEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredInstalledApps by viewModel.filteredInstalledApps.collectAsState()
    val installedAppsLoading by viewModel.installedAppsLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Apply shared text once on first composition
    LaunchedEffect(sharedText) {
        if (!sharedText.isNullOrBlank()) {
            viewModel.applySharedText(sharedText)
        }
    }

    // Navigate back when saved
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    // Show error in snackbar
    LaunchedEffect(uiState.error) {
        val err = uiState.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(err)
        viewModel.clearError()
    }

    // Installed-apps picker bottom sheet
    if (uiState.showAppPicker) {
        val sheetState = rememberModalBottomSheetState(skipPartialExpansion = true)
        ModalBottomSheet(
            onDismissRequest = viewModel::hideAppPicker,
            sheetState = sheetState,
        ) {
            AppPickerContent(
                query = uiState.appPickerQuery,
                onQueryChange = viewModel::setAppPickerQuery,
                isLoading = installedAppsLoading,
                apps = filteredInstalledApps,
                onSelectApp = viewModel::selectInstalledApp,
                onDismiss = viewModel::hideAppPicker,
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.isEditMode) "Edit App" else "Add App")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::save) {
                Icon(Icons.Default.Check, contentDescription = "Save")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // "Select from installed apps" button — visible in add mode only
            if (!uiState.isEditMode) {
                OutlinedButton(
                    onClick = viewModel::showAppPicker,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                    )
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text("インストール済みアプリから選ぶ")
                }
            }

            // Package Name
            OutlinedTextField(
                value = uiState.packageName,
                onValueChange = viewModel::setPackageName,
                label = { Text("Package Name *") },
                placeholder = { Text("e.g. com.example.app") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isEditMode,
                supportingText = if (uiState.isEditMode) {
                    { Text("Package name cannot be changed") }
                } else null,
            )

            // App Name
            OutlinedTextField(
                value = uiState.appName,
                onValueChange = viewModel::setAppName,
                label = { Text("App Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            // Status dropdown
            StatusDropdown(
                selected = uiState.status,
                onSelected = viewModel::setStatus,
                modifier = Modifier.fillMaxWidth(),
            )

            // Rating
            Column {
                Text("評価", style = MaterialTheme.typography.labelMedium)
                RatingSelector(
                    rating = uiState.rating,
                    onRatingChange = viewModel::setRating,
                )
            }

            // Tags
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Tags", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = uiState.tagInput,
                        onValueChange = viewModel::setTagInput,
                        label = { Text("Add tag") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.addTag() },
                        label = { Text("Add") },
                        enabled = uiState.tagInput.isNotBlank(),
                    )
                }
                if (uiState.tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        uiState.tags.forEach { tag ->
                            InputChip(
                                selected = true,
                                onClick = { viewModel.removeTag(tag) },
                                label = { Text(tag) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove $tag",
                                    )
                                },
                            )
                        }
                    }
                }
            }

            // Memo
            OutlinedTextField(
                value = uiState.memo,
                onValueChange = viewModel::setMemo,
                label = { Text("Memo") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
            )

            Spacer(modifier = Modifier.height(72.dp)) // Space for FAB
        }
    }
}

/**
 * Content rendered inside the installed-apps picker [ModalBottomSheet].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppPickerContent(
    query: String,
    onQueryChange: (String) -> Unit,
    isLoading: Boolean,
    apps: List<InstalledAppInfo>,
    onSelectApp: (InstalledAppInfo) -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "アプリを選択",
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "閉じる")
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("アプリを検索…") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "クリア")
                    }
                }
            },
            singleLine = true,
        )

        HorizontalDivider()

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            apps.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (query.isNotEmpty()) {
                            "「$query」に一致するアプリはありません"
                        } else {
                            "インストール済みアプリが見つかりません"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    items(
                        items = apps,
                        key = { it.packageName },
                    ) { app ->
                        AppPickerItem(
                            app = app,
                            onClick = { onSelectApp(app) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppPickerItem(
    app: InstalledAppInfo,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(packageName = app.packageName, size = 40.dp)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = app.appName,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusDropdown(
    selected: AppStatus,
    onSelected: (AppStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Status") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            AppStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.label) },
                    onClick = {
                        onSelected(status)
                        expanded = false
                    },
                )
            }
        }
    }
}

