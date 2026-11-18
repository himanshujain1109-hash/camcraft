package com.camcraft.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.camcraft.editor.CropAspectRatio
import com.camcraft.ui.components.CropImageView
import com.camcraft.ui.theme.AccentElectricBlue
import com.camcraft.ui.theme.AccentNeonOrange
import com.camcraft.ui.theme.DarkBackground
import com.camcraft.ui.theme.DarkBorder
import com.camcraft.ui.theme.DarkSurface
import com.camcraft.ui.theme.DarkSurfaceVariant
import com.camcraft.ui.theme.TextPrimaryDark
import com.camcraft.ui.theme.TextSecondaryDark
import com.camcraft.viewmodel.EditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditorScreen(
    photoUriString: String,
    editorViewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
    onSaveSuccess: (String) -> Unit
) {
    val photoUri = remember(photoUriString) { Uri.parse(photoUriString) }
    val state by editorViewModel.state.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Crop, 1: Rotate, 2: Adjust

    LaunchedEffect(photoUri) {
        editorViewModel.loadImage(photoUri)
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Edit Photo",
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
                    Button(
                        onClick = {
                            editorViewModel.saveEditedImage { newUri ->
                                onSaveSuccess(newUri.toString())
                            }
                        },
                        enabled = !state.isSaving && state.originalBitmap != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentElectricBlue,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
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
            // Main Canvas Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = AccentElectricBlue)
                } else if (state.originalBitmap != null) {
                    CropImageView(
                        bitmap = state.previewBitmap ?: state.originalBitmap!!,
                        cropRect = state.cropRect,
                        onCropRectChanged = { editorViewModel.updateCropRect(it) },
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage!!,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Bottom Tool Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(vertical = 12.dp)
            ) {
                // Tab Selection (Crop, Rotate, Adjust)
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = DarkSurface,
                    contentColor = AccentElectricBlue,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                            color = AccentElectricBlue
                        )
                    }
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        icon = { Icon(Icons.Default.Crop, contentDescription = null) },
                        text = { Text("Crop") }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        icon = { Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null) },
                        text = { Text("Rotate") }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        icon = { Icon(Icons.Default.Tune, contentDescription = null) },
                        text = { Text("Adjust") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sub-panel per tab
                when (activeTab) {
                    0 -> {
                        // Crop Aspect Ratios
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CropAspectRatio.entries.forEach { ratio ->
                                FilterChip(
                                    selected = state.selectedRatio == ratio,
                                    onClick = { editorViewModel.selectAspectRatio(ratio) },
                                    label = { Text(ratio.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentElectricBlue.copy(alpha = 0.2f),
                                        selectedLabelColor = AccentElectricBlue,
                                        labelColor = TextSecondaryDark
                                    )
                                )
                            }
                        }
                    }
                    1 -> {
                        // Rotate Actions
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { editorViewModel.rotate90() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkSurfaceVariant,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Rotate 90° (${state.rotationAngle}°)")
                            }

                            Button(
                                onClick = { editorViewModel.resetEdits() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkSurfaceVariant,
                                    contentColor = AccentNeonOrange
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reset")
                            }
                        }
                    }
                    2 -> {
                        // Color Adjustments (Brightness, Contrast, Saturation)
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            // Brightness
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Brightness", style = MaterialTheme.typography.labelSmall, color = TextSecondaryDark)
                                Text(String.format(java.util.Locale.US, "%.1f", state.brightness), style = MaterialTheme.typography.labelSmall, color = TextPrimaryDark)
                            }
                            Slider(
                                value = state.brightness,
                                onValueChange = {
                                    editorViewModel.updateAdjustments(it, state.contrast, state.saturation)
                                },
                                valueRange = -0.5f..0.5f,
                                colors = SliderDefaults.colors(
                                    thumbColor = AccentElectricBlue,
                                    activeTrackColor = AccentElectricBlue
                                )
                            )

                            // Contrast
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Contrast", style = MaterialTheme.typography.labelSmall, color = TextSecondaryDark)
                                Text(String.format(java.util.Locale.US, "%.1f", state.contrast), style = MaterialTheme.typography.labelSmall, color = TextPrimaryDark)
                            }
                            Slider(
                                value = state.contrast,
                                onValueChange = {
                                    editorViewModel.updateAdjustments(state.brightness, it, state.saturation)
                                },
                                valueRange = 0.5f..1.5f,
                                colors = SliderDefaults.colors(
                                    thumbColor = AccentElectricBlue,
                                    activeTrackColor = AccentElectricBlue
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
