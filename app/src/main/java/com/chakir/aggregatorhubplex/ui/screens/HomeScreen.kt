package com.chakir.aggregatorhubplex.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chakir.aggregatorhubplex.domain.model.Movie
import com.chakir.aggregatorhubplex.ui.components.HeroSection
import com.chakir.aggregatorhubplex.ui.components.SectionRow
import com.chakir.aggregatorhubplex.ui.theme.Dimens
import kotlinx.coroutines.delay

/**
 * Modern Home Screen with Hero Section and Horizontal Rows.
 * Uses strictly local data via ViewModel.
 */
@Composable
fun HomeScreen(
    onMovieClick: (Movie) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // Collect data states
    val featuredMovies by viewModel.featuredMovies.collectAsState()
    val continueWatchingItems by viewModel.continueWatchingItems.collectAsState()
    val recentlyAdded by viewModel.recentlyAdded.collectAsState()
    val hubs by viewModel.hubs.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()

    // Reset filter on entry
    LaunchedEffect(Unit) {
        viewModel.onTypeChange("all")
    }

    // Auto-rotation state
    var heroIndex by remember { mutableIntStateOf(0) }

    // Rotate Hero content every 20 seconds if multiple items exist
    LaunchedEffect(featuredMovies) {
        if (featuredMovies.isNotEmpty()) {
            while (true) {
                delay(20000)
                heroIndex = (heroIndex + 1) % featuredMovies.size
            }
        }
    }

    val currentHeroMovie = featuredMovies.getOrNull(heroIndex) ?: featuredMovies.firstOrNull()

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF1E1E1E), Color(0xFF141414))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // Add padding to ensure content isn't cut off at the edges (overscan/status bar)
            contentPadding = PaddingValues(bottom = Dimens.spacing_xxxl)
        ) {
            // --- HERO SECTION ---
            if (currentHeroMovie != null) {
                item {
                    // Animated transition for Hero content
                    AnimatedContent(
                        targetState = currentHeroMovie,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(1000)) togetherWith fadeOut(animationSpec = tween(1000))
                        },
                        label = "HeroAnimation"
                    ) { movie ->
                        HeroSection(
                            movie = movie,
                            onPlayClick = { onMovieClick(movie) },
                            onDetailsClick = { onMovieClick(movie) }
                        )
                    }
                }
            }

            // --- CONTINUE WATCHING ---
            if (continueWatchingItems.isNotEmpty()) {
                item {
                    SectionRow(
                        title = "Reprendre la lecture",
                        items = continueWatchingItems,
                        onItemClick = onMovieClick
                    )
                }
            }

            // --- RECENTLY ADDED ---
            if (recentlyAdded.isNotEmpty()) {
                item {
                    SectionRow(
                        title = "Récemment ajoutés",
                        items = recentlyAdded,
                        onItemClick = onMovieClick
                    )
                }
            }

            // --- HUBS (Top Rated, etc) ---
            items(hubs.toList()) { (hubTitle, hubMovies) ->
                if (hubMovies.isNotEmpty()) {
                    SectionRow(
                        title = hubTitle,
                        items = hubMovies,
                        onItemClick = onMovieClick
                    )
                }
            }

            // --- WATCH HISTORY ---
            if (watchHistory.isNotEmpty()) {
                item {
                    SectionRow(
                        title = "Historique",
                        items = watchHistory,
                        onItemClick = onMovieClick
                    )
                }
            }

            // Bottom Spacer
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
