package com.chakir.aggregatorhubplex.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.sqlite.db.SimpleSQLiteQuery
import com.chakir.aggregatorhubplex.data.GenreGrouping
import com.chakir.aggregatorhubplex.data.Movie
import com.chakir.aggregatorhubplex.data.local.AppDatabase
import com.chakir.aggregatorhubplex.ui.screens.SortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MediaRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : MediaRepository {

    override fun getTotalCount(): Flow<Int> {
        return database.movieDao().getCount()
    }

    override fun getMediaPaged(
        search: String?,
        type: String?,
        genreLabel: String,
        sort: SortOption
    ): Flow<PagingData<Movie>> {

        return Pager(
            config = PagingConfig(pageSize = 50, enablePlaceholders = true, maxSize = 300),
            pagingSourceFactory = {
                // 1. Récupération des mots-clés associés au groupe de genre
                val keywords = GenreGrouping.GROUPS[genreLabel] ?: emptyList()

                // 2. Construction de la requête SQL dynamique
                val sqlQuery = buildRawQuery(search, type, keywords, sort)

                // 3. Appel au DAO
                database.movieDao().getMoviesPagedRaw(sqlQuery)
            }
        ).flow.map { pagingData ->
            // Transformation Entity -> Domain Model
            pagingData.map { it.toMovie() }
        }
    }

    // --- LE BLOC MAGIQUE QUE TU VOULAIS GARDER ---
    private fun buildRawQuery(
        search: String?,
        type: String?,
        genreKeywords: List<String>,
        sort: SortOption
    ): SimpleSQLiteQuery {
        val args = ArrayList<Any>()
        val whereClauses = ArrayList<String>()

        // 1. Filtre Recherche
        if (!search.isNullOrBlank()) {
            whereClauses.add("(title LIKE ? OR description LIKE ?)")
            args.add("%$search%")
            args.add("%$search%")
        }

        // 2. Filtre Type (Film/Série)
        if (type != null) {
            whereClauses.add("type = ?")
            args.add(type)
        }

        // 3. Filtre Genre (Regroupement OR)
        if (genreKeywords.isNotEmpty()) {
            // Crée: (genres LIKE '%Anime%' OR genres LIKE '%Animation%' ...)
            val genreOrClauses = genreKeywords.joinToString(" OR ") { "genres LIKE ?" }
            whereClauses.add("($genreOrClauses)")
            genreKeywords.forEach { args.add("%$it%") }
        }

        // Assemblage
        var sql = "SELECT * FROM movies"
        if (whereClauses.isNotEmpty()) {
            sql += " WHERE " + whereClauses.joinToString(" AND ")
        }

        // Tri
        sql += " ORDER BY "
        sql += when (sort) {
            SortOption.ADDED_DESC -> "addedAt DESC"
            SortOption.TITLE -> "title ASC"
            SortOption.YEAR_DESC -> "year DESC"
            SortOption.YEAR_ASC -> "year ASC"
            SortOption.RATING_DESC -> "COALESCE(imdbRating, rating) DESC"
            SortOption.RATING_ASC -> "COALESCE(imdbRating, rating) ASC"
        }

        // Tri secondaire stable
        sql += ", id DESC"

        return SimpleSQLiteQuery(sql, args.toArray())
    }
}