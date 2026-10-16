package com.gec.emergency.feature.stations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gec.emergency.domain.usecase.GetActiveAgenciesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AgencySelectorViewModel @Inject constructor(
    private val getActiveAgenciesUseCase: GetActiveAgenciesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgencySelectorUiState())
    val uiState: StateFlow<AgencySelectorUiState> = _uiState.asStateFlow()

    init {
        loadActiveAgencies()
    }

    fun loadActiveAgencies() {
        viewModelScope.launch {
            getActiveAgenciesUseCase()
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
                .collect { agencies ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        agencies = agencies,
                        isEmpty = agencies.isEmpty(),
                        errorMessage = null,
                    )
                }
        }
    }
}
