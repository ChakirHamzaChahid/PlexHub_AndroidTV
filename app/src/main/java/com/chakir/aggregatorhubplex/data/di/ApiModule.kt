package com.chakir.aggregatorhubplex.data.di

import com.chakir.aggregatorhubplex.data.MovieApiService
import com.chakir.aggregatorhubplex.data.NetworkModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideMovieApiService(): MovieApiService {
        return NetworkModule.api
    }
}
