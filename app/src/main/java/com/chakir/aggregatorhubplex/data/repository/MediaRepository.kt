package com.chakir.aggregatorhubplex.data.repository

import androidx.paging.PagingData
import com.chakir.aggregatorhubplex.domain.model.Movie
import com.chakir.aggregatorhubplex.ui.screens.SortOption
import kotlinx.coroutines.flow.Flow

/**
 * Repository principal pour la gestion des médias (Films et Séries). Fournit les données paginées,
 * les détails, les top-rated et les fonctionnalités de filtrage.
 */
interface MediaRepository {
    /**
     * Récupère une liste paginée de médias.
     * @param search Terme de recherche optionnel.
     * @param type Filtre par type ("movie" ou "show").
     * @param genreLabel Filtre par genre/catégorie.
     * @param sort Option de tri.
     */
    fun getMediaPaged(
            search: String?,
            type: String?,
            genreLabel: String,
            sort: SortOption
    ): Flow<PagingData<Movie>>

    /** Compte le nombre de résultats pour un filtre donné. */
    fun getFilteredCount(search: String?, type: String?, genreLabel: String): Flow<Int>

    /** Récupère les items les mieux notés. */
    fun getTopRated(type: String?, limit: Int): Flow<List<Movie>>

    /**
     * Récupère les détails d'un film ou d'une série. Doit gérer la récupération locale puis la mise
     * à jour depuis le réseau.
     */
    fun getMovieDetail(movieId: String): Flow<Movie?>

    /** Liste tous les genres disponibles dans la base. */
    suspend fun getAvailableGenres(): List<String>

    /** Récupère la liste "Continuer la lecture". */
    fun getContinueWatching(): Flow<List<Movie>>

    /** Récupère l'historique de visionnage. */
    fun getWatchHistory(page: Int = 1, size: Int = 50): Flow<List<Movie>>

    /** Récupère les "Hubs" de découverte (Top Rated, etc.). */
    fun getHubs(): Flow<Map<String, List<Movie>>>

    /** Récupère les derniers ajouts. */
    fun getRecentlyAdded(limit: Int): Flow<List<Movie>>

    /** Recherche avancée. */
    fun search(
            title: String,
            year: Int? = null,
            unwatched: Boolean? = null,
            limit: Int = 50
    ): Flow<List<Movie>>

    /** Marquer comme vu/non vu. */
    suspend fun scrobbleMedia(id: String, action: String)

    /** Mettre à jour la progression. */
    suspend fun updateProgress(id: String, timeMs: Long)

    /** Basculer favori. */
    suspend fun toggleFavorite(id: String)

    /** Noter un média. */
    suspend fun rateMedia(id: String, rating: Float)
}
