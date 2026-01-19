package com.chakir.aggregatorhubplex.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chakir.aggregatorhubplex.domain.model.Movie
import com.chakir.aggregatorhubplex.data.repository.FavoriteRepository
import com.chakir.aggregatorhubplex.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class DetailViewModel
@Inject
constructor(
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

    /**
     * Charge les détails d'un média (film ou série).
     *
     * @param movieId L'identifiant du média.
     */
    fun loadMovie(movieId: String) {
        // Annuler les anciens jobs pour éviter les fuites
        dataJob?.cancel()
        favoriteJob?.cancel()

        // COROUTINE 1 : Observe le statut de favori
        favoriteJob =
                viewModelScope.launch {
                    favoriteRepository.isFavorite(movieId).collectLatest { isFav ->
                        _isFavorite.value = isFav
                    }
                }

        // COROUTINE 2 : Observe les données du film (Cache & Réseau)
        dataJob =
                viewModelScope.launch {
                    mediaRepository.getMovieDetail(movieId).collectLatest { movieData ->
                        // Le premier chargement est terminé dès qu'on reçoit des données (même du
                        // cache)
                        if (movieData != null) {
                            _isLoading.value = false
                            _movie.value = movieData
                        }
                    }
                }
    }

    /** Bascule l'état "favori" du média actuel. */
    fun toggleFavorite() {
        val currentMovie = _movie.value ?: return
        viewModelScope.launch {
            // Appel API pour synchro
            try {
                mediaRepository.toggleFavorite(currentMovie.id)
                // On met à jour le repo local aussi si besoin, ou on attend le refresh
                if (_isFavorite.value) {
                    favoriteRepository.removeFromFavorites(currentMovie.id)
                } else {
                    favoriteRepository.addToFavorites(currentMovie)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun rateMovie(rating: Float) {
        val currentMovie = _movie.value ?: return
        viewModelScope.launch { mediaRepository.rateMedia(currentMovie.id, rating) }
    }

    fun scrobble(action: String) { // "watched" or "unwatched"
        val currentMovie = _movie.value ?: return
        viewModelScope.launch { mediaRepository.scrobbleMedia(currentMovie.id, action) }
    }
}
