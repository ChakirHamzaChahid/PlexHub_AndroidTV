package com.chakir.aggregatorhubplex.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.chakir.aggregatorhubplex.domain.model.Episode
import com.chakir.aggregatorhubplex.domain.model.Movie
import com.chakir.aggregatorhubplex.domain.model.Season
import com.chakir.aggregatorhubplex.ui.components.Rating
import com.chakir.aggregatorhubplex.ui.components.SeasonCard
import com.chakir.aggregatorhubplex.ui.components.SimilarMediaSection

@OptIn(ExperimentalTvMaterial3Api::class)
/**
 * Écran de détail spécifique pour les séries TV. Affiche les informations de la série, la liste des
 * saisons et des épisodes.
 *
 * @param movie La série à afficher.
 * @param seasons Liste des saisons disponibles.
 * @param selectedSeason La saison actuellement sélectionnée.
 * @param onSeasonSelected Callback lorsqu'une saison est sélectionnée.
 * @param onEpisodeClick Callback lorsqu'un épisode est cliqué.
 * @param onSimilarItemClick Callback pour les médias similaires.
 * @param modifier Modificateur d'interface.
 */
@Composable
fun SeriesDetailScreen(
        movie: Movie,
        seasons: List<Season>,
        selectedSeason: Season?,
        onSeasonSelected: (Season) -> Unit,
        onEpisodeClick: (Episode) -> Unit,
        onSimilarItemClick: (String) -> Unit = {},
        modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp)) {
        Text("Séries Anime > VM NAS Plus", color = Color.White, fontSize = 9.sp)

        Text(
                text = movie.title,
                style =
                        MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                        ),
                color = Color.White,
        )

        movie.director?.let {
            Text(
                    text = it,
                    color = Color(0xFFE5A00D),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                    "${movie.year ?: ""}  •  ${movie.genres?.joinToString(", ") ?: ""}",
                    color = Color.White,
                    fontSize = 9.sp,
            )
        }
        movie.rating?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Rating(rating = it, maxRating = 5)
                movie.imdbRating?.let { imdbRating ->
                    Text(
                            text = "IMDb: ${"%.1f".format(imdbRating)}/10",
                            color = Color(0xFFE5A00D),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                    onClick = { /* TODO: Resume video */},
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE5A00D),
                                    contentColor = Color.Black
                            )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Reprendre")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reprendre")
            }
            // TODO : Ajouter d'autres boutons d'action
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
                text = movie.description ?: "Aucune description disponible.",
                color = Color.White,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Détails du média
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                    "Informations Média",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFE5A00D),
                    fontSize = 11.sp
            )

            Text(
                    "Video: ${movie.servers?.firstOrNull()?.resolution ?: "N/A"}",
                    color = Color.White,
                    fontSize = 9.sp
            )

            // Pistes Audio
            if (!movie.audioTracks.isNullOrEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                            "🔊 Pistes Audio (${movie.audioTracks!!.size}):",
                            color = Color(0xFFE5A00D),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                    )
                    movie.audioTracks!!.forEach { track ->
                        Text(
                                "  • ${track.displayTitle} - ${track.language} (${track.codec}, ${track.channels}ch)",
                                color = Color.White,
                                fontSize = 8.sp
                        )
                    }
                }
            }

            // Sous-titres
            if (!movie.subtitles.isNullOrEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                            "📝 Sous-titres (${movie.subtitles!!.size}):",
                            color = Color(0xFFE5A00D),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                    )
                    movie.subtitles!!.forEach { subtitle ->
                        Text(
                                "  • ${subtitle.displayTitle} - ${subtitle.language} (${subtitle.codec})",
                                color = Color.White,
                                fontSize = 8.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
                "Saisons",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(seasons) { season -> SeasonCard(season = season) { onSeasonSelected(season) } }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (selectedSeason != null) {
            // Utilisation d'une simple colonne pour l'instant, LazyColumn dans un verticalScroll
            // n'est pas idéal
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                selectedSeason.episodes.forEach { episode ->
                    EpisodeItem(episode) { onEpisodeClick(episode) }
                }
            }
        }

        // Section Médias Similaires
        if ((movie.similar?.isNotEmpty() == true) || (movie.similar?.size ?: 0) > 0) {
            SimilarMediaSection(
                    similarItems = movie.similar ?: emptyList(),
                    onItemClick = { similarItem -> onSimilarItemClick(similarItem.id) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
