package fr.vetbrain.vetnutri_mp.Utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Implémentation Android du stockage des préférences utilisant SharedPreferences */
actual class PreferencesStorage(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "vetnutri_preferences"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    actual suspend fun saveString(key: String, value: String) {
        withContext(Dispatchers.IO) { sharedPreferences.edit().putString(key, value).apply() }
    }

    actual suspend fun getString(key: String, defaultValue: String): String {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getString(key, defaultValue) ?: defaultValue
        }
    }

    actual suspend fun remove(key: String) {
        withContext(Dispatchers.IO) { sharedPreferences.edit().remove(key).apply() }
    }

    actual suspend fun contains(key: String): Boolean {
        return withContext(Dispatchers.IO) { sharedPreferences.contains(key) }
    }

    actual suspend fun clear() {
        withContext(Dispatchers.IO) { sharedPreferences.edit().clear().apply() }
    }
}

/** Fonction helper pour créer une instance de PreferencesStorage sur Android */
actual fun createPreferencesStorage(): PreferencesStorage {
    println("🔄 [Android] Création de PreferencesStorage")
    try {
        val context =
                fr.vetbrain.vetnutri_mp.Localization.AndroidContext.appContext
                        ?: throw IllegalStateException("AndroidContext.appContext n'est pas initialisé")
        println("✅ [Android] AndroidContext.appContext récupéré avec succès")
        val storage = PreferencesStorage(context)
        println("✅ [Android] PreferencesStorage créé avec succès")
        return storage
    } catch (e: Exception) {
        println("💥 [Android] Exception lors de la création de PreferencesStorage: ${e.message}")
        e.printStackTrace()
        throw e
    }
}
