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
import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.ConsultationRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Utils.EquationEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/**
 * Sélection/filtrage de consultations multi-animaux pour analyse croisée (phase 1).
 * - Charge les consultations via AnimalRepository.
 * - Recherche texte, filtre espèce, sélection multiple.
 * - Données prêtes à être agrégées/analysées ensuite.
 */
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
            val speciesLabel: String,
            val rationCount: Int,
            val totalRationQuantity: Double,
            val rations: List<RationSummary>,
            val espece: Espece,
            val rawDate: LocalDate?,
            val keywordIds: List<String>
    )

    data class RationSummary(
            val rationId: String,
            val name: String,
            val actual: Boolean,
            val quantity: Double,
            val energyDensity: Double,
            val beeKcal: Double?,
            val proteins: Double,
            val lipids: Double,
            val ratioCaP: Double,
            val ratioOmega6Omega3: Double,
            val nutrientValues: Map<String, Double>,
            val ratioValues: Map<String, Double>,
            val animalName: String,
            val consultationDate: String,
            val referenceLabel: String?,
            val speciesLabel: String
    )

    data class SelectionSummary(
            val selectedCount: Int = 0,
            val distinctAnimals: Int = 0,
            val distinctReferences: Int = 0,
            val totalRations: Int = 0
    )

    data class NutrientAggregate(
            val consultationId: String,
            val animalName: String,
            val referenceLabel: String?,
            val nutrientLabel: String,
            val value: Double
    )

    private val _items = MutableStateFlow<List<ConsultationItem>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _speciesFilter = MutableStateFlow<Espece?>(null)
    private val _keywordFilter = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    private val _isLoading = MutableStateFlow(false)
    private val _availableKeywords = MutableStateFlow<List<ConsultationKeyword>>(emptyList())
    private val rationAnalyzer = RationAnalyzer()

    val isLoading: StateFlow<Boolean> = _isLoading
    val searchQuery: StateFlow<String> = _searchQuery
    val speciesFilter: StateFlow<Espece?> = _speciesFilter
    val keywordFilter: StateFlow<Set<String>> = _keywordFilter
    val selectedIds: StateFlow<Set<String>> = _selectedIds
    val availableKeywords: StateFlow<List<ConsultationKeyword>> = _availableKeywords

    val consultations: StateFlow<List<ConsultationItem>> =
            combine(_items, _searchQuery, _speciesFilter, _keywordFilter) { items, query, specie, keywords ->
                val q = query.trim().lowercase()
                items.filter { item ->
                    val matchText =
                            q.isBlank() ||
                                    item.animalName.lowercase().contains(q) ||
                                    item.dateLabel.lowercase().contains(q) ||
                                    item.objective.lowercase().contains(q) ||
                                    (item.referenceLabel?.lowercase()?.contains(q) == true)
                    val matchSpecies = specie == null || item.espece == specie
                    val matchKeywords =
                            keywords.isEmpty() || item.keywordIds.any { keywords.contains(it) }
                    matchText && matchSpecies && matchKeywords
                }
            }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val selectedCount: StateFlow<Int> =
            _selectedIds
                    .combine(_selectedIds) { ids, _ -> ids.size }
                    .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val summary: StateFlow<SelectionSummary> =
            combine(_selectedIds, consultations) { ids, visible ->
                val selected = visible.filter { ids.contains(it.consultationId) }
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

    fun getSelectedConsultations(): List<ConsultationItem> {
        val ids: Set<String> = _selectedIds.value
        return consultations.value.filter { ids.contains(it.consultationId) }
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

    /**
     * Placeholder : à remplacer par une logique de recommandations.
     */
    fun getProposedConsultations(): List<ConsultationItem> = emptyList()

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

    /**
     * Calcule un agrégat simplifié : total d'énergie (kcal) par consultation sélectionnée.
     * (Phase 2 : exemple minimal, à enrichir avec nutriments/ratios.)
     */
    suspend fun computeEnergyAggregates(): List<NutrientAggregate> {
        val selectedIds = _selectedIds.value
        val selectedItems = _items.value.filter { it.consultationId in selectedIds }
        return selectedItems.map { item ->
            // Énergie simplifiée = somme densité énergétique * quantités (si disponible)
            val energy =
                    item.rawDate // unused; placeholder to keep structure
            NutrientAggregate(
                    consultationId = item.consultationId,
                    animalName = item.animalName,
                    referenceLabel = item.referenceLabel,
                    nutrientLabel = CrossConsultationAnalysis.ENERGY_PLACEHOLDER.translate(),
                    value = item.rationCount.toDouble() // placeholder pour démonstration
            )
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
        val beeKcal = computeBeeKcal(consultation, referenceEv)
        val dateLabel =
                consultation.date?.toString()
                        ?: CrossConsultationAnalysis.DATE_UNKNOWN.translate()
        val rationSummaries =
                consultation.rations.map { ration ->
                    val analyse = rationAnalyzer.analyserRation(ration, consultation)
                    val nutrientValues = buildNutrientValues(ration, referenceEv)
                    val proteins = nutrientValues[NutrientMain.PROTEINE.label] ?: 0.0
                    val lipids = nutrientValues[NutrientMain.LIPIDE.label] ?: 0.0
                    val ratioCaP = analyse.ratios["Calcium/Phosphore"] ?: 0.0
                    val ratioOmega = analyse.ratios["Oméga-6/Oméga-3"] ?: 0.0
                    val ratioValues = buildRatioValues(nutrientValues, analyse.ratios)
                    val energyDensity =
                            ration.getDensiteEnergetiqueMoyenne(referenceEv, equationRepository)

                    val qty = ration.getQuantiteTotale()
                    val energyTotalKcal = energyDensity * qty / 100.0
                    val proteinsPerMcal =
                            if (beeKcal != null && beeKcal > 0) proteins / (beeKcal / 1000.0)
                            else if (energyTotalKcal > 0) proteins / energyTotalKcal * 1000.0
                            else 0.0
                    val lipidsPerMcal =
                            if (beeKcal != null && beeKcal > 0) lipids / (beeKcal / 1000.0)
                            else if (energyTotalKcal > 0) lipids / energyTotalKcal * 1000.0
                            else 0.0

                    RationSummary(
                            rationId = ration.uuid,
                            name =
                                    ration.name.ifBlank {
                                        CrossConsultationAnalysis.RATION_FALLBACK.translate()
                                    },
                            actual = ration.actual,
                            quantity = ration.getQuantiteTotale(),
                            energyDensity = energyDensity,
                            beeKcal = beeKcal,
                            proteins = proteins,
                            lipids = lipids,
                            ratioCaP = ratioCaP,
                            ratioOmega6Omega3 = ratioOmega,
                            nutrientValues = nutrientValues,
                            ratioValues = ratioValues,
                            animalName = animal.nom.ifBlank { "Animal sans nom" },
                            consultationDate = dateLabel,
                            referenceLabel = refLabel,
                            speciesLabel = animal.getEspece().label
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
                speciesLabel = animal.getEspece().label,
                rationCount = consultation.rations.size,
                totalRationQuantity = totalQuantity,
                rations = rationSummaries,
                espece = animal.getEspece(),
                rawDate = consultation.date,
                keywordIds = consultation.keywordIds.toList()
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
