package com.chakir.aggregatorhubplex.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.chakir.aggregatorhubplex.data.repository.MediaRepository
import com.chakir.aggregatorhubplex.data.repository.PlayHistoryRepository
import com.chakir.aggregatorhubplex.domain.model.Chapter
import com.chakir.aggregatorhubplex.domain.model.Marker
import com.chakir.aggregatorhubplex.domain.model.Movie
import com.chakir.aggregatorhubplex.player.ChapterMarkerManager
import com.chakir.aggregatorhubplex.player.ExoPlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer la lecture vidéo. Contrôle l'instance d'ExoPlayer, gère les chapitres, les
 * marqueurs, la sauvegarde de la progression et les erreurs.
 */
@UnstableApi
@OptIn(UnstableApi::class)
@HiltViewModel
class PlayerViewModel
@Inject
constructor(
    private val playerManager: ExoPlayerManager,
    private val historyRepository: PlayHistoryRepository,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    val player = playerManager.getPlayer()
    val chapterMarkerManager = ChapterMarkerManager()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _nextMedia = MutableStateFlow<Movie?>(null)
    val nextMedia: StateFlow<Movie?> = _nextMedia

    private var currentMediaUrl: String = ""
    private var currentPlexId: String? = null
    private var isWatched: Boolean = false
    private var currentTitle: String = "Inconnu"
    private var currentPoster: String? = null
    private var currentType: String = "movie"
    private var serverName: String? = null

    // Pour éviter de spammer le scrobble
    private var hasScrobbledWatched = false

    init {
        player.addListener(
            object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    _error.value = "Erreur de lecture : ${error.message} (${error.errorCodeName})"
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        if (!hasScrobbledWatched && currentPlexId != null) {
                            viewModelScope.launch {
                                // Save finished state
                                historyRepository.markAsWatched(currentPlexId!!)
                                hasScrobbledWatched = true
                            }
                        }
                    }
                }

                // On utilise onEvents ou un Job périodique pour la progression, mais ici on simplifie
                // L'idéal est un ticker coroutine
            }
        )

        // Ticker pour la progression et le scrobble à 90%
        viewModelScope.launch {
            while (true) {
                if (player.isPlaying) {
                    val duration = player.duration
                    val current = player.currentPosition
                    if (duration > 0) {
                        val progress = current.toFloat() / duration.toFloat()
                        
                        // Save progress locally
                        currentPlexId?.let { mediaId ->
                            historyRepository.savePlaybackPosition(
                                mediaId = mediaId,
                                title = currentTitle,
                                posterUrl = currentPoster,
                                positionMs = current,
                                durationMs = duration,
                                seriesId = currentShowId,
                                type = currentType
                            )
                        }

                        if (progress > 0.9f && !hasScrobbledWatched && currentPlexId != null) {
                            hasScrobbledWatched = true
                            historyRepository.markAsWatched(currentPlexId!!)
                            // Déclencher la recherche du prochain épisode
                            checkForNextEpisode()
                        }
                    }
                }
                delay(5000) // Update every 5 seconds
            }
        }
    }

    /**
     * Lance la lecture d'un média.
     *
     * @param url L'URL du flux vidéo.
     * @param id Identifiant Plex du média.
     * @param startPositionMs Position de départ en millisecondes.
     * @param chapters Liste des chapitres optionnelle.
     * @param markers Liste des marqueurs optionnelle (intro, crédits).
     */
    private var currentShowId: String? = null

    // ...

    fun playMedia(
        url: String,
        id: String?,
        startPositionMs: Long,
        serverName: String?,
        chapters: List<Chapter>? = null,
        markers: List<Marker>? = null,
        showId: String? = null,
        title: String,
        posterUrl: String?,
        type: String = "movie"
    ) {
        // Decode URL to ensure it's valid for ExoPlayer
        val decodedUrl =
            try {
                URLDecoder.decode(url, StandardCharsets.UTF_8.toString())
            } catch (e: Exception) {
                url
            }

        currentMediaUrl = decodedUrl
        currentPlexId = id
        this.serverName = serverName
        this.currentShowId = showId
        this.currentTitle = title
        // TODO: Store posterUrl and type in class props if needed for savePlaybackPosition
        // We will need them for history
        this.currentPoster = posterUrl
        this.currentType = type
        
        isWatched = false
        hasScrobbledWatched = false
        _nextMedia.value = null // Reset next media
        _error.value = null // Reset error on new playback

        viewModelScope.launch {
            val savedPosition =
                if (startPositionMs > 0) startPositionMs
                else historyRepository.getPlaybackPosition(currentMediaUrl)

            try {
                playerManager.play(decodedUrl)

                if (savedPosition > 0) {
                    player.seekTo(savedPosition)
                }

                // Configurer chapitres et marqueurs
                chapters?.let { chapterMarkerManager.setChapters(it) }
                markers?.let { chapterMarkerManager.setMarkers(it) }

                // Pré-charger le prochain épisode dès le début
                if (id != null) {
                    checkForNextEpisode()
                }
            } catch (e: Exception) {
                _error.value = "Impossible de lancer la lecture : ${e.message}"
            }
        }
    }

    private fun checkForNextEpisode() {
        if (currentPlexId == null) return

        viewModelScope.launch {
            try {
                // 1. Essayer de récupérer l'épisode actuel
                var currentMovie: Movie? = null
                try {
                     mediaRepository.getMovieDetail(currentPlexId!!).collect { 
                         if (it != null) {
                             currentMovie = it
                             throw Exception("Found")
                         }
                     }
                } catch (e: Exception) { /* Flow break */ }

                // Si currentMovie est null, mais qu'on a un showId, on tente de passer directement à la récupération de la série
                var showId = currentMovie?.grandparentKey ?: currentShowId
                
                if (showId == null) {
                     android.util.Log.e("PlayerVM", "No showId found (neither in movie nor passed arg)")
                     return@launch
                }

                // 2. Récupérer la série complète (avec saisons/épisodes)
                var show: Movie? = null
                try {
                    mediaRepository.getMovieDetail(showId).collect {
                        if (it != null && !it.seasons.isNullOrEmpty()) {
                            show = it
                            throw Exception("FoundShow")
                        }
                    }
                } catch (e: Exception) { /* Flow break */ }

                if (show == null || show!!.seasons.isNullOrEmpty()) {
                     android.util.Log.e("PlayerVM", "Show not found or no seasons")
                     return@launch
                }

                // 3. Retrouver notre position dans la hiérarchie de la série
                var foundSeason: com.chakir.aggregatorhubplex.domain.model.Season? = null
                var foundEpisode: com.chakir.aggregatorhubplex.domain.model.Episode? = null
                
                // A. Essai par ID (Match exact)
                for (season in show!!.seasons!!) {
                    val match = season.episodes.find { 
                        it.id == currentPlexId || it.ratingKey == currentPlexId ||
                        (currentMovie != null && it.ratingKey == currentMovie!!.ratingKey)
                    }
                    if (match != null) {
                        foundSeason = season
                        foundEpisode = match
                        break
                    }
                }

                // B. Fallback : Essai par parsing de l'ID "SxxExx" (si l'ID est invalide comme "S01E01")
                if (foundSeason == null && currentPlexId!!.matches(Regex("S\\d+E\\d+"))) {
                    try {
                        val sIndex = currentPlexId!!.substringAfter("S").substringBefore("E").toInt()
                        val eIndex = currentPlexId!!.substringAfter("E").toInt()
                        
                        foundSeason = show!!.seasons!!.find { it.seasonNumber == sIndex }
                        if (foundSeason != null) {
                            foundEpisode = foundSeason!!.episodes.find { it.episodeNumber == eIndex }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("PlayerVM", "Failed to parse SxxExx ID: $currentPlexId")
                    }
                }


                if (foundSeason == null || foundEpisode == null) {
                     // Fallback: utiliser les index si l'ID match a échoué
                     val pIndex = currentMovie!!.parentIndex
                     if (pIndex != null) {
                         foundSeason = show!!.seasons!!.find { it.seasonNumber == pIndex }
                         if (foundSeason != null) {
                             foundEpisode = foundSeason.episodes.find { it.episodeNumber == currentMovie!!.index }
                         }
                     }
                }
                
                if (foundSeason == null || foundEpisode == null) {
                    android.util.Log.e("PlayerVM", "Could not locate current episode in show structure")
                    return@launch
                }

                // 4. Trouver le SUIVANT
                var nextEpisode: com.chakir.aggregatorhubplex.domain.model.Episode? = null
                var nextSeasonNumber = foundSeason.seasonNumber ?: 0

                // A. Essayer dans la même saison (épisode suivant)
                nextEpisode = foundSeason.episodes
                    .filter { (it.episodeNumber ?: -1) > (foundEpisode.episodeNumber ?: -1) }
                    .minByOrNull { it.episodeNumber ?: Int.MAX_VALUE }
                
                // B. Si rien, passer à la saison suivante
                if (nextEpisode == null) {
                    val nextSeason = show!!.seasons!!
                        .filter { (it.seasonNumber ?: -1) > (foundSeason.seasonNumber ?: -1) }
                        .minByOrNull { it.seasonNumber ?: Int.MAX_VALUE }
                    
                    if (nextSeason != null) {
                        nextEpisode = nextSeason.episodes.minByOrNull { it.episodeNumber ?: Int.MAX_VALUE }
                        if (nextEpisode != null) {
                            nextSeasonNumber = nextSeason.seasonNumber ?: (nextSeasonNumber + 1)
                        }
                    }
                }

                if (nextEpisode != null) {
                    android.util.Log.d("PlayerVM", "Next episode found: ${nextEpisode.title}")
                    
                    // Convertir Episode en Movie pour l'UI
                    val nextMovie = Movie(
                        id = nextEpisode.id,
                        ratingKey = nextEpisode.ratingKey ?: nextEpisode.id,
                        title = nextEpisode.title,
                        type = "episode",
                        description = nextEpisode.overview ?: "",
                        thumb = nextEpisode.thumbUrl ?: show!!.thumb,
                        posterPath = nextEpisode.thumbUrl ?: show!!.posterPath,
                        backdropPath = show!!.backdropPath,
                        servers = nextEpisode.servers,
                        grandparentKey = show!!.ratingKey,
                        grandparentTitle = show!!.title,
                        parentIndex = nextSeasonNumber,
                        index = nextEpisode.episodeNumber
                    )
                    _nextMedia.value = nextMovie
                } else {
                    android.util.Log.d("PlayerVM", "No next episode found")
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playNext() {
        val next = _nextMedia.value ?: return
        playMedia(
            url = next.servers?.firstOrNull()?.streamUrl ?: "", // On suppose le premier serveur
            id = next.ratingKey ?: next.id,
            startPositionMs = 0,
            serverName = next.servers?.firstOrNull()?.name,
            chapters = next.chapters,
            markers = next.markers,
            title = next.title,
            posterUrl = next.posterPath,
            type = next.type
        )
    }

    fun retry() {
        if (currentMediaUrl.isNotEmpty()) {
            _error.value = null
            player.prepare()
            player.play()
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    fun skipMarker(marker: Marker) {
        seekTo(marker.endTime * 1000L)
    }

    fun onDispose() {
        // Optionally handle cleanup if needed, but ExoPlayerManager handles release globally usually.
        // Or we pause here.
        player.pause()
    }
}
