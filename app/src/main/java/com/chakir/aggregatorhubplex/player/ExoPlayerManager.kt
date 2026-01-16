package com.chakir.aggregatorhubplex.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultAllocator
import com.chakir.aggregatorhubplex.data.preferences.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class ExoPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefsManager: PreferencesManager // <--- Vérifiez que cette classe existe aussi !
) {

    private var player: ExoPlayer? = null

    fun getPlayer(): ExoPlayer {
        if (player == null) {
            player = buildPlayer()
        }
        return player!!
    }

    private fun buildPlayer(): ExoPlayer {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(30000)
            .setReadTimeoutMs(30000)
            .setUserAgent("PlexHub-AndroidTV")

        val loadControl = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16 * 1024))
            .setBufferDurationsMs(30_000, 120_000, 2_500, 5_000)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        // Récupération synchrone de la langue préférée pour l'init
        val preferredAudio = try {
            runBlocking { prefsManager.preferredAudioLang.first() }
        } catch (e: Exception) {
            "fra" // Fallback
        }

        val trackSelector = DefaultTrackSelector(context).apply {
            parameters = buildUponParameters()
                .setPreferredAudioLanguage(preferredAudio)
                .build()
        }

        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(httpDataSourceFactory))
            .setRenderersFactory(DefaultRenderersFactory(context).setEnableDecoderFallback(true))
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .build()
    }

    fun play(url: String) {
        val player = getPlayer()
        if (player.currentMediaItem?.localConfiguration?.uri.toString() == url && player.isPlaying) return

        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.playWhenReady = true
    }

    fun pause() {
        player?.pause()
    }

    fun release() {
        player?.release()
        player = null
    }
}