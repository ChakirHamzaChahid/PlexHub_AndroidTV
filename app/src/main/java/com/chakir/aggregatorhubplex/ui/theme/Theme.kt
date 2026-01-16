package com.chakir.aggregatorhubplex.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

// On configure le schéma de couleurs (Mapping des variables Color.kt vers Material 3)
private val PlexColorScheme = darkColorScheme(
    primary = PlexOrange,
    onPrimary = AppBackground, // Texte noir sur bouton orange
    primaryContainer = SurfaceHighlight,
    onPrimaryContainer = TextPrimary,

    secondary = TextSecondary,
    onSecondary = AppBackground,

    background = AppBackground,
    onBackground = TextPrimary,

    surface = SurfaceColor,
    onSurface = TextPrimary,

    error = ErrorRed,
    onError = AppBackground
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlexHubTheme(
    content: @Composable () -> Unit
) {
    // Sur une TV, on force toujours le thème sombre (Dark Scheme)
    // On ne vérifie pas isSystemInDarkTheme() car l'interface TV doit être sombre par défaut.

    MaterialTheme(
        colorScheme = PlexColorScheme,
        typography = Typography, // S'assure que Type.kt est utilisé
        content = content
    )
}