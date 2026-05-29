package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vetbrain.vetnutri_mp.Data.SyncConfig
import fr.vetbrain.vetnutri_mp.Data.SyncManifest
import fr.vetbrain.vetnutri_mp.Data.SyncResult
import fr.vetbrain.vetnutri_mp.Service.SyncService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SyncViewModel(private val syncService: SyncService) : ViewModel() {

    private val _syncResult    = MutableStateFlow<SyncResult?>(null)
    val syncResult: StateFlow<SyncResult?> = _syncResult.asStateFlow()

    private val _syncConfig    = MutableStateFlow<SyncConfig?>(null)
    val syncConfig: StateFlow<SyncConfig?> = _syncConfig.asStateFlow()

    private val _remoteManifest = MutableStateFlow<SyncManifest?>(null)
    val remoteManifest: StateFlow<SyncManifest?> = _remoteManifest.asStateFlow()

    private val _isLoading     = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadConfig() {
        viewModelScope.launch {
            _syncConfig.value = syncService.getOrCreateConfig()
        }
    }

    fun saveDeviceName(name: String) {
        viewModelScope.launch {
            syncService.saveDeviceName(name)
            _syncConfig.value = syncService.getOrCreateConfig()
        }
    }

    fun push() {
        viewModelScope.launch {
            _isLoading.value = true
            _syncResult.value = syncService.push()
            _syncConfig.value = syncService.getOrCreateConfig()
            _isLoading.value = false
        }
    }

    fun pull(forceOverwrite: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _syncResult.value = syncService.pull(forceOverwrite)
            _syncConfig.value = syncService.getOrCreateConfig()
            _isLoading.value = false
        }
    }

    fun checkRemoteManifest() {
        viewModelScope.launch {
            _isLoading.value = true
            _remoteManifest.value = syncService.getRemoteManifest()
            _isLoading.value = false
        }
    }

    fun clearResult() {
        _syncResult.value = null
    }
}
