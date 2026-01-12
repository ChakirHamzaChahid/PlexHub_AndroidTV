package com.chakir.aggregatorhubplex.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chakir.aggregatorhubplex.data.Movie
import com.chakir.aggregatorhubplex.data.NetworkModule
import com.chakir.aggregatorhubplex.data.local.AppDatabase
import com.chakir.aggregatorhubplex.data.local.MovieEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    // On récupère la DB via le Singleton (ou Injection si Hilt est configuré pour le DAO)
    private val database = AppDatabase.getDatabase(context)
    private val api = NetworkModule.api

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.i("SyncWorker", "🚀 Démarrage de la synchronisation (Offline-First)...")

            // On lance la synchro globale (Films + Séries mélangés par date)
            syncAllContent()

            Log.i("SyncWorker", "✅ Synchronisation terminée avec succès !")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "❌ Erreur de synchro: ${e.message}")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private suspend fun syncAllContent() {
        var page = 1
        // Taille de page augmentée pour profiter de la vitesse du nouveau backend
        val pageSize = 500
        var hasMore = true

        while (hasMore) {
            Log.d("SyncWorker", "📥 Téléchargement page $page...")

            // Appel API : type = null pour tout récupérer
            val response = api.getMovies(
                page = page,
                size = pageSize,
                type = null, // On veut tout (Films ET Séries)
                sort = "added_at",
                order = "desc",
                search = null
            )

            if (response.isEmpty()) {
                hasMore = false
            } else {
                // Conversion DTO (Réseau) -> Entity (Base de données)
                val entities = response.map { it.toEntity() }

                // Sauvegarde en base (Utilisation de Upsert pour éviter les doublons)
                database.movieDao().upsertAll(entities)

                Log.d("SyncWorker", "💾 Page $page sauvegardée (${entities.size} items)")
                page++
            }
        }
    }

    // Fonction de mapping (Adaptée à votre MovieEntity)
    private fun Movie.toEntity() = MovieEntity(
        id = id,
        title = title,
        type = type,
        posterUrl = posterUrl,
        year = year,
        addedAt = addedAt,
        rating = rating,
        imdbRating = imdbRating,
        rottenRating = rottenRating,
        director = director,
        genres = genres,
        description = description,
        studio = studio,
        contentRating = contentRating,
        // Les listes complexes (servers, seasons) sont gérées par les TypeConverters de Room
        servers = servers,
        seasons = seasons,
        hasMultipleSources = (servers?.size ?: 0) > 1
    )
}