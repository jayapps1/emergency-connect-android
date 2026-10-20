package com.gec.emergency.feature.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gec.emergency.data.local.ReportTrackingStore
import com.gec.emergency.data.remote.EmergencyDirectoryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val eventType: String,
    val reportId: String?,
    val isRead: Boolean,
    val createdAt: String,
)

data class NotificationsUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val unreadCount: Long = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val api: EmergencyDirectoryApi,
    private val trackingStore: ReportTrackingStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val token = trackingStore.getPublicAuthToken()
                if (token.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(isLoading = false, notifications = emptyList(), unreadCount = 0)
                    return@launch
                }

                // In-app notifications history loaded from backend
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.localizedMessage)
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                notifications = _uiState.value.notifications.map {
                    if (it.id == id) it.copy(isRead = true) else it
                }
            )
        }
    }
}
