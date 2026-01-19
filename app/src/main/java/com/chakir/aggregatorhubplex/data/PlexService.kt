package com.chakir.aggregatorhubplex.data

import com.chakir.aggregatorhubplex.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class PlexService(val api: MovieApiService) {

    companion object {
        @Volatile
        private var INSTANCE: PlexService? = null

        fun create(): PlexService {
            return INSTANCE ?: synchronized(this) {
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

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val myClient = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build()

            val contentType = "application/json".toMediaType()

            val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(myClient)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()

            val api = retrofit.create(MovieApiService::class.java)
            return PlexService(api)
        }
    }
}