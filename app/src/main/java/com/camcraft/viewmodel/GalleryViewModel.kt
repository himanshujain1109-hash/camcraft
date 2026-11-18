package com.camcraft.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.camcraft.CamCraftApplication
import com.camcraft.data.CapturedPhoto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class GallerySortOrder(val label: String) {
    NEWEST_FIRST("Newest First"),
    OLDEST_FIRST("Oldest First")
}

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CamCraftApplication
    val photoRepository = app.photoRepository

    private val _sortOrder = MutableStateFlow(GallerySortOrder.NEWEST_FIRST)
    val sortOrder: StateFlow<GallerySortOrder> = _sortOrder

    private val _selectedPhotoForDeletion = MutableStateFlow<CapturedPhoto?>(null)
    val selectedPhotoForDeletion: StateFlow<CapturedPhoto?> = _selectedPhotoForDeletion

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting

    val photos: StateFlow<List<CapturedPhoto>> = combine(
        photoRepository.photos,
        _sortOrder
    ) { photoList, sort ->
        when (sort) {
            GallerySortOrder.NEWEST_FIRST -> photoList.sortedByDescending { it.dateTaken }
            GallerySortOrder.OLDEST_FIRST -> photoList.sortedBy { it.dateTaken }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun toggleSortOrder() {
        _sortOrder.value = if (_sortOrder.value == GallerySortOrder.NEWEST_FIRST) {
            GallerySortOrder.OLDEST_FIRST
        } else {
            GallerySortOrder.NEWEST_FIRST
        }
    }

    fun requestDeletePhoto(photo: CapturedPhoto) {
        _selectedPhotoForDeletion.value = photo
    }

    fun cancelDeletePhoto() {
        _selectedPhotoForDeletion.value = null
    }

    fun confirmDeletePhoto(onDeleted: () -> Unit = {}) {
        val photo = _selectedPhotoForDeletion.value ?: return
        viewModelScope.launch {
            _isDeleting.value = true
            val success = photoRepository.deletePhoto(photo)
            _isDeleting.value = false
            _selectedPhotoForDeletion.value = null
            if (success) {
                onDeleted()
            }
        }
    }

    fun sharePhoto(photo: CapturedPhoto) {
        photoRepository.sharePhoto(photo)
    }

    fun refresh() {
        photoRepository.refreshPhotos()
    }
}
