package com.chakir.aggregatorhubplex.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Définition des écrans de l'application et de leurs routes de navigation. Utilise une sealed class
 * pour garantir la sécurité de type dans la navigation.
 *
 * @param route L'identifiant unique de la route pour le contrôleur de navigation.
 * @param title Le titre affiché pour cet écran (par exemple dans la barre d'onglets).
 * @param icon L'icône associée à cet écran.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    // Ordre suggéré : Recherche en premier ou juste après Accueil

    /** Écran de recherche globale de contenu */
    object Search : Screen("search", "Recherche", Icons.Default.Search) // <--- AJOUT

    /** Écran d'accueil principal (Tableau de bord) */
    object Home : Screen("home", "Accueil", Icons.Default.Home)

    /** Écran listant tous les films */
    object Movies : Screen("movies", "Films", Icons.Default.Movie)

    /** Écran listant toutes les séries TV */
    object Shows : Screen("shows", "Séries", Icons.Default.Tv)

    // Favoris sera implémenté plus tard, mais on prépare le menu
    /** Écran des favoris utilisateur */
    object Favorites : Screen("favorites", "Favoris", Icons.Default.Favorite)

    /** Écran d'historique de visionnage */
    object History : Screen("history", "Historique", Icons.Default.History)

    /** Écran des paramètres de l'application */
    object Settings : Screen("settings", "Paramètres", Icons.Default.Settings)

    /** Écran de gestion et visualisation des serveurs connectés */
    object Servers :
            Screen(
                    "servers",
                    "Serveurs",
                    Icons.Default.Cloud
            ) // Si Cloud n'existe pas, utilisez Info ou Settings
}
