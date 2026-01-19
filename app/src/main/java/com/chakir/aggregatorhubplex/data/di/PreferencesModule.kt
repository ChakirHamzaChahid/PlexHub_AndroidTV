package com.chakir.aggregatorhubplex.data.di

import android.content.Context
import com.chakir.aggregatorhubplex.data.preferences.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Module Dagger/Hilt fournissant les gérants de préférences (DataStore/SharedPreferences). */
@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    /** Fournit l'instance singleton de [PreferencesManager]. */
    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }
}
