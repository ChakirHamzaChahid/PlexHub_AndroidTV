package com.chakir.aggregatorhubplex.data.network

import com.chakir.aggregatorhubplex.data.dto.MovieListItem
import com.chakir.aggregatorhubplex.data.dto.ProgressRequest
import com.chakir.aggregatorhubplex.data.dto.ScanResponse
import com.chakir.aggregatorhubplex.data.dto.ScrobbleRequest
import com.chakir.aggregatorhubplex.data.dto.ServerInfo
import com.chakir.aggregatorhubplex.domain.model.Movie
import com.chakir.aggregatorhubplex.domain.model.Season
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Interface Retrofit définissant les points de terminaison de l'API. */
interface MovieApiService {
    /** Récupère la liste paginée des films/séries. */
    @GET("/api/movies")
    suspend fun getMovies(
        @Query("page") page: Int?,
        @Query("size") size: Int?,
        @Query("type") type: String?,
        @Query("sort") sort: String?,
        @Query("order") order: String?,
        @Query("search") search: String?
    ): List<MovieListItem>

    /** Récupère les détails d'un film ou d'une série spécifique par son ID. */
    @GET("/api/movies/{id}")
    suspend fun getMovieDetail(@Path("id") id: String): Movie

    /**
     * Récupère les saisons d'une série spécifique. Note: Double slash dans le chemin pourrait
     * être intentionnel ou une erreur typo dans l'original.
     */
    @GET("//api/movies/{id}/seasons")
    suspend fun getShowSeasons(@Path("id") id: String): List<Season>

    /** Récupère les informations sur les serveurs disponibles. */
    @GET("/api/servers")
    suspend fun getServerInfo(): List<ServerInfo>

    /** Déclenche un rafraîchissement des données côté serveur. */
    @POST("/api/refresh")
    suspend fun triggerRefresh(): ScanResponse

    // --- NOUVEAUX ENDPOINTS ---

    /** Récupère les derniers médias ajoutés. */
        
    @GET("/api/recently-added")
    suspend fun getRecentlyAdded(@Query("limit") limit: Int? = 50): List<Movie>

    /** Récupère les Hubs de découverte (ex: "Top Rated", "Recently Released"). */
    @GET("/api/hubs")
    suspend fun getHubs(@Query("limit") limit: Int? = 10): Map<String, List<MovieListItem>>

    /** Récupère les médias "En cours de lecture" (On Deck). */
    @GET("/api/continue_watching")
    suspend fun getContinueWatching(): List<Movie>

    /** Récupère l'historique de visionnage. */
    @GET("/api/watch-history")
    suspend fun getWatchHistory(
        @Query("page") page: Int? = 1,
        @Query("size") size: Int? = 50
    ): List<Movie>

    /** Recherche avancée globale. */
    @GET("/api/search")
    suspend fun search(
        @Query("title") title: String?,
        @Query("year") year: Int? = null,
        @Query("unwatched") unwatched: Boolean? = null,
        @Query("limit") limit: Int? = 50
    ): List<MovieListItem>

    /** Marquer comme vu/non vu. */
    @POST("/api/actions/scrobble")
    suspend fun scrobble(@retrofit2.http.Body request: ScrobbleRequest)

    /** Mettre à jour la progression de lecture. */
    @POST("/api/actions/progress")
    suspend fun updateProgress(@retrofit2.http.Body request: ProgressRequest)

    /** Ajouter/Retirer des favoris (toggle). */
    @POST("/api/favorite/{id}")
    suspend fun toggleFavorite(@Path("id") id: String)

    /** Noter un média. */
    @POST("/api/rate/{id}/{rating}")
    suspend fun rateMedia(@Path("id") id: String, @Path("rating") rating: Float)

    /** Lancer une optimisation (transcodage). */
    @POST("/api/optimize/{id}")
    suspend fun optimizeMedia(
        @Path("id") id: String,
        @Query("target") target: String = "mobile"
    )
}
