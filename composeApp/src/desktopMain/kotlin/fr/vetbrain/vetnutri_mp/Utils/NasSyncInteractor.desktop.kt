package fr.vetbrain.vetnutri_mp.Utils

import fr.vetbrain.vetnutri_mp.Service.NasSyncService
import fr.vetbrain.vetnutri_mp.desktopAppDatabase

actual suspend fun syncNasDatabase(): NasSyncStatus {
    val db = desktopAppDatabase ?: return NasSyncStatus.Error("Base non initialisée")
    return when (val result = NasSyncService.syncIfConfigured(db)) {
        is NasSyncService.SyncResult.Ok ->
            NasSyncStatus.Ok(result.syncedAt, result.pushed, result.pulled)
        is NasSyncService.SyncResult.NasVersionTooHigh ->
            NasSyncStatus.Error("Version NAS incompatible (v${result.nasVersion})")
        is NasSyncService.SyncResult.NasIncompatible ->
            NasSyncStatus.Error(result.reason)
        NasSyncService.SyncResult.NasNotConfigured ->
            NasSyncStatus.NotConfigured
        is NasSyncService.SyncResult.Error ->
            NasSyncStatus.Error(result.exception.message ?: "Erreur inconnue")
    }
}
