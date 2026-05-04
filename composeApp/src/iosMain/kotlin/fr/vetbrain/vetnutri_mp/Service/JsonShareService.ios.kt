package fr.vetbrain.vetnutri_mp.Service

import io.ktor.client.*
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.*
import io.ktor.http.*

actual class JsonShareService {
    private val helper = JsonShareServiceHelper(
        httpClient = HttpClient(Darwin.create()) {
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 60_000
            }
            install(HttpRequestRetry) {
                maxRetries = 2
                retryOnServerErrors(maxRetries)
                exponentialDelay()
                retryIf { _, response -> response.status.value == 429 }
                retryOnExceptionIf { _, cause ->
                    cause is HttpRequestTimeoutException || (cause.message?.contains("timed out", ignoreCase = true) == true)
                }
            }
        }
    )
    
    actual suspend fun uploadJson(
        jsonContent: String,
        options: ShareOptions
    ): Result<ShareLink> = helper.uploadJson(jsonContent, options)
    
    actual suspend fun downloadJson(
        binId: String,
        keyBase64: String?,
        ivBase64: String?
    ): Result<String> = helper.downloadJson(binId, keyBase64, ivBase64)
    
    actual fun extractBinIdFromUrl(url: String): String? = helper.extractBinIdFromUrl(url)

    actual fun parseQrPayload(text: String): JsonBinQrPayload? = helper.parseQrPayload(text)
}

actual fun createJsonShareService(): JsonShareService = JsonShareService()
