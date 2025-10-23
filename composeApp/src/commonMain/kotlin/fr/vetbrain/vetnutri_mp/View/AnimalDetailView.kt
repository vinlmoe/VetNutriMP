package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
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
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Export.DocumentType
import fr.vetbrain.vetnutri_mp.Export.ExportData
import fr.vetbrain.vetnutri_mp.Export.HtmlDocumentBuilder
import fr.vetbrain.vetnutri_mp.Export.HtmlPreviewDialog
import fr.vetbrain.vetnutri_mp.Export.PdfExporter
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository
import fr.vetbrain.vetnutri_mp.Repository.RecipeRepository
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.createPreferencesStorage
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailSection
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias RecipeRepo = fr.vetbrain.vetnutri_mp.Repository.RecipeRepository

/**
 * Génère un nom de fichier par défaut pour l'export PDF
 * Format: "ID animal + Nom Animal + date consultation.pdf"
 */
private fun generateDefaultPdfFileName(animal: AnimalEv?, consultation: ConsultationEv?): String {
    val animalId = animal?.id ?: "ID_INCONNU"
    val animalName = animal?.nom ?: "NOM_INCONNU"
    val consultationDate = consultation?.date?.toString() ?: "DATE_INCONNUE"
    return "${animalId}_${animalName}_${consultationDate}.pdf"
}

/**
 * Fonction commune pour l'export PDF depuis la prévisualisation HTML
 */
private fun handlePdfExport(
    previewHtml: String,
    animalDetails: AnimalEv,
    selectedConsultation: ConsultationEv?,
    selectedRation: Ration?,
    referenceUtilisee: fr.vetbrain.vetnutri_mp.Data.ReferenceEv?,
    additionalText: String,
    getSelectedConseils: () -> List<fr.vetbrain.vetnutri_mp.Export.HtmlSection>,
    besoinEnergetiqueStandard: Double?,
    poidsMetabolique: Double?,
    equationRepository: EquationRepository,
    scope: CoroutineScope
) {
    val isPrescription = previewHtml.contains("Ordonnance nutritionnelle")
    if (isPrescription) {
        // Export ordonnance avec informations praticien
        val prefsStorage = createPreferencesStorage()
        val prefsRepo = PreferencesRepository(prefsStorage)
        scope.launch(fr.vetbrain.vetnutri_mp.Utils.AppDispatchers.Main) {
            try {
                prefsRepo.loadPreferences()
                val prefs = prefsRepo.preferences
                val practitioner = fr.vetbrain.vetnutri_mp.Export.PractitionerInfo(
                    nom = prefs.nomUtilisateur,
                    numeroOrdre = prefs.numeroOrdre,
                    adressePostale = prefs.adressePostale,
                    codePostal = prefs.codePostal,
                    ville = prefs.ville,
                    telephone = prefs.telephone,
                    email = prefs.email
                )
                val allRations = selectedConsultation?.rations?.toList() ?: emptyList()
                
                fr.vetbrain.vetnutri_mp.exportPdfDocument(
                    documentType = DocumentType.PRESCRIPTION,
                    data = ExportData(
                        animal = animalDetails,
                        ration = null,
                        reference = referenceUtilisee,
                        conseils = listOf("Veiller à l'hydratation"),
                        title = "Ordonnance nutritionnelle",
                        additionalText = additionalText,
                        htmlSections = getSelectedConseils(),
                        rations = allRations,
                        practitioner = practitioner,
                        preferences = null,
                        poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                        poidsMetabolique = null,
                        besoinEnergetiqueEntretien = null
                    ),
                    defaultFileName = generateDefaultPdfFileName(animalDetails, selectedConsultation)
                )
            } catch (e: Exception) {
                // En cas d'erreur, exporter sans les informations du prescripteur
                fr.vetbrain.vetnutri_mp.exportPdfDocument(
                    documentType = DocumentType.PRESCRIPTION,
                    data = ExportData(
                        animal = animalDetails,
                        ration = null,
                        reference = referenceUtilisee,
                        conseils = emptyList(),
                        title = "Ordonnance nutritionnelle",
                        additionalText = additionalText,
                        htmlSections = getSelectedConseils(),
                        rations = selectedConsultation?.rations?.toList() ?: emptyList(),
                        practitioner = null,
                        preferences = null,
                        poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                        poidsMetabolique = null,
                        besoinEnergetiqueEntretien = null
                    ),
                    defaultFileName = generateDefaultPdfFileName(animalDetails, selectedConsultation)
                )
            }
        }
    } else {
        // Export analyse de ration avec bullet graphs
        val bulletGraphImages = mutableMapOf<String, Map<String, String>>()
        
        selectedRation?.let { ration: Ration ->
            try {
                val prefsStorage = createPreferencesStorage()
                val prefsRepo = PreferencesRepository(prefsStorage)
                
                scope.launch {
                    prefsRepo.loadPreferences()
                }
                val prefs = prefsRepo.preferences
                val prefsEspece = prefs?.getPreferencesEspece(animalDetails.getEspece())
                
                val ref = referenceUtilisee
                if (prefsEspece != null && ref != null) {
                    val images = fr.vetbrain.vetnutri_mp.Export.BulletGraphImageCapture.generateRationBulletGraphImages(
                        ration = ration,
                        reference = ref,
                        animal = animalDetails,
                        preferences = prefsEspece,
                        poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                        poidsMetabolique = poidsMetabolique,
                        besoinEnergetiqueEntretien = besoinEnergetiqueStandard,
                        equationRepository = equationRepository
                    )
                    
                    val imagePaths = images.mapValues { (_, imageBytes) ->
                        val tempFilePath = fr.vetbrain.vetnutri_mp.Export.BulletGraphImageCapture.saveImageToTempFile(imageBytes, "export")
                        "file://$tempFilePath"
                    }
                    
                    bulletGraphImages[ration.uuid] = imagePaths
                } else {
                    // Générer des images de test
                    val testImages = mutableMapOf<String, ByteArray>()
                    listOf("PROTEINE", "LIPIDE", "ENA", "CELLULOSE", "CENDRE", "CAL", "PHOS").forEach { nom ->
                        val imageBytes = fr.vetbrain.vetnutri_mp.Export.BulletGraphImageCapture.generateBulletGraphImage(
                            nom, 25.0, 15.0, 40.0, 20.0, 35.0, "g/kg DM"
                        )
                        testImages[nom] = imageBytes
                    }
                    
                    val testImagePaths = testImages.mapValues { (_, imageBytes) ->
                        val tempFilePath = fr.vetbrain.vetnutri_mp.Export.BulletGraphImageCapture.saveImageToTempFile(imageBytes, "export_test")
                        "file://$tempFilePath"
                    }
                    
                    bulletGraphImages[ration.uuid] = testImagePaths
                }
            } catch (e: Exception) {
                println("Erreur génération bullet graphs pour export PDF: ${e.message}")
            }
        }
        
        PdfExporter.exportDocument(
            DocumentType.RATION_ANALYSIS,
            ExportData(
                animal = animalDetails,
                ration = selectedRation,
                reference = referenceUtilisee,
                title = "Analyse de ration",
                additionalText = additionalText,
                htmlSections = getSelectedConseils(),
                preferences = null,
                poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                poidsMetabolique = null,
                besoinEnergetiqueEntretien = null,
                bulletGraphImages = bulletGraphImages
            ),
            defaultFileName = "analyse_ration.pdf"
        )
    }
}

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
        conseilRepository: fr.vetbrain.vetnutri_mp.Repository.ConseilRepository
) {
        val animal by viewModel.animal.collectAsState()
        val currentSection by viewModel.currentSection.collectAsState()
        val showFullScreenEdit by viewModel.showFullScreenEdit.collectAsState()
        val selectedConsultation by viewModel.selectedConsultation.collectAsState()
        var showConsultationDetail by remember { mutableStateOf(false) }

        // Récupération des préférences pour l'espèce
        val preferencesStorage: fr.vetbrain.vetnutri_mp.Utils.PreferencesStorage = remember {
                createPreferencesStorage()
        }
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
                                        conseilRepository = conseilRepository
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
                                        conseilRepository = conseilRepository
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
        conseilRepository: fr.vetbrain.vetnutri_mp.Repository.ConseilRepository
) {
        // Scope pour les coroutines
        val scope = rememberCoroutineScope()
        
        // État pour l'éditeur de texte enrichi
        var currentHtmlContent by remember {
                mutableStateOf(fr.vetbrain.vetnutri_mp.Export.RichTextContent())
        }
        var showRichTextEditor by remember { mutableStateOf(false) }

        // État pour les conseils personnalisés (sauvegardés)
        var availableConseils by remember {
                mutableStateOf<List<fr.vetbrain.vetnutri_mp.Export.HtmlSection>>(emptyList())
        }
        // État pour les sections HTML créées localement (temporaires)
        var localHtmlSections by remember {
                mutableStateOf<List<fr.vetbrain.vetnutri_mp.Export.HtmlSection>>(emptyList())
        }
        var selectedConseils by remember {
                mutableStateOf<List<fr.vetbrain.vetnutri_mp.Export.HtmlSection>>(emptyList())
        }
        var isLoadingConseils by remember { mutableStateOf(true) }
        var searchQuery by remember { mutableStateOf("") }
        var showSearchDialog by remember { mutableStateOf(false) }

        // Charger les conseils personnalisés
        LaunchedEffect(Unit) {
                try {
                        val result = conseilRepository.getConseilsActifs()
                        if (result.isSuccess) {
                                availableConseils = result.getOrThrow()
                        }
                } catch (e: Exception) {
                        e.printStackTrace()
                } finally {
                        isLoadingConseils = false
                }
        }
        
        // Variables pour la prévisualisation et l'export
        var showPreview by remember {
                mutableStateOf(false)
        }
        var previewHtml by remember {
                mutableStateOf("")
        }
        var additionalText by remember {
                mutableStateOf("")
        }
        
        // Fonction pour récupérer les conseils sélectionnés (conseils + sections locales)
        val getSelectedConseils:
                () -> List<fr.vetbrain.vetnutri_mp.Export.HtmlSection> =
                {
                        selectedConseils + localHtmlSections
                }
        
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
                                        val availableFoods by
                                                viewModel.availableFoods.collectAsState()
                                        val isLoadingFoods by
                                                viewModel.isLoadingFoods.collectAsState()

                                        // Récupération des préférences pour l'espèce dans ce
                                        // contexte
                                        val preferencesStorageLocal:
                                                fr.vetbrain.vetnutri_mp.Utils.PreferencesStorage =
                                                remember {
                                                        createPreferencesStorage()
                                                }
                                        val preferencesRepositoryLocal: PreferencesRepository =
                                                remember {
                                                        PreferencesRepository(
                                                                preferencesStorageLocal
                                                        )
                                                }
                                        var preferencesApplicationLocal by remember {
                                                mutableStateOf<
                                                        fr.vetbrain.vetnutri_mp.Data.PreferencesApplication?>(
                                                        null
                                                )
                                        }

                                        // Charger les préférences au démarrage
                                        LaunchedEffect(Unit) {
                                                preferencesRepositoryLocal.loadPreferences()
                                                preferencesApplicationLocal =
                                                        preferencesRepositoryLocal.preferences
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
                                                                "Chargement des aliments...",
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
                                                                                        viewModel.getAlimentCompletSync(
                                                                                                aliment.uuid
                                                                                        )

                                                                                if (alimentComplet !=
                                                                                                null
                                                                                ) {

                                                                                        if (alimentComplet is AlimentEv) {
                                                                                                alimentsAvecValeurs
                                                                                                        .add(
                                                                                                                alimentComplet
                                                                                                        )
                                                                                                }
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
                                                                                                "Chargement des valeurs nutritionnelles...",
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
                                                                        preferencesEspece =
                                                                                animalDetails
                                                                                        ?.let {
                                                                                                animal
                                                                                                ->
                                                                                                preferencesApplicationLocal
                                                                                                        ?.getPreferencesEspece(
                                                                                                                animal.getEspece()
                                                                                                        )
                                                                                        },
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
                                                                onAnalyseGraphique = { aliments ->
                                                                        viewModel
                                                                                .lancerAnalyseGraphique(
                                                                                        aliments
                                                                                )
                                                                },
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
                                                                "Aucun aliment disponible",
                                                                style = MaterialTheme.typography.h5,
                                                                color = VetNutriColors.Primary
                                                        )
                                                        Text(
                                                                "Aucun aliment n'est disponible pour l'analyse graphique",
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
                                AnimalDetailSection.EXPORT -> {
                                        val selectedConsultation by
                                                viewModel.selectedConsultation.collectAsState()
                                        val selectedRation by
                                                viewModel.selectedRation.collectAsState()
                                        val referenceUtilisee by
                                                viewModel.referenceUtilisee.collectAsState()
                                        val besoinEnergetiqueStandard by viewModel.besoinEnergetiqueStandard.collectAsState()
                                        val poidsMetabolique by viewModel.poidsMetabolique.collectAsState()

                                        if (showRichTextEditor) {
                                                // Éditeur de texte enrichi
                                                Column(modifier = Modifier.fillMaxSize()) {
                                                        Row(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .padding(
                                                                                        AppSizes.paddingMedium
                                                                                ),
                                                                horizontalArrangement =
                                                                        Arrangement.SpaceBetween,
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                Text(
                                                                        "Éditeur de sections HTML",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .h6,
                                                                        color =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                )
                                                                Button(
                                                                        onClick = {
                                                                                showRichTextEditor =
                                                                                        false
                                                                        },
                                                                        colors =
                                                                                ButtonDefaults
                                                                                        .buttonColors(
                                                                                                backgroundColor =
                                                                                                        VetNutriColors
                                                                                                                .Secondary,
                                                                                                contentColor =
                                                                                                        VetNutriColors
                                                                                                                .OnSecondary
                                                                                        )
                                                                ) { Text("Retour à l'export") }
                                                        }

                                                        RichTextEditor(
                                                                initialContent = currentHtmlContent,
                                                                onContentChange = { content ->
                                                                        currentHtmlContent = content
                                                                },
                                                                modifier = Modifier.weight(1f)
                                                        )

                                                        // Boutons d'action
                                                        Row(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .padding(
                                                                                        AppSizes.paddingMedium
                                                                                ),
                                                                horizontalArrangement =
                                                                        Arrangement.spacedBy(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        ) {
                                                                Button(
                                                                        onClick = {
                                                                                // Créer une
                                                                                // nouvelle section
                                                                                // HTML
                                                                                val newSection =
                                                                                        fr.vetbrain
                                                                                                .vetnutri_mp
                                                                                                .Export
                                                                                                .HtmlSection(
                                                                                                        id =
                                                                                                                "section_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}",
                                                                                                        title =
                                                                                                                "Section personnalisée ${availableConseils.size + 1}",
                                                                                                        content =
                                                                                                                currentHtmlContent,
                                                                                                        category =
                                                                                                                fr.vetbrain
                                                                                                                        .vetnutri_mp
                                                                                                                        .Export
                                                                                                                        .SectionCategory
                                                                                                                        .CUSTOM
                                                                                                )
                                                                                // Ajouter à la
                                                                                // liste des
                                                                                // sections HTML
                                                                                // locales
                                                                                localHtmlSections =
                                                                                        localHtmlSections +
                                                                                                newSection
                                                                                currentHtmlContent =
                                                                                        fr.vetbrain
                                                                                                .vetnutri_mp
                                                                                                .Export
                                                                                                .RichTextContent()
                                                                                showRichTextEditor =
                                                                                        false
                                                                        },
                                                                        enabled =
                                                                                currentHtmlContent
                                                                                        .blocks
                                                                                        .isNotEmpty()
                                                                ) { Text("Ajouter la section") }

                                                                OutlinedButton(
                                                                        onClick = {
                                                                                currentHtmlContent =
                                                                                        fr.vetbrain
                                                                                                .vetnutri_mp
                                                                                                .Export
                                                                                                .RichTextContent()
                                                                        }
                                                                ) { Text("Effacer") }
                                                        }
                                                }
                                        } else {
                                                // Section export normale
                                                LazyColumn(
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
                                                        item {
                                                        Text(
                                                                "Export des documents",
                                                                style = MaterialTheme.typography.h6,
                                                                color = VetNutriColors.Primary
                                                        )
                                                        }
                                                        item {
                                                        Text(
                                                                text =
                                                                        if (selectedRation != null)
                                                                                "Ration sélectionnée: ${selectedRation!!.name}"
                                                                        else
                                                                                "Aucune ration sélectionnée",
                                                                color =
                                                                        MaterialTheme.colors
                                                                                .onSurface.copy(
                                                                                alpha = 0.7f
                                                                        )
                                                        )
                                                        }

                                                        // Section pour les conseils personnalisés
                                                        item {
                                                        Text(
                                                                "Conseils personnalisés:",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .subtitle1,
                                                                color = VetNutriColors.Primary
                                                        )
                                                        }

                                                        // Affichage des conseils sélectionnés
                                                        if (selectedConseils.isNotEmpty()) {
                                                                item {
                                                                Column(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth(),
                                                                        verticalArrangement =
                                                                                Arrangement
                                                                                        .spacedBy(
                                                                                                4.dp
                                                                                        )
                                                                ) {
                                                                        selectedConseils.forEach {
                                                                                conseil ->
                                                                                Card(
                                                                                        modifier =
                                                                                                Modifier.fillMaxWidth(),
                                                                                        elevation =
                                                                                                2.dp
                                                                                ) {
                                                                                        Row(
                                                                                                modifier =
                                                                                                        Modifier.fillMaxWidth()
                                                                                                                .padding(
                                                                                                                        8.dp
                                                                                                                ),
                                                                                                horizontalArrangement =
                                                                                                        Arrangement
                                                                                                                .SpaceBetween,
                                                                                                verticalAlignment =
                                                                                                        Alignment
                                                                                                                .CenterVertically
                                                                                        ) {
                                                                                                Column(
                                                                                                        modifier =
                                                                                                                Modifier.weight(
                                                                                                                        1f
                                                                                                                )
                                                                                                ) {
                                                                                                        Text(
                                                                                                                text =
                                                                                                                        conseil.title,
                                                                                                                style =
                                                                                                                        MaterialTheme
                                                                                                                                .typography
                                                                                                                                .body2,
                                                                                                                fontWeight =
                                                                                                                        FontWeight
                                                                                                                                .Medium
                                                                                                        )
                                                                                                        Text(
                                                                                                                text =
                                                                                                                        "Catégorie: ${conseil.category.name}",
                                                                                                                style =
                                                                                                                        MaterialTheme
                                                                                                                                .typography
                                                                                                                                .caption,
                                                                                                                color =
                                                                                                                        Color.Gray
                                                                                                        )
                                                                                                }
                                                                                                IconButton(
                                                                                                        onClick = {
                                                                                                                selectedConseils =
                                                                                                                        selectedConseils
                                                                                                                                .filter {
                                                                                                                                        it.id !=
                                                                                                                                                conseil.id
                                                                                                                                }
                                                                                                        }
                                                                                                ) {
                                                                                                        Icon(
                                                                                                                Icons.Default
                                                                                                                        .Delete,
                                                                                                                "Supprimer",
                                                                                                                tint =
                                                                                                                        Color.Red
                                                                                                        )
                                                                                                        }
                                                                                                }
                                                                                        }
                                                                                }
                                                                        }
                                                                }
                                                        }

                                                        // Bouton pour ajouter des conseils
                                                        item {
                                                        Button(
                                                                onClick = {
                                                                        showSearchDialog = true
                                                                },
                                                                modifier = Modifier.fillMaxWidth(),
                                                                colors =
                                                                        ButtonDefaults.buttonColors(
                                                                                backgroundColor =
                                                                                        VetNutriColors
                                                                                                .Secondary,
                                                                                contentColor =
                                                                                        VetNutriColors
                                                                                                .OnSecondary
                                                                        )
                                                        ) {
                                                                Icon(Icons.Default.Add, "Ajouter")
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.width(8.dp)
                                                                )
                                                                Text("Ajouter des conseils")
                                                                }
                                                        }

                                                        item {
                                                        Spacer(modifier = Modifier.height(16.dp))
                                                        }

                                                        // Section pour les sections HTML créées
                                                        // localement
                                                        if (localHtmlSections.isNotEmpty()) {
                                                                item {
                                                                Text(
                                                                        "Sections HTML créées localement (${localHtmlSections.size}):",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .subtitle1,
                                                                        color =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                )
                                                                }
                                                                item {
                                                                Column(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth(),
                                                                        verticalArrangement =
                                                                                Arrangement
                                                                                        .spacedBy(
                                                                                                4.dp
                                                                                        )
                                                                ) {
                                                                        localHtmlSections.forEach {
                                                                                section ->
                                                                                Card(
                                                                                        modifier =
                                                                                                Modifier.fillMaxWidth(),
                                                                                        elevation =
                                                                                                2.dp
                                                                                ) {
                                                                                        Row(
                                                                                                modifier =
                                                                                                        Modifier.fillMaxWidth()
                                                                                                                .padding(
                                                                                                                        8.dp
                                                                                                                ),
                                                                                                horizontalArrangement =
                                                                                                        Arrangement
                                                                                                                .SpaceBetween,
                                                                                                verticalAlignment =
                                                                                                        Alignment
                                                                                                                .CenterVertically
                                                                                        ) {
                                                                                                Column(
                                                                                                        modifier =
                                                                                                                Modifier.weight(
                                                                                                                        1f
                                                                                                                )
                                                                                                ) {
                                                                                                        Text(
                                                                                                                text =
                                                                                                                        section.title,
                                                                                                                style =
                                                                                                                        MaterialTheme
                                                                                                                                .typography
                                                                                                                                .body2,
                                                                                                                fontWeight =
                                                                                                                        FontWeight
                                                                                                                                .Medium
                                                                                                        )
                                                                                                        Text(
                                                                                                                text =
                                                                                                                        "${section.content.blocks.size} blocs",
                                                                                                                style =
                                                                                                                        MaterialTheme
                                                                                                                                .typography
                                                                                                                                .caption,
                                                                                                                color =
                                                                                                                        Color.Gray
                                                                                                        )
                                                                                                }
                                                                                                IconButton(
                                                                                                        onClick = {
                                                                                                                localHtmlSections =
                                                                                                                        localHtmlSections
                                                                                                                                .filter {
                                                                                                                                        it.id !=
                                                                                                                                                section.id
                                                                                                                                }
                                                                                                        }
                                                                                                ) {
                                                                                                        Icon(
                                                                                                                Icons.Default
                                                                                                                        .Delete,
                                                                                                                "Supprimer",
                                                                                                                tint =
                                                                                                                        Color.Red
                                                                                                        )
                                                                                                }
                                                                                        }
                                                                                }
                                                                        }
                                                                }
                                                                }
                                                                item {
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.height(
                                                                                        16.dp
                                                                                )
                                                                )
                                                                }
                                                        }

                                                        // Bouton pour accéder à l'éditeur de texte
                                                        // enrichi
                                                        item {
                                                        Button(
                                                                onClick = {
                                                                        showRichTextEditor = true
                                                                },
                                                                modifier = Modifier.fillMaxWidth(),
                                                                colors =
                                                                        ButtonDefaults.buttonColors(
                                                                                backgroundColor =
                                                                                        VetNutriColors
                                                                                                .Secondary,
                                                                                contentColor =
                                                                                        VetNutriColors
                                                                                                .OnSecondary
                                                                        )
                                                        ) {
                                                                Icon(
                                                                        Icons.Default.Edit,
                                                                        "Éditeur HTML"
                                                                )
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.width(
                                                                                        AppSizes.paddingSmall
                                                                                )
                                                                )
                                                                Text(
                                                                        "Créer des sections HTML personnalisées"
                                                                )
                                                        }
                                                        }

                                                item {
                                                        OutlinedTextField(
                                                                value = additionalText,
                                                                onValueChange = {
                                                                        additionalText = it
                                                                },
                                                                modifier = Modifier.fillMaxWidth(),
                                                                label = {
                                                                        Text(
                                                                                "Texte additionnel (apparaît en fin de document)"
                                                                        )
                                                                },
                                                                maxLines = 6
                                                        )
                                                        }

                                                        item {
                                                        Row(
                                                                horizontalArrangement =
                                                                        Arrangement.spacedBy(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        ) {
                                                                Button(
                                                                        onClick = {
                                                                                // Générer les images de bullet graphs pour l'analyse de manière synchrone
                                                                                val bulletGraphImages = mutableMapOf<String, Map<String, String>>()
                                                                                
                                                                                selectedRation?.let { ration ->
                                                                                        try {
                                                                                                // Charger les préférences de manière synchrone
                                                                                                val prefsStorage = createPreferencesStorage()
                                                                                                val prefsRepo = PreferencesRepository(prefsStorage)
                                                                                                
                                                                                                // Charger les préférences de manière asynchrone
                                                                                                scope.launch {
                                                                                                        prefsRepo.loadPreferences()
                                                                                                }
                                                                                                val prefs = prefsRepo.preferences
                                                                                                val prefsEspece = prefs?.getPreferencesEspece(animalDetails?.getEspece() ?: fr.vetbrain.vetnutri_mp.Enumer.Espece.CHIEN)
                                                                                                
                                                                                                val ref = referenceUtilisee
                                                                                                if (prefsEspece != null && ref != null) {
                                                                                                        // Générer les images avec la logique de RationsView
                                                                                                        val images = fr.vetbrain.vetnutri_mp.Export.BulletGraphImageCapture.generateRationBulletGraphImages(
                                                                                                                ration = ration,
                                                                                                                reference = ref,
                                                                                                                animal = animalDetails,
                                                                                                                preferences = prefsEspece,
                                                                                                                poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                                                                                                                poidsMetabolique = poidsMetabolique,
                                                                                                                besoinEnergetiqueEntretien = besoinEnergetiqueStandard,
                                                                                                                equationRepository = equationRepository
                                                                                                        )
                                                                                                        
                                                                                                        // Convertir les ByteArray en chemins de fichiers temporaires
                                                                                                        val imagePaths = images.mapValues { (_, imageBytes) ->
                                                        val tempFilePath = fr.vetbrain.vetnutri_mp.Export.BulletGraphImageCapture.saveImageToTempFile(imageBytes, "temp")
                                                        "file://$tempFilePath"
                                                                                                        }
                                                                                                        
                                                                                                        bulletGraphImages[ration.uuid] = imagePaths
                                                                                                        println("DEBUG: Généré ${imagePaths.size} images de bullet graphs")
                                                                                                } else {
                                                                                                        println("DEBUG: Données manquantes - prefsEspece: ${prefsEspece != null}, ref: ${ref != null}")
                                                                                                        
                                                                                                        // Générer des images de test même sans les vraies données
                                                                                                        println("DEBUG: Génération d'images de test...")
                                                                                                        val testImages = mutableMapOf<String, ByteArray>()
                                                                                                        listOf("PROTEINE", "LIPIDE", "ENA", "CELLULOSE", "CENDRE", "CAL", "PHOS").forEach { nom ->
                                                                                                                val imageBytes = fr.vetbrain.vetnutri_mp.Export.BulletGraphImageCapture.generateBulletGraphImage(
                                                                                                                        nom, 25.0, 15.0, 40.0, 20.0, 35.0, "g/kg DM"
                                                                                                                )
                                                                                                                testImages[nom] = imageBytes
                                                                                                        }
                                                                                                        
                                                                                                        val testImagePaths = testImages.mapValues { (_, imageBytes) ->
                                                                                                                val tempFilePath = fr.vetbrain.vetnutri_mp.Export.BulletGraphImageCapture.saveImageToTempFile(imageBytes, "test")
                                                                                                                "file://$tempFilePath"
                                                                                                        }
                                                                                                        
                                                                                                        bulletGraphImages[ration.uuid] = testImagePaths
                                                                                                        println("DEBUG: Généré ${testImagePaths.size} images de test")
                                                                                                }
                                                                                        } catch (e: Exception) {
                                                                                                // En cas d'erreur, continuer sans les images
                                                                                                println("Erreur génération bullet graphs: ${e.message}")
                                                                                                e.printStackTrace()
                                                                                        }
                                                                                }
                                                                                
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
                                                                                                                        additionalText,
                                                                                                                htmlSections =
                                                                                                                        getSelectedConseils(),
                                                                                                                preferences = null,
                                                                                                                poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                                                                                                                poidsMetabolique = poidsMetabolique,
                                                                                                                besoinEnergetiqueEntretien = besoinEnergetiqueStandard,
                                                                                                                bulletGraphImages = bulletGraphImages
                                                                                                        )
                                                                                                )
                                                                                showPreview = true
                                                                        }
                                                                ) { Text("Exporter analyse PDF") }

                                                                Button(
                                                                        onClick = {
                                                                                // Charger les préférences utilisateur pour l'en-tête praticien
                                                                                val prefsStorage = createPreferencesStorage()
                                                                                val prefsRepo = PreferencesRepository(prefsStorage)
                                                                                kotlinx.coroutines.GlobalScope.launch {
                                                                                        try {
                                                                                                prefsRepo.loadPreferences()
                                                                                                val prefs = prefsRepo.preferences
                                                                                                val practitioner = fr.vetbrain.vetnutri_mp.Export.PractitionerInfo(
                                                                                                        nom = prefs.nomUtilisateur,
                                                                                                        numeroOrdre = prefs.numeroOrdre,
                                                                                                        adressePostale = prefs.adressePostale,
                                                                                                        codePostal = prefs.codePostal,
                                                                                                        ville = prefs.ville,
                                                                                                        telephone = prefs.telephone,
                                                                                                        email = prefs.email
                                                                                                )
                                                                                                val allRations = selectedConsultation?.rations?.toList() ?: emptyList()
                                                                                                
                                                                                                // Générer la prévisualisation (sans bullet graphs pour l'ordonnance)
                                                                                                previewHtml =
                                                                                                        HtmlDocumentBuilder
                                                                                                                .buildHtml(
                                                                                                                        DocumentType
                                                                                                                                .PRESCRIPTION,
                                                                                                                        ExportData(
                                                                                                                                animal =
                                                                                                                                        animalDetails,
                                                                                                                                ration =
                                                                                                                                        null,
                                                                                                                                reference =
                                                                                                                                        referenceUtilisee,
                                                                                                                                conseils =
                                                                                                                                        listOf(
                                                                                                                                               
                                                                                                                                                "Veiller à l'hydratation"
                                                                                                                                        ),
                                                                                                                                title =
                                                                                                                                        "Ordonnance nutritionnelle",
                                                                                                                                additionalText =
                                                                                                                                        additionalText,
                                                                                                                                htmlSections =
                                                                                                                                        getSelectedConseils(),
                                                                                                                                rations = allRations,
                                                                                                                                practitioner = practitioner,
                                                                                                                                preferences = null,
                                                                                                                                poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                                                                                                                                poidsMetabolique = null,
                                                                                                                                besoinEnergetiqueEntretien = null
                                                                                                                        )
                                                                                                                )
                                                                                                showPreview = true
                                                                                        } catch (e: Exception) {
                                                                                                // En cas d'erreur, prévisualiser sans les informations du prescripteur
                                                                                                val allRations = selectedConsultation?.rations?.toList() ?: emptyList()
                                                                                                
                                                                                                previewHtml = HtmlDocumentBuilder.buildHtml(
                                                                                                        DocumentType.PRESCRIPTION,
                                                                                                        ExportData(
                                                                                                                animal = animalDetails,
                                                                                                                ration = null,
                                                                                                                reference = referenceUtilisee,
                                                                                                                conseils = emptyList(),
                                                                                                                title = "Ordonnance nutritionnelle",
                                                                                                                additionalText = additionalText,
                                                                                                                htmlSections = getSelectedConseils(),
                                                                                                                rations = allRations,
                                                                                                                practitioner = null,
                                                                                                                preferences = null,
                                                                                                                poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                                                                                                                poidsMetabolique = null,
                                                                                                                besoinEnergetiqueEntretien = null
                                                                                                        )
                                                                                                )
                                                                                                showPreview = true
                                                                                        }
                                                                                }
                                                                        }
                                                                ) {
                                                                        Text(
                                                                                "Prévisualiser ordonnance"
                                                                        )
                                                                }
                                                                }
                                                        }

                                                        // Texte additionnel
        
                                                }
                                        }

                                        // Dialogue de prévisualisation HTML (en dehors du LazyColumn)
                                                        HtmlPreviewDialog(
                                                                html = previewHtml,
                                                                isVisible = showPreview,
                                                                onConfirmExport = {
                                                                        handlePdfExport(
                                                                                previewHtml = previewHtml,
                                                                                animalDetails = animalDetails,
                                                                                selectedConsultation = selectedConsultation,
                                                                                selectedRation = selectedRation,
                                                                                referenceUtilisee = referenceUtilisee,
                                                                                                additionalText = additionalText,
                                                                                getSelectedConseils = getSelectedConseils,
                                                                                besoinEnergetiqueStandard = besoinEnergetiqueStandard,
                                                                                                                poidsMetabolique = poidsMetabolique,
                                                                                equationRepository = equationRepository,
                                                                                scope = scope
                                                                        )
                                                                        showPreview = false
                                                                },
                                                                onDismiss = { showPreview = false }
                                                        )
                                }
                        }

                        // Dialogue de recherche et sélection des conseils
                        if (showSearchDialog) {
                                AlertDialog(
                                        onDismissRequest = { showSearchDialog = false },
                                        title = { Text("Ajouter des conseils") },
                                        text = {
                                                Column {
                                                        OutlinedTextField(
                                                                value = searchQuery,
                                                                onValueChange = {
                                                                        searchQuery = it
                                                                },
                                                                label = {
                                                                        Text(
                                                                                "Rechercher un conseil..."
                                                                        )
                                                                },
                                                                modifier = Modifier.fillMaxWidth()
                                                        )

                                                        Spacer(modifier = Modifier.height(16.dp))

                                                        val filteredConseils =
                                                                availableConseils.filter { conseil
                                                                        ->
                                                                        conseil.title.contains(
                                                                                searchQuery,
                                                                                ignoreCase = true
                                                                        ) ||
                                                                                conseil.category
                                                                                        .name
                                                                                        .contains(
                                                                                                searchQuery,
                                                                                                ignoreCase =
                                                                                                        true
                                                                                        )
                                                                }

                                                        LazyColumn(
                                                                modifier =
                                                                        Modifier.heightIn(
                                                                                max = 300.dp
                                                                        ),
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(4.dp)
                                                        ) {
                                                                items(filteredConseils) { conseil ->
                                                                        val isAlreadySelected =
                                                                                selectedConseils
                                                                                        .any {
                                                                                                it.id ==
                                                                                                        conseil.id
                                                                                        }

                                                                        Card(
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth(),
                                                                                elevation =
                                                                                        if (isAlreadySelected
                                                                                        )
                                                                                                4.dp
                                                                                        else 1.dp,
                                                                                backgroundColor =
                                                                                        if (isAlreadySelected
                                                                                        )
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                                        .copy(
                                                                                                                alpha =
                                                                                                                        0.1f
                                                                                                        )
                                                                                        else
                                                                                                Color.Transparent
                                                                        ) {
                                                                                Row(
                                                                                        modifier =
                                                                                                Modifier.fillMaxWidth()
                                                                                                        .padding(
                                                                                                                12.dp
                                                                                                        ),
                                                                                        horizontalArrangement =
                                                                                                Arrangement
                                                                                                        .SpaceBetween,
                                                                                        verticalAlignment =
                                                                                                Alignment
                                                                                                        .CenterVertically
                                                                                ) {
                                                                                        Column(
                                                                                                modifier =
                                                                                                        Modifier.weight(
                                                                                                                1f
                                                                                                        )
                                                                                        ) {
                                                                                                Text(
                                                                                                        text =
                                                                                                                conseil.title,
                                                                                                        style =
                                                                                                                MaterialTheme
                                                                                                                        .typography
                                                                                                                        .body1,
                                                                                                        fontWeight =
                                                                                                                FontWeight
                                                                                                                        .Medium
                                                                                                )
                                                                                                Text(
                                                                                                        text =
                                                                                                                "Catégorie: ${conseil.category.name}",
                                                                                                        style =
                                                                                                                MaterialTheme
                                                                                                                        .typography
                                                                                                                        .caption,
                                                                                                        color =
                                                                                                                Color.Gray
                                                                                                )
                                                                                        }

                                                                                        if (isAlreadySelected
                                                                                        ) {
                                                                                                Icon(
                                                                                                        Icons.Default
                                                                                                                .Check,
                                                                                                        "Sélectionné",
                                                                                                        tint =
                                                                                                                VetNutriColors
                                                                                                                        .Primary
                                                                                                )
                                                                                        } else {
                                                                                                IconButton(
                                                                                                        onClick = {
                                                                                                                selectedConseils =
                                                                                                                        selectedConseils +
                                                                                                                                conseil
                                                                                                        }
                                                                                                ) {
                                                                                                        Icon(
                                                                                                                Icons.Default
                                                                                                                        .Add,
                                                                                                                "Ajouter",
                                                                                                                tint =
                                                                                                                        VetNutriColors
                                                                                                                                .Primary
                                                                                                        )
                                                                                                }
                                                                                        }
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        },
                                        confirmButton = {
                                                TextButton(onClick = { showSearchDialog = false }) {
                                                        Text("Fermer")
                                                }
                                        }
                                )
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
        conseilRepository: fr.vetbrain.vetnutri_mp.Repository.ConseilRepository
) {
        // État pour l'éditeur de texte enrichi
        var currentHtmlContent by remember {
                mutableStateOf(fr.vetbrain.vetnutri_mp.Export.RichTextContent())
        }
        var showRichTextEditor by remember { mutableStateOf(false) }

        // État pour les conseils personnalisés (sauvegardés)
        var availableConseils by remember {
                mutableStateOf<List<fr.vetbrain.vetnutri_mp.Export.HtmlSection>>(emptyList())
        }
        // État pour les sections HTML créées localement (temporaires)
        var localHtmlSections by remember {
                mutableStateOf<List<fr.vetbrain.vetnutri_mp.Export.HtmlSection>>(emptyList())
        }
        var selectedConseils by remember {
                mutableStateOf<List<fr.vetbrain.vetnutri_mp.Export.HtmlSection>>(emptyList())
        }
        var isLoadingConseils by remember { mutableStateOf(true) }
        var searchQuery by remember { mutableStateOf("") }
        var showSearchDialog by remember { mutableStateOf(false) }

        // Charger les conseils personnalisés
        LaunchedEffect(Unit) {
                try {
                        val result = conseilRepository.getConseilsActifs()
                        if (result.isSuccess) {
                                availableConseils = result.getOrThrow()
                        }
                } catch (e: Exception) {
                        e.printStackTrace()
                } finally {
                        isLoadingConseils = false
                }
        }

        // Variables pour la prévisualisation et l'export
        var showPreview by remember {
                mutableStateOf(false)
        }
        var previewHtml by remember {
                mutableStateOf("")
        }
        var additionalText by remember {
                mutableStateOf("")
        }

        // Fonction pour récupérer les conseils sélectionnés (conseils + sections locales)
        val getSelectedConseils:
                () -> List<fr.vetbrain.vetnutri_mp.Export.HtmlSection> =
                {
                        selectedConseils + localHtmlSections
                }

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
                                                        )
                                                }
                                                AnimalDetailSection.GRAPHIQUE -> {
                                                        AnalyseGraphiqueView(
                                                                viewModel = viewModel,
                                                                equationRepository =
                                                                        equationRepository,
                                                                modifier = Modifier.fillMaxSize()
                                                        )
                                                }
                                                AnimalDetailSection.GRAPHIQUE_ALIMENTS -> {
                                                        val availableFoods by
                                                                viewModel.availableFoods
                                                                        .collectAsState()
                                                        val isLoadingFoods by
                                                                viewModel.isLoadingFoods
                                                                        .collectAsState()

                                                        // 🔧 Récupération des préférences pour
                                                        // l'espèce dans ce contexte (même logique
                                                        // que layout large)
                                                        val preferencesStorageLocal:
                                                                fr.vetbrain.vetnutri_mp.Utils.PreferencesStorage =
                                                                remember {
                                                                        createPreferencesStorage()
                                                                }
                                                        val preferencesRepositoryLocal:
                                                                PreferencesRepository =
                                                                remember {
                                                                        PreferencesRepository(
                                                                                preferencesStorageLocal
                                                                        )
                                                                }
                                                        var preferencesApplicationLocal by remember {
                                                                mutableStateOf<
                                                                        fr.vetbrain.vetnutri_mp.Data.PreferencesApplication?>(
                                                                        null
                                                                )
                                                        }

                                                        // Charger les préférences au démarrage
                                                        LaunchedEffect(Unit) {
                                                                preferencesRepositoryLocal
                                                                        .loadPreferences()
                                                                preferencesApplicationLocal =
                                                                        preferencesRepositoryLocal
                                                                                .preferences
                                                        }

                                                        if (isLoadingFoods) {
                                                                Column(
                                                                        modifier =
                                                                                Modifier.fillMaxSize()
                                                                                        .padding(
                                                                                                AppSizes.paddingMedium
                                                                                        ),
                                                                        verticalArrangement =
                                                                                Arrangement.Center,
                                                                        horizontalAlignment =
                                                                                Alignment
                                                                                        .CenterHorizontally
                                                                ) {
                                                                        CircularProgressIndicator(
                                                                                color =
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        )
                                                                        Spacer(
                                                                                modifier =
                                                                                        Modifier.height(
                                                                                                AppSizes.paddingMedium
                                                                                        )
                                                                        )
                                                                        Text(
                                                                                "Chargement des aliments...",
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
                                                        } else if (availableFoods.isNotEmpty()) {
                                                                // ✨ MÊME LOGIQUE QUE LE LAYOUT
                                                                // LARGE - Utiliser les états du
                                                                // ViewModel pour persister la
                                                                // sélection
                                                                val showAnalyseGraphique by
                                                                        viewModel
                                                                                .showAnalyseGraphique
                                                                                .collectAsState()
                                                                val alimentsSelectionnes by
                                                                        viewModel
                                                                                .alimentsSelectionnes
                                                                                .collectAsState()

                                                                if (showAnalyseGraphique &&
                                                                                alimentsSelectionnes
                                                                                        .isNotEmpty()
                                                                ) {
                                                                        // Afficher la vue d'analyse
                                                                        // graphique
                                                                        // Récupérer les aliments
                                                                        // complets avec leurs
                                                                        // valeurs nutritionnelles
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

                                                                        LaunchedEffect(
                                                                                alimentsSelectionnes
                                                                        ) {
                                                                                isLoadingAlimentsComplets =
                                                                                        true
                                                                                val alimentsAvecValeurs =
                                                                                        mutableListOf<
                                                                                                fr.vetbrain.vetnutri_mp.Data.AlimentEv>()

                                                                                for (aliment in
                                                                                        alimentsSelectionnes) {
                                                                                        try {

                                                                                                // Récupérer l'aliment complet depuis le repository
                                                                                                val alimentComplet =
                                                                                                        viewModel.getAlimentCompletSync(
                                                                                                                aliment.uuid
                                                                                                        )

                                                                                                if (alimentComplet != null) {

                                                                                                        if (alimentComplet is AlimentEv) {
                                                                                                                alimentsAvecValeurs
                                                                                                                        .add(
                                                                                                                                alimentComplet
                                                                                                                        )
                                                                                                        }
                                                                                                } else {

                                                                                                        alimentsAvecValeurs
                                                                                                                .add(
                                                                                                                        aliment
                                                                                                                ) // Fallback
                                                                                                }
                                                                                        } catch (
                                                                                                e:
                                                                                                        Exception) {

                                                                                                e.printStackTrace()
                                                                                                alimentsAvecValeurs
                                                                                                        .add(
                                                                                                                aliment
                                                                                                        ) // Fallback
                                                                                        }
                                                                                }

                                                                                alimentsComplets =
                                                                                        alimentsAvecValeurs
                                                                                isLoadingAlimentsComplets =
                                                                                        false
                                                                        }

                                                                        if (isLoadingAlimentsComplets
                                                                        ) {
                                                                                Box(
                                                                                        modifier =
                                                                                                Modifier.fillMaxSize(),
                                                                                        contentAlignment =
                                                                                                Alignment
                                                                                                        .Center
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
                                                                                                                "Chargement des valeurs nutritionnelles...",
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
                                                                                        aliments =
                                                                                                alimentsComplets,
                                                                                        referenceEv =
                                                                                                viewModel
                                                                                                        .referenceUtilisee
                                                                                                        .value,
                                                                                        equationRepository =
                                                                                                equationRepository,
                                                                                        preferencesEspece =
                                                                                                animalDetails
                                                                                                        ?.let {
                                                                                                                animal
                                                                                                                ->
                                                                                                                preferencesApplicationLocal
                                                                                                                        ?.getPreferencesEspece(
                                                                                                                                animal.getEspece()
                                                                                                                        )
                                                                                                        },
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
                                                                        // Utiliser la vue de
                                                                        // sélection des aliments
                                                                        // avec possibilité
                                                                        // d'analyse graphique
                                                                        AnalyseSelectionAlimentsView(
                                                                                aliments =
                                                                                        availableFoods,
                                                                                onClose = { /* Retour à la section précédente */
                                                                                },
                                                                                onAlimentSelected = { /* Gestion de la sélection */
                                                                                },
                                                                                onAnalyseGraphique = {
                                                                                        aliments ->
                                                                                        viewModel
                                                                                                .lancerAnalyseGraphique(
                                                                                                        aliments
                                                                                                )
                                                                                },
                                                                                alimentsInitialementSelectionnes =
                                                                                        alimentsSelectionnes,
                                                                                onSelectionChanged = {
                                                                                        nouvelleSelection
                                                                                        ->
                                                                                        viewModel
                                                                                                .setAlimentsSelectionnes(
                                                                                                        nouvelleSelection
                                                                                                )
                                                                                }, // ✨ Synchroniser
                                                                                // avec le
                                                                                // ViewModel
                                                                                modifier =
                                                                                        Modifier.fillMaxSize()
                                                                        )
                                                                }
                                                        } else {
                                                                Column(
                                                                        modifier =
                                                                                Modifier.fillMaxSize()
                                                                                        .padding(
                                                                                                AppSizes.paddingMedium
                                                                                        ),
                                                                        verticalArrangement =
                                                                                Arrangement.Center,
                                                                        horizontalAlignment =
                                                                                Alignment
                                                                                        .CenterHorizontally
                                                                ) {
                                                                        Text(
                                                                                "Aucun aliment disponible",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .h5,
                                                                                color =
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        )
                                                                        Text(
                                                                                "Aucun aliment n'est disponible pour l'analyse graphique",
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
                                                        val besoinEnergetiqueStandard by viewModel.besoinEnergetiqueStandard.collectAsState()
                                                        val poidsMetabolique by viewModel.poidsMetabolique.collectAsState()

                                                        // Variables pour la prévisualisation et l'export
                                                        var showPreview by remember {
                                                                mutableStateOf(false)
                                                        }
                                                        var previewHtml by remember {
                                                                mutableStateOf("")
                                                        }
                                                        var additionalText by remember {
                                                                mutableStateOf("")
                                                        }

                                                        // Dialogue de prévisualisation HTML
                                                        HtmlPreviewDialog(
                                                                html = previewHtml,
                                                                isVisible = showPreview,
                                                                onConfirmExport = {
                                                                        handlePdfExport(
                                                                                previewHtml = previewHtml,
                                                                                animalDetails = animalDetails,
                                                                                selectedConsultation = selectedConsultation,
                                                                                selectedRation = selectedRation,
                                                                                referenceUtilisee = referenceUtilisee,
                                                                                additionalText = additionalText,
                                                                                getSelectedConseils = getSelectedConseils,
                                                                                besoinEnergetiqueStandard = besoinEnergetiqueStandard,
                                                                                poidsMetabolique = poidsMetabolique,
                                                                                equationRepository = equationRepository,
                                                                                scope = scope
                                                                        )
                                                                        showPreview = false
                                                                },
                                                                onDismiss = { showPreview = false }
                                                        )

                                                        if (showRichTextEditor) {
                                                                // Éditeur de texte enrichi
                                                                Column(
                                                                        modifier =
                                                                                Modifier.fillMaxSize()
                                                                ) {
                                                                        Row(
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth()
                                                                                                .padding(
                                                                                                        AppSizes.paddingMedium
                                                                                                ),
                                                                                horizontalArrangement =
                                                                                        Arrangement
                                                                                                .SpaceBetween,
                                                                                verticalAlignment =
                                                                                        Alignment
                                                                                                .CenterVertically
                                                                        ) {
                                                                                Text(
                                                                                        "Éditeur de sections HTML",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .h6,
                                                                                        color =
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                )
                                                                                Button(
                                                                                        onClick = {
                                                                                                showRichTextEditor =
                                                                                                        false
                                                                                        },
                                                                                        colors =
                                                                                                ButtonDefaults
                                                                                                        .buttonColors(
                                                                                                                backgroundColor =
                                                                                                                        VetNutriColors
                                                                                                                                .Secondary,
                                                                                                                contentColor =
                                                                                                                        VetNutriColors
                                                                                                                                .OnSecondary
                                                                                                        )
                                                                                ) { Text("Retour") }
                                                                        }

                                                                        RichTextEditor(
                                                                                initialContent =
                                                                                        currentHtmlContent,
                                                                                onContentChange = {
                                                                                        content ->
                                                                                        currentHtmlContent =
                                                                                                content
                                                                                },
                                                                                modifier =
                                                                                        Modifier.weight(
                                                                                                1f
                                                                                        )
                                                                        )

                                                                        // Boutons d'action
                                                                        Row(
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth()
                                                                                                .padding(
                                                                                                        AppSizes.paddingMedium
                                                                                                ),
                                                                                horizontalArrangement =
                                                                                        Arrangement
                                                                                                .spacedBy(
                                                                                                        AppSizes.paddingSmall
                                                                                                )
                                                                        ) {
                                                                                Button(
                                                                                        onClick = {
                                                                                                // Créer une nouvelle section HTML
                                                                                                val newSection =
                                                                                                        fr.vetbrain
                                                                                                                .vetnutri_mp
                                                                                                                .Export
                                                                                                                .HtmlSection(
                                                                                                                        id =
                                                                                                                                "section_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}",
                                                                                                                        title =
                                                                                                                                "Section personnalisée ${availableConseils.size + 1}",
                                                                                                                        content =
                                                                                                                                currentHtmlContent,
                                                                                                                        category =
                                                                                                                                fr.vetbrain
                                                                                                                                        .vetnutri_mp
                                                                                                                                        .Export
                                                                                                                                        .SectionCategory
                                                                                                                                        .CUSTOM
                                                                                                                )
                                                                                                // Ajouter à la liste des conseils disponibles
                                                                                                availableConseils =
                                                                                                        availableConseils +
                                                                                                                newSection
                                                                                                currentHtmlContent =
                                                                                                        fr.vetbrain
                                                                                                                .vetnutri_mp
                                                                                                                .Export
                                                                                                                .RichTextContent()
                                                                                                showRichTextEditor =
                                                                                                        false
                                                                                        },
                                                                                        enabled =
                                                                                                currentHtmlContent
                                                                                                        .blocks
                                                                                                        .isNotEmpty()
                                                                                ) {
                                                                                        Text(
                                                                                                "Ajouter"
                                                                                        )
                                                                                }

                                                                                OutlinedButton(
                                                                                        onClick = {
                                                                                                currentHtmlContent =
                                                                                                        fr.vetbrain
                                                                                                                .vetnutri_mp
                                                                                                                .Export
                                                                                                                .RichTextContent()
                                                                                        }
                                                                                ) {
                                                                                        Text(
                                                                                                "Effacer"
                                                                                        )
                                                                                }
                                                                        }
                                                                }
                                                        } else {
                                                                // Section export normale
                                                                LazyColumn(
                                                                        modifier =
                                                                                Modifier.fillMaxSize()
                                                                                        .padding(
                                                                                                AppSizes.paddingMedium
                                                                                        ),
                                                                        verticalArrangement =
                                                                                Arrangement
                                                                                        .spacedBy(
                                                                                                AppSizes.paddingMedium
                                                                                        )
                                                                ) {
                                                                        item {
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
                                                                        }
                                                                        item {
                                                                        Text(
                                                                                text =
                                                                                        if (selectedRation !=
                                                                                                        null
                                                                                        )
                                                                                                "Ration sélectionnée: ${selectedRation!!.name}"
                                                                                        else
                                                                                                "Aucune ration sélectionnée",
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

                                                                        // Section pour les conseils
                                                                        // personnalisés
                                                                        item {
                                                                        Text(
                                                                                "Conseils personnalisés:",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .subtitle1,
                                                                                color =
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        )
                                                                        }

                                                                        // Affichage des conseils
                                                                        // sélectionnés
                                                                        if (selectedConseils
                                                                                        .isNotEmpty()
                                                                        ) {
                                                                                item {
                                                                                Column(
                                                                                        modifier =
                                                                                                Modifier.fillMaxWidth(),
                                                                                        verticalArrangement =
                                                                                                Arrangement
                                                                                                        .spacedBy(
                                                                                                                4.dp
                                                                                                        )
                                                                                ) {
                                                                                        selectedConseils
                                                                                                .forEach {
                                                                                                        conseil
                                                                                                        ->
                                                                                                        Card(
                                                                                                                modifier =
                                                                                                                        Modifier.fillMaxWidth(),
                                                                                                                elevation =
                                                                                                                        2.dp
                                                                                                        ) {
                                                                                                                Row(
                                                                                                                        modifier =
                                                                                                                                Modifier.fillMaxWidth()
                                                                                                                                        .padding(
                                                                                                                                                8.dp
                                                                                                                                        ),
                                                                                                                        horizontalArrangement =
                                                                                                                                Arrangement
                                                                                                                                        .SpaceBetween,
                                                                                                                        verticalAlignment =
                                                                                                                                Alignment
                                                                                                                                        .CenterVertically
                                                                                                                ) {
                                                                                                                        Column(
                                                                                                                                modifier =
                                                                                                                                        Modifier.weight(
                                                                                                                                                1f
                                                                                                                                        )
                                                                                                                        ) {
                                                                                                                                Text(
                                                                                                                                        text =
                                                                                                                                                conseil.title,
                                                                                                                                        style =
                                                                                                                                                MaterialTheme
                                                                                                                                                        .typography
                                                                                                                                                        .body2,
                                                                                                                                        fontWeight =
                                                                                                                                                FontWeight
                                                                                                                                                        .Medium
                                                                                                                                )
                                                                                                                                Text(
                                                                                                                                        text =
                                                                                                                                                "Catégorie: ${conseil.category.name}",
                                                                                                                                        style =
                                                                                                                                                MaterialTheme
                                                                                                                                                        .typography
                                                                                                                                                        .caption,
                                                                                                                                        color =
                                                                                                                                                Color.Gray
                                                                                                                                )
                                                                                                                        }
                                                                                                                        IconButton(
                                                                                                                                onClick = {
                                                                                                                                        selectedConseils =
                                                                                                                                                selectedConseils
                                                                                                                                                        .filter {
                                                                                                                                                                it.id !=
                                                                                                                                                                        conseil.id
                                                                                                                                                        }
                                                                                                                                }
                                                                                                                        ) {
                                                                                                                                Icon(
                                                                                                                                        Icons.Default
                                                                                                                                                .Delete,
                                                                                                                                        "Supprimer",
                                                                                                                                        tint =
                                                                                                                                                Color.Red
                                                                                                                                )
                                                                                                                        }
                                                                                                                        }
                                                                                                                }
                                                                                                        }
                                                                                                }
                                                                                }
                                                                        }

                                                                        // Bouton pour ajouter des
                                                                        // conseils
                                                                        item {
                                                                        Button(
                                                                                onClick = {
                                                                                        showSearchDialog =
                                                                                                true
                                                                                },
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth(),
                                                                                colors =
                                                                                        ButtonDefaults
                                                                                                .buttonColors(
                                                                                                        backgroundColor =
                                                                                                                VetNutriColors
                                                                                                                        .Secondary,
                                                                                                        contentColor =
                                                                                                                VetNutriColors
                                                                                                                        .OnSecondary
                                                                                                )
                                                                        ) {
                                                                                Icon(
                                                                                        Icons.Default
                                                                                                .Add,
                                                                                        "Ajouter"
                                                                                )
                                                                                Spacer(
                                                                                        modifier =
                                                                                                Modifier.width(
                                                                                                        8.dp
                                                                                                )
                                                                                )
                                                                                Text(
                                                                                        "Ajouter des conseils"
                                                                                )
                                                                                }
                                                                        }

                                                                        item {
                                                                        Spacer(
                                                                                modifier =
                                                                                        Modifier.height(
                                                                                                16.dp
                                                                                        )
                                                                        )
                                                                        }

                                                                        // Section pour les sections
                                                                        // HTML créées localement
                                                                        if (localHtmlSections
                                                                                        .isNotEmpty()
                                                                        ) {
                                                                                item {
                                                                                Text(
                                                                                        "Sections HTML créées localement (${localHtmlSections.size}):",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .subtitle1,
                                                                                        color =
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                )
                                                                                }
                                                                                item {
                                                                                Column(
                                                                                        modifier =
                                                                                                Modifier.fillMaxWidth(),
                                                                                        verticalArrangement =
                                                                                                Arrangement
                                                                                                        .spacedBy(
                                                                                                                4.dp
                                                                                                        )
                                                                                ) {
                                                                                        localHtmlSections
                                                                                                .forEach {
                                                                                                        section
                                                                                                        ->
                                                                                                        Card(
                                                                                                                modifier =
                                                                                                                        Modifier.fillMaxWidth(),
                                                                                                                elevation =
                                                                                                                        2.dp
                                                                                                        ) {
                                                                                                                Row(
                                                                                                                        modifier =
                                                                                                                                Modifier.fillMaxWidth()
                                                                                                                                        .padding(
                                                                                                                                                8.dp
                                                                                                                                        ),
                                                                                                                        horizontalArrangement =
                                                                                                                                Arrangement
                                                                                                                                        .SpaceBetween,
                                                                                                                        verticalAlignment =
                                                                                                                                Alignment
                                                                                                                                        .CenterVertically
                                                                                                                ) {
                                                                                                                        Column(
                                                                                                                                modifier =
                                                                                                                                        Modifier.weight(
                                                                                                                                                1f
                                                                                                                                        )
                                                                                                                        ) {
                                                                                                                                Text(
                                                                                                                                        text =
                                                                                                                                                section.title,
                                                                                                                                        style =
                                                                                                                                                MaterialTheme
                                                                                                                                                        .typography
                                                                                                                                                        .body2,
                                                                                                                                        fontWeight =
                                                                                                                                                FontWeight
                                                                                                                                                        .Medium
                                                                                                                                )
                                                                                                                                Text(
                                                                                                                                        text =
                                                                                                                                                "${section.content.blocks.size} blocs",
                                                                                                                                        style =
                                                                                                                                                MaterialTheme
                                                                                                                                                        .typography
                                                                                                                                                        .caption,
                                                                                                                                        color =
                                                                                                                                                Color.Gray
                                                                                                                                )
                                                                                                                        }
                                                                                                                        IconButton(
                                                                                                                                onClick = {
                                                                                                                                        localHtmlSections =
                                                                                                                                                localHtmlSections
                                                                                                                                                        .filter {
                                                                                                                                                                it.id !=
                                                                                                                                                                        section.id
                                                                                                                                                        }
                                                                                                                                }
                                                                                                                        ) {
                                                                                                                                Icon(
                                                                                                                                        Icons.Default
                                                                                                                                                .Delete,
                                                                                                                                        "Supprimer",
                                                                                                                                        tint =
                                                                                                                                                Color.Red
                                                                                                                                )
                                                                                                                        }
                                                                                                                }
                                                                                                        }
                                                                                                }
                                                                                }
                                                                                }
                                                                                item {
                                                                                Spacer(
                                                                                        modifier =
                                                                                                Modifier.height(
                                                                                                        16.dp
                                                                                                )
                                                                                )
                                                                                }
                                                                        }

                                                                        // Bouton pour accéder à
                                                                        // l'éditeur de texte
                                                                        // enrichi
                                                                        item {
                                                                        Button(
                                                                                onClick = {
                                                                                        showRichTextEditor =
                                                                                                true
                                                                                },
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth(),
                                                                                colors =
                                                                                        ButtonDefaults
                                                                                                .buttonColors(
                                                                                                        backgroundColor =
                                                                                                                VetNutriColors
                                                                                                                        .Secondary,
                                                                                                        contentColor =
                                                                                                                VetNutriColors
                                                                                                                        .OnSecondary
                                                                                                )
                                                                        ) {
                                                                                Icon(
                                                                                        Icons.Default
                                                                                                .Edit,
                                                                                        "Éditeur HTML"
                                                                                )
                                                                                Spacer(
                                                                                        modifier =
                                                                                                Modifier.width(
                                                                                                        AppSizes.paddingSmall
                                                                                                )
                                                                                )
                                                                                Text(
                                                                                        "Créer des sections HTML"
                                                                                )
                                                                                }
                                                                        }


                                                                        item {
                                                                        OutlinedTextField(
                                                                                value = additionalText,
                                                                                onValueChange = {
                                                                                        additionalText = it
                                                                                },
                                                                                modifier = Modifier.fillMaxWidth(),
                                                                                label = {
                                                                                        Text(
                                                                                                "Texte additionnel (apparaît en fin de document)"
                                                                                        )
                                                                                },
                                                                                maxLines = 6
                                                                        )
                                                                                }

                                                                        item {
                                                                        Row(
                                                                                horizontalArrangement =
                                                                                        Arrangement
                                                                                                .spacedBy(
                                                                                                        AppSizes.paddingSmall
                                                                                                )
                                                                        ) {
                                                                                Button(
                                                                                        onClick = {
                                                                                                // Utiliser une coroutine pour éviter le freeze sur iOS
                                                                                                scope.launch {
                                                                                                        // Générer les images de bullet graphs pour l'analyse
                                                                                                        val bulletGraphImages = mutableMapOf<String, Map<String, String>>()
                                                                                                        
                                                                                                        selectedRation?.let { ration ->
                                                                                                                try {
                                                                                                                        // Charger les préférences de manière asynchrone
                                                                                                                        val prefsStorage = createPreferencesStorage()
                                                                                                                        val prefsRepo = PreferencesRepository(prefsStorage)
                                                                                                                        
                                                                                                                        prefsRepo.loadPreferences()
                                                                                                                        val prefs = prefsRepo.preferences
                                                                                                                        val prefsEspece = prefs?.getPreferencesEspece(animalDetails?.getEspece() ?: fr.vetbrain.vetnutri_mp.Enumer.Espece.CHIEN)
                                                                                                                        
                                                                                                                        val ref = referenceUtilisee
                                                                                                                        if (prefsEspece != null && ref != null) {
                                                                                                                                // Générer les images avec la logique de RationsView
                                                                                                                                val images = fr.vetbrain.vetnutri_mp.Export.BulletGraphImageCapture.generateRationBulletGraphImages(
                                                                                                                                        ration = ration,
                                                                                                                                        reference = ref,
                                                                                                                                        animal = animalDetails,
                                                                                                                                        preferences = prefsEspece,
                                                                                                                                        poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                                                                                                                                        poidsMetabolique = poidsMetabolique,
                                                                                                                                        besoinEnergetiqueEntretien = besoinEnergetiqueStandard,
                                                                                                                                        equationRepository = equationRepository
                                                                                                                                )
                                                                                                                                
                                                                                                                                // Convertir les ByteArray en chemins de fichiers temporaires
                                                                                                                                val imagePaths = images.mapValues { (_, imageBytes) ->
                                                                                                                                        val tempFilePath = fr.vetbrain.vetnutri_mp.Export.BulletGraphImageCapture.saveImageToTempFile(imageBytes, "temp")
                                                                                                                                        "file://$tempFilePath"
                                                                                                                                }
                                                                                                                                
                                                                                                                                bulletGraphImages[ration.uuid] = imagePaths
                                                                                                                                println("DEBUG: Généré ${imagePaths.size} images de bullet graphs")
                                                                                                                        } else {
                                                                                                                                println("DEBUG: Données manquantes - prefsEspece: ${prefsEspece != null}, ref: ${ref != null}")
                                                                                                                                
                                                                                                                                // Générer des images de test même sans les vraies données
                                                                                                                                println("DEBUG: Génération d'images de test...")
                                                                                                                                val testImages = mutableMapOf<String, ByteArray>()
                                                                                                                                listOf("PROTEINE", "LIPIDE", "ENA", "CELLULOSE", "CENDRE", "CAL", "PHOS").forEach { nom ->
                                                                                                                                        val imageBytes = fr.vetbrain.vetnutri_mp.Export.BulletGraphImageCapture.generateBulletGraphImage(
                                                                                                                                                nom, 25.0, 15.0, 40.0, 20.0, 35.0, "g/kg DM"
                                                                                                                                        )
                                                                                                                                        testImages[nom] = imageBytes
                                                                                                                                }
                                                                                                                                
                                                                                                                                val testImagePaths = testImages.mapValues { (_, imageBytes) ->
                                                                                                                                        val tempFilePath = fr.vetbrain.vetnutri_mp.Export.BulletGraphImageCapture.saveImageToTempFile(imageBytes, "test")
                                                                                                                                        "file://$tempFilePath"
                                                                                                                                }
                                                                                                                                
                                                                                                                                bulletGraphImages[ration.uuid] = testImagePaths
                                                                                                                                println("DEBUG: Généré ${testImagePaths.size} images de test")
                                                                                                                        }
                                                                                                                } catch (e: Exception) {
                                                                                                                        // En cas d'erreur, continuer sans les images
                                                                                                                        println("Erreur génération bullet graphs: ${e.message}")
                                                                                                                        e.printStackTrace()
                                                                                                                }
                                                                                                        }
                                                                                                        
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
                                                                                                                                                additionalText,
                                                                                                                        htmlSections =
                                                                                                                                                getSelectedConseils(),
                                                                                                                                        preferences = null,
                                                                                                                                        poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                                                                                                                                        poidsMetabolique = poidsMetabolique,
                                                                                                                                        besoinEnergetiqueEntretien = besoinEnergetiqueStandard,
                                                                                                                                        bulletGraphImages = bulletGraphImages
                                                                                                                                )
                                                                                                                        )
                                                                                                        showPreview = true
                                                                                                }
                                                                                        }
                                                                                ) { Text("Prévisualiser analyse PDF") }

                                                                                Button(
                                                                                        onClick = {
                                                                                                // Utiliser une coroutine pour éviter le freeze sur iOS
                                                                                                scope.launch {
                                                                                                        try {
                                                                                                                // Charger les préférences utilisateur pour l'en-tête praticien
                                                                                                                val prefsStorage = createPreferencesStorage()
                                                                                                                val prefsRepo = PreferencesRepository(prefsStorage)
                                                                                                                
                                                                                                                prefsRepo.loadPreferences()
                                                                                                                val prefs = prefsRepo.preferences
                                                                                                                val practitioner = fr.vetbrain.vetnutri_mp.Export.PractitionerInfo(
                                                                                                                        nom = prefs.nomUtilisateur,
                                                                                                                        numeroOrdre = prefs.numeroOrdre,
                                                                                                                        adressePostale = prefs.adressePostale,
                                                                                                                        codePostal = prefs.codePostal,
                                                                                                                        ville = prefs.ville,
                                                                                                                        telephone = prefs.telephone,
                                                                                                                        email = prefs.email
                                                                                                                )
                                                                                                                val allRations = selectedConsultation?.rations?.toList() ?: emptyList()
                                                                                                                
                                                                                                                // Générer la prévisualisation (sans bullet graphs pour l'ordonnance)
                                                                                                                previewHtml =
                                                                                                                        HtmlDocumentBuilder
                                                                                                                                .buildHtml(
                                                                                                                DocumentType
                                                                                                                        .PRESCRIPTION,
                                                                                                                ExportData(
                                                                                                                        animal =
                                                                                                                                animalDetails,
                                                                                                                        ration =
                                                                                                                                null,
                                                                                                                                                reference =
                                                                                                                                                        referenceUtilisee,
                                                                                                                        conseils =
                                                                                                                                listOf(
                                                                                                                                        
                                                                                                                                                                "Veiller à l'hydratation"
                                                                                                                                ),
                                                                                                                        title =
                                                                                                                                "Ordonnance nutritionnelle",
                                                                                                                                                additionalText =
                                                                                                                                                        additionalText,
                                                                                                                        htmlSections =
                                                                                                                                                        getSelectedConseils(),
                                                                                                                                                rations = allRations,
                                                                                                                                                practitioner = practitioner,
                                                                                                                                                preferences = null,
                                                                                                                                                poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                                                                                                                                                poidsMetabolique = null,
                                                                                                                                                besoinEnergetiqueEntretien = null
                                                                                                                                        )
                                                                                                                                )
                                                                                                                showPreview = true
                                                                                                        } catch (e: Exception) {
                                                                                                                // En cas d'erreur, prévisualiser sans les informations du prescripteur
                                                                                                                val allRations = selectedConsultation?.rations?.toList() ?: emptyList()
                                                                                                                
                                                                                                                previewHtml = HtmlDocumentBuilder.buildHtml(
                                                                                                                        DocumentType.PRESCRIPTION,
                                                                                                                        ExportData(
                                                                                                                                animal = animalDetails,
                                                                                                                                ration = null,
                                                                                                                                reference = referenceUtilisee,
                                                                                                                                conseils = emptyList(),
                                                                                                                                title = "Ordonnance nutritionnelle",
                                                                                                                                additionalText = additionalText,
                                                                                                                                htmlSections = getSelectedConseils(),
                                                                                                                                rations = allRations,
                                                                                                                                practitioner = null,
                                                                                                                                preferences = null,
                                                                                                                                poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                                                                                                                                poidsMetabolique = null,
                                                                                                                                besoinEnergetiqueEntretien = null
                                                                                                                        )
                                                                                                                )
                                                                                                                showPreview = true
                                                                                                        }
                                                                                                }
                                                                                        }
                                                                                ) {
                                                                                        Text(
                                                                                                "Prévisualiser ordonnance"
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

                        // Dialogue de recherche et sélection des conseils
                        if (showSearchDialog) {
                                AlertDialog(
                                        onDismissRequest = { showSearchDialog = false },
                                        title = { Text("Ajouter des conseils") },
                                        text = {
                                                Column {
                                                        OutlinedTextField(
                                                                value = searchQuery,
                                                                onValueChange = {
                                                                        searchQuery = it
                                                                },
                                                                label = {
                                                                        Text(
                                                                                "Rechercher un conseil..."
                                                                        )
                                                                },
                                                                modifier = Modifier.fillMaxWidth()
                                                        )

                                                        Spacer(modifier = Modifier.height(16.dp))

                                                        val filteredConseils =
                                                                availableConseils.filter { conseil
                                                                        ->
                                                                        conseil.title.contains(
                                                                                searchQuery,
                                                                                ignoreCase = true
                                                                        ) ||
                                                                                conseil.category
                                                                                        .name
                                                                                        .contains(
                                                                                                searchQuery,
                                                                                                ignoreCase =
                                                                                                        true
                                                                                        )
                                                                }

                                                        LazyColumn(
                                                                modifier =
                                                                        Modifier.heightIn(
                                                                                max = 300.dp
                                                                        ),
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(4.dp)
                                                        ) {
                                                                items(filteredConseils) { conseil ->
                                                                        val isAlreadySelected =
                                                                                selectedConseils
                                                                                        .any {
                                                                                                it.id ==
                                                                                                        conseil.id
                                                                                        }

                                                                        Card(
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth(),
                                                                                elevation =
                                                                                        if (isAlreadySelected
                                                                                        )
                                                                                                4.dp
                                                                                        else 1.dp,
                                                                                backgroundColor =
                                                                                        if (isAlreadySelected
                                                                                        )
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                                        .copy(
                                                                                                                alpha =
                                                                                                                        0.1f
                                                                                                        )
                                                                                        else
                                                                                                Color.Transparent
                                                                        ) {
                                                                                Row(
                                                                                        modifier =
                                                                                                Modifier.fillMaxWidth()
                                                                                                        .padding(
                                                                                                                12.dp
                                                                                                        ),
                                                                                        horizontalArrangement =
                                                                                                Arrangement
                                                                                                        .SpaceBetween,
                                                                                        verticalAlignment =
                                                                                                Alignment
                                                                                                        .CenterVertically
                                                                                ) {
                                                                                        Column(
                                                                                                modifier =
                                                                                                        Modifier.weight(
                                                                                                                1f
                                                                                                        )
                                                                                        ) {
                                                                                                Text(
                                                                                                        text =
                                                                                                                conseil.title,
                                                                                                        style =
                                                                                                                MaterialTheme
                                                                                                                        .typography
                                                                                                                        .body1,
                                                                                                        fontWeight =
                                                                                                                FontWeight
                                                                                                                        .Medium
                                                                                                )
                                                                                                Text(
                                                                                                        text =
                                                                                                                "Catégorie: ${conseil.category.name}",
                                                                                                        style =
                                                                                                                MaterialTheme
                                                                                                                        .typography
                                                                                                                        .caption,
                                                                                                        color =
                                                                                                                Color.Gray
                                                                                                )
                                                                                        }

                                                                                        if (isAlreadySelected
                                                                                        ) {
                                                                                                Icon(
                                                                                                        Icons.Default
                                                                                                                .Check,
                                                                                                        "Sélectionné",
                                                                                                        tint =
                                                                                                                VetNutriColors
                                                                                                                        .Primary
                                                                                                )
                                                                                        } else {
                                                                                                IconButton(
                                                                                                        onClick = {
                                                                                                                selectedConseils =
                                                                                                                        selectedConseils +
                                                                                                                                conseil
                                                                                                        }
                                                                                                ) {
                                                                                                        Icon(
                                                                                                                Icons.Default
                                                                                                                        .Add,
                                                                                                                "Ajouter",
                                                                                                                tint =
                                                                                                                        VetNutriColors
                                                                                                                                .Primary
                                                                                                        )
                                                                                                }
                                                                                        }
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        },
                                        confirmButton = {
                                                TextButton(onClick = { showSearchDialog = false }) {
                                                        Text("Fermer")
                                                }
                                        }
                                )
                        }

                }
        )
}
