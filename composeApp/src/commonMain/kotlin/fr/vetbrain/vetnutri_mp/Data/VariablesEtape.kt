package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.VariableKind

/**
 * Résolution unique du poids et des variables d'énergie utilisés pour calculer les besoins d'une
 * ration.
 *
 * - Ration principale (y compris la ration parente d'un plan) : comportement historique — poids
 *   effectif de la consultation (`idealWeight ?: weight`) et variables de la consultation.
 * - Étape de plan évolutif (ration rangée sous une ration parente) : poids propre de l'étape
 *   (null = poids réel `weight`, jamais le poids idéal) et variables fusionnées, celles de l'étape
 *   étant prioritaires sur celles de la consultation.
 */
object VariablesEtape {

    /** Vrai si la ration est une étape de plan évolutif (rangée sous une ration parente). */
    @Suppress("UNUSED_PARAMETER")
    fun estEtape(consultation: ConsultationEv?, ration: Ration?): Boolean =
            ration?.refRationParente != null

    /** Poids (kg) à utiliser pour les calculs de la ration. */
    fun poidsEtape(consultation: ConsultationEv, ration: Ration?): Double? =
            if (estEtape(consultation, ration)) ration!!.poids ?: consultation.weight
            else consultation.effectiveWeight

    /** Variables supplémentaires : étape > consultation (une seule valeur par variable). */
    fun variablesFusionnees(
            consultation: ConsultationEv,
            ration: Ration?
    ): List<SupplementalvariableP> {
        if (!estEtape(consultation, ration) || ration!!.suppVarp.isEmpty()) {
            return consultation.suppVarp
        }
        val parVariable = LinkedHashMap<VariableKind, SupplementalvariableP>()
        consultation.suppVarp.forEach { sv -> sv.variable?.let { parVariable[it] = sv } }
        ration.suppVarp.forEach { sv -> sv.variable?.let { parVariable[it] = sv } }
        return parVariable.values.toList()
    }

    /**
     * Consultation « vue depuis l'étape » : copie dont le poids et les variables sont ceux de
     * l'étape, pour réutiliser tels quels les calculs écrits pour une consultation. Renvoie la
     * consultation elle-même si la ration n'est pas une étape (iso-fonctionnel).
     */
    fun consultationPourEtape(consultation: ConsultationEv, ration: Ration?): ConsultationEv {
        if (!estEtape(consultation, ration)) return consultation
        return consultation.copy(
                weight = poidsEtape(consultation, ration),
                idealWeight = null,
                suppVarp = variablesFusionnees(consultation, ration).toMutableList()
        )
    }

    /** Variables calculées par l'application : une saisie ne doit jamais les écraser. */
    private val variablesCalculees =
            setOf(VariableKind.BW, VariableKind.MW, VariableKind.BEE, VariableKind.BE)

    /**
     * Injecte les variables supplémentaires sous le nom utilisé dans les équations
     * (`VariableKind.label` : AW, wG, wL, L, D, CW...).
     */
    fun injecterVariables(
            variables: MutableMap<String, Double>,
            variablesSupp: List<SupplementalvariableP>
    ) {
        variablesSupp.forEach { sv ->
            sv.variable
                    ?.takeIf { it !in variablesCalculees }
                    ?.let { kind -> variables[kind.label] = sv.varue ?: 0.0 }
        }
    }

    /** Clé de cache stable pour un couple (poids, variables) d'étape. */
    fun signature(consultation: ConsultationEv, ration: Ration?): String {
        val poids = poidsEtape(consultation, ration)
        val vars =
                variablesFusionnees(consultation, ration)
                        .mapNotNull { sv -> sv.variable?.let { "${it.label}=${sv.varue}" } }
                        .sorted()
                        .joinToString(",")
        return "$poids|$vars"
    }
}
