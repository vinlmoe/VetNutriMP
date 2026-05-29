package fr.vetbrain.vetnutri_mp.Service

import fr.vetbrain.vetnutri_mp.Data.SyncConfig
import fr.vetbrain.vetnutri_mp.Data.SyncEnvelope
import fr.vetbrain.vetnutri_mp.Data.SyncManifest
import fr.vetbrain.vetnutri_mp.Data.SyncResult
import fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.PreferencesStorage
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class SyncService(
    private val supabase: SupabaseClient,
    private val exportImport: ExportImportRepository,
    private val prefs: PreferencesStorage
) {
    companion object {
        const val DB_SCHEMA_VERSION = 36
        const val BUCKET = "vetnutri-sync"
        private const val KEY_DEVICE_ID   = "sync_device_id"
        private const val KEY_DEVICE_NAME = "sync_device_name"
        private const val KEY_LAST_PUSH   = "sync_last_push_ms"
        private const val KEY_LAST_PULL   = "sync_last_pull_ms"
    }

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    suspend fun getOrCreateConfig(): SyncConfig {
        var deviceId = prefs.getString(KEY_DEVICE_ID)
        if (deviceId.isBlank()) {
            deviceId = genUUID()
            prefs.saveString(KEY_DEVICE_ID, deviceId)
        }
        val deviceName = prefs.getString(KEY_DEVICE_NAME)
        val lastPushMs = prefs.getString(KEY_LAST_PUSH, "0").toLongOrNull() ?: 0L
        val lastPullMs = prefs.getString(KEY_LAST_PULL, "0").toLongOrNull() ?: 0L
        return SyncConfig(deviceId, deviceName, lastPushMs, lastPullMs)
    }

    suspend fun saveDeviceName(name: String) {
        prefs.saveString(KEY_DEVICE_NAME, name)
    }

    suspend fun push(): SyncResult {
        val userId = supabase.auth.currentSessionOrNull()?.user?.id
            ?: return SyncResult.Error("Non connecté — veuillez vous authentifier")
        val config = getOrCreateConfig()

        val apiEnvelope = try {
            withContext(AppDispatchers.IO) { exportImport.exportAllEnvelope() }
        } catch (e: Exception) {
            return SyncResult.Error("Export échoué : ${e.message}")
        }

        val now = Clock.System.now().toEpochMilliseconds()
        val envelope = SyncEnvelope(
            deviceId        = config.deviceId,
            deviceName      = config.deviceName.ifBlank { "Appareil" },
            pushedAtMs      = now,
            dbSchemaVersion = DB_SCHEMA_VERSION,
            data            = apiEnvelope
        )
        val manifest = SyncManifest(
            deviceId        = config.deviceId,
            deviceName      = envelope.deviceName,
            pushedAtMs      = now,
            dbSchemaVersion = DB_SCHEMA_VERSION
        )

        return try {
            val bucket = supabase.storage.from(BUCKET)
            bucket.upload("$userId/manifest.json", json.encodeToString(manifest).encodeToByteArray()) {
                upsert = true
            }
            bucket.upload("$userId/latest.json", json.encodeToString(envelope).encodeToByteArray()) {
                upsert = true
            }
            prefs.saveString(KEY_LAST_PUSH, now.toString())
            SyncResult.PushSuccess(now)
        } catch (e: Exception) {
            SyncResult.Error("Upload échoué : ${e.message}")
        }
    }

    suspend fun pull(forceOverwrite: Boolean = false): SyncResult {
        val userId = supabase.auth.currentSessionOrNull()?.user?.id
            ?: return SyncResult.Error("Non connecté — veuillez vous authentifier")
        val config = getOrCreateConfig()

        val manifest = getRemoteManifest(userId)
            ?: return SyncResult.Error("Aucune donnée dans le cloud pour ce compte")

        if (manifest.dbSchemaVersion != DB_SCHEMA_VERSION) {
            return SyncResult.SchemaIncompatible(DB_SCHEMA_VERSION, manifest.dbSchemaVersion)
        }
        if (manifest.pushedAtMs <= config.lastPullMs) {
            return SyncResult.AlreadyUpToDate
        }
        if (!forceOverwrite
            && config.lastPushMs > config.lastPullMs
            && manifest.deviceId != config.deviceId
        ) {
            return SyncResult.ConflictDetected(
                localLastPushMs   = config.lastPushMs,
                remoteLastPushMs  = manifest.pushedAtMs,
                remoteDeviceName  = manifest.deviceName
            )
        }

        val envelopeJson = try {
            supabase.storage.from(BUCKET)
                .downloadAuthenticated("$userId/latest.json")
                .decodeToString()
        } catch (e: Exception) {
            return SyncResult.Error("Téléchargement échoué : ${e.message}")
        }

        val counts = try {
            val syncEnvelope = json.decodeFromString<SyncEnvelope>(envelopeJson)
            val apiJson = json.encodeToString(syncEnvelope.data)
            withContext(AppDispatchers.IO) { exportImport.importAll(apiJson) }
        } catch (e: Exception) {
            return SyncResult.Error("Import échoué : ${e.message}")
        }

        prefs.saveString(KEY_LAST_PULL, Clock.System.now().toEpochMilliseconds().toString())
        return SyncResult.PullSuccess(manifest.deviceName, counts.animals, counts.foods)
    }

    suspend fun getRemoteManifest(userId: String? = null): SyncManifest? {
        val uid = userId ?: supabase.auth.currentSessionOrNull()?.user?.id ?: return null
        return try {
            val bytes = supabase.storage.from(BUCKET).downloadAuthenticated("$uid/manifest.json")
            json.decodeFromString<SyncManifest>(bytes.decodeToString())
        } catch (_: Exception) {
            null
        }
    }
}
