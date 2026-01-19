package com.chakir.aggregatorhubplex.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Data Access Object (DAO) pour gérer les favoris des utilisateurs. */
@Dao
interface FavoriteDao {
    /** Ajoute un élément aux favoris. Si l'élément existe déjà, il est remplacé. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFavorites(favorite: FavoriteEntity)

    /** Supprime un élément des favoris via son ID. */
    @Query("DELETE FROM favorites WHERE mediaId = :mediaId")
    suspend fun removeFromFavorites(mediaId: String)

    /** Vérifie si un média est marqué comme favori (retourne un Flow pour observation réactive). */
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE mediaId = :mediaId)")
    fun isFavorite(mediaId: String): Flow<Boolean>

    /** Récupère la liste de tous les favoris, triés par date d'ajout décroissante. */
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>
}
