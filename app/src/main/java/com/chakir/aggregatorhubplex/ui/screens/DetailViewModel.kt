package com.chakir.aggregatorhubplex.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chakir.aggregatorhubplex.data.Movie
import com.chakir.aggregatorhubplex.data.NetworkModule
import com.chakir.aggregatorhubplex.data.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    // État du film chargé
    private val _movie = MutableStateFlow<Movie?>(null)
    val movie: StateFlow<Movie?> = _movie

    // État de chargement
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    // État Favori (Est-ce que ce film est en favori ?)
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    fun loadMovie(movieId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Charger le film depuis l'API
                val fetchedMovie = NetworkModule.api.getMovieDetail(movieId)
                _movie.value = fetchedMovie

                // 2. Vérifier si c'est un favori en base locale
                favoriteRepository.isFavorite(movieId).collectLatest { isFav ->
                    _isFavorite.value = isFav
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite() {
        val currentMovie = _movie.value ?: return
        viewModelScope.launch {
            if (_isFavorite.value) {
                favoriteRepository.removeFromFavorites(currentMovie.id)
            } else {
                favoriteRepository.addToFavorites(currentMovie)
            }
        }
    }
}