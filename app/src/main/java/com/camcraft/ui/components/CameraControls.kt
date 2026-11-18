package com.camcraft.ui.components

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.Grid4x4
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Timer10
import androidx.compose.material.icons.filled.Timer3
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.camcraft.camera.CameraState
import com.camcraft.data.CameraFacing
import com.camcraft.data.FlashMode
import com.camcraft.data.GridMode
import com.camcraft.data.TimerDuration
import com.camcraft.ui.theme.AccentElectricBlue
import com.camcraft.ui.theme.AccentNeonOrange
import com.camcraft.ui.theme.DarkSurface

@Composable
fun CameraTopBar(
    cameraState: CameraState,
    onFlashClick: () -> Unit,
    onTimerClick: () -> Unit,
    onGridClick: () -> Unit,
    onRatioClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flash Mode
            IconButton(onClick = onFlashClick) {
                val icon = when (cameraState.flashMode) {
                    FlashMode.AUTO -> Icons.Default.FlashAuto
                    FlashMode.ON -> Icons.Default.FlashOn
                    FlashMode.OFF -> Icons.Default.FlashOff
                }
                val tint = if (cameraState.flashMode != FlashMode.OFF) AccentNeonOrange else Color.White
                Icon(icon, contentDescription = "Flash Mode", tint = tint)
            }

            // Timer Duration
            IconButton(onClick = onTimerClick) {
                val icon = when (cameraState.timerDuration) {
                    TimerDuration.OFF -> Icons.Default.TimerOff
                    TimerDuration.SEC_3 -> Icons.Default.Timer3
                    TimerDuration.SEC_5 -> Icons.Default.Timer
                    TimerDuration.SEC_10 -> Icons.Default.Timer10
                }
                val tint = if (cameraState.timerDuration != TimerDuration.OFF) AccentElectricBlue else Color.White
                Icon(icon, contentDescription = "Timer", tint = tint)
            }

            // Grid Mode
            IconButton(onClick = onGridClick) {
                val icon = when (cameraState.gridMode) {
                    GridMode.NONE -> Icons.Default.GridOff
                    GridMode.RULE_OF_THIRDS -> Icons.Default.Grid3x3
                    GridMode.GRID_4X4 -> Icons.Default.Grid4x4
                }
                val tint = if (cameraState.gridMode != GridMode.NONE) AccentElectricBlue else Color.White
                Icon(icon, contentDescription = "Grid Mode", tint = tint)
            }

            // Aspect Ratio Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable { onRatioClick() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = cameraState.aspectRatio.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Settings Button
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
            }
        }
    }
}

@Composable
fun CameraBottomBar(
    cameraState: CameraState,
    latestPhotoUri: Uri?,
    onShutterClick: () -> Unit,
    onBurstClick: () -> Unit,
    onSwitchCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(durationMillis = 350),
        label = "CameraFlip"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Latest Photo Thumbnail / Gallery Shortcut
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .clickable { onGalleryClick() },
                contentAlignment = Alignment.Center
            ) {
                if (latestPhotoUri != null) {
                    AsyncImage(
                        model = latestPhotoUri,
                        contentDescription = "Latest Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(56.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Center: Shutter Button & Burst Mode
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Main Shutter Button
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.88f else 1.0f,
                    animationSpec = tween(durationMillis = 100),
                    label = "ShutterScale"
                )

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(6.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(if (cameraState.isCapturing) AccentElectricBlue else Color.White)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            enabled = !cameraState.isCapturing && !cameraState.isBursting
                        ) {
                            onShutterClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (cameraState.isCapturing) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Quick Burst Capture Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(AccentNeonOrange.copy(alpha = 0.2f))
                        .border(1.dp, AccentNeonOrange, RoundedCornerShape(16.dp))
                        .clickable(enabled = !cameraState.isCapturing && !cameraState.isBursting) {
                            onBurstClick()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "BURST",
                        color = AccentNeonOrange,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Camera Switch Button (Front / Rear)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable(enabled = !cameraState.isCapturing && !cameraState.isBursting) {
                        rotationAngle += 180f
                        onSwitchCameraClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Switch Camera",
                    tint = Color.White,
                    modifier = Modifier
                        .size(30.dp)
                        .rotate(animatedRotation)
                )
            }
        }
    }
}
