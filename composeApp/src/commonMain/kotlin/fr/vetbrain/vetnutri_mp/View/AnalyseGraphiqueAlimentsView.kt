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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
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
        preferencesEspece: fr.vetbrain.vetnutri_mp.Data.PreferencesEspece?
): Double {
    // ✅ UTILISER LA MÊME APPROCHE QUE RATIONSVIEW : AlimentRation transitoire
    if (referenceEv != null && equationRepository != null) {
        try {
            val alimentRation = AlimentRation(aliment = aliment, quantite = 100.0, weight = 1.0)

            val _energie =
                    alimentRation.getNutrientWithComplementary(
                            nutrient = NutrientMain.ENERGIE,
                            preferences = null,
                            equationRepository = equationRepository,
                            referenceEv = referenceEv
                    )
            if (_energie != null && _energie > 0) {
                return _energie
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

    return kcalProteines + kcalLipides + kcalGlucides
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

/** Options de nutriments disponibles */
private val NUTRIMENT_OPTIONS =
        listOf(
                NutrimentOption("", "Aucun"), // Option "aucun" pour l'axe Y
                NutrimentOption("energie", "Énergie", "kcal/1000 kcal"),
                NutrimentOption("proteine", "Protéines"),
                NutrimentOption("lipide", "Lipides"),
                NutrimentOption("glucide", "Glucides"),
                NutrimentOption("fibre", "Magnésium"), // Remplacement de fibre par magnésium
                NutrimentOption("phosphore", "Phosphore"),
                NutrimentOption("calcium", "Calcium"),
                NutrimentOption("cendre", "Sodium"), // Remplacement de cendre par sodium
                NutrimentOption("eau", "Potassium") // Remplacement d'eau par potassium
        )

/** Récupère la valeur d'un nutriment depuis AlimentAnalyseData */
private fun AlimentAnalyseData.getNutrimentValue(key: String?): Double {
    return when (key) {
        "" -> 0.0 // Option "aucun"
        "energie" -> energiePer1000Kcal
        "proteine" -> proteinePer1000Kcal
        "lipide" -> lipidePer1000Kcal
        "glucide" -> glucidePer1000Kcal
        "fibre" -> fibrePer1000Kcal // Maintenant magnesium
        "phosphore" -> phosphorePer1000Kcal
        "calcium" -> calciumPer1000Kcal
        "cendre" -> cendrePer1000Kcal // Maintenant sodium
        "eau" -> eauPer1000Kcal // Maintenant potassium
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
        onClose: () -> Unit,
        modifier: Modifier = Modifier
) {
    // États pour les données d'analyse
    var alimentsAnalyses by remember { mutableStateOf<List<AlimentAnalyseData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // État pour l'aliment sélectionné (UUID de l'aliment)
    var alimentSelectionne by remember { mutableStateOf<String?>(null) }

    // État pour l'onglet actif
    var ongletActif by remember { mutableStateOf("densite_energetique") }

    // États pour les sélections de nutriments personnalisés
    var nutrimentX by remember { mutableStateOf<String?>("proteine") }
    var nutrimentY by remember { mutableStateOf<String?>("") } // Par défaut : aucun

    // Calculer les données d'analyse pour chaque aliment de manière asynchrone
    LaunchedEffect(aliments, referenceEv, equationRepository) {
        isLoading = true
        val resultat = mutableListOf<AlimentAnalyseData>()

        for (aliment in aliments) {
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

                val _energie =
                        alimentRation.getNutrientWithComplementary(
                                nutrient = NutrientMain.ENERGIE,
                                preferences = preferencesEspece,
                                equationRepository = equationRepository,
                                referenceEv = referenceEv
                        )
                                ?: 0.0

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

                val densiteEnergetique =
                        calculerDensiteEnergetiqueAsync(
                                aliment,
                                referenceEv,
                                equationRepository,
                                preferencesEspece
                        )
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

                // Calculs pour le graphique Phosphore/Protéines (par 1000 kcal)
                val proteinePer1000Kcal =
                        if (densiteEnergetique > 0) {
                            (_proteines * 1000.0) / densiteEnergetique
                        } else {
                            0.0
                        }

                val phosphorePer1000Kcal =
                        if (densiteEnergetique > 0) {
                            (phosphore * 1000.0) / densiteEnergetique
                        } else {
                            0.0
                        }

                val calciumPer1000Kcal =
                        if (densiteEnergetique > 0) {
                            (calcium * 1000.0) / densiteEnergetique
                        } else {
                            0.0
                        }

                // Calculs pour tous les nutriments en g/1000kcal
                val energiePer1000Kcal =
                        if (densiteEnergetique > 0) {
                            (_energie * 1000.0) / densiteEnergetique
                        } else {
                            0.0
                        }

                val lipidePer1000Kcal =
                        if (densiteEnergetique > 0) {
                            (_lipides * 1000.0) / densiteEnergetique
                        } else {
                            0.0
                        }

                val glucidePer1000Kcal =
                        if (densiteEnergetique > 0) {
                            (_glucides * 1000.0) / densiteEnergetique
                        } else {
                            0.0
                        }

                val magnesiumPer1000Kcal =
                        if (densiteEnergetique > 0) {
                            (magnesium * 1000.0) / densiteEnergetique
                        } else {
                            0.0
                        }

                val sodiumPer1000Kcal =
                        if (densiteEnergetique > 0) {
                            (sodium * 1000.0) / densiteEnergetique
                        } else {
                            0.0
                        }

                val potassiumPer1000Kcal =
                        if (densiteEnergetique > 0) {
                            (potassium * 1000.0) / densiteEnergetique
                        } else {
                            0.0
                        }

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
        }

        // Contenu principal - responsive selon la largeur
        if (isLoading) {
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
                    // SCROLLABLE
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
                        GraphiqueNuagePoints(
                                alimentsAnalyses = alimentsAnalyses,
                                ongletActif = ongletActif,
                                alimentSelectionne = alimentSelectionne,
                                nutrimentX = nutrimentX,
                                nutrimentY = nutrimentY,
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
                } else {
                    // Vue large : côte à côte
                    Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                    ) {
                        // Colonne gauche : liste des aliments (1/4 de la largeur)
                        Column(
                                modifier = Modifier.weight(0.25f),
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

                        // Colonne droite : graphiques (3/4 de la largeur)
                        Column(
                                modifier = Modifier.weight(0.75f),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                            GraphiqueNuagePoints(
                                    alimentsAnalyses = alimentsAnalyses,
                                    ongletActif = ongletActif,
                                    alimentSelectionne = alimentSelectionne,
                                    nutrimentX = nutrimentX,
                                    nutrimentY = nutrimentY,
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
        modifier: Modifier = Modifier
) {
    Card(modifier = modifier, elevation = AppSizes.elevationMedium) {
        Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
            // Titre dynamique selon l'onglet
            val titre =
                    when (ongletActif) {
                        "protein_lipid" -> "Répartition énergétique : Protéines vs Lipides"
                        "phosphore_protein" -> "Phosphore vs Protéines (par 1000 kcal)"
                        "calcium_phosphore" -> "Calcium vs Phosphore (par 1000 kcal)"
                        "nutriments_perso" -> {
                            val xOption = NUTRIMENT_OPTIONS.find { it.key == nutrimentX }
                            val yOption = NUTRIMENT_OPTIONS.find { it.key == nutrimentY }
                            if (nutrimentY != null && nutrimentY.isNotEmpty()) {
                                "${xOption?.displayName ?: "X"} vs ${yOption?.displayName ?: "Y"} (g/1000 kcal)"
                            } else {
                                "Distribution de ${xOption?.displayName ?: "Nutriment"} (g/1000 kcal)"
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
                            modifier = Modifier.fillMaxWidth().height(400.dp)
                    )
                } else if (ongletActif == "nutriments_perso") {
                    // ✨ GRAPHIQUE PERSONNALISÉ
                    GraphiqueNutrimentsPersonnalise(
                            alimentsAnalyses = alimentsAnalyses,
                            nutrimentX = nutrimentX ?: "proteine",
                            nutrimentY = nutrimentY,
                            alimentSelectionne = alimentSelectionne,
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

                    // Calculer les plages des axes selon le type de graphique
                    val minX = points.minOf { it.x }.coerceAtLeast(0f)
                    val maxX = points.maxOf { it.x }
                    val minY = points.minOf { it.y }.coerceAtLeast(0f)
                    val maxY = points.maxOf { it.y }

                    // Ajuster les plages selon le type de graphique
                    val (xRange, yRange) =
                            when (ongletActif) {
                                "phosphore_protein" -> {
                                    // Plages fixes pour une meilleure visibilité du graphique
                                    // phosphore/protéines
                                    val xRangeFixed = (minX - minX * 0.05f)..(maxX + maxX * 0.05f)
                                    val yRangeFixed = (minY - minY * 0.05f)..(maxY + maxY * 0.05f)
                                    Pair(xRangeFixed, yRangeFixed)
                                }
                                else -> {
                                    // Plages automatiques avec padding pour le graphique
                                    // protéines/lipides
                                    val xRangeAuto =
                                            (minX - minX * 0.05f)..(maxX.coerceAtMost(100f) +
                                                            maxX * 0.05f)
                                    val yRangeAuto =
                                            (minY - minY * 0.05f)..(maxY.coerceAtMost(100f) +
                                                            maxY * 0.05f)
                                    Pair(xRangeAuto, yRangeAuto)
                                }
                            }

                    // 🎯 Graphique avec numéros superposés
                    BoxWithConstraints(modifier = Modifier.height(400.dp)) {
                        // Graphique principal
                        XYGraph(
                                xAxisModel = FloatLinearAxisModel(range = xRange),
                                yAxisModel = FloatLinearAxisModel(range = yRange),
                                modifier = Modifier.fillMaxSize()
                        ) {
                            // Afficher chaque point individuellement avec LinePlot et symbol
                            alimentsAnalyses.forEachIndexed { index, data ->
                                val point =
                                        points[index] // Utiliser le point calculé selon l'onglet
                                // actif
                                LinePlot(
                                        data = listOf(point),
                                        symbol = {
                                            // Point principal avec couleur selon sélection
                                            val couleurPoint =
                                                    if (data.aliment.uuid == alimentSelectionne) {
                                                        Color(0xFF9C27B0) // Violet
                                                    } else {
                                                        VetNutriColors.Primary
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
                        alimentsAnalyses.forEachIndexed { index, data ->
                            val point =
                                    points[index] // Utiliser le point calculé selon l'onglet actif
                            // Calculer la position du numéro avec les vraies dimensions
                            val xPosition =
                                    ((point.x - xRange.start) /
                                            (xRange.endInclusive - xRange.start))
                            val yPosition =
                                    1f -
                                            ((point.y - yRange.start) /
                                                    (yRange.endInclusive - yRange.start))

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

                            // Couleur selon la sélection (même logique que les points)
                            val numeroColor =
                                    if (data.aliment.uuid == alimentSelectionne) {
                                        Color(0xFF9C27B0) // Violet pour sélectionné
                                    } else {
                                        VetNutriColors.Primary // Couleur par défaut pour tous
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
        Text(
                text = data.aliment.nom ?: "Sans nom",
                modifier = Modifier.weight(0.4f),
                style = MaterialTheme.typography.caption
        )
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
                    // ✨ Mode large : LazyColumn pour performance
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
        modifier: Modifier = Modifier
) {
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
                            x = data.getNutrimentValue(nutrimentX).toFloat(),
                            y = data.getNutrimentValue(nutrimentY).toFloat()
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

        // Calculer les plages avec validation
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

        val xRange =
                (safeMinX - safeMinX * 0.05f).coerceAtLeast(0f)..(safeMaxX + safeMaxX * 0.05f)
                                .coerceAtLeast(safeMinX + 0.1f)
        val yRange =
                (safeMinY - safeMinY * 0.05f).coerceAtLeast(0f)..(safeMaxY + safeMaxY * 0.05f)
                                .coerceAtLeast(safeMinY + 0.1f)

        XYGraph(
                xAxisModel = FloatLinearAxisModel(range = xRange),
                yAxisModel = FloatLinearAxisModel(range = yRange),
                xAxisTitle = "${xOption?.displayName} (${xOption?.unit})",
                yAxisTitle = "${yOption?.displayName} (${yOption?.unit})",
                modifier = modifier
        ) {
            // Afficher chaque point
            alimentsAnalyses.forEachIndexed { index, data ->
                val point = points[index]
                LinePlot(
                        data = listOf(point),
                        symbol = {
                            val couleurPoint =
                                    if (data.aliment.uuid == alimentSelectionne) {
                                        Color(0xFF9C27B0) // Violet
                                    } else {
                                        VetNutriColors.Primary
                                    }

                            androidx.compose.foundation.Canvas(modifier = Modifier.size(10.dp)) {
                                drawCircle(color = couleurPoint, radius = 5f, center = center)
                            }
                        }
                )
            }

            // Numéros superposés
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                alimentsAnalyses.forEachIndexed { index, data ->
                    val point = points[index]
                    val xPosition =
                            ((point.x - xRange.start) / (xRange.endInclusive - xRange.start))
                    val yPosition =
                            1f - ((point.y - yRange.start) / (yRange.endInclusive - yRange.start))

                    // Marges typiques des axes KoalaPlot (estimation)
                    // 🔧 Marges AJUSTÉES basées sur l'observation des logs (décalage empirique)
                    val leftAxisMargin = 10.dp // Marge pour les labels de l'axe Y (augmentée)
                    val bottomAxisMargin = 15.dp // Marge pour les labels de l'axe X (augmentée)
                    val topMargin = 10.dp // Marge supérieure
                    val rightMargin = 20.dp // Marge droite

                    // Zone de graphique effective
                    val effectiveGraphWidth = maxWidth - leftAxisMargin - rightMargin
                    val effectiveGraphHeight = maxHeight - bottomAxisMargin - topMargin

                    val numeroColor =
                            if (data.aliment.uuid == alimentSelectionne) {
                                Color(0xFF9C27B0)
                            } else {
                                VetNutriColors.Primary
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
        val valeurs = alimentsAnalyses.map { it.getNutrimentValue(nutrimentX).toFloat() }
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
                yAxisTitle = "${xOption?.displayName} (${xOption?.unit})",
                modifier = modifier
        ) {
            VerticalBarPlot(
                    xData = categories,
                    yData = valeurs,
                    bar = { index ->
                        val aliment = alimentsAnalyses[index]
                        val couleur =
                                if (aliment.aliment.uuid == alimentSelectionne) {
                                    Color(0xFF9C27B0)
                                } else {
                                    VetNutriColors.Primary
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
            yAxisTitle = "Densité énergétique (kcal/100g)",
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
                                VetNutriColors.Primary // Couleur par défaut
                            }
                    DefaultVerticalBar(SolidColor(couleur))
                }
        )
    }
}
