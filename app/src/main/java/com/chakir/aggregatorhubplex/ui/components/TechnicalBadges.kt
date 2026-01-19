package com.chakir.aggregatorhubplex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Affiche les badges techniques comme 4K, HDR, Atmos, etc. Utilise un [FlowRow] pour s'adapter à la
 * largeur disponible.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TechnicalBadges(badges: List<String>, modifier: Modifier = Modifier) {
    if (badges.isEmpty()) return

    FlowRow(
            modifier = modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
    ) { badges.forEach { badge -> TechnicalBadge(text = badge) } }
}

/** Badge technique individuel avec couleur adaptée au type. */
@Composable
fun TechnicalBadge(text: String, modifier: Modifier = Modifier) {
    val (backgroundColor, textColor) =
            when (text.uppercase()) {
                "4K", "UHD" -> Pair(Color(0xFF1A237E), Color(0xFF64B5F6))
                "HDR", "HDR10" -> Pair(Color(0xFF1B5E20), Color(0xFF81C784))
                "ATMOS", "DOLBY ATMOS" -> Pair(Color(0xFF4A148C), Color(0xFFCE93D8))
                "DTS" -> Pair(Color(0xFF1A1A00), Color(0xFFFFE082))
                else -> Pair(Color(0xFF424242), Color(0xFFBDBDBD))
            }

    Text(
            text = text,
            modifier =
                    modifier.background(color = backgroundColor, shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
    )
}

/** Affiche les informations sur les pistes audio et les sous-titres. */
@Composable
fun MediaTrackInfo(audioTrackCount: Int, subtitleCount: Int, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Row(
            modifier = modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (audioTrackCount > 0) {
            androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("🔊", fontSize = 12.sp)
                Text(text = "$audioTrackCount audio", color = Color.Gray, fontSize = 12.sp)
            }
        }

        if (subtitleCount > 0) {
            androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("📝", fontSize = 12.sp)
                Text(text = "$subtitleCount sous-titre(s)", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}
