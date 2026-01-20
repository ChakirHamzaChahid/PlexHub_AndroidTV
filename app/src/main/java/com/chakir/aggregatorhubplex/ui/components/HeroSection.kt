package com.chakir.aggregatorhubplex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chakir.aggregatorhubplex.domain.model.Movie
import com.chakir.aggregatorhubplex.ui.theme.Dimens

@Composable
fun HeroSection(
    movie: Movie,
    onPlayClick: () -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(500.dp)
    ) {
        // Background Image with Gradient Overlay
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(movie.backdropPath ?: movie.posterPath)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient Overlay (Dark at bottom/left, transparent at top/right)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x80000000),
                            Color(0xFF141414) // Matches app background
                        ),
                        startY = 0f
                    )
                )
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                             Color(0xFF141414),
                             Color(0x80141414),
                             Color.Transparent
                        ),
                        endX = 1000f
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = Dimens.spacing_xxxl, bottom = Dimens.spacing_xxl, end = Dimens.spacing_xxxl)
                .fillMaxWidth(0.6f) // Take up 60% of width
        ) {
            // Title
            Text(
                text = movie.title,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_s))

            // Metadata Row (Rating, Year, Duration)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if ((movie.rating ?: 0f) > 0) {
                    Text(
                        text = "★ ${String.format("%.1f", movie.rating ?: 0f)}",
                        color = Color(0xFFE5A00D), // Plex Orange
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(Dimens.spacing_m))
                }
                
                if ((movie.year ?: 0) > 0) {
                     Text(
                        text = "${movie.year ?: ""}",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.width(Dimens.spacing_m))
                }
                
                if (movie.duration > 0) {
                    val durationMs = movie.duration
                    val hours = durationMs / (1000 * 60 * 60)
                    val minutes = (durationMs / (1000 * 60)) % 60
                     Text(
                        text = "${hours}h ${minutes}m",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_m))

            // Description
            Text(
                text = movie.description ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_l))

            // Buttons
            Row {
                Button(
                    onClick = onPlayClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play"
                    )
                    Spacer(modifier = Modifier.width(Dimens.spacing_xs))
                    Text(text = "Lecture", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(Dimens.spacing_m))

                Button(
                    onClick = onDetailsClick,
                    colors = ButtonDefaults.buttonColors(
                         containerColor = Color.White.copy(alpha = 0.2f),
                         contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                     Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info"
                    )
                    Spacer(modifier = Modifier.width(Dimens.spacing_xs))
                    Text(text = "Plus d'infos", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
