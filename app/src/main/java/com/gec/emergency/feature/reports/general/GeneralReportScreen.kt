package com.gec.emergency.feature.reports.general

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.gec.emergency.domain.model.IncidentSeverity
import com.gec.emergency.domain.model.ReportMode
import com.gec.emergency.ui.theme.GhanaEmergencyConnectTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralReportScreen(
    uiState: GeneralReportUiState,
    onBackClick: () -> Unit,
    onCategorySelected: (IncidentCategory) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onVehicleRegChange: (String) -> Unit,
    onVehicleTypeChange: (String) -> Unit,
    onVehicleColourChange: (String) -> Unit,
    onDirectionChange: (String) -> Unit,
    onRefreshLocation: () -> Unit,
    onSubmitReport: () -> Unit,
    onViewReportDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isReckless = uiState.selectedCategory?.code == "RECKLESS_DRIVING"

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("General Incident Report") },
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
                            text = "Report Submitted",
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
                                    text = "Your report has been sent to responsible emergency dispatchers.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { onViewReportDetails(report.publicReference ?: report.id) },
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
                            Text(
                                text = "Select Incident Category",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(uiState.categories, key = { it.id }) { category ->
                            CategoryCard(
                                category = category,
                                isSelected = uiState.selectedCategory?.id == category.id,
                                onClick = { onCategorySelected(category) },
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            LocationSection(
                                isAvailable = uiState.isLocationAvailable,
                                isFetching = uiState.isFetchingLocation,
                                location = uiState.currentLocation,
                                accuracyMeters = uiState.locationAccuracyMeters,
                                onRefreshLocation = onRefreshLocation,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        item {
                            Text(
                                text = "Incident Description",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = uiState.description,
                                onValueChange = onDescriptionChange,
                                placeholder = { Text("Provide details about the incident...") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 4,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Conditional Optional Vehicle Information
                        if (isReckless) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Optional Vehicle Details",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = uiState.vehicleRegistrationNumber,
                                            onValueChange = onVehicleRegChange,
                                            label = { Text("Vehicle Reg / Plate Number") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            OutlinedTextField(
                                                value = uiState.vehicleType,
                                                onValueChange = onVehicleTypeChange,
                                                label = { Text("Vehicle Type / Make") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            OutlinedTextField(
                                                value = uiState.vehicleColour,
                                                onValueChange = onVehicleColourChange,
                                                label = { Text("Colour") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = uiState.directionOfTravel,
                                            onValueChange = onDirectionChange,
                                            label = { Text("Direction of Travel") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
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
                                enabled = !uiState.isSubmitting && uiState.selectedCategory != null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                            ) {
                                if (uiState.isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.height(24.dp).width(24.dp),
                                    )
                                } else {
                                    Text(
                                        text = "Submit Report",
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
private fun CategoryCard(
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
private fun LocationSection(
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
            containerColor = if (isAvailable && !isPlausible) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = if (isAvailable && !isPlausible) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = when {
                            isAvailable && isPlausible -> "Location detected"
                            isAvailable && !isPlausible -> "Location may be inaccurate / Outside Ghana"
                            else -> "Location optional / unavailable"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isAvailable && !isPlausible) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface,
                    )
                    if (isAvailable && accuracyMeters != null) {
                        Text(
                            text = if (isPlausible) "Accuracy ±${accuracyMeters.toInt()}m" else "Tap Refresh to capture fresh coordinates",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isAvailable && !isPlausible) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
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
fun GeneralReportScreenPreview() {
    val sampleCategories = listOf(
        IncidentCategory(
            id = "cat_accident",
            code = "ACCIDENT",
            name = "Road Traffic Accident",
            description = "Vehicle collisions with injuries",
            severity = IncidentSeverity.HIGH,
            reportModeSupported = setOf(ReportMode.GENERAL),
        ),
        IncidentCategory(
            id = "cat_hazard",
            code = "HAZARD",
            name = "Public Safety Hazard",
            description = "Flooding, fallen power lines",
            severity = IncidentSeverity.MEDIUM,
            reportModeSupported = setOf(ReportMode.GENERAL),
        ),
    )
    GhanaEmergencyConnectTheme {
        GeneralReportScreen(
            uiState = GeneralReportUiState(
                categories = sampleCategories,
                selectedCategory = sampleCategories.first(),
                isLocationAvailable = true,
                currentLocation = GeoLocation(5.5500, -0.2000, 10.0),
                locationAccuracyMeters = 10.0,
            ),
            onBackClick = {},
            onCategorySelected = {},
            onDescriptionChange = {},
            onVehicleRegChange = {},
            onVehicleTypeChange = {},
            onVehicleColourChange = {},
            onDirectionChange = {},
            onRefreshLocation = {},
            onSubmitReport = {},
            onViewReportDetails = {},
        )
    }
}
