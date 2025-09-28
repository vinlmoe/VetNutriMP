package fr.vetbrain.vetnutri_mp.Localization

import java.io.File
import kotlin.io.path.readText

actual open class ResourceReader actual constructor() {
    actual open fun readResource(name: String): String {
        return try {
            // Méthode 1: Essayer d'accéder via le classloader (ressources JAR)
            val classLoader = this::class.java.classLoader
            val resourceStream = classLoader.getResourceAsStream(name)
            if (resourceStream != null) {
                return resourceStream.bufferedReader().use { it.readText() }
            }

            // Méthode 2: Essayer avec un chemin relatif depuis le répertoire de travail
            val relativePath = "composeApp/src/commonMain/resources/$name"
            val relativeFile = File(relativePath)
            if (relativeFile.exists()) {
                return relativeFile.readText()
            }

            // Méthode 3: Essayer depuis le répertoire parent
            val parentPath = "../composeApp/src/commonMain/resources/$name"
            val parentFile = File(parentPath)
            if (parentFile.exists()) {
                return parentFile.readText()
            }

            // Méthode 4: Essayer le chemin original pour compatibilité
            val originalPath = "src/commonMain/resources/$name"
            val originalFile = File(originalPath)
            if (originalFile.exists()) {
                return originalFile.readText()
            }

            // Aucune méthode n'a fonctionné
            val currentDir = System.getProperty("user.dir")

            throw IllegalStateException("Resource not found: $name")
        } catch (e: Exception) {
            throw IllegalStateException("Failed to read resource $name: ${e.message}", e)
        }
    }
    
    /**
     * Lit une ressource de manière optimisée pour les gros fichiers.
     * Pour Desktop, utilise un buffer plus petit pour éviter les OutOfMemoryError.
     */
    actual open fun readResourceOptimized(name: String): String {
        return try {
            val classLoader = this::class.java.classLoader
            val resourceStream = classLoader.getResourceAsStream(name)
            if (resourceStream != null) {
                return resourceStream.use { inputStream ->
                    val buffer = ByteArray(8192) // Buffer de 8KB
                    val output = StringBuilder()
                    
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        output.append(String(buffer, 0, bytesRead))
                    }
                    output.toString()
                }
            }
            
            // Fallback vers readResource pour les autres méthodes
            readResource(name)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to read resource $name: ${e.message}", e)
        }
    }
    
    /**
     * Lit seulement le début d'une ressource JSON pour extraire la version.
     * Évite de charger tout le fichier en mémoire.
     */
    actual open fun readJsonVersion(name: String): String? {
        return try {
            val classLoader = this::class.java.classLoader
            val resourceStream = classLoader.getResourceAsStream(name)
            if (resourceStream != null) {
                return resourceStream.use { inputStream ->
                    val buffer = ByteArray(4096) // Buffer de 4KB
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead > 0) {
                        val partialContent = String(buffer, 0, bytesRead)
                        extractVersionFromJson(partialContent)
                    } else {
                        null
                    }
                }
            }
            
            // Fallback: lire le fichier complet et extraire la version
            val content = readResource(name)
            extractVersionFromJson(content)
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

    /** Lit un fichier utilisateur dans le répertoire de l'application */
    actual open fun readUserFile(filename: String): String? {
        val userDir = System.getProperty("user.home")
        val appDir = File(userDir, "VetNutriMP")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }

        val file = File(appDir, filename)
        return if (file.exists()) {
            try {
                file.readText()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    /** Écrit dans un fichier utilisateur dans le répertoire de l'application */
    actual open fun writeUserFile(filename: String, content: String): Boolean {
        val userDir = System.getProperty("user.home")
        val appDir = File(userDir, "VetNutriMP")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }

        val file = File(appDir, filename)
        return try {
            file.writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }
}
