package com.camcraft.storage

import android.content.Context
import android.location.Location
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.camcraft.data.CapturedPhoto
import com.camcraft.metadata.ExifManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppStorageManager(private val context: Context) {

    private val privatePicturesDir: File
        get() {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: File(context.filesDir, "pictures")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    suspend fun saveImage(
        jpegBytes: ByteArray,
        location: Location? = null,
        prefix: String = "CamCraft_"
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
            val file = File(privatePicturesDir, "$prefix$timestamp.jpg")

            FileOutputStream(file).use { out ->
                out.write(jpegBytes)
                out.flush()
            }

            ExifManager.writeExifToFile(file, location)

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getPhotos(): List<CapturedPhoto> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<CapturedPhoto>()
        try {
            val files = privatePicturesDir.listFiles() ?: emptyArray()
            for (file in files) {
                if (file.isFile && file.name.endsWith(".jpg", ignoreCase = true)) {
                    val contentUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    photos.add(
                        CapturedPhoto(
                            id = file.absolutePath,
                            uri = contentUri,
                            displayName = file.name,
                            dateTaken = file.lastModified(),
                            size = file.length(),
                            isSaf = false,
                            absolutePath = file.absolutePath
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        photos.sortedByDescending { it.dateTaken }
    }

    suspend fun deletePhoto(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val files = privatePicturesDir.listFiles() ?: return@withContext false
            for (file in files) {
                val fileUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                if (fileUri == uri) {
                    return@withContext file.delete()
                }
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
