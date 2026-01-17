package fr.vetbrain.vetnutri_mp.Localization

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
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
        println("ResourceReader: Reading resource: $name")
        val bundle = NSBundle.mainBundle
        val resourceName = name.removeSuffix(".json")
        val extension = "json"

        println("ResourceReader: Path for resource: $resourceName with extension: $extension")
        var path = bundle.pathForResource(resourceName, extension)

        if (path == null) {
            println("ResourceReader: Resource $name not found in main bundle. Trying with extension in name.")
            path = bundle.pathForResource(name, null)
        }

        if (path == null) {
            println("ResourceReader: Resource $name still not found. Listing all available json resources:")
            val allResources = bundle.pathsForResourcesOfType("json", null)
            allResources?.forEach { resourcePath ->
                println("ResourceReader: Found JSON resource: $resourcePath")
            }
            throw IllegalStateException("Resource $name not found")
        }

        println("ResourceReader: Found path: $path")
        val content = NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)

        if (content == null) {
            println("ResourceReader: Failed to read content from path: $path")
            throw IllegalStateException("Failed to read resource $name")
        }

        println("ResourceReader: Successfully read ${content.length} characters from $name")
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
     * Optimisé pour ne pas charger les 21MB en mémoire en utilisant l'API POSIX.
     */
    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    actual open fun readJsonVersion(name: String): String? {
        println("ResourceReader: Checking version for $name")
        return try {
            val bundle = NSBundle.mainBundle
            val resourceName = name.removeSuffix(".json")
            val extension = "json"
            val path = bundle.pathForResource(resourceName, extension) ?: bundle.pathForResource(name, null)
            
            if (path == null) return null
            
            // Utilisation de fopen/fread (POSIX) pour lire juste le début.
            // Très efficace et évite les problèmes d'interop Foundation complexes.
            val file = platform.posix.fopen(path, "r") ?: return null
            try {
                val buffer = ByteArray(4096)
                val bytesRead = buffer.usePinned { pinned ->
                    platform.posix.fread(pinned.addressOf(0), 1u, 4096u, file)
                }
                
                if (bytesRead == 0uL) return null
                
                val chunk = buffer.decodeToString(0, bytesRead.toInt())
                val version = extractVersionFromJson(chunk)
                println("ResourceReader: Version found for $name: $version")
                return version
            } finally {
                platform.posix.fclose(file)
            }
        } catch (e: Exception) {
            println("ResourceReader: Error reading version for $name: ${e.message}")
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
