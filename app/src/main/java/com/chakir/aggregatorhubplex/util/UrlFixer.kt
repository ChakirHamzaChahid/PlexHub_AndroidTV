package com.chakir.aggregatorhubplex.util

import com.chakir.aggregatorhubplex.BuildConfig
import java.net.URI

/** Utilitaire pour corriger les URLs relatives ou localhost provenant de l'API. */
object UrlFixer {
    /**
     * Corrige l'URL donnée pour qu'elle soit accessible depuis l'émulateur Android ou le
     * périphérique. Remplace localhost/127.0.0.1 par 10.0.2.2 ou l'hôte configuré. Ajoute l'URL
     * de base si le chemin est relatif.
     */
    fun fix(url: String?): String {
        if (url.isNullOrEmpty()) return ""
        val currentBase = BuildConfig.API_BASE_URL.trimEnd('/')

        if (url.startsWith("http")) {
            return try {
                val host = URI(currentBase).host ?: "10.0.2.2"
                url.replace("localhost", "10.0.2.2")
                    .replace("127.0.0.1", "10.0.2.2")
                    .replace("10.0.2.2", host)
            } catch (e: Exception) {
                url
            }
        }
        val relativePath = url.trimStart('/')
        return "$currentBase/$relativePath"
    }
}
