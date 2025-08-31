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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Components.AppDatePicker
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import io.github.koalaplot.core.*
import io.github.koalaplot.core.line.AreaBaseline
import io.github.koalaplot.core.line.AreaPlot
import io.github.koalaplot.core.line.LinePlot
import io.github.koalaplot.core.style.AreaStyle
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.*
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.Point
import io.github.koalaplot.core.xygraph.XYGraph
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

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
        val energieTotale: Double
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
                        

                        // Calculer l'énergie des macronutriments (en kcal pour 100g)
                        val energieProteinesAliment = (proteines * quantite / 100.0) * 3.5
                        val energieLipidesAliment = (lipides * quantite / 100.0) * 8.5
                       
                        // Ajouter à l'énergie totale
                        energieProteines += energieProteinesAliment
                        energieLipides += energieLipidesAliment
                        energieTotale += alimentRation.getEnergie(referenceEv, preferencesEspece, equationRepository)
                println("energieTotale: $energieTotale")
                }

                if (energieTotale <= 0) {
                        return null
                }

                // Calculer les pourcentages d'énergie
                val pourcentageProteines = (energieProteines / energieTotale) * 100.0
                val pourcentageLipides = (energieLipides / energieTotale) * 100.0

                return RationEnergyData(
                        consultationDate = null, // Sera rempli plus tard
                        consultationId = ration.idConsult,
                        rationName = ration.name.ifEmpty { "Ration ${ration.number}" },
                        rationId = ration.uuid,
                        numero = ration.number,
                        proteineEnergyPercentage = pourcentageProteines,
                        lipideEnergyPercentage = pourcentageLipides,
                        energieTotale = energieTotale
                )
        } catch (e: Exception) {
                e.printStackTrace()
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

                // Affichage du graphique sélectionné
                when (selectedChart) {
                        ChartType.EVOLUTION_POIDS -> EvolutionPoidsChart(viewModel)
                        ChartType.RATIONS_ENERGIE -> RationsEnergieChart(viewModel, equationRepository)
                        ChartType.COMPARAISON_BESOINS -> ComparaisonBesoinsChart()
                        ChartType.REPARTITION_ENERGIE -> RepartitionEnergieChart()
                }

                // Légende et informations
                GraphiqueLegend(selectedChart)
        }
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
                                onValueChange = { weightText = it },
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

                                Button(
                                        onClick = {
                                                val weight = weightText.toDoubleOrNull()
                                                if (weight != null && weight > 0) {
                                                        viewModel.addWeight(selectedDate, weight)
                                                        weightText = ""
                                                }
                                        },
                                        enabled =
                                                weightText.toDoubleOrNull() != null &&
                                                        weightText.toDoubleOrNull()!! > 0,
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
        COMPARAISON_BESOINS("Apports vs Besoins"),
        REPARTITION_ENERGIE("Répartition énergétique")
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
private fun EvolutionPoidsChart(viewModel: AnimalDetailViewModel) {
        val animal by viewModel.animal.collectAsState()

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
                                        val weight = consultation.weight

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

                        Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                        checked = showReferenceCurves,
                                        onCheckedChange = { showReferenceCurves = it }
                                )
                                Text(text = "Afficher courbes de référence")
                        }
                }

                // Préparer données réelles (peut être vide) et fallback courbe 50% 0–12 mois
                val donneesPoids =
                        consultationsWithAge.map { d ->
                                Point(x = d.ageInMonths.toFloat(), y = d.weight.toFloat())
                        }
                val courbeRef = selectedCurve
                val param50 = courbeRef?.params?.find { it.name == "50%" }
                val pointsRef0_12 =
                        param50?.let {
                                (0..12).map { mois ->
                                        val ageInMonths = mois.toFloat()
                                        val poids =
                                                calculerPoidsCroissance(it, ageInMonths.toDouble())
                                        Point(x = ageInMonths, y = poids.toFloat())
                                }
                        }
                                ?: emptyList()

                // Axes: si on a des données réelles, utiliser leur plage; sinon, utiliser 0..12
                // mois et y basé sur la courbe
                val useReal = donneesPoids.isNotEmpty()
                val minX = if (useReal) donneesPoids.minOf { it.x } else 0.0f
                val maxX = if (useReal) donneesPoids.maxOf { it.x } else 12.0f
                val xMargin = (maxX - minX).coerceAtLeast(1.0f) * 0.05f
                val xRange = (minX - xMargin)..(maxX + xMargin)

                val yCandidates = (if (useReal) donneesPoids else pointsRef0_12).map { it.y }
                val minY = yCandidates.minOrNull() ?: 0.0f
                val maxY = yCandidates.maxOrNull() ?: (minY + 5.0f)
                val yMargin = (maxY - minY).coerceAtLeast(1.0f) * 0.05f
                val yRange = (minY - yMargin)..(maxY + yMargin)

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
                                                FloatLinearAxisModel(
                                                        range = xRangeFloat,
                                                        minimumMajorTickIncrement =
                                                                safeTickIncrement
                                                ),
                                        yAxisModel = FloatLinearAxisModel(range = yRangeFloat),
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
                PoidsTableau(consultationsWithAge, viewModel)
        }
}

@Composable
private fun PoidsTableau(
        consultationsWithAge: List<ConsultationAgeData>,
        viewModel: AnimalDetailViewModel
) {
        val isAddingWeight = viewModel.isAddingWeight

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
                                                                fr.vetbrain.vetnutri_mp.Utils
                                                                        .TextUtils.formatDecimal(
                                                                        consultationData.weight
                                                                                .toDouble(),
                                                                        1
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
                                                // Bouton de suppression pour les poids hors
                                                // consultation
                                                if (!consultationData.isFromConsultation &&
                                                                consultationData.weightUuid != null
                                                ) {
                                                        IconButton(
                                                                onClick = {
                                                                        viewModel.deleteWeight(
                                                                                consultationData
                                                                                        .weightUuid!!
                                                                        )
                                                                },
                                                                modifier = Modifier.weight(0.5f)
                                                        ) {
                                                                Icon(
                                                                        imageVector =
                                                                                Icons.Default
                                                                                        .Delete,
                                                                        contentDescription =
                                                                                "Supprimer le poids",
                                                                        tint = Color.Red,
                                                                        modifier =
                                                                                Modifier.size(16.dp)
                                                                )
                                                        }
                                                } else {
                                                        Spacer(modifier = Modifier.weight(0.5f))
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

        if (isLoading) {
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

                        val xRange = (minX - minX * 0.05f)..(maxX + maxX * 0.05f)
                        val yRange = (minY - minY * 0.05f)..(maxY + maxY * 0.05f)

                        // Graphique avec numéros superposés
                        BoxWithConstraints(modifier = Modifier.height(400.dp)) {
                                // Graphique principal
                                XYGraph(
                                        xAxisModel = FloatLinearAxisModel(range = xRange),
                                        yAxisModel = FloatLinearAxisModel(range = yRange),
                                        xAxisTitle = "Pourcentage d'énergie des protéines (%)",
                                        yAxisTitle = "Pourcentage d'énergie des lipides (%)",
                                        modifier = Modifier.fillMaxSize()
                                ) {
                                        // Afficher chaque point individuellement
                                        rationsEnergieData.forEachIndexed { index, data ->
                                                val point = points[index]
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

                                // Numéros superposés
                                rationsEnergieData.forEachIndexed { index, data ->
                                        val point = points[index]
                                        // Calculer la position du numéro
                                        val xPosition =
                                                ((point.x - xRange.start) /
                                                        (xRange.endInclusive - xRange.start))
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
                                                maxWidth - leftAxisMargin - rightMargin
                                        val effectiveGraphHeight =
                                                maxHeight - bottomAxisMargin - topMargin

                                        // Couleur selon la sélection
                                        val numeroColor =
                                                if (data.rationId == rationSelectionnee) {
                                                        Color(0xFF9C27B0) // Violet pour sélectionné
                                                } else {
                                                        VetNutriColors.Primary // Couleur par défaut
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
                                                                                        10.dp,
                                                                        y =
                                                                                topMargin +
                                                                                        (yPosition *
                                                                                                        effectiveGraphHeight
                                                                                                                .value)
                                                                                                .dp -
                                                                                        30.dp
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
                                                Text(
                                                        text = "${data.numero}. ${data.rationName}",
                                                        style = MaterialTheme.typography.caption,
                                                        modifier = Modifier.weight(1f)
                                                )
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

@Composable
private fun ComparaisonBesoinsChart() {
        // Données d'exemple pour comparaison apports vs besoins
        val nutriments = listOf("Protéines", "Calcium", "Phosphore", "Vitamine A", "Fer")
        val apports = listOf(120.0, 95.0, 110.0, 85.0, 105.0) // en % des besoins

        GraphCard(
                titre = "Apports vs Besoins nutritionnels",
                sousTitre = "En % des recommandations (100% = ligne de référence)"
        ) {
                // Implémentation simplifiée d'un graphique en barres
                Column(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceEvenly
                ) {
                        nutriments.forEachIndexed { index, nutriment ->
                                val apport = apports[index]
                                val couleur =
                                        when {
                                                apport < 80.0 -> Color.Red
                                                apport < 100.0 -> Color(0xFFFFA500) // Orange
                                                apport > 120.0 -> Color.Blue
                                                else -> Color.Green
                                        }

                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                text = nutriment,
                                                modifier = Modifier.width(100.dp),
                                                style = MaterialTheme.typography.caption
                                        )

                                        LinearProgressIndicator(
                                                progress =
                                                        (apport / 150.0)
                                                                .coerceIn(0.0, 1.0)
                                                                .toFloat(),
                                                modifier = Modifier.weight(1.0f).height(20.dp),
                                                color = couleur,
                                                backgroundColor = Color.LightGray.copy(alpha = 0.3f)
                                        )

                                        Text(
                                                text = "${apport.toInt()}%",
                                                modifier = Modifier.width(50.dp),
                                                style = MaterialTheme.typography.caption,
                                                color = couleur
                                        )
                                }
                        }
                }
        }
}

@Composable
private fun RepartitionEnergieChart() {
        // Données d'exemple pour la répartition énergétique
        val donneesEnergie = remember {
                mapOf(
                        "Protéines\n(4 kcal/g)" to 30.0,
                        "Lipides\n(9 kcal/g)" to 35.0,
                        "Glucides\n(4 kcal/g)" to 35.0
                )
        }

        val couleurs = listOf(Color(0xFF3F51B5), Color(0xFFE91E63), Color(0xFFFF5722))

        GraphCard(
                titre = "Répartition énergétique",
                sousTitre = "Distribution calorique par macronutriment"
        ) {
                Column(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceEvenly
                ) {
                        donneesEnergie.entries.forEachIndexed { index, (nutriment, valeur) ->
                                val couleur = couleurs[index % couleurs.size]

                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                text = nutriment,
                                                modifier = Modifier.width(120.dp),
                                                style = MaterialTheme.typography.caption
                                        )

                                        LinearProgressIndicator(
                                                progress = (valeur / 100.0).toFloat(),
                                                modifier = Modifier.weight(1.0f).height(25.dp),
                                                color = couleur,
                                                backgroundColor = Color.LightGray.copy(alpha = 0.3f)
                                        )

                                        Text(
                                                text = "${valeur.toInt()}%",
                                                modifier = Modifier.width(50.dp),
                                                style = MaterialTheme.typography.caption,
                                                color = couleur
                                        )
                                }
                        }
                }
        }
}

@Composable
private fun GraphCard(titre: String, sousTitre: String, content: @Composable () -> Unit) {
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

                        Text(
                                text = sousTitre,
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )

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
                                        ChartType.COMPARAISON_BESOINS ->
                                                "Comparez les apports nutritionnels aux besoins recommandés. Vert = optimal, Orange = insuffisant, Rouge = carencé, Bleu = excès."
                                        ChartType.REPARTITION_ENERGIE ->
                                                "Analysez la distribution calorique entre protéines, lipides et glucides."
                                }

                        Text(
                                text = infoText,
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                }
        }
}
