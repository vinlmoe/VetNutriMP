package fr.vetbrain.vetnutri_mp.Data

import kotlinx.serialization.Serializable

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val userId: String, val email: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

@Serializable
data class SyncConfig(
    val deviceId: String,
    val deviceName: String = "",
    val lastPushMs: Long = 0L,
    val lastPullMs: Long = 0L
)

@Serializable
data class SyncManifest(
    val deviceId: String,
    val deviceName: String,
    val pushedAtMs: Long,
    val dbSchemaVersion: Int
)

@Serializable
data class SyncEnvelope(
    val syncVersion: Int = 1,
    val deviceId: String,
    val deviceName: String,
    val pushedAtMs: Long,
    val dbSchemaVersion: Int,
    val data: ApiEnvelope
)

sealed class SyncResult {
    data class PushSuccess(val pushedAtMs: Long) : SyncResult()
    data class PullSuccess(
        val fromDevice: String,
        val animalsImported: Int,
        val foodsImported: Int
    ) : SyncResult()
    data class ConflictDetected(
        val localLastPushMs: Long,
        val remoteLastPushMs: Long,
        val remoteDeviceName: String
    ) : SyncResult()
    data class SchemaIncompatible(val local: Int, val remote: Int) : SyncResult()
    object AlreadyUpToDate : SyncResult()
    data class Error(val message: String) : SyncResult()
}
