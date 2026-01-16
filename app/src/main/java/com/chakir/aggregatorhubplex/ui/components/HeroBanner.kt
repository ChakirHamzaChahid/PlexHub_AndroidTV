package com.chakir.aggregatorhubplex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
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
import androidx.tv.material3.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chakir.aggregatorhubplex.data.Movie
import com.chakir.aggregatorhubplex.ui.screens.PlexAccent

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HeroBanner(
    movie: Movie,
    onPlayClick: () -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(350.dp) // Hauteur légèrement réduite pour économiser du GPU
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(Color.DarkGray) // Couleur de fond par défaut pendant le chargement
    ) {
        // 1. IMAGE DE FOND
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(movie.backdropUrl ?: movie.posterUrl)
                .crossfade(true)
                // Important : on limite la taille en mémoire pour éviter de saturer
                .size(1280, 720)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. DÉGRADÉ UNIQUE (Optimisé)
        // Au lieu de 2 Box superposées, on en met une seule avec un dégradé diagonal sombre
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF141414).copy(alpha = 0.9f), // Plus opaque en bas
                            Color(0xFF141414)
                        )
                    )
                )
        )

        // 3. CONTENU
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 48.dp, bottom = 32.dp)
                .fillMaxWidth(0.7f)
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.displaySmall, // Typo un peu plus petite
                color = Color.White,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Boutons d'action
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = onPlayClick,
                    colors = ButtonDefaults.colors(containerColor = PlexAccent, contentColor = Color.Black),
                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(8.dp)),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lecture", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Button(
                    onClick = onDetailsClick,
                    colors = ButtonDefaults.colors(containerColor = Color.White.copy(alpha = 0.2f), contentColor = Color.White),
                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(8.dp)),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("Détails", fontSize = 14.sp)
                }
            }
        }
    }
}