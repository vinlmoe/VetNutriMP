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
     * Essaie plusieurs chemins possibles pour trouver le fichier.
     * Utilise une lecture progressive (4KB, 8KB, 16KB) si la version n'est pas trouvée.
     */
    actual open fun readJsonVersion(name: String): String? {
        // Liste des chemins possibles à essayer
        val candidatePaths = listOf(
            name,
            "data/$name"
        )
        
        // Tailles de buffer progressives pour le fallback (en KB)
        val bufferSizes = listOf(4096, 8192, 16384) // 4KB, 8KB, 16KB
        
        for (path in candidatePaths) {
            try {
                // Essayer avec des buffers de taille croissante
                // On doit ouvrir le fichier à nouveau pour chaque taille car on ne peut pas réinitialiser un InputStream
                for (bufferSize in bufferSizes) {
                    AndroidContext.appContext.assets.open(path).use { inputStream ->
                        val buffer = ByteArray(bufferSize)
                        val bytesRead = inputStream.read(buffer)
                        if (bytesRead > 0) {
                            val partialContent = String(buffer, 0, bytesRead)
                            val version = extractVersionFromJson(partialContent)
                            if (version != null) {
                                return version
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Continuer avec le chemin suivant
                continue
            }
        }
        
        return null
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
