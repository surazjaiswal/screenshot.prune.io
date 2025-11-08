package io.prune.screenshot.domain.usecase

import io.prune.screenshot.domain.model.Screenshot
import io.prune.screenshot.domain.repository.ScreenshotRepository

class DeleteScreenshotUseCase(
    private val repository: ScreenshotRepository
) {
    suspend operator fun invoke(screenshot: Screenshot): Result<Unit> =
        repository.deleteScreenshot(screenshot)
}

