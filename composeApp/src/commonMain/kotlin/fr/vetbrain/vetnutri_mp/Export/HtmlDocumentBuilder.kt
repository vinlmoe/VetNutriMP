package fr.vetbrain.vetnutri_mp.Export

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Data.PreferencesEspece
import fr.vetbrain.vetnutri_mp.Data.ValeurNutritionnelle
import fr.vetbrain.vetnutri_mp.Data.analyserValeursNutritionnellesRation
import fr.vetbrain.vetnutri_mp.Data.analyserValeursNutritionnellesRationAvecEquations
import fr.vetbrain.vetnutri_mp.Data.calculerAffichageNutriment
import fr.vetbrain.vetnutri_mp.Data.calculerCompositionPourcentages
import fr.vetbrain.vetnutri_mp.Data.calculerOrigineEnergetiquePourcentages
import fr.vetbrain.vetnutri_mp.Data.calculerBulletGraphData
import fr.vetbrain.vetnutri_mp.Data.estNutrimentAnalysisRatio
import fr.vetbrain.vetnutri_mp.Data.BulletGraphData
import fr.vetbrain.vetnutri_mp.Data.calculerContributionsIngredients
import fr.vetbrain.vetnutri_mp.Data.ContributionIngredient
import fr.vetbrain.vetnutri_mp.Data.grouperNutrimentsParCategorie
import fr.vetbrain.vetnutri_mp.Data.obtenirTitreCategorie
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
    private fun escapeXml(text: String?): String {
        if (text == null) return ""
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
    }

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
            val semantic = normalized.lowercase().filter { it.isLetterOrDigit() }
            if (semantic == "null" || semantic == "none" || semantic == "na") return null
            return normalized
        }
        val parts = listOf(
            clean(aliment.brand),
            clean(aliment.gamme),
            clean(aliment.nom)
        )
        return if (parts.isEmpty()) "?" else parts.joinToString(", ")
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

    // calculerAffichageNutriment vit maintenant dans Data/NutrientDisplayCalculations.kt
    // (partagé avec l'écran RationsView).

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
                            isLandscape = data.isLandscape,
                            preferences = data.preferences,
                            poidsAnimal = data.poidsAnimal,
                            poidsMetabolique = data.poidsMetabolique,
                            besoinEnergetiqueStandard = data.besoinEnergetiqueStandard,
                            besoinEnergetiqueTotal = data.besoinEnergetiqueTotal,
                            energieApportee = data.energieApportee,
                            energieAdditionnelle = data.energieAdditionnelle,
                            kCalcule = data.kCalcule,
                            kObserve = data.kObserve,
                            pourcentageCouverture = data.pourcentageCouverture,
                            equationRepository = data.equationRepository,
                            referencesMaladies = data.referencesMaladies
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

                /* Table nutriments : layout fixe requis pour que le bullet graph SVG (width:100%)
                   ait une largeur de colonne déjà connue, sinon le moteur PDF plante en essayant
                   de résoudre le pourcentage avant que la largeur de colonne soit calculée. */
                .nutrient-table { table-layout: fixed; }
                .nutrient-table col.col-nom { width: 28%; }
                .nutrient-table col.col-valeur { width: 27%; }
                .nutrient-table col.col-repere { width: 45%; }
                .nutrient-table tr { break-inside: avoid; page-break-inside: avoid; }
                .bullet-graph { width: 100%; border-collapse: collapse; table-layout: fixed; page-break-inside: avoid; break-inside: avoid; }
                .bullet-graph td { border: 0; padding: 0; line-height: 0; font-size: 0; }
                .bullet-graph-zone td { height: 8px; }
                .bullet-graph-apport td { height: 4px; }
                
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
            <title>${escapeXml(title)}</title>
        </head>
        <body>
            <h1>${escapeXml(title)}</h1>
    """.trimIndent()
    private fun buildPractitionerHeader(info: PractitionerInfo?): String {
        if (info == null) return ""
        val adresse = listOf(info.adressePostale, "${info.codePostal} ${info.ville}".trim())
                .filter { it.isNotBlank() }
                .map { escapeXml(it) }
                .joinToString("<br/>")
        return """
            <div class='header-card'>
                <div class='two-col'>
                    <div>
                        <div><b>${escapeXml(info.nom)}</b></div>
                        <div>N° ordre: ${escapeXml(info.numeroOrdre)}</div>
                        <div>${adresse}</div>
                    </div>
                    <div class='right'>
                        <div>Téléphone: ${escapeXml(info.telephone)}</div>
                        <div>Email: ${escapeXml(info.email)}</div>
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
                <div><b>ID:</b> ${escapeXml(animal.id)}</div>
                <div><b>Nom:</b> ${escapeXml(animal.nom)}</div>
                <div><b>Espèce:</b> ${escapeXml(espece)}</div>
                <div class='small muted'><b>UUID:</b> ${escapeXml(animal.uuid)}</div>
            </div>
        """.trimIndent()
    }

    // Même palette que VetNutriColors.FeedColors (Theme/Colors.kt), en hexadécimal brut pour
    // l'export HTML : sert à la fois de repère couleur dans la liste des aliments et de couleur
    // des segments de contribution dans le bullet graph, pour que les deux se correspondent.
    private val FEED_COLORS_HEX = listOf(
        "#2E7D32", "#1565C0", "#EF6C00", "#AD1457", "#6A1B9A",
        "#00838F", "#D84315", "#4527A0", "#37474F", "#C2185B"
    )

    private fun feedColorHex(index: Int): String = FEED_COLORS_HEX[index % FEED_COLORS_HEX.size]

    private fun buildRationBlock(ration: Ration?): String {
        if (ration == null) return ""
        val rows =
                ration.alimentMutableList.mapIndexed { index, a ->
                    val nom = formatAlimentDisplayName(a.aliment)
                    val qte = TextUtils.formatDecimal(a.quantite.toDouble(), 1)
                    val quantiteUnites = calculerQuantiteEnUnites(a)

                    val quantiteCell =
                            if (quantiteUnites != null) {
                                "${qte} g<br/><small style='color: #666;'>${escapeXml(quantiteUnites)}</small>"
                            } else {
                                "${qte} g"
                            }
                    val couleurCell =
                            "<div style='width:14px;height:14px;border-radius:2px;background:${feedColorHex(index)};'></div>"

                    "<tr><td>${couleurCell}</td><td>${escapeXml(nom)}</td><td style='text-align:right'>${quantiteCell}</td></tr>"
                }.joinToString("\n")
        return """
            <div class='section'>
                <h2>Composition de la ration</h2>
                <table>
                    <thead><tr><th>Couleur</th><th>Aliment</th><th>Quantité</th></tr></thead>
                    <tbody>
                        ${rows}
                    </tbody>
                </table>
            </div>
        """.trimIndent()
    }

    private fun buildRationsBlocks(rations: List<Ration>): String {
        if (rations.isEmpty()) return ""
        return buildString {
            rations.forEach { ration ->
                val header = if (ration.name.isNotBlank()) "<h2>Ration: ${escapeXml(ration.name)}</h2>" else ""
                val block = buildRationBlock(ration)
                append("<div class='section'>${header}${block}</div>")
            }
        }
    }

    private fun buildReferencesBlock(reference: ReferenceEv?): String {
        if (reference == null) return ""
        return """
            <div class='section'>
                <h2>Référence utilisée</h2>
                <div><b>Nom:</b> ${escapeXml(reference.nom)}</div>
                <div class='small muted'><b>UUID:</b> ${escapeXml(reference.uuid)}</div>
            </div>
        """.trimIndent()
    }

    private fun buildConseilsBlock(conseils: List<String>): String {
        if (conseils.isEmpty()) return ""
        val items = conseils.joinToString("\n") { "<li>${escapeXml(it)}</li>" }
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
            isLandscape: Boolean = false,
            preferences: PreferencesEspece? = null,
            poidsAnimal: Double? = null,
            poidsMetabolique: Double? = null,
            besoinEnergetiqueStandard: Double? = null,
            besoinEnergetiqueTotal: Double? = null,
            energieApportee: Double? = null,
            energieAdditionnelle: Double? = null,
            kCalcule: Double? = null,
            kObserve: Double? = null,
            pourcentageCouverture: Double? = null,
            equationRepository: EquationRepository? = null,
            referencesMaladies: List<ReferenceEv> = emptyList()
    ): String {
        return buildHeader(
                        if (title.isNotBlank()) title else "Analyse de ration",
                        isLandscape
                ) +
                buildAnimalBlock(animal) +
                buildReferencesBlock(reference) +
                buildBilanEnergetiqueBlock(
                        poidsMetabolique,
                        besoinEnergetiqueStandard,
                        besoinEnergetiqueTotal,
                        energieAdditionnelle,
                        kCalcule,
                        kObserve,
                        energieApportee,
                        pourcentageCouverture
                ) +
                (if (ration != null) buildRationBlock(ration) else "") +
                (
                        if (ration != null) {
                            buildNutrientTableBlock(
                                    ration,
                                    reference,
                                    preferences,
                                    equationRepository,
                                    poidsAnimal,
                                    poidsMetabolique,
                                    // Même piège de nommage que RationsView.kt : le "besoin énergétique
                                    // d'entretien" utilisé pour l'affichage PAR_KCAL/PAR_KJ est en réalité
                                    // le BE total, pas le BEE brut.
                                    besoinEnergetiqueTotal,
                                    referencesMaladies
                            )
                        } else ""
                ) +
                buildAdditionalTextBlock(additionalText) +
                buildHtmlSectionsBlock(htmlSections) +
                buildFooter()
    }

    /**
     * Bloc "Bilan énergétique" : mêmes chiffres que MetabolicSummarySection.kt (poids métabolique,
     * BEE, énergie additionnelle, BE total, K calculé/observé, énergie apportée, % de couverture),
     * avec les mêmes seuils de couleur.
     */
    private fun buildBilanEnergetiqueBlock(
            poidsMetabolique: Double?,
            besoinEnergetiqueStandard: Double?,
            besoinEnergetiqueTotal: Double?,
            energieAdditionnelle: Double?,
            kCalcule: Double?,
            kObserve: Double?,
            energieApportee: Double?,
            pourcentageCouverture: Double?
    ): String {
        if (besoinEnergetiqueStandard == null && besoinEnergetiqueTotal == null && energieApportee == null) {
            return ""
        }

        fun kcal(v: Double?): String = v?.let { "${TextUtils.formatDecimal(it, 0)} kcal/j" } ?: "—"

        val couvertureColor = pourcentageCouverture?.let {
            when {
                it in 90.0..110.0 -> "#4CAF50"
                it in 80.0..120.0 -> "#FF9800"
                else -> "#F44336"
            }
        } ?: "#222"

        val kObserveColor = if (kObserve != null && kCalcule != null && kCalcule > 0.0) {
            val ratio = kObserve / kCalcule
            when {
                ratio in 0.9..1.1 -> "#4CAF50"
                ratio in 0.8..1.2 -> "#FF9800"
                else -> "#F44336"
            }
        } else "#222"

        return """
            <div class='section'>
                <h2>Bilan énergétique</h2>
                <table>
                    <tbody>
                        <tr><td>Poids métabolique</td><td class='right'>${poidsMetabolique?.let { TextUtils.formatDecimal(it, 3) } ?: "—"} kg<sup>p</sup></td></tr>
                        <tr><td>Besoin énergétique standard (BEE)</td><td class='right'>${kcal(besoinEnergetiqueStandard)}</td></tr>
                        <tr><td>Énergie additionnelle (réf. maladies)</td><td class='right'>${kcal(energieAdditionnelle)}</td></tr>
                        <tr><td><b>Besoin énergétique total (BE)</b></td><td class='right'><b>${kcal(besoinEnergetiqueTotal)}</b></td></tr>
                        <tr><td>Énergie apportée par la ration</td><td class='right'>${kcal(energieApportee)}</td></tr>
                        <tr><td>% de couverture</td><td class='right'><span style='color:${couvertureColor}'>${pourcentageCouverture?.let { TextUtils.formatDecimal(it, 0) } ?: "—"}%</span></td></tr>
                        <tr><td>K calculé</td><td class='right'>${kCalcule?.let { TextUtils.formatDecimal(it, 2) } ?: "—"}</td></tr>
                        <tr><td>K observé</td><td class='right'><span style='color:${kObserveColor}'>${kObserve?.let { TextUtils.formatDecimal(it, 2) } ?: "—"}</span></td></tr>
                    </tbody>
                </table>
            </div>
        """.trimIndent()
    }

    /**
     * Tableau nutriments complet, groupé/ordonné comme l'écran (grouperNutrimentsParCategorie),
     * toujours en mode "tous les nutriments" (un document imprimé doit être complet, quel que soit
     * l'état du filtre à l'écran au moment de l'export).
     */
    private suspend fun buildNutrientTableBlock(
            ration: Ration,
            reference: ReferenceEv?,
            preferences: PreferencesEspece?,
            equationRepository: EquationRepository?,
            poidsAnimal: Double?,
            poidsMetabolique: Double?,
            besoinEnergetiqueEntretien: Double?,
            referencesMaladies: List<ReferenceEv>
    ): String {
        val valeurs: Map<String, ValeurNutritionnelle> =
                if (reference != null && preferences != null && equationRepository != null) {
                    try {
                        analyserValeursNutritionnellesRationAvecEquations(
                                ration = ration,
                                preferencesEspece = preferences,
                                equationRepository = equationRepository,
                                referenceEv = reference
                        )
                    } catch (e: Exception) {
                        analyserValeursNutritionnellesRation(ration)
                    }
                } else {
                    analyserValeursNutritionnellesRation(ration)
                }

        if (valeurs.isEmpty()) return ""

        val typeExpressionBesoin = preferences?.getTypeExpressionBesoinEnum() ?: TypeExpressionBesoin.DEFAULT
        val groupes = grouperNutrimentsParCategorie(valeurs)
        val ordreCategories = listOf("BASE", "MACRO", "MIN", "VITAM", "LIPID", "AMA", "ANA", "OTHER", "ENERGY")

        val sectionsHtml = ordreCategories.mapNotNull { categorie ->
            val nutrimentsAvecDonnees = groupes[categorie]?.filter { (_, valeur) ->
                estNutrimentAnalysisRatio(valeur.nutriment) || valeur.valeur > 0.0
            }
            if (nutrimentsAvecDonnees.isNullOrEmpty()) return@mapNotNull null

            val rows = nutrimentsAvecDonnees.map { (nom, valeur) ->
                val nomTraduit = obtenirNomTraduitNutriment(nom, valeur.nutriment)
                val (valeurAffichee, uniteAffichee) = calculerAffichageNutriment(
                        valeurNutritionnelle = valeur,
                        typeExpressionBesoin = typeExpressionBesoin,
                        poidsMetabolique = poidsMetabolique,
                        poidsAnimal = poidsAnimal,
                        besoinEnergetiqueEntretien = besoinEnergetiqueEntretien,
                        referenceUtilisee = reference
                )
                val bulletGraphData = calculerBulletGraphData(
                        valeur, reference, typeExpressionBesoin, poidsAnimal, poidsMetabolique, besoinEnergetiqueEntretien
                )
                val repereHtml = bulletGraphData?.let { bgData ->
                    val contributions = calculerContributionsIngredients(
                            ration, valeur.nutriment, reference, equationRepository, preferences
                    )
                    buildBulletGraphHtml(bgData, contributions)
                } ?: "—"
                val valeurCell = if (uniteAffichee.isNotBlank()) "$valeurAffichee $uniteAffichee" else valeurAffichee
                "<tr><td>${escapeXml(nomTraduit)}</td><td class='right'>${escapeXml(valeurCell)}</td><td>${repereHtml}</td></tr>"
            }.joinToString("\n")

            """
                <h3>${obtenirTitreCategorie(categorie)}</h3>
                <table class='nutrient-table'>
                    <colgroup>
                        <col class='col-nom'/><col class='col-valeur'/><col class='col-repere'/>
                    </colgroup>
                    <thead><tr><th>Nutriment</th><th>Valeur</th><th>Repère</th></tr></thead>
                    <tbody>
                        $rows
                    </tbody>
                </table>
            """.trimIndent()
        }.joinToString("\n")

        return """
            <div class='section'>
                <h2>Analyse nutritionnelle</h2>
                $sectionsHtml
                ${buildCompositionEnergyBlock(valeurs)}
                ${buildQuantitativeSectionBlock(ration, valeurs)}
            </div>
        """.trimIndent()
    }

    /**
     * Rendu HTML/CSS pur (pas de SVG, pas d'image bitmap) d'un bullet graph : mêmes zones
     * colorées et mêmes bornes que `DetailNutrimentAnalysis.kt::ReferenceBulletGraph` (rouge hors
     * MIN/MAX, bleu entre MIN/OPTIMIN et OPTIMAX/MAX, vert dans la zone optimale). La barre
     * d'apport est segmentée par ingrédient (même couleur que la ligne correspondante dans la
     * liste des aliments, via `feedColorHex`), ou une barre sombre unie si la contribution par
     * ingrédient n'est pas disponible. Volontairement sans SVG : `UIMarkupTextPrintFormatter`
     * (export PDF iOS) ne le rend pas (page blanche), alors que des <div> avec largeurs en %
     * fonctionnent sur toutes les plateformes (Desktop/openhtmltopdf, Android/WebView, iOS).
     */
    private fun buildBulletGraphHtml(data: BulletGraphData, contributions: List<ContributionIngredient>): String {
        val axisMax = data.maxAxis
        fun fmt(v: Double): String = TextUtils.formatDecimal(v, 2)
        fun pct(v: Double): Double = (v / axisMax).coerceIn(0.0, 1.0) * 100.0
        fun graphCell(widthPct: Double, color: String): String {
            if (widthPct <= 0.0) return ""
            return "<td style='width:${fmt(widthPct)}%;background:$color;'></td>"
        }

        val bornes = buildList {
            add(0.0)
            data.minRef?.let { add(it) }
            data.optiminRef?.let { add(it) }
            data.optimaxRef?.let { add(it) }
            data.maxRef?.let { add(it) }
            add(axisMax)
        }.distinct().sorted()

        val zoneSegments = StringBuilder()
        var cumulPct = 0.0
        for (i in 0 until bornes.size - 1) {
            val start = bornes[i]
            val end = bornes[i + 1]
            if (end <= start) continue
            val color = when {
                data.minRef != null && start == 0.0 && end == data.minRef -> "#B00020"
                data.maxRef != null && start == data.maxRef && end == axisMax -> "#B00020"
                data.minRef != null && data.optiminRef != null &&
                        start == data.minRef && end == data.optiminRef -> "#2196F3"
                data.optimaxRef != null && data.maxRef != null &&
                        start == data.optimaxRef && end == data.maxRef -> "#2196F3"
                data.optimaxRef != null && data.maxRef == null &&
                        start == data.optimaxRef && end == axisMax -> "#2196F3"
                data.minRef == null && data.optiminRef != null &&
                        start == 0.0 && end == data.optiminRef -> "#2196F3"
                else -> "#4CAF50"
            }
            // Le dernier segment prend la largeur restante pour éviter qu'un arrondi décalé
            // ne fasse dépasser 100% et retombe à la ligne suivante.
            val isLast = i == bornes.size - 2
            val widthPct = if (isLast) (100.0 - cumulPct).coerceAtLeast(0.0) else (pct(end) - pct(start))
            cumulPct += widthPct
            if (widthPct <= 0.0) continue
            zoneSegments.append(graphCell(widthPct, color))
        }

        val apportPct = pct(data.apport)
        val totalContribution = contributions.sumOf { it.contribution }

        val apportRow = StringBuilder()
        if (contributions.isEmpty() || totalContribution <= 0.0) {
            // Pas de détail par ingrédient disponible (ex: nutriment-ratio) : barre unie, comme avant.
            apportRow.append(graphCell(apportPct, "#222222"))
        } else {
            var cumulApportPct = 0.0
            contributions.forEachIndexed { i, contrib ->
                val isLast = i == contributions.size - 1
                val segPct = if (isLast) {
                    (apportPct - cumulApportPct).coerceAtLeast(0.0)
                } else {
                    (contrib.contribution / totalContribution) * apportPct
                }
                cumulApportPct += segPct
                if (segPct <= 0.0) return@forEachIndexed
                apportRow.append(graphCell(segPct, feedColorHex(contrib.index)))
            }
        }
        apportRow.append(graphCell(100.0 - apportPct, "transparent"))

        // Les segments sont rendus en tableau plutôt qu'en inline-block : certains moteurs PDF
        // font passer les inline-block à la ligne quand les arrondis de pourcentage dépassent de
        // quelques fractions de pixel. Une ligne de tableau reste indivisible.
        return """
            <table class='bullet-graph'>
                <tbody>
                    <tr class='bullet-graph-zone'>$zoneSegments</tr>
                </tbody>
            </table>
            <table class='bullet-graph' style='margin-top:1px;'>
                <tbody>
                    <tr class='bullet-graph-apport'>$apportRow</tr>
                </tbody>
            </table>
        """.trimIndent()
    }

    /** Composition (matière sèche) et origine énergétique, mêmes calculs que cardNutrient.kt. */
    private fun buildCompositionEnergyBlock(valeurs: Map<String, ValeurNutritionnelle>): String {
        val composition = calculerCompositionPourcentages(valeurs)
        val energie = calculerOrigineEnergetiquePourcentages(valeurs)
        if (composition.isEmpty() && energie.isEmpty()) return ""

        fun table(titre: String, data: List<Pair<String, Double>>): String {
            if (data.isEmpty()) return ""
            val rows = data.joinToString("\n") { (nom, pct) ->
                "<tr><td>${escapeXml(nom)}</td><td class='right'>${TextUtils.formatDecimal(pct, 1)}%</td></tr>"
            }
            return """
                <h3>${escapeXml(titre)}</h3>
                <table><tbody>$rows</tbody></table>
            """.trimIndent()
        }

        return """
            ${table("Composition", composition)}
            ${table("Origine de l'énergie", energie)}
        """.trimIndent()
    }

    /**
     * Section quantitative (par 100g de ration / par 100g de matière sèche / par 1000 kcal),
     * mêmes formules que RationQuantitativeSection.kt::facteurConversionQuantite.
     */
    private fun buildQuantitativeSectionBlock(ration: Ration, valeurs: Map<String, ValeurNutritionnelle>): String {
        val quantiteTotaleRation = ration.getQuantiteTotale()
        val humiditeTotale = valeurs["HUMIDITE"]?.valeur ?: 0.0
        val matiereSecheTotale = (quantiteTotaleRation - humiditeTotale).coerceAtLeast(0.0)
        val energieTotaleKcal = valeurs["ENERGIE"]?.valeur ?: 0.0

        data class Mode(val titre: String, val factor: Double?)
        val modes = listOf(
                Mode("Par 100g de ration", if (quantiteTotaleRation > 0.0) 100.0 / quantiteTotaleRation else null),
                Mode("Par 100g de matière sèche", if (matiereSecheTotale > 0.0) 100.0 / matiereSecheTotale else null),
                Mode("Par 1000 kcal", if (energieTotaleKcal > 0.0) 1000.0 / energieTotaleKcal else null)
        )

        val tables = modes.mapNotNull { mode ->
            val factor = mode.factor ?: return@mapNotNull null
            val rows = valeurs.entries
                    .filter { (_, valeur) -> estNutrimentAnalysisRatio(valeur.nutriment) || valeur.valeur > 0.0 }
                    .joinToString("\n") { (nom, valeur) ->
                        val isRatio = estNutrimentAnalysisRatio(valeur.nutriment)
                        val valeurAffichee = if (isRatio) valeur.valeur else valeur.valeur * factor
                        val nomTraduit = obtenirNomTraduitNutriment(nom, valeur.nutriment)
                        "<tr><td>${escapeXml(nomTraduit)}</td><td class='right'>${TextUtils.formatDecimal(valeurAffichee, 2)} ${escapeXml(valeur.unite.displayName)}</td></tr>"
                    }
            """
                <h3>${escapeXml(mode.titre)}</h3>
                <table><tbody>$rows</tbody></table>
            """.trimIndent()
        }.joinToString("\n")

        if (tables.isBlank()) return ""

        return """
            <h2>Section quantitative</h2>
            $tables
        """.trimIndent()
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
                buildRationsBlocks(rations) +
                buildConseilsBlock(conseils) +
                buildAdditionalTextBlock(additionalText) +
                buildHtmlSectionsBlock(htmlSections) +
                buildFooter()
    }

    private fun buildAdditionalTextBlock(text: String): String {
        if (text.isBlank()) return ""
        val escaped = escapeXml(text).replace("\n", "<br/>")
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

    // buildNutrientAnalysisBulletGraphs (capture PNG de bullet graphs) a été retiré : remplacé par
    // buildNutrientTableBlock (tableau HTML/CSS, fiable sur toutes les plateformes y compris iOS).
    //
    // 3.3.2 : le module Export/BulletGraphImageCapture.kt (et ses actuals iOS/Android/Desktop) qui
    // permettait de générer ces images a été supprimé. Il n'était plus appelé nulle part depuis ce
    // remplacement HTML/CSS — conservé en dead code jusqu'ici. Ne pas le réintroduire sans revoir
    // le rendu iOS (UIMarkupTextPrintFormatter ne sait pas rendre les images SVG/PNG).

}
