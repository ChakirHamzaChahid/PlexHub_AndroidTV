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
        modifier: Modifier = Modifier
) {
    if (player == null) return

    val chapters = chapterMarkerManager.chapters.collectAsState().value
    val markers = chapterMarkerManager.markers.collectAsState().value
    val currentChapter = chapterMarkerManager.currentChapter.collectAsState().value
    val visibleMarkers = chapterMarkerManager.visibleMarkers.collectAsState().value

    AnimatedVisibility(visible = showControls, enter = fadeIn(), exit = fadeOut()) {
        Column(modifier = modifier.fillMaxSize().background(Color.Transparent)) {
            // Top spacer
            Box(modifier = Modifier.weight(1f))

            // Chapter navigation (center)
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
                        modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally)
                )
            }

            // Bottom controls area
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
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
    }
}

/** Overlay de chargement du lecteur. */
@Composable
fun PlayerLoadingOverlay(isLoading: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
        Box(
                modifier = modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
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
                modifier = modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
        ) {
            Column(
                    modifier =
                            Modifier.background(
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
