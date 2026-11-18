package com.camcraft.data

import android.net.Uri

data class CapturedPhoto(
    val id: String,
    val uri: Uri,
    val displayName: String,
    val dateTaken: Long,
    val size: Long,
    val width: Int = 0,
    val height: Int = 0,
    val isSaf: Boolean = false,
    val absolutePath: String? = null
)

enum class CameraFacing {
    BACK,
    FRONT
}

enum class FlashMode {
    OFF,
    ON,
    AUTO
}

enum class GridMode(val title: String) {
    NONE("No Grid"),
    RULE_OF_THIRDS("3×3"),
    GRID_4X4("4×4")
}

enum class AspectRatioOption(val title: String, val ratioNumerator: Int, val ratioDenominator: Int) {
    RATIO_4_3("4:3", 4, 3),
    RATIO_16_9("16:9", 16, 9),
    RATIO_1_1("1:1", 1, 1)
}

enum class TimerDuration(val seconds: Int, val label: String) {
    OFF(0, "Off"),
    SEC_3(3, "3s"),
    SEC_5(5, "5s"),
    SEC_10(10, "10s")
}

enum class StorageLocationType(val title: String, val description: String) {
    MEDIA_STORE("Public Gallery (Pictures/CamCraft)", "Saved to standard Android MediaStore. Photos immediately appear in Google Photos / Gallery."),
    SAF_CUSTOM("Custom Folder (SAF)", "Select any external directory or SD card folder via Android Storage Access Framework."),
    APP_PRIVATE("App Private Storage", "Internal app folder. Hidden from external gallery apps.")
}

enum class AppTheme(val label: String) {
    DARK("Dark"),
    LIGHT("Light"),
    SYSTEM("System Default")
}

data class CameraHardwareStatus(
    val hasBackCamera: Boolean = true,
    val hasFrontCamera: Boolean = true,
    val hasFlash: Boolean = true,
    val isBackAvailable: Boolean = true,
    val isFrontAvailable: Boolean = true,
    val minZoomRatio: Float = 1.0f,
    val maxZoomRatio: Float = 8.0f
)
