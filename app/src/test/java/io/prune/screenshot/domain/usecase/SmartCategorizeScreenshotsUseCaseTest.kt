package io.prune.screenshot.domain.usecase

import io.prune.screenshot.domain.model.Screenshot
import io.prune.screenshot.domain.model.SmartCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class SmartCategorizeScreenshotsUseCaseTest {

    private val useCase = SmartCategorizeScreenshotsUseCase()

    @Test
    fun `classifies receipts based on filename keywords`() {
        val screenshot = Screenshot(
            id = 1,
            uri = "uri://1",
            displayName = "Screenshot_20231001_receipt_store.png",
            appName = "",
            takenAtMillis = 0,
            sizeBytes = 1_024
        )

        val result = useCase(listOf(screenshot))

        assertEquals(SmartCategory.Receipt, result.first().smartCategory)
    }

    @Test
    fun `falls back to unknown when heuristic misses`() {
        val screenshot = Screenshot(
            id = 1,
            uri = "uri://1",
            displayName = "Screenshot.png",
            appName = "",
            takenAtMillis = 0,
            sizeBytes = 1_024
        )

        val result = useCase(listOf(screenshot))

        assertEquals(SmartCategory.Unknown, result.first().smartCategory)
    }
}

