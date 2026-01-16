package com.chakir.aggregatorhubplex.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chakir.aggregatorhubplex.data.Movie
import com.chakir.aggregatorhubplex.data.NetworkModule
import com.chakir.aggregatorhubplex.data.local.MovieDao
import com.chakir.aggregatorhubplex.data.local.MovieEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    // --- CORRECTION : On injecte le DAO directement via Hilt ---
    private val movieDao: MovieDao
) : CoroutineWorker(appContext, workerParams) {

    // On utilise l'objet singleton NetworkModule pour l'API (c'est acceptable ici)
    private val api = NetworkModule.api

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // Dispatchers.IO est CRUCIAL ici pour éviter de figer l'interface (ANR)
        return@withContext try {
            Log.i("SyncWorker", "🚀 Démarrage de la synchronisation (Offline-First)...")

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
        val pageSize = 500 // On charge par gros blocs pour aller vite
        var hasMore = true

        while (hasMore) {
            Log.d("SyncWorker", "📥 Téléchargement page $page...")

            val response = try {
                api.getMovies(
                    page = page,
                    size = pageSize,
                    type = null, // Tout récupérer
                    sort = "added_at",
                    order = "desc",
                    search = null
                )
            } catch (e: Exception) {
                // Si une page plante, on arrête la boucle proprement
                Log.e("SyncWorker", "Erreur réseau page $page", e)
                throw e
            }

            if (response.isEmpty()) {
                hasMore = false
            } else {
                // Mapping
                val entities = response.map { it.toEntity() }

                // Sauvegarde via le DAO injecté
                movieDao.upsertAll(entities)

                Log.d("SyncWorker", "💾 Page $page sauvegardée (${entities.size} items)")
                page++
            }
        }
    }

    // Fonction de mapping
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
        servers = servers,
        seasons = seasons,
        // Calcul intelligent : s'il y a plus d'1 serveur, c'est du Multi-sources
        hasMultipleSources = (servers?.size ?: 0) > 1
    )
}