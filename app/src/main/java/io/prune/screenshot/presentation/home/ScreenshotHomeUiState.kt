package io.prune.screenshot.presentation.home

import io.prune.screenshot.domain.model.SortOption
import io.prune.screenshot.domain.model.ScreenshotFolder
import io.prune.screenshot.presentation.home.model.ScreenshotBucketUiModel

data class ScreenshotHomeUiState(
    val isLoading: Boolean = false,
    val sortOption: SortOption = SortOption.NewestFirst,
    val appBuckets: List<ScreenshotBucketUiModel> = emptyList(),
    val smartBuckets: List<ScreenshotBucketUiModel> = emptyList(),
    val destinations: List<ScreenshotFolder> = emptyList(),
    val errorMessage: String? = null
)

