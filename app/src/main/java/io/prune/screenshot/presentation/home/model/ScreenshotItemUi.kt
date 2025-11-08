package io.prune.screenshot.presentation.home.model

import io.prune.screenshot.domain.model.Screenshot

data class ScreenshotItemUi(
    val id: Long,
    val title: String,
    val subtitle: String,
    val sizeLabel: String,
    val smartLabel: String?,
    val uri: String,
    val original: Screenshot
)

