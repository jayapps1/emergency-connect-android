package com.gec.emergency.feature.reports.silent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gec.emergency.R
import com.gec.emergency.domain.model.GeoLocation
import com.gec.emergency.domain.model.IncidentCategory
import com.gec.emergency.domain.model.IncidentReport
import com.gec.emergency.domain.model.IncidentSeverity
import com.gec.emergency.domain.model.ReportMode
import com.gec.emergency.ui.theme.GhanaEmergencyConnectTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SilentReportScreen(
    uiState: SilentReportUiState,
    onBackClick: () -> Unit,
    onCategorySelected: (IncidentCategory) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAddPhotoClick: () -> Unit,
    onAddVideoClick: () -> Unit,
    onRemoveAttachment: (SelectedMediaAttachment) -> Unit,
    onRefreshLocation: () -> Unit,
    onSubmitReport: () -> Unit,
    onViewReportDetails: (String) -> Unit,
    onNavigateToEmergencyCall: () -> Unit,
    modifier: Modifier = Modifier,
    onVehicleRegistrationChange: (String) -> Unit = {},
    onVehicleTypeChange: (String) -> Unit = {},
    onVehicleColourChange: (String) -> Unit = {},
    onDirectionOfTravelChange: (String) -> Unit = {},
) {
    val isRecklessDriving = uiState.selectedCategory?.code == "RECKLESS_DRIVING"

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Silent Emergency Report") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back),
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            when {
                uiState.submissionSucceeded && uiState.submittedReport != null -> {
                    val report = uiState.submittedReport
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.height(64.dp).width(64.dp),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Silent Report Created",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Reference: ${report.publicReference ?: report.id}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Your alert has been sent quietly to dispatchers.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { onViewReportDetails(report.id) },
                            modifier = Modifier.fillMaxWidth(0.8f),
                        ) {
                            Text("View Report Details")
                        }
                    }
                }

                uiState.isLoadingCategories -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                ),
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Use this when speaking or making a call may be unsafe.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        item {
                            Text(
                                text = "Select Incident Type",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(uiState.categories, key = { it.id }) { category ->
                            CategorySelectCard(
                                category = category,
                                isSelected = uiState.selectedCategory?.id == category.id,
                                onClick = { onCategorySelected(category) },
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Conditional Vehicle Details Section for Reckless Driving
                        if (isRecklessDriving) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    ),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsCar,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Vehicle Details (Optional)",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))

                                        OutlinedTextField(
                                            value = uiState.vehicleRegistrationNumber,
                                            onValueChange = onVehicleRegistrationChange,
                                            label = { Text("Vehicle Registration Number") },
                                            placeholder = { Text("e.g. CR-300-26") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        OutlinedTextField(
                                            value = uiState.directionOfTravel,
                                            onValueChange = onDirectionOfTravelChange,
                                            label = { Text("Direction of Travel") },
                                            placeholder = { Text("e.g. Heading toward Accra") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            OutlinedTextField(
                                                value = uiState.vehicleColour,
                                                onValueChange = onVehicleColourChange,
                                                label = { Text("Colour") },
                                                placeholder = { Text("e.g. Red") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                            )

                                            OutlinedTextField(
                                                value = uiState.vehicleType,
                                                onValueChange = onVehicleTypeChange,
                                                label = { Text("Vehicle Type") },
                                                placeholder = { Text("e.g. Taxi / Bus") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        // Call Notice Banner for Non-Silent Emergencies
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                ),
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = "For fire, medical emergencies, or major road accidents, please use Emergency Call.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = onNavigateToEmergencyCall,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Icon(imageVector = Icons.Default.PhoneInTalk, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Emergency Call Options")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Location Section
                        item {
                            LocationStatusSection(
                                isAvailable = uiState.isLocationAvailable,
                                isFetching = uiState.isFetchingLocation,
                                location = uiState.currentLocation,
                                accuracyMeters = uiState.locationAccuracyMeters,
                                onRefreshLocation = onRefreshLocation,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Optional Evidence Section (Photo & Video Attachments)
                        item {
                            Text(
                                text = "Optional Evidence (Photo / Video)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Attach images (max 10MB) or video clips (max 50MB) if safe to do so.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                OutlinedButton(
                                    onClick = onAddPhotoClick,
                                    modifier = Modifier.weight(1f),
                                    enabled = uiState.attachments.size < 4,
                                ) {
                                    Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Photo")
                                }

                                OutlinedButton(
                                    onClick = onAddVideoClick,
                                    modifier = Modifier.weight(1f),
                                    enabled = uiState.attachments.size < 4,
                                ) {
                                    Icon(imageVector = Icons.Default.Videocam, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Video")
                                }
                            }

                            if (uiState.attachments.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    items(uiState.attachments, key = { it.uri.toString() }) { attachment ->
                                        AssistChip(
                                            onClick = { },
                                            label = {
                                                Text(
                                                    text = "${if (attachment.mediaType == "IMAGE") "Photo" else "Video"}: ${attachment.fileName}",
                                                    maxLines = 1,
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = if (attachment.mediaType == "IMAGE") Icons.Default.PhotoCamera else Icons.Default.Videocam,
                                                    contentDescription = null,
                                                )
                                            },
                                            trailingIcon = {
                                                IconButton(onClick = { onRemoveAttachment(attachment) }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Remove attachment",
                                                    )
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Optional Description Section
                        item {
                            Text(
                                text = "Optional Description",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Add brief notes if safe to do so.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = uiState.description,
                                onValueChange = onDescriptionChange,
                                placeholder = { Text("e.g. 2 men in dark clothes near rear entrance") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3,
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        if (uiState.errorMessage != null) {
                            item {
                                Text(
                                    text = uiState.errorMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        item {
                            Button(
                                onClick = onSubmitReport,
                                enabled = !uiState.isSubmitting && !uiState.isUploadingMedia && uiState.selectedCategory != null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError,
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                            ) {
                                if (uiState.isSubmitting || uiState.isUploadingMedia) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onError,
                                        modifier = Modifier.height(24.dp).width(24.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (uiState.isUploadingMedia) "UPLOADING EVIDENCE..." else "SENDING ALERT...",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                } else {
                                    Text(
                                        text = "SEND SILENT REPORT",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySelectCard(
    category: IncidentCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = if (isSelected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            )
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (!category.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun LocationStatusSection(
    isAvailable: Boolean,
    isFetching: Boolean,
    location: GeoLocation?,
    accuracyMeters: Double?,
    onRefreshLocation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPlausible = location?.isPlausibleForGhana() == true

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = when {
                            isAvailable && isPlausible -> "Location detected"
                            isAvailable && !isPlausible -> "Location needs refresh"
                            else -> "Location unavailable"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    if (isAvailable && accuracyMeters != null) {
                        Text(
                            text = if (isPlausible) "Accuracy ±${accuracyMeters.toInt()}m" else "Tap refresh icon to update",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (isFetching) {
                CircularProgressIndicator(modifier = Modifier.height(24.dp).width(24.dp))
            } else {
                IconButton(onClick = onRefreshLocation) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Location")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SilentReportScreenPreview() {
    val sampleCategories = listOf(
        IncidentCategory(
            id = "cat_robbery",
            code = "ROBBERY",
            name = "Theft / Robbery / Assault",
            description = "Theft, robbery, assault, or immediate personal threat",
            severity = IncidentSeverity.CRITICAL,
            reportModeSupported = setOf(ReportMode.SILENT),
        ),
        IncidentCategory(
            id = "cat_reckless",
            code = "RECKLESS_DRIVING",
            name = "Reckless / Dangerous Driving",
            description = "Dangerous driving, speeding, hit-and-run or road threat",
            severity = IncidentSeverity.HIGH,
            reportModeSupported = setOf(ReportMode.SILENT),
        ),
    )
    GhanaEmergencyConnectTheme {
        SilentReportScreen(
            uiState = SilentReportUiState(
                categories = sampleCategories,
                selectedCategory = sampleCategories.last(),
                isLocationAvailable = true,
                currentLocation = GeoLocation(5.5500, -0.2000, 10.0),
                locationAccuracyMeters = 10.0,
                vehicleRegistrationNumber = "CR-300-26",
                directionOfTravel = "Heading toward Accra",
            ),
            onBackClick = {},
            onCategorySelected = {},
            onDescriptionChange = {},
            onAddPhotoClick = {},
            onAddVideoClick = {},
            onRemoveAttachment = {},
            onRefreshLocation = {},
            onSubmitReport = {},
            onViewReportDetails = {},
            onNavigateToEmergencyCall = {},
        )
    }
}
