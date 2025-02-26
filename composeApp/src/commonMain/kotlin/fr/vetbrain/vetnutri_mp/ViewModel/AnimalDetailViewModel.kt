package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Repository.ConsultationRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class AnimalDetailViewModel(private val consultationRepository: ConsultationRepository) {
    private val viewModelScope = CoroutineScope(AppDispatchers.Main)
    private val _animal = MutableStateFlow<AnimalEv?>(null)
    val animal: StateFlow<AnimalEv?> = _animal.asStateFlow()

    private val _selectedConsultation = MutableStateFlow<ConsultationEv?>(null)
    val selectedConsultation: StateFlow<ConsultationEv?> = _selectedConsultation.asStateFlow()

    private val _selectedRation = MutableStateFlow<Ration?>(null)
    val selectedRation: StateFlow<Ration?> = _selectedRation.asStateFlow()

    var isEditingConsultation by mutableStateOf(false)
        private set

    var isEditingRation by mutableStateOf(false)
        private set

    fun setAnimal(animal: AnimalEv) {
        _animal.value = animal.copy()
        // Charger les consultations depuis la base de données
        viewModelScope.launch {
            val consultations = consultationRepository.getConsultationsForAnimal(animal.uuid)
            _animal.update { currentAnimal ->
                currentAnimal?.copy(consultations = consultations.toMutableList())
            }
        }
    }

    fun selectConsultation(consultation: ConsultationEv) {
        viewModelScope.launch {
            val fullConsultation = consultationRepository.getConsultationById(consultation.uuid)
            _selectedConsultation.value = fullConsultation
        }
    }

    fun selectRation(ration: Ration) {
        _selectedRation.value = ration.copy()
    }

    fun startEditingConsultation() {
        isEditingConsultation = true
    }

    fun stopEditingConsultation() {
        isEditingConsultation = false
        _selectedConsultation.value = null
    }

    fun startEditingRation() {
        isEditingRation = true
    }

    fun stopEditingRation() {
        isEditingRation = false
        _selectedRation.value = null
    }

    fun addConsultation(consultation: ConsultationEv) {
        viewModelScope.launch {
            consultation.idAnim = _animal.value?.uuid ?: return@launch
            consultationRepository.saveConsultation(consultation)

            // Rafraîchir les consultations depuis la base de données au lieu de mettre à jour
            // manuellement
            val updatedConsultations =
                    consultationRepository.getConsultationsForAnimal(consultation.idAnim)
            _animal.update { currentAnimal ->
                currentAnimal?.copy(consultations = updatedConsultations.toMutableList())
            }

            // Sélectionner la nouvelle consultation
            _selectedConsultation.value = consultation
        }
    }

    fun updateConsultation(consultation: ConsultationEv) {
        viewModelScope.launch {
            consultationRepository.saveConsultation(consultation)

            // Rafraîchir les consultations depuis la base de données au lieu de mettre à jour
            // manuellement
            val updatedConsultations =
                    consultationRepository.getConsultationsForAnimal(consultation.idAnim)
            _animal.update { currentAnimal ->
                currentAnimal?.copy(consultations = updatedConsultations.toMutableList())
            }
        }
    }

    fun addRationToConsultation(ration: Ration) {
        val consultation = _selectedConsultation.value?.copy() ?: return
        val updatedRations = consultation.rations.toMutableList()
        updatedRations.add(ration)
        val updatedConsultation = consultation.copy(rations = updatedRations)
        _selectedConsultation.value = updatedConsultation
        updateConsultation(updatedConsultation)
    }

    fun updateRationInConsultation(ration: Ration) {
        val consultation = _selectedConsultation.value?.copy() ?: return
        val updatedRations = consultation.rations.toMutableList()
        val index = updatedRations.indexOfFirst { it.uuid == ration.uuid }
        if (index >= 0) {
            updatedRations[index] = ration
            val updatedConsultation = consultation.copy(rations = updatedRations)
            _selectedConsultation.value = updatedConsultation
            updateConsultation(updatedConsultation)
        }
    }

    fun removeRationFromConsultation(ration: Ration) {
        val consultation = _selectedConsultation.value?.copy() ?: return
        val updatedRations = consultation.rations.toMutableList()
        updatedRations.removeAll { it.uuid == ration.uuid }
        val updatedConsultation = consultation.copy(rations = updatedRations)
        _selectedConsultation.value = updatedConsultation
        updateConsultation(updatedConsultation)
    }

    fun deleteConsultation(consultation: ConsultationEv) {
        viewModelScope.launch {
            consultationRepository.deleteConsultation(consultation)

            // Rafraîchir les consultations depuis la base de données au lieu de mettre à jour
            // manuellement
            val updatedConsultations =
                    consultationRepository.getConsultationsForAnimal(consultation.idAnim)
            _animal.update { currentAnimal ->
                currentAnimal?.copy(consultations = updatedConsultations.toMutableList())
            }

            // Si la consultation supprimée était sélectionnée, la désélectionner
            if (_selectedConsultation.value?.uuid == consultation.uuid) {
                _selectedConsultation.value = null
            }
        }
    }

    fun prepareNewConsultation(date: LocalDate) {
        _selectedConsultation.value =
                ConsultationEv(idAnim = _animal.value?.uuid ?: "", date = date)
    }
}
