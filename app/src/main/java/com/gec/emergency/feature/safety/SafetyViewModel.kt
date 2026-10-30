package com.gec.emergency.feature.safety

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gec.emergency.domain.model.SafetyInformation
import com.gec.emergency.domain.repository.StationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SafetyUiState(
    val isLoading: Boolean = false,
    val safetyPosts: List<SafetyInformation> = emptyList(),
    val errorMessage: String? = null,
)

/**
 * Android distinguishes an empty feed from an API failure instead of
 * silently returning an empty list.
 */
@HiltViewModel
class SafetyViewModel @Inject constructor(
    private val stationRepository: StationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SafetyUiState())
    val uiState: StateFlow<SafetyUiState> = _uiState.asStateFlow()

    init {
        loadSafetyPosts()
    }

    fun loadSafetyPosts() {
        viewModelScope.launch {
            stationRepository.getPublicSafetyInformation()
                .onStart {
                    _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                }
                .catch { ex ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = ex.localizedMessage ?: "Unable to load safety information.",
                        safetyPosts = emptyList(),
                    )
                }
                .collect { posts ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        safetyPosts = posts,
                        errorMessage = null,
                    )
                }
        }
    }
}
