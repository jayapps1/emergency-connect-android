package com.gec.emergency.feature.stations

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
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gec.emergency.R
import com.gec.emergency.core.calling.CallRequest
import com.gec.emergency.domain.model.Station
import com.gec.emergency.domain.model.StationContact
import com.gec.emergency.domain.model.StationContactType
import com.gec.emergency.ui.theme.GhanaEmergencyConnectTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationsScreen(
    uiState: StationsUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onStationClick: (String) -> Unit = {},
    onInitiateCall: (Station, StationContact) -> Unit = { _, _ -> },
    onConfirmCall: (CallRequest) -> Unit = {},
    onDismissCallConfirmation: () -> Unit = {},
    onRetryClick: () -> Unit = {},
    onOfflineDirectoryClick: () -> Unit = {},
) {
    val titleText = if (!uiState.selectedAgencyId.isNullOrEmpty()) {
        stringResource(R.string.title_stations)
    } else {
        stringResource(R.string.title_nearby_stations)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(titleText) },
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
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.stations.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.stations, key = { it.id }) { station ->
                            val contacts = uiState.stationContacts[station.id] ?: emptyList()
                            StationItem(
                                station = station,
                                contacts = contacts,
                                onStationClick = { onStationClick(station.id) },
                                onInitiateCall = { contact ->
                                    onInitiateCall(station, contact)
                                },
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Text(
                            text = uiState.errorMessage
                                ?: stringResource(R.string.placeholder_no_stations),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onRetryClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.try_again))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onOfflineDirectoryClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.title_offline_directory))
                        }
                    }
                }
            }
        }
    }

    CallConfirmationDialog(
        state = uiState.callConfirmationState,
        onConfirmCall = onConfirmCall,
        onDismiss = onDismissCallConfirmation,
    )
}

@Composable
private fun StationItem(
    station: Station,
    contacts: List<StationContact>,
    onStationClick: () -> Unit,
    onInitiateCall: (StationContact) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onStationClick),
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
                        onClick = onStationClick,
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
            Text(
                text = "${station.locality ?: station.district}, ${station.region}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (contacts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                contacts.forEach { contact ->
                    ContactRow(
                        contact = contact,
                        onCallClick = { onInitiateCall(contact) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ContactRow(
    contact: StationContact,
    onCallClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = formatContactType(contact.type),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (contact.priority == 1) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = contact.phoneNumber,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (contact.priority == 1) {
            Button(
                onClick = onCallClick,
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.call_button_label))
            }
        } else {
            OutlinedButton(
                onClick = onCallClick,
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.call_button_label))
            }
        }
    }
}

private fun formatContactType(type: StationContactType): String {
    return when (type) {
        StationContactType.WATCHROOM -> "Watchroom"
        StationContactType.CONTROL_ROOM -> "Control Room"
        StationContactType.EMERGENCY -> "Emergency Direct"
        StationContactType.ALTERNATIVE -> "Alternative Line"
        StationContactType.ADMINISTRATIVE -> "Administrative Office"
        StationContactType.OTHER -> "General Line"
    }
}

@Preview(showBackground = true)
@Composable
fun StationsScreenPreview() {
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
    val sampleContacts = listOf(
        StationContact(
            id = "sc_01",
            stationId = "st_01",
            phoneNumber = "+233 302 000 001",
            type = StationContactType.WATCHROOM,
            priority = 1,
        ),
    )
    GhanaEmergencyConnectTheme {
        StationsScreen(
            uiState = StationsUiState(
                stations = listOf(sampleStation),
                stationContacts = mapOf("st_01" to sampleContacts),
            ),
            onBackClick = {},
        )
    }
}
