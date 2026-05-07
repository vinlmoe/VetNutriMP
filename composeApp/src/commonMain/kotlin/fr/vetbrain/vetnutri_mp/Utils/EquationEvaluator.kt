package fr.vetbrain.vetnutri_mp.Utils

import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Data.PreferencesEspece
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Data.SupplementalvariableP
import fr.vetbrain.vetnutri_mp.Enumer.AAEnum
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository

/**
 * Évaluateur spécialisé pour les équations vétérinaires Facilite l'utilisation du parser
 * mathématique dans le contexte de VetNutriMP
 */
object EquationEvaluator {

    /** Variables de base toujours disponibles dans les équations vétérinaires */
    private var variablesDeBase: Set<String>? = null

    /** Variables supplémentaires disponibles selon le contexte */
    private var variablesSupplementaires: Set<String>? = null

    /** Toutes les variables disponibles dans le système */
    var toutesLesVariables: Set<String>? = null

    /** Cache pour éviter les recalculs d'expressions identiques */
    private val expressionCache = mutableMapOf<String, Double?>()
    private val variableCache =
            mutableMapOf<String, Map<String, Double>>() // Cache pour les variables pré-calculées

    /** Taille maximale du cache pour éviter la fuite mémoire */
    private const val MAX_CACHE_SIZE = 1000

    /**
     * Injecte dans la map de variables certains nutriments calculés à partir des équations
     * complémentaires de la `ReferenceEv` lorsque leur valeur n'est pas renseignée.
     *
     * Actuellement, cette fonction est utilisée surtout pour l'ENA dans les équations énergétiques.
     * Elle n'utilise que les équations portées par `ReferenceEv` (et pas les préférences d'espèce).
     */
    private fun injecterNutrimentsDepuisReference(
            variables: MutableMap<String, Double>,
            referenceEv: ReferenceEv?
    ) {
        if (referenceEv == null) return
        val equationsComplementaires =
                referenceEv.equationsNut.filter {
                    it.kind == fr.vetbrain.vetnutri_mp.Enumer.EquationKind.COMPLEMENTARY_NUTRIENT &&
                            it.equationScript.isNotBlank()
                }
        if (equationsComplementaires.isEmpty()) return
        val nutrimentsCibles = setOf(NutrientMain.ENA.label)
        val equationsParNutriment =
                equationsComplementaires.groupBy { eq -> eq.nutrient?.label ?: "" }
        nutrimentsCibles.forEach { label ->
            val listeEquations = equationsParNutriment[label] ?: emptyList()
            if (listeEquations.isEmpty()) return@forEach
            val valeurExistante = variables[label] ?: 0.0
            if (valeurExistante > 0.0) return@forEach
            var accumulation: Double? = null
            listeEquations.forEach { eq ->
                val res = ExpressionMathematique.evaluer(eq.equationScript, variables) ?: 0.0
                accumulation =
                        if (eq.ratio) {
                            res
                        } else {
                            (accumulation ?: 0.0) + res
                        }
            }
            val valeurFinale = accumulation
            if (valeurFinale != null && !valeurFinale.isNaN() && !valeurFinale.isInfinite()) {
                variables[label] = valeurFinale
            }
        }
    }

    /** Initialise les variables de base de manière paresseuse */
    private fun initializeVariables() {
        if (variablesDeBase == null) {
            variablesDeBase =
                    setOf(
                            "BW", // Body Weight - Poids corporel
                            "BEE", // Basic Energy Expenditure - Besoin énergétique de base
                            "MW", // Metabolic Weight - Poids métabolique
                            "iBW" // Ideal Body Weight - Poids corporel idéal
                    )
        }
        if (variablesSupplementaires == null) {
            variablesSupplementaires = VariableKind.entries.map { it.variable }.toSet()
        }
        if (toutesLesVariables == null) {
            toutesLesVariables = variablesDeBase!! + variablesSupplementaires!!
        }
    }

    /** Nettoie les caches si nécessaire pour éviter la fuite mémoire */
    private fun cleanupCacheIfNeeded() {
        if (expressionCache.size > MAX_CACHE_SIZE) {
            // Garder seulement les 500 entrées les plus récentes
            val sortedEntries =
                    expressionCache.entries.sortedByDescending { it.value?.hashCode() ?: 0 }
            expressionCache.clear()
            sortedEntries.take(500).forEach { (key, value) -> expressionCache[key] = value }
        }
    }

    /**
     * Évalue une équation pour un animal donné
     *
     * @param expression L'expression mathématique à évaluer
     * @param poidsCorps Le poids corporel de l'animal (kg)
     * @param variablesSupp Liste des variables supplémentaires
     * @return Le résultat de l'évaluation ou null en cas d'erreur
     */
    fun evaluerPourAnimal(
            expression: String,
            poidsCorps: Double,
            variablesSupp: List<SupplementalvariableP> = emptyList()
    ): Double? {
        initializeVariables()
        val variables = mutableMapOf<String, Double>()

        // Ajouter le poids corporel
        variables["BW"] = poidsCorps

        // Ajouter les variables supplémentaires
        for (variable in variablesSupp) {
            variable.variable?.let { varKind ->
                val v = variable.varue?.toDouble() ?: 0.0
                // Nom long (ex: "Distance", "CarriedWeiht")
                variables[varKind.variable] = v
                // Alias court (ex: "D", "CW")
                variables[varKind.label] = v
            }
        }

        // Mapper certains noms de variables supplémentaires vers les alias courts utilisés dans
        // les équations (compatibilité avec scripts existants)
        run {
            // Aliases standards et tolérants à la casse/synonymes
            val aliasPairs: List<Pair<String, String>> =
                    listOf(
                            // Masse/poids adultes
                            "adultWeight" to "AW",
                            "AdultWeight" to "AW",
                            // Taille de portée
                            "litterSize" to "L",
                            "LitterSize" to "L",
                            // Semaines de gestation/lactation
                            "gestationWeek" to "wG",
                            "WeekGestation" to "wG",
                            "lactationWeek" to "wL",
                            "WeekLactation" to "wL",
                            // Score d'état corporel
                            "bodyConditionScore" to "BCS",
                            "BCS" to "BCS",
                            // Distance portée (déplacements)
                            "Distance" to "D",
                            "distance" to "D",
                            "dist" to "D",
                            "D" to "D",
                            // Poids porté (alias, correction typos)
                            "CarriedWeight" to "CW",
                            "CarriedWeiht" to "CW",
                            "carriedWeight" to "CW",
                            "CW" to "CW"
                    )
            // Construire une map insensible à la casse à partir des variables présentes
            val lowerKeyToActual = variables.keys.associateBy { it.lowercase() }
            for ((from, to) in aliasPairs) {
                val actualKey = lowerKeyToActual[from.lowercase()]
                if (actualKey != null && !variables.containsKey(to)) {
                    variables[to] = variables[actualKey] ?: 0.0
                }
            }
        }

        // Nettoyer le cache si nécessaire
        cleanupCacheIfNeeded()

        // Utiliser le cache pour éviter les recalculs
        val cacheKey = "${expression}:${variables.hashCode()}"
        return expressionCache.getOrPut(cacheKey) {
            ExpressionMathematique.evaluer(expression, variables)
        }
    }

    /** Version avec cache optimisée pour les évaluations répétées */
    fun evaluerPourAnimalAvecCache(
            expression: String,
            poidsCorps: Double,
            variablesSupp: List<SupplementalvariableP> = emptyList()
    ): Double? {
        val variables = mutableMapOf<String, Double>()

        // Ajouter le poids corporel
        variables["BW"] = poidsCorps

        // Ajouter les variables supplémentaires
        for (variable in variablesSupp) {
            variable.variable?.let { varKind ->
                val v = variable.varue?.toDouble() ?: 0.0
                // Nom long (ex: "Distance", "CarriedWeiht")
                variables[varKind.variable] = v
                // Alias court (ex: "D", "CW")
                variables[varKind.label] = v
            }
        }

        // Nettoyer le cache si nécessaire
        cleanupCacheIfNeeded()

        // Utiliser le cache pour éviter les recalculs
        val cacheKey = "${expression}:${variables.hashCode()}"
        return expressionCache.getOrPut(cacheKey) {
            ExpressionMathematique.evaluer(expression, variables)
        }
    }

    /**
     * Évalue une équation de besoin nutritionnel
     *
     * @param expression L'expression mathématique à évaluer
     * @param poidsCorps Le poids corporel de l'animal (kg)
     * @param besoinEnergetique Le besoin énergétique de base
     * @param poidsMetabolique Le poids métabolique
     * @param variablesSupp Liste des variables supplémentaires
     * @param ration La ration à évaluer
     * @return Le résultat de l'évaluation ou null en cas d'erreur
     */
    fun evaluerBesoinNutritionnel(
            expression: String,
            poidsCorps: Double,
            besoinEnergetique: Double,
            poidsMetabolique: Double,
            variablesSupp: List<SupplementalvariableP> = emptyList(),
            ration: Ration
    ): Double? {
        val variables = mutableMapOf<String, Double>()

        // Variables de base
        variables["BW"] = poidsCorps
        variables["BEE"] = besoinEnergetique
        variables["MW"] = poidsMetabolique

        // Variables supplémentaires
        for (variable in variablesSupp) {
            variable.variable?.let { varKind ->
                val v = variable.varue?.toDouble() ?: 0.0
                // Nom long (ex: "Distance", "CarriedWeiht")
                variables[varKind.variable] = v
                // Alias court (ex: "D", "CW")
                variables[varKind.label] = v
            }
        }

        // Appliquer les mêmes alias tolérants à la casse/synonymes que dans evaluerPourAnimal
        run {
            val aliasPairs: List<Pair<String, String>> =
                    listOf(
                            // Masse/poids adultes
                            "adultWeight" to "AW",
                            "AdultWeight" to "AW",
                            // Taille de portée
                            "litterSize" to "L",
                            "LitterSize" to "L",
                            // Semaines de gestation/lactation
                            "gestationWeek" to "wG",
                            "WeekGestation" to "wG",
                            "lactationWeek" to "wL",
                            "WeekLactation" to "wL",
                            // Score d'état corporel
                            "bodyConditionScore" to "BCS",
                            "BCS" to "BCS",
                            // Distance portée (déplacements)
                            "Distance" to "D",
                            "distance" to "D",
                            "dist" to "D",
                            "D" to "D",
                            // Poids porté (alias, correction typos)
                            "CarriedWeight" to "CW",
                            "CarriedWeiht" to "CW",
                            "carriedWeight" to "CW",
                            "CW" to "CW"
                    )
            val lowerKeyToActual = variables.keys.associateBy { it.lowercase() }
            for ((from, to) in aliasPairs) {
                val actualKey = lowerKeyToActual[from.lowercase()]
                if (actualKey != null && !variables.containsKey(to)) {
                    variables[to] = variables[actualKey] ?: 0.0
                }
            }
        }

        // Nutriments de la ration (tous nutriments utiles pour ratios)
        // Remplacer les valeurs null par 0 pour éviter les erreurs dans les calculs
        NutrientMain.entries.forEach { nutrient ->
            val v = ration.getNutrient(nutrient)?.toDouble() ?: 0.0
            variables[nutrient.label] = if (v.isNaN() || v.isInfinite()) 0.0 else v
            // Log léger pour diagnostiquer en cas de pb d'équations
            //
        }
        NutrientLipid.entries.forEach { nutrient ->
            val v = ration.getNutrient(nutrient)?.toDouble() ?: 0.0
            variables[nutrient.label] = if (v.isNaN() || v.isInfinite()) 0.0 else v
        }
        NutrientVitam.entries.forEach { nutrient ->
            val v = ration.getNutrient(nutrient)?.toDouble() ?: 0.0
            variables[nutrient.label] = if (v.isNaN() || v.isInfinite()) 0.0 else v
        }
        NutrientMacro.entries.forEach { nutrient ->
            val v = ration.getNutrient(nutrient)?.toDouble() ?: 0.0
            variables[nutrient.label] = if (v.isNaN() || v.isInfinite()) 0.0 else v
        }
        NutrientMin.entries.forEach { nutrient ->
            val v = ration.getNutrient(nutrient)?.toDouble() ?: 0.0
            variables[nutrient.label] = if (v.isNaN() || v.isInfinite()) 0.0 else v
        }

        val res = ExpressionMathematique.evaluer(expression, variables)
        //
        return res
    }

    /**
     * Évalue une équation de besoin nutritionnel en tenant compte des nutriments complémentaires.
     * Utilise le calcul complémentaire pour chaque aliment de la ration si la valeur est absente.
     */
    suspend fun evaluerBesoinNutritionnelAvecComplementaires(
            expression: String,
            poidsCorps: Double,
            besoinEnergetique: Double,
            poidsMetabolique: Double,
            variablesSupp: List<SupplementalvariableP> = emptyList(),
            ration: Ration,
            preferences: PreferencesEspece,
            equationRepository: EquationRepository,
            referenceEv: ReferenceEv? = null
    ): Double? {
        val variables: MutableMap<String, Double> = mutableMapOf()
        variables["BW"] = poidsCorps
        variables["BEE"] = besoinEnergetique
        variables["MW"] = poidsMetabolique
        for (variable in variablesSupp) {
            variable.variable?.let { varKind ->
                val v = variable.varue?.toDouble() ?: 0.0
                // Nom long (ex: "Distance", "CarriedWeiht")
                variables[varKind.variable] = v
                // Alias court (ex: "D", "CW")
                variables[varKind.label] = v
            }
        }
        suspend fun sommeRation(nutriment: Nutrient): Double {
            var total: Double = 0.0
            for (aliment in ration.alimentMutableList) {
                val valeur: Double? =
                        aliment.getNutrientWithComplementary(
                                nutrient = nutriment,
                                preferences = preferences,
                                equationRepository = equationRepository,
                                referenceEv = referenceEv
                        )
                if (valeur != null) {
                    val contribution = (valeur * aliment.quantite.toDouble()) / 100.0
                    total += contribution
                } else {}
            }
            return total
        }
        for (n in NutrientMain.entries) variables[n.label] = sommeRation(n)
        for (n in NutrientLipid.entries) variables[n.label] = sommeRation(n)
        for (n in NutrientVitam.entries) variables[n.label] = sommeRation(n)
        for (n in NutrientMacro.entries) variables[n.label] = sommeRation(n)
        for (n in NutrientMin.entries) variables[n.label] = sommeRation(n)

        val resultat = ExpressionMathematique.evaluer(expression, variables)
        return resultat
    }

    /**
     * Évalue une équation de densité énergétique pour un aliment
     *
     * @param expression L'expression mathématique à évaluer
     * @param aliment L'aliment à évaluer
     * @return Le résultat de l'évaluation ou null en cas d'erreur
     */
    fun evaluerDensiteEnergetique(expression: String, aliment: AlimentRation): Double? {
        val variables: MutableMap<String, Double> = mutableMapOf()

        // Ajouter tous les nutriments de l'aliment
        for (nutrient in NutrientMain.entries) {
            variables[nutrient.label] = aliment.getNutrient(nutrient)?.toDouble() ?: 0.0
        }

        return ExpressionMathematique.evaluer(expression, variables)
    }

    /**
     * Variante suspendue: calcule la densité énergétique en utilisant aussi les nutriments
     * complémentaires si nécessaires.
     */
    suspend fun evaluerDensiteEnergetiqueAvecComplementaires(
            expression: String,
            aliment: AlimentRation,
            preferences: PreferencesEspece,
            equationRepository: EquationRepository,
            referenceEv: ReferenceEv? = null
    ): Double? {
        val variables: MutableMap<String, Double> = mutableMapOf()
        suspend fun valueOf(n: Nutrient): Double {
            return aliment.getNutrientWithComplementary(
                            nutrient = n,
                            preferences = preferences,
                            equationRepository = equationRepository,
                            referenceEv = referenceEv
                    )
                    ?.toDouble()
                    ?: 0.0
        }
        for (n in NutrientMain.entries) variables[n.label] = valueOf(n)
        return ExpressionMathematique.evaluer(expression, variables)
    }

    /** Liste toutes les équations disponibles dans une `ReferenceEv`. */
    fun listerEquationsReference(referenceEv: ReferenceEv): List<Equation> {
        return referenceEv.obtenirToutesEquations()
    }

    /**
     * Évalue une équation issue d'une `ReferenceEv` avec un dictionnaire de variables prêt à
     * l'emploi.
     */
    fun evaluerEquationReference(equation: Equation, variables: Map<String, Double>): Double? {
        return ExpressionMathematique.evaluer(equation.equationScript, variables)
    }

    /**
     * Calcule l'énergie pour 100 g d'un aliment en utilisant l'équation adaptée au type d'aliment.
     * - COMPLET: utilise l'équation commerciale (si disponible dans ReferenceEv, sinon fallback
     * générique)
     * - COMPLEMENTAIRE ou autres: utilise l'autre équation (générique) Les variables de nutriments
     * sont alimentées avec les valeurs directes de l'aliment (valMap), sans utiliser les équations
     * complémentaires des préférences.
     */
    suspend fun calculerEnergiePour100g(
            aliment: fr.vetbrain.vetnutri_mp.Data.AlimentRation,
            equationRepository: EquationRepository,
            referenceEv: ReferenceEv? = null
    ): Double {
        val variables: MutableMap<String, Double> = mutableMapOf()
        val alimentEv = aliment.aliment
        if (alimentEv != null) {
            for (n in NutrientMain.entries) {
                variables[n.label] = alimentEv.getNutrient(n, referenceEv)?.toDouble() ?: 0.0
            }
            for (n in NutrientLipid.entries) {
                variables[n.label] = alimentEv.getNutrient(n, referenceEv)?.toDouble() ?: 0.0
            }
            for (n in NutrientVitam.entries) {
                variables[n.label] = alimentEv.getNutrient(n, referenceEv)?.toDouble() ?: 0.0
            }
            for (n in NutrientMacro.entries) {
                variables[n.label] = alimentEv.getNutrient(n, referenceEv)?.toDouble() ?: 0.0
            }
            for (n in NutrientMin.entries) {
                variables[n.label] = alimentEv.getNutrient(n, referenceEv)?.toDouble() ?: 0.0
            }
        }

        // Harmoniser ENA avec le système d'analyse de ration :
        // si une ReferenceEv est fournie, calculer ENA via getNutrientWithComplementary
        // en n'utilisant PAS les préférences d'espèce (uniquement ReferenceEv).
        if (referenceEv != null) {
            try {
                val enaFromRefEv =
                        aliment.getNutrientWithComplementary(
                                nutrient = NutrientMain.ENA,
                                preferences = null,
                                equationRepository = equationRepository,
                                referenceEv = referenceEv
                        )
                if (enaFromRefEv != null && enaFromRefEv > 0.0) {
                    variables[NutrientMain.ENA.label] = enaFromRefEv
                }
            } catch (_: Throwable) {}
        }

        injecterNutrimentsDepuisReference(variables = variables, referenceEv = referenceEv)

        // Choisir explicitement l'équation ReferenceEv: DEcom pour COMPLET/COMPLEMENTAIRE, DEraw
        // sinon
        val kind = alimentEv?.typeAliment
        val eq: Equation? =
                when (kind) {
                    fr.vetbrain.vetnutri_mp.Enumer.FoodKind.COMPLET,
                    fr.vetbrain.vetnutri_mp.Enumer.FoodKind.COMPLEMENTAIRE ->
                            referenceEv?.equationDEcom
                    else -> referenceEv?.equationDEraw
                }
                        ?: run {
                            // Fallback générique si ReferenceEv n'a pas l'équation attendue
                            val ratioWanted =
                                    (kind == fr.vetbrain.vetnutri_mp.Enumer.FoodKind.COMPLET ||
                                            kind ==
                                                    fr.vetbrain.vetnutri_mp.Enumer.FoodKind
                                                            .COMPLEMENTAIRE)
                            equationRepository.getAllEquations().firstOrNull {
                                it.nutrient == NutrientMain.ENERGIE && it.ratio == ratioWanted
                            }
                        }
        val res =
                if (eq != null) ExpressionMathematique.evaluer(eq.equationScript, variables)
                else null
        return if (res == null || res.isNaN() || res.isInfinite()) 0.0 else res
    }

    /**
     * Calcule l'apport énergétique total d'une ration en sommant l'énergie de chaque ingrédient
     * calculée pour 100 g puis pondérée par la quantité (g/100g).
     */
    suspend fun calculerApportEnergetiqueRation(
            ration: fr.vetbrain.vetnutri_mp.Data.Ration,
            equationRepository: EquationRepository,
            referenceEv: ReferenceEv? = null
    ): Double {
        var total = 0.0
        for (ing in ration.alimentMutableList) {
            val kcal100 =
                    calculerEnergiePour100g(
                            aliment = ing,
                            equationRepository = equationRepository,
                            referenceEv = referenceEv
                    )
            total += kcal100 * (ing.quantite.toDouble() / 100.0)
        }
        return total
    }

    /**
     * Calcule l'énergie additionnelle issue des références maladies via des équations ENERCOMP.
     * - Somme toutes les équations avec kind == ENERCOMP présentes dans les références maladies.
     * - Retourne 0 si aucune équation n'est définie ou en cas d'échec d'évaluation.
     * - Borne à ≥ 0 pour éviter d'introduire des valeurs négatives.
     */
    suspend fun calculerEnergieAdditionnelle(
            referencesMaladies: List<ReferenceEv>,
            poidsCorps: Double,
            besoinEnergetiqueApresK: Double,
            besoinEnergetiqueStandard: Double,
            poidsMetabolique: Double,
            variablesSupp: List<SupplementalvariableP> = emptyList(),
            ration: Ration,
            preferences: PreferencesEspece? = null,
            equationRepository: EquationRepository? = null
    ): Double {
        if (referencesMaladies.isEmpty()) return 0.0

        // Construire les variables communes à toutes les évaluations ENERCOMP
        val variables: MutableMap<String, Double> = mutableMapOf()
        variables["BW"] = poidsCorps
        variables["BEE"] = besoinEnergetiqueStandard
        variables["BE"] = besoinEnergetiqueApresK
        variables["MW"] = poidsMetabolique
        for (variable in variablesSupp) {
            variable.variable?.let { varKind ->
                variables[varKind.label] = variable.varue?.toDouble() ?: 0.0
            }
        }

        // Logs d'entrée
        try {

            if (variablesSupp.isNotEmpty()) {
                val supp = variablesSupp.joinToString { v -> "${v.variable?.variable}=${v.varue}" }
            }
        } catch (_: Throwable) {}

        // Injecter les nutriments agrégés de la ration (avec compléments si disponibles)
        suspend fun sommeRation(n: Nutrient): Double {
            var s = 0.0
            for (aliment in ration.alimentMutableList) {
                val v =
                        aliment.getNutrientWithComplementary(
                                        nutrient = n,
                                        preferences = preferences,
                                        equationRepository = equationRepository,
                                        referenceEv = null
                                )
                                ?.toDouble()
                                ?: 0.0
                s += (v * aliment.quantite.toDouble()) / 100.0
            }
            return s
        }
        for (n in NutrientMain.entries) variables[n.label] = sommeRation(n)
        for (n in NutrientLipid.entries) variables[n.label] = sommeRation(n)
        for (n in NutrientVitam.entries) variables[n.label] = sommeRation(n)
        for (n in NutrientMacro.entries) variables[n.label] = sommeRation(n)
        for (n in NutrientMin.entries) variables[n.label] = sommeRation(n)

        // Log de quelques nutriments clés pour diagnostic
        try {
            val debugKeys = listOf("ENERGIE", "PROTEINE", "LIPIDE", "ENA")
            val dbg = debugKeys.map { k -> "$k=${variables[k] ?: 0.0}" }.joinToString(", ")
            // Log complet des variables (peut être volumineux)
            val all = variables.entries.sortedBy { it.key }.joinToString(", ") { (k, v) -> "$k=$v" }
        } catch (_: Throwable) {}

        var total = 0.0
        for (ref in referencesMaladies) {
            // ENERCOMP est porté par des équations dans equationsNut avec kind == ENERCOMP
            val eqs =
                    ref.equationsNut.filter {
                        it.kind == fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERCOMP &&
                                it.equationScript.isNotBlank()
                    }
            if (eqs.isEmpty()) continue
            for (eq in eqs) {
                try {} catch (_: Throwable) {}
                val res = ExpressionMathematique.evaluer(eq.equationScript, variables)
                try {} catch (_: Throwable) {}
                if (res != null && !res.isNaN() && !res.isInfinite()) total += res
            }
        }
        if (total.isNaN() || total.isInfinite()) {
            try {} catch (_: Throwable) {}
            return 0.0
        }
        if (total < 0.0) {
            try {} catch (_: Throwable) {}
            return 0.0
        }
        try {} catch (_: Throwable) {}
        return total
    }

    /** Construit un jeu de variables de base pour les équations de `ReferenceEv`. */
    fun construireVariablesPourReference(
            poidsCorps: Double,
            besoinEnergetique: Double? = null,
            poidsMetabolique: Double? = null,
            variablesSupp: List<SupplementalvariableP> = emptyList()
    ): MutableMap<String, Double> {
        val variables: MutableMap<String, Double> = mutableMapOf()
        variables["BW"] = poidsCorps
        if (besoinEnergetique != null) variables["BEE"] = besoinEnergetique
        if (poidsMetabolique != null) variables["MW"] = poidsMetabolique
        for (variable in variablesSupp) {
            variable.variable?.let { varKind ->
                variables[varKind.variable] = variable.varue?.toDouble() ?: 0.0
            }
        }
        return variables
    }

    /**
     * Évalue une équation de besoin/composition directement pour un aliment unique Utilise les
     * nutriments disponibles dans l'aliment comme variables (CAL, PHOS, etc.). Les variables de
     * base (BW, BEE, MW) peuvent être passées pour les équations qui en dépendent.
     */
    suspend fun evaluerBesoinNutritionnelPourAliment(
            expression: String,
            poidsCorps: Double = 0.0,
            besoinEnergetique: Double = 0.0,
            poidsMetabolique: Double = 0.0,
            variablesSupp: List<SupplementalvariableP> = emptyList(),
            aliment: AlimentRation,
            preferences: PreferencesEspece? = null,
            equationRepository: EquationRepository? = null,
            referenceEv: ReferenceEv? = null
    ): Double? {
        val variables = mutableMapOf<String, Double>()

        // Variables de base
        variables["BW"] = poidsCorps
        variables["BEE"] = besoinEnergetique
        variables["MW"] = poidsMetabolique

        // Variables supplémentaires
        for (variable in variablesSupp) {
            variable.variable?.let { varKind ->
                variables[varKind.variable] = variable.varue?.toDouble() ?: 0.0
            }
        }

        // Injecter les nutriments de l'aliment comme variables (directs uniquement ici pour éviter
        // toute récursivité au sein des équations complémentaires)
        // Remplacer les valeurs null par 0 pour éviter les erreurs dans les calculs
        aliment.aliment?.let { alim ->
            for (n in NutrientMain.entries) {
                val v = alim.getNutrient(n)?.toDouble() ?: 0.0
                variables[n.label] = if (v.isNaN() || v.isInfinite()) 0.0 else v
            }
            for (n in NutrientLipid.entries) {
                val v = alim.getNutrient(n)?.toDouble() ?: 0.0
                variables[n.label] = if (v.isNaN() || v.isInfinite()) 0.0 else v
            }
            for (n in NutrientVitam.entries) {
                val v = alim.getNutrient(n)?.toDouble() ?: 0.0
                variables[n.label] = if (v.isNaN() || v.isInfinite()) 0.0 else v
            }
            for (n in NutrientMacro.entries) {
                val v = alim.getNutrient(n)?.toDouble() ?: 0.0
                variables[n.label] = if (v.isNaN() || v.isInfinite()) 0.0 else v
            }
            for (n in NutrientMin.entries) {
                val v = alim.getNutrient(n)?.toDouble() ?: 0.0
                variables[n.label] = if (v.isNaN() || v.isInfinite()) 0.0 else v
            }
        }

        val r = ExpressionMathematique.evaluer(expression, variables)
        return if (r == null || r.isNaN() || r.isInfinite()) null else r
    }

    /** Variante suspendue utilisant les nutriments complémentaires pour un aliment unique. */
    suspend fun evaluerBesoinNutritionnelPourAlimentAvecComplementaires(
            expression: String,
            poidsCorps: Double = 0.0,
            besoinEnergetique: Double = 0.0,
            poidsMetabolique: Double = 0.0,
            variablesSupp: List<SupplementalvariableP> = emptyList(),
            aliment: AlimentRation,
            preferences: PreferencesEspece,
            equationRepository: EquationRepository,
            referenceEv: ReferenceEv? = null
    ): Double? {
        val variables = mutableMapOf<String, Double>()
        variables["BW"] = poidsCorps
        variables["BEE"] = besoinEnergetique
        variables["MW"] = poidsMetabolique
        for (variable in variablesSupp) {
            variable.variable?.let { varKind ->
                variables[varKind.variable] = variable.varue?.toDouble() ?: 0.0
            }
        }
        suspend fun valueOf(n: Nutrient): Double {
            val v =
                    aliment.getNutrientWithComplementary(
                                    nutrient = n,
                                    preferences = preferences,
                                    equationRepository = equationRepository,
                                    referenceEv = referenceEv
                            )
                            ?.toDouble()
                            ?: 0.0
            return (v * aliment.quantite.toDouble()) / 100.0
        }
        for (n in NutrientMain.entries) variables[n.label] = valueOf(n)
        for (n in NutrientLipid.entries) variables[n.label] = valueOf(n)
        for (n in NutrientVitam.entries) variables[n.label] = valueOf(n)
        for (n in NutrientMacro.entries) variables[n.label] = valueOf(n)
        for (n in NutrientMin.entries) variables[n.label] = valueOf(n)
        return ExpressionMathematique.evaluer(expression, variables)
    }

    /**
     * Valide une expression dans le contexte vétérinaire
     *
     * @param expression L'expression à valider
     * @param typeEquation Le type d'équation pour déterminer les variables disponibles
     * @return Résultat de la validation avec détails
     */
    fun validerExpression(
            expression: String,
            typeEquation: TypeEquationValidation = TypeEquationValidation.GENERALE
    ): ResultatValidation {
        if (expression.isBlank()) {
            return ResultatValidation(false, "Expression vide")
        }

        // Extraire les variables utilisées
        val variablesUtilisees = ExpressionMathematique.extraireVariables(expression)

        // Initialiser les variables si nécessaire
        initializeVariables()

        // Déterminer les variables disponibles selon le type d'équation
        val variablesDisponibles =
                when (typeEquation) {
                    TypeEquationValidation.BESOIN_ENERGETIQUE ->
                            variablesDeBase!! + variablesSupplementaires!!
                    TypeEquationValidation.BESOIN_NUTRITIONNEL ->
                            toutesLesVariables!! + getNutrientsVariables()
                    TypeEquationValidation.DENSITE_ENERGETIQUE -> getNutrientsVariables()
                    TypeEquationValidation.GENERALE ->
                            toutesLesVariables!! + getNutrientsVariables()
                }

        // Vérifier les variables manquantes
        val variablesManquantes = variablesUtilisees.filter { it !in variablesDisponibles }

        if (variablesManquantes.isNotEmpty()) {
            return ResultatValidation(
                    false,
                    "Variables non reconnues: ${variablesManquantes.joinToString(", ")}",
                    variablesManquantes,
                    variablesUtilisees.filter { it in variablesDisponibles }
            )
        }

        // Tester la syntaxe avec des valeurs par défaut
        val variablesTest = variablesUtilisees.associateWith { 1.0 }
        val estSyntaxeValide = ExpressionMathematique.estValide(expression, variablesTest)

        if (!estSyntaxeValide) {
            return ResultatValidation(false, "Erreur de syntaxe dans l'expression")
        }

        return ResultatValidation(true, "Expression valide", emptyList(), variablesUtilisees)
    }

    /** Obtient la liste des variables de nutriments disponibles */
    private fun getNutrientsVariables(): Set<String> {
        val nutrientsMain = NutrientMain.entries.map { it.label }
        val nutrientsLipides = NutrientLipid.entries.map { it.label }
        val nutrientsVitamines = NutrientVitam.entries.map { it.label }
        val nutrientsMacro = NutrientMacro.entries.map { it.label }
        val nutrientsMin = NutrientMin.entries.map { it.label }
        val acideAmines = AAEnum.entries.map { it.label }

        return (nutrientsMain +
                        nutrientsLipides +
                        nutrientsVitamines +
                        nutrientsMacro +
                        nutrientsMin +
                        acideAmines)
                .toSet()
    }

    /**
     * Calcule le poids métabolique standard (BW^0.75)
     *
     * @param poidsCorps Le poids corporel en kg
     * @return Le poids métabolique
     */
    fun calculerPoidsMetabolique(poidsCorps: Double): Double {
        return evaluerPourAnimal("BW ^ 0.75", poidsCorps) ?: 0.0
    }

    /**
     * Calcule le besoin énergétique de base avec l'équation standard
     *
     * @param poidsCorps Le poids corporel en kg
     * @param facteur Le facteur multiplicateur (défaut: 130 pour chiens)
     * @return Le besoin énergétique de base
     */
    fun calculerBesoinEnergetiqueBase(poidsCorps: Double, facteur: Double = 130.0): Double {
        return evaluerPourAnimal("$facteur * BW ^ 0.75", poidsCorps) ?: 0.0
    }

    /**
     * Teste une expression avec des valeurs d'exemple
     *
     * @param expression L'expression à tester
     * @param typeEquation Le type d'équation
     * @return Le résultat du test ou null en cas d'erreur
     */
    fun testerExpression(
            expression: String,
            typeEquation: TypeEquationValidation = TypeEquationValidation.GENERALE
    ): Double? {
        val variablesTest = mutableMapOf<String, Double>()

        // Valeurs d'exemple pour les variables de base
        variablesTest["BW"] = 25.0
        variablesTest["BEE"] = 400.0
        variablesTest["MW"] = 15.0
        variablesTest["iBW"] = 25.0

        // Valeurs d'exemple pour les variables supplémentaires
        VariableKind.entries.forEach { varKind -> variablesTest[varKind.variable] = 1.0 }

        // Valeurs d'exemple pour les nutriments si nécessaire
        if (typeEquation == TypeEquationValidation.BESOIN_NUTRITIONNEL ||
                        typeEquation == TypeEquationValidation.DENSITE_ENERGETIQUE ||
                        typeEquation == TypeEquationValidation.GENERALE
        ) {

            NutrientMain.entries.forEach { nutrient -> variablesTest[nutrient.label] = 10.0 }
            NutrientLipid.entries.forEach { nutrient -> variablesTest[nutrient.label] = 5.0 }
            NutrientVitam.entries.forEach { nutrient -> variablesTest[nutrient.label] = 1.0 }
            NutrientMacro.entries.forEach { nutrient -> variablesTest[nutrient.label] = 2.0 }
            NutrientMin.entries.forEach { nutrient -> variablesTest[nutrient.label] = 0.5 }
            AAEnum.entries.forEach { nutrient -> variablesTest[nutrient.label] = 3.0 }
        }

        return ExpressionMathematique.evaluer(expression, variablesTest)
    }
}

/** Types d'équations pour la validation */
enum class TypeEquationValidation {
    BESOIN_ENERGETIQUE, // Variables de base + supplémentaires
    BESOIN_NUTRITIONNEL, // Toutes les variables + nutriments
    DENSITE_ENERGETIQUE, // Nutriments uniquement
    GENERALE // Toutes les variables disponibles
}

/** Résultat de la validation d'une expression */
data class ResultatValidation(
        val estValide: Boolean,
        val message: String,
        val variablesManquantes: List<String> = emptyList(),
        val variablesReconnues: List<String> = emptyList()
)
