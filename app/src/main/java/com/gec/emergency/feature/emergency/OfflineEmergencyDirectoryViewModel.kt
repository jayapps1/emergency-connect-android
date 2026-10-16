package com.gec.emergency.feature.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gec.emergency.core.calling.CallManager
import com.gec.emergency.core.calling.CallRequest
import com.gec.emergency.core.calling.CallResult
import com.gec.emergency.core.calling.CallType
import com.gec.emergency.core.location.LocationProvider
import com.gec.emergency.core.location.LocationResult
import com.gec.emergency.domain.model.OfflineEmergencyContact
import com.gec.emergency.domain.model.OfflineRegionalHeadquartersContact
import com.gec.emergency.domain.repository.OfflineEmergencyDirectoryRepository
import com.gec.emergency.domain.usecase.GetOfflineEmergencyContactsUseCase
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
class OfflineEmergencyDirectoryViewModel @Inject constructor(
    private val getOfflineEmergencyContactsUseCase: GetOfflineEmergencyContactsUseCase,
    private val offlineDirectoryRepository: OfflineEmergencyDirectoryRepository,
    private val locationProvider: LocationProvider,
    private val callManager: CallManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OfflineEmergencyDirectoryUiState())
    val uiState: StateFlow<OfflineEmergencyDirectoryUiState> = _uiState.asStateFlow()

    private val _callEvents = Channel<CallResult>(Channel.BUFFERED)
    val callEvents = _callEvents.receiveAsFlow()

    init {
        loadShortCodes()
        loadRegions()
        detectLocationAndPreselectRegion()
        syncDirectory()
    }

    private fun syncDirectory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            offlineDirectoryRepository.syncOfflineDirectory()
            _uiState.value = _uiState.value.copy(isSyncing = false)
            loadRegionalContacts(_uiState.value.selectedRegionName)
        }
    }

    private fun loadShortCodes() {
        viewModelScope.launch {
            getOfflineEmergencyContactsUseCase()
                .onStart { _uiState.value = _uiState.value.copy(isLoading = true) }
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.localizedMessage,
                    )
                }
                .collect { contacts ->
                    val primary = contacts.find { it.isNational && it.phoneNumber == "112" }
                        ?: contacts.firstOrNull { it.isNational }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        shortCodeContacts = contacts,
                        primaryNationalContact = primary,
                    )
                }
        }
    }

    private fun loadRegions() {
        viewModelScope.launch {
            offlineDirectoryRepository.getRegions()
                .collect { regions ->
                    _uiState.value = _uiState.value.copy(availableRegions = regions)
                }
        }
    }

    private fun detectLocationAndPreselectRegion() {
        viewModelScope.launch {
            when (val result = locationProvider.getCurrentLocation()) {
                is LocationResult.Success -> {
                    val detectedRegion = inferRegionFromCoordinates(result.location.latitude, result.location.longitude)
                    if (!detectedRegion.isNullOrBlank()) {
                        selectRegion(detectedRegion)
                        return@launch
                    }
                }
                else -> { /* Use default region if location unavailable */ }
            }
            loadRegionalContacts(_uiState.value.selectedRegionName)
        }
    }

    fun selectRegion(regionName: String) {
        _uiState.value = _uiState.value.copy(selectedRegionName = regionName)
        loadRegionalContacts(regionName)
    }

    private fun loadRegionalContacts(regionName: String) {
        viewModelScope.launch {
            offlineDirectoryRepository.getRegionalHeadquartersContacts(regionName)
                .catch { /* keep existing list on error */ }
                .collect { rhqContacts ->
                    _uiState.value = _uiState.value.copy(regionalHeadquartersContacts = rhqContacts)
                }
        }
    }

    fun makeCall(contact: OfflineEmergencyContact, fastCall: Boolean = false) {
        viewModelScope.launch {
            val callType = when {
                contact.isNational && contact.phoneNumber == "112" -> CallType.NATIONAL_EMERGENCY
                fastCall -> CallType.FAST
                else -> CallType.GENERAL
            }
            val request = CallRequest(
                phoneNumber = contact.phoneNumber,
                callType = callType,
            )
            val result = callManager.placeCall(request)
            _callEvents.send(result)
        }
    }

    fun makeRhqCall(rhqContact: OfflineRegionalHeadquartersContact) {
        viewModelScope.launch {
            val request = CallRequest(
                phoneNumber = rhqContact.phoneNumber,
                callType = CallType.STATION_CONTROL_ROOM,
                stationId = rhqContact.stationId,
            )
            val result = callManager.placeCall(request)
            _callEvents.send(result)
        }
    }

    private fun inferRegionFromCoordinates(lat: Double, lng: Double): String? {
        // Approximate coordinate bounding box for Greater Accra vs Central vs Ashanti vs Western
        return when {
            lat in 5.50..5.80 && lng in -0.40..0.05 -> "Greater Accra"
            lat in 5.00..5.60 && lng in -2.10..-0.50 -> "Central"
            lat in 6.40..7.10 && lng in -2.20..-1.00 -> "Ashanti"
            lat in 4.80..5.50 && lng in -3.10..-1.80 -> "Western"
            else -> null
        }
    }
}
