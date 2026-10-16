package com.gec.emergency.di

import android.content.Context
import com.gec.emergency.core.location.AndroidLocationProvider
import com.gec.emergency.core.location.LocationProvider
import com.gec.emergency.domain.service.DistanceCalculator
import com.gec.emergency.domain.service.HaversineDistanceCalculator
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    @Binds
    @Singleton
    abstract fun bindLocationProvider(
        androidLocationProvider: AndroidLocationProvider,
    ): LocationProvider

    @Binds
    @Singleton
    abstract fun bindDistanceCalculator(
        haversineDistanceCalculator: HaversineDistanceCalculator,
    ): DistanceCalculator

    companion object {
        @Provides
        @Singleton
        fun provideFusedLocationProviderClient(
            @ApplicationContext context: Context,
        ): FusedLocationProviderClient {
            return LocationServices.getFusedLocationProviderClient(context)
        }
    }
}
