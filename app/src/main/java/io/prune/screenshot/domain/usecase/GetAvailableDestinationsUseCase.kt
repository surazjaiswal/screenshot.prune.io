package io.prune.screenshot.domain.usecase

import io.prune.screenshot.domain.model.ScreenshotFolder
import io.prune.screenshot.domain.repository.ScreenshotRepository

class GetAvailableDestinationsUseCase(
    private val repository: ScreenshotRepository
) {
    suspend operator fun invoke(): List<ScreenshotFolder> = repository.listDestinations()
}

