package com.gec.emergency.di

import com.gec.emergency.core.calling.AndroidCallManager
import com.gec.emergency.core.calling.CallManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CallingModule {

    @Binds
    @Singleton
    abstract fun bindCallManager(
        androidCallManager: AndroidCallManager,
    ): CallManager
}
