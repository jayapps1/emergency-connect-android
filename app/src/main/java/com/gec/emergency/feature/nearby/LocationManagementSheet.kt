package com.gec.emergency.feature.nearby

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationManagementSheet(
    uiState: NearbyUiState,
    onDismissRequest: () -> Unit,
    onRefreshLocationClick: () -> Unit,
    onGrantPermissionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val location = uiState.currentLocation
    val locTime = location?.timestampMillis ?: System.currentTimeMillis()
    val ageMin = ((System.currentTimeMillis() - locTime) / 60000).toInt()
    val isStale = ageMin > 5

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Location Management",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Location Permission Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.locationAvailable) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    },
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (uiState.locationAvailable) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (uiState.locationAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.locationAvailable) "Location Access Allowed" else "Location Permission Required",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (uiState.locationAvailable && location != null) {
                        Text(
                            text = "Accuracy: ±${location.accuracyMeters?.toInt() ?: 10} m",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = if (isStale) "Last Updated: $ageMin min ago (Location may be outdated)" else "Last Updated: Just now ($ageMin min ago)",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isStale) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isStale) FontWeight.Bold else FontWeight.Normal,
                        )
                    } else {
                        Text(
                            text = "Allow location access to calculate real-time distances and recommend the nearest available response unit.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.locationAvailable) {
                    Button(
                        onClick = {
                            onRefreshLocationClick()
                            onDismissRequest()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Refresh Location & Station Distances")
                    }
                } else {
                    Button(
                        onClick = {
                            onGrantPermissionClick()
                            onDismissRequest()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Allow Location Access")
                    }
                }

                if (uiState.isLocationDisabled) {
                    OutlinedButton(
                        onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            } catch (_: Exception) {
                                onRefreshLocationClick()
                            }
                            onDismissRequest()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Enable Device Location (GPS)")
                    }
                }

                if (uiState.permissionState == LocationPermissionState.PermanentlyDenied) {
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                onGrantPermissionClick()
                            }
                            onDismissRequest()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Open App Settings")
                    }
                }
            }

            // Diagnostics (Debug Tracing)
            if (location != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Diagnostics",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Lat: ${location.latitude}, Lon: ${location.longitude}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = "Source: ${if (location.isMock) "MOCK / EMULATOR" else "GPS"}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
