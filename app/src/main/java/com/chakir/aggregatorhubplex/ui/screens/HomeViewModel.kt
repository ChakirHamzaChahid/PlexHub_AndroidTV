package com.chakir.aggregatorhubplex.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.chakir.aggregatorhubplex.data.GenreGrouping
import com.chakir.aggregatorhubplex.domain.model.Movie
import com.chakir.aggregatorhubplex.data.preferences.PreferencesManager
import com.chakir.aggregatorhubplex.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// --- ÉTATS ET ÉNUMÉRATIONS ---
/** État interne de l'écran d'accueil. */
data class HomeState(
        var searchQuery: String = "",
        var selectedSort: SortOption = SortOption.ADDED_DESC,
        var selectedType: String? = null
)

/** Options de tri disponibles pour les médias. */
enum class SortOption(val label: String, val apiValue: String, val orderValue: String) {
    ADDED_DESC("Ajouts récents", "added_at", "desc"),
    TITLE("Titre (A-Z)", "title", "asc"),
    YEAR_DESC("Année (Récent)", "year", "desc"),
    YEAR_ASC("Année (Ancien)", "year", "asc"),
    RATING_DESC("Note (Haute)", "rating", "desc"),
    RATING_ASC("Note (Basse)", "rating", "asc");

    companion object {
        fun fromString(value: String): SortOption {
            return entries.find { it.name == value } ?: ADDED_DESC
        }
    }
}

// --- VIEWMODEL ---

/**
 * ViewModel pour l'écran d'accueil. Gère le chargement, le filtrage, le tri et la recherche des
 * médias.
 */
@HiltViewModel
class HomeViewModel
@Inject
constructor(
        private val repository: MediaRepository,
        private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow<String?>(null)
    private val _filterType = MutableStateFlow<String?>(null)
    private val _sortOption = MutableStateFlow(SortOption.ADDED_DESC)
    private val _filterGenreLabel = MutableStateFlow("Tout")

    // --- ÉTATS EXPOSÉS ---
    val currentFilterType: StateFlow<String?> = _filterType
    val currentSortOption: StateFlow<SortOption> = _sortOption
    val currentSearchQuery: StateFlow<String?> = _searchQuery
    val currentFilterGenre: StateFlow<String> = _filterGenreLabel
    val genreLabels: List<String> = GenreGrouping.UI_LABELS

    val continueWatchingItems: StateFlow<List<Movie>> =
            repository
                    .getContinueWatching()
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyAdded: StateFlow<List<Movie>> =
            repository
                    .getRecentlyAdded(20)
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hubs: StateFlow<Map<String, List<Movie>>> =
            repository
                    .getHubs()
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val watchHistory: StateFlow<List<Movie>> =
            repository
                    .getWatchHistory()
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // Charger l'option de tri sauvegardée au démarrage
            val savedSort = preferencesManager.sortOption.first()
            _sortOption.value = SortOption.fromString(savedSort)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val featuredMovies: StateFlow<List<Movie>> =
            currentFilterType
                    .flatMapLatest { type ->
                        val limit = if (type == "show") 30 else 20
                        repository.getTopRated(type, limit)
                    }
                    .map { movies -> movies.shuffled().take(5) }
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalCount: StateFlow<Int> =
            combine(_searchQuery, _filterType, _filterGenreLabel) { query, type, genre ->
                        Triple(query, type, genre)
                    }
                    .flatMapLatest { (query, type, genre) ->
                        repository.getFilteredCount(query, type, genre)
                    }
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val moviesPagingFlow: Flow<PagingData<Movie>> =
            combine(_searchQuery, _filterType, _filterGenreLabel, _sortOption) {
                            query,
                            type,
                            genreLabel,
                            sortOpt ->
                        QueryConfig(query, type, genreLabel, sortOpt)
                    }
                    .flatMapLatest { config ->
                        repository.getMediaPaged(
                                search = config.query,
                                type = config.type,
                                genreLabel = config.genreLabel,
                                sort = config.sortOpt
                        )
                    }
                    .cachedIn(viewModelScope)

    // --- ACTIONS ---
    fun onTypeChange(type: String) {
        _filterType.value = if (type == "all") null else type
    }

    fun onSortChange(option: SortOption) {
        _sortOption.value = option
        viewModelScope.launch { preferencesManager.setSortOption(option.name) }
    }

    fun onSearchChange(query: String) {
        _searchQuery.value = query.ifBlank { null }
    }

    fun onGenreChange(genreLabel: String) {
        _filterGenreLabel.value = genreLabel
    }
}

// Utility class
/** Configuration de la requête pour paged list. */
data class QueryConfig(
        val query: String?,
        val type: String?,
        val genreLabel: String,
        val sortOpt: SortOption
)
