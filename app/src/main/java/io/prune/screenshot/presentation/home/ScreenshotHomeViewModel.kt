package io.prune.screenshot.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.prune.screenshot.domain.model.CategorizedScreenshots
import io.prune.screenshot.domain.model.Screenshot
import io.prune.screenshot.domain.model.ScreenshotFolder
import io.prune.screenshot.domain.model.SortOption
import io.prune.screenshot.domain.usecase.DeleteScreenshotUseCase
import io.prune.screenshot.domain.usecase.GetAvailableDestinationsUseCase
import io.prune.screenshot.domain.usecase.GetScreenshotLibraryUseCase
import io.prune.screenshot.domain.usecase.MoveScreenshotUseCase
import io.prune.screenshot.presentation.home.model.ScreenshotBucketUiModel
import io.prune.screenshot.presentation.home.model.ScreenshotItemUi
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Coordinates the presentation state for the home screen. It orchestrates high level use
 * cases so the UI stays declarative and dumb: the ViewModel owns sorting, grouping, and
 * dispatching mutating actions (delete/move) while exposing a single state flow.
 */
class ScreenshotHomeViewModel(
    private val getScreenshotLibraryUseCase: GetScreenshotLibraryUseCase,
    private val deleteScreenshotUseCase: DeleteScreenshotUseCase,
    private val moveScreenshotUseCase: MoveScreenshotUseCase,
    private val getAvailableDestinationsUseCase: GetAvailableDestinationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenshotHomeUiState())
    val uiState: StateFlow<ScreenshotHomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    private var latestScreenshots: Map<Long, Screenshot> = emptyMap()

    fun refresh() {
        load(sortOption = _uiState.value.sortOption)
    }

    fun onSortSelected(sortOption: SortOption) {
        if (sortOption == _uiState.value.sortOption) return
        load(sortOption)
    }

    fun onDeleteScreenshot(id: Long) {
        val screenshot = latestScreenshots[id] ?: return
        viewModelScope.launch {
            deleteScreenshotUseCase(screenshot)
                .onSuccess {
                    _events.emit(HomeEvent.ShowMessage("Screenshot deleted"))
                    refresh()
                }
                .onFailure { throwable ->
                    _events.emit(
                        HomeEvent.ShowMessage(
                            throwable.message ?: "Unable to delete screenshot"
                        )
                    )
                }
        }
    }

    fun onMoveScreenshot(id: Long, destination: ScreenshotFolder) {
        val screenshot = latestScreenshots[id] ?: return
        viewModelScope.launch {
            moveScreenshotUseCase(screenshot, destination)
                .onSuccess {
                    _events.emit(HomeEvent.ShowMessage("Moved to ${destination.displayName}"))
                    refresh()
                }
                .onFailure { throwable ->
                    _events.emit(
                        HomeEvent.ShowMessage(
                            throwable.message ?: "Unable to move screenshot"
                        )
                    )
                }
        }
    }

    private fun load(sortOption: SortOption) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching {
                val categorized = getScreenshotLibraryUseCase(sortOption)
                val destinations = getAvailableDestinationsUseCase()
                categorized to destinations
            }.onSuccess { (categorized, destinations) ->
                latestScreenshots = categorized.collectAllScreenshots()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        sortOption = sortOption,
                        appBuckets = categorized.byApp.toUiBuckets(),
                        smartBuckets = categorized.bySmartCategory.toUiBuckets(),
                        destinations = destinations,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Unable to load screenshots"
                    )
                }
            }
        }
    }

    private fun CategorizedScreenshots.collectAllScreenshots(): Map<Long, Screenshot> =
        (byApp + bySmartCategory)
            .flatMap { it.screenshots }
            .associateBy { it.id }

    private fun List<io.prune.screenshot.domain.model.ScreenshotGroup>.toUiBuckets(): List<ScreenshotBucketUiModel> =
        map { group ->
            ScreenshotBucketUiModel(
                id = group.key.label,
                title = group.key.label,
                countLabel = "${group.screenshots.size} item${if (group.screenshots.size == 1) "" else "s"}",
                items = group.screenshots.map(::toScreenshotItemUi)
            )
        }

    private fun toScreenshotItemUi(screenshot: Screenshot): ScreenshotItemUi {
        return ScreenshotItemUi(
            id = screenshot.id,
            title = screenshot.displayName,
            subtitle = formatDate(screenshot.takenAtMillis),
            sizeLabel = formatSize(screenshot.sizeBytes),
            smartLabel = screenshot.smartCategory.takeIf {
                it.displayName.isNotBlank() && it.displayName != "Unclassified"
            }?.displayName,
            uri = screenshot.uri,
            original = screenshot
        )
    }

    private fun formatDate(millis: Long): String =
        DateFormat.getDateTimeInstance(
            DateFormat.SHORT,
            DateFormat.SHORT,
            Locale.getDefault()
        ).format(Date(millis))

    private fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0))
            .toInt()
            .coerceAtMost(units.lastIndex)
        val normalized = bytes / 1024.0.pow(digitGroups.toDouble())
        val rounded = (normalized * 10.0).roundToInt() / 10.0
        return String.format(Locale.getDefault(), "%.1f %s", rounded, units[digitGroups])
    }

    sealed class HomeEvent {
        data class ShowMessage(val message: String) : HomeEvent()
    }

    class Factory(
        private val getScreenshotLibraryUseCase: GetScreenshotLibraryUseCase,
        private val deleteScreenshotUseCase: DeleteScreenshotUseCase,
        private val moveScreenshotUseCase: MoveScreenshotUseCase,
        private val getAvailableDestinationsUseCase: GetAvailableDestinationsUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ScreenshotHomeViewModel::class.java)) {
                return ScreenshotHomeViewModel(
                    getScreenshotLibraryUseCase,
                    deleteScreenshotUseCase,
                    moveScreenshotUseCase,
                    getAvailableDestinationsUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

