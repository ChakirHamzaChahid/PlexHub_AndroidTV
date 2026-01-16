package com.chakir.aggregatorhubplex

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.chakir.aggregatorhubplex.data.NetworkModule
import com.chakir.aggregatorhubplex.data.preferences.PreferencesManager
import com.chakir.aggregatorhubplex.workers.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var prefsManager: PreferencesManager

    // L'injection de WorkManager se fait via le contexte, pas besoin d'@Inject ici.

    override fun onCreate() {
        super.onCreate()

        // Configuration de l'URL réseau au démarrage
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

        // Lancement du travail de synchronisation périodique
        setupPeriodicSync()
    }

    private fun setupPeriodicSync() {
        val workManager = WorkManager.getInstance(this)

        // On définit les contraintes (ex: réseau disponible)
        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
            .build()

        // On crée la requête de travail périodique (toutes les 6 heures)
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            6, TimeUnit.HOURS
        )
        .setConstraints(constraints)
        .build()

        // On met en file d'attente le travail unique pour éviter les doublons.
        // KEEP = si un travail est déjà planifié, on le garde et on n'en ajoute pas un nouveau.
        workManager.enqueueUniquePeriodicWork(
            "movie_sync_work",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
