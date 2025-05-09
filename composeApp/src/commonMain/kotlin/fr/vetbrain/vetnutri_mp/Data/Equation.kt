package fr.vetbrain.vetnutri_mp.Data

/**
 * Classe représentant une équation mathématique utilisée pour les calculs nutritionnels
 *
 * @property uuid Identifiant unique de l'équation
 * @property name Nom de l'équation
 * @property description Description détaillée de l'équation
 * @property formula Formule mathématique sous forme de chaîne de caractères
 * @property code Code utilisé pour évaluer l'équation (peut être dans un format spécifique)
 * @property bib Référence bibliographique associée à l'équation
 */
data class Equation(
        val uuid: String = java.util.UUID.randomUUID().toString(),
        var name: String = "",
        var description: String = "",
        var formula: String = "",
        var code: String = "",
        var bib: BiblioRef? = null
) {
        override fun toString(): String {
                return if (name.isNotBlank()) {
                        name
                } else if (description.isNotBlank()) {
                        description
                } else if (formula.isNotBlank()) {
                        formula
                } else {
                        "Équation sans nom"
                }
        }
}
