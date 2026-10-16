package com.gec.emergency.feature.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gec.emergency.core.location.LocationProvider
import com.gec.emergency.core.location.LocationResult
import com.gec.emergency.domain.model.NearbyStation
import com.gec.emergency.domain.model.NearbyStationQuery
import com.gec.emergency.domain.usecase.GetActiveAgenciesUseCase
import com.gec.emergency.domain.usecase.GetNearbyStationsUseCase
import com.gec.emergency.domain.usecase.GetPreferredStationContactUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearbyViewModel @Inject constructor(
    private val locationProvider: LocationProvider,
    private val getNearbyStationsUseCase: GetNearbyStationsUseCase,
    private val getActiveAgenciesUseCase: GetActiveAgenciesUseCase,
    private val getPreferredStationContactUseCase: GetPreferredStationContactUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NearbyUiState())
    val uiState: StateFlow<NearbyUiState> = _uiState.asStateFlow()

    init {
        loadActiveAgencies()
    }

    private fun loadActiveAgencies() {
        viewModelScope.launch {
            getActiveAgenciesUseCase()
                .catch { /* ignore error */ }
                .collect { agencies ->
                    _uiState.value = _uiState.value.copy(agencies = agencies)
                }
        }
    }

    fun onPermissionGranted() {
        _uiState.value = _uiState.value.copy(
            permissionState = LocationPermissionState.Granted,
        )
        refreshLocation()
    }

    fun onPermissionDenied(permanentlyDenied: Boolean = false) {
        val state = if (permanentlyDenied) {
            LocationPermissionState.PermanentlyDenied
        } else {
            LocationPermissionState.Denied
        }
        _uiState.value = _uiState.value.copy(
            permissionState = state,
            isLoadingLocation = false,
            locationAvailable = false,
        )
    }

    fun setSelectedAgency(agencyId: String?) {
        _uiState.value = _uiState.value.copy(selectedAgencyId = agencyId)
        fetchNearbyStations()
    }

    fun setSearchRadius(radiusKm: Double) {
        if (radiusKm <= 0.0) return
        _uiState.value = _uiState.value.copy(searchRadiusKm = radiusKm)
        fetchNearbyStations()
    }

    fun refreshLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingLocation = true,
                errorMessage = null,
            )

            when (val result = locationProvider.getCurrentLocation()) {
                is LocationResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingLocation = false,
                        currentLocation = result.location,
                        locationAccuracyMeters = result.location.accuracyMeters,
                        locationAvailable = true,
                        errorMessage = null,
                    )
                    fetchNearbyStations()
                }
                LocationResult.PermissionDenied -> {
                    onPermissionDenied(permanentlyDenied = false)
                }
                LocationResult.LocationDisabled -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingLocation = false,
                        locationAvailable = false,
                        errorMessage = "Location services are turned off.",
                    )
                }
                LocationResult.Unavailable -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingLocation = false,
                        locationAvailable = false,
                        errorMessage = "Unable to retrieve current location.",
                    )
                }
                is LocationResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingLocation = false,
                        locationAvailable = false,
                        errorMessage = result.message ?: "An error occurred fetching location.",
                    )
                }
            }
        }
    }

    private fun fetchNearbyStations() {
        val location = _uiState.value.currentLocation ?: return
        viewModelScope.launch {
            val query = NearbyStationQuery(
                location = location,
                agencyId = _uiState.value.selectedAgencyId,
                radiusKm = _uiState.value.searchRadiusKm,
            )
            getNearbyStationsUseCase(query)
                .onStart {
                    _uiState.value = _uiState.value.copy(errorMessage = null)
                }
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.localizedMessage ?: "Failed to load stations.",
                        nearbyStations = emptyList(),
                    )
                }
                .collect { stations ->
                    _uiState.value = _uiState.value.copy(
                        nearbyStations = stations,
                        errorMessage = null,
                    )
                    loadPreferredContacts(stations)
                }
        }
    }

    private fun loadPreferredContacts(stations: List<NearbyStation>) {
        stations.forEach { nearbyStation ->
            val station = nearbyStation.station
            viewModelScope.launch {
                getPreferredStationContactUseCase(station.id)
                    .catch { /* ignore error */ }
                    .collect { contact ->
                        if (contact != null) {
                            val map = _uiState.value.preferredContacts.toMutableMap()
                            map[station.id] = contact
                            _uiState.value = _uiState.value.copy(preferredContacts = map)
                        }
                    }
            }
        }
    }
}
