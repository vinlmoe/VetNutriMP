package fr.vetbrain.vetnutri_mp.Service

import kotlinx.serialization.Serializable

/**
 * Résultat d'un partage JSON
 */
data class ShareLink(
    val url: String,
    val binId: String,
    val expiresAt: Long? = null, // timestamp Unix en millisecondes
    val qrCodeData: String? = null // Données pour générer QR Code (ex: JSON {binId,key,iv})
)

/**
 * Payload QR Code chiffré (binId + key + iv)
 */
@Serializable
data class JsonBinQrPayload(
    val binId: String,
    val key: String,
    val iv: String
)

/**
 * Options de partage
 */
data class ShareOptions(
    val fileName: String? = null,
    val expiresInHours: Int? = null, // Durée avant expiration (null = pas d'expiration)
    val isPrivate: Boolean = false, // Si true, nécessite une clé API pour accéder
    val binName: String? = null, // Nom du bin (peut être l'UUID de l'animal pour identification)
    val binId: String? = null // ID du bin existant pour mise à jour (si fourni, fait un PUT au lieu d'un POST)
)

/**
 * Service de partage JSON via jsonbin.io
 * 
 * Note: jsonbin.io propose un plan gratuit avec certaines limitations:
 * - Pas de clé API nécessaire pour les bins publics (lecture seule)
 * - Pour les bins privés ou avec expiration, une clé API est nécessaire
 * - Limite de taille: ~100KB pour le plan gratuit
 */
expect class JsonShareService {
    /**
     * Upload un fichier JSON sur jsonbin.io
     * @param jsonContent Contenu JSON à partager
     * @param options Options de partage (nom fichier, expiration, etc.)
     * @return Result<ShareLink> avec l'URL de partage ou une erreur
     */
    suspend fun uploadJson(
        jsonContent: String,
        options: ShareOptions = ShareOptions()
    ): Result<ShareLink>
    
    /**
     * Télécharge un fichier JSON depuis jsonbin.io
     * @param binId ID du bin jsonbin.io (peut être extrait de l'URL)
     * @return Result<String> avec le contenu JSON ou une erreur
     */
    suspend fun downloadJson(
        binId: String,
        keyBase64: String? = null,
        ivBase64: String? = null
    ): Result<String>
    
    /**
     * Extrait l'ID du bin depuis une URL jsonbin.io
     * @param url URL complète du bin (ex: https://jsonbin.io/v3/b/1234567890)
     * @return L'ID du bin ou null si l'URL n'est pas valide
     */
    fun extractBinIdFromUrl(url: String): String?

    /**
     * Parse un QR JSON du type {binId, key, iv}
     */
    fun parseQrPayload(text: String): JsonBinQrPayload?
}

/**
 * Factory function pour créer une instance de JsonShareService
 */
expect fun createJsonShareService(): JsonShareService

/**
 * Modèles de données pour les réponses de l'API jsonbin.io v3
 */
@Serializable
data class JsonBinCreateResponse(
    val success: Boolean,
    val id: String,
    val metadata: JsonBinMetadata? = null
)

@Serializable
data class JsonBinMetadata(
    val id: String,
    val createdAt: String? = null,
    val private: Boolean = false
)

@Serializable
data class JsonBinReadResponse(
    val record: String? = null, // Le contenu JSON en string
    val metadata: JsonBinMetadata? = null
)

@Serializable
data class JsonBinErrorResponse(
    val success: Boolean = false,
    val message: String? = null
)
