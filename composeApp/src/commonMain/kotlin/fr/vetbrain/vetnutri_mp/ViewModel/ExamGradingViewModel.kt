package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Repository.ExamGradingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExamGradingViewModel(
    private val repository: ExamGradingRepository
) : ViewModel() {

    private val _rule = MutableStateFlow<ExamGradingRuleSet?>(null)
    val rule: StateFlow<ExamGradingRuleSet?> = _rule.asStateFlow()

    private val _grades = MutableStateFlow<List<ExamGrade>>(emptyList())
    val grades: StateFlow<List<ExamGrade>> = _grades.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() { _message.value = null }

    fun loadRule(examId: String, exerciseId: String) {
        viewModelScope.launch {
            val existing = repository.getRule(examId, exerciseId)
            _rule.value = existing ?: defaultRule(examId, exerciseId)
        }
    }

    fun updateRule(rule: ExamGradingRuleSet) {
        _rule.value = rule
    }

    fun saveRule() {
        val current = _rule.value ?: return
        viewModelScope.launch {
            repository.saveRule(current)
            _message.value = "✅ Règles sauvegardées"
        }
    }

    fun computeGrades(items: List<CrossConsultationAnalysisViewModel.ConsultationItem>, examId: String, exerciseId: String) {
        val ruleSet = _rule.value ?: defaultRule(examId, exerciseId)
        _rule.value = ruleSet
        val perAnimal = items.groupBy { it.animalId }
        val grades = perAnimal.values.mapNotNull { consultations ->
            val latest = consultations.maxByOrNull { it.rawDate ?: kotlinx.datetime.LocalDate(1900,1,1) } ?: return@mapNotNull null
            val detail = evaluate(ruleSet, latest)
            val maxScore = ruleSet.autoScoreMax.coerceAtLeast(0.0)
            val autoScore = detailTotal(detail).coerceIn(0.0, maxScore)
            ExamGrade(
                examId = examId,
                exerciseId = exerciseId,
                studentId = latest.studentIdentifier.orEmpty(),
                animalId = latest.animalId,
                animalName = latest.animalName,
                consultationId = latest.consultationId,
                autoScore = autoScore,
                manualScore = null,
                finalScore = autoScore,
                detail = detail
            )
        }
        _grades.value = grades
    }

    fun updateManualScore(studentId: String, manualScore: Double?) {
        _grades.value = _grades.value.map { grade ->
            if (grade.studentId == studentId) {
                val finalScore = manualScore ?: grade.autoScore
                grade.copy(manualScore = manualScore, finalScore = finalScore)
            } else grade
        }
    }

    fun saveGrades() {
        viewModelScope.launch {
            repository.saveGrades(_grades.value)
            _message.value = "✅ Notes sauvegardées"
        }
    }

    fun loadGrades(examId: String, exerciseId: String) {
        viewModelScope.launch {
            _grades.value = repository.getGrades(examId, exerciseId)
        }
    }

    private fun defaultRule(examId: String, exerciseId: String): ExamGradingRuleSet {
        return ExamGradingRuleSet(
            examId = examId,
            exerciseId = exerciseId,
            label = exerciseId,
            autoScoreMax = 20.0,
            ingredientRule = IngredientRule(points = 5),
            rationQuantityRule = MinMaxPointsRule(points = 5),
            energyDensityRule = MinMaxPointsRule(points = 5),
            nutrientRules = emptyList(),
            referenceRule = ReferenceRule(points = 0),
            adviceRule = AdviceRule(points = 3),
            customCriteria = emptyList()
        )
    }

    private fun evaluate(
        rule: ExamGradingRuleSet,
        item: CrossConsultationAnalysisViewModel.ConsultationItem
    ): ExamGradeDetail {
        val ingredientSet = item.rations.flatMap { it.ingredients }.map { it.name.lowercase() }.toSet()

        val ingredientOk =
            if (!rule.ingredientRule.enabled) false
            else {
                val required = rule.ingredientRule.required.map { it.lowercase().trim() }.filter { it.isNotBlank() }
                val forbidden = rule.ingredientRule.forbidden.map { it.lowercase().trim() }.filter { it.isNotBlank() }
                val requiredOk = required.isEmpty() || required.all { ingredientSet.contains(it) }
                val forbiddenOk = forbidden.isEmpty() || forbidden.none { ingredientSet.contains(it) }
                requiredOk && forbiddenOk && (required.isNotEmpty() || forbidden.isNotEmpty())
            }
        val ingredientPoints = if (ingredientOk) rule.ingredientRule.points else 0

        val rationQuantity = item.totalRationQuantity
        val rationOk =
            if (!rule.rationQuantityRule.enabled) false
            else {
                val min = rule.rationQuantityRule.min
                val max = rule.rationQuantityRule.max
                val hasBound = min != null || max != null
                if (!hasBound) false
                else (min == null || rationQuantity >= min) && (max == null || rationQuantity <= max)
            }
        val rationPoints = if (rationOk) rule.rationQuantityRule.points else 0

        val totalQty = item.rations.sumOf { it.quantity }
        val energyDensity =
            if (totalQty > 0.0) item.rations.sumOf { it.energyDensity * it.quantity } / totalQty else null
        val energyOk =
            if (!rule.energyDensityRule.enabled) false
            else {
                val min = rule.energyDensityRule.min
                val max = rule.energyDensityRule.max
                val hasBound = min != null || max != null
                if (!hasBound || energyDensity == null) false
                else (min == null || energyDensity >= min) && (max == null || energyDensity <= max)
            }
        val energyPoints = if (energyOk) rule.energyDensityRule.points else 0

        val nutrientTotals = mutableMapOf<String, Double>()
        item.rations.forEach { ration ->
            ration.nutrientValues.forEach { (label, value) ->
                nutrientTotals[label] = (nutrientTotals[label] ?: 0.0) + value
            }
        }
        val nutrientResults = rule.nutrientRules.map { ruleItem ->
            val value = nutrientTotals[ruleItem.nutrientLabel] ?: 0.0
            val ok =
                if (!ruleItem.enabled) false
                else {
                    val min = ruleItem.min
                    val max = ruleItem.max
                    val hasBound = min != null || max != null
                    if (!hasBound) false
                    else (min == null || value >= min) && (max == null || value <= max)
                }
            NutrientResult(
                nutrientLabel = ruleItem.nutrientLabel,
                value = value,
                ok = ok,
                points = if (ok) ruleItem.points else 0
            )
        }

        val referenceOk =
            if (!rule.referenceRule.enabled) false
            else {
                val allowed = rule.referenceRule.allowedReferenceIds.map { it.trim() }.filter { it.isNotBlank() }
                allowed.isNotEmpty() && item.referenceId != null && allowed.contains(item.referenceId)
            }
        val referencePoints = if (referenceOk) rule.referenceRule.points else 0

        val adviceText = item.adviceText.lowercase()
        val adviceOk =
            if (!rule.adviceRule.enabled) false
            else {
                val required = rule.adviceRule.requiredWords.map { it.lowercase().trim() }.filter { it.isNotBlank() }
                val forbidden = rule.adviceRule.forbiddenWords.map { it.lowercase().trim() }.filter { it.isNotBlank() }
                val requiredOk = required.isEmpty() || required.all { adviceText.contains(it) }
                val forbiddenOk = forbidden.isEmpty() || forbidden.none { adviceText.contains(it) }
                requiredOk && forbiddenOk && (required.isNotEmpty() || forbidden.isNotEmpty())
            }
        val advicePoints = if (adviceOk) rule.adviceRule.points else 0

        val customCriteriaResults = rule.customCriteria.map { criterion ->
            evaluateCustomCriterion(criterion, item)
        }

        return ExamGradeDetail(
            ingredientOk = ingredientOk,
            ingredientPoints = ingredientPoints,
            rationQuantity = rationQuantity,
            rationOk = rationOk,
            rationPoints = rationPoints,
            energyDensity = energyDensity,
            energyOk = energyOk,
            energyPoints = energyPoints,
            nutrientResults = nutrientResults,
            referenceId = item.referenceId,
            referenceOk = referenceOk,
            referencePoints = referencePoints,
            adviceOk = adviceOk,
            advicePoints = advicePoints,
            customCriteriaResults = customCriteriaResults
        )
    }

    private fun detailTotal(detail: ExamGradeDetail): Double {
        val nutrientPoints = detail.nutrientResults.sumOf { it.points }
        val customPoints = detail.customCriteriaResults.sumOf { it.pointsAwarded }
        return (detail.ingredientPoints + detail.rationPoints + detail.energyPoints + nutrientPoints + detail.referencePoints + detail.advicePoints).toDouble() + customPoints
    }

    private fun evaluateCustomCriterion(
        criterion: FlexibleCriterionRule,
        item: CrossConsultationAnalysisViewModel.ConsultationItem
    ): FlexibleCriterionResult {
        val criterionId = criterion.id.ifBlank { "${criterion.metric.name}:${criterion.label}" }
        if (!criterion.enabled) {
            return FlexibleCriterionResult(
                id = criterionId,
                label = criterion.label,
                metric = criterion.metric,
                rationScope = criterion.rationScope
            )
        }

        val rations = selectRationsByScope(item, criterion.rationScope)
        val ingredientText = rations.flatMap { ration -> ration.ingredients }.joinToString(" | ") { ingredient -> ingredient.name }
        val adviceText = item.adviceText
        val referenceId = item.referenceId.orEmpty()

        return when (criterion.metric) {
            FlexibleMetricKind.TOTAL_RATION_QUANTITY -> {
                val value = rations.sumOf { it.quantity }
                buildNumericResult(criterionId, criterion, value)
            }
            FlexibleMetricKind.ENERGY_DENSITY -> {
                val totalQty = rations.sumOf { it.quantity }
                val value = if (totalQty > 0.0) rations.sumOf { it.energyDensity * it.quantity } / totalQty else null
                buildNumericResult(criterionId, criterion, value)
            }
            FlexibleMetricKind.NUTRIENT_TOTAL -> {
                val nutrient = criterion.nutrientLabel.trim()
                val value = if (nutrient.isBlank()) null else rations.sumOf { it.nutrientValues[nutrient] ?: 0.0 }
                buildNumericResult(criterionId, criterion, value)
            }
            FlexibleMetricKind.RATION_COUNT -> {
                val value = rations.size.toDouble()
                buildNumericResult(criterionId, criterion, value)
            }
            FlexibleMetricKind.INGREDIENT_KEYWORDS -> {
                buildTextResult(criterionId, criterion, ingredientText)
            }
            FlexibleMetricKind.ADVICE_KEYWORDS -> {
                buildTextResult(criterionId, criterion, adviceText)
            }
            FlexibleMetricKind.REFERENCE_ID -> {
                buildTextResult(criterionId, criterion, referenceId, exactTokenMatch = true)
            }
        }
    }

    private fun selectRationsByScope(
        item: CrossConsultationAnalysisViewModel.ConsultationItem,
        scope: RationScope
    ): List<CrossConsultationAnalysisViewModel.RationSummary> {
        return when (scope) {
            RationScope.ALL -> item.rations
            RationScope.CURRENT_ONLY -> item.rations.filter { it.actual }
            RationScope.PROPOSED_ONLY -> item.rations.filter { !it.actual }
            RationScope.FIRST_PROPOSED -> item.rations.firstOrNull { !it.actual }?.let { listOf(it) } ?: emptyList()
        }
    }

    private fun buildNumericResult(
        id: String,
        criterion: FlexibleCriterionRule,
        value: Double?
    ): FlexibleCriterionResult {
        val hasBounds = criterion.min != null || criterion.max != null
        val ok = if (!hasBounds || value == null || !value.isFinite()) {
            false
        } else {
            (criterion.min == null || value >= criterion.min) &&
                (criterion.max == null || value <= criterion.max)
        }
        return FlexibleCriterionResult(
            id = id,
            label = criterion.label,
            metric = criterion.metric,
            rationScope = criterion.rationScope,
            measuredNumber = value,
            measuredText = value?.toString().orEmpty(),
            ok = ok,
            pointsAwarded = if (ok) criterion.points else 0.0
        )
    }

    private fun buildTextResult(
        id: String,
        criterion: FlexibleCriterionRule,
        sourceText: String,
        exactTokenMatch: Boolean = false
    ): FlexibleCriterionResult {
        val normalizedText = sourceText.lowercase()
        val includes = criterion.includes.map { it.trim().lowercase() }.filter { it.isNotBlank() }
        val excludes = criterion.excludes.map { it.trim().lowercase() }.filter { it.isNotBlank() }
        val includesOk =
            when {
                includes.isEmpty() -> true
                criterion.requireAllIncludes -> includes.all { tokenMatch(normalizedText, it, exactTokenMatch) }
                else -> includes.any { tokenMatch(normalizedText, it, exactTokenMatch) }
            }
        val excludesOk = excludes.none { tokenMatch(normalizedText, it, exactTokenMatch) }
        val hasCondition = includes.isNotEmpty() || excludes.isNotEmpty()
        val ok = hasCondition && includesOk && excludesOk
        return FlexibleCriterionResult(
            id = id,
            label = criterion.label,
            metric = criterion.metric,
            rationScope = criterion.rationScope,
            measuredText = sourceText,
            ok = ok,
            pointsAwarded = if (ok) criterion.points else 0.0
        )
    }

    private fun tokenMatch(text: String, token: String, exactTokenMatch: Boolean): Boolean {
        return if (exactTokenMatch) text.trim() == token else text.contains(token)
    }
}
