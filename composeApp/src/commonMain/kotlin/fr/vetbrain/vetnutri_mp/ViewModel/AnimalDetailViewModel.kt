package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Repository.AlimentRationRepository
import fr.vetbrain.vetnutri_mp.Repository.AlimentRepository
import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.ConsultationRepository
import fr.vetbrain.vetnutri_mp.Repository.RationRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class AnimalDetailViewModel(
        private val consultationRepository: ConsultationRepository,
        private val repository: AnimalRepository,
        private val rationRepository: RationRepository,
        private val alimentRationRepository: AlimentRationRepository,
        private val alimentRepository: AlimentRepository
) {
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

    private val _isEditing = MutableStateFlow(false)

    fun getRationsViewModel(): RationsViewModel {
        val rationsViewModel = RationsViewModel(rationRepository = rationRepository)
        rationsViewModel.setConsultation(_selectedConsultation.value?.uuid)
        return rationsViewModel
    }

    private fun ensureConsultationSelected() {
        val consultations = _animal.value?.consultations ?: return
        if (consultations.isEmpty()) return

        if (_selectedConsultation.value == null) {
            // Sélectionner la consultation la plus récente
            consultations.maxByOrNull { it.date ?: LocalDate(1970, 1, 1) }?.let {
                _selectedConsultation.value = it
            }
        } else {
            // Vérifier si la consultation sélectionnée existe toujours dans la liste
            val currentSelected = _selectedConsultation.value
            if (currentSelected != null && !consultations.any { it.uuid == currentSelected.uuid }) {
                // Si la consultation sélectionnée n'existe plus, sélectionner la plus récente
                consultations.maxByOrNull { it.date ?: LocalDate(1970, 1, 1) }?.let {
                    _selectedConsultation.value = it
                }
            }
        }
    }

    private fun sortConsultations(consultations: List<ConsultationEv>): List<ConsultationEv> {
        return consultations.sortedByDescending { it.date ?: LocalDate(1970, 1, 1) }
    }

    fun setAnimal(animal: AnimalEv) {
        _animal.value = animal.copy()
        // Charger les consultations depuis la base de données
        viewModelScope.launch {
            var consultations = consultationRepository.getConsultationsForAnimal(animal.uuid)

            // Créer une consultation initiale si nécessaire
            if (consultations.isEmpty()) {
                val currentMoment = Clock.System.now()
                val localDateTime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
                val currentDate = localDateTime.date
                val newConsultation =
                        ConsultationEv(
                                idAnim = animal.uuid,
                                date = currentDate,
                                objectConsult = "Consultation initiale"
                        )
                consultationRepository.saveConsultation(newConsultation)
                consultations = listOf(newConsultation)
            }

            // Trier les consultations avant de les assigner
            val sortedConsultations = sortConsultations(consultations)
            _animal.update { currentAnimal ->
                currentAnimal?.copy(consultations = sortedConsultations.toMutableList())
            }

            // Sélectionner la consultation la plus récente
            sortedConsultations.firstOrNull()?.let { consultation ->
                selectConsultation(consultation)
                // Mettre à jour le RationsViewModel
                getRationsViewModel().setConsultation(consultation.uuid)
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

            // Créer une ration par défaut pour la nouvelle consultation
            val defaultRation =
                    Ration(
                            idConsult = consultation.uuid,
                            name = "Ration initiale",
                            coef = 1.0f,
                            actual = true,
                            description = "Ration créée automatiquement avec la consultation"
                    )
            consultation.rations.add(defaultRation)

            // Ajouter d'abord localement pour une mise à jour immédiate de l'UI
            val currentConsultations =
                    _animal.value?.consultations?.toMutableList() ?: mutableListOf()
            currentConsultations.add(consultation)

            // Trier les consultations avant de les assigner
            val sortedConsultations = sortConsultations(currentConsultations)
            _animal.update { currentAnimal ->
                currentAnimal?.copy(consultations = sortedConsultations.toMutableList())
            }

            // Sélectionner la nouvelle consultation
            _selectedConsultation.value = consultation

            // Puis sauvegarder dans la base de données
            consultationRepository.saveConsultation(consultation)
            // Sauvegarder la ration par défaut
            rationRepository.saveRation(defaultRation)

            // Mettre à jour le RationsViewModel si nécessaire
            getRationsViewModel().setConsultation(consultation.uuid)
        }
    }

    fun updateConsultation(consultation: ConsultationEv) {
        viewModelScope.launch {
            // Mettre à jour d'abord localement pour une mise à jour immédiate de l'UI
            val currentConsultations =
                    _animal.value?.consultations?.toMutableList() ?: return@launch
            val index = currentConsultations.indexOfFirst { it.uuid == consultation.uuid }
            if (index != -1) {
                currentConsultations[index] = consultation

                // Trier les consultations avant de les assigner
                val sortedConsultations = sortConsultations(currentConsultations)
                _animal.update { currentAnimal ->
                    currentAnimal?.copy(consultations = sortedConsultations.toMutableList())
                }

                // Mettre à jour la consultation sélectionnée si nécessaire
                if (_selectedConsultation.value?.uuid == consultation.uuid) {
                    _selectedConsultation.value = consultation
                }
            }

            // S'assurer qu'une consultation est sélectionnée
            ensureConsultationSelected()

            // Puis sauvegarder dans la base de données
            consultationRepository.saveConsultation(consultation)
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
            // Vérifier qu'il y a plus d'une consultation avant de supprimer
            val currentConsultations =
                    _animal.value?.consultations?.toMutableList() ?: return@launch
            if (currentConsultations.size <= 1) {
                return@launch
            }

            // Supprimer d'abord localement pour une mise à jour immédiate de l'UI
            val updatedConsultations = currentConsultations.filter { it.uuid != consultation.uuid }

            // Trier les consultations avant de les assigner
            val sortedConsultations = sortConsultations(updatedConsultations)
            _animal.value = _animal.value?.copy(consultations = sortedConsultations.toMutableList())

            // S'assurer qu'une consultation est sélectionnée
            ensureConsultationSelected()

            // Puis supprimer dans la base de données
            consultationRepository.deleteConsultation(consultation)
        }
    }

    fun prepareNewConsultation(date: LocalDate) {
        val newConsultation =
                ConsultationEv(idAnim = _animal.value?.uuid ?: "", date = date, objectConsult = "")
        // Ajouter directement la consultation
        addConsultation(newConsultation)
        _selectedConsultation.value = newConsultation
    }

    fun updateAnimal(updatedAnimal: AnimalEv) {
        _animal.update { updatedAnimal }
        viewModelScope.launch {
            // TODO: Sauvegarder les modifications dans la base de données
            // Pour l'instant, nous mettons simplement à jour l'état local
        }
    }

    suspend fun saveAll() {
        // Sauvegarde l'animal actuel
        _animal.value?.let { animal -> updateAnimal(animal) }

        // Sauvegarde toutes les consultations
        _animal.value?.consultations?.forEach { consultation ->
            consultationRepository.saveConsultation(consultation)
        }
    }

    fun addRation(ration: Ration) {
        viewModelScope.launch {
            _animal.value?.let { animal ->
                // Ajouter la ration à la liste
                val updatedRations = animal.rations.toMutableList().apply { add(ration) }

                // Mettre à jour l'animal
                _animal.value = animal.copy(rations = updatedRations)

                // Sauvegarder la ration dans la base de données
                rationRepository.saveRation(ration)
            }
        }
    }

    fun updateRation(ration: Ration) {
        viewModelScope.launch {
            _animal.value?.let { animal ->
                // Trouver et mettre à jour la ration dans la liste
                val updatedRations =
                        animal.rations.toMutableList().apply {
                            val index = indexOfFirst { it.uuid == ration.uuid }
                            if (index != -1) {
                                this[index] = ration
                            }
                        }

                // Mettre à jour l'animal
                _animal.value = animal.copy(rations = updatedRations)

                // Sauvegarder la ration dans la base de données
                rationRepository.saveRation(ration)
            }
        }
    }

    fun deleteRation(ration: Ration) {
        viewModelScope.launch {
            _animal.value?.let { animal ->
                // Supprimer la ration de la liste
                val updatedRations =
                        animal.rations.toMutableList().apply { removeIf { it.uuid == ration.uuid } }

                // Mettre à jour l'animal
                _animal.value = animal.copy(rations = updatedRations)

                // Supprimer la ration de la base de données
                rationRepository.deleteRation(ration)
            }
        }
    }
}
