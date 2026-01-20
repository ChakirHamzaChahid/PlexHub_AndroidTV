package com.chakir.aggregatorhubplex.domain.model

import com.chakir.aggregatorhubplex.util.UrlFixer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Modèle représentant un serveur Plex ou une source de média. */
@Serializable
data class Server(
    @SerialName("server_name") val name: String = "Inconnu",
    @SerialName("stream_url") val url: String = "",
    @SerialName("m3u_url") val rawM3uUrl: String? = null,
    @SerialName("resolution") val resolution: String? = "SD",
    @SerialName("plex_deeplink") val plexDeepLink: String? = null,
    @SerialName("plex_web_url") val plexWebUrl: String? = null
) {
    // URL corrigée pour le fichier M3U
    val m3uUrl: String?
        get() = rawM3uUrl?.let { fixUrl(it) }

    // URL corrigée pour le flux
    val streamUrl: String
        get() = fixUrl(url)

    private fun fixUrl(url: String): String {
        return UrlFixer.fix(url)
    }
}

/** Modèle complet pour un épisode de série. */
@Serializable
data class Episode(
    val id: String = "",
    @SerialName("ratingKey") val ratingKey: String? = null,
    val title: String = "Épisode sans titre",
    @SerialName("index") val episodeNumber: Int? = 0,
    val description: String? = null,
    @SerialName("thumb_url") val thumbUrl: String? = null,
    @SerialName("summary") val overview: String? = null,
    @SerialName("sources") val servers: List<Server>? = emptyList(),
    @SerialName("still_url") val rawStillUrl: String? = null
) {
    // URL corrigée pour l'image de l'épisode
    val stillUrl: String?
        get() = rawStillUrl?.let { UrlFixer.fix(it) }
}

/** Modèle représentant une saison d'une série. */
@Serializable
data class Season(
    @SerialName("index") val seasonNumber: Int? = 0,
    val title: String = "",
    val episodes: List<Episode> = emptyList()
)

/** Modèle représentant un acteur. */
@Serializable
data class Actor(
    @SerialName("name") val name: String,
    @SerialName("role") val role: String? = null,
    @SerialName("thumb_url") val thumbUrl: String? = null
) {
    // URL corrigée pour la photo de l'acteur
    val actorImageUrl: String
        get() = UrlFixer.fix(thumbUrl)
}

/** Modèle représentant une piste audio détaillée. */
@Serializable
data class AudioTrack(
    @SerialName("display_title") val displayTitle: String,
    val language: String,
    val codec: String,
    val channels: Int,
    val forced: Boolean = false
)

/** Modèle représentant un sous-titre détaillé. */
@Serializable
data class Subtitle(
    @SerialName("display_title") val displayTitle: String,
    val language: String,
    val codec: String,
    val forced: Boolean = false
)

/** Modèle représentant un chapitre avec des temps en secondes/entier. */
@Serializable
data class Chapter(
    val title: String,
    @SerialName("start_time") val startTime: Int,
    @SerialName("end_time") val endTime: Int,
    @SerialName("thumb_url") val thumbUrl: String? = null
) {
    // URL corrigée pour la vignette du chapitre
    val chapterThumbUrl: String
        get() = UrlFixer.fix(thumbUrl)
}

/** Modèle représentant un marqueur temporel (intro, générique de fin). */
@Serializable
data class Marker(
    val title: String = "Marker",
    val type: String, // 'intro' ou 'credits'
    @SerialName("start_time") val startTime: Int,
    @SerialName("end_time") val endTime: Int
)

/** Modèle représentant un élément similaire (film ou série). */
@Serializable
data class SimilarItem(
    val id: String,
    val title: String,
    val year: Int,
    @SerialName("thumb_url") val thumbUrl: String? = null,
    val rating: Float = 0.0f
) {
    // URL corrigée pour la vignette
    val similarThumbUrl: String
        get() = UrlFixer.fix(thumbUrl)
}

/** Modèle représentant une bande-annonce (Trailer). */
@Serializable
data class Trailer(
    val title: String,
    val duration: Int, // Durée en ms
    @SerialName("thumb_url") val thumbUrl: String? = null,
    @SerialName("stream_url") val streamUrl: String? = null,
    val key: String? = null
) {
    val trailerThumbUrl: String?
        get() = thumbUrl?.let { UrlFixer.fix(it) }

    val trailerStreamUrl: String?
        get() = streamUrl?.let { UrlFixer.fix(it) }
}

/** Modèle détaillé représentant un film ou une série. Utilisé pour la vue détail. */
@Serializable
data class Movie(
    val id: String,
    @SerialName("ratingKey") val ratingKey: String? = null,
    val title: String = "Sans titre",
    val type: String = "movie",
    @SerialName("summary") val description: String? = "Aucune description disponible",
    val rating: Float? = null,
    @SerialName("genres") val genres: List<String>? = emptyList(),
    val director: String? = null,
    val studio: String? = null,
    @SerialName("content_rating") val contentRating: String? = null,
    @SerialName("imdb_rating") val imdbRating: Float? = null,
    @SerialName("rotten_rating") val rottenRating: Int? = null,
    @SerialName("added_at") val addedAt: String? = null,
    @SerialName("poster_url") val posterPath: String? = null,
    @SerialName("thumb") val thumb: String? = null,
    @SerialName("backdrop_url") val backdropPath: String? = null,
    val year: Int? = null,
    @SerialName("sources") val servers: List<Server>? = emptyList(),
    val seasons: List<Season>? = emptyList(),
    @SerialName("actors") val actors: List<Actor>? = emptyList(),
    // Nouveaux champs du backend Python
    val badges: List<String>? = emptyList(), // Badges techniques: 4K, HDR, Atmos...
    @SerialName("audio_tracks") val audioTracks: List<AudioTrack>? = emptyList(),
    val subtitles: List<Subtitle>? = emptyList(),
    val chapters: List<Chapter>? = emptyList(),
    val markers: List<Marker>? = emptyList(), // Marqueurs Intro/Crédits
    val similar: List<SimilarItem>? = emptyList(), // Éléments similaires
    val trailers: List<Trailer>? = emptyList(), // Bandes-annonces
    @SerialName("view_count") val viewCount: Int = 0, // Nombre de vues
    @SerialName("runtime")
    val runtime: Int = 0, // Durée en millisecondes depuis le backend Python
    // Champs existants
    val hasMultipleSources: Boolean = false,
    @SerialName("view_offset") val viewOffset: Long = 0,
    val duration: Long = 0,
    // Navigation fields
    val grandparentKey: String? = null,
    val grandparentTitle: String? = null,
    val parentIndex: Int? = null,
    val index: Int? = null
) {
    // URL corrigée pour l'affiche
    val posterUrl: String
        get() = UrlFixer.fix(posterPath ?: thumb)

    // URL corrigée pour l'arrière-plan (ou affiche si null)
    val backdropUrl: String
        get() = UrlFixer.fix(backdropPath ?: posterPath)

    // Vérifie si c'est une série
    val isSeries: Boolean
        get() = type == "show"
}
