package com.camcraft.storage

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import com.camcraft.data.CapturedPhoto
import com.camcraft.data.StorageLocationType
import com.camcraft.settings.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhotoRepository(
    private val context: Context,
    private val settingsManager: SettingsManager
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val mediaStoreManager = MediaStoreManager(context)
    val safStorageManager = SafStorageManager(context)
    val appStorageManager = AppStorageManager(context)

    private val _photos = MutableStateFlow<List<CapturedPhoto>>(emptyList())
    val photos: StateFlow<List<CapturedPhoto>> = _photos.asStateFlow()

    private val _latestPhoto = MutableStateFlow<CapturedPhoto?>(null)
    val latestPhoto: StateFlow<CapturedPhoto?> = _latestPhoto.asStateFlow()

    private val _photoCount = MutableStateFlow(0)
    val photoCount: StateFlow<Int> = _photoCount.asStateFlow()

    init {
        refreshPhotos()
    }

    fun refreshPhotos() {
        scope.launch {
            val list = loadAllPhotos()
            _photos.value = list
            _photoCount.value = list.size
            _latestPhoto.value = list.firstOrNull()
        }
    }

    private suspend fun loadAllPhotos(): List<CapturedPhoto> = withContext(Dispatchers.IO) {
        val result = mutableListOf<CapturedPhoto>()

        // Load MediaStore photos
        result.addAll(mediaStoreManager.getCamCraftPhotos())

        // Load SAF photos if SAF folder is configured
        val safUriStr = settingsManager.settings.value.safFolderUri
        if (!safUriStr.isNullOrEmpty()) {
            try {
                val treeUri = Uri.parse(safUriStr)
                result.addAll(safStorageManager.getPhotosFromSafFolder(treeUri))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Load App-private photos
        result.addAll(appStorageManager.getPhotos())

        // Deduplicate by ID and sort descending by dateTaken
        result.distinctBy { it.uri.toString() }.sortedByDescending { it.dateTaken }
    }

    suspend fun savePhoto(
        jpegBytes: ByteArray,
        location: Location? = null,
        prefix: String = "CamCraft_"
    ): Uri? = withContext(Dispatchers.IO) {
        val settings = settingsManager.settings.value
        val uri = when (settings.storageLocationType) {
            StorageLocationType.MEDIA_STORE -> {
                mediaStoreManager.saveImage(jpegBytes, location, prefix)
            }
            StorageLocationType.SAF_CUSTOM -> {
                val safUriStr = settings.safFolderUri
                if (!safUriStr.isNullOrEmpty()) {
                    val treeUri = Uri.parse(safUriStr)
                    safStorageManager.saveImageToSafFolder(treeUri, jpegBytes, location, prefix)
                        ?: mediaStoreManager.saveImage(jpegBytes, location, prefix)
                } else {
                    mediaStoreManager.saveImage(jpegBytes, location, prefix)
                }
            }
            StorageLocationType.APP_PRIVATE -> {
                appStorageManager.saveImage(jpegBytes, location, prefix)
            }
        }

        refreshPhotos()
        uri
    }

    suspend fun deletePhoto(photo: CapturedPhoto): Boolean = withContext(Dispatchers.IO) {
        val success = if (photo.isSaf) {
            safStorageManager.deleteSafPhoto(photo.uri)
        } else if (photo.absolutePath != null) {
            appStorageManager.deletePhoto(photo.uri)
        } else {
            mediaStoreManager.deletePhoto(photo.uri)
        }

        if (success) {
            refreshPhotos()
        }
        success
    }

    fun sharePhoto(photo: CapturedPhoto) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, photo.uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "Share CamCraft Photo").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
