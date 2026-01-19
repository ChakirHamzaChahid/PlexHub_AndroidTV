package com.chakir.aggregatorhubplex.data

import com.chakir.aggregatorhubplex.BuildConfig
import com.chakir.aggregatorhubplex.data.network.MovieApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

/**
 * Service Factory pour créer et configurer le client Retrofit. Gère la configuration JSON, les
 * timeouts, le logging HTTP et l'URL de base.
 */
class PlexService(val api: MovieApiService) {

    companion object {
        @Volatile private var INSTANCE: PlexService? = null

        /** Crée ou retourne l'instance unique du service. */
        fun create(): PlexService {
            return INSTANCE
                    ?: synchronized(this) {
                        val instance = createPlexService()
                        INSTANCE = instance
                        instance
                    }
        }

        private fun createPlexService(): PlexService {
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                isLenient = true
                explicitNulls = false
            }

            val logging =
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

            val myClient =
                    OkHttpClient.Builder()
                            .addInterceptor(logging)
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(120, TimeUnit.SECONDS)
                            .build()

            val contentType = "application/json".toMediaType()

            val retrofit =
                    Retrofit.Builder()
                            .baseUrl(BuildConfig.API_BASE_URL)
                            .client(myClient)
                            .addConverterFactory(json.asConverterFactory(contentType))
                            .build()

            val api = retrofit.create(MovieApiService::class.java)
            return PlexService(api)
        }
    }
}
