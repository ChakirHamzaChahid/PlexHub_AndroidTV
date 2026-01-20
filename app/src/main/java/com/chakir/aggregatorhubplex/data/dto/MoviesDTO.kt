package com.chakir.aggregatorhubplex.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Représente un élément de la liste de films ou séries. Utilisé pour l'affichage dans les listes
 * principales (accueil, recherche, etc.).
 */
@Serializable
data class MovieListItem(
    val id: String,
    @SerialName("ratingKey") val ratingKey: String? = null,
    @SerialName("guid") val guid: String? = null,
    @SerialName("imdb_id") val imdbId: String? = null,
    val title: String = "Sans titre",
    val type: String = "movie",
    val year: Int? = null,
    @SerialName("poster_url") val posterPath: String? = null,
    @SerialName("thumb") val thumb: String? = null,
    @SerialName("backdrop_url") val backdropPath: String? = null,
    val rating: Float? = null,
    @SerialName("imdb_rating") val imdbRating: Float? = null,
    val hasMultipleSources: Boolean = false,
    val genres: List<String>? = emptyList(),
    @SerialName("added_at") val addedAt: String? = null,
    // Champs additionnels du backend
    val director: String? = null,
    @SerialName("content_rating") val contentRating: String? = null,
    val studio: String? = null,
    val summary: String? = null,
    val badges: List<String>? = emptyList(),
    @SerialName("rotten_rating") val rottenRating: Int? = null,
    val duration: Long? = null, // Runtime en millisecondes
    @SerialName("audio_tracks") val audioTracks: List<AudioTrackDTO>? = emptyList(),
    val subtitles: List<SubtitleDTO>? = emptyList(),
    val chapters: List<ChapterDTO>? = emptyList(),
    val markers: List<MarkerDTO>? = emptyList(),
    @SerialName("view_offset") val viewOffset: Long? = null,
    @SerialName("view_count") val viewCount: Int? = null,
    val episodes: List<EpisodeDTO>? = emptyList(),
    val sources: List<SourceDTO>? = emptyList(),
    // Navigation fields for Episodes
    @SerialName("grandparentKey") val grandparentKey: String? = null,
    @SerialName("grandparentTitle") val grandparentTitle: String? = null,
    @SerialName("parentKey") val parentKey: String? = null,
    @SerialName("index") val index: Int? = null,
    @SerialName("parentIndex") val parentIndex: Int? = null
)

/** DTO pour les pistes audio. */
@Serializable
data class AudioTrackDTO(
    @SerialName("display_title") val displayTitle: String? = null,
    val language: String? = null,
    val codec: String? = null,
    val channels: Int? = null,
    val forced: Boolean = false
)

/** DTO pour les sous-titres. */
@Serializable
data class SubtitleDTO(
    @SerialName("display_title") val displayTitle: String? = null,
    val language: String? = null,
    val codec: String? = null,
    val forced: Boolean = false
)

/** DTO pour les chapitres. */
@Serializable
data class ChapterDTO(
    val title: String? = null,
    @SerialName("start_time") val startTime: Long? = null,
    @SerialName("end_time") val endTime: Long? = null,
    val thumb: String? = null
)

/** DTO pour les marqueurs (intro, crédits, etc.). */
@Serializable
data class MarkerDTO(
    val title: String? = null,
    val type: String? = null,
    @SerialName("start_time") val startTime: Long? = null,
    @SerialName("end_time") val endTime: Long? = null
)

/** DTO pour les épisodes de série. */
@Serializable
data class EpisodeDTO(
    val season: Int? = null,
    val index: Int? = null,
    val title: String? = null,
    val summary: String? = null,
    val thumb: String? = null,
    val key: String? = null
)

/** DTO pour les sources de lecture. */
@Serializable
data class SourceDTO(
    @SerialName("server_name") val serverName: String? = null,
    val resolution: String? = null,
    @SerialName("is_owned") val isOwned: Boolean = false,
    @SerialName("stream_url") val streamUrl: String? = null,
    @SerialName("m3u_url") val m3uUrl: String? = null,
    @SerialName("plex_deeplink") val plexDeepLink: String? = null,
    @SerialName("plex_web_url") val plexWebUrl: String? = null
)

/** Requête pour l'action de "scrobble" (marquer comme vu/non vu). */
@Serializable
data class ScrobbleRequest(
    val key: String,
    val action: String // "watched" ou "unwatched"
)

/** Requête pour la mise à jour de la progression. */
@Serializable
data class ProgressRequest(
    val key: String,
    @SerialName("time_ms") val timeMs: Long,
    @SerialName("server_name") val serverName: String? = null
)

/** Modèle représentant les informations d'un serveur Plex. */
@Serializable
data class ServerInfo(
    val name: String,
    val url: String,
    val owned: Boolean,
    val latency: Float = 0.0f,
    val status: String = "Online"
)

/** Modèle de réponse pour les opérations de scan/rafraîchissement. */
@Serializable
data class ScanResponse(val message: String, val status: String)

/** Modèle représentant un client connecté (lecteur). */
@Serializable
data class ClientInfo(
    val name: String,
    val address: String,
    val product: String? = null,
    val device: String? = null,
    val status: String = "Idle"
)
