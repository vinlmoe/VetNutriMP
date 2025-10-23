package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Implémentation Windows pour PlatformHttpClient
 * Fonctionnalité complète sur Windows
 */
actual object PlatformHttpClient {
    actual suspend fun fetchXml(url: String): String = withContext(Dispatchers.IO) {
        try {
            val urlObj = URL(url)
            val connection = urlObj.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "GET"
                connectTimeout = 10000 // 10 secondes
                readTimeout = 10000 // 10 secondes
                setRequestProperty("User-Agent", "VetNutriMP-UpdateChecker")
            }
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                throw IOException("Erreur HTTP: ${connection.responseCode}")
            }
        } catch (e: Exception) {
            throw UpdateException("Impossible de récupérer le fichier de mise à jour", e)
        }
    }
}
