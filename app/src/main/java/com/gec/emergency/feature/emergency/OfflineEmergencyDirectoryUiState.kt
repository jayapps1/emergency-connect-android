package com.gec.emergency.feature.emergency

import com.gec.emergency.domain.model.OfflineEmergencyContact

data class OfflineEmergencyDirectoryUiState(
    val isLoading: Boolean = false,
    val contacts: List<OfflineEmergencyContact> = emptyList(),
    val primaryNationalContact: OfflineEmergencyContact? = null,
    val errorMessage: String? = null,
)
