package com.chakir.aggregatorhubplex.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultAllocator
import androidx.media3.ui.PlayerView
import androidx.tv.material3.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chakir.aggregatorhubplex.data.Episode
import com.chakir.aggregatorhubplex.data.Movie
import com.chakir.aggregatorhubplex.data.NetworkModule
import com.chakir.aggregatorhubplex.data.Season
import com.chakir.aggregatorhubplex.data.Server

val DarkBackground = Color(0xFF141414)
val RatingBoxColor = Color(0xFF1F1F1F)

@OptIn(UnstableApi::class, ExperimentalTvMaterial3Api::class)
@Composable
fun DetailScreen(movieId: String) {
    // --- ÉTATS ---
    var movie by remember { mutableStateOf<Movie?>(null) }
    var seasons by remember { mutableStateOf<List<Season>>(emptyList()) }
    var selectedSeason by remember { mutableStateOf<Season?>(null) }

    // État lecture
    var isPlaying by remember { mutableStateOf(false) }
    var currentVideoUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // État Dialogues
    var showEpisodeSourceDialog by remember { mutableStateOf<Episode?>(null) }

    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // --- LOGIQUE PLAYER (inchangée) ---
    val exoPlayer = remember {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(30000)
            .setReadTimeoutMs(30000)
            .setUserAgent("PlexHub-AndroidTV")

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(httpDataSourceFactory)

        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
            .setEnableDecoderFallback(true)

        val loadControl = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16 * 1024))
            .setBufferDurationsMs(30_000, 120_000, 2_500, 5_000)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        ExoPlayer.Builder(context, renderersFactory)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .build()
    }

    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }

    BackHandler(enabled = isPlaying) {
        isPlaying = false
        exoPlayer.stop()
    }

    // --- CHARGEMENT ---
    LaunchedEffect(movieId) {
        isLoading = true
        try {
            val fetchedMovie = NetworkModule.api.getMovieDetail(movieId)
            movie = fetchedMovie
            if (fetchedMovie.isSeries) {
                try {
                    val fetchedSeasons = NetworkModule.api.getShowSeasons(movieId)
                    seasons = fetchedSeasons
                    selectedSeason = fetchedSeasons.firstOrNull()
                } catch (e: Exception) {
                    if (!fetchedMovie.seasons.isNullOrEmpty()) {
                        seasons = fetchedMovie.seasons
                        selectedSeason = fetchedMovie.seasons.firstOrNull()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DetailScreen", "Erreur", e)
        } finally {
            isLoading = false
        }
    }

    // --- RENDU ---
    if (isPlaying && currentVideoUrl.isNotEmpty()) {
        LaunchedEffect(currentVideoUrl) {
            exoPlayer.setMediaItem(MediaItem.fromUri(currentVideoUrl))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AndroidView(modifier = Modifier.fillMaxSize(), factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    controllerAutoShow = true
                    keepScreenOn = true
                }
            })
        }
    } else {
        val currentMovie = movie
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(DarkBackground), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PlexAccent)
            }
        } else if (currentMovie != null) {
            Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
                // Background Image + Gradient
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(currentMovie.backdropUrl).crossfade(true).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        alpha = 0.4f
                    )
                    Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(DarkBackground, Color.Transparent))))
                    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, DarkBackground))))
                }

                Row(modifier = Modifier.fillMaxSize().padding(50.dp)) {
                    // COLONNE GAUCHE (INFO) - Style de votre image
                    Column(modifier = Modifier.weight(0.55f).verticalScroll(scrollState).padding(end = 32.dp)) {

                        // Ligne Méta-data (Année | Rating | Studio)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${currentMovie.year ?: ""}", color = Color.White.copy(0.7f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            if (!currentMovie.contentRating.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(modifier = Modifier.border(1.dp, Color.White.copy(0.5f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text(currentMovie.contentRating, color = Color.White.copy(0.9f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (!currentMovie.studio.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(currentMovie.studio.uppercase(), color = PlexAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // TITRE GÉANT
                        Text(
                            text = currentMovie.title.uppercase(),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 56.sp,
                                letterSpacing = (-1).sp
                            ),
                            color = Color.White,
                            lineHeight = 60.sp
                        )

                        // Sous-titre "Dirigé par" ou Type
                        Spacer(modifier = Modifier.height(8.dp))
                        val directorText = if (!currentMovie.director.isNullOrBlank()) "DIRIGÉ PAR ${currentMovie.director.uppercase()}" else if (currentMovie.isSeries) "SÉRIE TV" else "FILM"
                        Text(directorText, color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)

                        Spacer(modifier = Modifier.height(24.dp))

                        // --- RATING BOX (Le style demandé) ---
                        Row(
                            modifier = Modifier
                                .background(RatingBoxColor, RoundedCornerShape(4.dp))
                                .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(4.dp))
                                .height(50.dp), // Hauteur fixe pour le style
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // IMDB
                            Column(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("IMDB", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("${currentMovie.imdbRating ?: currentMovie.rating ?: "-"}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                    Text("/10", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 2.dp))
                                }
                            }

                            // Divider vertical
                            Box(modifier = Modifier.width(1.dp).fillMaxHeight(0.6f).background(Color.White.copy(0.1f)))

                            // ROTTEN TOMATOES (Simulé si pas dispo, ou donnée réelle)
                            Column(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("ROTTEN TOMATOES", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val score = currentMovie.rottenRating ?: ((currentMovie.rating ?: 0f) * 10).toInt()
                                    Text("$score%", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("🍅", fontSize = 14.sp) // Emoji ou icône
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // SYNOPSIS avec barre verticale
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.width(4.dp).height(80.dp).background(PlexAccent))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = currentMovie.description ?: "Aucune description disponible.",
                                color = Color.White.copy(0.8f),
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    // COLONNE DROITE (CONTENU)
                    Column(modifier = Modifier.weight(0.45f).fillMaxHeight().padding(start = 24.dp)) {
                        if (currentMovie.isSeries) {
                            // Liste des Saisons
                            Text("SAISONS", color = PlexAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(seasons) { season ->
                                    SeasonChip(season, season == selectedSeason) { selectedSeason = season }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            // Liste des Épisodes
                            if (selectedSeason != null) {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(bottom = 50.dp)
                                ) {
                                    items(selectedSeason!!.episodes) { episode ->
                                        EpisodeItem(episode) {
                                            // ACTION : Ouvre le Dialog au lieu de jouer direct
                                            showEpisodeSourceDialog = episode
                                        }
                                    }
                                }
                            }
                        } else {
                            // Films : Liste des sources
                            Text("SOURCES DISPONIBLES", color = PlexAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(currentMovie.servers ?: emptyList()) { server ->
                                    SourceItemWithActions(
                                        server = server,
                                        onPlayClick = {
                                            currentVideoUrl = server.url.replace("localhost", "10.0.2.2")
                                            isPlaying = true
                                        },
                                        onPlexClick = { /* Intent logic */ },
                                        onExternalClick = { /* Intent logic */ }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGUE DE SOURCES POUR SÉRIES ---
    if (showEpisodeSourceDialog != null) {
        val episode = showEpisodeSourceDialog!!
        Dialog(onDismissRequest = { showEpisodeSourceDialog = null }) {
            Box(modifier = Modifier.size(600.dp, 400.dp).background(DarkBackground, RoundedCornerShape(16.dp)).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(16.dp)).padding(24.dp)) {
                Column {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("S${selectedSeason?.seasonNumber}E${episode.episodeNumber}", color = PlexAccent, fontWeight = FontWeight.Bold)
                            Text(episode.title, color = Color.White, style = MaterialTheme.typography.titleLarge, maxLines = 1)
                        }
                        IconButton(onClick = { showEpisodeSourceDialog = null }) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("CHOISISSEZ UNE SOURCE", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (episode.servers.isNullOrEmpty()) {
                        Text("Aucune source disponible", color = Color.Red)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(episode.servers) { server ->
                                SourceItemWithActions(
                                    server = server,
                                    onPlayClick = {
                                        currentVideoUrl = server.url.replace("localhost", "10.0.2.2")
                                        isPlaying = true
                                        showEpisodeSourceDialog = null
                                    },
                                    onPlexClick = { /* ... */ },
                                    onExternalClick = { /* ... */ }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// (Gardez les fonctions SeasonChip, EpisodeItem, SourceItemWithActions de votre ancien fichier, elles étaient bien)
// Assurez-vous juste que EpisodeItem a un onClick.

// --- COMPOSANTS UI HELPERS ---

@Composable
fun BadgeInfo(text: String, color: Color = Color.White.copy(alpha = 0.2f), textColor: Color = Color.White) {
    Box(modifier = Modifier.background(color, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = textColor, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SeasonChip(season: Season, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) PlexAccent else Color.White.copy(0.1f),
            contentColor = if (isSelected) Color.Black else Color.White,
            focusedContainerColor = PlexAccent,
            focusedContentColor = Color.Black
        )
    ) {
        Text(
            text = "S${season.seasonNumber}",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EpisodeItem(episode: Episode, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(0.05f),
            focusedContainerColor = Color.White.copy(0.2f),
            contentColor = Color.White,
            focusedContentColor = Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${episode.episodeNumber}.", color = PlexAccent, fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(episode.title, style = MaterialTheme.typography.bodyMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!episode.overview.isNullOrEmpty()) {
                    Text(episode.overview, style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SourceItemWithActions(
    server: Server,
    onPlayClick: () -> Unit,
    onPlexClick: () -> Unit,
    onExternalClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = server.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
            server.resolution?.let {
                Box(
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(it, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.White.copy(alpha = 0.8f))
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            // Bouton 1 : Lire (Interne)
            Button(
                onClick = onPlayClick,
                colors = ButtonDefaults.colors(
                    containerColor = PlexAccent,
                    contentColor = Color.Black,
                    focusedContainerColor = Color.White,
                    focusedContentColor = Color.Black
                ),
                shape = ButtonDefaults.shape(shape = RoundedCornerShape(4.dp)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Lire", style = MaterialTheme.typography.labelMedium)
                }
            }

            // Bouton 2 : Plex Native
            if (!server.plexDeepLink.isNullOrEmpty() || !server.plexWebUrl.isNullOrEmpty()) {
                Button(
                    onClick = onPlexClick,
                    colors = ButtonDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.15f),
                        contentColor = Color.White,
                        focusedContainerColor = Color(0xFFE5A00D),
                        focusedContentColor = Color.Black
                    ),
                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(4.dp)),
                    modifier = Modifier.width(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, "Plex", modifier = Modifier.size(16.dp))
                }
            }

            // Bouton 3 : Externe (M3U / VLC)
            if (!server.m3uUrl.isNullOrEmpty()) {
                Button(
                    onClick = onExternalClick,
                    colors = ButtonDefaults.colors(
                        containerColor = Color(0xFFE65100).copy(alpha = 0.8f),
                        contentColor = Color.White,
                        focusedContainerColor = Color(0xFFFF9800),
                        focusedContentColor = Color.Black
                    ),
                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(4.dp)),
                    modifier = Modifier.width(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Share, "Externe", modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}