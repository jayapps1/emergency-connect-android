package com.gec.emergency.domain.usecase

import com.gec.emergency.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetVoiceGuidanceUseCase @Inject constructor(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setVoiceGuidanceEnabled(enabled)
}
