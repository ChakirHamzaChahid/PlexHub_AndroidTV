package com.chakir.aggregatorhubplex.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultAllocator
import androidx.media3.extractor.DefaultExtractorsFactory
import com.chakir.aggregatorhubplex.data.preferences.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Gestionnaire Singleton pour l'instance ExoPlayer. Configure le lecteur avec les timeouts, le
 * cache, la sélection de piste audio préférée, et gère le cycle de vie de base (play, pause,
 * release).
 */
@UnstableApi
@Singleton
class ExoPlayerManager
@Inject
constructor(
        @ApplicationContext private val context: Context,
        private val prefsManager: PreferencesManager
) {

    private var player: ExoPlayer? = null

    /** Récupère l'instance active du lecteur ou en crée une nouvelle si nécessaire. */
    fun getPlayer(): ExoPlayer {
        if (player == null) {
            player = buildPlayer()
        }
        return player!!
    }

    /**
     * Construit et configure une nouvelle instance d'ExoPlayer. Définit :
     * - Les timeouts de connexion (30s)
     * - Le buffer de lecture (LoadControl)
     * - La langue audio préférée (récupérée des préférences)
     * - Les extracteurs par défaut (MKV, MP4, etc.)
     */
    private fun buildPlayer(): ExoPlayer {
        val httpDataSourceFactory =
                DefaultHttpDataSource.Factory()
                        .setAllowCrossProtocolRedirects(true)
                        .setConnectTimeoutMs(30000)
                        .setReadTimeoutMs(30000)
                        .setUserAgent("PlexHub-AndroidTV")

        val loadControl =
                DefaultLoadControl.Builder()
                        .setAllocator(DefaultAllocator(true, 16 * 1024))
                        .setBufferDurationsMs(30_000, 120_000, 2_500, 5_000)
                        .setPrioritizeTimeOverSizeThresholds(true)
                        .build()

        val preferredAudio =
                try {
                    runBlocking { prefsManager.preferredAudioLang.first() }
                } catch (e: Exception) {
                    "fra"
                }

        val trackSelector =
                DefaultTrackSelector(context).apply {
                    parameters =
                            buildUponParameters().setPreferredAudioLanguage(preferredAudio).build()
                }

        val extractorsFactory = DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)

        val mediaSourceFactory =
                DefaultMediaSourceFactory(context, extractorsFactory)
                        .setDataSourceFactory(httpDataSourceFactory)

        return ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .setRenderersFactory(
                        DefaultRenderersFactory(context).setEnableDecoderFallback(true)
                )
                .setTrackSelector(trackSelector)
                .setLoadControl(loadControl)
                .build()
    }

    /** Lance la lecture d'une URL. Si l'URL est déjà en cours de lecture, ne fait rien. */
    /** Lance la lecture d'une URL. Si l'URL est déjà en cours de lecture, ne fait rien ou reprend la lecture. */
    fun play(url: String) {
        val player = getPlayer()
        val currentUri = player.currentMediaItem?.localConfiguration?.uri.toString()

        if (currentUri == url) {
            if (!player.isPlaying) {
                player.play()
            }
            return
        }

        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.playWhenReady = true
    }

    fun pause() {
        player?.pause()
    }

    /**
     * Libère les ressources du lecteur. À appeler lorsque l'application ou l'écran de lecture est
     * détruit.
     */
    fun release() {
        player?.release()
        player = null
    }
}
