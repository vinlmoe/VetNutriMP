package fr.vetbrain.vetnutri_mp.Export

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Data.PreferencesEspece

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
        val preferences: PreferencesEspece? = null,
        val poidsAnimal: Double? = null,
        val poidsMetabolique: Double? = null,
        val besoinEnergetiqueEntretien: Double? = null,
        val bulletGraphImages: Map<String, Map<String, String>> = emptyMap()
)
