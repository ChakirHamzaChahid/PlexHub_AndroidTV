package com.chakir.aggregatorhubplex.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.sqlite.db.SimpleSQLiteQuery
import com.chakir.aggregatorhubplex.data.GenreGrouping
import com.chakir.aggregatorhubplex.data.dto.MovieListItem
import com.chakir.aggregatorhubplex.data.dto.ProgressRequest
import com.chakir.aggregatorhubplex.data.dto.ScrobbleRequest
import com.chakir.aggregatorhubplex.data.local.AppDatabase
import com.chakir.aggregatorhubplex.data.local.MovieEntity
import com.chakir.aggregatorhubplex.data.network.MovieApiService
import com.chakir.aggregatorhubplex.domain.model.Movie
import com.chakir.aggregatorhubplex.ui.screens.SortOption
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine

/**
 * Implémentation du [MediaRepository]. Combine les données locales (Room) et distantes (API
 * Retrofit). utilise [Pager] pour la pagination et [SimpleSQLiteQuery] pour les requêtes dynamiques
 * complexes.
 */
class MediaRepositoryImpl
@Inject
constructor(private val database: AppDatabase, private val api: MovieApiService) : MediaRepository {

    override fun getMediaPaged(
        search: String?,
        type: String?,
        genreLabel: String,
        sort: SortOption
    ): Flow<PagingData<Movie>> {
        return Pager(
            config =
                PagingConfig(
                    pageSize = 50,
                    enablePlaceholders = true,
                    maxSize = 300
                ),
            pagingSourceFactory = {
                val keywords = GenreGrouping.GROUPS[genreLabel] ?: emptyList()
                val sqlQuery = buildRawQuery(search, type, keywords, sort)
                database.movieDao().getMoviesPagedRaw(sqlQuery)
            }
        )
            .flow
            .map { pagingData -> pagingData.map { it.toMovie() } }
    }

    override fun getFilteredCount(search: String?, type: String?, genreLabel: String): Flow<Int> {
        val keywords = GenreGrouping.GROUPS[genreLabel] ?: emptyList()
        val sqlQuery = buildCountQuery(search, type, keywords)
        return database.movieDao().getFilteredCount(sqlQuery)
    }

    override fun getTopRated(type: String?, limit: Int): Flow<List<Movie>> {
        return database.movieDao().getTopRated(type, limit).map {
            it.map { entity -> entity.toMovie() }
        }
    }

    /**
     * Stratégie "Network-bound Resource" simplifiée :
     * 1. Émet immédiatement les données locales.
     * 2. Appelle l'API pour rafraîchir les données.
     * 3. Sauvegarde en base (ce qui déclenche une nouvelle émission via le Flow de Room).
     */
    override fun getMovieDetail(movieId: String): Flow<Movie?> = flow {
        // 1. Local
        emit(database.movieDao().getMovieById(movieId)?.toMovie())
        try {
            // 2. Réseau
            val networkMovie = api.getMovieDetail(movieId)
            // 3. Sauvegarde
            database.movieDao().upsertAll(listOf(networkMovie.toEntity()))
            // La mise à jour de la DB notifiera automatiquement les autres observateurs,
            // mais ici on ré-émet explicitement pour l'appelant direct si besoin.
            emit(database.movieDao().getMovieById(movieId)?.toMovie())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun getAvailableGenres(): List<String> {
        return database.movieDao()
            .getAllGenres()
            .flatMap { it.split(",") }
            .map { it.trim() }
            .distinct()
            .sorted()
    }

    override fun getContinueWatching(): Flow<List<Movie>> =
        database.playHistoryDao().getRecentHistory().map { historyList ->
            historyList.filter { !it.isFinished }.map { historyItem ->
                Movie(
                    id = historyItem.mediaId,
                    ratingKey = historyItem.mediaId,
                    title = historyItem.title,
                    type = historyItem.type,
                    posterPath = historyItem.posterUrl,
                    viewOffset = historyItem.positionMs, // Important for UI progress bar
                    duration = historyItem.durationMs, // Important for UI progress bar
                    backdropPath = historyItem.backdropUrl,
                    // Default values for other fields
                    addedAt = historyItem.lastPlayedAt.toString(),
                    year = 0,
                    rating = 0f,
                    imdbRating = null,
                    genres = emptyList(),
                    director = null,
                    contentRating = null,
                    studio = null,
                    description = "",
                    badges = emptyList(),
                    rottenRating = null,
                    runtime = historyItem.durationMs.toInt(),
                    hasMultipleSources = false,
                    viewCount = 0,
                    audioTracks = emptyList(),
                    subtitles = emptyList(),
                    chapters = emptyList(),
                    markers = emptyList(),
                    trailers = emptyList(),
                    similar = emptyList(),
                    actors = emptyList(),
                    seasons = emptyList(),
                    grandparentKey = historyItem.seriesId,
                    grandparentTitle = null,
                    parentIndex = null,
                    index = null
                )
            }
        }

    override fun getWatchHistory(page: Int, size: Int): Flow<List<Movie>> =
        database.playHistoryDao().getRecentHistory().map { historyList ->
            historyList.map { historyItem ->
                Movie(
                    id = historyItem.mediaId,
                    ratingKey = historyItem.mediaId,
                    title = historyItem.title,
                    type = historyItem.type,
                    posterPath = historyItem.posterUrl,
                    viewOffset = historyItem.positionMs,
                    duration = historyItem.durationMs,
                    backdropPath = historyItem.backdropUrl,
                    addedAt = historyItem.lastPlayedAt.toString(),
                    year = 0,
                    rating = 0f,
                    imdbRating = null,
                    genres = emptyList(),
                    director = null,
                    contentRating = null,
                    studio = null,
                    description = "",
                    badges = emptyList(),
                    rottenRating = null,
                    runtime = historyItem.durationMs.toInt(),
                    hasMultipleSources = false,
                    viewCount = 0,
                    audioTracks = emptyList(),
                    subtitles = emptyList(),
                    chapters = emptyList(),
                    markers = emptyList(),
                    trailers = emptyList(),
                    similar = emptyList(),
                    actors = emptyList(),
                    seasons = emptyList(),
                    grandparentKey = null,
                    grandparentTitle = null,
                    parentIndex = null,
                    index = null
                )
            }
        }

    override fun getHubs(): Flow<Map<String, List<Movie>>> =
        combine(
            database.movieDao().getRecentlyAddedByType("movie", 10),
            database.movieDao().getRecentlyAddedByType("show", 10),
            database.movieDao().getTopRated(null, 10)
        ) { recentMovies, recentShows, topRated ->
            mapOf(
                "Films récemment ajoutés" to recentMovies.map { it.toMovie() },
                "Séries récemment ajoutées" to recentShows.map { it.toMovie() },
                "Les mieux notés" to topRated.map { it.toMovie() }
            )
        }

    override fun getRecentlyAdded(limit: Int): Flow<List<Movie>> {
        return database.movieDao().getRecentlyAdded(limit).map { entities ->
            entities.map { it.toMovie() }
        }
    }

    override fun search(
        title: String,
        year: Int?,
        unwatched: Boolean?,
        limit: Int
    ): Flow<List<Movie>> = flow {
        try {
            val results = api.search(title, year, unwatched, limit).map { it.toMovie() }
            emit(results)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    override suspend fun scrobbleMedia(id: String, action: String) {
        if (action == "watched") {
            database.playHistoryDao().updateHistoryStatus(id, true)
        } else if (action == "unwatched") {
            database.playHistoryDao().deleteHistoryItem(id)
        }
    }

    override suspend fun updateProgress(id: String, timeMs: Long, serverName: String) {
        // Deprecated: Use PlayHistoryRepository.savePlaybackPosition directly in ViewModel
        // No-op here to break backend dependency
    }

    override suspend fun toggleFavorite(id: String) {
        try {
            api.toggleFavorite(id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun rateMedia(id: String, rating: Float) {
        try {
            api.rateMedia(id, rating)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Construit une requête SQL dynamique pour la récupération paginée. Gère la recherche FTS, le
     * filtrage par type et par genre, et le tri dynamique.
     */
    private fun buildRawQuery(
        search: String?,
        type: String?,
        genreKeywords: List<String>,
        sort: SortOption
    ): SimpleSQLiteQuery {
        val args = ArrayList<Any>()
        var sql = "SELECT m.* FROM movies AS m"
        val whereClauses = ArrayList<String>()

        if (!search.isNullOrBlank()) {
            sql += " JOIN movies_fts ON m.rowid = movies_fts.rowid"
            whereClauses.add("movies_fts MATCH ?")
            val ftsQuery = search.split(" ").filter { it.isNotBlank() }.joinToString(" ") { "$it*" }
            args.add(ftsQuery)
        }

        if (type != null) {
            whereClauses.add("m.type = ?")
            args.add(type)
        }

        if (genreKeywords.isNotEmpty()) {
            // Recherche si l'un des mots-clés est présent dans la liste des genres
            val genreOrClauses = genreKeywords.joinToString(" OR ") { "m.genres LIKE ?" }
            whereClauses.add("($genreOrClauses)")
            genreKeywords.forEach { args.add("%$it%") }
        }

        if (whereClauses.isNotEmpty()) {
            sql += " WHERE " + whereClauses.joinToString(" AND ")
        }

        // Le tri est désactivé lors d'une recherche textuelle pour prioriser la pertinence FTS
        if (search.isNullOrBlank()) {
            sql +=
                " ORDER BY " +
                        when (sort) {
                            SortOption.ADDED_DESC -> "m.addedAt DESC"
                            SortOption.TITLE -> "m.title ASC"
                            SortOption.YEAR_DESC -> "m.year DESC"
                            SortOption.YEAR_ASC -> "m.year ASC"
                            SortOption.RATING_DESC -> "COALESCE(m.imdbRating, m.rating) DESC"
                            SortOption.RATING_ASC -> "COALESCE(m.imdbRating, m.rating) ASC"
                        }
            sql += ", m.id DESC"
        }

        return SimpleSQLiteQuery(sql, args.toArray())
    }

    private fun buildCountQuery(
        search: String?,
        type: String?,
        genreKeywords: List<String>
    ): SimpleSQLiteQuery {
        val args = ArrayList<Any>()
        var sql = "SELECT COUNT(*) FROM movies AS m"
        val whereClauses = ArrayList<String>()

        if (!search.isNullOrBlank()) {
            sql += " JOIN movies_fts ON m.rowid = movies_fts.rowid"
            whereClauses.add("movies_fts MATCH ?")
            val ftsQuery = search.split(" ").filter { it.isNotBlank() }.joinToString(" ") { "$it*" }
            args.add(ftsQuery)
        }

        if (type != null) {
            whereClauses.add("m.type = ?")
            args.add(type)
        }

        if (genreKeywords.isNotEmpty()) {
            val genreOrClauses = genreKeywords.joinToString(" OR ") { "m.genres LIKE ?" }
            whereClauses.add("($genreOrClauses)")
            genreKeywords.forEach { args.add("%$it%") }
        }

        if (whereClauses.isNotEmpty()) {
            sql += " WHERE " + whereClauses.joinToString(" AND ")
        }

        return SimpleSQLiteQuery(sql, args.toArray())
    }

    private fun Movie.toEntity(): MovieEntity {
        return MovieEntity(
            id = this.id,
            title = this.title,
            type = this.type,
            posterUrl = this.posterPath,
            backdrop_url = this.backdropPath,
            year = this.year,
            addedAt = this.addedAt,
            rating = this.rating,
            imdbRating = this.imdbRating,
            rottenRating = this.rottenRating,
            director = this.director,
            genres = this.genres,
            description = this.description,
            studio = this.studio,
            contentRating = this.contentRating,
            servers = this.servers,
            seasons = this.seasons,
            hasMultipleSources = this.hasMultipleSources
        )
    }

    private fun MovieListItem.toMovie(): Movie {
        // Logique de résolution d'ID pour éviter les 404 si le backend attend un format spécifique
        // (tt...)
        val resolvedId =
            when {
                !imdbId.isNullOrEmpty() -> imdbId
                !guid.isNullOrEmpty() && guid.contains("tt") -> {
                    val extracted = guid.substringAfter("tt").substringBefore("?")
                    "tt$extracted"
                }

                else -> id
            }

        // Debug Log pour comprendre les IDs reçus - Uniquement pour les premiers items pour éviter
        // le spam
        if (title.length > 2) {
            android.util.Log.d(
                "MediaRepo",
                "Mapping item '${title.take(15)}': ID=$id, GUID=$guid, IMDB=$imdbId, RatingKey=$ratingKey -> Resolved=$resolvedId. Poster=${posterPath ?: thumb}"
            )
        }

        return Movie(
            id = resolvedId,
            ratingKey = this.ratingKey ?: this.id,
            title = this.title,
            type = this.type,
            year = this.year,
            posterPath = this.posterPath ?: this.thumb,
            backdropPath = this.backdropPath,
            rating = this.rating,
            imdbRating = this.imdbRating,
            genres = this.genres,
            addedAt = this.addedAt,
            director = this.director,
            contentRating = this.contentRating,
            studio = this.studio,
            description = this.summary,
            badges = this.badges,
            rottenRating = this.rottenRating,
            runtime = this.duration?.toInt() ?: 0,
            hasMultipleSources = this.hasMultipleSources,
            viewOffset = this.viewOffset ?: 0,
            viewCount = this.viewCount ?: 0,
            // Mapping des listes si non nulles
            audioTracks =
                this.audioTracks?.map {
                    com.chakir.aggregatorhubplex.domain.model.AudioTrack(
                        it.displayTitle ?: "",
                        it.language ?: "",
                        it.codec ?: "",
                        it.channels ?: 0,
                        it.forced
                    )
                },
            subtitles =
                this.subtitles?.map {
                    com.chakir.aggregatorhubplex.domain.model.Subtitle(
                        it.displayTitle ?: "",
                        it.language ?: "",
                        it.codec ?: "",
                        it.forced
                    )
                },
            chapters =
                this.chapters?.map {
                    com.chakir.aggregatorhubplex.domain.model.Chapter(
                        it.title ?: "",
                        it.startTime?.toInt() ?: 0,
                        it.endTime?.toInt() ?: 0,
                        it.thumb
                    )
                },
            markers =
                this.markers?.map {
                    com.chakir.aggregatorhubplex.domain.model.Marker(
                        it.title ?: "",
                        it.type ?: "",
                        it.startTime?.toInt() ?: 0,
                        it.endTime?.toInt() ?: 0
                    )
                },
            // Trailers n'est pas dans MovieListItem ? On verra si besoin.
            trailers = emptyList(), // Pas de trailers dans la liste simple
            similar = emptyList(),
            actors = emptyList(),
            seasons = emptyList(),
            // Mapping des champs de navigation
            grandparentKey = this.grandparentKey,
            grandparentTitle = this.grandparentTitle,
            parentIndex = this.parentIndex,
            index = this.index
        )
    }
}
