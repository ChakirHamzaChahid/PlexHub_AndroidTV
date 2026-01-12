package com.chakir.aggregatorhubplex.ui.screens

import androidx.lifecycle.ViewModel // On passe de AndroidViewModel à ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.chakir.aggregatorhubplex.data.Movie
import com.chakir.aggregatorhubplex.data.local.AppDatabase
import dagger.hilt.android.lifecycle.HiltViewModel // Import Hilt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject // Import Inject

// --- ÉTAT ET ENUMS ---
data class HomeState(
    var searchQuery: String = "",
    var selectedSort: SortOption = SortOption.ADDED_DESC,
    var selectedType: String? = null
)

enum class SortOption(val label: String, val apiValue: String, val orderValue: String) {
    ADDED_DESC("Ajouts récents", "added_at", "desc"),
    TITLE("Titre (A-Z)", "title", "asc"),
    YEAR_DESC("Année (Récent)", "year", "desc"),
    YEAR_ASC("Année (Ancien)", "year", "asc"),
    RATING_DESC("Note (Haute)", "rating", "desc"),
    RATING_ASC("Note (Basse)", "rating", "asc")
}

// --- VIEWMODEL ---

@HiltViewModel // ANNOTATION OBLIGATOIRE
class HomeViewModel @Inject constructor(
    private val database: AppDatabase // Injecté automatiquement par Hilt via le Module (voir étape suivante si erreur)
) : ViewModel() {

    // Si Hilt n'arrive pas à injecter AppDatabase directement, on peut utiliser Application comme fallback,
    // mais essayons d'abord l'injection propre.
    // Si ça plante ici, remplacez par : class HomeViewModel @Inject constructor(application: Application) : AndroidViewModel(application)
    // et faites val database = AppDatabase.getDatabase(application)

    private val _searchQuery = MutableStateFlow<String?>(null)
    private val _filterType = MutableStateFlow<String?>(null)
    private val _sortOption = MutableStateFlow(SortOption.ADDED_DESC)

    // --- AJOUTEZ CES 3 LIGNES ---
    // On expose l'état en lecture seule pour l'UI
    val currentFilterType: StateFlow<String?> = _filterType.asStateFlow()
    val currentSortOption: StateFlow<SortOption> = _sortOption.asStateFlow()
    val currentSearchQuery: StateFlow<String?> = _searchQuery.asStateFlow()
    // ----------------------------
    val totalCount: StateFlow<Int> = database.movieDao().getCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val moviesPagingFlow: Flow<PagingData<Movie>> = combine(
        _searchQuery, _filterType, _sortOption
    ) { query, type, sortOpt -> Triple(query, type, sortOpt) }
        .flatMapLatest { (query, type, sortOpt) ->
            Pager(
                config = PagingConfig(pageSize = 50, enablePlaceholders = true, maxSize = 300),
                pagingSourceFactory = {
                    database.movieDao().getMoviesPaged(
                        query, type, sortOpt.apiValue, sortOpt.orderValue
                    )
                }
            ).flow.map { pagingData -> pagingData.map { it.toMovie() } }
        }.cachedIn(viewModelScope)

    fun onTypeChange(type: String) { _filterType.value = if (type == "all") null else type }
    fun onSortChange(option: SortOption) { _sortOption.value = option }
    fun onSearchChange(query: String) { _searchQuery.value = query.ifBlank { null } }
}