package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestionnaire des versions de la base de données
 * Sauvegarde la version actuelle et propose des mises à jour
 */
class DatabaseVersionManager {
    
    companion object {
        private const val KEY_DB_VERSION = "database_version"
        private const val KEY_DB_LAST_UPDATE = "database_last_update"
        private const val KEY_DB_IMPORT_SOURCE = "database_import_source"
        
        // Version par défaut si aucune n'est définie
        const val DEFAULT_VERSION = "1.0.0"
    }
    
    private val _currentVersion = MutableStateFlow(DEFAULT_VERSION)
    val currentVersion: StateFlow<String> = _currentVersion.asStateFlow()
    
    private val _lastUpdateDate = MutableStateFlow<String?>(null)
    val lastUpdateDate: StateFlow<String?> = _lastUpdateDate.asStateFlow()
    
    private val _importSource = MutableStateFlow<String?>(null)
    val importSource: StateFlow<String?> = _importSource.asStateFlow()
    
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
    private fun compareVersions(version1: String, version2: String): Int {
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
            val now = java.util.Date()
            val calendar = java.util.Calendar.getInstance()
            calendar.time = now
            "${calendar.get(java.util.Calendar.YEAR)}-" +
                    "${(calendar.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')}-" +
                    "${calendar.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2, '0')}"
        } catch (e: Exception) {
            // Fallback très simple
            "Unknown"
        }
    }
}
