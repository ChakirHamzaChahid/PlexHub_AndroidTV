package com.chakir.aggregatorhubplex

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.chakir.aggregatorhubplex.workers.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Classe d'Application principale. Initialise Hilt pour l'injection de dépendances et configure le
 * WorkManager pour les tâches de fond.
 */
@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        setupPeriodicSync()
    }

    /**
     * Configure la synchronisation périodique des films (toutes les 6 heures). Utilise WorkManager
     * avec une politique de remplacement (KEEP) pour éviter les doublons.
     */
    private fun setupPeriodicSync() {
        val workManager = WorkManager.getInstance(this)

        val constraints =
                androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                        .build()

        val syncRequest =
                PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
                        .setConstraints(constraints)
                        .build()

        workManager.enqueueUniquePeriodicWork(
                "movie_sync_work",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
        )
    }

    /**
     * Configuration personnalisée du WorkManager pour utiliser Hilt (injection dans les Workers).
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()
}
