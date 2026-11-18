package com.camcraft.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import com.camcraft.storage.PhotoRepository
import com.camcraft.utils.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class EditorState(
    val originalUri: Uri? = null,
    val originalBitmap: Bitmap? = null,
    val previewBitmap: Bitmap? = null,
    val rotationAngle: Int = 0,
    val selectedRatio: CropAspectRatio = CropAspectRatio.FREE,
    val cropRect: RectF = RectF(0f, 0f, 1f, 1f),
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccessUri: Uri? = null,
    val errorMessage: String? = null
)

class ImageEditor(
    private val context: Context,
    private val photoRepository: PhotoRepository
) {
    suspend fun saveEditedImage(
        originalBitmap: Bitmap,
        cropRect: RectF,
        rotationAngle: Int,
        brightness: Float,
        contrast: Float,
        saturation: Float
    ): Uri? = withContext(Dispatchers.Default) {
        try {
            // 1. Rotate
            var processed = if (rotationAngle % 360 != 0) {
                BitmapUtils.rotateBitmap(originalBitmap, rotationAngle)
            } else {
                originalBitmap
            }

            // 2. Crop
            val cropped = BitmapUtils.cropBitmap(processed, cropRect)
            if (processed != originalBitmap && processed != cropped) {
                processed.recycle()
            }
            processed = cropped

            // 3. Color Adjustments
            val adjusted = BitmapUtils.applyColorAdjustments(processed, brightness, contrast, saturation)
            if (processed != adjusted && processed != originalBitmap) {
                processed.recycle()
            }

            // 4. Compress to JPEG
            val bytes = BitmapUtils.bitmapToByteArray(adjusted, quality = 95)
            adjusted.recycle()

            // 5. Save as separate file via PhotoRepository
            photoRepository.savePhoto(bytes, prefix = "CamCraft_Edit_")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
