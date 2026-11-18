package com.camcraft.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.camcraft.data.AppTheme
import com.camcraft.data.AspectRatioOption
import com.camcraft.data.CameraFacing
import com.camcraft.data.FlashMode
import com.camcraft.data.GridMode
import com.camcraft.data.StorageLocationType
import com.camcraft.data.TimerDuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val storageTypeStr = prefs.getString(KEY_STORAGE_TYPE, StorageLocationType.MEDIA_STORE.name)
        val storageType = try {
            StorageLocationType.valueOf(storageTypeStr ?: StorageLocationType.MEDIA_STORE.name)
        } catch (_: Exception) {
            StorageLocationType.MEDIA_STORE
        }

        val facingStr = prefs.getString(KEY_FACING, CameraFacing.BACK.name)
        val facing = try {
            CameraFacing.valueOf(facingStr ?: CameraFacing.BACK.name)
        } catch (_: Exception) {
            CameraFacing.BACK
        }

        val ratioStr = prefs.getString(KEY_RATIO, AspectRatioOption.RATIO_4_3.name)
        val ratio = try {
            AspectRatioOption.valueOf(ratioStr ?: AspectRatioOption.RATIO_4_3.name)
        } catch (_: Exception) {
            AspectRatioOption.RATIO_4_3
        }

        val flashStr = prefs.getString(KEY_FLASH, FlashMode.AUTO.name)
        val flash = try {
            FlashMode.valueOf(flashStr ?: FlashMode.AUTO.name)
        } catch (_: Exception) {
            FlashMode.AUTO
        }

        val gridStr = prefs.getString(KEY_GRID, GridMode.NONE.name)
        val grid = try {
            GridMode.valueOf(gridStr ?: GridMode.NONE.name)
        } catch (_: Exception) {
            GridMode.NONE
        }

        val timerStr = prefs.getString(KEY_TIMER, TimerDuration.OFF.name)
        val timer = try {
            TimerDuration.valueOf(timerStr ?: TimerDuration.OFF.name)
        } catch (_: Exception) {
            TimerDuration.OFF
        }

        val themeStr = prefs.getString(KEY_THEME, AppTheme.DARK.name)
        val theme = try {
            AppTheme.valueOf(themeStr ?: AppTheme.DARK.name)
        } catch (_: Exception) {
            AppTheme.DARK
        }

        return AppSettings(
            storageLocationType = storageType,
            safFolderUri = prefs.getString(KEY_SAF_URI, null),
            safFolderDisplayName = prefs.getString(KEY_SAF_DISPLAY_NAME, "Pictures/CamCraft") ?: "Pictures/CamCraft",
            defaultCameraFacing = facing,
            defaultAspectRatio = ratio,
            defaultFlashMode = flash,
            defaultGridMode = grid,
            defaultTimerDuration = timer,
            burstCount = prefs.getInt(KEY_BURST_COUNT, 3),
            geotaggingEnabled = prefs.getBoolean(KEY_GEOTAGGING, false),
            appTheme = theme
        )
    }

    fun updateStorageLocation(type: StorageLocationType) {
        prefs.edit().putString(KEY_STORAGE_TYPE, type.name).apply()
        _settings.value = _settings.value.copy(storageLocationType = type)
    }

    fun setSafFolder(uri: Uri, displayName: String) {
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        prefs.edit()
            .putString(KEY_SAF_URI, uri.toString())
            .putString(KEY_SAF_DISPLAY_NAME, displayName)
            .putString(KEY_STORAGE_TYPE, StorageLocationType.SAF_CUSTOM.name)
            .apply()

        _settings.value = _settings.value.copy(
            storageLocationType = StorageLocationType.SAF_CUSTOM,
            safFolderUri = uri.toString(),
            safFolderDisplayName = displayName
        )
    }

    fun resetSafFolderToDefault() {
        prefs.edit()
            .remove(KEY_SAF_URI)
            .putString(KEY_SAF_DISPLAY_NAME, "Pictures/CamCraft")
            .putString(KEY_STORAGE_TYPE, StorageLocationType.MEDIA_STORE.name)
            .apply()

        _settings.value = _settings.value.copy(
            storageLocationType = StorageLocationType.MEDIA_STORE,
            safFolderUri = null,
            safFolderDisplayName = "Pictures/CamCraft"
        )
    }

    fun updateCameraFacing(facing: CameraFacing) {
        prefs.edit().putString(KEY_FACING, facing.name).apply()
        _settings.value = _settings.value.copy(defaultCameraFacing = facing)
    }

    fun updateAspectRatio(ratio: AspectRatioOption) {
        prefs.edit().putString(KEY_RATIO, ratio.name).apply()
        _settings.value = _settings.value.copy(defaultAspectRatio = ratio)
    }

    fun updateFlashMode(flash: FlashMode) {
        prefs.edit().putString(KEY_FLASH, flash.name).apply()
        _settings.value = _settings.value.copy(defaultFlashMode = flash)
    }

    fun updateGridMode(grid: GridMode) {
        prefs.edit().putString(KEY_GRID, grid.name).apply()
        _settings.value = _settings.value.copy(defaultGridMode = grid)
    }

    fun updateTimerDuration(timer: TimerDuration) {
        prefs.edit().putString(KEY_TIMER, timer.name).apply()
        _settings.value = _settings.value.copy(defaultTimerDuration = timer)
    }

    fun updateBurstCount(count: Int) {
        val safeCount = count.coerceIn(2, 10)
        prefs.edit().putInt(KEY_BURST_COUNT, safeCount).apply()
        _settings.value = _settings.value.copy(burstCount = safeCount)
    }

    fun updateGeotagging(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GEOTAGGING, enabled).apply()
        _settings.value = _settings.value.copy(geotaggingEnabled = enabled)
    }

    fun updateTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        _settings.value = _settings.value.copy(appTheme = theme)
    }

    companion object {
        private const val PREFS_NAME = "camcraft_preferences"
        private const val KEY_STORAGE_TYPE = "storage_type"
        private const val KEY_SAF_URI = "saf_uri"
        private const val KEY_SAF_DISPLAY_NAME = "saf_display_name"
        private const val KEY_FACING = "camera_facing"
        private const val KEY_RATIO = "aspect_ratio"
        private const val KEY_FLASH = "flash_mode"
        private const val KEY_GRID = "grid_mode"
        private const val KEY_TIMER = "timer_duration"
        private const val KEY_BURST_COUNT = "burst_count"
        private const val KEY_GEOTAGGING = "geotagging"
        private const val KEY_THEME = "app_theme"
    }
}
