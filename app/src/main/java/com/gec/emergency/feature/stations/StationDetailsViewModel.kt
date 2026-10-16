package com.gec.emergency.feature.stations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gec.emergency.core.calling.CallManager
import com.gec.emergency.core.calling.CallRequest
import com.gec.emergency.core.calling.CallResult
import com.gec.emergency.core.calling.CallType
import com.gec.emergency.domain.model.Station
import com.gec.emergency.domain.model.StationContact
import com.gec.emergency.domain.model.StationContactType
import com.gec.emergency.domain.usecase.GetPublicStationContactsUseCase
import com.gec.emergency.domain.usecase.GetStationByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StationDetailsViewModel @Inject constructor(
    private val getStationByIdUseCase: GetStationByIdUseCase,
    private val getPublicStationContactsUseCase: GetPublicStationContactsUseCase,
    private val callManager: CallManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StationDetailsUiState())
    val uiState: StateFlow<StationDetailsUiState> = _uiState.asStateFlow()

    private val _callEvents = Channel<CallResult>(Channel.BUFFERED)
    val callEvents = _callEvents.receiveAsFlow()

    fun loadStationDetails(stationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val station = getStationByIdUseCase(stationId)
            if (station != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    station = station,
                )
                loadContacts(stationId)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Station not found.",
                )
            }
        }
    }

    private fun loadContacts(stationId: String) {
        viewModelScope.launch {
            getPublicStationContactsUseCase(stationId)
                .catch {
                    _uiState.value = _uiState.value.copy(
                        contacts = emptyList(),
                        isEmpty = true,
                    )
                }
                .collect { publicContacts ->
                    _uiState.value = _uiState.value.copy(
                        contacts = publicContacts,
                        isEmpty = publicContacts.isEmpty(),
                    )
                }
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

    fun makeFastCall(station: Station, contact: StationContact) {
        viewModelScope.launch {
            val request = CallRequest(
                phoneNumber = contact.phoneNumber,
                callType = CallType.FAST,
                stationId = station.id,
            )
            val result = callManager.placeCall(request)
            _callEvents.send(result)
        }
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

    private fun formatContactType(type: StationContactType): String {
        return when (type) {
            StationContactType.WATCHROOM -> "Watchroom"
            StationContactType.CONTROL_ROOM -> "Control Room"
            StationContactType.EMERGENCY -> "Emergency"
            StationContactType.ALTERNATIVE -> "Alternative"
            StationContactType.ADMINISTRATIVE -> "Administrative"
            StationContactType.OTHER -> "Other"
        }
    }
}
