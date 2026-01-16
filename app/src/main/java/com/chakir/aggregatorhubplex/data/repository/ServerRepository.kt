package com.chakir.aggregatorhubplex.data.repository

import android.util.Log
import com.chakir.aggregatorhubplex.data.NetworkModule
import com.chakir.aggregatorhubplex.data.ServerHealth
import com.chakir.aggregatorhubplex.data.ServerStatus
import com.chakir.aggregatorhubplex.data.preferences.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.system.measureTimeMillis

interface ServerRepository {
    fun getServerHealth(): Flow<ServerHealth>
}

class ServerRepositoryImpl @Inject constructor(
    private val prefsManager: PreferencesManager
) : ServerRepository {

    override fun getServerHealth(): Flow<ServerHealth> = flow {
        // On récupère l'URL actuelle
        val currentUrl = NetworkModule.currentBaseUrl

        // État initial : Vérification...
        emit(ServerHealth("Serveur Principal", currentUrl, 0, ServerStatus.CHECKING))

        try {
            var latency = 0L
            val time = measureTimeMillis {
                withContext(Dispatchers.IO) {
                    // CORRECTION : On passe 'null' explicitement pour les paramètres optionnels
                    // afin de respecter la signature : getMovies(page, size, type, sort, order, search)
                    NetworkModule.api.getMovies(
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

            // On définit les seuils de qualité
            val status = when {
                latency < 300 -> ServerStatus.ONLINE  // Très bon
                latency < 1000 -> ServerStatus.SLOW   // Acceptable
                else -> ServerStatus.SLOW             // Lent
            }

            Log.d("ServerHealth", "Ping success: ${latency}ms")
            emit(ServerHealth("Serveur Principal", currentUrl, latency, status, true))

        } catch (e: Exception) {
            Log.e("ServerHealth", "Ping failed: ${e.message}")
            // Si le ping échoue, c'est que le serveur est injoignable ou renvoie une erreur
            emit(ServerHealth("Serveur Principal", currentUrl, -1, ServerStatus.OFFLINE, true))
        }
    }
}