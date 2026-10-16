package com.gec.emergency.di

import com.gec.emergency.core.accessibility.AndroidVoiceGuidanceManager
import com.gec.emergency.core.accessibility.VoiceGuidanceManager
import com.gec.emergency.core.common.AndroidAnonymousSessionProvider
import com.gec.emergency.core.common.AnonymousSessionProvider
import com.gec.emergency.core.common.AppInfoProvider
import com.gec.emergency.core.common.DefaultAppInfoProvider
import com.gec.emergency.domain.service.DefaultIncidentRoutingService
import com.gec.emergency.domain.service.IncidentRoutingService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAppInfoProvider(
        defaultAppInfoProvider: DefaultAppInfoProvider,
    ): AppInfoProvider

    @Binds
    @Singleton
    abstract fun bindAnonymousSessionProvider(
        provider: AndroidAnonymousSessionProvider,
    ): AnonymousSessionProvider

    @Binds
    @Singleton
    abstract fun bindIncidentRoutingService(
        service: DefaultIncidentRoutingService,
    ): IncidentRoutingService

    @Binds
    @Singleton
    abstract fun bindVoiceGuidanceManager(
        manager: AndroidVoiceGuidanceManager,
    ): VoiceGuidanceManager
}
