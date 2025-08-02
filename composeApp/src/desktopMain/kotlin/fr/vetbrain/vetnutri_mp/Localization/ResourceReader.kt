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
