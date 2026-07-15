package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Utils.LinearProgrammingSolver
import fr.vetbrain.vetnutri_mp.Utils.LpConstraint
import fr.vetbrain.vetnutri_mp.Utils.LpConstraintSense
import fr.vetbrain.vetnutri_mp.Utils.LpModel
import fr.vetbrain.vetnutri_mp.Utils.LpSolution
import fr.vetbrain.vetnutri_mp.Utils.LpVariable
import fr.vetbrain.vetnutri_mp.View.AnalNut.AlimentAdjustmentData
import fr.vetbrain.vetnutri_mp.View.AnalNut.arrondirQuantiteSelonRegles

/**
 * Convertit une valeur de référence nutritionnelle (Nut4Ref) en besoin absolu, en grammes/jour,
 * selon son mode d'expression (par kg de poids, par kg de poids métabolique, par 1000 kcal, ou
 * déjà en valeur absolue). Logique reprise à l'identique de l'heuristique historique
 * (calculerBesoinAbsoluGrammes dans MultiNutrientAdjustmentDialog.kt), extraite ici pour être
 * partagée avec le solveur par contraintes.
 */
fun computeAbsoluteGramNeed(
        nutrimentRef: ReferenceEv.Nut4Ref,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        besoinEnergetiqueReference: Double
): Double {
        val quantiteEnGrammes = nutrimentRef.quantite * nutrimentRef.unite.conv
        return when (nutrimentRef.uniteReq) {
                UnitReqEnum.PERKG -> quantiteEnGrammes * (poidsAnimal ?: 0.0)
                UnitReqEnum.PERMS -> quantiteEnGrammes * (poidsMetabolique ?: 0.0)
                UnitReqEnum.PERKCAL -> (quantiteEnGrammes / 1000.0) * besoinEnergetiqueReference
                else -> quantiteEnGrammes
        }
}

/**
 * Correspondance numérateur/dénominateur pour les nutriments-ratios ([NutrientAnalysis]) qui sont
 * de vrais ratios de deux autres nutriments (vérifié contre `RationNutrientAnalyzer.kt` :
 * `calculerRatioGlobalRation` et la correction Ca:P codée en dur dans
 * `MultiNutrientAdjustmentDialog.kt`). Une borne `ratio >= r` (resp. `<= r`) se linéarise en
 * `numérateur - r·dénominateur >= 0` (resp. `<= 0`), car le dénominateur est une somme pondérée
 * positive de quantités d'aliments (jamais négative).
 *
 * `MethCys`/`PhenTyr` ne figurent pas ici : malgré leur regroupement dans le même enum, ce sont en
 * réalité des sommes (Méthionine+Cystéine, Phénylalanine+Tyrosine), pas des ratios — elles restent
 * gérées par le chemin générique (somme pondérée classique), sans traitement spécial.
 *
 * `nonOsPhos`/`nonOsProt`/`nonOsPP` (phosphore/protéine "non osseux") sont exclus de toute
 * génération automatique de contrainte : aucune formule de calcul n'existe dans le code (pas de
 * soustraction basée sur les cendres/le contenu osseux) — les inclure produirait des coefficients
 * silencieusement nuls plutôt qu'une valeur nutritionnellement correcte.
 */
private val RATIO_NUTRIENT_PAIRS: Map<NutrientAnalysis, Pair<Nutrient, Nutrient>> =
        mapOf(
                NutrientAnalysis.NaK to (NutrientMacro.K to NutrientMacro.NA),
                NutrientAnalysis.PCa to (NutrientMacro.CAL to NutrientMacro.PHOS),
                NutrientAnalysis.o6o3 to (NutrientLipid.O6 to NutrientLipid.O3),
                NutrientAnalysis.ZnCu to (NutrientMin.ZN to NutrientMin.CU),
                NutrientAnalysis.PhosphProt to (NutrientMain.PROTEINE to NutrientMacro.PHOS)
        )

private val EXCLUDED_NUTRIENT_ANALYSIS: Set<NutrientAnalysis> =
        setOf(NutrientAnalysis.nonOsPhos, NutrientAnalysis.nonOsProt, NutrientAnalysis.nonOsPP)

/**
 * Liste les nutriments "contraignables" pour une référence et une ration données : ceux ayant une
 * borne MIN/OPTIMIN et/ou MAX/OPTIMAX définie dans [referenceUtilisee] (les ratios non
 * linéarisables listés dans [EXCLUDED_NUTRIENT_ANALYSIS] étant exclus) ET dont la valeur
 * effective — via
 * [AlimentRation.getNutrientWithComplementary], qui retombe sur les équations complémentaires de
 * [referenceUtilisee] (`equationsNut`) si la valeur directe est absente, exactement comme
 * l'énergie retombe sur `equationDEcom`/`equationDEraw` — est non nulle pour au moins un aliment
 * de [ration]. Une contrainte sur un nutriment dont aucun aliment (ni directement, ni par
 * équation) ne fournit de valeur n'aurait que des coefficients nuls et ne pourrait jamais être
 * satisfaite si c'est une borne inférieure. L'énergie est toujours contraignable, indépendamment
 * de ce calcul (son besoin absolu est connu par ailleurs, cf. [AlimentRation.getEnergie]).
 * Sert à peupler la sélection de nutriments proposée à l'utilisateur avant de lancer l'ajustement
 * par contraintes.
 */
suspend fun listConstrainableNutrients(
        referenceUtilisee: ReferenceEv,
        ration: Ration,
        equationRepository: EquationRepository? = null
): List<Nutrient> {
        val candidates =
                linkedSetOf<Nutrient>().apply {
                        addAll(referenceUtilisee.getRefMapMin().keys)
                        addAll(referenceUtilisee.getRefMapOMin().keys)
                        addAll(referenceUtilisee.getRefMapMax().keys)
                        addAll(referenceUtilisee.getRefMapOMax().keys)
                        removeAll { it in EXCLUDED_NUTRIENT_ANALYSIS }
                }

        println(
                "[RCA] listConstrainableNutrients: reference='${referenceUtilisee.nom}' (${referenceUtilisee.espece}/${referenceUtilisee.stadePhysio}), " +
                        "candidates before presence-filter (${candidates.size}): " +
                        candidates.joinToString(", ") { "${it.label}[${it::class.simpleName}]" }
        )
        println(
                "[RCA] RATIO_NUTRIENT_PAIRS keys: " +
                        RATIO_NUTRIENT_PAIRS.keys.joinToString(", ") { "${it.label}[${it::class.simpleName}]" } +
                        " — intersection with candidates: " +
                        candidates.filter { it in RATIO_NUTRIENT_PAIRS }.joinToString(", ") { it.label }
        )

        suspend fun hasNonzeroValueSomewhere(nutrient: Nutrient): Boolean {
                for (alimentRation in ration.alimentMutableList) {
                        val value =
                                alimentRation.getNutrientWithComplementary(
                                        nutrient,
                                        equationRepository = equationRepository,
                                        referenceEv = referenceUtilisee
                                )
                                        ?: 0.0
                        println(
                                "[RCA]     ${nutrient.label} on aliment '${alimentRation.aliment?.nom}' " +
                                        "(uuid=${alimentRation.uuid}) = $value"
                        )
                        if (value > 0.0) return true
                }
                return false
        }

        val result = linkedSetOf<Nutrient>()
        for (nutrient in candidates) {
                val ratioPair = RATIO_NUTRIENT_PAIRS[nutrient]
                val presentSomewhere =
                        if (ratioPair != null) {
                                // Un ratio n'est exploitable que si numérateur ET dénominateur ont
                                // chacun une source quelque part dans la ration (pas forcément le
                                // même aliment).
                                val numeratorPresent = hasNonzeroValueSomewhere(ratioPair.first)
                                val denominatorPresent = hasNonzeroValueSomewhere(ratioPair.second)
                                println(
                                        "[RCA]   ratio ${nutrient.label}: numerator=${ratioPair.first.label} present=$numeratorPresent, " +
                                                "denominator=${ratioPair.second.label} present=$denominatorPresent"
                                )
                                numeratorPresent && denominatorPresent
                        } else {
                                hasNonzeroValueSomewhere(nutrient)
                        }
                if (presentSomewhere) result.add(nutrient)
        }
        result.add(NutrientMain.ENERGIE)
        println("[RCA] listConstrainableNutrients result (${result.size}): " + result.joinToString(", ") { it.label })
        return result.toList()
}

/** Une contrainte MIN/MAX construite pour un nutriment donné, conservée pour le diagnostic UI. */
data class NutrientConstraintInfo(
        val nutrient: Nutrient,
        val refLevel: Reflevel,
        val requiredGrams: Double,
        val sense: LpConstraintSense
)

/** Résultat de l'ajustement par programmation sous contrainte. */
data class ConstraintAdjustmentResult(
        val success: Boolean,
        val message: String,
        val adjustedAliments: List<AlimentRation>? = null,
        val violatedConstraints: List<NutrientConstraintInfo> = emptyList()
)

/**
 * Ajuste les quantités des aliments non verrouillés d'une ration afin de satisfaire
 * simultanément toutes les bornes MIN/OPTIMIN (inférieures) et MAX/OPTIMAX (supérieures)
 * définies dans [referenceUtilisee] pour chaque nutriment, via une résolution par programmation
 * linéaire (voir [LinearProgrammingSolver]).
 *
 * Contrairement à l'heuristique séquentielle historique (calculerAjustement), qui ne traite
 * qu'un nutriment à la fois et n'impose jamais de borne supérieure, cette fonction construit un
 * seul modèle linéaire couvrant tous les nutriments référencés et cherche la solution qui
 * s'écarte le moins possible des quantités actuelles (pondérée par le champ "weight" de
 * préférence par aliment), tout en respectant toutes les bornes simultanément. Il ne s'agit pas
 * d'un problème de coût au sens classique (l'application ne connaît pas de prix par aliment) :
 * l'objectif minimise le changement par rapport à la ration actuelle, pas un coût réel.
 *
 * Les nutriments-ratios ([NutrientAnalysis] : Ca:P, K:Na, Ω6:Ω3, Zn:Cu, Prot:Phos — voir
 * [RATIO_NUTRIENT_PAIRS]) sont linéarisés en `numérateur - r·dénominateur >= 0` (borne
 * inférieure) ou `<= 0` (borne supérieure). `nonOsPhos`/`nonOsProt`/`nonOsPP` restent exclus
 * (aucune formule de calcul définie dans le code, voir [EXCLUDED_NUTRIENT_ANALYSIS]).
 *
 * Les aliments verrouillés ([AlimentAdjustmentData.isLocked]) gardent leur quantité actuelle
 * (constante soustraite du second membre de chaque contrainte) et ne sont pas des variables de
 * décision. Les champs [AlimentAdjustmentData.minQuantity]/[AlimentAdjustmentData.maxQuantity]
 * (jusqu'ici jamais appliqués par l'heuristique existante) deviennent les bornes réelles des
 * variables de décision.
 *
 * [selectedNutrients] restreint l'ensemble des nutriments effectivement contraints (voir
 * [listConstrainableNutrients] pour l'ensemble complet proposé à l'utilisateur) : seule
 * l'intersection avec les nutriments ayant une borne dans [referenceUtilisee] est utilisée.
 */
suspend fun adjustRationByConstraints(
        ration: Ration,
        adjustmentData: List<AlimentAdjustmentData>,
        referenceUtilisee: ReferenceEv,
        besoinEnergetiqueTotal: Double,
        besoinEnergetiqueStandard: Double,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        equationRepository: EquationRepository?,
        selectedNutrients: Set<Nutrient>
): ConstraintAdjustmentResult {
        try {
                val lockedUuids =
                        adjustmentData.filter { it.isLocked }.map { it.alimentRation.uuid }.toSet()
                val freeAliments = ration.alimentMutableList.filter { it.uuid !in lockedUuids }
                val lockedAliments = ration.alimentMutableList.filter { it.uuid in lockedUuids }

                if (freeAliments.isEmpty()) {
                        return ConstraintAdjustmentResult(
                                success = false,
                                message =
                                        "Aucun aliment disponible pour l'ajustement par contraintes (tous les aliments sont verrouillés)."
                        )
                }

                val numFoods = freeAliments.size
                val totalVarCount = 3 * numFoods // aliments libres + dPlus + dMinus (déviation)

                suspend fun gramCoefficient(alimentRation: AlimentRation, nutrient: Nutrient): Double {
                        val aliment = alimentRation.aliment ?: return 0.0
                        return if (nutrient == NutrientMain.ENERGIE) {
                                val probe = AlimentRation(aliment = aliment, quantite = 100.0, weight = 1.0)
                                probe.getEnergie(referenceUtilisee, equationRepository) / 100.0
                        } else {
                                (alimentRation.getNutrientWithComplementary(
                                        nutrient,
                                        equationRepository = equationRepository,
                                        referenceEv = referenceUtilisee
                                )
                                        ?: 0.0) / 100.0
                        }
                }

                fun bestLowerBound(nutrient: Nutrient): ReferenceEv.Nut4Ref? {
                        val optimin = referenceUtilisee.obtenirNutrimentRef(nutrient, Reflevel.OPTIMIN)
                        if (optimin != null && optimin.quantite > 0.0) return optimin
                        return referenceUtilisee.obtenirNutrimentRef(nutrient, Reflevel.MIN)
                }

                fun bestUpperBound(nutrient: Nutrient): ReferenceEv.Nut4Ref? {
                        val optimax = referenceUtilisee.obtenirNutrimentRef(nutrient, Reflevel.OPTIMAX)
                        if (optimax != null && optimax.quantite > 0.0) return optimax
                        return referenceUtilisee.obtenirNutrimentRef(nutrient, Reflevel.MAX)
                }

                // Parmi tous les nutriments ayant une borne MIN/OPTIMIN et/ou MAX/OPTIMAX dans la
                // référence (pas seulement ceux sélectionnés manuellement par aliment dans le
                // dialogue), seuls ceux choisis par l'utilisateur via [selectedNutrients] sont
                // effectivement contraints — c'est l'intérêt de cette approche par rapport à
                // l'heuristique séquentielle existante, qui elle ne permet pas de choisir un
                // sous-ensemble explicite de nutriments à satisfaire simultanément.
                val candidateNutrients =
                        listConstrainableNutrients(referenceUtilisee, ration, equationRepository)
                                .filter { it in selectedNutrients }

                if (candidateNutrients.isEmpty()) {
                        return ConstraintAdjustmentResult(
                                success = false,
                                message = "Aucun nutriment sélectionné pour l'ajustement par contraintes."
                        )
                }

                val constraints = mutableListOf<LpConstraint>()
                val constraintInfos = mutableMapOf<String, NutrientConstraintInfo>()

                for (nutrient in candidateNutrients) {
                        val ratioPair = RATIO_NUTRIENT_PAIRS[nutrient]
                        if (ratioPair != null) {
                                // Linéarisation d'une borne de ratio : numérateur/dénominateur >= r
                                // (resp. <=) devient numérateur - r·dénominateur >= 0 (resp. <= 0),
                                // valide car le dénominateur (somme pondérée de quantités
                                // d'aliments) est toujours >= 0.
                                val (numerator, denominator) = ratioPair
                                val numeratorCoeffs = DoubleArray(totalVarCount)
                                val denominatorCoeffs = DoubleArray(totalVarCount)
                                for (i in 0 until numFoods) {
                                        numeratorCoeffs[i] = gramCoefficient(freeAliments[i], numerator)
                                        denominatorCoeffs[i] = gramCoefficient(freeAliments[i], denominator)
                                }
                                var lockedNumerator = 0.0
                                var lockedDenominator = 0.0
                                for (locked in lockedAliments) {
                                        lockedNumerator += gramCoefficient(locked, numerator) * locked.quantite
                                        lockedDenominator += gramCoefficient(locked, denominator) * locked.quantite
                                }

                                val lowerRatioRef = bestLowerBound(nutrient)
                                if (lowerRatioRef != null && lowerRatioRef.quantite > 0.0) {
                                        val ratioBound = lowerRatioRef.quantite
                                        val coeffs = DoubleArray(totalVarCount)
                                        for (i in 0 until numFoods) {
                                                coeffs[i] = numeratorCoeffs[i] - ratioBound * denominatorCoeffs[i]
                                        }
                                        val rhs = ratioBound * lockedDenominator - lockedNumerator
                                        val name = "MIN(${nutrient.label})"
                                        constraints.add(LpConstraint(name, coeffs, LpConstraintSense.GE, rhs))
                                        constraintInfos[name] =
                                                NutrientConstraintInfo(
                                                        nutrient,
                                                        lowerRatioRef.niveauRelatif,
                                                        ratioBound,
                                                        LpConstraintSense.GE
                                                )
                                }

                                val upperRatioRef = bestUpperBound(nutrient)
                                if (upperRatioRef != null && upperRatioRef.quantite > 0.0) {
                                        val ratioBound = upperRatioRef.quantite
                                        val coeffs = DoubleArray(totalVarCount)
                                        for (i in 0 until numFoods) {
                                                coeffs[i] = numeratorCoeffs[i] - ratioBound * denominatorCoeffs[i]
                                        }
                                        val rhs = ratioBound * lockedDenominator - lockedNumerator
                                        val name = "MAX(${nutrient.label})"
                                        constraints.add(LpConstraint(name, coeffs, LpConstraintSense.LE, rhs))
                                        constraintInfos[name] =
                                                NutrientConstraintInfo(
                                                        nutrient,
                                                        upperRatioRef.niveauRelatif,
                                                        ratioBound,
                                                        LpConstraintSense.LE
                                                )
                                }
                                continue
                        }

                        val coefficients = DoubleArray(totalVarCount)
                        for (i in 0 until numFoods) {
                                coefficients[i] = gramCoefficient(freeAliments[i], nutrient)
                        }
                        var lockedContribution = 0.0
                        for (locked in lockedAliments) {
                                lockedContribution += gramCoefficient(locked, nutrient) * locked.quantite
                        }

                        if (nutrient == NutrientMain.ENERGIE) {
                                val requiredGrams = besoinEnergetiqueTotal
                                val adjustedRhs = requiredGrams - lockedContribution
                                if (adjustedRhs > 1e-9) {
                                        val name = "MIN(${nutrient.label})"
                                        constraints.add(
                                                LpConstraint(name, coefficients.copyOf(), LpConstraintSense.GE, adjustedRhs)
                                        )
                                        constraintInfos[name] =
                                                NutrientConstraintInfo(nutrient, Reflevel.MIN, requiredGrams, LpConstraintSense.GE)
                                }
                                val upperRef = bestUpperBound(nutrient)
                                if (upperRef != null && upperRef.quantite > 0.0) {
                                        val name = "MAX(${nutrient.label})"
                                        constraints.add(
                                                LpConstraint(name, coefficients.copyOf(), LpConstraintSense.LE, adjustedRhs)
                                        )
                                        constraintInfos[name] =
                                                NutrientConstraintInfo(
                                                        nutrient,
                                                        upperRef.niveauRelatif,
                                                        requiredGrams,
                                                        LpConstraintSense.LE
                                                )
                                }
                                continue
                        }

                        val lowerRef = bestLowerBound(nutrient)
                        if (lowerRef != null && lowerRef.quantite > 0.0) {
                                val requiredGrams =
                                        computeAbsoluteGramNeed(
                                                lowerRef,
                                                poidsAnimal,
                                                poidsMetabolique,
                                                besoinEnergetiqueStandard
                                        )
                                val adjustedRhs = requiredGrams - lockedContribution
                                if (adjustedRhs > 1e-9) {
                                        val name = "MIN(${nutrient.label})"
                                        constraints.add(
                                                LpConstraint(name, coefficients.copyOf(), LpConstraintSense.GE, adjustedRhs)
                                        )
                                        constraintInfos[name] =
                                                NutrientConstraintInfo(
                                                        nutrient,
                                                        lowerRef.niveauRelatif,
                                                        requiredGrams,
                                                        LpConstraintSense.GE
                                                )
                                }
                        }

                        val upperRef = bestUpperBound(nutrient)
                        if (upperRef != null && upperRef.quantite > 0.0) {
                                val requiredGrams =
                                        computeAbsoluteGramNeed(
                                                upperRef,
                                                poidsAnimal,
                                                poidsMetabolique,
                                                besoinEnergetiqueStandard
                                        )
                                val adjustedRhs = requiredGrams - lockedContribution
                                val name = "MAX(${nutrient.label})"
                                constraints.add(
                                        LpConstraint(name, coefficients.copyOf(), LpConstraintSense.LE, adjustedRhs)
                                )
                                constraintInfos[name] =
                                        NutrientConstraintInfo(
                                                nutrient,
                                                upperRef.niveauRelatif,
                                                requiredGrams,
                                                LpConstraintSense.LE
                                        )
                        }
                }

                if (constraints.isEmpty()) {
                        return ConstraintAdjustmentResult(
                                success = false,
                                message = "Aucune contrainte MIN/MAX exploitable trouvée dans la référence sélectionnée."
                        )
                }

                val variables = mutableListOf<LpVariable>()
                for (i in 0 until numFoods) {
                        val data = adjustmentData.find { it.alimentRation.uuid == freeAliments[i].uuid }
                        val lb = (data?.minQuantity ?: 0.0).coerceAtLeast(0.0)
                        val ubRaw = data?.maxQuantity ?: Double.MAX_VALUE
                        val ub = if (ubRaw >= Double.MAX_VALUE) Double.POSITIVE_INFINITY else ubRaw
                        variables.add(
                                LpVariable(name = freeAliments[i].uuid, lowerBound = lb, upperBound = maxOf(ub, lb))
                        )
                }
                // dPlus_i (indices numFoods..2*numFoods-1) et dMinus_i (2*numFoods..3*numFoods-1) :
                // variables de déviation par rapport à la quantité actuelle de chaque aliment.
                // Pas de coût réel (pas de prix par aliment dans l'app) : l'objectif minimise le
                // changement, pondéré par le poids de préférence par aliment (un poids plus
                // faible rend le déplacement de cet aliment "moins coûteux" pour le solveur).
                for (i in 0 until numFoods) {
                        val data = adjustmentData.find { it.alimentRation.uuid == freeAliments[i].uuid }
                        val weight = (data?.weight ?: 1.0).let { if (it <= 0.0) 0.01 else it }
                        variables.add(LpVariable(name = "dPlus_${freeAliments[i].uuid}", objectiveCoefficient = weight))
                }
                for (i in 0 until numFoods) {
                        val data = adjustmentData.find { it.alimentRation.uuid == freeAliments[i].uuid }
                        val weight = (data?.weight ?: 1.0).let { if (it <= 0.0) 0.01 else it }
                        variables.add(LpVariable(name = "dMinus_${freeAliments[i].uuid}", objectiveCoefficient = weight))
                }

                // Contraintes de liaison quantité/déviation : x_i - dPlus_i + dMinus_i = quantité actuelle_i
                for (i in 0 until numFoods) {
                        val coeffs = DoubleArray(totalVarCount)
                        coeffs[i] = 1.0
                        coeffs[numFoods + i] = -1.0
                        coeffs[2 * numFoods + i] = 1.0
                        constraints.add(
                                LpConstraint(
                                        name = "deviation_${freeAliments[i].uuid}",
                                        coefficients = coeffs,
                                        sense = LpConstraintSense.EQ,
                                        rhs = freeAliments[i].quantite
                                )
                        )
                }

                val model = LpModel(variables, constraints)
                return when (val solution = LinearProgrammingSolver.solve(model)) {
                        is LpSolution.Optimal -> {
                                val adjusted =
                                        ration.alimentMutableList.map { ar ->
                                                if (ar.uuid in lockedUuids) {
                                                        ar
                                                } else {
                                                        val idx = freeAliments.indexOfFirst { it.uuid == ar.uuid }
                                                        val rawQuantite = solution.values[idx]
                                                        ar.copy(quantite = arrondirQuantiteSelonRegles(ar, rawQuantite))
                                                }
                                        }
                                ConstraintAdjustmentResult(
                                        success = true,
                                        message =
                                                "Ration ajustée par programmation sous contrainte : ${constraintInfos.size} contrainte(s) MIN/MAX satisfaite(s) simultanément.",
                                        adjustedAliments = adjusted
                                )
                        }
                        is LpSolution.Infeasible -> {
                                if (solution.violatedConstraints == listOf("__non_convergent__")) {
                                        ConstraintAdjustmentResult(
                                                success = false,
                                                message = "Le solveur n'a pas convergé — vérifiez les bornes min/max des aliments."
                                        )
                                } else {
                                        val violatedInfos =
                                                solution.violatedConstraints.mapNotNull { constraintInfos[it] }
                                        val details =
                                                violatedInfos.joinToString(", ") {
                                                        "${it.refLevel.label}(${it.nutrient.label})"
                                                }
                                        ConstraintAdjustmentResult(
                                                success = false,
                                                message =
                                                        "Impossible de satisfaire simultanément : $details avec les aliments et bornes actuels.",
                                                violatedConstraints = violatedInfos
                                        )
                                }
                        }
                }
        } catch (e: Exception) {
                return ConstraintAdjustmentResult(
                        success = false,
                        message = "Erreur lors de l'ajustement par contraintes : ${e.message}"
                )
        }
}
