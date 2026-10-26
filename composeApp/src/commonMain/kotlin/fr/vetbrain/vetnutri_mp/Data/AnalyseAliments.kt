package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.AAEnum
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientOther
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository

/**
 * Modèles et calculs de l'analyse graphique des aliments (densité énergétique, répartition
 * énergétique, valeur d'un nutriment par 1000 kcal ou par 100 g MS).
 * Déplacés depuis View/AnalyseGraphiqueAlimentsView.kt (logique métier hors des Composables).
 */

/** Données calculées pour un aliment avec sa densité énergétique et pourcentages */
data class AlimentAnalyseData(
        val aliment: AlimentEv,
        val numero: Int,
        val densiteEnergetique: Double,
        val pourcentageProteines: Double,
        val pourcentageLipides: Double,
        val phosphorePer1000Kcal: Double = 0.0,
        val proteinePer1000Kcal: Double = 0.0,
        val calciumPer1000Kcal: Double = 0.0,
        // Tous les nutriments principaux en g/1000kcal
        val energiePer1000Kcal: Double = 0.0,
        val lipidePer1000Kcal: Double = 0.0,
        val glucidePer1000Kcal: Double = 0.0,
        // Autres nutriments macro en g/1000kcal
        val fibrePer1000Kcal: Double = 0.0,
        val cendrePer1000Kcal: Double = 0.0,
        val eauPer1000Kcal: Double = 0.0,
        val rationActual: Boolean? = null,
        val isRationSum: Boolean = false,
        val energyAsFed: Double? = null
)

/** Calcule la densité énergétique d'un aliment de manière asynchrone */
suspend fun calculerDensiteEnergetiqueAliment(
        aliment: AlimentEv,
        referenceEv: ReferenceEv?,
        equationRepository: EquationRepository?,
        useDryMatterPer100g: Boolean = false
): Double {
    // ✅ UTILISER EquationEvaluator.calculerEnergiePour100g() qui utilise correctement
    // l'équation énergétique du référentiel (equationDEcom ou equationDEraw)
    // avec les valeurs directes de l'aliment (sans équations complémentaires des préférences)
    if (referenceEv != null && equationRepository != null) {
        try {
            val alimentRation = AlimentRation(aliment = aliment, quantite = 100.0, weight = 1.0)

            val _energie =
                    fr.vetbrain.vetnutri_mp.Utils.EquationEvaluator.calculerEnergiePour100g(
                            aliment = alimentRation,
                            equationRepository = equationRepository,
                            referenceEv = referenceEv
                    )
            if (_energie > 0) {
                // Si on veut la densité par matière sèche, on doit diviser par le pourcentage de matière sèche
                return if (useDryMatterPer100g) {
                    val humidite = alimentRation.getNutrientWithComplementary(
                            nutrient = NutrientMain.HUMIDITE,
                            equationRepository = equationRepository,
                            referenceEv = referenceEv
                    ) ?: 0.0
                    val matiereSeche = 100.0 - humidite
                    if (matiereSeche > 0) {
                        _energie / matiereSeche * 100.0 // Énergie pour 100g de matière sèche
                    } else {
                        _energie
                    }
                } else {
                    _energie // Énergie pour 100g d'aliment total
                }
            }
        } catch (e: Exception) {
            // Fallback sur la méthode simple
        }
    }

    // Méthode simple : calcul basé sur les macronutriments
    val alimentRation = AlimentRation(aliment = aliment, quantite = 100.0, weight = 1.0)

    val _proteines =
            alimentRation.getNutrientWithComplementary(
                    nutrient = NutrientMain.PROTEINE,
                    equationRepository = equationRepository,
                    referenceEv = referenceEv
            )
                    ?: 0.0

    val _lipides =
            alimentRation.getNutrientWithComplementary(
                    nutrient = NutrientMain.LIPIDE,
                    equationRepository = equationRepository,
                    referenceEv = referenceEv
            )
                    ?: 0.0

    // ENA (et non GLUCIDE) : même définition du 3e macronutriment énergétique que le
    // camembert "Origine Énergie".
    val _ena =
            alimentRation.getNutrientWithComplementary(
                    nutrient = NutrientMain.ENA,
                    equationRepository = equationRepository,
                    referenceEv = referenceEv
            )
                    ?: 0.0

    // Coefficients énergétiques (kcal/g)
    val kcalProteines = _proteines * 3.5
    val kcalLipides = _lipides * 8.5
    val kcalEna = _ena * 3.5

    val energieTotale = kcalProteines + kcalLipides + kcalEna
    
    // Si on veut la densité par matière sèche, on doit diviser par le pourcentage de matière sèche
    return if (useDryMatterPer100g) {
        val humidite = alimentRation.getNutrientWithComplementary(
                nutrient = NutrientMain.HUMIDITE,
                equationRepository = equationRepository,
                referenceEv = referenceEv
        ) ?: 0.0
        val matiereSeche = 100.0 - humidite
        if (matiereSeche > 0) {
            energieTotale / matiereSeche * 100.0 // Énergie pour 100g de matière sèche
        } else {
            energieTotale
        }
    } else {
        energieTotale // Énergie pour 100g d'aliment total
    }
}

/**
 * Pourcentages d'énergie apportés par les protéines et les lipides, rapportés à la somme
 * protéines + lipides + ENA (3.5 / 8.5 / 3.5 kcal/g) — même calcul que l'analyse graphique des
 * rations (calculerPourcentagesEnergieRation) et que le camembert "Origine Énergie".
 * On ne divise PAS par l'énergie de l'équation du référentiel : celle-ci peut différer de la somme
 * des macros (fibres, coefficients autres), ce qui décalait les points par rapport aux rations.
 */
suspend fun calculerPourcentagesEnergieAliment(
        aliment: AlimentEv,
        equationRepository: EquationRepository?,
        referenceEv: ReferenceEv?
): Pair<Double, Double> {
    val alimentRation = AlimentRation(aliment = aliment, quantite = 100.0, weight = 1.0)

    suspend fun valeur(nutrient: NutrientMain): Double =
            alimentRation.getNutrientWithComplementary(
                    nutrient = nutrient,
                    equationRepository = equationRepository,
                    referenceEv = referenceEv
            )
                    ?: 0.0

    val energieProteines = valeur(NutrientMain.PROTEINE) * 3.5
    val energieLipides = valeur(NutrientMain.LIPIDE) * 8.5
    val energieEna = valeur(NutrientMain.ENA) * 3.5
    val energieTotaleMacro = energieProteines + energieLipides + energieEna

    if (energieTotaleMacro <= 0) return 0.0 to 0.0

    return (energieProteines / energieTotaleMacro) * 100.0 to
            (energieLipides / energieTotaleMacro) * 100.0
}

/** Liste des nutriments disponibles pour les graphiques personnalisés */
data class NutrimentOption(
        val key: String,
        val displayName: String,
        val unit: String = "g/1000 kcal"
)

/** Récupère la valeur d'un nutriment depuis AlimentAnalyseData */
suspend fun AlimentAnalyseData.getNutrimentValue(
    key: String?,
    referenceEv: ReferenceEv?,
    equationRepository: EquationRepository?,
    useDryMatterPer100g: Boolean = false
): Double {
    if (key.isNullOrEmpty()) return 0.0
    
    // Créer un AlimentRation temporaire pour utiliser getNutrientWithComplementary
    val alimentRation = AlimentRation(
        aliment = this.aliment,
        quantite = 100.0, // 100g pour les calculs
        proportion = 100.0,
        weight = 100.0,
        densiteEnergetique = 0.0
    )
    
    val nutrient = when (key) {
        // Nutriments principaux (NutrientMain)
        "humidite" -> NutrientMain.HUMIDITE
        "proteine" -> NutrientMain.PROTEINE
        "lipide" -> NutrientMain.LIPIDE
        "glucide" -> NutrientMain.GLUCIDE
        "ena" -> NutrientMain.ENA
        "fibre" -> NutrientMain.CELLULOSE
        "cellulose" -> NutrientMain.CELLULOSE
        "cendre" -> NutrientMain.CENDRE
        "energie" -> NutrientMain.ENERGIE
        "sucre" -> NutrientMain.SUCRE
        "amidon" -> NutrientMain.AMIDON
        "fibresol" -> NutrientMain.FIBRESOL
        "fibretot" -> NutrientMain.FIBRETOT
        "ndf" -> NutrientMain.NDF
        "adf" -> NutrientMain.ADF
        "dm" -> NutrientMain.DM
        
        // Minéraux (NutrientMacro)
        "calcium" -> NutrientMacro.CAL
        "phosphore" -> NutrientMacro.PHOS
        "magnesium" -> NutrientMacro.MG
        "sodium" -> NutrientMacro.NA
        "potassium" -> NutrientMacro.K
        "chlore" -> NutrientMacro.CHL
        
        // Oligo-éléments (NutrientMin)
        "fer" -> NutrientMin.FE
        "cuivre" -> NutrientMin.CU
        "zinc" -> NutrientMin.ZN
        "manganese" -> NutrientMin.MN
        "iode" -> NutrientMin.I
        "selenium" -> NutrientMin.SE
        
        // Vitamines (NutrientVitam)
        "vitamine_a" -> NutrientVitam.VITA
        "vitamine_c" -> NutrientVitam.VITC
        "vitamine_d" -> NutrientVitam.VITD
        "vitamine_e" -> NutrientVitam.VITE
        "vitamine_k" -> NutrientVitam.VITK
        "vitamine_b1" -> NutrientVitam.VITB1
        "vitamine_b2" -> NutrientVitam.VITB2
        "vitamine_b3" -> NutrientVitam.VITB3
        "vitamine_b5" -> NutrientVitam.VITB5
        "vitamine_b6" -> NutrientVitam.VITB6
        "vitamine_b8" -> NutrientVitam.VITB8
        "vitamine_b9" -> NutrientVitam.VITB9
        "vitamine_b12" -> NutrientVitam.VITB12
        "choline" -> NutrientVitam.CHOLINE
        "retinol" -> NutrientVitam.RETINOL
        "betacarotene" -> NutrientVitam.BETACAR
        
        // Acides gras (NutrientLipid)
        "agsature" -> NutrientLipid.AGSATURE
        "agmono" -> NutrientLipid.AGMONO
        "agpoly" -> NutrientLipid.AGPOLY
        "ag40" -> NutrientLipid.AG40
        "ag60" -> NutrientLipid.AG60
        "ag80" -> NutrientLipid.AG80
        "ag100" -> NutrientLipid.AG100
        "ag120" -> NutrientLipid.AG120
        "ag140" -> NutrientLipid.AG140
        "ag160" -> NutrientLipid.AG160
        "ag180" -> NutrientLipid.AG180
        "ag181" -> NutrientLipid.AG181
        "ag182" -> NutrientLipid.AG182
        "ag183" -> NutrientLipid.AG183
        "ag204" -> NutrientLipid.AG204
        "ag205" -> NutrientLipid.AG205
        "ag226" -> NutrientLipid.AG226
        "cholesterol" -> NutrientLipid.CHOL
        "omega3" -> NutrientLipid.O3
        "omega6" -> NutrientLipid.O6
        "epadha" -> NutrientLipid.EPADHA
        
        // Acides aminés (AAEnum)
        "alanine" -> AAEnum.ALANINE
        "arginine" -> AAEnum.ARGININE
        "asparagine" -> AAEnum.ASPARAGINE
        "asparate" -> AAEnum.ASPARATE
        "cysteine" -> AAEnum.CYSTEINE
        "glutamate" -> AAEnum.GLUTAMATE
        "glutamine" -> AAEnum.GLUTAMINE
        "glycine" -> AAEnum.GLYCINE
        "histidine" -> AAEnum.HISTIDINE
        "isoleucine" -> AAEnum.ISOLEUCINE
        "leucine" -> AAEnum.LEUCINE
        "lysine" -> AAEnum.LYSINE
        "methionine" -> AAEnum.METHIONINE
        "phenylalanine" -> AAEnum.PHENYLALANINE
        "proline" -> AAEnum.PROLINE
        "pyrrolysine" -> AAEnum.PYRROLYSINE
        "selenocysteine" -> AAEnum.SELENOCYSTEINE
        "serine" -> AAEnum.SERINE
        "threonine" -> AAEnum.THREONINE
        "tryptophane" -> AAEnum.TRYPTOPHANE
        "tyrosine" -> AAEnum.TYROSINE
        "valine" -> AAEnum.VALINE
        
        // Autres nutriments (NutrientOther)
        "taurine" -> NutrientOther.TAURINE
        "carnitine" -> NutrientOther.CARNITINE
        "fos" -> NutrientOther.FOS
        "mos" -> NutrientOther.MOS
        "saccharose" -> NutrientOther.SUCR
        "fructose" -> NutrientOther.FRUCT
        "lactose" -> NutrientOther.LACT
        "maltose" -> NutrientOther.MALT
        "acide_oxalique" -> NutrientOther.AcOx
        "galactose" -> NutrientOther.GAL
        "glucose" -> NutrientOther.GLUCOSE
        "dextrose" -> NutrientOther.DEXTROSE
        
        else -> return 0.0
    }
    val baseValue = if (rationActual != null) aliment.getNutrient(nutrient) ?: 0.0
        else alimentRation.getNutrientWithComplementary(nutrient, equationRepository, referenceEv) ?: 0.0
    
    // Convertir selon le mode d'affichage
    return if (useDryMatterPer100g) {
        // Mode /100g MS : convertir de g/100g as fed vers g/100g MS
        // Obtenir l'humidité pour calculer la matière sèche
        val humidite = if (rationActual != null) aliment.getNutrient(NutrientMain.HUMIDITE) ?: 0.0
            else alimentRation.getNutrientWithComplementary(NutrientMain.HUMIDITE, equationRepository, referenceEv) ?: 0.0
        val matiereSeche = 100.0 - humidite
        if (matiereSeche > 0) {
            // Convertir : valeur_MS = (valeur_as_fed * 100) / matière_sèche
            (baseValue * 100.0) / matiereSeche
        } else {
            baseValue // Fallback si pas de matière sèche
        }
    } else {
        // Mode /1000 kcal : convertir en valeur par 1000 kcal
        if ((energyAsFed ?: densiteEnergetique) > 0) {
            (baseValue * 1000.0) / (energyAsFed ?: densiteEnergetique)
        } else {
            0.0
        }
    }
}
