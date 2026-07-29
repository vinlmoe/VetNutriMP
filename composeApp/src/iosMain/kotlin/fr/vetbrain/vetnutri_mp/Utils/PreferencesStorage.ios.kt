package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSUserDefaults

/** Implémentation iOS du stockage des préférences utilisant NSUserDefaults */
actual class PreferencesStorage {

    private val userDefaults = NSUserDefaults.standardUserDefaults

    actual suspend fun saveString(key: String, value: String) {
        withContext(Dispatchers.Main) {
            try {
                userDefaults.setObject(value, key)
                userDefaults.synchronize()
            } catch (e: Exception) {
                throw e
            }
        }
    }

    actual suspend fun getString(key: String, defaultValue: String): String {
        return withContext(Dispatchers.Main) { 
            try {
                val value = userDefaults.stringForKey(key) ?: defaultValue
                value
            } catch (e: Exception) {
                defaultValue
            }
        }
    }

    actual suspend fun remove(key: String) {
        withContext(Dispatchers.Main) {
            userDefaults.removeObjectForKey(key)
            userDefaults.synchronize()
        }
    }

    actual suspend fun contains(key: String): Boolean {
        return withContext(Dispatchers.Main) { userDefaults.objectForKey(key) != null }
    }

    actual suspend fun clear() {
        withContext(Dispatchers.Main) {
            val userDefaults = NSUserDefaults.standardUserDefaults
            for (key in getAllKeys()) {
                userDefaults.removeObjectForKey(key)
            }
        }
    }

    private fun getAllKeys(): List<String> {
        // Retourne toutes les clés commençant par le préfixe de l'app
        val userDefaults = NSUserDefaults.standardUserDefaults
        val dictionary = userDefaults.dictionaryRepresentation()
        return dictionary.keys.filterIsInstance<String>().filter { it.startsWith("vetnutri_") }
    }
}

/** Fonction helper pour créer une instance de PreferencesStorage sur iOS */
actual fun createPreferencesStorage(): PreferencesStorage {
    return PreferencesStorage()
}
