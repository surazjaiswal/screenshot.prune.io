package io.prune.screenshot.domain.model

/**
 * Represents a folder the user can move screenshots into.
 */
data class ScreenshotFolder(
    val displayName: String,
    val relativePath: String
)

