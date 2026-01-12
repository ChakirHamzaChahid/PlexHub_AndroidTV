package com.chakir.aggregatorhubplex.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.darkColorScheme
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.chakir.aggregatorhubplex.ui.screens.DetailScreen
import com.chakir.aggregatorhubplex.ui.screens.HomeScreen
import com.chakir.aggregatorhubplex.ui.screens.HomeViewModel
import com.chakir.aggregatorhubplex.ui.screens.ServerDiscoveryScreen
import com.chakir.aggregatorhubplex.workers.SyncWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // On lance le Worker en mode "KEEP" (ne remplace pas s'il tourne déjà)
        // Il utilisera l'URL qui sera configurée dans l'écran Discovery
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            "GlobalSync",
            ExistingWorkPolicy.KEEP,
            syncRequest
        )

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    // Démarrage TOUJOURS sur discovery pour vérifier l'URL ou la saisir
                    AppNavigation(startDestination = "discovery")
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AppNavigation(startDestination: String = "discovery") {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {

        composable("discovery") {
            ServerDiscoveryScreen(
                onServerFound = {
                    navController.navigate("home") {
                        popUpTo("discovery") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            // Utilisation correcte de Hilt pour injecter le ViewModel
            val viewModel = androidx.hilt.navigation.compose.hiltViewModel<HomeViewModel>()

            HomeScreen(
                onMovieClick = { movie -> navController.navigate("details/${movie.id}") },
                viewModel = viewModel
            )
        }

        composable(
            route = "details/{movieId}",
            arguments = listOf(navArgument("movieId") { type = NavType.StringType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
            DetailScreen(movieId = movieId)
        }
    }
}