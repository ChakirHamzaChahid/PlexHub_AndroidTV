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
    // --- Requêtes pour l'UI --- 

    @RawQuery(observedEntities = [MovieEntity::class])
    fun getMoviesPagedRaw(query: SupportSQLiteQuery): PagingSource<Int, MovieEntity>

    @Query("""
        SELECT * FROM movies
        JOIN movies_fts ON movies.rowid = movies_fts.rowid
        WHERE movies_fts MATCH :query
    """)
    fun searchMoviesFts(query: String): PagingSource<Int, MovieEntity>

    // --- Requêtes utilitaires ---

    @Query("SELECT COUNT(*) FROM movies")
    fun getCount(): Flow<Int>

    @RawQuery(observedEntities = [MovieEntity::class])
    fun getFilteredCount(query: SupportSQLiteQuery): Flow<Int>

    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: String): MovieEntity?

    /**
     * NOUVEAU : Récupère les films les mieux notés pour le carrousel, avec un filtre de type.
     */
    @Query("""
        SELECT * FROM movies
        WHERE (:type IS NULL OR type = :type)
        AND COALESCE(imdbRating, rating) IS NOT NULL
        ORDER BY COALESCE(imdbRating, rating) DESC
        LIMIT :limit
    """)
    fun getTopRated(type: String?, limit: Int): Flow<List<MovieEntity>>

    @Query("SELECT genres FROM movies")
    suspend fun getAllGenres(): List<String>

    // --- Opérations d'écriture ---

    @Upsert
    suspend fun upsertAll(movies: List<MovieEntity>)

    @Query("DELETE FROM movies")
    suspend fun clearAll()
}
