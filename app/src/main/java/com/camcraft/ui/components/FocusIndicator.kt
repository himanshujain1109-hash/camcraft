package com.camcraft.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.camcraft.ui.theme.AccentElectricBlue
import kotlin.math.roundToInt

@Composable
fun FocusIndicator(
    focusPoint: Offset?,
    onAnimationEnd: () -> Unit
) {
    if (focusPoint == null) return

    val scale = remember(focusPoint) { Animatable(1.5f) }
    val alpha = remember(focusPoint) { Animatable(1.0f) }

    LaunchedEffect(focusPoint) {
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
        )
        // Wait 1 second then fade out
        kotlinx.coroutines.delay(800)
        alpha.animateTo(
            targetValue = 0.0f,
            animationSpec = tween(durationMillis = 300)
        )
        onAnimationEnd()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val sizeDp = 72.dp
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (focusPoint.x - (sizeDp.toPx() / 2)).roundToInt(),
                        (focusPoint.y - (sizeDp.toPx() / 2)).roundToInt()
                    )
                }
                .size(sizeDp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val currentScale = scale.value
                val currentAlpha = alpha.value
                val strokeWidth = 2.dp.toPx()
                val radius = (size.minDimension / 2) * currentScale

                // Focus Ring
                drawCircle(
                    color = AccentElectricBlue.copy(alpha = currentAlpha),
                    radius = radius,
                    style = Stroke(width = strokeWidth)
                )

                // Center crosshair / dot
                drawCircle(
                    color = Color.White.copy(alpha = currentAlpha),
                    radius = 3.dp.toPx()
                )
            }
        }
    }
}
