package com.camcraft.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.camcraft.data.AspectRatioOption
import com.camcraft.data.CameraFacing
import com.camcraft.data.FlashMode
import com.camcraft.utils.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CameraManager(private val context: Context) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var preview: Preview? = null

    private val mainExecutor: Executor = ContextCompat.getMainExecutor(context)

    suspend fun getCameraProvider(): ProcessCameraProvider = suspendCancellableCoroutine { continuation ->
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            try {
                val provider = future.get()
                cameraProvider = provider
                continuation.resume(provider)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }, mainExecutor)
    }

    fun isFacingAvailable(facing: CameraFacing): Boolean {
        val provider = cameraProvider ?: return false
        val selector = if (facing == CameraFacing.BACK) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
        return try {
            provider.hasCamera(selector)
        } catch (_: Exception) {
            false
        }
    }

    suspend fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        facing: CameraFacing,
        aspectRatioOption: AspectRatioOption,
        flashMode: FlashMode
    ): Camera = withContext(Dispatchers.Main) {
        val provider = cameraProvider ?: getCameraProvider()
        provider.unbindAll()

        val selector = if (facing == CameraFacing.FRONT) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        val targetRatio = when (aspectRatioOption) {
            AspectRatioOption.RATIO_16_9 -> AspectRatio.RATIO_16_9
            else -> AspectRatio.RATIO_4_3
        }

        preview = Preview.Builder()
            .setTargetAspectRatio(targetRatio)
            .build()
            .also {
                it.surfaceProvider = previewView.surfaceProvider
            }

        val captureFlashMode = when (flashMode) {
            FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
            FlashMode.ON -> ImageCapture.FLASH_MODE_ON
            FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
        }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(targetRatio)
            .setFlashMode(captureFlashMode)
            .build()

        val newCamera = provider.bindToLifecycle(
            lifecycleOwner,
            selector,
            preview,
            imageCapture
        )
        camera = newCamera
        newCamera
    }

    fun updateFlashMode(flashMode: FlashMode) {
        val capture = imageCapture ?: return
        val mode = when (flashMode) {
            FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
            FlashMode.ON -> ImageCapture.FLASH_MODE_ON
            FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
        }
        try {
            capture.flashMode = mode
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setZoomRatio(ratio: Float) {
        camera?.cameraControl?.setZoomRatio(ratio)
    }

    fun setExposure(index: Int) {
        camera?.cameraControl?.setExposureCompensationIndex(index)
    }

    fun startFocusAndMetering(meteringPoint: MeteringPoint) {
        val action = FocusMeteringAction.Builder(meteringPoint, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
            .setAutoCancelDuration(3, TimeUnit.SECONDS)
            .build()
        camera?.cameraControl?.startFocusAndMetering(action)
    }

    fun getCameraControl(): CameraControl? = camera?.cameraControl
    fun getCameraInfo(): CameraInfo? = camera?.cameraInfo

    suspend fun capturePhoto(isFrontCamera: Boolean): ByteArray = suspendCancellableCoroutine { continuation ->
        val capture = imageCapture
        if (capture == null) {
            continuation.resumeWithException(IllegalStateException("ImageCapture not bound"))
            return@suspendCancellableCoroutine
        }

        capture.takePicture(
            mainExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    try {
                        val bytes = processImageProxyToJpeg(imageProxy, isFrontCamera)
                        continuation.resume(bytes)
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    } finally {
                        imageProxy.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    continuation.resumeWithException(exception)
                }
            }
        )
    }

    private fun processImageProxyToJpeg(imageProxy: ImageProxy, isFrontCamera: Boolean): ByteArray {
        val plane = imageProxy.planes[0]
        val buffer: ByteBuffer = plane.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val rotation = imageProxy.imageInfo.rotationDegrees

        if (rotation == 0 && !isFrontCamera) {
            return bytes
        }

        // Decode, apply rotation and front mirror if needed
        val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: return bytes

        val matrix = Matrix().apply {
            if (rotation != 0) {
                postRotate(rotation.toFloat())
            }
            if (isFrontCamera) {
                postScale(-1f, 1f)
            }
        }

        val transformedBitmap = Bitmap.createBitmap(
            originalBitmap,
            0,
            0,
            originalBitmap.width,
            originalBitmap.height,
            matrix,
            true
        )

        val output = ByteArrayOutputStream()
        transformedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, output)
        if (transformedBitmap != originalBitmap) {
            originalBitmap.recycle()
        }
        transformedBitmap.recycle()
        return output.toByteArray()
    }
}
