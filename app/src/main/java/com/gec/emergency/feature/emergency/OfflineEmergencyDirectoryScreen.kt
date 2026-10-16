package com.gec.emergency.feature.emergency

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ChipColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gec.emergency.R
import com.gec.emergency.domain.model.AgencyType
import com.gec.emergency.domain.model.OfflineEmergencyContact
import com.gec.emergency.domain.model.OfflineRegionalHeadquartersContact
import com.gec.emergency.ui.theme.GhanaEmergencyConnectTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineEmergencyDirectoryScreen(
    uiState: OfflineEmergencyDirectoryUiState,
    onBackClick: () -> Unit,
    onRegionSelected: (String) -> Unit,
    onMakeCall: (OfflineEmergencyContact, Boolean) -> Unit,
    onMakeRhqCall: (OfflineRegionalHeadquartersContact) -> Unit,
    modifier: Modifier = Modifier,
) {
    var regionDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_offline_directory)) },
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
                uiState.isLoading -> {
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
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.offline_directory_notice),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // Section 1: Short Emergency Codes
                        item {
                            Text(
                                text = "National & Service Emergency Short Codes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        items(uiState.shortCodeContacts, key = { "short_${it.id}" }) { contact ->
                            OfflineContactCard(
                                contact = contact,
                                onCallClick = { onMakeCall(contact, false) },
                                onFastCallClick = { onMakeCall(contact, true) },
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Section 2: Regional Headquarters Contacts
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Regional Headquarters Contacts",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )

                                Box {
                                    OutlinedButton(
                                        onClick = { regionDropdownExpanded = true },
                                    ) {
                                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(uiState.selectedRegionName)
                                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                    }

                                    DropdownMenu(
                                        expanded = regionDropdownExpanded,
                                        onDismissRequest = { regionDropdownExpanded = false },
                                    ) {
                                        uiState.availableRegions.forEach { region ->
                                            DropdownMenuItem(
                                                text = { Text(region.name) },
                                                onClick = {
                                                    regionDropdownExpanded = false
                                                    onRegionSelected(region.name)
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (uiState.regionalHeadquartersContacts.isNotEmpty()) {
                            items(uiState.regionalHeadquartersContacts, key = { "rhq_${it.id}" }) { rhq ->
                                RhqContactCard(
                                    rhq = rhq,
                                    onCallClick = { onMakeRhqCall(rhq) },
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        } else {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    ),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                    ) {
                                        Text(
                                            text = "No verified Regional Headquarters numbers synced for ${uiState.selectedRegionName} Region yet.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Emergency short codes above remain available 24/7.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
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
}

@Composable
private fun OfflineContactCard(
    contact: OfflineEmergencyContact,
    onCallClick: () -> Unit,
    onFastCallClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = if (contact.isNational && contact.phoneNumber == "112") {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = contact.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (contact.isNational && contact.phoneNumber == "112") {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = contact.phoneNumber,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (contact.isNational && contact.phoneNumber == "112") {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = onCallClick) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.call_button_label))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onFastCallClick,
                    colors = if (contact.isNational && contact.phoneNumber == "112") {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    } else {
                        ButtonDefaults.buttonColors()
                    },
                ) {
                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.fast_call_button_label))
                }
            }
        }
    }
}

@Composable
private fun RhqContactCard(
    rhq: OfflineRegionalHeadquartersContact,
    onCallClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = rhq.agencyName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                SuggestionChip(
                    onClick = { },
                    label = { Text("RHQ") },
                )
            }

            Text(
                text = "${rhq.stationName} (${rhq.regionName} Region)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = rhq.phoneNumber,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = onCallClick) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call Regional HQ")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OfflineEmergencyDirectoryScreenPreview() {
    val sampleContacts = listOf(
        OfflineEmergencyContact(
            id = "off_112",
            agencyType = AgencyType.OTHER,
            label = "National Emergency Number",
            phoneNumber = "112",
            priority = 1,
            isNational = true,
        ),
        OfflineEmergencyContact(
            id = "off_gnfs",
            agencyType = AgencyType.FIRE,
            label = "Ghana National Fire Service",
            phoneNumber = "192",
            priority = 2,
        ),
    )
    GhanaEmergencyConnectTheme {
        OfflineEmergencyDirectoryScreen(
            uiState = OfflineEmergencyDirectoryUiState(shortCodeContacts = sampleContacts),
            onBackClick = {},
            onRegionSelected = {},
            onMakeCall = { _, _ -> },
            onMakeRhqCall = {},
        )
    }
}
