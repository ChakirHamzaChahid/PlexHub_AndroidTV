package com.chakir.aggregatorhubplex.data.repository

import android.util.Log
import com.chakir.aggregatorhubplex.BuildConfig
import com.chakir.aggregatorhubplex.data.network.MovieApiService
import com.chakir.aggregatorhubplex.data.ServerHealth
import com.chakir.aggregatorhubplex.data.ServerStatus
import com.chakir.aggregatorhubplex.di.CoroutineDispatchers
import javax.inject.Inject
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/** Repository responsable de surveiller l'état et la santé du serveur backend. */
interface ServerRepository {
    /**
     * Vérifie la santé du serveur (Ping, Latence). Retourne un flux émettant l'état du serveur
     * (Checking, Online, Offline, Slow).
     */
    fun getServerHealth(): Flow<ServerHealth>
}

/**
 * Implémentation du [ServerRepository]. Effectue un appel réseau simple pour mesurer la latence.
 */
class ServerRepositoryImpl
@Inject
constructor(
        private val apiService: MovieApiService,
        private val dispatchers: CoroutineDispatchers
) : ServerRepository {

    override fun getServerHealth(): Flow<ServerHealth> = flow {
        val currentUrl = BuildConfig.API_BASE_URL

        // État initial de vérification
        emit(ServerHealth("Serveur Principal", currentUrl, 0, ServerStatus.CHECKING))

        try {
            var latency = 0L
            val time = measureTimeMillis {
                withContext(dispatchers.io) {
                    // Appel léger pour pinger le serveur
                    apiService.getMovies(
                            page = 1,
                            size = 1,
                            type = null,
                            sort = null,
                            order = null,
                            search = null
                    )
                }
            }
            latency = time

            // Détermination du statut en fonction de la latence
            val status =
                    when {
                        latency < 300 -> ServerStatus.ONLINE
                        latency < 1000 -> ServerStatus.SLOW
                        else -> ServerStatus.SLOW
                    }

            Log.d("ServerHealth", "Ping success: ${latency}ms")
            emit(ServerHealth("Serveur Principal", currentUrl, latency, status, true))
        } catch (e: Exception) {
            Log.e("ServerHealth", "Ping failed: ${e.message}")
            emit(ServerHealth("Serveur Principal", currentUrl, -1, ServerStatus.OFFLINE, true))
        }
    }
}
