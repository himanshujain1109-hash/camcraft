package com.camcraft.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.camcraft.ui.theme.AccentElectricBlue
import com.camcraft.ui.theme.DarkSurface
import com.camcraft.ui.theme.TextPrimaryDark
import com.camcraft.ui.theme.TextSecondaryDark

@Composable
fun PermissionDialog(
    title: String,
    description: String,
    isPermanentDenial: Boolean,
    onGrantPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
    isCamera: Boolean = true
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = if (isCamera) Icons.Default.CameraAlt else Icons.Default.LocationOn,
                contentDescription = null,
                tint = AccentElectricBlue
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimaryDark
            )
        },
        text = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryDark
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isPermanentDenial) {
                        onOpenSettings()
                    } else {
                        onGrantPermission()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentElectricBlue,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isPermanentDenial) "Open Settings" else "Grant Permission")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Dismiss", color = TextSecondaryDark)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp)
    )
}
