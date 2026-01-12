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
import java.util.concurrent.TimeUnit

// --- MODELS ---

@Serializable
data class Server(
    // CORRECTION : Mapping JSON exact avec le backend Python
    @SerialName("server_name") val name: String = "Inconnu",
    @SerialName("stream_url") val url: String = "",
    @SerialName("m3u_url") private val _m3uUrl: String? = null,
    @SerialName("resolution") val resolution: String? = "SD",
    @SerialName("plex_deeplink") val plexDeepLink: String? = null,
    @SerialName("plex_web_url") val plexWebUrl: String? = null
) {
    val m3uUrl: String? get() = _m3uUrl?.let { fixUrl(it) }
    val streamUrl: String get() = fixUrl(url)

    private fun fixUrl(url: String): String {
        if (url.startsWith("http")) {
            return url.replace("localhost", "10.0.2.2").replace("127.0.0.1", "10.0.2.2")
        }
        val baseUrl = NetworkModule.currentBaseUrl.trimEnd('/')
        val relativePath = url.trimStart('/')
        return "$baseUrl/$relativePath"
    }
}

@Serializable
data class Episode(
    val id: String = "",
    val title: String = "Épisode sans titre",
    @SerialName("index") val episodeNumber: Int? = 0, // Backend envoie "index", pas "episode_number"
    val description: String? = null,
    @SerialName("thumb_url") val thumbUrl: String? = null,
    @SerialName("summary") val overview: String? = null,

    // CORRECTION : Le backend envoie "sources"
    @SerialName("sources") val servers: List<Server>? = emptyList(),

    @SerialName("still_url") private val _stillUrl: String? = null
) {
    val stillUrl: String?
        get() = _stillUrl?.let {
            if (it.startsWith("http")) it.replace("localhost", "10.0.2.2")
            else "${NetworkModule.currentBaseUrl.trimEnd('/')}/${it.trimStart('/')}"
        }
}

@Serializable
data class Season(
    // Backend n'envoie pas d'ID sur la saison, on peut l'ignorer ou le générer
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
    val director: String? = null, // Déjà présent dans votre code actuel, on s'assure qu'il est bien mappé
    val studio: String? = null,
    @SerialName("content_rating") val contentRating: String? = null,
    @SerialName("imdb_rating") val imdbRating: Float? = null,     // Vérification
    @SerialName("rotten_rating") val rottenRating: Int? = null,   // Vérification
    @SerialName("added_at") val addedAt: String? = null,
    @SerialName("poster_url") private val _posterUrl: String? = null,
    @SerialName("backdrop_url") private val _backdropUrl: String? = null,
    val year: Int? = null,
    @SerialName("sources") val servers: List<Server>? = emptyList(),
    val seasons: List<Season>? = emptyList(),
    val hasMultipleSources: Boolean = false
) {
    val posterUrl: String get() = fixUrl(_posterUrl)
    val backdropUrl: String get() = fixUrl(_backdropUrl ?: _posterUrl)
    val isSeries: Boolean get() = type == "show"


    private fun fixUrl(url: String?): String {
        if (url.isNullOrEmpty()) return ""
        if (url.startsWith("http")) {
            return url.replace("localhost", "10.0.2.2").replace("127.0.0.1", "10.0.2.2")
        }
        val baseUrl = NetworkModule.currentBaseUrl.trimEnd('/')
        val relativePath = url.trimStart('/')
        return "$baseUrl/$relativePath"
    }
}

data class ServerInfo(
    val name: String,
    val url: String,
    val owned: Boolean,
    val latency: Float = 0.0f,
    val status: String = "Online"
)

@Serializable
data class ScanResponse(val message: String, val status: String)

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