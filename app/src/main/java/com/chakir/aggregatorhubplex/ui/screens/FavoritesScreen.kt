package com.chakir.aggregatorhubplex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.chakir.aggregatorhubplex.data.Movie
import com.chakir.aggregatorhubplex.data.local.FavoriteEntity
import com.chakir.aggregatorhubplex.data.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- VIEWMODEL ---
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: FavoriteRepository
) : ViewModel() {
    private val _favorites = MutableStateFlow<List<FavoriteEntity>>(emptyList())
    val favorites: StateFlow<List<FavoriteEntity>> = _favorites

    init {
        viewModelScope.launch {
            repository.getAllFavorites().collect {
                _favorites.value = it
            }
        }
    }
}

// --- SCREEN ---
@Composable
fun FavoritesScreen(
    onMovieClick: (String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.padding(48.dp)) {
            Text(
                "Mes Favoris",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (favorites.isEmpty()) {
                Text("Aucun favori pour le moment.", color = MaterialTheme.colorScheme.secondary)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(110.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(favorites) { fav ->
                        // CORRECTION ICI : On utilise les vrais noms de champs du DataLayer
                        val movie = Movie(
                            id = fav.mediaId,
                            title = fav.title,
                            // Le constructeur attend 'posterPath', pas 'posterUrl'
                            posterPath = fav.posterUrl,
                            // Le constructeur attend 'type' (String), pas 'isSeries' (Boolean)
                            type = fav.type
                        )

                        // On réutilise votre MovieCard existant
                        MovieCard(movie = movie, onClick = { onMovieClick(fav.mediaId) })
                    }
                }
            }
        }
    }
}