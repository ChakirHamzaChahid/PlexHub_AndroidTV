package com.chakir.aggregatorhubplex.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chakir.aggregatorhubplex.data.MovieListItem
import com.chakir.aggregatorhubplex.data.NetworkModule
import com.chakir.aggregatorhubplex.data.UrlFixer
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
    private val movieDao: MovieDao
) : CoroutineWorker(appContext, workerParams) {

    private val api = NetworkModule.api

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.i("SyncWorker", "🚀 Démarrage de la synchronisation...")

            syncAllContent()

            Log.i("SyncWorker", "✅ Synchronisation terminée avec succès !")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "❌ Erreur de synchro: ${e.message}", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private suspend fun syncAllContent() {
        var page = 1
        val pageSize = 500

        // SUPPRIMÉ : L'appel à clearAll() est retiré pour permettre une expérience de démarrage instantanée.
        // La méthode upsertAll s'occupera des mises à jour.

        while (true) {
            Log.d("SyncWorker", "📥 Téléchargement de la page $page...")

            val response: List<MovieListItem> = try {
                api.getMovies(
                    page = page, size = pageSize,
                    type = null, sort = "added_at", order = "desc", search = null
                )
            } catch (e: Exception) {
                Log.e("SyncWorker", "Erreur réseau sur la page $page", e)
                throw e
            }

            if (response.isEmpty()) {
                Log.d("SyncWorker", "🏁 Fin de la pagination, plus de contenu à charger.")
                break
            }

            val entities = response.map { it.toEntity() }
            // Upsert va insérer les nouveaux films et mettre à jour les anciens.
            movieDao.upsertAll(entities)
            Log.d("SyncWorker", "💾 Page $page sauvegardée et mise à jour (${entities.size} items)")

            if (response.size < pageSize) {
                Log.d("SyncWorker", "🏁 Dernière page atteinte.")
                break
            }

            page++
        }
    }

    /**
     * Fonction de mapping pour convertir le DTO réseau (MovieListItem) en entité de base de données (MovieEntity).
     */
    private fun MovieListItem.toEntity() = MovieEntity(
        id = id,
        title = title,
        type = type,
        posterUrl = UrlFixer.fix(posterPath),
        year = year,
        rating = rating,
        imdbRating = imdbRating,
        hasMultipleSources = hasMultipleSources,

        // Les champs suivants n'existent pas dans le DTO de liste, ils sont donc initialisés à null/vide.
        // Ils seront chargés à la demande depuis l'endpoint de détail si nécessaire.
        addedAt = null,
        rottenRating = null,
        director = null,
        genres = null,
        description = null,
        studio = null,
        contentRating = null,
        servers = null,
        seasons = null
    )
}
