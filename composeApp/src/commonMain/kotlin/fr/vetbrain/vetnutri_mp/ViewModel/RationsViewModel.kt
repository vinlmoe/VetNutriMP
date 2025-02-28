package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Repository.AlimentRationRepository
import fr.vetbrain.vetnutri_mp.Repository.AlimentRepository
import fr.vetbrain.vetnutri_mp.Repository.RationRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RationsViewModel(
        private val rationRepository: RationRepository,
        private val alimentRationRepository: AlimentRationRepository,
        private val alimentRepository: AlimentRepository
) {
    private val viewModelScope = CoroutineScope(AppDispatchers.Main)
    private val _rations = MutableStateFlow<List<Ration>>(emptyList())
    val rations: StateFlow<List<Ration>> = _rations.asStateFlow()

    private val _selectedRation = MutableStateFlow<Ration?>(null)
    val selectedRation: StateFlow<Ration?> = _selectedRation.asStateFlow()

    private val _alimentRations = MutableStateFlow<List<AlimentRation>>(emptyList())
    val alimentRations: StateFlow<List<AlimentRation>> = _alimentRations.asStateFlow()

    private val _aliments = MutableStateFlow<List<AlimentEv>>(emptyList())
    val aliments: StateFlow<List<AlimentEv>> = _aliments.asStateFlow()

    private var currentConsultationId: String? = null

    init {
        viewModelScope.launch {
            loadRations()
            loadAliments()
        }
    }

    fun setConsultation(consultationId: String?) {
        currentConsultationId = consultationId
        viewModelScope.launch { loadRations() }
    }

    private suspend fun loadRations() {
        currentConsultationId?.let { consultId ->
            val loadedRations = rationRepository.getRationsForConsultation(consultId)
            _rations.value = loadedRations
            if (loadedRations.isNotEmpty() &&
                            (_selectedRation.value == null ||
                                    _selectedRation.value?.idConsult != consultId)
            ) {
                selectRation(loadedRations.first())
            }
        }
                ?: run { _rations.value = rationRepository.getAllRations() }
    }

    private suspend fun loadAliments() {
        _aliments.value = alimentRepository.getAllAliments()
    }

    private suspend fun loadAlimentRations(rationId: String) {
        _alimentRations.value = alimentRationRepository.getAlimentRationsForRation(rationId)
    }

    fun selectRation(ration: Ration) {
        if (ration.idConsult == currentConsultationId) {
            _selectedRation.value = ration
            viewModelScope.launch { loadAlimentRations(ration.uuid) }
        }
    }

    fun addRation(ration: Ration) {
        val consultId = currentConsultationId
        if (consultId == null) {
            return
        }
        viewModelScope.launch {
            val rationToSave = ration.copy(idConsult = consultId)
            rationRepository.saveRation(rationToSave)
            loadRations()
        }
    }

    fun updateRation(ration: Ration) {
        viewModelScope.launch {
            val rationToSave = ration.copy(idConsult = currentConsultationId ?: ration.idConsult)
            rationRepository.saveRation(rationToSave)
            loadRations()
        }
    }

    fun deleteRation(ration: Ration) {
        if (_rations.value.count { it.idConsult == currentConsultationId } <= 1) {
            return
        }
        viewModelScope.launch {
            rationRepository.deleteRation(ration)
            loadRations()
        }
    }

    fun addAlimentRation(alimentRation: AlimentRation) {
        viewModelScope.launch {
            alimentRationRepository.saveAlimentRation(alimentRation)
            _selectedRation.value?.let { loadAlimentRations(it.uuid) }
        }
    }

    fun updateAlimentRation(alimentRation: AlimentRation) {
        viewModelScope.launch {
            alimentRationRepository.saveAlimentRation(alimentRation)
            _selectedRation.value?.let { loadAlimentRations(it.uuid) }
        }
    }

    fun deleteAlimentRation(alimentRation: AlimentRation) {
        viewModelScope.launch {
            alimentRationRepository.deleteAlimentRation(alimentRation)
            _selectedRation.value?.let { loadAlimentRations(it.uuid) }
        }
    }
}
