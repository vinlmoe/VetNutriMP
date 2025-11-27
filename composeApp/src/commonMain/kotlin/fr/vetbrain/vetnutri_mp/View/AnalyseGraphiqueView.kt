package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Components.AppDatePicker
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import androidx.compose.ui.draw.rotate
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import fr.vetbrain.vetnutri_mp.Utils.GraphFormattingUtils
import fr.vetbrain.vetnutri_mp.Utils.KoalaPlotExtensions
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.Export.PdfExporter
import fr.vetbrain.vetnutri_mp.Export.DocumentType
import fr.vetbrain.vetnutri_mp.Export.ExportData
import fr.vetbrain.vetnutri_mp.Export.HtmlSection
import fr.vetbrain.vetnutri_mp.Export.RichTextContent
import fr.vetbrain.vetnutri_mp.Export.TextBlock
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import io.github.koalaplot.core.*
import io.github.koalaplot.core.bar.DefaultVerticalBar
import io.github.koalaplot.core.bar.VerticalBarPlot
import io.github.koalaplot.core.line.AreaBaseline
import io.github.koalaplot.core.line.AreaPlot
import io.github.koalaplot.core.line.LinePlot
import io.github.koalaplot.core.style.AreaStyle
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.*
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.Point
import io.github.koalaplot.core.xygraph.XYGraph
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlin.math.pow
import kotlin.math.round

// Data class pour gérer l'état du cône de perte de poids
data class WeightConeState(
    val startDate: LocalDate,
    val startWeight: Double,
    val targetWeight: Double? = null
)

// Data class pour stocker les informations d'âge d'une consultation
data class ConsultationAgeData(
        val date: LocalDate,
        val ageInDays: Int,
        val ageInYears: Double,
        val ageInMonths: Double,
        val weight: Double,
        val isFromConsultation: Boolean = false,
        val weightUuid: String? = null
)

// Data class pour stocker les données énergétiques des rations
data class RationEnergyData(
        val consultationDate: LocalDate?,
        val consultationId: String,
        val rationName: String,
        val rationId: String,
        val numero: Int,
        val proteineEnergyPercentage: Double,
        val lipideEnergyPercentage: Double,
        val energieTotale: Double,
        val matiereSeche: Double = 0.0,
        val poidsTotal: Double = 0.0
)

// Classes pour les courbes de croissance
data class CurveParamP(
        val name: String,
        val max: Double,
        val half: Double,
        val slope: Double,
        val UUID: String = generateUuidString()
)

data class CurveP(
        val description: String,
        val params: List<CurveParamP>,
        val biblioRef: String,
        val espece: String,
        val code: String,
        val ageMax: Int
)

// Fonction pour calculer le poids selon l'équation de croissance
private fun calculerPoidsCroissance(param: CurveParamP, ageInMonths: Double): Double {
        val t = ageInMonths * 4
        val base = t / param.half
        val exponent = param.slope
        val powVal = expPow(base, exponent)
        return param.max - (param.max / (1 + powVal))
}

private fun expPow(base: Double, exponent: Double): Double {
        if (base <= 0.0) return 0.0
        return kotlin.math.exp(exponent * kotlin.math.ln(base))
}

private fun generateUuidString(): String {
        return Clock.System.now().toEpochMilliseconds().toString() +
                "-" +
                kotlin.random.Random.nextInt()
}

// Données étendues des nutriments des rations
data class RationNutrimentData(
        val consultationDate: LocalDate?,
        val consultationId: String,
        val rationName: String,
        val rationId: String,
        val numero: Int,
        val proteines: Double = 0.0,
        val lipides: Double = 0.0,
        val glucides: Double = 0.0,
        val energie: Double = 0.0,
        val calcium: Double = 0.0,
        val phosphore: Double = 0.0,
        val magnesium: Double = 0.0,
        val sodium: Double = 0.0,
        val potassium: Double = 0.0,
        val matiereSeche: Double = 0.0,
        val poidsTotal: Double = 0.0,
        val isRationActuelle: Boolean = false
)

// Fonction pour calculer tous les nutriments des rations
private suspend fun calculerNutrimentsRation(
        ration: fr.vetbrain.vetnutri_mp.Data.Ration,
        referenceEv: fr.vetbrain.vetnutri_mp.Data.ReferenceEv?,
        preferencesEspece: fr.vetbrain.vetnutri_mp.Data.PreferencesEspece?,
        equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository?,
        isRationActuelle: Boolean = false
): RationNutrimentData? {
        try {
                if (ration.alimentMutableList.isEmpty()) {
                        return null
                }

                var proteines = 0.0
                var lipides = 0.0
                var glucides = 0.0
                var energie = 0.0
                var calcium = 0.0
                var phosphore = 0.0
                var magnesium = 0.0
                var sodium = 0.0
                var potassium = 0.0
                var poidsTotal = 0.0
                var humiditeTotale = 0.0

                for (alimentRation in ration.alimentMutableList) {
                        val aliment = alimentRation.aliment
                        val quantite = alimentRation.quantite

                        if (aliment == null) {
                                continue
                        }

                        // Créer un AlimentRation temporaire pour utiliser
                        // getNutrientWithComplementary
                        val alimentRationTemp =
                                fr.vetbrain.vetnutri_mp.Data.AlimentRation(
                                        aliment = aliment,
                                        quantite = quantite,
                                        weight = 1.0
                                )

                        // Récupérer les valeurs nutritionnelles avec les équations
                        proteines +=
                                alimentRationTemp.getNutrientWithComplementary(
                                        fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.PROTEINE,
                                        preferencesEspece,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0

                        lipides +=
                                alimentRationTemp.getNutrientWithComplementary(
                                        fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.LIPIDE,
                                        preferencesEspece,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0

                        glucides +=
                                alimentRationTemp.getNutrientWithComplementary(
                                        fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.GLUCIDE,
                                        preferencesEspece,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0

                        // Utiliser getEnergie() qui utilise EquationEvaluator.calculerEnergiePour100g()
                        // quand tous les paramètres sont disponibles, garantissant l'utilisation
                        // de l'équation énergétique du référentiel
                        energie +=
                                alimentRationTemp.getEnergie(
                                        referenceEv = referenceEv,
                                        equationRepository = equationRepository
                                )

                        calcium +=
                                alimentRationTemp.getNutrientWithComplementary(
                                        fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.CAL,
                                        preferencesEspece,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0

                        phosphore +=
                                alimentRationTemp.getNutrientWithComplementary(
                                        fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.PHOS,
                                        preferencesEspece,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0

                        magnesium +=
                                alimentRationTemp.getNutrientWithComplementary(
                                        fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.MG,
                                        preferencesEspece,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0

                        sodium +=
                                alimentRationTemp.getNutrientWithComplementary(
                                        fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.NA,
                                        preferencesEspece,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0

                        potassium +=
                                alimentRationTemp.getNutrientWithComplementary(
                                        fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.K,
                                        preferencesEspece,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0

                        // Humidité pour calculer la matière sèche
                        val humidite =
                                alimentRationTemp.getNutrientWithComplementary(
                                        fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.HUMIDITE,
                                        preferencesEspece,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0

                        poidsTotal += quantite
                        humiditeTotale += (humidite * quantite / 100.0)
                }

                if (energie <= 0) {
                        return null
                }

                // Calculer la matière sèche
                val matiereSeche = poidsTotal - humiditeTotale

                return RationNutrimentData(
                        consultationDate = null, // Sera rempli plus tard
                        consultationId = ration.idConsult,
                        rationName = ration.name.ifEmpty { "Ration ${ration.number}" },
                        rationId = ration.uuid,
                        numero = ration.number,
                        proteines = proteines,
                        lipides = lipides,
                        glucides = glucides,
                        energie = energie,
                        calcium = calcium,
                        phosphore = phosphore,
                        magnesium = magnesium,
                        sodium = sodium,
                        potassium = potassium,
                        matiereSeche = matiereSeche,
                        poidsTotal = poidsTotal,
                        isRationActuelle = isRationActuelle
                )
        } catch (e: Exception) {
                e.printStackTrace()
                return null
        }
}

// Fonction pour récupérer la valeur d'un nutriment depuis RationNutrimentData
private fun RationNutrimentData.getNutrimentValue(key: String): Double {
        return when (key) {
                "proteine" -> proteines
                "lipide" -> lipides
                "glucide" -> glucides
                "energie" -> energie
                "calcium" -> calcium
                "phosphore" -> phosphore
                "magnesium" -> magnesium
                "sodium" -> sodium
                "potassium" -> potassium
                else -> 0.0
        }
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
private fun arrondirPlage(range: ClosedFloatingPointRange<Float>, maxDecimals: Int = 2): ClosedFloatingPointRange<Float> {
    val start = arrondirPourAffichage(range.start, maxDecimals)
    val endInclusive = arrondirPourAffichage(range.endInclusive, maxDecimals)
    return start..endInclusive
}

// Data class pour gérer l'état du zoom et du pan (privée au fichier)
private data class ZoomPanStateView(
        val scaleX: Float = 1f,
        val scaleY: Float = 1f,
        val panX: Float = 0f,
        val panY: Float = 0f
) {
        fun reset(): ZoomPanStateView = ZoomPanStateView()
}

// Fonction pour calculer la plage zoomée/panée (privée au fichier)
private fun calculateZoomedRangeView(
        originalRange: ClosedFloatingPointRange<Float>,
        zoomPanState: ZoomPanStateView,
        isXAxis: Boolean = true
): ClosedFloatingPointRange<Float> {
        val scale = if (isXAxis) zoomPanState.scaleX else zoomPanState.scaleY
        val pan = if (isXAxis) zoomPanState.panX else zoomPanState.panY
        
        val originalSize = originalRange.endInclusive - originalRange.start
        val newSize = originalSize / scale
        
        val center = (originalRange.start + originalRange.endInclusive) / 2f
        val panOffset = pan
        
        val newStart = center - newSize / 2f + panOffset
        val newEnd = center + newSize / 2f + panOffset
        
        return arrondirPlage(newStart..newEnd)
}

// Fonction pour calculer les pourcentages d'énergie des rations
private suspend fun calculerPourcentagesEnergieRation(
        ration: fr.vetbrain.vetnutri_mp.Data.Ration,
        referenceEv: fr.vetbrain.vetnutri_mp.Data.ReferenceEv?,
        preferencesEspece: fr.vetbrain.vetnutri_mp.Data.PreferencesEspece?,
        equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository?
): RationEnergyData? {
        try {
                if (ration.alimentMutableList.isEmpty()) {
                        return null
                }

                // Calculer l'énergie totale de la ration et l'énergie des macronutriments
                var energieTotale = 0.0
                var energieProteines = 0.0
                var energieLipides = 0.0
                var poidsTotal = 0.0
                var humiditeTotale = 0.0

                for (alimentRation in ration.alimentMutableList) {
                        val aliment = alimentRation.aliment
                        val quantite = alimentRation.quantite

                        if (aliment == null) {
                                continue
                        }

                        // Récupérer les valeurs nutritionnelles
                        val proteines =
                                aliment.valMap[fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.PROTEINE]
                                        ?.value
                                        ?: 0.0
                        val lipides =
                                aliment.valMap[fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.LIPIDE]
                                        ?.value
                                        ?: 0.0
                        val humidite =
                                aliment.valMap[fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.HUMIDITE]
                                        ?.value
                                        ?: 0.0
                        val ena =
                                aliment.valMap[fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.ENA]
                                        ?.value
                                        ?: 0.0

                        // Calculer l'énergie des macronutriments (en kcal pour 100g)
                        val energieProteinesAliment = (proteines * quantite / 100.0) * 3.5
                        val energieLipidesAliment = (lipides * quantite / 100.0) * 8.5
                        val energieEnaAliment = (ena * quantite / 100.0) * 3.5

                        // Calculer le poids et l'humidité
                        poidsTotal += quantite
                        humiditeTotale += (humidite * quantite / 100.0)

                        // Ajouter à l'énergie totale
                        energieProteines += energieProteinesAliment
                        energieLipides += energieLipidesAliment
                        energieTotale += (energieProteinesAliment + energieLipidesAliment + energieEnaAliment)
                        
                }

                if (energieTotale <= 0) {
                        return null
                }

                // Calculer les pourcentages d'énergie
                val pourcentageProteines = (energieProteines / energieTotale) * 100.0
                val pourcentageLipides = (energieLipides / energieTotale) * 100.0

                // Calculer la matière sèche
                val matiereSeche = poidsTotal - humiditeTotale

                return RationEnergyData(
                        consultationDate = null, // Sera rempli plus tard
                        consultationId = ration.idConsult,
                        rationName = ration.name.ifEmpty { "Ration ${ration.number}" },
                        rationId = ration.uuid,
                        numero = ration.number,
                        proteineEnergyPercentage = pourcentageProteines,
                        lipideEnergyPercentage = pourcentageLipides,
                        energieTotale = energieTotale,
                        matiereSeche = matiereSeche,
                        poidsTotal = poidsTotal
                )
        } catch (e: Exception) {
                return null
        }
}

// Données des courbes de croissance pour chiens
private val courbesCroissanceChien =
        listOf(
                CurveP(
                        "Female < 6.5kg",
                        listOf(
                                CurveParamP("0.4%", 1.109854, 12.08458, 1.810753),
                                CurveParamP("2%", 1.535427, 14.0034, 1.597385),
                                CurveParamP("9%", 2.039161, 15.27291, 1.562812),
                                CurveParamP("25%", 2.616284, 16.01972, 1.537392),
                                CurveParamP("50%", 3.211721, 15.877, 1.579648),
                                CurveParamP("75%", 3.866406, 15.38784, 1.625081),
                                CurveParamP("91%", 4.578398, 14.65672, 1.682141),
                                CurveParamP("98%", 5.349884, 13.75693, 1.760424),
                                CurveParamP("99.6%", 6.165964, 12.8673, 1.828332)
                        ),
                        "Référence bibliographique",
                        "Canis familiaris",
                        "0",
                        12
                ),
                CurveP(
                        "Male < 6.5kg",
                        listOf(
                                CurveParamP("0.4%", 1.272559, 12.78178, 2.038467),
                                CurveParamP("2%", 1.687415, 14.07996, 1.904777),
                                CurveParamP("9%", 2.255764, 15.48813, 1.754783),
                                CurveParamP("25%", 2.91251, 16.3563, 1.731087),
                                CurveParamP("50%", 3.699072, 16.83234, 1.719296),
                                CurveParamP("75%", 4.561009, 16.77394, 1.749617),
                                CurveParamP("91%", 5.447244, 16.18983, 1.775277),
                                CurveParamP("98%", 6.330286, 15.13836, 1.820627),
                                CurveParamP("99.6%", 7.250097, 14.11965, 1.82745)
                        ),
                        "Référence bibliographique",
                        "Canis familiaris",
                        "0",
                        12
                ),
                CurveP(
                        "Female [6.5-9]kg",
                        listOf(
                                CurveParamP("0.4%", 2.863651, 16.3876, 1.835619),
                                CurveParamP("2%", 3.459252, 16.58917, 1.845262),
                                CurveParamP("9%", 4.189704, 16.83913, 1.823969),
                                CurveParamP("25%", 4.971365, 16.67714, 1.835692),
                                CurveParamP("50%", 5.885028, 16.33739, 1.786134),
                                CurveParamP("75%", 6.76091, 15.82528, 1.882438),
                                CurveParamP("91%", 7.816347, 15.42912, 1.900119),
                                CurveParamP("98%", 8.969031, 15.0315, 1.936451),
                                CurveParamP("99.6%", 10.213104, 14.70955, 1.943391)
                        ),
                        "Référence bibliographique",
                        "Canis familiaris",
                        "0",
                        12
                ),
                CurveP(
                        "Female [9-15]kg",
                        listOf(
                                CurveParamP("0.4%", 3.935947, 19.21075, 1.68409),
                                CurveParamP("2%", 5.565112, 20.72705, 1.629216),
                                CurveParamP("9%", 7.121573, 20.5551, 1.66364),
                                CurveParamP("25%", 8.647994, 19.88941, 1.686283),
                                CurveParamP("50%", 10.151493, 18.98618, 1.717085),
                                CurveParamP("75%", 11.604435, 18.076, 1.743606),
                                CurveParamP("91%", 12.938343, 17.19162, 1.768996),
                                CurveParamP("98%", 14.178662, 16.33742, 1.797089),
                                CurveParamP("99.6%", 15.292909, 15.58999, 1.824913)
                        ),
                        "Référence bibliographique",
                        "Canis familiaris",
                        "0",
                        12
                ),
                CurveP(
                        "Female [15-30]kg",
                        listOf(
                                CurveParamP("0.4%", 12.48477, 20.53315, 2.285163),
                                CurveParamP("2%", 14.89311, 19.4463, 2.321938),
                                CurveParamP("9%", 17.52067, 18.76811, 2.316963),
                                CurveParamP("25%", 20.12642, 18.34083, 2.332147),
                                CurveParamP("50%", 22.95002, 18.25048, 2.308356),
                                CurveParamP("75%", 25.67431, 18.02113, 2.28165),
                                CurveParamP("91%", 28.05483, 17.54262, 2.296781),
                                CurveParamP("98%", 30.07991, 16.82682, 2.333136),
                                CurveParamP("99.6%", 31.97356, 16.1728, 2.357102)
                        ),
                        "Référence bibliographique",
                        "Canis familiaris",
                        "0",
                        12
                ),
                CurveP(
                        "Female [30-40]kg",
                        listOf(
                                CurveParamP("0.4%", 19.73027, 23.17412, 2.201269),
                                CurveParamP("2%", 22.28789, 21.42993, 2.20492),
                                CurveParamP("9%", 24.69983, 20.03905, 2.313005),
                                CurveParamP("25%", 27.27341, 19.08208, 2.332765),
                                CurveParamP("50%", 29.79266, 18.34165, 2.337946),
                                CurveParamP("75%", 32.23491, 17.68342, 2.364681),
                                CurveParamP("91%", 35.00966, 17.18375, 2.346992),
                                CurveParamP("98%", 37.94763, 16.67522, 2.318143),
                                CurveParamP("99.6%", 40.90867, 16.4006, 2.296036)
                        ),
                        "Référence bibliographique",
                        "Canis familiaris",
                        "0",
                        12
                ),
                CurveP(
                        "Male [6.5-9]kg",
                        listOf(
                                CurveParamP("0.4%", 3.347141, 17.37989, 2.040052),
                                CurveParamP("2%", 4.102578, 17.57553, 2.009356),
                                CurveParamP("9%", 4.962623, 17.49206, 1.993075),
                                CurveParamP("25%", 5.850609, 17.11389, 1.999331),
                                CurveParamP("50%", 6.763033, 16.4929, 2.018385),
                                CurveParamP("75%", 7.754581, 15.8306, 2.033104),
                                CurveParamP("91%", 8.86195, 15.26972, 2.066372),
                                CurveParamP("98%", 10.163427, 14.78888, 2.065822),
                                CurveParamP("99.6%", 11.483938, 14.29, 2.088452)
                        ),
                        "Référence bibliographique",
                        "Canis familiaris",
                        "0",
                        12
                ),
                CurveP(
                        "Male [9-15]kg",
                        listOf(
                                CurveParamP("0.4%", 5.984049, 20.14643, 1.967823),
                                CurveParamP("2%", 9.846229, 19.27645, 2.022343),
                                CurveParamP("9%", 13.263023, 17.86271, 2.06549),
                                CurveParamP("25%", 3.899886, 19.13236, 1.930418),
                                CurveParamP("50%", 8.058689, 19.93668, 1.954662),
                                CurveParamP("75%", 11.590806, 18.56514, 2.046613),
                                CurveParamP("91%", 14.746881, 16.89629, 2.110846),
                                CurveParamP("98%", 16.172128, 15.87276, 2.149589),
                                CurveParamP("99.6%", 17.471866, 14.94524, 2.197558)
                        ),
                        "Référence bibliographique",
                        "Canis familiaris",
                        "0",
                        12
                ),
                CurveP(
                        "Male [15-30]kg",
                        listOf(
                                CurveParamP("0.4%", 14.24569, 21.71184, 2.494588),
                                CurveParamP("2%", 18.17658, 20.35052, 2.479084),
                                CurveParamP("9%", 21.85644, 19.68832, 2.425187),
                                CurveParamP("25%", 25.18467, 19.15104, 2.358881),
                                CurveParamP("50%", 28.14069, 18.80447, 2.365743),
                                CurveParamP("75%", 31.03546, 18.58387, 2.298283),
                                CurveParamP("91%", 33.55348, 18.07695, 2.276793),
                                CurveParamP("98%", 35.93635, 17.49292, 2.234906),
                                CurveParamP("99.6%", 38.02187, 16.82385, 2.191478)
                        ),
                        "Référence bibliographique",
                        "Canis familiaris",
                        "0",
                        12
                ),
                CurveP(
                        "Male [30-40]kg",
                        listOf(
                                CurveParamP("0.4%", 23.20063, 24.57251, 2.338363),
                                CurveParamP("2%", 26.14607, 22.2654, 2.399394),
                                CurveParamP("9%", 29.25459, 20.79793, 2.426847),
                                CurveParamP("25%", 32.24568, 19.60027, 2.406423),
                                CurveParamP("50%", 34.91356, 18.56937, 2.444728),
                                CurveParamP("75%", 37.58418, 17.9064, 2.443216),
                                CurveParamP("91%", 40.59932, 17.33738, 2.429597),
                                CurveParamP("98%", 43.82808, 16.94919, 2.413749),
                                CurveParamP("99.6%", 47.07196, 16.65365, 2.365383)
                        ),
                        "Référence bibliographique",
                        "Canis familiaris",
                        "0",
                        12
                )
        )

// Liste des nutriments disponibles pour les graphiques personnalisés (spécifique à cette vue)
data class ViewNutrimentOption(
        val key: String,
        val displayName: String,
        val unit: String = "g/1000 kcal"
)

private val VIEW_NUTRIMENT_OPTIONS =
        listOf(
                ViewNutrimentOption("", "Aucun"), // Option "aucun" pour l'axe Y
                // Nutriments principaux
                ViewNutrimentOption("proteine", "Protéines", "g"),
                ViewNutrimentOption("lipide", "Lipides", "g"),
                ViewNutrimentOption("energie", "Énergie", "kcal"),
                // Minéraux
                ViewNutrimentOption("calcium", "Calcium", "g"),
                ViewNutrimentOption("phosphore", "Phosphore", "g")
        )

// Fonction pour formater l'âge en années et mois
private fun formatAge(ageInYears: Double, ageInMonths: Double): String {
        return when {
                ageInYears >= 1.0 -> {
                        val years = ageInYears.toInt()
                        val remainingMonths = ((ageInYears - years) * 12).toInt()
                        if (remainingMonths > 0) {
                                "$years an${if (years > 1) "s" else ""} $remainingMonths mois"
                        } else {
                                "$years an${if (years > 1) "s" else ""}"
                        }
                }
                else -> {
                        val months = ageInMonths.toInt()
                        "$months mois"
                }
        }
}

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun AnalyseGraphiqueView(
        viewModel: AnimalDetailViewModel,
        equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository? = null,
        modifier: Modifier = Modifier
) {
        var selectedChart by remember { mutableStateOf(ChartType.EVOLUTION_POIDS) }
        var weightConeState by remember { mutableStateOf<WeightConeState?>(null) }
        var useDryMatterPer100g by remember {
                mutableStateOf(false)
        } // Toggle pour /1000 kcal vs /100g MS

        Column(
                modifier =
                        modifier.fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(AppSizes.paddingMedium),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
                // En-tête avec sélecteur de type de graphique
                GraphiqueHeader(
                        selectedChart = selectedChart,
                        onChartSelected = { selectedChart = it }
                )

                // Toggle pour /1000 kcal vs /100g MS (seulement pour les graphiques de densité)
                if (selectedChart == ChartType.DENSITE_RATIONS) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text = "/1000 kcal",
                                        style = MaterialTheme.typography.caption,
                                        color =
                                                if (!useDryMatterPer100g) VetNutriColors.Primary
                                                else
                                                        MaterialTheme.colors.onSurface.copy(
                                                                alpha = 0.7f
                                                        )
                                )
                                Switch(
                                        checked = useDryMatterPer100g,
                                        onCheckedChange = { useDryMatterPer100g = it }
                                )
                                Text(
                                        text = "/100g MS",
                                        style = MaterialTheme.typography.caption,
                                        color =
                                                if (useDryMatterPer100g) VetNutriColors.Primary
                                                else
                                                        MaterialTheme.colors.onSurface.copy(
                                                                alpha = 0.7f
                                                        )
                                )
                        }
                }

                // Affichage du graphique sélectionné
                when (selectedChart) {
                        ChartType.EVOLUTION_POIDS -> EvolutionPoidsChart(
                            viewModel,
                            weightConeState,
                            onActivateConeAction = { d, w, t -> weightConeState = WeightConeState(d, w, t) },
                            onClearCone = { weightConeState = null }
                        )
                        ChartType.RATIONS_ENERGIE ->
                                RationsEnergieChart(viewModel, equationRepository)
                        ChartType.DENSITE_RATIONS ->
                                DensiteRationsChart(
                                        viewModel,
                                        equationRepository,
                                        useDryMatterPer100g
                                )
                        ChartType.NUTRIMENTS_RATIONS ->
                                NutrimentsRationsChart(viewModel, equationRepository)
                }

                // Légende et informations
                GraphiqueLegend(selectedChart)
        }
}

// Fonction helper pour normaliser le texte de poids (virgule -> point)
private fun normaliserTextePoids(texte: String): String {
        // Remplacer la virgule par un point pour la conversion
        return texte.replace(',', '.')
}

// Fonction helper pour convertir le texte en poids (gère virgule et point)
private fun convertirTexteEnPoids(texte: String): Double? {
        val texteNormalise = normaliserTextePoids(texte)
        return texteNormalise.toDoubleOrNull()
}

@Composable
private fun AddWeightForm(viewModel: AnimalDetailViewModel) {
        var selectedDate by remember {
                mutableStateOf(
                        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                )
        }
        var weightText by remember { mutableStateOf("") }
        var showDatePicker by remember { mutableStateOf(false) }

        Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = AppSizes.elevationSmall,
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.8f)
        ) {
                Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                        Text(
                                text = "Ajouter un nouveau poids",
                                style = MaterialTheme.typography.subtitle2,
                                fontWeight = FontWeight.Bold,
                                color = VetNutriColors.Primary
                        )

                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                        // Sélecteur de date
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text = "Date: ${selectedDate}",
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.body2
                                )

                                Button(
                                        onClick = { showDatePicker = true },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Primary
                                                )
                                ) { Text("Choisir une date") }
                        }

                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                        // Champ de poids
                        OutlinedTextField(
                                value = weightText,
                                onValueChange = { nouveauTexte ->
                                        // Filtrer pour n'accepter que les chiffres, point et virgule
                                        val texteFiltre =
                                                nouveauTexte.filter { char ->
                                                        char.isDigit() || char == '.' || char == ','
                                                }
                                        // S'assurer qu'il n'y a qu'un seul séparateur décimal
                                        val pointCount = texteFiltre.count { it == '.' }
                                        val virguleCount = texteFiltre.count { it == ',' }
                                        if (pointCount <= 1 && virguleCount <= 1 && pointCount + virguleCount <= 1) {
                                                weightText = texteFiltre
                                        }
                                },
                                label = { Text("Poids (kg)") },
                                keyboardOptions =
                                        KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                        )

                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

                        // Boutons d'action
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                        ) {
                                TextButton(onClick = { viewModel.stopAddingWeight() }) {
                                        Text("Annuler")
                                }

                                Spacer(modifier = Modifier.width(AppSizes.paddingSmall))

                                val poidsValide = convertirTexteEnPoids(weightText)
                                val isPoidsValide = poidsValide != null && poidsValide > 0

                                Button(
                                        onClick = {
                                                val weight = convertirTexteEnPoids(weightText)
                                                if (weight != null && weight > 0) {
                                                        viewModel.addWeight(selectedDate, weight)
                                                        weightText = ""
                                                }
                                        },
                                        enabled = isPoidsValide,
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Primary
                                                )
                                ) { Text("Ajouter") }
                        }
                }
        }

        // Date picker
        if (showDatePicker) {
                AppDatePicker(
                        selectedDate = selectedDate,
                        onDateSelected = {
                                selectedDate = it
                                showDatePicker = false
                        },
                        label = "Date de mesure"
                )
        }
}

enum class ChartType(val displayName: String) {
        EVOLUTION_POIDS("Évolution du poids"),
        RATIONS_ENERGIE("Rations énergétiques"),
        DENSITE_RATIONS("Densité énergétique des rations"),
        NUTRIMENTS_RATIONS("Nutriments des rations")
}

@Composable
private fun GraphiqueHeader(selectedChart: ChartType, onChartSelected: (ChartType) -> Unit) {
        Card(modifier = Modifier.fillMaxWidth(), elevation = AppSizes.elevationSmall) {
                Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                Icon(
                                        imageVector = Icons.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = VetNutriColors.Primary
                                )
                                Text(
                                        text = "Analyse Graphique",
                                        style = MaterialTheme.typography.h6,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Primary
                                )
                        }

                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

                        // Sélecteur de type de graphique avec Row normale
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                ChartType.values().forEach { chartType ->
                                        Button(
                                                onClick = { onChartSelected(chartType) },
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                backgroundColor =
                                                                        if (selectedChart ==
                                                                                        chartType
                                                                        )
                                                                                VetNutriColors
                                                                                        .Primary
                                                                        else
                                                                                MaterialTheme.colors
                                                                                        .surface
                                                        ),
                                                modifier = Modifier.weight(1f)
                                        ) {
                                                Text(
                                                        text = chartType.displayName,
                                                        style = MaterialTheme.typography.caption,
                                                        color =
                                                                if (selectedChart == chartType)
                                                                        Color.White
                                                                else MaterialTheme.colors.onSurface
                                                )
                                        }
                                }
                        }
                }
        }
}

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun EvolutionPoidsChart(
    viewModel: AnimalDetailViewModel,
    coneState: WeightConeState? = null,
    onActivateConeAction: (LocalDate, Double, Double?) -> Unit = { _, _, _ -> },
    onClearCone: () -> Unit = {}
) {
    var showZoom by remember { mutableStateOf(false) }

    if (showZoom && coneState != null) {
        ConeZoomView(viewModel, coneState!!, onClose = { showZoom = false })
        return
    }

    val animal by viewModel.animal.collectAsState()
        val targetWeight = coneState?.targetWeight

        // Utiliser derivedStateOf pour forcer la recomposition quand les données changent
        val consultationsWithAge by
                remember(animal?.consultations?.size, animal?.weightHistory?.size) {
                        derivedStateOf {
                                val consultations =
                                        animal?.consultations?.sortedBy { it.date } ?: emptyList()
                                val weightHistory =
                                        animal?.weightHistory?.sortedBy { it.date } ?: emptyList()

                                // Combiner les poids des consultations et de l'historique
                                val allWeights = mutableListOf<ConsultationAgeData>()

                                // Ajouter les poids des consultations
                                consultations.forEach { consultation ->
                                        val birthDate = animal?.birthdate
                                        val consultationDate = consultation.date
                                        val weight = consultation.effectiveWeight

                                        if (birthDate != null &&
                                                        consultationDate != null &&
                                                        weight != null
                                        ) {
                                                val ageInDays =
                                                        birthDate.daysUntil(consultationDate)
                                                val ageInYears = ageInDays / 365.25
                                                val ageInMonths = ageInDays / 30.44
                                                allWeights.add(
                                                        ConsultationAgeData(
                                                                consultationDate,
                                                                ageInDays,
                                                                ageInYears,
                                                                ageInMonths,
                                                                weight,
                                                                isFromConsultation = true
                                                        )
                                                )
                                        }
                                }

                                // Ajouter les poids supplémentaires de l'historique
                                weightHistory.forEach { weightEntry ->
                                        val birthDate = animal?.birthdate
                                        val weightDate = weightEntry.date

                                        if (birthDate != null) {
                                                val ageInDays = birthDate.daysUntil(weightDate)
                                                val ageInYears = ageInDays / 365.25
                                                val ageInMonths = ageInDays / 30.44
                                                allWeights.add(
                                                        ConsultationAgeData(
                                                                weightDate,
                                                                ageInDays,
                                                                ageInYears,
                                                                ageInMonths,
                                                                weightEntry.value,
                                                                isFromConsultation = false,
                                                                weightUuid = weightEntry.uuid
                                                        )
                                                )
                                        }
                                }

                                // Trier par date
                                allWeights.sortedBy { it.date }
                        }
                }

        Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
                // États UI: sélection de la courbe de référence et affichage
                var selectedCurveIndex by remember { mutableStateOf(0) }
                var showReferenceCurves by remember { mutableStateOf(true) }
                val selectedCurve = courbesCroissanceChien.getOrNull(selectedCurveIndex)

                // Contrôles: ComboBox (Dropdown) + Checkbox
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        // Dropdown simple
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                                Button(onClick = { expanded = true }) {
                                        Text(
                                                text = selectedCurve?.description
                                                                ?: "Sélectionner courbe"
                                        )
                                }
                                DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                ) {
                                        courbesCroissanceChien.forEachIndexed { index, courbe ->
                                                DropdownMenuItem(
                                                        onClick = {
                                                                selectedCurveIndex = index
                                                                expanded = false
                                                        }
                                                ) { Text(text = courbe.description) }
                                        }
                                }
                        }

                        if (coneState != null) {
                            Button(
                                onClick = { showZoom = true },
                                colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Secondary),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.ZoomIn, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Zoom Cône & Rapport")
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                        checked = showReferenceCurves,
                                        onCheckedChange = { showReferenceCurves = it }
                                )
                                Text(text = "Afficher courbes de référence")
                        }
                }

                // Préparer données réelles (peut être vide) et fallback courbe 50% 0–12 mois
                // donneesPoids est calculé plus bas, après avoir fusionné les min/max avec le cône
                
                // Calculer les lignes du cône
                val coneLines = remember(coneState, animal?.birthdate) {
                    val birthDate = animal?.birthdate
                    if (coneState != null && birthDate != null) {
                        val startAgeDays = birthDate.daysUntil(coneState.startDate)
                        val startAgeMonths = startAgeDays / 30.44f
                        val startWeight = coneState.startWeight.toFloat()
                        val targetW = coneState.targetWeight?.toFloat()

                        val weeks = 26 // Default projection duration

                        // Function to calculate points until target
                        fun calculatePoints(percentagePerWeek: Float): List<Point<Float, Float>> {
                            val points = mutableListOf<Point<Float, Float>>()
                            points.add(Point(startAgeMonths, startWeight))

                            val effectivePercentage = if (targetW != null && targetW > startWeight && percentagePerWeek < 0) {
                                -percentagePerWeek // Invert for gain
                            } else {
                                percentagePerWeek
                            }

                            val endWeight26Weeks = startWeight * (1f + effectivePercentage * weeks)
                            var endAgeMonthsCalculated = startAgeMonths + (weeks * 7 / 30.44f)
                            var endWeightCalculated = endWeight26Weeks

                            if (targetW != null && effectivePercentage != 0f) {
                                val weeksToTarget = (targetW / startWeight - 1f) / effectivePercentage
                                if (weeksToTarget > 0) {
                                    val daysToTarget = (weeksToTarget * 7).toInt()
                                    val targetDate = coneState.startDate.plus(DatePeriod(days = daysToTarget))
                                    val targetAgeDays = birthDate.daysUntil(targetDate)
                                    endAgeMonthsCalculated = targetAgeDays / 30.44f
                                    endWeightCalculated = targetW
                                }
                            }

                            points.add(Point(endAgeMonthsCalculated, endWeightCalculated))
                            return points
                        }

                        val slowLine = calculatePoints(-0.005f)
                        val fastLine = calculatePoints(-0.02f)

                        Triple(slowLine, fastLine, targetW)
                    } else {
                        null
                    }
                }

                val courbeRef = selectedCurve
                val param50 = courbeRef?.params?.find { it.name == "50%" }
                val pointsRef0_12 =
                        param50?.let {
                                (0..12).map { mois ->
                                        val ageInMonths = mois.toFloat()
                                        val poids = calculerPoidsCroissance(it, ageInMonths.toDouble())
                                        Point(x = ageInMonths, y = poids.toFloat())
                                }
                        } ?: emptyList<Point<Float, Float>>()

                // Axes: si on a des données réelles, utiliser leur plage; sinon, utiliser 0..12
                // mois et y basé sur la courbe. Inclure aussi le cône s'il existe.
                val donneesPoids: List<Point<Float, Float>> =
                        consultationsWithAge.map { d ->
                                Point(x = d.ageInMonths.toFloat(), y = d.weight.toFloat())
                        }
                val useReal = donneesPoids.isNotEmpty()
                val pointsCone: List<Point<Float, Float>> = if (coneLines != null) coneLines.first + coneLines.second else emptyList()
                val targetY = coneLines?.third

                val minXData: Float = if (useReal) donneesPoids.minOf { it.x } else 0.0f
                val minXCone: Float = if (pointsCone.isNotEmpty()) pointsCone.minOf { it.x } else Float.MAX_VALUE
                // On prend le min global, en s'assurant de ne pas casser si un est vide (MAX_VALUE)
                val minXVal = if (minXCone < minXData) minXCone else minXData
                val minX = if (minXVal == Float.MAX_VALUE) 0f else minXVal

                val maxXData: Float = if (useReal) donneesPoids.maxOf { it.x } else 12.0f
                val maxXCone: Float = if (pointsCone.isNotEmpty()) pointsCone.maxOf { it.x } else Float.MIN_VALUE
                val maxXVal = if (maxXCone > maxXData) maxXCone else maxXData
                val maxX = if (maxXVal == Float.MIN_VALUE) 12f else maxXVal

                val xMargin = (maxX - minX).coerceAtLeast(1.0f) * 0.05f
                val xRange = arrondirPlage((minX - xMargin)..(maxX + xMargin))

                val yCandidates = (if (useReal) donneesPoids else pointsRef0_12).map { it.y } + pointsCone.map { it.y } + (if(targetY != null) listOf(targetY) else emptyList())
                val minY = yCandidates.minOrNull() ?: 0.0f
                val maxY = yCandidates.maxOrNull() ?: (minY + 5.0f)
                val yMargin = (maxY - minY).coerceAtLeast(1.0f) * 0.05f
                val yRange = arrondirPlage((minY - yMargin)..(maxY + yMargin))

                // Créer des plages Float pour KoalaPlot (déjà Float)
                val xRangeFloat = xRange
                val yRangeFloat = yRange

                val xRangeWidth = (xRange.endInclusive - xRange.start).coerceAtLeast(0.0f)
                val xTickIncrement = if (xRangeWidth > 10.0f) 3.0f else 1.0f
                val safeTickIncrement =
                        if (xRangeWidth > 0.0f) xTickIncrement.coerceAtMost(xRangeWidth) else 1.0f

                // Graphique
                GraphCard(
                        titre = "Évolution du poids corporel",
                        sousTitre = "Poids en kg selon l'âge (avec courbes de référence)"
                ) {
                        Column {
                                XYGraph(
                                        xAxisModel =
                                                KoalaPlotExtensions.createSmartXAxisModel(
                                                        range = xRangeFloat
                                                ),
                                        yAxisModel =
                                                KoalaPlotExtensions.createSmartYAxisModel(
                                                        yRangeFloat
                                                ),
                                        modifier = Modifier.height(500.dp)
                                ) {
                                        // Courbes de référence: toutes les percentiles si demandé
                                        if (showReferenceCurves && courbeRef != null) {
                                                courbeRef.params.forEach { param ->
                                                        val pts =
                                                                (0..12).map { mois ->
                                                                        val ageInMonths =
                                                                                mois.toFloat()
                                                                        val y =
                                                                                calculerPoidsCroissance(
                                                                                        param,
                                                                                        ageInMonths
                                                                                                .toDouble()
                                                                                )
                                                                        Point(
                                                                                x = ageInMonths,
                                                                                y = y.toFloat()
                                                                        )
                                                                }

                                                        if (pts.isNotEmpty()) {
                                                                // Logs DEBUG: première et dernière
                                                                // valeur
                                                                val firstY = pts.first().y
                                                                val lastY = pts.last().y

                                                                AreaPlot(
                                                                        data = pts,
                                                                        lineStyle =
                                                                                LineStyle(
                                                                                        brush =
                                                                                                SolidColor(
                                                                                                        Color.Gray
                                                                                                ),
                                                                                        strokeWidth =
                                                                                                0.2.dp
                                                                                ),
                                                                        areaStyle =
                                                                                AreaStyle(
                                                                                        brush =
                                                                                                SolidColor(
                                                                                                        Color.Green
                                                                                                ),
                                                                                        alpha =
                                                                                                0.1f,
                                                                                ),
                                                                        areaBaseline =
                                                                                AreaBaseline
                                                                                        .ConstantLine(
                                                                                                0.0f
                                                                                        )
                                                                )
                                                        }
                                                }
                                        } else if (pointsRef0_12.isNotEmpty()) {
                                                // Sinon, au minimum la 50%
                                                LinePlot(data = pointsRef0_12)
                                        }

                                        // Cône de perte de poids
                                        if (coneLines != null) {
                                            val targetW = coneLines.third
                                            
                                            // Ligne objectif (Target Weight) - Pointillés noirs
                                            if (targetW != null) {
                                                val lineTarget = listOf(
                                                    Point(xRange.start, targetW),
                                                    Point(xRange.endInclusive, targetW)
                                                )
                                                LinePlot(
                                                    data = lineTarget,
                                                    lineStyle = LineStyle(
                                                        brush = SolidColor(Color.Black),
                                                        strokeWidth = 1.5.dp,
                                                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                                                    )
                                                )
                                            }
                                            
                                            // Ligne lente (-0.5%) - Vert
                                            LinePlot(
                                                data = coneLines.first,
                                                lineStyle = LineStyle(
                                                    brush = SolidColor(Color(0xFF4CAF50)), // Green
                                                    strokeWidth = 2.dp,
                                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                                )
                                            )

                                            // Ligne rapide (-2.0%) - Orange
                                            LinePlot(
                                                data = coneLines.second,
                                                lineStyle = LineStyle(
                                                    brush = SolidColor(Color(0xFFFF5722)), // Deep Orange
                                                    strokeWidth = 2.dp,
                                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                                )
                                            )
                                        }

                                        // Courbe des données réelles de l'animal
                                        if (donneesPoids.isNotEmpty()) {
                                                LinePlot(
                                                        data = donneesPoids,
                                                        symbol = {
                                                                androidx.compose.foundation.Canvas(
                                                                        modifier =
                                                                                Modifier.size(6.dp)
                                                                ) { drawCircle(color = Color.Blue) }
                                                        }
                                                )
                                        }
                                }

                                // Légende supprimée
                        }
                }
                // Tableau des poids
                PoidsTableau(
                    consultationsWithAge,
                    viewModel,
                    onActivateCone = onActivateConeAction,
                    onClearCone = onClearCone,
                    isConeActive = coneState != null
                )
        }
}

@Composable
private fun PoidsTableau(
        consultationsWithAge: List<ConsultationAgeData>,
        viewModel: AnimalDetailViewModel,
        onActivateCone: (LocalDate, Double, Double?) -> Unit = { _, _, _ -> },
        onClearCone: () -> Unit = {},
        isConeActive: Boolean = false
) {
        val isAddingWeight = viewModel.isAddingWeight
        var targetWeightInput by remember { mutableStateOf("") }
        
        // Helper to convert input to Double safely
        fun getTargetWeight(): Double? = targetWeightInput.replace(',', '.').toDoubleOrNull()

        Card(modifier = Modifier.fillMaxWidth(), elevation = AppSizes.elevationMedium) {
                Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text = "Historique des poids",
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Primary
                                )

                                // Champ Poids Objectif
                                OutlinedTextField(
                                    value = targetWeightInput,
                                    onValueChange = { targetWeightInput = it },
                                    label = { Text("Objectif (kg)", fontSize = 10.sp) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.width(100.dp).height(55.dp),
                                    textStyle = MaterialTheme.typography.body2
                                )

                                if (isConeActive) {
                                    Button(
                                        onClick = onClearCone,
                                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE57373)),
                                        modifier = Modifier.padding(end = AppSizes.paddingSmall)
                                    ) {
                                        Text("Effacer cône", color = Color.White)
                                    }
                                }

                                Button(
                                        onClick = { viewModel.startAddingWeight() },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Secondary
                                                )
                                ) {
                                        Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Ajouter un poids",
                                                tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                                        Text("Ajouter un poids")
                                }
                        }

                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                        // Interface d'ajout de poids
                        if (isAddingWeight) {
                                AddWeightForm(viewModel)
                                Spacer(modifier = Modifier.height(AppSizes.paddingMedium))
                        }

                        if (consultationsWithAge.isEmpty()) {
                                Text(
                                        text = "Aucun poids enregistré",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        } else {
                                // En-têtes du tableau
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                        Text(
                                                text = "Date",
                                                modifier = Modifier.weight(1f),
                                                style = MaterialTheme.typography.caption,
                                                fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                                text = "Âge",
                                                modifier = Modifier.weight(1f),
                                                style = MaterialTheme.typography.caption,
                                                fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                                text = "Poids (kg)",
                                                modifier = Modifier.weight(1f),
                                                style = MaterialTheme.typography.caption,
                                                fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                                text = "Source",
                                                modifier = Modifier.weight(1f),
                                                style = MaterialTheme.typography.caption,
                                                fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                                text = "Actions",
                                                modifier = Modifier.weight(0.5f),
                                                style = MaterialTheme.typography.caption,
                                                fontWeight = FontWeight.Bold
                                        )
                                }

                                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                                // Lignes du tableau
                                consultationsWithAge.forEach { consultationData ->
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Text(
                                                        text = consultationData.date.toString(),
                                                        modifier = Modifier.weight(1f),
                                                        style = MaterialTheme.typography.caption
                                                )
                                                Text(
                                                        text =
                                                                formatAge(
                                                                        consultationData.ageInYears,
                                                                        consultationData.ageInMonths
                                                                ),
                                                        modifier = Modifier.weight(1f),
                                                        style = MaterialTheme.typography.caption
                                                )
                                                Text(
                                                        text =
                                                                GraphFormattingUtils.formatWeight(
                                                                        consultationData.weight
                                                                                .toDouble()
                                                                ),
                                                        modifier = Modifier.weight(1f),
                                                        style = MaterialTheme.typography.caption
                                                )
                                                Text(
                                                        text =
                                                                if (consultationData
                                                                                .isFromConsultation
                                                                )
                                                                        "Consultation"
                                                                else "Hors consultation",
                                                        modifier = Modifier.weight(1f),
                                                        style = MaterialTheme.typography.caption,
                                                        color =
                                                                if (consultationData
                                                                                .isFromConsultation
                                                                )
                                                                        VetNutriColors.Primary
                                                                else VetNutriColors.Secondary
                                                )
                                                // Actions: Cône et Suppression
                                                Row(
                                                    modifier = Modifier.weight(0.5f),
                                                    horizontalArrangement = Arrangement.End,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    IconButtonWithTooltip(
                                                        onClick = { onActivateCone(consultationData.date, consultationData.weight.toDouble(), getTargetWeight()) },
                                                        imageVector = Icons.Default.TrendingDown,
                                                        contentDescription = "Cône de perte",
                                                        tooltip = "Cône de perte",
                                                        tint = VetNutriColors.Secondary,
                                                        iconModifier = Modifier.size(20.dp)
                                                    )

                                                    if (!consultationData.isFromConsultation && consultationData.weightUuid != null) {
                                                        IconButtonWithTooltip(
                                                            onClick = {
                                                                viewModel.deleteWeight(consultationData.weightUuid!!)
                                                            },
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Supprimer le poids",
                                                            tooltip = "Supprimer le poids",
                                                            tint = Color.Red,
                                                            iconModifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                        }

                                        if (consultationsWithAge.last() != consultationData) {
                                                Divider(
                                                        modifier =
                                                                Modifier.padding(
                                                                        vertical =
                                                                                AppSizes.paddingSmall /
                                                                                        2
                                                                ),
                                                        color =
                                                                MaterialTheme.colors.onSurface.copy(
                                                                        alpha = 0.1f
                                                                )
                                                )
                                        }
                                }
                        }
                }
        }
}

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun RationsEnergieChart(
        viewModel: AnimalDetailViewModel,
        equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository? = null
) {
        val animal by viewModel.animal.collectAsState()
        val referenceUtilisee by viewModel.referenceUtilisee.collectAsState()
        val speciesPreferences by viewModel.speciesPreferences.collectAsState()
        val scope = rememberCoroutineScope()

        // États pour les données des rations
        var rationsEnergieData by remember { mutableStateOf<List<RationEnergyData>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var rationSelectionnee by remember { mutableStateOf<String?>(null) }

        // Calculer les données des rations de manière asynchrone
        LaunchedEffect(animal?.consultations?.size, referenceUtilisee, speciesPreferences) {
                isLoading = true
                val resultat = mutableListOf<RationEnergyData>()

                animal?.consultations?.forEachIndexed { consultationIndex, consultation ->
                        consultation.rations.forEachIndexed { rationIndex, ration ->
                                try {
                                        // 🔍 LOG DIAGNOSTIC : Vérifier les données de la ration

                                        val rationData =
                                                calculerPourcentagesEnergieRation(
                                                        ration = ration,
                                                        referenceEv = referenceUtilisee,
                                                        preferencesEspece = speciesPreferences,
                                                        equationRepository = equationRepository
                                                )

                                        rationData?.let { data ->
                                                // Ajouter la date de consultation et un numéro
                                                // unique
                                                val dataWithDate =
                                                        data.copy(
                                                                consultationDate =
                                                                        consultation.date,
                                                                numero =
                                                                        consultationIndex * 100 +
                                                                                rationIndex +
                                                                                1
                                                        )
                                                resultat.add(dataWithDate)
                                        }
                                                ?: run {}
                                } catch (e: Exception) {

                                        e.printStackTrace()
                                }
                        }
                }

                rationsEnergieData = resultat
                isLoading = false
        }

        // Vérifier si une consultation et une référence sont disponibles
        val hasConsultations = animal?.consultations?.isNotEmpty() == true
        val hasReference = referenceUtilisee != null
        val hasPreferences = speciesPreferences != null

        if (!hasConsultations) {
                // Aucune consultation disponible
                Box(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                ) {
                        Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                Text(
                                        text = "Aucune consultation disponible",
                                        style = MaterialTheme.typography.body1,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Error
                                )
                                Text(
                                        text = "Veuillez créer une consultation pour analyser les rations",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        }
                }
        } else if (!hasReference) {
                // Aucune référence disponible
                Box(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                ) {
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
                Box(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                ) {
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
        } else if (isLoading) {
                // Indicateur de chargement
                Box(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                ) {
                        Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                CircularProgressIndicator(color = VetNutriColors.Primary)
                                Text(
                                        text = "Calcul des données énergétiques des rations...",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        }
                }
        } else if (rationsEnergieData.isEmpty()) {
                // Aucune donnée disponible
                Box(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                ) {
                        Text(
                                text = "Aucune ration disponible pour l'analyse",
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                }
        } else {
                // Identifier les rations actuelles basées sur la propriété 'actual' des rations
                val rationsActuellesIds =
                        rationsEnergieData
                                .filter { data ->
                                        // Trouver la ration originale pour vérifier sa propriété
                                        // 'actual'
                                        animal?.consultations
                                                ?.flatMap { it.rations }
                                                ?.find { it.uuid == data.rationId }
                                                ?.actual == true
                                }
                                .map { it.rationId }
                                .toSet()

                // Graphique des rations
                GraphCard(
                        titre = "Répartition énergétique des rations",
                        sousTitre = "Pourcentages d'énergie des protéines vs lipides par ration"
                ) {
                        // Préparer les données pour le graphique
                        val points =
                                rationsEnergieData.map { data ->
                                        Point(
                                                x = data.proteineEnergyPercentage.toFloat(),
                                                y = data.lipideEnergyPercentage.toFloat()
                                        )
                                }

                        // Calculer les plages des axes
                        val minX = points.minOf { it.x }.coerceAtLeast(0f)
                        val maxX = points.maxOf { it.x }.coerceAtMost(100f)
                        val minY = points.minOf { it.y }.coerceAtLeast(0f)
                        val maxY = points.maxOf { it.y }.coerceAtMost(100f)

                        val baseXRange = arrondirPlage((minX - minX * 0.05f)..(maxX + maxX * 0.05f))
                        val baseYRange = arrondirPlage((minY - minY * 0.05f)..(maxY + maxY * 0.05f))
                        
                        // État du zoom/pan
                        var zoomPanState by remember { mutableStateOf(ZoomPanStateView()) }
                        val originalRanges = remember(baseXRange, baseYRange) {
                                Pair(baseXRange, baseYRange)
                        }
                        
                        // Réinitialiser le zoom quand les données changent
                        LaunchedEffect(rationsEnergieData.size) {
                                zoomPanState = ZoomPanStateView()
                        }
                        
                        // Calculer les plages zoomées
                        val xRange = calculateZoomedRangeView(originalRanges.first, zoomPanState, isXAxis = true)
                        val yRange = calculateZoomedRangeView(originalRanges.second, zoomPanState, isXAxis = false)
                        
                        // Boutons de zoom (pour desktop où le pinch ne fonctionne pas)
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                IconButtonWithTooltip(
                                        onClick = {
                                                // Zoom out
                                                val newScaleX = (zoomPanState.scaleX * 0.9f).coerceIn(0.5f, 5f)
                                                val newScaleY = (zoomPanState.scaleY * 0.9f).coerceIn(0.5f, 5f)
                                                zoomPanState = ZoomPanStateView(
                                                        scaleX = newScaleX,
                                                        scaleY = newScaleY,
                                                        panX = zoomPanState.panX,
                                                        panY = zoomPanState.panY
                                                )
                                        },
                                        imageVector = Icons.Default.ZoomOut,
                                        contentDescription = "Zoom arrière",
                                        tooltip = "Zoom arrière"
                                )
                                IconButtonWithTooltip(
                                        onClick = {
                                                // Zoom in
                                                val newScaleX = (zoomPanState.scaleX * 1.1f).coerceIn(0.5f, 5f)
                                                val newScaleY = (zoomPanState.scaleY * 1.1f).coerceIn(0.5f, 5f)
                                                zoomPanState = ZoomPanStateView(
                                                        scaleX = newScaleX,
                                                        scaleY = newScaleY,
                                                        panX = zoomPanState.panX,
                                                        panY = zoomPanState.panY
                                                )
                                        },
                                        imageVector = Icons.Default.ZoomIn,
                                        contentDescription = "Zoom avant",
                                        tooltip = "Zoom avant"
                                )
                                if (zoomPanState.scaleX != 1f || zoomPanState.scaleY != 1f || 
                                    zoomPanState.panX != 0f || zoomPanState.panY != 0f) {
                                        TextButton(
                                                onClick = { zoomPanState = ZoomPanStateView() }
                                        ) {
                                                Text("Réinitialiser", fontSize = 12.sp)
                                        }
                                }
                        }

                        // Graphique avec numéros superposés
                        BoxWithConstraints(modifier = Modifier.height(400.dp).clipToBounds()) {
                                // Graphique principal
                                XYGraph(
                                        xAxisModel =
                                                KoalaPlotExtensions.createSmartPercentageAxisModel(
                                                        xRange
                                                ),
                                        yAxisModel =
                                                KoalaPlotExtensions.createSmartPercentageAxisModel(
                                                        yRange
                                                ),
                                        xAxisTitle = "Énergie des protéines (%)",
                                        yAxisTitle = "Énergie des lipides (%)",
                                        modifier = Modifier
                                                .fillMaxSize()
                                                .clipToBounds()
                                                .pointerInput(Unit) {
                                                        detectTransformGestures { _, pan, zoom, _ ->
                                                                // Limiter le zoom entre 0.5x et 5x
                                                                val newScaleX = (zoomPanState.scaleX * zoom).coerceIn(0.5f, 5f)
                                                                val newScaleY = (zoomPanState.scaleY * zoom).coerceIn(0.5f, 5f)
                                                                
                                                                // Calculer les plages actuelles (zoomées) pour le pan
                                                                val currentXRange = calculateZoomedRangeView(originalRanges.first, zoomPanState, isXAxis = true)
                                                                val currentYRange = calculateZoomedRangeView(originalRanges.second, zoomPanState, isXAxis = false)
                                                                
                                                                // Convertir le pan en coordonnées de données (basé sur la plage actuelle)
                                                                val panXDelta = pan.x / size.width * (currentXRange.endInclusive - currentXRange.start)
                                                                val panYDelta = -pan.y / size.height * (currentYRange.endInclusive - currentYRange.start)
                                                                
                                                                zoomPanState = ZoomPanStateView(
                                                                        scaleX = newScaleX,
                                                                        scaleY = newScaleY,
                                                                        panX = zoomPanState.panX + panXDelta,
                                                                        panY = zoomPanState.panY + panYDelta
                                                                )
                                                        }
                                                }
                                ) {
                                        // 🔸 LIGNES DE RÉFÉRENCE pour la répartition énergétique
                                        // Ligne 80-x : Protéines + Lipides = 80% (ENA = 20%)
                                        val ligne80MinusX =
                                                listOf(
                                                        Point(
                                                                x = xRange.start,
                                                                y = 80f - xRange.start
                                                        ),
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

                                        // Ligne 60-x : Protéines + Lipides = 60% (ENA = 40%)
                                        val ligne60MinusX =
                                                listOf(
                                                        Point(
                                                                x = xRange.start,
                                                                y = 60f - xRange.start
                                                        ),
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

                                        // Ligne 40-x : Protéines + Lipides = 40% (ENA = 60%)
                                        val ligne40MinusX =
                                                listOf(
                                                        Point(
                                                                x = xRange.start,
                                                                y = 40f - xRange.start
                                                        ),
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

                                        // Afficher chaque point individuellement (uniquement ceux dans la plage visible)
                                        rationsEnergieData.forEachIndexed { index, data ->
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
                                                                // Point principal avec couleur
                                                                // selon sélection
                                                                val couleurPoint =
                                                                        if (data.rationId ==
                                                                                        rationSelectionnee
                                                                        ) {
                                                                                Color(
                                                                                        0xFF9C27B0
                                                                                ) // Violet
                                                                        } else {
                                                                                VetNutriColors
                                                                                        .Primary
                                                                        }

                                                                androidx.compose.foundation.Canvas(
                                                                        modifier =
                                                                                Modifier.size(12.dp)
                                                                ) {
                                                                        drawCircle(
                                                                                color =
                                                                                        couleurPoint,
                                                                                radius = 6f,
                                                                                center = center
                                                                        )
                                                                }
                                                        }
                                                )
                                        }
                                }

                                // Numéros superposés (uniquement ceux dans la plage visible)
                                val leftAxisMargin = 10.dp
                                val bottomAxisMargin = 15.dp
                                val topMargin = 10.dp
                                val rightMargin = 20.dp
                                
                                // Zone de graphique effective
                                val effectiveGraphWidth = maxWidth - leftAxisMargin - rightMargin
                                val effectiveGraphHeight = maxHeight - bottomAxisMargin - topMargin
                                
                                rationsEnergieData.forEachIndexed { index, data ->
                                        val point = points[index]
                                        
                                        // Vérifier si le point est dans la plage visible
                                        val isPointVisible = point.x >= xRange.start && 
                                                                point.x <= xRange.endInclusive &&
                                                                point.y >= yRange.start && 
                                                                point.y <= yRange.endInclusive
                                        
                                        if (!isPointVisible) return@forEachIndexed
                                        
                                        // Calculer la position du numéro
                                        val xPosition =
                                                ((point.x - xRange.start) /
                                                        (xRange.endInclusive - xRange.start))
                                        val yPosition =
                                                1f -
                                                        ((point.y - yRange.start) /
                                                                (yRange.endInclusive -
                                                                        yRange.start))

                                        // Couleur selon la sélection
                                        val numeroColor =
                                                if (data.rationId == rationSelectionnee) {
                                                        Color(0xFF9C27B0) // Violet pour sélectionné
                                                } else {
                                                        VetNutriColors.Primary // Couleur par défaut
                                                }

                                        // Vérifier si le label est visible
                                        val labelX = leftAxisMargin + (xPosition * effectiveGraphWidth.value).dp - 10.dp
                                        val labelY = topMargin + (yPosition * effectiveGraphHeight.value).dp - 30.dp
                                        
                                        val isLabelVisible = labelX >= (-20).dp && 
                                                        labelX <= maxWidth + 20.dp &&
                                                        labelY >= (-20).dp && 
                                                        labelY <= maxHeight + 20.dp
                                        
                                        if (!isLabelVisible) return@forEachIndexed

                                        Box(
                                                modifier =
                                                        Modifier.fillMaxSize()
                                                                .wrapContentSize(Alignment.TopStart)
                                                                .offset(
                                                                        x = labelX,
                                                                        y = labelY
                                                                ),
                                                contentAlignment = Alignment.Center
                                        ) {
                                                // Fond du numéro
                                                androidx.compose.foundation.Canvas(
                                                        modifier = Modifier.size(20.dp)
                                                ) {
                                                        drawCircle(
                                                                color = Color.White,
                                                                radius = 10.dp.toPx()
                                                        )
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
                                                                MaterialTheme.typography.caption
                                                                        .copy(
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Bold,
                                                                                fontSize = 12.sp
                                                                        ),
                                                        color = numeroColor
                                                )
                                        }
                                }
                        }

                        // Légende des lignes de référence
                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                // Ligne magenta 80-x
                                androidx.compose.foundation.Canvas(
                                        modifier = Modifier.size(20.dp, 2.dp)
                                ) {
                                        drawLine(
                                                color = Color.Magenta.copy(alpha = 0.7f),
                                                start =
                                                        androidx.compose.ui.geometry.Offset(
                                                                0f,
                                                                size.height / 2
                                                        ),
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
                                        text = "80-x: 20% ENA",
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Magenta.copy(alpha = 0.7f)
                                )

                                Spacer(modifier = Modifier.width(AppSizes.paddingMedium))

                                // Ligne cyan 60-x
                                androidx.compose.foundation.Canvas(
                                        modifier = Modifier.size(20.dp, 2.dp)
                                ) {
                                        drawLine(
                                                color = Color.Cyan.copy(alpha = 0.7f),
                                                start =
                                                        androidx.compose.ui.geometry.Offset(
                                                                0f,
                                                                size.height / 2
                                                        ),
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
                                        text = "60-x: 40% ENA",
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Cyan.copy(alpha = 0.7f)
                                )

                                Spacer(modifier = Modifier.width(AppSizes.paddingMedium))

                                // Ligne jaune 40-x
                                androidx.compose.foundation.Canvas(
                                        modifier = Modifier.size(20.dp, 2.dp)
                                ) {
                                        drawLine(
                                                color = Color.Yellow.copy(alpha = 0.7f),
                                                start =
                                                        androidx.compose.ui.geometry.Offset(
                                                                0f,
                                                                size.height / 2
                                                        ),
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
                                        text = "40-x: 60% ENA",
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Yellow.copy(alpha = 0.7f)
                                )
                        }

                        // Légende des rations
                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))
                        Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                Text(
                                        text = "Légende des rations :",
                                        style = MaterialTheme.typography.caption,
                                        fontWeight = FontWeight.Bold
                                )
                                rationsEnergieData.forEach { data ->
                                        Row(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .clickable {
                                                                        rationSelectionnee =
                                                                                if (rationSelectionnee ==
                                                                                                data.rationId
                                                                                )
                                                                                        null
                                                                                else data.rationId
                                                                }
                                                                .background(
                                                                        if (rationSelectionnee ==
                                                                                        data.rationId
                                                                        )
                                                                                Color(0xFF9C27B0)
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.1f
                                                                                        )
                                                                        else Color.Transparent
                                                                )
                                                                .padding(AppSizes.paddingSmall),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                                text =
                                                                        "${data.numero}. ${data.rationName}",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption,
                                                                fontWeight = FontWeight.Medium
                                                        )
                                                        Text(
                                                                text =
                                                                        "Protéines: ${GraphFormattingUtils.formatPercentage(data.proteineEnergyPercentage)} | Lipides: ${GraphFormattingUtils.formatPercentage(data.lipideEnergyPercentage)}",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption,
                                                                color =
                                                                        MaterialTheme.colors
                                                                                .onSurface.copy(
                                                                                alpha = 0.6f
                                                                        ),
                                                                fontSize = 10.sp
                                                        )
                                                }
                                                Text(
                                                        text =
                                                                "${data.consultationDate?.toString() ?: "Date inconnue"}",
                                                        style = MaterialTheme.typography.caption,
                                                        color =
                                                                MaterialTheme.colors.onSurface.copy(
                                                                        alpha = 0.7f
                                                                )
                                                )
                                        }
                                }
                        }
                }
        }
}

/** Graphique d'histogramme de la densité énergétique des rations */
@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun DensiteRationsChart(
        viewModel: AnimalDetailViewModel,
        equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository? = null,
        useDryMatterPer100g: Boolean = false
) {
        val animal by viewModel.animal.collectAsState()
        val referenceUtilisee by viewModel.referenceUtilisee.collectAsState()
        val speciesPreferences by viewModel.speciesPreferences.collectAsState()

        // États pour les données des rations
        var rationsEnergieData by remember { mutableStateOf<List<RationEnergyData>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var rationSelectionnee by remember { mutableStateOf<String?>(null) }
        var nutrimentX by remember { mutableStateOf<String?>("energie") }

        // Calculer les données des rations de manière asynchrone
        LaunchedEffect(animal?.consultations?.size, referenceUtilisee, speciesPreferences) {
                isLoading = true
                val resultat = mutableListOf<RationEnergyData>()

                animal?.consultations?.forEachIndexed { consultationIndex, consultation ->
                        consultation.rations.forEachIndexed { rationIndex, ration ->
                                try {
                                        val rationData =
                                                calculerPourcentagesEnergieRation(
                                                        ration = ration,
                                                        referenceEv = referenceUtilisee,
                                                        preferencesEspece = speciesPreferences,
                                                        equationRepository = equationRepository
                                                )

                                        rationData?.let { data ->
                                                val dataWithDate =
                                                        data.copy(
                                                                consultationDate =
                                                                        consultation.date,
                                                                numero =
                                                                        consultationIndex * 100 +
                                                                                rationIndex +
                                                                                1
                                                        )
                                                resultat.add(dataWithDate)
                                        }
                                } catch (e: Exception) {
                                        e.printStackTrace()
                                }
                        }
                }

                rationsEnergieData = resultat
                isLoading = false
        }

        // Vérifier si une consultation et une référence sont disponibles
        val hasConsultations = animal?.consultations?.isNotEmpty() == true
        val hasReference = referenceUtilisee != null
        val hasPreferences = speciesPreferences != null

        if (!hasConsultations) {
                // Aucune consultation disponible
                Box(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                ) {
                        Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                Text(
                                        text = "Aucune consultation disponible",
                                        style = MaterialTheme.typography.body1,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Error
                                )
                                Text(
                                        text = "Veuillez créer une consultation pour analyser les rations",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        }
                }
        } else if (!hasReference) {
                // Aucune référence disponible
                Box(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                ) {
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
                Box(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                ) {
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
        } else if (isLoading) {
                Box(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                ) {
                        Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                CircularProgressIndicator(color = VetNutriColors.Primary)
                                Text(
                                        text = "Calcul des données énergétiques des rations...",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        }
                }
        } else if (rationsEnergieData.isEmpty()) {
                Box(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                ) {
                        Text(
                                text = "Aucune ration disponible pour l'analyse de densité",
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                }
        } else {
                // Identifier les rations actuelles basées sur la propriété 'actual' des rations
                val rationsActuellesIds =
                        rationsEnergieData
                                .filter { data ->
                                        // Trouver la ration originale pour vérifier sa propriété
                                        // 'actual'
                                        animal?.consultations
                                                ?.flatMap { it.rations }
                                                ?.find { it.uuid == data.rationId }
                                                ?.actual == true
                                }
                                .map { it.rationId }
                                .toSet()

                // Préparer les données pour l'histogramme
                val categories = rationsEnergieData.map { "${it.numero}" }
                val densiteEnergetique =
                        rationsEnergieData.map {
                                if (useDryMatterPer100g) {
                                        // /100g MS : utiliser la matière sèche
                                        val poidsBase = it.matiereSeche
                                        if (poidsBase > 0) {
                                                (it.energieTotale / poidsBase * 100.0).toFloat()
                                        } else {
                                                0f
                                        }
                                } else {
                                        // /1000 kcal : calculer la densité énergétique pour 1000
                                        // kcal
                                        if (it.energieTotale > 0) {
                                                (it.poidsTotal / it.energieTotale * 1000.0)
                                                        .toFloat()
                                        } else {
                                                0f
                                        }
                                }
                        }

                // Calculer la plage adaptative
                val valeursValides = densiteEnergetique.filter { it.isFinite() && !it.isNaN() }
                val yRange =
                        if (valeursValides.isNotEmpty()) {
                                // Calcul adaptatif simple
                                val min = valeursValides.minOf { it }
                                val max = valeursValides.maxOf { it }
                                val range = max - min
                                val padding = maxOf(range * 0.1f, 10f)
                                arrondirPlage((maxOf(0f, min - padding))..(max + padding))
                        } else {
                                0f..1f
                        }

                // Graphique d'histogramme
                GraphCard(
                        titre = "Densité énergétique des rations",
                        sousTitre =
                                if (useDryMatterPer100g)
                                        "Énergie pour 100g de matière sèche (kcal/100g MS)"
                                else "Poids pour 1000 kcal (g/1000 kcal)"
                ) {
                        XYGraph(
                                xAxisModel = remember(categories) { CategoryAxisModel(categories) },
                                yAxisModel = remember(yRange) { KoalaPlotExtensions.createSmartDensityAxisModel(yRange) },
                                yAxisTitle =
                                        if (useDryMatterPer100g)
                                                "Densité énergétique (kcal/100g MS)"
                                        else "Densité énergétique (g/1000 kcal)",
                                modifier = Modifier.height(400.dp)
                        ) {
                                VerticalBarPlot(
                                        xData = categories,
                                        yData = densiteEnergetique,
                                        bar = { index ->
                                                val ration = rationsEnergieData[index]
                                                val couleur =
                                                        if (ration.rationId == rationSelectionnee) {
                                                                Color(
                                                                        0xFF9C27B0
                                                                ) // Violet pour sélectionnée
                                                        } else if (ration.rationId in
                                                                        rationsActuellesIds
                                                        ) {
                                                                Color(
                                                                        0xFFFF9800
                                                                ) // Orange pour rations actuelles
                                                        } else {
                                                                VetNutriColors
                                                                        .Primary // Couleur normale
                                                        }
                                                DefaultVerticalBar(brush = SolidColor(couleur))
                                        }
                                )
                        }

                        // Légende
                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))
                        Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                text = "Légende des rations :",
                                                style = MaterialTheme.typography.caption,
                                                fontWeight = FontWeight.Bold
                                        )

                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall)
                                        ) {
                                                androidx.compose.foundation.Canvas(
                                                        modifier = Modifier.size(12.dp)
                                                ) {
                                                        drawCircle(
                                                                color = Color(0xFFFF9800),
                                                                radius = 6f,
                                                                center = center
                                                        )
                                                }
                                                Text(
                                                        text = "Rations actuelles",
                                                        style = MaterialTheme.typography.caption,
                                                        color = Color(0xFFFF9800)
                                                )
                                        }
                                }

                                rationsEnergieData.forEach { data ->
                                        Row(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .clickable {
                                                                        rationSelectionnee =
                                                                                if (rationSelectionnee ==
                                                                                                data.rationId
                                                                                )
                                                                                        null
                                                                                else data.rationId
                                                                }
                                                                .background(
                                                                        when {
                                                                                rationSelectionnee ==
                                                                                        data.rationId ->
                                                                                        Color(
                                                                                                        0xFF9C27B0
                                                                                                )
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.1f
                                                                                                )
                                                                                data.rationId in
                                                                                        rationsActuellesIds ->
                                                                                        Color(
                                                                                                        0xFFFF9800
                                                                                                )
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.1f
                                                                                                )
                                                                                else ->
                                                                                        Color.Transparent
                                                                        }
                                                                )
                                                                .padding(AppSizes.paddingSmall),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Row(
                                                        modifier = Modifier.weight(1f),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically,
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(
                                                                        AppSizes.paddingSmall
                                                                )
                                                ) {
                                                        androidx.compose.foundation.Canvas(
                                                                modifier = Modifier.size(8.dp)
                                                        ) {
                                                                drawCircle(
                                                                        color =
                                                                                when {
                                                                                        data.rationId ==
                                                                                                rationSelectionnee ->
                                                                                                Color(
                                                                                                        0xFF9C27B0
                                                                                                )
                                                                                        data.rationId in
                                                                                                rationsActuellesIds ->
                                                                                                Color(
                                                                                                        0xFFFF9800
                                                                                                )
                                                                                        else ->
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                },
                                                                        radius = 4f,
                                                                        center = center
                                                                )
                                                        }
                                                        Column {
                                                                Text(
                                                                        text =
                                                                                "${data.numero}. ${data.rationName}",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .caption,
                                                                        fontWeight =
                                                                                FontWeight.Medium
                                                                )
                                                                Text(
                                                                        text =
                                                                                if (useDryMatterPer100g
                                                                                )
                                                                                        "Densité: ${GraphFormattingUtils.formatEnergyDensity(data.energieTotale / data.matiereSeche * 100.0)}"
                                                                                else
                                                                                        "Densité: ${GraphFormattingUtils.formatEnergyDensity(data.poidsTotal / data.energieTotale * 1000.0)}",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .caption,
                                                                        color =
                                                                                MaterialTheme.colors
                                                                                        .onSurface
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.6f
                                                                                        ),
                                                                        fontSize = 10.sp
                                                                )
                                                        }
                                                }
                                                Text(
                                                        text =
                                                                "${data.consultationDate?.toString() ?: "Date inconnue"}",
                                                        style = MaterialTheme.typography.caption,
                                                        color =
                                                                MaterialTheme.colors.onSurface.copy(
                                                                        alpha = 0.7f
                                                                )
                                                )
                                        }
                                }
                        }
                }

                // Sélecteurs de nutriments pour l'histogramme
               
        }
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
                        val selectedOption =
                                VIEW_NUTRIMENT_OPTIONS.find { it.key == selectedNutriment }
                        Text(
                                text = selectedOption?.displayName ?: "Sélectionner...",
                                style = MaterialTheme.typography.body2
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Déplier"
                        )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        VIEW_NUTRIMENT_OPTIONS.forEach { option ->
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

/** Graphique personnalisé pour l'analyse des nutriments des rations */
@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun NutrimentsRationsChart(
        viewModel: AnimalDetailViewModel,
        equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository? = null
) {
        val animal by viewModel.animal.collectAsState()
        val referenceUtilisee by viewModel.referenceUtilisee.collectAsState()
        val speciesPreferences by viewModel.speciesPreferences.collectAsState()

        // États pour les données des rations
        var rationsNutrimentData by remember {
                mutableStateOf<List<RationNutrimentData>>(emptyList())
        }
        var isLoading by remember { mutableStateOf(true) }
        var rationSelectionnee by remember { mutableStateOf<String?>(null) }
        var nutrimentX by remember { mutableStateOf<String?>("proteine") }
        var nutrimentY by remember {
                mutableStateOf<String?>("")
        } // "" = histogramme, autre = scatter plot

        // Calculer les données des rations de manière asynchrone
        LaunchedEffect(animal?.consultations?.size, referenceUtilisee, speciesPreferences) {
                isLoading = true
                val resultat = mutableListOf<RationNutrimentData>()

                // Identifier les rations actuelles
                val rationsActuellesIds =
                        animal?.consultations
                                ?.flatMap { it.rations }
                                ?.filter { it.actual }
                                ?.map { it.uuid }
                                ?.toSet()
                                ?: emptySet()

                animal?.consultations?.forEachIndexed { consultationIndex, consultation ->
                        consultation.rations.forEachIndexed { rationIndex, ration ->
                                try {
                                        val rationData =
                                                calculerNutrimentsRation(
                                                        ration = ration,
                                                        referenceEv = referenceUtilisee,
                                                        preferencesEspece = speciesPreferences,
                                                        equationRepository = equationRepository,
                                                        isRationActuelle =
                                                                ration.uuid in rationsActuellesIds
                                                )

                                        rationData?.let { data ->
                                                val dataWithDate =
                                                        data.copy(
                                                                consultationDate =
                                                                        consultation.date,
                                                                numero =
                                                                        consultationIndex * 100 +
                                                                                rationIndex +
                                                                                1
                                                        )
                                                resultat.add(dataWithDate)
                                        }
                                } catch (e: Exception) {
                                        e.printStackTrace()
                                }
                        }
                }

                rationsNutrimentData = resultat
                isLoading = false
        }

        // Vérifier si une consultation et une référence sont disponibles
        val hasConsultations = animal?.consultations?.isNotEmpty() == true
        val hasReference = referenceUtilisee != null
        val hasPreferences = speciesPreferences != null

        if (!hasConsultations) {
                // Aucune consultation disponible
                Box(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                ) {
                        Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                Text(
                                        text = "Aucune consultation disponible",
                                        style = MaterialTheme.typography.body1,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Error
                                )
                                Text(
                                        text = "Veuillez créer une consultation pour analyser les rations",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        }
                }
        } else if (!hasReference) {
                // Aucune référence disponible
                Box(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                ) {
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
                Box(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                ) {
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
        } else if (isLoading) {
                Box(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                ) {
                        Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                CircularProgressIndicator(color = VetNutriColors.Primary)
                                Text(
                                        text = "Calcul des données des rations...",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        }
                }
        } else if (rationsNutrimentData.isEmpty()) {
                Box(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                ) {
                        Text(
                                text = "Aucune ration disponible pour l'analyse des nutriments",
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                }
        } else {
                // Graphique personnalisé des nutriments des rations
                GraphCard(
                        titre = "Analyse nutritionnelle des rations",
                        sousTitre =
                                "Visualisation personnalisée des caractéristiques nutritionnelles"
                ) {
                        // Récupérer les informations des nutriments sélectionnés
                        val xOption = VIEW_NUTRIMENT_OPTIONS.find { it.key == nutrimentX }
                        val yOption = VIEW_NUTRIMENT_OPTIONS.find { it.key == nutrimentY }

                        // Titre dynamique selon le type de graphique
                        val titre =
                                if (nutrimentY.isNullOrEmpty()) {
                                        "Distribution de ${xOption?.displayName ?: "Nutriment"}"
                                } else {
                                        "${xOption?.displayName ?: "X"} vs ${yOption?.displayName ?: "Y"}"
                                }

                        Text(
                                text = titre,
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold,
                                color = VetNutriColors.Primary
                        )

                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

                        if (nutrimentX.isNullOrEmpty()) {
                                Text(
                                        text =
                                                "Veuillez sélectionner au moins un nutriment pour l'axe X",
                                        style = MaterialTheme.typography.body1,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        } else if (nutrimentY.isNullOrEmpty()) {
                                // 📊 HISTOGRAMME : Distribution du nutriment X
                                // Copier dans une variable locale pour permettre le smart cast
                                val nutrimentXLocal: String = nutrimentX!!
                                val valeurs =
                                        rationsNutrimentData.map {
                                                it.getNutrimentValue(nutrimentXLocal).toFloat()
                                        }
                                val categories = rationsNutrimentData.map { "${it.numero}" }

                                // Vérifier que nous avons des données valides
                                if (valeurs.isEmpty() || valeurs.all { it == 0f }) {
                                        Text(
                                                text = "Données insuffisantes pour l'histogramme",
                                                style = MaterialTheme.typography.body1,
                                                color =
                                                        MaterialTheme.colors.onSurface.copy(
                                                                alpha = 0.7f
                                                        )
                                        )
                                } else {
                                        // Calculer la plage adaptative
                                        val valeursValides =
                                                valeurs.filter { it.isFinite() && !it.isNaN() }
                                        val yRange =
                                                if (valeursValides.isNotEmpty()) {
                                                        val range = calculateAdaptiveRange(
                                                                valeursValides,
                                                                paddingPercent = 0.06f
                                                        )
                                                        arrondirPlage(range)
                                                } else {
                                                        0f..1f
                                                }

                                        XYGraph(
                                                xAxisModel =
                                                        remember(categories) {
                                                                CategoryAxisModel(categories)
                                                        },
                                                yAxisModel =
                                                        remember(yRange) {
                                                                KoalaPlotExtensions
                                                                        .createSmartYAxisModel(
                                                                                yRange
                                                                        )
                                                        },
                                                yAxisTitle =
                                                        "${xOption?.displayName} (${xOption?.unit})",
                                                modifier = Modifier.height(400.dp)
                                        ) {
                                                VerticalBarPlot(
                                                        xData = categories,
                                                        yData = valeurs,
                                                        bar = { index ->
                                                                val ration =
                                                                        rationsNutrimentData[index]
                                                                val couleur =
                                                                        if (ration.rationId ==
                                                                                        rationSelectionnee
                                                                        ) {
                                                                                Color(
                                                                                        0xFF9C27B0
                                                                                ) // Violet pour
                                                                                // sélectionné
                                                                        } else if (ration.isRationActuelle
                                                                        ) {
                                                                                Color(
                                                                                        0xFFFF9800
                                                                                ) // Orange pour
                                                                                // rations
                                                                                // actuelles
                                                                        } else {
                                                                                VetNutriColors
                                                                                        .Primary // Couleur normale
                                                                        }
                                                                DefaultVerticalBar(
                                                                        SolidColor(couleur)
                                                                )
                                                        }
                                                )
                                        }

                                        // Numéros superposés sur les barres
                                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                                rationsNutrimentData.forEachIndexed { index, ration
                                                        ->
                                                        val barWidth =
                                                                maxWidth / rationsNutrimentData.size
                                                        val xPosition =
                                                                (index.toFloat() + 0.5f) *
                                                                        (maxWidth.value /
                                                                                rationsNutrimentData
                                                                                        .size)

                                                        Box(
                                                                modifier =
                                                                        Modifier.fillMaxSize()
                                                                                .wrapContentSize(
                                                                                        Alignment
                                                                                                .TopStart
                                                                                )
                                                                                .offset(
                                                                                        x =
                                                                                                (xPosition -
                                                                                                                10)
                                                                                                        .dp,
                                                                                        y = 20.dp
                                                                                ),
                                                                contentAlignment = Alignment.Center
                                                        ) {
                                                                // Fond du numéro
                                                                androidx.compose.foundation.Canvas(
                                                                        modifier =
                                                                                Modifier.size(20.dp)
                                                                ) {
                                                                        drawCircle(
                                                                                color = Color.White,
                                                                                radius =
                                                                                        10.dp.toPx()
                                                                        )
                                                                        drawCircle(
                                                                                color =
                                                                                        if (ration.rationId ==
                                                                                                        rationSelectionnee
                                                                                        ) {
                                                                                                Color(
                                                                                                        0xFF9C27B0
                                                                                                )
                                                                                        } else if (ration.isRationActuelle
                                                                                        ) {
                                                                                                Color(
                                                                                                        0xFFFF9800
                                                                                                )
                                                                                        } else {
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                        },
                                                                                radius =
                                                                                        10.dp.toPx(),
                                                                                style =
                                                                                        Stroke(
                                                                                                width =
                                                                                                        2.dp.toPx()
                                                                                        )
                                                                        )
                                                                }

                                                                // Numéro
                                                                Text(
                                                                        text = "${ration.numero}",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .caption
                                                                                        .copy(
                                                                                                fontWeight =
                                                                                                        FontWeight
                                                                                                                .Bold,
                                                                                                fontSize =
                                                                                                        12.sp
                                                                                        ),
                                                                        color =
                                                                                if (ration.rationId ==
                                                                                                rationSelectionnee
                                                                                ) {
                                                                                        Color(
                                                                                                0xFF9C27B0
                                                                                        )
                                                                                } else if (ration.isRationActuelle
                                                                                ) {
                                                                                        Color(
                                                                                                0xFFFF9800
                                                                                        )
                                                                                } else {
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                                }
                                                                )
                                                        }
                                                }
                                        }
                                }
                        } else {
                                // 📈 SCATTER PLOT : X vs Y
                                // Copier dans des variables locales pour permettre le smart cast
                                val nutrimentXLocal: String = nutrimentX!!
                                val nutrimentYLocal: String = nutrimentY!!
                                val points =
                                        rationsNutrimentData.map { data ->
                                                Point(
                                                        x =
                                                                data.getNutrimentValue(
                                                                                nutrimentXLocal
                                                                        )
                                                                        .toFloat(),
                                                        y =
                                                                data.getNutrimentValue(
                                                                                nutrimentYLocal
                                                                        )
                                                                        .toFloat()
                                                )
                                        }

                                // Vérifier que nous avons des données valides
                                if (points.isEmpty() || points.all { it.x == 0f && it.y == 0f }) {
                                        Text(
                                                text =
                                                        "Données insuffisantes pour le graphique scatter plot",
                                                style = MaterialTheme.typography.body1,
                                                color =
                                                        MaterialTheme.colors.onSurface.copy(
                                                                alpha = 0.7f
                                                        )
                                        )
                                } else {
                                        // Calculer les plages
                                        val minX = points.minOf { it.x }.coerceAtLeast(0f)
                                        val maxX = points.maxOf { it.x }
                                        val minY = points.minOf { it.y }.coerceAtLeast(0f)
                                        val maxY = points.maxOf { it.y }

                                        val baseXRange = arrondirPlage((minX - minX * 0.05f)..(maxX + maxX * 0.05f))
                                        val baseYRange = arrondirPlage((minY - minY * 0.05f)..(maxY + maxY * 0.05f))
                                        
                                        // État du zoom/pan
                                        var zoomPanState by remember { mutableStateOf(ZoomPanStateView()) }
                                        val originalRanges = remember(baseXRange, baseYRange) {
                                                Pair(baseXRange, baseYRange)
                                        }
                                        
                                        // Réinitialiser le zoom quand les nutriments changent
                                        LaunchedEffect(nutrimentX, nutrimentY) {
                                                zoomPanState = ZoomPanStateView()
                                        }
                                        
                                        // Calculer les plages zoomées
                                        val xRange = calculateZoomedRangeView(originalRanges.first, zoomPanState, isXAxis = true)
                                        val yRange = calculateZoomedRangeView(originalRanges.second, zoomPanState, isXAxis = false)
                                        
                                        // Boutons de zoom (pour desktop où le pinch ne fonctionne pas)
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                IconButtonWithTooltip(
                                                        onClick = {
                                                                // Zoom out
                                                                val newScaleX = (zoomPanState.scaleX * 0.9f).coerceIn(0.5f, 5f)
                                                                val newScaleY = (zoomPanState.scaleY * 0.9f).coerceIn(0.5f, 5f)
                                                                zoomPanState = ZoomPanStateView(
                                                                        scaleX = newScaleX,
                                                                        scaleY = newScaleY,
                                                                        panX = zoomPanState.panX,
                                                                        panY = zoomPanState.panY
                                                                )
                                                        },
                                                        imageVector = Icons.Default.ZoomOut,
                                                        contentDescription = "Zoom arrière",
                                                        tooltip = "Zoom arrière"
                                                )
                                                IconButtonWithTooltip(
                                                        onClick = {
                                                                // Zoom in
                                                                val newScaleX = (zoomPanState.scaleX * 1.1f).coerceIn(0.5f, 5f)
                                                                val newScaleY = (zoomPanState.scaleY * 1.1f).coerceIn(0.5f, 5f)
                                                                zoomPanState = ZoomPanStateView(
                                                                        scaleX = newScaleX,
                                                                        scaleY = newScaleY,
                                                                        panX = zoomPanState.panX,
                                                                        panY = zoomPanState.panY
                                                                )
                                                        },
                                                        imageVector = Icons.Default.ZoomIn,
                                                        contentDescription = "Zoom avant",
                                                        tooltip = "Zoom avant"
                                                )
                                                if (zoomPanState.scaleX != 1f || zoomPanState.scaleY != 1f || 
                                                    zoomPanState.panX != 0f || zoomPanState.panY != 0f) {
                                                        TextButton(
                                                                onClick = { zoomPanState = ZoomPanStateView() }
                                                        ) {
                                                                Text("Réinitialiser", fontSize = 12.sp)
                                                        }
                                                }
                                        }

                                        XYGraph(
                                                xAxisModel =
                                                        KoalaPlotExtensions.createSmartXAxisModel(
                                                                xRange
                                                        ),
                                                yAxisModel =
                                                        KoalaPlotExtensions.createSmartYAxisModel(
                                                                yRange
                                                        ),
                                                xAxisTitle =
                                                        "${xOption?.displayName} (${xOption?.unit})",
                                                yAxisTitle =
                                                        "${yOption?.displayName} (${yOption?.unit})",
                                                modifier = Modifier
                                                        .height(400.dp)
                                                        .clipToBounds()
                                                        .pointerInput(Unit) {
                                                                detectTransformGestures { _, pan, zoom, _ ->
                                                                        // Limiter le zoom entre 0.5x et 5x
                                                                        val newScaleX = (zoomPanState.scaleX * zoom).coerceIn(0.5f, 5f)
                                                                        val newScaleY = (zoomPanState.scaleY * zoom).coerceIn(0.5f, 5f)
                                                                        
                                                                        // Calculer les plages actuelles (zoomées) pour le pan
                                                                        val currentXRange = calculateZoomedRangeView(originalRanges.first, zoomPanState, isXAxis = true)
                                                                        val currentYRange = calculateZoomedRangeView(originalRanges.second, zoomPanState, isXAxis = false)
                                                                        
                                                                        // Convertir le pan en coordonnées de données (basé sur la plage actuelle)
                                                                        val panXDelta = pan.x / size.width * (currentXRange.endInclusive - currentXRange.start)
                                                                        val panYDelta = -pan.y / size.height * (currentYRange.endInclusive - currentYRange.start)
                                                                        
                                                                        zoomPanState = ZoomPanStateView(
                                                                                scaleX = newScaleX,
                                                                                scaleY = newScaleY,
                                                                                panX = zoomPanState.panX + panXDelta,
                                                                                panY = zoomPanState.panY + panYDelta
                                                                        )
                                                                }
                                                        }
                                        ) {
                                                rationsNutrimentData.forEachIndexed { index, data ->
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
                                                                                if (data.rationId ==
                                                                                                rationSelectionnee
                                                                                ) {
                                                                                        Color(
                                                                                                0xFF9C27B0
                                                                                        ) // Violet
                                                                                        // pour
                                                                                        // sélectionné
                                                                                } else if (data.isRationActuelle
                                                                                ) {
                                                                                        Color(
                                                                                                0xFFFF9800
                                                                                        ) // Orange
                                                                                        // pour
                                                                                        // rations
                                                                                        // actuelles
                                                                                } else {
                                                                                        VetNutriColors
                                                                                                .Primary // Couleur normale
                                                                                }

                                                                        androidx.compose.foundation
                                                                                .Canvas(
                                                                                        modifier =
                                                                                                Modifier.size(
                                                                                                        12.dp
                                                                                                )
                                                                                ) {
                                                                                        drawCircle(
                                                                                                color =
                                                                                                        couleurPoint,
                                                                                                radius =
                                                                                                        6f,
                                                                                                center =
                                                                                                        center
                                                                                        )
                                                                                }
                                                                }
                                                        )
                                                }
                                        }

                                        // Numéros superposés
                                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                                rationsNutrimentData.forEachIndexed { index, data ->
                                                        val point = points[index]
                                                        val xPosition =
                                                                ((point.x - xRange.start) /
                                                                        (xRange.endInclusive -
                                                                                xRange.start))
                                                        val yPosition =
                                                                1f -
                                                                        ((point.y - yRange.start) /
                                                                                (yRange.endInclusive -
                                                                                        yRange.start))

                                                        // Marges typiques des axes KoalaPlot
                                                        val leftAxisMargin = 10.dp
                                                        val bottomAxisMargin = 15.dp
                                                        val topMargin = 10.dp
                                                        val rightMargin = 20.dp

                                                        // Zone de graphique effective
                                                        val effectiveGraphWidth =
                                                                maxWidth -
                                                                        leftAxisMargin -
                                                                        rightMargin
                                                        val effectiveGraphHeight =
                                                                maxHeight -
                                                                        bottomAxisMargin -
                                                                        topMargin

                                                        val numeroColor =
                                                                if (data.rationId ==
                                                                                rationSelectionnee
                                                                ) {
                                                                        Color(
                                                                                0xFF9C27B0
                                                                        ) // Violet pour sélectionné
                                                                } else if (data.isRationActuelle) {
                                                                        Color(
                                                                                0xFFFF9800
                                                                        ) // Orange pour rations
                                                                        // actuelles
                                                                } else {
                                                                        VetNutriColors
                                                                                .Primary // Couleur
                                                                        // par
                                                                        // défaut
                                                                }

                                                        // Vérifier si le label est visible
                                                        val labelX = leftAxisMargin + (xPosition * effectiveGraphWidth.value).dp - 10.dp
                                                        val labelY = topMargin + (yPosition * effectiveGraphHeight.value).dp - 30.dp
                                                        
                                                        val isLabelVisible = labelX >= (-20).dp && 
                                                                        labelX <= maxWidth + 20.dp &&
                                                                        labelY >= (-20).dp && 
                                                                        labelY <= maxHeight + 20.dp
                                                        
                                                        if (!isLabelVisible) return@forEachIndexed

                                                        Box(
                                                                modifier =
                                                                        Modifier.fillMaxSize()
                                                                                .wrapContentSize(
                                                                                        Alignment
                                                                                                .TopStart
                                                                                )
                                                                                .offset(
                                                                                        x = labelX,
                                                                                        y = labelY
                                                                                ),
                                                                contentAlignment = Alignment.Center
                                                        ) {
                                                                androidx.compose.foundation.Canvas(
                                                                        modifier =
                                                                                Modifier.size(20.dp)
                                                                ) {
                                                                        drawCircle(
                                                                                color = Color.White,
                                                                                radius =
                                                                                        10.dp.toPx()
                                                                        )
                                                                        drawCircle(
                                                                                color = numeroColor,
                                                                                radius =
                                                                                        10.dp.toPx(),
                                                                                style =
                                                                                        Stroke(
                                                                                                width =
                                                                                                        2.dp.toPx()
                                                                                        )
                                                                        )
                                                                }

                                                                Text(
                                                                        text = "${data.numero}",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .caption
                                                                                        .copy(
                                                                                                fontWeight =
                                                                                                        FontWeight
                                                                                                                .Bold,
                                                                                                fontSize =
                                                                                                        12.sp
                                                                                        ),
                                                                        color = numeroColor
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }
                }

                // Sélecteurs de nutriments
                Spacer(modifier = Modifier.height(AppSizes.paddingMedium))
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
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

                // Légende des rations
                Spacer(modifier = Modifier.height(AppSizes.paddingMedium))
                Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                        Text(
                                text = "Légende des rations :",
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.Bold
                        )
                        rationsNutrimentData.forEach { data ->
                                Row(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .clickable {
                                                                rationSelectionnee =
                                                                        if (rationSelectionnee ==
                                                                                        data.rationId
                                                                        )
                                                                                null
                                                                        else data.rationId
                                                        }
                                                        .background(
                                                                when {
                                                                        rationSelectionnee ==
                                                                                data.rationId ->
                                                                                Color(0xFF9C27B0)
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.1f
                                                                                        )
                                                                        data.isRationActuelle ->
                                                                                Color(0xFFFF9800)
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.1f
                                                                                        )
                                                                        else -> Color.Transparent
                                                                }
                                                        )
                                                        .padding(AppSizes.paddingSmall),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall)
                                        ) {
                                                androidx.compose.foundation.Canvas(
                                                        modifier = Modifier.size(8.dp)
                                                ) {
                                                        drawCircle(
                                                                color =
                                                                        when {
                                                                                data.rationId ==
                                                                                        rationSelectionnee ->
                                                                                        Color(
                                                                                                0xFF9C27B0
                                                                                        )
                                                                                data.isRationActuelle ->
                                                                                        Color(
                                                                                                0xFFFF9800
                                                                                        )
                                                                                else ->
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        },
                                                                radius = 4f,
                                                                center = center
                                                        )
                                                }
                                                Column {
                                                        Text(
                                                                text =
                                                                        "${data.numero}. ${data.rationName}",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption,
                                                                fontWeight = FontWeight.Medium
                                                        )
                                                        Text(
                                                                text =
                                                                        "Protéines: ${GraphFormattingUtils.formatDecimal(data.proteines, 1)}g | Lipides: ${GraphFormattingUtils.formatDecimal(data.lipides, 1)}g",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption,
                                                                color =
                                                                        MaterialTheme.colors
                                                                                .onSurface.copy(
                                                                                alpha = 0.6f
                                                                        ),
                                                                fontSize = 10.sp
                                                        )
                                                }
                                        }
                                        Text(
                                                text =
                                                        "${data.consultationDate?.toString() ?: "Date inconnue"}",
                                                style = MaterialTheme.typography.caption,
                                                color =
                                                        MaterialTheme.colors.onSurface.copy(
                                                                alpha = 0.7f
                                                        )
                                        )
                                }
                        }
                }
        }
}

@Composable
private fun GraphCard(titre: String, sousTitre: String? = null, content: @Composable () -> Unit) {
        Card(modifier = Modifier.fillMaxWidth(), elevation = AppSizes.elevationMedium) {
                Column(
                        modifier = Modifier.padding(AppSizes.paddingMedium),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        Text(
                                text = titre,
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold,
                                color = VetNutriColors.Primary
                        )

                        if (sousTitre != null) {
                                Text(
                                        text = sousTitre,
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        }

                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

                        content()
                }
        }
}

@Composable
private fun GraphiqueLegend(selectedChart: ChartType) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = AppSizes.elevationSmall,
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.8f)
        ) {
                Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                        Text(
                                text = "Informations",
                                style = MaterialTheme.typography.subtitle2,
                                fontWeight = FontWeight.Bold,
                                color = VetNutriColors.Primary
                        )

                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                        val infoText =
                                when (selectedChart) {
                                        ChartType.EVOLUTION_POIDS ->
                                                "Suivez l'évolution du poids de l'animal sur plusieurs mois pour détecter les tendances."
                                        ChartType.RATIONS_ENERGIE ->
                                                "Analysez la répartition énergétique des rations de chaque consultation. Chaque point représente une ration avec ses pourcentages de protéines et lipides."
                                        ChartType.DENSITE_RATIONS ->
                                                "Visualisez la densité énergétique de chaque ration. Les rations actuelles sont en orange."
                                        ChartType.NUTRIMENTS_RATIONS ->
                                                "Analysez les nutriments personnalisés des rations. Choisissez les axes X et Y pour des comparaisons spécifiques."
                                }

                        Text(
                                text = infoText,
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                }
        }
}

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun ConeZoomView(
    viewModel: AnimalDetailViewModel,
    coneState: WeightConeState,
    onClose: () -> Unit
) {
    val animal by viewModel.animal.collectAsState()
    val scope = rememberCoroutineScope()

    // Préparer les données
    val zoomData = remember(animal, coneState) {
        val startDate = coneState.startDate
        val startWeight = coneState.startWeight
        val targetWeight = coneState.targetWeight
        
        // Points réels filtrés et convertis en semaines
        val points = animal?.consultations?.flatMap { c ->
            val cDate = c.date
            if (cDate != null && cDate >= startDate && c.effectiveWeight != null) {
                listOf(Pair(cDate, c.effectiveWeight!!))
            } else emptyList()
        }?.plus(
            animal?.weightHistory?.filter { it.date >= startDate }?.map { Pair(it.date, it.value) } ?: emptyList()
        )?.distinctBy { it.first }?.sortedBy { it.first }?.map { (date, weight) ->
            val weeks = startDate.daysUntil(date) / 7.0f
            Triple(date, weeks, weight)
        } ?: emptyList()

        // Lignes du cône (calculées sur 26 semaines ou jusqu'à l'atteinte de l'objectif)
        val weeksDuration = 26

        fun calculateConeLine(basePercentage: Float): List<Point<Float, Float>> {
            val linePoints = mutableListOf<Point<Float, Float>>()
            linePoints.add(Point(0f, startWeight.toFloat()))

            // Inverser le pourcentage si prise de poids (cible > départ)
            val effectivePercentage = if (targetWeight != null && targetWeight > startWeight && basePercentage < 0) {
                -basePercentage
            } else {
                basePercentage
            }

            var finalWeeks = weeksDuration.toFloat()
            var finalWeight = (startWeight * (1 + effectivePercentage * weeksDuration)).toFloat()

            if (targetWeight != null && effectivePercentage != 0f) {
                val weeksToTarget = ((targetWeight / startWeight - 1.0) / effectivePercentage).toFloat()
                // Si l'objectif est atteint dans le futur, on arrête la ligne à l'objectif
                if (weeksToTarget > 0) {
                    finalWeeks = weeksToTarget
                    finalWeight = targetWeight.toFloat()
                }
            }

            linePoints.add(Point(finalWeeks, finalWeight))
            return linePoints
        }

        val slowLine = calculateConeLine(-0.005f)
        val fastLine = calculateConeLine(-0.02f)
        
        Triple(points, Pair(slowLine, fastLine), targetWeight)
    }

    val (realPoints, coneLines, targetW) = zoomData
    val (slowLine, fastLine) = coneLines

    // Calcul des plages
    val maxWeekData = realPoints.maxOfOrNull { it.second } ?: 0f
    val maxLineWeeks = maxOf(slowLine.last().x, fastLine.last().x)
    val maxX = maxOf(maxOf(26f, maxWeekData), maxLineWeeks)
    val xRange = 0f..maxX
    
    val allY = realPoints.map { it.third.toFloat() } + 
               slowLine.map { it.y } + 
               fastLine.map { it.y } + 
               (if (targetW != null) listOf(targetW.toFloat()) else emptyList())
               
    val minY = allY.minOrNull() ?: 0f
    val maxY = allY.maxOrNull() ?: 10f
    val yRange = calculateAdaptiveRange(allY.map { it }, paddingPercent = 0.05f)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
    ) {
        // Header avec bouton retour et export
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onClose) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.rotate(90f))
                Text("Retour")
            }
            
            Button(
                onClick = {
                    val svgGraph = generateConeGraphSvg(
                        realPoints, slowLine, fastLine, targetW, xRange, yRange
                    )
                    
                    val exportData = ExportData(
                        animal = animal,
                        ration = null,
                        reference = null,
                        title = "Suivi Perte de Poids - ${animal?.nom}",
                        additionalText = "Rapport généré le ${Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date}",
                        htmlSections = listOf(
                            HtmlSection(
                                id = genUUID(),
                                title = "Graphique d'évolution",
                                content = RichTextContent(
                                    blocks = listOf(
                                        TextBlock.RawHtml(
                                            id = genUUID(),
                                            html = svgGraph
                                        )
                                    )
                                )
                            ),
                            HtmlSection(
                                id = genUUID(),
                                title = "Données détaillées",
                                content = RichTextContent(
                                    blocks = listOf(
                                        TextBlock.Paragraph(
                                            id = genUUID(), 
                                            text = "Début du régime: ${coneState.startDate} | Poids initial: ${coneState.startWeight}kg${if(coneState.targetWeight != null) " | Objectif: ${coneState.targetWeight}kg" else ""}"
                                        ),
                                        TextBlock.TableBlock(
                                            id = genUUID(),
                                            headers = listOf("Date", "Semaine", "Poids (kg)"),
                                            rows = realPoints.map { (date, week, weight) ->
                                                listOf(
                                                    date.toString(),
                                                    GraphFormattingUtils.formatDecimal(week.toDouble(), 1),
                                                    GraphFormattingUtils.formatDecimal(weight, 2)
                                                )
                                            }
                                        )
                                    )
                                )
                            )
                        )
                    )
                    PdfExporter.exportDocument(DocumentType.RATION_ANALYSIS, exportData, "suivi_poids_${animal?.nom ?: "animal"}.pdf")
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Secondary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export PDF (Données + Graph)")
            }
        }

        // Graphique Zoomé
        GraphCard(
            titre = "Zoom sur le cône de perte de poids",
            sousTitre = "Évolution en semaines depuis le début du régime (${coneState.startDate})"
        ) {
            XYGraph(
                xAxisModel = FloatLinearAxisModel(xRange, minimumMajorTickIncrement = 2f),
                yAxisModel = KoalaPlotExtensions.createSmartYAxisModel(yRange),
                xAxisTitle = "Semaines",
                yAxisTitle = "Poids (kg)",
                modifier = Modifier.height(400.dp)
            ) {
                // Cône
                LinePlot(
                    data = slowLine,
                    lineStyle = LineStyle(brush = SolidColor(Color(0xFF4CAF50)), strokeWidth = 2.dp, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                )
                LinePlot(
                    data = fastLine,
                    lineStyle = LineStyle(brush = SolidColor(Color(0xFFFF5722)), strokeWidth = 2.dp, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                )
                
                if (targetW != null) {
                    val lineTarget = listOf(Point(xRange.start, targetW.toFloat()), Point(xRange.endInclusive, targetW.toFloat()))
                    LinePlot(
                        data = lineTarget,
                        lineStyle = LineStyle(brush = SolidColor(Color.Black), strokeWidth = 1.5.dp, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f))
                    )
                }

                // Points réels
                if (realPoints.isNotEmpty()) {
                    LinePlot(
                        data = realPoints.map { Point(it.second, it.third.toFloat()) },
                        symbol = {
                            androidx.compose.foundation.Canvas(modifier = Modifier.size(8.dp)) {
                                drawCircle(color = Color.Blue)
                            }
                        }
                    )
                }
            }
        }
        
        // Tableau des données
        Card(modifier = Modifier.fillMaxWidth(), elevation = AppSizes.elevationSmall) {
            Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                Text("Données de la période", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                realPoints.forEach { (date, week, weight) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(date.toString(), style = MaterialTheme.typography.body2)
                        Text("Semaine ${GraphFormattingUtils.formatDecimal(week.toDouble(), 1)}", style = MaterialTheme.typography.body2)
                        Text("${GraphFormattingUtils.formatDecimal(weight, 2)} kg", style = MaterialTheme.typography.body2, fontWeight = FontWeight.Bold)
                    }
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}

// Fonction pour générer un SVG du graphique de cône
private fun generateConeGraphSvg(
    realPoints: List<Triple<LocalDate, Float, Double>>,
    slowLine: List<Point<Float, Float>>,
    fastLine: List<Point<Float, Float>>,
    targetWeight: Double?,
    xRange: ClosedFloatingPointRange<Float>,
    yRange: ClosedFloatingPointRange<Float>,
    width: Int = 600,
    height: Int = 400
): String {
    val padding = 40.0
    val graphWidth = width - 2 * padding
    val graphHeight = height - 2 * padding
    
    val xMin = xRange.start
    val xMax = xRange.endInclusive
    val yMin = yRange.start
    val yMax = yRange.endInclusive
    
    fun scaleX(x: Float): Double = padding + (x - xMin) / (xMax - xMin) * graphWidth
    fun scaleY(y: Float): Double = height - padding - (y - yMin) / (yMax - yMin) * graphHeight
    
    val sb = StringBuilder()
    sb.append("<svg width='$width' height='$height' viewBox='0 0 $width $height' xmlns='http://www.w3.org/2000/svg' version='1.1'>")
    
    // Fond blanc
    sb.append("<rect width='$width' height='$height' fill='white' />")
    
    // Axes
    sb.append("<line x1='$padding' y1='${height - padding}' x2='${width - padding}' y2='${height - padding}' stroke='black' stroke-width='1' />") // X
    sb.append("<line x1='$padding' y1='$padding' x2='$padding' y2='${height - padding}' stroke='black' stroke-width='1' />") // Y
    
    // Grille et labels Y (Calcul de pas "intelligent")
    val yRangeSpan = yMax - yMin
    val targetYSteps = 5.0
    val rawYStep = yRangeSpan / targetYSteps
    val magY = 10.0.pow(kotlin.math.floor(kotlin.math.log10(rawYStep.toDouble())))
    val normY = rawYStep / magY
    val yStep = (when {
        normY < 1.5 -> 1.0
        normY < 3.5 -> 2.0
        normY < 7.5 -> 5.0
        else -> 10.0
    } * magY).toFloat()

    val startY = (kotlin.math.ceil(yMin / yStep) * yStep).toFloat()
    var currentY = startY
    
    while (currentY <= yMax + (yStep * 0.01f)) {
        val yPos = scaleY(currentY)
        // Ne dessiner que si c'est dans la zone visible
        if (yPos >= padding - 1 && yPos <= height - padding + 1) {
            sb.append("<line x1='$padding' y1='$yPos' x2='${width - padding}' y2='$yPos' stroke='lightgray' stroke-width='0.5' />")
            sb.append("<text x='${padding - 5}' y='$yPos' font-family='Arial' font-size='10' text-anchor='end' dominant-baseline='middle'>${GraphFormattingUtils.formatDecimal(currentY.toDouble(), 1)}</text>")
        }
        currentY += yStep
    }
    
    // Labels X (Semaines - Pas entier)
    val xRangeSpan = xMax - xMin
    val xStep = when {
        xRangeSpan <= 10 -> 1f
        xRangeSpan <= 20 -> 2f
        xRangeSpan <= 50 -> 5f
        else -> 10f
    }
    
    var currentX = (kotlin.math.ceil(xMin / xStep) * xStep).toFloat()
    
    while (currentX <= xMax + (xStep * 0.01f)) {
        val xPos = scaleX(currentX)
        if (xPos >= padding - 1 && xPos <= width - padding + 1) {
            sb.append("<line x1='$xPos' y1='${height - padding}' x2='$xPos' y2='${height - padding + 5}' stroke='black' stroke-width='1' />")
            sb.append("<text x='$xPos' y='${height - padding + 15}' font-family='Arial' font-size='10' text-anchor='middle'>${currentX.toInt()}</text>")
        }
        currentX += xStep
    }
    
    // Titres axes
    sb.append("<text x='${width / 2}' y='${height - 5}' font-family='Arial' font-size='12' text-anchor='middle'>Semaines</text>")
    sb.append("<text x='10' y='${height / 2}' font-family='Arial' font-size='12' text-anchor='middle' transform='rotate(-90 10 ${height / 2})'>Poids (kg)</text>")

    // Ligne Objectif
    if (targetWeight != null) {
        val yTarget = scaleY(targetWeight.toFloat())
        sb.append("<line x1='${scaleX(xMin)}' y1='$yTarget' x2='${scaleX(xMax)}' y2='$yTarget' stroke='black' stroke-width='1.5' stroke-dasharray='5,5' />")
    }
    
    // Ligne Lente (Verte)
    if (slowLine.size >= 2) {
        val x1 = scaleX(slowLine[0].x)
        val y1 = scaleY(slowLine[0].y)
        val x2 = scaleX(slowLine[1].x)
        val y2 = scaleY(slowLine[1].y)
        sb.append("<line x1='$x1' y1='$y1' x2='$x2' y2='$y2' stroke='#4CAF50' stroke-width='2' stroke-dasharray='10,5' />")
    }
    
    // Ligne Rapide (Orange)
    if (fastLine.size >= 2) {
        val x1 = scaleX(fastLine[0].x)
        val y1 = scaleY(fastLine[0].y)
        val x2 = scaleX(fastLine[1].x)
        val y2 = scaleY(fastLine[1].y)
        sb.append("<line x1='$x1' y1='$y1' x2='$x2' y2='$y2' stroke='#FF5722' stroke-width='2' stroke-dasharray='10,5' />")
    }
    
    // Points réels (Bleu)
    realPoints.forEach { (_, week, weight) ->
        val cx = scaleX(week)
        val cy = scaleY(weight.toFloat())
        sb.append("<circle cx='$cx' cy='$cy' r='4' fill='blue' />")
    }
    
    sb.append("</svg>")
    return sb.toString()
}
