package fr.vetbrain.vetnutri_mp.Utils

/** Interface multiplateforme pour le stockage des préférences */
expect class PreferencesStorage {

    /** Sauvegarde une valeur string */
    suspend fun saveString(key: String, value: String)

    /** Récupère une valeur string */
    suspend fun getString(key: String, defaultValue: String = ""): String

    /** Supprime une valeur */
    suspend fun remove(key: String)

    /** Vérifie si une clé existe */
    suspend fun contains(key: String): Boolean

    /** Efface toutes les préférences */
    suspend fun clear()
}

/** Fonction helper pour créer une instance de PreferencesStorage */
expect fun createPreferencesStorage(): PreferencesStorage
 