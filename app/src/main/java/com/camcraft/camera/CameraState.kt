package com.camcraft.camera

import android.net.Uri
import androidx.compose.ui.geometry.Offset
import com.camcraft.data.AspectRatioOption
import com.camcraft.data.CameraFacing
import com.camcraft.data.FlashMode
import com.camcraft.data.GridMode
import com.camcraft.data.TimerDuration

data class CameraState(
    val isInitializing: Boolean = true,
    val isCapturing: Boolean = false,
    val isBursting: Boolean = false,
    val burstCurrent: Int = 0,
    val burstTotal: Int = 0,
    val currentFacing: CameraFacing = CameraFacing.BACK,
    val flashMode: FlashMode = FlashMode.AUTO,
    val gridMode: GridMode = GridMode.NONE,
    val aspectRatio: AspectRatioOption = AspectRatioOption.RATIO_4_3,
    val timerDuration: TimerDuration = TimerDuration.OFF,
    val countdownSeconds: Int = 0,
    val zoomRatio: Float = 1.0f,
    val minZoom: Float = 1.0f,
    val maxZoom: Float = 8.0f,
    val exposureIndex: Int = 0,
    val minExposure: Int = 0,
    val maxExposure: Int = 0,
    val focusPoint: Offset? = null,
    val hasFlash: Boolean = true,
    val errorMessage: String? = null,
    val lastSavedUri: Uri? = null,
    val triggerShutterAnimation: Boolean = false
)
