package fr.vetbrain.vetnutri_mp.Utils

import java.io.File
import java.util.Properties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Implémentation Desktop du stockage des préférences utilisant un fichier Properties */
actual class PreferencesStorage {

    private val preferencesFile =
            File(System.getProperty("user.home"), ".vetnutri_preferences.properties")
    private val properties = Properties()

    init {
        loadProperties()
    }

    private fun loadProperties() {
        try {
            if (preferencesFile.exists()) {
                preferencesFile.inputStream().use { input -> properties.load(input) }
            }
        } catch (e: Exception) {
        }
    }

    private suspend fun saveProperties() {
        withContext(Dispatchers.IO) {
            try {
                preferencesFile.parentFile?.mkdirs()
                preferencesFile.outputStream().use { output ->
                    properties.store(output, "VetNutri Preferences")
                }
            } catch (e: Exception) {
            }
        }
    }

    actual suspend fun saveString(key: String, value: String) {
        properties.setProperty(key, value)
        saveProperties()
    }

    actual suspend fun getString(key: String, defaultValue: String): String {
        return properties.getProperty(key, defaultValue)
    }

    actual suspend fun remove(key: String) {
        properties.remove(key)
        saveProperties()
    }

    actual suspend fun contains(key: String): Boolean {
        return properties.containsKey(key)
    }

    actual suspend fun clear() {
        properties.clear()
        saveProperties()
    }
}

/** Fonction helper pour créer une instance de PreferencesStorage sur Desktop */
actual fun createPreferencesStorage(): PreferencesStorage {
    println("🔄 [Desktop] Création de PreferencesStorage")
    try {
        val storage = PreferencesStorage()
        println("✅ [Desktop] PreferencesStorage créé avec succès")
        return storage
    } catch (e: Exception) {
        println("💥 [Desktop] Exception lors de la création de PreferencesStorage: ${e.message}")
        e.printStackTrace()
        throw e
    }
}
 