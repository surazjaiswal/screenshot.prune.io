package io.prune.screenshot.presentation.home.model

data class ScreenshotBucketUiModel(
    val id: String,
    val title: String,
    val countLabel: String,
    val items: List<ScreenshotItemUi>
)

