package fr.vetbrain.vetnutri_mp.View.AnalyseGraphique

import kotlin.math.pow
import kotlin.math.round
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Data.PreferencesEspece
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro

// Fonction pour calculer le poids selon l'équation de croissance
fun calculerPoidsCroissance(param: CurveParamP, ageInMonths: Double): Double {
        val t = ageInMonths * 4
        val base = t / param.half
        val exponent = param.slope
        val powVal = expPow(base, exponent)
        return param.max - (param.max / (1 + powVal))
}

fun expPow(base: Double, exponent: Double): Double {
        if (base <= 0.0) return 0.0
        return kotlin.math.exp(exponent * kotlin.math.ln(base))
}

// Fonction pour calculer tous les nutriments des rations
suspend fun calculerNutrimentsRation(
        ration: Ration,
        referenceEv: ReferenceEv?,
        preferencesEspece: PreferencesEspece?,
        equationRepository: EquationRepository?,
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
                                AlimentRation(
                                        aliment = aliment,
                                        quantite = quantite,
                                        weight = 1.0
                                )

                        // Récupérer les valeurs nutritionnelles avec les équations
                        proteines +=
                                alimentRationTemp.getNutrientWithComplementary(
                                        NutrientMain.PROTEINE,
                                        preferencesEspece,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0

                        lipides +=
                                alimentRationTemp.getNutrientWithComplementary(
                                        NutrientMain.LIPIDE,
                                        preferencesEspece,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0

                        glucides +=
                                alimentRationTemp.getNutrientWithComplementary(
                                        NutrientMain.GLUCIDE,
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
                                        NutrientMacro.CAL,
                                        preferencesEspece,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0

                        phosphore +=
                                alimentRationTemp.getNutrientWithComplementary(
                                        NutrientMacro.PHOS,
                                        preferencesEspece,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0

                        magnesium +=
                                alimentRationTemp.getNutrientWithComplementary(
                                        NutrientMacro.MG,
                                        preferencesEspece,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0

                        sodium +=
                                alimentRationTemp.getNutrientWithComplementary(
                                        NutrientMacro.NA,
                                        preferencesEspece,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0

                        potassium +=
                                alimentRationTemp.getNutrientWithComplementary(
                                        NutrientMacro.K,
                                        preferencesEspece,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0

                        // Humidité pour calculer la matière sèche
                        val humidite =
                                alimentRationTemp.getNutrientWithComplementary(
                                        NutrientMain.HUMIDITE,
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
fun RationNutrimentData.getNutrimentValue(key: String): Double {
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
fun calculateAdaptiveRange(
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
fun arrondirPourAffichage(value: Float, maxDecimals: Int = 2): Float {
    val multiplier = 10.0.pow(maxDecimals.toDouble()).toFloat()
    return (round(value * multiplier) / multiplier)
}

/**
 * Arrondit une plage pour éviter les problèmes d'arrondi dans les labels d'axes
 */
fun arrondirPlage(range: ClosedFloatingPointRange<Float>, maxDecimals: Int = 2): ClosedFloatingPointRange<Float> {
    val start = arrondirPourAffichage(range.start, maxDecimals)
    val endInclusive = arrondirPourAffichage(range.endInclusive, maxDecimals)
    return start..endInclusive
}

// Fonction pour calculer la plage zoomée/panée
fun calculateZoomedRangeView(
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
suspend fun calculerPourcentagesEnergieRation(
        ration: Ration,
        referenceEv: ReferenceEv?,
        preferencesEspece: PreferencesEspece?,
        equationRepository: EquationRepository?
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
                                aliment.valMap[NutrientMain.PROTEINE]
                                        ?.value
                                        ?: 0.0
                        val lipides =
                                aliment.valMap[NutrientMain.LIPIDE]
                                        ?.value
                                        ?: 0.0
                        val humidite =
                                aliment.valMap[NutrientMain.HUMIDITE]
                                        ?.value
                                        ?: 0.0
                        val ena =
                                aliment.valMap[NutrientMain.ENA]
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

// Fonction pour formater l'âge en années et mois
fun formatAge(ageInYears: Double, ageInMonths: Double): String {
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

// Fonction helper pour normaliser le texte de poids (virgule -> point)
fun normaliserTextePoids(texte: String): String {
        // Remplacer la virgule par un point pour la conversion
        return texte.replace(',', '.')
}

// Fonction helper pour convertir le texte en poids (gère virgule et point)
fun convertirTexteEnPoids(texte: String): Double? {
        val texteNormalise = normaliserTextePoids(texte)
        return texteNormalise.toDoubleOrNull()
}

