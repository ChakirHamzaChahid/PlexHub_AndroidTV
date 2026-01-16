package com.chakir.aggregatorhubplex.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    // Ordre suggéré : Recherche en premier ou juste après Accueil
    object Search : Screen("search", "Recherche", Icons.Default.Search) // <--- AJOUT
    object Home : Screen("home", "Accueil", Icons.Default.Home)
    object Movies : Screen("movies", "Films", Icons.Default.Movie)
    object Shows : Screen("shows", "Séries", Icons.Default.Tv)
    // Favoris sera implémenté plus tard, mais on prépare le menu
    object Favorites : Screen("favorites", "Favoris", Icons.Default.Favorite)
    object Settings : Screen("settings", "Paramètres", Icons.Default.Settings)

    object Servers : Screen("servers", "Serveurs", Icons.Default.Cloud) // Si Cloud n'existe pas, utilisez Info ou Settings
}