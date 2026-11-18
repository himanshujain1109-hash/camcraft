package com.camcraft.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.camcraft.CamCraftApplication
import com.camcraft.editor.CropAspectRatio
import com.camcraft.editor.EditorState
import com.camcraft.editor.ImageEditor
import com.camcraft.utils.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CamCraftApplication
    private val imageEditor = ImageEditor(application, app.photoRepository)

    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    private var previewJob: Job? = null

    fun loadImage(uri: Uri) {
        if (_state.value.originalUri == uri && _state.value.originalBitmap != null) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, originalUri = uri, errorMessage = null)
            val bitmap = BitmapUtils.decodeSampledBitmapFromUri(
                context = getApplication(),
                uri = uri,
                reqWidth = 2048,
                reqHeight = 2048
            )

            if (bitmap != null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    originalBitmap = bitmap,
                    previewBitmap = bitmap,
                    rotationAngle = 0,
                    cropRect = RectF(0f, 0f, 1f, 1f),
                    selectedRatio = CropAspectRatio.FREE,
                    brightness = 0f,
                    contrast = 1f,
                    saturation = 1f
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load image for editing"
                )
            }
        }
    }

    fun rotate90() {
        val nextAngle = (_state.value.rotationAngle + 90) % 360
        _state.value = _state.value.copy(
            rotationAngle = nextAngle,
            cropRect = RectF(0f, 0f, 1f, 1f) // reset crop when rotating
        )
        updatePreview()
    }

    fun resetEdits() {
        _state.value = _state.value.copy(
            rotationAngle = 0,
            selectedRatio = CropAspectRatio.FREE,
            cropRect = RectF(0f, 0f, 1f, 1f),
            brightness = 0f,
            contrast = 1f,
            saturation = 1f
        )
        updatePreview()
    }

    fun selectAspectRatio(ratio: CropAspectRatio) {
        val currentRect = _state.value.cropRect
        val bitmap = _state.value.originalBitmap ?: return
        val currentWidth = if (_state.value.rotationAngle % 180 == 0) bitmap.width else bitmap.height
        val currentHeight = if (_state.value.rotationAngle % 180 == 0) bitmap.height else bitmap.width

        val targetRatio = ratio.ratio
        val newRect = if (targetRatio != null) {
            val imgAspect = currentWidth.toFloat() / currentHeight.toFloat()
            if (targetRatio > imgAspect) {
                // Wider than image: clamp height
                val h = (1f / targetRatio) * imgAspect
                val top = (1f - h) / 2f
                RectF(0f, top, 1f, top + h)
            } else {
                // Taller than image: clamp width
                val w = targetRatio / imgAspect
                val left = (1f - w) / 2f
                RectF(left, 0f, left + w, 1f)
            }
        } else {
            RectF(0f, 0f, 1f, 1f)
        }

        _state.value = _state.value.copy(
            selectedRatio = ratio,
            cropRect = newRect
        )
    }

    fun updateCropRect(rect: RectF) {
        _state.value = _state.value.copy(cropRect = rect)
    }

    fun updateAdjustments(brightness: Float, contrast: Float, saturation: Float) {
        _state.value = _state.value.copy(
            brightness = brightness,
            contrast = contrast,
            saturation = saturation
        )
        updatePreview()
    }

    private fun updatePreview() {
        val orig = _state.value.originalBitmap ?: return
        previewJob?.cancel()
        previewJob = viewModelScope.launch(Dispatchers.Default) {
            // 1. Rotate
            var bmp = if (_state.value.rotationAngle % 360 != 0) {
                BitmapUtils.rotateBitmap(orig, _state.value.rotationAngle)
            } else {
                orig
            }

            // 2. Color adjustments
            val hasColorChanges = _state.value.brightness != 0f ||
                                  _state.value.contrast != 1f ||
                                  _state.value.saturation != 1f
            if (hasColorChanges) {
                val adjusted = BitmapUtils.applyColorAdjustments(
                    bmp,
                    _state.value.brightness,
                    _state.value.contrast,
                    _state.value.saturation
                )
                if (bmp != orig) {
                    bmp.recycle()
                }
                bmp = adjusted
            }

            val oldPreview = _state.value.previewBitmap
            _state.value = _state.value.copy(previewBitmap = bmp)

            if (oldPreview != null && oldPreview != orig && oldPreview != bmp) {
                oldPreview.recycle()
            }
        }
    }

    fun saveEditedImage(onSuccess: (Uri) -> Unit) {
        val bitmap = _state.value.originalBitmap ?: return
        if (_state.value.isSaving) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, errorMessage = null)
            val uri = imageEditor.saveEditedImage(
                originalBitmap = bitmap,
                cropRect = _state.value.cropRect,
                rotationAngle = _state.value.rotationAngle,
                brightness = _state.value.brightness,
                contrast = _state.value.contrast,
                saturation = _state.value.saturation
            )

            if (uri != null) {
                _state.value = _state.value.copy(isSaving = false, saveSuccessUri = uri)
                onSuccess(uri)
            } else {
                _state.value = _state.value.copy(
                    isSaving = false,
                    errorMessage = "Failed to save edited photo"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        previewJob?.cancel()
        val orig = _state.value.originalBitmap
        val prev = _state.value.previewBitmap
        if (prev != null && prev != orig) {
            prev.recycle()
        }
        orig?.recycle()
    }
}
