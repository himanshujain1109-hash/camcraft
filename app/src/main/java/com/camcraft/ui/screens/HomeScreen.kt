package com.camcraft.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.camcraft.data.CameraHardwareStatus
import com.camcraft.data.CapturedPhoto
import com.camcraft.ui.theme.AccentElectricBlue
import com.camcraft.ui.theme.AccentNeonOrange
import com.camcraft.ui.theme.DarkBackground
import com.camcraft.ui.theme.DarkBorder
import com.camcraft.ui.theme.DarkSurface
import com.camcraft.ui.theme.DarkSurfaceVariant
import com.camcraft.ui.theme.SuccessGreen
import com.camcraft.ui.theme.TextPrimaryDark
import com.camcraft.ui.theme.TextSecondaryDark
import com.camcraft.viewmodel.CameraViewModel
import com.camcraft.viewmodel.GalleryViewModel

@Composable
fun HomeScreen(
    cameraViewModel: CameraViewModel,
    galleryViewModel: GalleryViewModel,
    onNavigateToCamera: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPhotoViewer: (String) -> Unit
) {
    val hardwareStatus by cameraViewModel.hardwareStatus.collectAsState()
    val totalPhotos by galleryViewModel.photoRepository.photoCount.collectAsState()
    val latestPhoto by galleryViewModel.photoRepository.latestPhoto.collectAsState()

    LaunchedEffect(Unit) {
        galleryViewModel.refresh()
    }

    Scaffold(
        containerColor = DarkBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header / App Logo & Name
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(AccentElectricBlue, AccentNeonOrange)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "CamCraft",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "Advanced Camera Studio",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentElectricBlue,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .clickable { onNavigateToSettings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
            }

            // Welcome Hero Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(24.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, AccentElectricBlue.copy(alpha = 0.3f))))
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Text(
                            text = "Professional Camera Experience",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "High-precision live preview, MediaStore & SAF custom storage, in-app cropping, rotation, and EXIF tools.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryDark
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // Big Open Camera Button
                        Button(
                            onClick = onNavigateToCamera,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentElectricBlue,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Open Camera",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Quick Stats Row: Photo Count & Latest Thumbnail
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Total Photos Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToGallery() },
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(20.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(DarkBorder, Color.Transparent)))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "GALLERY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondaryDark,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = AccentElectricBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = totalPhotos.toString(),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = if (totalPhotos == 1) "Captured Photo" else "Captured Photos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    // Latest Photo Thumbnail Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (latestPhoto != null) {
                                    onNavigateToPhotoViewer(latestPhoto!!.uri.toString())
                                } else {
                                    onNavigateToGallery()
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(20.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(DarkBorder, Color.Transparent)))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(125.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (latestPhoto != null) {
                                AsyncImage(
                                    model = latestPhoto!!.uri,
                                    contentDescription = "Latest Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                            )
                                        )
                                )
                                Text(
                                    text = "Latest Capture",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(12.dp)
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        tint = TextSecondaryDark,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "No Photos Yet",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondaryDark
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Camera Hardware Status Section
            item {
                Text(
                    text = "HARDWARE AVAILABILITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryDark,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(20.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(DarkBorder, Color.Transparent)))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        HardwareStatusRow(
                            icon = Icons.Default.CameraAlt,
                            title = "Rear Camera",
                            status = if (hardwareStatus.hasBackCamera) "Ready & Available" else "Not Detected",
                            isOk = hardwareStatus.hasBackCamera
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        HardwareStatusRow(
                            icon = Icons.Default.Videocam,
                            title = "Front Selfie Camera",
                            status = if (hardwareStatus.hasFrontCamera) "Ready & Available" else "Not Detected",
                            isOk = hardwareStatus.hasFrontCamera
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        HardwareStatusRow(
                            icon = Icons.Default.FlashOn,
                            title = "LED Flash Unit",
                            status = if (hardwareStatus.hasFlash) "Supported" else "Unavailable",
                            isOk = hardwareStatus.hasFlash
                        )
                    }
                }
            }

            // Action Buttons Section: Saved Photos & Settings
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToGallery,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = Brush.horizontalGradient(listOf(DarkBorder, DarkBorder)))
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = AccentElectricBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saved Photos", fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = Brush.horizontalGradient(listOf(DarkBorder, DarkBorder)))
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = AccentNeonOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Settings", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun HardwareStatusRow(
    icon: ImageVector,
    title: String,
    status: String,
    isOk: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isOk) AccentElectricBlue else TextSecondaryDark,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimaryDark,
                fontWeight = FontWeight.Medium
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Info,
                contentDescription = null,
                tint = if (isOk) SuccessGreen else TextSecondaryDark,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                color = if (isOk) SuccessGreen else TextSecondaryDark,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
