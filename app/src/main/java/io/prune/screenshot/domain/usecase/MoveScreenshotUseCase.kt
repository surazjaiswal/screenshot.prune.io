package io.prune.screenshot.domain.usecase

import io.prune.screenshot.domain.model.Screenshot
import io.prune.screenshot.domain.model.ScreenshotFolder
import io.prune.screenshot.domain.repository.ScreenshotRepository

class MoveScreenshotUseCase(
    private val repository: ScreenshotRepository
) {
    suspend operator fun invoke(
        screenshot: Screenshot,
        destination: ScreenshotFolder
    ): Result<Unit> = repository.moveScreenshot(screenshot, destination)
}

