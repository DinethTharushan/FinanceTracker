package com.app.financetracker.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
object TimeModule {

    /** Unscoped: each injection gets the system zone current at that moment. */
    @Provides
    fun provideClock(): Clock = Clock.systemDefaultZone()
}
