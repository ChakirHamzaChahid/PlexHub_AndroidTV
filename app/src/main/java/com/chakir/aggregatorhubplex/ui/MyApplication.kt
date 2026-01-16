package com.chakir.aggregatorhubplex

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.chakir.aggregatorhubplex.data.NetworkModule
import com.chakir.aggregatorhubplex.data.preferences.PreferencesManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var prefsManager: PreferencesManager // On injecte le gestionnaire de préférences

    override fun onCreate() {
        super.onCreate()

        // --- CORRECTION CRITIQUE ---
        // On force la configuration de l'URL réseau AVANT tout le reste.
        // runBlocking est acceptable ici car c'est une lecture rapide DataStore au boot
        // et c'est vital pour que les Workers fonctionnent.
        try {
            runBlocking {
                val savedUrl = prefsManager.serverUrl.first()
                if (!savedUrl.isNullOrEmpty()) {
                    NetworkModule.updateBaseUrl(savedUrl)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}