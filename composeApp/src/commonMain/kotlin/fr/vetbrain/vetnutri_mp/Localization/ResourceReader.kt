package fr.vetbrain.vetnutri_mp.Localization

expect open class ResourceReader() {
    open fun readResource(name: String): String

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
