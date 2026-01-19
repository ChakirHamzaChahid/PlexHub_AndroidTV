package com.chakir.aggregatorhubplex.di

import android.content.Context
import com.chakir.aggregatorhubplex.data.local.AppDatabase
import com.chakir.aggregatorhubplex.data.local.FavoriteDao
import com.chakir.aggregatorhubplex.data.local.MovieDao
import com.chakir.aggregatorhubplex.data.local.PlayHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Module Dagger/Hilt fournissant les dépendances liées à la base de données Room. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /** Fournit l'instance unique de la base de données [AppDatabase]. */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    /** Fournit le DAO pour les films et séries [MovieDao]. */
    @Provides
    fun provideMovieDao(database: AppDatabase): MovieDao {
        return database.movieDao()
    }

    // --- AJOUTS ---

    /** Fournit le DAO pour les favoris [FavoriteDao]. */
    @Provides
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao {
        return database.favoriteDao()
    }

    /** Fournit le DAO pour l'historique de lecture [PlayHistoryDao]. */
    @Provides
    fun providePlayHistoryDao(database: AppDatabase): PlayHistoryDao {
        return database.playHistoryDao()
    }
}
