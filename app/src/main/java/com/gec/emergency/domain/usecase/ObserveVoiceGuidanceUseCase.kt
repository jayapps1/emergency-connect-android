package com.gec.emergency.domain.usecase

import com.gec.emergency.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveVoiceGuidanceUseCase @Inject constructor(
    private val repository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeVoiceGuidanceEnabled()
}
