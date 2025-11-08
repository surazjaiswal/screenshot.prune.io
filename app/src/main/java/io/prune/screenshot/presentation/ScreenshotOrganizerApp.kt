package io.prune.screenshot.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.prune.screenshot.presentation.home.ScreenshotHomeRoute
import io.prune.screenshot.presentation.home.ScreenshotHomeViewModel
import io.prune.screenshot.presentation.ui.theme.ScreenshotOrganizerTheme

@Composable
fun ScreenshotOrganizerApp(
    homeViewModel: ScreenshotHomeViewModel,
    onRefresh: () -> Unit
) {
    ScreenshotOrganizerTheme {
        LaunchedEffect(Unit) {
            onRefresh()
        }
        ScreenshotHomeRoute(
            viewModel = homeViewModel,
            onRefresh = onRefresh
        )
    }
}

