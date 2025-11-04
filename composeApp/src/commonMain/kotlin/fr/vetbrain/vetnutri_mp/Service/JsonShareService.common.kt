package fr.vetbrain.vetnutri_mp.Service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.withContext

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
    
    suspend fun uploadJson(
        jsonContent: String,
        options: ShareOptions
    ): Result<ShareLink> = withContext(AppDispatchers.IO) {
        try {
            val isUpdate = options.binId != null
            println("🔵 [JsonShareService] Début de l'${if (isUpdate) "update" else "upload"} vers jsonbin.io")
            println("🔵 [JsonShareService] Taille du JSON: ${jsonContent.length} caractères")
            println("🔵 [JsonShareService] Options: expiresInHours=${options.expiresInHours}, isPrivate=${options.isPrivate}, binId=${options.binId}")
            println("🔵 [JsonShareService] Clé API écriture présente: ${createUpdateApiKey != null}")
            
            // Construire l'URL
            // Si binId est fourni, on fait un PUT pour mettre à jour, sinon POST pour créer
            val url = if (isUpdate) {
                "$baseUrl/b/${options.binId}"
            } else {
                "$baseUrl/b"
            }
            println("🔵 [JsonShareService] URL: $url (${if (isUpdate) "PUT" else "POST"})")
            
            // Faire la requête POST ou PUT
            val response = if (isUpdate) {
                httpClient.put(url) {
                    headers {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        // Utiliser X-Access-Key pour la clé d'écriture (pas X-Master-Key)
                        createUpdateApiKey?.let { key ->
                            println("🔵 [JsonShareService] Ajout header X-Access-Key (${key.take(10)}...)")
                            append("X-Access-Key", key)
                        }
                        // Pour l'expiration lors de la mise à jour, nécessite une clé API
                        if (options.expiresInHours != null && createUpdateApiKey != null) {
                            val expirationSeconds = options.expiresInHours * 3600
                            println("🔵 [JsonShareService] Ajout header X-Bin-Expiration: $expirationSeconds secondes")
                            append("X-Bin-Expiration", expirationSeconds.toString())
                        }
                    }
                    setBody(jsonContent)
                }
            } else {
                httpClient.post(url) {
                    headers {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        // Utiliser X-Access-Key pour la clé d'écriture (pas X-Master-Key)
                        createUpdateApiKey?.let { key ->
                            println("🔵 [JsonShareService] Ajout header X-Access-Key (${key.take(10)}...)")
                            append("X-Access-Key", key)
                        }
                        // Nom du bin (peut être l'UUID de l'animal pour identification)
                        // Note: jsonbin.io ne permet pas d'ID personnalisé, mais on peut nommer le bin
                        options.binName?.let { name ->
                            println("🔵 [JsonShareService] Ajout header X-Bin-Name: $name")
                            append("X-Bin-Name", name)
                        }
                        // Pour les bins privés, nécessite une clé API
                        if (options.isPrivate && createUpdateApiKey != null) {
                            println("🔵 [JsonShareService] Ajout header X-Bin-Private: true")
                            append("X-Bin-Private", "true")
                        }
                        // Pour l'expiration, nécessite une clé API
                        if (options.expiresInHours != null && createUpdateApiKey != null) {
                            val expirationSeconds = options.expiresInHours * 3600
                            println("🔵 [JsonShareService] Ajout header X-Bin-Expiration: $expirationSeconds secondes")
                            append("X-Bin-Expiration", expirationSeconds.toString())
                        }
                    }
                    setBody(jsonContent)
                }
            }
            
            println("🔵 [JsonShareService] Réponse reçue: status=${response.status.value}, description=${response.status.description}")
            
            // Afficher tous les headers de réponse pour debug
            println("🔵 [JsonShareService] Headers de réponse:")
            response.headers.forEach { name, values ->
                println("🔵 [JsonShareService]   $name: ${values.joinToString(", ")}")
            }
            
            if (response.status.isSuccess()) {
                // Parser la réponse JSON manuellement
                val responseText = response.body<String>()
                println("🔵 [JsonShareService] Réponse JSON complète: $responseText")
                val json = Json { ignoreUnknownKeys = true; isLenient = true }
                
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
                                println("🔵 [JsonShareService] Location header: $locationHeader")
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
                                println("⚠️ [JsonShareService] Format réponse non standard, tentative alternative...")
                                // Essayer de parser le format alternatif avec "metadata"
                                try {
                                    val jsonObj = Json.parseToJsonElement(responseText).jsonObject
                                    val metadata = jsonObj["metadata"]?.jsonObject
                                    metadata?.get("id")?.jsonPrimitive?.content
                                } catch (e2: Exception) {
                                    println("❌ [JsonShareService] Impossible d'extraire l'ID: ${e2.message}")
                                    throw Exception("Format de réponse inattendu: $responseText")
                                }
                            }
                        }
                }
                
                if (binId == null) {
                    throw Exception("Impossible d'extraire l'ID du bin depuis la réponse")
                }
                
                val shareUrl = "https://jsonbin.io/$binId"
                
                println("✅ [JsonShareService] ${if (isUpdate) "Mise à jour" else "Upload"} réussi! Bin ID: $binId")
                println("✅ [JsonShareService] URL de partage: $shareUrl")
                
                // Calculer la date d'expiration si spécifiée
                val expiresAt = options.expiresInHours?.let { hours ->
                    System.currentTimeMillis() + (hours * 3600 * 1000L)
                }
                
                Result.success(ShareLink(
                    url = shareUrl,
                    binId = binId,
                    expiresAt = expiresAt,
                    qrCodeData = shareUrl
                ))
            } else {
                val responseText = response.body<String>()
                println("❌ [JsonShareService] Erreur HTTP: ${response.status.value} - ${response.status.description}")
                println("❌ [JsonShareService] Réponse erreur: $responseText")
                
                val errorResponse = try {
                    val json = Json { ignoreUnknownKeys = true; isLenient = true }
                    json.decodeFromString<JsonBinErrorResponse>(responseText)
                } catch (e: Exception) {
                    println("❌ [JsonShareService] Erreur parsing réponse: ${e.message}")
                    null
                }
                
                val errorMessage = "Erreur lors de l'upload: ${response.status} - ${errorResponse?.message ?: response.status.description}"
                println("❌ [JsonShareService] $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            println("❌ [JsonShareService] Exception lors de l'upload: ${e.message}")
            e.printStackTrace()
            Result.failure(Exception("Erreur lors de l'upload vers jsonbin.io: ${e.message}", e))
        }
    }
    
    suspend fun downloadJson(binId: String): Result<String> = withContext(AppDispatchers.IO) {
        try {
            println("🔵 [JsonShareService] Début du téléchargement depuis jsonbin.io")
            println("🔵 [JsonShareService] Bin ID: $binId")
            println("🔵 [JsonShareService] Clé API lecture présente: ${readApiKey != null}")
            
            val url = "$baseUrl/b/$binId"
            println("🔵 [JsonShareService] URL: $url")
            
            val response = httpClient.get(url) {
                headers {
                    // Utiliser X-Access-Key pour la clé de lecture (pas X-Master-Key)
                    readApiKey?.let { key ->
                        println("🔵 [JsonShareService] Ajout header X-Access-Key (${key.take(10)}...)")
                        append("X-Access-Key", key)
                    }
                }
            }
            
            println("🔵 [JsonShareService] Réponse reçue: status=${response.status.value}, description=${response.status.description}")
            
            if (response.status.isSuccess()) {
                // Parser la réponse JSON manuellement
                val responseText = response.body<String>()
                println("🔵 [JsonShareService] Réponse JSON: ${responseText.take(200)}...")
                val json = Json { ignoreUnknownKeys = true; isLenient = true }
                val readResponse = json.decodeFromString<JsonBinReadResponse>(responseText)
                val content = readResponse.record
                if (content != null) {
                    println("✅ [JsonShareService] Téléchargement réussi! Taille: ${content.length} caractères")
                    Result.success(content)
                } else {
                    println("❌ [JsonShareService] Le bin est vide ou inaccessible")
                    Result.failure(Exception("Le bin est vide ou inaccessible"))
                }
            } else {
                val responseText = response.body<String>()
                println("❌ [JsonShareService] Erreur HTTP: ${response.status.value} - ${response.status.description}")
                println("❌ [JsonShareService] Réponse erreur: $responseText")
                
                val errorResponse = try {
                    val json = Json { ignoreUnknownKeys = true; isLenient = true }
                    json.decodeFromString<JsonBinErrorResponse>(responseText)
                } catch (e: Exception) {
                    println("❌ [JsonShareService] Erreur parsing réponse: ${e.message}")
                    null
                }
                
                val errorMessage = "Erreur lors du téléchargement: ${response.status} - ${errorResponse?.message ?: response.status.description}"
                println("❌ [JsonShareService] $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            println("❌ [JsonShareService] Exception lors du téléchargement: ${e.message}")
            e.printStackTrace()
            Result.failure(Exception("Erreur lors du téléchargement depuis jsonbin.io: ${e.message}", e))
        }
    }
    
    fun extractBinIdFromUrl(url: String): String? {
        // Formats d'URL jsonbin.io:
        // https://jsonbin.io/v3/b/1234567890
        // https://jsonbin.io/1234567890
        // jsonbin.io/1234567890
        
        val patterns = listOf(
            Regex("jsonbin\\.io/v3/b/([a-zA-Z0-9]+)"),
            Regex("jsonbin\\.io/([a-zA-Z0-9]+)"),
            Regex("([a-zA-Z0-9]{10,})") // ID seul (au moins 10 caractères)
        )
        
        for (pattern in patterns) {
            val match = pattern.find(url)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        
        return null
    }
}

