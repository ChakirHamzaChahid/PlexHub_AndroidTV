package com.chakir.aggregatorhubplex.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.chakir.aggregatorhubplex.ui.theme.Dimens

@Composable
fun SectionRow(
    title: String,
    items: List<Movie>,
    onItemClick: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE5E5E5),
            modifier = Modifier.padding(start = Dimens.spacing_xxxl, bottom = Dimens.spacing_m)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = Dimens.spacing_xxxl),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_m),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items) { movie ->
                 MovieCard(
                    movie = movie,
                    onClick = { onItemClick(movie) },
                    plexColor = Color(0xFFE5A00D),
                    modifier = Modifier.width(150.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(Dimens.spacing_xl))
    }
}
