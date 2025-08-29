package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSUserDefaults

/** Implémentation iOS du stockage des préférences utilisant NSUserDefaults */
actual class PreferencesStorage {

    private val userDefaults = NSUserDefaults.standardUserDefaults

    actual suspend fun saveString(key: String, value: String) {
        println("🔄 [iOS] Sauvegarde de la clé '$key' avec la valeur '$value'")
        withContext(Dispatchers.Main) {
            try {
                userDefaults.setObject(value, key)
                userDefaults.synchronize()
                println("✅ [iOS] Clé '$key' sauvegardée avec succès")
            } catch (e: Exception) {
                println("💥 [iOS] Exception lors de la sauvegarde de '$key': ${e.message}")
                throw e
            }
        }
    }

    actual suspend fun getString(key: String, defaultValue: String): String {
        println("🔄 [iOS] Lecture de la clé '$key' avec valeur par défaut '$defaultValue'")
        return withContext(Dispatchers.Main) { 
            try {
                val value = userDefaults.stringForKey(key) ?: defaultValue
                println("✅ [iOS] Clé '$key' lue avec succès: '$value'")
                value
            } catch (e: Exception) {
                println("💥 [iOS] Exception lors de la lecture de '$key': ${e.message}")
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
