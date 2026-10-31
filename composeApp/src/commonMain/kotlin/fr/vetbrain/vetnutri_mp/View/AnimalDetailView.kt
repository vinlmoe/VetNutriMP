package fr.vetbrain.vetnutri_mp.View

import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import fr.vetbrain.vetnutri_mp.Utils.AppLog
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.DrawerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.ConfirmDialog
import fr.vetbrain.vetnutri_mp.Components.RichTextEditor
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Export.DocumentType
import fr.vetbrain.vetnutri_mp.Export.ExportData
import fr.vetbrain.vetnutri_mp.Export.HtmlDocumentBuilder
import fr.vetbrain.vetnutri_mp.Export.HtmlPreviewDialog
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.AnimalDetail
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Chart
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Settings
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Repository.RecipeRepository
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.copyToClipboardComposable
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailSection
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import fr.vetbrain.vetnutri_mp.Components.ShareLinkDialog
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import fr.vetbrain.vetnutri_mp.Components.AnonymizationDialog

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
        conseilRepository: fr.vetbrain.vetnutri_mp.Repository.ConseilRepository,
        isExamMode: Boolean = false
) {
        val animal by viewModel.animal.collectAsState()
        val currentSection by viewModel.currentSection.collectAsState()
        val showFullScreenEdit by viewModel.showFullScreenEdit.collectAsState()
        val selectedConsultation by viewModel.selectedConsultation.collectAsState()
        var showConsultationDetail by remember { mutableStateOf(false) }

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
                                title = translate(AnimalDetail.IDENTIFICATION),
                                icon = Icons.Default.Person
                        ),
                        MenuOption(
                                section = AnimalDetailSection.CONSULTATIONS,
                                title = translate(AnimalDetail.CONSULTATIONS),
                                icon = Icons.Default.Info
                        ),
                        MenuOption(
                                section = AnimalDetailSection.RATIONS,
                                title = translate(AnimalDetail.RATIONS),
                                icon = Icons.AutoMirrored.Filled.List
                        ),
                        MenuOption(
                                section = AnimalDetailSection.GRAPHIQUE,
                                title = translate(AnimalDetail.GRAPH),
                                icon = AppIcons.Analytics
                        ),
                        MenuOption(
                                section = AnimalDetailSection.GRAPHIQUE_ALIMENTS,
                                title = translate(AnimalDetail.FOOD_GRAPH),
                                icon = AppIcons.Analytics
                        ),
                        MenuOption(
                                section = AnimalDetailSection.EXPORT,
                                title = translate(AnimalDetail.EXPORT),
                                icon = Icons.Default.Description
                        )
                )

        var showDeleteConfirmation by remember { mutableStateOf(false) }
        var isEditing by remember { mutableStateOf(false) }

        animal?.let { animalDetails ->
                // Utiliser BoxWithConstraints pour déterminer si l'écran est large
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val isWideScreen = maxWidth > AppSizes.breakpointWideScreen

                        // Écran large : menu en barre latérale ; écran étroit : menu en tiroir
                        AnimalDetailLayout(
                                isWideScreen = isWideScreen,
                                drawerState = drawerState,
                                animalDetails = animalDetails,
                                currentSection = currentSection,
                                menuOptions = menuOptions,
                                onNavigateBack = handleNavigateBack,
                                onOpenSettings = handleOpenSettings,
                                viewModel = viewModel,
                                settingsViewModel = settingsViewModel,
                                isEditing = isEditing,
                                onIsEditingChange = { isEditing = it },
                                onShowDeleteConfirmation = { showDeleteConfirmation = true },
                                showConsultationDetail = showConsultationDetail,
                                onShowConsultationDetail = { showConsultationDetail = it },
                                equationRepository = equationRepository,
                                recipeRepository = recipeRepository,
                                conseilRepository = conseilRepository,
                                isExamMode = isExamMode
                        )

                        // Boîte de dialogue de confirmation de suppression
                        if (showDeleteConfirmation) {
                                ConfirmDialog(
                                        title = translate(General.CONFIRM_DELETE),
                                        message = translate(AnimalDetail.DELETE_CONFIRM_MESSAGE),
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

/**
 * Fiche animal : menu des sections et contenu de la section courante. Écran large : menu en
 * barre latérale permanente ; écran étroit : même menu dans un tiroir ouvert par un bouton d'en-tête.
 */
@Composable
private fun AnimalDetailLayout(
        isWideScreen: Boolean,
        drawerState: DrawerState,
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
        conseilRepository: fr.vetbrain.vetnutri_mp.Repository.ConseilRepository,
        isExamMode: Boolean = false
) {
        // Scope pour les coroutines
        val scope = rememberCoroutineScope()
        
        // État pour les messages Snackbar
        val snackbarHostState = remember { SnackbarHostState() }
        
        // État pour le partage en ligne
        var shareLink by remember { mutableStateOf<fr.vetbrain.vetnutri_mp.Service.ShareLink?>(null) }
        var showShareDialog by remember { mutableStateOf(false) }
        var showAnonymizationDialog by remember { mutableStateOf(false) }
        val shareLauncher = fr.vetbrain.vetnutri_mp.Utils.rememberShareLauncher()
        
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
        var showSearchDialog by remember { mutableStateOf(false) }
        var pendingCopyText by remember { mutableStateOf<String?>(null) }
        var anamneseText by remember { mutableStateOf("") }
        var examenCliniqueText by remember { mutableStateOf("") }
        var facteurNutritionnelClefText by remember { mutableStateOf("") }
        var additionalText by remember { mutableStateOf("") }
        var practitionerContact by remember { mutableStateOf(PractitionerContact()) }
        val selectedConsultation by viewModel.selectedConsultation.collectAsState()
        var selectedRationIdsForPrescription by remember(selectedConsultation?.uuid) {
                val initialSelection: Set<String> =
                        selectedConsultation?.rations
                                ?.filter { ration: Ration -> !ration.actual }
                                ?.map { ration: Ration -> ration.uuid }
                                ?.toSet() ?: emptySet()
                mutableStateOf(initialSelection)
        }
        var savePrescriptionJob by remember(selectedConsultation?.uuid) {
                mutableStateOf<Job?>(null)
        }

        fun schedulePrescriptionSave() {
                val consultation = selectedConsultation ?: return
                savePrescriptionJob?.cancel()
                savePrescriptionJob =
                        scope.launch {
                                delay(400)
                                val updatedConsultation =
                                        consultation.copy(
                                                prescriptionAnamnese = anamneseText,
                                                prescriptionExamenClinique = examenCliniqueText,
                                                prescriptionFacteurNutritionnelClef =
                                                        facteurNutritionnelClefText,
                                                prescriptionAdditionalText = additionalText,
                                                prescriptionSelectedConseilIds =
                                                        selectedConseils
                                                                .map { it.id }
                                                                .toMutableList(),
                                                prescriptionLocalHtmlSections =
                                                        localHtmlSections.toMutableList(),
                                                prescriptionSelectedRationIds =
                                                        selectedRationIdsForPrescription
                                                                .toMutableList()
                                        )
                                if (updatedConsultation != consultation) {
                                        viewModel.updateConsultation(updatedConsultation)
                                }
                        }
        }

        LaunchedEffect(selectedConsultation?.uuid) {
                val consultation = selectedConsultation
                if (consultation == null) {
                        anamneseText = ""
                        examenCliniqueText = ""
                        facteurNutritionnelClefText = ""
                        additionalText = ""
                        localHtmlSections = emptyList()
                        selectedConseils = emptyList()
                        selectedRationIdsForPrescription = emptySet()
                        return@LaunchedEffect
                }
                anamneseText = consultation.prescriptionAnamnese
                examenCliniqueText = consultation.prescriptionExamenClinique
                facteurNutritionnelClefText = consultation.prescriptionFacteurNutritionnelClef
                additionalText = consultation.prescriptionAdditionalText
                localHtmlSections = consultation.prescriptionLocalHtmlSections
                if (consultation.prescriptionSelectedRationIds.isNotEmpty()) {
                        selectedRationIdsForPrescription =
                                consultation.prescriptionSelectedRationIds.toSet()
                }
                val selectedConseilIds =
                        consultation.prescriptionSelectedConseilIds.toSet()
                selectedConseils =
                        if (selectedConseilIds.isEmpty()) {
                                emptyList()
                        } else {
                                availableConseils.filter { it.id in selectedConseilIds }
                        }
        }

        LaunchedEffect(availableConseils, selectedConsultation?.uuid) {
                val consultation = selectedConsultation ?: return@LaunchedEffect
                if (selectedConseils.isNotEmpty()) {
                        return@LaunchedEffect
                }
                if (consultation.prescriptionSelectedConseilIds.isEmpty()) {
                        return@LaunchedEffect
                }
                val selectedConseilIds =
                        consultation.prescriptionSelectedConseilIds.toSet()
                selectedConseils =
                        availableConseils.filter { it.id in selectedConseilIds }
        }
        // Charger les conseils personnalisés
        LaunchedEffect(Unit) {
                if (isExamMode) {
                        availableConseils = emptyList()
                        isLoadingConseils = false
                        return@LaunchedEffect
                }
                try {
                        val result = conseilRepository.getConseilsActifs()
                        if (result.isSuccess) {
                                availableConseils = result.getOrThrow()
                        }
                } catch (e: Exception) {
                        e.rethrowIfCancellation()
                        AppLog.e("AnimalDetailView", e.message ?: "Erreur", e)
                } finally {
                        isLoadingConseils = false
                }
        }
        LaunchedEffect(Unit) {
                try {
                        val prefs = viewModel.getLoadedPreferences()
                        practitionerContact =
                                PractitionerContact(
                                        nom = prefs.nomUtilisateur,
                                        numeroOrdre = prefs.numeroOrdre,
                                        adressePostale = prefs.adressePostale,
                                        codePostal = prefs.codePostal,
                                        ville = prefs.ville,
                                        telephone = prefs.telephone,
                                        email = prefs.email
                                )
                } catch (ignored: Exception) { ignored.rethrowIfCancellation(); AppLog.d("AnimalDetailView", "Exception ignorée : ${ignored.message}") }
        }
        
        // Variables pour la prévisualisation et l'export
        var showPreview by remember {
                mutableStateOf(false)
        }
        var previewHtml by remember {
                mutableStateOf("")
        }
        var previewMode by remember { mutableStateOf("PRESCRIPTION") }
        var previewCompteRenduText by remember { mutableStateOf("") }
        
        // Fonction pour récupérer les conseils sélectionnés (conseils + sections locales)
        val getSelectedConseils:
                () -> List<fr.vetbrain.vetnutri_mp.Export.HtmlSection> =
                {
                        selectedConseils + localHtmlSections
                }

        // Menu des sections, actions sur l'animal et accès aux paramètres ; apresNavigation ferme le
        // tiroir sur écran étroit
        val menu: @Composable (apresNavigation: () -> Unit) -> Unit = { apresNavigation ->
                Column(
                        modifier =
                                Modifier.width(250.dp)
                                        .fillMaxHeight()
                                        .verticalScroll(rememberScrollState())
                                        .padding(AppSizes.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                        // En-tête avec nom et espèce de l'animal
                        Column(modifier = Modifier.fillMaxWidth()) {
                                Text(text = animalDetails.nom, style = MaterialTheme.typography.h5)
                                if (isExamMode && !animalDetails.examExerciseId.isNullOrBlank()) {
                                        Text(
                                                text =
                                                        "${translate(Animal.EXAM_EXERCISE_ID)}: ${animalDetails.examExerciseId}",
                                                style = MaterialTheme.typography.body2,
                                                color = VetNutriColors.Primary
                                        )
                                }
                                Text(
                                        text =
                                                buildString {
                                                        append(animalDetails.getEspece().translateEnum())
                                                        if (!animalDetails.id.isNullOrBlank()) {
                                                                append(" (")
                                                                append(animalDetails.id)
                                                                append(")")
                                                        }
                                                },
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
                                                apresNavigation()
                                        }
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
                                        contentDescription = translate(AnimalDetail.EXPORT_ANIMAL)
                                )
                                Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                                Text(text = translate(AnimalDetail.EXPORT_ANIMAL))
                        }
                        
                        // Bouton partager en ligne
                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                        Button(
                                onClick = {
                                        showAnonymizationDialog = true
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

                        // Ajout de l'option Paramètres en bas du menu
                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                        MenuOptionItem(
                                option =
                                        MenuOption(
                                                section = AnimalDetailSection.IDENTIFICATION,
                                                title = translate(Settings.TITLE),
                                                icon = Icons.Default.Settings
                                        ),
                                isSelected = false,
                                onClick = {
                                        onOpenSettings()
                                        apresNavigation()
                                }
                        )
                }
        }

        // Contenu de la section courante, dialogues et messages
        val contenu: @Composable BoxScope.() -> Unit = {
                SnackbarHost(hostState = snackbarHostState)

                // Dialog d'anonymisation
                if (showAnonymizationDialog) {
                        AnonymizationDialog(
                                onConfirm = { shouldAnonymize, shouldEncrypt ->
                                        showAnonymizationDialog = false
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
                                                        },
                                                        shouldAnonymize = shouldAnonymize,
                                                        shouldEncrypt = shouldEncrypt
                                                )
                                        }
                                },
                                onDismiss = {
                                        showAnonymizationDialog = false
                                }
                        )
                }

                // Dialog de partage avec QR Code
                shareLink?.let { link ->
                        if (showShareDialog) {
                                ShareLinkDialog(
                                        shareLink = link,
                                        onDismiss = {
                                                showShareDialog = false
                                                shareLink = null
                                        },
                                        onShare = { shareLauncher(link.qrCodeData ?: link.binId) }
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
                                        isExamMode = isExamMode
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
                                                                        e.rethrowIfCancellation()

                                                                        AppLog.e("AnimalDetailView", e.message ?: "Erreur", e)
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
                        AnimalDetailSection.EXPORT -> {
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
                                                                translate(AnimalDetail.HTML_EDITOR),
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
                                                        ) { Text(translate(AnimalDetail.BACK_TO_EXPORT)) }
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
                                                                                                        translate(
                                                                                                                AnimalDetail.CUSTOM_SECTION_TITLE,
                                                                                                                (availableConseils.size + 1).toString()
                                                                                                        ),
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
                                                                        schedulePrescriptionSave()
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
                                                        ) { Text(translate(AnimalDetail.ADD_SECTION)) }

                                                        OutlinedButton(
                                                                onClick = {
                                                                        currentHtmlContent =
                                                                                fr.vetbrain
                                                                                        .vetnutri_mp
                                                                                        .Export
                                                                                        .RichTextContent()
                                                                }
                                                        ) { Text(translate(AnimalDetail.CLEAR_SECTION)) }
                                                }
                                        }
                                } else {
                                        // Préparation de l'ordonnance et du compte rendu
                                        val compteRenduText =
                                                buildCompteRenduText(
                                                        animal = animalDetails,
                                                        consultation = selectedConsultation,
                                                        practitionerContact = practitionerContact,
                                                        anamnese = anamneseText,
                                                        examenClinique = examenCliniqueText,
                                                        facteurNutritionnelClef = facteurNutritionnelClefText,
                                                        additionalText = additionalText,
                                                        selectedConseils = selectedConseils
                                                )
                                        PrescriptionPreparationPanel(
                                                consultation = selectedConsultation,
                                                selectedRationIds = selectedRationIdsForPrescription,
                                                onSelectedRationIdsChange = {
                                                        selectedRationIdsForPrescription = it
                                                        schedulePrescriptionSave()
                                                },
                                                isExamMode = isExamMode,
                                                selectedConseils = selectedConseils,
                                                onRemoveConseil = { conseil ->
                                                        selectedConseils = selectedConseils.filter { it.id != conseil.id }
                                                        schedulePrescriptionSave()
                                                },
                                                onAddConseil = { showSearchDialog = true },
                                                localSections = localHtmlSections,
                                                onRemoveLocalSection = { section ->
                                                        localHtmlSections = localHtmlSections.filter { it.id != section.id }
                                                        schedulePrescriptionSave()
                                                },
                                                onOpenSectionEditor = { showRichTextEditor = true },
                                                anamnese = anamneseText,
                                                onAnamneseChange = {
                                                        anamneseText = it
                                                        schedulePrescriptionSave()
                                                },
                                                examenClinique = examenCliniqueText,
                                                onExamenCliniqueChange = {
                                                        examenCliniqueText = it
                                                        schedulePrescriptionSave()
                                                },
                                                facteurNutritionnelClef = facteurNutritionnelClefText,
                                                onFacteurNutritionnelClefChange = {
                                                        facteurNutritionnelClefText = it
                                                        schedulePrescriptionSave()
                                                },
                                                additionalText = additionalText,
                                                onAdditionalTextChange = {
                                                        additionalText = it
                                                        schedulePrescriptionSave()
                                                },
                                                onCopyCompteRendu = {
                                                        pendingCopyText = compteRenduText
                                                        scope.launch {
                                                                snackbarHostState.showSnackbar(translate(AnimalDetail.CR_COPY_SUCCESS))
                                                        }
                                                },
                                                onPreviewCompteRendu = {
                                                        previewMode = "CR"
                                                        previewCompteRenduText = compteRenduText
                                                        previewHtml =
                                                                buildCompteRenduHtml(
                                                                        animal = animalDetails,
                                                                        consultation = selectedConsultation,
                                                                        practitionerContact = practitionerContact,
                                                                        anamnese = anamneseText,
                                                                        examenClinique = examenCliniqueText,
                                                                        facteurNutritionnelClef = facteurNutritionnelClefText,
                                                                        additionalText = additionalText,
                                                                        selectedConseils = selectedConseils
                                                                )
                                                        showPreview = true
                                                },
                                                onPreviewPrescription = {
                                                        scope.launch {
                                                                try {
                                                                        val prefs = viewModel.getLoadedPreferences()
                                                                        val practitioner = fr.vetbrain.vetnutri_mp.Export.PractitionerInfo(
                                                                                nom = prefs.nomUtilisateur,
                                                                                numeroOrdre = prefs.numeroOrdre,
                                                                                adressePostale = prefs.adressePostale,
                                                                                codePostal = prefs.codePostal,
                                                                                ville = prefs.ville,
                                                                                telephone = prefs.telephone,
                                                                                email = prefs.email
                                                                        )
                                                                        val selectedRationsForPrescription: List<Ration> =
                                                                                selectedConsultation?.rations
                                                                                        ?.filter { ration: Ration ->
                                                                                                selectedRationIdsForPrescription
                                                                                                        .contains(
                                                                                                                ration.uuid
                                                                                                        )
                                                                                        }
                                                                                        ?.toList()
                                                                                                ?: emptyList()
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
                                                                                                                        translate(AnimalDetail.DEFAULT_ADVICE_HYDRATION)
                                                                                                                ),
                                                                                                        title =
                                                                                                                translate(AnimalDetail.PRESCRIPTION_TITLE),
                                                                                                        additionalText =
                                                                                                                additionalText,
                                                                                                        htmlSections =
                                                                                                                getSelectedConseils(),
                                                                                                        rations = selectedRationsForPrescription,
                                                                                                        practitioner = practitioner,
                                                                                                        poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                                                                                                        poidsMetabolique = null,
                                                                                                        besoinEnergetiqueEntretien = null,
                                                                                                        // Plan évolutif exporté en tableau ingrédients × étapes
                                                                                                        consultation = selectedConsultation
                                                                                                )
                                                                                        )
                                                                        previewMode = "PRESCRIPTION"
                                                                        previewCompteRenduText = ""
                                                                        showPreview = true
                                                                } catch (e: Exception) {
                                                                        e.rethrowIfCancellation()
                                                                        val selectedRationsForPrescription: List<Ration> =
                                                                                selectedConsultation?.rations
                                                                                        ?.filter { ration: Ration ->
                                                                                                selectedRationIdsForPrescription
                                                                                                        .contains(
                                                                                                                ration.uuid
                                                                                                        )
                                                                                        }
                                                                                        ?.toList()
                                                                                                ?: emptyList()
                                                                        previewHtml = HtmlDocumentBuilder.buildHtml(
                                                                                DocumentType.PRESCRIPTION,
                                                                                ExportData(
                                                                                        animal = animalDetails,
                                                                                        ration = null,
                                                                                        reference = referenceUtilisee,
                                                                                        conseils = emptyList(),
                                                                                        title = translate(AnimalDetail.PRESCRIPTION_TITLE),
                                                                                        additionalText = additionalText,
                                                                                        htmlSections = getSelectedConseils(),
                                                                                        rations = selectedRationsForPrescription,
                                                                                        practitioner = null,
                                                                                        poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                                                                                        poidsMetabolique = null,
                                                                                        besoinEnergetiqueEntretien = null,
                                                                                        // Plan évolutif exporté en tableau ingrédients × étapes
                                                                                        consultation = selectedConsultation
                                                                                )
                                                                        )
                                                                        previewMode = "PRESCRIPTION"
                                                                        previewCompteRenduText = ""
                                                                        showPreview = true
                                                                }
                                                        }
                                                }
                                        )
                                }

                                // Dialogue de prévisualisation HTML (en dehors du LazyColumn)
                                                HtmlPreviewDialog(
                                                        html = previewHtml,
                                                        isVisible = showPreview,
                                                        onConfirmExport = {
                                                                val selectedRationsForPrescription: List<Ration> =
                                                                        selectedConsultation?.rations
                                                                                ?.filter { ration: Ration ->
                                                                                        selectedRationIdsForPrescription
                                                                                                .contains(
                                                                                                        ration.uuid
                                                                                                )
                                                                                }
                                                                                ?.toList()
                                                                                        ?: emptyList()
                                                                handlePdfExport(
                                                                        previewHtml = previewHtml,
                                                                        previewMode = previewMode,
                                                                        animalDetails = animalDetails,
                                                                        selectedConsultation = selectedConsultation,
                                                                        selectedRationsForPrescription = selectedRationsForPrescription,
                                                                        referenceUtilisee = referenceUtilisee,
                                                                        additionalText = additionalText,
                                                                        getSelectedConseils = getSelectedConseils,
                                                                        loadPreferences = viewModel::getLoadedPreferences,
                                                                        scope = scope
                                                                )
                                                                showPreview = false
                                                        },
                                                        onDismiss = { showPreview = false }
                                                )
                                if (pendingCopyText != null) {
                                        copyToClipboardComposable(pendingCopyText!!)
                                        LaunchedEffect(pendingCopyText) { pendingCopyText = null }
                                }
                        }
                }

                // Dialogue de recherche et sélection des conseils
                if (showSearchDialog) {
                        ConseilPickerDialog(
                                availableConseils = availableConseils,
                                selectedConseils = selectedConseils,
                                onAdd = { conseil ->
                                        selectedConseils = selectedConseils + conseil
                                        schedulePrescriptionSave()
                                },
                                onDismiss = { showSearchDialog = false }
                        )
                }
        }

        val examBorderModifier =
                if (isExamMode) {
                        Modifier.border(1.dp, Color.Red)
                } else {
                        Modifier
                }

        Box(
                modifier =
                        Modifier.fillMaxSize().then(examBorderModifier)
                                .padding(if (isExamMode) 2.dp else 0.dp)
        ) {
                if (isWideScreen) {
                        Row(modifier = Modifier.fillMaxSize()) {
                                menu {}
                                Box(modifier = Modifier.weight(1f).fillMaxHeight(), content = contenu)
                        }
                } else {
                        ModalDrawer(
                                drawerState = drawerState,
                                drawerContent = { menu { scope.launch { drawerState.close() } } }
                        ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                        // En-tête avec bouton menu (remplace la TopAppBar)
                                        Row(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .padding(AppSizes.paddingMedium),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                IconButtonWithTooltip(
                                                        onClick = { scope.launch { drawerState.open() } },
                                                        imageVector = Icons.Default.Menu,
                                                        contentDescription = translate(AnimalDetail.MENU),
                                                        tooltip = translate(AnimalDetail.MENU),
                                                        tint = VetNutriColors.Primary
                                                )

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

                                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), content = contenu)
                                }
                        }
                }
        }
}
