package io.prune.screenshot.domain.repository

import io.prune.screenshot.domain.model.Screenshot
import io.prune.screenshot.domain.model.ScreenshotFolder

/**
 * Defines the contract for interacting with screenshot storage.
 */
interface ScreenshotRepository {

    /**
     * Returns all screenshots discovered on the device.
     */
    suspend fun fetchScreenshots(): List<Screenshot>

    /**
     * Deletes the provided screenshot from storage.
     */
    suspend fun deleteScreenshot(screenshot: Screenshot): Result<Unit>

    /**
     * Moves the provided screenshot to the destination folder.
     */
    suspend fun moveScreenshot(
        screenshot: Screenshot,
        destination: ScreenshotFolder
    ): Result<Unit>

    /**
     * Returns folders the app can move screenshots into (including Screenshots).
     */
    suspend fun listDestinations(): List<ScreenshotFolder>
}

