package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.ExamGrade
import fr.vetbrain.vetnutri_mp.Data.ExamGradeDetail
import fr.vetbrain.vetnutri_mp.Data.ExamGradingRuleSet
import fr.vetbrain.vetnutri_mp.DataBase.ExamGradeEntity
import fr.vetbrain.vetnutri_mp.DataBase.ExamGradingDao
import fr.vetbrain.vetnutri_mp.DataBase.ExamGradingRuleEntity
import fr.vetbrain.vetnutri_mp.Utils.TimeStamp
import kotlinx.serialization.json.Json

class ExamGradingRepository(
    private val dao: ExamGradingDao
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    suspend fun getRule(examId: String, exerciseId: String): ExamGradingRuleSet? {
        val entity = dao.getRule(examId, exerciseId) ?: return null
        return json.decodeFromString(ExamGradingRuleSet.serializer(), entity.rulesJson)
    }

    suspend fun saveRule(rule: ExamGradingRuleSet) {
        val entity = ExamGradingRuleEntity(
            examId = rule.examId,
            exerciseId = rule.exerciseId,
            rulesJson = json.encodeToString(ExamGradingRuleSet.serializer(), rule),
            updatedAtEpochMs = TimeStamp()
        )
        dao.upsertRule(entity)
    }

    suspend fun getGrades(examId: String, exerciseId: String): List<ExamGrade> {
        return dao.getGrades(examId, exerciseId).map { it.toDomain(json) }
    }

    suspend fun saveGrades(grades: List<ExamGrade>) {
        val entities = grades.map { it.toEntity(json) }
        dao.upsertGrades(entities)
    }

    suspend fun clearGrades(examId: String, exerciseId: String) {
        dao.deleteGrades(examId, exerciseId)
    }
}

private fun ExamGradeEntity.toDomain(json: Json): ExamGrade {
    val detail =
        if (detailsJson.isNotBlank())
            json.decodeFromString(ExamGradeDetail.serializer(), detailsJson)
        else ExamGradeDetail()
    val manual = manualScore
    val final = manual ?: autoScore
    return ExamGrade(
        examId = examId,
        exerciseId = exerciseId,
        studentId = studentId,
        animalId = animalId,
        animalName = animalName,
        consultationId = consultationId,
        autoScore = autoScore,
        manualScore = manual,
        finalScore = final,
        detail = detail
    )
}

private fun ExamGrade.toEntity(json: Json): ExamGradeEntity {
    return ExamGradeEntity(
        examId = examId,
        exerciseId = exerciseId,
        studentId = studentId,
        animalId = animalId,
        animalName = animalName,
        consultationId = consultationId,
        autoScore = autoScore,
        manualScore = manualScore,
        finalScore = finalScore,
        detailsJson = json.encodeToString(ExamGradeDetail.serializer(), detail),
        updatedAtEpochMs = TimeStamp()
    )
}
