package com.camcraft.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.camcraft.CamCraftApplication
import com.camcraft.data.AppTheme
import com.camcraft.data.AspectRatioOption
import com.camcraft.data.CameraFacing
import com.camcraft.data.FlashMode
import com.camcraft.data.GridMode
import com.camcraft.data.StorageLocationType
import com.camcraft.data.TimerDuration
import com.camcraft.settings.AppSettings
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CamCraftApplication
    private val settingsManager = app.settingsManager

    val settings: StateFlow<AppSettings> = settingsManager.settings

    fun setStorageLocation(type: StorageLocationType) {
        settingsManager.updateStorageLocation(type)
    }

    fun setSafFolder(uri: Uri, displayName: String) {
        settingsManager.setSafFolder(uri, displayName)
    }

    fun resetSafFolder() {
        settingsManager.resetSafFolderToDefault()
    }

    fun setDefaultCameraFacing(facing: CameraFacing) {
        settingsManager.updateCameraFacing(facing)
    }

    fun setDefaultAspectRatio(ratio: AspectRatioOption) {
        settingsManager.updateAspectRatio(ratio)
    }

    fun setDefaultFlashMode(flash: FlashMode) {
        settingsManager.updateFlashMode(flash)
    }

    fun setDefaultGridMode(grid: GridMode) {
        settingsManager.updateGridMode(grid)
    }

    fun setDefaultTimerDuration(timer: TimerDuration) {
        settingsManager.updateTimerDuration(timer)
    }

    fun setBurstCount(count: Int) {
        settingsManager.updateBurstCount(count)
    }

    fun setGeotagging(enabled: Boolean) {
        settingsManager.updateGeotagging(enabled)
    }

    fun setAppTheme(theme: AppTheme) {
        settingsManager.updateTheme(theme)
    }
}
