package fr.vetbrain.vetnutri_mp.Export

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Data.PreferencesEspece
import fr.vetbrain.vetnutri_mp.Enumer.ContEnum
import fr.vetbrain.vetnutri_mp.Utils.NumberUtils
import fr.vetbrain.vetnutri_mp.Utils.TextUtils

object HtmlDocumentBuilder {

    /**
     * Calcule la quantité en unités (sachet, cuillère, etc.) pour un aliment ration
     * @param alimentRation L'aliment ration
     * @return Une chaîne de caractères représentant la quantité en unités ou null si non applicable
     */
    private fun calculerQuantiteEnUnites(
            alimentRation: fr.vetbrain.vetnutri_mp.Data.AlimentRation
    ): String? {
        val alim = alimentRation.aliment ?: return null
        val cont = alim.cont ?: return null
        val quantInt = alim.quantInt ?: return null

        // Vérifier que le cont n'est pas NO et que quantInt > 0
        if (cont == ContEnum.NO || quantInt <= 0) return null

        // Calculer le nombre d'unités
        val nombreUnites = alimentRation.quantite / quantInt

        // Formater le résultat
        return when (cont) {
            ContEnum.SACHET ->
                    "${NumberUtils.format(nombreUnites.toDouble(), 1)} sachet${if (nombreUnites > 1) "s" else ""} (${quantInt}g/sachet)"
            ContEnum.CAN ->
                    "${NumberUtils.format(nombreUnites.toDouble(), 1)} boîte${if (nombreUnites > 1) "s" else ""} (${quantInt}g/boîte)"
            ContEnum.ML -> "${NumberUtils.format(nombreUnites.toDouble(), 1)} ml (${quantInt}g/ml)"
            ContEnum.COMP ->
                    "${NumberUtils.format(nombreUnites.toDouble(), 1)} comprimé${if (nombreUnites > 1) "s" else ""} (${quantInt}g/comprimé)"
            ContEnum.BOUCH ->
                    "${NumberUtils.format(nombreUnites.toDouble(), 1)} cuillère${if (nombreUnites > 1) "s" else ""} (${quantInt}g/cuillère)"
            ContEnum.DOSETTE ->
                    "${NumberUtils.format(nombreUnites.toDouble(), 1)} dosette${if (nombreUnites > 1) "s" else ""} (${quantInt}g/dosette)"
            ContEnum.GEL -> "${NumberUtils.format(nombreUnites.toDouble(), 1)} gel (${quantInt}g/gel)"
            ContEnum.PRESSION ->
                    "${NumberUtils.format(nombreUnites.toDouble(), 1)} pression${if (nombreUnites > 1) "s" else ""} (${quantInt}g/pression)"
            else -> null
        }
    }

    fun buildHtml(documentType: DocumentType, data: ExportData): String {
        return when (documentType) {
            DocumentType.RATION_ANALYSIS ->
                    buildRationAnalysisHtml(
                            data.animal,
                            data.ration,
                            data.reference,
                            data.title,
                            data.additionalText,
                            data.htmlSections,
                            data.bulletGraphImages
                    )
            DocumentType.PRESCRIPTION ->
                    buildPrescriptionHtml(
                            data.animal,
                            data.conseils,
                            data.title,
                            data.additionalText,
                            data.htmlSections,
                            data.rations,
                            data.practitioner,
                            data.reference,
                            data.preferences,
                            data.poidsAnimal,
                            data.poidsMetabolique,
                            data.besoinEnergetiqueEntretien,
                            data.bulletGraphImages
                    )
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
                .header-card { border: 2px solid #222; padding: 10px; margin-bottom: 12px; }
                .two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
                .right { text-align: right; }
                
                /* Styles pour les bullet graphs */
                .bullet-graphs-container { margin-top: 12px; }
                .bullet-graph-item { margin-bottom: 20px; padding: 12px; border: 1px solid #ddd; border-radius: 6px; background: #fafafa; }
                .bullet-graph-item h3 { margin: 0 0 8px 0; font-size: 12pt; color: #333; }
                .bullet-graph-image { max-width: 100%; height: auto; border: 1px solid #ccc; border-radius: 4px; }
            </style>
            <title>${title}</title>
        </head>
        <body>
            <h1>${title}</h1>
    """.trimIndent()
    private fun buildPractitionerHeader(info: PractitionerInfo?): String {
        if (info == null) return ""
        val adresse = listOf(info.adressePostale, "${info.codePostal} ${info.ville}".trim())
                .filter { it.isNotBlank() }
                .joinToString("<br/>")
        return """
            <div class='header-card'>
                <div class='two-col'>
                    <div>
                        <div><b>${info.nom}</b></div>
                        <div>N° ordre: ${info.numeroOrdre}</div>
                        <div>${adresse}</div>
                    </div>
                    <div class='right'>
                        <div>Téléphone: ${info.telephone}</div>
                        <div>Email: ${info.email}</div>
                    </div>
                </div>
            </div>
        """.trimIndent()
    }


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
                    val qte = TextUtils.formatDecimal(a.quantite.toDouble(), 1)
                    val quantiteUnites = calculerQuantiteEnUnites(a)

                    val quantiteCell =
                            if (quantiteUnites != null) {
                                "${qte} g<br/><small style='color: #666;'>${quantiteUnites}</small>"
                            } else {
                                "${qte} g"
                            }

                    "<tr><td>${nom}</td><td style='text-align:right'>${quantiteCell}</td></tr>"
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

    private fun buildRationsBlocks(
            rations: List<Ration>,
            reference: ReferenceEv? = null,
            animal: AnimalEv? = null,
            preferences: PreferencesEspece? = null,
            poidsAnimal: Double? = null,
            poidsMetabolique: Double? = null,
            besoinEnergetiqueEntretien: Double? = null,
            bulletGraphImages: Map<String, Map<String, String>> = emptyMap()
    ): String {
        if (rations.isEmpty()) return ""
        return rations.joinToString("\n") { ration ->
            val header = if (ration.name.isNotBlank()) "<h2>Ration: ${ration.name}</h2>" else ""
            val block = buildRationBlock(ration)
            val rationImages = bulletGraphImages[ration.uuid] ?: emptyMap()
            val bulletGraphs = buildNutrientAnalysisBulletGraphs(
                    ration, reference, animal, preferences, 
                    poidsAnimal, poidsMetabolique, besoinEnergetiqueEntretien, rationImages
            )
            "<div class='section'>${header}${block}${bulletGraphs}</div>"
        }
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
            title: String,
            additionalText: String,
            htmlSections: List<HtmlSection> = emptyList(),
            bulletGraphImages: Map<String, Map<String, String>> = emptyMap()
    ): String {
        return buildHeader(if (title.isNotBlank()) title else "Analyse de ration") +
                buildAnimalBlock(animal) +
                buildRationBlock(ration) +
                buildReferencesBlock(reference) +
                buildAdditionalTextBlock(additionalText) +
                buildHtmlSectionsBlock(htmlSections) +
                buildRationsBlocks(
                    listOfNotNull(ration),
                    reference,
                    animal,
                    null, // preferences
                    null, // poidsAnimal
                    null, // poidsMetabolique
                    null, // besoinEnergetiqueEntretien
                    bulletGraphImages
                ) +
                buildFooter()
    }

    private fun buildPrescriptionHtml(
            animal: AnimalEv?,
            conseils: List<String>,
            title: String,
            additionalText: String,
            htmlSections: List<HtmlSection> = emptyList(),
            rations: List<Ration> = emptyList(),
            practitioner: PractitionerInfo? = null,
            reference: ReferenceEv? = null,
            preferences: PreferencesEspece? = null,
            poidsAnimal: Double? = null,
            poidsMetabolique: Double? = null,
            besoinEnergetiqueEntretien: Double? = null,
            bulletGraphImages: Map<String, Map<String, String>> = emptyMap()
    ): String {
        return buildHeader(if (title.isNotBlank()) title else "Ordonnance nutritionnelle") +
                buildPractitionerHeader(practitioner) +
                buildAnimalBlock(animal) +
                buildRationsBlocks(rations, reference, animal, preferences, poidsAnimal, poidsMetabolique, besoinEnergetiqueEntretien, bulletGraphImages) +
                buildConseilsBlock(conseils) +
                buildAdditionalTextBlock(additionalText) +
                buildHtmlSectionsBlock(htmlSections) +
                buildFooter()
    }

    private fun buildAdditionalTextBlock(text: String): String {
        if (text.isBlank()) return ""
        val escaped =
                text.replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
                        .replace("\n", "<br/>")
        return """
            <div class='section'>
                <h2>Notes</h2>
                <div class='small'>${escaped}</div>
            </div>
        """.trimIndent()
    }

    private fun buildHtmlSectionsBlock(sections: List<HtmlSection>): String {
        if (sections.isEmpty()) return ""

        val sectionsHtml =
                sections.joinToString("\n") { section ->
                    HtmlSectionParser.parseSectionToHtmlForExport(section)
                }

        return """
            <div class='custom-sections'>
                $sectionsHtml
            </div>
        """.trimIndent()
    }

    /**
     * Génère les bullet graphs pour l'analyse nutritionnelle d'une ration
     */
    private fun buildNutrientAnalysisBulletGraphs(
        ration: Ration,
        reference: ReferenceEv?,
        animal: AnimalEv?,
        preferences: PreferencesEspece?,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        besoinEnergetiqueEntretien: Double?,
        bulletGraphImages: Map<String, String> = emptyMap()
    ): String {
        println("DEBUG: buildNutrientAnalysisBulletGraphs appelé avec ${bulletGraphImages.size} images")
        bulletGraphImages.forEach { (name, path) ->
            println("DEBUG: Image $name -> $path")
        }
        
        if (reference == null || animal == null) {
            println("DEBUG: Reference ou animal manquant")
            return ""
        }

        if (bulletGraphImages.isEmpty()) {
            println("DEBUG: Aucune image de bullet graph fournie")
            return """
                <div class='section'>
                    <h2>Analyse nutritionnelle - Bullet Graphs</h2>
                    <div class='bullet-graphs-container'>
                        <p><em>Les bullet graphs d'analyse nutritionnelle seront affichés ici pour chaque nutriment de la ration.</em></p>
                        <p>Ration: ${ration.name}</p>
                        <p>Référence: ${reference.nom}</p>
                        <p>DEBUG: Aucune image fournie (${bulletGraphImages.size} images)</p>
                    </div>
                </div>
            """.trimIndent()
        }

        val bulletGraphsHtml = bulletGraphImages.entries.joinToString("\n") { (nutrientName, imagePath) ->
            """
                <div class='bullet-graph-item'>
                    <h3>$nutrientName</h3>
                    <img src='$imagePath' alt='Bullet graph pour $nutrientName' class='bullet-graph-image' />
                    <p>DEBUG: Chemin image: $imagePath</p>
                </div>
            """.trimIndent()
        }

        return """
            <div class='section'>
                <h2>Analyse nutritionnelle - Bullet Graphs</h2>
                <div class='bullet-graphs-container'>
                    $bulletGraphsHtml
                </div>
            </div>
        """.trimIndent()
    }

}
