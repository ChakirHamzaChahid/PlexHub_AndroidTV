package com.chakir.aggregatorhubplex.data.repository

import com.chakir.aggregatorhubplex.data.local.PlayHistoryDao
import com.chakir.aggregatorhubplex.data.local.PlayHistoryEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/** Repository pour gérer l'historique de lecture (reprise de lecture). */
interface PlayHistoryRepository {
    /** Sauvegarde la position de lecture courante. */
    suspend fun savePlaybackPosition(
            mediaId: String,
            title: String,
            posterUrl: String?,
            positionMs: Long,
            durationMs: Long
    )

    /**
     * Récupère la dernière position de lecture sauvegardée pour un média. Si le média est marqué
     * comme "fini", retourne 0.
     */
    suspend fun getPlaybackPosition(mediaId: String): Long

    /** Récupère la liste des médias en cours de lecture pour l'écran d'accueil. */
    fun getContinueWatchingList(): Flow<List<PlayHistoryEntity>>
}

/**
 * Implémentation concrète de [PlayHistoryRepository]. Contient la logique métier pour déterminer si
 * un contenu est "fini" (>90% de visionnage).
 */
@Singleton
class PlayHistoryRepositoryImpl @Inject constructor(private val historyDao: PlayHistoryDao) :
        PlayHistoryRepository {

    override suspend fun savePlaybackPosition(
            mediaId: String,
            title: String,
            posterUrl: String?,
            positionMs: Long,
            durationMs: Long
    ) {
        // Règle métier : Si on a regardé moins de 10 secondes, on ne sauvegarde pas (évite le
        // bruit)
        if (positionMs < 10_000) return

        // Règle métier : Si on a regardé plus de 90% du contenu, on considère comme "Vu"
        // (IsFinished)
        val progress = if (durationMs > 0) positionMs.toFloat() / durationMs.toFloat() else 0f
        val isFinished = progress > 0.90f

        // Si fini, on remet la position à 0 (pour revoir du début) ou on le marque juste comme vu.
        // Ici, on sauvegarde l'état actuel.
        val entity =
                PlayHistoryEntity(
                        mediaId = mediaId,
                        title = title,
                        posterUrl = posterUrl,
                        lastPlayedAt = System.currentTimeMillis(),
                        positionMs = positionMs,
                        durationMs = durationMs,
                        isFinished = isFinished
                )
        historyDao.updateHistory(entity)
    }

    override suspend fun getPlaybackPosition(mediaId: String): Long {
        val history = historyDao.getHistoryItem(mediaId)
        // Si l'item est marqué comme "fini", on recommence au début (0L)
        if (history != null && history.isFinished) return 0L
        return history?.positionMs ?: 0L
    }

    override fun getContinueWatchingList(): Flow<List<PlayHistoryEntity>> {
        return historyDao.getRecentHistory()
    }
}
