package io.prune.screenshot.domain.usecase

import io.prune.screenshot.domain.model.CategorizedScreenshots
import io.prune.screenshot.domain.model.SortOption

/**
 * High-level orchestration use case that combines scanning, smart detection and grouping.
 */
class GetScreenshotLibraryUseCase(
    private val scanScreenshots: ScanScreenshotsUseCase,
    private val smartCategorize: SmartCategorizeScreenshotsUseCase,
    private val sortScreenshots: SortScreenshotsUseCase,
    private val categorizeScreenshots: CategorizeScreenshotsUseCase
) {

    suspend operator fun invoke(sortOption: SortOption): CategorizedScreenshots {
        val raw = scanScreenshots()
        val classified = smartCategorize(raw)
        val sorted = sortScreenshots(classified, sortOption)
        return categorizeScreenshots(sorted)
    }
}

