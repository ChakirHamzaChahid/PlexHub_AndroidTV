package com.chakir.aggregatorhubplex.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.chakir.aggregatorhubplex.ui.theme.AppBackground
import com.chakir.aggregatorhubplex.ui.theme.Dimens
import com.chakir.aggregatorhubplex.ui.theme.PlexOrange
import com.chakir.aggregatorhubplex.ui.theme.PlexOrangeDim
import com.chakir.aggregatorhubplex.ui.theme.SurfaceColor
import com.chakir.aggregatorhubplex.ui.theme.TextPrimary
import com.chakir.aggregatorhubplex.ui.theme.TextSecondary

/**
 * Barre de filtres horizontale (Chips). Permet de sélectionner une catégorie (ex: "Tous", "Films",
 * "Séries"). Optimisée pour la navigation TV avec [Surface] et [ClickableSurfaceDefaults].
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FilterBar(items: List<String>, selectedItem: String, onItemSelected: (String) -> Unit) {
    LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_m),
            contentPadding = PaddingValues(bottom = Dimens.spacing_xl)
    ) {
        items(items) { item ->
            val isSelected = item == selectedItem

            Surface(
                    onClick = { onItemSelected(item) },
                    shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50)),
                    colors =
                            ClickableSurfaceDefaults.colors(
                                    // --- 1. ETAT DE REPOS ---
                                    containerColor =
                                            if (isSelected) PlexOrangeDim else SurfaceColor,
                                    contentColor = if (isSelected) TextPrimary else TextSecondary,

                                    // --- 2. ETAT FOCUS ---
                                    focusedContainerColor = PlexOrange,
                                    focusedContentColor = AppBackground,

                                    // --- 3. ETAT PRESSÉ ---
                                    pressedContainerColor = PlexOrangeDim,
                                    pressedContentColor = AppBackground
                            ),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f)
            ) {
                Text(
                        text = item,
                        modifier =
                                Modifier.padding(
                                        horizontal = Dimens.spacing_l,
                                        vertical = Dimens.spacing_s
                                ),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
