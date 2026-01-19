package com.chakir.aggregatorhubplex.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chakir.aggregatorhubplex.domain.model.Season
import com.chakir.aggregatorhubplex.ui.theme.TextPrimary

/**
 * Carte affichant les informations d'une saison (affiche, numéro). Utilise l'image du premier
 * épisode si disponible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonCard(season: Season, onClick: () -> Unit) {
    Card(
            onClick = onClick,
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor = Color(0xFF1F1F1F), // Fond sombre Plex
                            contentColor = TextPrimary
                    ),
            modifier = Modifier.padding(8.dp)
    ) {
        Column {
            AsyncImage(
                    model =
                            ImageRequest.Builder(LocalContext.current)
                                    .data(season.episodes.firstOrNull()?.stillUrl ?: "")
                                    .crossfade(true)
                                    .build(),
                    contentDescription = season.title,
                    contentScale = ContentScale.Crop,
                    modifier =
                            Modifier.aspectRatio(16 / 9f)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                    text = "Saison ${season.seasonNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
