package com.chakir.aggregatorhubplex.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.chakir.aggregatorhubplex.domain.model.Movie

/** Carte standard pour un film ou une série. */
@Composable
fun MovieCard(
        movie: Movie,
        onClick: () -> Unit,
        plexColor: Color = Color(0xFFE5A00D),
        modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column(
            modifier =
                    modifier
                            .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = onClick
                            )
    ) {
        Card(
                shape = RoundedCornerShape(8.dp),
                colors =
                        CardDefaults.cardColors(
                                containerColor = if (isFocused) Color.White else Color.Transparent
                        ),
                modifier =
                        Modifier.fillMaxWidth().aspectRatio(2f / 3f).clip(RoundedCornerShape(8.dp))
        ) {
            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                        model = movie.posterUrl,
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                )
            }
        }
        Text(
                text = movie.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isFocused) plexColor else Color.White,
                modifier = Modifier.padding(top = 8.dp)
        )
    }
}
