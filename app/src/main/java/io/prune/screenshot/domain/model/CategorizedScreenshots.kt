package io.prune.screenshot.domain.model

/**
 * Bundled result containing multiple views over the screenshot library.
 */
data class CategorizedScreenshots(
    val byApp: List<ScreenshotGroup>,
    val bySmartCategory: List<ScreenshotGroup>
)

