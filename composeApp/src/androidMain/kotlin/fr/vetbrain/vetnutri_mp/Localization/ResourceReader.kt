package fr.vetbrain.vetnutri_mp.Localization

import java.io.File

actual open class ResourceReader actual constructor() {
    actual open fun readResource(name: String): String {
        return AndroidContext.appContext.assets.open(name).bufferedReader().use { it.readText() }
    }

    /** Lit un fichier utilisateur dans le répertoire des fichiers de l'application */
    actual open fun readUserFile(filename: String): String? {
        val context = AndroidContext.appContext
        val file = File(context.filesDir, filename)

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

    /** Écrit dans un fichier utilisateur dans le répertoire des fichiers de l'application */
    actual open fun writeUserFile(filename: String, content: String): Boolean {
        val context = AndroidContext.appContext
        val file = File(context.filesDir, filename)

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
