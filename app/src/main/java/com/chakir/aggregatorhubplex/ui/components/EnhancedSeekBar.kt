package com.chakir.aggregatorhubplex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chakir.aggregatorhubplex.domain.model.Chapter
import com.chakir.aggregatorhubplex.domain.model.Marker
import kotlin.math.max
import kotlin.math.min

/**
 * Barre de progression personnalisée (SeekBar) pour le lecteur vidéo. Affiche :
 * - La progression actuelle (glissable)
 * - Les marqueurs de chapitres (bandes sombres/claires)
 * - Les marqueurs spéciaux (Intro en vert, Crédits en rouge)
 */
@Composable
fun EnhancedSeekBar(
    currentPosition: Long,
    duration: Long,
    chapters: List<Chapter> = emptyList(),
    markers: List<Marker> = emptyList(),
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isDragging: Boolean = false
) {
    if (duration <= 0) return

    var isDrag by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableStateOf(currentPosition) }

    val displayPosition = if (isDrag) dragPosition else currentPosition
    val progress = if (duration > 0) displayPosition.toFloat() / duration.toFloat() else 0f
    var boxWidth by remember { mutableStateOf(0f) }

    Column(modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 4.dp)) {
        // Main seekbar container with chapters
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(
                        Color.DarkGray,
                        shape =
                            androidx.compose.foundation.shape
                                .RoundedCornerShape(4.dp)
                    )
                    .onSizeChanged { boxWidth = it.width.toFloat() }
                    .pointerInput(Unit) {
                        var dragStartX = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                isDrag = true
                                dragStartX = offset.x
                            },
                            onHorizontalDrag = { change, _ ->
                                change.consume()
                                isDrag = true
                                val dragDelta = change.position.x - dragStartX
                                val seekDelta = (dragDelta / boxWidth) * duration
                                dragPosition =
                                    max(
                                        0,
                                        min(
                                            currentPosition +
                                                    seekDelta.toLong(),
                                            duration
                                        )
                                    )
                            },
                            onDragEnd = {
                                isDrag = false
                                onSeek(dragPosition)
                                dragStartX = 0f
                            }
                        )
                    }
        ) {
            // Progress background
            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(Color(0xFFE5A00D))
            )

            // Chapter markers background
            chapters.forEach { chapter ->
                val chapterStart = (chapter.startTime.toFloat() / duration.toFloat())
                val chapterEnd = (chapter.endTime.toFloat() / duration.toFloat())

                Box(
                    modifier =
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(chapterEnd)
                            .offset(x = (chapterStart * boxWidth).dp)
                            .background(Color(0xFF1A1A1A).copy(alpha = 0.3f))
                )
            }

            // Intro marker indicator
            markers.filter { it.type == "intro" }.forEach { marker ->
                val markerStart = (marker.startTime.toFloat() / duration.toFloat())
                Box(
                    modifier =
                        Modifier
                            .fillMaxHeight()
                            .width(3.dp)
                            .background(Color.Green)
                            .align(Alignment.CenterStart)
                            .offset(x = (markerStart * boxWidth).dp)
                )
            }

            // Credits marker indicator
            markers.filter { it.type == "credits" }.forEach { marker ->
                val markerStart = (marker.startTime.toFloat() / duration.toFloat())
                Box(
                    modifier =
                        Modifier
                            .fillMaxHeight()
                            .width(3.dp)
                            .background(Color.Red)
                            .align(Alignment.CenterStart)
                            .offset(x = (markerStart * boxWidth).dp)
                )
            }

            // Current position thumb
            Box(
                modifier =
                    Modifier
                        .size(8.dp)
                        .background(
                            Color.White,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .align(Alignment.CenterStart)
                        .offset(x = (progress * boxWidth).dp)
            )
        }

        // Time display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = formatTime(displayPosition), color = Color.White, fontSize = 12.sp)

            // Current chapter display
            chapters
                .firstOrNull { chapter ->
                    displayPosition >= chapter.startTime && displayPosition < chapter.endTime
                }
                ?.let { chapter ->
                    Text(text = chapter.title, color = Color(0xFFE5A00D), fontSize = 11.sp)
                }

            Text(text = formatTime(duration), color = Color.White, fontSize = 12.sp)
        }

        // Chapter indicators legend
        if (chapters.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) { Text(text = "Chapitres: ${chapters.size}", color = Color.Gray, fontSize = 10.sp) }
        }

        // Marker indicators legend
        if (markers.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                markers.groupBy { it.type }.forEach { (type, _) ->
                    val color =
                        when (type) {
                            "intro" -> Color.Green
                            "credits" -> Color.Red
                            else -> Color.Yellow
                        }
                    Box(
                        modifier =
                            Modifier
                                .size(6.dp)
                                .background(
                                    color,
                                    shape =
                                        androidx.compose.foundation.shape
                                            .RoundedCornerShape(1.dp)
                                )
                    )
                    Text(text = type.capitalize(), color = Color.Gray, fontSize = 10.sp)
                }
            }
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

private fun String.capitalize(): String =
    this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
