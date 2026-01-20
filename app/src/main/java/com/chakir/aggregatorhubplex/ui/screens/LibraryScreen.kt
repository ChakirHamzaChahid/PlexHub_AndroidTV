package com.chakir.aggregatorhubplex.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.onFocusChanged
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.chakir.aggregatorhubplex.domain.model.Movie
import com.chakir.aggregatorhubplex.ui.components.FilterBar
import com.chakir.aggregatorhubplex.ui.components.SkeletonCard
import com.chakir.aggregatorhubplex.ui.components.SortMenu
import com.chakir.aggregatorhubplex.ui.theme.Dimens
import com.chakir.aggregatorhubplex.ui.components.MovieCard

/**
 * Écran dédié à l'affichage d'une bibliothèque (Films ou Séries) sous forme de grille.
 * Gère le filtrage par genre et le tri.
 *
 * @param type Le type de média à afficher ("movie" ou "show").
 * @param onMovieClick Callback lorsqu'un média est cliqué.
 * @param viewModel ViewModel partagé (HomeViewModel) pour la logique de données.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LibraryScreen(
    type: String,
    onMovieClick: (Movie) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // Initialiser le filtre sur le type spécifique
    LaunchedEffect(type) {
        viewModel.onTypeChange(type)
    }

    val movies = viewModel.moviesPagingFlow.collectAsLazyPagingItems()
    val totalCount by viewModel.totalCount.collectAsState()
    val genreLabels = viewModel.genreLabels

    var isSortMenuOpen by remember { mutableStateOf(false) }
    val currentSort by viewModel.currentSortOption.collectAsState()
    val currentGenre by viewModel.currentFilterGenre.collectAsState()

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
    
    // Request focus and restore scroll position on content when screen loads
    val lastFocusedIndices by viewModel.lastFocusedIndex.collectAsState()
    
    // Only request focus if we have items
    LaunchedEffect(Unit, movies.itemCount) {
        if (movies.itemCount > 0) {
            val savedIndex = lastFocusedIndices[type] ?: 0
            if (savedIndex > 0 && savedIndex < movies.itemCount) {
                gridState.scrollToItem(savedIndex)
            }
             contentFocusRequester.requestFocus()
        }
    }

    val displayTitle =
        when (type) {
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
            columns = GridCells.Fixed(6), // Grille fixe de 6 colonnes
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
            // Header: Titre, Compteur, Tri et Filtres
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    bottom =
                                        Dimens.spacing_l,
                                    top = Dimens.spacing_l // Marge en haut pour le titre
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

            // Loading Initial
            if (movies.loadState.refresh is LoadState.Loading && movies.itemCount == 0) {
                 items(20) { SkeletonCard() }
            } else {
                // Items de la grille
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused || focusState.hasFocus) {
                                        viewModel.setLastFocusedIndex(type, index)
                                    }
                                }
                        )
                    } else {
                        SkeletonCard(modifier = Modifier.fillMaxWidth())
                    }
                }

                // Append Loading
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
