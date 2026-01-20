package com.chakir.aggregatorhubplex

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.chakir.aggregatorhubplex.workers.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import okhttp3.OkHttpClient

@HiltAndroidApp
class PlexHubApplication : Application(), ImageLoaderFactory, Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        setupPeriodicSync()
    }

    private fun setupPeriodicSync() {
        val workManager = WorkManager.getInstance(this)

        val constraints =
            androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()

        val syncRequest =
            PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

        workManager.enqueueUniquePeriodicWork(
            "movie_sync_work",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient {
                // Créer un client OkHttp permissif pour les images
                val trustAllCerts =
                    arrayOf<TrustManager>(
                        object : X509TrustManager {
                            override fun checkClientTrusted(
                                chain: Array<out X509Certificate>?,
                                authType: String?
                            ) {
                            }

                            override fun checkServerTrusted(
                                chain: Array<out X509Certificate>?,
                                authType: String?
                            ) {
                            }

                            override fun getAcceptedIssuers(): Array<X509Certificate> {
                                return arrayOf()
                            }
                        }
                    )

                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())

                OkHttpClient.Builder()
                    .sslSocketFactory(
                        sslContext.socketFactory,
                        trustAllCerts[0] as X509TrustManager
                    )
                    .hostnameVerifier { _, _ -> true }
                    .build()
            }
            .components {
                add(VideoFrameDecoder.Factory()) // Support pour les thumbnails vidéo si besoin
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .respectCacheHeaders(false) // Ignorer headers cache du serveur
            .build()
    }
}
