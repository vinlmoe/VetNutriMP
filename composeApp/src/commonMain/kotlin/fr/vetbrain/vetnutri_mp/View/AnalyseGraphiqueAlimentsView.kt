package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.GraphFormattingUtils
import io.github.koalaplot.core.*
import io.github.koalaplot.core.bar.DefaultVerticalBar
import io.github.koalaplot.core.bar.VerticalBarPlot
import io.github.koalaplot.core.line.LinePlot
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.*
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.Point
import io.github.koalaplot.core.xygraph.XYGraph
import kotlinx.coroutines.runBlocking

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
        val eauPer1000Kcal: Double = 0.0
)

/** Calcule la densité énergétique d'un aliment de manière asynchrone */
private suspend fun calculerDensiteEnergetiqueAsync(
        aliment: AlimentEv,
        referenceEv: ReferenceEv?,
        equationRepository: EquationRepository?,
        preferencesEspece: fr.vetbrain.vetnutri_mp.Data.PreferencesEspece?,
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
                            preferences = preferencesEspece,
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
                    preferences = preferencesEspece,
                    equationRepository = equationRepository,
                    referenceEv = referenceEv
            )
                    ?: 0.0

    val _lipides =
            alimentRation.getNutrientWithComplementary(
                    nutrient = NutrientMain.LIPIDE,
                    preferences = preferencesEspece,
                    equationRepository = equationRepository,
                    referenceEv = referenceEv
            )
                    ?: 0.0

    val _glucides =
            alimentRation.getNutrientWithComplementary(
                    nutrient = NutrientMain.GLUCIDE,
                    preferences = preferencesEspece,
                    equationRepository = equationRepository,
                    referenceEv = referenceEv
            )
                    ?: 0.0

    // Coefficients énergétiques (kcal/g)
    val kcalProteines = _proteines * 3.5
    val kcalLipides = _lipides * 8.5
    val kcalGlucides = _glucides * 3.5

    val energieTotale = kcalProteines + kcalLipides + kcalGlucides
    
    // Si on veut la densité par matière sèche, on doit diviser par le pourcentage de matière sèche
    return if (useDryMatterPer100g) {
        val humidite = alimentRation.getNutrientWithComplementary(
                nutrient = NutrientMain.HUMIDITE,
                preferences = preferencesEspece,
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

/** Calcule le pourcentage d'énergie apporté par les protéines de manière asynchrone */
private suspend fun calculerPourcentageEnergieProteinesAsync(
        aliment: AlimentEv,
        densiteEnergetique: Double,
        equationRepository: EquationRepository?,
        preferencesEspece: fr.vetbrain.vetnutri_mp.Data.PreferencesEspece?
): Double {
    if (densiteEnergetique <= 0) return 0.0

    // ✅ UTILISER LA MÊME APPROCHE QUE RATIONSVIEW : AlimentRation transitoire
    val alimentRation = AlimentRation(aliment = aliment, quantite = 100.0, weight = 1.0)

    val _proteines =
            alimentRation.getNutrientWithComplementary(
                    nutrient = NutrientMain.PROTEINE,
                    preferences = preferencesEspece,
                    equationRepository = equationRepository,
                    referenceEv = null
            )
                    ?: 0.0
    val energieProteines = _proteines * 3.5

    return (energieProteines / densiteEnergetique) * 100.0
}

/** Calcule le pourcentage d'énergie apporté par les lipides de manière asynchrone */
private suspend fun calculerPourcentageEnergieLipidesAsync(
        aliment: AlimentEv,
        densiteEnergetique: Double,
        equationRepository: EquationRepository?,
        preferencesEspece: fr.vetbrain.vetnutri_mp.Data.PreferencesEspece?
): Double {
    if (densiteEnergetique <= 0) return 0.0

    // ✅ UTILISER LA MÊME APPROCHE QUE RATIONSVIEW : AlimentRation transitoire
    val alimentRation = AlimentRation(aliment = aliment, quantite = 100.0, weight = 1.0)

    val _lipides =
            alimentRation.getNutrientWithComplementary(
                    nutrient = NutrientMain.LIPIDE,
                    preferences = preferencesEspece,
                    equationRepository = equationRepository,
                    referenceEv = null
            )
                    ?: 0.0
    val energieLipides = _lipides * 8.5

    return (energieLipides / densiteEnergetique) * 100.0
}

/** Liste des nutriments disponibles pour les graphiques personnalisés */
data class NutrimentOption(
        val key: String,
        val displayName: String,
        val unit: String = "g/1000 kcal"
)

/** Options de nutriments disponibles - Tous les nutriments sauf NutrientAnalysis */
private val NUTRIMENT_OPTIONS =
        listOf(
                NutrimentOption("", "Aucun"), // Option "aucun" pour l'axe Y
                
                // Nutriments principaux (NutrientMain)
                NutrimentOption("humidite", "Humidité", "g"),
                NutrimentOption("proteine", "Protéines", "g"),
                NutrimentOption("lipide", "Lipides", "g"),
                NutrimentOption("glucide", "Glucides", "g"),
                NutrimentOption("ena", "ENA", "g"),
                NutrimentOption("fibre", "Fibres brutes", "g"),
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

/** Récupère la valeur d'un nutriment depuis AlimentAnalyseData */
private suspend fun AlimentAnalyseData.getNutrimentValue(
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
    
    val baseValue = when (key) {
        // Nutriments principaux (NutrientMain)
        "humidite" -> alimentRation.getNutrientWithComplementary(NutrientMain.HUMIDITE, null, equationRepository, referenceEv) ?: 0.0
        "proteine" -> alimentRation.getNutrientWithComplementary(NutrientMain.PROTEINE, null, equationRepository, referenceEv) ?: 0.0
        "lipide" -> alimentRation.getNutrientWithComplementary(NutrientMain.LIPIDE, null, equationRepository, referenceEv) ?: 0.0
        "glucide" -> alimentRation.getNutrientWithComplementary(NutrientMain.GLUCIDE, null, equationRepository, referenceEv) ?: 0.0
        "ena" -> alimentRation.getNutrientWithComplementary(NutrientMain.ENA, null, equationRepository, referenceEv) ?: 0.0
        "fibre" -> alimentRation.getNutrientWithComplementary(NutrientMain.FIBRE, null, equationRepository, referenceEv) ?: 0.0
        "cellulose" -> alimentRation.getNutrientWithComplementary(NutrientMain.CELLULOSE, null, equationRepository, referenceEv) ?: 0.0
        "cendre" -> alimentRation.getNutrientWithComplementary(NutrientMain.CENDRE, null, equationRepository, referenceEv) ?: 0.0
        "energie" -> alimentRation.getNutrientWithComplementary(NutrientMain.ENERGIE, null, equationRepository, referenceEv) ?: 0.0
        "sucre" -> alimentRation.getNutrientWithComplementary(NutrientMain.SUCRE, null, equationRepository, referenceEv) ?: 0.0
        "amidon" -> alimentRation.getNutrientWithComplementary(NutrientMain.AMIDON, null, equationRepository, referenceEv) ?: 0.0
        "fibresol" -> alimentRation.getNutrientWithComplementary(NutrientMain.FIBRESOL, null, equationRepository, referenceEv) ?: 0.0
        "fibretot" -> alimentRation.getNutrientWithComplementary(NutrientMain.FIBRETOT, null, equationRepository, referenceEv) ?: 0.0
        "ndf" -> alimentRation.getNutrientWithComplementary(NutrientMain.NDF, null, equationRepository, referenceEv) ?: 0.0
        "adf" -> alimentRation.getNutrientWithComplementary(NutrientMain.ADF, null, equationRepository, referenceEv) ?: 0.0
        "dm" -> alimentRation.getNutrientWithComplementary(NutrientMain.DM, null, equationRepository, referenceEv) ?: 0.0
        
        // Minéraux (NutrientMacro)
        "calcium" -> alimentRation.getNutrientWithComplementary(NutrientMacro.CAL, null, equationRepository, referenceEv) ?: 0.0
        "phosphore" -> alimentRation.getNutrientWithComplementary(NutrientMacro.PHOS, null, equationRepository, referenceEv) ?: 0.0
        "magnesium" -> alimentRation.getNutrientWithComplementary(NutrientMacro.MG, null, equationRepository, referenceEv) ?: 0.0
        "sodium" -> alimentRation.getNutrientWithComplementary(NutrientMacro.NA, null, equationRepository, referenceEv) ?: 0.0
        "potassium" -> alimentRation.getNutrientWithComplementary(NutrientMacro.K, null, equationRepository, referenceEv) ?: 0.0
        "chlore" -> alimentRation.getNutrientWithComplementary(NutrientMacro.CHL, null, equationRepository, referenceEv) ?: 0.0
        
        // Oligo-éléments (NutrientMin)
        "fer" -> alimentRation.getNutrientWithComplementary(NutrientMin.FE, null, equationRepository, referenceEv) ?: 0.0
        "cuivre" -> alimentRation.getNutrientWithComplementary(NutrientMin.CU, null, equationRepository, referenceEv) ?: 0.0
        "zinc" -> alimentRation.getNutrientWithComplementary(NutrientMin.ZN, null, equationRepository, referenceEv) ?: 0.0
        "manganese" -> alimentRation.getNutrientWithComplementary(NutrientMin.MN, null, equationRepository, referenceEv) ?: 0.0
        "iode" -> alimentRation.getNutrientWithComplementary(NutrientMin.I, null, equationRepository, referenceEv) ?: 0.0
        "selenium" -> alimentRation.getNutrientWithComplementary(NutrientMin.SE, null, equationRepository, referenceEv) ?: 0.0
        
        // Vitamines (NutrientVitam)
        "vitamine_a" -> alimentRation.getNutrientWithComplementary(NutrientVitam.VITA, null, equationRepository, referenceEv) ?: 0.0
        "vitamine_c" -> alimentRation.getNutrientWithComplementary(NutrientVitam.VITC, null, equationRepository, referenceEv) ?: 0.0
        "vitamine_d" -> alimentRation.getNutrientWithComplementary(NutrientVitam.VITD, null, equationRepository, referenceEv) ?: 0.0
        "vitamine_e" -> alimentRation.getNutrientWithComplementary(NutrientVitam.VITE, null, equationRepository, referenceEv) ?: 0.0
        "vitamine_k" -> alimentRation.getNutrientWithComplementary(NutrientVitam.VITK, null, equationRepository, referenceEv) ?: 0.0
        "vitamine_b1" -> alimentRation.getNutrientWithComplementary(NutrientVitam.VITB1, null, equationRepository, referenceEv) ?: 0.0
        "vitamine_b2" -> alimentRation.getNutrientWithComplementary(NutrientVitam.VITB2, null, equationRepository, referenceEv) ?: 0.0
        "vitamine_b3" -> alimentRation.getNutrientWithComplementary(NutrientVitam.VITB3, null, equationRepository, referenceEv) ?: 0.0
        "vitamine_b5" -> alimentRation.getNutrientWithComplementary(NutrientVitam.VITB5, null, equationRepository, referenceEv) ?: 0.0
        "vitamine_b6" -> alimentRation.getNutrientWithComplementary(NutrientVitam.VITB6, null, equationRepository, referenceEv) ?: 0.0
        "vitamine_b8" -> alimentRation.getNutrientWithComplementary(NutrientVitam.VITB8, null, equationRepository, referenceEv) ?: 0.0
        "vitamine_b9" -> alimentRation.getNutrientWithComplementary(NutrientVitam.VITB9, null, equationRepository, referenceEv) ?: 0.0
        "vitamine_b12" -> alimentRation.getNutrientWithComplementary(NutrientVitam.VITB12, null, equationRepository, referenceEv) ?: 0.0
        "choline" -> alimentRation.getNutrientWithComplementary(NutrientVitam.CHOLINE, null, equationRepository, referenceEv) ?: 0.0
        "retinol" -> alimentRation.getNutrientWithComplementary(NutrientVitam.RETINOL, null, equationRepository, referenceEv) ?: 0.0
        "betacarotene" -> alimentRation.getNutrientWithComplementary(NutrientVitam.BETACAR, null, equationRepository, referenceEv) ?: 0.0
        
        // Acides gras (NutrientLipid)
        "agsature" -> alimentRation.getNutrientWithComplementary(NutrientLipid.AGSATURE, null, equationRepository, referenceEv) ?: 0.0
        "agmono" -> alimentRation.getNutrientWithComplementary(NutrientLipid.AGMONO, null, equationRepository, referenceEv) ?: 0.0
        "agpoly" -> alimentRation.getNutrientWithComplementary(NutrientLipid.AGPOLY, null, equationRepository, referenceEv) ?: 0.0
        "ag40" -> alimentRation.getNutrientWithComplementary(NutrientLipid.AG40, null, equationRepository, referenceEv) ?: 0.0
        "ag60" -> alimentRation.getNutrientWithComplementary(NutrientLipid.AG60, null, equationRepository, referenceEv) ?: 0.0
        "ag80" -> alimentRation.getNutrientWithComplementary(NutrientLipid.AG80, null, equationRepository, referenceEv) ?: 0.0
        "ag100" -> alimentRation.getNutrientWithComplementary(NutrientLipid.AG100, null, equationRepository, referenceEv) ?: 0.0
        "ag120" -> alimentRation.getNutrientWithComplementary(NutrientLipid.AG120, null, equationRepository, referenceEv) ?: 0.0
        "ag140" -> alimentRation.getNutrientWithComplementary(NutrientLipid.AG140, null, equationRepository, referenceEv) ?: 0.0
        "ag160" -> alimentRation.getNutrientWithComplementary(NutrientLipid.AG160, null, equationRepository, referenceEv) ?: 0.0
        "ag180" -> alimentRation.getNutrientWithComplementary(NutrientLipid.AG180, null, equationRepository, referenceEv) ?: 0.0
        "ag181" -> alimentRation.getNutrientWithComplementary(NutrientLipid.AG181, null, equationRepository, referenceEv) ?: 0.0
        "ag182" -> alimentRation.getNutrientWithComplementary(NutrientLipid.AG182, null, equationRepository, referenceEv) ?: 0.0
        "ag183" -> alimentRation.getNutrientWithComplementary(NutrientLipid.AG183, null, equationRepository, referenceEv) ?: 0.0
        "ag204" -> alimentRation.getNutrientWithComplementary(NutrientLipid.AG204, null, equationRepository, referenceEv) ?: 0.0
        "ag205" -> alimentRation.getNutrientWithComplementary(NutrientLipid.AG205, null, equationRepository, referenceEv) ?: 0.0
        "ag226" -> alimentRation.getNutrientWithComplementary(NutrientLipid.AG226, null, equationRepository, referenceEv) ?: 0.0
        "cholesterol" -> alimentRation.getNutrientWithComplementary(NutrientLipid.CHOL, null, equationRepository, referenceEv) ?: 0.0
        "omega3" -> alimentRation.getNutrientWithComplementary(NutrientLipid.O3, null, equationRepository, referenceEv) ?: 0.0
        "omega6" -> alimentRation.getNutrientWithComplementary(NutrientLipid.O6, null, equationRepository, referenceEv) ?: 0.0
        "epadha" -> alimentRation.getNutrientWithComplementary(NutrientLipid.EPADHA, null, equationRepository, referenceEv) ?: 0.0
        
        // Acides aminés (AAEnum)
        "alanine" -> alimentRation.getNutrientWithComplementary(AAEnum.ALANINE, null, equationRepository, referenceEv) ?: 0.0
        "arginine" -> alimentRation.getNutrientWithComplementary(AAEnum.ARGININE, null, equationRepository, referenceEv) ?: 0.0
        "asparagine" -> alimentRation.getNutrientWithComplementary(AAEnum.ASPARAGINE, null, equationRepository, referenceEv) ?: 0.0
        "asparate" -> alimentRation.getNutrientWithComplementary(AAEnum.ASPARATE, null, equationRepository, referenceEv) ?: 0.0
        "cysteine" -> alimentRation.getNutrientWithComplementary(AAEnum.CYSTEINE, null, equationRepository, referenceEv) ?: 0.0
        "glutamate" -> alimentRation.getNutrientWithComplementary(AAEnum.GLUTAMATE, null, equationRepository, referenceEv) ?: 0.0
        "glutamine" -> alimentRation.getNutrientWithComplementary(AAEnum.GLUTAMINE, null, equationRepository, referenceEv) ?: 0.0
        "glycine" -> alimentRation.getNutrientWithComplementary(AAEnum.GLYCINE, null, equationRepository, referenceEv) ?: 0.0
        "histidine" -> alimentRation.getNutrientWithComplementary(AAEnum.HISTIDINE, null, equationRepository, referenceEv) ?: 0.0
        "isoleucine" -> alimentRation.getNutrientWithComplementary(AAEnum.ISOLEUCINE, null, equationRepository, referenceEv) ?: 0.0
        "leucine" -> alimentRation.getNutrientWithComplementary(AAEnum.LEUCINE, null, equationRepository, referenceEv) ?: 0.0
        "lysine" -> alimentRation.getNutrientWithComplementary(AAEnum.LYSINE, null, equationRepository, referenceEv) ?: 0.0
        "methionine" -> alimentRation.getNutrientWithComplementary(AAEnum.METHIONINE, null, equationRepository, referenceEv) ?: 0.0
        "phenylalanine" -> alimentRation.getNutrientWithComplementary(AAEnum.PHENYLALANINE, null, equationRepository, referenceEv) ?: 0.0
        "proline" -> alimentRation.getNutrientWithComplementary(AAEnum.PROLINE, null, equationRepository, referenceEv) ?: 0.0
        "pyrrolysine" -> alimentRation.getNutrientWithComplementary(AAEnum.PYRROLYSINE, null, equationRepository, referenceEv) ?: 0.0
        "selenocysteine" -> alimentRation.getNutrientWithComplementary(AAEnum.SELENOCYSTEINE, null, equationRepository, referenceEv) ?: 0.0
        "serine" -> alimentRation.getNutrientWithComplementary(AAEnum.SERINE, null, equationRepository, referenceEv) ?: 0.0
        "threonine" -> alimentRation.getNutrientWithComplementary(AAEnum.THREONINE, null, equationRepository, referenceEv) ?: 0.0
        "tryptophane" -> alimentRation.getNutrientWithComplementary(AAEnum.TRYPTOPHANE, null, equationRepository, referenceEv) ?: 0.0
        "tyrosine" -> alimentRation.getNutrientWithComplementary(AAEnum.TYROSINE, null, equationRepository, referenceEv) ?: 0.0
        "valine" -> alimentRation.getNutrientWithComplementary(AAEnum.VALINE, null, equationRepository, referenceEv) ?: 0.0
        
        // Autres nutriments (NutrientOther)
        "taurine" -> alimentRation.getNutrientWithComplementary(NutrientOther.TAURINE, null, equationRepository, referenceEv) ?: 0.0
        "carnitine" -> alimentRation.getNutrientWithComplementary(NutrientOther.CARNITINE, null, equationRepository, referenceEv) ?: 0.0
        "fos" -> alimentRation.getNutrientWithComplementary(NutrientOther.FOS, null, equationRepository, referenceEv) ?: 0.0
        "mos" -> alimentRation.getNutrientWithComplementary(NutrientOther.MOS, null, equationRepository, referenceEv) ?: 0.0
        "saccharose" -> alimentRation.getNutrientWithComplementary(NutrientOther.SUCR, null, equationRepository, referenceEv) ?: 0.0
        "fructose" -> alimentRation.getNutrientWithComplementary(NutrientOther.FRUCT, null, equationRepository, referenceEv) ?: 0.0
        "lactose" -> alimentRation.getNutrientWithComplementary(NutrientOther.LACT, null, equationRepository, referenceEv) ?: 0.0
        "maltose" -> alimentRation.getNutrientWithComplementary(NutrientOther.MALT, null, equationRepository, referenceEv) ?: 0.0
        "acide_oxalique" -> alimentRation.getNutrientWithComplementary(NutrientOther.AcOx, null, equationRepository, referenceEv) ?: 0.0
        "galactose" -> alimentRation.getNutrientWithComplementary(NutrientOther.GAL, null, equationRepository, referenceEv) ?: 0.0
        "glucose" -> alimentRation.getNutrientWithComplementary(NutrientOther.GLUCOSE, null, equationRepository, referenceEv) ?: 0.0
        "dextrose" -> alimentRation.getNutrientWithComplementary(NutrientOther.DEXTROSE, null, equationRepository, referenceEv) ?: 0.0
        
        else -> 0.0
    }
    
    // Convertir selon le mode d'affichage
    return if (useDryMatterPer100g) {
        // Mode /100g MS : convertir de g/100g as fed vers g/100g MS
        // Obtenir l'humidité pour calculer la matière sèche
        val humidite = alimentRation.getNutrientWithComplementary(NutrientMain.HUMIDITE, null, equationRepository, referenceEv) ?: 0.0
        val matiereSeche = 100.0 - humidite
        if (matiereSeche > 0) {
            // Convertir : valeur_MS = (valeur_as_fed * 100) / matière_sèche
            (baseValue * 100.0) / matiereSeche
        } else {
            baseValue // Fallback si pas de matière sèche
        }
    } else {
        // Mode /1000 kcal : convertir en valeur par 1000 kcal
        if (densiteEnergetique > 0) {
            (baseValue * 1000.0) / densiteEnergetique
        } else {
            0.0
        }
    }
}

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
 * Calcule les nouvelles plages d'axes en fonction du zoom et du pan
 */
private fun calculateZoomedRange(
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
    
    // Permettre le zoom au-delà des limites originales
    return newStart..newEnd
}

/**
 * Calcule une plage adaptative pour les axes Y des histogrammes basée sur la distribution des
 * données - Version robuste et flexible
 */
private fun calculateAdaptiveRange(
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

/** Sélecteur de nutriment avec dropdown */
@Composable
private fun NutrimentSelector(
        label: String,
        selectedNutriment: String?,
        onNutrimentSelected: (String) -> Unit,
        modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
                text = label,
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Bold,
                color = VetNutriColors.Primary
        )

        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            val selectedOption = NUTRIMENT_OPTIONS.find { it.key == selectedNutriment }
            Text(
                    text = selectedOption?.displayName ?: "Sélectionner...",
                    style = MaterialTheme.typography.body2
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Déplier")
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            NUTRIMENT_OPTIONS.forEach { option ->
                DropdownMenuItem(
                        onClick = {
                            onNutrimentSelected(option.key)
                            expanded = false
                        }
                ) {
                    Text(
                            text = "${option.displayName} (${option.unit})",
                            style = MaterialTheme.typography.body2
                    )
                }
            }
        }
    }
}

/** Vue d'analyse graphique des aliments sélectionnés */
@Composable
fun AnalyseGraphiqueAlimentsView(
        aliments: List<AlimentEv>,
        referenceEv: ReferenceEv?,
        equationRepository: EquationRepository?,
        preferencesEspece: fr.vetbrain.vetnutri_mp.Data.PreferencesEspece? = null,
        viewModel: fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel? = null,
        onClose: () -> Unit,
        modifier: Modifier = Modifier
) {
    // États pour les données d'analyse
    var alimentsAnalyses by remember { mutableStateOf<List<AlimentAnalyseData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // États pour observer les consultations et rations sélectionnées
    val selectedConsultation by viewModel?.selectedConsultation?.collectAsState() ?: remember { mutableStateOf(null) }
    val selectedRation by viewModel?.selectedRation?.collectAsState() ?: remember { mutableStateOf(null) }
    
    // États pour les valeurs métaboliques nécessaires à la HeatMap
    val poidsMetabolique by viewModel?.poidsMetabolique?.collectAsState() ?: remember { mutableStateOf(null) }
    val besoinEnergetiqueStandard by viewModel?.besoinEnergetiqueStandard?.collectAsState() ?: remember { mutableStateOf(null) }
    val besoinEnergetiqueTotal by viewModel?.besoinEnergetiqueTotal?.collectAsState() ?: remember { mutableStateOf(null) }
    
    // Déclencher les calculs métaboliques si nécessaire
    LaunchedEffect(selectedConsultation) {
        selectedConsultation?.let { viewModel?.calculerValeursMetaboliques(it) }
    }

    // CoroutineScope pour les opérations asynchrones
    val coroutineScope = rememberCoroutineScope()

    // État pour l'aliment sélectionné (UUID de l'aliment)
    var alimentSelectionne by remember { mutableStateOf<String?>(null) }

    // État pour l'onglet actif
    var ongletActif by remember { mutableStateOf("densite_energetique") }

    // États pour les sélections de nutriments personnalisés
    var nutrimentX by remember { mutableStateOf<String?>("proteine") }
    var nutrimentY by remember { mutableStateOf<String?>("") } // Par défaut : aucun

    // États pour les toggles d'unités
    var useDryMatterPer100g by remember { mutableStateOf(false) } // Toggle pour /1000 kcal vs /100g MS

    // Utiliser directement la liste des aliments sans filtrage
    val alimentsFiltres = aliments

    // Calculer les données d'analyse pour chaque aliment filtré de manière asynchrone
    LaunchedEffect(alimentsFiltres, referenceEv, equationRepository, useDryMatterPer100g) {
        isLoading = true
        val resultat = mutableListOf<AlimentAnalyseData>()

        for (aliment in alimentsFiltres) {
            try {
                // 🔍 LOG DIAGNOSTIC : Vérifier les valeurs de base de l'aliment

                // ✅ CRÉER UN ALIMENTRATION TRANSITOIRE POUR UTILISER getNutrientWithComplementary
                val alimentRation =
                        AlimentRation(
                                aliment = aliment,
                                quantite = 100.0, // 100g pour les calculs
                                weight = 1.0
                        )

                // Utiliser getNutrientWithComplementary de manière asynchrone
                val _proteines =
                        alimentRation.getNutrientWithComplementary(
                                nutrient = NutrientMain.PROTEINE,
                                preferences = preferencesEspece,
                                equationRepository = equationRepository,
                                referenceEv = referenceEv
                        )
                                ?: 0.0

                val _lipides =
                        alimentRation.getNutrientWithComplementary(
                                nutrient = NutrientMain.LIPIDE,
                                preferences = preferencesEspece,
                                equationRepository = equationRepository,
                                referenceEv = referenceEv
                        )
                                ?: 0.0

                val _glucides =
                        alimentRation.getNutrientWithComplementary(
                                nutrient = NutrientMain.GLUCIDE,
                                preferences = preferencesEspece,
                                equationRepository = equationRepository,
                                referenceEv = referenceEv
                        )
                                ?: 0.0

                // Utiliser getEnergie() qui utilise EquationEvaluator.calculerEnergiePour100g()
                // quand tous les paramètres sont disponibles, garantissant l'utilisation
                // de l'équation énergétique du référentiel
                val _energie =
                        alimentRation.getEnergie(
                                referenceEv = referenceEv,
                                equationRepository = equationRepository
                        )

                // Récupération du phosphore pour le second graphique
                val phosphore =
                        alimentRation.getNutrientWithComplementary(
                                nutrient = NutrientMacro.PHOS,
                                preferences = preferencesEspece,
                                equationRepository = equationRepository,
                                referenceEv = referenceEv
                        )
                                ?: 0.0

                // Récupération du calcium pour le troisième graphique
                val calcium =
                        alimentRation.getNutrientWithComplementary(
                                nutrient = NutrientMacro.CAL,
                                preferences = preferencesEspece,
                                equationRepository = equationRepository,
                                referenceEv = referenceEv
                        )
                                ?: 0.0

                // Récupération des autres nutriments pour le graphique personnalisé
                // Note: Seuls CAL, PHOS, MG, NA, K, CHL sont disponibles dans NutrientMacro
                val magnesium =
                        alimentRation.getNutrientWithComplementary(
                                nutrient = NutrientMacro.MG,
                                preferences = preferencesEspece,
                                equationRepository = equationRepository,
                                referenceEv = referenceEv
                        )
                                ?: 0.0

                val sodium =
                        alimentRation.getNutrientWithComplementary(
                                nutrient = NutrientMacro.NA,
                                preferences = preferencesEspece,
                                equationRepository = equationRepository,
                                referenceEv = referenceEv
                        )
                                ?: 0.0

                val potassium =
                        alimentRation.getNutrientWithComplementary(
                                nutrient = NutrientMacro.K,
                                preferences = preferencesEspece,
                                equationRepository = equationRepository,
                                referenceEv = referenceEv
                        )
                                ?: 0.0

                // Obtenir l'humidité pour la conversion en matière sèche
                val humidite =
                        alimentRation.getNutrientWithComplementary(
                                nutrient = NutrientMain.HUMIDITE,
                                preferences = preferencesEspece,
                                equationRepository = equationRepository,
                                referenceEv = referenceEv
                        )
                                ?: 0.0
                val matiereSeche = 100.0 - humidite

                val densiteEnergetiqueBase =
                        calculerDensiteEnergetiqueAsync(
                                aliment,
                                referenceEv,
                                equationRepository,
                                preferencesEspece,
                                useDryMatterPer100g
                        )
                val densiteEnergetique = densiteEnergetiqueBase
                val pourcentageProteines =
                        calculerPourcentageEnergieProteinesAsync(
                                aliment,
                                densiteEnergetique,
                                equationRepository,
                                preferencesEspece
                        )
                val pourcentageLipides =
                        calculerPourcentageEnergieLipidesAsync(
                                aliment,
                                densiteEnergetique,
                                equationRepository,
                                preferencesEspece
                        )

                // Fonction helper pour convertir en matière sèche si nécessaire
                fun convertirEnMatiereSeche(valeurAsFed: Double): Double {
                        return if (useDryMatterPer100g) {
                                // /100g MS : convertir de g/100g as fed vers g/100g MS
                                if (matiereSeche > 0) {
                                        (valeurAsFed * 100.0) / matiereSeche
                        } else {
                                        valeurAsFed // Fallback si pas de matière sèche
                                }
                        } else {
                                // /1000 kcal : calculer par 1000 kcal
                        if (densiteEnergetique > 0) {
                                        (valeurAsFed * 1000.0) / densiteEnergetique
                        } else {
                            0.0
                                }
                        }
                }

                // Calculs pour le graphique Phosphore/Protéines (par 1000 kcal ou /100g MS)
                val proteinePer1000Kcal = convertirEnMatiereSeche(_proteines)
                val phosphorePer1000Kcal = convertirEnMatiereSeche(phosphore)
                val calciumPer1000Kcal = convertirEnMatiereSeche(calcium)

                // Calculs pour tous les nutriments en g/1000kcal ou /100g MS
                val energiePer1000Kcal = convertirEnMatiereSeche(_energie)
                val lipidePer1000Kcal = convertirEnMatiereSeche(_lipides)
                val glucidePer1000Kcal = convertirEnMatiereSeche(_glucides)
                val magnesiumPer1000Kcal = convertirEnMatiereSeche(magnesium)
                val sodiumPer1000Kcal = convertirEnMatiereSeche(sodium)
                val potassiumPer1000Kcal = convertirEnMatiereSeche(potassium)

                // 🔍 LOG DIAGNOSTIC : Vérifier les valeurs calculées

                resultat.add(
                        AlimentAnalyseData(
                                aliment = aliment,
                                numero = 0, // Numéro temporaire, sera réassigné après le tri
                                densiteEnergetique = densiteEnergetique,
                                pourcentageProteines = pourcentageProteines,
                                pourcentageLipides = pourcentageLipides,
                                phosphorePer1000Kcal = phosphorePer1000Kcal,
                                proteinePer1000Kcal = proteinePer1000Kcal,
                                calciumPer1000Kcal = calciumPer1000Kcal,
                                energiePer1000Kcal = energiePer1000Kcal,
                                lipidePer1000Kcal = lipidePer1000Kcal,
                                glucidePer1000Kcal = glucidePer1000Kcal,
                                fibrePer1000Kcal =
                                        magnesiumPer1000Kcal, // Utilisation de magnesium à la place
                                // de fibre
                                cendrePer1000Kcal =
                                        sodiumPer1000Kcal, // Utilisation de sodium à la place de
                                // cendre
                                eauPer1000Kcal =
                                        potassiumPer1000Kcal // Utilisation de potassium à la place
                                // d'eau
                                )
                )
            } catch (e: Exception) {

                e.printStackTrace()
            }
        }

        // Mettre à jour l'état avec les résultats triés par densité énergétique décroissante
        // et réassigner les numéros dans l'ordre du tri
        alimentsAnalyses =
                resultat.sortedByDescending { it.densiteEnergetique }.mapIndexed { index, data ->
                    data.copy(numero = index + 1)
                }
        isLoading = false
    }

    Column(
            modifier = modifier.fillMaxSize().padding(AppSizes.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
    ) {
        // En-tête avec titre et bouton de fermeture
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour à la sélection d'aliments",
                            tint = VetNutriColors.Primary
                    )
                }
                Column {
                    Text(
                            text = "Analyse graphique de ${aliments.size} aliment(s)",
                            style = MaterialTheme.typography.h5,
                            fontWeight = FontWeight.Bold,
                            color = VetNutriColors.Primary
                    )
                    Text(
                            text = "Visualisation des caractéristiques nutritionnelles",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Onglets pour choisir le type de graphique
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ✨ Onglet Histogramme Densité Énergétique (PREMIER)
            Button(
                    onClick = { ongletActif = "densite_energetique" },
                    colors =
                            ButtonDefaults.buttonColors(
                                    backgroundColor =
                                            if (ongletActif == "densite_energetique")
                                                    VetNutriColors.Primary
                                            else Color.Gray.copy(alpha = 0.3f),
                                    contentColor =
                                            if (ongletActif == "densite_energetique") Color.White
                                            else Color.Black
                            ),
                    modifier = Modifier.weight(1f)
            ) { Text("Densité énergétique") }

            // Onglet Protéines/Lipides
            Button(
                    onClick = { ongletActif = "protein_lipid" },
                    colors =
                            ButtonDefaults.buttonColors(
                                    backgroundColor =
                                            if (ongletActif == "protein_lipid")
                                                    VetNutriColors.Primary
                                            else Color.Gray.copy(alpha = 0.3f),
                                    contentColor =
                                            if (ongletActif == "protein_lipid") Color.White
                                            else Color.Black
                            ),
                    modifier = Modifier.weight(1f)
            ) { Text("Protéines/Lipides (%)") }

            // Onglet Phosphore/Protéines
            Button(
                    onClick = { ongletActif = "phosphore_protein" },
                    colors =
                            ButtonDefaults.buttonColors(
                                    backgroundColor =
                                            if (ongletActif == "phosphore_protein")
                                                    VetNutriColors.Primary
                                            else Color.Gray.copy(alpha = 0.3f),
                                    contentColor =
                                            if (ongletActif == "phosphore_protein") Color.White
                                            else Color.Black
                            ),
                    modifier = Modifier.weight(1f)
            ) { Text("Phosphore/Protéines") }

            // Onglet Calcium/Phosphore
            Button(
                    onClick = { ongletActif = "calcium_phosphore" },
                    colors =
                            ButtonDefaults.buttonColors(
                                    backgroundColor =
                                            if (ongletActif == "calcium_phosphore")
                                                    VetNutriColors.Primary
                                            else Color.Gray.copy(alpha = 0.3f),
                                    contentColor =
                                            if (ongletActif == "calcium_phosphore") Color.White
                                            else Color.Black
                            ),
                    modifier = Modifier.weight(1f)
            ) { Text("Calcium/Phosphore") }

            // ✨ Nouvel onglet Nutriments personnalisés
            Button(
                    onClick = { ongletActif = "nutriments_perso" },
                    colors =
                            ButtonDefaults.buttonColors(
                                    backgroundColor =
                                            if (ongletActif == "nutriments_perso")
                                                    VetNutriColors.Primary
                                            else Color.Gray.copy(alpha = 0.3f),
                                    contentColor =
                                            if (ongletActif == "nutriments_perso") Color.White
                                            else Color.Black
                            ),
                    modifier = Modifier.weight(1f)
            ) { Text("Nutriments\npersonnalisés") }

            // ✨ Nouvel onglet Analyse détaillée
            Button(
                    onClick = { ongletActif = "analyse_detaillee" },
                    colors =
                            ButtonDefaults.buttonColors(
                                    backgroundColor =
                                            if (ongletActif == "analyse_detaillee")
                                                    VetNutriColors.Primary
                                            else Color.Gray.copy(alpha = 0.3f),
                                    contentColor =
                                            if (ongletActif == "analyse_detaillee") Color.White
                                            else Color.Black
                            ),
                    modifier = Modifier.weight(1f)
            ) { Text("Analyse\ndétaillée") }

            // ✨ Nouvel onglet HeatMap
            Button(
                    onClick = { ongletActif = "heatmap" },
                    colors =
                            ButtonDefaults.buttonColors(
                                    backgroundColor =
                                            if (ongletActif == "heatmap")
                                                    VetNutriColors.Primary
                                            else Color.Gray.copy(alpha = 0.3f),
                                    contentColor =
                                            if (ongletActif == "heatmap") Color.White
                                            else Color.Black
                            ),
                    modifier = Modifier.weight(1f)
            ) { Text("HeatMap") }
        }

        // Toggle pour /1000 kcal vs /100g MS (pas pour les ratios énergétiques, l'analyse détaillée et la heatmap)
        if (ongletActif != "protein_lipid" && ongletActif != "analyse_detaillee" && ongletActif != "heatmap") {
        Row(
                modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
        ) {
                Text(
                                text = "/1000 kcal",
                        style = MaterialTheme.typography.caption,
                                color = if (!useDryMatterPer100g) VetNutriColors.Primary else MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                Switch(
                                checked = useDryMatterPer100g,
                                onCheckedChange = { useDryMatterPer100g = it }
                )
                Text(
                                text = "/100g MS",
                        style = MaterialTheme.typography.caption,
                                color = if (useDryMatterPer100g) VetNutriColors.Primary else MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }


        // Vérifier si une référence et des préférences sont disponibles
        val hasReference = referenceEv != null
        val hasPreferences = preferencesEspece != null
        val hasEquationRepository = equationRepository != null

        // Contenu principal - responsive selon la largeur
        if (!hasReference) {
            // Aucune référence disponible
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                    Text(
                            text = "Aucune référence sélectionnée",
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Bold,
                            color = VetNutriColors.Error
                    )
                    Text(
                            text = "Veuillez sélectionner une référence dans une consultation pour calculer l'énergie avec les équations du référentiel",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else if (!hasPreferences) {
            // Aucune préférence disponible
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                    Text(
                            text = "Aucune préférence disponible",
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Bold,
                            color = VetNutriColors.Error
                    )
                    Text(
                            text = "Veuillez configurer les préférences pour l'espèce dans les paramètres",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else if (!hasEquationRepository) {
            // Aucun repository d'équations disponible
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                    Text(
                            text = "Repository d'équations non disponible",
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Bold,
                            color = VetNutriColors.Error
                    )
                    Text(
                            text = "Le repository d'équations est requis pour calculer l'énergie avec les équations du référentiel",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else if (isLoading) {
            // Indicateur de chargement
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                    CircularProgressIndicator(color = VetNutriColors.Primary)
                    Text(
                            text = "Calcul des valeurs nutritionnelles...",
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else {

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isCompact = maxWidth < 800.dp

                if (isCompact) {
                    // Vue compacte : graphiques puis liste avec bouton retour plus visible ET
                    // SCROLLABLE (sauf pour analyse_detaillee et heatmap qui gèrent leur propre scroll)
                    if (ongletActif == "analyse_detaillee") {
                        // ✨ Nouvelle vue détaillée - pas de scroll parent, le LazyColumn gère le scroll
                        AnalyseDetailleeAlimentsView(
                                aliments = aliments,
                                alimentsAnalyses = alimentsAnalyses,
                                referenceEv = referenceEv,
                                equationRepository = equationRepository,
                                preferencesEspece = preferencesEspece,
                                viewModel = viewModel,
                                useDryMatterPer100g = useDryMatterPer100g,
                                alimentSelectionne = alimentSelectionne,
                                onAlimentSelected = { uuid -> alimentSelectionne = uuid },
                                onUseDryMatterPer100gChange = { newValue -> useDryMatterPer100g = newValue },
                                modifier = Modifier.fillMaxSize()
                        )
                    } else if (ongletActif == "heatmap") {
                        // ✨ Vue HeatMap - pas de scroll parent, le LazyColumn gère le scroll
                        HeatMapAlimentsView(
                                aliments = aliments,
                                alimentsAnalyses = alimentsAnalyses,
                                referenceEv = referenceEv,
                                equationRepository = equationRepository,
                                preferencesEspece = preferencesEspece,
                                besoinEnergetiqueEntretien = besoinEnergetiqueStandard,
                                poidsAnimal = selectedConsultation?.weight,
                                poidsMetabolique = poidsMetabolique,
                                modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .verticalScroll(
                                                        rememberScrollState()
                                                ), // ✨ Rendre la vue principale scrollable
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                            // 🔧 Bouton retour plus visible en mode compact

                            // Graphique principal
                            // Graphique principal
                            GraphiqueNuagePoints(
                                    alimentsAnalyses = alimentsAnalyses,
                                    ongletActif = ongletActif,
                                    alimentSelectionne = alimentSelectionne,
                                    nutrimentX = nutrimentX,
                                    nutrimentY = nutrimentY,
                                    useDryMatterPer100g = useDryMatterPer100g,
                                    referenceEv = referenceEv,
                                    equationRepository = equationRepository,
                                    modifier = Modifier.fillMaxWidth()
                            )

                            // ✨ Sélecteurs de nutriments pour l'onglet personnalisé (sous le graphique)
                            if (ongletActif == "nutriments_perso") {
                                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingMedium)
                                ) {
                                    NutrimentSelector(
                                            label = "Axe X",
                                            selectedNutriment = nutrimentX,
                                            onNutrimentSelected = { nutrimentX = it },
                                            modifier = Modifier.weight(1f)
                                    )

                                    NutrimentSelector(
                                            label = "Axe Y",
                                            selectedNutriment = nutrimentY,
                                            onNutrimentSelected = { nutrimentY = it },
                                            modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Liste des aliments (sans LazyColumn en mode compact)
                            ListeAlimentsAnalyse(
                                    alimentsAnalyses = alimentsAnalyses,
                                    alimentSelectionne = alimentSelectionne,
                                    onAlimentSelected = { uuid -> alimentSelectionne = uuid },
                                    isCompactMode = true, // ✨ Mode compact = pas de LazyColumn
                                    modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    // Vue large : côte à côte avec scroll uniquement sur la partie droite
                    Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                    ) {
                        // Colonne gauche : liste des aliments (1/4 de la largeur) - masquée pour l'analyse détaillée et la heatmap
                        if (ongletActif != "analyse_detaillee" && ongletActif != "heatmap") {
                            Column(
                                    modifier = Modifier
                                        .weight(0.25f)
                                        .fillMaxHeight(),
                                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                            ) {
                                ListeAlimentsAnalyse(
                                        alimentsAnalyses = alimentsAnalyses,
                                        alimentSelectionne = alimentSelectionne,
                                        onAlimentSelected = { uuid -> alimentSelectionne = uuid },
                                        isCompactMode = false, // ✨ Mode large = avec LazyColumn
                                        modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Colonne droite : graphiques (3/4 de la largeur) ou analyse détaillée/heatmap (plein écran)
                        if (ongletActif == "analyse_detaillee") {
                            // ✨ Nouvelle vue détaillée - pas de scroll parent, le LazyColumn gère le scroll
                            AnalyseDetailleeAlimentsView(
                                    aliments = aliments,
                                    alimentsAnalyses = alimentsAnalyses,
                                    referenceEv = referenceEv,
                                    equationRepository = equationRepository,
                                    preferencesEspece = preferencesEspece,
                                    viewModel = viewModel,
                                    useDryMatterPer100g = useDryMatterPer100g,
                                    alimentSelectionne = alimentSelectionne,
                                    onAlimentSelected = { uuid -> alimentSelectionne = uuid },
                                    onUseDryMatterPer100gChange = { newValue -> useDryMatterPer100g = newValue },
                                    modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                            )
                        } else if (ongletActif == "heatmap") {
                            // ✨ Vue HeatMap - pas de scroll parent, le LazyColumn gère le scroll
                            HeatMapAlimentsView(
                                    aliments = aliments,
                                    alimentsAnalyses = alimentsAnalyses,
                                    referenceEv = referenceEv,
                                    equationRepository = equationRepository,
                                    preferencesEspece = preferencesEspece,
                                    besoinEnergetiqueEntretien = besoinEnergetiqueStandard,
                                    poidsAnimal = selectedConsultation?.weight,
                                    poidsMetabolique = poidsMetabolique,
                                    modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                            )
                        } else {
                            // Graphiques avec scroll
                            Column(
                                    modifier = Modifier
                                        .weight(0.75f)
                                        .fillMaxHeight()
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                            ) {
                                GraphiqueNuagePoints(
                                        alimentsAnalyses = alimentsAnalyses,
                                        ongletActif = ongletActif,
                                        alimentSelectionne = alimentSelectionne,
                                        nutrimentX = nutrimentX,
                                        nutrimentY = nutrimentY,
                                        useDryMatterPer100g = useDryMatterPer100g,
                                        referenceEv = referenceEv,
                                        equationRepository = equationRepository,
                                        modifier = Modifier.fillMaxWidth()
                                )

                                // ✨ Sélecteurs de nutriments pour l'onglet personnalisé (sous le
                                // graphique)
                                if (ongletActif == "nutriments_perso") {
                                    Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                                    Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement =
                                                    Arrangement.spacedBy(AppSizes.paddingMedium)
                                    ) {
                                        NutrimentSelector(
                                                label = "Axe X",
                                                selectedNutriment = nutrimentX,
                                                onNutrimentSelected = { nutrimentX = it },
                                                modifier = Modifier.weight(1f)
                                        )

                                        NutrimentSelector(
                                                label = "Axe Y",
                                                selectedNutriment = nutrimentY,
                                                onNutrimentSelected = { nutrimentY = it },
                                                modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Boutons flottants pour ajouter des aliments aux rations
                if (viewModel != null) {
                    // Bouton + (ajouter à la ration existante)
                    FloatingActionButton(
                        onClick = {
                            val aliment = aliments.find { it.uuid == alimentSelectionne }
                            if (aliment != null && selectedRation != null && selectedConsultation != null) {
                                // Utiliser la même logique que RationsView.kt pour une mise à jour immédiate
                                coroutineScope.launch {
                                    val alimentComplet = viewModel.getAlimentComplet(aliment.uuid)
                                    if (alimentComplet != null) {
                                        // Créer un nouvel AlimentRation
                                        val newAlimentRation = AlimentRation(
                                            refAlimUnif = alimentComplet.uuid,
                                            quantite = 100.0,
                                            aliment = alimentComplet,
                                            refRation = selectedRation!!.uuid
                                        )

                                        // Créer une copie de la liste des aliments de la ration
                                        val updatedAliments = selectedRation!!.alimentMutableList.toMutableList()
                                        updatedAliments.add(newAlimentRation)

                                        // Créer une ration mise à jour
                                        val updatedRation = selectedRation!!.copy(
                                            alimentMutableList = updatedAliments
                                        )

                                        // Mettre à jour la consultation avec la ration modifiée
                                        val updatedRations = selectedConsultation!!.rations.toMutableList()
                                        val rationIndex = updatedRations.indexOfFirst { it.uuid == selectedRation!!.uuid }
                                        if (rationIndex >= 0) {
                                            updatedRations[rationIndex] = updatedRation

                                            val updatedConsultation = selectedConsultation!!.copy(
                                                rations = updatedRations
                                            )

                                            // Sauvegarder la consultation mise à jour
                                            viewModel.updateConsultation(updatedConsultation)

                                            // Sélectionner la ration mise à jour pour une mise à jour immédiate
                                            viewModel.selectRation(updatedRation)
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .offset(y = (-80).dp),
                        backgroundColor = VetNutriColors.Primary,
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Ajouter à la ration"
                        )
                    }
                    
                    // Bouton ++ (créer nouvelle ration)
                    FloatingActionButton(
                        onClick = {
                            val aliment = aliments.find { it.uuid == alimentSelectionne }
                            if (aliment != null && selectedConsultation != null) {
                                // Utiliser la même logique que RationsView.kt pour une mise à jour immédiate
                                coroutineScope.launch {
                                    val alimentComplet = viewModel.getAlimentComplet(aliment.uuid)
                                    if (alimentComplet != null) {
                                        // Créer une nouvelle ration avec le nom de la marque de l'aliment
                                        val nomRation = alimentComplet.brand ?: alimentComplet.nom ?: "Nouvelle ration"
                                        val nouvelleRation = fr.vetbrain.vetnutri_mp.Data.Ration(
                                            name = nomRation,
                                            actual = false,
                                            alimentMutableList = mutableListOf()
                                        )

                                        // Créer un nouvel AlimentRation pour la nouvelle ration
                                        val newAlimentRation = AlimentRation(
                                            refAlimUnif = alimentComplet.uuid,
                                            quantite = 100.0,
                                            aliment = alimentComplet,
                                            refRation = nouvelleRation.uuid
                                        )

                                        // Ajouter l'aliment à la nouvelle ration
                                        val updatedAliments = nouvelleRation.alimentMutableList.toMutableList()
                                        updatedAliments.add(newAlimentRation)
                                        val rationAvecAliment = nouvelleRation.copy(
                                            alimentMutableList = updatedAliments
                                        )

                                        // Ajouter la ration à la consultation
                                        val updatedRations = selectedConsultation!!.rations.toMutableList()
                                        updatedRations.add(rationAvecAliment)

                                        val updatedConsultation = selectedConsultation!!.copy(
                                            rations = updatedRations
                                        )

                                        // Sauvegarder la consultation mise à jour
                                        viewModel.updateConsultation(updatedConsultation)

                                        // Sélectionner automatiquement la nouvelle ration créée
                                        viewModel.selectRation(rationAvecAliment)
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        backgroundColor = VetNutriColors.Secondary,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = "+R",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/** Graphique en nuage de points : % énergie protéines vs % énergie lipides */
@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun GraphiqueNuagePoints(
        alimentsAnalyses: List<AlimentAnalyseData>,
        ongletActif: String,
        alimentSelectionne: String? = null,
        nutrimentX: String? = null,
        nutrimentY: String? = null,
        useDryMatterPer100g: Boolean = false,
        referenceEv: ReferenceEv?,
        equationRepository: EquationRepository?,
        modifier: Modifier = Modifier
) {
    // États pour le zoom et le pan (uniquement pour les scatter plots)
    val zoomPanState = remember { mutableStateOf(ZoomPanState()) }
    val originalRanges = remember(alimentsAnalyses, ongletActif) {
        if (alimentsAnalyses.isEmpty() || ongletActif == "densite_energetique" || ongletActif == "nutriments_perso") {
            null
        } else {
            // Calculer les plages originales
            val points = when (ongletActif) {
                "protein_lipid" -> {
                    alimentsAnalyses.map { data ->
                        Point(
                                x = data.pourcentageProteines.toFloat(),
                                y = data.pourcentageLipides.toFloat()
                        )
                    }
                }
                "phosphore_protein" -> {
                    alimentsAnalyses.map { data ->
                        Point(
                                x = data.phosphorePer1000Kcal.toFloat(),
                                y = data.proteinePer1000Kcal.toFloat()
                        )
                    }
                }
                "calcium_phosphore" -> {
                    alimentsAnalyses.map { data ->
                        Point(
                                x = data.phosphorePer1000Kcal.toFloat(),
                                y = data.calciumPer1000Kcal.toFloat()
                        )
                    }
                }
                else -> emptyList()
            }
            
            if (points.isNotEmpty()) {
                val minX = points.minOf { it.x }.coerceAtLeast(0f)
                val maxX = points.maxOf { it.x }
                val minY = points.minOf { it.y }.coerceAtLeast(0f)
                val maxY = points.maxOf { it.y }
                
                val xRange = when (ongletActif) {
                    "phosphore_protein" -> (minX - minX * 0.05f)..(maxX + maxX * 0.05f)
                    else -> (minX - minX * 0.05f)..(maxX.coerceAtMost(100f) + maxX * 0.05f)
                }
                
                val yRange = when (ongletActif) {
                    "phosphore_protein" -> (minY - minY * 0.05f)..(maxY + maxY * 0.05f)
                    else -> (minY - minY * 0.05f)..(maxY.coerceAtMost(100f) + maxY * 0.05f)
                }
                
                Pair(xRange, yRange)
            } else {
                null
            }
        }
    }
    
    // Réinitialiser le zoom/pan quand l'onglet change
    LaunchedEffect(ongletActif) {
        zoomPanState.value = ZoomPanState()
    }
    
    Card(modifier = modifier, elevation = AppSizes.elevationMedium) {
        Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
            // Titre dynamique selon l'onglet
            val titre =
                    when (ongletActif) {
                        "protein_lipid" -> "Répartition énergétique : Protéines vs Lipides"
                        "phosphore_protein" -> if (useDryMatterPer100g) "Phosphore vs Protéines (/100g MS)" else "Phosphore vs Protéines (/1000 kcal)"
                        "calcium_phosphore" -> if (useDryMatterPer100g) "Calcium vs Phosphore (/100g MS)" else "Calcium vs Phosphore (/1000 kcal)"
                        "nutriments_perso" -> {
                            val xOption = NUTRIMENT_OPTIONS.find { it.key == nutrimentX }
                            val yOption = NUTRIMENT_OPTIONS.find { it.key == nutrimentY }
                            val unit = if (useDryMatterPer100g) "/100g MS" else "/1000 kcal"
                            if (nutrimentY != null && nutrimentY.isNotEmpty()) {
                                "${xOption?.displayName ?: "X"} vs ${yOption?.displayName ?: "Y"} ($unit)"
                            } else {
                                "Distribution de ${xOption?.displayName ?: "Nutriment"} ($unit)"
                            }
                        }
                        else -> "Analyse nutritionnelle"
                    }

            Text(
                    text = titre,
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    color = VetNutriColors.Primary
            )

            Text(
                    text =
                            "Chaque point représente un aliment (numéroté de 1 à ${alimentsAnalyses.size})",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
            
            // Informations supplémentaires selon le type de graphique
            when (ongletActif) {
                "densite_energetique" -> {
                    val avgDensity = alimentsAnalyses.map { it.densiteEnergetique }.average()
                    Text(
                            text = "Densité moyenne: ${GraphFormattingUtils.formatEnergyDensity(avgDensity)}",
                            style = MaterialTheme.typography.caption,
                            color = VetNutriColors.Primary.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                    )
                }
                "protein_lipid" -> {
                    val avgProtein = alimentsAnalyses.map { it.pourcentageProteines }.average()
                    val avgLipid = alimentsAnalyses.map { it.pourcentageLipides }.average()
                    Text(
                            text = "Moyennes - Protéines: ${GraphFormattingUtils.formatPercentage(avgProtein)} | Lipides: ${GraphFormattingUtils.formatPercentage(avgLipid)}",
                            style = MaterialTheme.typography.caption,
                            color = VetNutriColors.Primary.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                    )
                }
                "phosphore_protein" -> {
                    val avgPhosphore = alimentsAnalyses.map { it.phosphorePer1000Kcal }.average()
                    val avgProtein = alimentsAnalyses.map { it.proteinePer1000Kcal }.average()
                    Text(
                            text = "Moyennes - Phosphore: ${GraphFormattingUtils.formatNutrientPer1000Kcal(avgPhosphore)} | Protéines: ${GraphFormattingUtils.formatNutrientPer1000Kcal(avgProtein)}",
                            style = MaterialTheme.typography.caption,
                            color = VetNutriColors.Primary.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                    )
                }
                "calcium_phosphore" -> {
                    val avgCalcium = alimentsAnalyses.map { it.calciumPer1000Kcal }.average()
                    val avgPhosphore = alimentsAnalyses.map { it.phosphorePer1000Kcal }.average()
                    Text(
                            text = "Moyennes - Calcium: ${GraphFormattingUtils.formatNutrientPer1000Kcal(avgCalcium)} | Phosphore: ${GraphFormattingUtils.formatNutrientPer1000Kcal(avgPhosphore)}",
                            style = MaterialTheme.typography.caption,
                            color = VetNutriColors.Primary.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                    )
                }
            }

            // Légende des lignes de référence pour le graphique Calcium/Phosphore
            if (ongletActif == "calcium_phosphore") {
                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ligne rouge 1:1
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp, 2.dp)) {
                        drawLine(
                                color = Color.Red.copy(alpha = 0.7f),
                                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                                end =
                                        androidx.compose.ui.geometry.Offset(
                                                size.width,
                                                size.height / 2
                                        ),
                                strokeWidth = 2.dp.toPx()
                        )
                    }
                    Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                    Text(
                            text = "Ratio 1:1",
                            style = MaterialTheme.typography.caption,
                            color = Color.Red.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.width(AppSizes.paddingMedium))

                    // Ligne bleue 2:1
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp, 2.dp)) {
                        drawLine(
                                color = Color.Blue.copy(alpha = 0.7f),
                                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                                end =
                                        androidx.compose.ui.geometry.Offset(
                                                size.width,
                                                size.height / 2
                                        ),
                                strokeWidth = 2.dp.toPx()
                        )
                    }
                    Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                    Text(
                            text = "Ratio 2:1",
                            style = MaterialTheme.typography.caption,
                            color = Color.Blue.copy(alpha = 0.7f)
                    )
                }
            }

            // Légende des lignes de référence pour le graphique Protéines/Phosphore
            if (ongletActif == "phosphore_protein") {
                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ligne verte 35:1
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp, 2.dp)) {
                        drawLine(
                                color = Color.Green.copy(alpha = 0.7f),
                                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                                end =
                                        androidx.compose.ui.geometry.Offset(
                                                size.width,
                                                size.height / 2
                                        ),
                                strokeWidth = 2.dp.toPx()
                        )
                    }
                    Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                    Text(
                            text = "Ratio 35:1",
                            style = MaterialTheme.typography.caption,
                            color = Color.Green.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.width(AppSizes.paddingMedium))

                    // Ligne orange 25:1
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp, 2.dp)) {
                        drawLine(
                                color = Color(0xFFFF9800).copy(alpha = 0.7f),
                                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                                end =
                                        androidx.compose.ui.geometry.Offset(
                                                size.width,
                                                size.height / 2
                                        ),
                                strokeWidth = 2.dp.toPx()
                        )
                    }
                    Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                    Text(
                            text = "Ratio 25:1",
                            style = MaterialTheme.typography.caption,
                            color = Color(0xFFFF9800).copy(alpha = 0.7f)
                    )
                }
            }

            // Légende des lignes de référence pour le graphique Répartition énergétique
            if (ongletActif == "protein_lipid") {
                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ligne magenta 80-x
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp, 2.dp)) {
                        drawLine(
                                color = Color.Magenta.copy(alpha = 0.7f),
                                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                                end =
                                        androidx.compose.ui.geometry.Offset(
                                                size.width,
                                                size.height / 2
                                        ),
                                strokeWidth = 2.dp.toPx()
                        )
                    }
                    Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                    Text(
                            text = "20% ENA",
                            style = MaterialTheme.typography.caption,
                            color = Color.Magenta.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.width(AppSizes.paddingMedium))

                    // Ligne cyan 60-x
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp, 2.dp)) {
                        drawLine(
                                color = Color.Cyan.copy(alpha = 0.7f),
                                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                                end =
                                        androidx.compose.ui.geometry.Offset(
                                                size.width,
                                                size.height / 2
                                        ),
                                strokeWidth = 2.dp.toPx()
                        )
                    }
                    Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                    Text(
                            text = "60% ENA",
                            style = MaterialTheme.typography.caption,
                            color = Color.Cyan.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.width(AppSizes.paddingMedium))

                    // Ligne jaune 40-x
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp, 2.dp)) {
                        drawLine(
                                color = Color.Yellow.copy(alpha = 0.7f),
                                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                                end =
                                        androidx.compose.ui.geometry.Offset(
                                                size.width,
                                                size.height / 2
                                        ),
                                strokeWidth = 2.dp.toPx()
                        )
                    }
                    Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                    Text(
                            text = "40% ENA",
                            style = MaterialTheme.typography.caption,
                            color = Color.Yellow.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

            if (alimentsAnalyses.isNotEmpty()) {
                // Affichage selon le type de graphique
                if (ongletActif == "densite_energetique") {
                    // 📊 HISTOGRAMME DENSITÉ ÉNERGÉTIQUE
                    HistogrammeEnergieAliments(
                            alimentsAnalyses = alimentsAnalyses,
                            alimentSelectionne = alimentSelectionne,
                            useDryMatterPer100g = useDryMatterPer100g,
                            modifier = Modifier.fillMaxWidth().height(400.dp)
                    )
                } else if (ongletActif == "nutriments_perso") {
                    // ✨ GRAPHIQUE PERSONNALISÉ
                    GraphiqueNutrimentsPersonnalise(
                            alimentsAnalyses = alimentsAnalyses,
                            nutrimentX = nutrimentX ?: "proteine",
                            nutrimentY = nutrimentY,
                            alimentSelectionne = alimentSelectionne,
                            useDryMatterPer100g = useDryMatterPer100g,
                            referenceEv = referenceEv,
                            equationRepository = equationRepository,
                            modifier = Modifier.fillMaxWidth().height(400.dp)
                    )
                } else {
                    // 📈 SCATTER PLOTS (PROTÉINES/LIPIDES et PHOSPHORE/PROTÉINES)
                    // Préparer les données pour le graphique selon l'onglet actif
                    val points =
                            when (ongletActif) {
                                "protein_lipid" -> {
                                    alimentsAnalyses.map { data ->
                                        Point(
                                                x = data.pourcentageProteines.toFloat(),
                                                y = data.pourcentageLipides.toFloat()
                                        )
                                    }
                                }
                                "phosphore_protein" -> {
                                    alimentsAnalyses.map { data ->
                                        Point(
                                                x = data.phosphorePer1000Kcal.toFloat(),
                                                y = data.proteinePer1000Kcal.toFloat()
                                        )
                                    }
                                }
                                "calcium_phosphore" -> {
                                    alimentsAnalyses.map { data ->
                                        Point(
                                                x = data.phosphorePer1000Kcal.toFloat(),
                                                y = data.calciumPer1000Kcal.toFloat()
                                        )
                                    }
                                }
                                else -> {
                                    alimentsAnalyses.map { data ->
                                        Point(
                                                x = data.pourcentageProteines.toFloat(),
                                                y = data.pourcentageLipides.toFloat()
                                        )
                                    }
                                }
                            }

                    // Utiliser les plages originales ou calculer si nécessaire
                    val (baseXRange, baseYRange) = originalRanges ?: run {
                        val minX = points.minOf { it.x }.coerceAtLeast(0f)
                        val maxX = points.maxOf { it.x }
                        val minY = points.minOf { it.y }.coerceAtLeast(0f)
                        val maxY = points.maxOf { it.y }
                        
                        val xRange = when (ongletActif) {
                            "phosphore_protein" -> (minX - minX * 0.05f)..(maxX + maxX * 0.05f)
                            else -> (minX - minX * 0.05f)..(maxX.coerceAtMost(100f) + maxX * 0.05f)
                        }
                        
                        val yRange = when (ongletActif) {
                            "phosphore_protein" -> (minY - minY * 0.05f)..(maxY + maxY * 0.05f)
                            else -> (minY - minY * 0.05f)..(maxY.coerceAtMost(100f) + maxY * 0.05f)
                        }
                        
                        Pair(xRange, yRange)
                    }
                    
                    // Calculer les plages zoomées
                    val xRange = calculateZoomedRange(baseXRange, zoomPanState.value, isXAxis = true)
                    val yRange = calculateZoomedRange(baseYRange, zoomPanState.value, isXAxis = false)
                    
                    // Boutons de zoom (pour desktop où le pinch ne fonctionne pas)
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                                onClick = {
                                    // Zoom out
                                    val newScaleX = (zoomPanState.value.scaleX * 0.9f).coerceIn(0.5f, 5f)
                                    val newScaleY = (zoomPanState.value.scaleY * 0.9f).coerceIn(0.5f, 5f)
                                    zoomPanState.value = ZoomPanState(
                                            scaleX = newScaleX,
                                            scaleY = newScaleY,
                                            panX = zoomPanState.value.panX,
                                            panY = zoomPanState.value.panY
                                    )
                                }
                        ) {
                            Icon(
                                    imageVector = Icons.Default.ZoomOut,
                                    contentDescription = "Zoom arrière"
                            )
                        }
                        IconButton(
                                onClick = {
                                    // Zoom in
                                    val newScaleX = (zoomPanState.value.scaleX * 1.1f).coerceIn(0.5f, 5f)
                                    val newScaleY = (zoomPanState.value.scaleY * 1.1f).coerceIn(0.5f, 5f)
                                    zoomPanState.value = ZoomPanState(
                                            scaleX = newScaleX,
                                            scaleY = newScaleY,
                                            panX = zoomPanState.value.panX,
                                            panY = zoomPanState.value.panY
                                    )
                                }
                        ) {
                            Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = "Zoom avant"
                            )
                        }
                        if (zoomPanState.value.scaleX != 1f || zoomPanState.value.scaleY != 1f || 
                            zoomPanState.value.panX != 0f || zoomPanState.value.panY != 0f) {
                            TextButton(
                                    onClick = { zoomPanState.value = ZoomPanState() }
                            ) {
                                Text("Réinitialiser", fontSize = 12.sp)
                            }
                        }
                    }

                    // 🎯 Graphique avec numéros superposés et zoom/pan
                    BoxWithConstraints(modifier = Modifier.height(400.dp).clipToBounds()) {
                        // Graphique principal avec gestes de zoom/pan
                        XYGraph(
                                xAxisModel = FloatLinearAxisModel(range = xRange),
                                yAxisModel = FloatLinearAxisModel(range = yRange),
                                xAxisTitle = when (ongletActif) {
                                    "protein_lipid" -> "Protéines (% énergie)"
                                    "phosphore_protein" -> if (useDryMatterPer100g) "Phosphore (g/100g MS)" else "Phosphore (g/1000 kcal)"
                                    "calcium_phosphore" -> if (useDryMatterPer100g) "Phosphore (g/100g MS)" else "Phosphore (g/1000 kcal)"
                                    else -> ""
                                },
                                yAxisTitle = when (ongletActif) {
                                    "protein_lipid" -> "Lipides (% énergie)"
                                    "phosphore_protein" -> if (useDryMatterPer100g) "Protéines (g/100g MS)" else "Protéines (g/1000 kcal)"
                                    "calcium_phosphore" -> if (useDryMatterPer100g) "Calcium (g/100g MS)" else "Calcium (g/1000 kcal)"
                                    else -> ""
                                },
                                modifier = Modifier
                                        .fillMaxSize()
                                        .clipToBounds()
                                        .pointerInput(Unit) {
                                            detectTransformGestures { _, pan, zoom, _ ->
                                                // Limiter le zoom entre 0.5x et 5x
                                                val newScaleX = (zoomPanState.value.scaleX * zoom).coerceIn(0.5f, 5f)
                                                val newScaleY = (zoomPanState.value.scaleY * zoom).coerceIn(0.5f, 5f)
                                                
                                                // Calculer les plages actuelles (zoomées) pour le pan
                                                val currentXRange = calculateZoomedRange(baseXRange, zoomPanState.value, isXAxis = true)
                                                val currentYRange = calculateZoomedRange(baseYRange, zoomPanState.value, isXAxis = false)
                                                
                                                // Convertir le pan en coordonnées de données (basé sur la plage actuelle)
                                                val panXDelta = pan.x / size.width * (currentXRange.endInclusive - currentXRange.start)
                                                val panYDelta = -pan.y / size.height * (currentYRange.endInclusive - currentYRange.start)
                                                
                                                zoomPanState.value = ZoomPanState(
                                                        scaleX = newScaleX,
                                                        scaleY = newScaleY,
                                                        panX = zoomPanState.value.panX + panXDelta,
                                                        panY = zoomPanState.value.panY + panYDelta
                                                )
                                            }
                                        }
                        ) {
                            // Afficher chaque point individuellement avec LinePlot et symbol
                            // Filtrer les points qui sont dans la plage visible
                            alimentsAnalyses.forEachIndexed { index, data ->
                                val point =
                                        points[index] // Utiliser le point calculé selon l'onglet
                                // actif
                                
                                // Vérifier si le point est dans la plage visible
                                val isPointVisible = point.x >= xRange.start && 
                                                    point.x <= xRange.endInclusive &&
                                                    point.y >= yRange.start && 
                                                    point.y <= yRange.endInclusive
                                
                                if (isPointVisible) {
                                    LinePlot(
                                            data = listOf(point),
                                            symbol = {
                                            // Point principal avec couleur selon sélection
                                            val couleurPoint =
                                                    if (data.aliment.uuid == alimentSelectionne) {
                                                        Color(0xFF9C27B0) // Violet pour sélectionné
                                                    } else {
                                                        // Vérifier l'humidité pour les aliments non
                                                        // sélectionnés
                                                        val humidite =
                                                                data.aliment.getNutrient(
                                                                        NutrientMain.HUMIDITE
                                                                )
                                                        if (humidite == null || humidite < 20.0) {
                                                            Color(
                                                                    0xFFFF9800
                                                            ) // Orange pour aliments sans humidité
                                                            // ou < 20%
                                                        } else {
                                                            VetNutriColors
                                                                    .Primary // Couleur normale
                                                        }
                                                    }

                                            androidx.compose.foundation.Canvas(
                                                    modifier = Modifier.size(12.dp)
                                            ) {
                                                drawCircle(
                                                        color = couleurPoint,
                                                        radius = 6f,
                                                        center = center
                                                )
                                            }
                                        }
                                    )
                                }
                            }

                            // 🔸 LIGNES DE RÉFÉRENCE pour le graphique Calcium/Phosphore
                            if (ongletActif == "calcium_phosphore") {
                                // Ligne 1:1 (Calcium = Phosphore)
                                val ligne1to1 =
                                        listOf(
                                                Point(x = xRange.start, y = xRange.start),
                                                Point(
                                                        x = xRange.endInclusive,
                                                        y = xRange.endInclusive
                                                )
                                        )
                                LinePlot(
                                        data = ligne1to1,
                                        lineStyle =
                                                LineStyle(
                                                        brush =
                                                                SolidColor(
                                                                        Color.Red.copy(alpha = 0.7f)
                                                                ),
                                                        strokeWidth = 2.dp
                                                )
                                )

                                // Ligne 2:1 (Calcium = 2 × Phosphore)
                                val ligne2to1 =
                                        listOf(
                                                Point(x = xRange.start, y = xRange.start * 2),
                                                Point(
                                                        x = xRange.endInclusive,
                                                        y = xRange.endInclusive * 2
                                                )
                                        )
                                LinePlot(
                                        data = ligne2to1,
                                        lineStyle =
                                                LineStyle(
                                                        brush =
                                                                SolidColor(
                                                                        Color.Blue.copy(
                                                                                alpha = 0.7f
                                                                        )
                                                                ),
                                                        strokeWidth = 2.dp
                                                )
                                )
                            }

                            // 🔸 LIGNES DE RÉFÉRENCE pour le graphique Protéines/Phosphore
                            if (ongletActif == "phosphore_protein") {
                                // Ligne 35:1 (Protéines = 35 × Phosphore)
                                val ligne35to1 =
                                        listOf(
                                                Point(x = xRange.start, y = xRange.start * 35),
                                                Point(
                                                        x = xRange.endInclusive,
                                                        y = xRange.endInclusive * 35
                                                )
                                        )
                                LinePlot(
                                        data = ligne35to1,
                                        lineStyle =
                                                LineStyle(
                                                        brush =
                                                                SolidColor(
                                                                        Color.Green.copy(
                                                                                alpha = 0.7f
                                                                        )
                                                                ),
                                                        strokeWidth = 2.dp
                                                )
                                )

                                // Ligne 25:1 (Protéines = 25 × Phosphore)
                                val ligne25to1 =
                                        listOf(
                                                Point(x = xRange.start, y = xRange.start * 25),
                                                Point(
                                                        x = xRange.endInclusive,
                                                        y = xRange.endInclusive * 25
                                                )
                                        )
                                LinePlot(
                                        data = ligne25to1,
                                        lineStyle =
                                                LineStyle(
                                                        brush =
                                                                SolidColor(
                                                                        Color(0xFFFF9800)
                                                                                .copy(alpha = 0.7f)
                                                                ),
                                                        strokeWidth = 2.dp
                                                )
                                )
                            }

                            // 🔸 LIGNES DE RÉFÉRENCE pour le graphique Répartition énergétique
                            // (Protéines/Lipides)
                            if (ongletActif == "protein_lipid") {
                                // Ligne 80-x : Protéines + Lipides = 80% (reste = glucides)
                                val ligne80MinusX =
                                        listOf(
                                                Point(x = xRange.start, y = 80f - xRange.start),
                                                Point(
                                                        x = xRange.endInclusive,
                                                        y = 80f - xRange.endInclusive
                                                )
                                        )
                                LinePlot(
                                        data = ligne80MinusX,
                                        lineStyle =
                                                LineStyle(
                                                        brush =
                                                                SolidColor(
                                                                        Color.Magenta.copy(
                                                                                alpha = 0.7f
                                                                        )
                                                                ),
                                                        strokeWidth = 2.dp
                                                )
                                )

                                // Ligne 60-x : Protéines + Lipides = 60% (reste = glucides)
                                val ligne60MinusX =
                                        listOf(
                                                Point(x = xRange.start, y = 60f - xRange.start),
                                                Point(
                                                        x = xRange.endInclusive,
                                                        y = 60f - xRange.endInclusive
                                                )
                                        )
                                LinePlot(
                                        data = ligne60MinusX,
                                        lineStyle =
                                                LineStyle(
                                                        brush =
                                                                SolidColor(
                                                                        Color.Cyan.copy(
                                                                                alpha = 0.7f
                                                                        )
                                                                ),
                                                        strokeWidth = 2.dp
                                                )
                                )

                                // Ligne 40-x : Protéines + Lipides = 40% (reste = glucides)
                                val ligne40MinusX =
                                        listOf(
                                                Point(x = xRange.start, y = 40f - xRange.start),
                                                Point(
                                                        x = xRange.endInclusive,
                                                        y = 40f - xRange.endInclusive
                                                )
                                        )
                                LinePlot(
                                        data = ligne40MinusX,
                                        lineStyle =
                                                LineStyle(
                                                        brush =
                                                                SolidColor(
                                                                        Color.Yellow.copy(
                                                                                alpha = 0.7f
                                                                        )
                                                                ),
                                                        strokeWidth = 2.dp
                                                )
                                )
                            }
                        }

                        // 🎯 Numéros superposés directement avec correction des marges d'axes
                        // Marges typiques des axes KoalaPlot (estimation)
                        // 🔧 Marges AJUSTÉES basées sur l'observation des logs (décalage
                        // empirique)
                        val leftAxisMargin =
                                10.dp // Marge pour les labels de l'axe Y (augmentée)
                        val bottomAxisMargin =
                                15.dp // Marge pour les labels de l'axe X (augmentée)
                        val topMargin = 10.dp // Marge supérieure
                        val rightMargin = 20.dp // Marge droite

                        // Zone de graphique effective
                        val effectiveGraphWidth = maxWidth - leftAxisMargin - rightMargin
                        val effectiveGraphHeight = maxHeight - bottomAxisMargin - topMargin
                        
                        alimentsAnalyses.forEachIndexed { index, data ->
                            val point =
                                    points[index] // Utiliser le point calculé selon l'onglet actif
                            
                            // Vérifier si le point est dans la plage visible
                            val isPointVisible = point.x >= xRange.start && 
                                                point.x <= xRange.endInclusive &&
                                                point.y >= yRange.start && 
                                                point.y <= yRange.endInclusive
                            
                            if (!isPointVisible) return@forEachIndexed
                            
                            // Calculer la position du numéro avec les vraies dimensions
                            val xPosition =
                                    ((point.x - xRange.start) /
                                            (xRange.endInclusive - xRange.start))
                            val yPosition =
                                    1f -
                                            ((point.y - yRange.start) /
                                                    (yRange.endInclusive - yRange.start))
                            
                            // Vérifier si le label est dans la zone visible (avec une petite marge)
                            val labelX = leftAxisMargin + (xPosition * effectiveGraphWidth.value).dp - 10.dp
                            val labelY = topMargin + (yPosition * effectiveGraphHeight.value).dp - 30.dp
                            val isLabelVisible = labelX >= (-20).dp && 
                                                labelX <= maxWidth + 20.dp &&
                                                labelY >= (-20).dp && 
                                                labelY <= maxHeight + 20.dp
                            
                            if (!isLabelVisible) return@forEachIndexed

                            // Couleur selon la sélection et l'humidité
                            val numeroColor =
                                    if (data.aliment.uuid == alimentSelectionne) {
                                        Color(0xFF9C27B0) // Violet pour sélectionné
                                    } else {
                                        // Vérifier l'humidité pour les aliments non sélectionnés
                                        val humidite =
                                                data.aliment.getNutrient(NutrientMain.HUMIDITE)
                                        if (humidite == null || humidite < 20.0) {
                                            Color(
                                                    0xFFFF9800
                                            ) // Orange pour aliments sans humidité ou < 20%
                                        } else {
                                            VetNutriColors.Primary // Couleur par défaut
                                        }
                                    }

                            Box(
                                    modifier =
                                            Modifier.fillMaxSize()
                                                    .wrapContentSize(Alignment.TopStart)
                                                    .offset(
                                                            x =
                                                                    leftAxisMargin +
                                                                            (xPosition *
                                                                                            effectiveGraphWidth
                                                                                                    .value)
                                                                                    .dp -
                                                                            10.dp, // Position dans
                                                            // la zone
                                                            // effective +
                                                            // centrage
                                                            y =
                                                                    topMargin +
                                                                            (yPosition *
                                                                                            effectiveGraphHeight
                                                                                                    .value)
                                                                                    .dp -
                                                                            30.dp // Position dans
                                                            // la zone
                                                            // effective +
                                                            // au-dessus du
                                                            // point
                                                            ),
                                    contentAlignment = Alignment.Center
                            ) {
                                // Fond du numéro
                                androidx.compose.foundation.Canvas(
                                        modifier = Modifier.size(20.dp)
                                ) {
                                    drawCircle(color = Color.White, radius = 10.dp.toPx())
                                    drawCircle(
                                            color = numeroColor,
                                            radius = 10.dp.toPx(),
                                            style = Stroke(width = 2.dp.toPx())
                                    )
                                }

                                // Numéro
                                Text(
                                        text = "${data.numero}",
                                        style =
                                                MaterialTheme.typography.caption.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                ),
                                        color = numeroColor
                                )
                            }
                        }
                    }
                } // Fin du bloc else (scatter plots)
            } else {
                Text(
                        text = "Aucune donnée disponible pour l'analyse",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/** Ligne d'aliment dans le tableau */
@Composable
private fun AlimentRow(
        data: AlimentAnalyseData,
        isSelected: Boolean,
        onAlimentSelected: (String?) -> Unit
) {
    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .clickable {
                                onAlimentSelected(if (isSelected) null else data.aliment.uuid)
                            }
                            .background(
                                    if (isSelected) Color(0xFF9C27B0).copy(alpha = 0.1f)
                                    else Color.Transparent
                            )
                            .padding(AppSizes.paddingSmall),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
                text = "${data.numero}",
                modifier = Modifier.weight(0.1f),
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Bold,
                color = VetNutriColors.Primary
        )
        Column(modifier = Modifier.weight(0.4f)) {
                Text(
                        text = data.aliment.nom ?: "Sans nom",
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Medium
                )
                Text(
                        text = "Densité: ${GraphFormattingUtils.formatEnergyDensity(data.densiteEnergetique)}",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        fontSize = 10.sp
                )
        }
        Text(
                text = data.aliment.gamme ?: "-",
                modifier = Modifier.weight(0.25f),
                style = MaterialTheme.typography.caption
        )
        Text(
                text = data.aliment.brand ?: "-",
                modifier = Modifier.weight(0.25f),
                style = MaterialTheme.typography.caption
        )
    }
}

/** Liste des aliments avec leurs données d'analyse */
@Composable
private fun ListeAlimentsAnalyse(
        alimentsAnalyses: List<AlimentAnalyseData>,
        alimentSelectionne: String? = null,
        onAlimentSelected: (String?) -> Unit = {},
        isCompactMode: Boolean = false, // ✨ Mode compact pour éviter les conflits de scroll
        modifier: Modifier = Modifier
) {
    Card(modifier = modifier, elevation = AppSizes.elevationMedium) {
        Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
            Text(
                    text = "Liste des aliments",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    color = VetNutriColors.Primary
            )

            Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

            if (alimentsAnalyses.isNotEmpty()) {
                // En-têtes du tableau
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                            text = "N°",
                            modifier = Modifier.weight(0.1f),
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Bold
                    )
                    Text(
                            text = "Nom",
                            modifier = Modifier.weight(0.4f),
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Bold
                    )
                    Text(
                            text = "Gamme",
                            modifier = Modifier.weight(0.25f),
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Bold
                    )
                    Text(
                            text = "Marque",
                            modifier = Modifier.weight(0.25f),
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Bold
                    )
                }

                Divider(modifier = Modifier.padding(vertical = AppSizes.paddingSmall))

                // Liste des aliments - LazyColumn en mode large, Column normale en mode compact
                if (isCompactMode) {
                    // ✨ Mode compact : Column normale (pas de scroll interne)
                    Column(verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall / 2)) {
                        alimentsAnalyses.forEach { data ->
                            AlimentRow(
                                    data = data,
                                    isSelected = data.aliment.uuid == alimentSelectionne,
                                    onAlimentSelected = onAlimentSelected
                            )
                        }
                    }
                } else {
                    // ✨ Mode large : LazyColumn pour performance avec scroll
                    LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall / 2),
                            modifier = Modifier.fillMaxHeight()
                    ) {
                        items(alimentsAnalyses) { data ->
                            AlimentRow(
                                    data = data,
                                    isSelected = data.aliment.uuid == alimentSelectionne,
                                    onAlimentSelected = onAlimentSelected
                            )
                        }
                    }
                }
            } else {
                Text(
                        text = "Aucun aliment à analyser",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/** Graphique personnalisé pour les nutriments sélectionnés */
@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun GraphiqueNutrimentsPersonnalise(
        alimentsAnalyses: List<AlimentAnalyseData>,
        nutrimentX: String?,
        nutrimentY: String?,
        alimentSelectionne: String? = null,
        useDryMatterPer100g: Boolean = false,
        referenceEv: ReferenceEv?,
        equationRepository: EquationRepository?,
        modifier: Modifier = Modifier
) {
    // États pour le zoom et le pan
    val zoomPanState = remember { mutableStateOf(ZoomPanState()) }
    
    // Réinitialiser le zoom/pan quand les nutriments changent
    LaunchedEffect(nutrimentX, nutrimentY) {
        zoomPanState.value = ZoomPanState()
    }
    
    if (alimentsAnalyses.isEmpty()) {
        Text(
                text = "Aucune donnée disponible pour le graphique personnalisé",
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        return
    }

    // Récupérer les informations des nutriments sélectionnés
    val xOption = NUTRIMENT_OPTIONS.find { it.key == nutrimentX }
    val yOption = NUTRIMENT_OPTIONS.find { it.key == nutrimentY }

    // Si aucun nutriment X n'est sélectionné
    if (nutrimentX == null) {
        Text(
                text = "Veuillez sélectionner au moins un nutriment pour l'axe X",
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        return
    }

    // Si Y est sélectionné (et pas "aucun") : scatter plot
    if (nutrimentY != null && nutrimentY.isNotEmpty()) {
        // 📈 SCATTER PLOT : X vs Y
        val points =
                alimentsAnalyses.map { data ->
                    Point(
                            x = runBlocking { data.getNutrimentValue(nutrimentX, referenceEv, equationRepository, useDryMatterPer100g).toFloat() },
                            y = runBlocking { data.getNutrimentValue(nutrimentY, referenceEv, equationRepository, useDryMatterPer100g).toFloat() }
                    )
                }

        // Vérifier que nous avons des données valides
        if (points.isEmpty() || points.all { it.x == 0f && it.y == 0f }) {
            Text(
                    text = "Données insuffisantes pour le graphique scatter plot",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
            return
        }

        // Calculer les plages de base avec validation
        val minX = points.minOf { it.x }.coerceAtLeast(0f)
        val maxX = points.maxOf { it.x }

        // S'assurer que minX != maxX pour éviter la division par zéro
        val safeMinX = if (minX == maxX) minX * 0.9f else minX
        val safeMaxX = if (minX == maxX) maxX * 1.1f else maxX

        val minY = points.minOf { it.y }.coerceAtLeast(0f)
        val maxY = points.maxOf { it.y }

        // S'assurer que minY != maxY pour éviter la division par zéro
        val safeMinY = if (minY == maxY) minY * 0.9f else minY
        val safeMaxY = if (minY == maxY) maxY * 1.1f else maxY

        val baseXRange =
                (safeMinX - safeMinX * 0.05f).coerceAtLeast(0f)..(safeMaxX + safeMaxX * 0.05f)
                                .coerceAtLeast(safeMinX + 0.1f)
        val baseYRange =
                (safeMinY - safeMinY * 0.05f).coerceAtLeast(0f)..(safeMaxY + safeMaxY * 0.05f)
                                .coerceAtLeast(safeMinY + 0.1f)
        
        // Calculer les plages zoomées
        val xRange = calculateZoomedRange(baseXRange, zoomPanState.value, isXAxis = true)
        val yRange = calculateZoomedRange(baseYRange, zoomPanState.value, isXAxis = false)
        
        // Boutons de zoom (pour desktop où le pinch ne fonctionne pas)
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                    onClick = {
                        // Zoom out
                        val newScaleX = (zoomPanState.value.scaleX * 0.9f).coerceIn(0.5f, 5f)
                        val newScaleY = (zoomPanState.value.scaleY * 0.9f).coerceIn(0.5f, 5f)
                        zoomPanState.value = ZoomPanState(
                                scaleX = newScaleX,
                                scaleY = newScaleY,
                                panX = zoomPanState.value.panX,
                                panY = zoomPanState.value.panY
                        )
                    }
            ) {
                Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = "Zoom arrière"
                )
            }
            IconButton(
                    onClick = {
                        // Zoom in
                        val newScaleX = (zoomPanState.value.scaleX * 1.1f).coerceIn(0.5f, 5f)
                        val newScaleY = (zoomPanState.value.scaleY * 1.1f).coerceIn(0.5f, 5f)
                        zoomPanState.value = ZoomPanState(
                                scaleX = newScaleX,
                                scaleY = newScaleY,
                                panX = zoomPanState.value.panX,
                                panY = zoomPanState.value.panY
                        )
                    }
            ) {
                Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom avant"
                )
            }
            if (zoomPanState.value.scaleX != 1f || zoomPanState.value.scaleY != 1f || 
                zoomPanState.value.panX != 0f || zoomPanState.value.panY != 0f) {
                TextButton(
                        onClick = { zoomPanState.value = ZoomPanState() }
                ) {
                    Text("Réinitialiser", fontSize = 12.sp)
                }
            }
        }

        BoxWithConstraints(modifier = modifier.clipToBounds()) {
            XYGraph(
                    xAxisModel = FloatLinearAxisModel(range = xRange),
                    yAxisModel = FloatLinearAxisModel(range = yRange),
                    xAxisTitle = "${xOption?.displayName} (${if (useDryMatterPer100g) "/100g MS" else "/1000 kcal"})",
                    yAxisTitle = "${yOption?.displayName} (${if (useDryMatterPer100g) "/100g MS" else "/1000 kcal"})",
                    modifier = Modifier
                            .fillMaxSize()
                            .clipToBounds()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    // Limiter le zoom entre 0.5x et 5x
                                    val newScaleX = (zoomPanState.value.scaleX * zoom).coerceIn(0.5f, 5f)
                                    val newScaleY = (zoomPanState.value.scaleY * zoom).coerceIn(0.5f, 5f)
                                    
                                    // Calculer les plages actuelles (zoomées) pour le pan
                                    val currentXRange = calculateZoomedRange(baseXRange, zoomPanState.value, isXAxis = true)
                                    val currentYRange = calculateZoomedRange(baseYRange, zoomPanState.value, isXAxis = false)
                                    
                                    // Convertir le pan en coordonnées de données (basé sur la plage actuelle)
                                    val panXDelta = pan.x / size.width * (currentXRange.endInclusive - currentXRange.start)
                                    val panYDelta = -pan.y / size.height * (currentYRange.endInclusive - currentYRange.start)
                                    
                                    zoomPanState.value = ZoomPanState(
                                            scaleX = newScaleX,
                                            scaleY = newScaleY,
                                            panX = zoomPanState.value.panX + panXDelta,
                                            panY = zoomPanState.value.panY + panYDelta
                                    )
                                }
                            }
            ) {
                // Afficher chaque point (uniquement ceux dans la plage visible)
                alimentsAnalyses.forEachIndexed { index, data ->
                    val point = points[index]
                    
                    // Vérifier si le point est dans la plage visible
                    val isPointVisible = point.x >= xRange.start && 
                                        point.x <= xRange.endInclusive &&
                                        point.y >= yRange.start && 
                                        point.y <= yRange.endInclusive
                    
                    if (!isPointVisible) return@forEachIndexed
                    
                    LinePlot(
                            data = listOf(point),
                            symbol = {
                                val couleurPoint =
                                        if (data.aliment.uuid == alimentSelectionne) {
                                            Color(0xFF9C27B0) // Violet pour sélectionné
                                        } else {
                                            // Vérifier l'humidité pour les aliments non sélectionnés
                                            val humidite =
                                                    data.aliment.getNutrient(NutrientMain.HUMIDITE)
                                            if (humidite == null || humidite < 20.0) {
                                                Color(
                                                        0xFFFF9800
                                                ) // Orange pour aliments sans humidité ou < 20%
                                            } else {
                                                VetNutriColors.Primary // Couleur normale
                                            }
                                        }

                                androidx.compose.foundation.Canvas(modifier = Modifier.size(10.dp)) {
                                    drawCircle(color = couleurPoint, radius = 5f, center = center)
                                }
                            }
                    )
                }
            }
            
            // Numéros superposés (en dehors de XYGraph pour pouvoir utiliser maxWidth/maxHeight)
            BoxWithConstraints(modifier = Modifier.fillMaxSize().clipToBounds()) {
                // Marges typiques des axes KoalaPlot (estimation)
                // 🔧 Marges AJUSTÉES basées sur l'observation des logs (décalage empirique)
                val leftAxisMargin = 10.dp // Marge pour les labels de l'axe Y (augmentée)
                val bottomAxisMargin = 15.dp // Marge pour les labels de l'axe X (augmentée)
                val topMargin = 10.dp // Marge supérieure
                val rightMargin = 20.dp // Marge droite

                // Zone de graphique effective
                val effectiveGraphWidth = maxWidth - leftAxisMargin - rightMargin
                val effectiveGraphHeight = maxHeight - bottomAxisMargin - topMargin
                
                alimentsAnalyses.forEachIndexed { index, data ->
                    val point = points[index]
                    
                    // Vérifier si le point est dans la plage visible
                    val isPointVisible = point.x >= xRange.start && 
                                        point.x <= xRange.endInclusive &&
                                        point.y >= yRange.start && 
                                        point.y <= yRange.endInclusive
                    
                    if (!isPointVisible) return@forEachIndexed
                    
                    val xPosition =
                            ((point.x - xRange.start) / (xRange.endInclusive - xRange.start))
                    val yPosition =
                            1f - ((point.y - yRange.start) / (yRange.endInclusive - yRange.start))
                    
                    // Vérifier si le label est dans la zone visible (avec une petite marge)
                    val labelX = leftAxisMargin + (xPosition * effectiveGraphWidth.value).dp - 10.dp
                    val labelY = topMargin + (yPosition * effectiveGraphHeight.value).dp - 30.dp
                    val isLabelVisible = labelX >= (-20).dp && 
                                        labelX <= maxWidth + 20.dp &&
                                        labelY >= (-20).dp && 
                                        labelY <= maxHeight + 20.dp
                    
                    if (!isLabelVisible) return@forEachIndexed

                    val numeroColor =
                            if (data.aliment.uuid == alimentSelectionne) {
                                Color(0xFF9C27B0) // Violet pour sélectionné
                            } else {
                                // Vérifier l'humidité pour les aliments non sélectionnés
                                val humidite = data.aliment.getNutrient(NutrientMain.HUMIDITE)
                                if (humidite == null || humidite < 20.0) {
                                    Color(0xFFFF9800) // Orange pour aliments sans humidité ou < 20%
                                } else {
                                    VetNutriColors.Primary // Couleur par défaut
                                }
                            }

                    Box(
                            modifier =
                                    Modifier.fillMaxSize()
                                            .wrapContentSize(Alignment.TopStart)
                                            .offset(
                                                    x =
                                                            leftAxisMargin +
                                                                    (xPosition *
                                                                                    effectiveGraphWidth
                                                                                            .value)
                                                                            .dp -
                                                                    10.dp, // Position dans la zone
                                                    // effective + centrage
                                                    y =
                                                            topMargin +
                                                                    (yPosition *
                                                                                    effectiveGraphHeight
                                                                                            .value)
                                                                            .dp -
                                                                    30.dp // Position dans la zone
                                                    // effective + au-dessus
                                                    // du point
                                                    ),
                            contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp)) {
                            drawCircle(color = Color.White, radius = 10.dp.toPx())
                            drawCircle(
                                    color = numeroColor,
                                    radius = 10.dp.toPx(),
                                    style = Stroke(width = 2.dp.toPx())
                            )
                        }

                        Text(
                                text = "${data.numero}",
                                style =
                                        MaterialTheme.typography.caption.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                        ),
                                color = numeroColor
                        )
                    }
                }
            }
        }
    } else {
        // 📊 HISTOGRAMME : Distribution du nutriment X
        val valeurs = alimentsAnalyses.map { runBlocking { it.getNutrimentValue(nutrimentX, referenceEv, equationRepository, useDryMatterPer100g).toFloat() } }
        val categories = alimentsAnalyses.map { "${it.numero}" }

        // Vérifier que nous avons des données valides
        if (valeurs.isEmpty() || valeurs.all { it == 0f }) {
            Text(
                    text = "Données insuffisantes pour l'histogramme",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
            return
        }

        // Calculer la plage adaptative basée sur la distribution des données
        // Filtrer les valeurs invalides (NaN, Infinite) avant le calcul
        val valeursValides = valeurs.filter { it.isFinite() && !it.isNaN() }
        val yRange =
                if (valeursValides.isNotEmpty()) {
                    calculateAdaptiveRange(valeursValides, paddingPercent = 0.06f)
                } else {
                    // Valeurs par défaut si aucune valeur valide
                    0f..1f
                }

        XYGraph(
                xAxisModel = remember(categories) { CategoryAxisModel(categories) },
                yAxisModel = remember(yRange) { FloatLinearAxisModel(yRange) },
                yAxisTitle = "${xOption?.displayName} (${if (useDryMatterPer100g) "/100g MS" else "/1000 kcal"})",
                modifier = modifier
        ) {
            VerticalBarPlot(
                    xData = categories,
                    yData = valeurs,
                    bar = { index ->
                        val aliment = alimentsAnalyses[index]
                        val couleur =
                                if (aliment.aliment.uuid == alimentSelectionne) {
                                    Color(0xFF9C27B0) // Violet pour sélectionné
                                } else {
                                    // Vérifier l'humidité pour les aliments non sélectionnés
                                    val humidite =
                                            aliment.aliment.getNutrient(NutrientMain.HUMIDITE)
                                    if (humidite == null || humidite < 20.0) {
                                        Color(
                                                0xFFFF9800
                                        ) // Orange pour aliments sans humidité ou < 20%
                                    } else {
                                        VetNutriColors.Primary // Couleur normale
                                    }
                                }
                        DefaultVerticalBar(SolidColor(couleur))
                    }
            )
        }
    }
}

/** Composant graphique pour afficher un histogramme de densité énergétique */
@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun HistogrammeEnergieAliments(
        alimentsAnalyses: List<AlimentAnalyseData>,
        alimentSelectionne: String? = null,
        useDryMatterPer100g: Boolean = false,
        modifier: Modifier = Modifier
) {
    if (alimentsAnalyses.isEmpty()) {
        Text(
                text = "Aucune donnée disponible pour l'histogramme",
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        return
    }

    // Préparer les données pour l'histogramme
    // Créer les catégories avec seulement les numéros des aliments
    val categories = alimentsAnalyses.map { data -> "${data.numero}" }

    // Données de densité énergétique
    val densiteEnergetique = alimentsAnalyses.map { it.densiteEnergetique.toFloat() }

    // Vérifier que nous avons des données valides
    if (densiteEnergetique.isEmpty() || densiteEnergetique.all { it == 0f }) {
        Text(
                text = "Données insuffisantes pour l'histogramme de densité énergétique",
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        return
    }

    // Plage pour l'axe Y basée sur les données
    // Calculer la plage adaptative basée sur la distribution des données
    // Filtrer les valeurs invalides (NaN, Infinite) avant le calcul
    val valeursValides = densiteEnergetique.filter { it.isFinite() && !it.isNaN() }
    val yRange =
            if (valeursValides.isNotEmpty()) {
                calculateAdaptiveRange(valeursValides, paddingPercent = 0.06f)
            } else {
                // Valeurs par défaut si aucune valeur valide
                0f..1f
            }

    // Créer le graphique
    XYGraph(
            xAxisModel = remember(categories) { CategoryAxisModel(categories) },
            yAxisModel = remember(yRange) { FloatLinearAxisModel(yRange) },
            yAxisTitle = if (useDryMatterPer100g) 
                    "Densité énergétique (kcal/100g MS)"
                else 
                    "Densité énergétique (kcal/100g)",
            modifier = modifier
    ) {
        VerticalBarPlot(
                xData = categories,
                yData = densiteEnergetique,
                bar = { index ->
                    // Couleur de la barre selon si l'aliment est sélectionné
                    val aliment = alimentsAnalyses[index]
                    val couleur =
                            if (aliment.aliment.uuid == alimentSelectionne) {
                                Color(0xFF9C27B0) // Violet pour l'aliment sélectionné
                            } else {
                                // Vérifier l'humidité pour les aliments non sélectionnés
                                val humidite = aliment.aliment.getNutrient(NutrientMain.HUMIDITE)
                                if (humidite == null || humidite < 20.0) {
                                    Color(0xFFFF9800) // Orange pour aliments sans humidité ou < 20%
                                } else {
                                    VetNutriColors.Primary // Couleur par défaut
                                }
                            }
                    DefaultVerticalBar(SolidColor(couleur))
                }
        )
    }
}
