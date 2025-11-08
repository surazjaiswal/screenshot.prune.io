package io.prune.screenshot.domain.usecase

import io.prune.screenshot.domain.model.Screenshot
import io.prune.screenshot.domain.model.SortOption
import org.junit.Assert.assertEquals
import org.junit.Test

class SortScreenshotsUseCaseTest {

    private val useCase = SortScreenshotsUseCase()

    @Test
    fun `newest first sorts by descending timestamp`() {
        val screenshots = listOf(
            stubScreenshot(id = 1, takenAtMillis = 10),
            stubScreenshot(id = 2, takenAtMillis = 30),
            stubScreenshot(id = 3, takenAtMillis = 20)
        )

        val result = useCase(screenshots, SortOption.NewestFirst)

        assertEquals(listOf(2L, 3L, 1L), result.map { it.id })
    }

    @Test
    fun `smallest first sorts by ascending size`() {
        val screenshots = listOf(
            stubScreenshot(id = 1, sizeBytes = 30),
            stubScreenshot(id = 2, sizeBytes = 10),
            stubScreenshot(id = 3, sizeBytes = 20)
        )

        val result = useCase(screenshots, SortOption.SmallestFirst)

        assertEquals(listOf(2L, 3L, 1L), result.map { it.id })
    }

    private fun stubScreenshot(
        id: Long,
        takenAtMillis: Long = 0,
        sizeBytes: Long = 0
    ) = Screenshot(
        id = id,
        uri = "uri://$id",
        displayName = "Screenshot$id",
        appName = "App$id",
        takenAtMillis = takenAtMillis,
        sizeBytes = sizeBytes
    )
}

