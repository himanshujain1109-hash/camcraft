package com.camcraft.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.camcraft.data.GridMode

@Composable
fun GridOverlay(
    gridMode: GridMode,
    modifier: Modifier = Modifier
) {
    if (gridMode == GridMode.NONE) return

    val gridColor = Color.White.copy(alpha = 0.35f)
    val strokeWidth = 1.dp

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val strokePx = strokeWidth.toPx()

        when (gridMode) {
            GridMode.RULE_OF_THIRDS -> {
                // 3x3 grid (2 vertical lines, 2 horizontal lines)
                val oneThirdW = width / 3f
                val twoThirdsW = width * 2f / 3f
                val oneThirdH = height / 3f
                val twoThirdsH = height * 2f / 3f

                // Verticals
                drawLine(gridColor, Offset(oneThirdW, 0f), Offset(oneThirdW, height), strokePx)
                drawLine(gridColor, Offset(twoThirdsW, 0f), Offset(twoThirdsW, height), strokePx)

                // Horizontals
                drawLine(gridColor, Offset(0f, oneThirdH), Offset(width, oneThirdH), strokePx)
                drawLine(gridColor, Offset(0f, twoThirdsH), Offset(width, twoThirdsH), strokePx)
            }
            GridMode.GRID_4X4 -> {
                // 4x4 grid (3 vertical lines, 3 horizontal lines)
                for (i in 1..3) {
                    val x = width * (i / 4f)
                    val y = height * (i / 4f)
                    drawLine(gridColor, Offset(x, 0f), Offset(x, height), strokePx)
                    drawLine(gridColor, Offset(0f, y), Offset(width, y), strokePx)
                }
            }
            GridMode.NONE -> {}
        }
    }
}
