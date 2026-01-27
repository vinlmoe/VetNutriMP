package fr.vetbrain.vetnutri_mp.Service

import io.ktor.client.*
import io.ktor.client.engine.darwin.Darwin

actual class JsonShareService {
    private val helper = JsonShareServiceHelper(
        httpClient = HttpClient(Darwin.create())
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
