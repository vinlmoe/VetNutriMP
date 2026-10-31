package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate

// Ajustement multi-nutriments d'une ration (heuristique séquentielle) et arrondi des quantités : moteur sans interface, utilisé par la vue d'ajustement, RationConstraintAdjuster et l'exploration multiration

/** Données d'ajustement pour un aliment spécifique */
data class AlimentAdjustmentData(
        val alimentRation: AlimentRation,
        var selectedNutrient: String? = null,
        var isLocked: Boolean = false,
        var mode: ConstraintMode = ConstraintMode.ADJUSTABLE,
        var weight: Double = 1.0,
        var minQuantity: Double = 0.0,
        var maxQuantity: Double = Double.MAX_VALUE,
        var isEnergyAdjustable: Boolean = true
)

/** Modes de contrainte pour un aliment */
enum class ConstraintMode {
        ADJUSTABLE,
        FIXED,
        EXCLUDED
}

/** Contrainte appliquée lors du calcul */
data class AlimentConstraint(
        val alimentUuid: String,
        val mode: ConstraintMode,
        val weight: Double,
        val minQuantity: Double,
        val maxQuantity: Double
)

/** Paramètres de l'algorithme d'ajustement */
data class AdjustmentParams(
        val toleranceGrams: Double = 0.1,
        val roundTo: Double = 0.1
)

/** La cellulose est ajustée à un objectif clinique de cinq fois le besoin de référence. */
internal fun adjustmentNeedMultiplier(nutrient: Nutrient): Double =
        if (nutrient == NutrientMain.CELLULOSE) 5.0 else 1.0

/** Résultat d'un ajustement de ration */
data class RationAdjustmentResult(
        val success: Boolean,
        val message: String,
        val adjustedAliments: List<AlimentRation>? = null
)

/** Adapte le résultat du solveur par contraintes vers le type affiché par la prévisualisation existante. */
internal fun ConstraintAdjustmentResult.toRationAdjustmentResult(): RationAdjustmentResult {
        val detailedMessage =
                if (!success && violatedConstraints.isNotEmpty()) {
                        val bullets =
                                violatedConstraints.joinToString("\n") {
                                        "• ${it.refLevel.label} ${it.nutrient.label}"
                                }
                        "$message\n$bullets"
                } else {
                        message
                }
        return RationAdjustmentResult(
                success = success,
                message = detailedMessage,
                adjustedAliments = adjustedAliments
        )
}

/**
 * Récupère la valeur CAP minimale depuis la référence nutritionnelle
 * @param referenceUtilisee La référence nutritionnelle
 * @return La valeur CAP OPTIMIN si disponible, sinon CAP MIN, sinon 1.0 par défaut
 */
private fun obtenirCapMinDepuisReference(referenceUtilisee: ReferenceEv): Double {
        // Essayer d'abord CAP OPTIMIN (priorité maximale)
        val capOptiminRef = referenceUtilisee.obtenirNutrimentRef(NutrientAnalysis.PCa, Reflevel.OPTIMIN)
        if (capOptiminRef != null && capOptiminRef.quantite > 0) {
                return capOptiminRef.quantite.toDouble()
        }

        // Si OPTIMIN n'existe pas, essayer CAP MIN
        val capMinRef = referenceUtilisee.obtenirNutrimentRef(NutrientAnalysis.PCa, Reflevel.MIN)
        if (capMinRef != null && capMinRef.quantite > 0) {
                return capMinRef.quantite.toDouble()
        }

        // Valeur par défaut si aucune référence n'est trouvée
        return 1.0
}

/**
 * Suggère automatiquement le nutriment cible à ajuster pour un aliment, en adaptant la logique
 * fournie. Les comparaisons se font sur matière sèche (MS): 100 * valeur / (100 - HUMIDITE).
 *
 * @param alimentRation L'aliment ration pour lequel suggérer un nutriment cible
 * @param referenceUtilisee La référence utilisée pour vérifier la disponibilité des nutriments
 * @return Le label du nutriment suggéré ou null si aucun n'est disponible
 */
fun suggestDefaultTargetNutrient(
        alimentRation: AlimentRation,
        referenceUtilisee: ReferenceEv
): String? {
        val aliment = alimentRation.aliment ?: return null
        val kind: FoodKind? = aliment.typeAliment

        fun n(nutrient: Nutrient): Double {
                return aliment.valMap[nutrient]?.value ?: 0.0
        }

        val humidite: Double = n(NutrientMain.HUMIDITE).coerceIn(0.0, 99.9)
        val msDenom: Double = (100.0 - humidite).takeIf { it > 0.001 } ?: 100.0
        fun toMsPercent(value: Double): Double = (100.0 * value) / msDenom

        val calciumMs: Double = toMsPercent(n(NutrientMacro.CAL))
        val proteineMs: Double = toMsPercent(n(NutrientMain.PROTEINE))
        val lipideMs: Double = toMsPercent(n(NutrientMain.LIPIDE))
        val enaMs: Double = toMsPercent(n(NutrientMain.ENA))
        val celluloseMs: Double = toMsPercent(n(NutrientMain.CELLULOSE))
        val sodiumMs: Double = toMsPercent(n(NutrientMacro.NA))
        val o6Ms: Double = toMsPercent(n(NutrientLipid.O6))
        val epaDha: Double = n(NutrientLipid.EPADHA)

        // Adaptation des cibles: chaînes compatibles avec nos labels de nutriments pour
        // pré-sélection
        // targetAdjust.CALCIUMPHOS → favoriser le Calcium (CAL) en pratique
        // targetAdjust.PROT → PROTEINE
        // targetAdjust.O6 → O6
        // targetAdjust.EPA → EPADHA
        // targetAdjust.LIP → LIPIDE
        // targetAdjust.FIBER → CELLULOSE
        // L'énergie est maintenant proposée mais traitée en dernier dans l'ordre de traitement

        // Fonction helper pour vérifier si un nutriment a des références disponibles
        fun hasReferenceForNutrient(nutrient: Nutrient): Boolean {
                return Reflevel.entries.any { refLevel ->
                        referenceUtilisee.obtenirNutrimentRef(nutrient, refLevel) != null
                }
        }

        return when {
                // Un aliment très concentré en sodium (notamment le sel) doit être ajusté sur NA,
                // jamais utilisé comme source d'énergie.
                (sodiumMs > 20.0 && hasReferenceForNutrient(NutrientMacro.NA)) ->
                        NutrientMacro.NA.label
                // Si aliment complet → par défaut ÉNERGIE (ajustement énergétique par défaut)
                kind == FoodKind.COMPLET -> NutrientMain.ENERGIE.label
                // Source minérale réellement concentrée en calcium (>= 5 % de MS).
                // Une teneur élevée en cendres seule est fréquente pour les légumes humides
                // en conserve et ne doit pas les transformer en ajusteurs de calcium.
                (calciumMs >= 5.0 &&
                        hasReferenceForNutrient(NutrientMacro.CAL)) -> NutrientMacro.CAL.label
                // Protéines/MS > 30
                (proteineMs > 30.0 && hasReferenceForNutrient(NutrientMain.PROTEINE)) ->
                        NutrientMain.PROTEINE.label
                // Lipides/MS > 30 et O6/MS > 15
                (lipideMs > 30.0 && o6Ms > 15.0 && hasReferenceForNutrient(NutrientLipid.O6)) ->
                        NutrientLipid.O6.label
                // Lipides/MS > 30 et EPADHA > 0
                (lipideMs > 30.0 &&
                        epaDha > 0.0 &&
                        hasReferenceForNutrient(NutrientLipid.EPADHA)) -> NutrientLipid.EPADHA.label
                // Lipides/MS > 40
                (lipideMs > 40.0 && hasReferenceForNutrient(NutrientMain.LIPIDE)) ->
                        NutrientMain.LIPIDE.label
                // Cellulose/MS > 20
                (celluloseMs > 20.0 && hasReferenceForNutrient(NutrientMain.CELLULOSE)) ->
                        NutrientMain.CELLULOSE.label
                // ENA/MS > 50 → énergie/carbs
                (enaMs > 50.0 && hasReferenceForNutrient(NutrientMain.PROTEINE)) ->
                        NutrientMain.ENERGIE.label
                // Pour tous les autres cas (aliments inclassables) → ÉNERGIE par défaut
                // L'énergie est toujours disponible, pas besoin de vérifier les références
                else -> NutrientMain.ENERGIE.label
        }
}

/** Calcule l'ajustement multi-nutriments de la ration */
suspend fun calculerAjustement(
        ration: Ration,
        adjustmentData: List<AlimentAdjustmentData>,
        referenceUtilisee: ReferenceEv,
        besoinEnergetiqueTotal: Double,
        besoinEnergetiqueStandard: Double,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        equationRepository: EquationSource?,
        pourcentageEnergieAlimentsComplets: Double? = null
): RationAdjustmentResult {
        try {
                // Créer une copie des aliments pour les ajustements
                val adjustedAliments = ration.alimentMutableList.map { it.copy() }.toMutableList()

                // Étape 1: Mettre tous les aliments à 0 (sauf ceux verrouillés)
                val alimentsVerrouilles = mutableSetOf<String>()

                for (i in adjustedAliments.indices) {
                        val alimentUuid = adjustedAliments[i].uuid
                        val estVerrouille =
                                adjustmentData.any {
                                        it.alimentRation.uuid == alimentUuid && it.isLocked
                                }

                        if (estVerrouille) {
                                alimentsVerrouilles.add(alimentUuid)
                        } else {
                                adjustedAliments[i] = adjustedAliments[i].copy(quantite = 0.0)
                        }
                }

                // Fixer la contribution des aliments complets avant les autres ajustements.
                if (pourcentageEnergieAlimentsComplets != null) {
                        require(pourcentageEnergieAlimentsComplets.isFinite() &&
                                pourcentageEnergieAlimentsComplets in 0.0..100.0) {
                                "Le pourcentage d'énergie des aliments complets doit être compris entre 0 et 100."
                        }
                        val cible = pourcentageEnergieAlimentsComplets / 100.0 * besoinEnergetiqueTotal
                        var apportVerrouille = 0.0
                        val disponibles = mutableListOf<Pair<Int, Double>>()
                        for (i in adjustedAliments.indices) {
                                val ar = adjustedAliments[i]
                                if (ar.aliment?.typeAliment != FoodKind.COMPLET) continue
                                if (ar.uuid in alimentsVerrouilles) {
                                        apportVerrouille += ar.getEnergie(referenceUtilisee, equationRepository)
                                } else {
                                        val densite = ar.copy(quantite = 100.0).getEnergie(referenceUtilisee, equationRepository) / 100.0
                                        if (densite > 0.0) disponibles.add(i to densite)
                                }
                        }
                        val reste = cible - apportVerrouille
                        require(reste >= -1e-6) {
                                "Les aliments complets verrouillés dépassent la part d'énergie demandée."
                        }
                        require(reste <= 1e-6 || disponibles.isNotEmpty()) {
                                "Aucun aliment complet ajustable ne permet d'atteindre la part d'énergie demandée."
                        }
                        for ((i, densite) in disponibles) {
                                adjustedAliments[i] = adjustedAliments[i].copy(
                                        quantite = reste.coerceAtLeast(0.0) / disponibles.size / densite
                                )
                        }
                }
                // Les étapes suivantes ne doivent plus modifier les aliments complets.
                val remainingAdjustmentData = adjustmentData.filter {
                        pourcentageEnergieAlimentsComplets == null ||
                                it.alimentRation.aliment?.typeAliment != FoodKind.COMPLET
                }

                // Étape 2: Traiter les nutriments sélectionnés par l'utilisateur, avec ordre
                // dynamique
                val nutrimentsTraites = mutableSetOf<String>()
                val processingOrder = buildProcessingOrderFromSelections(remainingAdjustmentData)

                remainingAdjustmentData.forEach { data -> }

                // PREMIÈRE ÉTAPE : Ajuster tous les nutriments sauf l'énergie
                val nutrimentsNonEnergetiques = processingOrder.filter { it != "ENERGIE" }
                for (nutrientLabel in nutrimentsNonEnergetiques) {
                        val nutrient = findNutrientByLabel(nutrientLabel)
                        if (nutrient == null) {

                                continue
                        }

                        // Calculer le besoin absolu en fonction du nutriment
                        val rl = Reflevel.OPTIMIN
                        val nutrimentRef = referenceUtilisee.obtenirNutrimentRef(nutrient, rl)
                        if (nutrimentRef == null || nutrimentRef.quantite <= 0) {

                                continue
                        }

                        val besoinReferenceGrammes =
                                calculerBesoinAbsoluGrammes(
                                        nutrimentRef = nutrimentRef,
                                        poidsAnimal = poidsAnimal,
                                        poidsMetabolique = poidsMetabolique,
                                        besoinEnergetiqueReference = besoinEnergetiqueStandard
                                )
                        val besoinAbsoluGrammes =
                                besoinReferenceGrammes * adjustmentNeedMultiplier(nutrient)

                        if (besoinAbsoluGrammes <= 0) {

                                continue
                        }

                        // Trouver les aliments ajustables pour ce nutriment
                        val constraintByUuid = emptyMap<String, AlimentConstraint>()
                        val alimentsAjustables =
                                remainingAdjustmentData.filter {
                                        it.selectedNutrient == nutrientLabel && !it.isLocked
                                }

                        if (alimentsAjustables.isEmpty()) {

                                continue
                        }

                        // Calculer l'apport actuel pour ce nutriment
                        var apportActuel = 0.0
                        for (i in adjustedAliments.indices) {
                                val alimentRation = adjustedAliments[i]
                                val aliment = alimentRation.aliment ?: continue
                                val quantiteAliment: Double = alimentRation.quantite.toDouble()
                                
                                // Pour l'énergie, utiliser getEnergie() qui sélectionne correctement equationDEcom ou equationDEraw
                                val apportNutriment: Double = if (nutrient == NutrientMain.ENERGIE) {
                                        alimentRation.getEnergie(referenceUtilisee, equationRepository)
                                } else {
                                        val quantiteNutriment: Double =
                                                (aliment.valMap?.get(nutrient)?.value ?: 0.0).toDouble()
                                        (quantiteNutriment * quantiteAliment) / 100.0
                                }
                                apportActuel += apportNutriment
                        }

                        val manque = besoinAbsoluGrammes - apportActuel

                        if (manque > 0.01) {

                                val result =
                                        ajusterAlimentsPourNutriment(
                                                nutriment = nutrient,
                                                manque = manque,
                                                alimentsAjustables = alimentsAjustables,
                                                adjustedAliments = adjustedAliments,
                                                alimentsVerrouilles = alimentsVerrouilles,
                                                constraints = constraintByUuid,
                                                referenceUtilisee = referenceUtilisee,
                                                equationRepository = equationRepository,
                                                roundQuantities = pourcentageEnergieAlimentsComplets == null
                                        )

                                if (!result.success) {

                                        return result
                                } else {}
                        } else {}

                        nutrimentsTraites.add(nutrientLabel)
                }

                // DEUXIÈME ÉTAPE : Ajuster l'énergie en recalculant l'apport total de la ration
                // finale
                if (processingOrder.contains("ENERGIE") || pourcentageEnergieAlimentsComplets != null) {

                        // Créer une ration temporaire avec les ajustements effectués
                        val rationTemp =
                                ration.copy(
                                        alimentMutableList =
                                                adjustedAliments.map { it.copy() }.toMutableList()
                                )
                        for (i in adjustedAliments.indices) {
                                val alimentRation = adjustedAliments[i]
                                val alimentRationTemp =
                                        rationTemp.alimentMutableList.find {
                                                it.uuid == alimentRation.uuid
                                        }
                                if (alimentRationTemp != null) {
                                        // Créer une nouvelle instance avec la quantité modifiée
                                        val index =
                                                rationTemp.alimentMutableList.indexOf(
                                                        alimentRationTemp
                                                )
                                        if (index >= 0) {
                                                rationTemp.alimentMutableList[index] =
                                                        alimentRationTemp.copy(
                                                                quantite = alimentRation.quantite
                                                        )
                                        }
                                }
                        }

                        // Calculer l'apport énergétique total de la ration finale
                        var apportEnergetiqueTotal = 0.0
                        for (alimentRation in rationTemp.alimentMutableList) {
                                if (alimentRation.quantite > 0.01) {
                                        // Utiliser la même logique que le système principal pour
                                        // assurer la cohérence
                                        val energieAliment =
                                                alimentRation.getEnergie(
                                                        referenceUtilisee,
                                                        equationRepository
                                                )
                                        apportEnergetiqueTotal += energieAliment
                                }
                        }

                        val manqueEnergie = besoinEnergetiqueTotal - apportEnergetiqueTotal

                        if (manqueEnergie > 0.01) {
                                // PRIORITÉ : Utiliser d'abord les aliments qui ont l'énergie comme
                                // nutriment
                                // principal
                                val constraintByUuid = emptyMap<String, AlimentConstraint>()

                                // 1. Aliments avec l'énergie comme nutriment principal (priorité
                                // maximale)
                                val alimentsEnergiePrincipale =
                                        remainingAdjustmentData.filter {
                                                it.selectedNutrient == "ENERGIE" && !it.isLocked
                                        }

                                // 2. Aliments avec ajustement énergétique secondaire (priorité
                                // secondaire)
                                // IMPORTANT : On n'ajuste les autres aliments pour l'énergie QUE si
                                // aucun aliment n'a l'énergie comme nutriment principal
                                val alimentsEnergieSecondaire =
                                        if (alimentsEnergiePrincipale.isNotEmpty()) {
                                                // Si on a des aliments avec l'énergie comme
                                                // nutriment principal,
                                                // on n'ajuste PAS les autres aliments pour
                                                // l'énergie
                                                emptyList()
                                        } else {
                                                // Seulement si aucun aliment n'a l'énergie comme
                                                // nutriment principal
                                                remainingAdjustmentData.filter {
                                                        it.selectedNutrient !=
                                                                "ENERGIE" && // Pas déjà traité
                                                                // comme nutriment
                                                                // principal
                                                                it.isEnergyAdjustable &&
                                                                !it.isLocked
                                                }
                                        }

                                // Combiner les deux listes avec priorité aux aliments énergie
                                // principale
                                val alimentsAjustablesEnergie =
                                        alimentsEnergiePrincipale + alimentsEnergieSecondaire

                                val result =
                                        ajusterAlimentsPourNutriment(
                                                nutriment = NutrientMain.ENERGIE,
                                                manque = manqueEnergie,
                                                alimentsAjustables = alimentsAjustablesEnergie,
                                                adjustedAliments = adjustedAliments,
                                                alimentsVerrouilles = alimentsVerrouilles,
                                                constraints = constraintByUuid,
                                                referenceUtilisee = referenceUtilisee,
                                                equationRepository = equationRepository,
                                                roundQuantities = pourcentageEnergieAlimentsComplets == null
                                        )

                                if (result.success) {

                                        nutrimentsTraites.add("ENERGIE")
                                } else {

                                        return RationAdjustmentResult(
                                                success = false,
                                                message =
                                                        translate(LocalizationKeys.AnalNut.ADJUST_FAIL) + ": ${result.message}",
                                                adjustedAliments = null
                                        )
                                }
                        } else {

                                nutrimentsTraites.add("ENERGIE")
                        }
                }

                // Pas de rééquilibrage énergétique final : l'énergie est ajustée dans la boucle
                // principale, comme les autres nutriments.

                // TROISIÈME ÉTAPE : Ajustement final du ratio CAP après tous les ajustements
                val calciumSelectionne: Boolean = remainingAdjustmentData.any { it.selectedNutrient == NutrientMacro.CAL.label && !it.isLocked }

                if (calciumSelectionne) {
                        val capMinRequis: Double = obtenirCapMinDepuisReference(referenceUtilisee)
                        val alimentsCalcium = remainingAdjustmentData.filter {
                                it.selectedNutrient == NutrientMacro.CAL.label && !it.isLocked 
                        }
                        
                        if (alimentsCalcium.isNotEmpty()) {
                                // Ajustement itératif pour respecter le ratio CAP
                                var iterations = 0
                                val maxIterations = 10
                                
                        while (iterations < maxIterations) {
                                // Utiliser la même logique que le système pour calculer les totaux
                                var totalCalcium: Double = 0.0
                                var totalPhosphore: Double = 0.0
                                
                                // Recalculer les totaux avec les quantités actuelles (même logique que calculerQuantiteTotaleNutriment)
                                // Utiliser getNutrientWithComplementary pour être cohérent avec le reste du système
                                for (i in adjustedAliments.indices) {
                                        val alimentRation = adjustedAliments[i]
                                        val quantite = alimentRation.quantite.toDouble()
                                        
                                        // Calcium - utiliser getNutrientWithComplementary pour être cohérent
                                        val calPar100g = alimentRation.getNutrientWithComplementary(
                                                nutrient = NutrientMacro.CAL,
                                                equationRepository = equationRepository,
                                                referenceEv = referenceUtilisee
                                        )
                                        if (calPar100g != null) {
                                                totalCalcium += (calPar100g * quantite) / 100.0
                                        }
                                        
                                        // Phosphore - utiliser getNutrientWithComplementary pour être cohérent
                                        val pPar100g = alimentRation.getNutrientWithComplementary(
                                                nutrient = NutrientMacro.PHOS,
                                                equationRepository = equationRepository,
                                                referenceEv = referenceUtilisee
                                        )
                                        if (pPar100g != null) {
                                                totalPhosphore += (pPar100g * quantite) / 100.0
                                        }
                                }
                                        
                                        if (totalPhosphore > 0.0) {
                                                val ratioActuel: Double = totalCalcium / totalPhosphore
                                                
                                                // Vérifier si le ratio est suffisant (avec une petite tolérance)
                                                if (ratioActuel >= capMinRequis ) {
                                                        break
                                                }
                                                
                                                // Calculer le facteur d'ajustement nécessaire
                                                val facteurAjustement: Double = capMinRequis / ratioActuel
                                                
                                                // Ajuster les aliments calcium proportionnellement
                                                var ajustementEffectue = false
                                                for (alimentData in alimentsCalcium) {
                                                        val index = adjustedAliments.indexOfFirst { 
                                                                it.uuid == alimentData.alimentRation.uuid 
                                                        }
                                                        if (index >= 0) {
                                                                val alimentRation = adjustedAliments[index]
                                                                val quantiteActuelle: Double = alimentRation.quantite.toDouble()
                                                                // Utiliser getNutrientWithComplementary pour être cohérent
                                                                val PhosAct: Double = alimentRation.getNutrientWithComplementary(
                                                                        nutrient = NutrientMacro.PHOS,
                                                                        equationRepository = equationRepository,
                                                                        referenceEv = referenceUtilisee
                                                                )?.let { (it * quantiteActuelle) / 100.0 } ?: 0.0
                                                                val CalAct: Double = alimentRation.getNutrientWithComplementary(
                                                                        nutrient = NutrientMacro.CAL,
                                                                        equationRepository = equationRepository,
                                                                        referenceEv = referenceUtilisee
                                                                )?.let { (it * quantiteActuelle) / 100.0 } ?: 0.0
                                                                val calPar100g = alimentRation.getNutrientWithComplementary(
                                                                        nutrient = NutrientMacro.CAL,
                                                                        equationRepository = equationRepository,
                                                                        referenceEv = referenceUtilisee
                                                                ) ?: 0.0
                                                                val pPar100g = alimentRation.getNutrientWithComplementary(
                                                                        nutrient = NutrientMacro.PHOS,
                                                                        equationRepository = equationRepository,
                                                                        referenceEv = referenceUtilisee
                                                                ) ?: 0.0
                                                                val nouvelleQuantite: Double = 1.2 * (capMinRequis * (totalPhosphore - PhosAct) - (totalCalcium - CalAct)) / ((calPar100g - capMinRequis * pPar100g) / 100.0)
                                                                // Arrondir au centième de gramme pour éviter les erreurs de précision
                                                                val quantiteArrondie: Double = kotlin.math.round(nouvelleQuantite * 100.0) / 100.0
                                                                adjustedAliments[index] = adjustedAliments[index].copy(
                                                                        quantite = quantiteArrondie.coerceAtLeast(0.0)
                                                                )
                                                                ajustementEffectue = true
                                                        }
                                                }
                                                
                                                if (!ajustementEffectue) {
                                                        break
                                                }
                                        } else {
                                                break
                                        }
                                        
                                        iterations++
                                }
                                
                                if (iterations >= maxIterations) {
                                        // Maximum d'itérations atteint pour l'ajustement CAP
                                }
                        }
                        
                }

                if (pourcentageEnergieAlimentsComplets != null) {
                        var energieTotale = 0.0
                        for (ar in adjustedAliments) {
                                energieTotale += ar.getEnergie(referenceUtilisee, equationRepository)
                        }
                        require(kotlin.math.abs(energieTotale - besoinEnergetiqueTotal) <= 0.01) {
                                "Impossible de respecter le besoin énergétique avec cette répartition et ces ajustements. Essayez l'ajustement par contraintes."
                        }
                }

                // Arrondi final selon règles métier
                for (i in adjustedAliments.indices) {
                        if (pourcentageEnergieAlimentsComplets != null || adjustedAliments[i].uuid in alimentsVerrouilles) continue
                        val rounded: Double = arrondirQuantiteSelonRegles(adjustedAliments[i], adjustedAliments[i].quantite.toDouble())
                        adjustedAliments[i] = adjustedAliments[i].copy(quantite = rounded)
                }

                return RationAdjustmentResult(
                        success = true,
                        message =
                                translate("analnut.success.sequential_adjust", nutrimentsTraites.size.toString()),
                        adjustedAliments = adjustedAliments
                )
        } catch (e: Exception) {
                e.rethrowIfCancellation()
                return RationAdjustmentResult(
                        success = false,
                        message = translate("analnut.error.sequential_adjust", e.message ?: "")
                )
        }
}

/**
 * Calcule le besoin absolu en grammes pour un nutriment donné.
 * Délègue vers [fr.vetbrain.vetnutri_mp.Data.computeAbsoluteGramNeed], extrait pour être partagé
 * avec le solveur d'ajustement par contraintes.
 */
private fun calculerBesoinAbsoluGrammes(
        nutrimentRef: ReferenceEv.Nut4Ref,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        besoinEnergetiqueReference: Double
): Double =
        fr.vetbrain.vetnutri_mp.Data.computeAbsoluteGramNeed(
                nutrimentRef,
                poidsAnimal,
                poidsMetabolique,
                besoinEnergetiqueReference
        )

/** Obtient l'ordre de traitement des nutriments */
private fun buildProcessingOrderFromSelections(
        adjustmentData: List<AlimentAdjustmentData>
): List<String> {
        val selected = adjustmentData.mapNotNull { it.selectedNutrient }.toSet()
        if (selected.isEmpty()) return emptyList()

        val energyLabel = NutrientMain.ENERGIE.label
        // L'énergie doit être traitée si elle est sélectionnée OU si au moins un aliment a
        // l'énergie
        // cochée
        val hasEnergy =
                selected.contains(energyLabel) || adjustmentData.any { it.isEnergyAdjustable }

        fun isOf(label: String, group: Array<out Nutrient>): Boolean {
                return group.any { it.label == label }
        }

        val nonEnergy = selected.filter { it != energyLabel }

        val order = mutableListOf<String>()

        // Priorité: Main (sans l'énergie) > Macro > Minéraux > Vitamines > Lipides > AA > Autres,
        // trié
        // par label
        order +=
                nonEnergy
                        .filter { isOf(it, NutrientMain.values()) && it != energyLabel }
                        .sorted() // 1er - Base de la ration
        order += nonEnergy.filter { isOf(it, NutrientMacro.values()) }.sorted() // 2ème
        order += nonEnergy.filter { isOf(it, NutrientMin.values()) }.sorted() // 3ème
        order += nonEnergy.filter { isOf(it, NutrientVitam.values()) }.sorted() // 4ème
        order += nonEnergy.filter { isOf(it, NutrientLipid.values()) }.sorted() // 5ème
        order += nonEnergy.filter { isOf(it, AAEnum.values()) }.sorted() // 6ème
        order += nonEnergy.filter { isOf(it, NutrientOther.values()) }.sorted() // 7ème

        if (hasEnergy) order += energyLabel // Toujours en dernier

        // Dédupliquer en conservant l'ordre
        return order.distinct()
}

private fun buildProcessingOrderFromSelectionLabels(labels: List<String>): List<String> {
        val selected = labels.toSet()
        if (selected.isEmpty()) return emptyList()

        val energyLabel = NutrientMain.ENERGIE.label
        val hasEnergy = selected.contains(energyLabel)

        fun isOf(label: String, group: Array<out Nutrient>): Boolean {
                return group.any { it.label == label }
        }

        val nonEnergy = selected.filter { it != energyLabel }

        val order = mutableListOf<String>()

        order +=
                nonEnergy
                        .filter { isOf(it, NutrientMain.values()) && it != energyLabel }
                        .sorted() // 1er - Base de la ration
        order += nonEnergy.filter { isOf(it, NutrientMacro.values()) }.sorted() // 2ème
        order += nonEnergy.filter { isOf(it, NutrientMin.values()) }.sorted() // 3ème
        order += nonEnergy.filter { isOf(it, NutrientVitam.values()) }.sorted() // 4ème
        order += nonEnergy.filter { isOf(it, NutrientLipid.values()) }.sorted() // 5ème
        order += nonEnergy.filter { isOf(it, AAEnum.values()) }.sorted() // 6ème
        order += nonEnergy.filter { isOf(it, NutrientOther.values()) }.sorted() // 7ème

        if (hasEnergy) order += energyLabel // Toujours en dernier
        return order.distinct()
}

/** Trouve un nutriment par son label */
private fun findNutrientByLabel(label: String): Nutrient? {
        return when {
                NutrientMain.entries.any { it.label == label } ->
                        NutrientMain.entries.find { it.label == label }
                NutrientLipid.entries.any { it.label == label } ->
                        NutrientLipid.entries.find { it.label == label }
                NutrientVitam.entries.any { it.label == label } ->
                        NutrientVitam.entries.find { it.label == label }
                NutrientMacro.entries.any { it.label == label } ->
                        NutrientMacro.entries.find { it.label == label }
                NutrientMin.entries.any { it.label == label } ->
                        NutrientMin.entries.find { it.label == label }
                NutrientOther.entries.any { it.label == label } ->
                        NutrientOther.entries.find { it.label == label }
                AAEnum.entries.any { it.label == label } ->
                        AAEnum.entries.find { it.label == label }
                else -> null
        }
}

/** Vérifie quels niveaux de référence sont disponibles pour un nutriment donné */
private fun getAvailableRefLevelsForNutrient(
        nutrient: Nutrient,
        referenceUtilisee: ReferenceEv
): List<Reflevel> {
        return Reflevel.entries.filter { refLevel ->
                referenceUtilisee.obtenirNutrimentRef(nutrient, refLevel) != null
        }
}

/** Ajuste la ration pour un nutriment spécifique */
private fun adjustRationForNutrient(
        alimentsAjustables: List<AlimentAdjustmentData>,
        adjustedAliments: MutableList<AlimentRation>,
        nutrient: Nutrient,
        besoinAbsoluGrammes: Double,
        referenceUtilisee: ReferenceEv
): RationAdjustmentResult {
        try {
                // Calculer l'apport actuel du nutriment
                var apportActuel = 0.0
                for (alimentData in alimentsAjustables) {
                        val aliment = alimentData.alimentRation.aliment ?: continue
                        val quantiteNutriment: Double =
                                (aliment.valMap?.get(nutrient)?.value ?: 0.0).toDouble()
                        val quantiteAliment: Double = alimentData.alimentRation.quantite.toDouble()
                        apportActuel += (quantiteNutriment * quantiteAliment) / 100.0
                }

                if (apportActuel <= 0) {
                        return RationAdjustmentResult(
                                success = false,
                                message = translate(LocalizationKeys.AnalNut.NO_INTAKE_DETECTED, nutrient.label)
                        )
                }

                // Calculer le ratio d'ajustement
                val ratio = besoinAbsoluGrammes / apportActuel

                // Appliquer l'ajustement aux aliments concernés
                for (alimentData in alimentsAjustables) {
                        val index =
                                adjustedAliments.indexOfFirst {
                                        it.uuid == alimentData.alimentRation.uuid
                                }
                        if (index >= 0) {
                                val quantiteActuelle: Double =
                                        alimentData.alimentRation.quantite.toDouble()
                                val nouvelleQuantite: Double = quantiteActuelle * ratio
                                // Arrondir selon règles
                                val nouvelleQuantiteArrondie: Double =
                                        arrondirQuantiteSelonRegles(adjustedAliments[index], nouvelleQuantite)
                                adjustedAliments[index] =
                                        adjustedAliments[index].copy(
                                                quantite = nouvelleQuantiteArrondie
                                        )
                        }
                }

                return RationAdjustmentResult(
                        success = true,
                        message = translate(LocalizationKeys.AnalNut.ADJUST_NUTRIENT_SUCCESS, nutrient.label)
                )
        } catch (e: Exception) {
                e.rethrowIfCancellation()
                return RationAdjustmentResult(
                        success = false,
                        message = translate(LocalizationKeys.AnalNut.ADJUST_FAIL) + ": ${e.message}"
                )
        }
}

/** Ajuste les aliments pour couvrir le manque d'un nutriment spécifique */
private suspend fun ajusterAlimentsPourNutriment(
        nutriment: Nutrient,
        manque: Double,
        alimentsAjustables: List<AlimentAdjustmentData>,
        adjustedAliments: MutableList<AlimentRation>,
        alimentsVerrouilles: Set<String>,
        constraints: Map<String, AlimentConstraint> = emptyMap(),
        referenceUtilisee: ReferenceEv? = null,
        equationRepository: EquationSource? = null,
        roundQuantities: Boolean = true
): RationAdjustmentResult {
        try {
                // Filtrer les aliments disponibles pour l'ajustement
                val alimentsDisponibles =
                        alimentsAjustables.filter { data ->
                                if (alimentsVerrouilles.contains(data.alimentRation.uuid))
                                        return@filter false
                                val aliment = data.alimentRation.aliment ?: return@filter false
                                if (nutriment == NutrientMain.ENERGIE) {
                                        // Utiliser getEnergie() qui utilise EquationEvaluator.calculerEnergiePour100g()
                                        // pour sélectionner correctement equationDEcom ou equationDEraw
                                        val alimentRationTemp = AlimentRation(
                                                aliment = data.alimentRation.aliment,
                                                quantite = 100.0,
                                                weight = 1.0
                                        )
                                        val energiePour100g = alimentRationTemp.getEnergie(
                                                referenceUtilisee,
                                                equationRepository
                                        )
                                        val densite: Double = energiePour100g / 100.0
                                        densite > 0.0
                                } else {
                                        (aliment.valMap[nutriment]?.value ?: 0.0) > 0.0
                                }
                        }

                if (alimentsDisponibles.isEmpty()) {
                        return RationAdjustmentResult(
                                success = false,
                                message =
                                        translate(LocalizationKeys.AnalNut.NO_FOOD_FOR_NUTRIENT, nutriment.label)
                        )
                }

                // Calculer la contribution potentielle de chaque aliment
                val contributions = mutableListOf<Triple<AlimentAdjustmentData, Double, Int>>()

                for (alimentData in alimentsDisponibles) {
                        val index =
                                adjustedAliments.indexOfFirst {
                                        it.uuid == alimentData.alimentRation.uuid
                                }
                        if (index < 0) continue
                        if (nutriment == NutrientMain.ENERGIE) {
                                // Utiliser getEnergie() qui utilise EquationEvaluator.calculerEnergiePour100g()
                                // pour sélectionner correctement equationDEcom ou equationDEraw
                                val alimentRationTemp = AlimentRation(
                                        aliment = alimentData.alimentRation.aliment,
                                        quantite = 100.0,
                                        weight = 1.0
                                )
                                val energiePour100g = alimentRationTemp.getEnergie(
                                        referenceUtilisee,
                                        equationRepository
                                )
                                val densite: Double = energiePour100g / 100.0
                                if (densite > 0.0) {
                                        val quantiteNecessaire = manque / densite
                                        contributions.add(
                                                Triple(alimentData, quantiteNecessaire, index)
                                        )
                                }
                        } else {
                                val aliment = alimentData.alimentRation.aliment ?: continue
                                val quantiteNutriment: Double =
                                        (aliment.valMap[nutriment]?.value ?: 0.0).toDouble()
                                if (quantiteNutriment > 0.0) {
                                        // Quantité (g) nécessaire pour couvrir le manque (g) à
                                        // partir de valeur/100g
                                        val quantiteNecessaire =
                                                (manque * 100.0) / quantiteNutriment
                                        contributions.add(
                                                Triple(alimentData, quantiteNecessaire, index)
                                        )
                                }
                        }
                }

                if (contributions.isEmpty()) {
                        return RationAdjustmentResult(
                                success = false,
                                message =
                                        translate(LocalizationKeys.AnalNut.NO_CONTRIBUTION_POSSIBLE, nutriment.label)
                        )
                }

                // Appliquer une pondération par poids (préférence d'usage) avant tri
                for (i in contributions.indices) {
                        val (data, need, idx) = contributions[i]
                        val w =
                                (constraints[data.alimentRation.uuid]?.weight ?: data.weight)
                                        .coerceIn(0.0, 1.0)
                        val adjNeed = if (w > 0.0) need / w else Double.POSITIVE_INFINITY
                        contributions[i] = Triple(data, adjNeed, idx)
                }
                // Trier par efficacité pondérée
                contributions.sortBy { it.second }

                // Répartir le manque entre les aliments les plus efficaces
                var manqueRestant = manque
                var indexContribution = 0
                var totalAjoute = 0.0

                while (manqueRestant > 0.01 &&
                        indexContribution < contributions.size) { // Tolérance
                        val (alimentData, quantiteNecessaire, index) =
                                contributions[indexContribution]
                        val quantiteAAjouter =
                                if (nutriment == NutrientMain.ENERGIE) {
                                        // Utiliser getEnergie() qui utilise EquationEvaluator.calculerEnergiePour100g()
                                        // pour sélectionner correctement equationDEcom ou equationDEraw
                                        val alimentRationTemp = AlimentRation(
                                                aliment = adjustedAliments[index].aliment,
                                                quantite = 100.0,
                                                weight = 1.0
                                        )
                                        val energiePour100g = alimentRationTemp.getEnergie(
                                                referenceUtilisee,
                                                equationRepository
                                        )
                                        val densite: Double = energiePour100g / 100.0
                                        if (densite <= 0.0) 0.0
                                        else if (indexContribution == contributions.size - 1) {
                                                manqueRestant / densite
                                        } else {
                                                val alimentsRestants =
                                                        contributions.size - indexContribution
                                                (manqueRestant / densite) / alimentsRestants
                                        }
                                } else {
                                        val aliment = alimentData.alimentRation.aliment
                                        val quantiteNutriment: Double =
                                                (aliment?.valMap?.get(nutriment)?.value ?: 0.0)
                                                        .toDouble()
                                        if (quantiteNutriment <= 0.0) 0.0
                                        else if (indexContribution == contributions.size - 1) {
                                                (manqueRestant * 100.0) / quantiteNutriment
                                        } else {
                                                val alimentsRestants =
                                                        contributions.size - indexContribution
                                                (manqueRestant * 100.0) /
                                                        (quantiteNutriment * alimentsRestants)
                                        }
                                }

                        if (quantiteAAjouter > 0) {
                                // Ajouter cette quantité à l'aliment en respectant min/max
                                val quantiteActuelle: Double = adjustedAliments[index].quantite
                                val minQ =
                                        alimentsAjustables
                                                .firstOrNull {
                                                        it.alimentRation.uuid ==
                                                                adjustedAliments[index].uuid
                                                }
                                                ?.minQuantity
                                                ?: 0.0
                                val maxQ =
                                        alimentsAjustables
                                                .firstOrNull {
                                                        it.alimentRation.uuid ==
                                                                adjustedAliments[index].uuid
                                                }
                                                ?.maxQuantity
                                                ?: Double.MAX_VALUE
                                val nouvelleQuantite: Double =
                                        (quantiteActuelle + quantiteAAjouter).coerceIn(minQ, maxQ)
                                // Arrondir selon règles
                                val nouvelleQuantiteArrondie: Double =
                                        if (roundQuantities) arrondirQuantiteSelonRegles(adjustedAliments[index], nouvelleQuantite) else nouvelleQuantite
                                adjustedAliments[index] =
                                        adjustedAliments[index].copy(
                                                quantite = nouvelleQuantiteArrondie
                                        )

                                // Mettre à jour le manque restant
                                val apportAjoute: Double =
                                        if (nutriment == NutrientMain.ENERGIE) {
                                                // Utiliser getEnergie() qui utilise EquationEvaluator.calculerEnergiePour100g()
                                                // pour sélectionner correctement equationDEcom ou equationDEraw
                                                val alimentRationTemp = AlimentRation(
                                                        aliment = adjustedAliments[index].aliment,
                                                        quantite = quantiteAAjouter,
                                                        weight = 1.0
                                                )
                                                alimentRationTemp.getEnergie(
                                                        referenceUtilisee,
                                                        equationRepository
                                                )
                                        } else {
                                                val aliment = alimentData.alimentRation.aliment
                                                val quantiteNutriment: Double =
                                                        (aliment?.valMap?.get(nutriment)?.value
                                                                        ?: 0.0).toDouble()
                                                (quantiteNutriment * quantiteAAjouter) / 100.0
                                        }
                                manqueRestant -= apportAjoute
                                totalAjoute += apportAjoute
                        }

                        indexContribution++
                }

                if (manqueRestant > 0.01) {
                        return RationAdjustmentResult(
                                success = false,
                                message =
                                        translate(LocalizationKeys.AnalNut.IMPOSSIBLE_COVERAGE, nutriment.label, TextUtils.formatDecimal(manqueRestant, 2), TextUtils.formatDecimal(totalAjoute, 2))
                        )
                }

                return RationAdjustmentResult(
                        success = true,
                        message =
                                translate(LocalizationKeys.AnalNut.ADJUST_NUTRIENT_ADDED, nutriment.label, TextUtils.formatDecimal(totalAjoute, 2))
                )
        } catch (e: Exception) {
                e.rethrowIfCancellation()
                return RationAdjustmentResult(
                        success = false,
                        message =
                                translate(LocalizationKeys.AnalNut.ADJUST_FAIL) + ": ${e.message}"
                )
        }
}

/** Ajuste la ration pour plusieurs nutriments de manière séquentielle */
private suspend fun adjustRationForMultipleNutrients(
        besoinsNutriments: Map<String, Double>,
        alimentsParNutriment: Map<String, List<AlimentAdjustmentData>>,
        adjustedAliments: MutableList<AlimentRation>,
        referenceUtilisee: ReferenceEv,
        equationRepository: EquationSource? = null
): RationAdjustmentResult {
        try {
                // Étape 1: Mettre tous les aliments à 0 (sauf ceux verrouillés)
                val alimentsVerrouilles = mutableSetOf<String>()

                for (i in adjustedAliments.indices) {
                        val alimentUuid = adjustedAliments[i].uuid
                        val estVerrouille =
                                alimentsParNutriment.values.flatten().any {
                                        it.alimentRation.uuid == alimentUuid && it.isLocked
                                }

                        if (estVerrouille) {
                                alimentsVerrouilles.add(alimentUuid)
                        } else {
                                adjustedAliments[i] = adjustedAliments[i].copy(quantite = 0.0)
                        }
                }

                // Étape 2: Traiter les nutriments selon les sélections utilisateur
                val nutrimentsTraites = mutableSetOf<String>()
                val processingOrder =
                        buildProcessingOrderFromSelectionLabels(alimentsParNutriment.keys.toList())

                for (nutrientLabel in processingOrder) {
                        val besoinAbsolu = besoinsNutriments[nutrientLabel] ?: continue
                        val alimentsAjustables = alimentsParNutriment[nutrientLabel] ?: continue
                        val nutrient = findNutrientByLabel(nutrientLabel) ?: continue

                        // Étape 3: Calculer l'apport actuel du nutriment (avec TOUS les aliments
                        // déjà ajustés)
                        var apportActuel = 0.0
                        for (i in adjustedAliments.indices) {
                                val alimentRation = adjustedAliments[i]
                                val aliment = alimentRation.aliment ?: continue
                                val quantiteAliment: Double = alimentRation.quantite.toDouble()
                                
                                // Pour l'énergie, utiliser getEnergie() qui sélectionne correctement equationDEcom ou equationDEraw
                                val apportNutriment: Double = if (nutrient == NutrientMain.ENERGIE) {
                                        alimentRation.getEnergie(referenceUtilisee, equationRepository)
                                } else {
                                        val quantiteNutriment: Double =
                                                (aliment.valMap?.get(nutrient)?.value ?: 0.0).toDouble()
                                        (quantiteNutriment * quantiteAliment) / 100.0
                                }
                                apportActuel += apportNutriment
                        }

                        // Étape 4: Calculer ce qui manque
                        val manque = besoinAbsolu - apportActuel

                        val unite = if (nutrientLabel == NutrientMain.ENERGIE.label) "kcal" else "g"

                        if (manque > 0.01) { // Tolérance de 0.01g
                                // Étape 5: Ajuster les aliments pour couvrir le manque

                                val result =
                                        ajusterAlimentsPourNutriment(
                                                nutriment = nutrient,
                                                manque = manque,
                                                alimentsAjustables = alimentsAjustables,
                                                adjustedAliments = adjustedAliments,
                                                alimentsVerrouilles = alimentsVerrouilles,
                                                constraints = emptyMap(),
                                                referenceUtilisee = referenceUtilisee,
                                                equationRepository = null
                                        )

                                if (!result.success) {

                                        return result
                                } else {}
                        } else {}

                        nutrimentsTraites.add(nutrientLabel)
                }

                // Vérification et ajustement CAP après tous les nutriments
                val calciumSelectionne: Boolean = alimentsParNutriment.keys.any { it == NutrientMacro.CAL.label }
                if (calciumSelectionne) {
                        val capMinRequis: Double = obtenirCapMinDepuisReference(referenceUtilisee)
                        val alimentsCalcium = alimentsParNutriment[NutrientMacro.CAL.label] ?: emptyList()
                        
                        if (alimentsCalcium.isNotEmpty()) {
                                // Ajustement itératif pour respecter le ratio CAP
                                var iterations = 0
                                val maxIterations = 10
                                
                        while (iterations < maxIterations) {
                                // Utiliser la même logique que le système pour calculer les totaux
                                var totalCalcium: Double = 0.0
                                var totalPhosphore: Double = 0.0
                                
                                // Recalculer les totaux avec les quantités actuelles (même logique que calculerQuantiteTotaleNutriment)
                                // Utiliser getNutrientWithComplementary pour être cohérent avec le reste du système
                                for (i in adjustedAliments.indices) {
                                        val alimentRation = adjustedAliments[i]
                                        val quantite = alimentRation.quantite.toDouble()
                                        
                                        // Calcium - utiliser getNutrientWithComplementary pour être cohérent
                                        val calPar100g = alimentRation.getNutrientWithComplementary(
                                                nutrient = NutrientMacro.CAL,
                                                equationRepository = equationRepository,
                                                referenceEv = referenceUtilisee
                                        )
                                        if (calPar100g != null) {
                                                totalCalcium += (calPar100g * quantite) / 100.0
                                        }
                                        
                                        // Phosphore - utiliser getNutrientWithComplementary pour être cohérent
                                        val pPar100g = alimentRation.getNutrientWithComplementary(
                                                nutrient = NutrientMacro.PHOS,
                                                equationRepository = equationRepository,
                                                referenceEv = referenceUtilisee
                                        )
                                        if (pPar100g != null) {
                                                totalPhosphore += (pPar100g * quantite) / 100.0
                                        }
                                }
                                        
                                        if (totalPhosphore > 0.0) {
                                                val ratioActuel: Double = totalCalcium / totalPhosphore
                                                
                                                // Vérifier si le ratio est suffisant
                                                if (ratioActuel >= capMinRequis) {
                                                        break
                                                }
                                                
                                                // Calculer le facteur d'ajustement nécessaire
                                                val facteurAjustement: Double = capMinRequis / ratioActuel
                                                
                                                // Ajuster les aliments calcium proportionnellement
                                                var ajustementEffectue = false
                                                for (alimentData in alimentsCalcium) {
                                                        val index = adjustedAliments.indexOfFirst { 
                                                                it.uuid == alimentData.alimentRation.uuid 
                                                        }
                                                        if (index >= 0) {
                                                                val alimentRation = adjustedAliments[index]
                                                                val quantiteActuelle: Double = alimentRation.quantite.toDouble()
                                                                // Utiliser getNutrientWithComplementary pour être cohérent
                                                                val PhosAct: Double = alimentRation.getNutrientWithComplementary(
                                                                        nutrient = NutrientMacro.PHOS,
                                                                        equationRepository = equationRepository,
                                                                        referenceEv = referenceUtilisee
                                                                )?.let { (it * quantiteActuelle) / 100.0 } ?: 0.0
                                                                val CalAct: Double = alimentRation.getNutrientWithComplementary(
                                                                        nutrient = NutrientMacro.CAL,
                                                                        equationRepository = equationRepository,
                                                                        referenceEv = referenceUtilisee
                                                                )?.let { (it * quantiteActuelle) / 100.0 } ?: 0.0
                                                                val calPar100g = alimentRation.getNutrientWithComplementary(
                                                                        nutrient = NutrientMacro.CAL,
                                                                        equationRepository = equationRepository,
                                                                        referenceEv = referenceUtilisee
                                                                ) ?: 0.0
                                                                val pPar100g = alimentRation.getNutrientWithComplementary(
                                                                        nutrient = NutrientMacro.PHOS,
                                                                        equationRepository = equationRepository,
                                                                        referenceEv = referenceUtilisee
                                                                ) ?: 0.0
                                                                val nouvelleQuantite: Double = (capMinRequis * (totalPhosphore - PhosAct) - (totalCalcium - CalAct)) / ((calPar100g - capMinRequis * pPar100g) / 100.0)
                                                                // Arrondir au centième de gramme pour éviter les erreurs de précision
                                                                val quantiteArrondie: Double = kotlin.math.round(nouvelleQuantite * 100.0) / 100.0
                                                                adjustedAliments[index] = adjustedAliments[index].copy(
                                                                        quantite = quantiteArrondie.coerceAtLeast(0.0)
                                                                )
                                                                ajustementEffectue = true
                                                        }
                                                }
                                                
                                                if (!ajustementEffectue) {
                                                        break
                                                }
                                        } else {
                                                break
                                        }
                                        
                                        iterations++
                                }
                        }
                }

                for (i in adjustedAliments.indices) {
                        val quantiteClamped = adjustedAliments[i].quantite.toDouble().coerceAtLeast(0.0)
                        adjustedAliments[i] = adjustedAliments[i].copy(quantite = quantiteClamped)
                }

                return RationAdjustmentResult(
                        success = true,
                        message =
                                translate("analnut.success.sequential_adjust", nutrimentsTraites.size.toString())
                )
        } catch (e: Exception) {
                e.rethrowIfCancellation()
                return RationAdjustmentResult(
                        success = false,
                        message = translate("analnut.error.sequential_adjust", e.message ?: "")
                )
        }
}

/**
 * Arrondit la quantité selon les règles métier
 * @param alimentRation L'aliment concerné
 * @param quantite La quantité à arrondir
 * @return La quantité arrondie
 */
/** Non privée : réutilisée par le solveur d'ajustement par contraintes (RationConstraintAdjuster.kt). */
fun arrondirQuantiteSelonRegles(alimentRation: AlimentRation, quantite: Double): Double {
        val aliment = alimentRation.aliment
        if (aliment != null) {
                val cont = aliment.cont
                val quantInt = aliment.quantInt
                
                // Règle contenant: si contenant défini et quantInt > 0, arrondir au multiple de quantInt/2
                if (cont != null && cont != ContEnum.NO && quantInt != null && quantInt > 0.0) {
                        val step = quantInt / 2.0
                        return fr.vetbrain.vetnutri_mp.Utils.NumberUtils.arrondirAuPas(quantite, step).coerceAtLeast(0.0)
                }
        }
        
        // Règles générales par défaut
        val arrondie = when {
                quantite < 20.0 -> kotlin.math.round(quantite) // Arrondi au gramme
                quantite < 200.0 -> kotlin.math.round(quantite / 5.0) * 5.0 // Arrondi aux 5g
                else -> kotlin.math.round(quantite / 25.0) * 25.0 // Arrondi aux 25g
        }
        return arrondie.coerceAtLeast(0.0)
}
