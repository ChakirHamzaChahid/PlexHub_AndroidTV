package com.chakir.aggregatorhubplex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.tv.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import android.util.Log
import androidx.tv.material3.ExperimentalTvMaterial3Api
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chakir.aggregatorhubplex.domain.model.Episode

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EpisodeDetailScreen(
    seriesId: String,
    episodeId: String,
    onPlayVideo: (videoUrl: String, title: String, id: String, position: Long, serverName: String, showId: String?, posterUrl: String?, type: String) -> Unit,
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    LaunchedEffect(seriesId) { viewModel.loadMovie(seriesId) }

    val movie by viewModel.movie.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    // Find the current episode from the loaded series
    val currentEpisode = remember(movie, episodeId) {
        movie?.seasons?.flatMap { it.episodes }?.find {
            it.id == episodeId || it.ratingKey == episodeId
        }
    }

    // Find season for the episode
    val currentSeason = remember(movie, currentEpisode) {
        movie?.seasons?.find { season ->
            season.episodes.any { it.id == episodeId || it.ratingKey == episodeId }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
    } else if (movie != null && currentEpisode != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(currentEpisode.stillUrl ?: movie!!.backdropUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.2f
            )

            // Gradients
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black,
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent
                            ),
                            startX = 0f,
                            endX = 1200f
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black
                            ),
                            startY = 0f,
                            endY = 2000f
                        )
                    )
            )

            Row(
                modifier = Modifier
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
                        model = ImageRequest.Builder(context)
                            .data(movie!!.posterUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Series Poster",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(2 / 3f)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                // Right Column: Content
                Column(modifier = Modifier.weight(0.7f)) {
                    // Header (Back + Title)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Retour",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = movie!!.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = " > S${currentSeason?.seasonNumber} : E${currentEpisode.episodeNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Episode Title
                    Text(
                        text = currentEpisode.title,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxSize()) {
                        // Info Column
                        Column(
                            modifier = Modifier
                                .weight(0.6f)
                                .padding(end = 24.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BadgeInfo("S${currentSeason?.seasonNumber} E${currentEpisode.episodeNumber}")
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = currentEpisode.overview ?: "Aucune description disponible.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFFCCCCCC),
                                lineHeight = 24.sp
                            )
                        }

                        // Sources Column
                        Column(
                            modifier = Modifier
                                .weight(0.4f)
                                .background(
                                    Color(0xFF1F1F1F).copy(alpha = 0.9f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(24.dp)
                        ) {
                            Text(
                                "SOURCES DISPONIBLES",
                                color = Color(0xFFE5A00D),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            if (currentEpisode.servers.isNullOrEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Aucune source disponible", color = Color(0xFFFF6B6B))
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(currentEpisode.servers!!) { server ->
                                        CompactSourceItem(
                                            server = server,
                                            onPlayClick = {
                                                val fullUrl = server.streamUrl
                                                val playId =
                                                    currentEpisode.ratingKey ?: currentEpisode.id
                                                val episodeTitle = currentEpisode.title
                                                onPlayVideo(
                                                    fullUrl,
                                                    episodeTitle,
                                                    playId,
                                                    -1L, // Start from beginning
                                                    server.name,
                                                    seriesId,
                                                    currentEpisode.thumbUrl ?: movie!!.posterUrl,
                                                    "episode"
                                                )
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
    } else {
        // Error or Not Found State
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Épisode introuvable",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("SeriesID: $seriesId", color = Color.Gray)
                Log.d("SeriesID:", "SeriesID: $seriesId")
                Log.d("EpisodeID:", "EpisodeID: $episodeId")
                Log.d("Movie Loaded:", "Movie Loaded: ${movie != null}")

                Text("EpisodeID: $episodeId", color = Color.Gray)
                Text("Movie Loaded: ${movie != null}", color = Color.Gray)
                if (movie != null) {
                    Text("Seasons: ${movie?.seasons?.size ?: 0}", color = Color.Gray)
                    Log.d("Seasons:", "Seasons: ${movie?.seasons?.size ?: 0}")
                    val allEpisodes = movie?.seasons?.flatMap { it.episodes } ?: emptyList()
                    Text("Total Episodes: ${allEpisodes.size}", color = Color.Gray)
                    Log.d("Total Episodes:", "Total Episodes: ${allEpisodes.size}")
                    if (allEpisodes.isNotEmpty()) {
                        Text(
                            "First Ep ID: ${allEpisodes.first().id} / RK: ${allEpisodes.first().ratingKey}",
                            color = Color.Gray
                        )
                        Log.d(
                            "First Ep ID:",
                            "First Ep ID: ${allEpisodes.first().id} / RK: ${allEpisodes.first().ratingKey}"
                        )
                    }
                }
            }
            Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) {
                Text("Retour")
            }
        }
    }
}

@Composable
fun CompactSourceItem(
    server: com.chakir.aggregatorhubplex.domain.model.Server,
    onPlayClick: () -> Unit,
    onPlexClick: () -> Unit,
    onExternalClick: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color(0xFF2A2A2A), RoundedCornerShape(6.dp))
                .padding(6.dp) // Reduced padding
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp), // Reduced bottom padding
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = server.name,
                style = MaterialTheme.typography.bodySmall, // Smaller font
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
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), // Smaller font
                        color = Color.White
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp), // Reduced spacing
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
                contentPadding = PaddingValues(
                    horizontal = 8.dp,
                    vertical = 2.dp
                ), // Compact padding
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp) // Fixed small height
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Lire", style = MaterialTheme.typography.labelSmall)
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
                    modifier = Modifier
                        .width(32.dp)
                        .height(32.dp), // Smaller square button
                    contentPadding = PaddingValues(0.dp)
                ) { Icon(Icons.Default.ExitToApp, "Plex", modifier = Modifier.size(14.dp)) }
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
                    modifier = Modifier
                        .width(32.dp)
                        .height(32.dp), // Smaller square button
                    contentPadding = PaddingValues(0.dp)
                ) { Icon(Icons.Default.Share, "Externe", modifier = Modifier.size(12.dp)) }
            }
        }
    }
}
