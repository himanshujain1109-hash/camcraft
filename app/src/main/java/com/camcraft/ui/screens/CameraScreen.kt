package com.camcraft.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.camcraft.camera.CameraState
import com.camcraft.permissions.PermissionManager
import com.camcraft.ui.components.BurstProgressOverlay
import com.camcraft.ui.components.CameraBottomBar
import com.camcraft.ui.components.CameraPreview
import com.camcraft.ui.components.CameraTopBar
import com.camcraft.ui.components.PermissionDialog
import com.camcraft.ui.components.TimerOverlay
import com.camcraft.ui.components.ZoomControls
import com.camcraft.ui.theme.AccentElectricBlue
import com.camcraft.ui.theme.DarkSurface
import com.camcraft.ui.theme.SuccessGreen
import com.camcraft.ui.theme.TextPrimaryDark
import com.camcraft.viewmodel.CameraViewModel
import com.camcraft.viewmodel.GalleryViewModel
import kotlinx.coroutines.delay

@Composable
fun CameraScreen(
    cameraViewModel: CameraViewModel,
    galleryViewModel: GalleryViewModel,
    onNavigateToGallery: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPhotoViewer: (String) -> Unit
) {
    val context = LocalContext.current
    val cameraState by cameraViewModel.cameraState.collectAsState()
    val latestPhoto by galleryViewModel.photoRepository.latestPhoto.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(PermissionManager.hasCameraPermission(context))
    }
    var showPermissionRationale by remember { mutableStateOf(false) }
    var isPermanentDenial by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            isPermanentDenial = true
            showPermissionRationale = true
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Success Banner Auto-Dismiss
    LaunchedEffect(cameraState.lastSavedUri) {
        if (cameraState.lastSavedUri != null) {
            galleryViewModel.refresh()
            delay(3000)
            cameraViewModel.clearSavedNotification()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            // Live Camera Preview
            CameraPreview(
                cameraViewModel = cameraViewModel,
                cameraState = cameraState,
                modifier = Modifier.fillMaxSize()
            )

            // Shutter Flash Animation Overlay
            AnimatedVisibility(
                visible = cameraState.triggerShutterAnimation,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize().background(Color.White))
            }

            // Top Bar Controls
            CameraTopBar(
                cameraState = cameraState,
                onFlashClick = { cameraViewModel.cycleFlashMode() },
                onTimerClick = { cameraViewModel.cycleTimerDuration() },
                onGridClick = { cameraViewModel.cycleGridMode() },
                onRatioClick = { cameraViewModel.cycleAspectRatio() },
                onSettingsClick = onNavigateToSettings,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Zoom Quick Controls
            ZoomControls(
                currentZoom = cameraState.zoomRatio,
                minZoom = cameraState.minZoom,
                maxZoom = cameraState.maxZoom,
                onZoomSelected = { cameraViewModel.setZoomRatio(it) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
            )

            // Bottom Bar Controls
            CameraBottomBar(
                cameraState = cameraState,
                latestPhotoUri = latestPhoto?.uri,
                onShutterClick = { cameraViewModel.onShutterPressed() },
                onBurstClick = { cameraViewModel.executeBurstCapture() },
                onSwitchCameraClick = { cameraViewModel.toggleCameraFacing() },
                onGalleryClick = onNavigateToGallery,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Self-Timer Countdown Overlay
            TimerOverlay(
                secondsRemaining = cameraState.countdownSeconds,
                onCancel = { cameraViewModel.cancelCountdown() }
            )

            // Burst Mode Progress Overlay
            BurstProgressOverlay(
                current = cameraState.burstCurrent,
                total = cameraState.burstTotal
            )

            // Photo Saved Success Toast Banner
            AnimatedVisibility(
                visible = cameraState.lastSavedUri != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 70.dp, start = 20.dp, end = 20.dp)
            ) {
                Surface(
                    color = DarkSurface.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Photo Captured & Saved",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "Saved to storage location",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AccentElectricBlue
                            )
                        }
                    }
                }
            }
        } else {
            // Permission Blocked State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "Camera Permission Required",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(
                        text = "Please grant camera permission to capture photos and view live preview.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
        }

        // Permission Rationale Dialog
        if (showPermissionRationale) {
            PermissionDialog(
                title = "Camera Access Required",
                description = "CamCraft requires camera access to preview and capture high-resolution photos.",
                isPermanentDenial = isPermanentDenial,
                onGrantPermission = {
                    showPermissionRationale = false
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onOpenSettings = {
                    showPermissionRationale = false
                    PermissionManager.openAppSettings(context)
                },
                onDismiss = { showPermissionRationale = false }
            )
        }
    }
}
