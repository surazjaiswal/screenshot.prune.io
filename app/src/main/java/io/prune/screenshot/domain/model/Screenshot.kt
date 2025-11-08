package io.prune.screenshot.domain.model

/**
 * Domain representation of a screenshot item sourced from the device.
 *
 * @param id Unique identifier provided by the system (MediaStore id).
 * @param uri String representation of the content Uri (kept as String to keep the domain pure).
 * @param displayName Friendly filename displayed to the user.
 * @param appName Parsed app or source name extracted from the filename/path.
 * @param takenAtMillis UTC epoch millis when the screenshot was captured.
 * @param sizeBytes File size in bytes.
 * @param smartCategory Optional smart classification, defaults to [SmartCategory.Unknown].
 */
data class Screenshot(
    val id: Long,
    val uri: String,
    val displayName: String,
    val appName: String,
    val takenAtMillis: Long,
    val sizeBytes: Long,
    val smartCategory: SmartCategory = SmartCategory.Unknown,
    val absolutePath: String? = null
)

