package com.chakir.aggregatorhubplex.ui.screens

import android.app.Activity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.chakir.aggregatorhubplex.player.ExoPlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import com.chakir.aggregatorhubplex.data.repository.PlayHistoryRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: ExoPlayerManager,
    private val historyRepository: PlayHistoryRepository // <--- Injection
) : ViewModel() {

    val player = playerManager.getPlayer()

    // On garde une référence pour sauvegarder à la fin
    private var currentMediaId: String = ""
    private var currentTitle: String = "Unknown" // Idéalement passé en paramètre, sinon on fera sans pour l'instant

    fun playMedia(url: String) {
        // Extraction basique d'un ID depuis l'URL si possible,
        // ou utilisation de l'URL comme ID unique (hash) pour simplifier ici.
        // Dans une vraie app Plex, on passerait l'ID du film en argument de navigation.
        // Ici, on va utiliser l'URL décodée comme clé unique.
        currentMediaId = url

        viewModelScope.launch {
            // 1. Récupérer la position
            val savedPosition = historyRepository.getPlaybackPosition(currentMediaId)

            // 2. Lancer la lecture
            playerManager.play(url)

            // 3. Appliquer la position si > 0
            if (savedPosition > 0) {
                player.seekTo(savedPosition)
            }
        }
    }

    fun saveProgress() {
        if (currentMediaId.isNotEmpty() && player.duration > 0) {
            val position = player.currentPosition
            val duration = player.duration

            viewModelScope.launch {
                historyRepository.savePlaybackPosition(
                    mediaId = currentMediaId,
                    title = "Vidéo", // Pour l'instant placeholder, on améliorera
                    posterUrl = null,
                    positionMs = position,
                    durationMs = duration
                )
            }
        }
    }

    fun pause() {
        saveProgress() // On sauvegarde dès qu'on met en pause ou qu'on quitte
        playerManager.pause()
    }

    override fun onCleared() {
        super.onCleared()
        saveProgress()
    }
}


@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    streamUrl: String,
    mediaTitle: String = "Vidéo", // <--- AJOUT
    mediaId: String = "",         // <--- AJOUT
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val decodedUrl = remember(streamUrl) {
        try {
            URLDecoder.decode(streamUrl, StandardCharsets.UTF_8.toString())
        } catch (e: Exception) {
            streamUrl
        }
    }

    // Gestion Plein Écran (Cache les barres système)
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            val window = (context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
            viewModel.pause() // <--- Ceci déclenchera saveProgress()
        }
    }

    // Lancement de la vidéo
    LaunchedEffect(decodedUrl) {
        // On passe maintenant l'ID et le Titre au ViewModel pour l'historique
        viewModel.playMedia(url = decodedUrl)
        // Note: Idéalement, playMedia devrait prendre aussi mediaId et title
        // pour saveProgress(), mais pour l'instant playMedia(url) utilise l'url comme ID.
        // Si vous avez mis à jour PlayerViewModel comme demandé précédemment,
        // assurez-vous de passer ces infos si besoin, ou utilisez decodedUrl comme ID temporaire.
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = viewModel.player
                    keepScreenOn = true
                    useController = true
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }
        )
    }
}