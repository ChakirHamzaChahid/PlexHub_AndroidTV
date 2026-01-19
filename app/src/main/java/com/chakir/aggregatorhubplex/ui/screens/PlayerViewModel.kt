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
import com.chakir.aggregatorhubplex.player.ChapterMarkerManager
import com.chakir.aggregatorhubplex.player.ExoPlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private var currentMediaUrl: String = ""
    private var currentPlexId: String? = null
    private var isWatched: Boolean = false
    private var currentTitle: String = "Inconnu"

    init {
        player.addListener(
            object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    _error.value = "Erreur de lecture : ${'$'}{error.message} (${'$'}{error.errorCodeName})"
                }
            }
        )
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
    fun playMedia(
        url: String,
        id: String?,
        startPositionMs: Long,
        chapters: List<Chapter>? = null,
        markers: List<Marker>? = null
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
        isWatched = false
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
            } catch (e: Exception) {
                _error.value = "Impossible de lancer la lecture : ${'$'}{e.message}"
            }
        }
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
