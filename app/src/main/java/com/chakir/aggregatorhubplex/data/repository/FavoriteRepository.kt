package com.chakir.aggregatorhubplex.data.repository

import com.chakir.aggregatorhubplex.domain.model.Movie
import com.chakir.aggregatorhubplex.data.local.FavoriteDao
import com.chakir.aggregatorhubplex.data.local.FavoriteEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

// 1. L'interface (Le contrat)
/**
 * Repository gérant les favoris. Fait l'abstraction entre l'UI et la base de données locale (DAO).
 */
interface FavoriteRepository {
    /** Récupère le flux de tous les favoris. */
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    /** Vérifie si un média est favori. */
    fun isFavorite(mediaId: String): Flow<Boolean>

    /** Ajoute un média aux favoris. Convertit l'objet [Movie] en [FavoriteEntity]. */
    suspend fun addToFavorites(movie: Movie)

    /** Retire un média des favoris. */
    suspend fun removeFromFavorites(mediaId: String)
}

// 2. L'implémentation (La logique réelle)
/**
 * Implémentation concrète de [FavoriteRepository]. Utilise [FavoriteDao] pour persister les
 * données.
 */
@Singleton
class FavoriteRepositoryImpl @Inject constructor(private val favoriteDao: FavoriteDao) :
        FavoriteRepository {

    override fun getAllFavorites(): Flow<List<FavoriteEntity>> {
        return favoriteDao.getAllFavorites()
    }

    override fun isFavorite(mediaId: String): Flow<Boolean> {
        return favoriteDao.isFavorite(mediaId)
    }

    override suspend fun addToFavorites(movie: Movie) {
        val entity =
                FavoriteEntity(
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
