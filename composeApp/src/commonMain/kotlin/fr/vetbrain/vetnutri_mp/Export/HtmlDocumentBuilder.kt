package fr.vetbrain.vetnutri_mp.Export

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Data.PreferencesEspece
import fr.vetbrain.vetnutri_mp.Data.analyserValeursNutritionnellesRation
import fr.vetbrain.vetnutri_mp.Enumer.ContEnum
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientOther
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import fr.vetbrain.vetnutri_mp.Enumer.AAEnum
import fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis
import fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Utils.NumberUtils
import fr.vetbrain.vetnutri_mp.Utils.TextUtils

object HtmlDocumentBuilder {
    private fun formatAlimentDisplayName(aliment: fr.vetbrain.vetnutri_mp.Data.AlimentEv?): String {
        if (aliment == null) return "?"
        fun debugChars(input: String): String =
            input.map { c -> "${c.code.toString(16).padStart(4, '0')}(${c})" }.joinToString(" ")
        fun clean(value: String?): String? {
            if (value == null) return null
            var normalized = value.trim()
            while (normalized.length >= 2 &&
                ((normalized.startsWith("\"") && normalized.endsWith("\"")) ||
                    (normalized.startsWith("'") && normalized.endsWith("'")))) {
                normalized = normalized.substring(1, normalized.length - 1).trim()
            }
            normalized =
                normalized
                    .replace('\u00A0', ' ')
                    .replace(Regex("""^[\s"'`]+|[\s"'`]+$"""), "")
            if (normalized.isBlank()) return null
            val semantic =
                normalized.lowercase().replace(Regex("""[^\p{L}\p{N}]+"""), "")
            if (semantic == "null" || semantic == "none" || semantic == "na") return null
            return normalized
        }
        if ((aliment.brand ?: "").contains("null", ignoreCase = true) ||
            (aliment.gamme ?: "").contains("null", ignoreCase = true) ||
            (aliment.nom ?: "").contains("null", ignoreCase = true)) {
            println(
                "[ORDO_ALIMENT_DEBUG] raw brand='${aliment.brand}' gamme='${aliment.gamme}' nom='${aliment.nom}'"
            )
            println(
                "[ORDO_ALIMENT_DEBUG] raw gamme chars: ${
                    aliment.gamme?.let { debugChars(it) } ?: "<null>"
                }"
            )
        }
        val parts = listOf(
            clean(aliment.brand),
            clean(aliment.gamme),
            clean(aliment.nom)
        )
        val result = if (parts.isEmpty()) "?" else parts.joinToString(", ")
        if (result.contains(", null,", ignoreCase = true) || result.contains(" null", ignoreCase = true)) {
            println(
                "[ORDO_ALIMENT_DEBUG] cleaned parts=$parts result='$result' for uuid='${aliment.uuid}'"
            )
        }
        return result
    }

    /**
     * Obtient le nom traduit d'un nutriment selon son type en utilisant les traductions JSON
     */
    private fun obtenirNomTraduitNutriment(nom: String, nutriment: Any): String {
        return when (nutriment) {
            is NutrientLipid -> nutriment.translateEnum()
            is NutrientMacro -> nutriment.translateEnum()
            is NutrientMain -> nutriment.translateEnum()
            is NutrientMin -> nutriment.translateEnum()
            is NutrientOther -> nutriment.translateEnum()
            is NutrientVitam -> nutriment.translateEnum()
            is AAEnum -> nutriment.translateEnum()
            is NutrientAnalysis -> nutriment.translateEnum()
            else -> nom // Fallback sur le nom original si le type n'est pas reconnu
        }
    }

    /**
     * Calcule l'affichage d'un nutriment selon le type d'expression des besoins choisi
     * @param valeurNutritionnelle Valeur nutritionnelle du nutriment
     * @param typeExpressionBesoin Type d'expression des besoins (préférences utilisateur)
     * @param poidsMetabolique Poids métabolique de l'animal
     * @param poidsAnimal Poids vif de l'animal
     * @param besoinEnergetiqueEntretien Besoin énergétique d'entretien (BEE)
     * @param referenceUtilisee Référence utilisée pour extraire la puissance de l'équation BW
     * @return Pair<valeur formatée, unité d'affichage>
     */
    private fun calculerAffichageNutriment(
        valeurNutritionnelle: fr.vetbrain.vetnutri_mp.Data.ValeurNutritionnelle,
        typeExpressionBesoin: TypeExpressionBesoin?,
        poidsMetabolique: Double?,
        poidsAnimal: Double?,
        besoinEnergetiqueEntretien: Double?,
        referenceUtilisee: ReferenceEv? = null
    ): Pair<String, String> {

        val valeurAbsolue = valeurNutritionnelle.valeur
        val uniteOriginale = valeurNutritionnelle.unite.displayName

        // Cas spécial: nutriments d'analyse/ratio sans unité (ex: CAP, KNA, O6O3...)
        // - Ne pas afficher d'unité
        // - Ne pas appliquer de transformation UnitReqEnum
        val isUnitEmpty = uniteOriginale.isBlank()
        val isAnalysis = valeurNutritionnelle.nutriment is NutrientAnalysis
        if (isAnalysis && isUnitEmpty) {
            return Pair(TextUtils.formatDecimal(valeurAbsolue, 2), "")
        }

        // Si pas de type d'expression défini, affichage par défaut
        val typeExpression = typeExpressionBesoin ?: TypeExpressionBesoin.DEFAULT

        return when (typeExpression) {
            TypeExpressionBesoin.PAR_KG -> {
                // Par kg de poids vif
                poidsAnimal?.let { poids ->
                    if (poids > 0) {
                        val valeurParKg = valeurAbsolue / poids
                        Pair(TextUtils.formatDecimal(valeurParKg, 2), "$uniteOriginale/kg")
                    } else {
                        // Si pas de poids disponible, garder l'unité originale mais indiquer le type
                        // d'expression
                        Pair(
                            TextUtils.formatDecimal(valeurAbsolue, 2),
                            "$uniteOriginale (par kg si poids disponible)"
                        )
                    }
                }
                    ?: Pair(
                        TextUtils.formatDecimal(valeurAbsolue, 2),
                        "$uniteOriginale (par kg si poids disponible)"
                    )
            }
            TypeExpressionBesoin.PAR_KG_METABOLIQUE -> {
                // Par kg de poids métabolique (kg^puissance)
                val puissance = TextUtils.extrairePuissanceEquationBW(
                        referenceUtilisee?.equationBW?.equationScript
                )
                poidsMetabolique?.let { poidsMetab ->
                    if (poidsMetab > 0) {
                        val valeurParKgMetab = valeurAbsolue / poidsMetab
                        Pair(
                            TextUtils.formatDecimal(valeurParKgMetab, 2),
                            "$uniteOriginale/kg${TextUtils.toSuperscript(puissance)}"
                        )
                    } else {
                        // Si pas de poids métabolique disponible, garder l'unité originale mais
                        // indiquer le type d'expression
                        Pair(
                            TextUtils.formatDecimal(valeurAbsolue, 2),
                            "$uniteOriginale (par kg^$puissance si poids métabolique disponible)"
                        )
                    }
                }
                    ?: Pair(
                        TextUtils.formatDecimal(valeurAbsolue, 2),
                        "$uniteOriginale (par kg^$puissance si poids métabolique disponible)"
                    )
            }
            TypeExpressionBesoin.PAR_KCAL -> {
                // Par 1000 kcal de BEE (Besoin Énergétique d'Entretien)
                besoinEnergetiqueEntretien?.let { bee ->
                    if (bee > 0) {
                        val valeurPar1000Kcal = (valeurAbsolue / bee) * 1000
                        Pair(TextUtils.formatDecimal(valeurPar1000Kcal, 2), "$uniteOriginale/1000 kcal")
                    } else {
                        // Si pas de BEE disponible, garder l'unité originale mais indiquer le type
                        // d'expression
                        Pair(
                            TextUtils.formatDecimal(valeurAbsolue, 2),
                            "$uniteOriginale (par 1000 kcal si BEE disponible)"
                        )
                    }
                }
                    ?: Pair(
                        TextUtils.formatDecimal(valeurAbsolue, 2),
                        "$uniteOriginale (par 1000 kcal si BEE disponible)"
                    )
            }
            TypeExpressionBesoin.PAR_KJ -> {
                // Par 1000 kJ de BEE (conversion : 1 kcal = 4.184 kJ)
                besoinEnergetiqueEntretien?.let { bee ->
                    if (bee > 0) {
                        val beeEnKj = bee * 4.184 // Conversion kcal vers kJ
                        val valeurPar1000Kj = (valeurAbsolue / beeEnKj) * 1000
                        Pair(TextUtils.formatDecimal(valeurPar1000Kj, 2), "$uniteOriginale/1000 kJ")
                    } else {
                        // Si pas de BEE disponible, garder l'unité originale mais indiquer le type
                        // d'expression
                        Pair(
                            TextUtils.formatDecimal(valeurAbsolue, 2),
                            "$uniteOriginale (par 1000 kJ si BEE disponible)"
                        )
                    }
                }
                    ?: Pair(
                        TextUtils.formatDecimal(valeurAbsolue, 2),
                        "$uniteOriginale (par 1000 kJ si BEE disponible)"
                    )
            }
        }
    }

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

    suspend fun buildHtml(documentType: DocumentType, data: ExportData): String {
        return when (documentType) {
            DocumentType.RATION_ANALYSIS ->
                    buildRationAnalysisHtml(
                            animal = data.animal,
                            ration = data.ration,
                            reference = data.reference,
                            title = data.title,
                            additionalText = data.additionalText,
                            htmlSections = data.htmlSections,
                            bulletGraphImages = data.bulletGraphImages,
                            isLandscape = data.isLandscape
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

    private fun buildHeader(title: String, isLandscape: Boolean): String =
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
                ${
                        if (isLandscape)
                                "@page { size: A4 landscape; margin: 1cm; }"
                        else ""
                }
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
                <div><b>ID:</b> ${animal.id}</div>
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
                    val nom = formatAlimentDisplayName(a.aliment)
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

    private suspend fun buildRationsBlocks(
            rations: List<Ration>,
            reference: ReferenceEv? = null,
            animal: AnimalEv? = null,
            preferences: PreferencesEspece? = null,
            poidsAnimal: Double? = null,
            poidsMetabolique: Double? = null,
            besoinEnergetiqueEntretien: Double? = null,
            bulletGraphImages: Map<String, Map<String, String>> = emptyMap(),
            includeBulletGraphs: Boolean = true
    ): String {
        if (rations.isEmpty()) return ""
        return buildString {
            rations.forEach { ration ->
                val header = if (ration.name.isNotBlank()) "<h2>Ration: ${ration.name}</h2>" else ""
                val block = buildRationBlock(ration)
                val rationImages = bulletGraphImages[ration.uuid] ?: emptyMap()
                val bulletGraphs =
                    if (includeBulletGraphs) {
                        buildNutrientAnalysisBulletGraphs(
                                ration, reference, animal, preferences,
                                poidsAnimal, poidsMetabolique, besoinEnergetiqueEntretien, rationImages
                        )
                    } else {
                        ""
                    }
                append("<div class='section'>${header}${block}${bulletGraphs}</div>")
            }
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

    private suspend fun buildRationAnalysisHtml(
            animal: AnimalEv?,
            ration: Ration?,
            reference: ReferenceEv?,
            title: String,
            additionalText: String,
            htmlSections: List<HtmlSection> = emptyList(),
            bulletGraphImages: Map<String, Map<String, String>> = emptyMap(),
            isLandscape: Boolean = false
    ): String {
        return buildHeader(
                        if (title.isNotBlank()) title else "Analyse de ration",
                        isLandscape
                ) +
                buildAnimalBlock(animal) +
                
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

    private suspend fun buildPrescriptionHtml(
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
        return buildHeader(
                        if (title.isNotBlank()) title else "Ordonnance nutritionnelle",
                        false
                ) +
                buildPractitionerHeader(practitioner) +
                buildAnimalBlock(animal) +
                buildRationsBlocks(rations, reference, animal, preferences, poidsAnimal, poidsMetabolique, besoinEnergetiqueEntretien, bulletGraphImages, includeBulletGraphs = false) +
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
    private suspend fun buildNutrientAnalysisBulletGraphs(
        ration: Ration,
        reference: ReferenceEv?,
        animal: AnimalEv?,
        preferences: PreferencesEspece?,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        besoinEnergetiqueEntretien: Double?,
        bulletGraphImages: Map<String, String> = emptyMap()
    ): String {
       

        if (bulletGraphImages.isEmpty()) {
           
            return """
                <div class='section'>
                    <h2>Analyse nutritionnelle - Bullet Graphs</h2>
                    <div class='bullet-graphs-container'>
                        <p><em>Les bullet graphs d'analyse nutritionnelle seront affichés ici pour chaque nutriment de la ration.</em></p>
                        <p>Ration: ${ration.name}</p>
                        <p>Référence: ${reference?.nom ?: "Non spécifiée"}</p>
                      
                    </div>
                </div>
            """.trimIndent()
        }

        // Obtenir les valeurs nutritionnelles pour calculer les affichages
        val valeursNutritionnelles =
            try {
                analyserValeursNutritionnellesRation(ration)
            } catch (e: Exception) {
                emptyMap()
            }

        // Obtenir le type d'expression des besoins depuis les préférences
        val typeExpressionBesoin = preferences?.getTypeExpressionBesoinEnum() ?: TypeExpressionBesoin.DEFAULT

        val bulletGraphsHtml = bulletGraphImages.entries.joinToString("\n") { (nutrientName, imagePath) ->
            // Trouver la valeur nutritionnelle correspondante
            val valeurNutritionnelle = valeursNutritionnelles[nutrientName]
            val nomTraduit = if (valeurNutritionnelle != null) {
                obtenirNomTraduitNutriment(nutrientName, valeurNutritionnelle.nutriment)
            } else {
                nutrientName
            }
            
            val valeurAffichee = if (valeurNutritionnelle != null) {
                val (valeur, unite) = calculerAffichageNutriment(
                    valeurNutritionnelle = valeurNutritionnelle,
                    typeExpressionBesoin = typeExpressionBesoin,
                    poidsMetabolique = poidsMetabolique,
                    poidsAnimal = poidsAnimal,
                    besoinEnergetiqueEntretien = besoinEnergetiqueEntretien,
                    referenceUtilisee = reference
                )
                if (unite.isNotBlank()) "$valeur $unite" else valeur
            } else {
                "Valeur non disponible"
            }

            """
                <div class='bullet-graph-item'>
              
                    <img src='$imagePath' alt='Bullet graph pour $nomTraduit' class='bullet-graph-image' />
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
