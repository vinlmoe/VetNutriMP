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
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Export.DocumentType
import fr.vetbrain.vetnutri_mp.Export.ExportData
import fr.vetbrain.vetnutri_mp.Export.HtmlDocumentBuilder
import fr.vetbrain.vetnutri_mp.Export.HtmlPreviewDialog
import fr.vetbrain.vetnutri_mp.Export.PdfExporter
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository
import fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository
import fr.vetbrain.vetnutri_mp.Repository.RecipeRepository
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.createPreferencesStorage
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailSection
import fr.vetbrain.vetnutri_mp.exportJsonToFile
import kotlinx.coroutines.withContext
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import fr.vetbrain.vetnutri_mp.Service.JsonShareService
import fr.vetbrain.vetnutri_mp.Service.ShareOptions
import fr.vetbrain.vetnutri_mp.Components.ShareLinkDialog

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
 * Fonction pour exporter un animal complet avec toutes ses données associées (références, rations, consultations, aliments)
 * Utilise un sélecteur de fichier pour choisir l'emplacement de sauvegarde
 */
private suspend fun exporterAnimalComplet(
        animal: AnimalEv,
        viewModel: AnimalDetailViewModel,
        settingsViewModel: SettingsViewModel,
        equationRepository: EquationRepository,
        recipeRepository: RecipeRepository,
        conseilRepository: fr.vetbrain.vetnutri_mp.Repository.ConseilRepository,
        snackbarHostState: SnackbarHostState
) {
        try {
                // Créer le repository d'export/import avec tous les repositories nécessaires depuis settingsViewModel (sur IO)
                val exportImportRepository = withContext(AppDispatchers.IO) {
                        ExportImportRepository(
                                animalRepository = settingsViewModel.animalRepository,
                                foodRepository = settingsViewModel.foodRepository,
                                equationRepository = settingsViewModel.equationRepository ?: equationRepository,
                                referenceRepository = settingsViewModel.referenceEvRepository,
                                biblioRepository = settingsViewModel.biblioRefRepository,
                                consultationRepository = settingsViewModel.consultationRepository,
                                recipeRepository = settingsViewModel.recipeRepository ?: recipeRepository,
                                conseilRepository = settingsViewModel.conseilRepository ?: conseilRepository
                        )
                }
                
                // Collecter tous les aliments utilisés dans les rations de l'animal (sur IO)
                val foodIds = withContext(AppDispatchers.IO) {
                        val ids = mutableSetOf<String>()
                        animal.consultations.forEach { consultation ->
                                consultation.rations.forEach { ration ->
                                        ration.alimentMutableList.forEach { alimentRation ->
                                                // Collecter l'ID depuis refAlimUnif ou depuis l'aliment complet
                                                alimentRation.refAlimUnif?.takeIf { it.isNotBlank() }?.let { ids.add(it) }
                                                alimentRation.aliment?.uuid?.takeIf { it.isNotBlank() }?.let { ids.add(it) }
                                        }
                                }
                        }
                        ids
                }
                
                // Collecter toutes les références nutritionnelles utilisées dans les consultations (sur IO)
                val (referenceIds, equationIds) = withContext(AppDispatchers.IO) {
                        val refIds = mutableSetOf<String>()
                        val eqIds = mutableSetOf<String>()
                        
                        animal.consultations.forEach { consultation ->
                                // Référence générale
                                consultation.referenceGeneraleId?.let { refId ->
                                        refIds.add(refId)
                                        // Charger la référence pour obtenir ses équations
                                        val reference = settingsViewModel.referenceEvRepository?.getReferenceEvById(refId)
                                        reference?.let { ref ->
                                                // Collecter toutes les équations de la référence
                                                ref.obtenirToutesEquations().forEach { equation ->
                                                        eqIds.add(equation.uuid)
                                                }
                                        }
                                }
                                // Références maladies
                                consultation.referencesMaladies.forEach { refId ->
                                        refIds.add(refId)
                                        // Charger la référence pour obtenir ses équations
                                        val reference = settingsViewModel.referenceEvRepository?.getReferenceEvById(refId)
                                        reference?.let { ref ->
                                                // Collecter toutes les équations de la référence
                                                ref.obtenirToutesEquations().forEach { equation ->
                                                        eqIds.add(equation.uuid)
                                                }
                                        }
                                }
                        }
                        Pair(refIds, eqIds)
                }
                
                // Exporter avec sélection (sur IO)
                // Note: Les références bibliographiques sont automatiquement incluses dans l'export
                // car elles sont liées aux références nutritionnelles et équations exportées
                val jsonContent = withContext(AppDispatchers.IO) {
                        val exportOptions = ExportImportRepository.ExportSelectionOptions(
                                includeAnimals = true,
                                includeFoods = true,
                                includeRations = true,
                                includeRecipes = false,
                                includeEquations = true,
                                includeConseils = false,
                                animalIds = setOf(animal.uuid),
                                foodIds = foodIds,
                                referenceIds = referenceIds,
                                equationIds = equationIds
                        )
                        exportImportRepository.exportWithSelection(exportOptions)
                }
                
                // Générer le nom de fichier
                val fileName = "${animal.id ?: animal.uuid}_${animal.nom}_export.json"
                
                // Sauvegarder le fichier avec sélecteur (sur Main pour l'UI)
                val success = withContext(AppDispatchers.Main) {
                        exportJsonToFile(jsonContent, fileName)
                }
                
                // Afficher le résultat (déjà sur Main)
                if (success) {
                        snackbarHostState.showSnackbar(
                                message = "Animal exporté avec succès: $fileName",
                                duration = SnackbarDuration.Short
                        )
                } else {
                        snackbarHostState.showSnackbar(
                                message = "Export annulé ou erreur lors de l'export",
                                duration = SnackbarDuration.Long
                        )
                }
        } catch (e: Exception) {
                e.printStackTrace()
                snackbarHostState.showSnackbar(
                        message = "Erreur lors de l'export: ${e.message}",
                        duration = SnackbarDuration.Long
                )
        }
}

/**
 * Fonction pour partager un animal complet en ligne via jsonbin.io
 * Génère un lien de partage unique que l'utilisateur peut partager
 */
private suspend fun partagerAnimalEnLigne(
        animal: AnimalEv,
        viewModel: AnimalDetailViewModel,
        settingsViewModel: SettingsViewModel,
        equationRepository: EquationRepository,
        recipeRepository: RecipeRepository,
        conseilRepository: fr.vetbrain.vetnutri_mp.Repository.ConseilRepository,
        snackbarHostState: SnackbarHostState,
        onShareLinkGenerated: (fr.vetbrain.vetnutri_mp.Service.ShareLink) -> Unit
) {
        try {
                // Créer le repository d'export/import avec tous les repositories nécessaires depuis settingsViewModel (sur IO)
                val exportImportRepository = withContext(AppDispatchers.IO) {
                        ExportImportRepository(
                                animalRepository = settingsViewModel.animalRepository,
                                foodRepository = settingsViewModel.foodRepository,
                                equationRepository = settingsViewModel.equationRepository ?: equationRepository,
                                referenceRepository = settingsViewModel.referenceEvRepository,
                                biblioRepository = settingsViewModel.biblioRefRepository,
                                consultationRepository = settingsViewModel.consultationRepository,
                                recipeRepository = settingsViewModel.recipeRepository ?: recipeRepository,
                                conseilRepository = settingsViewModel.conseilRepository ?: conseilRepository
                        )
                }
                
                // Collecter tous les aliments utilisés dans les rations de l'animal (sur IO)
                val foodIds = withContext(AppDispatchers.IO) {
                        val ids = mutableSetOf<String>()
                        animal.consultations.forEach { consultation ->
                                consultation.rations.forEach { ration ->
                                        ration.alimentMutableList.forEach { alimentRation ->
                                                alimentRation.refAlimUnif?.takeIf { it.isNotBlank() }?.let { ids.add(it) }
                                                alimentRation.aliment?.uuid?.takeIf { it.isNotBlank() }?.let { ids.add(it) }
                                        }
                                }
                        }
                        ids
                }
                
                // Collecter toutes les références nutritionnelles utilisées dans les consultations (sur IO)
                val (referenceIds, equationIds) = withContext(AppDispatchers.IO) {
                        val refIds = mutableSetOf<String>()
                        val eqIds = mutableSetOf<String>()
                        
                        animal.consultations.forEach { consultation ->
                                consultation.referenceGeneraleId?.let { refId ->
                                        refIds.add(refId)
                                        val reference = settingsViewModel.referenceEvRepository?.getReferenceEvById(refId)
                                        reference?.let { ref ->
                                                ref.obtenirToutesEquations().forEach { equation ->
                                                        eqIds.add(equation.uuid)
                                                }
                                        }
                                }
                                consultation.referencesMaladies.forEach { refId ->
                                        refIds.add(refId)
                                        val reference = settingsViewModel.referenceEvRepository?.getReferenceEvById(refId)
                                        reference?.let { ref ->
                                                ref.obtenirToutesEquations().forEach { equation ->
                                                        eqIds.add(equation.uuid)
                                                }
                                        }
                                }
                        }
                        Pair(refIds, eqIds)
                }
                
                // Exporter avec sélection (sur IO)
                val jsonContent = withContext(AppDispatchers.IO) {
                        val exportOptions = ExportImportRepository.ExportSelectionOptions(
                                includeAnimals = true,
                                includeFoods = true,
                                includeRations = true,
                                includeRecipes = false,
                                includeEquations = true,
                                includeConseils = false,
                                animalIds = setOf(animal.uuid),
                                foodIds = foodIds,
                                referenceIds = referenceIds,
                                equationIds = equationIds
                        )
                        exportImportRepository.exportWithSelection(exportOptions)
                }
                
                // Générer le nom de fichier
                val fileName = "${animal.id ?: animal.uuid}_${animal.nom}_export.json"
                
                // IMPORTANT: Recharger l'animal depuis la BDD pour s'assurer d'avoir le jsonbinId à jour
                val animalFromDb: AnimalEv? = withContext(AppDispatchers.IO) {
                        settingsViewModel.animalRepository.getAnimalById(animal.uuid)
                }
                
                // Utiliser l'animal de la BDD si disponible, sinon l'animal du ViewModel
                val animalToUse = animalFromDb ?: animal
                
                // Récupérer le binId existant directement depuis la BDD
                val existingBinId = animalToUse.jsonbinId
                
                // Si l'animal du ViewModel n'a pas le jsonbinId mais qu'il existe en BDD, mettre à jour le ViewModel
                if (animalFromDb != null && animal.jsonbinId != animalFromDb.jsonbinId) {
                        withContext(AppDispatchers.Main) {
                                viewModel.setAnimal(animalFromDb)
                        }
                }
                
                // Uploader sur jsonbin.io (sur IO)
                val shareService = fr.vetbrain.vetnutri_mp.Service.createJsonShareService()
                val shareOptions = fr.vetbrain.vetnutri_mp.Service.ShareOptions(
                        fileName = fileName,
                        expiresInHours = 168, // 7 jours par défaut
                        binName = animalToUse.uuid, // Utiliser l'UUID de l'animal comme nom du bin pour identification
                        binId = existingBinId // Utiliser le binId existant pour mise à jour
                )
                
                val shareResult = withContext(AppDispatchers.IO) {
                        shareService.uploadJson(jsonContent, shareOptions)
                }
                
                // Gérer le résultat (sur Main pour l'UI)
                withContext(AppDispatchers.Main) {
                        shareResult.fold(
                                onSuccess = { shareLink ->
                                        // Mettre à jour le binId de l'animal et le sauvegarder en base (sur IO)
                                        val animalToUpdate = animalToUse.copy(jsonbinId = shareLink.binId)
                                        withContext(AppDispatchers.IO) {
                                                viewModel.updateAnimal(animalToUpdate)
                                                
                                                // Recharger l'animal depuis la BDD pour s'assurer que le jsonbinId est bien présent
                                                val updatedAnimal: AnimalEv? = settingsViewModel.animalRepository.getAnimalById(animalToUse.uuid)
                                                if (updatedAnimal != null) {
                                                        withContext(AppDispatchers.Main) {
                                                                viewModel.setAnimal(updatedAnimal)
                                                        }
                                                }
                                        }
                                        onShareLinkGenerated(shareLink)
                                        val message = if (existingBinId != null) {
                                                "Fichier mis à jour avec succès !"
                                        } else {
                                                "Fichier uploadé avec succès !"
                                        }
                                        snackbarHostState.showSnackbar(
                                                message = message,
                                                duration = SnackbarDuration.Short
                                        )
                                },
                                onFailure = { error ->
                                        snackbarHostState.showSnackbar(
                                                message = "Erreur lors du partage en ligne: ${error.message}",
                                                duration = SnackbarDuration.Long
                                        )
                                }
                        )
                }
        } catch (e: Exception) {
                e.printStackTrace()
                withContext(AppDispatchers.Main) {
                        snackbarHostState.showSnackbar(
                                message = "Erreur lors du partage: ${e.message}",
                                duration = SnackbarDuration.Long
                        )
                }
        }
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
                                        settingsViewModel = settingsViewModel,
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
                                        settingsViewModel = settingsViewModel,
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
        settingsViewModel: SettingsViewModel,
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
        
        // État pour les messages Snackbar
        val snackbarHostState = remember { SnackbarHostState() }
        
        // État pour le partage en ligne
        var shareLink by remember { mutableStateOf<fr.vetbrain.vetnutri_mp.Service.ShareLink?>(null) }
        var showShareDialog by remember { mutableStateOf(false) }
        
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

                        // Bouton exporter animal
                        Button(
                                onClick = {
                                        scope.launch {
                                                exporterAnimalComplet(
                                                        animal = animalDetails,
                                                        viewModel = viewModel,
                                                        settingsViewModel = settingsViewModel,
                                                        equationRepository = equationRepository,
                                                        recipeRepository = recipeRepository,
                                                        conseilRepository = conseilRepository,
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
                                        contentDescription = "Exporter"
                                )
                                Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                                Text(text = "Exporter animal")
                        }
                        
                        // Bouton partager en ligne
                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                        Button(
                                onClick = {
                                        scope.launch {
                                                partagerAnimalEnLigne(
                                                        animal = animalDetails,
                                                        viewModel = viewModel,
                                                        settingsViewModel = settingsViewModel,
                                                        equationRepository = equationRepository,
                                                        recipeRepository = recipeRepository,
                                                        conseilRepository = conseilRepository,
                                                        snackbarHostState = snackbarHostState,
                                                        onShareLinkGenerated = { link ->
                                                                shareLink = link
                                                                showShareDialog = true
                                                        }
                                                )
                                        }
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
                                        contentDescription = "Partager"
                                )
                                Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                                Text(text = "Partager en ligne")
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

                // Contenu principal avec SnackbarHost
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        SnackbarHost(hostState = snackbarHostState)
                        
                        // Dialog de partage
                        shareLink?.let { link ->
                                if (showShareDialog) {
                                        ShareLinkDialog(
                                                shareLink = link,
                                                onDismiss = {
                                                        showShareDialog = false
                                                        shareLink = null
                                                },
                                                onShare = null // Peut être utilisé pour ouvrir le Share Sheet natif
                                        )
                                }
                        }
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
        settingsViewModel: SettingsViewModel,
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
        // État pour les messages Snackbar
        val snackbarHostState = remember { SnackbarHostState() }
        
        // État pour le partage en ligne
        var shareLink by remember { mutableStateOf<fr.vetbrain.vetnutri_mp.Service.ShareLink?>(null) }
        var showShareDialog by remember { mutableStateOf(false) }
        
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

                                // Bouton exporter animal
                                Button(
                                        onClick = {
                                                scope.launch {
                                                        exporterAnimalComplet(
                                                                animal = animalDetails,
                                                                viewModel = viewModel,
                                                                settingsViewModel = settingsViewModel,
                                                                equationRepository = equationRepository,
                                                                recipeRepository = recipeRepository,
                                                                conseilRepository = conseilRepository,
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
                                                contentDescription = "Exporter"
                                        )
                                        Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                                        Text(text = "Exporter animal")
                                }
                                
                                // Bouton partager en ligne
                                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                                Button(
                                        onClick = {
                                                scope.launch {
                                                        partagerAnimalEnLigne(
                                                                animal = animalDetails,
                                                                viewModel = viewModel,
                                                                settingsViewModel = settingsViewModel,
                                                                equationRepository = equationRepository,
                                                                recipeRepository = recipeRepository,
                                                                conseilRepository = conseilRepository,
                                                                snackbarHostState = snackbarHostState,
                                                                onShareLinkGenerated = { link ->
                                                                        shareLink = link
                                                                        showShareDialog = true
                                                                }
                                                        )
                                                }
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
                                                contentDescription = "Partager"
                                        )
                                        Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                                        Text(text = "Partager en ligne")
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

                                // Contenu principal avec SnackbarHost
                                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                        SnackbarHost(hostState = snackbarHostState)
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
                                                                                },
                                                                                onLoadNutrients = { foodUuids, nutrients ->
                                                                                        viewModel.loadNutrientsForFoods(foodUuids, nutrients)
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
