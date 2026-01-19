package com.chakir.aggregatorhubplex.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chakir.aggregatorhubplex.domain.model.AudioTrack
import com.chakir.aggregatorhubplex.domain.model.Chapter
import com.chakir.aggregatorhubplex.domain.model.Marker
import com.chakir.aggregatorhubplex.domain.model.Movie
import com.chakir.aggregatorhubplex.domain.model.Season
import com.chakir.aggregatorhubplex.domain.model.Server
import com.chakir.aggregatorhubplex.domain.model.SimilarItem
import com.chakir.aggregatorhubplex.domain.model.Subtitle

/**
 * Entité Room principale représentant un film ou une série. Stocke toutes les métadonnées, les
 * informations de lecture, et les listes complexes (converties en JSON).
 */
@Entity(tableName = "movies")
data class MovieEntity(
        @PrimaryKey val id: String,
        val title: String,
        val type: String, // "movie" ou "show"
        val posterUrl: String?,
        val backdrop_url: String?,
        val year: Int?,
        val addedAt: String?,
        val rating: Float?,
        val imdbRating: Float?,
        val rottenRating: Int?,
        val director: String?,
        val genres: List<String>?,
        val description: String?,
        val studio: String?,
        val contentRating: String?,
        val servers: List<Server>?,
        val seasons: List<Season>?,
        // NEW FIELDS FROM PYTHON BACKEND
        val badges: List<String>? = null,
        val audioTracks: List<AudioTrack>? = null,
        val subtitles: List<Subtitle>? = null,
        val chapters: List<Chapter>? = null,
        val markers: List<Marker>? = null,
        val similar: List<SimilarItem>? = null,
        val viewCount: Int = 0,
        val runtime: Int = 0,
        // EXISTING FIELDS
        val hasMultipleSources: Boolean = false,
        val viewOffset: Long = 0,
        val duration: Long = 0
) {
    /** Convertit cette entité de base de données en objet modèle [Movie] utilisable par l'UI. */
    fun toMovie(): Movie {
        return Movie(
                id = id,
                title = title,
                type = type,
                posterPath = posterUrl,
                backdropPath = backdrop_url,
                year = year,
                addedAt = addedAt,
                rating = rating,
                imdbRating = imdbRating,
                rottenRating = rottenRating,
                director = director,
                genres = genres,
                description = description,
                studio = studio,
                contentRating = contentRating,
                servers = servers ?: emptyList(),
                seasons = seasons ?: emptyList(),
                // NEW FIELDS
                badges = badges ?: emptyList(),
                audioTracks = audioTracks ?: emptyList(),
                subtitles = subtitles ?: emptyList(),
                chapters = chapters ?: emptyList(),
                markers = markers ?: emptyList(),
                similar = similar ?: emptyList(),
                viewCount = viewCount,
                runtime = runtime,
                // EXISTING FIELDS
                hasMultipleSources = hasMultipleSources,
                viewOffset = viewOffset,
                duration = duration
        )
    }
}

// Table technique indispensable pour la pagination infinie
@Entity(tableName = "remote_keys")
data class RemoteKeys(@PrimaryKey val movieId: String, val prevKey: Int?, val nextKey: Int?)
