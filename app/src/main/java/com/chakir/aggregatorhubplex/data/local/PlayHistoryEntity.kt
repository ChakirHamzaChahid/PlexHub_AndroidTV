package com.chakir.aggregatorhubplex.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room représentant une entrée dans l'historique de lecture. Stocke la position de lecture,
 * la durée et si le média est considéré comme fini.
 */
@Entity(tableName = "play_history")
data class PlayHistoryEntity(
        @PrimaryKey val mediaId: String,
        val title: String,
        val posterUrl: String?,
        val lastPlayedAt: Long = System.currentTimeMillis(),
        val positionMs: Long = 0, // Position arrêtée en ms
        val durationMs: Long = 0, // Durée totale
        val isFinished: Boolean = false // Si > 90%
)
