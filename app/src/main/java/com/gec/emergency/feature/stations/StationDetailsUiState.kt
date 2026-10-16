package com.gec.emergency.feature.stations

import com.gec.emergency.domain.model.Station
import com.gec.emergency.domain.model.StationContact

data class StationDetailsUiState(
    val isLoading: Boolean = false,
    val station: Station? = null,
    val contacts: List<StationContact> = emptyList(),
    val callConfirmationState: CallConfirmationState = CallConfirmationState(),
    val errorMessage: String? = null,
    val isEmpty: Boolean = false,
)
