package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Data.AnalyseResultat
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ComparaisonNutriment
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.RationAnalyzer
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Repository.AlimentRepository
import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.ConsultationRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.ExpressionEvaluator
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Énumération des différentes sections de la vue détaillée d'un animal */
enum class AnimalDetailSection {
    IDENTIFICATION, // Informations d'identification de l'animal
    CONSULTATIONS, // Liste des consultations
    RATIONS, // Vue des rations
    GRAPHIQUE
}

class AnimalDetailViewModel(
        private val consultationRepository: ConsultationRepository,
        private val animalRepository: AnimalRepository,
        private val databaseReferenceEvRepository: DatabaseReferenceEvRepository
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

    // Liste de tous les aliments disponibles (StateFlow pour observation par Compose)
    private val _availableFoods = MutableStateFlow<List<AlimentEv>>(emptyList())
    val availableFoods: StateFlow<List<AlimentEv>> = _availableFoods.asStateFlow()

    // État de chargement des aliments (StateFlow pour observation par Compose)
    private val _isLoadingFoods = MutableStateFlow(false)
    val isLoadingFoods: StateFlow<Boolean> = _isLoadingFoods.asStateFlow()

    // Requête de recherche d'aliment
    private val _alimentSearchQuery = mutableStateOf("")
    val alimentSearchQuery: String
        get() = _alimentSearchQuery.value

    // Nouvelle instance de l'analyseur de rations
    private val rationAnalyzer = RationAnalyzer()

    // StateFlow pour stocker les résultats d'analyse de la ration sélectionnée
    private val _rationAnalyseResultat = MutableStateFlow<AnalyseResultat?>(null)
    val rationAnalyseResultat: StateFlow<AnalyseResultat?> = _rationAnalyseResultat.asStateFlow()

    // StateFlow pour stocker la comparaison entre deux rations
    private val _rationsComparaison =
            MutableStateFlow<Map<String, ComparaisonNutriment>>(emptyMap())
    val rationsComparaison: StateFlow<Map<String, ComparaisonNutriment>> =
            _rationsComparaison.asStateFlow()

    // StateFlow pour indiquer si une analyse est en cours
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    // Nouveaux StateFlow pour les références nutritionnelles
    private val _availableReferences = MutableStateFlow<List<ReferenceEv>>(emptyList())
    val availableReferences: StateFlow<List<ReferenceEv>> = _availableReferences.asStateFlow()

    private val _isLoadingReferences = MutableStateFlow(false)
    val isLoadingReferences: StateFlow<Boolean> = _isLoadingReferences.asStateFlow()

    // StateFlow pour gérer l'affichage de la vue plein écran de consultation
    private val _showFullScreenEdit = MutableStateFlow(false)
    val showFullScreenEdit: StateFlow<Boolean> = _showFullScreenEdit.asStateFlow()

    // StateFlow pour les calculs métaboliques
    private val _poidsMetabolique = MutableStateFlow<Double?>(null)
    val poidsMetabolique: StateFlow<Double?> = _poidsMetabolique.asStateFlow()

    private val _besoinEnergetiqueStandard = MutableStateFlow<Double?>(null)
    val besoinEnergetiqueStandard: StateFlow<Double?> = _besoinEnergetiqueStandard.asStateFlow()

    private val _besoinEnergetiqueTotal = MutableStateFlow<Double?>(null)
    val besoinEnergetiqueTotal: StateFlow<Double?> = _besoinEnergetiqueTotal.asStateFlow()

    // StateFlow pour la référence utilisée dans les calculs
    private val _referenceUtilisee = MutableStateFlow<ReferenceEv?>(null)
    val referenceUtilisee: StateFlow<ReferenceEv?> = _referenceUtilisee.asStateFlow()

    init {
        // Démarrer automatiquement l'observation des aliments
        loadAvailableFoods()
    }

    /**
     * S'abonne au Flow des aliments pour recevoir des mises à jour automatiques quand de nouveaux
     * aliments sont ajoutés ou modifiés
     */
    fun loadAvailableFoods() {
        _isLoadingFoods.value = true
        println("AnimalDetailViewModel: Démarrage de l'observation des aliments")

        // S'abonner au Flow réactif des aliments pour des mises à jour automatiques
        // Utiliser onEach + launchIn pour lancer l'observation en arrière-plan
        AlimentRepository.observeAllAliments()
                .onEach { aliments ->
                    println(
                            "AnimalDetailViewModel: Mise à jour automatique de la liste des aliments - ${aliments.size} aliments"
                    )

                    // Convertir en version légère pour l'affichage (sans valMap pour les
                    // performances)
                    _availableFoods.value =
                            aliments.map { aliment ->
                                aliment.copy(
                                        valMap = mutableMapOf()
                                ) // Vider valMap pour les performances
                            }

                    if (_isLoadingFoods.value) {
                        _isLoadingFoods.value = false
                    }
                }
                .catch { e ->
                    println("Erreur lors de l'observation des aliments: ${e.message}")
                    e.printStackTrace()
                    _availableFoods.value = emptyList()
                    _isLoadingFoods.value = false
                }
                .launchIn(viewModelScope)
    }

    /** Définit la requête de recherche pour les aliments */
    fun setAlimentSearchQuery(query: String) {
        _alimentSearchQuery.value = query
    }

    /** Retourne la liste des aliments filtrée par la requête de recherche */
    fun getFilteredFoods(query: String): List<AlimentEv> {
        val currentFoods = _availableFoods.value
        if (query.isBlank()) return currentFoods

        return currentFoods.filter { aliment ->
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
                                quantite = quantite,
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
            _showFullScreenEdit.value = false

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

                    // Sélectionner automatiquement la consultation la plus récente
                    val mostRecentConsultation =
                            consultations.filter { it.date != null }.maxByOrNull { it.date!! }
                    if (mostRecentConsultation != null) {
                        println(
                                "DEBUG_ALIMENTS: Sélection automatique de la consultation la plus récente: ${mostRecentConsultation.date}"
                        )
                        selectConsultation(mostRecentConsultation)
                    } else if (consultations.isNotEmpty()) {
                        // Si aucune consultation n'a de date, prendre la première
                        println(
                                "DEBUG_ALIMENTS: Aucune consultation avec date valide, sélection de la première consultation"
                        )
                        selectConsultation(consultations.first())
                    }
                } else {
                    // Si aucune consultation n'est chargée, conservons celles qui étaient déjà dans
                    // l'animal
                    println(
                            "DEBUG_ALIMENTS: Aucune consultation chargée, conservation des ${animal.consultations.size} consultations existantes"
                    )

                    // Si l'animal a des consultations existantes, sélectionner la plus récente
                    if (animal.consultations.isNotEmpty()) {
                        val mostRecentConsultation =
                                animal.consultations.filter { it.date != null }.maxByOrNull {
                                    it.date!!
                                }
                        if (mostRecentConsultation != null) {
                            println(
                                    "DEBUG_ALIMENTS: Sélection automatique de la consultation existante la plus récente: ${mostRecentConsultation.date}"
                            )
                            selectConsultation(mostRecentConsultation)
                        } else {
                            // Si aucune consultation n'a de date, prendre la première
                            println(
                                    "DEBUG_ALIMENTS: Aucune consultation existante avec date valide, sélection de la première consultation"
                            )
                            selectConsultation(animal.consultations.first())
                        }
                    }
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

            // Calculer automatiquement les valeurs métaboliques pour la consultation sélectionnée
            fullConsultation?.let { consultation ->
                println(
                        "🔍 DEBUG: Déclenchement calculerValeursMetaboliques pour consultation ${consultation.uuid}"
                )
                calculerValeursMetaboliques(consultation)
            }
        }
    }

    /**
     * Sélectionne une ration et lance automatiquement son analyse
     *
     * @param ration La ration à sélectionner et analyser
     */
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

        // Lancer l'analyse de la ration automatiquement
        analyserRationSelectionnee()

        println("DEBUG_ALIMENTS: Fin selectRation")
    }

    /**
     * Analyse la ration actuellement sélectionnée L'analyse est lancée automatiquement lors de la
     * sélection d'une ration, mais cette méthode peut être appelée explicitement après des
     * modifications.
     */
    fun analyserRationSelectionnee() {
        val rationActuelle = _selectedRation.value ?: return
        val consultationActuelle = _selectedConsultation.value

        viewModelScope.launch {
            try {
                _isAnalyzing.value = true

                // Vérifier que la ration contient des aliments
                if (rationActuelle.alimentMutableList.isEmpty()) {
                    _rationAnalyseResultat.value =
                            AnalyseResultat(
                                    rationId = rationActuelle.uuid,
                                    rationName = rationActuelle.name,
                                    alertes = listOf("Cette ration ne contient aucun aliment")
                            )
                    return@launch
                }

                // Effectuer l'analyse en passant la consultation pour les variables supplémentaires
                println("DEBUG: Lancement analyse avec consultation: ${consultationActuelle?.uuid}")
                val resultat = rationAnalyzer.analyserRation(rationActuelle, consultationActuelle)
                _rationAnalyseResultat.value = resultat

                println(
                        "Analyse de la ration terminée: ${resultat.ratios.size} ratios calculés, ${resultat.alertes.size} alertes générées"
                )
            } catch (e: Exception) {
                println("Erreur lors de l'analyse de la ration: ${e.message}")
                e.printStackTrace()

                // Créer un résultat avec une erreur
                _rationAnalyseResultat.value =
                        AnalyseResultat(
                                rationId = rationActuelle.uuid,
                                rationName = rationActuelle.name,
                                alertes = listOf("Erreur lors de l'analyse: ${e.message}")
                        )
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    /**
     * Compare la ration sélectionnée avec une autre ration
     *
     * @param autreRation La ration à comparer avec la ration sélectionnée
     */
    fun comparerAvecAutreRation(autreRation: Ration) {
        val rationActuelle = _selectedRation.value ?: return

        viewModelScope.launch {
            try {
                _isAnalyzing.value = true

                // Effectuer la comparaison
                val resultat = rationAnalyzer.comparerRations(rationActuelle, autreRation)
                _rationsComparaison.value = resultat

                println("Comparaison des rations terminée: ${resultat.size} nutriments comparés")
            } catch (e: Exception) {
                println("Erreur lors de la comparaison des rations: ${e.message}")
                e.printStackTrace()

                // Réinitialiser la comparaison en cas d'erreur
                _rationsComparaison.value = emptyMap()
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    /**
     * Génère un rapport d'analyse nutritionnelle détaillé de la ration sélectionnée
     *
     * @return Une chaîne formatée contenant le rapport d'analyse
     */
    fun genererRapportAnalyse(): String {
        val analyse = _rationAnalyseResultat.value ?: return "Aucune analyse disponible"
        val ration = _selectedRation.value ?: return "Aucune ration sélectionnée"

        val rapport = StringBuilder()

        // En-tête
        rapport.append("RAPPORT D'ANALYSE NUTRITIONNELLE\n")
        rapport.append("===============================\n\n")

        // Informations générales
        rapport.append("Ration: ${analyse.rationName}\n")
        rapport.append("Quantité totale: ${analyse.quantiteTotale}g\n")
        rapport.append("Densité énergétique: ${analyse.densiteEnergetique} kcal/g\n\n")

        // Scores
        rapport.append("Score de complétude: ${analyse.completude.toInt()}%\n")
        rapport.append("Score d'équilibre: ${analyse.equilibre.toInt()}%\n\n")

        // Macronutriments
        rapport.append("MACRONUTRIMENTS\n")
        rapport.append("--------------\n")
        analyse.macronutriments.forEach { (nutriment, valeur) ->
            rapport.append("$nutriment: $valeur g\n")
        }
        rapport.append("\n")

        // Minéraux
        rapport.append("MINÉRAUX\n")
        rapport.append("--------\n")
        analyse.mineraux.forEach { (nutriment, valeur) ->
            rapport.append("$nutriment: $valeur g\n")
        }
        rapport.append("\n")

        // Vitamines
        rapport.append("VITAMINES\n")
        rapport.append("---------\n")
        analyse.vitamines.forEach { (nutriment, valeur) ->
            rapport.append("$nutriment: $valeur UI/mg\n")
        }
        rapport.append("\n")

        // Lipides
        rapport.append("LIPIDES\n")
        rapport.append("-------\n")
        analyse.lipides.forEach { (nutriment, valeur) -> rapport.append("$nutriment: $valeur g\n") }
        rapport.append("\n")

        // Ratios
        rapport.append("RATIOS NUTRITIONNELS\n")
        rapport.append("-------------------\n")
        analyse.ratios.forEach { (ratio, valeur) -> rapport.append("$ratio: $valeur\n") }
        rapport.append("\n")

        // Alertes
        if (analyse.alertes.isNotEmpty()) {
            rapport.append("ALERTES\n")
            rapport.append("-------\n")
            analyse.alertes.forEach { alerte -> rapport.append("- $alerte\n") }
            rapport.append("\n")
        }

        // Aliments de la ration
        rapport.append("COMPOSITION DE LA RATION\n")
        rapport.append("----------------------\n")
        ration.alimentMutableList.forEach { aliment ->
            rapport.append(
                    "- ${aliment.aliment?.nom ?: "Aliment sans nom"}: ${aliment.quantite}g\n"
            )
        }

        return rapport.toString()
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
                _showFullScreenEdit.value = false
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
                                aliment.copy(quantite = newQuantity)
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

        // Relancer l'analyse pour tenir compte des modifications
        analyserRationSelectionnee()
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

        // Relancer l'analyse pour tenir compte des modifications
        analyserRationSelectionnee()
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
     * Met à jour une ration existante
     *
     * @param ration Ration à mettre à jour
     */
    fun updateRation(ration: Ration) {
        // Mettre à jour la ration sélectionnée
        _selectedRation.value = ration

        // Mettre à jour la ration dans la consultation
        updateRationInConsultation(ration)

        // Relancer l'analyse pour tenir compte des modifications
        analyserRationSelectionnee()
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

        // Mettre à jour la ration sélectionnée pour rafraîchir l'UI
        _selectedRation.value = updatedRation

        // Mettre à jour la ration dans la consultation
        updateRationInConsultation(updatedRation)
    }

    /**
     * Renvoie la liste des aliments disponibles
     *
     * @return Liste des aliments disponibles
     */
    fun getAliments(): List<AlimentEv> {
        return _availableFoods.value
    }

    /**
     * Analyse une ration et met à jour le StateFlow des résultats
     *
     * @param ration La ration à analyser
     * @param consultation La consultation associée (optionnelle)
     */
    fun analyserRation(ration: Ration, consultation: ConsultationEv? = null) {
        viewModelScope.launch {
            try {
                _isAnalyzing.value = true
                val resultat = rationAnalyzer.analyserRation(ration, consultation)
                _rationAnalyseResultat.value = resultat
                println("Analyse de la ration ${ration.name} terminée")
            } catch (e: Exception) {
                println("Erreur lors de l'analyse de la ration: ${e.message}")
                e.printStackTrace()
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    /**
     * Compare deux rations et met à jour le StateFlow des comparaisons
     *
     * @param ration1 Première ration à comparer
     * @param ration2 Seconde ration à comparer
     */
    fun comparerRations(ration1: Ration, ration2: Ration) {
        viewModelScope.launch {
            try {
                _isAnalyzing.value = true
                val comparaison = rationAnalyzer.comparerRations(ration1, ration2)
                _rationsComparaison.value = comparaison
                println("Comparaison entre ${ration1.name} et ${ration2.name} terminée")
            } catch (e: Exception) {
                println("Erreur lors de la comparaison des rations: ${e.message}")
                e.printStackTrace()
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    // === MÉTHODES POUR LES RÉFÉRENCES NUTRITIONNELLES ===

    /** Charge toutes les références nutritionnelles disponibles pour l'espèce de l'animal */
    fun chargerReferencesDisponibles() {
        viewModelScope.launch {
            try {
                _isLoadingReferences.value = true
                val animalActuel = _animal.value
                if (animalActuel != null) {
                    val espece = Espece.getByLabel(animalActuel.specieId ?: "") ?: Espece.CHIEN

                    // Charger toutes les références depuis la base de données
                    val references = databaseReferenceEvRepository.getAllReferenceEv()

                    // Pour l'instant on prend toutes les références, mais on pourrait filtrer par
                    // espèce
                    _availableReferences.value = references

                    println(
                            "Références chargées pour l'espèce: ${espece.label} - ${references.size} références trouvées"
                    )
                    references.forEach { ref -> println("- ${ref.nom} (maladie: ${ref.maladie})") }
                }
            } catch (e: Exception) {
                println("Erreur lors du chargement des références: ${e.message}")
                _availableReferences.value = emptyList()
            } finally {
                _isLoadingReferences.value = false
            }
        }
    }

    /**
     * Définit la référence générale pour une consultation
     *
     * @param consultationId L'ID de la consultation
     * @param referenceId L'ID de la référence à définir
     */
    fun definirReferenceGenerale(consultationId: String, referenceId: String?) {
        viewModelScope.launch {
            try {
                val animalActuel = _animal.value
                if (animalActuel != null) {
                    val consultation = animalActuel.consultations.find { it.uuid == consultationId }
                    if (consultation != null) {
                        consultation.referenceGeneraleId = referenceId
                        updateConsultation(consultation)
                        println(
                                "Référence générale définie pour la consultation $consultationId: $referenceId"
                        )
                    }
                }
            } catch (e: Exception) {
                println("Erreur lors de la définition de la référence générale: ${e.message}")
            }
        }
    }

    /**
     * Ajoute une référence de maladie à une consultation
     *
     * @param consultationId L'ID de la consultation
     * @param referenceId L'ID de la référence de maladie à ajouter
     */
    fun ajouterReferenceMaladie(consultationId: String, referenceId: String) {
        viewModelScope.launch {
            try {
                val animalActuel = _animal.value
                if (animalActuel != null) {
                    val consultation = animalActuel.consultations.find { it.uuid == consultationId }
                    if (consultation != null) {
                        consultation.ajouterReferenceMaladie(referenceId)
                        updateConsultation(consultation)
                        println(
                                "Référence de maladie ajoutée à la consultation $consultationId: $referenceId"
                        )
                    }
                }
            } catch (e: Exception) {
                println("Erreur lors de l'ajout de la référence de maladie: ${e.message}")
            }
        }
    }

    /**
     * Supprime une référence de maladie d'une consultation
     *
     * @param consultationId L'ID de la consultation
     * @param referenceId L'ID de la référence de maladie à supprimer
     */
    fun supprimerReferenceMaladie(consultationId: String, referenceId: String) {
        viewModelScope.launch {
            try {
                val animalActuel = _animal.value
                if (animalActuel != null) {
                    val consultation = animalActuel.consultations.find { it.uuid == consultationId }
                    if (consultation != null) {
                        consultation.supprimerReferenceMaladie(referenceId)
                        updateConsultation(consultation)
                        println(
                                "Référence de maladie supprimée de la consultation $consultationId: $referenceId"
                        )
                    }
                }
            } catch (e: Exception) {
                println("Erreur lors de la suppression de la référence de maladie: ${e.message}")
            }
        }
    }

    /**
     * Obtient toutes les références (générale + maladies) pour une consultation
     *
     * @param consultationId L'ID de la consultation
     * @return Liste des IDs des références associées
     */
    fun obtenirReferencesConsultation(consultationId: String): List<String> {
        val animalActuel = _animal.value
        if (animalActuel != null) {
            val consultation = animalActuel.consultations.find { it.uuid == consultationId }
            return consultation?.obtenirToutesReferences() ?: emptyList()
        }
        return emptyList()
    }

    /**
     * Filtre les références disponibles selon des critères
     *
     * @param pourMaladie true pour les références de maladie, false pour les références générales
     * @return Liste des références filtrées
     */
    fun filtrerReferences(pourMaladie: Boolean): List<ReferenceEv> {
        return _availableReferences.value.filter { reference -> reference.maladie == pourMaladie }
    }

    /** Met à jour la référence générale d'une consultation */
    fun updateConsultationReferenceGenerale(consultationId: String, referenceId: String?) {
        viewModelScope.launch {
            try {
                val consultation = _animal.value?.consultations?.find { it.uuid == consultationId }
                consultation?.let {
                    val updatedConsultation = it.copy(referenceGeneraleId = referenceId)
                    updateConsultation(updatedConsultation)
                }
            } catch (e: Exception) {
                println("Erreur lors de la mise à jour de la référence générale: ${e.message}")
            }
        }
    }

    /** Ajoute une référence de maladie à une consultation */
    fun addConsultationReferenceMaladie(consultationId: String, referenceId: String) {
        viewModelScope.launch {
            try {
                val consultation = _animal.value?.consultations?.find { it.uuid == consultationId }
                consultation?.let {
                    val newReferences = it.referencesMaladies.toMutableList()
                    if (!newReferences.contains(referenceId)) {
                        newReferences.add(referenceId)
                        val updatedConsultation = it.copy(referencesMaladies = newReferences)
                        updateConsultation(updatedConsultation)
                    }
                }
            } catch (e: Exception) {
                println("Erreur lors de l'ajout de la référence de maladie: ${e.message}")
            }
        }
    }

    /** Supprime une référence de maladie d'une consultation */
    fun removeConsultationReferenceMaladie(consultationId: String, referenceId: String) {
        viewModelScope.launch {
            try {
                val consultation = _animal.value?.consultations?.find { it.uuid == consultationId }
                consultation?.let {
                    val newReferences = it.referencesMaladies.toMutableList()
                    newReferences.remove(referenceId)
                    val updatedConsultation = it.copy(referencesMaladies = newReferences)
                    updateConsultation(updatedConsultation)
                }
            } catch (e: Exception) {
                println("Erreur lors de la suppression de la référence de maladie: ${e.message}")
            }
        }
    }

    /** Ouvre la vue plein écran d'édition de consultation */
    fun openFullScreenEdit() {
        _showFullScreenEdit.value = true
    }

    /** Ferme la vue plein écran d'édition de consultation */
    fun closeFullScreenEdit() {
        _showFullScreenEdit.value = false
    }

    /** Prépare l'édition d'une consultation en plein écran */
    fun editConsultationFullScreen(consultation: ConsultationEv) {
        selectConsultation(consultation)
        startEditingConsultation()
        openFullScreenEdit()
    }

    /** Prépare la création d'une nouvelle consultation en plein écran */
    fun createNewConsultationFullScreen() {
        val currentMoment = Clock.System.now()
        val localDateTime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
        val currentDate = localDateTime.date
        prepareNewConsultation(currentDate)
        startEditingConsultation()
        openFullScreenEdit()
    }

    /** Sauvegarde depuis la vue plein écran et ferme */
    fun saveFromFullScreen(consultation: ConsultationEv) {
        viewModelScope.launch {
            try {
                if (_selectedConsultation.value?.uuid?.isEmpty() == true) {
                    // Nouvelle consultation
                    addConsultation(consultation)
                } else {
                    // Mise à jour d'une consultation existante
                    updateConsultation(consultation)
                }
                stopEditingConsultation()
                closeFullScreenEdit()

                // Recalculer les valeurs métaboliques après la sauvegarde
                calculerValeursMetaboliques(consultation)
            } catch (e: Exception) {
                println("Erreur lors de la sauvegarde de la consultation: ${e.message}")
            }
        }
    }

    // ===== MÉTHODES DE CALCUL MÉTABOLIQUE =====

    /**
     * Calcule les valeurs métaboliques (poids métabolique, BEE) pour une consultation donnée
     *
     * @param consultation La consultation pour laquelle calculer les valeurs
     */
    fun calculerValeursMetaboliques(consultation: ConsultationEv) {
        println(
                "🎯 DEBUG: calculerValeursMetaboliques APPELÉE pour consultation ${consultation.uuid}"
        )

        viewModelScope.launch {
            try {
                println(
                        "🚀 DEBUG: Début calcul valeurs métaboliques pour consultation ${consultation.uuid}"
                )

                // 1. Charger la référence appropriée
                val reference = obtenirReferenceActiveConsultation(consultation)
                _referenceUtilisee.value = reference

                if (reference == null) {
                    println("❌ DEBUG: Aucune référence trouvée pour la consultation")
                    resetCalculsMetaboliques()
                    return@launch
                }

                println("✅ DEBUG: Référence utilisée: ${reference.nom} (UUID: ${reference.uuid})")

                // 2. Calculer le poids métabolique
                val poidsMetabolique = calculerPoidsMetabolique(consultation, reference)
                _poidsMetabolique.value = poidsMetabolique
                println("📊 DEBUG: Poids métabolique calculé: $poidsMetabolique")

                // 3. Calculer le BEE (Besoin Énergétique Standard)
                println("📋 DEBUG: Début calcul BEE...")
                val bee = calculerBesoinEnergetiqueStandard(consultation, reference)
                _besoinEnergetiqueStandard.value = bee
                println("⚡ DEBUG: BEE calculé et stocké: $bee")

                if (bee == null) {
                    println("❌ DEBUG: BEE est null - vérifier l'équation BEE de la référence")
                }

                // 4. Calculer le besoin énergétique total en multipliant le BEE avec tous les
                // coefficients
                println("📋 DEBUG: Début calcul besoin énergétique total...")
                val besoinTotal = bee?.let { calculerBesoinEnergetiqueTotal(consultation, it) }
                _besoinEnergetiqueTotal.value = besoinTotal
                println("🎯 DEBUG: Besoin total calculé et stocké: $besoinTotal")

                println(
                        "🎯 DEBUG: Calculs terminés - PM: $poidsMetabolique, BEE: $bee, Total: $besoinTotal"
                )

                // Vérifier les valeurs dans les StateFlow
                println(
                        "📊 DEBUG: Vérification StateFlow - PM: ${_poidsMetabolique.value}, BEE: ${_besoinEnergetiqueStandard.value}, Total: ${_besoinEnergetiqueTotal.value}"
                )
            } catch (e: Exception) {
                println("💥 Erreur lors du calcul des valeurs métaboliques: ${e.message}")
                e.printStackTrace()
                resetCalculsMetaboliques()
            }
        }
    }

    /** Calcule le poids métabolique en utilisant l'équation BW de la référence */
    private fun calculerPoidsMetabolique(
            consultation: ConsultationEv,
            reference: ReferenceEv
    ): Double? {
        try {
            val poids = consultation.weight ?: return null
            val equationBW = reference.equationBW

            if (equationBW == null || equationBW.equationScript.isEmpty()) {
                println("DEBUG: Aucune équation BW définie pour la référence ${reference.nom}")
                return null
            }

            println(
                    "DEBUG: Calcul poids métabolique avec équation: ${equationBW.name} - ${equationBW.equationScript}"
            )

            // Créer la map des variables incluant BW et les variables supplémentaires
            val variables = mutableMapOf<String, Double>()
            variables["BW"] = poids.toDouble()

            // Ajouter les variables supplémentaires de la consultation
            consultation.suppVarp.forEach { suppVar ->
                suppVar.variable?.let { varKind ->
                    val valeur = suppVar.varue?.toDouble() ?: 0.0
                    variables[varKind.variable] = valeur
                    println("DEBUG: Variable supplémentaire pour BW: ${varKind.variable} = $valeur")
                }
            }

            // Mapper les variables avec leurs équivalents dans l'équation
            mapperVariablesEquation(variables)

            // Ajouter des valeurs par défaut pour les variables manquantes courantes
            val variablesManquantes =
                    ajouterVariablesParDefaut(variables, equationBW.equationScript)

            val resultat = ExpressionEvaluator.evaluer(equationBW.equationScript, variables)

            println("DEBUG: Poids métabolique calculé: $resultat kg^0.75")
            return resultat
        } catch (e: Exception) {
            println("ERROR: Erreur lors du calcul du poids métabolique: ${e.message}")
            return null
        }
    }

    /** Calcule le besoin énergétique standard en utilisant l'équation BEE de la référence */
    private fun calculerBesoinEnergetiqueStandard(
            consultation: ConsultationEv,
            reference: ReferenceEv
    ): Double? {
        try {
            val poids = consultation.weight ?: return null
            val equationBEE = reference.equationBEE

            if (equationBEE == null || equationBEE.equationScript.isEmpty()) {
                println("DEBUG: Aucune équation BEE définie pour la référence ${reference.nom}")
                return null
            }

            // Validation du type de poids et conversion sécurisée
            val poidsDouble: Double =
                    try {
                        when (poids) {
                            is Double -> poids
                            is Float -> poids.toDouble()
                            is Int -> poids.toDouble()
                            is String -> poids.toDouble()
                            else -> {
                                println("ERROR: Type de poids non supporté: ${poids::class}")
                                return null
                            }
                        }
                    } catch (e: NumberFormatException) {
                        println("ERROR: Conversion du poids impossible: $poids")
                        return null
                    }

            println(
                    "DEBUG: Calcul BEE avec équation: ${equationBEE.name} - ${equationBEE.equationScript}"
            )

            // Créer la map des variables incluant BW et les variables supplémentaires
            val variables = mutableMapOf<String, Double>()
            variables["BW"] = poidsDouble
            println("🔍 DEBUG BEE: Équation à évaluer: ${equationBEE.equationScript}")
            println("🔍 DEBUG BEE: Variables de base - BW = $poidsDouble")

            // Ajouter les variables supplémentaires de la consultation
            println(
                    "🔍 DEBUG BEE: Nombre de variables supplémentaires: ${consultation.suppVarp.size}"
            )
            consultation.suppVarp.forEach { suppVar ->
                suppVar.variable?.let { varKind ->
                    val valeur = suppVar.varue?.toDouble() ?: 0.0
                    variables[varKind.variable] = valeur
                    println(
                            "🔍 DEBUG BEE: Variable supplémentaire ajoutée: ${varKind.variable} = $valeur"
                    )
                }
            }

            // Mappe les variables avec leurs équivalents dans les équations
            mapperVariablesEquation(variables)

            // Ajouter des valeurs par défaut pour les variables manquantes courantes
            println("🔍 DEBUG BEE: Variables avant ajout par défaut: ${variables.keys}")
            val variablesManquantes =
                    ajouterVariablesParDefaut(variables, equationBEE.equationScript)
            if (variablesManquantes.isNotEmpty()) {
                println("WARNING: Variables manquantes dans l'équation: $variablesManquantes")
                // Ne pas arrêter l'exécution, mais logger pour le debugging
            }
            println("🔍 DEBUG BEE: Variables finales avant évaluation: $variables")

            val resultat = ExpressionEvaluator.evaluer(equationBEE.equationScript, variables)

            if (resultat == null) {
                println("ERROR: Échec de l'évaluation de l'équation BEE")
                return null
            }

            println("DEBUG: BEE calculé: $resultat kcal/jour")
            return resultat
        } catch (e: Exception) {
            println("ERROR: Erreur lors du calcul du BEE: ${e.message}")
            e.printStackTrace() // Ajout pour debugging
            return null
        }
    }

    /**
     * Calcule le besoin énergétique total en multipliant le BEE par tous les coefficients K et le
     * coefficient d'ajustement
     */
    private fun calculerBesoinEnergetiqueTotal(consultation: ConsultationEv, bee: Double): Double {
        // Récupération des coefficients K de la consultation
        val k1 = consultation.k1Value?.toDouble() ?: 1.0
        val k2 = consultation.k2Value?.toDouble() ?: 1.0
        val k3 = consultation.k3Value?.toDouble() ?: 1.0
        val k4 = consultation.k4Value?.toDouble() ?: 1.0
        val k5 = consultation.k5Value?.toDouble() ?: 1.0
        val coefficientAjustement = consultation.coefficientAjustement?.toDouble() ?: 1.0

        // Calcul du besoin total
        val besoinTotal = bee * k1 * k2 * k3 * k4 * k5 * coefficientAjustement

        println(
                "DEBUG: Calcul besoin total - BEE: $bee, K1: $k1, K2: $k2, K3: $k3, K4: $k4, K5: $k5, Coeff: $coefficientAjustement"
        )
        println("DEBUG: Besoin énergétique total calculé: $besoinTotal kcal/jour")

        return besoinTotal
    }

    /**
     * Obtient la référence active pour une consultation (générale ou première référence de maladie)
     */
    private suspend fun obtenirReferenceActiveConsultation(
            consultation: ConsultationEv
    ): ReferenceEv? {
        return try {
            // Priorité 1: Référence générale
            consultation.referenceGeneraleId?.let { referenceId ->
                val reference = databaseReferenceEvRepository.getReferenceEvById(referenceId)
                if (reference != null) {
                    println("DEBUG: Référence générale trouvée: ${reference.nom}")
                    return reference
                }
            }

            // Priorité 2: Première référence de maladie
            consultation.referencesMaladies.firstOrNull()?.let { referenceId ->
                val reference = databaseReferenceEvRepository.getReferenceEvById(referenceId)
                if (reference != null) {
                    println("DEBUG: Référence de maladie trouvée: ${reference.nom}")
                    return reference
                }
            }

            println("DEBUG: Aucune référence trouvée pour la consultation")
            null
        } catch (e: Exception) {
            println("Erreur lors de la récupération de la référence: ${e.message}")
            null
        }
    }

    /** Mappe les variables avec leurs équivalents dans les équations */
    private fun mapperVariablesEquation(variables: MutableMap<String, Double>) {
        // Mapping des variables courantes vers les noms utilisés dans les équations
        val mappings =
                mapOf(
                        "adultWeight" to "AW", // Adult Weight
                        "litterSize" to "L", // Litter size (taille de portée)
                        "gestationWeek" to
                                "wG", // Gestation week (peut être utilisé pour weight gain)
                        "lactationWeek" to
                                "wL", // Lactation week (peut être utilisé pour weight loss)
                        "bodyConditionScore" to "BCS" // Body Condition Score
                )

        mappings.forEach { (originalName, mappedName) ->
            variables[originalName]?.let { value ->
                variables[mappedName] = value
                println("🔄 DEBUG: Variable mappée: $originalName ($value) -> $mappedName")
            }
        }
    }

    /** Ajoute des valeurs par défaut pour les variables manquantes dans une équation */
    private fun ajouterVariablesParDefaut(
            variables: MutableMap<String, Double>,
            equationScript: String
    ): List<String> {
        // Extraire toutes les variables utilisées dans l'équation
        val variablesUtilisees = ExpressionEvaluator.extraireVariables(equationScript)
        println("🔍 DEBUG: Variables extraites de l'équation: $variablesUtilisees")

        // Valeurs par défaut pour les variables courantes
        val valeursParDefaut =
                mapOf(
                        "wG" to 0.0, // Weight gain (gain de poids)
                        "AW" to 0.0, // Adult weight (poids adulte)
                        "L" to 0.0, // Lactation
                        "wL" to 0.0, // Weight loss (perte de poids)
                        "BCS" to 5.0, // Body condition score
                        "REI" to 1.0, // Reproductive efficiency index
                        "AF" to 1.0, // Activity factor
                        "TE" to 1.0, // Thermic effect
                        "GE" to 1.0, // Growth efficiency
                        "ME" to 1.0 // Metabolizable energy
                )

        val variablesManquantes = mutableListOf<String>()

        // Ajouter les valeurs par défaut pour les variables manquantes
        variablesUtilisees.forEach { variable ->
            if (!variables.containsKey(variable)) {
                if (valeursParDefaut.containsKey(variable)) {
                    variables[variable] = valeursParDefaut[variable]!!
                    println(
                            "🎯 DEBUG: Variable par défaut ajoutée: $variable = ${valeursParDefaut[variable]}"
                    )
                } else {
                    println("⚠️ DEBUG: Variable $variable non trouvée dans les valeurs par défaut!")
                    variablesManquantes.add(variable)
                }
            } else {
                println(
                        "✅ DEBUG: Variable $variable déjà présente avec valeur: ${variables[variable]}"
                )
            }
        }

        return variablesManquantes
    }

    /** Remet à zéro tous les calculs métaboliques */
    private fun resetCalculsMetaboliques() {
        _poidsMetabolique.value = null
        _besoinEnergetiqueStandard.value = null
        _besoinEnergetiqueTotal.value = null
        _referenceUtilisee.value = null
    }

    /** Recalcule les valeurs métaboliques quand une consultation est sélectionnée */
    private fun recalculerValeursMetaboliques() {
        _selectedConsultation.value?.let { consultation ->
            calculerValeursMetaboliques(consultation)
        }
    }

    /** Met à jour le coefficient d'ajustement et recalcule les valeurs métaboliques */
    fun updateCoefficientAjustement(consultationId: String, nouveauCoefficient: Double) {
        viewModelScope.launch {
            try {
                val animalActuel = _animal.value
                if (animalActuel != null) {
                    val consultation = animalActuel.consultations.find { it.uuid == consultationId }
                    if (consultation != null) {
                        val consultationMiseAJour =
                                consultation.copy(coefficientAjustement = nouveauCoefficient)
                        updateConsultation(consultationMiseAJour)

                        // Recalculer les valeurs métaboliques
                        calculerValeursMetaboliques(consultationMiseAJour)

                        println("Coefficient d'ajustement mis à jour: $nouveauCoefficient")
                    }
                }
            } catch (e: Exception) {
                println("Erreur lors de la mise à jour du coefficient d'ajustement: ${e.message}")
            }
        }
    }

    /** Met à jour un coefficient K spécifique (K1-K5) et recalcule les valeurs métaboliques */
    fun updateCoefficient(
            consultationId: String,
            coefficientType: String,
            nouveauCoefficient: Float,
            description: String?
    ) {
        viewModelScope.launch {
            try {
                val animalActuel = _animal.value
                if (animalActuel != null) {
                    val consultation = animalActuel.consultations.find { it.uuid == consultationId }
                    if (consultation != null) {
                        val consultationMiseAJour =
                                when (coefficientType) {
                                    "k1" ->
                                            consultation.copy(
                                                    k1Value = nouveauCoefficient,
                                                    k1Id = description
                                            )
                                    "k2" ->
                                            consultation.copy(
                                                    k2Value = nouveauCoefficient,
                                                    k2Id = description
                                            )
                                    "k3" ->
                                            consultation.copy(
                                                    k3Value = nouveauCoefficient,
                                                    k3Id = description
                                            )
                                    "k4" ->
                                            consultation.copy(
                                                    k4Value = nouveauCoefficient,
                                                    k4Id = description
                                            )
                                    "k5" ->
                                            consultation.copy(
                                                    k5Value = nouveauCoefficient,
                                                    k5Id = description
                                            )
                                    else -> {
                                        println("Type de coefficient non reconnu: $coefficientType")
                                        return@launch
                                    }
                                }

                        updateConsultation(consultationMiseAJour)

                        // Recalculer les valeurs métaboliques
                        calculerValeursMetaboliques(consultationMiseAJour)

                        println(
                                "Coefficient $coefficientType mis à jour: $nouveauCoefficient (description: $description)"
                        )
                    }
                }
            } catch (e: Exception) {
                println(
                        "Erreur lors de la mise à jour du coefficient $coefficientType: ${e.message}"
                )
            }
        }
    }

    /**
     * Récupère un aliment complet (avec valeurs nutritionnelles) depuis le repository
     * @param uuid L'identifiant de l'aliment
     * @return AlimentEv complet ou null si non trouvé
     */
    suspend fun getAlimentComplet(uuid: String): AlimentEv? {
        return AlimentRepository.getAlimentByUUID(uuid)
    }
}
