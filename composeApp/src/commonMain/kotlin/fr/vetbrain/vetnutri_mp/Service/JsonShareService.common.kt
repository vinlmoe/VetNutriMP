package fr.vetbrain.vetnutri_mp.Service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.datetime.Clock
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.CryptoUtils
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

/**
 * Helper class pour l'implémentation commune de JsonShareService
 * Ne peut pas hériter de expect class, donc on utilise la composition
 */
internal class JsonShareServiceHelper(private val httpClient: HttpClient) {
    
    // Clés API jsonbin.io (optionnelles)
    // Pour utiliser une clé API personnalisée, obtenez-la sur https://jsonbin.io/api-keys
    // Utilisez X-Access-Key (pas X-Master-Key) pour les access keys
    // - createUpdateApiKey : Clé d'accès en écriture (read/write)
    // - readApiKey : Clé d'accès en lecture seule (read-only)
    private val createUpdateApiKey: String? = "\$2a\$10\$MT3DjYFhsa1dDkM4CD.FVu/hgGmlbnPlKnJFo8BeoKlLjn01DUD7e"
    private val readApiKey: String? = "\$2a\$10\$/HY9ayqrm63ps0vx5apI1.KG5tpNGdhsC3Hyx03SrwEpgnlrwD0Yq"
    
    private val baseUrl = "https://api.jsonbin.io/v3"

    private fun log(@Suppress("UNUSED_PARAMETER") message: String) = Unit

    @Serializable
    private data class EncryptedPayload(
        val cipherText: String
    )
    
    suspend fun uploadJson(
        jsonContent: String,
        options: ShareOptions
    ): Result<ShareLink> = withContext(AppDispatchers.IO) {
        try {
            val isUpdate = options.binId != null
            val json = Json { ignoreUnknownKeys = true; isLenient = true }

            val encryptionResult = if (options.encryptJson) {
                CryptoUtils.encryptJson(jsonContent)
            } else {
                null
            }
            val bodyJsonContent = if (options.encryptJson && encryptionResult != null) {
                json.encodeToString(
                    EncryptedPayload.serializer(),
                    EncryptedPayload(cipherText = encryptionResult.cipherTextBase64)
                )
            } else {
                jsonContent
            }
            log(
                "Upload start (isUpdate=$isUpdate, jsonSize=${jsonContent.length}, " +
                    "encrypt=${options.encryptJson}, bodySize=${bodyJsonContent.length})"
            )
            
            // Construire l'URL
            // Si binId est fourni, on fait un PUT pour mettre à jour, sinon POST pour créer
            val url = if (isUpdate) {
                "$baseUrl/b/${options.binId}"
            } else {
                "$baseUrl/b"
            }
            log("Upload URL: $url")
            
            // Faire la requête POST ou PUT
            val response = if (isUpdate) {
                httpClient.put(url) {
                    headers {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        // Utiliser X-Access-Key pour la clé d'écriture (pas X-Master-Key)
                        createUpdateApiKey?.let { key ->
                            append("X-Access-Key", key)
                        }
                        // Pour l'expiration lors de la mise à jour, nécessite une clé API
                        if (options.expiresInHours != null && createUpdateApiKey != null) {
                            val expirationSeconds = options.expiresInHours * 3600
                            append("X-Bin-Expiration", expirationSeconds.toString())
                        }
                    }
                    setBody(bodyJsonContent)
                }
            } else {
                httpClient.post(url) {
                    headers {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        // Utiliser X-Access-Key pour la clé d'écriture (pas X-Master-Key)
                        createUpdateApiKey?.let { key ->
                            append("X-Access-Key", key)
                        }
                        // Nom du bin (peut être l'UUID de l'animal pour identification)
                        // Note: jsonbin.io ne permet pas d'ID personnalisé, mais on peut nommer le bin
                        options.binName?.let { name ->
                            append("X-Bin-Name", name)
                        }
                        // Pour les bins privés, nécessite une clé API
                        if (options.isPrivate && createUpdateApiKey != null) {
                            append("X-Bin-Private", "true")
                        }
                        // Pour l'expiration, nécessite une clé API
                        if (options.expiresInHours != null && createUpdateApiKey != null) {
                            val expirationSeconds = options.expiresInHours * 3600
                            append("X-Bin-Expiration", expirationSeconds.toString())
                        }
                    }
                    setBody(bodyJsonContent)
                }
            }
            
            if (response.status.isSuccess()) {
                log("Upload success (${response.status})")
                // Parser la réponse JSON manuellement
                val responseText = response.body<String>()
                log("Upload response size=${responseText.length}")
                
                // Si c'est une mise à jour, utiliser le binId fourni dans les options
                // Sinon, extraire l'ID depuis la réponse (création)
                val binId = if (isUpdate) {
                    options.binId ?: throw Exception("binId manquant pour la mise à jour")
                } else {
                    // Essayer d'extraire l'ID depuis les headers d'abord (méthode la plus fiable)
                    response.headers["X-Bin-Id"] 
                        ?: response.headers["x-bin-id"]
                        ?: run {
                            // Si pas dans les headers, essayer le header Location
                            val locationHeader = response.headers["location"] ?: response.headers["Location"]
                            if (locationHeader != null) {
                                val locationMatch = Regex("/b/([a-zA-Z0-9]+)").find(locationHeader)
                                locationMatch?.groupValues?.get(1)
                            } else {
                                null
                            }
                        }
                        ?: run {
                            // Dernière tentative: parser depuis le JSON
                            try {
                                val createResponse = json.decodeFromString<JsonBinCreateResponse>(responseText)
                                createResponse.id
                            } catch (e: Exception) {
                                // Essayer de parser le format alternatif avec "metadata"
                                try {
                                    val jsonObj = kotlinx.serialization.json.Json.parseToJsonElement(responseText).jsonObject
                                    val metadata = jsonObj["metadata"]?.jsonObject
                                    metadata?.get("id")?.jsonPrimitive?.content
                                } catch (e2: Exception) {
                                    throw Exception("Format de réponse inattendu: $responseText")
                                }
                            }
                        }
                }
                
                if (binId == null) {
                    throw Exception("Impossible d'extraire l'ID du bin depuis la réponse")
                }
                
                val shareUrl = "https://jsonbin.io/$binId"
                val qrPayload = if (options.encryptJson && encryptionResult != null) {
                    JsonBinQrPayload(
                        binId = binId,
                        key = encryptionResult.keyBase64,
                        iv = encryptionResult.ivBase64
                    )
                } else {
                    JsonBinQrPayload(binId = binId)
                }
                val qrCodeData = json.encodeToString(JsonBinQrPayload.serializer(), qrPayload)
                log("Upload done (binId=$binId, qrJsonSize=${qrCodeData.length})")
                
                // Calculer la date d'expiration si spécifiée
                val expiresAt = options.expiresInHours?.let { hours ->
                    Clock.System.now().toEpochMilliseconds() + (hours * 3600 * 1000L)
                }
                
                Result.success(ShareLink(
                    url = shareUrl,
                    binId = binId,
                    expiresAt = expiresAt,
                    qrCodeData = qrCodeData
                ))
            } else {
                val responseText = response.body<String>()
                log("Upload failed (${response.status}) responseSize=${responseText.length}")
                
                val errorResponse = try {
                    val json = Json { ignoreUnknownKeys = true; isLenient = true }
                    json.decodeFromString<JsonBinErrorResponse>(responseText)
                } catch (e: Exception) {
                    null
                }
                
                val errorMessage = "Erreur lors de l'upload: ${response.status} - ${errorResponse?.message ?: response.status.description}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            log("Upload exception: ${e.message}")
            Result.failure(Exception("Erreur lors de l'upload vers jsonbin.io: ${e.message}", e))
        }
    }
    
    suspend fun downloadJson(
        binId: String,
        keyBase64: String?,
        ivBase64: String?
    ): Result<String> = withContext(AppDispatchers.IO) {
        try {
            val url = "$baseUrl/b/$binId"
            log("Download start (binId=$binId, hasKey=${keyBase64 != null && ivBase64 != null})")
            log("Download URL: $url")
            
            val response = httpClient.get(url) {
                headers {
                    // Utiliser X-Access-Key pour la clé de lecture (pas X-Master-Key)
                    readApiKey?.let { key ->
                        append("X-Access-Key", key)
                    }
                }
            }
            
            if (response.status.isSuccess()) {
                log("Download success (${response.status})")
                // Parser la réponse JSON manuellement
                val responseText = response.body<String>()
                log("Download response size=${responseText.length}")
                val json = Json { ignoreUnknownKeys = true; isLenient = true }
                
                // jsonbin.io peut retourner record comme string ou comme objet JSON
                // On doit parser manuellement pour gérer les deux cas
                val content = try {
                    // Essayer d'abord de parser avec le modèle standard (record comme string)
                    val readResponse = json.decodeFromString<JsonBinReadResponse>(responseText)
                    readResponse.record
                } catch (e: Exception) {
                    // Si ça échoue, c'est que record est un objet JSON, pas une string
                    try {
                        val jsonObj = kotlinx.serialization.json.Json.parseToJsonElement(responseText).jsonObject
                        val recordElement = jsonObj["record"]
                        if (recordElement != null) {
                            // Convertir l'objet JSON en string
                            json.encodeToString(JsonElement.serializer(), recordElement)
                        } else {
                            null
                        }
                    } catch (e2: Exception) {
                        null
                    }
                }
                
                if (content != null) {
                    if (keyBase64 != null && ivBase64 != null) {
                        try {
                            val cipherTextBase64 = extractCipherText(content)
                            log("Decrypting content (cipherBase64Size=${cipherTextBase64.length})")
                            val decrypted = CryptoUtils.decryptJson(cipherTextBase64, keyBase64, ivBase64)
                            log("Decrypt success (plainSize=${decrypted.length})")
                            Result.success(decrypted)
                        } catch (e: Exception) {
                            log("Decrypt error: ${e.message}")
                            Result.failure(Exception("Erreur lors du déchiffrement: ${e.message}", e))
                        }
                    } else {
                        Result.success(content)
                    }
                } else {
                    Result.failure(Exception("Le bin est vide ou inaccessible"))
                }
            } else {
                val responseText = response.body<String>()
                log("Download failed (${response.status}) responseSize=${responseText.length}")
                
                val errorResponse = try {
                    val json = Json { ignoreUnknownKeys = true; isLenient = true }
                    json.decodeFromString<JsonBinErrorResponse>(responseText)
                } catch (e: Exception) {
                    null
                }
                
                val errorMessage = "Erreur lors du téléchargement: ${response.status} - ${errorResponse?.message ?: response.status.description}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            log("Download exception: ${e.message}")
            Result.failure(Exception("Erreur lors du téléchargement depuis jsonbin.io: ${e.message}", e))
        }
    }
    
    fun extractBinIdFromUrl(url: String): String? {
        // Formats d'URL jsonbin.io:
        // https://jsonbin.io/v3/b/1234567890
        // https://jsonbin.io/1234567890
        // jsonbin.io/1234567890
        // https://jsonbin.io/690a000643b1c97be9982db5
        
        val patterns = listOf(
            Regex("jsonbin\\.io/v3/b/([a-zA-Z0-9]+)", RegexOption.IGNORE_CASE),
            Regex("jsonbin\\.io/([a-zA-Z0-9]+)", RegexOption.IGNORE_CASE),
            Regex("https?://jsonbin\\.io/v3/b/([a-zA-Z0-9]+)", RegexOption.IGNORE_CASE),
            Regex("https?://jsonbin\\.io/([a-zA-Z0-9]+)", RegexOption.IGNORE_CASE),
            Regex("([a-zA-Z0-9]{10,})") // ID seul (au moins 10 caractères alphanumériques)
        )
        
        for (pattern in patterns) {
            val match = pattern.find(url.trim())
            if (match != null) {
                val extractedId = match.groupValues[1]
                // Vérifier que l'ID extrait a au moins 10 caractères (format jsonbin.io)
                if (extractedId.length >= 10) {
                    return extractedId
                }
            }
        }
        
        return null
    }

    fun parseQrPayload(text: String): JsonBinQrPayload? {
        val trimmed = text.trim()
        if (!trimmed.startsWith("{")) {
            return null
        }
        return try {
            val json = Json { ignoreUnknownKeys = true; isLenient = true }
            json.decodeFromString(JsonBinQrPayload.serializer(), trimmed)
        } catch (e: Exception) {
            null
        }
    }

    private fun extractCipherText(content: String): String {
        val trimmed = content.trim()
        if (!trimmed.startsWith("{")) {
            return trimmed
        }
        return try {
            val json = Json { ignoreUnknownKeys = true; isLenient = true }
            json.decodeFromString(EncryptedPayload.serializer(), trimmed).cipherText
        } catch (e: Exception) {
            trimmed
        }
    }
}
