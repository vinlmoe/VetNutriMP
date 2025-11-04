package fr.vetbrain.vetnutri_mp.Service

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*

actual class JsonShareService {
    private val helper = JsonShareServiceHelper(
        httpClient = HttpClient(OkHttp.create())
    )
    
    actual suspend fun uploadJson(
        jsonContent: String,
        options: ShareOptions
    ): Result<ShareLink> = helper.uploadJson(jsonContent, options)
    
    actual suspend fun downloadJson(binId: String): Result<String> = helper.downloadJson(binId)
    
    actual fun extractBinIdFromUrl(url: String): String? = helper.extractBinIdFromUrl(url)
}

actual fun createJsonShareService(): JsonShareService = JsonShareService()
