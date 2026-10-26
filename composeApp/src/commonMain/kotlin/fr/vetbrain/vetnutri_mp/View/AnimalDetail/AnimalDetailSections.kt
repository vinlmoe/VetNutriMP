package fr.vetbrain.vetnutri_mp.View

// Sections partagées par WideScreenLayout et NarrowScreenLayout (extraites de AnimalDetailView.kt).
// Les corps sont repris tels quels : les deux layouts contenaient le même code.

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.DrawerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.ConfirmDialog
import fr.vetbrain.vetnutri_mp.Components.RichTextEditor
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.PlanEvolutif
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.sortedForDisplay
import fr.vetbrain.vetnutri_mp.Export.DocumentType
import fr.vetbrain.vetnutri_mp.Export.ExportData
import fr.vetbrain.vetnutri_mp.Export.HtmlDocumentBuilder
import fr.vetbrain.vetnutri_mp.Export.HtmlPreviewDialog
import fr.vetbrain.vetnutri_mp.Export.PdfExporter
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.AnimalDetail
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Chart
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Settings
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.copyToClipboardComposable
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailSection
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalExportViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.RecipeDialogViewModel
import fr.vetbrain.vetnutri_mp.Export.PractitionerContact
import fr.vetbrain.vetnutri_mp.Export.buildCompteRenduText
import fr.vetbrain.vetnutri_mp.Export.buildCompteRenduHtml
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import fr.vetbrain.vetnutri_mp.Components.ShareLinkDialog
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import fr.vetbrain.vetnutri_mp.Components.AnonymizationDialog
import kotlin.math.roundToInt

/** Boutons du menu latéral : exporter l'animal, partager en ligne, retour. */
@Composable
internal fun AnimalDetailSidebarActions(
        animalDetails: AnimalEv,
        animalExportViewModel: AnimalExportViewModel,
        snackbarHostState: SnackbarHostState,
        scope: CoroutineScope,
        onShareOnline: () -> Unit,
        onNavigateBack: () -> Unit
) {
        // Bouton exporter animal
        Button(
                onClick = {
                        scope.launch {
                                exporterAnimalComplet(
                                        animal = animalDetails,
                                        animalExportViewModel = animalExportViewModel,
                                        snackbarHostState = snackbarHostState
                                )
                        }
                },
                colors =
                        ButtonDefaults.buttonColors(
                                backgroundColor = VetNutriColors.Primary,
                                contentColor = VetNutriColors.OnPrimary
                        ),
                modifier = Modifier.fillMaxWidth()
        ) {
                Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = translate(AnimalDetail.EXPORT_ANIMAL)
                )
                Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                Text(text = translate(AnimalDetail.EXPORT_ANIMAL))
        }

        // Bouton partager en ligne
        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
        Button(
                onClick = {
                        onShareOnline()
                },
                colors =
                        ButtonDefaults.buttonColors(
                                backgroundColor = VetNutriColors.Secondary,
                                contentColor = VetNutriColors.OnSecondary
                        ),
                modifier = Modifier.fillMaxWidth()
        ) {
                Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = translate(AnimalDetail.SHARE_ONLINE)
                )
                Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                Text(text = translate(AnimalDetail.SHARE_ONLINE))
        }

        // Bouton retour
        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
        Button(
                onClick = onNavigateBack,
                colors =
                        ButtonDefaults.buttonColors(
                                backgroundColor = VetNutriColors.Secondary,
                                contentColor = VetNutriColors.OnSecondary
                        ),
                modifier = Modifier.fillMaxWidth()
        ) {
                Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = translate(AnimalDetail.BACK)
                )
                Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                Text(text = translate(AnimalDetail.BACK))
        }
}

/** Section « Graphique aliments » de la fiche animal (identique dans les deux layouts). */
@Composable
internal fun GraphiqueAlimentsSection(
        viewModel: AnimalDetailViewModel,
        equationRepository: EquationRepository
) {
        val availableFoods by
                viewModel.availableFoods.collectAsState()
        val isLoadingFoods by
                viewModel.isLoadingFoods.collectAsState()

        // Récupération des préférences pour l'espèce dans ce
        // contexte
        var preferencesApplicationLocal by remember {
                mutableStateOf<
                        fr.vetbrain.vetnutri_mp.Data.PreferencesApplication?>(
                        null
                )
        }

        // Charger les préférences au démarrage
        LaunchedEffect(Unit) {
                preferencesApplicationLocal = viewModel.chargerPreferencesApplication()
        }

        if (isLoadingFoods) {
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(
                                                AppSizes.paddingMedium
                                        ),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment =
                                Alignment.CenterHorizontally
                ) {
                        CircularProgressIndicator(
                                color = VetNutriColors.Primary
                        )
                        Spacer(
                                modifier =
                                        Modifier.height(
                                                AppSizes.paddingMedium
                                        )
                        )
                        Text(
                                translate(AnimalDetail.LOADING_FOODS),
                                style =
                                        MaterialTheme.typography
                                                .body1,
                                color =
                                        MaterialTheme.colors
                                                .onSurface.copy(
                                                alpha = 0.7f
                                        )
                        )
                }
        } else if (availableFoods.isNotEmpty()) {
                // ✨ Utiliser les états du ViewModel pour persister
                // la sélection
                val showAnalyseGraphique by
                        viewModel.showAnalyseGraphique
                                .collectAsState()
                val alimentsSelectionnes by
                        viewModel.alimentsSelectionnes
                                .collectAsState()
                val analyseSelectionFilters by
                        viewModel.analyseSelectionFilters
                                .collectAsState()

                if (showAnalyseGraphique &&
                                alimentsSelectionnes.isNotEmpty()
                ) {
                        // Afficher la vue d'analyse graphique
                        // Récupérer les aliments complets avec
                        // leurs valeurs nutritionnelles
                        var alimentsComplets by remember {
                                mutableStateOf<
                                        List<
                                                fr.vetbrain.vetnutri_mp.Data.AlimentEv>>(
                                        emptyList()
                                )
                        }
                        var isLoadingAlimentsComplets by remember {
                                mutableStateOf(true)
                        }

                        LaunchedEffect(alimentsSelectionnes) {
                                isLoadingAlimentsComplets = true
                                val alimentsAvecValeurs =
                                        mutableListOf<
                                                fr.vetbrain.vetnutri_mp.Data.AlimentEv>()

                                for (aliment in
                                        alimentsSelectionnes) {
                                        try {

                                                // Récupérer
                                                // l'aliment complet
                                                // depuis le
                                                // repository
                                                val alimentComplet =
                                                        viewModel.getAlimentComplet(
                                                                aliment.uuid
                                                        )

                                                if (alimentComplet !=
                                                                null
                                                ) {
                                                        alimentsAvecValeurs
                                                                .add(
                                                                        alimentComplet
                                                                )
                                                } else {

                                                        alimentsAvecValeurs
                                                                .add(
                                                                        aliment
                                                                ) // Fallback
                                                }
                                        } catch (e: Exception) {

                                                e.printStackTrace()
                                                alimentsAvecValeurs
                                                        .add(
                                                                aliment
                                                        ) // Fallback
                                        }
                                }

                                alimentsComplets =
                                        alimentsAvecValeurs
                                isLoadingAlimentsComplets = false
                        }

                        if (isLoadingAlimentsComplets) {
                                Box(
                                        modifier =
                                                Modifier.fillMaxSize(),
                                        contentAlignment =
                                                Alignment.Center
                                ) {
                                        Column(
                                                horizontalAlignment =
                                                        Alignment
                                                                .CenterHorizontally,
                                                verticalArrangement =
                                                        Arrangement
                                                                .spacedBy(
                                                                        AppSizes.paddingMedium
                                                                )
                                        ) {
                                                CircularProgressIndicator(
                                                        color =
                                                                VetNutriColors
                                                                        .Primary
                                                )
                                                Text(
                                                        text =
                                                                translate(AnimalDetail.LOADING_NUTRITION),
                                                        style =
                                                                MaterialTheme
                                                                        .typography
                                                                        .body1,
                                                        color =
                                                                MaterialTheme
                                                                        .colors
                                                                        .onSurface
                                                                        .copy(
                                                                                alpha =
                                                                                        0.7f
                                                                        )
                                                )
                                        }
                                }
                        } else {
                                AnalyseGraphiqueAlimentsView(
                                        aliments = alimentsComplets,
                                        referenceEv =
                                                viewModel
                                                        .referenceUtilisee
                                                        .value,
                                        equationRepository =
                                                equationRepository,
                                        viewModel = viewModel,
                                        onClose = {
                                                viewModel
                                                        .hideAnalyseGraphique()
                                        },
                                        modifier =
                                                Modifier.fillMaxSize()
                                )
                        }
                } else {
                        // Utiliser la vue de sélection des aliments
                        // avec possibilité d'analyse graphique
                        AnalyseSelectionAlimentsView(
                                aliments = availableFoods,
                                onClose = { /* Retour à la section précédente */
                                },
                                onAlimentSelected = { /* Gestion de la sélection */
                                },
                                onPrimaryAction = { aliments ->
                                        viewModel
                                                .lancerAnalyseGraphique(
                                                        aliments
                                                )
                                },
                                primaryActionLabel = translate(Chart.VIEW_GRAPHIC_ANALYSIS_ACTION),
                                alimentsInitialementSelectionnes =
                                        alimentsSelectionnes,
                                onSelectionChanged = {
                                        nouvelleSelection ->
                                        viewModel
                                                .setAlimentsSelectionnes(
                                                        nouvelleSelection
                                                )
                                }, // ✨ Synchroniser avec le
                                // ViewModel
                                filtersInitial =
                                        analyseSelectionFilters,
                                onFiltersChange = {
                                        viewModel
                                                .setAnalyseSelectionFilters(
                                                        it
                                                )
                                },
                                onLoadNutrients = { foodUuids, nutrients ->
                                        viewModel.loadNutrientsForFoods(foodUuids, nutrients)
                                },
                                modifier = Modifier.fillMaxSize()
                        )
                }
        } else {
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(
                                                AppSizes.paddingMedium
                                        ),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment =
                                Alignment.CenterHorizontally
                ) {
                        Text(
                                translate(AnimalDetail.NO_FOOD_AVAILABLE),
                                style = MaterialTheme.typography.h5,
                                color = VetNutriColors.Primary
                        )
                        Text(
                                translate(AnimalDetail.NO_FOOD_GRAPH_AVAILABLE),
                                style =
                                        MaterialTheme.typography
                                                .body1,
                                color =
                                        MaterialTheme.colors
                                                .onSurface.copy(
                                                alpha = 0.7f
                                        )
                        )
                }
        }
}
