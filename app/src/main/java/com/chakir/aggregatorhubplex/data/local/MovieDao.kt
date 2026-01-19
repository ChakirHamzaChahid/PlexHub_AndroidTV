package com.chakir.aggregatorhubplex.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Upsert
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) principal pour l'accès aux films et séries. Gère la pagination, la
 * recherche plein texte (FTS) et les opérations CRUD de base.
 */
@Dao
interface MovieDao {
    // --- Requêtes pour l'UI ---

    /**
     * Récupère une source de pagination (PagingSource) pour les films, filtrée par une requête SQL
     * brute. Cette méthode est utilisée pour flexibiliser le tri et le filtrage dynamique.
     */
    @RawQuery(observedEntities = [MovieEntity::class])
    fun getMoviesPagedRaw(query: SupportSQLiteQuery): PagingSource<Int, MovieEntity>

    /**
     * Recherche des films via la table virtuelle FTS (Full Text Search).
     * @param query Le terme de recherche.
     */
    @Query(
            """
        SELECT * FROM movies
        JOIN movies_fts ON movies.rowid = movies_fts.rowid
        WHERE movies_fts MATCH :query
    """
    )
    fun searchMoviesFts(query: String): PagingSource<Int, MovieEntity>

    // --- Requêtes utilitaires ---

    /** Compte le nombre total de médias dans la base. */
    @Query("SELECT COUNT(*) FROM movies") fun getCount(): Flow<Int>

    /** Compte le nombre de médias correspondant à un filtre (requête brute). */
    @RawQuery(observedEntities = [MovieEntity::class])
    fun getFilteredCount(query: SupportSQLiteQuery): Flow<Int>

    /** Récupère un média spécifique par son ID. */
    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: String): MovieEntity?

    /**
     * NOUVEAU : Récupère les films les mieux notés pour le carrousel, avec un filtre de type. Trie
     * par note IMDb ou note générique si non disponible.
     */
    @Query(
            """
        SELECT * FROM movies
        WHERE (:type IS NULL OR type = :type)
        AND COALESCE(imdbRating, rating) IS NOT NULL
        ORDER BY COALESCE(imdbRating, rating) DESC
        LIMIT :limit
    """
    )
    fun getTopRated(type: String?, limit: Int): Flow<List<MovieEntity>>

    /** Récupère la liste de tous les genres disponibles (format JSON brut, doit être parsé). */
    @Query("SELECT genres FROM movies") suspend fun getAllGenres(): List<String>

    // --- Opérations d'écriture ---

    /**
     * Insère ou met à jour une liste de films. Si un film existe déjà, il est mis à jour (Upsert).
     */
    @Upsert suspend fun upsertAll(movies: List<MovieEntity>)

    /** Vide entièrement la table des films. */
    @Query("DELETE FROM movies") suspend fun clearAll()

    /**
     * Récupère les médias en cours de lecture pour la section "Continuer la lecture". Filtre les
     * éléments où la progression est significative (entre 5% et 95%).
     */
    @Query(
            """
        SELECT * FROM movies
        WHERE duration > 0 AND (viewOffset * 1.0 / duration) BETWEEN 0.05 AND 0.95
        ORDER BY addedAt DESC
        LIMIT 10
    """
    )
    fun getContinueWatching(): Flow<List<MovieEntity>>
}
