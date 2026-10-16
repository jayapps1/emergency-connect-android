package com.gec.emergency.feature.nearby

import com.gec.emergency.domain.model.Agency
import com.gec.emergency.domain.model.GeoLocation
import com.gec.emergency.domain.model.NearbyStation
import com.gec.emergency.domain.model.StationContact

data class NearbyUiState(
    val isLoadingLocation: Boolean = false,
    val permissionState: LocationPermissionState = LocationPermissionState.Unknown,
    val currentLocation: GeoLocation? = null,
    val locationAccuracyMeters: Double? = null,
    val nearbyStations: List<NearbyStation> = emptyList(),
    val preferredContacts: Map<String, StationContact> = emptyMap(),
    val agencies: List<Agency> = emptyList(),
    val selectedAgencyId: String? = null,
    val searchRadiusKm: Double = 25.0,
    val errorMessage: String? = null,
    val locationAvailable: Boolean = false,
    val isLocationDisabled: Boolean = false,
)
