package com.chakir.aggregatorhubplex.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Carte de chargement (Squelette) avec animation de scintillement (Shimmer). Utilisé pour indiquer
 * le chargement des listes de médias.
 */
@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    // Animation de scintillement (Shimmer)
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by
            transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1000f,
                    animationSpec =
                            infiniteRepeatable(
                                    animation =
                                            tween(
                                                    durationMillis = 1200,
                                                    easing = FastOutSlowInEasing
                                            ),
                                    repeatMode = RepeatMode.Restart
                            ),
                    label = "shimmer_float"
            )

    val shimmerColors =
            listOf(
                    Color.White.copy(alpha = 0.05f),
                    Color.White.copy(alpha = 0.15f), // Point lumineux
                    Color.White.copy(alpha = 0.05f),
            )

    val brush =
            Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset.Zero,
                    end = Offset(x = translateAnim, y = translateAnim)
            )

    Column(
            modifier =
                    modifier.width(110.dp) // Même largeur que MovieCard
                            .padding(bottom = 16.dp)
    ) {
        // 1. Poster Squelette
        Box(
                modifier =
                        Modifier.height(165.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1F1F1F)) // Fond gris sombre
                                .background(brush) // Animation par dessus
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Titre Squelette (Petite ligne)
        Box(
                modifier =
                        Modifier.height(12.dp)
                                .fillMaxWidth(0.8f) // 80% de la largeur
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1F1F1F))
                                .background(brush)
        )
    }
}
