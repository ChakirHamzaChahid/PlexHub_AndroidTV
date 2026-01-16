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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.chakir.aggregatorhubplex.ui.screens.DarkSurface
import com.chakir.aggregatorhubplex.ui.screens.PlexAccent
import com.chakir.aggregatorhubplex.ui.screens.TextGrey

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FilterBar(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(items) { item ->
            val isSelected = item == selectedItem
            Surface(
                onClick = { onItemSelected(item) },
                shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50)),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = if (isSelected) PlexAccent else DarkSurface,
                    contentColor = if (isSelected) Color.Black else TextGrey, // MODIFIÉ ICI
                    focusedContainerColor = PlexAccent,
                    focusedContentColor = Color.Black
                )
            ) {
                Text(
                    text = item,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
