package com.chakir.aggregatorhubplex.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chakir.aggregatorhubplex.ui.navigation.Screen
import com.chakir.aggregatorhubplex.ui.theme.PlexOrange

/**
 * Barre de navigation latérale (Sidebar) pour Android TV. S'étend automatiquement lorsqu'elle
 * reçoit le focus.
 */
@Composable
fun AppSidebar(currentRoute: String, onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }

    // Largeur dynamique : 70dp (fermé) <-> 220dp (ouvert)
    val width by
            animateDpAsState(
                    targetValue = if (isExpanded) 220.dp else 70.dp,
                    label = "SidebarWidth"
            )

    Column(
            modifier =
                    modifier.width(width)
                            .fillMaxHeight()
                            .background(Color(0xFF0F0F0F))
                            .padding(vertical = 32.dp, horizontal = 12.dp)
                            .onFocusChanged {
                                isExpanded = it.hasFocus
                            }, // S'ouvre si un enfant a le focus
            verticalArrangement = Arrangement.Center
    ) {
        val screens =
                listOf(
                        Screen.Home,
                        Screen.Search, // <--- AJOUT ICI
                        Screen.Movies,
                        Screen.Shows,
                        Screen.Favorites,
                        Screen.History,
                        Screen.Settings,
                        Screen.Servers
                )

        screens.forEach { screen ->
            SidebarItem(
                    screen = screen,
                    isSelected = currentRoute == screen.route,
                    isExpanded = isExpanded,
                    onClick = { onNavigate(screen.route) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/** Item individuel de la Sidebar. Change d'apparence selon l'état de focus et de sélection. */
@Composable
fun SidebarItem(screen: Screen, isSelected: Boolean, isExpanded: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val backgroundColor =
            when {
                isFocused -> Color.White
                isSelected -> Color.White.copy(alpha = 0.1f)
                else -> Color.Transparent
            }

    val contentColor =
            when {
                isFocused -> Color.Black
                isSelected -> PlexOrange
                else -> Color.Gray
            }

    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .height(48.dp)
                            .background(backgroundColor, RoundedCornerShape(8.dp))
                            .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = onClick
                            )
                            .focusable(interactionSource = interactionSource)
                            .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isExpanded) Arrangement.Start else Arrangement.Center
    ) {
        Icon(
                imageVector = screen.icon,
                contentDescription = screen.title,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
        )

        AnimatedVisibility(visible = isExpanded) {
            Text(
                    text = screen.title,
                    color = contentColor,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 16.dp),
                    maxLines = 1
            )
        }
    }
}
