package com.chakir.aggregatorhubplex.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme as TvMaterialTheme
import androidx.tv.material3.Surface
import com.chakir.aggregatorhubplex.domain.model.Movie
import androidx.tv.material3.Surface
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chakir.aggregatorhubplex.ui.components.ContinueWatchingCarousel
import com.chakir.aggregatorhubplex.ui.components.FeaturedCarousel
import com.chakir.aggregatorhubplex.ui.components.FilterBar
import com.chakir.aggregatorhubplex.ui.components.SkeletonCard
import com.chakir.aggregatorhubplex.ui.components.SortMenu
import com.chakir.aggregatorhubplex.ui.theme.Dimens

/**
 * Écran d'accueil principal. Affiche le carrousel "Reprendre", le carrousel "À la une", les
 * filtres, le tri et la grille principale des médias (paginée).
 *
 * @param onMovieClick Callback lorsqu'un média est cliqué.
 * @param viewModel ViewModel pour gérer l'état de l'écran d'accueil.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(onMovieClick: (Movie) -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
    val movies = viewModel.moviesPagingFlow.collectAsLazyPagingItems()
    val featuredMovies by viewModel.featuredMovies.collectAsState()
    val continueWatchingItems by viewModel.continueWatchingItems.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val genreLabels = viewModel.genreLabels
    val recentlyAdded by viewModel.recentlyAdded.collectAsState()
    val hubs by viewModel.hubs.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()

    var isSortMenuOpen by remember { mutableStateOf(false) }
    val currentSort by viewModel.currentSortOption.collectAsState()
    val currentGenre by viewModel.currentFilterGenre.collectAsState()
    val currentType by viewModel.currentFilterType.collectAsState()

    val gridState = rememberLazyGridState()
    var filterVersion by remember { mutableIntStateOf(0) }

    val sortMenuFocusRequester = remember { FocusRequester() }
    val contentFocusRequester = remember { FocusRequester() }

    val plexColor = Color(0xFFE5A00D)
    val plexBackgroundColor =
        Brush.verticalGradient(colors = listOf(Color(0xFF282828), Color(0xFF141414)))

    LaunchedEffect(isSortMenuOpen) {
        if (isSortMenuOpen) {
            sortMenuFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(filterVersion) { if (filterVersion > 0) gridState.scrollToItem(0) }

    // Fix focus management: Request focus on content when screen loads
    LaunchedEffect(Unit) {
        contentFocusRequester.requestFocus()
    }

    val displayTitle =
        when (currentType) {
            "movie" -> "Films"
            "show" -> "Séries"
            else -> "Médiathèque"
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(plexBackgroundColor)
    ) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(6),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing_xl),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_l),
            contentPadding =
                PaddingValues(
                    bottom = Dimens.spacing_xxxl,
                    start = Dimens.spacing_xxxl,
                    end = Dimens.spacing_xxxl
                ),
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(contentFocusRequester)
        ) {
            if (movies.loadState.refresh is LoadState.Loading && movies.itemCount == 0
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(350.dp)
                                .padding(bottom = Dimens.spacing_xl)
                                .clip(
                                    RoundedCornerShape(
                                        bottomStart =
                                            Dimens.spacing_l,
                                        bottomEnd =
                                            Dimens.spacing_l
                                    )
                                )
                                .background(Color(0xFF2E2E2E))
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier =
                            Modifier.padding(
                                vertical = Dimens.spacing_l
                            )
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(200.dp, 30.dp)
                                    .background(
                                        Color(0xFF2E2E2E),
                                        RoundedCornerShape(
                                            Dimens.spacing_xs
                                        )
                                    )
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacing_l))
                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    Dimens.spacing_m
                                )
                        ) {
                            repeat(6) {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(
                                                80.dp,
                                                30.dp
                                            )
                                            .background(
                                                Color(
                                                    0xFF2E2E2E
                                                ),
                                                RoundedCornerShape(
                                                    50
                                                )
                                            )
                                )
                            }
                        }
                    }
                }
                items(20) { SkeletonCard() }
            } else {
                if (continueWatchingItems.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ContinueWatchingCarousel(
                            items = continueWatchingItems,
                            onItemClick = onMovieClick
                        )
                    }
                }

                if (featuredMovies.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        FeaturedCarousel(
                            movies = featuredMovies,
                            onMovieClick = onMovieClick,
                            modifier =
                                Modifier.padding(
                                    bottom = Dimens.spacing_xl
                                )
                        )
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        bottom =
                                            Dimens.spacing_l
                                    ),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = displayTitle,
                                    style =
                                        MaterialTheme
                                            .typography
                                            .headlineSmall,
                                    fontWeight =
                                        FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(
                                    modifier =
                                        Modifier.width(
                                            Dimens.spacing_m
                                        )
                                )
                                Text(
                                    "$totalCount TITRES",
                                    style =
                                        MaterialTheme
                                            .typography
                                            .labelMedium,
                                    color = Color.LightGray,
                                    modifier =
                                        Modifier.padding(
                                            bottom =
                                                Dimens.spacing_xs
                                        )
                                )
                            }

                            SortMenu(
                                currentSort = currentSort,
                                onSortClick = {
                                    isSortMenuOpen = true
                                },
                                isSortMenuOpen = isSortMenuOpen,
                                onSortChange = {
                                    viewModel.onSortChange(it)
                                    filterVersion++
                                    isSortMenuOpen = false
                                },
                                onDismiss = {
                                    isSortMenuOpen = false
                                },
                                focusRequester =
                                    sortMenuFocusRequester
                            )
                        }

                        FilterBar(
                            items = genreLabels,
                            selectedItem = currentGenre,
                            onItemSelected = {
                                viewModel.onGenreChange(it)
                                filterVersion++
                            }
                        )
                    }
                }

                // --- NEW SECTIONS ---
                // recentlyAdded and hubs are collected at the top

                if (recentlyAdded.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        MediaRow(
                            title = "Récemment ajoutés",
                            movies = recentlyAdded,
                            onMovieClick = onMovieClick,
                            plexColor = plexColor
                        )
                    }
                }

                hubs.forEach { (hubTitle, hubMovies) ->
                    if (hubMovies.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            MediaRow(
                                title = hubTitle,
                                movies = hubMovies,
                                onMovieClick = onMovieClick,
                                plexColor = plexColor
                            )
                        }
                    }
                }

                if (watchHistory.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        MediaRow(
                            title = "Historique",
                            movies = watchHistory,
                            onMovieClick = onMovieClick,
                            plexColor = plexColor
                        )
                    }
                }

                // --- END NEW SECTIONS ---

                items(
                    count = movies.itemCount,
                    key = movies.itemKey { it.id },
                    contentType = movies.itemContentType { "movie" }
                ) { index ->
                    val movie = movies[index]
                    if (movie != null) {
                        MovieCard(
                            movie = movie,
                            onClick = { onMovieClick(movie) },
                            plexColor = plexColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (movies.loadState.append is LoadState.Loading) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier =
                                Modifier
                                    .height(100.dp)
                                    .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(30.dp),
                                color = plexColor
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Carte de média affichant l'image et le titre. Gère le focus et l'animation de mise à l'échelle.
 */

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieCard(
    movie: Movie,
    onClick: () -> Unit,
    plexColor: Color,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val context = LocalContext.current

    val scale by
    animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .scale(scale)
            .zIndex(if (isFocused) 10f else 1f)
    ) {
        Surface(
            onClick = { onClick() },
            shape =
                ClickableSurfaceDefaults.shape(
                    shape = RoundedCornerShape(Dimens.spacing_s)
                ),
            modifier =
                Modifier
                    .height(165.dp)
                    .fillMaxWidth()
                    .focusable(interactionSource = interactionSource)
        ) {
            AsyncImage(
                model =
                    ImageRequest.Builder(context)
                        .data(movie.posterUrl)
                        .crossfade(true)
                        .size(300, 450)
                        .build(),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (isFocused) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .border(
                                Dimens.spacing_xxs,
                                Color.White,
                                RoundedCornerShape(Dimens.spacing_s)
                            )
                )
            }

            if (movie.hasMultipleSources) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(Dimens.spacing_xs)
                            .background(
                                plexColor,
                                RoundedCornerShape(
                                    Dimens.spacing_xxs
                                )
                            )
                            .padding(
                                horizontal = Dimens.spacing_xs,
                                vertical = Dimens.spacing_xxs
                            )
                ) {
                    Text(
                        "MULTI",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            val displayRating = movie.imdbRating ?: movie.rating
            if (displayRating != null && displayRating > 0) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(Dimens.spacing_xs)
                            .background(
                                Color.Black.copy(alpha = 0.7f),
                                RoundedCornerShape(
                                    Dimens.spacing_xxs
                                )
                            )
                            .padding(
                                horizontal = Dimens.spacing_xs,
                                vertical = Dimens.spacing_xxs
                            )
                ) {
                    Text(
                        "★ $displayRating",
                        fontWeight = FontWeight.Bold,
                        color = plexColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spacing_s))

        Text(
            text = movie.title,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            color = if (isFocused) Color.White else Color.LightGray,
            maxLines = 1,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal
        )
    }
}
