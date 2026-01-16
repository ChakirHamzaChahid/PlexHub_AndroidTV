package com.chakir.aggregatorhubplex.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Upsert
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    // 1. La requête principale pour l'UI (Filtres + Tris)
    @Query("""
        SELECT * FROM movies 
        WHERE (:query IS NULL OR title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        AND (:type IS NULL OR type = :type)
        ORDER BY 
            CASE WHEN :sort = 'added_at' AND :order = 'desc' THEN addedAt END DESC,
            CASE WHEN :sort = 'added_at' AND :order = 'asc' THEN addedAt END ASC,
            
            CASE WHEN :sort = 'title' AND :order = 'asc' THEN title END ASC,
            CASE WHEN :sort = 'title' AND :order = 'desc' THEN title END DESC,
            
            CASE WHEN :sort = 'year' AND :order = 'desc' THEN year END DESC,
            CASE WHEN :sort = 'year' AND :order = 'asc' THEN year END ASC,
            
            CASE WHEN :sort = 'rating' AND :order = 'desc' THEN COALESCE(imdbRating, rating) END DESC,
            CASE WHEN :sort = 'rating' AND :order = 'asc' THEN COALESCE(imdbRating, rating) END ASC,
            
            id DESC
    """)
    fun getMoviesPaged(
        query: String?,
        type: String?,
        sort: String,
        order: String
    ): PagingSource<Int, MovieEntity>

    // --- MODIFICATION ICI : On passe en @RawQuery ---
    // Cela permet d'envoyer une requête construite "à la main" avec tous nos OR pour les genres
    @RawQuery(observedEntities = [MovieEntity::class])
    fun getMoviesPagedRaw(query: SupportSQLiteQuery): PagingSource<Int, MovieEntity>


    // 2. Compteur Live
    @Query("SELECT COUNT(*) FROM movies")
    fun getCount(): Flow<Int>

    // 3. Insertion optimisée (Upsert remplace Insert+Replace)
    @Upsert
    suspend fun upsertAll(movies: List<MovieEntity>)

    // 4. Nettoyage total
    @Query("DELETE FROM movies")
    suspend fun clearAll()

    // 5. Utilitaires
    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: String): MovieEntity?
}