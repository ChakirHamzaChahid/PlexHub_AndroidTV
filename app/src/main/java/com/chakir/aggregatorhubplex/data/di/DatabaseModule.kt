// Fichier: app/src/main/java/com/chakir/aggregatorhubplex/di/DatabaseModule.kt
package com.chakir.aggregatorhubplex.di

import android.content.Context
import com.chakir.aggregatorhubplex.data.local.AppDatabase
import com.chakir.aggregatorhubplex.data.local.MovieDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideMovieDao(database: AppDatabase): MovieDao {
        return database.movieDao()
    }
}