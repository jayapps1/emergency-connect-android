package com.gec.emergency.feature.nearby

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
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
    onRadiusSelected: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_nearby_stations)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back),
                        )
                    }
                },
                actions = {
                    if (uiState.locationAvailable) {
                        IconButton(onClick = onRefreshLocationClick) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.refresh_location),
                            )
                        }
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
                // Permission not granted
                uiState.permissionState != LocationPermissionState.Granted -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        LocationPermissionCard(
                            onGrantPermissionClick = onGrantPermissionClick,
                            onContinueWithoutLocationClick = onBrowseAllServicesClick,
                        )
                    }
                }

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

                // Location available
                uiState.locationAvailable -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Location Status Header
                        item {
                            LocationStatusCard(
                                accuracyMeters = uiState.locationAccuracyMeters,
                                onRefreshClick = onRefreshLocationClick,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Agency Filter Chips
                        item {
                            AgencyFilterRow(
                                agencies = uiState.agencies,
                                selectedAgencyId = uiState.selectedAgencyId,
                                onAgencySelected = onAgencySelected,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Radius Filter Chips
                        item {
                            RadiusFilterRow(
                                selectedRadiusKm = uiState.searchRadiusKm,
                                onRadiusSelected = onRadiusSelected,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Station List
                        if (uiState.nearbyStations.isNotEmpty()) {
                            items(uiState.nearbyStations, key = { it.station.id }) { nearbyStation ->
                                val preferredContact = uiState.preferredContacts[nearbyStation.station.id]
                                NearbyStationCard(
                                    nearbyStation = nearbyStation,
                                    preferredContact = preferredContact,
                                    formattedDistance = DistanceFormatter.formatDistance(context, nearbyStation.distanceKm),
                                    onClick = { onStationClick(nearbyStation.station.id) },
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        } else {
                            item {
                                EmptyNearbyStationsCard(
                                    radiusKm = uiState.searchRadiusKm.toInt(),
                                    onIncreaseRadiusClick = { onRadiusSelected(50.0) },
                                    onBrowseAllClick = onBrowseAllServicesClick,
                                )
                            }
                        }
                    }
                }

                // Location unavailable / error
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = uiState.errorMessage
                                ?: stringResource(R.string.location_unavailable_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.location_unavailable_message),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onRefreshLocationClick,
                            modifier = Modifier.fillMaxWidth(0.8f),
                        ) {
                            Text(stringResource(R.string.try_again))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onBrowseAllServicesClick,
                            modifier = Modifier.fillMaxWidth(0.8f),
                        ) {
                            Text(stringResource(R.string.browse_all_services))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationStatusCard(
    accuracyMeters: Double?,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val accuracyText = DistanceFormatter.formatAccuracy(context, accuracyMeters)

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
                        text = stringResource(R.string.your_location_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (accuracyText != null) {
                        Text(
                            text = accuracyText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
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
                label = { Text(stringResource(R.string.filter_all_agencies)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                    )
                },
            )
        }

        items(agencies, key = { it.id }) { agency ->
            FilterChip(
                selected = selectedAgencyId == agency.id,
                onClick = {
                    if (selectedAgencyId == agency.id) onAgencySelected(null) else onAgencySelected(agency.id)
                },
                label = { Text(agency.code) },
            )
        }
    }
}

@Composable
private fun RadiusFilterRow(
    selectedRadiusKm: Double,
    onRadiusSelected: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val radiusOptions = listOf(5.0, 10.0, 25.0, 50.0)

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(radiusOptions) { radius ->
            FilterChip(
                selected = selectedRadiusKm == radius,
                onClick = { onRadiusSelected(radius) },
                label = { Text(stringResource(R.string.search_radius_chip, radius.toInt())) },
            )
        }
    }
}

@Composable
private fun NearbyStationCard(
    nearbyStation: NearbyStation,
    preferredContact: StationContact?,
    formattedDistance: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val station = nearbyStation.station

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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
                    SuggestionChip(
                        onClick = onClick,
                        label = { Text(stringResource(R.string.verified_label)) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${station.locality ?: station.district}, ${station.region}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formattedDistance,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }

            if (preferredContact != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

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
                                label = { Text(stringResource(R.string.twenty_four_hour_badge)) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                ),
                            )
                        }
                    }

                    Button(onClick = onClick) {
                        Text(stringResource(R.string.call_button_label))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyNearbyStationsCard(
    radiusKm: Int,
    onIncreaseRadiusClick: () -> Unit,
    onBrowseAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.no_stations_in_radius, radiusKm),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onIncreaseRadiusClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.increase_search_radius))
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onBrowseAllClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.browse_all_stations))
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
            onRadiusSelected = {},
        )
    }
}
