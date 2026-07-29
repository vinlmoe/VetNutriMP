package fr.vetbrain.vetnutri_mp.Export

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository

enum class DocumentType {
    RATION_ANALYSIS,
    PRESCRIPTION
}

data class PractitionerInfo(
        val nom: String,
        val numeroOrdre: String,
        val adressePostale: String,
        val codePostal: String,
        val ville: String,
        val telephone: String,
        val email: String
)

data class ExportData(
        val animal: AnimalEv?,
        val ration: Ration?,
        val reference: ReferenceEv?,
        val conseils: List<String> = emptyList(),
        val title: String = "",
        val additionalText: String = "",
        val htmlSections: List<HtmlSection> = emptyList(),
        val rations: List<Ration> = emptyList(),
        val practitioner: PractitionerInfo? = null,
        // Mode d'expression des besoins (PAR_KG/PAR_KCAL/...) pour le formatage d'affichage —
        // n'intervient dans aucun calcul, seule ReferenceEv pilote les équations complémentaires.
        val typeExpressionBesoin: TypeExpressionBesoin? = null,
        val poidsAnimal: Double? = null,
        val poidsMetabolique: Double? = null,
        val besoinEnergetiqueEntretien: Double? = null,
        val bulletGraphImages: Map<String, Map<String, String>> = emptyMap(),
        val isLandscape: Boolean = false,
        // Bilan énergétique complet de la ration (RATION_ANALYSIS), pour reproduire à l'identique
        // les valeurs affichées dans RationsView.kt.
        val besoinEnergetiqueTotal: Double? = null,
        val besoinEnergetiqueStandard: Double? = null,
        val energieApportee: Double? = null,
        val energieAdditionnelle: Double? = null,
        val kCalcule: Double? = null,
        val kObserve: Double? = null,
        val pourcentageCouverture: Double? = null,
        val equationRepository: EquationRepository? = null,
        val referencesMaladies: List<ReferenceEv> = emptyList()
)
