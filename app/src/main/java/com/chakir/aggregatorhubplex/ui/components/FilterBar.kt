package com.chakir.aggregatorhubplex.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.chakir.aggregatorhubplex.ui.theme.PlexOrange

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FilterBar(
    items: List<String>, 
    selectedItem: String, 
    onItemSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 20.dp), // Marge pour le scale
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        items(items) { item ->
            FilterChip(
                label = item,
                isSelected = item == selectedItem,
                onClick = { onItemSelected(item) }
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // --- ANIMATIONS FLUIDES ---
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.15f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "scale"
    )

    // Couleur de fond : Blanc au Focus (Pop), Orange subtil si Sélectionné, Transparent sinon
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected -> PlexOrange.copy(alpha = 0.15f)
            else -> Color.Transparent
        },
        label = "bgColor"
    )

    // Couleur du texte : Noir au Focus (Contraste), Orange si Sélectionné, Gris sinon
    val textColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.Black
            isSelected -> PlexOrange
            else -> Color(0xFFBDC1C6) // Google Grey 400
        },
        label = "textColor"
    )
    
    // Bordure : Subtilement Orange si sélectionné (pour délimiter sans focus)
    val borderColor by animateColorAsState(
        targetValue = if (isSelected && !isFocused) PlexOrange.copy(alpha = 0.5f) else Color.Transparent,
        label = "borderColor"
    )
    
    val shape = RoundedCornerShape(12.dp)

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f), // On gère le scale manuellement pour plus de contrôle
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            pressedContainerColor = Color.Transparent
        ),
        modifier = Modifier.scale(scale)
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(backgroundColor)
                .border(BorderStroke(1.dp, borderColor), shape)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = label.uppercase(),
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium.copy(
                    fontSize = 10.sp, // Police réduite
                    letterSpacing = 1.2.sp, // Espacement "Cinéma"
                    fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium
                ),
                color = textColor
            )
        }
    }
}
