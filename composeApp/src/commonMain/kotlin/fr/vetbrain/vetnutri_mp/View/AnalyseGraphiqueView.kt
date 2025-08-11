package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.AppDatePicker
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import io.github.koalaplot.core.*
import io.github.koalaplot.core.line.LinePlot
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.*
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
        val weight: Float,
        val isFromConsultation: Boolean = false,
        val weightUuid: String? = null
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
fun AnalyseGraphiqueView(viewModel: AnimalDetailViewModel, modifier: Modifier = Modifier) {
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
                        ChartType.COMPOSITION_NUTRITIONNELLE -> CompositionNutritionnelleChart()
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
                                                val weight = weightText.toFloatOrNull()
                                                if (weight != null && weight > 0) {
                                                        viewModel.addWeight(selectedDate, weight)
                                                        weightText = ""
                                                }
                                        },
                                        enabled =
                                                weightText.toFloatOrNull() != null &&
                                                        weightText.toFloatOrNull()!! > 0,
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
        COMPOSITION_NUTRITIONNELLE("Composition nutritionnelle"),
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
                // Graphique d'évolution (si on a des données)
                if (consultationsWithAge.isNotEmpty()) {
                        // Déterminer si on utilise les années ou les mois selon l'âge maximal
                        val maxAgeInMonths = consultationsWithAge.maxOf { it.ageInMonths }
                        val useYears = maxAgeInMonths > 18.0

                        val donneesPoids =
                                consultationsWithAge.map { consultationData ->
                                        val xValue =
                                                if (useYears) consultationData.ageInYears.toFloat()
                                                else consultationData.ageInMonths.toFloat()
                                        Point(x = xValue, y = consultationData.weight)
                                }

                        val xAxisTitle = if (useYears) "Âge (années)" else "Âge (mois)"

                        // Calculer les marges pour les axes
                        val minX = donneesPoids.minOfOrNull { it.x } ?: 0f
                        val maxX = donneesPoids.maxOfOrNull { it.x } ?: if (useYears) 10f else 24f
                        val xMargin = (maxX - minX) * 0.05f
                        val xRange =
                                if (minX == maxX) (minX - 1f)..(maxX + 1f)
                                else (minX - xMargin)..(maxX + xMargin)

                        val minY = donneesPoids.minOfOrNull { it.y } ?: 0f
                        val maxY = donneesPoids.maxOfOrNull { it.y } ?: 50f
                        val yMargin = (maxY - minY) * 0.05f
                        val yRange =
                                if (minY == maxY) (minY - 1f)..(maxY + 1f)
                                else (minY - yMargin)..(maxY + yMargin)

                        // Ajuster dynamiquement l'incrément des graduations
                        val xRangeWidth = (xRange.endInclusive - xRange.start).coerceAtLeast(0f)
                        val xTickIncrement =
                                if (xRangeWidth > 10f) (if (useYears) 1f else 3f) else 1f
                        val safeTickIncrement =
                                if (xRangeWidth > 0f) {
                                        xTickIncrement.coerceAtMost(xRangeWidth)
                                } else 1f

                        GraphCard(
                                titre = "Évolution du poids corporel",
                                sousTitre = "Poids en kg selon l'âge"
                        ) {
                                XYGraph(
                                        xAxisModel =
                                                FloatLinearAxisModel(
                                                        range = xRange,
                                                        minimumMajorTickIncrement =
                                                                safeTickIncrement
                                                ),
                                        yAxisModel = FloatLinearAxisModel(range = yRange),
                                        modifier = Modifier.height(250.dp)
                                ) {
                                        LinePlot(
                                                data = donneesPoids,
                                                symbol = { point ->
                                                        Symbol(
                                                                fillBrush = SolidColor(Color.Blue),
                                                                outlineBrush =
                                                                        SolidColor(Color.Black)
                                                        )
                                                }
                                        )
                                }
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
                                                                "%.1f".format(
                                                                        consultationData.weight
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

@Composable
private fun CompositionNutritionnelleChart() {
        // Données d'exemple pour la composition sous forme de barres
        val donneesComposition = remember {
                mapOf(
                        "Protéines" to 25f,
                        "Lipides" to 15f,
                        "Glucides" to 45f,
                        "Fibres" to 10f,
                        "Cendres" to 5f
                )
        }

        val couleurs =
                listOf(
                        Color(0xFF2196F3),
                        Color(0xFF4CAF50),
                        Color(0xFFFF9800),
                        Color(0xFF9C27B0),
                        Color(0xFF607D8B)
                )

        GraphCard(
                titre = "Composition nutritionnelle",
                sousTitre = "Répartition en % de matière sèche"
        ) {
                // Implémentation avec des barres de progression
                Column(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceEvenly
                ) {
                        donneesComposition.entries.forEachIndexed { index, (nutriment, valeur) ->
                                val couleur = couleurs[index % couleurs.size]

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
                                                progress = valeur / 100f,
                                                modifier = Modifier.weight(1f).height(20.dp),
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
private fun ComparaisonBesoinsChart() {
        // Données d'exemple pour comparaison apports vs besoins
        val nutriments = listOf("Protéines", "Calcium", "Phosphore", "Vitamine A", "Fer")
        val apports = listOf(120f, 95f, 110f, 85f, 105f) // en % des besoins

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
                                                apport < 80f -> Color.Red
                                                apport < 100f -> Color(0xFFFFA500) // Orange
                                                apport > 120f -> Color.Blue
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
                                                progress = (apport / 150f).coerceIn(0f, 1f),
                                                modifier = Modifier.weight(1f).height(20.dp),
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
                        "Protéines\n(4 kcal/g)" to 30f,
                        "Lipides\n(9 kcal/g)" to 35f,
                        "Glucides\n(4 kcal/g)" to 35f
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
                                                progress = valeur / 100f,
                                                modifier = Modifier.weight(1f).height(25.dp),
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
                                        ChartType.COMPOSITION_NUTRITIONNELLE ->
                                                "Visualisez la répartition des macronutriments dans la ration actuelle."
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
