package io.prune.screenshot.domain.usecase

import io.prune.screenshot.domain.model.Screenshot
import io.prune.screenshot.domain.repository.ScreenshotRepository

class ScanScreenshotsUseCase(
    private val repository: ScreenshotRepository
) {
    suspend operator fun invoke(): List<Screenshot> = repository.fetchScreenshots()
}

