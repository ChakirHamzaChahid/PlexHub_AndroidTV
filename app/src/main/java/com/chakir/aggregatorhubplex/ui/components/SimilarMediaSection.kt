package com.chakir.aggregatorhubplex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.chakir.aggregatorhubplex.domain.model.SimilarItem

/**
 * Section affichant des médias similaires ou recommandés. Affiche une liste horizontale défilante.
 */
@Composable
fun SimilarMediaSection(
    similarItems: List<SimilarItem>,
    isLoading: Boolean = false,
    onItemClick: (SimilarItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (similarItems.isEmpty() && !isLoading) return

    Column(modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 24.dp)) {
        // Section Title
        Text(
            text = "Vous aimerez aussi",
            style =
                androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Loading state
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = Color(0xFFE5A00D)) }
        } else {
            // Lazy row with similar items
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(items = similarItems, key = { it.id }) { item ->
                    SimilarMediaCard(item = item, onClick = { onItemClick(item) })
                }
            }
        }
    }
}

/**
 * Carte individuelle pour un média similaire. Affiche l'image (miniature), le titre, l'année et la
 * note.
 */
@Composable
fun SimilarMediaCard(item: SimilarItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .width(150.dp)
                .height(225.dp)
                .background(color = Color.DarkGray, shape = RoundedCornerShape(8.dp))
                .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Poster Image
        if (item.thumbUrl != null) {
            AsyncImage(
                model = item.thumbUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
        } else {
            // Placeholder for missing thumbnail
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF424242)),
                contentAlignment = Alignment.Center
            ) { Text(text = "🎬", fontSize = 48.sp) }
        }

        // Overlay with info
        Box(modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black.copy(alpha = 0.6f)))

        // Title and rating overlay (bottom)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.year > 0) {
                    Text(text = item.year.toString(), color = Color.Gray, fontSize = 10.sp)
                }

                if (item.rating > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(text = "★", color = Color(0xFFFFB800), fontSize = 10.sp)
                        Text(
                            text = String.format("%.1f", item.rating),
                            color = Color(0xFFFFB800),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
