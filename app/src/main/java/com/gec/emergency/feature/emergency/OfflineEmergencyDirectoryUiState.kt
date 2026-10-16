package com.gec.emergency.feature.emergency

import com.gec.emergency.domain.model.OfflineEmergencyContact
import com.gec.emergency.domain.model.OfflineRegionalHeadquartersContact
import com.gec.emergency.domain.model.RegionRef

data class OfflineEmergencyDirectoryUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val shortCodeContacts: List<OfflineEmergencyContact> = emptyList(),
    val regionalHeadquartersContacts: List<OfflineRegionalHeadquartersContact> = emptyList(),
    val availableRegions: List<RegionRef> = emptyList(),
    val selectedRegionName: String = "Central",
    val primaryNationalContact: OfflineEmergencyContact? = null,
    val errorMessage: String? = null,
)
