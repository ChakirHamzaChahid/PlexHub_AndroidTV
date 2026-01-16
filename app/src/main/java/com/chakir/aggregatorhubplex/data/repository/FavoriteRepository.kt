package com.chakir.aggregatorhubplex.data.repository

import com.chakir.aggregatorhubplex.data.Movie
import com.chakir.aggregatorhubplex.data.local.FavoriteDao
import com.chakir.aggregatorhubplex.data.local.FavoriteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// 1. L'interface (Le contrat)
interface FavoriteRepository {
    fun getAllFavorites(): Flow<List<FavoriteEntity>>
    fun isFavorite(mediaId: String): Flow<Boolean>
    suspend fun addToFavorites(movie: Movie)
    suspend fun removeFromFavorites(mediaId: String)
}

// 2. L'implémentation (La logique réelle)
@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao
) : FavoriteRepository {

    override fun getAllFavorites(): Flow<List<FavoriteEntity>> {
        return favoriteDao.getAllFavorites()
    }

    override fun isFavorite(mediaId: String): Flow<Boolean> {
        return favoriteDao.isFavorite(mediaId)
    }

    override suspend fun addToFavorites(movie: Movie) {
        val entity = FavoriteEntity(
            mediaId = movie.id,
            type = if (movie.isSeries) "show" else "movie",
            title = movie.title,
            posterUrl = movie.posterUrl,
            addedAt = System.currentTimeMillis()
        )
        favoriteDao.addToFavorites(entity)
    }

    override suspend fun removeFromFavorites(mediaId: String) {
        favoriteDao.removeFromFavorites(mediaId)
    }
}