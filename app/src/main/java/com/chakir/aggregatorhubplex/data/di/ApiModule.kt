package com.chakir.aggregatorhubplex.data.di

import com.chakir.aggregatorhubplex.data.PlexService
import com.chakir.aggregatorhubplex.data.network.MovieApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module Dagger/Hilt fournissant les dépendances liées à l'API réseau. Installé dans le
 * SingletonComponent pour que les instances soient uniques au niveau de l'application.
 */
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    /**
     * Fournit l'instance de l'interface Retrofit [MovieApiService]. Cette instance est utilisée
     * pour effectuer les appels réseau.
     */
    @Provides
    @Singleton
    fun provideMovieApiService(plexService: PlexService): MovieApiService {
        return plexService.api
    }

    /** Fournit l'instance de [PlexService] qui configure le client HTTP et Retrofit. */
    @Provides
    @Singleton
    fun providePlexService(): PlexService {
        return PlexService.create()
    }
}
