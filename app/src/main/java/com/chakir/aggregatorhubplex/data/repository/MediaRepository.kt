package com.chakir.aggregatorhubplex.data.repository

import androidx.paging.PagingData
import com.chakir.aggregatorhubplex.data.Movie
import com.chakir.aggregatorhubplex.ui.screens.SortOption
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    // Récupère les films/séries pour l'affichage en grille, en utilisant le cache local (Room).
    fun getMediaPaged(
        search: String?,
        type: String?,
        genreLabel: String,
        sort: SortOption
    ): Flow<PagingData<Movie>>

    // Récupère le nombre d'éléments en fonction des filtres actifs.
    fun getFilteredCount(
        search: String?,
        type: String?,
        genreLabel: String
    ): Flow<Int>

    /**
     * Récupère les films les mieux notés pour le carrousel en page d'accueil.
     */
    fun getTopRated(type: String?, limit: Int): Flow<List<Movie>>

    /**
     * Récupère les détails d'un film en utilisant une stratégie "cache-then-network".
     */
    fun getMovieDetail(movieId: String): Flow<Movie?>

    suspend fun getAvailableGenres(): List<String>
}
