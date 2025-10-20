package fr.vetbrain.vetnutri_mp.Localization

expect open class ResourceReader() {
    open fun readResource(name: String): String

    /**
     * Lit une ressource de manière optimisée pour les gros fichiers.
     * Utilise un buffer plus petit pour éviter les OutOfMemoryError.
     */
    open fun readResourceOptimized(name: String): String

    /**
     * Lit seulement le début d'une ressource JSON pour extraire la version.
     * Évite de charger tout le fichier en mémoire.
     */
    open fun readJsonVersion(name: String): String?

    /**
     * Lit un fichier utilisateur
     * @param filename Nom du fichier à lire
     * @return Contenu du fichier ou null s'il n'existe pas
     */
    open fun readUserFile(filename: String): String?

    /**
     * Écrit dans un fichier utilisateur
     * @param filename Nom du fichier à écrire
     * @param content Contenu à écrire
     * @return true si l'écriture a réussi, false sinon
     */
    open fun writeUserFile(filename: String, content: String): Boolean
}
