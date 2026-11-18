package com.camcraft.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.camcraft.CamCraftApplication
import com.camcraft.camera.CameraManager
import com.camcraft.camera.CameraState
import com.camcraft.data.AspectRatioOption
import com.camcraft.data.CameraFacing
import com.camcraft.data.CameraHardwareStatus
import com.camcraft.data.FlashMode
import com.camcraft.data.GridMode
import com.camcraft.data.TimerDuration
import com.camcraft.utils.HapticFeedbackHelper
import com.camcraft.utils.LocationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CamCraftApplication
    private val settingsManager = app.settingsManager
    private val photoRepository = app.photoRepository
    val cameraManager = CameraManager(application)
    private val hapticHelper = HapticFeedbackHelper(application)
    private val locationHelper = LocationHelper(application)

    private val _cameraState = MutableStateFlow(CameraState())
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    private val _hardwareStatus = MutableStateFlow(CameraHardwareStatus())
    val hardwareStatus: StateFlow<CameraHardwareStatus> = _hardwareStatus.asStateFlow()

    private var countdownJob: Job? = null

    init {
        // Load initial settings
        val settings = settingsManager.settings.value
        _cameraState.value = _cameraState.value.copy(
            currentFacing = settings.defaultCameraFacing,
            aspectRatio = settings.defaultAspectRatio,
            flashMode = settings.defaultFlashMode,
            gridMode = settings.defaultGridMode,
            timerDuration = settings.defaultTimerDuration
        )
        checkHardwareStatus()
    }

    private fun checkHardwareStatus() {
        viewModelScope.launch {
            try {
                cameraManager.getCameraProvider()
                val hasBack = cameraManager.isFacingAvailable(CameraFacing.BACK)
                val hasFront = cameraManager.isFacingAvailable(CameraFacing.FRONT)
                _hardwareStatus.value = CameraHardwareStatus(
                    hasBackCamera = hasBack,
                    hasFrontCamera = hasFront,
                    hasFlash = true
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onCameraBound() {
        val cameraInfo = cameraManager.getCameraInfo()
        if (cameraInfo != null) {
            val zoomState = cameraInfo.zoomState.value
            val exposureState = cameraInfo.exposureState
            val hasFlashUnit = cameraInfo.hasFlashUnit()

            _cameraState.value = _cameraState.value.copy(
                isInitializing = false,
                minZoom = zoomState?.minZoomRatio ?: 1.0f,
                maxZoom = (zoomState?.maxZoomRatio ?: 8.0f).coerceAtMost(10.0f),
                zoomRatio = zoomState?.zoomRatio ?: 1.0f,
                minExposure = exposureState.exposureCompensationRange.lower,
                maxExposure = exposureState.exposureCompensationRange.upper,
                exposureIndex = exposureState.exposureCompensationIndex,
                hasFlash = hasFlashUnit
            )
        } else {
            _cameraState.value = _cameraState.value.copy(isInitializing = false)
        }
    }

    fun toggleCameraFacing() {
        val newFacing = if (_cameraState.value.currentFacing == CameraFacing.BACK) {
            CameraFacing.FRONT
        } else {
            CameraFacing.BACK
        }
        _cameraState.value = _cameraState.value.copy(currentFacing = newFacing)
        settingsManager.updateCameraFacing(newFacing)
    }

    fun setFlashMode(flashMode: FlashMode) {
        _cameraState.value = _cameraState.value.copy(flashMode = flashMode)
        cameraManager.updateFlashMode(flashMode)
        settingsManager.updateFlashMode(flashMode)
    }

    fun cycleFlashMode() {
        val next = when (_cameraState.value.flashMode) {
            FlashMode.AUTO -> FlashMode.ON
            FlashMode.ON -> FlashMode.OFF
            FlashMode.OFF -> FlashMode.AUTO
        }
        setFlashMode(next)
    }

    fun cycleGridMode() {
        val next = when (_cameraState.value.gridMode) {
            GridMode.NONE -> GridMode.RULE_OF_THIRDS
            GridMode.RULE_OF_THIRDS -> GridMode.GRID_4X4
            GridMode.GRID_4X4 -> GridMode.NONE
        }
        _cameraState.value = _cameraState.value.copy(gridMode = next)
        settingsManager.updateGridMode(next)
    }

    fun setAspectRatio(ratio: AspectRatioOption) {
        _cameraState.value = _cameraState.value.copy(aspectRatio = ratio)
        settingsManager.updateAspectRatio(ratio)
    }

    fun cycleAspectRatio() {
        val next = when (_cameraState.value.aspectRatio) {
            AspectRatioOption.RATIO_4_3 -> AspectRatioOption.RATIO_16_9
            AspectRatioOption.RATIO_16_9 -> AspectRatioOption.RATIO_1_1
            AspectRatioOption.RATIO_1_1 -> AspectRatioOption.RATIO_4_3
        }
        setAspectRatio(next)
    }

    fun setTimerDuration(duration: TimerDuration) {
        _cameraState.value = _cameraState.value.copy(timerDuration = duration)
        settingsManager.updateTimerDuration(duration)
    }

    fun cycleTimerDuration() {
        val next = when (_cameraState.value.timerDuration) {
            TimerDuration.OFF -> TimerDuration.SEC_3
            TimerDuration.SEC_3 -> TimerDuration.SEC_5
            TimerDuration.SEC_5 -> TimerDuration.SEC_10
            TimerDuration.SEC_10 -> TimerDuration.OFF
        }
        setTimerDuration(next)
    }

    fun setZoomRatio(ratio: Float) {
        val clamped = ratio.coerceIn(_cameraState.value.minZoom, _cameraState.value.maxZoom)
        _cameraState.value = _cameraState.value.copy(zoomRatio = clamped)
        cameraManager.setZoomRatio(clamped)
    }

    fun setExposure(index: Int) {
        _cameraState.value = _cameraState.value.copy(exposureIndex = index)
        cameraManager.setExposure(index)
    }

    fun setFocusPoint(point: Offset?) {
        _cameraState.value = _cameraState.value.copy(focusPoint = point)
    }

    fun cancelCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        _cameraState.value = _cameraState.value.copy(countdownSeconds = 0)
    }

    fun onShutterPressed() {
        if (_cameraState.value.isCapturing || _cameraState.value.isBursting) return

        val timer = _cameraState.value.timerDuration
        if (timer != TimerDuration.OFF) {
            startCountdown(timer.seconds)
        } else {
            executeSingleCapture()
        }
    }

    private fun startCountdown(seconds: Int) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in seconds downTo 1) {
                _cameraState.value = _cameraState.value.copy(countdownSeconds = i)
                hapticHelper.vibrateTick()
                delay(1000)
            }
            _cameraState.value = _cameraState.value.copy(countdownSeconds = 0)
            executeSingleCapture()
        }
    }

    private fun executeSingleCapture() {
        if (_cameraState.value.isCapturing) return

        viewModelScope.launch {
            _cameraState.value = _cameraState.value.copy(
                isCapturing = true,
                triggerShutterAnimation = true,
                errorMessage = null
            )
            hapticHelper.vibrateShutter()

            // Small delay to allow shutter animation trigger
            delay(100)
            _cameraState.value = _cameraState.value.copy(triggerShutterAnimation = false)

            try {
                val isFront = _cameraState.value.currentFacing == CameraFacing.FRONT
                val bytes = cameraManager.capturePhoto(isFront)

                val location = if (settingsManager.settings.value.geotaggingEnabled) {
                    locationHelper.getLastKnownLocation()
                } else null

                val savedUri = photoRepository.savePhoto(bytes, location)
                _cameraState.value = _cameraState.value.copy(
                    isCapturing = false,
                    lastSavedUri = savedUri
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _cameraState.value = _cameraState.value.copy(
                    isCapturing = false,
                    errorMessage = "Failed to capture image: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    fun executeBurstCapture() {
        if (_cameraState.value.isCapturing || _cameraState.value.isBursting) return

        val total = settingsManager.settings.value.burstCount
        viewModelScope.launch {
            _cameraState.value = _cameraState.value.copy(
                isBursting = true,
                burstTotal = total,
                burstCurrent = 0,
                errorMessage = null
            )

            val isFront = _cameraState.value.currentFacing == CameraFacing.FRONT
            val location = if (settingsManager.settings.value.geotaggingEnabled) {
                locationHelper.getLastKnownLocation()
            } else null

            var lastUri: Uri? = null

            for (i in 1..total) {
                _cameraState.value = _cameraState.value.copy(burstCurrent = i)
                hapticHelper.vibrateBurst()

                try {
                    val bytes = cameraManager.capturePhoto(isFront)
                    lastUri = photoRepository.savePhoto(bytes, location, prefix = "CamCraft_Burst_")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(250) // burst spacing
            }

            _cameraState.value = _cameraState.value.copy(
                isBursting = false,
                burstCurrent = 0,
                burstTotal = 0,
                lastSavedUri = lastUri
            )
        }
    }

    fun clearSavedNotification() {
        _cameraState.value = _cameraState.value.copy(lastSavedUri = null, errorMessage = null)
    }
}
