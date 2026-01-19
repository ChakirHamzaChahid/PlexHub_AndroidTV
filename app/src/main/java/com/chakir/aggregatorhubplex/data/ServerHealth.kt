package com.chakir.aggregatorhubplex.data

/** Statuts possibles pour un serveur. */
enum class ServerStatus {
    ONLINE,
    SLOW,
    OFFLINE,
    CHECKING
}

/**
 * Modèle représentant l'état de santé d'un serveur. Utilisé pour afficher l'indicateur de connexion
 * dans l'interface.
 */
data class ServerHealth(
        val name: String,
        val url: String,
        val latencyMs: Long = 0,
        val status: ServerStatus = ServerStatus.CHECKING,
        val isPreferred: Boolean = false
)
