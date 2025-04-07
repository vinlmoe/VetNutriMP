package fr.vetbrain.vetnutri_mp.Localization

import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.readText

actual open class ResourceReader actual constructor() {
    actual open fun readResource(name: String): String {
        val resourcePath = "src/commonMain/resources/$name"
        return try {
            Path(resourcePath).readText()
        } catch (e: Exception) {
            throw IllegalStateException("Failed to read resource $name: ${e.message}")
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
