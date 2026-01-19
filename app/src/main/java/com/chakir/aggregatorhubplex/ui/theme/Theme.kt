package com.chakir.aggregatorhubplex.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

// On configure le schéma de couleurs (Mapping des variables Color.kt vers Material 3)
private val PlexColorScheme =
        darkColorScheme(
                primary = PlexOrange,
                onPrimary = TextPrimary, // Texte clair sur fond primaire (orange)
                primaryContainer = SurfaceHighlight,
                onPrimaryContainer = TextPrimary,
                secondary = TextSecondary,
                onSecondary = AppBackground,
                background = AppBackground,
                onBackground = TextSecondary, // Texte par défaut -> Gris
                surface = SurfaceColor,
                onSurface = TextSecondary, // Texte sur les surfaces -> Gris
                error = ErrorRed,
                onError = AppBackground
        )

/**
 * Thème principal de l'application (Android TV). Force le thème sombre (Dark Scheme) car
 * l'interface TV doit être immersive.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlexHubTheme(content: @Composable () -> Unit) {
    MaterialTheme(
            colorScheme = PlexColorScheme,
            typography = Typography, // S'assure que Type.kt est utilisé
            content = content
    )
}
