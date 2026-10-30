package com.gec.emergency.feature.nearby

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gec.emergency.R
import com.gec.emergency.domain.model.Agency
import com.gec.emergency.domain.model.GeoLocation
import com.gec.emergency.domain.model.NearbyStation
import com.gec.emergency.domain.model.Station
import com.gec.emergency.domain.model.StationContact
import com.gec.emergency.domain.model.StationContactType
import com.gec.emergency.ui.theme.GhanaEmergencyConnectTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyScreen(
    uiState: NearbyUiState,
    onBackClick: () -> Unit,
    onGrantPermissionClick: () -> Unit,
    onBrowseAllServicesClick: () -> Unit,
    onStationClick: (String) -> Unit,
    onRefreshLocationClick: () -> Unit,
    onAgencySelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showLocationSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Emergency Stations") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showLocationSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location Management",
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
                // Location is loading
                uiState.isLoadingLocation -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.getting_location),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Interactive Location Permission & GPS Banner
                        item {
                            when (uiState.permissionState) {
                                LocationPermissionState.Granted -> {
                                    if (uiState.isLocationDisabled) {
                                        LocationServicesOffBanner(
                                            onOpenSettingsClick = {
                                                try {
                                                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                                                } catch (_: Exception) {
                                                    onRefreshLocationClick()
                                                }
                                            }
                                        )
                                    } else if (uiState.locationAvailable) {
                                        LocationStatusCard(
                                            accuracyMeters = uiState.locationAccuracyMeters,
                                            isMock = uiState.currentLocation?.isMock == true,
                                            onCardClick = { showLocationSheet = true },
                                            onRefreshClick = onRefreshLocationClick,
                                        )
                                    }
                                }
                                LocationPermissionState.PermanentlyDenied -> {
                                    LocationPermanentlyDeniedBanner(
                                        onOpenSettingsClick = {
                                            try {
                                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                    data = Uri.fromParts("package", context.packageName, null)
                                                }
                                                context.startActivity(intent)
                                            } catch (_: Exception) {
                                                onGrantPermissionClick()
                                            }
                                        }
                                    )
                                }
                                else -> {
                                    LocationPermissionCard(
                                        onGrantPermissionClick = onGrantPermissionClick,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Agency Filter Chips (All Services | GNFS | GPS | NAS)
                        item {
                            AgencyFilterRow(
                                agencies = uiState.agencies,
                                selectedAgencyId = uiState.selectedAgencyId,
                                onAgencySelected = onAgencySelected,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Station List with Nearest Geographical & Nearest Available Response Cards
                        if (uiState.nearbyStations.isNotEmpty()) {
                            val nearestGeographical = uiState.nearbyStations.firstOrNull()
                            val nearestAvailable = uiState.nearbyStations.firstOrNull { it.isAvailable }

                            // If nearest station is unavailable, highlight Nearest Available Response card
                            if (nearestGeographical != null && !nearestGeographical.isAvailable && nearestAvailable != null) {
                                item {
                                    Text(
                                        text = "Nearest Available Response",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val preferredContact = uiState.preferredContacts[nearestAvailable.station.id]
                                    NearbyStationCard(
                                        nearbyStation = nearestAvailable,
                                        preferredContact = preferredContact,
                                        locationAvailable = uiState.locationAvailable,
                                        formattedDistance = if (uiState.locationAvailable) DistanceFormatter.formatDistance(context, nearestAvailable.distanceKm) else "",
                                        isHighlightedAvailable = true,
                                        onClick = { onStationClick(nearestAvailable.station.id) },
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "All Nearby Stations",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            items(uiState.nearbyStations, key = { it.station.id }) { nearbyStation ->
                                val preferredContact = uiState.preferredContacts[nearbyStation.station.id]
                                NearbyStationCard(
                                    nearbyStation = nearbyStation,
                                    preferredContact = preferredContact,
                                    locationAvailable = uiState.locationAvailable,
                                    formattedDistance = if (uiState.locationAvailable) DistanceFormatter.formatDistance(context, nearbyStation.distanceKm) else "",
                                    onClick = { onStationClick(nearbyStation.station.id) },
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        } else {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    ),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text(
                                            text = "No active stations found for the selected filter.",
                                            style = MaterialTheme.typography.titleMedium,
                                            textAlign = TextAlign.Center,
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        OutlinedButton(
                                            onClick = { onAgencySelected(null) },
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Text("Show All Services")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLocationSheet) {
        LocationManagementSheet(
            uiState = uiState,
            onDismissRequest = { showLocationSheet = false },
            onRefreshLocationClick = onRefreshLocationClick,
            onGrantPermissionClick = onGrantPermissionClick,
        )
    }
}

@Composable
private fun LocationPermissionCard(
    onGrantPermissionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Find Nearest Emergency Stations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Allow location access to calculate real-time distance and recommend the nearest available emergency response unit.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onGrantPermissionClick, modifier = Modifier.fillMaxWidth()) {
                Text("Allow Location Access")
            }
        }
    }
}

@Composable
private fun LocationServicesOffBanner(
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Turn On Device Location",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = "Device location (GPS) is turned off. Turn on location to calculate accurate station distances.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onOpenSettingsClick) {
                Text("Enable Device Location")
            }
        }
    }
}

@Composable
private fun LocationPermanentlyDeniedBanner(
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Location Access Disabled",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = "Location permission is disabled in system settings. Enable location to use nearby station distance features.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onOpenSettingsClick) {
                Text("Open App Settings")
            }
        }
    }
}

@Composable
private fun LocationStatusCard(
    accuracyMeters: Double?,
    isMock: Boolean,
    onCardClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val accuracyText = DistanceFormatter.formatAccuracy(context, accuracyMeters) ?: "±5 m"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
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
                        text = stringResource(R.string.your_location_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = if (isMock) "Test Location Active ($accuracyText)" else accuracyText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isMock) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            IconButton(onClick = onRefreshClick) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.refresh_location),
                )
            }
        }
    }
}

@Composable
private fun AgencyFilterRow(
    agencies: List<Agency>,
    selectedAgencyId: String?,
    onAgencySelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = selectedAgencyId == null,
                onClick = { onAgencySelected(null) },
                label = { Text("All Services") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                    )
                },
            )
        }

        items(agencies, key = { it.id }) { agency ->
            val labelText = when (agency.type) {
                com.gec.emergency.domain.model.AgencyType.FIRE -> "GNFS"
                com.gec.emergency.domain.model.AgencyType.POLICE -> "GPS"
                com.gec.emergency.domain.model.AgencyType.AMBULANCE -> "NAS"
                com.gec.emergency.domain.model.AgencyType.DISASTER_MANAGEMENT -> "NADMO"
                else -> agency.code
            }
            FilterChip(
                selected = selectedAgencyId == agency.id,
                onClick = {
                    if (selectedAgencyId == agency.id) onAgencySelected(null) else onAgencySelected(agency.id)
                },
                label = { Text(labelText) },
            )
        }
    }
}

@Composable
private fun NearbyStationCard(
    nearbyStation: NearbyStation,
    preferredContact: StationContact?,
    locationAvailable: Boolean,
    formattedDistance: String,
    onClick: () -> Unit,
    isHighlightedAvailable: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val station = nearbyStation.station
    val isAvailable = nearbyStation.isAvailable
    val cardBg = if (!isAvailable) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    } else if (isHighlightedAvailable) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val unavailableReason = when {
        station.outOfCommissionCount > 0 -> "Out of Commission"
        station.atEmergencyCount > 0 -> "Responding at another emergency"
        station.maintenanceCount > 0 -> "Under Maintenance"
        else -> "No appliance currently available"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlightedAvailable) 4.dp else 2.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Row 1: Station Name + Verified Badge
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                if (station.isVerified) {
                    Spacer(modifier = Modifier.width(6.dp))
                    AssistChip(
                        onClick = onClick,
                        label = { Text("Verified") },
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 2: Locality & Region
            Text(
                text = "${station.locality ?: station.district}, ${station.region}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Dedicated Operational Status Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Operational Status: ",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isAvailable) {
                    AssistChip(
                        onClick = onClick,
                        label = { Text("Appliance Available") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    )
                } else {
                    AssistChip(
                        onClick = onClick,
                        label = { Text(unavailableReason) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        },
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 4: Distance Label
            Text(
                text = if (locationAvailable && formattedDistance.isNotBlank()) "Approx. $formattedDistance away" else "Distance unavailable",
                style = MaterialTheme.typography.bodySmall,
                color = if (locationAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (locationAvailable) FontWeight.Bold else FontWeight.Normal,
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Row 5: Phone Contact & Call Button (Enabled if available, Disabled if unavailable)
            if (preferredContact != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = preferredContact.phoneNumber,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        if (preferredContact.is24Hour) {
                            Spacer(modifier = Modifier.width(6.dp))
                            AssistChip(
                                onClick = onClick,
                                label = { Text("24/7") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                ),
                            )
                        }
                    }

                    Button(
                        onClick = onClick,
                        enabled = isAvailable,
                    ) {
                        Text(if (isAvailable) stringResource(R.string.call_button_label) else "Call Unavailable")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Contact currently unavailable",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )

                    OutlinedButton(onClick = onClick) {
                        Text("View Details")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NearbyScreenPreview() {
    val sampleStation = Station(
        id = "st_01",
        agencyId = "gnfs",
        name = "GNFS Headquarters Station",
        code = "GNFS-ACC-01",
        region = "Greater Accra",
        district = "Accra Metropolitan",
        locality = "Makola",
        latitude = 5.5500,
        longitude = -0.2000,
        isVerified = true,
    )
    val sampleNearby = NearbyStation(
        station = sampleStation,
        distanceKm = 1.2,
    )
    val sampleContact = StationContact(
        id = "sc_01",
        stationId = "st_01",
        phoneNumber = "+233 302 000 001",
        type = StationContactType.WATCHROOM,
        priority = 1,
        is24Hour = true,
    )

    GhanaEmergencyConnectTheme {
        NearbyScreen(
            uiState = NearbyUiState(
                permissionState = LocationPermissionState.Granted,
                locationAvailable = true,
                currentLocation = GeoLocation(5.5500, -0.2000, 10.0),
                locationAccuracyMeters = 10.0,
                nearbyStations = listOf(sampleNearby),
                preferredContacts = mapOf("st_01" to sampleContact),
            ),
            onBackClick = {},
            onGrantPermissionClick = {},
            onBrowseAllServicesClick = {},
            onStationClick = {},
            onRefreshLocationClick = {},
            onAgencySelected = {},
        )
    }
}
