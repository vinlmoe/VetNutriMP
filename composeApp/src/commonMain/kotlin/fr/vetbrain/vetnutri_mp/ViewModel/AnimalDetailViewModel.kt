package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.ConsultationRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/** Énumération des différentes sections de la vue détaillée d'un animal */
enum class AnimalDetailSection {
    IDENTIFICATION, // Informations d'identification de l'animal
    CONSULTATIONS, // Liste des consultations
    RATIONS // Vue des rations
}

class AnimalDetailViewModel(
        private val consultationRepository: ConsultationRepository,
        private val animalRepository: AnimalRepository
) {
    private val viewModelScope = CoroutineScope(AppDispatchers.Main)
    private val _animal = MutableStateFlow<AnimalEv?>(null)
    val animal: StateFlow<AnimalEv?> = _animal.asStateFlow()

    private val _selectedConsultation = MutableStateFlow<ConsultationEv?>(null)
    val selectedConsultation: StateFlow<ConsultationEv?> = _selectedConsultation.asStateFlow()

    private val _selectedRation = MutableStateFlow<Ration?>(null)
    val selectedRation: StateFlow<Ration?> = _selectedRation.asStateFlow()

    // Section actuellement sélectionnée dans la vue détaillée
    private val _currentSection = MutableStateFlow(AnimalDetailSection.IDENTIFICATION)
    val currentSection: StateFlow<AnimalDetailSection> = _currentSection.asStateFlow()

    var isEditingConsultation by mutableStateOf(false)
        private set

    var isEditingRation by mutableStateOf(false)
        private set

    var isEditingAnimal by mutableStateOf(false)
        private set

    fun setAnimal(animal: AnimalEv) {
        viewModelScope.launch {
            println("DEBUG_ALIMENTS: Début setAnimal pour ${animal.nom}")

            // Conserver une référence à l'animal original
            val originalAnimal = animal.copy()

            // Mettre à jour l'animal dans le ViewModel
            _animal.value = originalAnimal

            // Charger les consultations depuis la base de données
            try {
                val consultations = consultationRepository.getConsultationsForAnimal(animal.uuid)
                println(
                        "DEBUG_ALIMENTS: ${consultations.size} consultations chargées pour ${animal.nom}"
                )

                // Pour chaque consultation, vérifiez combien de rations et d'aliments elle contient
                consultations.forEachIndexed { i, c ->
                    println(
                            "DEBUG_ALIMENTS: Consultation $i (${c.date}) a ${c.rations.size} rations"
                    )
                    c.rations.forEachIndexed { j, r ->
                        println(
                                "DEBUG_ALIMENTS: --Ration $j (${r.name}) a ${r.alimentMutableList.size} aliments"
                        )
                        r.alimentMutableList.forEachIndexed { k, a ->
                            println(
                                    "DEBUG_ALIMENTS: ----Aliment $k: UUID=${a.uuid}, refAlimUnif=${a.refAlimUnif}, nom=${a.aliment?.nom ?: "ALIMENT NULL"}"
                            )
                        }
                    }
                }

                // Mettre à jour l'animal avec les consultations chargées
                _animal.update { currentAnimal ->
                    currentAnimal?.copy(consultations = consultations.toMutableList())
                }

                println("DEBUG_ALIMENTS: Animal mis à jour avec les consultations")
            } catch (e: Exception) {
                // Gérer les erreurs potentielles lors du chargement des consultations
                println("DEBUG_ALIMENTS: Erreur lors du chargement des consultations: ${e.message}")
                e.printStackTrace()
            }

            // Réinitialiser les états
            _selectedConsultation.value = null
            isEditingConsultation = false
            isEditingRation = false
            isEditingAnimal = false

            println("DEBUG_ALIMENTS: Fin setAnimal pour ${animal.nom}")
        }
    }

    fun navigateTo(section: AnimalDetailSection) {
        _currentSection.value = section
    }

    fun selectConsultation(consultation: ConsultationEv) {
        viewModelScope.launch {
            val fullConsultation = consultationRepository.getConsultationById(consultation.uuid)
            _selectedConsultation.value = fullConsultation

            // Afficher des détails de débogage pour s'assurer que les aliments sont bien chargés
            println(
                    "DEBUG selectConsultation - Consultation sélectionnée: ${fullConsultation?.date}, Rations: ${fullConsultation?.rations?.size}"
            )
            fullConsultation?.rations?.forEachIndexed { index, ration ->
                println(
                        "DEBUG selectConsultation - Ration[$index]: ${ration.name}, Aliments: ${ration.alimentMutableList.size}"
                )
                ration.alimentMutableList.forEachIndexed { alimentIndex, aliment ->
                    println(
                            "DEBUG selectConsultation - Aliment[$alimentIndex]: UUID=${aliment.uuid}, refAlimUnif=${aliment.refAlimUnif}, aliment=${aliment.aliment?.nom ?: "null"}"
                    )
                }
            }

            // Si aucune ration n'est sélectionnée mais qu'il y en a dans la consultation, en
            // sélectionner une
            if (_selectedRation.value == null && !fullConsultation?.rations.isNullOrEmpty()) {
                selectRation(fullConsultation!!.rations.first())
            }
        }
    }

    fun selectRation(ration: Ration) {
        println("DEBUG_ALIMENTS: Début selectRation pour ration ${ration.uuid} (${ration.name})")
        println("DEBUG_ALIMENTS: Ration a ${ration.alimentMutableList.size} aliments avant copie")

        ration.alimentMutableList.forEachIndexed { k, a ->
            println(
                    "DEBUG_ALIMENTS: --Avant copie - Aliment $k: UUID=${a.uuid}, refAlimUnif=${a.refAlimUnif}, nom=${a.aliment?.nom ?: "ALIMENT NULL"}"
            )
        }

        // Créer une copie profonde de la ration, y compris sa liste d'aliments
        val rationCopy =
                ration.copy(
                        alimentMutableList =
                                ration.alimentMutableList.map { it.copy() }.toMutableList()
                )

        println("DEBUG_ALIMENTS: Ration copiée a ${rationCopy.alimentMutableList.size} aliments")
        rationCopy.alimentMutableList.forEachIndexed { k, a ->
            println(
                    "DEBUG_ALIMENTS: --Après copie - Aliment $k: UUID=${a.uuid}, refAlimUnif=${a.refAlimUnif}, nom=${a.aliment?.nom ?: "ALIMENT NULL"}"
            )
        }

        _selectedRation.value = rationCopy
        println("DEBUG_ALIMENTS: Ration sélectionnée mise à jour")

        println("DEBUG_ALIMENTS: Fin selectRation")
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

    fun startEditingAnimal() {
        isEditingAnimal = true
    }

    fun stopEditingAnimal() {
        isEditingAnimal = false
    }

    fun addConsultation(consultation: ConsultationEv) {
        viewModelScope.launch {
            try {
                // Assigner l'ID de l'animal à la consultation
                val animalId = _animal.value?.uuid ?: return@launch
                println("Tentative d'ajout d'une consultation pour l'animal avec ID: $animalId")

                // Vérifier que l'animal existe dans la base de données
                val animalExists = animalRepository.getAnimalById(animalId) != null
                if (!animalExists) {
                    println(
                            "Erreur: Impossible d'ajouter une consultation car l'animal avec l'ID $animalId n'existe pas dans la base de données"
                    )
                    return@launch
                }

                // Créer une copie de la consultation avec l'ID de l'animal
                val consultationToSave = consultation.copy(idAnim = animalId)
                println("UUID de la consultation à sauvegarder: ${consultationToSave.uuid}")
                println("Date de la consultation à sauvegarder: ${consultationToSave.date}")
                println(
                        "Objet de la consultation à sauvegarder: ${consultationToSave.objectConsult}"
                )

                // Sauvegarder la consultation
                consultationRepository.saveConsultation(consultationToSave)
                println("Consultation sauvegardée avec succès")

                // Rafraîchir les consultations depuis la base de données
                val updatedConsultations =
                        consultationRepository.getConsultationsForAnimal(animalId)
                println(
                        "Nombre de consultations récupérées après sauvegarde: ${updatedConsultations.size}"
                )

                // Mettre à jour l'animal avec les consultations rafraîchies
                _animal.update { currentAnimal ->
                    currentAnimal?.copy(consultations = updatedConsultations.toMutableList())
                }

                // Récupérer la consultation complète depuis la base de données
                val savedConsultation =
                        consultationRepository.getConsultationById(consultationToSave.uuid)
                if (savedConsultation != null) {
                    println("Consultation récupérée depuis la base de données avec succès")
                    _selectedConsultation.value = savedConsultation
                } else {
                    println(
                            "Erreur: Impossible de récupérer la consultation sauvegardée depuis la base de données"
                    )
                }

                // Arrêter le mode édition
                isEditingConsultation = false
            } catch (e: Exception) {
                println("Erreur lors de l'ajout de la consultation: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun updateConsultation(consultation: ConsultationEv) {
        viewModelScope.launch {
            try {
                // Vérifier que l'animal existe dans la base de données
                val animalId = consultation.idAnim ?: return@launch
                val animalExists = animalRepository.getAnimalById(animalId) != null

                if (!animalExists) {
                    println(
                            "Erreur: Impossible de mettre à jour la consultation car l'animal avec l'ID $animalId n'existe pas dans la base de données"
                    )
                    return@launch
                }

                // Sauvegarder la consultation
                consultationRepository.saveConsultation(consultation)

                // Rafraîchir les consultations depuis la base de données au lieu de mettre à jour
                // manuellement
                val updatedConsultations =
                        consultationRepository.getConsultationsForAnimal(consultation.idAnim)
                _animal.update { currentAnimal ->
                    currentAnimal?.copy(consultations = updatedConsultations.toMutableList())
                }

                // Mettre à jour la consultation sélectionnée
                _selectedConsultation.value = consultation
            } catch (e: Exception) {
                println("Erreur lors de la mise à jour de la consultation: ${e.message}")
                e.printStackTrace()
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
        println(
                "DEBUG_ALIMENTS: Début updateRationInConsultation pour ration ${ration.uuid} (${ration.name})"
        )
        println(
                "DEBUG_ALIMENTS: Ration à mettre à jour contient ${ration.alimentMutableList.size} aliments"
        )

        ration.alimentMutableList.forEachIndexed { k, a ->
            println(
                    "DEBUG_ALIMENTS: --Ration à mettre à jour - Aliment $k: UUID=${a.uuid}, refAlimUnif=${a.refAlimUnif}, nom=${a.aliment?.nom ?: "ALIMENT NULL"}"
            )
        }

        val consultation = _selectedConsultation.value?.copy() ?: return
        println("DEBUG_ALIMENTS: Consultation actuelle: ${consultation.uuid}, ${consultation.date}")
        println("DEBUG_ALIMENTS: Consultation contient ${consultation.rations.size} rations")

        val updatedRations = consultation.rations.toMutableList()
        val index = updatedRations.indexOfFirst { it.uuid == ration.uuid }

        if (index >= 0) {
            println("DEBUG_ALIMENTS: Ration trouvée à l'index $index")
            updatedRations[index] = ration
            val updatedConsultation = consultation.copy(rations = updatedRations)

            println("DEBUG_ALIMENTS: Mise à jour de la consultation avec la ration modifiée")
            println(
                    "DEBUG_ALIMENTS: Consultation mise à jour contient ${updatedConsultation.rations.size} rations"
            )

            _selectedConsultation.value = updatedConsultation

            println(
                    "DEBUG_ALIMENTS: Sauvegarde des modifications dans la base de données via updateConsultation"
            )
            updateConsultation(updatedConsultation)
        } else {
            println("DEBUG_ALIMENTS: ERREUR - Ration non trouvée dans la consultation")
        }

        println("DEBUG_ALIMENTS: Fin updateRationInConsultation")
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

            // Rafraîchir les consultations depuis la base de données
            val updatedConsultations =
                    consultationRepository.getConsultationsForAnimal(consultation.idAnim)
            _animal.update { currentAnimal ->
                currentAnimal?.copy(consultations = updatedConsultations.toMutableList())
            }

            // Désélectionner la consultation supprimée
            if (_selectedConsultation.value?.uuid == consultation.uuid) {
                _selectedConsultation.value = null
            }
        }
    }

    fun updateAnimal(updatedAnimal: AnimalEv) {
        viewModelScope.launch {
            // Conserver l'UUID et les consultations de l'animal original
            val animalToUpdate =
                    _animal.value?.let { originalAnimal ->
                        updatedAnimal.copy(
                                uuid = originalAnimal.uuid,
                                consultations = originalAnimal.consultations
                        )
                    }
                            ?: return@launch

            animalRepository.updateAnimal(animalToUpdate)

            // Mettre à jour l'animal dans le ViewModel
            _animal.value = animalToUpdate

            // Arrêter le mode édition
            isEditingAnimal = false
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun prepareNewConsultation(date: LocalDate) {
        val newConsultation =
                ConsultationEv(
                        uuid = kotlin.uuid.Uuid.random().toString(),
                        date = date,
                        idAnim = _animal.value?.uuid ?: ""
                )
        _selectedConsultation.value = newConsultation
    }

    /**
     * Supprime l'animal actuel de la base de données
     *
     * @return true si la suppression a réussi, false sinon
     */
    fun deleteAnimal(): Boolean {
        val animalToDelete = _animal.value ?: return false

        viewModelScope.launch {
            try {
                // Supprimer l'animal de la base de données
                animalRepository.deleteAnimal(animalToDelete)

                // Réinitialiser les états
                _animal.value = null
                _selectedConsultation.value = null
                _currentSection.value = AnimalDetailSection.IDENTIFICATION
                isEditingConsultation = false
                isEditingRation = false
                isEditingAnimal = false
            } catch (e: Exception) {
                println("Erreur lors de la suppression de l'animal: ${e.message}")
                e.printStackTrace()
                return@launch
            }
        }

        return true
    }

    /**
     * Met à jour la quantité d'un aliment dans la ration sélectionnée
     *
     * @param alimentUuid UUID de l'aliment à mettre à jour
     * @param newQuantity Nouvelle quantité de l'aliment
     */
    fun updateAlimentQuantity(alimentUuid: String, newQuantity: Float) {
        val currentRation = _selectedRation.value?.copy() ?: return

        // Créer une nouvelle liste d'aliments avec la quantité mise à jour
        val updatedAliments =
                currentRation
                        .alimentMutableList
                        .map { aliment ->
                            if (aliment.uuid == alimentUuid) {
                                // Créer une copie de l'aliment avec la nouvelle quantité
                                aliment.copy(quantity = newQuantity)
                            } else {
                                aliment
                            }
                        }
                        .toMutableList()

        // Créer une nouvelle ration avec la liste d'aliments mise à jour
        val updatedRation = currentRation.copy(alimentMutableList = updatedAliments)

        // Mettre à jour la ration sélectionnée
        _selectedRation.value = updatedRation

        // Mettre à jour la ration dans la consultation
        updateRationInConsultation(updatedRation)
    }
}
