package com.chakir.aggregatorhubplex.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chakir.aggregatorhubplex.data.Movie
import com.chakir.aggregatorhubplex.data.Season
import com.chakir.aggregatorhubplex.data.Server

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String, // "movie" ou "show"
    val posterUrl: String?,
    val year: Int?,
    val addedAt: String?,
    val rating: Float?,
    // --- NOUVEAUX CHAMPS ---
    val imdbRating: Float?,
    val rottenRating: Int?,
    val director: String?,
    // -----------------------
    val genres: List<String>?,
    val description: String?,
    val studio: String?,
    val contentRating: String?,
    val servers: List<Server>?,
    val seasons: List<Season>?,
    // AJOUTEZ CE CHAMP MANQUANT :
    val hasMultipleSources: Boolean = false
) {
    fun toMovie(): Movie {
        return Movie(
            id = id,
            title = title,
            type = type,

            // CORRECTION : On utilise les nouveaux noms définis dans DataLayer.kt
            posterPath = posterUrl,
            backdropPath = posterUrl, // Assurez-vous aussi de mapper le backdrop si présent dans l'entity

            year = year,
            addedAt = addedAt,
            rating = rating,

            // Mapping des nouveaux champs
            imdbRating = imdbRating,
            rottenRating = rottenRating,
            director = director,
            // --------------------------

            genres = genres,
            description = description,
            studio = studio,
            contentRating = contentRating,
            servers = servers ?: emptyList(),
            seasons = seasons ?: emptyList(),
            hasMultipleSources = hasMultipleSources
        )
    }
}

// Table technique indispensable pour la pagination infinie
@Entity(tableName = "remote_keys")
data class RemoteKeys(
    @PrimaryKey val movieId: String,
    val prevKey: Int?,
    val nextKey: Int?
)