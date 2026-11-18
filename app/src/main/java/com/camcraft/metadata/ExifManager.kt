package com.camcraft.metadata

import android.content.Context
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExifManager {

    suspend fun readMetadata(context: Context, uri: Uri): PhotoMetadata = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) return@withContext PhotoMetadata()

            inputStream.use { stream ->
                val exif = ExifInterface(stream)
                val latLong = exif.latLong
                val hasGps = latLong != null

                PhotoMetadata(
                    dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME)
                        ?: exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL),
                    width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0).let {
                        if (it == 0) exif.getAttributeInt(ExifInterface.TAG_PIXEL_X_DIMENSION, 0) else it
                    },
                    height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0).let {
                        if (it == 0) exif.getAttributeInt(ExifInterface.TAG_PIXEL_Y_DIMENSION, 0) else it
                    },
                    deviceMake = exif.getAttribute(ExifInterface.TAG_MAKE) ?: Build.MANUFACTURER,
                    deviceModel = exif.getAttribute(ExifInterface.TAG_MODEL) ?: Build.MODEL,
                    iso = exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY),
                    aperture = exif.getAttribute(ExifInterface.TAG_F_NUMBER)?.let { "f/$it" },
                    exposureTime = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)?.let { "${it}s" },
                    focalLength = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)?.let { "${it}mm" },
                    hasGps = hasGps,
                    latitude = latLong?.getOrNull(0),
                    longitude = latLong?.getOrNull(1),
                    altitude = exif.getAltitude(0.0)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PhotoMetadata()
        }
    }

    suspend fun writeExifToFile(
        file: File,
        location: Location? = null,
        userComment: String = "Captured with CamCraft"
    ) = withContext(Dispatchers.IO) {
        try {
            val exif = ExifInterface(file)
            val now = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US).format(Date())

            exif.setAttribute(ExifInterface.TAG_DATETIME, now)
            exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, now)
            exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, now)
            exif.setAttribute(ExifInterface.TAG_MAKE, Build.MANUFACTURER.replaceFirstChar { it.uppercase() })
            exif.setAttribute(ExifInterface.TAG_MODEL, Build.MODEL)
            exif.setAttribute(ExifInterface.TAG_SOFTWARE, "CamCraft 1.0")
            exif.setAttribute(ExifInterface.TAG_USER_COMMENT, userComment)

            if (location != null) {
                exif.setGpsInfo(location)
            }

            exif.saveAttributes()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun writeExifToUri(
        context: Context,
        uri: Uri,
        location: Location? = null
    ) = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openFileDescriptor(uri, "rw")?.use { pfd ->
                val exif = ExifInterface(pfd.fileDescriptor)
                val now = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US).format(Date())

                exif.setAttribute(ExifInterface.TAG_DATETIME, now)
                exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, now)
                exif.setAttribute(ExifInterface.TAG_MAKE, Build.MANUFACTURER.replaceFirstChar { it.uppercase() })
                exif.setAttribute(ExifInterface.TAG_MODEL, Build.MODEL)
                exif.setAttribute(ExifInterface.TAG_SOFTWARE, "CamCraft 1.0")

                if (location != null) {
                    exif.setGpsInfo(location)
                }

                exif.saveAttributes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
