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
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Utils.GraphFormattingUtils
import fr.vetbrain.vetnutri_mp.Utils.TextUtils

/**
 * Calculs d'affichage/conformité partagés entre l'écran d'analyse de ration (View/AnalNut, via
 * Compose) et l'export PDF (Export/HtmlDocumentBuilder.kt, via HTML). Pas de dépendance
 * Compose ici : c'est ce qui garantit que le PDF ne peut pas diverger de l'écran.
 */
enum class ConformiteStatus { CONFORME, CARENCE, EXCES, CARENCE_MALADIE, EXCES_MALADIE }

data class ConformiteResult(
    val status: ConformiteStatus,
    val isCritical: Boolean, // true = MIN/MAX (icône double), false = OPTIMIN/OPTIMAX (icône simple)
    val description: String
)

/**
 * true seulement pour les vrais ratios de NutrientAnalysis (NaK/KNA, PCa/CAP, o6o3/O6O3,
 * ZnCu/ZNCU, PhosphProt/PROTP, nonOsPP — `unite` vide) — pas pour ses valeurs absolues calculées
 * (nonOsPhos/nonOsProt en %, MethCys/PhenTyr en g), qui doivent être sommées comme un nutriment
 * normal. Point d'entrée unique pour cette classification : ne pas tester `is NutrientAnalysis`
 * seul, ni `ValeurNutritionnelle.unite.displayName` (toujours vide pour les 10 entrées).
 */
fun estNutrimentAnalysisRatio(nutriment: Nutrient): Boolean =
    nutriment is NutrientAnalysis && nutriment.unite.isBlank()

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

/**
 * Convertit une valeur de référence (stockée dans son unité `uniteRef`) vers l'unité des
 * préférences utilisateur `unitePreferences`, en passant par une valeur absolue intermédiaire.
 * Utilisé pour aligner les bornes de référence (MIN/OPTIMIN/OPTIMAX/MAX) sur la même échelle que
 * l'apport affiché dans le bullet graph.
 */
fun convertirVersUnitePreferences(
    valeurRef: Double,
    uniteRef: UnitReqEnum,
    unitePreferences: UnitReqEnum,
    besoinEnergetiqueEntretien: Double?,
    poidsAnimal: Double?,
    poidsMetabolique: Double?
): Double? {
    if (uniteRef == unitePreferences) {
        return valeurRef
    }

    val valeurAbsolue = calculerBesoinAbsolu(
        valeurRef, uniteRef, besoinEnergetiqueEntretien, poidsAnimal, poidsMetabolique
    ) ?: return null

    return when (unitePreferences) {
        UnitReqEnum.PERKG -> poidsAnimal?.let { poids -> if (poids > 0.0) (valeurAbsolue / poids) else null }
        UnitReqEnum.PERMS -> poidsMetabolique?.let { poidsMetab -> if (poidsMetab > 0.0) (valeurAbsolue / poidsMetab) else null }
        UnitReqEnum.PERKCAL -> besoinEnergetiqueEntretien?.let { bee -> if (bee > 0.0) ((valeurAbsolue * 1000.0) / bee) else null }
        UnitReqEnum.PERKJ -> besoinEnergetiqueEntretien?.let { bee ->
            if (bee > 0.0) {
                val beeEnKj = bee * 4.184
                (valeurAbsolue * 1000.0) / beeEnKj
            } else null
        }
        UnitReqEnum.ABSOLUTE -> valeurAbsolue
        UnitReqEnum.RATIO -> null
    }
}

/** true si NutrientMain.ENERGIE a une bande synthétique par défaut (pas de référence explicite). */
fun hasDefaultEnergyReferenceLevels(nutriment: Nutrient, besoinEnergetiqueEntretien: Double?): Boolean {
    return nutriment == NutrientMain.ENERGIE && besoinEnergetiqueEntretien != null && besoinEnergetiqueEntretien > 0.0
}

/**
 * Borne synthétique (MIN=90, MAX=110) exprimée en % du BEE pour NutrientMain.ENERGIE quand
 * aucune référence explicite n'est disponible — même échelle que le bullet graph (apport en % BEE).
 * `typeExpressionBesoin`/`poidsAnimal`/`poidsMetabolique` ne sont pas utilisés par ce calcul
 * (conservés pour compatibilité de signature avec les appels existants).
 */
fun defaultEnergyReferenceLevel(
    nutriment: Nutrient,
    level: Reflevel,
    typeExpressionBesoin: TypeExpressionBesoin? = null,
    besoinEnergetiqueEntretien: Double?,
    poidsAnimal: Double? = null,
    poidsMetabolique: Double? = null
): Double? {
    if (!hasDefaultEnergyReferenceLevels(nutriment, besoinEnergetiqueEntretien)) return null
    val factor = when (level) {
        Reflevel.MIN -> 0.9
        Reflevel.MAX -> 1.1
        else -> return null
    }
    return factor * 100.0
}

/** Données prêtes à tracer pour un bullet graph (apport + bornes de référence, même échelle). */
data class BulletGraphData(
    val apport: Double,
    val minRef: Double?,
    val optiminRef: Double?,
    val optimaxRef: Double?,
    val maxRef: Double?,
    val maxAxis: Double
)

/**
 * Calcule les données du bullet graph pour un nutriment, exactement comme
 * `DetailNutrimentAnalysis.kt::ReferenceBulletGraph` : apport et bornes de référence convertis
 * dans la même unité de préférences, échelle 0..max(valeurs)*1.1. Retourne null si rien à tracer
 * (aucune référence, ou toutes les valeurs à 0).
 */
fun calculerBulletGraphData(
    valeurNutritionnelle: ValeurNutritionnelle,
    reference: ReferenceEv?,
    typeExpressionBesoin: TypeExpressionBesoin?,
    poidsAnimal: Double?,
    poidsMetabolique: Double?,
    besoinEnergetiqueEntretien: Double?,
    besoinEnergetiqueCible: Double?
): BulletGraphData? {
    if (reference == null) return null
    val nutrient = valeurNutritionnelle.nutriment
    val typeExpr = typeExpressionBesoin ?: TypeExpressionBesoin.DEFAULT
    val isAnalysisNoUnit = estNutrimentAnalysisRatio(nutrient)

    fun convertirRef(valeurRef: Double, uniteRef: UnitReqEnum): Double? =
        if (isAnalysisNoUnit) valeurRef
        else convertirVersUnitePreferences(
            valeurRef, uniteRef, typeExpr.unitReqEnum, besoinEnergetiqueEntretien, poidsAnimal, poidsMetabolique
        ) ?: valeurRef

    fun refLevel(level: Reflevel, defaultLevel: Reflevel?): Double? {
        val valeurRef = reference.obtenirNutriment(nutrient, level)
        return if (valeurRef > 0.0) {
            convertirRef(valeurRef, UnitReqEnum.getById(reference.obtenirUniteNutriment(nutrient, level)))
        } else if (defaultLevel != null) {
            defaultEnergyReferenceLevel(nutrient, defaultLevel, besoinEnergetiqueEntretien = besoinEnergetiqueCible)
        } else null
    }

    val minRef = refLevel(Reflevel.MIN, Reflevel.MIN)
    val optiminRef = refLevel(Reflevel.OPTIMIN, null)
    val optimaxRef = refLevel(Reflevel.OPTIMAX, null)
    val maxRef = refLevel(Reflevel.MAX, Reflevel.MAX)

    val apportConverti = if (isAnalysisNoUnit) {
        valeurNutritionnelle.valeur
    } else {
        convertirVersUnitePreferences(
            valeurNutritionnelle.valeur, UnitReqEnum.ABSOLUTE, typeExpr.unitReqEnum,
            besoinEnergetiqueEntretien, poidsAnimal, poidsMetabolique
        ) ?: valeurNutritionnelle.valeur
    }
    val apport = if (hasDefaultEnergyReferenceLevels(nutrient, besoinEnergetiqueCible)) {
        (valeurNutritionnelle.valeur / besoinEnergetiqueCible!!) * 100.0
    } else {
        apportConverti
    }

    val valeurs = listOfNotNull(apport, minRef, optiminRef, optimaxRef, maxRef)
    if (valeurs.isEmpty()) return null
    val maxAxis = valeurs.max() * 1.1
    if (maxAxis <= 0.0) return null

    return BulletGraphData(apport, minRef, optiminRef, optimaxRef, maxRef, maxAxis)
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
    referencesMaladies: List<ReferenceEv> = emptyList(),
    besoinEnergetiqueCible: Double? = besoinEnergetiqueEntretien
): ConformiteResult? {
    val nutrient = valeurNutritionnelle.nutriment
    val apportAbsolu = valeurNutritionnelle.valeur
    val isNutrimentRatio = estNutrimentAnalysisRatio(nutrient)

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
            val defaultNeed = defaultEnergyNeed(nutrient, level, besoinEnergetiqueCible)
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
            val defaultNeed = defaultEnergyNeed(nutrient, level, besoinEnergetiqueCible)
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

    if (estNutrimentAnalysisRatio(valeurNutritionnelle.nutriment)) {
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

/**
 * Contribution absolue d'un ingrédient de la ration à l'apport total d'un nutriment — même calcul
 * que `DetailNutrimentAnalysis.kt::calculateContributionForGraph` (segments colorés par ingrédient
 * du bullet graph).
 */
suspend fun calculerContributionIngredient(
    alimentRation: AlimentRation,
    nutriment: Nutrient,
    reference: ReferenceEv?,
    equationRepository: EquationRepository?
): Double {
    val quantiteIngredient = alimentRation.quantite
    if (quantiteIngredient <= 0.0) return 0.0

    if (nutriment == NutrientMain.ENERGIE) {
        return alimentRation.getEnergie(reference, equationRepository)
    }

    val valeurPour100g = alimentRation.getNutrientWithComplementary(
        nutrient = nutriment,
        equationRepository = equationRepository,
        referenceEv = reference
    ) ?: 0.0

    val valeurConvertie = if (nutriment is AAEnum) {
        val teneurProteines = alimentRation.aliment?.getNutrient(NutrientMain.PROTEINE) ?: 0.0
        (valeurPour100g * teneurProteines) / 100.0
    } else {
        valeurPour100g
    }

    return (valeurConvertie * quantiteIngredient) / 100.0
}

/** Contribution d'un ingrédient (son index dans la ration + sa contribution absolue au nutriment). */
data class ContributionIngredient(val index: Int, val contribution: Double)

/**
 * Contributions par ingrédient pour un nutriment donné, dans l'ordre de la ration (index utilisé
 * pour la couleur, via le même index que la liste des aliments). Vide pour les nutriments-ratio
 * (pas de notion de contribution par ingrédient) ou si l'apport est nul.
 */
suspend fun calculerContributionsIngredients(
    ration: Ration,
    nutriment: Nutrient,
    reference: ReferenceEv?,
    equationRepository: EquationRepository?
): List<ContributionIngredient> {
    if (estNutrimentAnalysisRatio(nutriment)) return emptyList()
    return ration.alimentMutableList.mapIndexedNotNull { index, alimentRation ->
        val contribution = calculerContributionIngredient(alimentRation, nutriment, reference, equationRepository)
        if (contribution > 0.0) ContributionIngredient(index, contribution) else null
    }
}

/**
 * Composition d'un aliment seul (hors ration), pour le dialogue "voir la composition" déclenché
 * depuis la liste des ingrédients. `valMap` est déjà exprimé pour 100g de l'aliment tel que fourni
 * (as-fed) — même convention que `calculerQuantiteTotaleNutriment` dans RationNutrientAnalyzer.kt.
 */
fun analyserCompositionAliment(aliment: AlimentEv): Map<String, ValeurNutritionnelle> {
    val resultat = mutableMapOf<String, ValeurNutritionnelle>()

    aliment.valMap.keys.forEach { nutrient ->
        val valeur = aliment.getNutrient(nutrient) ?: return@forEach
        val isRatio = estNutrimentAnalysisRatio(nutrient)
        if (!isRatio && valeur <= 0.0) return@forEach
        resultat[nutrient.label] = ValeurNutritionnelle(
            nutriment = nutrient,
            unite = nutrient.ue,
            valeur = valeur,
            description = "",
            complete = true
        )
    }

    // L'énergie peut être calculée via une équation de référence (energieParEspece) plutôt que
    // stockée directement dans valMap : on s'assure qu'elle apparaît quand même si disponible.
    val energie = aliment.getNutrient(NutrientMain.ENERGIE)
    if (energie != null && energie > 0.0) {
        resultat[NutrientMain.ENERGIE.label] = ValeurNutritionnelle(
            nutriment = NutrientMain.ENERGIE,
            unite = NutrientMain.ENERGIE.ue,
            valeur = energie,
            description = "",
            complete = true
        )
    }

    return resultat
}
