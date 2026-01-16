package com.chakir.aggregatorhubplex.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.sqlite.db.SimpleSQLiteQuery
import com.chakir.aggregatorhubplex.data.GenreGrouping
import com.chakir.aggregatorhubplex.data.Movie
import com.chakir.aggregatorhubplex.data.MovieApiService
import com.chakir.aggregatorhubplex.data.local.AppDatabase
import com.chakir.aggregatorhubplex.data.local.MovieEntity
import com.chakir.aggregatorhubplex.ui.screens.SortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MediaRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val api: MovieApiService
) : MediaRepository {

    override fun getMediaPaged(
        search: String?,
        type: String?,
        genreLabel: String,
        sort: SortOption
    ): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = 50, enablePlaceholders = true, maxSize = 300),
            pagingSourceFactory = {
                val keywords = GenreGrouping.GROUPS[genreLabel] ?: emptyList()
                val sqlQuery = buildRawQuery(search, type, keywords, sort)
                database.movieDao().getMoviesPagedRaw(sqlQuery)
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toMovie() }
        }
    }

    override fun getFilteredCount(
        search: String?,
        type: String?,
        genreLabel: String
    ): Flow<Int> {
        val keywords = GenreGrouping.GROUPS[genreLabel] ?: emptyList()
        val sqlQuery = buildCountQuery(search, type, keywords)
        return database.movieDao().getFilteredCount(sqlQuery)
    }

    override fun getTopRated(type: String?, limit: Int): Flow<List<Movie>> {
        return database.movieDao().getTopRated(type, limit).map {
            it.map { entity -> entity.toMovie() }
        }
    }

    override fun getMovieDetail(movieId: String): Flow<Movie?> = flow {
        emit(database.movieDao().getMovieById(movieId)?.toMovie())
        try {
            val networkMovie = api.getMovieDetail(movieId)
            database.movieDao().upsertAll(listOf(networkMovie.toEntity()))
            emit(database.movieDao().getMovieById(movieId)?.toMovie())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

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
            val genreOrClauses = genreKeywords.joinToString(" OR ") { "m.genres LIKE ?" }
            whereClauses.add("($genreOrClauses)")
            genreKeywords.forEach { args.add("%$it%") }
        }

        if (whereClauses.isNotEmpty()) {
            sql += " WHERE " + whereClauses.joinToString(" AND ")
        }

        if (search.isNullOrBlank()) {
            sql += " ORDER BY " + when (sort) {
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
            id = this.id, title = this.title, type = this.type, posterUrl = this.posterPath,
            year = this.year, addedAt = this.addedAt, rating = this.rating, imdbRating = this.imdbRating,
            rottenRating = this.rottenRating, director = this.director, genres = this.genres,
            description = this.description, studio = this.studio, contentRating = this.contentRating,
            servers = this.servers, seasons = this.seasons, hasMultipleSources = this.hasMultipleSources
        )
    }
}
