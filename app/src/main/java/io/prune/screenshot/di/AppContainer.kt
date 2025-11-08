package io.prune.screenshot.di

import android.content.Context
import io.prune.screenshot.data.cache.InMemoryScreenshotCache
import io.prune.screenshot.data.local.MediaStoreScreenshotDataSource
import io.prune.screenshot.data.repository.ScreenshotRepositoryImpl
import io.prune.screenshot.domain.repository.ScreenshotRepository
import io.prune.screenshot.domain.usecase.CategorizeScreenshotsUseCase
import io.prune.screenshot.domain.usecase.DeleteScreenshotUseCase
import io.prune.screenshot.domain.usecase.GetAvailableDestinationsUseCase
import io.prune.screenshot.domain.usecase.GetScreenshotLibraryUseCase
import io.prune.screenshot.domain.usecase.MoveScreenshotUseCase
import io.prune.screenshot.domain.usecase.ScanScreenshotsUseCase
import io.prune.screenshot.domain.usecase.SmartCategorizeScreenshotsUseCase
import io.prune.screenshot.domain.usecase.SortScreenshotsUseCase
import io.prune.screenshot.presentation.home.ScreenshotHomeViewModel

/**
 * Minimal manual dependency graph. Keeps object creation centralized without introducing
 * a DI framework, which keeps the sample easy to read yet extensible.
 */
class AppContainer(context: Context) {

    private val cache by lazy { InMemoryScreenshotCache() }
    private val mediaStoreDataSource by lazy {
        MediaStoreScreenshotDataSource(context.contentResolver)
    }

    private val screenshotRepository: ScreenshotRepository by lazy {
        ScreenshotRepositoryImpl(
            dataSource = mediaStoreDataSource,
            contentResolver = context.contentResolver,
            cache = cache
        )
    }

    private val scanScreenshotsUseCase by lazy { ScanScreenshotsUseCase(screenshotRepository) }
    private val smartCategorizeScreenshotsUseCase by lazy { SmartCategorizeScreenshotsUseCase() }
    private val sortScreenshotsUseCase by lazy { SortScreenshotsUseCase() }
    private val categorizeScreenshotsUseCase by lazy { CategorizeScreenshotsUseCase() }

    val getScreenshotLibraryUseCase by lazy {
        GetScreenshotLibraryUseCase(
            scanScreenshots = scanScreenshotsUseCase,
            smartCategorize = smartCategorizeScreenshotsUseCase,
            sortScreenshots = sortScreenshotsUseCase,
            categorizeScreenshots = categorizeScreenshotsUseCase
        )
    }

    val deleteScreenshotUseCase by lazy { DeleteScreenshotUseCase(screenshotRepository) }
    val moveScreenshotUseCase by lazy { MoveScreenshotUseCase(screenshotRepository) }
    val getAvailableDestinationsUseCase by lazy {
        GetAvailableDestinationsUseCase(screenshotRepository)
    }

    fun provideHomeViewModelFactory(): ScreenshotHomeViewModel.Factory =
        ScreenshotHomeViewModel.Factory(
            getScreenshotLibraryUseCase = getScreenshotLibraryUseCase,
            deleteScreenshotUseCase = deleteScreenshotUseCase,
            moveScreenshotUseCase = moveScreenshotUseCase,
            getAvailableDestinationsUseCase = getAvailableDestinationsUseCase
        )
}

