package com.gec.emergency.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gec.emergency.core.calling.CallManager
import com.gec.emergency.core.calling.CallRequest
import com.gec.emergency.core.calling.CallResult
import com.gec.emergency.core.calling.CallType
import com.gec.emergency.core.common.AppInfoProvider
import com.gec.emergency.core.common.EmergencyNumberConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val appName: String = "",
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    appInfoProvider: AppInfoProvider,
    private val callManager: CallManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(appName = appInfoProvider.getAppName()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun makeNationalEmergencyCall(onResult: (CallResult) -> Unit) {
        viewModelScope.launch {
            val request = CallRequest(
                phoneNumber = EmergencyNumberConfig.NATIONAL_EMERGENCY_NUMBER,
                callType = CallType.NATIONAL_EMERGENCY,
            )
            val result = callManager.placeCall(request)
            onResult(result)
        }
    }
}
