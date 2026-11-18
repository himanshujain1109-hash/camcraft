package com.camcraft.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.camcraft.data.AppTheme
import com.camcraft.data.AspectRatioOption
import com.camcraft.data.CameraFacing
import com.camcraft.data.FlashMode
import com.camcraft.data.GridMode
import com.camcraft.data.StorageLocationType
import com.camcraft.data.TimerDuration
import com.camcraft.permissions.PermissionManager
import com.camcraft.ui.theme.AccentElectricBlue
import com.camcraft.ui.theme.AccentNeonOrange
import com.camcraft.ui.theme.DarkBackground
import com.camcraft.ui.theme.DarkBorder
import com.camcraft.ui.theme.DarkSurface
import com.camcraft.ui.theme.DarkSurfaceVariant
import com.camcraft.ui.theme.TextPrimaryDark
import com.camcraft.ui.theme.TextSecondaryDark
import com.camcraft.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by settingsViewModel.settings.collectAsState()

    // SAF Document Tree Folder Picker Launcher
    val safPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        if (treeUri != null) {
            val displayName = treeUri.lastPathSegment ?: "Custom Folder"
            settingsViewModel.setSafFolder(treeUri, displayName)
        }
    }

    // Geotagging Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        settingsViewModel.setGeotagging(isGranted)
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: Storage Location
            item {
                Text(
                    text = "PHOTO STORAGE & DESTINATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentElectricBlue,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        StorageLocationType.entries.forEach { locationType ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { settingsViewModel.setStorageLocation(locationType) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.storageLocationType == locationType,
                                    onClick = { settingsViewModel.setStorageLocation(locationType) },
                                    colors = RadioButtonDefaults.colors(selectedColor = AccentElectricBlue)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = locationType.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextPrimaryDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = locationType.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondaryDark
                                    )
                                }
                            }
                        }

                        // SAF Folder Picker & Reset Actions
                        if (settings.storageLocationType == StorageLocationType.SAF_CUSTOM) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Current SAF Directory:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondaryDark
                                    )
                                    Text(
                                        text = settings.safFolderDisplayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimaryDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Button(
                                            onClick = { safPickerLauncher.launch(null) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = AccentElectricBlue,
                                                contentColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Choose Folder")
                                        }

                                        OutlinedButton(
                                            onClick = { settingsViewModel.resetSafFolder() },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Reset", color = TextSecondaryDark)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Camera Defaults
            item {
                Text(
                    text = "CAMERA PREFERENCES",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentElectricBlue,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Default Camera
                        SettingRow(title = "Default Camera") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = settings.defaultCameraFacing == CameraFacing.BACK,
                                    onClick = { settingsViewModel.setDefaultCameraFacing(CameraFacing.BACK) },
                                    label = { Text("Rear") },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentElectricBlue.copy(alpha = 0.2f), selectedLabelColor = AccentElectricBlue)
                                )
                                FilterChip(
                                    selected = settings.defaultCameraFacing == CameraFacing.FRONT,
                                    onClick = { settingsViewModel.setDefaultCameraFacing(CameraFacing.FRONT) },
                                    label = { Text("Front") },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentElectricBlue.copy(alpha = 0.2f), selectedLabelColor = AccentElectricBlue)
                                )
                            }
                        }

                        // Default Aspect Ratio
                        SettingRow(title = "Aspect Ratio") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AspectRatioOption.entries.forEach { ratio ->
                                    FilterChip(
                                        selected = settings.defaultAspectRatio == ratio,
                                        onClick = { settingsViewModel.setDefaultAspectRatio(ratio) },
                                        label = { Text(ratio.title) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentElectricBlue.copy(alpha = 0.2f), selectedLabelColor = AccentElectricBlue)
                                    )
                                }
                            }
                        }

                        // Burst Count
                        SettingRow(title = "Burst Shots Count") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(3, 5, 10).forEach { count ->
                                    FilterChip(
                                        selected = settings.burstCount == count,
                                        onClick = { settingsViewModel.setBurstCount(count) },
                                        label = { Text("$count Shots") },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentElectricBlue.copy(alpha = 0.2f), selectedLabelColor = AccentElectricBlue)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Geotagging & Privacy
            item {
                Text(
                    text = "LOCATION & PRIVACY",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentElectricBlue,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "GPS Geotagging",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimaryDark,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Attach GPS coordinates to image EXIF metadata. Location is saved locally into the photo only.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryDark
                            )
                        }
                        Switch(
                            checked = settings.geotaggingEnabled,
                            onCheckedChange = { enable ->
                                if (enable) {
                                    if (PermissionManager.hasLocationPermission(context)) {
                                        settingsViewModel.setGeotagging(true)
                                    } else {
                                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                } else {
                                    settingsViewModel.setGeotagging(false)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = AccentElectricBlue
                            )
                        )
                    }
                }
            }

            // Section 4: Theme
            item {
                Text(
                    text = "APPEARANCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentElectricBlue,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AppTheme.entries.forEach { theme ->
                            FilterChip(
                                selected = settings.appTheme == theme,
                                onClick = { settingsViewModel.setAppTheme(theme) },
                                label = { Text(theme.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentElectricBlue.copy(alpha = 0.2f),
                                    selectedLabelColor = AccentElectricBlue
                                )
                            )
                        }
                    }
                }
            }

            // Section 5: About CamCraft
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = AccentElectricBlue)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "About CamCraft 1.0",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimaryDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Engineered with native Kotlin, Jetpack Compose, Android CameraX, Coroutines, MediaStore API, and Storage Access Framework. Built for production reliability.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryDark,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}
