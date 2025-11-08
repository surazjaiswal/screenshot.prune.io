package io.prune.screenshot.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.prune.screenshot.domain.model.ScreenshotFolder
import io.prune.screenshot.domain.model.SortOption
import io.prune.screenshot.presentation.components.ScreenshotThumbnail
import io.prune.screenshot.presentation.home.model.ScreenshotBucketUiModel
import io.prune.screenshot.presentation.home.model.ScreenshotItemUi

@Composable
fun ScreenshotHomeRoute(
    viewModel: ScreenshotHomeViewModel,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ScreenshotHomeViewModel.HomeEvent.ShowMessage ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    ScreenshotHomeScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onRefresh = onRefresh,
        onSortSelected = viewModel::onSortSelected,
        onDelete = viewModel::onDeleteScreenshot,
        onMove = viewModel::onMoveScreenshot,
        destinations = uiState.destinations,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotHomeScreen(
    uiState: ScreenshotHomeUiState,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit,
    onSortSelected: (SortOption) -> Unit,
    onDelete: (Long) -> Unit,
    onMove: (Long, ScreenshotFolder) -> Unit,
    destinations: List<ScreenshotFolder>,
    modifier: Modifier = Modifier
) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Screenshot Organizer") },
                scrollBehavior = scrollBehavior,
                actions = {
                    TextButton(onClick = onRefresh) {
                        Text("Refresh")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                ErrorState(
                    message = uiState.errorMessage,
                    onRetry = onRefresh,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        SortFilterRow(
                            selected = uiState.sortOption,
                            onSelect = onSortSelected
                        )
                    }

                    if (uiState.smartBuckets.isNotEmpty()) {
                        item {
                            SmartBucketsSection(
                                buckets = uiState.smartBuckets,
                                destinations = destinations,
                                onDelete = onDelete,
                                onMove = onMove
                            )
                        }
                    }

                    items(uiState.appBuckets) { bucket ->
                        ScreenshotBucketSection(
                            bucket = bucket,
                            destinations = destinations,
                            onDelete = onDelete,
                            onMove = onMove
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onRetry) {
            Text("Try Again")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortFilterRow(
    selected: SortOption,
    onSelect: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = SortOption.values()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Sort by") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.displayName) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun SmartBucketsSection(
    buckets: List<ScreenshotBucketUiModel>,
    destinations: List<ScreenshotFolder>,
    onDelete: (Long) -> Unit,
    onMove: (Long, ScreenshotFolder) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Smart categories",
            style = MaterialTheme.typography.titleMedium
        )
        buckets.forEach { bucket ->
            ScreenshotBucketSection(
                bucket = bucket,
                destinations = destinations,
                onDelete = onDelete,
                onMove = onMove
            )
        }
    }
}

@Composable
private fun ScreenshotBucketSection(
    bucket: ScreenshotBucketUiModel,
    destinations: List<ScreenshotFolder>,
    onDelete: (Long) -> Unit,
    onMove: (Long, ScreenshotFolder) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(bucket.title, style = MaterialTheme.typography.titleMedium)
                Text(bucket.countLabel, style = MaterialTheme.typography.bodyMedium)
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(bucket.items, key = { it.id }) { item ->
                ScreenshotCard(
                    item = item,
                    destinations = destinations,
                    onDelete = onDelete,
                    onMove = onMove
                )
            }
        }
    }
}

@Composable
private fun ScreenshotCard(
    item: ScreenshotItemUi,
    destinations: List<ScreenshotFolder>,
    onDelete: (Long) -> Unit,
    onMove: (Long, ScreenshotFolder) -> Unit,
    modifier: Modifier = Modifier
) {
    var moveMenuExpanded by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = modifier
            .width(200.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(modifier = Modifier.height(120.dp)) {
                ScreenshotThumbnail(
                    uriString = item.uri,
                    contentDescription = item.title
                )
            }

            Text(item.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            Text(item.subtitle, style = MaterialTheme.typography.bodySmall)
            Text(item.sizeLabel, style = MaterialTheme.typography.labelSmall)

            AnimatedVisibility(
                visible = item.smartLabel != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                item.smartLabel?.let { label ->
                    AssistChip(
                        onClick = {},
                        label = { Text(label) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onDelete(item.id) }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete"
                    )
                }

                Box {
                    TextButton(onClick = { moveMenuExpanded = true }) {
                        Text("Move")
                    }
                    androidx.compose.material3.DropdownMenu(
                        expanded = moveMenuExpanded,
                        onDismissRequest = { moveMenuExpanded = false }
                    ) {
                        destinations.forEach { destination ->
                            DropdownMenuItem(
                                text = { Text(destination.displayName) },
                                onClick = {
                                    moveMenuExpanded = false
                                    onMove(item.id, destination)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

