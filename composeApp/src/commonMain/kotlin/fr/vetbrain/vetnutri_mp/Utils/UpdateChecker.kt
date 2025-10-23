package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

/**
 * Exception personnalisée pour les erreurs de mise à jour
 */
class UpdateException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Interface multiplateforme pour les requêtes HTTP
 * Désactivé sur iOS et Android pour éviter les problèmes de compatibilité
 */
expect object PlatformHttpClient {
    suspend fun fetchXml(url: String): String
}

/**
 * Gestionnaire de vérification des mises à jour de l'application
 * Lit le fichier XML de mise à jour et compare les versions
 */
class UpdateChecker {
    
    companion object {
        private const val UPDATE_URL = "http://www.vetbrain.fr/vetnutri/updateVNMP.xml"
        private const val DOWNLOAD_URL = "https://vetbrain.fr/index.php/vetnutri-multiplatforme/"
    }
    
    @Serializable
    data class UpdateDescriptor(
        val baseUrl: String = "",
        val entry: UpdateEntry
    )
    
    @Serializable
    data class UpdateEntry(
        val targetMediaFileId: String = "",
        val updatableVersionMin: String = "",
        val updatableVersionMax: String = "",
        val newVersion: String = "",
        val newMediaFileId: String = "",
        val fileSize: String = "",
        val archive: String = "false",
        val singleBundle: String = "false"
    )
    
    /**
     * Résultat de la vérification de mise à jour
     */
    data class UpdateCheckResult(
        val isUpdateAvailable: Boolean,
        val currentVersion: String,
        val newVersion: String?,
        val downloadUrl: String = DOWNLOAD_URL,
        val error: String? = null
    )
    
    /**
     * Vérifie si une mise à jour est disponible
     * @param currentVersion La version actuelle de l'application
     * @return UpdateCheckResult avec les informations de mise à jour
     */
    suspend fun checkForUpdate(currentVersion: String): UpdateCheckResult {
        return try {
            val xmlContent = fetchUpdateXml()
            val updateDescriptor = parseUpdateXml(xmlContent)
            
            val newVersion = updateDescriptor.entry.newVersion
            val isUpdateAvailable = compareVersions(currentVersion, newVersion) < 0
            
            UpdateCheckResult(
                isUpdateAvailable = isUpdateAvailable,
                currentVersion = currentVersion,
                newVersion = if (isUpdateAvailable) newVersion else null
            )
            
        } catch (e: Exception) {
            UpdateCheckResult(
                isUpdateAvailable = false,
                currentVersion = currentVersion,
                newVersion = null,
                error = "Erreur lors de la vérification de mise à jour: ${e.message}"
            )
        }
    }
    
    /**
     * Récupère le contenu XML depuis l'URL
     */
    private suspend fun fetchUpdateXml(): String = withContext(Dispatchers.Default) {
        try {
            PlatformHttpClient.fetchXml(UPDATE_URL)
        } catch (e: Exception) {
            throw UpdateException("Impossible de récupérer le fichier de mise à jour", e)
        }
    }
    
    /**
     * Parse le contenu XML pour extraire les informations de mise à jour
     */
    private fun parseUpdateXml(xmlContent: String): UpdateDescriptor {
        return try {
            // Extraction simple avec des regex
            val baseUrlRegex = """baseUrl="([^"]*)"""".toRegex()
            val newVersionRegex = """newVersion="([^"]*)"""".toRegex()
            val targetMediaFileIdRegex = """targetMediaFileId="([^"]*)"""".toRegex()
            val updatableVersionMinRegex = """updatableVersionMin="([^"]*)"""".toRegex()
            val updatableVersionMaxRegex = """updatableVersionMax="([^"]*)"""".toRegex()
            val newMediaFileIdRegex = """newMediaFileId="([^"]*)"""".toRegex()
            val fileSizeRegex = """fileSize="([^"]*)"""".toRegex()
            val archiveRegex = """archive="([^"]*)"""".toRegex()
            val singleBundleRegex = """singleBundle="([^"]*)"""".toRegex()
            
            val baseUrl = baseUrlRegex.find(xmlContent)?.groupValues?.get(1) ?: ""
            val newVersion = newVersionRegex.find(xmlContent)?.groupValues?.get(1) ?: ""
            val targetMediaFileId = targetMediaFileIdRegex.find(xmlContent)?.groupValues?.get(1) ?: ""
            val updatableVersionMin = updatableVersionMinRegex.find(xmlContent)?.groupValues?.get(1) ?: ""
            val updatableVersionMax = updatableVersionMaxRegex.find(xmlContent)?.groupValues?.get(1) ?: ""
            val newMediaFileId = newMediaFileIdRegex.find(xmlContent)?.groupValues?.get(1) ?: ""
            val fileSize = fileSizeRegex.find(xmlContent)?.groupValues?.get(1) ?: ""
            val archive = archiveRegex.find(xmlContent)?.groupValues?.get(1) ?: "false"
            val singleBundle = singleBundleRegex.find(xmlContent)?.groupValues?.get(1) ?: "false"
            
            val entry = UpdateEntry(
                targetMediaFileId = targetMediaFileId,
                updatableVersionMin = updatableVersionMin,
                updatableVersionMax = updatableVersionMax,
                newVersion = newVersion,
                newMediaFileId = newMediaFileId,
                fileSize = fileSize,
                archive = archive,
                singleBundle = singleBundle
            )
            
            UpdateDescriptor(baseUrl = baseUrl, entry = entry)
            
        } catch (e: Exception) {
            throw UpdateException("Erreur lors du parsing XML", e)
        }
    }
    
    /**
     * Compare deux versions au format semver (ex: "1.2.3")
     * @param version1 Première version
     * @param version2 Deuxième version
     * @return -1 si version1 < version2, 0 si égales, 1 si version1 > version2
     */
    private fun compareVersions(version1: String, version2: String): Int {
        val v1Parts = version1.split(".").map { it.toIntOrNull() ?: 0 }
        val v2Parts = version2.split(".").map { it.toIntOrNull() ?: 0 }
        
        val maxLength = maxOf(v1Parts.size, v2Parts.size)
        
        for (i in 0 until maxLength) {
            val v1Part = v1Parts.getOrNull(i) ?: 0
            val v2Part = v2Parts.getOrNull(i) ?: 0
            
            when {
                v1Part < v2Part -> return -1
                v1Part > v2Part -> return 1
            }
        }
        
        return 0
    }
}
