package io.prune.screenshot.data.local

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import io.prune.screenshot.domain.model.Screenshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Reads screenshot metadata from MediaStore and maps it into domain models.
 * All heavy I/O work is performed off the main thread with coroutines.
 */
class MediaStoreScreenshotDataSource(
    private val contentResolver: ContentResolver
) {

    suspend fun loadScreenshots(): List<Screenshot> = withContext(Dispatchers.IO) {
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.RELATIVE_PATH,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        ) + legacyDataColumn()

        val selection: String
        val selectionArgs: Array<String>
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
            selectionArgs = arrayOf("%Screenshots%")
        } else {
            selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?"
            selectionArgs = arrayOf("Screenshots")
        }

        contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    parseScreenshot(cursor, collection)?.let(::add)
                }
            }
        } ?: emptyList()
    }

    private fun parseScreenshot(
        cursor: Cursor,
        baseUri: android.net.Uri
    ): Screenshot? {
        val id = cursor.getLongOrNull(MediaStore.Images.Media._ID) ?: return null
        val displayName = cursor.getStringOrNull(MediaStore.Images.Media.DISPLAY_NAME)
            ?: "Screenshot_$id"

        val takenAtMillis = cursor.getLongOrNull(MediaStore.Images.Media.DATE_TAKEN)
            ?: cursor.getLongOrNull(MediaStore.Images.Media.DATE_ADDED)?.let {
                TimeUnit.SECONDS.toMillis(it)
            } ?: System.currentTimeMillis()

        val sizeBytes = cursor.getLongOrNull(MediaStore.Images.Media.SIZE) ?: 0L
        val relativePath = cursor.getStringOrNull(MediaStore.Images.Media.RELATIVE_PATH)
        val bucketName = cursor.getStringOrNull(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        val absolutePath = cursor.getStringOrNull(MediaStore.Images.Media.DATA)

        val uri = ContentUris.withAppendedId(baseUri, id).toString()
        val appName = deriveAppName(displayName, relativePath, bucketName)

        return Screenshot(
            id = id,
            uri = uri,
            displayName = displayName,
            appName = appName,
            takenAtMillis = takenAtMillis,
            sizeBytes = sizeBytes,
            absolutePath = absolutePath
        )
    }

    private fun deriveAppName(
        displayName: String,
        relativePath: String?,
        bucketName: String?
    ): String {
        val fromFileName = displayName
            .substringBeforeLast('.', missingDelimiterValue = displayName)
            .split('_', '-', ' ', '@')
            .map { it.trim() }
            .firstOrNull { candidate ->
                candidate.isNotBlank() &&
                    candidate.lowercase(Locale.getDefault()) !in ignoredTokens &&
                    candidate.any { it.isLetter() }
            }?.let(::formatToken)

        if (!fromFileName.isNullOrBlank()) return fromFileName

        val fromBucket = bucketName
            ?.takeIf { it.isNotBlank() && it.lowercase(Locale.getDefault()) != "screenshots" }
            ?.let(::formatToken)
        if (!fromBucket.isNullOrBlank()) return fromBucket

        val fromRelativePath = relativePath
            ?.split('/')
            ?.lastOrNull { segment ->
                segment.isNotBlank() &&
                    segment.lowercase(Locale.getDefault()) !in ignoredTokens
            }
            ?.let(::formatToken)

        return fromRelativePath.orEmpty()
    }

    private fun formatToken(token: String): String {
        val sanitized = token
            .replace("Screenshot", "", ignoreCase = true)
            .replace("IMG", "", ignoreCase = true)
            .replace(Regex("\\d"), "")
            .replace('_', ' ')
            .trim { it <= ' ' || it == '-' }

        if (sanitized.isBlank()) return ""

        val dotted = sanitized.split('.')
        val candidate = dotted.lastOrNull { it.isNotBlank() } ?: sanitized

        return candidate.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    private fun Cursor.getLongOrNull(columnName: String): Long? =
        getColumnIndex(columnName).takeIf { it != -1 }?.let { index ->
            if (isNull(index)) null else getLong(index)
        }

    private fun Cursor.getStringOrNull(columnName: String): String? =
        getColumnIndex(columnName).takeIf { it != -1 }?.let { index ->
            if (isNull(index)) null else getString(index)
        }

    private fun legacyDataColumn(): Array<String> =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            arrayOf(MediaStore.Images.Media.DATA)
        } else {
            emptyArray()
        }

    private companion object {
        val ignoredTokens = setOf(
            "",
            "screenshot",
            "screenshots",
            "image",
            "img",
            "picture",
            "screencap",
            "capture"
        )
    }
}

