package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

actual object PlatformHttpClient {
    actual suspend fun fetchJson(url: String): String = withContext(Dispatchers.IO) {
        httpGet(url, "VetNutriMP/1.0")
    }

    actual suspend fun fetchXml(url: String): String = withContext(Dispatchers.IO) {
        httpGet(url, "VetNutriMP-UpdateChecker")
    }

    private fun httpGet(url: String, userAgent: String): String {
        var currentUrl = url
        var redirectCount = 0
        val maxRedirects = 5

        while (redirectCount < maxRedirects) {
            val connection = URL(currentUrl).openConnection() as HttpURLConnection
            connection.apply {
                instanceFollowRedirects = true
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("User-Agent", userAgent)
            }

            val responseCode = connection.responseCode

            if (responseCode in 300..399) {
                val location = connection.getHeaderField("Location")
                        ?: throw IOException("Redirection $responseCode sans en-tête Location")
                redirectCount++
                currentUrl = location
                connection.disconnect()
                continue
            }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                return connection.inputStream.bufferedReader().use { it.readText() }
            }

            throw IOException("Erreur HTTP: $responseCode - ${connection.responseMessage}")
        }

        throw IOException("Trop de redirections (max: $maxRedirects)")
    }
}
