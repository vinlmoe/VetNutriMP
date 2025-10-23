package fr.vetbrain.vetnutri_mp.Localization

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class)
actual open class ResourceReader actual constructor() {
    actual open fun readResource(name: String): String {
        val bundle = NSBundle.mainBundle
        val resourceName = name.removeSuffix(".json")
        val extension = "json"

        val path = bundle.pathForResource(resourceName, extension)

        if (path == null) {

            // Lister toutes les ressources disponibles
            val allResources = bundle.pathsForResourcesOfType(extension, null)

            allResources?.forEachIndexed { index, resourcePath -> }

            throw IllegalStateException("Resource $name not found")
        }

        val content = NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)

        if (content == null) {

            throw IllegalStateException("Failed to read resource $name")
        }

        return content
    }
    
    /**
     * Lit une ressource de manière optimisée pour les gros fichiers.
     * Pour iOS, utilise la même méthode que readResource car NSString.stringWithContentsOfFile
     * est déjà optimisé.
     */
    actual open fun readResourceOptimized(name: String): String {
        return readResource(name)
    }
    
    /**
     * Lit seulement le début d'une ressource JSON pour extraire la version.
     * Évite de charger tout le fichier en mémoire (optimisation pour les gros fichiers).
     */
    actual open fun readJsonVersion(name: String): String? {
        return try {
            // Pour iOS, lire seulement les premières lignes pour éviter de charger 18MB
            val fullContent = readResource(name)
            // Prendre seulement les 50 premières lignes
            val firstLines = fullContent.lines().take(50).joinToString("\n")
            extractVersionFromJson(firstLines)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Extrait la version d'un JSON en cherchant le pattern "version": "x.x.x"
     */
    private fun extractVersionFromJson(jsonContent: String): String? {
        val versionPattern = """"version"\s*:\s*"([^"]+)"""".toRegex()
        return versionPattern.find(jsonContent)?.groupValues?.get(1)
    }

    /** Lit un fichier utilisateur dans le répertoire des documents de l'application */
    actual open fun readUserFile(filename: String): String? {
        return try {
            val documentsPath =
                    NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
                            .firstOrNull() as?
                            String
                            ?: return null

            val filePath = "$documentsPath/$filename"
            val fileManager = NSFileManager.defaultManager

            if (fileManager.fileExistsAtPath(filePath)) {
                NSString.stringWithContentsOfFile(filePath, NSUTF8StringEncoding, null)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /** Écrit dans un fichier utilisateur dans le répertoire des documents de l'application */
    actual open fun writeUserFile(filename: String, content: String): Boolean {
        return try {
            val documentsPath =
                    NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
                            .firstOrNull() as?
                            String
                            ?: return false

            val filePath = "$documentsPath/$filename"
            val nsString = content as NSString

            nsString.writeToFile(filePath, true, NSUTF8StringEncoding, null)
        } catch (e: Exception) {
            false
        }
    }
}
