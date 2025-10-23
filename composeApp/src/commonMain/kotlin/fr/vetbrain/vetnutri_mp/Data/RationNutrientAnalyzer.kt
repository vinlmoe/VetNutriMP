package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.AAEnum
import fr.vetbrain.vetnutri_mp.Enumer.MainNutrientEnum
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis
import fr.vetbrain.vetnutri_mp.Enumer.NutrientEnergy
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientOther
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Utils.NutrientUtils
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import kotlinx.coroutines.runBlocking

/**
 * Vérifie si un nutriment est un ratio (ne doit pas être multiplié par la quantité)
 */
private fun estNutrimentRatio(nutriment: Nutrient): Boolean {
    return when (nutriment) {
        is NutrientAnalysis -> {
            // Tous les nutriments d'analyse sont des ratios, qu'ils aient une unité vide ou non
            // car ils représentent des rapports entre nutriments
            true
        }
        else -> false
    }
}

/**
 * Calcule la quantité totale d'un nutriment dans une ration
 */
private suspend fun calculerQuantiteTotaleNutriment(ration: Ration, nutriment: Nutrient): Double {
    var total = 0.0
    for (aliment in ration.alimentMutableList) {
        val valeur = aliment.getNutrient(nutriment)
        if (valeur != null) {
            val contribution = (valeur * aliment.quantite) / 100.0
            total += contribution
        }
    }
    return total
}

/**
 * Vérifie si un nutriment est associé à une équation de type ratio
 */
private suspend fun estNutrimentRatio(
    nutriment: Nutrient,
    preferencesEspece: PreferencesEspece,
    equationRepository: EquationRepository,
    referenceEv: ReferenceEv?
): Boolean {
    // PRIORITÉ 1: Vérifier si c'est un NutrientAnalysis (tous sont des ratios)
    if (nutriment is NutrientAnalysis) {
        return true
    }
    
    // PRIORITÉ 2: Vérifier dans les équations de la ReferenceEv
    referenceEv?.equationsNut?.forEach { eq ->
        if (eq.nutrient == nutriment && eq.ratio == true) {
            return true
        }
    }
    
    // PRIORITÉ 3: Vérifier dans les préférences de l'espèce (fallback)
    val equationUuid = preferencesEspece.getEquationComplementaire(nutriment.label)
    if (equationUuid != null) {
        val equation = equationRepository.getEquationById(equationUuid)
        if (equation?.ratio == true) {
            return true
        }
    }
    
    return false
}

/**
 * Calcule le ratio global d'une ration en utilisant les équations complémentaires de type ratio
 */
private suspend fun calculerRatioGlobalRation(
    ration: Ration,
    nutriment: Nutrient,
    preferencesEspece: PreferencesEspece,
    equationRepository: EquationRepository,
    referenceEv: ReferenceEv?
): Double {
    // Trouver l'équation complémentaire de type ratio pour ce nutriment
    val equation = findRatioEquation(nutriment, preferencesEspece, equationRepository, referenceEv)
    
    if (equation != null) {
        // Utiliser l'équation pour calculer le ratio avec les quantités totales de la ration
        val resultat = fr.vetbrain.vetnutri_mp.Utils.EquationEvaluator.evaluerBesoinNutritionnelAvecComplementaires(
            expression = equation.equationScript,
            poidsCorps = 0.0, // Pas utilisé pour les ratios
            besoinEnergetique = 0.0, // Pas utilisé pour les ratios
            poidsMetabolique = 0.0, // Pas utilisé pour les ratios
            variablesSupp = emptyList(),
            ration = ration,
            preferences = preferencesEspece,
            equationRepository = equationRepository,
            referenceEv = referenceEv
        ) ?: 0.0
        
        return resultat
    }
    
    // Fallback: calculer directement les ratios selon le type de nutriment
    return when (nutriment) {
        is fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis -> {
            when (nutriment.label) {
                "KNA" -> {
                    // Rapport K/Na = Potassium / Sodium
                    val k = runBlocking { calculerQuantiteTotaleNutriment(ration, fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.K) }
                    val na = runBlocking { calculerQuantiteTotaleNutriment(ration, fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.NA) }
                    if (na > 0) k / na else 0.0
                }
                "CAP" -> {
                    // Rapport Ca/P = Calcium / Phosphore
                    val ca = runBlocking { calculerQuantiteTotaleNutriment(ration, fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.CAL) }
                    val p = runBlocking { calculerQuantiteTotaleNutriment(ration, fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.PHOS) }
                    if (p > 0) ca / p else 0.0
                }
                "O6O3" -> {
                    // Rapport O6/O3 = Oméga 6 / Oméga 3
                    val o6 = runBlocking { calculerQuantiteTotaleNutriment(ration, fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid.O6) }
                    val o3 = runBlocking { calculerQuantiteTotaleNutriment(ration, fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid.O3) }
                    if (o3 > 0) o6 / o3 else 0.0
                }
                "ZNCU" -> {
                    // Rapport Zn/Cu = Zinc / Cuivre
                    val zn = runBlocking { calculerQuantiteTotaleNutriment(ration, fr.vetbrain.vetnutri_mp.Enumer.NutrientMin.ZN) }
                    val cu = runBlocking { calculerQuantiteTotaleNutriment(ration, fr.vetbrain.vetnutri_mp.Enumer.NutrientMin.CU) }
                    if (cu > 0) zn / cu else 0.0
                }
                "PROTP" -> {
                    // Rapport Protéines/Phosphore
                    val prot = runBlocking { calculerQuantiteTotaleNutriment(ration, fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.PROTEINE) }
                    val p = runBlocking { calculerQuantiteTotaleNutriment(ration, fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.PHOS) }
                    if (p > 0) prot / p else 0.0
                }
                "nonOsPP" -> {
                    // Ratio Prot/Phos non osseux
                    val nonOsProt = runBlocking { calculerQuantiteTotaleNutriment(ration, fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis.nonOsProt) }
                    val nonOsPhos = runBlocking { calculerQuantiteTotaleNutriment(ration, fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis.nonOsPhos) }
                    if (nonOsPhos > 0) nonOsProt / nonOsPhos else 0.0
                }
                else -> {
                    0.0
                }
            }
        }
        else -> {
            0.0
        }
    }
}

/**
 * Trouve l'équation complémentaire de type ratio pour un nutriment donné
 */
private suspend fun findRatioEquation(
    nutriment: Nutrient,
    preferencesEspece: PreferencesEspece,
    equationRepository: EquationRepository,
    referenceEv: ReferenceEv?
): fr.vetbrain.vetnutri_mp.Data.Equation? {
    // Vérifier dans les préférences de l'espèce
    val equationUuid = preferencesEspece.getEquationComplementaire(nutriment.label)
    if (equationUuid != null) {
        val equation = equationRepository.getEquationById(equationUuid)
        if (equation?.ratio == true) {
            return equation
        }
    }
    
    // Vérifier dans les équations de la ReferenceEv
    referenceEv?.equationsNut?.forEach { eq ->
        if (eq.nutrient == nutriment && eq.ratio == true) {
            return eq
        }
    }
    
    return null
}

/**
 * Fonction qui analyse une ration et retourne une Map de tous les nutriments avec en clés le label
 * de chaque nutriment et en valeur ValeurNutritionnelle
 *
 * @param ration La ration à analyser
 * @return Map<String, ValeurNutritionnelle> contenant l'analyse nutritionnelle complète
 */
fun analyserValeursNutritionnellesRation(ration: Ration): Map<String, ValeurNutritionnelle> {
    val resultat = mutableMapOf<String, ValeurNutritionnelle>()

    // Obtenir tous les nutriments disponibles
    val tousLesNutriments = obtenirTousLesNutriments()

    // Pour chaque nutriment, calculer sa valeur dans la ration
    tousLesNutriments.forEach { nutriment ->
        val valeurNutritionnelle = calculerValeurNutrimentDansRation(ration, nutriment)
        resultat[nutriment.label] = valeurNutritionnelle
    }

    return resultat
}

/**
 * Variante qui intègre les équations complémentaires par ingrédient via
 * getNutrientWithComplementary
 */
suspend fun analyserValeursNutritionnellesRationAvecEquations(
        ration: Ration,
        preferencesEspece: PreferencesEspece,
        equationRepository: EquationRepository,
        referenceEv: ReferenceEv? = null
): Map<String, ValeurNutritionnelle> {
    val resultat = mutableMapOf<String, ValeurNutritionnelle>()
    val tousLesNutriments = obtenirTousLesNutriments()

    tousLesNutriments.forEach { nutriment ->
        var valeurTotale = 0.0
        val contributrionsIngredients = mutableListOf<String>()
        var tousLesIngredientsOntUneValeur = true
        var auMoinsUnIngredientAUneValeur = false

        if (nutriment is AAEnum) {
            println("[AA_CALC][EQUATIONS] ===== DEBUT CALCUL ${nutriment.label} =====")
        }

        val isRatio = kotlinx.coroutines.runBlocking {
            estNutrimentRatio(nutriment, preferencesEspece, equationRepository, referenceEv)
        }
        
        if (isRatio) {
            // Pour les ratios, calculer le ratio global de la ration entière
            val quantitesTotales = mutableMapOf<String, Double>()
            for (n in fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.entries) {
                val qte = kotlinx.coroutines.runBlocking { calculerQuantiteTotaleNutriment(ration, n) }
                quantitesTotales[n.label] = qte
            }
            for (n in fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.entries) {
                val qte = kotlinx.coroutines.runBlocking { calculerQuantiteTotaleNutriment(ration, n) }
                quantitesTotales[n.label] = qte
            }
            for (n in fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid.entries) {
                val qte = kotlinx.coroutines.runBlocking { calculerQuantiteTotaleNutriment(ration, n) }
                quantitesTotales[n.label] = qte
            }
            for (n in fr.vetbrain.vetnutri_mp.Enumer.NutrientMin.entries) {
                val qte = kotlinx.coroutines.runBlocking { calculerQuantiteTotaleNutriment(ration, n) }
                quantitesTotales[n.label] = qte
            }
            
            valeurTotale = kotlinx.coroutines.runBlocking {
                calculerRatioGlobalRation(ration, nutriment, preferencesEspece, equationRepository, referenceEv)
            }
            auMoinsUnIngredientAUneValeur = valeurTotale > 0.0
            contributrionsIngredients.add("Ratio global:$valeurTotale")
        } else {
            // Pour les autres nutriments, calculer la somme pondérée des contributions
            ration.alimentMutableList.forEach { alimentRation ->
                val nomIngredient = alimentRation.aliment?.nom ?: "Ingrédient inconnu"
                val quantiteIngredient = alimentRation.quantite

                // Utiliser la logique unifiée: valeur table > 0 sinon équation complémentaire
                // (évite que 0.0 bloque l'utilisation de l'équation)
                val valeurPour100g: Double? = alimentRation.getNutrientWithComplementary(
                        nutrient = nutriment,
                        preferences = preferencesEspece,
                        equationRepository = equationRepository,
                        referenceEv = referenceEv
                )

                if (valeurPour100g != null) {
                    // Alignement avec la logique hors-équations: les AA sont exprimés en % de protéines
                    val contributionIngredient = if (nutriment is AAEnum) {
                        val teneurProteines = alimentRation.aliment?.getNutrient(NutrientMain.PROTEINE) ?: 0.0
                        val valeurAminoAcideEnPourcentAliment = (valeurPour100g * teneurProteines) / 100.0
                        (valeurAminoAcideEnPourcentAliment * quantiteIngredient) / 100.0
                    } else {
                        (valeurPour100g * quantiteIngredient) / 100.0
                    }
                    valeurTotale += contributionIngredient
                    auMoinsUnIngredientAUneValeur = true
                    contributrionsIngredients.add("$nomIngredient:$contributionIngredient")
                } else {
                    tousLesIngredientsOntUneValeur = false
                    contributrionsIngredients.add("$nomIngredient:NA")
                }
            }
        }

        val description =
                if (auMoinsUnIngredientAUneValeur && valeurTotale > 0) {
                    contributrionsIngredients
                            .map { contrib ->
                                val parts = contrib.split(":")
                                val nom = parts[0]
                                val valeur = parts[1]

                                if (valeur == "NA") {
                                    "$nom: NA"
                                } else {
                                    val valeurAbs = valeur.toDouble()
                                    val pourcentage =
                                            ((valeurAbs / valeurTotale) * 100).let {
                                                TextUtils.formatDecimal(it, 1)
                                            }
                                    "$nom: ${TextUtils.formatDecimal(valeurAbs, 2)} (${pourcentage}%)"
                                }
                            }
                            .joinToString(", ")
                } else {
                    "Aucune valeur disponible pour ce nutriment"
                }

        

        resultat[nutriment.label] =
                ValeurNutritionnelle(
                        nutriment = nutriment,
                        unite = nutriment.ue,
                        valeur = valeurTotale,
                        description = description,
                        complete = tousLesIngredientsOntUneValeur && auMoinsUnIngredientAUneValeur
                )
    }

    return resultat
}

/**
 * Calcule la valeur d'un nutriment spécifique dans une ration
 *
 * @param ration La ration à analyser
 * @param nutriment Le nutriment à calculer
 * @return ValeurNutritionnelle pour ce nutriment
 */
private fun calculerValeurNutrimentDansRation(
        ration: Ration,
        nutriment: Nutrient
): ValeurNutritionnelle {
    // Pas de dérivation automatique: les analyses (CAP, KNA, ...) ne sont pas calculées ici.
    var valeurTotale = 0.0
    val contributrionsIngredients = mutableListOf<String>()
    var tousLesIngredientsOntUneValeur = true
    var auMoinsUnIngredientAUneValeur = false

    

    if (estNutrimentRatio(nutriment)) {
        // Pour les ratios, on ne peut pas les calculer ici car on n'a pas accès aux équations
        // On retourne 0.0 pour indiquer qu'il faut utiliser la version avec équations
        valeurTotale = 0.0
        auMoinsUnIngredientAUneValeur = false
        contributrionsIngredients.add("Ratio:Non calculable sans équations")
    } else {
        // Pour les autres nutriments, calculer la somme pondérée
        ration.alimentMutableList.forEach { alimentRation ->
            val nomIngredient = alimentRation.aliment?.nom ?: "Ingrédient inconnu"
            val quantiteIngredient = alimentRation.quantite

            // Obtenir la valeur du nutriment pour cet ingrédient (pour 100g)
            val valeurNutrimentPour100g = alimentRation.aliment?.getNutrient(nutriment)

            if (valeurNutrimentPour100g != null) {
                // Calculer la contribution pondérée par la quantité
                val contributionIngredient = if (nutriment is AAEnum) {
                    // Pour les acides aminés, les valeurs sont en % de protéines
                    // Il faut multiplier par la teneur en protéines de l'aliment
                    val teneurProteines = alimentRation.aliment?.getNutrient(NutrientMain.PROTEINE) ?: 0.0
                    val valeurAminoAcideEnPourcentAliment = (valeurNutrimentPour100g * teneurProteines) / 100.0
                    val contribution = (valeurAminoAcideEnPourcentAliment * quantiteIngredient) / 100.0
                    println("[AA_CALC][SIMPLE] ${nutriment.label} - Aliment: $nomIngredient")
                    println("[AA_CALC][SIMPLE]   valeurNutrimentPour100g (%% protéines): $valeurNutrimentPour100g")
                    println("[AA_CALC][SIMPLE]   teneurProteines (g/100g): $teneurProteines")
                    println("[AA_CALC][SIMPLE]   quantiteIngredient (g): $quantiteIngredient")
                    println("[AA_CALC][SIMPLE]   valeurAminoAcideEnPourcentAliment (g/100g): $valeurAminoAcideEnPourcentAliment")
                    println("[AA_CALC][SIMPLE]   contribution (g): $contribution")
                    contribution
                } else {
                    // Pour les autres nutriments, calcul normal
                    (valeurNutrimentPour100g * quantiteIngredient) / 100.0
                }
                valeurTotale += contributionIngredient

                // Calculer le pourcentage d'apport de cet ingrédient
                auMoinsUnIngredientAUneValeur = true

                // On stocke temporairement la contribution pour calculer les pourcentages après
                contributrionsIngredients.add("$nomIngredient:$contributionIngredient")
            } else {
                // L'ingrédient n'a pas de valeur pour ce nutriment
                tousLesIngredientsOntUneValeur = false
                if (nutriment is AAEnum) {
                    val db = try { alimentRation.aliment?.dataB } catch (e: Exception) { null }
                    println("[AA_CALC][SIMPLE] ${nutriment.label} - Aliment: $nomIngredient : valeurNutrimentPour100g NULL (dataB=$db)")
                }
                contributrionsIngredients.add("$nomIngredient:NA")
            }
        }
    }

    // Calculer les pourcentages d'apport et construire la description
    val description =
            if (auMoinsUnIngredientAUneValeur && valeurTotale > 0) {
                contributrionsIngredients
                        .map { contrib ->
                            val parts = contrib.split(":")
                            val nom = parts[0]
                            val valeur = parts[1]

                            if (valeur == "NA") {
                                "$nom: NA"
                            } else {
                                val valeurAbs = valeur.toDouble()
                                val pourcentage =
                                        ((valeurAbs / valeurTotale) * 100).let {
                                            TextUtils.formatDecimal(it, 1)
                                        }
                                "$nom: ${TextUtils.formatDecimal(valeurAbs, 2)} (${pourcentage}%)"
                            }
                        }
                        .joinToString(", ")
            } else {
                "Aucune valeur disponible pour ce nutriment"
            }

    

    return ValeurNutritionnelle(
            nutriment = nutriment,
            unite = nutriment.ue,
            valeur = valeurTotale,
            description = description,
            complete = tousLesIngredientsOntUneValeur && auMoinsUnIngredientAUneValeur
    )
}

/**
 * Obtient la liste de tous les nutriments disponibles dans le système
 *
 * @return Liste de tous les nutriments
 */
private fun obtenirTousLesNutriments(): List<Nutrient> {
    val nutriments = mutableListOf<Nutrient>()

    // Ajouter tous les types de nutriments
    nutriments.addAll(NutrientMain.entries)
    nutriments.addAll(NutrientMacro.entries)
    nutriments.addAll(NutrientMin.entries)
    nutriments.addAll(NutrientVitam.entries)
    nutriments.addAll(NutrientLipid.entries)
    nutriments.addAll(NutrientAnalysis.entries)
    nutriments.addAll(AAEnum.entries)
    nutriments.addAll(NutrientOther.entries)
    nutriments.addAll(NutrientEnergy.entries)

    return nutriments
}

/**
 * Obtient une liste filtrée de nutriments selon les préférences utilisateur
 *
 * @param nutrimentsSelectionnes Liste des labels des nutriments sélectionnés dans les préférences
 * @return Liste des nutriments correspondant aux préférences
 */
fun obtenirNutrimentsSelonPreferences(nutrimentsSelectionnes: List<String>): List<Nutrient> {
    // Obtenir tous les nutriments disponibles
    val tousLesNutriments = obtenirTousLesNutriments()

    // Filtrer selon les préférences (comparaison par label)
    return tousLesNutriments.filter { nutriment ->
        nutrimentsSelectionnes.contains(nutriment.label)
    }
}

/**
 * Obtient une liste filtrée de nutriments selon les préférences utilisateur (version alternative
 * avec enum names)
 *
 * @param nutrimentsSelectionnes Liste des noms d'enum des nutriments sélectionnés dans les
 * préférences
 * @return Liste des nutriments correspondant aux préférences
 */
fun obtenirNutrimentsSelonPreferencesParNom(nutrimentsSelectionnes: List<String>): List<Nutrient> {
    // Obtenir tous les nutriments disponibles
    val tousLesNutriments = obtenirTousLesNutriments()

    // Filtrer selon les préférences (comparaison par label puisque les nutriments sont labelable)
    return tousLesNutriments.filter { nutriment ->
        nutrimentsSelectionnes.contains(nutriment.label)
    }
}

/**
 * Version modifiée de analyserValeursNutritionnellesRation qui ne traite que les nutriments
 * sélectionnés
 *
 * @param ration La ration à analyser
 * @param nutrimentsSelectionnes Liste des labels des nutriments à analyser
 * @return Map<String, ValeurNutritionnelle> contenant uniquement l'analyse des nutriments
 * sélectionnés
 */
suspend fun analyserValeursNutritionnellesRationSelective(
        ration: Ration,
        nutrimentsSelectionnes: List<String>,
        preferencesEspece: PreferencesEspece? = null,
        equationRepository: EquationRepository? = null,
        referenceEv: ReferenceEv? = null
): Map<String, ValeurNutritionnelle> {
    val resultat = mutableMapOf<String, ValeurNutritionnelle>()
    
    // Obtenir seulement les nutriments sélectionnés selon les préférences
    val nutrimentsAAnalyser = obtenirNutrimentsSelonPreferences(nutrimentsSelectionnes)

    // Pour chaque nutriment sélectionné, calculer sa valeur dans la ration
    nutrimentsAAnalyser.forEach { nutriment ->
        if (equationRepository != null) {
            
            // Vérifier si c'est un ratio AVANT de traiter
            val isRatio = estNutrimentRatio(nutriment, preferencesEspece ?: PreferencesEspece(), equationRepository, referenceEv)
            
            // Version intégrant les équations complémentaires par ingrédient
            var valeurTotale = 0.0
            val contributrionsIngredients = mutableListOf<String>()
            var tousLesIngredientsOntUneValeur = true
            var auMoinsUnIngredientAUneValeur = false
            
            if (isRatio) {
                // Pour les ratios, calculer le ratio global de la ration entière
                
                valeurTotale = calculerRatioGlobalRation(ration, nutriment, preferencesEspece ?: PreferencesEspece(), equationRepository, referenceEv)
                auMoinsUnIngredientAUneValeur = valeurTotale > 0.0
                contributrionsIngredients.add("Ratio global:$valeurTotale")
            } else {
                // Pour les autres nutriments, calculer la somme pondérée des contributions

                ration.alimentMutableList.forEach { alimentRation ->
                val nomIngredient = alimentRation.aliment?.nom ?: "Ingrédient inconnu"
                val quantiteIngredient = alimentRation.quantite

                val valeurPour100g: Double? = alimentRation.getNutrientWithComplementary(
                        nutrient = nutriment,
                        preferences = preferencesEspece,
                        equationRepository = equationRepository,
                        referenceEv = referenceEv
                )

                if (valeurPour100g != null) {
                    val contributionIngredient = if (nutriment is AAEnum) {
                        val teneurProteines = alimentRation.aliment?.getNutrient(NutrientMain.PROTEINE) ?: 0.0
                        val valeurAA100g = (valeurPour100g * teneurProteines) / 100.0
                        (valeurAA100g * quantiteIngredient) / 100.0
                    } else {
                        (valeurPour100g * quantiteIngredient) / 100.0
                    }
                    valeurTotale += contributionIngredient
                    auMoinsUnIngredientAUneValeur = true
                    contributrionsIngredients.add("$nomIngredient:$contributionIngredient")
                } else {
                    tousLesIngredientsOntUneValeur = false
                    contributrionsIngredients.add("$nomIngredient:NA")
                }
            }
            }

            val description =
                    if (auMoinsUnIngredientAUneValeur && valeurTotale > 0) {
                        contributrionsIngredients
                                .map { contrib ->
                                    val parts = contrib.split(":")
                                    val nom = parts[0]
                                    val valeur = parts[1]

                                    if (valeur == "NA") {
                                        "$nom: NA"
                                    } else {
                                        val pourcentage =
                                                ((valeur.toDouble() / valeurTotale) * 100).let {
                                                    TextUtils.formatDecimal(it, 1)
                                                }
                                        "$nom: $pourcentage%"
                                    }
                                }
                                .joinToString(", ")
                    } else {
                        "Aucune valeur disponible pour ce nutriment"
                    }

            resultat[nutriment.label] =
                    ValeurNutritionnelle(
                            nutriment = nutriment,
                            unite = nutriment.ue,
                            valeur = valeurTotale,
                            description = description,
                            complete =
                                    tousLesIngredientsOntUneValeur && auMoinsUnIngredientAUneValeur
                    )
        } else {
            val valeurNutritionnelle = calculerValeurNutrimentDansRation(ration, nutriment)
            resultat[nutriment.label] = valeurNutritionnelle
        }
    }

    return resultat
}

/**
 * Fonction d'exemple montrant comment utiliser analyserValeursNutritionnellesRation
 *
 * @param ration La ration à analyser
 */
fun exempleUtilisationAnalyseRation(ration: Ration) {

    val valeursNutritionnelles = analyserValeursNutritionnellesRation(ration)

    // Afficher les résultats pour quelques nutriments clés
    val nutrimentsImportants =
            listOf("PROTEINE", "LIPIDE", "GLUCIDE", "ENERGIE", "CAL", "PHOS", "FE", "VITA")

    nutrimentsImportants.forEach { labelNutriment ->
        val valeur = valeursNutritionnelles[labelNutriment]
        if (valeur != null) {
            
            
        }
    }

    // Statistiques générales
    val nutrimentsComplets = valeursNutritionnelles.values.count { it.complete }
    val nutrimentsAvecValeur = valeursNutritionnelles.values.count { it.valeur > 0 }
    val totalNutriments = valeursNutritionnelles.size
}

/**
 * Exemple d'utilisation des nouvelles fonctions de filtrage par préférences
 *
 * @param ration La ration à analyser
 */
suspend fun exempleUtilisationAnalyseRationSelective(ration: Ration) {

    // Exemple de préférences utilisateur : seulement les nutriments principaux et quelques
    // vitamines
    val preferencesUtilisateur =
            listOf(
                    "PROTEINE",
                    "LIPIDE",
                    "GLUCIDE",
                    "ENERGIE",
                    "CAL",
                    "PHOS",
                    "VITA",
                    "VITD",
                    "VITE",
                    "FE",
                    "ZN",
                    "CU"
            )

    // Analyser seulement les nutriments sélectionnés
    val valeursNutritionnellesSelectives =
            analyserValeursNutritionnellesRationSelective(ration, preferencesUtilisateur)

    // Afficher les résultats
    valeursNutritionnellesSelectives.forEach { (label, valeur) -> }

    // Comparaison avec l'analyse complète
    val valeursNutritionnellesCompletes = analyserValeursNutritionnellesRation(ration)
}

/**
 * Convertit les préférences d'une espèce en liste de labels de nutriments sélectionnés
 *
 * @param preferencesEspece Les préférences pour l'espèce
 * @return Liste des labels des nutriments sélectionnés
 */
fun convertirPreferencesVersLabelsNutriments(preferencesEspece: PreferencesEspece): List<String> {
    val labelsSelectionnes = mutableListOf<String>()

    // Parcourir chaque catégorie de nutriments dans les préférences
    preferencesEspece.nutrimentsSelectionnes.forEach { (categorieNom, coefsSelectionnes) ->
        // Convertir le nom de catégorie en enum
        val categorie =
                try {
                    MainNutrientEnum.valueOf(categorieNom)
                } catch (e: IllegalArgumentException) {
                    return@forEach // Ignorer les catégories non reconnues
                }

        // Obtenir tous les nutriments de cette catégorie
        val nutrimentsCategorie = NutrientUtils.getNutrientsForCategory(categorie)

        // Filtrer les nutriments sélectionnés et récupérer leurs labels
        coefsSelectionnes.forEach { coefSelectionne ->
            val nutrimentTrouve = nutrimentsCategorie.find { it.coef == coefSelectionne }
            nutrimentTrouve?.let { nutriment -> labelsSelectionnes.add(nutriment.label) }
        }
    }

    return labelsSelectionnes.distinct() // Éliminer les doublons éventuels
}
