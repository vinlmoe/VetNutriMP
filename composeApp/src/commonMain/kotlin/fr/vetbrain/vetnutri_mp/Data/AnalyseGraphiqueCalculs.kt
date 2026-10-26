package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository

/**
 * Calculs nutritionnels des graphiques d'analyse (rations, croissance).
 * Déplacés depuis View/AnalyseGraphique/AnalyseGraphiqueUtils.kt : la couche View ne doit
 * contenir ni logique métier ni accès aux repositories. Les Composables passent par
 * AnimalDetailViewModel (calculerNutrimentsRationGraphique / calculerEnergieRationGraphique).
 */

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

                        // Récupérer les valeurs nutritionnelles avec les équations.
                        // getNutrientWithComplementary retourne des valeurs per 100g (convention
                        // identique à AlimentEv.valMap), donc on scale par quantite/100.
                        proteines +=
                                (alimentRationTemp.getNutrientWithComplementary(
                                        NutrientMain.PROTEINE,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0) * quantite / 100.0

                        lipides +=
                                (alimentRationTemp.getNutrientWithComplementary(
                                        NutrientMain.LIPIDE,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0) * quantite / 100.0

                        glucides +=
                                (alimentRationTemp.getNutrientWithComplementary(
                                        NutrientMain.GLUCIDE,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0) * quantite / 100.0

                        // getEnergie() scale déjà par quantite/100 en interne.
                        energie +=
                                alimentRationTemp.getEnergie(
                                        referenceEv = referenceEv,
                                        equationRepository = equationRepository
                                )

                        calcium +=
                                (alimentRationTemp.getNutrientWithComplementary(
                                        NutrientMacro.CAL,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0) * quantite / 100.0

                        phosphore +=
                                (alimentRationTemp.getNutrientWithComplementary(
                                        NutrientMacro.PHOS,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0) * quantite / 100.0

                        magnesium +=
                                (alimentRationTemp.getNutrientWithComplementary(
                                        NutrientMacro.MG,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0) * quantite / 100.0

                        sodium +=
                                (alimentRationTemp.getNutrientWithComplementary(
                                        NutrientMacro.NA,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0) * quantite / 100.0

                        potassium +=
                                (alimentRationTemp.getNutrientWithComplementary(
                                        NutrientMacro.K,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0) * quantite / 100.0

                        // Humidité pour calculer la matière sèche
                        val humidite =
                                alimentRationTemp.getNutrientWithComplementary(
                                        NutrientMain.HUMIDITE,
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
                        rationName = ration.name.ifEmpty { translate("graph.rationFallbackName", ration.number.toString()) },
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

// Fonction pour calculer les pourcentages d'énergie des rations
suspend fun calculerPourcentagesEnergieRation(
        ration: Ration,
        referenceEv: ReferenceEv?,
        equationRepository: EquationRepository?
): RationEnergyData? {
        try {
                if (ration.alimentMutableList.isEmpty()) {
                        return null
                }

                // Calculer l'énergie des macronutriments avec les mêmes sources que le reste de
                // l'application (valeurs directes + équations complémentaires).
                var energieTotaleMacro = 0.0
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

                        val alimentRationTemp =
                                AlimentRation(
                                        aliment = aliment,
                                        quantite = quantite,
                                        weight = 1.0
                                )

                        // Récupérer les macronutriments en g (sur la quantité réellement donnée)
                        // avec la logique de compléments utilisée dans les autres vues.
                        // getNutrientWithComplementary retourne des valeurs per 100g → scale par quantite/100.
                        val proteines =
                                (alimentRationTemp.getNutrientWithComplementary(
                                        NutrientMain.PROTEINE,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0) * quantite / 100.0
                        val lipides =
                                (alimentRationTemp.getNutrientWithComplementary(
                                        NutrientMain.LIPIDE,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0) * quantite / 100.0
                        // ENA (et non GLUCIDE) : même définition que le camembert "Origine
                        // Énergie" et que les iso-lignes ENA de RationsEnergieChart.
                        val ena =
                                (alimentRationTemp.getNutrientWithComplementary(
                                        NutrientMain.ENA,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0) * quantite / 100.0
                        val humidite =
                                alimentRationTemp.getNutrientWithComplementary(
                                        NutrientMain.HUMIDITE,
                                        equationRepository,
                                        referenceEv
                                )
                                        ?: 0.0

                        // Coefficients utilisés ailleurs dans l'application pour ces graphes.
                        val energieProteinesAliment = proteines * 3.5
                        val energieLipidesAliment = lipides * 8.5
                        val energieEnaAliment = ena * 3.5

                        // Calculer le poids et l'humidité
                        poidsTotal += quantite
                        humiditeTotale += (humidite * quantite / 100.0)

                        // Ajouter à l'énergie totale
                        energieProteines += energieProteinesAliment
                        energieLipides += energieLipidesAliment
                        energieTotaleMacro +=
                                (energieProteinesAliment +
                                        energieLipidesAliment +
                                        energieEnaAliment)
                }

                if (energieTotaleMacro <= 0) {
                        return null
                }

                // Calculer les pourcentages d'énergie
                val pourcentageProteines = (energieProteines / energieTotaleMacro) * 100.0
                val pourcentageLipides = (energieLipides / energieTotaleMacro) * 100.0

                // Calculer la matière sèche
                val matiereSeche = (poidsTotal - humiditeTotale).coerceAtLeast(0.0)

                // Énergie totale de la ration pour les graphes de densité.
                // On conserve getEnergie() pour respecter l'équation énergétique du référentiel.
                var energieTotaleRation = 0.0
                for (alimentRation in ration.alimentMutableList) {
                        val aliment = alimentRation.aliment ?: continue
                        val alimentRationTemp =
                                AlimentRation(
                                        aliment = aliment,
                                        quantite = alimentRation.quantite,
                                        weight = 1.0
                                )
                        energieTotaleRation +=
                                alimentRationTemp.getEnergie(
                                        referenceEv = referenceEv,
                                        equationRepository = equationRepository
                                )
                }

                return RationEnergyData(
                        consultationDate = null, // Sera rempli plus tard
                        consultationId = ration.idConsult,
                        rationName = ration.name.ifEmpty { translate("graph.rationFallbackName", ration.number.toString()) },
                        rationId = ration.uuid,
                        numero = ration.number,
                        proteineEnergyPercentage = pourcentageProteines,
                        lipideEnergyPercentage = pourcentageLipides,
                        energieTotale = energieTotaleRation,
                        matiereSeche = matiereSeche,
                        poidsTotal = poidsTotal
                )
        } catch (e: Exception) {
                return null
        }
}
