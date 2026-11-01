package fr.vetbrain.vetnutri_mp.View

import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import fr.vetbrain.vetnutri_mp.Utils.AppLog
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import kotlin.math.pow
import kotlin.math.round
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Data.nomTraduitNutriment
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import io.github.koalaplot.core.*
import io.github.koalaplot.core.xygraph.*

// Données et calculs de l'analyse graphique des aliments : densité, énergie, valeurs de nutriments, zoom

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
internal suspend fun calculerDensiteEnergetiqueAsync(
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
        } catch (e: Exception) { e.rethrowIfCancellation(); AppLog.d("AnalyseGraphiqueAlimentsView", "Exception ignorée : ${e.message}") }
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
internal suspend fun calculerPourcentagesEnergieAlimentAsync(
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
        val libelle: String,
        val unit: String = "g/1000 kcal"
) {
    /** Nom dans la langue de l'interface ; le libellé français sert de repli. */
    val displayName: String
        get() = if (key.isEmpty()) translate("graph.nutrimentNone")
                else nutrimentDeCle(key)?.let { nomTraduitNutriment(it) } ?: libelle
}

/** Récupère la valeur d'un nutriment depuis AlimentAnalyseData */
internal suspend fun AlimentAnalyseData.getNutrimentValue(
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
    
    val nutrient = nutrimentDeCle(key) ?: return 0.0
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

/** Nutriment correspondant à la clé d'une option de graphique (null : aucune ou inconnue). */
internal fun nutrimentDeCle(key: String): Nutrient? = when (key) {
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
        
    else -> null
}

/** Options de nutriments disponibles - Tous les nutriments sauf NutrientAnalysis */
internal val NUTRIMENT_OPTIONS =
        listOf(
                NutrimentOption("", "Aucun"), // Option "aucun" pour l'axe Y
                
                // Nutriments principaux (NutrientMain)
                NutrimentOption("humidite", "Humidité", "g"),
                NutrimentOption("proteine", "Protéines", "g"),
                NutrimentOption("lipide", "Lipides", "g"),
                NutrimentOption("glucide", "Glucides", "g"),
                NutrimentOption("ena", "ENA", "g"),
                NutrimentOption("cellulose", "Cellulose brute", "g"),
                NutrimentOption("cendre", "Cendres", "g"),
                NutrimentOption("energie", "Énergie", "kcal"),
                NutrimentOption("sucre", "Sucres", "g"),
                NutrimentOption("amidon", "Amidon", "g"),
                NutrimentOption("fibresol", "Fibre soluble", "g"),
                NutrimentOption("fibretot", "Fibre totale", "g"),
                NutrimentOption("ndf", "NDF", "g"),
                NutrimentOption("adf", "ADF", "g"),
                NutrimentOption("dm", "Matière sèche", "g"),
                
                // Minéraux (NutrientMacro)
                NutrimentOption("calcium", "Calcium", "g"),
                NutrimentOption("phosphore", "Phosphore", "g"),
                NutrimentOption("magnesium", "Magnésium", "g"),
                NutrimentOption("sodium", "Sodium", "g"),
                NutrimentOption("potassium", "Potassium", "g"),
                NutrimentOption("chlore", "Chlore", "g"),
                
                // Oligo-éléments (NutrientMin)
                NutrimentOption("fer", "Fer", "mg"),
                NutrimentOption("cuivre", "Cuivre", "mg"),
                NutrimentOption("zinc", "Zinc", "mg"),
                NutrimentOption("manganese", "Manganèse", "mg"),
                NutrimentOption("iode", "Iode", "µg"),
                NutrimentOption("selenium", "Sélénium", "µg"),
                
                // Vitamines (NutrientVitam)
                NutrimentOption("vitamine_a", "Vitamine A", "UI"),
                NutrimentOption("vitamine_c", "Vitamine C", "mg"),
                NutrimentOption("vitamine_d", "Vitamine D", "UI"),
                NutrimentOption("vitamine_e", "Vitamine E", "UI"),
                NutrimentOption("vitamine_k", "Vitamine K", "mg"),
                NutrimentOption("vitamine_b1", "Thiamine (B1)", "mg"),
                NutrimentOption("vitamine_b2", "Riboflavine (B2)", "mg"),
                NutrimentOption("vitamine_b3", "Niacine (B3)", "mg"),
                NutrimentOption("vitamine_b5", "Acide pantothénique (B5)", "mg"),
                NutrimentOption("vitamine_b6", "Pyridoxine (B6)", "mg"),
                NutrimentOption("vitamine_b8", "Biotine (B8)", "µg"),
                NutrimentOption("vitamine_b9", "Acide folique (B9)", "µg"),
                NutrimentOption("vitamine_b12", "Cyanocobalamine (B12)", "µg"),
                NutrimentOption("choline", "Choline", "mg"),
                NutrimentOption("retinol", "Rétinol", "µg"),
                NutrimentOption("betacarotene", "Bêta-carotène", "µg"),
                
                // Acides gras (NutrientLipid)
                NutrimentOption("agsature", "Acides gras saturés", "g"),
                NutrimentOption("agmono", "Acides gras mono-insaturés", "g"),
                NutrimentOption("agpoly", "Acides gras poly-insaturés", "g"),
                NutrimentOption("ag40", "C4:0", "g"),
                NutrimentOption("ag60", "C6:0", "g"),
                NutrimentOption("ag80", "C8:0", "g"),
                NutrimentOption("ag100", "C10:0", "g"),
                NutrimentOption("ag120", "C12:0", "g"),
                NutrimentOption("ag140", "C14:0", "g"),
                NutrimentOption("ag160", "C16:0", "g"),
                NutrimentOption("ag180", "C18:0", "g"),
                NutrimentOption("ag181", "C18:1-n9", "g"),
                NutrimentOption("ag182", "C18:2-n6", "g"),
                NutrimentOption("ag183", "C18:3-n3", "g"),
                NutrimentOption("ag204", "C20:4-n6", "g"),
                NutrimentOption("ag205", "EPA", "g"),
                NutrimentOption("ag226", "DHA", "g"),
                NutrimentOption("cholesterol", "Cholestérol", "g"),
                NutrimentOption("omega3", "Oméga 3", "g"),
                NutrimentOption("omega6", "Oméga 6", "g"),
                NutrimentOption("epadha", "EPA et DHA", "g"),
                
                // Acides aminés (AAEnum)
                NutrimentOption("alanine", "Alanine", "g"),
                NutrimentOption("arginine", "Arginine", "g"),
                NutrimentOption("asparagine", "Asparagine", "g"),
                NutrimentOption("asparate", "Asparate", "g"),
                NutrimentOption("cysteine", "Cystéine", "g"),
                NutrimentOption("glutamate", "Glutamate", "g"),
                NutrimentOption("glutamine", "Glutamine", "g"),
                NutrimentOption("glycine", "Glycine", "g"),
                NutrimentOption("histidine", "Histidine", "g"),
                NutrimentOption("isoleucine", "Isoleucine", "g"),
                NutrimentOption("leucine", "Leucine", "g"),
                NutrimentOption("lysine", "Lysine", "g"),
                NutrimentOption("methionine", "Méthionine", "g"),
                NutrimentOption("phenylalanine", "Phénylalanine", "g"),
                NutrimentOption("proline", "Proline", "g"),
                NutrimentOption("pyrrolysine", "Pyrrolysine", "g"),
                NutrimentOption("selenocysteine", "Sélénocystéine", "g"),
                NutrimentOption("serine", "Sérine", "g"),
                NutrimentOption("threonine", "Thréonine", "g"),
                NutrimentOption("tryptophane", "Tryptophane", "g"),
                NutrimentOption("tyrosine", "Tyrosine", "g"),
                NutrimentOption("valine", "Valine", "g"),
                
                // Autres nutriments (NutrientOther)
                NutrimentOption("taurine", "Taurine", "g"),
                NutrimentOption("carnitine", "L-Carnitine", "mg"),
                NutrimentOption("fos", "FOS", "g"),
                NutrimentOption("mos", "MOS", "g"),
                NutrimentOption("saccharose", "Saccharose", "g"),
                NutrimentOption("fructose", "Fructose", "g"),
                NutrimentOption("lactose", "Lactose", "g"),
                NutrimentOption("maltose", "Maltose", "g"),
                NutrimentOption("acide_oxalique", "Acide Oxalique", "mg"),
                NutrimentOption("galactose", "Galactose", "g"),
                NutrimentOption("glucose", "Glucose", "g"),
                NutrimentOption("dextrose", "Dextrose", "g")
        )

/**
 * État pour gérer le zoom et le pan d'un graphique
 */
data class ZoomPanState(
        val scaleX: Float = 1f,
        val scaleY: Float = 1f,
        val panX: Float = 0f,
        val panY: Float = 0f
) {
    fun reset(): ZoomPanState = ZoomPanState()
}

/**
 * Arrondit une valeur float à un nombre raisonnable de décimales pour l'affichage
 */
private fun arrondirPourAffichage(value: Float, maxDecimals: Int = 2): Float {
    val multiplier = 10.0.pow(maxDecimals.toDouble()).toFloat()
    return (round(value * multiplier) / multiplier)
}

/**
 * Arrondit une plage pour éviter les problèmes d'arrondi dans les labels d'axes
 */
internal fun arrondirPlage(range: ClosedFloatingPointRange<Float>, maxDecimals: Int = 2): ClosedFloatingPointRange<Float> {
    val start = arrondirPourAffichage(range.start, maxDecimals)
    val endInclusive = arrondirPourAffichage(range.endInclusive, maxDecimals)
    return start..endInclusive
}

/**
 * Calcule les nouvelles plages d'axes en fonction du zoom et du pan
 */
internal fun calculateZoomedRange(
        originalRange: ClosedFloatingPointRange<Float>,
        zoomPanState: ZoomPanState,
        isXAxis: Boolean = true
): ClosedFloatingPointRange<Float> {
    val scale = if (isXAxis) zoomPanState.scaleX else zoomPanState.scaleY
    val pan = if (isXAxis) zoomPanState.panX else zoomPanState.panY
    
    val originalSize = originalRange.endInclusive - originalRange.start
    val newSize = originalSize / scale
    
    // Utiliser le centre de la plage originale comme point de référence
    val center = (originalRange.start + originalRange.endInclusive) / 2f
    
    // Appliquer le pan (en unités de données)
    val panOffset = pan
    
    val newStart = center - newSize / 2f + panOffset
    val newEnd = center + newSize / 2f + panOffset
    
    // Permettre le zoom au-delà des limites originales et arrondir pour éviter les problèmes d'arrondi
    return arrondirPlage(newStart..newEnd)
}

/**
 * Calcule une plage adaptative pour les axes Y des histogrammes basée sur la distribution des
 * données - Version robuste et flexible
 */
internal fun calculateAdaptiveRange(
        values: List<Float>,
        paddingPercent: Float = 0.08f
): ClosedFloatingPointRange<Float> {
    if (values.isEmpty()) return 0f..1f

    val minValue = values.minOf { it }
    val maxValue = values.maxOf { it }

    // Valeurs de sécurité pour éviter tout problème
    val safeMinValue = maxOf(0f, minValue) // Pas de valeurs négatives
    val safeMaxValue = maxOf(safeMinValue + 0.1f, maxValue) // Au moins une petite plage

    // Calcul de la plage de données
    val dataRange = safeMaxValue - safeMinValue

    // Padding adaptatif basé sur la plage des données
    val adaptivePadding =
            when {
                dataRange == 0f -> maxOf(safeMaxValue * 0.2f, 1f) // Valeurs identiques
                dataRange < 10f -> dataRange * 0.3f // Petite plage
                dataRange < 100f -> dataRange * 0.15f // Plage moyenne
                else -> dataRange * 0.1f // Grande plage
            }

    // Calcul des bornes avec padding adaptatif
    val lowerBound = maxOf(0f, safeMinValue - adaptivePadding)
    val upperBound = safeMaxValue + adaptivePadding

    // Garantie finale : plage d'au moins 0.1f et au plus raisonnable
    val finalLowerBound = lowerBound
    val finalUpperBound = maxOf(upperBound, lowerBound + 0.1f)

    return finalLowerBound..finalUpperBound
}
