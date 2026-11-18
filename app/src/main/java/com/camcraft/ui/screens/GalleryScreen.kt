package com.camcraft.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.camcraft.data.CapturedPhoto
import com.camcraft.ui.theme.AccentElectricBlue
import com.camcraft.ui.theme.DangerRed
import com.camcraft.ui.theme.DarkBackground
import com.camcraft.ui.theme.DarkBorder
import com.camcraft.ui.theme.DarkSurface
import com.camcraft.ui.theme.TextPrimaryDark
import com.camcraft.ui.theme.TextSecondaryDark
import com.camcraft.viewmodel.GallerySortOrder
import com.camcraft.viewmodel.GalleryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    galleryViewModel: GalleryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToPhotoViewer: (String) -> Unit
) {
    val photos by galleryViewModel.photos.collectAsState()
    val sortOrder by galleryViewModel.sortOrder.collectAsState()
    val photoToDelete by galleryViewModel.selectedPhotoForDeletion.collectAsState()

    LaunchedEffect(Unit) {
        galleryViewModel.refresh()
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Saved Photos (${photos.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { galleryViewModel.toggleSortOrder() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort",
                            tint = AccentElectricBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Sort Filter Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sort: ${sortOrder.label}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryDark,
                    fontWeight = FontWeight.Medium
                )
                FilterChip(
                    selected = sortOrder == GallerySortOrder.NEWEST_FIRST,
                    onClick = { galleryViewModel.toggleSortOrder() },
                    label = { Text(if (sortOrder == GallerySortOrder.NEWEST_FIRST) "Newest" else "Oldest") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentElectricBlue.copy(alpha = 0.2f),
                        selectedLabelColor = AccentElectricBlue
                    )
                )
            }

            if (photos.isEmpty()) {
                // Empty State UI
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(DarkSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = AccentElectricBlue,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "No Photos Yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Capture your first photo using CamCraft's high quality camera.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryDark,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onNavigateToCamera,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentElectricBlue,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Take Photo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Photo Grid (3 columns)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(photos, key = { it.uri.toString() }) { photo ->
                        GalleryItem(
                            photo = photo,
                            onClick = { onNavigateToPhotoViewer(photo.uri.toString()) }
                        )
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (photoToDelete != null) {
            AlertDialog(
                onDismissRequest = { galleryViewModel.cancelDeletePhoto() },
                icon = {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed)
                },
                title = {
                    Text(
                        text = "Delete Photo?",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimaryDark
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to permanently delete \"${photoToDelete!!.displayName}\"?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryDark
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { galleryViewModel.confirmDeletePhoto() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DangerRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { galleryViewModel.cancelDeletePhoto() },
                        shape = RoundedCornerShape(10.dp)
                    ) {
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
private fun GalleryItem(
    photo: CapturedPhoto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = photo.uri,
                contentDescription = photo.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Date Badge on bottom
            val dateStr = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(photo.dateTaken))
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text(
                    text = dateStr,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
