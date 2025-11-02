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
        private const val UPDATE_URL = "https://www.vetbrain.fr/vetnutri/updateVNMP.xml"
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
        println("[UpdateChecker] Début de checkForUpdate avec version: $currentVersion")
        println("[UpdateChecker] URL de mise à jour: $UPDATE_URL")
        
        return try {
            println("[UpdateChecker] Tentative de récupération du XML...")
            val xmlContent = fetchUpdateXml()
            println("[UpdateChecker] XML récupéré avec succès (${xmlContent.length} caractères)")
            println("[UpdateChecker] Aperçu XML (100 premiers caractères): ${xmlContent.take(100)}")
            
            println("[UpdateChecker] Parsing du XML...")
            val updateDescriptor = parseUpdateXml(xmlContent)
            println("[UpdateChecker] Parsing réussi")
            println("[UpdateChecker]  - baseUrl: ${updateDescriptor.baseUrl}")
            println("[UpdateChecker]  - newVersion: ${updateDescriptor.entry.newVersion}")
            println("[UpdateChecker]  - targetMediaFileId: ${updateDescriptor.entry.targetMediaFileId}")
            
            val newVersion = updateDescriptor.entry.newVersion
            println("[UpdateChecker] Comparaison des versions:")
            println("[UpdateChecker]  - Version actuelle: $currentVersion")
            println("[UpdateChecker]  - Nouvelle version: $newVersion")
            
            val comparisonResult = compareVersions(currentVersion, newVersion)
            println("[UpdateChecker] Résultat comparaison: $comparisonResult (-1 = actuelle < nouvelle, 0 = égales, 1 = actuelle > nouvelle)")
            
            val isUpdateAvailable = comparisonResult < 0
            println("[UpdateChecker] Mise à jour disponible: $isUpdateAvailable")
            
            val result = UpdateCheckResult(
                isUpdateAvailable = isUpdateAvailable,
                currentVersion = currentVersion,
                newVersion = if (isUpdateAvailable) newVersion else null
            )
            println("[UpdateChecker] Résultat final créé avec succès")
            result
            
        } catch (e: Exception) {
            println("[UpdateChecker] ❌ ERREUR lors de la vérification:")
            println("[UpdateChecker] Type: ${e::class.simpleName}")
            println("[UpdateChecker] Message: ${e.message}")
            println("[UpdateChecker] Cause: ${e.cause?.message ?: "Aucune"}")
            e.printStackTrace()
            
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
        println("[UpdateChecker] fetchUpdateXml() - Début")
        println("[UpdateChecker] URL: $UPDATE_URL")
        
        try {
            println("[UpdateChecker] Appel de PlatformHttpClient.fetchXml()...")
            val xmlContent = PlatformHttpClient.fetchXml(UPDATE_URL)
            println("[UpdateChecker] fetchXml() réussi, contenu reçu (${xmlContent.length} caractères)")
            xmlContent
        } catch (e: Exception) {
            println("[UpdateChecker] ❌ ERREUR dans fetchUpdateXml():")
            println("[UpdateChecker] Type: ${e::class.simpleName}")
            println("[UpdateChecker] Message: ${e.message}")
            println("[UpdateChecker] Cause: ${e.cause?.message ?: "Aucune"}")
            e.printStackTrace()
            throw UpdateException("Impossible de récupérer le fichier de mise à jour", e)
        }
    }
    
    /**
     * Parse le contenu XML pour extraire les informations de mise à jour
     */
    private fun parseUpdateXml(xmlContent: String): UpdateDescriptor {
        println("[UpdateChecker] parseUpdateXml() - Début")
        println("[UpdateChecker] Taille du contenu XML: ${xmlContent.length} caractères")
        
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
            
            println("[UpdateChecker] Extraction des valeurs avec regex...")
            val baseUrl = baseUrlRegex.find(xmlContent)?.groupValues?.get(1) ?: ""
            val newVersion = newVersionRegex.find(xmlContent)?.groupValues?.get(1) ?: ""
            val targetMediaFileId = targetMediaFileIdRegex.find(xmlContent)?.groupValues?.get(1) ?: ""
            val updatableVersionMin = updatableVersionMinRegex.find(xmlContent)?.groupValues?.get(1) ?: ""
            val updatableVersionMax = updatableVersionMaxRegex.find(xmlContent)?.groupValues?.get(1) ?: ""
            val newMediaFileId = newMediaFileIdRegex.find(xmlContent)?.groupValues?.get(1) ?: ""
            val fileSize = fileSizeRegex.find(xmlContent)?.groupValues?.get(1) ?: ""
            val archive = archiveRegex.find(xmlContent)?.groupValues?.get(1) ?: "false"
            val singleBundle = singleBundleRegex.find(xmlContent)?.groupValues?.get(1) ?: "false"
            
            println("[UpdateChecker] Valeurs extraites:")
            println("[UpdateChecker]  - baseUrl: '$baseUrl'")
            println("[UpdateChecker]  - newVersion: '$newVersion'")
            println("[UpdateChecker]  - targetMediaFileId: '$targetMediaFileId'")
            println("[UpdateChecker]  - updatableVersionMin: '$updatableVersionMin'")
            println("[UpdateChecker]  - updatableVersionMax: '$updatableVersionMax'")
            println("[UpdateChecker]  - newMediaFileId: '$newMediaFileId'")
            println("[UpdateChecker]  - fileSize: '$fileSize'")
            println("[UpdateChecker]  - archive: '$archive'")
            println("[UpdateChecker]  - singleBundle: '$singleBundle'")
            
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
            
            val descriptor = UpdateDescriptor(baseUrl = baseUrl, entry = entry)
            println("[UpdateChecker] UpdateDescriptor créé avec succès")
            descriptor
            
        } catch (e: Exception) {
            println("[UpdateChecker] ❌ ERREUR dans parseUpdateXml():")
            println("[UpdateChecker] Type: ${e::class.simpleName}")
            println("[UpdateChecker] Message: ${e.message}")
            println("[UpdateChecker] Cause: ${e.cause?.message ?: "Aucune"}")
            e.printStackTrace()
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
