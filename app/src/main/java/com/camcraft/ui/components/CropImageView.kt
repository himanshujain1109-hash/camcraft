package com.camcraft.ui.components

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.camcraft.ui.theme.AccentElectricBlue
import kotlin.math.min

@Composable
fun CropImageView(
    bitmap: Bitmap,
    cropRect: RectF, // normalized [0..1]
    onCropRectChanged: (RectF) -> Unit,
    modifier: Modifier = Modifier
) {
    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
    var activeHandle by remember { mutableStateOf<DragHandle?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(bitmap, cropRect) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val canvasW = size.width.toFloat()
                            val canvasH = size.height.toFloat()

                            val scale = min(canvasW / bitmap.width, canvasH / bitmap.height)
                            val displayedW = bitmap.width * scale
                            val displayedH = bitmap.height * scale
                            val leftOffset = (canvasW - displayedW) / 2f
                            val topOffset = (canvasH - displayedH) / 2f

                            val cropLeft = leftOffset + cropRect.left * displayedW
                            val cropTop = topOffset + cropRect.top * displayedH
                            val cropRight = leftOffset + cropRect.right * displayedW
                            val cropBottom = topOffset + cropRect.bottom * displayedH

                            val touchThreshold = 48.dp.toPx()

                            activeHandle = when {
                                (offset - Offset(cropLeft, cropTop)).getDistance() < touchThreshold -> DragHandle.TOP_LEFT
                                (offset - Offset(cropRight, cropTop)).getDistance() < touchThreshold -> DragHandle.TOP_RIGHT
                                (offset - Offset(cropLeft, cropBottom)).getDistance() < touchThreshold -> DragHandle.BOTTOM_LEFT
                                (offset - Offset(cropRight, cropBottom)).getDistance() < touchThreshold -> DragHandle.BOTTOM_RIGHT
                                offset.x in cropLeft..cropRight && offset.y in cropTop..cropBottom -> DragHandle.CENTER
                                else -> null
                            }
                        },
                        onDragEnd = { activeHandle = null },
                        onDragCancel = { activeHandle = null },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val handle = activeHandle ?: return@detectDragGestures

                            val canvasW = size.width.toFloat()
                            val canvasH = size.height.toFloat()
                            val scale = min(canvasW / bitmap.width, canvasH / bitmap.height)
                            val displayedW = bitmap.width * scale
                            val displayedH = bitmap.height * scale

                            val dx = dragAmount.x / displayedW
                            val dy = dragAmount.y / displayedH

                            val current = RectF(cropRect)

                            when (handle) {
                                DragHandle.TOP_LEFT -> {
                                    current.left = (current.left + dx).coerceIn(0f, current.right - 0.1f)
                                    current.top = (current.top + dy).coerceIn(0f, current.bottom - 0.1f)
                                }
                                DragHandle.TOP_RIGHT -> {
                                    current.right = (current.right + dx).coerceIn(current.left + 0.1f, 1f)
                                    current.top = (current.top + dy).coerceIn(0f, current.bottom - 0.1f)
                                }
                                DragHandle.BOTTOM_LEFT -> {
                                    current.left = (current.left + dx).coerceIn(0f, current.right - 0.1f)
                                    current.bottom = (current.bottom + dy).coerceIn(current.top + 0.1f, 1f)
                                }
                                DragHandle.BOTTOM_RIGHT -> {
                                    current.right = (current.right + dx).coerceIn(current.left + 0.1f, 1f)
                                    current.bottom = (current.bottom + dy).coerceIn(current.top + 0.1f, 1f)
                                }
                                DragHandle.CENTER -> {
                                    val width = current.width()
                                    val height = current.height()
                                    var newLeft = current.left + dx
                                    var newTop = current.top + dy

                                    if (newLeft < 0f) newLeft = 0f
                                    if (newLeft + width > 1f) newLeft = 1f - width
                                    if (newTop < 0f) newTop = 0f
                                    if (newTop + height > 1f) newTop = 1f - height

                                    current.set(newLeft, newTop, newLeft + width, newTop + height)
                                }
                            }
                            onCropRectChanged(current)
                        }
                    )
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height

            val scale = min(canvasW / bitmap.width, canvasH / bitmap.height)
            val displayedW = bitmap.width * scale
            val displayedH = bitmap.height * scale
            val leftOffset = (canvasW - displayedW) / 2f
            val topOffset = (canvasH - displayedH) / 2f

            // 1. Draw Image centered
            drawImage(
                image = imageBitmap,
                dstOffset = IntOffset(leftOffset.toInt(), topOffset.toInt()),
                dstSize = IntSize(displayedW.toInt(), displayedH.toInt())
            )

            val cropLeft = leftOffset + cropRect.left * displayedW
            val cropTop = topOffset + cropRect.top * displayedH
            val cropRight = leftOffset + cropRect.right * displayedW
            val cropBottom = topOffset + cropRect.bottom * displayedH
            val cropW = cropRight - cropLeft
            val cropH = cropBottom - cropTop

            val dimColor = Color.Black.copy(alpha = 0.6f)

            // Dim outside regions
            // Top dim
            drawRect(dimColor, Offset(0f, 0f), Size(canvasW, cropTop))
            // Bottom dim
            drawRect(dimColor, Offset(0f, cropBottom), Size(canvasW, canvasH - cropBottom))
            // Left dim
            drawRect(dimColor, Offset(0f, cropTop), Size(cropLeft, cropH))
            // Right dim
            drawRect(dimColor, Offset(cropRight, cropTop), Size(canvasW - cropRight, cropH))

            // Crop border
            val strokePx = 2.dp.toPx()
            drawRect(
                color = AccentElectricBlue,
                topLeft = Offset(cropLeft, cropTop),
                size = Size(cropW, cropH),
                style = Stroke(width = strokePx)
            )

            // Crop grid lines (Rule of thirds)
            val gridColor = Color.White.copy(alpha = 0.35f)
            val gridStroke = 1.dp.toPx()
            drawLine(gridColor, Offset(cropLeft + cropW / 3f, cropTop), Offset(cropLeft + cropW / 3f, cropBottom), gridStroke)
            drawLine(gridColor, Offset(cropLeft + cropW * 2f / 3f, cropTop), Offset(cropLeft + cropW * 2f / 3f, cropBottom), gridStroke)
            drawLine(gridColor, Offset(cropLeft, cropTop + cropH / 3f), Offset(cropRight, cropTop + cropH / 3f), gridStroke)
            drawLine(gridColor, Offset(cropLeft, cropTop + cropH * 2f / 3f), Offset(cropRight, cropTop + cropH * 2f / 3f), gridStroke)

            // Corner handles
            val handleLen = 18.dp.toPx()
            val handleThickness = 4.dp.toPx()
            val handleColor = AccentElectricBlue

            // Top Left
            drawLine(handleColor, Offset(cropLeft - 2f, cropTop), Offset(cropLeft + handleLen, cropTop), handleThickness)
            drawLine(handleColor, Offset(cropLeft, cropTop - 2f), Offset(cropLeft, cropTop + handleLen), handleThickness)

            // Top Right
            drawLine(handleColor, Offset(cropRight + 2f, cropTop), Offset(cropRight - handleLen, cropTop), handleThickness)
            drawLine(handleColor, Offset(cropRight, cropTop - 2f), Offset(cropRight, cropTop + handleLen), handleThickness)

            // Bottom Left
            drawLine(handleColor, Offset(cropLeft - 2f, cropBottom), Offset(cropLeft + handleLen, cropBottom), handleThickness)
            drawLine(handleColor, Offset(cropLeft, cropBottom + 2f), Offset(cropLeft, cropBottom - handleLen), handleThickness)

            // Bottom Right
            drawLine(handleColor, Offset(cropRight + 2f, cropBottom), Offset(cropRight - handleLen, cropBottom), handleThickness)
            drawLine(handleColor, Offset(cropRight, cropBottom + 2f), Offset(cropRight, cropBottom - handleLen), handleThickness)
        }
    }
}

private enum class DragHandle {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    CENTER
}
