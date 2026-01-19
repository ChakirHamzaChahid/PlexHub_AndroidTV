package com.chakir.aggregatorhubplex.di

import com.chakir.aggregatorhubplex.data.repository.FavoriteRepository
import com.chakir.aggregatorhubplex.data.repository.FavoriteRepositoryImpl
import com.chakir.aggregatorhubplex.data.repository.MediaRepository
import com.chakir.aggregatorhubplex.data.repository.MediaRepositoryImpl
import com.chakir.aggregatorhubplex.data.repository.PlayHistoryRepository
import com.chakir.aggregatorhubplex.data.repository.PlayHistoryRepositoryImpl
import com.chakir.aggregatorhubplex.data.repository.ServerRepository
import com.chakir.aggregatorhubplex.data.repository.ServerRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module Dagger/Hilt pour lier les implémentations concrètes aux interfaces des repositories.
 * Utilise @Binds pour une déclaration plus efficace (pas de génération de code inutile).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /** Lie [MediaRepositoryImpl] à l'interface [MediaRepository]. */
    @Binds
    @Singleton
    abstract fun bindMediaRepository(mediaRepositoryImpl: MediaRepositoryImpl): MediaRepository

    /** Lie [ServerRepositoryImpl] à l'interface [ServerRepository]. */
    @Binds
    @Singleton
    abstract fun bindServerRepository(impl: ServerRepositoryImpl): ServerRepository

    /** Lie [PlayHistoryRepositoryImpl] à l'interface [PlayHistoryRepository]. */
    @Binds
    @Singleton
    abstract fun bindPlayHistoryRepository(impl: PlayHistoryRepositoryImpl): PlayHistoryRepository

    // --- AJOUT ESSENTIEL POUR LES FAVORIS ---
    /** Lie [FavoriteRepositoryImpl] à l'interface [FavoriteRepository]. */
    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): FavoriteRepository
}
