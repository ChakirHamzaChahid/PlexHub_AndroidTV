package com.chakir.aggregatorhubplex.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chakir.aggregatorhubplex.domain.model.Marker

/** Bouton animé qui apparaît pendant les intros ou les crédits pour les passer. */
@Composable
fun SkipMarkerButton(
    marker: Marker?,
    markerType: String,
    isVisible: Boolean,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayText =
        when (markerType) {
            "intro" -> "Passer l'intro"
            "credits" -> "Passer les crédits"
            else -> "Passer"
        }

    val buttonColor =
        when (markerType) {
            "intro" -> Color(0xFF4CAF50)
            "credits" -> Color(0xFFFF9800)
            else -> Color(0xFF2196F3)
        }

    AnimatedVisibility(
        visible = isVisible && marker != null,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier =
                Modifier
                    .background(
                        color = buttonColor.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSkip() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = displayText,
                    tint = Color.White,
                    modifier = Modifier
                )
                Text(
                    text = displayText,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/** Boutons de navigation entre les chapitres. */
@Composable
fun ChapterNavigationButtons(
    currentChapter: String?,
    onNextChapter: () -> Unit,
    onPreviousChapter: () -> Unit,
    hasNextChapter: Boolean,
    hasPreviousChapter: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasPreviousChapter) {
            Box(
                modifier =
                    Modifier
                        .background(
                            color = Color(0xFF2196F3).copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onPreviousChapter() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "◀ Chapitre précédent",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (hasNextChapter) {
            Box(
                modifier =
                    Modifier
                        .background(
                            color = Color(0xFF2196F3).copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onNextChapter() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chapitre suivant ▶",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
