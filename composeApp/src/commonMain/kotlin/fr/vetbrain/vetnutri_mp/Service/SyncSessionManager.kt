package fr.vetbrain.vetnutri_mp.Service

import fr.vetbrain.vetnutri_mp.Utils.PreferencesStorage
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/** Persiste la session Supabase dans PreferencesStorage pour survie au redémarrage. */
class SyncSessionManager(private val prefs: PreferencesStorage) : SessionManager {

    private val json = Json { ignoreUnknownKeys = true }
    private val KEY = "sync_supabase_session"

    override suspend fun saveSession(session: UserSession) {
        prefs.saveString(KEY, json.encodeToString(session))
    }

    override suspend fun loadSession(): UserSession? {
        val raw = prefs.getString(KEY)
        if (raw.isBlank()) return null
        return try { json.decodeFromString(raw) } catch (_: Exception) { null }
    }

    override suspend fun deleteSession() {
        prefs.remove(KEY)
    }
}
