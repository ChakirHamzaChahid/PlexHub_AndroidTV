package com.chakir.aggregatorhubplex.data.repository

import androidx.paging.PagingData
import com.chakir.aggregatorhubplex.data.Movie
import com.chakir.aggregatorhubplex.ui.screens.SortOption
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    // Récupère les films/séries avec pagination et filtres
    fun getMediaPaged(
        search: String?,
        type: String?,
        genreLabel: String,
        sort: SortOption
    ): Flow<PagingData<Movie>>

    // Récupère le nombre total d'éléments
    fun getTotalCount(): Flow<Int>
}