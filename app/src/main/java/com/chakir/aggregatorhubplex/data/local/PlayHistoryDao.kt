package com.chakir.aggregatorhubplex.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Data Access Object (DAO) pour gérer l'historique de lecture local. */
@Dao
interface PlayHistoryDao {
    /** Met à jour ou insère une entrée dans l'historique de lecture. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateHistory(history: PlayHistoryEntity)

    /** Récupère l'historique de lecture pour un média spécifique. */
    @Query("SELECT * FROM play_history WHERE mediaId = :mediaId")
    suspend fun getHistoryItem(mediaId: String): PlayHistoryEntity?

    /** Récupère les 20 derniers éléments lus par l'utilisateur. */
    @Query("SELECT * FROM play_history ORDER BY lastPlayedAt DESC LIMIT 20")
    fun getRecentHistory(): Flow<List<PlayHistoryEntity>>

    /** Supprime une entrée de l'historique. */
    @Query("DELETE FROM play_history WHERE mediaId = :mediaId")
    suspend fun deleteHistoryItem(mediaId: String)
}
