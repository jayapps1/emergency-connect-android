package com.gec.emergency.feature.stations

import com.gec.emergency.domain.model.Station
import com.gec.emergency.domain.model.StationContact

data class StationsUiState(
    val isLoading: Boolean = false,
    val stations: List<Station> = emptyList(),
    val stationContacts: Map<String, List<StationContact>> = emptyMap(),
    val selectedAgencyId: String? = null,
    val callConfirmationState: CallConfirmationState = CallConfirmationState(),
    val errorMessage: String? = null,
    val isEmpty: Boolean = false,
)
