package com.camcraft.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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

@Composable
fun TimerOverlay(
    secondsRemaining: Int,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (secondsRemaining <= 0) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedContent(
                targetState = secondsRemaining,
                transitionSpec = {
                    (scaleIn(initialScale = 1.6f, animationSpec = tween(300)) + fadeIn()) togetherWith
                            (scaleOut(targetScale = 0.4f, animationSpec = tween(300)) + fadeOut())
                },
                label = "TimerCountdown"
            ) { count ->
                Text(
                    text = count.toString(),
                    fontSize = 110.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentElectricBlue
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            FilledTonalButton(
                onClick = onCancel,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cancel Timer")
                Text(
                    text = " Cancel",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
