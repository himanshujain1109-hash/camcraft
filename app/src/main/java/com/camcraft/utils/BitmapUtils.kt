package com.camcraft.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.max
import kotlin.math.roundToInt

object BitmapUtils {

    suspend fun decodeSampledBitmapFromUri(
        context: Context,
        uri: Uri,
        reqWidth: Int = 2048,
        reqHeight: Int = 2048
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            var input: InputStream? = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(input, null, options)
            input?.close()

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            input = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(input, null, options)
            input?.close()

            if (bitmap != null) {
                // Correct orientation from EXIF if needed
                val orientation = getExifOrientation(context, uri)
                if (orientation != 0) {
                    return@withContext rotateBitmap(bitmap, orientation)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return max(1, inSampleSize)
    }

    fun getExifOrientation(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun rotateBitmap(source: Bitmap, angle: Int): Bitmap {
        if (angle % 360 == 0) return source
        val matrix = Matrix().apply { postRotate(angle.toFloat()) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun mirrorBitmap(source: Bitmap): Bitmap {
        val matrix = Matrix().apply { preScale(-1.0f, 1.0f) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun cropBitmap(source: Bitmap, normalizedRect: RectF): Bitmap {
        val left = (normalizedRect.left * source.width).roundToInt().coerceIn(0, source.width - 1)
        val top = (normalizedRect.top * source.height).roundToInt().coerceIn(0, source.height - 1)
        val right = (normalizedRect.right * source.width).roundToInt().coerceIn(left + 1, source.width)
        val bottom = (normalizedRect.bottom * source.height).roundToInt().coerceIn(top + 1, source.height)

        val cropWidth = right - left
        val cropHeight = bottom - top

        return Bitmap.createBitmap(source, left, top, cropWidth, cropHeight)
    }

    fun applyColorAdjustments(
        source: Bitmap,
        brightness: Float, // -1f to 1f (0 is default)
        contrast: Float,   // 0.5f to 2f (1 is default)
        saturation: Float  // 0f to 2f (1 is default)
    ): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val cm = ColorMatrix()

        // Saturation
        val satMatrix = ColorMatrix().apply { setSaturation(saturation) }
        cm.postConcat(satMatrix)

        // Contrast and Brightness
        val scale = contrast
        val translate = brightness * 255f + (1f - scale) * 128f
        val cbMatrix = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(cbMatrix)

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    suspend fun saveBitmapToStream(
        bitmap: Bitmap,
        outputStream: OutputStream,
        quality: Int = 95
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            outputStream.use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun bitmapToByteArray(bitmap: Bitmap, quality: Int = 95): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }
}
