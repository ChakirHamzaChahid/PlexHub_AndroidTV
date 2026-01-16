package com.chakir.aggregatorhubplex.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chakir.aggregatorhubplex.data.Movie
import com.chakir.aggregatorhubplex.data.repository.FavoriteRepository
import com.chakir.aggregatorhubplex.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val mediaRepository: MediaRepository, // <-- INJECTION DU REPOSITORY PRINCIPAL
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _movie = MutableStateFlow<Movie?>(null)
    val movie: StateFlow<Movie?> = _movie

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    private var dataJob: Job? = null
    private var favoriteJob: Job? = null

    fun loadMovie(movieId: String) {
        // Annuler les anciens jobs pour éviter les fuites
        dataJob?.cancel()
        favoriteJob?.cancel()

        // COROUTINE 1 : Observe le statut de favori
        favoriteJob = viewModelScope.launch {
            favoriteRepository.isFavorite(movieId).collectLatest { isFav ->
                _isFavorite.value = isFav
            }
        }

        // COROUTINE 2 : Observe les données du film (Cache & Network)
        dataJob = viewModelScope.launch {
            mediaRepository.getMovieDetail(movieId).collectLatest { movieData ->
                // Le premier chargement est terminé dès qu'on reçoit des données (même du cache)
                if (movieData != null) {
                    _isLoading.value = false
                    _movie.value = movieData
                }
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
