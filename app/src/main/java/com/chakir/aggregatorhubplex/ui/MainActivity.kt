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
import androidx.tv.material3.SurfaceDefaults
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

/**
 * Activité principale de l'application. Point d'entrée de l'interface utilisateur Compose.
 * Initialise également le Worker de synchronisation en arrière-plan.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Synchro en arrière-plan
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
        WorkManager.getInstance(this)
                .enqueueUniqueWork("GlobalSync", ExistingWorkPolicy.KEEP, syncRequest)

        setContent {
            PlexHubTheme {
                Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RectangleShape,
                        colors =
                                SurfaceDefaults.colors(
                                        containerColor = MaterialTheme.colorScheme.background
                                )
                ) { AppNavigation() }
            }
        }
    }
}

/**
 * Gestionnaire de navigation principal de l'application. Définit le NavHost et toutes les routes
 * composables disponibles. Gère également les transitions entre les écrans.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route ?: "discovery"

    // Routes qui nécessitent la barre latérale
    val sidebarRoutes =
            listOf(
                    Screen.Home.route,
                    Screen.Movies.route,
                    Screen.Shows.route,
                    Screen.Favorites.route,
                    Screen.History.route,
                    Screen.Settings.route,
                    Screen.Servers.route,
                    Screen.Search.route
            )

    // Affiche la sidebar si la route courante est dans la liste ou si la route parente (si graph imbriqué) l'est
    val showSidebar = sidebarRoutes.any { route -> currentRoute == route } || 
                      (currentDestination?.parent?.route != null && sidebarRoutes.contains(currentDestination?.parent?.route))

    Row(modifier = Modifier.fillMaxSize()) {
        if (showSidebar) {
            AppSidebar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Évite d'empiler les copies de la même destination
                            launchSingleTop = true
                            restoreState = true

                            // Si on retourne à Home, on vide la stack jusqu'à Home pour éviter une boucle infinie
                            if (route == Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            NavHost(
                    navController = navController,
                    startDestination = "discovery",
                    enterTransition = { fadeIn(animationSpec = tween(300)) },
                    exitTransition = { fadeOut(animationSpec = tween(300)) },
                    popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                    popExitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                composable("discovery") {
                    ServerDiscoveryScreen(
                            onServerFound = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo("discovery") { inclusive = true }
                                }
                            }
                    )
                }

                composable(Screen.Search.route) {
                    val viewModel = androidx.hilt.navigation.compose.hiltViewModel<HomeViewModel>()
                    SearchScreen(
                            onMovieClick = { movie -> navController.navigate("details/${movie.id}") },
                            viewModel = viewModel
                    )
                }

                composable(Screen.Home.route) {
                    val viewModel = androidx.hilt.navigation.compose.hiltViewModel<HomeViewModel>()
                    LaunchedEffect(Unit) { viewModel.onTypeChange("all") }
                    HomeScreen(
                        onMovieClick = { movie ->
                            if (movie.type == "episode" && !movie.grandparentKey.isNullOrEmpty()) {
                                navController.navigate(
                                    "episode_detail/${movie.grandparentKey}/${movie.id}"
                                )
                            } else {
                                navController.navigate(
                                    "details/${movie.id}?startPosition=${movie.viewOffset}"
                                )
                            }
                        },
                            viewModel = viewModel
                    )
                }

                composable(Screen.Movies.route) {
                    val viewModel = androidx.hilt.navigation.compose.hiltViewModel<HomeViewModel>()
                    LibraryScreen(
                            type = "movie",
                            onMovieClick = { movie ->
                                navController.navigate(
                                        "details/${movie.id}?startPosition=${movie.viewOffset}"
                                )
                            },
                            viewModel = viewModel
                    )
                }

                composable(Screen.Shows.route) {
                    val viewModel = androidx.hilt.navigation.compose.hiltViewModel<HomeViewModel>()
                    LibraryScreen(
                            type = "show",
                            onMovieClick = { movie ->
                                navController.navigate(
                                        "details/${movie.id}?startPosition=${movie.viewOffset}"
                                )
                            },
                            viewModel = viewModel
                    )
                }

                composable(Screen.Favorites.route) {
                    FavoritesScreen(
                            onMovieClick = { movieId -> navController.navigate("details/$movieId") }
                    )
                }

                composable(Screen.History.route) {
                    HistoryScreen(
                            onMovieClick = { movie ->
                                navController.navigate(
                                        "details/${movie.id}?startPosition=${movie.viewOffset}"
                                )
                            }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                            onLogout = {
                                navController.navigate("discovery") { popUpTo(0) { inclusive = true } }
                            }
                    )
                }

                composable(Screen.Servers.route) { ServersScreen() }

                composable(
                        route = "details/{movieId}?startPosition={startPosition}",
                        arguments =
                                listOf(
                                        navArgument("movieId") { type = NavType.StringType },
                                        navArgument("startPosition") {
                                            type = NavType.LongType
                                            defaultValue = -1L
                                        }
                                ),
                        enterTransition = { slideInHorizontally { it } + fadeIn() },
                        popExitTransition = { slideOutHorizontally { it } + fadeOut() }
                ) { backStackEntry ->
                    val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
                    val startPosition = backStackEntry.arguments?.getLong("startPosition") ?: -1L
                    DetailScreen(
                            movieId = movieId,
                            startPositionMs = startPosition,
                            onPlayVideo = { videoUrl, title, id, position, serverName, showId, posterUrl, type ->
                                val encodedUrl =
                                        URLEncoder.encode(videoUrl, StandardCharsets.UTF_8.toString())
                                val encodedTitle =
                                        URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
                                val encodedServerName =
                                        URLEncoder.encode(serverName, StandardCharsets.UTF_8.toString())
                                val encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8.toString())
                                val encodedShowId = if (showId != null) "&show_id=" + URLEncoder.encode(showId, StandardCharsets.UTF_8.toString()) else ""
                                val encodedPoster =
                                    if (posterUrl != null)
                                        "&poster_url=" + URLEncoder.encode(posterUrl, StandardCharsets.UTF_8.toString())
                                    else ""
                                val encodedType = "&media_type=$type"
                                
                                navController.navigate(
                                        "player/$encodedUrl?title=$encodedTitle&id=$encodedId&start_position=$position&server_name=$encodedServerName$encodedShowId$encodedPoster$encodedType"
                                )
                            },
                            onEpisodeClick = { episodeId ->
                                val encodedEpisodeId = URLEncoder.encode(episodeId, StandardCharsets.UTF_8.toString())
                                navController.navigate("episode_detail/$movieId/$encodedEpisodeId")
                            }
                    )
                }

                composable(
                    route = "episode_detail/{seriesId}/{episodeId}",
                    arguments = listOf(
                        navArgument("seriesId") { type = NavType.StringType },
                        navArgument("episodeId") { type = NavType.StringType }
                    ),
                    enterTransition = { slideInHorizontally { it } + fadeIn() },
                    popExitTransition = { slideOutHorizontally { it } + fadeOut() }
                ) { backStackEntry ->
                    val seriesId = backStackEntry.arguments?.getString("seriesId") ?: ""
                    val episodeId = backStackEntry.arguments?.getString("episodeId") ?: ""
                    
                    EpisodeDetailScreen(
                        seriesId = seriesId,
                        episodeId = episodeId,
                        onBack = { navController.popBackStack() },
                        onPlayVideo = { videoUrl, title, id, position, serverName, showId, posterUrl, type ->
                            val encodedUrl = URLEncoder.encode(videoUrl, StandardCharsets.UTF_8.toString())
                            val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
                            val encodedServerName = URLEncoder.encode(serverName, StandardCharsets.UTF_8.toString())
                            val encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8.toString())
                            val encodedShowId = if (showId != null) "&show_id=" + URLEncoder.encode(showId, StandardCharsets.UTF_8.toString()) else ""
                            val encodedPoster = if (posterUrl != null)
                                "&poster_url=" + URLEncoder.encode(posterUrl, StandardCharsets.UTF_8.toString())
                            else ""
                            val encodedType = "&media_type=$type"
                            
                            navController.navigate(
                                "player/$encodedUrl?title=$encodedTitle&id=$encodedId&start_position=$position&server_name=$encodedServerName$encodedShowId$encodedPoster$encodedType"
                            )
                        }
                    )
                }

                composable(
                        route = "player/{videoUrl}?title={title}&id={id}&start_position={startPosition}&server_name={serverName}&show_id={showId}&poster_url={posterUrl}&media_type={mediaType}",
                        arguments =
                                listOf(
                                        navArgument("videoUrl") { type = NavType.StringType },
                                        navArgument("title") {
                                            type = NavType.StringType
                                            defaultValue = "Vidéo"
                                        },
                                        navArgument("id") {
                                            type = NavType.StringType
                                            defaultValue = ""
                                        },
                                        navArgument("startPosition") {
                                            type = NavType.LongType
                                            defaultValue = -1L
                                        },
                                        navArgument("serverName") {
                                            type = NavType.StringType
                                            nullable = true
                                        },
                                        navArgument("showId") {
                                            type = NavType.StringType
                                            nullable = true
                                        },
                                        navArgument("posterUrl") {
                                            type = NavType.StringType
                                            nullable = true
                                        },
                                        navArgument("mediaType") {
                                            type = NavType.StringType
                                            defaultValue = "movie"
                                        }
                                ),
                        enterTransition = { fadeIn(tween(500)) },
                        exitTransition = { fadeOut(tween(500)) }
                ) { backStackEntry ->
                    val videoUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
                    val title = backStackEntry.arguments?.getString("title") ?: "Vidéo"
                    val id = backStackEntry.arguments?.getString("id") ?: ""
                    val startPosition = backStackEntry.arguments?.getLong("startPosition") ?: -1L
                    val serverName = backStackEntry.arguments?.getString("serverName")
                    val showId = backStackEntry.arguments?.getString("showId")
                    val posterUrl = backStackEntry.arguments?.getString("posterUrl")
                    val mediaType = backStackEntry.arguments?.getString("mediaType") ?: "movie"

                    PlayerScreen(
                            streamUrl = videoUrl,
                            mediaTitle = title,
                            mediaId = id,
                            startPositionMs = startPosition,
                            serverName = serverName,
                            showId = showId,
                            posterUrl = posterUrl,
                            type = mediaType,
                            onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
