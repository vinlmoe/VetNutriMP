package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationKeyword
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.RationAnalyzer
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.CrossConsultationAnalysis
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.ConsultationRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Utils.EquationEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.math.pow
import kotlin.math.sqrt
import fr.vetbrain.vetnutri_mp.Utils.Recherche

/**
 * Énergie totale (kcal) d'une ration à partir de sa densité énergétique. `Ration.getDensiteEnergetiqueMoyenne()`
 * renvoie déjà des kcal par gramme de ration (l'énergie de chaque ingrédient est mise à l'échelle
 * par sa quantité avant d'être divisée par la quantité totale) : pas de `/100` supplémentaire ici,
 * contrairement à une densité "pour 100g" classique. Extrait en fonction pure pour rester testable
 * indépendamment du ViewModel (qui dépend de `DatabaseReferenceEvRepository`, non substituable).
 */
internal fun energyTotalKcalFromDensity(densiteKcalParGramme: Double, quantiteTotaleGrammes: Double): Double =
        densiteKcalParGramme * quantiteTotaleGrammes

/**
 * Sélection/filtrage de consultations multi-animaux pour analyse croisée (phase 1).
 * - Charge les consultations via AnimalRepository.
 * - Recherche texte, filtre espèce, sélection multiple.
 * - Données prêtes à être agrégées/analysées ensuite.
 */
/**
 * Statistiques descriptives d'une série (écart-type de population). Les valeurs nulles ou négatives
 * sont des nutriments non renseignés : elles sont écartées. Null si aucune valeur ne reste.
 */
internal fun statistiquesDescriptives(nom: String, valeurs: List<Double>): CrossConsultationAnalysisViewModel.NutrientStat? {
    val retenues = valeurs.filter { it > 0.0 && it.isFinite() }
    if (retenues.isEmpty()) return null
    val moyenne = retenues.average()
    val ecartType = sqrt(retenues.map { (it - moyenne).pow(2) }.average())
    val triees = retenues.sorted()
    val mediane = if (triees.size % 2 == 0) (triees[triees.size / 2 - 1] + triees[triees.size / 2]) / 2.0 else triees[triees.size / 2]
    return CrossConsultationAnalysisViewModel.NutrientStat(
            name = nom, mean = moyenne, stdDev = ecartType, median = mediane,
            min = triees.first(), max = triees.last(), count = retenues.size
    )
}

/**
 * Compare, nutriment par nutriment, les rations actuelles aux rations proposées : statistiques de
 * chaque groupe et écart relatif des moyennes. Les nutriments absents des deux groupes sont omis.
 */
internal fun comparerStatistiques(
        actuelles: List<CrossConsultationAnalysisViewModel.RationSummary>,
        proposees: List<CrossConsultationAnalysisViewModel.RationSummary>
): List<CrossConsultationAnalysisViewModel.NutrientComparison> {
    val cles = (actuelles + proposees).flatMap { it.nutrientValues.keys }.distinct().sorted()
    return cles.mapNotNull { cle ->
        val a = statistiquesDescriptives(cle, actuelles.mapNotNull { it.nutrientValues[cle] })
        val b = statistiquesDescriptives(cle, proposees.mapNotNull { it.nutrientValues[cle] })
        if (a == null && b == null) null else CrossConsultationAnalysisViewModel.NutrientComparison(cle, a, b)
    }
}

class CrossConsultationAnalysisViewModel(
        private val animalRepository: AnimalRepository,
        private val consultationRepository: ConsultationRepository,
        private val referenceEvRepository: DatabaseReferenceEvRepository,
        private val equationRepository: EquationRepository
) : ViewModel() {

    data class ConsultationItem(
            val consultationId: String,
            val animalId: String,
            val animalName: String,
            val objective: String,
            val dateLabel: String,
            val referenceLabel: String?,
            val referenceId: String?,
            val speciesLabel: String,
            val rationCount: Int,
            val totalRationQuantity: Double,
            val rations: List<RationSummary>,
            val espece: Espece,
            val rawDate: LocalDate?,
            val keywordIds: List<String>,
            val examId: String? = null,
            val studentIdentifier: String? = null,
            val examExerciseId: String? = null,
            val adviceText: String = ""
    )

    data class RationSummary(
            val rationId: String,
            val name: String,
            val actual: Boolean,
            val quantity: Double,
            val energyTotalKcal: Double,
            val energyDensity: Double,
            val beeKcal: Double?,
            val animalWeightKg: Double,
            val animalMetabolicWeightKg: Double,
            val proteins: Double,
            val lipids: Double,
            val ratioCaP: Double,
            val ratioOmega6Omega3: Double,
            val nutrientValues: Map<String, Double>,
            val ratioValues: Map<String, Double>,
            val ingredients: List<IngredientSummary>,
            val animalName: String,
            val consultationDate: String,
            val referenceLabel: String?,
            val speciesLabel: String
    )

    data class IngredientSummary(
            val name: String,
            val quantity: Double
    )

    data class SelectionSummary(
            val selectedCount: Int = 0,
            val distinctAnimals: Int = 0,
            val distinctReferences: Int = 0,
            val totalRations: Int = 0
    )

    data class NutrientStat(
            val name: String,
            val mean: Double,
            val stdDev: Double,
            val median: Double,
            val min: Double,
            val max: Double,
            /** Nombre de rations où le nutriment est renseigné. */
            val count: Int = 0
    )

    /** Un nutriment : rations actuelles et proposées côte à côte. */
    data class NutrientComparison(
            val label: String,
            val actual: NutrientStat?,
            val proposed: NutrientStat?
    ) {
        /** Écart relatif (%) de la moyenne proposée à la moyenne actuelle. */
        val ecartPourcent: Double?
            get() {
                val a = actual?.mean ?: return null
                val b = proposed?.mean ?: return null
                return if (a > 0.0) 100.0 * (b - a) / a else null
            }
    }

    private data class FilterState(
            val items: List<ConsultationItem>,
            val query: String,
            val specie: Espece?,
            val keywords: Set<String>,
            val examId: String,
            val examExerciseId: String
    )

    private val _items = MutableStateFlow<List<ConsultationItem>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _speciesFilter = MutableStateFlow<Espece?>(null)
    private val _keywordFilter = MutableStateFlow<Set<String>>(emptySet())
    private val _examIdFilter = MutableStateFlow("")
    private val _examExerciseIdFilter = MutableStateFlow("")
    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    private val _isLoading = MutableStateFlow(false)
    private val _availableKeywords = MutableStateFlow<List<ConsultationKeyword>>(emptyList())
    private val rationAnalyzer = RationAnalyzer()

    val isLoading: StateFlow<Boolean> = _isLoading
    val searchQuery: StateFlow<String> = _searchQuery
    val speciesFilter: StateFlow<Espece?> = _speciesFilter
    val keywordFilter: StateFlow<Set<String>> = _keywordFilter
    val examIdFilter: StateFlow<String> = _examIdFilter
    val examExerciseIdFilter: StateFlow<String> = _examExerciseIdFilter
    val selectedIds: StateFlow<Set<String>> = _selectedIds
    val availableKeywords: StateFlow<List<ConsultationKeyword>> = _availableKeywords
    val allItems: StateFlow<List<ConsultationItem>> = _items

    val consultations: StateFlow<List<ConsultationItem>> =
            combine(
                            _items,
                            _searchQuery,
                            _speciesFilter,
                            _keywordFilter
                    ) { items, query, specie, keywords ->
                        FilterState(items, query, specie, keywords, "", "")
                    }
                    .combine(_examIdFilter) { state, examId ->
                        state.copy(examId = examId)
                    }
                    .combine(_examExerciseIdFilter) { state, exerciseId ->
                        state.copy(examExerciseId = exerciseId)
                    }
                    .map { state ->
                        val mots = Recherche.mots(state.query)
                        val qExamId = state.examId.trim()
                        val qExerciseId = state.examExerciseId.trim()
                        state.items.filter { item ->
                            val matchText =
                                    mots.isEmpty() ||
                                            Recherche.correspond(
                                                    mots,
                                                    Recherche.indexer(
                                                            item.animalName,
                                                            item.dateLabel,
                                                            item.objective,
                                                            item.referenceLabel,
                                                            item.examId,
                                                            item.studentIdentifier,
                                                            item.examExerciseId
                                                    )
                                            )
                            val matchSpecies = state.specie == null || item.espece == state.specie
                            val matchKeywords =
                                    state.keywords.isEmpty() ||
                                            item.keywordIds.any { state.keywords.contains(it) }
                            val matchExamPair =
                                    when {
                                        qExamId.isBlank() && qExerciseId.isBlank() -> true
                                        qExamId.isNotBlank() && qExerciseId.isNotBlank() ->
                                                item.examId.equals(qExamId, ignoreCase = true) &&
                                                        item.examExerciseId.equals(
                                                                qExerciseId,
                                                                ignoreCase = true
                                                        )
                                        qExamId.isNotBlank() ->
                                                item.examId.equals(qExamId, ignoreCase = true)
                                        else ->
                                                item.examExerciseId.equals(
                                                        qExerciseId,
                                                        ignoreCase = true
                                                )
                                    }
                            matchText && matchSpecies && matchKeywords && matchExamPair
                        }
                    }
                    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val selectedCount: StateFlow<Int> =
            _selectedIds
                    .combine(_selectedIds) { ids, _ -> ids.size }
                    .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val summary: StateFlow<SelectionSummary> =
            combine(_selectedIds, _items) { ids, items ->
                // Toutes les consultations sélectionnées, même masquées par un filtre :
                // ce sont elles que l'analyse utilise.
                val selected = items.filter { ids.contains(it.consultationId) }
                SelectionSummary(
                        selectedCount = selected.size,
                        distinctAnimals = selected.map { it.animalId }.toSet().size,
                        distinctReferences = selected.mapNotNull { it.referenceLabel }.toSet().size,
                        totalRations = selected.sumOf { it.rationCount }
                )
            }.stateIn(viewModelScope, SharingStarted.Eagerly, SelectionSummary())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setExamIdFilter(value: String) {
        _examIdFilter.value = value
    }

    fun setExamExerciseIdFilter(value: String) {
        _examExerciseIdFilter.value = value
    }

    fun setSpeciesFilter(espece: Espece?) {
        _speciesFilter.value = espece
    }

    fun toggleKeywordFilter(keywordId: String) {
        _keywordFilter.value =
                if (_keywordFilter.value.contains(keywordId)) {
                    _keywordFilter.value - keywordId
                } else {
                    _keywordFilter.value + keywordId
                }
    }

    fun clearKeywordFilter() {
        _keywordFilter.value = emptySet()
    }

    fun toggleSelection(id: String) {
        _selectedIds.value =
                if (_selectedIds.value.contains(id)) _selectedIds.value - id
                else _selectedIds.value + id
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun selectAllVisible(ids: List<String>) {
        _selectedIds.value = (_selectedIds.value + ids).toSet()
    }

    fun deselectAllVisible(ids: List<String>) {
        _selectedIds.value = _selectedIds.value - ids.toSet()
    }

    /**
     * Récupère les rations des consultations sélectionnées.
     *
     * @param actualOnly si true -> uniquement rations actuelles, si false -> uniquement proposées,
     *                   si null -> toutes.
     */
    fun getSelectedRations(actualOnly: Boolean?): List<RationSummary> {
        val ids: Set<String> = _selectedIds.value
        return _items.value
                .filter { ids.contains(it.consultationId) }
                .flatMap { it.rations }
                .filter { ration -> actualOnly == null || ration.actual == actualOnly }
    }

    /** Calcule les statistiques descriptives sur les nutriments des rations sélectionnées. */
    fun computeNutrientStats(actualOnly: Boolean?): List<NutrientStat> {
        val rations = getSelectedRations(actualOnly)
        if (rations.isEmpty()) return emptyList()
        val allKeys = rations.flatMap { it.nutrientValues.keys }.distinct().sorted()
        return allKeys.mapNotNull { key -> statistiquesDescriptives(key, rations.mapNotNull { it.nutrientValues[key] }) }
    }

    /** Rations actuelles et proposées de la sélection, comparées nutriment par nutriment. */
    fun compareNutrientStats(): List<NutrientComparison> =
            comparerStatistiques(getSelectedRations(actualOnly = true), getSelectedRations(actualOnly = false))

    /** Statistiques d'une série de valeurs déjà exprimées (graphique en cours). */
    fun describe(values: List<Double>): NutrientStat? = statistiquesDescriptives("", values)

    fun loadConsultations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _availableKeywords.value = consultationRepository.getAllKeywords()
                val animals: List<AnimalEv> = animalRepository.getAllAnimals()
                val items =
                        animals.flatMap { animal ->
                            consultationRepository
                                    .getConsultationsForAnimal(animal.uuid)
                                    .filter { it.referenceGeneraleId != null }
                                    .map { consult -> mapConsultation(animal, consult) }
                        }
                _items.value = items.sortedBy { it.rawDate ?: LocalDate(1900, 1, 1) }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun mapConsultation(
            animal: AnimalEv,
            consultation: ConsultationEv
    ): ConsultationItem {
        val refLabel =
                consultation.referenceGeneraleId?.let { refId ->
                    referenceEvRepository.getReferenceEvById(refId)?.nom
                }
        val referenceEv =
                consultation.referenceGeneraleId?.let {
                    referenceEvRepository.getReferenceEvById(it)
                }
        val beeConsultation = computeBeeKcal(consultation, referenceEv)
        val dateLabel =
                consultation.date?.toString()
                        ?: CrossConsultationAnalysis.DATE_UNKNOWN.translate()
        val rationSummaries =
                consultation.rations.map { ration ->
                    // Plan évolutif : poids, variables et BEE propres à l'étape
                    val consultationEtape =
                            fr.vetbrain.vetnutri_mp.Data.VariablesEtape.consultationPourEtape(
                                    consultation,
                                    ration
                            )
                    val beeKcal =
                            if (consultationEtape === consultation) beeConsultation
                            else computeBeeKcal(consultationEtape, referenceEv)
                    val analyse = rationAnalyzer.analyserRation(ration, consultationEtape)
                    val nutrientValues = buildNutrientValues(ration, referenceEv)
                    val proteins = nutrientValues[NutrientMain.PROTEINE.label] ?: 0.0
                    val lipids = nutrientValues[NutrientMain.LIPIDE.label] ?: 0.0
                    val ratioCaP = analyse.ratios["Calcium/Phosphore"] ?: 0.0
                    val ratioOmega = analyse.ratios["Oméga-6/Oméga-3"] ?: 0.0
                    val ratioValues = buildRatioValues(nutrientValues, analyse.ratios)
                    val energyDensity =
                            ration.getDensiteEnergetiqueMoyenne(referenceEv, equationRepository)

                    val qty = ration.getQuantiteTotale()
                    val energyTotalKcal = energyTotalKcalFromDensity(energyDensity, qty)
                    val weightKg =
                            consultationEtape.effectiveWeight?.toDouble()
                                    ?: consultationEtape.weight?.toDouble()
                                    ?: 0.0
                    val metabolicWeight =
                            if (weightKg > 0.0) weightKg.pow(0.75) else 0.0
                    val proteinsPerMcal =
                            if (beeKcal != null && beeKcal > 0) proteins / (beeKcal / 1000.0)
                            else if (energyTotalKcal > 0) proteins / energyTotalKcal * 1000.0
                            else 0.0
                    val lipidsPerMcal =
                            if (beeKcal != null && beeKcal > 0) lipids / (beeKcal / 1000.0)
                            else if (energyTotalKcal > 0) lipids / energyTotalKcal * 1000.0
                            else 0.0
                    val ingredientSummaries =
                            ration.alimentMutableList.map { alimentRation ->
                                IngredientSummary(
                                        name =
                                                alimentRation.aliment?.nom
                                                        ?: CrossConsultationAnalysis.INGREDIENT_NO_NAME.translate(),
                                        quantity = alimentRation.quantite
                                )
                            }

                    RationSummary(
                            rationId = ration.uuid,
                            name =
                                    ration.name.ifBlank {
                                        CrossConsultationAnalysis.RATION_FALLBACK.translate()
                                    },
                            actual = ration.actual,
                            quantity = ration.getQuantiteTotale(),
                            energyTotalKcal = energyTotalKcal,
                            energyDensity = energyDensity,
                            beeKcal = beeKcal,
                            animalWeightKg = weightKg,
                            animalMetabolicWeightKg = metabolicWeight,
                            proteins = proteins,
                            lipids = lipids,
                            ratioCaP = ratioCaP,
                            ratioOmega6Omega3 = ratioOmega,
                            nutrientValues = nutrientValues,
                            ratioValues = ratioValues,
                            ingredients = ingredientSummaries,
                            animalName = animal.nom.ifBlank { CrossConsultationAnalysis.ANIMAL_NO_NAME.translate() },
                            consultationDate = dateLabel,
                            referenceLabel = refLabel,
                            speciesLabel = animal.getEspece().translateEnum()
                    )
                }
        val totalQuantity = rationSummaries.sumOf { it.quantity }
        return ConsultationItem(
                consultationId = consultation.uuid,
                animalId = animal.uuid,
                animalName =
                        animal.nom.ifBlank {
                            CrossConsultationAnalysis.ANIMAL_NO_NAME.translate()
                        },
                objective =
                        consultation.objectConsult.ifBlank {
                            CrossConsultationAnalysis.OBJECTIVE_NONE.translate()
                        },
                dateLabel = dateLabel,
                referenceLabel = refLabel,
                referenceId = consultation.referenceGeneraleId,
                speciesLabel = animal.getEspece().translateEnum(),
                rationCount = consultation.rations.size,
                totalRationQuantity = totalQuantity,
                rations = rationSummaries,
                espece = animal.getEspece(),
                rawDate = consultation.date,
                keywordIds = consultation.keywordIds.toList(),
                examId = animal.examStudentNumber,
                studentIdentifier = animal.examStudentId,
                examExerciseId = animal.examExerciseId,
                adviceText =
                        listOf(
                                        consultation.prescriptionAdditionalText,
                                        consultation.cRendu,
                                        consultation.observation
                                )
                                .joinToString(" ")
        )
    }

    private fun computeBeeKcal(
            consultation: ConsultationEv,
            referenceEv: fr.vetbrain.vetnutri_mp.Data.ReferenceEv?
    ): Double? {
        val poids: Double =
                consultation.effectiveWeight?.toDouble()
                        ?: consultation.weight?.toDouble()
                        ?: return null

        val equationScript = referenceEv?.equationBEE?.equationScript
        val beeFromReference =
                equationScript
                        ?.takeIf { it.isNotBlank() }
                        ?.let {
                            EquationEvaluator.evaluerPourAnimal(
                                    expression = it,
                                    poidsCorps = poids,
                                    variablesSupp = consultation.suppVarp
                            )
                        }
        val bee =
                beeFromReference?.takeIf { it > 0.0 }
                        ?: EquationEvaluator.calculerBesoinEnergetiqueBase(poids)

        return bee
    }

    private fun buildNutrientValues(
            ration: Ration,
            referenceEv: fr.vetbrain.vetnutri_mp.Data.ReferenceEv?
    ): Map<String, Double> {
        val result = mutableMapOf<String, Double>()
        NutrientMain.entries.forEach { nutrient ->
            result[nutrient.label] = ration.getNutrient(nutrient, referenceEv) ?: 0.0
        }
        NutrientMacro.entries.forEach { nutrient ->
            result[nutrient.label] = ration.getNutrient(nutrient, referenceEv) ?: 0.0
        }
        NutrientMin.entries.forEach { nutrient ->
            result[nutrient.label] = ration.getNutrient(nutrient, referenceEv) ?: 0.0
        }
        NutrientVitam.entries.forEach { nutrient ->
            result[nutrient.label] = ration.getNutrient(nutrient, referenceEv) ?: 0.0
        }
        NutrientLipid.entries.forEach { nutrient ->
            result[nutrient.label] = ration.getNutrient(nutrient, referenceEv) ?: 0.0
        }
        return result
    }

    private fun buildRatioValues(
            nutrientValues: Map<String, Double>,
            computedRatios: Map<String, Double>
    ): Map<String, Double> {
        val result = mutableMapOf<String, Double>()
        NutrientAnalysis.entries.forEach { ratio ->
            result[ratio.label] =
                    when (ratio.label) {
                        "KNA" -> computedRatios["Potassium/Sodium"] ?: safeDiv(
                                nutrientValues[NutrientMacro.K.label],
                                nutrientValues[NutrientMacro.NA.label]
                        )
                        "CAP" -> computedRatios["Calcium/Phosphore"] ?: safeDiv(
                                nutrientValues[NutrientMacro.CAL.label],
                                nutrientValues[NutrientMacro.PHOS.label]
                        )
                        "O6O3" -> computedRatios["Oméga-6/Oméga-3"] ?: safeDiv(
                                nutrientValues[NutrientLipid.O6.label],
                                nutrientValues[NutrientLipid.O3.label]
                        )
                        "ZNCU" -> computedRatios["Zinc/Cuivre"] ?: safeDiv(
                                nutrientValues[NutrientMin.ZN.label],
                                nutrientValues[NutrientMin.CU.label]
                        )
                        "PROTP" -> computedRatios["Protéines/Phosphore"] ?: safeDiv(
                                nutrientValues[NutrientMain.PROTEINE.label],
                                nutrientValues[NutrientMacro.PHOS.label]
                        )
                        else -> 0.0
                    }
        }
        return result
    }

    private fun safeDiv(a: Double?, b: Double?): Double {
        val numerator = a ?: return 0.0
        val denominator = b ?: return 0.0
        return if (denominator > 0.0) numerator / denominator else 0.0
    }
}
