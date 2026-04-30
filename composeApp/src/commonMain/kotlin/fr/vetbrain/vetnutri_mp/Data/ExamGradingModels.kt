package fr.vetbrain.vetnutri_mp.Data

import kotlinx.serialization.Serializable

@Serializable
data class ExamGradingRuleSet(
    val examId: String,
    val exerciseId: String,
    val label: String = "",
    val autoScoreMax: Double = 20.0,
    val ingredientRule: IngredientRule = IngredientRule(),
    val rationQuantityRule: MinMaxPointsRule = MinMaxPointsRule(points = 5),
    val energyDensityRule: MinMaxPointsRule = MinMaxPointsRule(points = 5),
    val nutrientRules: List<NutrientRule> = emptyList(),
    val referenceRule: ReferenceRule = ReferenceRule(points = 0),
    val adviceRule: AdviceRule = AdviceRule(points = 3),
    val customCriteria: List<FlexibleCriterionRule> = emptyList()
)

@Serializable
enum class FlexibleMetricKind {
    TOTAL_RATION_QUANTITY,
    ENERGY_DENSITY,
    BEE_KCAL,
    ENERGY_INTAKE_KCAL,
    PROTEIN_INTAKE_G,
    ANIMAL_WEIGHT_KG,
    NUTRIENT_TOTAL,
    RATION_COUNT,
    INGREDIENT_KEYWORDS,
    ADVICE_KEYWORDS,
    REFERENCE_ID
}

@Serializable
enum class RationScope {
    ALL,
    CURRENT_ONLY,
    PROPOSED_ONLY,
    FIRST_PROPOSED
}

@Serializable
data class FlexibleCriterionRule(
    val id: String = "",
    val label: String = "",
    val enabled: Boolean = true,
    val metric: FlexibleMetricKind = FlexibleMetricKind.TOTAL_RATION_QUANTITY,
    val rationScope: RationScope = RationScope.ALL,
    val nutrientLabel: String = "",
    val includes: List<String> = emptyList(),
    val excludes: List<String> = emptyList(),
    val requireAllIncludes: Boolean = true,
    val min: Double? = null,
    val max: Double? = null,
    val points: Double = 0.0
)

@Serializable
data class IngredientRule(
    val enabled: Boolean = true,
    val required: List<String> = emptyList(),
    val forbidden: List<String> = emptyList(),
    val points: Int = 5
)

@Serializable
data class MinMaxPointsRule(
    val enabled: Boolean = true,
    val min: Double? = null,
    val max: Double? = null,
    val points: Int = 0
)

@Serializable
data class NutrientRule(
    val nutrientLabel: String,
    val enabled: Boolean = true,
    val min: Double? = null,
    val max: Double? = null,
    val points: Int = 1
)

@Serializable
data class ReferenceRule(
    val enabled: Boolean = true,
    val allowedReferenceIds: List<String> = emptyList(),
    val points: Int = 0
)

@Serializable
data class AdviceRule(
    val enabled: Boolean = true,
    val requiredWords: List<String> = emptyList(),
    val forbiddenWords: List<String> = emptyList(),
    val points: Int = 0
)

@Serializable
data class ExamGradeDetail(
    val ingredientOk: Boolean = false,
    val ingredientPoints: Int = 0,
    val rationQuantity: Double = 0.0,
    val rationOk: Boolean = false,
    val rationPoints: Int = 0,
    val energyDensity: Double? = null,
    val energyOk: Boolean = false,
    val energyPoints: Int = 0,
    val nutrientResults: List<NutrientResult> = emptyList(),
    val referenceId: String? = null,
    val referenceOk: Boolean = false,
    val referencePoints: Int = 0,
    val adviceOk: Boolean = false,
    val advicePoints: Int = 0,
    val customCriteriaResults: List<FlexibleCriterionResult> = emptyList()
)

@Serializable
data class NutrientResult(
    val nutrientLabel: String,
    val value: Double,
    val ok: Boolean,
    val points: Int
)

@Serializable
data class FlexibleCriterionResult(
    val id: String,
    val label: String,
    val metric: FlexibleMetricKind,
    val rationScope: RationScope,
    val measuredNumber: Double? = null,
    val measuredText: String = "",
    val ok: Boolean = false,
    val pointsAwarded: Double = 0.0
)

@Serializable
data class ExamGrade(
    val examId: String,
    val exerciseId: String,
    val studentId: String,
    val animalId: String?,
    val animalName: String,
    val consultationId: String?,
    val autoScore: Double,
    val manualScore: Double?,
    val finalScore: Double,
    val detail: ExamGradeDetail
)

@Serializable
data class ExamCsvRow(
    val nom: String = "",
    val prenom: String = "",
    val email: String = "",
    val binId: String
)

@Serializable
data class ExamCopyImportEntry(
    val nom: String = "",
    val prenom: String = "",
    val email: String = "",
    val binId: String,
    val success: Boolean,
    val fromCache: Boolean = false,
    val error: String = "",
    val importedAtEpochMs: Long
)

@Serializable
data class ExamCopyImportSession(
    val sessionId: String = "",
    val examId: String,
    val exerciseId: String,
    val sourceFileName: String = "moodle.csv",
    val importedAtEpochMs: Long,
    val forceRefresh: Boolean = false,
    val offlineOnly: Boolean = false,
    val entries: List<ExamCopyImportEntry> = emptyList()
) {
    val total: Int get() = entries.size
    val successCount: Int get() = entries.count { it.success }
    val errorCount: Int get() = entries.count { !it.success }
}

@Serializable
data class RuleSnapshotEntry(
    val snapshotId: String,
    val savedAtEpochMs: Long,
    val rule: ExamGradingRuleSet
)
