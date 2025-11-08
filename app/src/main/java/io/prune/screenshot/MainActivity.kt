package io.prune.screenshot

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import io.prune.screenshot.presentation.ScreenshotOrganizerApp
import io.prune.screenshot.presentation.home.ScreenshotHomeViewModel

/**
 * Entry point to the presentation layer. We keep the activity extremely lean and delegate
 * all behaviour to the composable + ViewModel layer to stay aligned with Clean Architecture.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: ScreenshotHomeViewModel by viewModels {
        (application as ScreenshotApplication).container.provideHomeViewModelFactory()
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
            if (hasAllPermissions()) {
                viewModel.refresh()
            } else {
                Toast.makeText(
                    this,
                    R.string.storage_permission_required,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScreenshotOrganizerApp(
                homeViewModel = viewModel,
                onRefresh = { refreshIfPermitted() }
            )
        }
    }

    private fun refreshIfPermitted() {
        if (hasAllPermissions()) {
            viewModel.refresh()
        } else {
            permissionLauncher.launch(requiredPermissions())
        }
    }

    private fun requiredPermissions(): Array<String> = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES
        )

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        else -> arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private fun hasAllPermissions(): Boolean =
        requiredPermissions().all { permission ->
            ContextCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED
        }
}