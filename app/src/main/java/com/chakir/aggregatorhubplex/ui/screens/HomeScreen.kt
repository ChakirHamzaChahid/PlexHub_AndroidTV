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
import androidx.tv.material3.Surface
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chakir.aggregatorhubplex.data.Movie
import com.chakir.aggregatorhubplex.ui.components.FeaturedCarousel
import com.chakir.aggregatorhubplex.ui.components.FilterBar
import com.chakir.aggregatorhubplex.ui.components.SkeletonCard
import com.chakir.aggregatorhubplex.ui.components.SortMenu

val PlexAccent = Color(0xFFE5A00D)
val NetflixBlack = Color(0xFF141414)
val DarkSurface = Color(0xFF1F1F1F)
val TextWhite = Color(0xFFE5E5E5)
val TextGrey = Color(0xFFB3B3B3)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    onMovieClick: (Movie) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val movies = viewModel.moviesPagingFlow.collectAsLazyPagingItems()
    val featuredMovies by viewModel.featuredMovies.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val genreLabels = viewModel.genreLabels

    var isSortMenuOpen by remember { mutableStateOf(false) }
    val currentSort by viewModel.currentSortOption.collectAsState()
    val currentGenre by viewModel.currentFilterGenre.collectAsState()
    val currentType by viewModel.currentFilterType.collectAsState()

    val gridState = rememberLazyGridState()
    var filterVersion by remember { mutableIntStateOf(0) }

    val sortMenuFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isSortMenuOpen) {
        if (isSortMenuOpen) {
            sortMenuFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(filterVersion) {
        if (filterVersion > 0) gridState.scrollToItem(0)
    }

    val displayTitle = when (currentType) {
        "movie" -> "Films"
        "show" -> "Séries"
        else -> "Médiathèque"
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(NetflixBlack)) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Adaptive(minSize = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 48.dp, start = 48.dp, end = 48.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (movies.loadState.refresh is LoadState.Loading && movies.itemCount == 0) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .padding(bottom = 24.dp)
                            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                            .background(DarkSurface)
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
                        Box(modifier = Modifier
                            .size(200.dp, 30.dp)
                            .background(DarkSurface, RoundedCornerShape(4.dp)))
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            repeat(6) {
                                Box(modifier = Modifier
                                    .size(80.dp, 30.dp)
                                    .background(DarkSurface, RoundedCornerShape(50)))
                            }
                        }
                    }
                }
                items(20) {
                    SkeletonCard()
                }
            } else {
                if (featuredMovies.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        FeaturedCarousel(movies = featuredMovies, onMovieClick = onMovieClick, modifier = Modifier.padding(bottom = 24.dp))
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = displayTitle,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "$totalCount TITRES",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextGrey,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            SortMenu(
                                currentSort = currentSort,
                                onSortClick = { isSortMenuOpen = true },
                                isSortMenuOpen = isSortMenuOpen,
                                onSortChange = {
                                    viewModel.onSortChange(it)
                                    filterVersion++
                                    isSortMenuOpen = false
                                },
                                onDismiss = { isSortMenuOpen = false },
                                focusRequester = sortMenuFocusRequester
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

                items(
                    count = movies.itemCount,
                    key = movies.itemKey { it.id },
                    contentType = movies.itemContentType { "movie" }
                ) { index ->
                    val movie = movies[index]
                    if (movie != null) {
                        MovieCard(movie = movie, onClick = { onMovieClick(movie) })
                    }
                }

                if (movies.loadState.append is LoadState.Loading) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(modifier = Modifier
                            .height(100.dp)
                            .fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(30.dp), color = PlexAccent)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieCard(movie: Movie, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val context = LocalContext.current

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(110.dp)
            .scale(scale)
            .zIndex(if (isFocused) 10f else 1f)
    ) {
        Surface(
            onClick = { onClick() },
            shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(8.dp)),
            modifier = Modifier
                .height(165.dp)
                .fillMaxWidth()
                .focusable(interactionSource = interactionSource)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
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
                    modifier = Modifier
                        .fillMaxSize()
                        .border(3.dp, Color.White, RoundedCornerShape(8.dp))
                )
            }

            if (movie.hasMultipleSources) {
                Box(modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(PlexAccent, RoundedCornerShape(2.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)) {
                    Text("MULTI", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            val displayRating = movie.imdbRating ?: movie.rating
            if (displayRating != null && displayRating > 0) {
                Box(modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)) {
                    Text("★ $displayRating", fontWeight = FontWeight.Bold, color = PlexAccent)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = movie.title,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            color = if (isFocused) PlexAccent else TextGrey,
            maxLines = 1,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal
        )
    }
}
