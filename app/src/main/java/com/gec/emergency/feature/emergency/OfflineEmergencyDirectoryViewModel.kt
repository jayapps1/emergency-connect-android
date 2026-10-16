package com.gec.emergency.feature.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gec.emergency.core.calling.CallManager
import com.gec.emergency.core.calling.CallRequest
import com.gec.emergency.core.calling.CallResult
import com.gec.emergency.core.calling.CallType
import com.gec.emergency.domain.model.OfflineEmergencyContact
import com.gec.emergency.domain.usecase.GetOfflineEmergencyContactsUseCase
import com.gec.emergency.domain.usecase.GetPrimaryNationalEmergencyContactUseCase
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
    private val getPrimaryNationalEmergencyContactUseCase: GetPrimaryNationalEmergencyContactUseCase,
    private val callManager: CallManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OfflineEmergencyDirectoryUiState())
    val uiState: StateFlow<OfflineEmergencyDirectoryUiState> = _uiState.asStateFlow()

    private val _callEvents = Channel<CallResult>(Channel.BUFFERED)
    val callEvents = _callEvents.receiveAsFlow()

    init {
        loadOfflineContacts()
    }

    private fun loadOfflineContacts() {
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
                        contacts = contacts,
                        primaryNationalContact = primary,
                    )
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
}
