package com.chakir.aggregatorhubplex.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chakir.aggregatorhubplex.domain.model.Movie
import com.chakir.aggregatorhubplex.ui.theme.PlexOrange
import com.chakir.aggregatorhubplex.ui.theme.TextSecondary

/**
 * Carrousel mis en avant (Featured) affichant les nouveautés ou tendances. Utilise un [LazyRow]
 * pour le défilement horizontal.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FeaturedCarousel(
        movies: List<Movie>,
        onMovieClick: (Movie) -> Unit,
        modifier: Modifier = Modifier
) {
    LazyRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(horizontal = 48.dp)
    ) {
        items(movies) { movie ->
            FeaturedMovieCard(movie = movie, onClick = { onMovieClick(movie) })
        }
    }
}

/** Carte individuelle pour un élément mis en avant. Affiche l'affiche, le titre et la note. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun FeaturedMovieCard(movie: Movie, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Card(
            onClick = onClick,
            modifier = modifier.width(180.dp) // Largeur de la carte
    ) {
        Column {
            AsyncImage(
                    model =
                            ImageRequest.Builder(context)
                                    .data(movie.posterUrl)
                                    .crossfade(true)
                                    .build(),
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier =
                            Modifier.fillMaxWidth()
                                    .height(270.dp) // Hauteur de l'image
                                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                    text = movie.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
            )

            val rating = movie.imdbRating ?: movie.rating
            if (rating != null && rating > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = PlexOrange,
                            modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                            text = String.format("%.1f", rating),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                    )
                }
            }
        }
    }
}
