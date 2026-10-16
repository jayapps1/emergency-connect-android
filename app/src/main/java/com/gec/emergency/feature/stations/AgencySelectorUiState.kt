package com.gec.emergency.feature.stations

import com.gec.emergency.domain.model.Agency

data class AgencySelectorUiState(
    val isLoading: Boolean = false,
    val agencies: List<Agency> = emptyList(),
    val errorMessage: String? = null,
    val isEmpty: Boolean = false,
)
