package io.prune.screenshot.domain.model

/**
 * Domain-friendly grouping of screenshots for presentation.
 */
data class ScreenshotGroup(
    val key: ScreenshotGroupingKey,
    val screenshots: List<Screenshot>
)

/**
 * Identifier for a bucket/group of screenshots.
 */
sealed class ScreenshotGroupingKey(open val label: String) {
    data class App(val appName: String) : ScreenshotGroupingKey(
        label = if (appName.isBlank()) "Unknown App" else appName
    )

    data class Smart(val smartCategory: SmartCategory) :
        ScreenshotGroupingKey(label = smartCategory.displayName)

    data class Folder(val folder: ScreenshotFolder) :
        ScreenshotGroupingKey(label = folder.displayName)

    data object Uncategorized : ScreenshotGroupingKey(label = "Uncategorized")
}

