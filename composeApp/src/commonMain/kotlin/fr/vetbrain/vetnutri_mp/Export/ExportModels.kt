package fr.vetbrain.vetnutri_mp.Export

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv

enum class DocumentType {
    RATION_ANALYSIS,
    PRESCRIPTION
}

data class ExportData(
        val animal: AnimalEv?,
        val ration: Ration?,
        val reference: ReferenceEv?,
        val conseils: List<String> = emptyList(),
        val title: String = "",
        val additionalText: String = "",
        val htmlSections: List<HtmlSection> = emptyList()
)
