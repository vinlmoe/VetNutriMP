package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.RationAnalysisScope
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate

/**
 * Agrégation de plusieurs rations d'une consultation en une ration virtuelle unique.
 *
 * Chaque ration du groupe contribue proportionnellement à son coefficient (`Ration.coef`) :
 * la quantité retenue pour un aliment est `quantite * coef`. Les aliments identiques présents
 * dans plusieurs rations sont fusionnés en une seule ligne dont la quantité est la somme des
 * quantités pondérées.
 *
 * Comme tous les calculs nutritionnels de `Ration` et `AlimentRation` sont linéaires en quantité
 * (`valeur * quantite / 100`), la ration agrégée fournit exactement la somme pondérée des apports
 * des rations du groupe, et sa densité énergétique correspond à la moyenne pondérée.
 *
 * La ration produite est un objet de travail en mémoire : elle n'est jamais persistée.
 */
object RationAggregator {

    /** UUID de la ration virtuelle regroupant les rations actuelles. */
    const val UUID_GROUPE_ACTUELLES = "groupe-rations-actuelles"

    /** UUID de la ration virtuelle regroupant les rations proposées. */
    const val UUID_GROUPE_PROPOSEES = "groupe-rations-proposees"

    /** Vrai si l'UUID correspond à une ration virtuelle issue d'une agrégation. */
    fun estRationGroupee(uuid: String?): Boolean =
            uuid == UUID_GROUPE_ACTUELLES || uuid == UUID_GROUPE_PROPOSEES

    /** UUID de la ration virtuelle associée au périmètre demandé, ou null hors mode groupe. */
    fun uuidPour(scope: RationAnalysisScope): String? =
            when (scope) {
                RationAnalysisScope.GROUPE_ACTUELLES -> UUID_GROUPE_ACTUELLES
                RationAnalysisScope.GROUPE_PROPOSEES -> UUID_GROUPE_PROPOSEES
                RationAnalysisScope.RATION_UNIQUE -> null
            }

    /**
     * Rations de la consultation appartenant au groupe demandé.
     *
     * @return la liste vide pour [RationAnalysisScope.RATION_UNIQUE] ou une consultation absente.
     */
    fun rationsDuGroupe(
            consultation: ConsultationEv?,
            scope: RationAnalysisScope
    ): List<Ration> {
        if (!scope.estGroupe) return emptyList()
        val rations = consultation?.rations ?: return emptyList()
        return rations.filter { it.actual == scope.cibleRationsActuelles }
    }

    /**
     * Construit la ration virtuelle agrégeant toutes les rations du groupe demandé.
     *
     * @return null si le groupe ne contient aucune ration.
     */
    fun agreger(consultation: ConsultationEv?, scope: RationAnalysisScope): Ration? {
        val rations = rationsDuGroupe(consultation, scope)
        if (rations.isEmpty()) return null
        return agregerRations(rations, scope, consultation?.uuid ?: "")
    }

    /**
     * Construit la ration virtuelle à partir d'une liste de rations déjà filtrée.
     *
     * @param rations Les rations à agréger (au moins une).
     * @param scope Le périmètre d'agrégation (détermine nom et drapeau `actual` du résultat).
     * @param idConsult Identifiant de la consultation d'origine.
     */
    fun agregerRations(
            rations: List<Ration>,
            scope: RationAnalysisScope,
            idConsult: String = ""
    ): Ration? {
        if (rations.isEmpty()) return null

        // Fusion des aliments : une entrée par aliment sous-jacent, quantités pondérées cumulées
        val alimentsFusionnes = LinkedHashMap<String, AlimentRation>()
        rations.forEach { ration ->
            val coefficient = coefficientEffectif(ration)
            ration.alimentMutableList.forEach { alimentRation ->
                val cle = cleFusion(alimentRation)
                val quantitePonderee = alimentRation.quantite * coefficient
                val existant = alimentsFusionnes[cle]
                alimentsFusionnes[cle] =
                        if (existant == null) {
                            alimentRation.copy(quantite = quantitePonderee, proportion = 0.0)
                        } else {
                            existant.copy(quantite = existant.quantite + quantitePonderee)
                        }
            }
        }

        // Recalcul des proportions sur la base des quantités agrégées
        val quantiteTotale = alimentsFusionnes.values.sumOf { it.quantite }
        val aliments =
                alimentsFusionnes
                        .values
                        .map { aliment ->
                            val proportion =
                                    if (quantiteTotale > 0.0) {
                                        (aliment.quantite / quantiteTotale) * 100.0
                                    } else 0.0
                            aliment.copy(proportion = proportion)
                        }
                        .toMutableList()

        val estActuelle = scope.cibleRationsActuelles
        return Ration(
                uuid = uuidPour(scope) ?: UUID_GROUPE_PROPOSEES,
                idConsult = idConsult,
                name = nomGroupe(scope),
                coef = 1.0,
                actual = estActuelle,
                number = 0,
                espece = rations.firstNotNullOfOrNull { it.espece },
                recette = false,
                description = descriptionGroupe(rations),
                alimentMutableList = aliments
        )
    }

    /** Libellé de la ration virtuelle, suffixé du nombre de rations agrégées. */
    fun nomGroupe(scope: RationAnalysisScope): String =
            when (scope) {
                RationAnalysisScope.GROUPE_ACTUELLES ->
                        translate(LocalizationKeys.Ration.GROUP_ACTUAL_NAME)
                RationAnalysisScope.GROUPE_PROPOSEES ->
                        translate(LocalizationKeys.Ration.GROUP_PROPOSED_NAME)
                RationAnalysisScope.RATION_UNIQUE -> ""
            }

    /**
     * Coefficient réellement appliqué à une ration lors de l'agrégation.
     *
     * Un coefficient nul exclut la ration du cumul ; une valeur invalide (négative, NaN, infinie)
     * retombe sur 1.0 pour ne pas fausser silencieusement l'analyse.
     */
    fun coefficientEffectif(ration: Ration): Double {
        val coef = ration.coef
        return if (coef.isFinite() && coef >= 0.0) coef else 1.0
    }

    /** Détail lisible des rations agrégées et de leur coefficient. */
    private fun descriptionGroupe(rations: List<Ration>): String =
            rations.joinToString(separator = ", ") { ration ->
                val nom = ration.name.ifBlank { translate(LocalizationKeys.Ration.NAME) }
                "$nom (x${formaterCoefficient(coefficientEffectif(ration))})"
            }

    private fun formaterCoefficient(coefficient: Double): String {
        val arrondi = kotlin.math.round(coefficient * 100.0) / 100.0
        return if (arrondi == arrondi.toLong().toDouble()) {
            arrondi.toLong().toString()
        } else {
            arrondi.toString()
        }
    }

    /**
     * Clé de fusion d'un aliment : on regroupe sur l'aliment sous-jacent afin qu'un même aliment
     * présent dans plusieurs rations n'apparaisse qu'une fois dans la ration agrégée.
     */
    private fun cleFusion(alimentRation: AlimentRation): String {
        val uuidUnif = alimentRation.uuidUnif
        if (uuidUnif.isNotBlank()) return uuidUnif
        val uuidAliment = alimentRation.aliment?.uuid
        if (!uuidAliment.isNullOrBlank()) return uuidAliment
        return alimentRation.uuid
    }
}
