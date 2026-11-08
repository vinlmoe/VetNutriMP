package fr.vetbrain.vetnutri_mp.View

import fr.vetbrain.vetnutri_mp.Utils.enTexte

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import fr.vetbrain.vetnutri_mp.Export.DocumentGraphiques
import fr.vetbrain.vetnutri_mp.Export.DocumentGraphiquesRations
import fr.vetbrain.vetnutri_mp.Utils.AppLog
import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import kotlinx.coroutines.launch
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.View.AnalyseGraphique.*
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun AnalyseGraphiqueView(
        viewModel: AnimalDetailViewModel,
        equationRepository: EquationRepository? = null,
        modifier: Modifier = Modifier
) {
        var currentConsultationOnly by remember { mutableStateOf(true) }
        var showRationArea by remember { mutableStateOf(true) }
        var selectedChart by remember { mutableStateOf(ChartType.EVOLUTION_POIDS) }
        var weightConeState by remember { mutableStateOf<WeightConeState?>(null) }
        var minVariationPercent by remember { mutableStateOf<Double>(DEFAULT_MIN_VARIATION_PERCENT) }
        var maxVariationPercent by remember { mutableStateOf<Double>(DEFAULT_MAX_VARIATION_PERCENT) }
        var useDryMatterPer100g by remember {
                mutableStateOf(false)
        } // Toggle pour /1000 kcal vs /100g MS

        // Export : capture de la zone du graphique affiché (le graphique change pendant l'export)
        val scope = rememberCoroutineScope()
        val coucheGraphique = rememberGraphicsLayer()
        val suiviCalculs = remember { SuiviCalculsGraphiques() }
        var dialogueExport by remember { mutableStateOf(false) }
        var exportEnCours by remember { mutableStateOf(false) }
        var messageExport by remember { mutableStateOf<String?>(null) }
        val animal by viewModel.animal.collectAsState()
        val consultationAffichee by viewModel.selectedConsultation.collectAsState()

        fun exporter(format: FormatExport, types: List<ChartType>) {
                scope.launch {
                        exportEnCours = true
                        val graphiqueInitial = selectedChart
                        try {
                                val images = capturerGraphiques(
                                        types, coucheGraphique, suiviCalculs,
                                        afficher = { selectedChart = it },
                                        fichier = { index, type -> "images/${index + 1}_${type.name.lowercase()}.jpg" },
                                        titre = { translate(it.displayName) }
                                )
                                selectedChart = graphiqueInitial
                                val rations = rationsPourExport(
                                        prepareGraphConsultations(
                                                animal?.consultations.orEmpty(), consultationAffichee,
                                                currentConsultationOnly, true
                                        )
                                )
                                val nom = animal?.nom.orEmpty()
                                val document = DocumentGraphiques(
                                        titre = translate("graph.export.doc.title", nom),
                                        sousTitre = consultationAffichee?.date?.enTexte().orEmpty(),
                                        graphiques = images.map { it.first },
                                        rations = rations
                                )
                                val texte = when (format) {
                                        FormatExport.MARKDOWN -> DocumentGraphiquesRations.markdown(document, libellesExport())
                                        FormatExport.LATEX -> DocumentGraphiquesRations.latex(document, libellesExport())
                                }
                                messageExport = enregistrerArchiveGraphiques(
                                        "graphiques_${nomFichierSur(nom)}", format, texte, images
                                )
                        } catch (e: Exception) {
                                e.rethrowIfCancellation()
                                AppLog.e("AnalyseGraphiqueView", e.message ?: "Erreur", e)
                                messageExport = translate("graph.export.error", e.message.orEmpty())
                        } finally {
                                selectedChart = graphiqueInitial
                                exportEnCours = false
                        }
                }
        }

        if (dialogueExport) {
                DialogueExportGraphiques(
                        onDismiss = { dialogueExport = false },
                        onExport = { format, types ->
                                dialogueExport = false
                                exporter(format, types)
                        }
                )
        }
        if (exportEnCours) DialogueExportEnCours()
        messageExport?.let { message ->
                AlertDialog(
                        onDismissRequest = { messageExport = null },
                        text = { Text(message) },
                        confirmButton = { TextButton(onClick = { messageExport = null }) { Text(translate("general.close")) } }
                )
        }

        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
                val isCompact = maxWidth < 600.dp
                val contentPadding =
                        if (isCompact) AppSizes.paddingSmall else AppSizes.paddingMedium
                val verticalSpacing =
                        if (isCompact) AppSizes.paddingSmall else AppSizes.paddingMedium

                CompositionLocalProvider(
                        LocalHauteurGraphique provides hauteurGraphiquePour(maxWidth, maxHeight)
                ) {
                ZoneGraphiquesExportables(suiviCalculs, exportEnCours) {
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(contentPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
                ) {
                        // En-tête avec sélecteur de type de graphique
                        GraphiqueHeader(
                                selectedChart = selectedChart,
                                onChartSelected = { selectedChart = it },
                                isCompact = isCompact,
                                onExport = { dialogueExport = true }
                        )

                        if (selectedChart != ChartType.EVOLUTION_POIDS) {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Switch(currentConsultationOnly, { currentConsultationOnly = it })
                                                Text(translate("graph.consultationActuelleUniquement"), style = MaterialTheme.typography.body2)
                                        }
                                        if (selectedChart != ChartType.DENSITE_RATIONS) {
                                                Row(
                                                        modifier = Modifier.weight(1f),
                                                        verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                        Switch(showRationArea, { showRationArea = it })
                                                        Text(translate("graph.aireEtSommes"), style = MaterialTheme.typography.body2)
                                                }
                                        }
                                }
                                if (selectedChart != ChartType.DENSITE_RATIONS) {
                                        if (showRationArea) Text(
                                                translate("graph.aireExplication"),
                                                style = MaterialTheme.typography.caption
                                        )
                                }
                        }

                        // Toggle pour /1000 kcal vs /100g MS (seulement pour les graphiques de densité)
                        if (selectedChart == ChartType.DENSITE_RATIONS) {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                text = translate(LocalizationKeys.Chart.TOGGLE_PER_1000_KCAL),
                                                style = MaterialTheme.typography.caption,
                                                color =
                                                        if (!useDryMatterPer100g)
                                                                VetNutriColors.Primary
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
                                                text = translate(LocalizationKeys.Chart.TOGGLE_PER_100G_DM),
                                                style = MaterialTheme.typography.caption,
                                                color =
                                                        if (useDryMatterPer100g)
                                                                VetNutriColors.Primary
                                                        else
                                                                MaterialTheme.colors.onSurface.copy(
                                                                        alpha = 0.7f
                                                                )
                                        )
                                }
                        }

                        // Affichage du graphique sélectionné (enregistré dans coucheGraphique pour l'export)
                        // Colonne : certains graphiques émettent plusieurs blocs (carte, sélecteurs, légende)
                        Column(
                                modifier = Modifier.fillMaxWidth().drawWithContent {
                                        coucheGraphique.record { this@drawWithContent.drawContent() }
                                        drawLayer(coucheGraphique)
                                },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(verticalSpacing)
                        ) {
                        when (selectedChart) {
                                ChartType.EVOLUTION_POIDS -> EvolutionPoidsChart(
                                        viewModel,
                                        weightConeState,
                                        onActivateConeAction = { d, w, t ->
                                                weightConeState = WeightConeState(d, w, t)
                                        },
                                        onClearCone = { weightConeState = null },
                                        minVariationPercent = minVariationPercent,
                                        maxVariationPercent = maxVariationPercent,
                                        onUpdateMinVariation = { minVariationPercent = it },
                                        onUpdateMaxVariation = { maxVariationPercent = it }
                                )
                                ChartType.RATIONS_ENERGIE ->
                                        RationsEnergieChart(viewModel, equationRepository, currentConsultationOnly, showRationArea)
                                ChartType.DENSITE_RATIONS ->
                                        DensiteRationsChart(
                                                viewModel,
                                                equationRepository,
                                                useDryMatterPer100g,
                                                currentConsultationOnly
                                        )
                                ChartType.NUTRIMENTS_RATIONS ->
                                        NutrimentsRationsChart(viewModel, equationRepository, currentConsultationOnly, showRationArea)
                        }
                        }

                        // Légende et informations
                        GraphiqueLegend(selectedChart)
                }
                }
                }
        }
}
