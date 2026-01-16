package com.chakir.aggregatorhubplex.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chakir.aggregatorhubplex.data.Movie
import com.chakir.aggregatorhubplex.data.NetworkModule
import com.chakir.aggregatorhubplex.data.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _movie = MutableStateFlow<Movie?>(null)
    val movie: StateFlow<Movie?> = _movie

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    private var favoriteObservationJob: Job? = null

    fun loadMovie(movieId: String) {
        // On annule l'ancienne observation pour éviter les fuites si on charge un nouveau film
        favoriteObservationJob?.cancel()

        // COROUTINE 1 : Observe en continu le statut de favori
        favoriteObservationJob = viewModelScope.launch {
            favoriteRepository.isFavorite(movieId).collectLatest { isFav ->
                _isFavorite.value = isFav
            }
        }

        // COROUTINE 2 : Charge les données du film (opération unique)
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fetchedMovie = NetworkModule.api.getMovieDetail(movieId)
                _movie.value = fetchedMovie
            } catch (e: Exception) {
                e.printStackTrace()
                // Gérer l'erreur dans l'UI si nécessaire
            } finally {
                // Cette coroutine se termine, donc le finally est toujours atteint
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite() {
        val currentMovie = _movie.value ?: return
        viewModelScope.launch {
            // La mise à jour de l'UI se fait automatiquement via la coroutine d'observation
            if (_isFavorite.value) {
                favoriteRepository.removeFromFavorites(currentMovie.id)
            } else {
                favoriteRepository.addToFavorites(currentMovie)
            }
        }
    }
}