package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

actual object PlatformHttpClient {
    actual suspend fun fetchXml(url: String): String {
        throw UpdateException("Vérification des mises à jour désactivée sur Android")
    }

    actual suspend fun fetchJson(url: String): String = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.apply {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("User-Agent", "VetNutriMP/1.0")
        }
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            throw Exception("Erreur HTTP: ${connection.responseCode}")
        }
    }
}
