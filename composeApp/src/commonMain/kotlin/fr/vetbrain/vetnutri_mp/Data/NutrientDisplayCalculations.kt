package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.AAEnum
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis
import fr.vetbrain.vetnutri_mp.Enumer.NutrientEnergy
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientOther
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Utils.GraphFormattingUtils
import fr.vetbrain.vetnutri_mp.Utils.TextUtils

/**
 * Calculs d'affichage/conformité partagés entre l'écran d'analyse de ration (View/AnalNut/*.kt,
 * via Compose) et l'export PDF (Export/HtmlDocumentBuilder.kt, via HTML). Pas de dépendance
 * Compose ici : c'est ce qui garantit que le PDF ne peut pas diverger de l'écran.
 */
enum class ConformiteStatus { CONFORME, CARENCE, EXCES, CARENCE_MALADIE, EXCES_MALADIE }

data class ConformiteResult(
    val status: ConformiteStatus,
    val isCritical: Boolean, // true = MIN/MAX (icône double), false = OPTIMIN/OPTIMAX (icône simple)
    val description: String
)

/** Convertit une valeur de référence + son unité en besoin absolu (même unité que l'apport). */
fun calculerBesoinAbsolu(
    valeurRef: Double,
    uniteRef: UnitReqEnum,
    besoinEnergetiqueEntretien: Double?,
    poidsAnimal: Double?,
    poidsMetabolique: Double?
): Double? {
    return when (uniteRef) {
        UnitReqEnum.PERKCAL ->
            besoinEnergetiqueEntretien?.let { bee: Double -> (valeurRef * bee) / 1000.0 }
        UnitReqEnum.PERKJ ->
            besoinEnergetiqueEntretien?.let { bee: Double ->
                val beeEnKj: Double = bee * 4.184
                (valeurRef * beeEnKj) / 1000.0
            }
        UnitReqEnum.PERKG -> poidsAnimal?.let { poids: Double -> valeurRef * poids }
        UnitReqEnum.PERMS -> poidsMetabolique?.let { poidsMetab: Double -> valeurRef * poidsMetab }
        UnitReqEnum.ABSOLUTE -> valeurRef
        UnitReqEnum.RATIO -> null
    }
}

/** Bande synthétique ±10% autour du BEE pour NutrientMain.ENERGIE quand aucune référence explicite n'existe. */
private fun defaultEnergyNeed(
    nutrient: Nutrient,
    level: Reflevel,
    besoinEnergetiqueEntretien: Double?
): Double? {
    if (nutrient != NutrientMain.ENERGIE ||
        besoinEnergetiqueEntretien == null ||
        besoinEnergetiqueEntretien <= 0.0
    ) {
        return null
    }
    val factor =
        when (level) {
            Reflevel.MIN -> 0.9
            Reflevel.MAX -> 1.1
            else -> return null
        }
    return besoinEnergetiqueEntretien * factor
}

/**
 * Détermine le statut de conformité d'un apport nutritionnel vis-à-vis des références (maladie
 * prioritaire, puis référence principale MIN/OPTIMIN/MAX/OPTIMAX). Retourne null si conforme ou
 * si aucune référence n'est disponible.
 */
fun calculerConformite(
    valeurNutritionnelle: ValeurNutritionnelle,
    referenceUtilisee: ReferenceEv?,
    besoinEnergetiqueEntretien: Double?,
    poidsAnimal: Double?,
    poidsMetabolique: Double?,
    referencesMaladies: List<ReferenceEv> = emptyList()
): ConformiteResult? {
    val nutrient = valeurNutritionnelle.nutriment
    val apportAbsolu = valeurNutritionnelle.valeur
    val isNutrimentRatio = when (nutrient) {
        is NutrientAnalysis -> nutrient.unite.isEmpty()
        else -> false
    }

    // Références de maladies (prioritaire)
    referencesMaladies.forEach { refMaladie ->
        listOf(Reflevel.MIN, Reflevel.MAX).forEach { level ->
            if (refMaladie.contientNutriment(nutrient, level)) {
                val valeurRef = refMaladie.obtenirNutriment(nutrient, level)
                val uniteRef = UnitReqEnum.getById(refMaladie.obtenirUniteNutriment(nutrient, level))
                val besoinAbsolu = if (isNutrimentRatio) {
                    valeurRef
                } else {
                    calculerBesoinAbsolu(valeurRef, uniteRef, besoinEnergetiqueEntretien, poidsAnimal, poidsMetabolique)
                }
                besoinAbsolu?.let { besoin ->
                    val isCarence = level == Reflevel.MIN && apportAbsolu < besoin
                    val isExces = level == Reflevel.MAX && apportAbsolu > besoin
                    if (isCarence || isExces) {
                        return ConformiteResult(
                            status = if (isCarence) ConformiteStatus.CARENCE_MALADIE else ConformiteStatus.EXCES_MALADIE,
                            isCritical = true,
                            description = if (isCarence) "Carence (réf. maladie)" else "Excès (réf. maladie)"
                        )
                    }
                }
            }
        }
    }

    referenceUtilisee?.let { reference ->
        var hasReferences = false

        listOf(Reflevel.MIN, Reflevel.OPTIMIN).forEach { level ->
            val defaultNeed = defaultEnergyNeed(nutrient, level, besoinEnergetiqueEntretien)
            if (reference.contientNutriment(nutrient, level) || defaultNeed != null) {
                hasReferences = true
                val valeurRef = reference.obtenirNutriment(nutrient, level)
                val uniteRef = UnitReqEnum.getById(reference.obtenirUniteNutriment(nutrient, level))
                val besoinAbsolu = defaultNeed ?: if (isNutrimentRatio) {
                    valeurRef
                } else {
                    calculerBesoinAbsolu(valeurRef, uniteRef, besoinEnergetiqueEntretien, poidsAnimal, poidsMetabolique)
                }
                besoinAbsolu?.let { besoin ->
                    if (apportAbsolu < besoin) {
                        return ConformiteResult(
                            status = ConformiteStatus.CARENCE,
                            isCritical = level == Reflevel.MIN,
                            description = "Carence : apport inférieur au ${if (level == Reflevel.MIN) "minimum" else "optimal minimum"}"
                        )
                    }
                }
            }
        }

        listOf(Reflevel.MAX, Reflevel.OPTIMAX).forEach { level ->
            val defaultNeed = defaultEnergyNeed(nutrient, level, besoinEnergetiqueEntretien)
            if (reference.contientNutriment(nutrient, level) || defaultNeed != null) {
                hasReferences = true
                val valeurRef = reference.obtenirNutriment(nutrient, level)
                val uniteRef = UnitReqEnum.getById(reference.obtenirUniteNutriment(nutrient, level))
                val besoinAbsolu = defaultNeed ?: if (isNutrimentRatio) {
                    valeurRef
                } else {
                    calculerBesoinAbsolu(valeurRef, uniteRef, besoinEnergetiqueEntretien, poidsAnimal, poidsMetabolique)
                }
                besoinAbsolu?.let { besoin ->
                    if (apportAbsolu > besoin) {
                        return ConformiteResult(
                            status = ConformiteStatus.EXCES,
                            isCritical = level == Reflevel.MAX,
                            description = "Excès : apport supérieur au ${if (level == Reflevel.MAX) "maximum" else "optimal maximum"}"
                        )
                    }
                }
            }
        }

        if (hasReferences) {
            return null // Toutes les normes respectées = pas d'icône
        }
    }

    return null // Pas de référence
}

/**
 * Calcule l'affichage d'un nutriment selon le type d'expression des besoins choisi (préférences
 * utilisateur). Les nutriments d'analyse/ratio (unité vide) court-circuitent la conversion et sont
 * affichés en valeur brute à 2 décimales.
 *
 * @return Pair<valeur formatée, unité d'affichage>
 */
fun calculerAffichageNutriment(
    valeurNutritionnelle: ValeurNutritionnelle,
    typeExpressionBesoin: TypeExpressionBesoin?,
    poidsMetabolique: Double?,
    poidsAnimal: Double?,
    besoinEnergetiqueEntretien: Double?,
    referenceUtilisee: ReferenceEv? = null
): Pair<String, String> {
    val valeurAbsolue = valeurNutritionnelle.valeur
    val uniteOriginale = valeurNutritionnelle.unite.displayName

    val isUnitEmpty = uniteOriginale.isBlank()
    val isAnalysis = valeurNutritionnelle.nutriment is NutrientAnalysis
    if (isAnalysis && isUnitEmpty) {
        return Pair(GraphFormattingUtils.formatDecimal(valeurAbsolue, 2), "")
    }

    val typeExpression = typeExpressionBesoin ?: TypeExpressionBesoin.DEFAULT

    return when (typeExpression) {
        TypeExpressionBesoin.PAR_KG -> {
            poidsAnimal?.let { poids ->
                if (poids > 0) {
                    val valeurParKg = valeurAbsolue / poids
                    Pair(GraphFormattingUtils.formatSmartDecimal(valeurParKg), "$uniteOriginale/kg")
                } else {
                    Pair(
                        GraphFormattingUtils.formatSmartDecimal(valeurAbsolue),
                        "$uniteOriginale (par kg si poids disponible)"
                    )
                }
            }
                ?: Pair(
                    GraphFormattingUtils.formatSmartDecimal(valeurAbsolue),
                    "$uniteOriginale (par kg si poids disponible)"
                )
        }
        TypeExpressionBesoin.PAR_KG_METABOLIQUE -> {
            val puissance = TextUtils.extrairePuissanceEquationBW(
                referenceUtilisee?.equationBW?.equationScript
            )
            poidsMetabolique?.let { poidsMetab ->
                if (poidsMetab > 0) {
                    val valeurParKgMetab = valeurAbsolue / poidsMetab
                    Pair(
                        GraphFormattingUtils.formatSmartDecimal(valeurParKgMetab),
                        "$uniteOriginale/kg${TextUtils.toSuperscript(puissance)}"
                    )
                } else {
                    Pair(
                        GraphFormattingUtils.formatSmartDecimal(valeurAbsolue),
                        "$uniteOriginale (par kg^$puissance si poids métabolique disponible)"
                    )
                }
            }
                ?: Pair(
                    GraphFormattingUtils.formatSmartDecimal(valeurAbsolue),
                    "$uniteOriginale (par kg^$puissance si poids métabolique disponible)"
                )
        }
        TypeExpressionBesoin.PAR_KCAL -> {
            besoinEnergetiqueEntretien?.let { bee ->
                if (bee > 0) {
                    val valeurPar1000Kcal = (valeurAbsolue / bee) * 1000
                    Pair(GraphFormattingUtils.formatSmartDecimal(valeurPar1000Kcal), "$uniteOriginale/1000 kcal")
                } else {
                    Pair(
                        GraphFormattingUtils.formatSmartDecimal(valeurAbsolue),
                        "$uniteOriginale (par 1000 kcal si BEE disponible)"
                    )
                }
            }
                ?: Pair(
                    GraphFormattingUtils.formatSmartDecimal(valeurAbsolue),
                    "$uniteOriginale (par 1000 kcal si BEE disponible)"
                )
        }
        TypeExpressionBesoin.PAR_KJ -> {
            besoinEnergetiqueEntretien?.let { bee ->
                if (bee > 0) {
                    val beeEnKj = bee * 4.184
                    val valeurPar1000Kj = (valeurAbsolue / beeEnKj) * 1000
                    Pair(GraphFormattingUtils.formatSmartDecimal(valeurPar1000Kj), "$uniteOriginale/1000 kJ")
                } else {
                    Pair(
                        GraphFormattingUtils.formatSmartDecimal(valeurAbsolue),
                        "$uniteOriginale (par 1000 kJ si BEE disponible)"
                    )
                }
            }
                ?: Pair(
                    GraphFormattingUtils.formatSmartDecimal(valeurAbsolue),
                    "$uniteOriginale (par 1000 kJ si BEE disponible)"
                )
        }
    }
}

/** Détermine la catégorie d'un nutriment selon son type. */
fun determinerCategorieNutriment(nom: String, nutriment: Any): String {
    return when {
        nutriment is NutrientMain -> "BASE"
        nutriment is NutrientMacro -> "MACRO"
        nutriment is NutrientMin -> "MIN"
        nutriment is NutrientVitam -> "VITAM"
        nutriment is NutrientLipid -> "LIPID"
        nutriment is AAEnum -> "AMA"
        nutriment is NutrientAnalysis -> "ANA"
        nutriment is NutrientEnergy -> "ENERGY"
        else -> "OTHER"
    }
}

/** Traduit les codes de catégorie en titres lisibles. */
fun obtenirTitreCategorie(categorie: String): String {
    return when (categorie) {
        "BASE" -> translate(LocalizationKeys.NutrientCategory.BASE_NAME)
        "MACRO" -> translate(LocalizationKeys.NutrientCategory.MACRO_NAME)
        "MIN" -> translate(LocalizationKeys.NutrientCategory.MIN_NAME)
        "VITAM" -> translate(LocalizationKeys.NutrientCategory.VITAM_NAME)
        "LIPID" -> translate(LocalizationKeys.NutrientCategory.LIPID_NAME)
        "AMA" -> translate(LocalizationKeys.NutrientCategory.AMA_NAME)
        "ANA" -> translate(LocalizationKeys.NutrientCategory.ANA_NAME)
        "OTHER" -> translate(LocalizationKeys.NutrientCategory.OTHER_NAME)
        "ENERGY" -> translate(LocalizationKeys.NutrientCategory.ENERGIE_NAME)
        else -> categorie
    }
}

/** Ordre d'affichage d'un nutriment : offset de catégorie + ordinal de l'enum. */
fun ordreNutrimentParType(nutriment: Nutrient): Int {
    val categorieOffset =
        when (nutriment) {
            is NutrientMain -> 0
            is NutrientMacro -> 1000
            is NutrientMin -> 2000
            is NutrientVitam -> 3000
            is NutrientLipid -> 4000
            is AAEnum -> 5000
            is NutrientAnalysis -> 6000
            is NutrientOther -> 7000
            is NutrientEnergy -> 8000
            else -> 9000
        }

    val index =
        when (nutriment) {
            is NutrientMain -> nutriment.ordinal
            is NutrientMacro -> nutriment.ordinal
            is NutrientMin -> nutriment.ordinal
            is NutrientVitam -> nutriment.ordinal
            is NutrientLipid -> nutriment.ordinal
            is AAEnum -> nutriment.ordinal
            is NutrientAnalysis -> nutriment.ordinal
            is NutrientOther -> nutriment.ordinal
            is NutrientEnergy -> nutriment.ordinal
            else -> 999
        }

    return categorieOffset + index
}

/** Regroupe et trie les valeurs nutritionnelles par catégorie, dans le même ordre que l'écran. */
fun grouperNutrimentsParCategorie(
    valeursNutritionnelles: Map<String, ValeurNutritionnelle>
): Map<String, List<Pair<String, ValeurNutritionnelle>>> {
    val groupes = mutableMapOf<String, MutableList<Pair<String, ValeurNutritionnelle>>>()

    valeursNutritionnelles.forEach { (nom, valeur) ->
        val categorie = determinerCategorieNutriment(nom, valeur.nutriment)
        groupes.getOrPut(categorie) { mutableListOf() }.add(nom to valeur)
    }

    return groupes.mapValues { (_, nutriments) ->
        nutriments.sortedBy { (_, valeur) -> ordreNutrimentParType(valeur.nutriment) }
    }
}

/**
 * Répartition de la composition (matière sèche) en pourcentages des 6 nutriments de base, comme
 * le pie chart "Composition" de l'écran (cardNutrient.kt::generateCompositionData).
 */
fun calculerCompositionPourcentages(
    valeurs: Map<String, ValeurNutritionnelle>
): List<Pair<String, Double>> {
    val targetNutrients = listOf(
        NutrientMain.HUMIDITE,
        NutrientMain.PROTEINE,
        NutrientMain.LIPIDE,
        NutrientMain.ENA,
        NutrientMain.CENDRE,
        NutrientMain.CELLULOSE
    )

    val total = targetNutrients.sumOf { (valeurs[it.name]?.valeur ?: 0.0).coerceAtLeast(0.0) }
    if (total <= 0.0) return emptyList()

    return targetNutrients.mapNotNull { nutrient ->
        val value = valeurs[nutrient.name]?.valeur ?: 0.0
        if (value > 0) nutrient.translateEnum() to (value / total) * 100.0 else null
    }
}

/**
 * Répartition de l'origine énergétique en pourcentages (protéines/lipides/ENA), avec les mêmes
 * coefficients Atwater simplifiés que l'écran (cardNutrient.kt::generateEnergyData) : 3.5 / 8.5 / 3.5.
 */
fun calculerOrigineEnergetiquePourcentages(
    valeurs: Map<String, ValeurNutritionnelle>
): List<Pair<String, Double>> {
    val prot = valeurs[NutrientMain.PROTEINE.name]?.valeur ?: 0.0
    val lipid = valeurs[NutrientMain.LIPIDE.name]?.valeur ?: 0.0
    val ena = valeurs[NutrientMain.ENA.name]?.valeur ?: 0.0

    val energyProt = prot * 3.5
    val energyLipid = lipid * 8.5
    val energyEna = ena * 3.5
    val total = energyProt + energyLipid + energyEna
    if (total <= 0.0) return emptyList()

    return listOfNotNull(
        if (energyProt > 0) NutrientMain.PROTEINE.translateEnum() to (energyProt / total) * 100.0 else null,
        if (energyLipid > 0) NutrientMain.LIPIDE.translateEnum() to (energyLipid / total) * 100.0 else null,
        if (energyEna > 0) NutrientMain.ENA.translateEnum() to (energyEna / total) * 100.0 else null
    )
}
