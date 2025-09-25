package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/**
 * Gestionnaire des versions de la base de données
 * Sauvegarde la version actuelle et propose des mises à jour
 */
class DatabaseVersionManager {
    
    companion object {
        private const val KEY_DB_VERSION = "database_version"
        private const val KEY_DB_LAST_UPDATE = "database_last_update"
        private const val KEY_DB_IMPORT_SOURCE = "database_import_source"
        private const val KEY_JSON_VERSION = "json_version"
        private const val KEY_JSON_TIMESTAMP = "json_timestamp"

        // Version par défaut si aucune n'est définie
        const val DEFAULT_VERSION = "1.0.0"
    }
    
    private val _currentVersion = MutableStateFlow(DEFAULT_VERSION)
    val currentVersion: StateFlow<String> = _currentVersion.asStateFlow()
    
    private val _lastUpdateDate = MutableStateFlow<String?>(null)
    val lastUpdateDate: StateFlow<String?> = _lastUpdateDate.asStateFlow()
    
    private val _importSource = MutableStateFlow<String?>(null)
    val importSource: StateFlow<String?> = _importSource.asStateFlow()

    private val _jsonVersion = MutableStateFlow<String?>(null)
    val jsonVersion: StateFlow<String?> = _jsonVersion.asStateFlow()

    private val _jsonTimestamp = MutableStateFlow<Long?>(null)
    val jsonTimestamp: StateFlow<Long?> = _jsonTimestamp.asStateFlow()
    
    /**
     * Vérifie si une mise à jour est disponible
     * @param newVersion La nouvelle version disponible
     * @return true si une mise à jour est nécessaire
     */
    suspend fun isUpdateAvailable(newVersion: String): Boolean {
        val currentVersion = getStoredDatabaseVersion()
        return compareVersions(currentVersion, newVersion) < 0
    }
    
    /**
     * Met à jour la version de la base de données
     * @param newVersion La nouvelle version
     * @param importSource La source de l'import (fichier, URL, etc.)
     */
    suspend fun updateDatabaseVersion(newVersion: String, importSource: String) {
        try {
            val storage = createPreferencesStorage()
            
            // Sauvegarder la nouvelle version
            storage.saveString(KEY_DB_VERSION, newVersion)
            
            // Sauvegarder la date de mise à jour
            val currentDate = getCurrentDate()
            storage.saveString(KEY_DB_LAST_UPDATE, currentDate)
            
            // Sauvegarder la source d'import
            storage.saveString(KEY_DB_IMPORT_SOURCE, importSource)
            
            // Mettre à jour les états locaux
            _currentVersion.value = newVersion
            _lastUpdateDate.value = currentDate
            _importSource.value = importSource
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Récupère la version actuelle de la base de données
     * @return La version actuelle
     */
    suspend fun getCurrentDatabaseVersion(): String {
        val version = getStoredDatabaseVersion()
        _currentVersion.value = version
        return version
    }
    
    /**
     * Récupère la date de la dernière mise à jour
     * @return La date de la dernière mise à jour ou null
     */
    suspend fun getLastUpdateDate(): String? {
        val date = getStoredLastUpdateDate()
        _lastUpdateDate.value = date
        return date
    }
    
    /**
     * Récupère la source de la dernière importation
     * @return La source de l'import ou null
     */
    suspend fun getImportSource(): String? {
        val source = getStoredImportSource()
        _importSource.value = source
        return source
    }
    
    /**
     * Compare deux versions et retourne le résultat de la comparaison
     * @param version1 Première version
     * @param version2 Deuxième version
     * @return -1 si version1 < version2, 0 si égales, 1 si version1 > version2
     */
    fun compareVersions(version1: String, version2: String): Int {
        val parts1 = version1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = version2.split(".").map { it.toIntOrNull() ?: 0 }
        
        val maxLength = maxOf(parts1.size, parts2.size)
        
        for (i in 0 until maxLength) {
            val part1 = parts1.getOrNull(i) ?: 0
            val part2 = parts2.getOrNull(i) ?: 0
            
            when {
                part1 < part2 -> return -1
                part1 > part2 -> return 1
            }
        }
        
        return 0
    }
    
    /**
     * Formate une version pour l'affichage
     * @param version La version à formater
     * @return La version formatée
     */
    fun formatVersion(version: String): String {
        return "v$version"
    }
    
    /**
     * Lit et stocke la version du fichier JSON intégré
     * @param jsonContent Le contenu du fichier JSON
     */
    suspend fun readEmbeddedJsonVersion(jsonContent: String) {
        try {
            val jsonElement = kotlinx.serialization.json.Json.parseToJsonElement(jsonContent)
            if (jsonElement is kotlinx.serialization.json.JsonObject) {
                val version = jsonElement["version"]?.toString()?.removeSurrounding("\"")
                val timestamp = jsonElement["generatedAtEpochMs"]?.toString()?.toLongOrNull()

                if (version != null) {
                    _jsonVersion.value = version
                    if (timestamp != null) {
                        _jsonTimestamp.value = timestamp
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Récupère la version du JSON stockée localement
     * @return La version du JSON ou null
     */
    suspend fun getStoredJsonVersion(): String? {
        val storage = createPreferencesStorage()
        val version = storage.getString(KEY_JSON_VERSION, "")
        return if (version.isNotBlank()) version else null
    }

    /**
     * Récupère le timestamp du JSON stockée localement
     * @return Le timestamp du JSON ou null
     */
    suspend fun getStoredJsonTimestamp(): Long? {
        val storage = createPreferencesStorage()
        val timestamp = storage.getString(KEY_JSON_TIMESTAMP, "")
        return if (timestamp.isNotBlank()) timestamp.toLongOrNull() else null
    }

    /**
     * Vérifie si le JSON intégré est plus récent que celui déjà importé
     * @param jsonContent Le contenu du fichier JSON intégré
     * @return true si une mise à jour est nécessaire
     */
    suspend fun isJsonUpdateNeeded(jsonContent: String): Boolean {
        println("🔄 [VERSION] Début de isJsonUpdateNeeded")
        try {
            // Lire la version du JSON intégré
            val jsonElement = kotlinx.serialization.json.Json.parseToJsonElement(jsonContent)
            if (jsonElement is kotlinx.serialization.json.JsonObject) {
                val embeddedVersion = jsonElement["version"]?.toString()?.removeSurrounding("\"")
                val embeddedTimestamp = jsonElement["generatedAtEpochMs"]?.toString()?.toLongOrNull()

                println("🔄 [VERSION] Version intégrée: $embeddedVersion, Timestamp: $embeddedTimestamp")

                if (embeddedVersion != null) {
                    // Récupérer la version déjà importée
                    val storedVersion = getStoredJsonVersion()
                    val storedTimestamp = getStoredJsonTimestamp()

                    println("🔄 [VERSION] Version stockée: $storedVersion, Timestamp: $storedTimestamp")

                    // Si aucune version n'a été importée, une mise à jour est nécessaire
                    if (storedVersion == null) {
                        println("✅ [VERSION] Aucune version stockée, mise à jour nécessaire")
                        return true
                    }

                    // Comparer les versions
                    val versionComparison = compareVersions(embeddedVersion, storedVersion)
                    println("🔄 [VERSION] Comparaison des versions: $embeddedVersion vs $storedVersion = $versionComparison")

                    // Si les versions sont différentes
                    if (versionComparison > 0) {
                        println("✅ [VERSION] Version intégrée plus récente, mise à jour nécessaire")
                        return true
                    }

                    // Si les versions sont identiques, comparer les timestamps
                    if (versionComparison == 0 && embeddedTimestamp != null && storedTimestamp != null) {
                        val timestampComparison = embeddedTimestamp > storedTimestamp
                        println("🔄 [VERSION] Versions identiques, comparaison des timestamps: $timestampComparison")
                        return timestampComparison
                    }

                }
            }

            println("ℹ️ [VERSION] Aucune mise à jour nécessaire")
            return false
        } catch (e: Exception) {
            println("💥 [VERSION] Exception dans isJsonUpdateNeeded: ${e.message}")
            e.printStackTrace()
            // En cas d'erreur, considérer qu'une mise à jour est nécessaire
            return true
        }
    }

    /**
     * Met à jour la version du JSON après un import réussi
     * @param jsonContent Le contenu du fichier JSON importé
     */
    suspend fun updateJsonVersionAfterImport(jsonContent: String) {
        try {
            val jsonElement = kotlinx.serialization.json.Json.parseToJsonElement(jsonContent)
            if (jsonElement is kotlinx.serialization.json.JsonObject) {
                val version = jsonElement["version"]?.toString()?.removeSurrounding("\"")
                val timestamp = jsonElement["generatedAtEpochMs"]?.toString()?.toLongOrNull()

                if (version != null) {
                    val storage = createPreferencesStorage()
                    storage.saveString(KEY_JSON_VERSION, version)
                    _jsonVersion.value = version

                    if (timestamp != null) {
                        storage.saveString(KEY_JSON_TIMESTAMP, timestamp.toString())
                        _jsonTimestamp.value = timestamp
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Réinitialise les versions JSON stockées (pour les tests)
     */
    suspend fun resetJsonVersions() {
        val storage = createPreferencesStorage()
        storage.saveString(KEY_JSON_VERSION, "")
        storage.saveString(KEY_JSON_TIMESTAMP, "")
        _jsonVersion.value = null
        _jsonTimestamp.value = null
    }





    /**
     * Génère un message de mise à jour pour le JSON
     * @param currentJsonVersion Version actuelle du JSON importé
     * @param newJsonVersion Nouvelle version du JSON intégré
     * @return Message formaté
     */
    fun generateJsonUpdateMessage(currentJsonVersion: String?, newJsonVersion: String?): String {
        return "Une nouvelle version du fichier de données est disponible :\n" +
                "• Version actuelle : ${currentJsonVersion ?: "Aucune"} → ${formatVersion(newJsonVersion ?: "Inconnue")}\n" +
                "• Nouvelle version : ${formatVersion(newJsonVersion ?: "Inconnue")}\n" +
                "• Source : Fichier intégré à l'application"
    }

    /**
     * Génère un message de mise à jour
     * @param currentVersion Version actuelle
     * @param newVersion Nouvelle version disponible
     * @return Message formaté
     */
    fun generateUpdateMessage(currentVersion: String, newVersion: String): String {
        return "Une nouvelle version de la base de données est disponible :\n" +
                "• Version actuelle : ${formatVersion(currentVersion)}\n" +
                "• Nouvelle version : ${formatVersion(newVersion)}\n" +
                "• Dernière mise à jour : ${_lastUpdateDate.value ?: "Jamais"}"
    }
    
    /**
     * Vérifie si la base de données a déjà été initialisée
     * @return true si la base a été initialisée
     */
    suspend fun isDatabaseInitialized(): Boolean {
        val version = getStoredDatabaseVersion()
        return version != DEFAULT_VERSION
    }
    
    /**
     * Réinitialise la version de la base de données (utile pour les tests)
     */
    suspend fun resetDatabaseVersion() {
        try {
            val storage = createPreferencesStorage()
            storage.saveString(KEY_DB_VERSION, DEFAULT_VERSION)
            storage.saveString(KEY_DB_LAST_UPDATE, "")
            storage.saveString(KEY_DB_IMPORT_SOURCE, "")
            
            _currentVersion.value = DEFAULT_VERSION
            _lastUpdateDate.value = null
            _importSource.value = null
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Méthodes privées pour la persistance des données
    private suspend fun getStoredDatabaseVersion(): String {
        val storage = createPreferencesStorage()
        return storage.getString(KEY_DB_VERSION, DEFAULT_VERSION)
    }
    
    private suspend fun getStoredLastUpdateDate(): String? {
        val storage = createPreferencesStorage()
        val date = storage.getString(KEY_DB_LAST_UPDATE, "")
        return if (date.isNotBlank()) date else null
    }
    
    private suspend fun getStoredImportSource(): String? {
        val storage = createPreferencesStorage()
        val source = storage.getString(KEY_DB_IMPORT_SOURCE, "")
        return if (source.isNotBlank()) source else null
    }
    
    /**
     * Obtient la date actuelle au format simple
     */
    private fun getCurrentDate(): String {
        return try {
            // Format simple : YYYY-MM-DD
            val now = today()
            "${now.year}-" +
                    "${now.monthNumber.toString().padStart(2, '0')}-" +
                    "${now.dayOfMonth.toString().padStart(2, '0')}"
        } catch (e: Exception) {
            // Fallback très simple
            "Unknown"
        }
    }
}
