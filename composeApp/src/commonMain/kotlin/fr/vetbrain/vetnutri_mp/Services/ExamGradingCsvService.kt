package fr.vetbrain.vetnutri_mp.Services

import fr.vetbrain.vetnutri_mp.Data.ExamGrade

class ExamGradingCsvService {
    fun exportGradesToCsv(grades: List<ExamGrade>): String {
        val headers = listOf(
            "examId",
            "exerciseId",
            "studentId",
            "animalId",
            "animalName",
            "consultationId",
            "autoScore",
            "manualScore",
            "finalScore",
            "ingredientOk",
            "ingredientPoints",
            "rationQuantity",
            "rationOk",
            "rationPoints",
            "energyDensity",
            "energyOk",
            "energyPoints",
            "referenceId",
            "referenceOk",
            "referencePoints",
            "adviceOk",
            "advicePoints",
            "nutrientDetails",
            "customCriteriaDetails"
        )

        val lines = mutableListOf(headers.joinToString(","))
        grades.forEach { grade ->
            val detail = grade.detail
            val nutrientDetails = detail.nutrientResults.joinToString("|") { n ->
                "${n.nutrientLabel}:${n.value}:${n.ok}:${n.points}"
            }
            val customDetails = detail.customCriteriaResults.joinToString("|") { c ->
                val metricValue = c.measuredNumber?.toString() ?: c.measuredText
                "${c.label}:${c.metric}:${c.rationScope}:${metricValue}:${c.ok}:${c.pointsAwarded}"
            }
            val row = listOf(
                grade.examId,
                grade.exerciseId,
                grade.studentId,
                grade.animalId ?: "",
                grade.animalName,
                grade.consultationId ?: "",
                grade.autoScore.toString(),
                grade.manualScore?.toString() ?: "",
                grade.finalScore.toString(),
                detail.ingredientOk.toString(),
                detail.ingredientPoints.toString(),
                detail.rationQuantity.toString(),
                detail.rationOk.toString(),
                detail.rationPoints.toString(),
                detail.energyDensity?.toString() ?: "",
                detail.energyOk.toString(),
                detail.energyPoints.toString(),
                detail.referenceId ?: "",
                detail.referenceOk.toString(),
                detail.referencePoints.toString(),
                detail.adviceOk.toString(),
                detail.advicePoints.toString(),
                nutrientDetails,
                customDetails
            ).joinToString(",") { escapeCsv(it) }
            lines.add(row)
        }
        return lines.joinToString("\n")
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
