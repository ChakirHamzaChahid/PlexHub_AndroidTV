package com.chakir.aggregatorhubplex.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chakir.aggregatorhubplex.domain.model.Episode
import com.chakir.aggregatorhubplex.domain.model.Season
import com.chakir.aggregatorhubplex.domain.model.Server

@OptIn(ExperimentalTvMaterial3Api::class)
/**
 * Écran de détail affichant les informations d'un film ou d'une série. Affiche l'arrière-plan, le
 * poster, les informations (titre, description...) et gère la sélection des saisons/épisodes pour
 * les séries.
 *
 * @param movieId Identifiant du média à afficher.
 * @param startPositionMs Position de lecture initiale (si reprise).
 * @param onPlayVideo Callback déclenché pour lancer la lecture.
 * @param viewModel ViewModel injecté par Hilt.
 */
@Composable
fun DetailScreen(
    movieId: String,
    startPositionMs: Long,
    onPlayVideo: (videoUrl: String, title: String, id: String, position: Long) -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    // --- ÉTATS ---
    LaunchedEffect(movieId) { viewModel.loadMovie(movieId) }

    val movie by viewModel.movie.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var seasons by remember { mutableStateOf<List<Season>>(emptyList()) }
    var selectedSeason by remember { mutableStateOf<Season?>(null) }
    var showEpisodeSourceDialog by remember { mutableStateOf<Episode?>(null) }
    var showMovieSourceDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(movie) {
        if (movie != null && movie!!.isSeries) {
            if (!movie!!.seasons.isNullOrEmpty()) {
                seasons = movie!!.seasons!!
                selectedSeason = seasons.firstOrNull()
            }
        }
    }

    val currentMovie = movie

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
    } else if (currentMovie != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model =
                    ImageRequest.Builder(context)
                        .data(currentMovie.backdropUrl)
                        .crossfade(true)
                        .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.2f
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme
                                            .background,
                                        MaterialTheme.colorScheme
                                            .background.copy(
                                                alpha = 0.5f
                                            ),
                                        Color.Transparent
                                    ),
                                startX = 0f,
                                endX = 1200f
                            )
                        )
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme
                                            .background
                                    ),
                                startY = 0f,
                                endY = 2000f
                            )
                        )
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            start = 50.dp,
                            top = 50.dp,
                            bottom = 50.dp,
                            end = 24.dp
                        )
            ) {

                // Left Column: Poster
                Box(
                    modifier = Modifier
                        .weight(0.3f)
                        .padding(end = 32.dp)
                ) {
                    AsyncImage(
                        model =
                            ImageRequest.Builder(context)
                                .data(currentMovie.posterUrl)
                                .crossfade(true)
                                .build(),
                        contentDescription = "Movie Poster",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(2 / 3f)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    // TODO: Add overlays on the poster here
                }

                if (currentMovie.isSeries) {
                    SeriesDetailScreen(
                        movie = currentMovie,
                        seasons = seasons,
                        selectedSeason = selectedSeason,
                        onSeasonSelected = { selectedSeason = it },
                        onEpisodeClick = { showEpisodeSourceDialog = it },
                        modifier = Modifier.weight(0.7f)
                    )
                } else {
                    MovieDetailScreen(
                        movie = currentMovie,
                        onPlayClick = { showMovieSourceDialog = true },
                        onTrailerClick = { url ->
                            openExternalLink(context, url, "Bande-annonce", "video/*")
                        },
                        onFavoriteClick = { viewModel.toggleFavorite() },
                        onRateClick = { rating -> viewModel.rateMovie(rating) },
                        modifier = Modifier.weight(0.7f)
                    )
                }
            }
        }
    }

    if (showMovieSourceDialog) {
        Dialog(onDismissRequest = { showMovieSourceDialog = false }) {
            Box(
                modifier =
                    Modifier
                        .size(600.dp, 400.dp)
                        .background(Color(0xFF1F1F1F), RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            Color(0xFFE5A00D).copy(0.3f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(24.dp)
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            movie?.title ?: "",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1
                        )
                        IconButton(onClick = { showMovieSourceDialog = false }) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "CHOISISSEZ UNE SOURCE",
                        color = Color(0xFFE5A00D),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (movie?.servers.isNullOrEmpty()) {
                        Text("Aucune source disponible", color = Color(0xFFFF6B6B))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(movie!!.servers!!) { server ->
                                SourceItemWithActions(
                                    server = server,
                                    onPlayClick = {
                                        val fullUrl = server.streamUrl
                                        val playId = movie!!.ratingKey ?: movie!!.id
                                        onPlayVideo(
                                            fullUrl,
                                            movie!!.title,
                                            playId,
                                            startPositionMs
                                        )
                                        showMovieSourceDialog = false
                                    },
                                    onPlexClick = {
                                        openExternalLink(
                                            context,
                                            server.plexDeepLink ?: server.plexWebUrl,
                                            "Plex"
                                        )
                                    },
                                    onExternalClick = {
                                        openExternalLink(
                                            context,
                                            server.m3uUrl,
                                            "Video",
                                            "video/*"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEpisodeSourceDialog != null) {
        val episode = showEpisodeSourceDialog!!
        movie?.title ?: ""

        Dialog(onDismissRequest = { showEpisodeSourceDialog = null }) {
            Box(
                modifier =
                    Modifier
                        .size(600.dp, 400.dp)
                        .background(Color(0xFF1F1F1F), RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            Color(0xFFE5A00D).copy(0.3f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(24.dp)
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                "S${selectedSeason?.seasonNumber}E${episode.episodeNumber}",
                                color = Color(0xFFE5A00D),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                episode.title,
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1
                            )
                        }
                        IconButton(onClick = { showEpisodeSourceDialog = null }) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "CHOISISSEZ UNE SOURCE",
                        color = Color(0xFFE5A00D),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (episode.servers.isNullOrEmpty()) {
                        Text("Aucune source disponible", color = Color(0xFFFF6B6B))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(episode.servers) { server ->
                                SourceItemWithActions(
                                    server = server,
                                    onPlayClick = {
                                        val fullUrl = server.streamUrl
                                        // Use ratingKey if available, fallback to id, fallback to constructed ID
                                        val episodeId = episode.ratingKey ?: episode.id
                                        val episodeTitle = episode.title
                                        onPlayVideo(
                                            fullUrl,
                                            episodeTitle,
                                            episodeId,
                                            -1L
                                        ) // Start from beginning for episodes
                                        showEpisodeSourceDialog = null
                                    },
                                    onPlexClick = {
                                        openExternalLink(
                                            context,
                                            server.plexDeepLink ?: server.plexWebUrl,
                                            "Plex"
                                        )
                                    },
                                    onExternalClick = {
                                        openExternalLink(
                                            context,
                                            server.m3uUrl,
                                            "Video",
                                            "video/*"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- LOGIQUE UTILITAIRE ---

fun openExternalLink(
    context: android.content.Context,
    url: String?,
    name: String,
    mimeType: String? = null
) {
    if (url.isNullOrBlank()) {
        Toast.makeText(context, "Lien $name non disponible", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val intent = Intent(Intent.ACTION_VIEW)
        if (mimeType != null) {
            intent.setDataAndType(Uri.parse(url), mimeType)
        } else {
            intent.data = Uri.parse(url)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Impossible d'ouvrir $name", Toast.LENGTH_SHORT).show()
    }
}

// --- COMPOSANTS UI HELPERS ---

@Composable
fun BadgeInfo(text: String) {
    Box(
        modifier =
            Modifier
                .background(
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SeasonChip(season: Season, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50.dp)),
        colors =
            ClickableSurfaceDefaults.colors(
                containerColor =
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(0.1f),
                contentColor =
                    if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = MaterialTheme.colorScheme.primary,
                focusedContentColor = MaterialTheme.colorScheme.onPrimary
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
        colors =
            ClickableSurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
                focusedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${episode.episodeNumber}.",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(30.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    episode.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!episode.overview.isNullOrEmpty()) {
                    Text(
                        episode.overview,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                Icons.Default.PlayArrow,
                null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
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
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
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
                    modifier =
                        Modifier
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color.White
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Bouton 1 : Lire (Interne)
            Button(
                onClick = onPlayClick,
                colors =
                    ButtonDefaults.colors(
                        containerColor = Color(0xFF404040),
                        contentColor = Color.Black,
                        focusedContainerColor = Color(0xFFFFA500),
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
                    colors =
                        ButtonDefaults.colors(
                            containerColor = Color(0xFF404040),
                            contentColor = Color.White,
                            focusedContainerColor = Color(0xFFE5A00D),
                            focusedContentColor = Color.Black
                        ),
                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(4.dp)),
                    modifier = Modifier.width(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) { Icon(Icons.Default.ExitToApp, "Plex", modifier = Modifier.size(16.dp)) }
            }

            // Bouton 3 : Externe (M3U / VLC)
            if (!server.m3uUrl.isNullOrEmpty()) {
                Button(
                    onClick = onExternalClick,
                    colors =
                        ButtonDefaults.colors(
                            containerColor = Color(0xFFE65100).copy(alpha = 0.8f),
                            contentColor = Color.White,
                            focusedContainerColor = Color(0xFFFF9800),
                            focusedContentColor = Color.Black
                        ),
                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(4.dp)),
                    modifier = Modifier.width(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) { Icon(Icons.Default.Share, "Externe", modifier = Modifier.size(14.dp)) }
            }
        }
    }
}
