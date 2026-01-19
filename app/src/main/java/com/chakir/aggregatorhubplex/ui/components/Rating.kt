package com.chakir.aggregatorhubplex.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun Rating(modifier: Modifier = Modifier, rating: Float, maxRating: Int = 5) {
    Row(modifier = modifier) {
        val scaledRating = (rating / 10f) * maxRating
        val fullStars = scaledRating.toInt()
        val halfStar = if (scaledRating - fullStars >= 0.5f) 1 else 0
        val emptyStars = maxRating - fullStars - halfStar

        repeat(fullStars) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Color.Yellow)
        }

        if (halfStar == 1) {
            Icon(Icons.Filled.StarHalf, contentDescription = null, tint = Color.Yellow)
        }

        repeat(emptyStars) {
            Icon(Icons.Filled.StarOutline, contentDescription = null, tint = Color.Yellow)
        }
    }
}
