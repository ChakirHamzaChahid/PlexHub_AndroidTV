package com.chakir.aggregatorhubplex.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateHistory(history: PlayHistoryEntity)

    @Query("SELECT * FROM play_history WHERE mediaId = :mediaId")
    suspend fun getHistoryItem(mediaId: String): PlayHistoryEntity?

    @Query("SELECT * FROM play_history ORDER BY lastPlayedAt DESC LIMIT 20")
    fun getRecentHistory(): Flow<List<PlayHistoryEntity>>

    @Query("DELETE FROM play_history WHERE mediaId = :mediaId")
    suspend fun deleteHistoryItem(mediaId: String)
}