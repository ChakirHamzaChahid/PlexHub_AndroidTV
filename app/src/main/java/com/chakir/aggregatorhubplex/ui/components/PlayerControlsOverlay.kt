package com.chakir.aggregatorhubplex.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.media3.common.Player
import com.chakir.aggregatorhubplex.player.ChapterMarkerManager

/**
 * Overlay de contrôle du lecteur avec barre de progression, chapitres et marqueurs. Gère la
 * navigation entre chapitres et l'affichage des informations de lecture.
 */
@Composable
fun PlayerControlsOverlay(
    player: Player?,
    chapterMarkerManager: ChapterMarkerManager,
    showControls: Boolean = true,
    onSeek: (Long) -> Unit = {},
    nextMedia: com.chakir.aggregatorhubplex.domain.model.Movie? = null,
    onPlayNext: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (player == null) return

    val chapters = chapterMarkerManager.chapters.collectAsState().value
    val markers = chapterMarkerManager.markers.collectAsState().value
    val currentChapter = chapterMarkerManager.currentChapter.collectAsState().value
    chapterMarkerManager.visibleMarkers.collectAsState().value

    // Focus Requesters pour la navigation explicite
    val nextEpisodeFocusRequester =
        androidx.compose.runtime.remember { androidx.compose.ui.focus.FocusRequester() }
    val nextButtonFocusRequester =
        androidx.compose.runtime.remember { androidx.compose.ui.focus.FocusRequester() }
    val playPauseFocusRequester =
        androidx.compose.runtime.remember { androidx.compose.ui.focus.FocusRequester() }

    // State pour contrôler l'apparition de la popup "Prochain épisode" (à 90%)
    var showNextPopup by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(
            false
        )
    }

    // Vérification périodique de la progression pour afficher la popup à 90%
    androidx.compose.runtime.LaunchedEffect(player) {
        while (true) {
            if (player != null && player.isPlaying && player.duration > 0) {
                val progress = player.currentPosition.toFloat() / player.duration.toFloat()
                if (progress >= 0.9f) {
                    showNextPopup = true
                }
            }
            kotlinx.coroutines.delay(1000)
        }
    }

    // Demander le focus sur le bouton Play quand les contrôles s'affichent
    androidx.compose.runtime.LaunchedEffect(showControls) {
        if (showControls) {
            // Petit délai pour laisser le temps à l'UI de s'afficher
            kotlinx.coroutines.delay(100)
            try {
                playPauseFocusRequester.requestFocus()
            } catch (e: Exception) {
                // Ignore focus errors
            }
        }
    }

    AnimatedVisibility(visible = showControls, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Center controls (Play/Pause/Next/Prev)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                            32.dp
                        )
                    ) {
                        // Previous Button
                        androidx.tv.material3.Surface(
                            onClick = { /* TODO: Implement previous Logic */ },
                            shape = androidx.tv.material3.ClickableSurfaceDefaults.shape(shape = androidx.compose.foundation.shape.CircleShape),
                            enabled = false,
                            colors = androidx.tv.material3.ClickableSurfaceDefaults.colors(
                                containerColor = Color.Transparent,
                                contentColor = Color.LightGray.copy(alpha = 0.5f),
                                focusedContainerColor = Color(0xFFE5A00D),
                                focusedContentColor = Color.Black
                            ),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.SkipPrevious,
                                    contentDescription = "Précédent",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Play/Pause Button
                        androidx.tv.material3.Surface(
                            onClick = {
                                if (player.isPlaying) player.pause() else player.play()
                            },
                            shape = androidx.tv.material3.ClickableSurfaceDefaults.shape(shape = androidx.compose.foundation.shape.CircleShape),
                            colors = androidx.tv.material3.ClickableSurfaceDefaults.colors(
                                containerColor = Color.Transparent, // Or semi-transparent black
                                contentColor = Color.White,
                                focusedContainerColor = Color(0xFFE5A00D),
                                focusedContentColor = Color.Black
                            ),
                            modifier = Modifier
                                .size(64.dp)
                                .focusRequester(playPauseFocusRequester)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = if (player.isPlaying)
                                        androidx.compose.material.icons.Icons.Default.Pause
                                    else
                                        androidx.compose.material.icons.Icons.Default.PlayArrow,
                                    contentDescription = if (player.isPlaying) "Pause" else "Lire",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // Next Button
                        androidx.tv.material3.Surface(
                            onClick = onPlayNext,
                            enabled = nextMedia != null,
                            shape = androidx.tv.material3.ClickableSurfaceDefaults.shape(shape = androidx.compose.foundation.shape.CircleShape),
                            colors = androidx.tv.material3.ClickableSurfaceDefaults.colors(
                                containerColor = Color.Transparent,
                                contentColor = if (nextMedia != null) Color.White else Color.LightGray.copy(
                                    alpha = 0.5f
                                ),
                                focusedContainerColor = Color(0xFFE5A00D),
                                focusedContentColor = Color.Black
                            ),
                            modifier = Modifier
                                .size(48.dp)
                                .focusRequester(nextButtonFocusRequester)
                                .focusProperties {
                                    if (nextMedia != null) {
                                        right = nextEpisodeFocusRequester
                                        down = nextEpisodeFocusRequester
                                    }
                                }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.SkipNext,
                                    contentDescription = "Suivant",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // ... (ChapterNavigationButtons and EnhancedSeekBar remain unchanged) ...
                if (chapters.isNotEmpty()) {
                    ChapterNavigationButtons(
                        currentChapter = currentChapter?.title,
                        onNextChapter = {
                            currentChapter?.let { current ->
                                chapterMarkerManager.getNextChapter(current.endTime.toLong())?.let {
                                    onSeek(it.startTime.toLong())
                                }
                            }
                        },
                        onPreviousChapter = {
                            currentChapter?.let { current ->
                                chapterMarkerManager.getPreviousChapter(current.startTime.toLong())
                                    ?.let { onSeek(it.startTime.toLong()) }
                            }
                        },
                        hasNextChapter =
                            chapterMarkerManager.getNextChapter(
                                currentChapter?.endTime?.toLong() ?: player.currentPosition
                            ) != null,
                        hasPreviousChapter =
                            chapterMarkerManager.getPreviousChapter(
                                currentChapter?.startTime?.toLong()
                                    ?: player.currentPosition
                            ) != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                    )
                }

                // Bottom controls area
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(8.dp)
                ) {
                    // Seek bar with chapters
                    EnhancedSeekBar(
                        currentPosition = player.currentPosition,
                        duration = player.duration,
                        chapters = chapters,
                        markers = markers,
                        onSeek = onSeek
                    )
                }
            }

            // Next Episode Overlay (Bottom Right)
            if (nextMedia != null && showNextPopup) {
                NextEpisodeCard(
                    nextMedia = nextMedia,
                    onPlayNow = onPlayNext,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 100.dp, end = 32.dp)
                        .focusRequester(nextEpisodeFocusRequester)
                )
            }
        }
    }
}

@Composable
fun NextEpisodeCard(
    nextMedia: com.chakir.aggregatorhubplex.domain.model.Movie,
    onPlayNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.tv.material3.Surface(
        onClick = onPlayNow,
        shape = androidx.tv.material3.ClickableSurfaceDefaults.shape(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ),
        colors = androidx.tv.material3.ClickableSurfaceDefaults.colors(
            containerColor = Color(0xFF1F1F1F),
            contentColor = Color.White,
            focusedContainerColor = Color(0xFFE5A00D),
            focusedContentColor = Color.Black
        ),
        modifier = modifier
            .width(350.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PROCHAIN ÉPISODE",
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = nextMedia.title,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            if (nextMedia.index != null && nextMedia.parentIndex != null) {
                Text(
                    text = "S${nextMedia.parentIndex} : E${nextMedia.index}",
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    color = Color.LightGray
                )
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            // Button visual representation (Text only, as Surface handles click)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White,
                        androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    )
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Lancer maintenant",
                    color = Color.Black,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
            }
        }
    }
}

/** Overlay de chargement du lecteur. */
@Composable
fun PlayerLoadingOverlay(isLoading: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = Color(0xFFE5A00D)) }
    }
}

/** Overlay de message d'erreur du lecteur. */
@Composable
fun PlayerErrorOverlay(
    errorMessage: String?,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {}
) {
    AnimatedVisibility(visible = errorMessage != null, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier =
                    Modifier
                        .background(
                            color = Color(0xFF1A1A1A),
                            shape =
                                androidx.compose.foundation.shape
                                    .RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Erreur de lecture", color = Color.White, fontSize = 18.sp)
                Text(
                    text = errorMessage ?: "Une erreur inconnue s'est produite",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                androidx.compose.material3.Button(
                    onClick = onDismiss,
                    modifier = Modifier.padding(top = 16.dp)
                ) { Text("OK") }
            }
        }
    }
}
