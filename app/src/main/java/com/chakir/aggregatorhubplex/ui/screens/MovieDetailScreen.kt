package com.chakir.aggregatorhubplex.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.chakir.aggregatorhubplex.domain.model.Movie
import com.chakir.aggregatorhubplex.ui.components.ActorChip
import com.chakir.aggregatorhubplex.ui.components.Rating
import com.chakir.aggregatorhubplex.ui.components.SimilarMediaSection

@OptIn(ExperimentalTvMaterial3Api::class)
/**
 * Écran de détail spécifique pour les films. Affiche les informations détaillées, les informations
 * techniques (audio/sous-titres), le casting et les films similaires.
 *
 * @param movie Le film à afficher.
 * @param onPlayClick Callback pour lancer la lecture.
 * @param onSimilarItemClick Callback lors du clic sur un film similaire.
 * @param modifier Modificateur pour l'interface.
 */
@Composable
fun MovieDetailScreen(
    movie: Movie,
    onPlayClick: () -> Unit,
    onTrailerClick: (String) -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onRateClick: (Float) -> Unit = {},
    onSimilarItemClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    // Pour l'action "Rate", on peut utiliser un petit state local pour afficher un dialogue si besoin,
    // ou simplement appeler le callback avec une valeur fixe pour l'exemple (ex: incrémenter).
    // Ici on suppose que le parent gère l'UI de notation si complexe.

    Column(modifier = modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(16.dp)) {
        Text("Films > VM NAS Plus", color = Color.White, fontSize = 9.sp)

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
                "${movie.year ?: ""}  •  ${(movie.runtime?.toLong() ?: movie.duration) / 60000} min  •  ${
                    movie.genres?.joinToString(
                        ", "
                    ) ?: ""
                }",
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onPlayClick,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE5A00D),
                        contentColor = Color.Black
                    )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Lecture")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lecture")
            }

            // Bouton Bande-annonce
            if (!movie.trailers.isNullOrEmpty()) {
                val trailerUrl = movie.trailers.firstOrNull()?.trailerStreamUrl
                if (!trailerUrl.isNullOrEmpty()) {
                    Button(
                        onClick = { onTrailerClick(trailerUrl) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF404040),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Bande-annonce"
                        ) // TODO: Icone Film/Trailer
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bande-annonce")
                    }
                }
            }

            // Bouton Favoris
            IconButton(onClick = onFavoriteClick) {
                // Idéalement on passe l'état isFavorite en paramètre du composable pour changer l'icône
                Icon(
                    Icons.Default.FavoriteBorder, // ou Favorite si true
                    contentDescription = "Favoris",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            // Bouton Noter (Exemple simple)
            IconButton(onClick = { onRateClick(5f) }) { // TODO: Ouvrir un dialogue de notation
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Noter",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(onClick = { /*TODO: More actions*/ }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Plus",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = movie.description ?: "Aucune description disponible.",
            color = Color.White,
            fontSize = 14.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Détails du média
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Informations Média",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFE5A00D),
                fontSize = 11.sp
            )

            Text(
                "Vidéo: ${movie.servers?.firstOrNull()?.resolution ?: "N/A"}",
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

        // Casting
        Text(
            "Casting et équipe",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(movie.actors ?: emptyList()) { actor -> ActorChip(actor = actor) }
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
