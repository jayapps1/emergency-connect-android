package com.gec.emergency.domain.usecase

import com.gec.emergency.domain.model.AppLanguage
import com.gec.emergency.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAppLanguageUseCase @Inject constructor(
    private val repository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<AppLanguage> = repository.observeLanguage()
}
