package io.prune.screenshot.domain.usecase

import io.prune.screenshot.domain.model.CategorizedScreenshots
import io.prune.screenshot.domain.model.Screenshot
import io.prune.screenshot.domain.model.ScreenshotGroup
import io.prune.screenshot.domain.model.ScreenshotGroupingKey
import io.prune.screenshot.domain.model.SmartCategory

class CategorizeScreenshotsUseCase {

    operator fun invoke(screenshots: List<Screenshot>): CategorizedScreenshots {
        val byApp = screenshots
            .groupBy { it.appName.ifBlank { "Unknown App" } }
            .map { (app, shots) ->
                ScreenshotGroup(
                    key = ScreenshotGroupingKey.App(app),
                    screenshots = shots
                )
            }
            .sortedBy { it.key.label.lowercase() }

        val bySmart = screenshots
            .groupBy { it.smartCategory }
            .filterKeys { it != SmartCategory.Unknown }
            .map { (smartCategory, shots) ->
                ScreenshotGroup(
                    key = ScreenshotGroupingKey.Smart(smartCategory),
                    screenshots = shots
                )
            }
            .sortedBy { it.key.label.lowercase() }

        return CategorizedScreenshots(
            byApp = byApp,
            bySmartCategory = bySmart
        )
    }
}

