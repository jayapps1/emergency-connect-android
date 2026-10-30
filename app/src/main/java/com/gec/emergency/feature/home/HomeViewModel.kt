package com.gec.emergency.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gec.emergency.core.calling.CallManager
import com.gec.emergency.core.calling.CallRequest
import com.gec.emergency.core.calling.CallResult
import com.gec.emergency.core.calling.CallType
import com.gec.emergency.core.common.AppInfoProvider
import com.gec.emergency.core.common.EmergencyNumberConfig
import com.gec.emergency.domain.model.SafetyInformation
import com.gec.emergency.domain.repository.StationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val appName: String = "",
    val safetyPosts: List<SafetyInformation> = emptyList(),
    val activeSafetyAlert: SafetyInformation? = null,
    val unreadNotificationCount: Long = 0L,
)

/**
 * The notification badge reflects persisted unread notifications rather
 * than the number of items currently rendered on screen.
 * Safety alerts use the existing public notification pipeline and
 * deep-link to the authoritative Safety Information record.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    appInfoProvider: AppInfoProvider,
    private val callManager: CallManager,
    private val stationRepository: StationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(appName = appInfoProvider.getAppName()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refreshDashboardData()
    }

    fun refreshDashboardData() {
        loadSafetyPosts()
        loadUnreadNotificationCount()
    }

    private fun loadSafetyPosts() {
        viewModelScope.launch {
            stationRepository.getPublicSafetyInformation()
                .catch { /* ignore error */ }
                .collect { posts ->
                    val topAlert = posts.firstOrNull { it.severity.equals("CRITICAL", ignoreCase = true) || it.severity.equals("WARNING", ignoreCase = true) }
                        ?: posts.firstOrNull()
                    _uiState.value = _uiState.value.copy(
                        safetyPosts = posts.take(5),
                        activeSafetyAlert = topAlert,
                    )
                }
        }
    }

    private fun loadUnreadNotificationCount() {
        viewModelScope.launch {
            val count = stationRepository.getUnreadNotificationCount()
            _uiState.value = _uiState.value.copy(unreadNotificationCount = count)
        }
    }

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
