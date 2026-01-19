package com.chakir.aggregatorhubplex.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.chakir.aggregatorhubplex.ui.screens.SortOption
import com.chakir.aggregatorhubplex.ui.theme.PlexOrange
import com.chakir.aggregatorhubplex.ui.theme.SurfaceColor
import com.chakir.aggregatorhubplex.ui.theme.TextPrimary
import com.chakir.aggregatorhubplex.ui.theme.TextSecondary

/** Menu de tri pour les listes de médias. Affiche les options de tri disponibles lors du clic. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SortMenu(
        currentSort: SortOption,
        onSortClick: () -> Unit,
        isSortMenuOpen: Boolean,
        onSortChange: (SortOption) -> Unit,
        onDismiss: () -> Unit,
        focusRequester: FocusRequester
) {
    Box {
        Surface(
                onClick = onSortClick,
                colors =
                        ClickableSurfaceDefaults.colors(
                                containerColor = Color.Transparent,
                                contentColor = TextSecondary,
                                focusedContainerColor = SurfaceColor,
                                focusedContentColor = TextPrimary
                        ),
                shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(4.dp)),
        ) {
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                        text = currentSort.label.substringBefore(" "),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary // MODIFIÉ ICI
                )
                Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = TextSecondary // MODIFIÉ ICI
                )
            }
        }

        AnimatedVisibility(visible = isSortMenuOpen, enter = fadeIn(), exit = fadeOut()) {
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.8f))
                                    .clickable { onDismiss() },
                    contentAlignment = Alignment.CenterEnd
            ) {
                Column(
                        modifier =
                                Modifier.fillMaxHeight()
                                        .width(350.dp)
                                        .background(SurfaceColor)
                                        .padding(32.dp),
                        horizontalAlignment = Alignment.Start
                ) {
                    Text(
                            "Trier par",
                            style = MaterialTheme.typography.headlineSmall,
                            color = PlexOrange,
                            modifier = Modifier.padding(bottom = 16.dp)
                    )
                    SortOption.values().forEach { option ->
                        SortMenuItem(
                                label = option.label,
                                isSelected = currentSort == option,
                                onClick = { onSortChange(option) },
                                modifier =
                                        if (currentSort == option)
                                                Modifier.focusRequester(focusRequester)
                                        else Modifier
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

/** Item individuel du menu de tri. Affiche le libellé et une coche si sélectionné. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SortMenuItem(
        label: String,
        isSelected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    Surface(
            onClick = onClick,
            colors =
                    ClickableSurfaceDefaults.colors(
                            containerColor =
                                    if (isSelected) Color.White.copy(alpha = 0.1f)
                                    else Color.Transparent,
                            contentColor = if (isSelected) PlexOrange else Color.White,
                            focusedContainerColor = PlexOrange,
                            focusedContentColor = Color.Black
                    ),
            shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(8.dp)),
            modifier = modifier.fillMaxWidth()
    ) {
        Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                    label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = PlexOrange)
            }
        }
    }
}
