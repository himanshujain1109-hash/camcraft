package com.camcraft.storage

import android.content.Context
import android.location.Location
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.camcraft.data.CapturedPhoto
import com.camcraft.metadata.ExifManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SafStorageManager(private val context: Context) {

    suspend fun saveImageToSafFolder(
        treeUri: Uri,
        jpegBytes: ByteArray,
        location: Location? = null,
        prefix: String = "CamCraft_"
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val rootDoc = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext null
            if (!rootDoc.exists() || !rootDoc.canWrite()) return@withContext null

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
            val fileName = "$prefix$timestamp.jpg"

            val newFile = rootDoc.createFile("image/jpeg", fileName) ?: return@withContext null
            val fileUri = newFile.uri

            context.contentResolver.openOutputStream(fileUri)?.use { out ->
                out.write(jpegBytes)
                out.flush()
            }

            // Write EXIF metadata
            ExifManager.writeExifToUri(context, fileUri, location)

            fileUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getPhotosFromSafFolder(treeUri: Uri): List<CapturedPhoto> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<CapturedPhoto>()
        try {
            val rootDoc = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext emptyList()
            val files = rootDoc.listFiles()

            for (file in files) {
                if (file.isFile && (file.type?.startsWith("image/") == true || file.name?.endsWith(".jpg", true) == true || file.name?.endsWith(".jpeg", true) == true)) {
                    photos.add(
                        CapturedPhoto(
                            id = file.uri.toString(),
                            uri = file.uri,
                            displayName = file.name ?: "SAF Photo",
                            dateTaken = file.lastModified(),
                            size = file.length(),
                            width = 0,
                            height = 0,
                            isSaf = true
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        photos.sortedByDescending { it.dateTaken }
    }

    suspend fun deleteSafPhoto(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val doc = DocumentFile.fromSingleUri(context, uri)
            doc?.delete() == true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
