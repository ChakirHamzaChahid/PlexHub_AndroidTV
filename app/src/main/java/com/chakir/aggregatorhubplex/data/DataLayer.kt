package com.chakir.aggregatorhubplex.data

import com.chakir.aggregatorhubplex.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import kotlinx.serialization.json.Json
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.Query
import java.net.URI
import java.util.concurrent.TimeUnit

// --- MODELS ---

@Serializable
data class Server(
    @SerialName("server_name") val name: String = "Inconnu",
    @SerialName("stream_url") val url: String = "",
    // CORRECTION : Renommé en public pour pouvoir être instancié si besoin
    @SerialName("m3u_url") val rawM3uUrl: String? = null,
    @SerialName("resolution") val resolution: String? = "SD",
    @SerialName("plex_deeplink") val plexDeepLink: String? = null,
    @SerialName("plex_web_url") val plexWebUrl: String? = null
) {
    val m3uUrl: String? get() = rawM3uUrl?.let { fixUrl(it) }
    val streamUrl: String get() = fixUrl(url)

    private fun fixUrl(url: String): String {
        return UrlFixer.fix(url)
    }
}

@Serializable
data class Episode(
    val id: String = "",
    val title: String = "Épisode sans titre",
    @SerialName("index") val episodeNumber: Int? = 0,
    val description: String? = null,
    @SerialName("thumb_url") val thumbUrl: String? = null,
    @SerialName("summary") val overview: String? = null,
    @SerialName("sources") val servers: List<Server>? = emptyList(),
    // CORRECTION : Renommé en public (rawStillUrl)
    @SerialName("still_url") val rawStillUrl: String? = null
) {
    val stillUrl: String? get() = rawStillUrl?.let { UrlFixer.fix(it) }
}

@Serializable
data class Season(
    @SerialName("index") val seasonNumber: Int? = 0,
    val title: String = "",
    val episodes: List<Episode> = emptyList()
)

@Serializable
data class Movie(
    val id: String,
    val title: String = "Sans titre",
    val type: String = "movie",
    @SerialName("summary") val description: String? = "Aucune description disponible",
    val rating: Float? = null,
    @SerialName("genres") val genres: List<String>? = emptyList(),
    val director: String? = null,
    val studio: String? = null,
    @SerialName("content_rating") val contentRating: String? = null,
    @SerialName("imdb_rating") val imdbRating: Float? = null,
    @SerialName("rotten_rating") val rottenRating: Int? = null,
    @SerialName("added_at") val addedAt: String? = null,

    // CORRECTION IMPORTANTE : Champs publics pour permettre la création manuelle (Favoris)
    @SerialName("poster_url") val posterPath: String? = null,
    @SerialName("backdrop_url") val backdropPath: String? = null,

    val year: Int? = null,
    @SerialName("sources") val servers: List<Server>? = emptyList(),
    val seasons: List<Season>? = emptyList(),
    val hasMultipleSources: Boolean = false
) {
    // Helpers calculés
    val posterUrl: String get() = UrlFixer.fix(posterPath)
    val backdropUrl: String get() = UrlFixer.fix(backdropPath ?: posterPath)
    val isSeries: Boolean get() = type == "show"
}

// CORRECTION : Ajout de @Serializable sinon le scan serveur crashera
@Serializable
data class ServerInfo(
    val name: String,
    val url: String,
    val owned: Boolean,
    val latency: Float = 0.0f,
    val status: String = "Online"
)

@Serializable
data class ScanResponse(val message: String, val status: String)

// --- UTILITAIRE URL (Centralisé) ---
object UrlFixer {
    fun fix(url: String?): String {
        if (url.isNullOrEmpty()) return ""
        val currentBase = NetworkModule.currentBaseUrl.trimEnd('/')

        if (url.startsWith("http")) {
            // Remplace localhost/127.0.0.1 par l'IP réelle configurée dans l'app
            return try {
                val host = URI(currentBase).host ?: "10.0.2.2"
                url.replace("localhost", "10.0.2.2")
                    .replace("127.0.0.1", "10.0.2.2")
                    .replace("10.0.2.2", host) // Adapte si on est sur une vraie TV
            } catch (e: Exception) {
                url // Fallback si l'URI est invalide
            }
        }
        val relativePath = url.trimStart('/')
        return "$currentBase/$relativePath"
    }
}

// --- RETROFIT INTERFACE ---

interface MovieApiService {
    @GET("/api/movies")
    suspend fun getMovies(
        @Query("page") page: Int?,
        @Query("size") size: Int?,
        @Query("type") type: String?,
        @Query("sort") sort: String?,
        @Query("order") order: String?,
        @Query("search") search: String?
    ): List<Movie>

    @GET("/api/movies/{id}")
    suspend fun getMovieDetail(@Path("id") id: String): Movie

    @GET("/api/shows/{id}/seasons")
    suspend fun getShowSeasons(@Path("id") id: String): List<Season>

    @GET("/api/servers")
    suspend fun getServerInfo(): List<ServerInfo>

    @POST("/api/refresh")
    suspend fun triggerRefresh(): ScanResponse
}

// --- NETWORK MODULE ---

object NetworkModule {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        explicitNulls = false
    }

    var currentBaseUrl: String = BuildConfig.API_BASE_URL
        private set

    private var retrofit: Retrofit = createRetrofit(currentBaseUrl)
    var api: MovieApiService = retrofit.create(MovieApiService::class.java)
        private set

    fun updateBaseUrl(newUrl: String) {
        val formattedUrl = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        if (formattedUrl == currentBaseUrl) return

        currentBaseUrl = formattedUrl
        retrofit = createRetrofit(formattedUrl)
        api = retrofit.create(MovieApiService::class.java)
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val myClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()

        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(myClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
}