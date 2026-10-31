package fr.vetbrain.vetnutri_mp.View

import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.AppDatePicker
import fr.vetbrain.vetnutri_mp.Components.AppTextField
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationKeyword
import fr.vetbrain.vetnutri_mp.Data.avecParametresType
import fr.vetbrain.vetnutri_mp.Data.SupplementalvariableP
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.ConsultationEdit
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Ration
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalUuidApi::class)
@Composable
fun ConsultationFullScreenEditView(
        consultation: ConsultationEv?,
        animalName: String = "",
        animalEspece: fr.vetbrain.vetnutri_mp.Enumer.Espece? = null,
        availableReferences: List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv> = emptyList(),
        availableKeywords: List<ConsultationKeyword> = emptyList(),
        onBackPressed: (ConsultationEv) -> Unit,
        onCancel: () -> Unit,
        onLoadReferences: () -> Unit = {},
        onLoadKeywords: () -> Unit = {},
        onCreateKeyword: (ConsultationKeyword) -> Unit = {},
        // Troupeau : groupes d'individus (null = individu). L'état du troupeau (effectif, poids,
        // NEC, référentiel, K de chaque groupe) remplace les champs de l'individu.
        typesTroupeau: List<fr.vetbrain.vetnutri_mp.Data.TypeAnimalTroupeau>? = null
) {
        val editedConsultationState =
                remember(consultation) {
                        val newConsultation = consultation ?: ConsultationEv()

                        // Si c'est une nouvelle consultation, créer automatiquement une ration par
                        // défaut
                        if (consultation == null && newConsultation.rations.isEmpty()) {
                                val defaultRation =
                                        fr.vetbrain.vetnutri_mp.Data.Ration(
                                                idConsult = newConsultation.uuid,
                                                name = translate(Ration.CURRENT_NAME),
                                                actual = true, // Marquer comme ration actuelle par
                                                // défaut
                                                number = 1
                                        )
                                newConsultation.rations.add(defaultRation)
                        }

                        mutableStateOf(newConsultation)
                }
        var editedConsultation by editedConsultationState
        var weightText by
                remember(consultation) { mutableStateOf(consultation?.weight?.toString() ?: "") }
        var showDateError by remember(consultation) { mutableStateOf(false) }
        var showWeightError by remember(consultation) { mutableStateOf(false) }
        var dateErrorMessage by remember(consultation) { mutableStateOf<String?>(null) }
        var weightErrorMessage by remember(consultation) { mutableStateOf<String?>(null) }

        // États pour les dialogues de validation
        var showMissingVariablesDialog by remember { mutableStateOf(false) }
        var missingVariables by remember { mutableStateOf<List<String>>(emptyList()) }

        // États pour la confirmation de sortie et les avertissements de données manquantes
        var showExitConfirmationDialog by remember { mutableStateOf(false) }
        var showMissingDataDialog by remember { mutableStateOf(false) }
        var missingDataMessage by remember { mutableStateOf("") }

        // États pour les dialogues de sélection de références
        val showReferenceGeneraleDialogState = remember(consultation) { mutableStateOf(false) }
        var showReferenceGeneraleDialog by showReferenceGeneraleDialogState
        val showReferenceMaladieDialogState = remember(consultation) { mutableStateOf(false) }
        var showReferenceMaladieDialog by showReferenceMaladieDialogState
        val showKeywordDialogState = remember(consultation) { mutableStateOf(false) }
        var showKeywordDialog by showKeywordDialogState

        // États pour les dialogues de coefficients
        var showK1Dialog by remember { mutableStateOf(false) }
        var showK2Dialog by remember { mutableStateOf(false) }
        var showK3Dialog by remember { mutableStateOf(false) }
        var showK4Dialog by remember { mutableStateOf(false) }
        var showK5Dialog by remember { mutableStateOf(false) }

        // Filtrer les références générales par espèce
        val referencesGeneralesFiltrees =
                remember(availableReferences, animalEspece) {
                        if (animalEspece != null) {
                                availableReferences.filter {
                                        !it.maladie && it.espece == animalEspece
                                }
                        } else {
                                availableReferences.filter { !it.maladie }
                        }
                }

        // Filtrer les références complémentaires par espèce
        val referencesMaladies =
                remember(availableReferences, animalEspece) {
                        if (animalEspece != null) {
                                availableReferences.filter {
                                        it.maladie && it.espece == animalEspece
                                }
                        } else {
                                availableReferences.filter { it.maladie }
                        }
                }

        // Trouver la référence générale sélectionnée
        val referenceGeneraleSelectionnee =
                remember(editedConsultation.referenceGeneraleId, availableReferences) {
                        availableReferences.find {
                                it.uuid == editedConsultation.referenceGeneraleId
                        }
                }

        val keywordsById =
                remember(availableKeywords) { availableKeywords.associateBy { it.uuid } }

        // Charger les références et mots-clés au démarrage
        LaunchedEffect(Unit) {
                onLoadReferences()
                onLoadKeywords()
        }

        // Fonction pour sauvegarder et retourner
        val saveAndGoBack = {
                // Extraire les variables requises (inclure ENERCOMP des maladies sélectionnées)
                val selectedDiseaseRefs = referencesMaladies.filter { maladieRef ->
                        editedConsultation.referencesMaladies.contains(maladieRef.uuid)
                }
                val variablesRequises =
                        fr.vetbrain.vetnutri_mp.Utils.VariablesEnergie.variablesRequises(
                                reference = referenceGeneraleSelectionnee,
                                referencesComplementaires = selectedDiseaseRefs
                        )

                // Vérifier les variables supplémentaires manquantes
                // Consultation évolutive : une variable renseignée dans toutes les étapes du plan
                // n'est pas manquante
                val variablesManquantes =
                        variablesRequises.filter { variableKind ->
                                editedConsultation.suppVarp.none { it.variable == variableKind } &&
                                        !fr.vetbrain.vetnutri_mp.Data.PlanEvolutif
                                                .variableDansToutesLesEtapes(
                                                        editedConsultation,
                                                        variableKind
                                                )
                        }

                if (!showDateError &&
                                !showWeightError &&
                                editedConsultation.date != null &&
                                variablesManquantes.isEmpty()
                ) {
                        // S'assurer que l'UUID est généré si c'est une nouvelle consultation
                        if (editedConsultation.uuid.isEmpty()) {
                                // Générer un UUID unique avec timestamp pour éviter les conflits
                                editedConsultation =
                                        editedConsultation.copy(
                                                uuid = fr.vetbrain.vetnutri_mp.Utils.genUniqueUUID()
                                        )
                        }
                        onBackPressed(editedConsultation)
                } else {
                        if (editedConsultation.date == null) {
                                showDateError = true
                                dateErrorMessage = ConsultationEdit.DATE_REQUIRED.translate()
                        }
                        if (variablesManquantes.isNotEmpty()) {
                                missingVariables = variablesManquantes.map { it.label }
                                showMissingVariablesDialog = true
                        }
                }
        }

        // Fonction pour sauvegarder automatiquement (sans validation stricte)
        val autoSave = {
                // S'assurer que l'UUID est généré si c'est une nouvelle consultation
                val consultationToSave =
                        if (editedConsultation.uuid.isEmpty()) {
                                editedConsultation.copy(
                                        uuid = fr.vetbrain.vetnutri_mp.Utils.genUniqueUUID()
                                )
                        } else {
                                editedConsultation
                        }

                // Sauvegarder même si la date est null (sauvegarde automatique)
                onBackPressed(consultationToSave)
        }

        // Effet pour sauvegarder automatiquement quand le composant est détruit
        var shouldAutoSave by remember { mutableStateOf(true) }
        DisposableEffect(Unit) {
                onDispose {
                        // Sauvegarder automatiquement les modifications en cours sauf si l'utilisateur a validé ou annulé explicitement
                        if (shouldAutoSave) {
                                autoSave()
                        }
                }
        }

        Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                        TopAppBar(
                                title = {
                                        Column {
                                                Text(
                                                        text =
                                                                if (consultation == null ||
                                                                                consultation.uuid
                                                                                        .isEmpty()
                                                                )
                                                                        ConsultationEdit.NEW_TITLE.translate()
                                                                else ConsultationEdit.EDIT_TITLE.translate(),
                                                        style = MaterialTheme.typography.h6,
                                                        color = VetNutriColors.OnPrimary
                                                )
                                                if (animalName.isNotEmpty()) {
                                                        Text(
                                                                text = animalName,
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .subtitle2,
                                                                color =
                                                                        VetNutriColors.OnPrimary
                                                                                .copy(alpha = 0.8f)
                                                        )
                                                }
                                        }
                                },
                                navigationIcon = {
                                        IconButtonWithTooltip(
                                                onClick = {
                                                        // Demander confirmation avant d'annuler la saisie
                                                        showExitConfirmationDialog = true
                                                },
                                                imageVector = AppIcons.ArrowBack,
                                                contentDescription = LocalizationKeys.AnimalDetail.BACK.translate(),
                                                tooltip = LocalizationKeys.AnimalDetail.BACK.translate(),
                                                tint = VetNutriColors.OnPrimary
                                        )
                                },
                                actions = {},
                                backgroundColor = VetNutriColors.Primary,
                                contentColor = VetNutriColors.OnPrimary
                        )
                },
                floatingActionButton = {
                        FloatingActionButton(
                                onClick = {
                                        // Troupeau : chaque groupe nourri doit avoir un référentiel et un poids
                                        if (typesTroupeau != null) {
                                                val referencesConnues =
                                                        availableReferences.takeIf { it.isNotEmpty() }?.map { it.uuid }?.toSet()
                                                val incomplets =
                                                        fr.vetbrain.vetnutri_mp.View.groupesIncompletsTroupeau(
                                                                typesTroupeau, editedConsultation, referencesConnues
                                                        )
                                                if (incomplets.isNotEmpty()) {
                                                        missingDataMessage =
                                                                "Compléter le référentiel (existant), le poids moyen et un K positif de : ${incomplets.joinToString()}."
                                                        showMissingDataDialog = true
                                                        return@FloatingActionButton
                                                }
                                                shouldAutoSave = false
                                                saveAndGoBack()
                                                return@FloatingActionButton
                                        }
                                        // Empêcher la sauvegarde si aucune référence générale n'est
                                        // sélectionnée et si aucun poids n'est saisi
                                        val hasGeneralRef =
                                                !editedConsultation.referenceGeneraleId
                                                        .isNullOrBlank()
                                        val hasWeight = editedConsultation.weight != null
                                        if (!hasGeneralRef || !hasWeight) {
                                                missingDataMessage =
                                                        when {
                                                                !hasGeneralRef && !hasWeight ->
                                                                        ConsultationEdit.VALIDATION_ERROR.translate()
                                                                !hasGeneralRef ->
                                                                        ConsultationEdit.VALIDATION_ERROR_REF.translate()
                                                                else ->
                                                                        ConsultationEdit.VALIDATION_ERROR_WEIGHT.translate()
                                                        }
                                                showMissingDataDialog = true
                                                return@FloatingActionButton
                                        }
                                        shouldAutoSave = false
                                        saveAndGoBack()
                                },
                                backgroundColor = VetNutriColors.Primary
                        ) {
                                Icon(
                                        imageVector = AppIcons.Check,
                                        contentDescription = ConsultationEdit.CONFIRM_VALIDE.translate(),
                                        tint = VetNutriColors.OnPrimary
                                )
                        }
                }
        ) { paddingValues ->
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(paddingValues)
                                        .verticalScroll(rememberScrollState())
                                        .padding(AppSizes.paddingLarge),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                        // Dialog d'avertissement au clic sur Valider si référence manquante
                        // (désactivé dans cette version pour éviter des conflits de portée)
                        // Date
                        AppDatePicker(
                                selectedDate = editedConsultation.date,
                                onDateSelected = { date: LocalDate ->
                                        editedConsultation = editedConsultation.copy(date = date)
                                        showDateError = false
                                        dateErrorMessage = null
                                },
                                label = fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Consultation.DATE.translate(),
                                isError = showDateError,
                                errorMessage = dateErrorMessage,
                                modifier = Modifier.fillMaxWidth()
                        )

                        // Troupeau : état du troupeau par groupe d'individus, à la place du poids,
                        // de la NEC et du poids idéal d'un individu
                        if (typesTroupeau != null) {
                                EditeurEtatTroupeau(
                                        types = typesTroupeau,
                                        consultation = editedConsultation,
                                        referencesDuGroupe = { type ->
                                                val espece = type.especeEnum() ?: animalEspece
                                                val generales = availableReferences.filter { !it.maladie }
                                                generales.filter { it.espece == espece }.ifEmpty { generales }.sortedBy { it.nom }
                                        },
                                        onChange = { parametres ->
                                                editedConsultation = editedConsultation.avecParametresType(parametres)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                )
                        } else {
                        // Poids (grand clavier numérique via OutlinedTextField)
                        Column(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                        value = weightText,
                                        onValueChange = { newValue: String ->
                                                // Filtrer pour n'accepter que les chiffres, point et virgule
                                                val texteFiltre =
                                                        newValue.filter { char ->
                                                                char.isDigit() || char == '.' || char == ','
                                                        }
                                                // S'assurer qu'il n'y a qu'un seul séparateur décimal
                                                val pointCount = texteFiltre.count { it == '.' }
                                                val virguleCount = texteFiltre.count { it == ',' }
                                                if (pointCount <= 1 &&
                                                                virguleCount <= 1 &&
                                                                pointCount + virguleCount <= 1
                                                ) {
                                                        weightText = texteFiltre
                                                        val texteNormalise = texteFiltre.replace(',', '.')
                                                        try {
                                                                if (texteNormalise.isNotEmpty()) {
                                                                        val weight = texteNormalise.toDouble()
                                                                        editedConsultation =
                                                                                editedConsultation.copy(
                                                                                        weight = weight
                                                                                )
                                                                        showWeightError = false
                                                                        weightErrorMessage = null
                                                                } else {
                                                                        editedConsultation =
                                                                                editedConsultation.copy(
                                                                                        weight = null
                                                                                )
                                                                        showWeightError = false
                                                                        weightErrorMessage = null
                                                                }
                                                        } catch (e: Exception) {
                                                                e.rethrowIfCancellation()
                                                                showWeightError = true
                                                                weightErrorMessage =
                                                                        ConsultationEdit.FORMAT_WEIGHT_ERROR.translate()
                                                        }
                                                }
                                        },
                                        label = { Text(Animal.WEIGHT.translate()) },
                                        leadingIcon = {
                                                Icon(
                                                        imageVector = AppIcons.Weight,
                                                        contentDescription = null
                                                )
                                        },
                                        isError = showWeightError,
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions =
                                                KeyboardOptions(
                                                        keyboardType = KeyboardType.Text,
                                                        imeAction = ImeAction.Done
                                                ),
                                        singleLine = true
                                )
                                val currentWeightErrorMessage = weightErrorMessage
                                if (showWeightError && currentWeightErrorMessage != null) {
                                        Text(
                                                text = currentWeightErrorMessage,
                                                color = MaterialTheme.colors.error,
                                                style = MaterialTheme.typography.caption,
                                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                        )
                                }
                        }

                        // Section Note d'État Corporel
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = AppSizes.elevationSmall,
                                backgroundColor = VetNutriColors.Surface
                        ) {
                                Column(
                                        modifier = Modifier.padding(AppSizes.paddingLarge),
                                        verticalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingMedium)
                                ) {
                                        Text(
                                                text = ConsultationEdit.BCS_SECTION_TITLE.translate(),
                                                style = MaterialTheme.typography.h6,
                                                color = VetNutriColors.Primary
                                        )

                                        Divider(color = VetNutriColors.Primary.copy(alpha = 0.3f))

                                        // Sélecteur de BCS
                                        ScoreSelector(
                                                label = ConsultationEdit.BCS_SELECTOR_LABEL.translate(),
                                                valeurSelectionnee =
                                                        editedConsultation.BCS?.toDouble(),
                                                onScoreSelected = { score ->
                                                        editedConsultation =
                                                                editedConsultation.copy(
                                                                        BCS = score?.toInt()
                                                                )
                                                },
                                                plageScore = 1..9,
                                                descriptions =
                                                        mapOf(
                                                                1 to
                                                                        ConsultationEdit.BCS_DESC_1.translate(),
                                                                2 to
                                                                        ConsultationEdit.BCS_DESC_2.translate(),
                                                                3 to
                                                                        ConsultationEdit.BCS_DESC_3.translate(),
                                                                4 to
                                                                        ConsultationEdit.BCS_DESC_4.translate(),
                                                                5 to
                                                                        ConsultationEdit.BCS_DESC_5.translate(),
                                                                6 to
                                                                        ConsultationEdit.BCS_DESC_6.translate(),
                                                                7 to
                                                                        ConsultationEdit.BCS_DESC_7.translate(),
                                                                8 to
                                                                        ConsultationEdit.BCS_DESC_8.translate(),
                                                                9 to
                                                                        ConsultationEdit.BCS_DESC_9.translate()
                                                        )
                                        )
                                }
                        }

                        // Poids idéal
                        val idealWeightTextState =
                                remember(consultation) {
                                        mutableStateOf(consultation?.idealWeight?.toString() ?: "")
                                }
                        var idealWeightText by idealWeightTextState
                        val showIdealWeightErrorState = remember(consultation) { mutableStateOf(false) }
                        var showIdealWeightError by showIdealWeightErrorState
                        val idealWeightErrorMessageState =
                                remember(consultation) { mutableStateOf<String?>(null) }
                        var idealWeightErrorMessage by idealWeightErrorMessageState

                        // Calcul de l'estimation du poids idéal
                        val estimatedIdealWeight =
                                remember(editedConsultation.weight, editedConsultation.BCS) {
                                        if (editedConsultation.weight != null &&
                                                        editedConsultation.BCS != null
                                        ) {
                                                fr.vetbrain.vetnutri_mp.Data.CalculMetabolique.estimerPoidsIdeal(
                                                        editedConsultation.weight!!,
                                                        editedConsultation.BCS!!
                                                )
                                        } else {
                                                null
                                        }
                                }

                        LignePoidsIdealConsultation(
                                editedConsultationState = editedConsultationState,
                                estimatedIdealWeight = estimatedIdealWeight,
                                idealWeightErrorMessageState = idealWeightErrorMessageState,
                                idealWeightTextState = idealWeightTextState,
                                showIdealWeightErrorState = showIdealWeightErrorState
                        )

                        }

                        // Objectif
                        AppTextField(
                                value = editedConsultation.objectConsult,
                                onValueChange = { newValue: String ->
                                        editedConsultation =
                                                editedConsultation.copy(objectConsult = newValue)
                                },
                                label = fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Consultation.OBJECTIVE.translate(),
                                leadingIcon = AppIcons.Info,
                                modifier = Modifier.fillMaxWidth()
                        )

                        // Section Mots-clés
                        CarteMotsClesConsultation(
                                editedConsultationState = editedConsultationState,
                                keywordsById = keywordsById,
                                showKeywordDialogState = showKeywordDialogState
                        )

                        // Observations
                        AppTextField(
                                value = editedConsultation.observation,
                                onValueChange = { newValue: String ->
                                        editedConsultation =
                                                editedConsultation.copy(observation = newValue)
                                },
                                label = fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Consultation.OBSERVATION.translate(),
                                leadingIcon = AppIcons.Info,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                                maxLines = 6,
                                singleLine = false
                        )

                        // Référentiel, variables et coefficients : ceux de l'individu (pour un
                        // troupeau, définis par groupe dans l'état du troupeau)
                        if (typesTroupeau == null) {
                        // Section Références Nutritionnelles
                        CarteReferencesConsultation(
                                availableReferences = availableReferences,
                                editedConsultationState = editedConsultationState,
                                referenceGeneraleSelectionnee = referenceGeneraleSelectionnee,
                                referencesMaladies = referencesMaladies,
                                showReferenceGeneraleDialogState = showReferenceGeneraleDialogState,
                                showReferenceMaladieDialogState = showReferenceMaladieDialogState
                        )

                        // Section Variables Supplémentaires
        val variablesRequises =
                remember(referenceGeneraleSelectionnee, editedConsultation.referencesMaladies, referencesMaladies) {
                        val selectedDiseaseRefs = referencesMaladies.filter { maladieRef ->
                                editedConsultation.referencesMaladies.contains(maladieRef.uuid)
                        }
                        fr.vetbrain.vetnutri_mp.Utils.VariablesEnergie.variablesRequises(
                                reference = referenceGeneraleSelectionnee,
                                referencesComplementaires = selectedDiseaseRefs
                        )
                }

                        // Debug des équations disponibles
                        LaunchedEffect(referenceGeneraleSelectionnee) {
                                referenceGeneraleSelectionnee?.let { ref ->
                                        ref.equationsNut.forEachIndexed { index, eq -> }
                                }
                        }

                        // Section Variables Supplémentaires
                        VariablesSupplementairesConsultation(
                                editedConsultationState = editedConsultationState,
                                variablesRequises = variablesRequises
                        )

                        // Section Coefficients
                        CarteCoefficientsConsultation(
                                availableReferences = availableReferences,
                                editedConsultationState = editedConsultationState,
                                referenceGeneraleSelectionnee = referenceGeneraleSelectionnee
                        )

                        }

                        // Espacement final
                        Spacer(modifier = Modifier.height(AppSizes.paddingLarge))
                }
        }

        if (showReferenceGeneraleDialog) {
                ReferenceGeneraleDialog(
                        value = editedConsultation.referenceGeneraleId ?: "",
                        onValueChange = { newValue: String ->
                                editedConsultation =
                                        editedConsultation.copy(referenceGeneraleId = newValue)
                                showReferenceGeneraleDialog = false
                        },
                        availableReferences = referencesGeneralesFiltrees
                )
        }

        if (showReferenceMaladieDialog) {
                ReferenceMaladieDialog(
                        references = editedConsultation.referencesMaladies,
                        availableReferences = referencesMaladies,
                        onReferenceSelected = { referenceId ->
                                editedConsultation =
                                        editedConsultation.copy(
                                                referencesMaladies = referenceId.toMutableList()
                                        )
                        },
                        onReferenceRemoved = { referenceId ->
                                editedConsultation =
                                        editedConsultation.copy(
                                                referencesMaladies = referenceId.toMutableList()
                                        )
                        },
                        onDismiss = { showReferenceMaladieDialog = false }
                )
        }

        if (showKeywordDialog) {
                ConsultationKeywordDialog(
                        selectedKeywordIds = editedConsultation.keywordIds,
                        availableKeywords = availableKeywords,
                        onKeywordsSelected = { keywordIds ->
                                editedConsultation =
                                        editedConsultation.copy(
                                                keywordIds = keywordIds.toMutableList()
                                        )
                        },
                        onKeywordCreated = { keyword ->
                                onCreateKeyword(keyword)
                                editedConsultation =
                                        editedConsultation.copy(
                                                keywordIds =
                                                        (editedConsultation.keywordIds +
                                                                        keyword.uuid)
                                                                .distinct()
                                                                .toMutableList()
                                        )
                        },
                        onDismiss = { showKeywordDialog = false }
                )
        }

        // Dialogue pour les variables supplémentaires manquantes
        if (showMissingVariablesDialog) {
                AlertDialog(
                        onDismissRequest = { showMissingVariablesDialog = false },
                        title = { Text(ConsultationEdit.MISSING_VARS_TITLE.translate()) },
                        text = {
                                Column {
                                        Text(
                                                ConsultationEdit.MISSING_VARS_DESC.translate()
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        missingVariables.forEach { variableName ->
                                                Text(
                                                        text = "• $variableName",
                                                        style = MaterialTheme.typography.body2,
                                                        color = MaterialTheme.colors.primary
                                                )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                                ConsultationEdit.MISSING_VARS_FOOTER.translate(),
                                                style = MaterialTheme.typography.body2,
                                                color = Color.Gray
                                        )
                                }
                        },
                        confirmButton = {
                                TextButton(onClick = { showMissingVariablesDialog = false }) {
                                        Text(LocalizationKeys.General.CONFIRM.translate())
                                }
                        }
                )
        }

        // Dialogue de confirmation pour l'annulation de la saisie
        if (showExitConfirmationDialog) {
                AlertDialog(
                        onDismissRequest = { showExitConfirmationDialog = false },
                        title = { Text(ConsultationEdit.CANCEL_DIALOG_TITLE.translate()) },
                        text = {
                                Text(
                                        ConsultationEdit.CANCEL_DIALOG_DESC.translate()
                                )
                        },
                        confirmButton = {
                                TextButton(
                                        onClick = {
                                                shouldAutoSave = false
                                                showExitConfirmationDialog = false
                                                onCancel()
                                        }
                                ) {
                                        Text(ConsultationEdit.CANCEL_DIALOG_CONFIRM.translate())
                                }
                        },
                        dismissButton = {
                                TextButton(onClick = { showExitConfirmationDialog = false }) {
                                        Text(ConsultationEdit.CANCEL_DIALOG_CONTINUE.translate())
                                }
                        }
                )
        }

        // Dialogue d'avertissement pour référence ou poids manquants à la validation
        if (showMissingDataDialog) {
            AlertDialog(
                    onDismissRequest = { showMissingDataDialog = false },
                    title = { Text(ConsultationEdit.MISSING_DATA_TITLE.translate()) },
                    text = { Text(missingDataMessage) },
                    confirmButton = {
                            TextButton(onClick = { showMissingDataDialog = false }) {
                                    Text(LocalizationKeys.General.CONFIRM.translate())
                            }
                    }
            )
        }
}
