package fr.vetbrain.vetnutri_mp.Data

/**
 * Contexte du mode examen.
 * @param studentNumber Numéro de l'étudiant
 * @param studentId Identifiant de l'étudiant
 */
data class ExamSession(
    val studentNumber: String,
    val studentId: String
)
