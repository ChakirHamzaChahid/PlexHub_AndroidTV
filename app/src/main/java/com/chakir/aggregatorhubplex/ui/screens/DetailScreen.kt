package com.chakir.aggregatorhubplex.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.chakir.aggregatorhubplex.data.Episode
import com.chakir.aggregatorhubplex.data.Movie
import com.chakir.aggregatorhubplex.data.NetworkModule
import com.chakir.aggregatorhubplex.data.Season
import com.chakir.aggregatorhubplex.data.Server

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DetailScreen(
    movieId: String,
    onPlayVideo: (String, String, String) -> Unit, // <--- NOUVEAU : URL, Titre, ID
    viewModel: DetailViewModel = hiltViewModel() // Injection du ViewModel
) {
    // --- ÉTATS ---
    // On utilise le ViewModel pour le chargement principal
    LaunchedEffect(movieId) {
        viewModel.loadMovie(movieId)
    }

    val movie by viewModel.movie.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // États locaux pour les séries (Saisons/Episodes)
    var seasons by remember { mutableStateOf<List<Season>>(emptyList()) }
    var selectedSeason by remember { mutableStateOf<Season?>(null) }
    var showEpisodeSourceDialog by remember { mutableStateOf<Episode?>(null) }

    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // --- CHARGEMENT DES SAISONS (Si Série) ---
    // On garde cette logique ici pour l'instant
    LaunchedEffect(movie) {
        if (movie != null && movie!!.isSeries) {
            try {
                val fetchedSeasons = NetworkModule.api.getShowSeasons(movieId)
                seasons = fetchedSeasons
                selectedSeason = fetchedSeasons.firstOrNull()
            } catch (e: Exception) {
                if (!movie!!.seasons.isNullOrEmpty()) {
                    seasons = movie!!.seasons!!
                    selectedSeason = seasons.firstOrNull()
                }
            }
        }
    }

    // --- RENDU UI ---
    val currentMovie = movie

    // Si chargement
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
    // Si film chargé
    else if (currentMovie != null) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

            // 1. IMAGE DE FOND (Backdrop)
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(currentMovie.backdropUrl).crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alpha = 0.4f
                )
                // Dégradés pour fondre l'image dans le fond noir
                Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.background, Color.Transparent))))
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background))))
            }

            // 2. CONTENU PRINCIPAL
            Row(modifier = Modifier.fillMaxSize().padding(50.dp)) {

                // --- COLONNE GAUCHE (INFOS) ---
                Column(modifier = Modifier.weight(0.55f).verticalScroll(scrollState).padding(end = 32.dp)) {

                    // Ligne Méta-data (Année | Rating | Studio)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${currentMovie.year ?: ""}", color = MaterialTheme.colorScheme.onBackground.copy(0.7f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        if (!currentMovie.contentRating.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(12.dp))
                            BadgeInfo(text = currentMovie.contentRating)
                        }
                        if (!currentMovie.studio.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(currentMovie.studio.uppercase(), color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // TITRE GÉANT
                    Text(
                        text = currentMovie.title.uppercase(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 42.sp,
                            letterSpacing = (-1).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 46.sp
                    )

                    // Sous-titre
                    Spacer(modifier = Modifier.height(8.dp))
                    val directorText = if (!currentMovie.director.isNullOrBlank()) "DIRIGÉ PAR ${currentMovie.director.uppercase()}" else if (currentMovie.isSeries) "SÉRIE TV" else "FILM"
                    Text(directorText, color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)

                    Spacer(modifier = Modifier.height(24.dp))

                    // ACTIONS (FAVORIS)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Bouton Favori
                        Button(
                            onClick = { viewModel.toggleFavorite() },
                            colors = ButtonDefaults.colors(
                                containerColor = if (isFavorite) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surface,
                                contentColor = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                focusedContainerColor = if (isFavorite) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.primary,
                                focusedContentColor = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = ButtonDefaults.shape(shape = RoundedCornerShape(50)),
                            modifier = Modifier.size(48.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favoris",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Ajouter aux favoris", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // NOTES (IMDB / ROTTEN)
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.1f), RoundedCornerShape(4.dp))
                            .height(50.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // IMDB
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("IMDB", color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("${currentMovie.imdbRating ?: currentMovie.rating ?: "-"}", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                Text("/10", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 2.dp))
                            }
                        }

                        // Séparateur vertical
                        Box(modifier = Modifier.width(1.dp).fillMaxHeight(0.6f).background(MaterialTheme.colorScheme.onSurface.copy(0.1f)))

                        // ROTTEN TOMATOES
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("ROTTEN TOMATOES", color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val score = currentMovie.rottenRating ?: ((currentMovie.rating ?: 0f) * 10).toInt()
                                Text("$score%", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🍅", fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // SYNOPSIS
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.width(4.dp).height(80.dp).background(MaterialTheme.colorScheme.primary))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = currentMovie.description ?: "Aucune description disponible.",
                            color = MaterialTheme.colorScheme.onBackground.copy(0.8f),
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                // --- COLONNE DROITE (CONTENU / SOURCES) ---
                Column(modifier = Modifier.weight(0.45f).fillMaxHeight().padding(start = 24.dp)) {

                    // CAS SÉRIES TV
                    if (currentMovie.isSeries) {
                        Text("SAISONS", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(seasons) { season ->
                                SeasonChip(season, season == selectedSeason) { selectedSeason = season }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        if (selectedSeason != null) {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 50.dp)
                            ) {
                                items(selectedSeason!!.episodes) { episode ->
                                    EpisodeItem(episode) {
                                        // Ouvre le popup de sources pour l'épisode
                                        showEpisodeSourceDialog = episode
                                    }
                                }
                            }
                        }
                    }
                    // CAS FILMS
                    else {
                        Text("SOURCES DISPONIBLES", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(currentMovie.servers ?: emptyList()) { server ->
                                SourceItemWithActions(
                                    server = server,
                                    onPlayClick = {
                                        // ACTION : Lance le lecteur (URL, Titre, ID)
                                        val fullUrl = buildFullUrl(server.streamUrl)
                                        onPlayVideo(fullUrl, currentMovie.title, currentMovie.id)
                                    },
                                    onPlexClick = { openExternalLink(context, server.plexDeepLink ?: server.plexWebUrl, "Plex") },
                                    onExternalClick = { openExternalLink(context, server.m3uUrl, "Video", "video/*") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- POPUP ÉPISODES ---
    if (showEpisodeSourceDialog != null) {
        val episode = showEpisodeSourceDialog!!
        val currentMovieTitle = movie?.title ?: ""

        Dialog(onDismissRequest = { showEpisodeSourceDialog = null }) {
            Box(modifier = Modifier.size(600.dp, 400.dp).background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp)).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.1f), RoundedCornerShape(16.dp)).padding(24.dp)) {
                Column {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("S${selectedSeason?.seasonNumber}E${episode.episodeNumber}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text(episode.title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge, maxLines = 1)
                        }
                        IconButton(onClick = { showEpisodeSourceDialog = null }) {
                            Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("CHOISISSEZ UNE SOURCE", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (episode.servers.isNullOrEmpty()) {
                        Text("Aucune source disponible", color = MaterialTheme.colorScheme.error)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(episode.servers) { server ->
                                SourceItemWithActions(
                                    server = server,
                                    onPlayClick = {
                                        // ACTION : Lance l'épisode
                                        val fullUrl = buildFullUrl(server.streamUrl)
                                        // ID composite pour l'historique : FilmID_S01E05
                                        val episodeId = "${movie?.id}_S${selectedSeason?.seasonNumber}E${episode.episodeNumber}"
                                        val episodeTitle = "$currentMovieTitle - S${selectedSeason?.seasonNumber}E${episode.episodeNumber}"

                                        onPlayVideo(fullUrl, episodeTitle, episodeId)
                                        showEpisodeSourceDialog = null
                                    },
                                    onPlexClick = { openExternalLink(context, server.plexDeepLink ?: server.plexWebUrl, "Plex") },
                                    onExternalClick = { openExternalLink(context, server.m3uUrl, "Video", "video/*") }
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

fun buildFullUrl(url: String): String {
    return if (url.startsWith("http")) {
        url
    } else {
        "${NetworkModule.currentBaseUrl.trimEnd('/')}/${url.trimStart('/')}"
    }
}

fun openExternalLink(context: android.content.Context, url: String?, name: String, mimeType: String? = null) {
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
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SeasonChip(season: Season, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(0.1f),
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
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
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            focusedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${episode.episodeNumber}.", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(episode.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!episode.overview.isNullOrEmpty()) {
                    Text(episode.overview, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
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
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
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
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            server.resolution?.let {
                Box(
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(it, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.White)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            // Bouton 1 : Lire (Interne)
            Button(
                onClick = onPlayClick,
                colors = ButtonDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContentColor = MaterialTheme.colorScheme.onSurface
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
                        containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.primary,
                        focusedContentColor = MaterialTheme.colorScheme.onPrimary
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
