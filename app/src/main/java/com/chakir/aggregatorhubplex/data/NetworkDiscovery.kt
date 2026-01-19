package com.chakir.aggregatorhubplex.data

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Classe utilitaire pour la découverte de serveurs sur le réseau local (mDNS/NSD). Permet de
 * trouver automatiquement le serveur backend ou de formater une IP saisie manuellement.
 */
class NetworkDiscovery(context: Context) {

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val SERVICE_TYPE = "_http._tcp."
    private val SERVICE_NAME_PREFIX = "plexhub"

    /**
     * Lance la découverte automatique des services mDNS. Retourne un flux d'URLs potentielles (ex:
     * "http://192.168.1.15:8186/").
     */
    fun discoverServer(): Flow<String> = callbackFlow {
        val discoveryListener =
                object : NsdManager.DiscoveryListener {
                    override fun onDiscoveryStarted(regType: String) {
                        Log.d("NSD", "Découverte démarrée")
                    }

                    override fun onServiceFound(service: NsdServiceInfo) {
                        if (service.serviceName.contains(SERVICE_NAME_PREFIX, ignoreCase = true)) {
                            nsdManager.resolveService(
                                    service,
                                    object : NsdManager.ResolveListener {
                                        override fun onResolveFailed(
                                                serviceInfo: NsdServiceInfo,
                                                errorCode: Int
                                        ) {
                                            Log.e("NSD", "Échec résolution: $errorCode")
                                        }

                                        override fun onServiceResolved(
                                                serviceInfo: NsdServiceInfo
                                        ) {
                                            val host = serviceInfo.host.hostAddress
                                            val port = serviceInfo.port
                                            val url = "http://$host:$port/"
                                            trySend(url)
                                        }
                                    }
                            )
                        }
                    }

                    override fun onServiceLost(service: NsdServiceInfo) = Unit
                    override fun onDiscoveryStopped(serviceType: String) = Unit
                    override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                        nsdManager.stopServiceDiscovery(this)
                    }

                    override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                        nsdManager.stopServiceDiscovery(this)
                    }
                }

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        awaitClose { nsdManager.stopServiceDiscovery(discoveryListener) }
    }

    /**
     * Valide et formate une adresse serveur saisie manuellement par l'utilisateur. Gère les formats
     * IP simples, avec port, avec http/https, etc.
     * @param input L'entrée utilisateur (ex: "192.168.1.50").
     * @return L'URL complète et valide, ou null si l'entrée est vide.
     */
    fun formatManualAddress(input: String): String? {
        var address = input.trim()
        if (address.isEmpty()) return null

        // 1. Ajouter le protocole si manquant
        if (!address.startsWith("http://") && !address.startsWith("https://")) {
            address = "http://$address"
        }
        // 2. Nettoyage des slashs multiples en fin de chaîne puis ajout d'un seul slash
        // Retrofit CRASH si l'URL de base ne finit pas par /
        address = address.removeSuffix("/") + "/"
        // 2. Ajouter le port par défaut (8186) si aucun port n'est détecté
        // On vérifie s'il y a un ":" après l'adresse (ex: http://1.1.1.1:8186)
        val portRegex = Regex(":\\d+$")
        if (!address.contains(portRegex) && !address.endsWith("/")) {
            address = "$address:8186"
        } else if (address.endsWith("/")) {
            // Si finit par /, on vérifie le port avant le slash
            val addressWithoutSlash = address.removeSuffix("/")
            if (!addressWithoutSlash.contains(portRegex)) {
                address = "$addressWithoutSlash:8186"
            }
        }

        // 3. Garantir le slash final pour Retrofit
        if (!address.endsWith("/")) {
            address = "$address/"
        }

        return address
    }
}
