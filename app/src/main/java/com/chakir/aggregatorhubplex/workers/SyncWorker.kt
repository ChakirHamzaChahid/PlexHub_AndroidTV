package com.chakir.aggregatorhubplex.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chakir.aggregatorhubplex.data.dto.MovieListItem
import com.chakir.aggregatorhubplex.data.local.MovieDao
import com.chakir.aggregatorhubplex.data.local.MovieEntity
import com.chakir.aggregatorhubplex.data.network.MovieApiService
import com.chakir.aggregatorhubplex.domain.model.AudioTrack
import com.chakir.aggregatorhubplex.domain.model.Chapter
import com.chakir.aggregatorhubplex.domain.model.Marker
import com.chakir.aggregatorhubplex.domain.model.Server
import com.chakir.aggregatorhubplex.domain.model.Subtitle
import com.chakir.aggregatorhubplex.util.UrlFixer
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker en arrière-plan responsable de la synchronisation des films et séries depuis l'API vers la
 * base de données locale. Utilise Hilt pour l'injection de dépendances.
 */
@HiltWorker
class SyncWorker
@AssistedInject
constructor(
        @Assisted appContext: Context,
        @Assisted workerParams: WorkerParameters,
        private val movieDao: MovieDao,
        private val apiService: MovieApiService
) : CoroutineWorker(appContext, workerParams) {

    /**
     * Point d'entrée principal du Worker. Exécuté sur un thread d'E/S. Tente de synchroniser tout
     * le contenu. En cas d'échec, demande une nouvelle tentative (jusqu'à 3 fois).
     */
    override suspend fun doWork(): Result =
            withContext(Dispatchers.IO) {
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

    /**
     * Parcourt toutes les pages de résultats de l'API et met à jour la base de données locale.
     * Continue jusqu'à ce que la réponse de l'API soit vide ou contienne moins d'éléments que la
     * taille de page.
     */
    private suspend fun syncAllContent() {
        var page = 1
        val pageSize = 500

        while (true) {
            Log.d("SyncWorker", "📥 Téléchargement de la page $page...")

            val response: List<MovieListItem> =
                    try {
                        apiService.getMovies(
                                page = page,
                                size = pageSize,
                                type = null,
                                sort = "added_at",
                                order = "desc",
                                search = null
                        )
                    } catch (e: Exception) {
                        Log.e("SyncWorker", "Erreur réseau sur la page $page", e)
                        throw e
                    }

            if (response.isEmpty()) {
                Log.d("SyncWorker", "🏁 Fin de la pagination, plus de contenu à charger.")
                break
            }

            Log.d("SyncWorker", "✅ Réponse brute du backend - ${response.size} items reçus")

            // Affiche les données complètes des 3 premiers items
            response.take(3).forEachIndexed { index, item ->
                Log.d(
                        "SyncWorker",
                        """
                    📋 Item #$index du backend (COMPLET):
                    - ID: ${item.id}
                    - Titre: ${item.title}
                    - Type: ${item.type}
                    - Année: ${item.year}
                    - Durée: ${item.duration?.let { it / 60000 } ?: "N/A"} min
                    - Rating: ${item.rating}
                    - IMDb Rating: ${item.imdbRating}
                    - Rotten Rating: ${item.rottenRating}
                    - Genres: ${item.genres}
                    - Director: ${item.director}
                    - Studio: ${item.studio}
                    - Content Rating: ${item.contentRating}
                    - Badges: ${item.badges}
                    - Summary: ${item.summary?.take(100)}...
                    - Added At: ${item.addedAt}
                    - Multiple Sources: ${item.hasMultipleSources}
                    - View Offset: ${item.viewOffset}
                    - View Count: ${item.viewCount}
                    - Audio Tracks: ${item.audioTracks?.size ?: 0}
                    - Subtitles: ${item.subtitles?.size ?: 0}
                    - Chapters: ${item.chapters?.size ?: 0}
                    - Markers: ${item.markers?.size ?: 0}
                    - Episodes: ${item.episodes?.size ?: 0}
                    - Sources: ${item.sources?.size ?: 0}
                    - Poster Path (brut): ${item.posterPath}
                    - Backdrop Path (brut): ${item.backdropPath}
                """.trimIndent()
                )
            }

            val entities =
                    response.map { item ->
                        val entity = item.toEntity()
                        entity
                    }
            movieDao.upsertAll(entities)
            Log.i(
                    "SyncWorker",
                    "💾 Page $page sauvegardée (${entities.size} items). Total: ${entities.size * (page - 1) + entities.size}"
            )

            if (response.size < pageSize) {
                Log.d("SyncWorker", "🏁 Dernière page atteinte.")
                break
            }

            page++
        }
    }

    /**
     * Convertit un objet DTO [MovieListItem] en entité de base de données [MovieEntity]. Prend en
     * charge la transformation des listes complexes (pistes audio, chapitres, etc.) et la
     * correction des URLs.
     */
    private fun MovieListItem.toEntity() =
            MovieEntity(
                    id = id,
                    title = title,
                    type = type,
                    posterUrl = UrlFixer.fix(posterPath),
                    backdrop_url = UrlFixer.fix(backdropPath),
                    year = year,
                    rating = rating,
                    imdbRating = imdbRating,
                    genres = genres,
                    addedAt = addedAt,
                    hasMultipleSources = hasMultipleSources,
                    rottenRating = rottenRating,
                    director = director,
                    description = summary,
                    studio = studio,
                    contentRating = contentRating,
                    badges = badges,
                    audioTracks =
                            audioTracks?.map {
                                AudioTrack(
                                        displayTitle = it.displayTitle ?: "Unknown",
                                        language = it.language ?: "und",
                                        codec = it.codec ?: "unknown",
                                        channels = it.channels ?: 2
                                )
                            },
                    subtitles =
                            subtitles?.map {
                                Subtitle(
                                        displayTitle = it.displayTitle ?: "Unknown",
                                        language = it.language ?: "und",
                                        codec = it.codec ?: "unknown"
                                )
                            },
                    chapters =
                            chapters?.map {
                                Chapter(
                                        title = it.title ?: "Chapter",
                                        startTime = it.startTime?.toInt() ?: 0,
                                        endTime = it.endTime?.toInt() ?: 0,
                                        thumbUrl = it.thumb?.let { t -> UrlFixer.fix(t) } ?: ""
                                )
                            },
                    markers =
                            markers?.map {
                                Marker(
                                        title = it.title ?: "Marker",
                                        type = it.type ?: "unknown",
                                        startTime = it.startTime?.toInt() ?: 0,
                                        endTime = it.endTime?.toInt() ?: 0
                                )
                            },
                    viewOffset = viewOffset ?: 0L,
                    viewCount = viewCount ?: 0,
                    runtime = duration?.toInt() ?: 0,
                    duration = duration ?: 0L,
                    servers =
                            sources?.map { s ->
                                Server(
                                        name = s.serverName ?: "Inconnu",
                                        url = s.streamUrl ?: "",
                                        rawM3uUrl = s.m3uUrl,
                                        resolution = s.resolution ?: "SD",
                                        plexDeepLink = s.plexDeepLink,
                                        plexWebUrl = s.plexWebUrl
                                )
                            },
                    seasons = null
            )
}
