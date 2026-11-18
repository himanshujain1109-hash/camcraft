package com.camcraft.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.camcraft.data.CapturedPhoto
import com.camcraft.metadata.ExifManager
import com.camcraft.metadata.PhotoMetadata
import com.camcraft.ui.theme.AccentElectricBlue
import com.camcraft.ui.theme.DangerRed
import com.camcraft.ui.theme.DarkBackground
import com.camcraft.ui.theme.DarkBorder
import com.camcraft.ui.theme.DarkSurface
import com.camcraft.ui.theme.TextPrimaryDark
import com.camcraft.ui.theme.TextSecondaryDark
import com.camcraft.viewmodel.GalleryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoViewerScreen(
    photoUriString: String,
    galleryViewModel: GalleryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditor: (String) -> Unit
) {
    val context = LocalContext.current
    val photoUri = remember(photoUriString) { Uri.parse(photoUriString) }
    val photos by galleryViewModel.photos.collectAsState()

    val currentPhoto = remember(photos, photoUri) {
        photos.find { it.uri.toString() == photoUriString } ?: CapturedPhoto(
            id = photoUriString,
            uri = photoUri,
            displayName = "Photo",
            dateTaken = System.currentTimeMillis(),
            size = 0
        )
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var showBars by remember { mutableStateOf(true) }

    var showExifSheet by remember { mutableStateOf(false) }
    var photoMetadata by remember { mutableStateOf<PhotoMetadata?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(photoUri) {
        photoMetadata = ExifManager.readMetadata(context, photoUri)
    }

    Scaffold(
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Full Screen Zoomable Image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1f) {
                                offsetX += pan.x
                                offsetY += pan.y
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = currentPhoto.displayName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                )
            }

            // Top Bar
            AnimatedVisibility(
                visible = showBars,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = currentPhoto.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )

                    IconButton(onClick = {
                        scope.launch {
                            photoMetadata = ExifManager.readMetadata(context, photoUri)
                            showExifSheet = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "EXIF Info",
                            tint = AccentElectricBlue
                        )
                    }
                }
            }

            // Bottom Bar
            AnimatedVisibility(
                visible = showBars,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 32.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Share
                    IconButton(onClick = { galleryViewModel.sharePhoto(currentPhoto) }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Edit
                    IconButton(onClick = { onNavigateToEditor(photoUriString) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = AccentElectricBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Delete
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = DangerRed,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // EXIF Metadata Bottom Sheet
        if (showExifSheet && photoMetadata != null) {
            ModalBottomSheet(
                onDismissRequest = { showExifSheet = false },
                sheetState = sheetState,
                containerColor = DarkSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Image Information & EXIF",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    MetadataItem(
                        icon = Icons.Default.Image,
                        title = "Resolution",
                        value = if (photoMetadata!!.width > 0) "${photoMetadata!!.width} × ${photoMetadata!!.height} px" else "Original High-Res"
                    )
                    MetadataItem(
                        icon = Icons.Default.CalendarToday,
                        title = "Captured On",
                        value = photoMetadata!!.dateTime ?: "Timestamp Available"
                    )
                    MetadataItem(
                        icon = Icons.Default.Camera,
                        title = "Camera Hardware",
                        value = "${photoMetadata!!.deviceMake ?: "CamCraft"} ${photoMetadata!!.deviceModel ?: ""}".trim()
                    )

                    if (!photoMetadata!!.iso.isNullOrEmpty()) {
                        MetadataItem(
                            icon = Icons.Default.Info,
                            title = "ISO Sensitivity",
                            value = "ISO ${photoMetadata!!.iso}"
                        )
                    }
                    if (!photoMetadata!!.aperture.isNullOrEmpty()) {
                        MetadataItem(
                            icon = Icons.Default.Info,
                            title = "Aperture",
                            value = photoMetadata!!.aperture!!
                        )
                    }
                    if (!photoMetadata!!.exposureTime.isNullOrEmpty()) {
                        MetadataItem(
                            icon = Icons.Default.Info,
                            title = "Exposure Time",
                            value = photoMetadata!!.exposureTime!!
                        )
                    }

                    if (photoMetadata!!.hasGps && photoMetadata!!.latitude != null) {
                        MetadataItem(
                            icon = Icons.Default.LocationOn,
                            title = "GPS Geotag",
                            value = String.format(java.util.Locale.US, "%.5f, %.5f", photoMetadata!!.latitude, photoMetadata!!.longitude)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed) },
                title = { Text("Delete Photo", color = TextPrimaryDark) },
                text = { Text("Are you sure you want to delete this photo?", color = TextSecondaryDark) },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            galleryViewModel.requestDeletePhoto(currentPhoto)
                            galleryViewModel.confirmDeletePhoto {
                                onNavigateBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", color = TextSecondaryDark)
                    }
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
private fun MetadataItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentElectricBlue,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryDark
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimaryDark,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
