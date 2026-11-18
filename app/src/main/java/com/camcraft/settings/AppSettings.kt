package com.camcraft.settings

import com.camcraft.data.AppTheme
import com.camcraft.data.AspectRatioOption
import com.camcraft.data.CameraFacing
import com.camcraft.data.FlashMode
import com.camcraft.data.GridMode
import com.camcraft.data.StorageLocationType
import com.camcraft.data.TimerDuration

data class AppSettings(
    val storageLocationType: StorageLocationType = StorageLocationType.MEDIA_STORE,
    val safFolderUri: String? = null,
    val safFolderDisplayName: String = "Pictures/CamCraft",
    val defaultCameraFacing: CameraFacing = CameraFacing.BACK,
    val defaultAspectRatio: AspectRatioOption = AspectRatioOption.RATIO_4_3,
    val defaultFlashMode: FlashMode = FlashMode.AUTO,
    val defaultGridMode: GridMode = GridMode.NONE,
    val defaultTimerDuration: TimerDuration = TimerDuration.OFF,
    val burstCount: Int = 3,
    val geotaggingEnabled: Boolean = false,
    val appTheme: AppTheme = AppTheme.DARK
)
