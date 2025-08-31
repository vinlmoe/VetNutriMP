package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.DrawerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.ConfirmDialog
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Export.DocumentType
import fr.vetbrain.vetnutri_mp.Export.ExportData
import fr.vetbrain.vetnutri_mp.Export.HtmlDocumentBuilder
import fr.vetbrain.vetnutri_mp.Export.HtmlPreviewDialog
import fr.vetbrain.vetnutri_mp.Export.PdfExporter
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Repository.RecipeRepository
import fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository
import fr.vetbrain.vetnutri_mp.Utils.createPreferencesStorage
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailSection
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import fr.vetbrain.vetnutri_mp.View.AnalyseGraphiqueAlimentsView
import fr.vetbrain.vetnutri_mp.View.AnalyseSelectionAlimentsView
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias RecipeRepo = fr.vetbrain.vetnutri_mp.Repository.RecipeRepository

/**
 * Vue principale pour afficher les détails d'un animal
 *
 * @param viewModel ViewModel contenant les données de l'animal
 * @param settingsViewModel ViewModel pour les paramètres
 * @param onNavigateBack Action à exécuter pour revenir à l'écran précédent
 * @param onOpenSettings Action à exécuter pour ouvrir les paramètres
 * @param modifier Modificateur optionnel pour personnaliser l'apparence
 */
@Composable
fun AnimalDetailView(
        viewModel: AnimalDetailViewModel,
        settingsViewModel: SettingsViewModel,
        onNavigateBack: () -> Unit,
        onOpenSettings: () -> Unit = {},
        modifier: Modifier = Modifier,
        equationRepository: EquationRepository,
        recipeRepository: RecipeRepository,
        foodRepository: FoodRepository
) {
        val animal by viewModel.animal.collectAsState()
        val currentSection by viewModel.currentSection.collectAsState()
        val showFullScreenEdit by viewModel.showFullScreenEdit.collectAsState()
        val selectedConsultation by viewModel.selectedConsultation.collectAsState()
        var showConsultationDetail by remember { mutableStateOf(false) }
        
        // Récupération des préférences pour l'espèce
        val preferencesStorage: fr.vetbrain.vetnutri_mp.Utils.PreferencesStorage = remember { createPreferencesStorage() }
        val preferencesRepository: PreferencesRepository = remember {
                PreferencesRepository(preferencesStorage)
        }
        var preferencesApplication by remember {
                mutableStateOf<fr.vetbrain.vetnutri_mp.Data.PreferencesApplication?>(null)
        }
        
        // Charger les préférences au démarrage
        LaunchedEffect(Unit) {
                preferencesRepository.loadPreferences()
                preferencesApplication = preferencesRepository.preferences
        }

        // État du drawer pour les écrans étroits
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        // État pour les messages Snackbar
        val snackbarHostState = remember { SnackbarHostState() }

        // Effet pour détecter les changements de section et sauvegarder automatiquement
        LaunchedEffect(currentSection) {
                // Si on quitte la section consultation et qu'une édition plein écran est en cours
                val currentConsultation = selectedConsultation
                if (currentSection != AnimalDetailSection.CONSULTATIONS &&
                                showFullScreenEdit &&
                                currentConsultation != null
                ) {
                        // Sauvegarder automatiquement la consultation en cours d'édition
                        viewModel.saveFromFullScreen(currentConsultation)
                }
        }

        // Fonction pour gérer la navigation avec sauvegarde automatique
        val handleNavigateBack: () -> Unit = {
                val currentConsultation = selectedConsultation
                if (showFullScreenEdit && currentConsultation != null) {
                        // Sauvegarder avant de naviguer
                        scope.launch {
                                viewModel.saveFromFullScreen(currentConsultation)
                                onNavigateBack()
                        }
                } else {
                        onNavigateBack()
                }
        }

        // Fonction pour gérer l'ouverture des paramètres avec sauvegarde automatique
        val handleOpenSettings: () -> Unit = {
                val currentConsultation = selectedConsultation
                if (showFullScreenEdit && currentConsultation != null) {
                        // Sauvegarder avant d'ouvrir les paramètres
                        scope.launch {
                                viewModel.saveFromFullScreen(currentConsultation)
                                onOpenSettings()
                        }
                } else {
                        onOpenSettings()
                }
        }

        // Options du menu
        val menuOptions =
                listOf(
                        MenuOption(
                                section = AnimalDetailSection.IDENTIFICATION,
                                title = "Identification",
                                icon = Icons.Default.Person
                        ),
                        MenuOption(
                                section = AnimalDetailSection.CONSULTATIONS,
                                title = "Consultations",
                                icon = Icons.Default.Info
                        ),
                        MenuOption(
                                section = AnimalDetailSection.RATIONS,
                                title = "Rations",
                                icon = Icons.AutoMirrored.Filled.List
                        ),
                        MenuOption(
                                section = AnimalDetailSection.GRAPHIQUE,
                                title = "Graphique",
                                icon = AppIcons.Analytics
                        ),
                        MenuOption(
                                section = AnimalDetailSection.GRAPHIQUE_ALIMENTS,
                                title = "Graphique Aliments",
                                icon = AppIcons.Analytics
                        ),
                        MenuOption(
                                section = AnimalDetailSection.EXPORT,
                                title = "Export",
                                icon = Icons.Default.Settings
                        )
                )

        var showDeleteConfirmation by remember { mutableStateOf(false) }
        var isEditing by remember { mutableStateOf(false) }

        animal?.let { animalDetails ->
                // Utiliser BoxWithConstraints pour déterminer si l'écran est large
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val isWideScreen = maxWidth > AppSizes.breakpointWideScreen

                        if (isWideScreen) {
                                // Layout pour écrans larges avec sidebar permanente
                                WideScreenLayout(
                                        animalDetails = animalDetails,
                                        currentSection = currentSection,
                                        menuOptions = menuOptions,
                                        onNavigateBack = handleNavigateBack,
                                        onOpenSettings = handleOpenSettings,
                                        viewModel = viewModel,
                                        isEditing = isEditing,
                                        onIsEditingChange = { isEditing = it },
                                        onShowDeleteConfirmation = {
                                                showDeleteConfirmation = true
                                        },
                                        showConsultationDetail = showConsultationDetail,
                                        onShowConsultationDetail = { showConsultationDetail = it },
                                        equationRepository = equationRepository,
                                        recipeRepository = recipeRepository,
                                        foodRepository = foodRepository
                                )
                        } else {
                                // Layout pour écrans étroits avec drawer
                                NarrowScreenLayout(
                                        animalDetails = animalDetails,
                                        currentSection = currentSection,
                                        menuOptions = menuOptions,
                                        onNavigateBack = handleNavigateBack,
                                        onOpenSettings = handleOpenSettings,
                                        viewModel = viewModel,
                                        isEditing = isEditing,
                                        onIsEditingChange = { isEditing = it },
                                        onShowDeleteConfirmation = {
                                                showDeleteConfirmation = true
                                        },
                                        showConsultationDetail = showConsultationDetail,
                                        onShowConsultationDetail = { showConsultationDetail = it },
                                        drawerState = drawerState,
                                        scope = scope,
                                        equationRepository = equationRepository,
                                        recipeRepository = recipeRepository,
                                        foodRepository = foodRepository
                                )
                        }

                        // Boîte de dialogue de confirmation de suppression
                        if (showDeleteConfirmation) {
                                ConfirmDialog(
                                        title = "Confirmation de suppression",
                                        message = "Êtes-vous sûr de vouloir supprimer cet animal ?",
                                        onConfirm = {
                                                // Sauvegarder automatiquement si une édition est en
                                                // cours
                                                val currentConsultation = selectedConsultation
                                                if (showFullScreenEdit &&
                                                                currentConsultation != null
                                                ) {
                                                        scope.launch {
                                                                viewModel.saveFromFullScreen(
                                                                        currentConsultation
                                                                )
                                                                // Puis supprimer l'animal
                                                                val success =
                                                                        viewModel.deleteAnimal()
                                                                if (success) {
                                                                        onNavigateBack()
                                                                }
                                                                showDeleteConfirmation = false
                                                        }
                                                } else {
                                                        // Appeler la fonction de suppression du
                                                        // ViewModel
                                                        val success = viewModel.deleteAnimal()
                                                        if (success) {
                                                                // Naviguer vers la liste des
                                                                // animaux
                                                                onNavigateBack()
                                                        }
                                                        showDeleteConfirmation = false
                                                }
                                        },
                                        onDismiss = { showDeleteConfirmation = false }
                                )
                        }
                }
        }
}

/** Layout pour les écrans larges avec une sidebar permanente */
@Composable
private fun WideScreenLayout(
        animalDetails: AnimalEv,
        currentSection: AnimalDetailSection,
        menuOptions: List<MenuOption>,
        onNavigateBack: () -> Unit,
        onOpenSettings: () -> Unit,
        viewModel: AnimalDetailViewModel,
        isEditing: Boolean,
        onIsEditingChange: (Boolean) -> Unit,
        onShowDeleteConfirmation: () -> Unit,
        showConsultationDetail: Boolean,
        onShowConsultationDetail: (Boolean) -> Unit,
        equationRepository: EquationRepository,
        recipeRepository: RecipeRepository,
        foodRepository: FoodRepository
) {
        Row(modifier = Modifier.fillMaxSize()) {
                // Sidebar
                Column(
                        modifier =
                                Modifier.width(250.dp)
                                        .fillMaxHeight()
                                        .padding(AppSizes.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                        // En-tête avec nom et espèce de l'animal
                        Column(modifier = Modifier.fillMaxWidth()) {
                                Text(text = animalDetails.nom, style = MaterialTheme.typography.h5)
                                Text(
                                        text = animalDetails.getEspece().translateEnum(),
                                        style = MaterialTheme.typography.subtitle1,
                                        color = Color.Gray
                                )
                        }

                        Divider()

                        // Options du menu
                        menuOptions.forEach { option ->
                                MenuOptionItem(
                                        option = option,
                                        isSelected = currentSection == option.section,
                                        onClick = { viewModel.navigateTo(option.section) }
                                )
                        }

                        Spacer(modifier = Modifier.weight(1.0f))

                        // Bouton retour
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
                                        contentDescription = "Retour"
                                )
                                Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                                Text(text = "Retour")
                        }

                        // Ajout de l'option Paramètres en bas du menu
                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                        MenuOptionItem(
                                option =
                                        MenuOption(
                                                section = AnimalDetailSection.IDENTIFICATION,
                                                title = "Paramètres",
                                                icon = Icons.Default.Settings
                                        ),
                                isSelected = false,
                                onClick = onOpenSettings
                        )
                }

                // Contenu principal
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        when (currentSection) {
                                AnimalDetailSection.IDENTIFICATION -> {
                                        if (isEditing) {
                                                AnimalEditView(
                                                        animal = animalDetails,
                                                        onSave = { updatedAnimal ->
                                                                viewModel.updateAnimal(
                                                                        updatedAnimal
                                                                )
                                                                onIsEditingChange(false)
                                                        },
                                                        onCancel = { onIsEditingChange(false) },
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(
                                                                                AppSizes.paddingMedium
                                                                        )
                                                )
                                        } else {
                                                AnimalIdentificationView(
                                                        animal = animalDetails,
                                                        onEdit = { onIsEditingChange(true) },
                                                        onDelete = onShowDeleteConfirmation,
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(
                                                                                AppSizes.paddingMedium
                                                                        )
                                                )
                                        }
                                }
                                AnimalDetailSection.CONSULTATIONS -> {
                                        ConsultationsView(
                                                viewModel = viewModel,
                                                showConsultationDetail = showConsultationDetail,
                                                onShowConsultationDetail = onShowConsultationDetail,
                                                modifier = Modifier.fillMaxSize()
                                        )
                                }
                                AnimalDetailSection.RATIONS -> {
                                        RationsView(
                                                viewModel = viewModel,
                                                showSnackbar = { message -> },
                                                equationRepository = equationRepository,
                                                recipeRepository = recipeRepository,
                                                foodRepository = foodRepository
                                        )
                                }
                                AnimalDetailSection.GRAPHIQUE -> {
                                        AnalyseGraphiqueView(
                                                viewModel = viewModel,
                                                equationRepository = equationRepository,
                                                modifier = Modifier.fillMaxSize()
                                        )
                                }
                                AnimalDetailSection.GRAPHIQUE_ALIMENTS -> {
                                        val availableFoods by viewModel.availableFoods.collectAsState()
                                        val isLoadingFoods by viewModel.isLoadingFoods.collectAsState()
                                        
                                        // Récupération des préférences pour l'espèce dans ce contexte
                                        val preferencesStorageLocal: fr.vetbrain.vetnutri_mp.Utils.PreferencesStorage = remember { createPreferencesStorage() }
                                        val preferencesRepositoryLocal: PreferencesRepository = remember {
                                                PreferencesRepository(preferencesStorageLocal)
                                        }
                                        var preferencesApplicationLocal by remember {
                                                mutableStateOf<fr.vetbrain.vetnutri_mp.Data.PreferencesApplication?>(null)
                                        }
                                        
                                        // Charger les préférences au démarrage
                                        LaunchedEffect(Unit) {
                                                preferencesRepositoryLocal.loadPreferences()
                                                preferencesApplicationLocal = preferencesRepositoryLocal.preferences
                                        }
                                        
                                        if (isLoadingFoods) {
                                        Column(
                                                modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
                                                verticalArrangement = Arrangement.Center,
                                                horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                                        CircularProgressIndicator(color = VetNutriColors.Primary)
                                                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))
                                                Text(
                                                                "Chargement des aliments...",
                                                                style = MaterialTheme.typography.body1,
                                                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                                        )
                                                }
                                        } else if (availableFoods.isNotEmpty()) {
                                                // ✨ Utiliser les états du ViewModel pour persister la sélection
                                                val showAnalyseGraphique by viewModel.showAnalyseGraphique.collectAsState()
                                                val alimentsSelectionnes by viewModel.alimentsSelectionnes.collectAsState()
                                                
                                                if (showAnalyseGraphique && alimentsSelectionnes.isNotEmpty()) {
                                                        // Afficher la vue d'analyse graphique
                                                        // Récupérer les aliments complets avec leurs valeurs nutritionnelles
                                                        var alimentsComplets by remember { mutableStateOf<List<fr.vetbrain.vetnutri_mp.Data.AlimentEv>>(emptyList()) }
                                                        var isLoadingAlimentsComplets by remember { mutableStateOf(true) }
                                                        
                                                        LaunchedEffect(alimentsSelectionnes) {
                                                                isLoadingAlimentsComplets = true
                                                                val alimentsAvecValeurs = mutableListOf<fr.vetbrain.vetnutri_mp.Data.AlimentEv>()
                                                                
                                                                
                                                                
                                                                for (aliment in alimentsSelectionnes) {
                                                                        try {
                                                                                
                                                                                
                                                                                
                                                                                // Récupérer l'aliment complet depuis le repository
                                                                                val alimentComplet = fr.vetbrain.vetnutri_mp.Repository.AlimentRepository.getAlimentByUUID(aliment.uuid)
                                                                                
                                                                                if (alimentComplet != null) {
                                                                                        
                                                                                        alimentsAvecValeurs.add(alimentComplet)
                                                                                } else {
                                                                                        
                                                                                        alimentsAvecValeurs.add(aliment) // Fallback
                                                                                }
                                                                        } catch (e: Exception) {
                                                                                
                                                                                e.printStackTrace()
                                                                                alimentsAvecValeurs.add(aliment) // Fallback
                                                                        }
                                                                }
                                                                
                                                                
                                                                alimentsComplets = alimentsAvecValeurs
                                                                isLoadingAlimentsComplets = false
                                                        }
                                                        
                                                        if (isLoadingAlimentsComplets) {
                                                                Box(
                                                                        modifier = Modifier.fillMaxSize(),
                                                                        contentAlignment = Alignment.Center
                                                                ) {
                                                                        Column(
                                                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                                                                        ) {
                                                                                CircularProgressIndicator(color = VetNutriColors.Primary)
                                                                                Text(
                                                                                        text = "Chargement des valeurs nutritionnelles...",
                                                                                        style = MaterialTheme.typography.body1,
                                                                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                                                                )
                                                                        }
                                                                }
                                                        } else {
                                                                AnalyseGraphiqueAlimentsView(
                                                                        aliments = alimentsComplets,
                                                                        referenceEv = viewModel.referenceUtilisee.value,
                                                                        equationRepository = equationRepository,
                                                                        preferencesEspece = animalDetails?.let { animal ->
                                                                                preferencesApplicationLocal?.getPreferencesEspece(animal.getEspece())
                                                                        },
                                                                        onClose = { viewModel.hideAnalyseGraphique() },
                                                                        modifier = Modifier.fillMaxSize()
                                                                )
                                                        }
                                                } else {
                                                        // Utiliser la vue de sélection des aliments avec possibilité d'analyse graphique
                                                        AnalyseSelectionAlimentsView(
                                                                aliments = availableFoods,
                                                                onClose = { /* Retour à la section précédente */ },
                                                                onAlimentSelected = { /* Gestion de la sélection */ },
                                                                onAnalyseGraphique = { aliments ->
                                                                        viewModel.lancerAnalyseGraphique(aliments)
                                                                },
                                                                alimentsInitialementSelectionnes = alimentsSelectionnes,
                                                                onSelectionChanged = { nouvelleSelection ->
                                                                        viewModel.setAlimentsSelectionnes(nouvelleSelection)
                                                                }, // ✨ Synchroniser avec le ViewModel
                                                                modifier = Modifier.fillMaxSize()
                                                        )
                                                }
                                        } else {
                                                Column(
                                                        modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
                                                        verticalArrangement = Arrangement.Center,
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                        Text(
                                                                "Aucun aliment disponible",
                                                        style = MaterialTheme.typography.h5,
                                                        color = VetNutriColors.Primary
                                                )
                                                Text(
                                                                "Aucun aliment n'est disponible pour l'analyse graphique",
                                                        style = MaterialTheme.typography.body1,
                                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                                )
                                                }
                                        }
                                }
                                AnimalDetailSection.EXPORT -> {
                                        val selectedConsultation by
                                                viewModel.selectedConsultation.collectAsState()
                                        val selectedRation by
                                                viewModel.selectedRation.collectAsState()
                                        val referenceUtilisee by
                                                viewModel.referenceUtilisee.collectAsState()
                                        Column(
                                                modifier =
                                                        Modifier.fillMaxSize()
                                                                .padding(AppSizes.paddingMedium),
                                                verticalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingMedium)
                                        ) {
                                                Text(
                                                        "Export des documents",
                                                        style = MaterialTheme.typography.h6,
                                                        color = VetNutriColors.Primary
                                                )
                                                Text(
                                                        text =
                                                                if (selectedRation != null)
                                                                        "Ration sélectionnée: ${selectedRation!!.name}"
                                                                else "Aucune ration sélectionnée",
                                                        color =
                                                                MaterialTheme.colors.onSurface.copy(
                                                                        alpha = 0.7f
                                                                )
                                                )
                                                var showPreview by remember {
                                                        mutableStateOf(false)
                                                }
                                                var previewHtml by remember { mutableStateOf("") }
                                                var additionalText by remember {
                                                        mutableStateOf("")
                                                }
                                                Row(
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(
                                                                        AppSizes.paddingSmall
                                                                )
                                                ) {
                                                        Button(
                                                                onClick = {
                                                                        previewHtml =
                                                                                HtmlDocumentBuilder
                                                                                        .buildHtml(
                                                                                                DocumentType
                                                                                                        .RATION_ANALYSIS,
                                                                                                ExportData(
                                                                                                        animal =
                                                                                                                animalDetails,
                                                                                                        ration =
                                                                                                                selectedRation,
                                                                                                        reference =
                                                                                                                referenceUtilisee,
                                                                                                        title =
                                                                                                                "Analyse de ration",
                                                                                                        additionalText =
                                                                                                                additionalText
                                                                                                )
                                                                                        )
                                                                        showPreview = true
                                                                }
                                                        ) { Text("Exporter analyse PDF") }

                                                        Button(
                                                                onClick = {
                                                                        previewHtml =
                                                                                HtmlDocumentBuilder
                                                                                        .buildHtml(
                                                                                                DocumentType
                                                                                                        .PRESCRIPTION,
                                                                                                ExportData(
                                                                                                        animal =
                                                                                                                animalDetails,
                                                                                                        ration =
                                                                                                                selectedRation,
                                                                                                        reference =
                                                                                                                null,
                                                                                                        conseils =
                                                                                                                listOf(
                                                                                                                        "Fractionner la ration en 2-3 repas",
                                                                                                                        "Veiller à l'hydratation"
                                                                                                                ),
                                                                                                        title =
                                                                                                                "Ordonnance nutritionnelle",
                                                                                                        additionalText =
                                                                                                                additionalText
                                                                                                )
                                                                                        )
                                                                        showPreview = true
                                                                }
                                                        ) { Text("Exporter ordonnance PDF") }
                                                }

                                                // Texte additionnel
                                                OutlinedTextField(
                                                        value = additionalText,
                                                        onValueChange = { additionalText = it },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        label = {
                                                                Text(
                                                                        "Texte additionnel (apparaît en fin de document)"
                                                                )
                                                        },
                                                        maxLines = 6
                                                )
                                                HtmlPreviewDialog(
                                                        html = previewHtml,
                                                        isVisible = showPreview,
                                                        onConfirmExport = {
                                                                // Décider du type à partir du
                                                                // contenu titre
                                                                val isPrescription =
                                                                        previewHtml.contains(
                                                                                "Ordonnance nutritionnelle"
                                                                        )
                                                                if (isPrescription) {
                                                                        PdfExporter.exportDocument(
                                                                                DocumentType
                                                                                        .PRESCRIPTION,
                                                                                ExportData(
                                                                                        animal =
                                                                                                animalDetails,
                                                                                        ration =
                                                                                                selectedRation,
                                                                                        reference =
                                                                                                null,
                                                                                        conseils =
                                                                                                listOf(
                                                                                                        "Fractionner la ration en 2-3 repas",
                                                                                                        "Veiller à l'hydratation"
                                                                                                ),
                                                                                        title =
                                                                                                "Ordonnance nutritionnelle",
                                                                                        additionalText =
                                                                                                additionalText
                                                                                ),
                                                                                defaultFileName =
                                                                                        "ordonnance.pdf"
                                                                        )
                                                                } else {
                                                                        PdfExporter.exportDocument(
                                                                                DocumentType
                                                                                        .RATION_ANALYSIS,
                                                                                ExportData(
                                                                                        animal =
                                                                                                animalDetails,
                                                                                        ration =
                                                                                                selectedRation,
                                                                                        reference =
                                                                                                referenceUtilisee,
                                                                                        title =
                                                                                                "Analyse de ration",
                                                                                        additionalText =
                                                                                                additionalText
                                                                                ),
                                                                                defaultFileName =
                                                                                        "analyse_ration.pdf"
                                                                        )
                                                                }
                                                                showPreview = false
                                                        },
                                                        onDismiss = { showPreview = false }
                                                )
                                        }
                                }
                        }
                }
        }
}

/** Layout pour les écrans étroits avec un drawer */
@Composable
private fun NarrowScreenLayout(
        animalDetails: AnimalEv,
        currentSection: AnimalDetailSection,
        menuOptions: List<MenuOption>,
        onNavigateBack: () -> Unit,
        onOpenSettings: () -> Unit,
        viewModel: AnimalDetailViewModel,
        isEditing: Boolean,
        onIsEditingChange: (Boolean) -> Unit,
        onShowDeleteConfirmation: () -> Unit,
        showConsultationDetail: Boolean,
        onShowConsultationDetail: (Boolean) -> Unit,
        drawerState: DrawerState,
        scope: CoroutineScope,
        equationRepository: EquationRepository,
        recipeRepository: RecipeRepository,
        foodRepository: FoodRepository
) {
        ModalDrawer(
                drawerState = drawerState,
                drawerContent = {
                        Column(
                                modifier =
                                        Modifier.fillMaxHeight()
                                                .width(250.dp)
                                                .padding(AppSizes.paddingMedium),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                // En-tête avec nom et espèce de l'animal
                                Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                                text = animalDetails.nom,
                                                style = MaterialTheme.typography.h5
                                        )
                                        Text(
                                                text = animalDetails.getEspece().translateEnum(),
                                                style = MaterialTheme.typography.subtitle1,
                                                color = Color.Gray
                                        )
                                }

                                Divider()

                                // Options du menu
                                menuOptions.forEach { option ->
                                        MenuOptionItem(
                                                option = option,
                                                isSelected = currentSection == option.section,
                                                onClick = {
                                                        viewModel.navigateTo(option.section)
                                                        scope.launch { drawerState.close() }
                                                }
                                        )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                // Bouton retour
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
                                                contentDescription = "Retour"
                                        )
                                        Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                                        Text(text = "Retour")
                                }

                                // Ajout de l'option Paramètres en bas du menu
                                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                                MenuOptionItem(
                                        option =
                                                MenuOption(
                                                        section =
                                                                AnimalDetailSection.IDENTIFICATION,
                                                        title = "Paramètres",
                                                        icon = Icons.Default.Settings
                                                ),
                                        isSelected = false,
                                        onClick = {
                                                onOpenSettings()
                                                scope.launch { drawerState.close() }
                                        }
                                )
                        }
                },
                content = {
                        Column(modifier = Modifier.fillMaxSize()) {
                                // En-tête avec bouton menu (remplace la TopAppBar)
                                Row(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .padding(AppSizes.paddingMedium),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        IconButton(
                                                onClick = { scope.launch { drawerState.open() } }
                                        ) {
                                                Icon(
                                                        imageVector = Icons.Default.Menu,
                                                        contentDescription = "Menu",
                                                        tint = VetNutriColors.Primary
                                                )
                                        }

                                        Text(
                                                text = animalDetails.nom,
                                                style = MaterialTheme.typography.h6,
                                                color = VetNutriColors.Primary
                                        )

                                        // Espace vide pour équilibrer la mise en page
                                        Spacer(modifier = Modifier.size(AppSizes.iconSizeLarge))
                                }

                                Divider(
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                                        thickness = AppSizes.dividerHeight
                                )

                                // Contenu principal
                                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                        when (currentSection) {
                                                AnimalDetailSection.IDENTIFICATION -> {
                                                        if (isEditing) {
                                                                AnimalEditView(
                                                                        animal = animalDetails,
                                                                        onSave = { updatedAnimal ->
                                                                                viewModel
                                                                                        .updateAnimal(
                                                                                                updatedAnimal
                                                                                        )
                                                                                onIsEditingChange(
                                                                                        false
                                                                                )
                                                                        },
                                                                        onCancel = {
                                                                                onIsEditingChange(
                                                                                        false
                                                                                )
                                                                        },
                                                                        modifier =
                                                                                Modifier.fillMaxSize()
                                                                                        .padding(
                                                                                                AppSizes.paddingMedium
                                                                                        )
                                                                )
                                                        } else {
                                                                AnimalIdentificationView(
                                                                        animal = animalDetails,
                                                                        onEdit = {
                                                                                onIsEditingChange(
                                                                                        true
                                                                                )
                                                                        },
                                                                        onDelete =
                                                                                onShowDeleteConfirmation,
                                                                        modifier =
                                                                                Modifier.fillMaxSize()
                                                                                        .padding(
                                                                                                AppSizes.paddingMedium
                                                                                        )
                                                                )
                                                        }
                                                }
                                                AnimalDetailSection.CONSULTATIONS -> {
                                                        ConsultationsView(
                                                                viewModel = viewModel,
                                                                showConsultationDetail =
                                                                        showConsultationDetail,
                                                                onShowConsultationDetail =
                                                                        onShowConsultationDetail,
                                                                modifier = Modifier.fillMaxSize()
                                                        )
                                                }
                                                AnimalDetailSection.RATIONS -> {
                                                        RationsView(
                                                                viewModel = viewModel,
                                                                showSnackbar = { message -> },
                                                                equationRepository =
                                                                        equationRepository,
                                                                recipeRepository = recipeRepository,
                                                                foodRepository = foodRepository
                                                        )
                                                }
                                                AnimalDetailSection.GRAPHIQUE -> {
                                                        AnalyseGraphiqueView(
                                                                viewModel = viewModel,
                                                                equationRepository = equationRepository,
                                                                modifier = Modifier.fillMaxSize()
                                                        )
                                                }
                                                AnimalDetailSection.GRAPHIQUE_ALIMENTS -> {
                                                        val availableFoods by viewModel.availableFoods.collectAsState()
                                                        val isLoadingFoods by viewModel.isLoadingFoods.collectAsState()
                                                        
                                                        // 🔧 Récupération des préférences pour l'espèce dans ce contexte (même logique que layout large)
                                                        val preferencesStorageLocal: fr.vetbrain.vetnutri_mp.Utils.PreferencesStorage = remember { createPreferencesStorage() }
                                                        val preferencesRepositoryLocal: PreferencesRepository = remember {
                                                                PreferencesRepository(preferencesStorageLocal)
                                                        }
                                                        var preferencesApplicationLocal by remember {
                                                                mutableStateOf<fr.vetbrain.vetnutri_mp.Data.PreferencesApplication?>(null)
                                                        }
                                                        
                                                        // Charger les préférences au démarrage
                                                        LaunchedEffect(Unit) {
                                                                preferencesRepositoryLocal.loadPreferences()
                                                                preferencesApplicationLocal = preferencesRepositoryLocal.preferences
                                                        }
                                                        
                                                        if (isLoadingFoods) {
                                                        Column(
                                                                modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
                                                                verticalArrangement = Arrangement.Center,
                                                                horizontalAlignment = Alignment.CenterHorizontally
                                                        ) {
                                                                        CircularProgressIndicator(color = VetNutriColors.Primary)
                                                                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))
                                                                Text(
                                                                                "Chargement des aliments...",
                                                                                style = MaterialTheme.typography.body1,
                                                                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                                                        )
                                                                }
                                                        } else if (availableFoods.isNotEmpty()) {
                                                                // ✨ MÊME LOGIQUE QUE LE LAYOUT LARGE - Utiliser les états du ViewModel pour persister la sélection
                                                                val showAnalyseGraphique by viewModel.showAnalyseGraphique.collectAsState()
                                                                val alimentsSelectionnes by viewModel.alimentsSelectionnes.collectAsState()
                                                                
                                                                if (showAnalyseGraphique && alimentsSelectionnes.isNotEmpty()) {
                                                                        // Afficher la vue d'analyse graphique
                                                                        // Récupérer les aliments complets avec leurs valeurs nutritionnelles
                                                                        var alimentsComplets by remember { mutableStateOf<List<fr.vetbrain.vetnutri_mp.Data.AlimentEv>>(emptyList()) }
                                                                        var isLoadingAlimentsComplets by remember { mutableStateOf(true) }
                                                                        
                                                                        LaunchedEffect(alimentsSelectionnes) {
                                                                                isLoadingAlimentsComplets = true
                                                                                val alimentsAvecValeurs = mutableListOf<fr.vetbrain.vetnutri_mp.Data.AlimentEv>()
                                                                                
                                                                                
                                                                                
                                                                                for (aliment in alimentsSelectionnes) {
                                                                                        try {
                                                                                                
                                                                                                
                                                                                                
                                                                                                // Récupérer l'aliment complet depuis le repository
                                                                                                val alimentComplet = fr.vetbrain.vetnutri_mp.Repository.AlimentRepository.getAlimentByUUID(aliment.uuid)
                                                                                                
                                                                                                if (alimentComplet != null) {
                                                                                                        
                                                                                                        alimentsAvecValeurs.add(alimentComplet)
                                                                                                } else {
                                                                                                        
                                                                                                        alimentsAvecValeurs.add(aliment) // Fallback
                                                                                                }
                                                                                        } catch (e: Exception) {
                                                                                                
                                                                                                e.printStackTrace()
                                                                                                alimentsAvecValeurs.add(aliment) // Fallback
                                                                                        }
                                                                                }
                                                                                
                                                                                
                                                                                alimentsComplets = alimentsAvecValeurs
                                                                                isLoadingAlimentsComplets = false
                                                                        }
                                                                        
                                                                        if (isLoadingAlimentsComplets) {
                                                                                Box(
                                                                                        modifier = Modifier.fillMaxSize(),
                                                                                        contentAlignment = Alignment.Center
                                                                                ) {
                                                                                        Column(
                                                                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                                                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                                                                                        ) {
                                                                                                CircularProgressIndicator(color = VetNutriColors.Primary)
                                                                                                Text(
                                                                                                        text = "Chargement des valeurs nutritionnelles...",
                                                                                                        style = MaterialTheme.typography.body1,
                                                                                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                                                                                )
                                                                                        }
                                                                                }
                                                                        } else {
                                                                                AnalyseGraphiqueAlimentsView(
                                                                                        aliments = alimentsComplets,
                                                                                        referenceEv = viewModel.referenceUtilisee.value,
                                                                                        equationRepository = equationRepository,
                                                                                        preferencesEspece = animalDetails?.let { animal ->
                                                                                                preferencesApplicationLocal?.getPreferencesEspece(animal.getEspece())
                                                                                        },
                                                                                        onClose = { viewModel.hideAnalyseGraphique() },
                                                                                        modifier = Modifier.fillMaxSize()
                                                                                )
                                                                        }
                                                                } else {
                                                                        // Utiliser la vue de sélection des aliments avec possibilité d'analyse graphique
                                                                        AnalyseSelectionAlimentsView(
                                                                                aliments = availableFoods,
                                                                                onClose = { /* Retour à la section précédente */ },
                                                                                onAlimentSelected = { /* Gestion de la sélection */ },
                                                                                onAnalyseGraphique = { aliments ->
                                                                                        viewModel.lancerAnalyseGraphique(aliments)
                                                                                },
                                                                                alimentsInitialementSelectionnes = alimentsSelectionnes,
                                                                                onSelectionChanged = { nouvelleSelection ->
                                                                                        viewModel.setAlimentsSelectionnes(nouvelleSelection)
                                                                                }, // ✨ Synchroniser avec le ViewModel
                                                                                modifier = Modifier.fillMaxSize()
                                                                        )
                                                                }
                                                        } else {
                                                                Column(
                                                                        modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
                                                                        verticalArrangement = Arrangement.Center,
                                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                                ) {
                                                                        Text(
                                                                                "Aucun aliment disponible",
                                                                        style = MaterialTheme.typography.h5,
                                                                        color = VetNutriColors.Primary
                                                                )
                                                                Text(
                                                                                "Aucun aliment n'est disponible pour l'analyse graphique",
                                                                        style = MaterialTheme.typography.body1,
                                                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                                                )
                                                                }
                                                        }
                                                }
                                                AnimalDetailSection.EXPORT -> {
                                                        val selectedConsultation by
                                                                viewModel.selectedConsultation
                                                                        .collectAsState()
                                                        val selectedRation by
                                                                viewModel.selectedRation
                                                                        .collectAsState()
                                                        val referenceUtilisee by
                                                                viewModel.referenceUtilisee
                                                                        .collectAsState()
                                                        Column(
                                                                modifier =
                                                                        Modifier.fillMaxSize()
                                                                                .padding(
                                                                                        AppSizes.paddingMedium
                                                                                ),
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(
                                                                                AppSizes.paddingMedium
                                                                        )
                                                        ) {
                                                                Text(
                                                                        "Export des documents",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .h6,
                                                                        color =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                )
                                                                Text(
                                                                        text =
                                                                                if (selectedRation !=
                                                                                                null
                                                                                )
                                                                                        "Ration sélectionnée: ${selectedRation!!.name}"
                                                                                else
                                                                                        "Aucune ration sélectionnée",
                                                                        color =
                                                                                MaterialTheme.colors
                                                                                        .onSurface
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.7f
                                                                                        )
                                                                )
                                                                Row(
                                                                        horizontalArrangement =
                                                                                Arrangement
                                                                                        .spacedBy(
                                                                                                AppSizes.paddingSmall
                                                                                        )
                                                                ) {
                                                                        Button(
                                                                                onClick = {
                                                                                        val ok =
                                                                                                PdfExporter
                                                                                                        .exportDocument(
                                                                                                                documentType =
                                                                                                                        DocumentType
                                                                                                                                .RATION_ANALYSIS,
                                                                                                                data =
                                                                                                                        ExportData(
                                                                                                                                animal =
                                                                                                                                        animalDetails,
                                                                                                                                ration =
                                                                                                                                        selectedRation,
                                                                                                                                reference =
                                                                                                                                        referenceUtilisee,
                                                                                                                                title =
                                                                                                                                        "Analyse de ration"
                                                                                                                        ),
                                                                                                                defaultFileName =
                                                                                                                        "analyse_ration.pdf"
                                                                                                        )
                                                                                }
                                                                        ) {
                                                                                Text(
                                                                                        "Exporter analyse PDF"
                                                                                )
                                                                        }

                                                                        Button(
                                                                                onClick = {
                                                                                        val ok =
                                                                                                PdfExporter
                                                                                                        .exportDocument(
                                                                                                                documentType =
                                                                                                                        DocumentType
                                                                                                                                .PRESCRIPTION,
                                                                                                                data =
                                                                                                                        ExportData(
                                                                                                                                animal =
                                                                                                                                        animalDetails,
                                                                                                                                ration =
                                                                                                                                        selectedRation,
                                                                                                                                reference =
                                                                                                                                        null,
                                                                                                                                conseils =
                                                                                                                                        listOf(
                                                                                                                                                "Fractionner la ration en 2-3 repas",
                                                                                                                                                "Veiller à l'hydratation"
                                                                                                                                        ),
                                                                                                                                title =
                                                                                                                                        "Ordonnance nutritionnelle"
                                                                                                                        ),
                                                                                                                defaultFileName =
                                                                                                                        "ordonnance.pdf"
                                                                                                        )
                                                                                }
                                                                        ) {
                                                                                Text(
                                                                                        "Exporter ordonnance PDF"
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }
        )
}
