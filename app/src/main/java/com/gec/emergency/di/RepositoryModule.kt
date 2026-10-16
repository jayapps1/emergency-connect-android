package com.gec.emergency.di

import com.gec.emergency.data.repository.AgencyRepositoryImpl
import com.gec.emergency.data.repository.OfflineEmergencyDirectoryRepositoryImpl
import com.gec.emergency.data.repository.RemoteIncidentReportRepository
import com.gec.emergency.data.repository.StationRepositoryImpl
import com.gec.emergency.data.repository.UserPreferencesRepositoryImpl
import com.gec.emergency.domain.repository.AgencyRepository
import com.gec.emergency.domain.repository.IncidentReportRepository
import com.gec.emergency.domain.repository.OfflineEmergencyDirectoryRepository
import com.gec.emergency.domain.repository.StationRepository
import com.gec.emergency.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAgencyRepository(
        agencyRepositoryImpl: AgencyRepositoryImpl,
    ): AgencyRepository

    @Binds
    @Singleton
    abstract fun bindStationRepository(
        stationRepositoryImpl: StationRepositoryImpl,
    ): StationRepository

    @Binds
    @Singleton
    abstract fun bindOfflineEmergencyDirectoryRepository(
        impl: OfflineEmergencyDirectoryRepositoryImpl,
    ): OfflineEmergencyDirectoryRepository

    @Binds
    @Singleton
    abstract fun bindIncidentReportRepository(
        impl: RemoteIncidentReportRepository,
    ): IncidentReportRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl,
    ): UserPreferencesRepository
}
