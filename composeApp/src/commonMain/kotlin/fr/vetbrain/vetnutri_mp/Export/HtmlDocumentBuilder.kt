package fr.vetbrain.vetnutri_mp.Export

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv

object HtmlDocumentBuilder {

    fun buildHtml(documentType: DocumentType, data: ExportData): String {
        return when (documentType) {
            DocumentType.RATION_ANALYSIS ->
                    buildRationAnalysisHtml(data.animal, data.ration, data.reference, data.title)
            DocumentType.PRESCRIPTION ->
                    buildPrescriptionHtml(data.animal, data.ration, data.conseils, data.title)
        }
    }

    private fun buildHeader(title: String): String =
            """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset='UTF-8'/>
            <style>
                body { font-family: -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif; font-size: 12pt; color: #222; }
                h1 { font-size: 20pt; margin: 0 0 8px 0; }
                h2 { font-size: 14pt; margin: 16px 0 8px 0; }
                .section { margin-bottom: 16px; }
                table { width: 100%; border-collapse: collapse; }
                th, td { border: 1px solid #ccc; padding: 6px 8px; }
                th { background: #f5f5f5; text-align: left; }
                .muted { color: #666; }
                .small { font-size: 10pt; }
            </style>
            <title>${title}</title>
        </head>
        <body>
            <h1>${title}</h1>
    """.trimIndent()

    private fun buildFooter(): String = """
        </body>
        </html>
    """.trimIndent()

    private fun buildAnimalBlock(animal: AnimalEv?): String {
        if (animal == null) return ""
        val espece = animal.getEspece().label
        return """
            <div class='section'>
                <h2>Animal</h2>
                <div><b>Nom:</b> ${animal.nom}</div>
                <div><b>Espèce:</b> ${espece}</div>
                <div class='small muted'><b>UUID:</b> ${animal.uuid}</div>
            </div>
        """.trimIndent()
    }

    private fun buildRationBlock(ration: Ration?): String {
        if (ration == null) return ""
        val rows =
                ration.alimentMutableList.joinToString("\n") { a ->
                    val nom = a.aliment?.nom ?: "?"
                    val qte = String.format("%.1f", a.quantite)
                    "<tr><td>${nom}</td><td style='text-align:right'>${qte} g</td></tr>"
                }
        return """
            <div class='section'>
                <h2>Composition de la ration</h2>
                <table>
                    <thead><tr><th>Aliment</th><th>Quantité</th></tr></thead>
                    <tbody>
                        ${rows}
                    </tbody>
                </table>
            </div>
        """.trimIndent()
    }

    private fun buildReferencesBlock(reference: ReferenceEv?): String {
        if (reference == null) return ""
        return """
            <div class='section'>
                <h2>Référence utilisée</h2>
                <div><b>Nom:</b> ${reference.nom}</div>
                <div class='small muted'><b>UUID:</b> ${reference.uuid}</div>
            </div>
        """.trimIndent()
    }

    private fun buildConseilsBlock(conseils: List<String>): String {
        if (conseils.isEmpty()) return ""
        val items = conseils.joinToString("\n") { "<li>${it}</li>" }
        return """
            <div class='section'>
                <h2>Conseils</h2>
                <ul>${items}</ul>
            </div>
        """.trimIndent()
    }

    private fun buildRationAnalysisHtml(
            animal: AnimalEv?,
            ration: Ration?,
            reference: ReferenceEv?,
            title: String
    ): String {
        return buildHeader(if (title.isNotBlank()) title else "Analyse de ration") +
                buildAnimalBlock(animal) +
                buildRationBlock(ration) +
                buildReferencesBlock(reference) +
                buildFooter()
    }

    private fun buildPrescriptionHtml(
            animal: AnimalEv?,
            ration: Ration?,
            conseils: List<String>,
            title: String
    ): String {
        return buildHeader(if (title.isNotBlank()) title else "Ordonnance nutritionnelle") +
                buildAnimalBlock(animal) +
                buildRationBlock(ration) +
                buildConseilsBlock(conseils) +
                buildFooter()
    }
}

