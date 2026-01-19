package com.chakir.aggregatorhubplex.player

import android.os.Handler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.media3.common.Player
import com.chakir.aggregatorhubplex.domain.model.Chapter
import com.chakir.aggregatorhubplex.domain.model.Marker

/**
 * Effet Composable (Side-effect) assurant la synchronisation entre :
 * - La position de lecture de l'ExoPlayer
 * - Les données de chapitrage et de marqueurs (Introduction, Crédits) gérées par
 * [ChapterMarkerManager].
 *
 * Met à jour périodiquement (toutes les 500ms) la position courante pour détecter les marqueurs.
 */
@Composable
fun PlayerChapterSyncEffect(
    player: Player?,
    chapterMarkerManager: ChapterMarkerManager,
    chapters: List<Chapter>?,
    markers: List<Marker>?
) {
    // Initialise les données statiques (chapitres et marqueurs) dès qu'elles changent
    LaunchedEffect(chapters, markers) {
        chapters?.let { chapterMarkerManager.setChapters(it) }
        markers?.let { chapterMarkerManager.setMarkers(it) }
    }

    // Gestion du cycle de vie des listeners du Player
    DisposableEffect(player) {
        if (player == null) return@DisposableEffect onDispose {}

        val listener =
            object : Player.Listener {
                override fun onPositionDiscontinuity(
                    oldPosition: Player.PositionInfo,
                    newPosition: Player.PositionInfo,
                    reason: Int
                ) {
                    chapterMarkerManager.updatePlaybackPosition(newPosition.positionMs)
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        chapterMarkerManager.updatePlaybackPosition(player.currentPosition)
                    }
                }
            }

        player.addListener(listener)

        val handler = Handler(player.applicationLooper)

        // Tâche périodique pour la mise à jour de la position (Polling)
        val updateTask =
            object : Runnable {
                override fun run() {
                    if (player.isPlaying) {
                        chapterMarkerManager.updatePlaybackPosition(player.currentPosition)
                    }
                    handler.postDelayed(this, 500) // Update every 500ms
                }
            }
        handler.postDelayed(updateTask, 500)

        // Nettoyage à la destruction du Composable
        onDispose {
            player.removeListener(listener)
            handler.removeCallbacks(updateTask)
        }
    }
}
