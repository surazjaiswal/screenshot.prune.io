package io.prune.screenshot.data.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import io.prune.screenshot.data.cache.InMemoryScreenshotCache
import io.prune.screenshot.data.local.MediaStoreScreenshotDataSource
import io.prune.screenshot.domain.model.Screenshot
import io.prune.screenshot.domain.model.ScreenshotFolder
import io.prune.screenshot.domain.repository.ScreenshotRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.Locale

/**
 * Concrete repository that bridges the domain contract with MediaStore + file system APIs.
 * The implementation keeps disk work on [ioDispatcher] and maintains a lightweight in-memory cache.
 */
class ScreenshotRepositoryImpl(
    private val dataSource: MediaStoreScreenshotDataSource,
    private val contentResolver: ContentResolver,
    private val cache: InMemoryScreenshotCache,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ScreenshotRepository {

    override suspend fun fetchScreenshots(): List<Screenshot> {
        val cached = cache.get()
        if (cached != null) return cached

        val fresh = dataSource.loadScreenshots()
        cache.set(fresh)
        return fresh
    }

    override suspend fun deleteScreenshot(screenshot: Screenshot): Result<Unit> = runCatching {
        withContext(ioDispatcher) {
            val deleted = contentResolver.delete(Uri.parse(screenshot.uri), null, null)
            if (deleted <= 0) {
                error("Unable to delete screenshot with id=${screenshot.id}")
            }
            val cached = cache.get() ?: emptyList()
            if (cached.isNotEmpty()) {
                cache.set(cached.filterNot { it.id == screenshot.id })
            } else {
                cache.set(dataSource.loadScreenshots())
            }
        }
    }

    override suspend fun moveScreenshot(
        screenshot: Screenshot,
        destination: ScreenshotFolder
    ): Result<Unit> = runCatching {
        withContext(ioDispatcher) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, destination.relativePath.ensureTrailingSlash())
                }
                val updated = contentResolver.update(
                    Uri.parse(screenshot.uri),
                    values,
                    null,
                    null
                )
                if (updated <= 0) {
                    error("Unable to move screenshot via MediaStore update")
                }
            } else {
                val absolutePath = screenshot.absolutePath
                    ?: error("Absolute path required to move screenshot on legacy devices")
                moveFileLegacy(absolutePath, destination)
            }
            // Refresh cache
            cache.set(dataSource.loadScreenshots())
        }
    }

    override suspend fun listDestinations(): List<ScreenshotFolder> = withContext(ioDispatcher) {
        val base = ScreenshotFolder(
            displayName = "Screenshots",
            relativePath = "Pictures/Screenshots/"
        )

        val screenshots = fetchScreenshots()
        val appFolders = screenshots
            .map { it.appName }
            .filter { it.isNotBlank() && it.lowercase(Locale.getDefault()) != "unknown app" }
            .distinct()
            .map { appName ->
                ScreenshotFolder(
                    displayName = appName,
                    relativePath = "Pictures/Screenshots/${appName.ensureFolderFriendly()}/"
                )
            }

        (listOf(base) + appFolders)
            .distinctBy { it.relativePath }
    }

    private fun moveFileLegacy(
        absolutePath: String,
        destination: ScreenshotFolder
    ) {
        val sourceFile = File(absolutePath)
        if (!sourceFile.exists()) {
            error("Source file missing: $absolutePath")
        }

        val root = Environment.getExternalStorageDirectory()
        val destDir = File(root, destination.relativePath)
        if (!destDir.exists() && !destDir.mkdirs()) {
            throw IOException("Unable to create destination directory at ${destDir.absolutePath}")
        }

        val targetFile = File(destDir, sourceFile.name)
        if (!sourceFile.renameTo(targetFile)) {
            throw IOException("Unable to move file to ${targetFile.absolutePath}")
        }

        // Update MediaStore reference
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DATA, targetFile.absolutePath)
        }
        contentResolver.update(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values,
            "${MediaStore.Images.Media.DATA} = ?",
            arrayOf(absolutePath)
        )
    }

    private fun String.ensureTrailingSlash(): String =
        if (endsWith("/")) this else "$this/"

    private fun String.ensureFolderFriendly(): String =
        replace(Regex("[^A-Za-z0-9_ ]"), "")
            .trim()
            .replace(' ', '_')
}

