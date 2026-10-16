package com.gec.emergency.domain.usecase

import com.gec.emergency.domain.model.AppThemeMode
import com.gec.emergency.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetThemeModeUseCase @Inject constructor(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(mode: AppThemeMode) = repository.setThemeMode(mode)
}
