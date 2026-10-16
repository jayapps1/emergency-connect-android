package com.gec.emergency.domain.usecase

import com.gec.emergency.domain.model.AppLanguage
import com.gec.emergency.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetAppLanguageUseCase @Inject constructor(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(language: AppLanguage) = repository.setLanguage(language)
}
