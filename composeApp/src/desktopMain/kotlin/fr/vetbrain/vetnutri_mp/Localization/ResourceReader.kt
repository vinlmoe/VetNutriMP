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
                println("DEBUG ResourceReader: Ressource trouvée via classloader: $name")
                return resourceStream.bufferedReader().use { it.readText() }
            }

            // Méthode 2: Essayer avec un chemin relatif depuis le répertoire de travail
            val relativePath = "composeApp/src/commonMain/resources/$name"
            val relativeFile = File(relativePath)
            if (relativeFile.exists()) {
                println("DEBUG ResourceReader: Ressource trouvée via chemin relatif: $relativePath")
                return relativeFile.readText()
            }

            // Méthode 3: Essayer depuis le répertoire parent
            val parentPath = "../composeApp/src/commonMain/resources/$name"
            val parentFile = File(parentPath)
            if (parentFile.exists()) {
                println("DEBUG ResourceReader: Ressource trouvée via chemin parent: $parentPath")
                return parentFile.readText()
            }

            // Méthode 4: Essayer le chemin original pour compatibilité
            val originalPath = "src/commonMain/resources/$name"
            val originalFile = File(originalPath)
            if (originalFile.exists()) {
                println(
                        "DEBUG ResourceReader: Ressource trouvée via chemin original: $originalPath"
                )
                return originalFile.readText()
            }

            // Aucune méthode n'a fonctionné
            val currentDir = System.getProperty("user.dir")
            println("DEBUG ResourceReader: Répertoire de travail actuel: $currentDir")
            println("DEBUG ResourceReader: Tentatives de chemins:")
            println("  - Classloader: $name")
            println("  - Relatif: $relativePath")
            println("  - Parent: $parentPath")
            println("  - Original: $originalPath")

            throw IllegalStateException("Resource not found: $name")
        } catch (e: Exception) {
            println("ERROR ResourceReader: Failed to read resource $name: ${e.message}")
            throw IllegalStateException("Failed to read resource $name: ${e.message}", e)
        }
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
                println(
                        "ERROR ResourceReader: Impossible de lire le fichier $filename: ${e.message}"
                )
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
            println(
                    "ERROR ResourceReader: Impossible d'écrire dans le fichier $filename: ${e.message}"
            )
            false
        }
    }
}
