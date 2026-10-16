package com.gec.emergency.feature.stations

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
fun StationDetailsScreen(
    uiState: StationDetailsUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onInitiateCall: (Station, StationContact) -> Unit = { _, _ -> },
    onFastCall: (Station, StationContact) -> Unit = { _, _ -> },
    onConfirmCall: (CallRequest) -> Unit = {},
    onDismissCallConfirmation: () -> Unit = {},
    onRetryClick: () -> Unit = {},
    onOfflineDirectoryClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_station_details)) },
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
                uiState.station != null -> {
                    val station = uiState.station
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Station Info Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = station.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f),
                                    )
                                    if (station.isVerified) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        SuggestionChip(
                                            onClick = { },
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

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${station.locality ?: station.district}, ${station.region}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Location Section Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.location_section_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.latitude_label, station.latitude),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.longitude_label, station.longitude),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (station.locationAccuracyMeters != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.accuracy_label, station.locationAccuracyMeters),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline,
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Contacts Section Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                if (uiState.contacts.isNotEmpty()) {
                                    val primaryContact = uiState.contacts.first()
                                    val otherContacts = uiState.contacts.drop(1)

                                    Text(
                                        text = "Primary Contact",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    ContactDetailRow(
                                        contact = primaryContact,
                                        onCallClick = { onInitiateCall(station, primaryContact) },
                                        onFastCallClick = { onFastCall(station, primaryContact) },
                                    )

                                    if (otherContacts.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        HorizontalDivider()
                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "Other Contact Numbers",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        otherContacts.forEachIndexed { index, contact ->
                                            if (index > 0) {
                                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                            }
                                            ContactDetailRow(
                                                contact = contact,
                                                onCallClick = { onInitiateCall(station, contact) },
                                                onFastCallClick = { onFastCall(station, contact) },
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = stringResource(R.string.placeholder_no_contacts),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
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
                                ?: stringResource(R.string.placeholder_station_not_found),
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
private fun ContactDetailRow(
    contact: StationContact,
    onCallClick: () -> Unit,
    onFastCallClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatContactTypeLabel(contact.type),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (contact.priority == 1) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (contact.is24Hour) {
                        Spacer(modifier = Modifier.width(6.dp))
                        AssistChip(
                            onClick = { },
                            label = { Text(stringResource(R.string.twenty_four_hour_badge)) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            ),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = contact.phoneNumber,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
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

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onFastCallClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.fast_call_button_label))
            }
        }
    }
}

@Composable
private fun formatContactTypeLabel(type: StationContactType): String {
    return when (type) {
        StationContactType.WATCHROOM -> stringResource(R.string.contact_type_watchroom)
        StationContactType.CONTROL_ROOM -> stringResource(R.string.contact_type_control_room)
        StationContactType.EMERGENCY -> stringResource(R.string.contact_type_emergency)
        StationContactType.ALTERNATIVE -> stringResource(R.string.contact_type_alternative)
        StationContactType.ADMINISTRATIVE -> stringResource(R.string.contact_type_administrative)
        StationContactType.OTHER -> stringResource(R.string.contact_type_other)
    }
}

@Preview(showBackground = true)
@Composable
fun StationDetailsScreenPreview() {
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
        locationAccuracyMeters = 10.0,
        isVerified = true,
    )
    val sampleContacts = listOf(
        StationContact(
            id = "sc_01",
            stationId = "st_01",
            phoneNumber = "+233 302 000 001",
            type = StationContactType.WATCHROOM,
            priority = 1,
            is24Hour = true,
        ),
        StationContact(
            id = "sc_02",
            stationId = "st_01",
            phoneNumber = "+233 302 000 002",
            type = StationContactType.CONTROL_ROOM,
            priority = 2,
            is24Hour = true,
        ),
    )
    GhanaEmergencyConnectTheme {
        StationDetailsScreen(
            uiState = StationDetailsUiState(
                station = sampleStation,
                contacts = sampleContacts,
            ),
            onBackClick = {},
        )
    }
}
