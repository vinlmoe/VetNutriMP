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
            var currentUrl = url
            var redirectCount = 0
            val maxRedirects = 5
            
            while (redirectCount < maxRedirects) {
                val urlObj = URL(currentUrl)
                val connection = urlObj.openConnection() as HttpURLConnection
                
                connection.apply {
                    instanceFollowRedirects = true // Suivre automatiquement les redirections
                    requestMethod = "GET"
                    connectTimeout = 10000 // 10 secondes
                    readTimeout = 10000 // 10 secondes
                    setRequestProperty("User-Agent", "VetNutriMP-UpdateChecker")
                }
                
                val responseCode = connection.responseCode
                
                // Codes de redirection (301, 302, 303, 307, 308)
                if (responseCode in 300..399) {
                    val location = connection.getHeaderField("Location")
                    if (location != null) {
                        redirectCount++
                        println("[PlatformHttpClient] Redirection $responseCode vers: $location")
                        currentUrl = location
                        connection.disconnect()
                        continue
                    } else {
                        throw IOException("Redirection $responseCode sans en-tête Location")
                    }
                }
                
                // Code de succès (200)
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return@withContext connection.inputStream.bufferedReader().use { it.readText() }
                }
                
                // Autres codes d'erreur
                throw IOException("Erreur HTTP: $responseCode - ${connection.responseMessage}")
            }
            
            throw IOException("Trop de redirections (max: $maxRedirects)")
            
        } catch (e: Exception) {
            println("[PlatformHttpClient] Erreur: ${e::class.simpleName} - ${e.message}")
            e.printStackTrace()
            throw UpdateException("Impossible de récupérer le fichier de mise à jour", e)
        }
    }
}
