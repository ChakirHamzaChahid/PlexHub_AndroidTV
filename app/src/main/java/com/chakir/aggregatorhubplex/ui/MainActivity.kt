package com.chakir.aggregatorhubplex.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.darkColorScheme
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.chakir.aggregatorhubplex.ui.components.AppSidebar
import com.chakir.aggregatorhubplex.ui.navigation.Screen
import com.chakir.aggregatorhubplex.ui.screens.*
import com.chakir.aggregatorhubplex.ui.theme.PlexHubTheme
import com.chakir.aggregatorhubplex.workers.SyncWorker
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.tv.material3.SurfaceDefaults

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Synchro en arrière-plan
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            "GlobalSync",
            ExistingWorkPolicy.KEEP,
            syncRequest
        )

        setContent {
            PlexHubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape,
                    // CORRECTION : On utilise 'colors' avec SurfaceDefaults, pas 'color' direct
                    colors = SurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    NavHost(
        navController = navController,
        startDestination = "discovery",
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) },
        popExitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {

        // 1. DÉCOUVERTE
        composable("discovery") {
            ServerDiscoveryScreen(
                onServerFound = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo("discovery") { inclusive = true }
                    }
                }
            )
        }

        // 2. ÉCRANS PRINCIPAUX (Sidebar)

        composable(Screen.Search.route) {
            SidebarLayout(currentRoute, navController::navigate) {
                val viewModel = androidx.hilt.navigation.compose.hiltViewModel<HomeViewModel>()
                SearchScreen(
                    onMovieClick = { movie -> navController.navigate("details/${movie.id}") },
                    viewModel = viewModel
                )
            }
        }

        composable(Screen.Home.route) {
            SidebarLayout(currentRoute, navController::navigate) {
                val viewModel = androidx.hilt.navigation.compose.hiltViewModel<HomeViewModel>()
                LaunchedEffect(Unit) { viewModel.onTypeChange("all") }
                HomeScreen(
                    onMovieClick = { movie -> navController.navigate("details/${movie.id}") },
                    viewModel = viewModel
                )
            }
        }

        composable(Screen.Movies.route) {
            SidebarLayout(currentRoute, navController::navigate) {
                val viewModel = androidx.hilt.navigation.compose.hiltViewModel<HomeViewModel>()
                LaunchedEffect(Unit) { viewModel.onTypeChange("movie") }
                HomeScreen(
                    onMovieClick = { movie -> navController.navigate("details/${movie.id}") },
                    viewModel = viewModel
                )
            }
        }

        composable(Screen.Shows.route) {
            SidebarLayout(currentRoute, navController::navigate) {
                val viewModel = androidx.hilt.navigation.compose.hiltViewModel<HomeViewModel>()
                LaunchedEffect(Unit) { viewModel.onTypeChange("show") }
                HomeScreen(
                    onMovieClick = { movie -> navController.navigate("details/${movie.id}") },
                    viewModel = viewModel
                )
            }
        }

        // MISE A JOUR : Écran Favoris réel
        composable(Screen.Favorites.route) {
            SidebarLayout(currentRoute, navController::navigate) {
                FavoritesScreen(
                    onMovieClick = { movieId -> navController.navigate("details/$movieId") }
                )
            }
        }

        composable(Screen.Settings.route) {
            SidebarLayout(currentRoute, navController::navigate) {
                SettingsScreen(
                    onLogout = {
                        navController.navigate("discovery") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Screen.Servers.route) {
            SidebarLayout(currentRoute, navController::navigate) {
                ServersScreen()
            }
        }

        // 3. DÉTAIL
        composable(
            route = "details/{movieId}",
            arguments = listOf(navArgument("movieId") { type = NavType.StringType }),
            enterTransition = { slideInHorizontally { it } + fadeIn() },
            popExitTransition = { slideOutHorizontally { it } + fadeOut() }
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
            DetailScreen(
                movieId = movieId,
                // MISE A JOUR : Passage des 3 arguments pour l'historique
                onPlayVideo = { videoUrl, title, id ->
                    val encodedUrl = URLEncoder.encode(videoUrl, StandardCharsets.UTF_8.toString())
                    val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
                    // On utilise des Query Params (?title=...) pour les rendre optionnels et sûrs
                    navController.navigate("player/$encodedUrl?title=$encodedTitle&id=$id")
                }
            )
        }

        // 4. LECTEUR VIDÉO (Mise à jour arguments)
        composable(
            route = "player/{videoUrl}?title={title}&id={id}",
            arguments = listOf(
                navArgument("videoUrl") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType; defaultValue = "Vidéo" },
                navArgument("id") { type = NavType.StringType; defaultValue = "" }
            ),
            enterTransition = { fadeIn(tween(500)) },
            exitTransition = { fadeOut(tween(500)) }
        ) { backStackEntry ->
            val videoUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: "Vidéo"
            val id = backStackEntry.arguments?.getString("id") ?: ""

            PlayerScreen(
                streamUrl = videoUrl,
                mediaTitle = title, // Passage du titre
                mediaId = id,       // Passage de l'ID pour Room
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// Wrapper pour afficher la Sidebar + Contenu
@Composable
fun SidebarLayout(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    content: @Composable () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        AppSidebar(
            currentRoute = currentRoute,
            onNavigate = onNavigate,
            // Utilisation de la couleur Surface du thème pour la sidebar
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        )
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}