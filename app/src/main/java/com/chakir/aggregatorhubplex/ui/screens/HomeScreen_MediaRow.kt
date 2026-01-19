package com.chakir.aggregatorhubplex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chakir.aggregatorhubplex.domain.model.Movie
import com.chakir.aggregatorhubplex.ui.components.MovieCard
import com.chakir.aggregatorhubplex.ui.theme.Dimens

// Helper Composable for Rows
@Composable
fun MediaRow(title: String, movies: List<Movie>, onMovieClick: (Movie) -> Unit, plexColor: Color) {
    Column(modifier = Modifier.padding(bottom = Dimens.spacing_l)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = Dimens.spacing_xxxl, bottom = Dimens.spacing_m)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = Dimens.spacing_xxxl),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_m)
        ) {
            items(movies) { movie ->
                MovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie) },
                    plexColor = plexColor,
                    modifier = Modifier.width(150.dp)
                )
            }
        }
    }
}
