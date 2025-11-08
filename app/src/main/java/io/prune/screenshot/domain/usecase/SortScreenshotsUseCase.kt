package io.prune.screenshot.domain.usecase

import io.prune.screenshot.domain.model.Screenshot
import io.prune.screenshot.domain.model.SortOption

class SortScreenshotsUseCase {

    operator fun invoke(
        screenshots: List<Screenshot>,
        sortOption: SortOption
    ): List<Screenshot> = when (sortOption) {
        SortOption.NewestFirst -> screenshots.sortedByDescending { it.takenAtMillis }
        SortOption.OldestFirst -> screenshots.sortedBy { it.takenAtMillis }
        SortOption.LargestFirst -> screenshots.sortedByDescending { it.sizeBytes }
        SortOption.SmallestFirst -> screenshots.sortedBy { it.sizeBytes }
    }
}

