package com.gec.emergency.feature.stations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gec.emergency.core.calling.CallManager
import com.gec.emergency.core.calling.CallRequest
import com.gec.emergency.core.calling.CallResult
import com.gec.emergency.core.calling.CallType
import com.gec.emergency.domain.model.GeoLocation
import com.gec.emergency.domain.model.NearbyStationQuery
import com.gec.emergency.domain.model.Station
import com.gec.emergency.domain.model.StationContact
import com.gec.emergency.domain.model.StationContactType
import com.gec.emergency.domain.usecase.GetNearbyStationsUseCase
import com.gec.emergency.domain.usecase.GetStationContactsUseCase
import com.gec.emergency.domain.usecase.GetStationsByAgencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StationsViewModel @Inject constructor(
    private val getStationsByAgencyUseCase: GetStationsByAgencyUseCase,
    private val getNearbyStationsUseCase: GetNearbyStationsUseCase,
    private val getStationContactsUseCase: GetStationContactsUseCase,
    private val callManager: CallManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StationsUiState())
    val uiState: StateFlow<StationsUiState> = _uiState.asStateFlow()

    private val _callEvents = Channel<CallResult>(Channel.BUFFERED)
    val callEvents = _callEvents.receiveAsFlow()

    init {
        loadNearbyStations(latitude = 5.5500, longitude = -0.2000)
    }

    fun selectAgency(agencyId: String?) {
        _uiState.value = _uiState.value.copy(selectedAgencyId = agencyId)
        if (agencyId.isNullOrEmpty()) {
            loadNearbyStations(latitude = 5.5500, longitude = -0.2000)
        } else {
            loadStationsByAgency(agencyId)
        }
    }

    fun initiateCall(station: Station, contact: StationContact) {
        val callType = when (contact.type) {
            StationContactType.EMERGENCY -> CallType.EMERGENCY_DIRECT
            StationContactType.WATCHROOM -> CallType.STATION_WATCHROOM
            StationContactType.CONTROL_ROOM -> CallType.STATION_CONTROL_ROOM
            else -> CallType.GENERAL
        }

        val request = CallRequest(
            phoneNumber = contact.phoneNumber,
            callType = callType,
            stationId = station.id,
        )

        val contactLabel = formatContactType(contact.type)

        _uiState.value = _uiState.value.copy(
            callConfirmationState = CallConfirmationState(
                isVisible = true,
                stationName = station.name,
                phoneNumber = contact.phoneNumber,
                contactTypeLabel = contactLabel,
                callRequest = request,
            )
        )
    }

    fun dismissCallConfirmation() {
        _uiState.value = _uiState.value.copy(
            callConfirmationState = _uiState.value.callConfirmationState.copy(isVisible = false)
        )
    }

    fun confirmCall(request: CallRequest) {
        dismissCallConfirmation()
        viewModelScope.launch {
            val result = callManager.placeCall(request)
            _callEvents.send(result)
        }
    }

    fun loadStationsByAgency(agencyId: String) {
        viewModelScope.launch {
            getStationsByAgencyUseCase(agencyId)
                .onStart {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        errorMessage = null,
                    )
                }
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.localizedMessage ?: "An error occurred",
                        isEmpty = true,
                    )
                }
                .collect { stations ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        stations = stations,
                        isEmpty = stations.isEmpty(),
                        errorMessage = null,
                    )
                    loadContactsForStations(stations)
                }
        }
    }

    fun loadNearbyStations(latitude: Double, longitude: Double, radiusKm: Double = 10.0) {
        viewModelScope.launch {
            val query = NearbyStationQuery(
                location = GeoLocation(latitude = latitude, longitude = longitude),
                agencyId = _uiState.value.selectedAgencyId,
                radiusKm = radiusKm,
            )
            getNearbyStationsUseCase(query)
                .onStart {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        errorMessage = null,
                    )
                }
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.localizedMessage ?: "An error occurred",
                        isEmpty = true,
                    )
                }
                .collect { nearbyStations ->
                    val stations = nearbyStations.map { it.station }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        stations = stations,
                        isEmpty = stations.isEmpty(),
                        errorMessage = null,
                    )
                    loadContactsForStations(stations)
                }
        }
    }

    private fun loadContactsForStations(stations: List<Station>) {
        stations.forEach { station ->
            viewModelScope.launch {
                getStationContactsUseCase(station.id)
                    .catch { /* ignore contact loading errors gracefully */ }
                    .collect { contacts ->
                        val updatedMap = _uiState.value.stationContacts.toMutableMap()
                        updatedMap[station.id] = contacts
                        _uiState.value = _uiState.value.copy(stationContacts = updatedMap)
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
}
