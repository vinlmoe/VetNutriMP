package fr.vetbrain.vetnutri_mp.Localization

import java.io.File

actual open class ResourceReader actual constructor() {
    actual open fun readResource(name: String): String {
        return AndroidContext.appContext.assets.open(name).bufferedReader().use { it.readText() }
    }
    
    /**
     * Lit une ressource de manière optimisée pour les gros fichiers.
     * Utilise un buffer plus petit pour éviter les OutOfMemoryError.
     */
    actual open fun readResourceOptimized(name: String): String {
        return AndroidContext.appContext.assets.open(name).use { inputStream ->
            val buffer = ByteArray(8192) // Buffer de 8KB
            val output = StringBuilder()
            
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                output.append(String(buffer, 0, bytesRead))
            }
            output.toString()
        }
    }
    
    /**
     * Lit seulement le début d'une ressource JSON pour extraire la version.
     * Évite de charger tout le fichier en mémoire.
     */
    actual open fun readJsonVersion(name: String): String? {
        return try {
            AndroidContext.appContext.assets.open(name).use { inputStream ->
                val buffer = ByteArray(4096) // Buffer de 4KB
                val bytesRead = inputStream.read(buffer)
                if (bytesRead > 0) {
                    val partialContent = String(buffer, 0, bytesRead)
                    // Chercher la version dans les premiers 4KB
                    extractVersionFromJson(partialContent)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Extrait la version d'un JSON partiel en cherchant le pattern "version": "x.x.x"
     */
    private fun extractVersionFromJson(jsonContent: String): String? {
        val versionPattern = """"version"\s*:\s*"([^"]+)"""".toRegex()
        return versionPattern.find(jsonContent)?.groupValues?.get(1)
    }

    /** Lit un fichier utilisateur dans le répertoire des fichiers de l'application */
    actual open fun readUserFile(filename: String): String? {
        val context = AndroidContext.appContext
        val file = File(context.filesDir, filename)

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

    /** Écrit dans un fichier utilisateur dans le répertoire des fichiers de l'application */
    actual open fun writeUserFile(filename: String, content: String): Boolean {
        val context = AndroidContext.appContext
        val file = File(context.filesDir, filename)

        return try {
            file.writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }
}
