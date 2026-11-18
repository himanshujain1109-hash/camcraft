package com.camcraft.ui.components

import androidx.camera.view.PreviewView
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.camcraft.camera.CameraState
import com.camcraft.viewmodel.CameraViewModel

@Composable
fun CameraPreview(
    cameraViewModel: CameraViewModel,
    cameraState: CameraState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    // Rebind camera when facing or aspect ratio changes
    LaunchedEffect(cameraState.currentFacing, cameraState.aspectRatio) {
        try {
            cameraViewModel.cameraManager.bindCamera(
                lifecycleOwner = lifecycleOwner,
                previewView = previewView,
                facing = cameraState.currentFacing,
                aspectRatioOption = cameraState.aspectRatio,
                flashMode = cameraState.flashMode
            )
            cameraViewModel.onCameraBound()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    val newZoom = cameraState.zoomRatio * zoom
                    cameraViewModel.setZoomRatio(newZoom)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    cameraViewModel.setFocusPoint(offset)
                    val factory = previewView.meteringPointFactory
                    val point = factory.createPoint(offset.x, offset.y)
                    cameraViewModel.cameraManager.startFocusAndMetering(point)
                }
            }
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Grid Overlay
        GridOverlay(gridMode = cameraState.gridMode)

        // Tap-to-focus ring
        FocusIndicator(
            focusPoint = cameraState.focusPoint,
            onAnimationEnd = { cameraViewModel.setFocusPoint(null) }
        )
    }
}
