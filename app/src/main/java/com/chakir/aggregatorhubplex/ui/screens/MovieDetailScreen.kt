package com.chakir.aggregatorhubplex.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.chakir.aggregatorhubplex.data.Movie
import com.chakir.aggregatorhubplex.ui.components.Rating

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movie: Movie,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .weight(0.7f)
            .verticalScroll(scrollState)
    ) {
        Text(
            "Films > VM NAS Plus",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = movie.title,
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${movie.year ?: ""}  •  ${movie.genres?.joinToString(", ") ?: ""}",
                color = MaterialTheme.colorScheme.onBackground.copy(0.7f),
                fontSize = 14.sp,
            )
        }
        movie.rating?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Rating(rating = it, maxRating = 5)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { /* TODO: Resume video */ }) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Reprendre")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reprendre")
            }
            // TODO: Add other action buttons
        }


        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = movie.description ?: "Aucune description disponible.",
            color = MaterialTheme.colorScheme.onBackground.copy(0.8f),
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(32.dp))
        // ... Non-series content ...
    }
}
