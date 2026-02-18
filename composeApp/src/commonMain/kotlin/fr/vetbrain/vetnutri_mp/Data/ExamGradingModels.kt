package fr.vetbrain.vetnutri_mp.Data

import kotlinx.serialization.Serializable

@Serializable
data class ExamGradingRuleSet(
    val examId: String,
    val exerciseId: String,
    val ingredientRule: IngredientRule = IngredientRule(),
    val rationQuantityRule: MinMaxPointsRule = MinMaxPointsRule(points = 5),
    val energyDensityRule: MinMaxPointsRule = MinMaxPointsRule(points = 5),
    val nutrientRules: List<NutrientRule> = emptyList(),
    val referenceRule: ReferenceRule = ReferenceRule(points = 0),
    val adviceRule: AdviceRule = AdviceRule(points = 3)
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
    val advicePoints: Int = 0
)

@Serializable
data class NutrientResult(
    val nutrientLabel: String,
    val value: Double,
    val ok: Boolean,
    val points: Int
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
