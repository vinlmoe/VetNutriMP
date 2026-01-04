package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
        onCreateKeyword: (ConsultationKeyword) -> Unit = {}
) {
        var editedConsultation by
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
        var showReferenceGeneraleDialog by remember(consultation) { mutableStateOf(false) }
        var showReferenceMaladieDialog by remember(consultation) { mutableStateOf(false) }
        var showKeywordDialog by remember(consultation) { mutableStateOf(false) }

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
                        extraireVariablesRequises(
                                reference = referenceGeneraleSelectionnee,
                                referencesMaladies = selectedDiseaseRefs
                        )

                // Vérifier les variables supplémentaires manquantes
                val variablesManquantes =
                        variablesRequises.filter { variableKind ->
                                editedConsultation.suppVarp.none { it.variable == variableKind }
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
                                                contentDescription = ConsultationEdit.REF_SEARCH_TOOLTIP.translate(), // Using search tooltip for back for now if no generic back
                                                tooltip = ConsultationEdit.REF_SEARCH_TOOLTIP.translate(),
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

                        // Poids idéal
                        var idealWeightText by
                                remember(consultation) {
                                        mutableStateOf(consultation?.idealWeight?.toString() ?: "")
                                }
                        var showIdealWeightError by remember(consultation) { mutableStateOf(false) }
                        var idealWeightErrorMessage by
                                remember(consultation) { mutableStateOf<String?>(null) }

                        // Calcul de l'estimation du poids idéal
                        val estimatedIdealWeight =
                                remember(editedConsultation.weight, editedConsultation.BCS) {
                                        if (editedConsultation.weight != null &&
                                                        editedConsultation.BCS != null
                                        ) {
                                                val poids = editedConsultation.weight!!
                                                val nec = editedConsultation.BCS!!
                                                val estimation =
                                                        poids * 100 / (100 + (nec - 5) * 10)
                                                estimation
                                        } else {
                                                null
                                        }
                                }

                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                // Champ de saisie du poids idéal (grand clavier numérique)
                                Column(modifier = Modifier.weight(1f)) {
                                        OutlinedTextField(
                                                value = idealWeightText,
                                                onValueChange = { newValue: String ->
                                                        val texteFiltre =
                                                                newValue.filter { char ->
                                                                        char.isDigit() || char == '.' || char == ','
                                                                }
                                                        val pointCount = texteFiltre.count { it == '.' }
                                                        val virguleCount = texteFiltre.count { it == ',' }
                                                        if (pointCount <= 1 &&
                                                                        virguleCount <= 1 &&
                                                                        pointCount + virguleCount <= 1
                                                        ) {
                                                                idealWeightText = texteFiltre
                                                                val texteNormalise = texteFiltre.replace(',', '.')
                                                                try {
                                                                        if (texteNormalise.isNotEmpty()) {
                                                                                val idealWeight =
                                                                                        texteNormalise.toDouble()
                                                                                editedConsultation =
                                                                                        editedConsultation.copy(
                                                                                                idealWeight =
                                                                                                        idealWeight
                                                                                        )
                                                                                showIdealWeightError = false
                                                                                idealWeightErrorMessage = null
                                                                        } else {
                                                                                editedConsultation =
                                                                                        editedConsultation.copy(
                                                                                                idealWeight = null
                                                                                        )
                                                                                showIdealWeightError = false
                                                                                idealWeightErrorMessage = null
                                                                        }
                                                                } catch (e: Exception) {
                                                                        showIdealWeightError = true
                                                                        idealWeightErrorMessage =
                                                                                ConsultationEdit.FORMAT_IDEAL_WEIGHT_ERROR.translate()
                                                                }
                                                        }
                                                },
                                                label = { Text(ConsultationEdit.IDEAL_WEIGHT_LABEL.translate()) },
                                                leadingIcon = {
                                                        Icon(
                                                                imageVector = AppIcons.Weight,
                                                                contentDescription = null
                                                        )
                                                },
                                                isError = showIdealWeightError,
                                                modifier = Modifier.fillMaxWidth(),
                                                keyboardOptions =
                                                        KeyboardOptions(
                                                                keyboardType = KeyboardType.Text,
                                                                imeAction = ImeAction.Done
                                                        ),
                                                singleLine = true
                                        )
                                        val currentIdealWeightErrorMessage = idealWeightErrorMessage
                                        if (showIdealWeightError && currentIdealWeightErrorMessage != null) {
                                                Text(
                                                        text = currentIdealWeightErrorMessage,
                                                        color = MaterialTheme.colors.error,
                                                        style = MaterialTheme.typography.caption,
                                                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                                )
                                        }
                                }

                                // Affichage de l'estimation
                                if (estimatedIdealWeight != null) {
                                        Card(
                                                modifier = Modifier.weight(1f).padding(top = 8.dp),
                                                backgroundColor =
                                                        VetNutriColors.Surface.copy(alpha = 0.7f),
                                                elevation = 1.dp
                                        ) {
                                                Column(
                                                        modifier =
                                                                Modifier.padding(
                                                                        AppSizes.paddingMedium
                                                                ),
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        Text(
                                                                text = ConsultationEdit.ESTIMATION.translate(),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption,
                                                                color = Color.Gray
                                                        )
                                                        Text(
                                                                text =
                                                                        "${fr.vetbrain.vetnutri_mp.Utils.TextUtils.formatDecimal(estimatedIdealWeight, 2)} kg",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body1,
                                                                fontWeight = FontWeight.Bold,
                                                                color = VetNutriColors.Primary
                                                        )
                                                }
                                        }
                                } else {
                                        Card(
                                                modifier = Modifier.weight(1f).padding(top = 8.dp),
                                                backgroundColor =
                                                        VetNutriColors.Surface.copy(alpha = 0.3f),
                                                elevation = 1.dp
                                        ) {
                                                Column(
                                                        modifier =
                                                                Modifier.padding(
                                                                        AppSizes.paddingMedium
                                                                ),
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        Text(
                                                                text = ConsultationEdit.ESTIMATION.translate(),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption,
                                                                color = Color.Gray
                                                        )
                                                        Text(
                                                                text = ConsultationEdit.ESTIMATION_REQUIRES.translate(),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption,
                                                                color = Color.Gray
                                                        )
                                                }
                                        }
                                }
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
                                                                        "Très maigre - Côtes, vertèbres et os du bassin très saillants",
                                                                2 to
                                                                        "Maigre - Côtes facilement palpables, faible couverture graisseuse",
                                                                3 to
                                                                        "Mince - Côtes palpables avec légère couverture graisseuse",
                                                                4 to
                                                                        "Idéal inférieur - Côtes facilement palpables, taille visible",
                                                                5 to
                                                                        "Idéal - Côtes palpables sans excès de graisse, taille bien définie",
                                                                6 to
                                                                        "Idéal supérieur - Côtes palpables avec légère couverture graisseuse",
                                                                7 to
                                                                        "Surpoids - Côtes difficiles à palper, dépôts graisseux visibles",
                                                                8 to
                                                                        "Obèse - Côtes très difficiles à palper, importante couverture graisseuse",
                                                                9 to
                                                                        "Très obèse - Côtes non palpables, dépôts graisseux massifs"
                                                        )
                                        )
                                }
                        }

                        // Section Références Nutritionnelles
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
                                                text = ConsultationEdit.REF_SECTION_TITLE.translate(),
                                                style = MaterialTheme.typography.h6,
                                                color = VetNutriColors.Primary
                                        )

                                        Divider(color = VetNutriColors.Primary.copy(alpha = 0.3f))

                                        // Référence générale
                                        Text(
                                                text = ConsultationEdit.REF_GENERAL_SUBTITLE.translate(),
                                                style = MaterialTheme.typography.subtitle1,
                                                fontWeight = FontWeight.Bold
                                        )

                                        OutlinedTextField(
                                                value = referenceGeneraleSelectionnee?.nom
                                                                ?: ConsultationEdit.REF_GENERAL_NONE.translate(),
                                                onValueChange = {},
                                                label = { Text(ConsultationEdit.REF_GENERAL_SUBTITLE.translate()) },
                                                readOnly = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                trailingIcon = {
                                                        IconButtonWithTooltip(
                                                                onClick = {
                                                                        showReferenceGeneraleDialog =
                                                                                true
                                                                },
                                                                imageVector = AppIcons.ArrowDropDown,
                                                                contentDescription = ConsultationEdit.REF_SELECT_TOOLTIP.translate(),
                                                                tooltip = ConsultationEdit.REF_SELECT_TOOLTIP.translate()
                                                        )
                                                }
                                        )

                                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

                                        // Références liées aux maladies
                                        Text(
                                                text = ConsultationEdit.REF_DISEASE_SUBTITLE.translate(),
                                                style = MaterialTheme.typography.subtitle1,
                                                fontWeight = FontWeight.Bold
                                        )

                                        // Liste des références de maladies
                                        if (editedConsultation.referencesMaladies.isNotEmpty()) {
                                                Column(
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(
                                                                        AppSizes.paddingSmall
                                                                )
                                                ) {
                                                        editedConsultation.referencesMaladies
                                                                .forEach { referenceId ->
                                                                        val referenceMaladie =
                                                                                availableReferences
                                                                                        .find {
                                                                                                it.uuid ==
                                                                                                        referenceId
                                                                                        }
                                                                        Card(
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth(),
                                                                                backgroundColor =
                                                                                        VetNutriColors
                                                                                                .Surface
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.7f
                                                                                                ),
                                                                                elevation = 2.dp
                                                                        ) {
                                                                                Row(
                                                                                        modifier =
                                                                                                Modifier.padding(
                                                                                                        AppSizes.paddingMedium
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
                                                                                                                referenceMaladie
                                                                                                                        ?.nom
                                                                                                                        ?: ConsultationEdit.REF_UNKNOWN.translate(),
                                                                                                        style =
                                                                                                                MaterialTheme
                                                                                                                        .typography
                                                                                                                        .body1,
                                                                                                        fontWeight =
                                                                                                                FontWeight
                                                                                                                        .Bold
                                                                                                )
                                                                                                if (referenceMaladie
                                                                                                                ?.description
                                                                                                                ?.isNotBlank() ==
                                                                                                                true
                                                                                                ) {
                                                                                                        Text(
                                                                                                                text =
                                                                                                                        referenceMaladie
                                                                                                                                .description,
                                                                                                                style =
                                                                                                                        MaterialTheme
                                                                                                                                .typography
                                                                                                                                .body2,
                                                                                                                color =
                                                                                                                        Color.Gray
                                                                                                        )
                                                                                                }
                                                                                        }
                                                                                        IconButtonWithTooltip(
                                                                                                onClick = {
                                                                                                        val nouvellesReferences =
                                                                                                                editedConsultation
                                                                                                                        .referencesMaladies
                                                                                                                        .toMutableList()
                                                                                                        nouvellesReferences
                                                                                                                .remove(
                                                                                                                        referenceId
                                                                                                                )
                                                                                                        editedConsultation =
                                                                                                                editedConsultation
                                                                                                                        .copy(
                                                                                                                                referencesMaladies =
                                                                                                                                        nouvellesReferences
                                                                                                                        )
                                                                                                },
                                                                                                imageVector = AppIcons.Delete,
                                                                                                contentDescription = ConsultationEdit.REF_DELETE_TOOLTIP.translate(),
                                                                                                tooltip = ConsultationEdit.REF_DELETE_TOOLTIP.translate(),
                                                                                                tint = Color.Red
                                                                                        )
                                                                                }
                                                                        }
                                                                }
                                                }
                                        }

                                        // Bouton pour ajouter une référence de maladie
                                        OutlinedButton(
                                                onClick = { showReferenceMaladieDialog = true },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors =
                                                        ButtonDefaults.outlinedButtonColors(
                                                                contentColor =
                                                                        VetNutriColors.Primary
                                                        )
                                        ) {
                                                Icon(
                                                        AppIcons.Add,
                                                        contentDescription =
                                                                ConsultationEdit.REF_ADD_DISEASE.translate(),
                                                        modifier =
                                                                Modifier.size(
                                                                        AppSizes.iconSizeSmall
                                                                )
                                                )
                                                Spacer(
                                                        modifier =
                                                                Modifier.width(
                                                                        AppSizes.paddingSmall
                                                                )
                                                )
                                                Text(ConsultationEdit.REF_ADD_DISEASE.translate())
                                        }
                                }
                        }

                        // Section Mots-clés
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
                                                text = ConsultationEdit.KEYWORDS_SECTION_TITLE.translate(),
                                                style = MaterialTheme.typography.h6,
                                                color = VetNutriColors.Primary
                                        )

                                        Divider(color = VetNutriColors.Primary.copy(alpha = 0.3f))

                                        if (editedConsultation.keywordIds.isNotEmpty()) {
                                                Column(
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(
                                                                        AppSizes.paddingSmall
                                                                )
                                                ) {
                                                        editedConsultation.keywordIds.forEach {
                                                                keywordId ->
                                                                val keywordLabel =
                                                                        keywordsById[keywordId]
                                                                                ?.label
                                                                                ?: ConsultationEdit.KEYWORDS_UNKNOWN.translate()

                                                                Card(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth(),
                                                                        backgroundColor =
                                                                                VetNutriColors
                                                                                        .Surface
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.7f
                                                                                        ),
                                                                        elevation = 2.dp
                                                                ) {
                                                                        Row(
                                                                                modifier =
                                                                                        Modifier.padding(
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
                                                                                        text = keywordLabel,
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .body1,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Bold,
                                                                                        modifier =
                                                                                                Modifier.weight(
                                                                                                        1f
                                                                                                )
                                                                                )
                                                                                IconButtonWithTooltip(
                                                                                        onClick = {
                                                                                                val updatedKeywords =
                                                                                                        editedConsultation
                                                                                                                .keywordIds
                                                                                                                .toMutableList()
                                                                                                updatedKeywords.remove(
                                                                                                        keywordId
                                                                                                )
                                                                                                editedConsultation =
                                                                                                        editedConsultation
                                                                                                                .copy(
                                                                                                                        keywordIds =
                                                                                                                                updatedKeywords
                                                                                                                )
                                                                                        },
                                                                                        imageVector = AppIcons.Delete,
                                                                                        contentDescription = ConsultationEdit.KEYWORDS_DELETE_TOOLTIP.translate(),
                                                                                        tooltip = ConsultationEdit.KEYWORDS_DELETE_TOOLTIP.translate(),
                                                                                        tint = Color.Red
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                }
                                        } else {
                                                Text(
                                                        text = ConsultationEdit.KEYWORDS_NONE.translate(),
                                                        style = MaterialTheme.typography.body2,
                                                        color = Color.Gray
                                                )
                                        }

                                        OutlinedButton(
                                                onClick = { showKeywordDialog = true },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors =
                                                        ButtonDefaults.outlinedButtonColors(
                                                                contentColor =
                                                                        VetNutriColors.Primary
                                                        )
                                        ) {
                                                Icon(
                                                        AppIcons.Add,
                                                        contentDescription =
                                                                ConsultationEdit.KEYWORDS_ADD.translate(),
                                                        modifier =
                                                                Modifier.size(
                                                                        AppSizes.iconSizeSmall
                                                                )
                                                )
                                                Spacer(
                                                        modifier =
                                                                Modifier.width(
                                                                        AppSizes.paddingSmall
                                                                )
                                                )
                                                Text(ConsultationEdit.KEYWORDS_ADD.translate())
                                        }
                                }
                        }

                        // Section Variables Supplémentaires
        val variablesRequises =
                remember(referenceGeneraleSelectionnee, editedConsultation.referencesMaladies, referencesMaladies) {
                        val selectedDiseaseRefs = referencesMaladies.filter { maladieRef ->
                                editedConsultation.referencesMaladies.contains(maladieRef.uuid)
                        }
                        extraireVariablesRequises(
                                reference = referenceGeneraleSelectionnee,
                                referencesMaladies = selectedDiseaseRefs
                        )
                }

                        // Debug des équations disponibles
                        LaunchedEffect(referenceGeneraleSelectionnee) {
                                referenceGeneraleSelectionnee?.let { ref ->
                                        ref.equationsNut.forEachIndexed { index, eq -> }
                                }
                        }

                        // Section Variables Supplémentaires
                        if (variablesRequises.isNotEmpty()) {
                                Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        backgroundColor =
                                                VetNutriColors.Primary.copy(alpha = 0.05f),
                                        elevation = 2.dp
                                ) {
                                        Column(
                                                modifier = Modifier.padding(AppSizes.paddingMedium)
                                        ) {
                                                Text(
                                                        text = ConsultationEdit.SUPP_VAR_TITLE.translate(),
                                                        style = MaterialTheme.typography.h6,
                                                        color = VetNutriColors.Primary,
                                                        fontWeight = FontWeight.Bold
                                                )

                                                Text(
                                                        text =
                                                                ConsultationEdit.SUPP_VAR_DESC.translate(),
                                                        style = MaterialTheme.typography.body2,
                                                        color = Color.Gray,
                                                        modifier =
                                                                Modifier.padding(
                                                                        vertical =
                                                                                AppSizes.paddingSmall
                                                                )
                                                )

                                                variablesRequises.forEach { variableKind ->
                                                        VariableSupplementaireField(
                                                                variable = variableKind,
                                                                valeurActuelle =
                                                                        editedConsultation.suppVarp
                                                                                .find {
                                                                                        it.variable ==
                                                                                                variableKind
                                                                                }
                                                                                ?.varue,
                                                                onValeurChange = { nouvelleValeur ->
                                                                        val variablesModifiees =
                                                                                editedConsultation
                                                                                        .suppVarp
                                                                                        .toMutableList()

                                                                        // Supprimer
                                                                        // l'ancienne valeur
                                                                        variablesModifiees
                                                                                .removeAll {
                                                                                        it.variable ==
                                                                                                variableKind
                                                                                }

                                                                        // Ajouter
                                                                        // la
                                                                        // nouvelle
                                                                        // valeur si
                                                                        // elle
                                                                        // n'est pas
                                                                        // nulle
                                                                        if (nouvelleValeur != null
                                                                        ) {
                                                                                variablesModifiees
                                                                                        .add(
                                                                                                SupplementalvariableP(
                                                                                                        variableKind,
                                                                                                        nouvelleValeur
                                                                                                )
                                                                                        )
                                                                        }

                                                editedConsultation =
                                                        editedConsultation
                                                                .copy(
                                                                        suppVarp =
                                                                                variablesModifiees
                                                                )
                                        },
                                                                modifier =
                                                                        Modifier.padding(
                                                                                vertical =
                                                                                        AppSizes.paddingXSmall
                                                                        )
                                                        )
                                                }
                                        }
                                }
                        }

                        // Section Coefficients
                        if (availableReferences.isNotEmpty()) {
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
                                                        text =
                                                                ConsultationEdit.ADJUST_COEF_TITLE.translate(),
                                                        style = MaterialTheme.typography.h6,
                                                        color = VetNutriColors.Primary
                                                )

                                                Divider(
                                                        color =
                                                                VetNutriColors.Primary.copy(
                                                                        alpha = 0.3f
                                                                )
                                                )

                                                // Grille des coefficients K1-K5
                                                Column(
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(
                                                                        AppSizes.paddingSmall
                                                                )
                                                ) {
                                                        // Coefficient K1
                                                        CoefficientSelector(
                                                                nom =
                                                                        referenceGeneraleSelectionnee
                                                                                ?.nomk1?.takeIf {
                                                                                it.isNotBlank()
                                                                        }
                                                                                ?: ConsultationEdit.COEF_K1_FALLBACK.translate(),
                                                                valeurSelectionnee =
                                                                        editedConsultation.k1Value,
                                                                descriptionSelectionnee =
                                                                        editedConsultation.k1Id,
                                                                coefficients =
                                                                        referenceGeneraleSelectionnee
                                                                                ?.modk1
                                                                                ?: arrayListOf(),
                                                                onCoefficientSelected = { coef ->
                                                                        editedConsultation =
                                                                                editedConsultation
                                                                                        .copy(
                                                                                                k1Id =
                                                                                                        coef.description,
                                                                                                k1Value =
                                                                                                        coef.coef
                                                                                        )
                                                                }
                                                        )

                                                        // Coefficient K2
                                                        CoefficientSelector(
                                                                nom =
                                                                        referenceGeneraleSelectionnee
                                                                                ?.nomk2?.takeIf {
                                                                                it.isNotBlank()
                                                                        }
                                                                                ?: ConsultationEdit.COEF_K2_FALLBACK.translate(),
                                                                valeurSelectionnee =
                                                                        editedConsultation.k2Value,
                                                                descriptionSelectionnee =
                                                                        editedConsultation.k2Id,
                                                                coefficients =
                                                                        referenceGeneraleSelectionnee
                                                                                ?.modk2
                                                                                ?: arrayListOf(),
                                                                onCoefficientSelected = { coef ->
                                                                        editedConsultation =
                                                                                editedConsultation
                                                                                        .copy(
                                                                                                k2Id =
                                                                                                        coef.description,
                                                                                                k2Value =
                                                                                                        coef.coef
                                                                                        )
                                                                }
                                                        )

                                                        // Coefficient K3
                                                        CoefficientSelector(
                                                                nom =
                                                                        referenceGeneraleSelectionnee
                                                                                ?.nomk3?.takeIf {
                                                                                it.isNotBlank()
                                                                        }
                                                                                ?: ConsultationEdit.COEF_K3_FALLBACK.translate(),
                                                                valeurSelectionnee =
                                                                        editedConsultation.k3Value,
                                                                descriptionSelectionnee =
                                                                        editedConsultation.k3Id,
                                                                coefficients =
                                                                        referenceGeneraleSelectionnee
                                                                                ?.modk3
                                                                                ?: arrayListOf(),
                                                                onCoefficientSelected = { coef ->
                                                                        editedConsultation =
                                                                                editedConsultation
                                                                                        .copy(
                                                                                                k3Id =
                                                                                                        coef.description,
                                                                                                k3Value =
                                                                                                        coef.coef
                                                                                        )
                                                                }
                                                        )

                                                        // Coefficient K4
                                                        CoefficientSelector(
                                                                nom =
                                                                        referenceGeneraleSelectionnee
                                                                                ?.nomk4?.takeIf {
                                                                                it.isNotBlank()
                                                                        }
                                                                                ?: ConsultationEdit.COEF_K4_FALLBACK.translate(),
                                                                valeurSelectionnee =
                                                                        editedConsultation.k4Value,
                                                                descriptionSelectionnee =
                                                                        editedConsultation.k4Id,
                                                                coefficients =
                                                                        referenceGeneraleSelectionnee
                                                                                ?.modk4
                                                                                ?: arrayListOf(),
                                                                onCoefficientSelected = { coef ->
                                                                        editedConsultation =
                                                                                editedConsultation
                                                                                        .copy(
                                                                                                k4Id =
                                                                                                        coef.description,
                                                                                                k4Value =
                                                                                                        coef.coef
                                                                                        )
                                                                }
                                                        )

                                                        // Coefficient K5
                                                        CoefficientSelector(
                                                                nom =
                                                                        referenceGeneraleSelectionnee
                                                                                ?.nomk5?.takeIf {
                                                                                it.isNotBlank()
                                                                        }
                                                                                ?: ConsultationEdit.COEF_K5_FALLBACK.translate(),
                                                                valeurSelectionnee =
                                                                        editedConsultation.k5Value,
                                                                descriptionSelectionnee =
                                                                        editedConsultation.k5Id,
                                                                coefficients =
                                                                        referenceGeneraleSelectionnee
                                                                                ?.modk5
                                                                                ?: arrayListOf(),
                                                                onCoefficientSelected = { coef ->
                                                                        editedConsultation =
                                                                                editedConsultation
                                                                                        .copy(
                                                                                                k5Id =
                                                                                                        coef.description,
                                                                                                k5Value =
                                                                                                        coef.coef
                                                                                        )
                                                                }
                                                        )
                                                }
                                        }
                                }
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
                                showReferenceMaladieDialog = false
                        },
                        onReferenceRemoved = { referenceId ->
                                editedConsultation =
                                        editedConsultation.copy(
                                                referencesMaladies = referenceId.toMutableList()
                                        )
                                showReferenceMaladieDialog = false
                        }
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
                                showKeywordDialog = false
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
                                showKeywordDialog = false
                        }
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

@Composable
private fun ScoreSelector(
        label: String,
        valeurSelectionnee: Double?,
        onScoreSelected: (Double?) -> Unit,
        plageScore: IntRange,
        descriptions: Map<Int, String>,
        modifier: Modifier = Modifier
) {
        var showDialog by remember { mutableStateOf(false) }

        Column(modifier = modifier) {
                // Label du score
                Text(
                        text = label,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold,
                        color = VetNutriColors.Primary
                )

                // Champ de sélection
                OutlinedTextField(
                        value =
                                if (valeurSelectionnee != null) {
                                        "$valeurSelectionnee/9 - ${descriptions[valeurSelectionnee.toInt()] ?: ConsultationEdit.REF_DESCRIPTION_NONE.translate()}"
                                } else {
                                        ConsultationEdit.BCS_NONE_SELECTED.translate()
                                },
                        onValueChange = {},
                        label = { Text(ConsultationEdit.BCS_FIELD_LABEL.translate()) },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                                Row {
                                        if (valeurSelectionnee != null) {
                                                IconButtonWithTooltip(
                                                        onClick = { onScoreSelected(null) },
                                                        imageVector = AppIcons.Close,
                                                        contentDescription = ConsultationEdit.BCS_CLEAR_TOOLTIP.translate(),
                                                        tooltip = ConsultationEdit.BCS_CLEAR_TOOLTIP.translate(),
                                                        tint = Color.Gray
                                                )
                                        }
                                        IconButtonWithTooltip(
                                                onClick = { showDialog = true },
                                                imageVector = AppIcons.ArrowDropDown,
                                                contentDescription = ConsultationEdit.BCS_SELECT_TOOLTIP.translate(),
                                                tooltip = ConsultationEdit.BCS_SELECT_TOOLTIP.translate()
                                        )
                                }
                        }
                )
        }

        // Dialog de sélection
        if (showDialog) {
                ScoreSelectionDialog(
                        label = label,
                        plageScore = plageScore,
                        descriptions = descriptions,
                        scoreSelectionne = valeurSelectionnee,
                        onScoreSelected = { score ->
                                onScoreSelected(score)
                                showDialog = false
                        },
                        onDismiss = { showDialog = false }
                )
        }
}

@Composable
private fun ScoreSelectionDialog(
        label: String,
        plageScore: IntRange,
        descriptions: Map<Int, String>,
        scoreSelectionne: Double?,
        onScoreSelected: (Double) -> Unit,
        onDismiss: () -> Unit
) {
        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(ConsultationEdit.BCS_DIALOG_TITLE.translate(label)) },
                text = {
                        LazyColumn {
                                items(plageScore.toList()) { score ->
                                        Row(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .padding(vertical = 4.dp)
                                                                .clickable {
                                                                        onScoreSelected(
                                                                                score.toDouble()
                                                                        )
                                                                },
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                RadioButton(
                                                        selected =
                                                                scoreSelectionne ==
                                                                        score.toDouble(),
                                                        onClick = {
                                                                onScoreSelected(score.toDouble())
                                                        }
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                        Text(
                                                                text = "$score/9",
                                                                style = MaterialTheme.typography.h6,
                                                                fontWeight = FontWeight.Bold,
                                                                color = VetNutriColors.Primary
                                                        )
                                                        Text(
                                                                text = descriptions[score]
                                                                                ?: ConsultationEdit.REF_DESCRIPTION_NONE.translate(),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body2,
                                                                color = Color.Gray
                                                        )
                                                }
                                        }
                                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                                }
                        }
                },
                confirmButton = { TextButton(onClick = onDismiss) { Text(LocalizationKeys.General.CLOSE.translate()) } }
        )
}

@Composable
private fun CoefficientSelector(
        nom: String,
        valeurSelectionnee: Double?,
        descriptionSelectionnee: String?,
        coefficients: List<fr.vetbrain.vetnutri_mp.Data.CoefP>,
        onCoefficientSelected: (fr.vetbrain.vetnutri_mp.Data.CoefP) -> Unit,
        modifier: Modifier = Modifier
) {
        var showDialog by remember { mutableStateOf(false) }

        Column(modifier = modifier) {
                // Label du coefficient
                Text(
                        text = nom,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold,
                        color = VetNutriColors.Primary
                )

                // Champ de sélection
                OutlinedTextField(
                        value =
                                if (valeurSelectionnee != null && descriptionSelectionnee != null) {
                                        "$descriptionSelectionnee (${fr.vetbrain.vetnutri_mp.Utils.TextUtils.formatDecimal(valeurSelectionnee.toDouble(), 2)})"
                                } else {
                                        ConsultationEdit.COEF_SELECTOR_NONE.translate()
                                },
                        onValueChange = {},
                        label = { Text(ConsultationEdit.COEF_SELECTOR_LABEL.translate()) },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                                IconButtonWithTooltip(
                                        onClick = { showDialog = true },
                                        imageVector = AppIcons.ArrowDropDown,
                                        contentDescription = ConsultationEdit.COEF_SELECTOR_NONE.translate(),
                                        tooltip = ConsultationEdit.COEF_SELECTOR_NONE.translate()
                                )
                        }
                )
        }

        // Dialog de sélection
        if (showDialog) {
                CoefficientSelectionDialog(
                        nom = nom,
                        coefficients = coefficients,
                        descriptionSelectionnee = descriptionSelectionnee,
                        onCoefficientSelected = { coef ->
                                onCoefficientSelected(coef)
                                showDialog = false
                        },
                        onDismiss = { showDialog = false }
                )
        }
}

@Composable
private fun CoefficientSelectionDialog(
        nom: String,
        coefficients: List<fr.vetbrain.vetnutri_mp.Data.CoefP>,
        descriptionSelectionnee: String?,
        onCoefficientSelected: (fr.vetbrain.vetnutri_mp.Data.CoefP) -> Unit,
        onDismiss: () -> Unit
) {
        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(ConsultationEdit.BCS_DIALOG_TITLE.translate(nom)) },
                text = {
                        LazyColumn {
                                items(coefficients) { coef ->
                                        Row(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                RadioButton(
                                                        selected =
                                                                descriptionSelectionnee ==
                                                                        coef.description,
                                                        onClick = { onCoefficientSelected(coef) }
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                        Text(
                                                                text = coef.description
                                                                                ?: ConsultationEdit.REF_COEF_NO_DESC.translate(),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body1
                                                        )
                                                        Text(
                                                                text =
                                                                        ConsultationEdit.COEF_VAL_LABEL.translate(coef.coef?.toString() ?: "0.0"),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body2,
                                                                color = Color.Gray
                                                        )
                                                }
                                        }
                                }
                        }
                },
                confirmButton = { TextButton(onClick = onDismiss) { Text(LocalizationKeys.General.CLOSE.translate()) } }
        )
}

@Composable
private fun ReferenceGeneraleDialog(
        value: String,
        onValueChange: (String) -> Unit,
        availableReferences: List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv>
) {
        val referencesGenerales = availableReferences.filter { !it.maladie }
        var searchText by remember { mutableStateOf("") }

        // Filtrer les références selon le texte de recherche
        val filteredReferences =
                remember(referencesGenerales, searchText) {
                        if (searchText.isBlank()) {
                                referencesGenerales
                        } else {
                                referencesGenerales.filter { reference ->
                                        reference.nom.contains(searchText, ignoreCase = true) ||
                                                reference.description?.contains(
                                                        searchText,
                                                        ignoreCase = true
                                                ) == true
                                }
                        }
                }

        AlertDialog(
                onDismissRequest = { onValueChange(value) },
                title = { Text(ConsultationEdit.REF_GENERAL_DIALOG_TITLE.translate()) },
                text = {
                        Column(modifier = Modifier.width(500.dp).height(400.dp)) {
                                // Barre de recherche
                                OutlinedTextField(
                                        value = searchText,
                                        onValueChange = { searchText = it },
                                        label = { Text(ConsultationEdit.REF_SEARCH_LABEL.translate()) },
                                        leadingIcon = {
                                                Icon(
                                                        AppIcons.Search,
                                                        contentDescription = ConsultationEdit.REF_SEARCH_TOOLTIP.translate()
                                                )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                        text = ConsultationEdit.REF_SEARCH_RESULTS.translate(filteredReferences.size.toString()),
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Liste scrollable des références
                                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                        // Option pour aucune référence
                                        item {
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(4.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        RadioButton(
                                                                selected = value.isEmpty(),
                                                                onClick = { onValueChange("") }
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                                text = ConsultationEdit.REF_NONE.translate(),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body1,
                                                                fontWeight = FontWeight.Bold
                                                        )
                                                }
                                        }

                                        // Toutes les références filtrées
                                        items(filteredReferences) { reference ->
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(4.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        RadioButton(
                                                                selected = value == reference.uuid,
                                                                onClick = {
                                                                        onValueChange(
                                                                                reference.uuid
                                                                        )
                                                                }
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                        text =
                                                                                reference.nom
                                                                                        .ifBlank {
                                                                                                ConsultationEdit.REF_NO_NAME.translate()
                                                                                        },
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body1,
                                                                        fontWeight =
                                                                                FontWeight.Medium
                                                                )
                                                                if (!reference.description
                                                                                .isNullOrBlank()
                                                                ) {
                                                                        Text(
                                                                                text =
                                                                                        reference
                                                                                                .description,
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .body2,
                                                                                color = Color.Gray,
                                                                                maxLines = 2
                                                                        )
                                                                }
                                                                Text(
                                                                        text =
                                                                                ConsultationEdit.REF_SPECIES_LABEL.translate(reference.espece?.name ?: ConsultationEdit.REF_SPECIES_UNSPECIFIED.translate()),
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .caption,
                                                                        color =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                )
                                                        }
                                                }
                                                Divider(color = Color.LightGray.copy(alpha = 0.3f))
                                        }
                                }
                        }
                },
                confirmButton = {
                        TextButton(onClick = { onValueChange(value) }) { Text(LocalizationKeys.General.CLOSE.translate()) }
                }
        )
}

@Composable
private fun ReferenceMaladieDialog(
        references: List<String>,
        availableReferences: List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv>,
        onReferenceSelected: (List<String>) -> Unit,
        onReferenceRemoved: (List<String>) -> Unit
) {
        // Les références arrivent déjà filtrées par espèce et type complémentaire
        var searchText by remember { mutableStateOf("") }

        // Filtrer les références selon le texte de recherche
        val filteredReferences =
                remember(availableReferences, searchText) {
                        if (searchText.isBlank()) {
                                availableReferences
                        } else {
                                availableReferences.filter { reference ->
                                        reference.nom.contains(searchText, ignoreCase = true) ||
                                                reference.description?.contains(
                                                        searchText,
                                                        ignoreCase = true
                                                ) == true
                                }
                        }
                }

        AlertDialog(
                onDismissRequest = { onReferenceSelected(references) },
                title = { Text(ConsultationEdit.REF_DISEASE_DIALOG_TITLE.translate()) },
                text = {
                        Column(modifier = Modifier.width(500.dp).height(400.dp)) {
                                // Barre de recherche
                                OutlinedTextField(
                                        value = searchText,
                                        onValueChange = { searchText = it },
                                        label = { Text(ConsultationEdit.REF_DISEASE_SEARCH_LABEL.translate()) },
                                        leadingIcon = {
                                                Icon(
                                                        AppIcons.Search,
                                                        contentDescription = ConsultationEdit.REF_SEARCH_TOOLTIP.translate()
                                                )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                        text =
                                                ConsultationEdit.REF_DISEASE_SEARCH_RESULTS.translate(filteredReferences.size.toString()),
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Liste scrollable des références
                                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                        items(filteredReferences) { reference ->
                                                val isSelected = references.contains(reference.uuid)

                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(4.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Checkbox(
                                                                checked = isSelected,
                                                                onCheckedChange = { checked ->
                                                                        val updatedReferences =
                                                                                if (checked) {
                                                                                        references +
                                                                                                reference
                                                                                                        .uuid
                                                                                } else {
                                                                                        references -
                                                                                                reference
                                                                                                        .uuid
                                                                                }
                                                                        onReferenceSelected(
                                                                                updatedReferences
                                                                        )
                                                                }
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                        text =
                                                                                reference.nom
                                                                                        .ifBlank {
                                                                                                ConsultationEdit.REF_NO_NAME.translate()
                                                                                        },
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body1,
                                                                        fontWeight =
                                                                                FontWeight.Medium
                                                                )
                                                                if (!reference.description
                                                                                .isNullOrBlank()
                                                                ) {
                                                                        Text(
                                                                                text =
                                                                                        reference
                                                                                                .description,
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .body2,
                                                                                color = Color.Gray,
                                                                                maxLines = 2
                                                                        )
                                                                }
                                                                Text(
                                                                        text =
                                                                                ConsultationEdit.REF_SPECIES_LABEL.translate(reference.espece?.name ?: ConsultationEdit.REF_SPECIES_UNSPECIFIED.translate()),
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .caption,
                                                                        color =
                                                                                VetNutriColors
                                                                                        .Secondary
                                                                )
                                                        }
                                                }
                                                Divider(color = Color.LightGray.copy(alpha = 0.3f))
                                        }
                                }
                        }
                },
                confirmButton = {
                        TextButton(onClick = { onReferenceSelected(references) }) { Text(LocalizationKeys.General.CLOSE.translate()) }
                }
        )
}

@Composable
private fun ConsultationKeywordDialog(
        selectedKeywordIds: List<String>,
        availableKeywords: List<ConsultationKeyword>,
        onKeywordsSelected: (List<String>) -> Unit,
        onKeywordCreated: (ConsultationKeyword) -> Unit
) {
        var searchText by remember { mutableStateOf("") }
        val trimmedSearch = searchText.trim()

        val filteredKeywords =
                remember(availableKeywords, searchText) {
                        if (searchText.isBlank()) {
                                availableKeywords
                        } else {
                                availableKeywords.filter { keyword ->
                                        keyword.label.contains(searchText, ignoreCase = true)
                                }
                        }
                }

        val existingKeyword =
                remember(availableKeywords, trimmedSearch) {
                        if (trimmedSearch.isBlank()) {
                                null
                        } else {
                                availableKeywords.firstOrNull {
                                        it.label.equals(trimmedSearch, ignoreCase = true)
                                }
                        }
                }

        AlertDialog(
                onDismissRequest = { onKeywordsSelected(selectedKeywordIds) },
                title = { Text(ConsultationEdit.KEYWORDS_DIALOG_TITLE.translate()) },
                text = {
                        Column(modifier = Modifier.width(500.dp).height(420.dp)) {
                                OutlinedTextField(
                                        value = searchText,
                                        onValueChange = { searchText = it },
                                        label = { Text(ConsultationEdit.KEYWORDS_SEARCH_LABEL.translate()) },
                                        leadingIcon = {
                                                Icon(
                                                        AppIcons.Search,
                                                        contentDescription = ConsultationEdit.KEYWORDS_SEARCH_TOOLTIP.translate()
                                                )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                )

                                if (trimmedSearch.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        if (existingKeyword == null) {
                                                OutlinedButton(
                                                        onClick = {
                                                                val newKeyword =
                                                                        ConsultationKeyword(
                                                                                label = trimmedSearch
                                                                        )
                                                                onKeywordCreated(newKeyword)
                                                        },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors =
                                                                ButtonDefaults.outlinedButtonColors(
                                                                        contentColor =
                                                                                VetNutriColors.Primary
                                                                )
                                                ) {
                                                        Icon(
                                                                AppIcons.Add,
                                                                contentDescription =
                                                                        ConsultationEdit.KEYWORDS_ADD_NEW.translate(
                                                                                trimmedSearch
                                                                        ),
                                                                modifier =
                                                                        Modifier.size(
                                                                                AppSizes.iconSizeSmall
                                                                        )
                                                        )
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.width(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        )
                                                        Text(
                                                                ConsultationEdit.KEYWORDS_ADD_NEW.translate(
                                                                        trimmedSearch
                                                                )
                                                        )
                                                }
                                        } else {
                                                OutlinedButton(
                                                        onClick = {
                                                                onKeywordsSelected(
                                                                        (selectedKeywordIds +
                                                                                        existingKeyword.uuid)
                                                                                .distinct()
                                                                )
                                                        },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors =
                                                                ButtonDefaults.outlinedButtonColors(
                                                                        contentColor =
                                                                                VetNutriColors.Primary
                                                                )
                                                ) {
                                                        Icon(
                                                                AppIcons.Add,
                                                                contentDescription =
                                                                        ConsultationEdit.KEYWORDS_ADD_EXISTING.translate(
                                                                                existingKeyword.label
                                                                        ),
                                                                modifier =
                                                                        Modifier.size(
                                                                                AppSizes.iconSizeSmall
                                                                        )
                                                        )
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.width(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        )
                                                        Text(
                                                                ConsultationEdit.KEYWORDS_ADD_EXISTING.translate(
                                                                        existingKeyword.label
                                                                )
                                                        )
                                                }
                                        }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                        text =
                                                ConsultationEdit.KEYWORDS_SEARCH_RESULTS.translate(
                                                        filteredKeywords.size.toString()
                                                ),
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                        items(filteredKeywords) { keyword ->
                                                val isSelected =
                                                        selectedKeywordIds.contains(keyword.uuid)

                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(4.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Checkbox(
                                                                checked = isSelected,
                                                                onCheckedChange = { checked ->
                                                                        val updatedKeywords =
                                                                                if (checked) {
                                                                                        selectedKeywordIds +
                                                                                                keyword
                                                                                                        .uuid
                                                                                } else {
                                                                                        selectedKeywordIds -
                                                                                                keyword
                                                                                                        .uuid
                                                                                }
                                                                        onKeywordsSelected(
                                                                                updatedKeywords
                                                                        )
                                                                }
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                                text = keyword.label,
                                                                style = MaterialTheme.typography.body1,
                                                                fontWeight = FontWeight.Medium
                                                        )
                                                }
                                                Divider(color = Color.LightGray.copy(alpha = 0.3f))
                                        }
                                }
                        }
                },
                confirmButton = {
                        TextButton(onClick = { onKeywordsSelected(selectedKeywordIds) }) {
                                Text(LocalizationKeys.General.CLOSE.translate())
                        }
                }
        )
}

/** Fonction pour extraire les variables requises par les équations d'une référence */
private fun extraireVariablesRequises(
        reference: fr.vetbrain.vetnutri_mp.Data.ReferenceEv?,
        referencesMaladies: List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv> = emptyList()
): List<fr.vetbrain.vetnutri_mp.Enumer.VariableKind> {
        if (reference == null) return emptyList()

        val variablesRequises = mutableSetOf<fr.vetbrain.vetnutri_mp.Enumer.VariableKind>()
        val scriptVariables = mutableSetOf<String>()

        // Extraire les variables en parsant les scripts des équations
        reference.equationBW?.equationScript?.let { script ->
                if (script.isNotBlank()) {
                        val variables =
                                fr.vetbrain.vetnutri_mp.Utils.ExpressionEvaluator.extraireVariables(
                                        script
                                )
                        scriptVariables.addAll(variables)
                }
        }

        reference.equationBEE?.equationScript?.let { script ->
                if (script.isNotBlank()) {
                        val variables =
                                fr.vetbrain.vetnutri_mp.Utils.ExpressionEvaluator.extraireVariables(
                                        script
                                )
                        scriptVariables.addAll(variables)
                }
        }

        reference.equationDEcom?.equationScript?.let { script ->
                if (script.isNotBlank()) {
                        val variables =
                                fr.vetbrain.vetnutri_mp.Utils.ExpressionEvaluator.extraireVariables(
                                        script
                                )
                        scriptVariables.addAll(variables)
                }
        }

        reference.equationDEraw?.equationScript?.let { script ->
                if (script.isNotBlank()) {
                        val variables =
                                fr.vetbrain.vetnutri_mp.Utils.ExpressionEvaluator.extraireVariables(
                                        script
                                )
                        scriptVariables.addAll(variables)
                }
        }

        reference.equationME?.equationScript?.let { script ->
                if (script.isNotBlank()) {
                        val variables =
                                fr.vetbrain.vetnutri_mp.Utils.ExpressionEvaluator.extraireVariables(
                                        script
                                )
                        scriptVariables.addAll(variables)
                }
        }

        // Extraire les variables des équations nutritionnelles (référence générale)
        reference.equationsNut.forEach { equation ->
                if (equation.equationScript.isNotBlank()) {
                        val variables =
                                fr.vetbrain.vetnutri_mp.Utils.ExpressionEvaluator.extraireVariables(
                                        equation.equationScript
                                )
                        scriptVariables.addAll(variables)
                }
        }

        // Inclure uniquement les variables des équations ENERCOMP des références maladies sélectionnées
        referencesMaladies.forEach { refMaladie ->
                refMaladie.equationsNut.forEach { eq ->
                        if (eq.kind == fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERCOMP &&
                                        eq.equationScript.isNotBlank()
                        ) {
                                val variables =
                                        fr.vetbrain.vetnutri_mp.Utils.ExpressionEvaluator.extraireVariables(
                                                eq.equationScript
                                        )
                                scriptVariables.addAll(variables)
                        }
                }
        }

        // Convertir les noms de variables en VariableKind
        for (variableName in scriptVariables) {
                val variableKind =
                        fr.vetbrain.vetnutri_mp.Enumer.VariableKind.entries.find {
                                it.label == variableName
                        }
                if (variableKind != null) {
                        variablesRequises.add(variableKind)
                } else {}
        }

        // Exclure les variables calculées/pilotées par le système
        variablesRequises.remove(fr.vetbrain.vetnutri_mp.Enumer.VariableKind.BW) // Poids saisi
        variablesRequises.remove(fr.vetbrain.vetnutri_mp.Enumer.VariableKind.MW) // Poids métabolique calculé
        variablesRequises.remove(fr.vetbrain.vetnutri_mp.Enumer.VariableKind.BEE) // BEE calculé/résolu
        variablesRequises.remove(fr.vetbrain.vetnutri_mp.Enumer.VariableKind.BE) // BE dérivé (après K et compl.)

        return variablesRequises.toList().sortedBy { it.label }
}

@Composable
private fun VariableSupplementaireField(
        variable: fr.vetbrain.vetnutri_mp.Enumer.VariableKind,
        valeurActuelle: Double?,
        onValeurChange: (Double?) -> Unit,
        modifier: Modifier = Modifier
) {
        var textValue by
                remember(valeurActuelle) { mutableStateOf(valeurActuelle?.toString() ?: "") }
        var isError by remember { mutableStateOf(false) }

        Card(
                modifier = modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 1.dp
        ) {
                Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                        // En-tête avec le nom de la variable
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text = variable.label,
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.Medium,
                                        color = VetNutriColors.Primary
                                )
                                Text(
                                        text = "(${variable.variable})",
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Gray
                                )
                        }

                        // Description de la variable
                        getVariableDescription(variable)?.let { description ->
                                Text(
                                        text = description,
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                )
                        }

                        // Champ de saisie (grand clavier numérique)
                        OutlinedTextField(
                                value = textValue,
                                onValueChange = { newValue ->
                                        val texteFiltre =
                                                newValue.filter { char ->
                                                        char.isDigit() || char == '.' || char == ','
                                                }
                                        val pointCount = texteFiltre.count { it == '.' }
                                        val virguleCount = texteFiltre.count { it == ',' }
                                        if (pointCount <= 1 &&
                                                        virguleCount <= 1 &&
                                                        pointCount + virguleCount <= 1
                                        ) {
                                                textValue = texteFiltre
                                                try {
                                                        val texteNormalise = texteFiltre.replace(',', '.')
                                                        when {
                                                                texteNormalise.isEmpty() -> {
                                                                        onValeurChange(null)
                                                                        isError = false
                                                                }
                                                                texteNormalise.toDoubleOrNull() != null -> {
                                                                        val doubleValue =
                                                                                texteNormalise.toDouble()
                                                                        if (doubleValue >= 0) {
                                                                                onValeurChange(doubleValue)
                                                                                isError = false
                                                                        } else {
                                                                                isError = true
                                                                        }
                                                                }
                                                                else -> {
                                                                        isError = true
                                                                }
                                                        }
                                                } catch (e: NumberFormatException) {
                                                        isError = true
                                                }
                                        }
                                },
                                label = { Text(ConsultationEdit.VAR_VALUE_LABEL.translate()) },
                                placeholder = { Text(ConsultationEdit.VAR_VALUE_PLACEHOLDER.translate()) },
                                leadingIcon = {
                                        Icon(
                                                imageVector = AppIcons.Analysis,
                                                contentDescription = null,
                                                tint =
                                                        if (isError) MaterialTheme.colors.error
                                                        else VetNutriColors.Primary
                                        )
                                },
                                isError = isError,
                                modifier = Modifier.fillMaxWidth(),
                                colors =
                                        TextFieldDefaults.outlinedTextFieldColors(
                                                focusedBorderColor = VetNutriColors.Primary,
                                                focusedLabelColor = VetNutriColors.Primary
                                        ),
                                keyboardOptions =
                                        KeyboardOptions(
                                                keyboardType = KeyboardType.Text,
                                                imeAction = ImeAction.Done
                                        ),
                                singleLine = true
                        )
                }

                // (supprimé) pas de dialog ici; géré au niveau supérieur de l'écran
        }
}

/** Fonction pour obtenir une description de la variable */
private fun getVariableDescription(variable: fr.vetbrain.vetnutri_mp.Enumer.VariableKind): String? {
        return when (variable) {
                fr.vetbrain.vetnutri_mp.Enumer.VariableKind.AdultWeight ->
                        fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Variables.DESC_ADULT_WEIGHT.translate()
                fr.vetbrain.vetnutri_mp.Enumer.VariableKind.LitterSize ->
                        fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Variables.DESC_LITTER_SIZE.translate()
                fr.vetbrain.vetnutri_mp.Enumer.VariableKind.WeekGestation ->
                        fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Variables.DESC_WEEK_GESTATION.translate()
                fr.vetbrain.vetnutri_mp.Enumer.VariableKind.WeekLactation ->
                        fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Variables.DESC_WEEK_LACTATION.translate()
                fr.vetbrain.vetnutri_mp.Enumer.VariableKind.BEE ->
                        fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Variables.DESC_BEE.translate()
                fr.vetbrain.vetnutri_mp.Enumer.VariableKind.BE -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Variables.DESC_BE.translate()
                fr.vetbrain.vetnutri_mp.Enumer.VariableKind.iBW -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Variables.DESC_IBW.translate()
                fr.vetbrain.vetnutri_mp.Enumer.VariableKind.MW -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Variables.DESC_MW.translate()
                else -> null
        }
}
