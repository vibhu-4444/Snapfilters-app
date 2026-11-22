package com.snapfilters

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.snapfilters.core.designsystem.component.PermissionCard
import com.snapfilters.core.designsystem.component.PermissionState
import com.snapfilters.core.designsystem.theme.SnapTheme
import com.snapfilters.feature.camera.ui.CameraRoute

class MainActivity : ComponentActivity() {

    private var hasCameraPermission by mutableStateOf(false)
    private var isPermanentlyDenied by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            isPermanentlyDenied = !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermission()

        setContent {
            SnapTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SnapTheme.colors.surfaceBase)
                ) {
                    if (hasCameraPermission) {
                        AppNavHost()
                    } else {
                        PermissionCard(
                            permissionName = "Camera",
                            rationale = "Snapfilters requires camera access to capture photographs and preview real-time cinematic filters.",
                            state = if (isPermanentlyDenied) PermissionState.PermanentlyDenied else PermissionState.Denied,
                            onRequestPermission = { checkPermission() },
                            onOpenSettings = {
                                val intent = android.content.Intent(
                                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    android.net.Uri.fromParts("package", packageName, null)
                                )
                                startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun checkPermission() {
        val status = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (status == PackageManager.PERMISSION_GRANTED) {
            hasCameraPermission = true
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "camera"
    ) {
        composable("camera") {
            CameraRoute(
                onNavigateToEditor = { captureResult ->
                    // Navigation to editor
                },
                onNavigateToLibrary = {
                    // Navigation to library
                }
            )
        }
    }
}
