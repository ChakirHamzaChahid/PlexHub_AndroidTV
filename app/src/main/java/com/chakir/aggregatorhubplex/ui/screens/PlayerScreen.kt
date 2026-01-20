package com.chakir.aggregatorhubplex.ui.screens

import android.app.Activity
import android.view.LayoutInflater
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.chakir.aggregatorhubplex.R
import com.chakir.aggregatorhubplex.data.repository.PlayHistoryRepository
import com.chakir.aggregatorhubplex.domain.model.Chapter
import com.chakir.aggregatorhubplex.domain.model.Marker
import com.chakir.aggregatorhubplex.player.ChapterMarkerManager
import com.chakir.aggregatorhubplex.player.ExoPlayerManager
import com.chakir.aggregatorhubplex.ui.components.EnhancedSeekBar
import com.chakir.aggregatorhubplex.ui.components.SkipMarkerButton
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer la lecture vidéo. Contrôle l'instance d'ExoPlayer, gère les chapitres, les
 * marqueurs et la sauvegarde de la progression.
 */

// ... [Existing Methods in ViewModel] ...

/**
 * Écran de lecteur vidéo. Intègre ExoPlayer, l'interface de contrôle et les boutons de saut
 * (Intro/Crédits).
 *
 * @param streamUrl URL du flux vidéo.
 * @param mediaTitle Titre du média.
 * @param mediaId Identifiant du média.
 * @param startPositionMs Position de départ.
 * @param chapters Liste des chapitres.
 * @param markers Liste des marqueurs.
 * @param onBack Callback pour le retour arrière.
 */
@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    streamUrl: String,
    mediaTitle: String = "Vidéo",
    mediaId: String = "",
    serverName: String?,
    showId: String? = null,
    startPositionMs: Long = -1L,
    chapters: List<Chapter>? = null,
    markers: List<Marker>? = null,
    posterUrl: String? = null,
    type: String = "movie",
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    LocalContext.current

    // URL decoding moved to ViewModel

    // URL decoding moved to ViewModel

    val error by viewModel.error.collectAsState()

    if (error != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewModel.clearError(); onBack() },
            title = { androidx.compose.material3.Text("Erreur de lecture") },
            text = { androidx.compose.material3.Text(error ?: "Erreur inconnue") },
            confirmButton = {
                androidx.compose.material3.Button(onClick = { viewModel.retry() }) {
                    androidx.compose.material3.Text("Réessayer")
                }
            },
            dismissButton = {
                androidx.compose.material3.Button(onClick = { viewModel.clearError(); onBack() }) {
                    androidx.compose.material3.Text("Quitter")
                }
            }
        )
    }

    remember { 0 } // Simplifié pour l'instant
    remember { 0L } // Serait collecté depuis l'état du joueur dans une implémentation réelle
    chapters ?: emptyList()
    markers ?: emptyList()
    remember { mutableStateOf<Chapter?>(null) }
    val visibleMarkersState = remember { mutableStateOf<List<Marker>>(emptyList()) }
    var showControls by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Masquer automatiquement les contrôles après 5 secondes d'inactivité
    LaunchedEffect(showControls, lastInteractionTime) {
        if (showControls) {
            delay(5000)
            // On vérifie directement si le player joue. 
            // Note: Comme isPlaying n'est pas un state, cela ne réagira pas au changement play/pause,
            // mais l'auto-hide se fera 5s après la dernière interaction si ça joue à ce moment-là.
            if (viewModel.player.isPlaying) {
                showControls = false
            }
        }
    }

    val onUserInteraction: () -> Unit = {
        lastInteractionTime = System.currentTimeMillis()
        showControls = true
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.onDispose()
        }
    }

    LaunchedEffect(chapters, markers) {
        chapters?.let { viewModel.chapterMarkerManager.setChapters(it) }
        markers?.let { viewModel.chapterMarkerManager.setMarkers(it) }
    }

    LaunchedEffect(streamUrl) {
        viewModel.playMedia(
            url = streamUrl,
            id = mediaId,
            startPositionMs = startPositionMs,
            serverName = serverName,
            chapters = chapters,
            markers = markers,
            showId = showId,
            title = mediaTitle,
            posterUrl = posterUrl,
            type = type
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val inflater = LayoutInflater.from(ctx)
                val playerView = inflater.inflate(R.layout.view_player, null) as PlayerView
                playerView.player = viewModel.player
                playerView.keepScreenOn = true
                playerView.useController = false // Disable native controls to use custom overlay
                playerView
            }
        )

        // Zone transparente pour détecter les clics et afficher/masquer les contrôles
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) {
                    showControls = !showControls
                    if (showControls) {
                        lastInteractionTime = System.currentTimeMillis()
                    }
                }
        )


        // Passer 'nextMedia' à l'overlay
        if (showControls) {
            val nextMedia by viewModel.nextMedia.collectAsState()
            com.chakir.aggregatorhubplex.ui.components.PlayerControlsOverlay(
                player = viewModel.player,
                chapterMarkerManager = viewModel.chapterMarkerManager,
                showControls = showControls,
                onSeek = { position ->
                    onUserInteraction()
                    viewModel.seekTo(position)
                },
                nextMedia = nextMedia,
                onPlayNext = {
                    onUserInteraction()
                    viewModel.playNext()
                }
            )
        }

        // Bouton pour sauter l'intro (haut droit)
        if (showControls) {
            val introMarker = viewModel.chapterMarkerManager.introMarker.collectAsState().value
            SkipMarkerButton(
                marker = introMarker,
                markerType = "intro",
                isVisible = visibleMarkersState.value.any { it.type == "intro" },
                onSkip = {
                    introMarker?.let {
                        onUserInteraction()
                        viewModel.skipMarker(it)
                    }
                },
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.TopEnd)
                    .padding(16.dp)
            )
        }

        // Bouton pour sauter les crédits (centre droit)
        if (showControls) {
            val creditsMarker = viewModel.chapterMarkerManager.creditsMarker.collectAsState().value
            SkipMarkerButton(
                marker = creditsMarker,
                markerType = "credits",
                isVisible = visibleMarkersState.value.any { it.type == "credits" },
                onSkip = {
                    creditsMarker?.let {
                        onUserInteraction()
                        viewModel.skipMarker(it)
                    }
                },
                modifier =
                    Modifier
                        .align(androidx.compose.ui.Alignment.CenterEnd)
                        .padding(16.dp)
            )
        }
    }
}
