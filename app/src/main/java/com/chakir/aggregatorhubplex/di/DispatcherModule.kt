package com.chakir.aggregatorhubplex.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Module Dagger pour fournir l'implémentation de [CoroutineDispatchers]. */
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides @Singleton fun provideDispatchers(): CoroutineDispatchers = AppCoroutineDispatchers()
}
