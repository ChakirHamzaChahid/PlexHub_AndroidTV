package com.chakir.aggregatorhubplex.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chakir.aggregatorhubplex.domain.model.Movie
import com.chakir.aggregatorhubplex.ui.theme.Dimens

/**
 * Carte de média affichant l'image et le titre. Gère le focus et l'animation de mise à l'échelle.
 * (Restored from Legacy Implementation)
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieCard(
    movie: Movie,
    onClick: () -> Unit,
    plexColor: Color = Color(0xFFE5A00D),
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val context = LocalContext.current

    val scale by
    animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1f,
        animationSpec =
        spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .scale(scale)
            .zIndex(if (isFocused) 10f else 1f)
    ) {
        Surface(
            onClick = { onClick() },
            shape =
            ClickableSurfaceDefaults.shape(
                shape = RoundedCornerShape(Dimens.spacing_s)
            ),
            modifier =
            Modifier
                .height(165.dp)
                .fillMaxWidth()
                .focusable(interactionSource = interactionSource)
        ) {
            AsyncImage(
                model =
                ImageRequest.Builder(context)
                    .data(movie.posterUrl)
                    .crossfade(true)
                    .size(300, 450)
                    .build(),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().background(Color.DarkGray)
            )

            if (isFocused) {
                Box(
                    modifier =
                    Modifier
                        .fillMaxSize()
                        .border(
                            Dimens.spacing_xxs,
                            Color.White,
                            RoundedCornerShape(Dimens.spacing_s)
                        )
                )
            }

            if (movie.hasMultipleSources) {
                Box(
                    modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(Dimens.spacing_xs)
                        .background(
                            plexColor,
                            RoundedCornerShape(
                                Dimens.spacing_xxs
                            )
                        )
                        .padding(
                            horizontal = Dimens.spacing_xs,
                            vertical = Dimens.spacing_xxs
                        )
                ) {
                    Text(
                        "MULTI",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            val displayRating = movie.imdbRating ?: movie.rating
            if (displayRating != null && displayRating > 0) {
                Box(
                    modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(Dimens.spacing_xs)
                        .background(
                            Color.Black.copy(alpha = 0.7f),
                            RoundedCornerShape(
                                Dimens.spacing_xxs
                            )
                        )
                        .padding(
                            horizontal = Dimens.spacing_xs,
                            vertical = Dimens.spacing_xxs
                        )
                ) {
                    Text(
                        "★ $displayRating",
                        fontWeight = FontWeight.Bold,
                        color = plexColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spacing_s))

        Text(
            text = movie.title,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            color = if (isFocused) Color.White else Color.LightGray,
            maxLines = 1,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal
        )
    }
}
