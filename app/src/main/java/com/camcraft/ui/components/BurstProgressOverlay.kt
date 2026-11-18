package com.camcraft.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.camcraft.ui.theme.AccentElectricBlue
import com.camcraft.ui.theme.DarkSurface

@Composable
fun BurstProgressOverlay(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    if (total <= 0) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(DarkSurface.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                .padding(horizontal = 32.dp, vertical = 24.dp)
        ) {
            Text(
                text = "BURST CAPTURE",
                style = MaterialTheme.typography.labelSmall,
                color = AccentElectricBlue,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "$current / $total",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            val progress = current.toFloat() / total.toFloat()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .width(160.dp)
                    .height(6.dp),
                color = AccentElectricBlue,
                trackColor = Color.White.copy(alpha = 0.2f),
            )
        }
    }
}
