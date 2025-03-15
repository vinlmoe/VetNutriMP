package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Repository.AlimentRepository
import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.ConsultationRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
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

    // Liste de tous les aliments disponibles
    private val _availableFoods = mutableStateOf<List<AlimentEv>>(emptyList())
    val availableFoods: List<AlimentEv>
        get() = _availableFoods.value

    // État de chargement des aliments
    private val _isLoadingFoods = mutableStateOf(false)
    val isLoadingFoods: Boolean
        get() = _isLoadingFoods.value

    // Requête de recherche d'aliment
    private val _alimentSearchQuery = mutableStateOf("")
    val alimentSearchQuery: String
        get() = _alimentSearchQuery.value

    /**
     * Charge tous les aliments disponibles en utilisant la version légère pour de meilleures
     * performances
     */
    fun loadAvailableFoods() {
        viewModelScope.launch {
            try {
                _isLoadingFoods.value = true

                // Utiliser la version légère des aliments pour de meilleures performances
                val alimentsLight = AlimentRepository.getAllAlimentsLight()

                // Convertir les AlimentEvLight en AlimentEv avec une valMap vide
                _availableFoods.value =
                        alimentsLight.map { alimentLight ->
                            AlimentEv(
                                    uuid = alimentLight.uuid,
                                    nom = alimentLight.nom,
                                    typeAliment = alimentLight.typeAliment,
                                    group = alimentLight.group,
                                    brand = alimentLight.brand,
                                    gamme = alimentLight.gamme,
                                    especes = alimentLight.especes.toMutableList(),
                                    indicat = alimentLight.indicat.toMutableList(),
                                    deprecated = alimentLight.deprecated,
                                    valMap = mutableMapOf(),
                                    rationUUID = ""
                            )
                        }

                println("Aliments légers chargés: ${_availableFoods.value.size}")
            } catch (e: Exception) {
                println("Erreur lors du chargement des aliments: ${e.message}")
                e.printStackTrace()
                _availableFoods.value = emptyList()
            } finally {
                _isLoadingFoods.value = false
            }
        }
    }

    /** Définit la requête de recherche pour les aliments */
    fun setAlimentSearchQuery(query: String) {
        _alimentSearchQuery.value = query
    }

    /** Retourne la liste des aliments filtrée par la requête de recherche */
    fun getFilteredFoods(query: String): List<AlimentEv> {
        if (query.isBlank()) return availableFoods

        return availableFoods.filter { aliment ->
            aliment.nom?.contains(query, ignoreCase = true) == true ||
                    aliment.brand?.contains(query, ignoreCase = true) == true ||
                    aliment.group?.label?.contains(query, ignoreCase = true) == true ||
                    aliment.typeAliment?.label?.contains(query, ignoreCase = true) == true
        }
    }

    /** Ajoute un aliment à une ration */
    @OptIn(ExperimentalUuidApi::class)
    fun addAlimentToRation(ration: Ration, aliment: AlimentEv, quantite: Float) {
        println("Ajout de l'aliment ${aliment.nom} (${quantite}g) à la ration ${ration.uuid}")

        viewModelScope.launch {
            try {
                // Si l'aliment n'a pas de valeurs nutritionnelles (car c'est une version légère),
                // récupérer l'aliment complet depuis le repository
                val alimentComplet =
                        if (aliment.valMap.isEmpty()) {
                            // Utiliser la méthode statique du companion object pour éviter le
                            // problème de null
                            val alimentCompletFromRepo =
                                    AlimentRepository.getAlimentByUUID(aliment.uuid)
                            alimentCompletFromRepo ?: aliment
                        } else {
                            aliment
                        }

                val alimentRation =
                        AlimentRation(
                                uuid = Uuid.random().toString(),
                                aliment = alimentComplet,
                                quantity = quantite,
                                refRation = ration.uuid,
                                refAlimUnif = alimentComplet.uuid
                        )

                // Créer une copie de la ration avec le nouvel aliment
                val updatedRation = ration.copy()
                updatedRation.alimentMutableList.add(alimentRation)

                // Mettre à jour la ration dans la consultation
                updateRationInConsultation(updatedRation)
            } catch (e: Exception) {
                println("Erreur lors de l'ajout de l'aliment à la ration: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun setAnimal(animal: AnimalEv) {
        viewModelScope.launch {
            println("DEBUG_ALIMENTS: Début setAnimal pour ${animal.nom}")

            // Réinitialiser les états immédiatement pour éviter toute rémanence
            _selectedRation.value = null
            _selectedConsultation.value = null
            isEditingConsultation = false
            isEditingRation = false
            isEditingAnimal = false

            // Conserver une référence à l'animal original
            val originalAnimal =
                    animal.copy(
                            // Assurons-nous de copier les consultations existantes
                            consultations = animal.consultations.map { it.copy() }.toMutableList()
                    )

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
                if (consultations.isNotEmpty()) {
                    _animal.update { currentAnimal ->
                        currentAnimal?.copy(consultations = consultations.toMutableList())
                    }
                    println(
                            "DEBUG_ALIMENTS: Animal mis à jour avec ${consultations.size} consultations"
                    )
                } else {
                    // Si aucune consultation n'est chargée, conservons celles qui étaient déjà dans
                    // l'animal
                    println(
                            "DEBUG_ALIMENTS: Aucune consultation chargée, conservation des ${animal.consultations.size} consultations existantes"
                    )
                }

                println("DEBUG_ALIMENTS: Animal mis à jour avec les consultations")
            } catch (e: Exception) {
                // Gérer les erreurs potentielles lors du chargement des consultations
                println("DEBUG_ALIMENTS: Erreur lors du chargement des consultations: ${e.message}")
                e.printStackTrace()
            }

            println(
                    "DEBUG_ALIMENTS: Fin setAnimal - toutes les données de l'animal précédent ont été réinitialisées"
            )
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

    /** Réinitialise la ration sélectionnée en mettant selectedRation à null */
    fun resetSelectedRation() {
        println("DEBUG_ALIMENTS: Réinitialisation de la ration sélectionnée")
        _selectedRation.value = null
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

        // Log pour déboguer
        println("DEBUG: Ration ajoutée avec UUID=${ration.uuid}, nom=${ration.name}")
        println("DEBUG: La consultation a maintenant ${updatedRations.size} rations")
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

        // Forcer la mise à jour du StateFlow pour notifier l'interface
        _selectedConsultation.value = updatedConsultation

        // Mettre à jour dans la base de données
        updateConsultation(updatedConsultation)

        // Si la ration supprimée était sélectionnée, sélectionner une autre ration si disponible
        if (_selectedRation.value?.uuid == ration.uuid) {
            updatedRations.firstOrNull()?.let { selectRation(it) }
                    ?: run { _selectedRation.value = null }
        }

        // Log pour déboguer
        println("DEBUG: Ration supprimée avec UUID=${ration.uuid}, nom=${ration.name}")
        println("DEBUG: La consultation a maintenant ${updatedRations.size} rations")
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
            println(
                    "DEBUG_DETAIL_VM: Début updateAnimal avec updatedAnimal.specieId=${updatedAnimal.specieId}, espece=${updatedAnimal.getEspece().label}"
            )

            // Conserver l'UUID et les consultations de l'animal original
            val animalToUpdate =
                    _animal.value?.let { originalAnimal ->
                        println(
                                "DEBUG_DETAIL_VM: Animal original avec specieId=${originalAnimal.specieId}, espece=${originalAnimal.getEspece().label}"
                        )

                        updatedAnimal.copy(
                                uuid = originalAnimal.uuid,
                                consultations = originalAnimal.consultations
                        )
                    }
                            ?: return@launch

            println(
                    "DEBUG_DETAIL_VM: Avant mise à jour en DB: animalToUpdate.specieId=${animalToUpdate.specieId}, espece=${animalToUpdate.getEspece().label}"
            )

            animalRepository.updateAnimal(animalToUpdate)

            // Mettre à jour l'animal dans le ViewModel
            _animal.value = animalToUpdate
            println(
                    "DEBUG_DETAIL_VM: Animal mis à jour dans le ViewModel: specieId=${animalToUpdate.specieId}, espece=${animalToUpdate.getEspece().label}"
            )

            // Arrêter le mode édition
            isEditingAnimal = false
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun prepareNewConsultation(date: LocalDate) {
        val newConsultation =
                ConsultationEv(
                        uuid = Uuid.random().toString(),
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

    /**
     * Crée une ration par défaut pour une consultation
     *
     * @param consultation La consultation pour laquelle créer une ration
     */
    fun createDefaultRation(consultation: ConsultationEv) {
        viewModelScope.launch {
            // Créer une nouvelle ration avec un nom par défaut
            val newRation =
                    Ration(
                            name = "Ration proposée",
                            actual = false,
                            alimentMutableList = mutableListOf()
                    )

            // Créer une copie de la liste des rations et y ajouter la nouvelle ration
            val updatedRations = consultation.rations.toMutableList()
            updatedRations.add(newRation)

            // Créer une copie de la consultation avec la liste mise à jour
            val updatedConsultation = consultation.copy(rations = updatedRations)

            // Mettre à jour le StateFlow avec la nouvelle consultation
            _selectedConsultation.value = updatedConsultation

            // Sauvegarder la consultation mise à jour
            consultationRepository.saveConsultation(updatedConsultation)

            // Sélectionner la nouvelle ration
            selectRation(newRation)

            // Log pour débogage
            println("DEBUG: Ration créée avec UUID=${newRation.uuid}, nom=${newRation.name}")
            println("DEBUG: La consultation a maintenant ${updatedRations.size} rations")
        }
    }

    /**
     * Supprime un aliment d'une ration
     *
     * @param alimentUuid UUID de l'aliment à supprimer
     */
    fun removeAlimentFromRation(alimentUuid: String) {
        val currentRation = _selectedRation.value?.copy() ?: return

        // Créer une nouvelle liste d'aliments sans l'aliment à supprimer
        val updatedAliments =
                currentRation.alimentMutableList.filter { it.uuid != alimentUuid }.toMutableList()

        // Créer une nouvelle ration avec la liste d'aliments mise à jour
        val updatedRation = currentRation.copy(alimentMutableList = updatedAliments)

        // Mettre à jour la ration sélectionnée
        _selectedRation.value = updatedRation

        // Mettre à jour la ration dans la consultation
        updateRationInConsultation(updatedRation)
    }

    /**
     * Récupère une consultation par son identifiant
     *
     * @param consultationId Identifiant de la consultation
     * @return La consultation ou null si elle n'est pas trouvée
     */
    suspend fun getConsultationById(consultationId: String): ConsultationEv? {
        return consultationRepository.getConsultationById(consultationId)
    }

    /**
     * Duplique une ration existante et tous ses aliments avec de nouveaux UUID
     *
     * @param ration Ration à dupliquer
     */
    @OptIn(ExperimentalUuidApi::class)
    fun duplicateRation(ration: Ration) {
        viewModelScope.launch {
            // Obtenir la consultation courante
            val currentConsultation = _selectedConsultation.value ?: return@launch

            // Créer une copie de la ration avec un nouveau UUID et un nom indiquant qu'il s'agit
            // d'une copie
            val duplicatedRation =
                    ration.copy(
                            uuid = Uuid.random().toString(),
                            name = "${ration.name} (copie)",
                            alimentMutableList =
                                    mutableListOf() // Liste vide temporaire, nous allons la remplir
                            // juste après
                            )

            // Duplicater chaque aliment avec un nouveau UUID
            val duplicatedAliments =
                    ration.alimentMutableList.map { aliment ->
                        aliment.copy(
                                uuid = Uuid.random().toString(),
                                refRation =
                                        duplicatedRation
                                                .uuid // Référence au nouveau UUID de la ration
                        )
                    }

            // Ajouter les aliments dupliqués à la ration dupliquée
            duplicatedRation.alimentMutableList.addAll(duplicatedAliments)

            // Créer une copie de la liste des rations et y ajouter la ration dupliquée
            val updatedRations = currentConsultation.rations.toMutableList()
            updatedRations.add(duplicatedRation)

            // Créer une copie de la consultation avec la liste mise à jour
            val updatedConsultation = currentConsultation.copy(rations = updatedRations)

            // Mettre à jour le StateFlow avec la nouvelle consultation
            _selectedConsultation.value = updatedConsultation

            // Sélectionner la ration dupliquée
            _selectedRation.value = duplicatedRation

            // Sauvegarder les modifications dans la base de données
            consultationRepository.saveConsultation(updatedConsultation)

            println("DEBUG: Ration dupliquée avec succès, UUID=${duplicatedRation.uuid}")
        }
    }

    /**
     * Met à jour la liste des aliments dans une ration
     *
     * @param ration Ration à mettre à jour
     * @param aliments Nouvelle liste d'aliments
     */
    fun updateRationAliments(ration: Ration, aliments: List<AlimentRation>) {
        // Créer une copie de la ration avec la liste d'aliments mise à jour
        val updatedRation = ration.copy(alimentMutableList = aliments.toMutableList())

        // Mettre à jour la ration dans la consultation
        updateRationInConsultation(updatedRation)
    }
}
