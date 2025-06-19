package fr.vetbrain.vetnutri_mp.Enumer

/** Types d'expression des besoins nutritionnels */
enum class TypeExpressionBesoin(
        val id: String,
        val displayName: String,
        val unite: String,
        val description: String
) {
    PAR_KG(
            id = "par_kg",
            displayName = "Par kg de poids corporel",
            unite = "unité/kg",
            description = "Expression des besoins par kilogramme de poids corporel"
    ),
    PAR_KG_METABOLIQUE(
            id = "par_kg_metabolique",
            displayName = "Par kg de poids métabolique",
            unite = "unité/kg⁰·⁷⁵",
            description = "Expression des besoins par kilogramme de poids métabolique (BW^0.75)"
    ),
    PAR_MCAL_BEE(
            id = "par_mcal_bee",
            displayName = "Par Mcal de BEE",
            unite = "unité/Mcal BEE",
            description = "Expression des besoins par Mégacalorie de besoin énergétique d'entretien"
    ),
    PAR_MJ_BEE(
            id = "par_mj_bee",
            displayName = "Par MJ de BEE",
            unite = "unité/MJ BEE",
            description = "Expression des besoins par Mégajoule de besoin énergétique d'entretien"
    );

    companion object {
        /** Obtient un TypeExpressionBesoin par son ID */
        fun getById(id: String): TypeExpressionBesoin? {
            return entries.find { it.id == id }
        }

        /** Valeur par défaut */
        val DEFAULT = PAR_KG_METABOLIQUE
    }
}
