package fr.vetbrain.vetnutri_mp.Utils

sealed class NasSyncStatus {
    object Idle : NasSyncStatus()
    object InProgress : NasSyncStatus()
    object NotConfigured : NasSyncStatus()
    data class Ok(val syncedAt: Long, val pushed: Int, val pulled: Int) : NasSyncStatus()
    data class Error(val message: String) : NasSyncStatus()
}

expect suspend fun syncNasDatabase(): NasSyncStatus
