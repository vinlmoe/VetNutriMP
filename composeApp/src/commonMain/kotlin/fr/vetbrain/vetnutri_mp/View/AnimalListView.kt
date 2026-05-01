package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.ConfirmDialog
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationKeyword
import fr.vetbrain.vetnutri_mp.Data.ExamSession
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.AnimalList
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Settings
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import fr.vetbrain.vetnutri_mp.View.Components.QRCodeScannerView
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.launch
import fr.vetbrain.vetnutri_mp.Utils.copyToClipboardComposable
import fr.vetbrain.vetnutri_mp.Utils.isIosPlatform

/**
 * Liste des animaux.
 * - Observe les flux filtrés du `AnimalListViewModel` (recherche + espèce).
 * - Pilote l'export examen et la navigation vers aliments / calculs.
 */
@OptIn(ExperimentalUuidApi::class, ExperimentalMaterialApi::class)
@Composable
fun AnimalListView(
        viewModel: AnimalListViewModel,
        onAddAnimal: () -> Unit,
        onSelectAnimal: (AnimalEv) -> Unit,
        onEditAnimal: (AnimalEv) -> Unit,
        onShowFoodList: () -> Unit,
        onShowCalculationTabs: () -> Unit,
        examSession: ExamSession? = null,
        modifier: Modifier = Modifier
) {
        val animals: List<AnimalEv> = viewModel.animals.collectAsState().value
        val searchQuery = viewModel.searchQuery.collectAsState().value
        val selectedEspece = viewModel.selectedEspece.collectAsState().value
        val availableKeywords = viewModel.availableKeywords.collectAsState().value
        val keywordIncludeIds = viewModel.keywordIncludeIds.collectAsState().value
        val keywordExcludeIds = viewModel.keywordExcludeIds.collectAsState().value
        val coroutineScope = rememberCoroutineScope()

        // États pour l'export examen
        var isExporting by remember { mutableStateOf(false) }
        var exportError by remember { mutableStateOf<String?>(null) }
        var exportLink by remember {
                mutableStateOf<fr.vetbrain.vetnutri_mp.Service.ShareLink?>(null)
        }
        var showExportResultDialog by remember { mutableStateOf(false) }
        var pendingCopyText by remember { mutableStateOf<String?>(null) }
        var showQuickImportDialog by remember { mutableStateOf(false) }
        var quickImportInput by remember { mutableStateOf("") }
        var showQuickImportScanner by remember { mutableStateOf(false) }
        var pendingAutoOpenAnimalId by remember { mutableStateOf<String?>(null) }
        var shouldAutoOpenAfterDialog by remember { mutableStateOf(false) }
        var showKeywordFilterDialog by remember { mutableStateOf(false) }
        val hasKeywordFilter = keywordIncludeIds.isNotEmpty() || keywordExcludeIds.isNotEmpty()
        val isExamMode = examSession != null

        LaunchedEffect(Unit) { viewModel.loadAnimals() }

        Scaffold(
                floatingActionButton = {
                        FloatingActionButton(
                                onClick = onAddAnimal,
                                backgroundColor = VetNutriColors.Primary
                        ) {
                                Icon(
                                        imageVector = AppIcons.Add,
                                        contentDescription = "Ajouter un animal",
                                        tint = VetNutriColors.OnPrimary
                                )
                        }
                }
        ) { paddingValues ->
                Column(
                        modifier = modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                                // Espace réservé pour éventuels raccourcis (imports désactivés)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Rangée de boutons pour les listes spéciales
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                // Bouton pour accéder à la liste des aliments
                                Button(
                                        onClick = onShowFoodList,
                                        modifier = Modifier.weight(1f),
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Primary,
                                                        contentColor = VetNutriColors.OnPrimary
                                                )
                                ) { Text(translate(AnimalList.FOOD_LIST)) }

                                // Bouton pour accéder aux données de calcul
                                Button(
                                        onClick = onShowCalculationTabs,
                                        modifier = Modifier.weight(1f),
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Primary,
                                                        contentColor = VetNutriColors.OnPrimary
                                                )
                                ) { Text(translate(AnimalList.CALCULATION_DATA)) }

                                if (examSession == null) {
                                        Button(
                                                onClick = { showQuickImportDialog = true },
                                                modifier = Modifier.weight(1f),
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                backgroundColor =
                                                                        VetNutriColors.Secondary,
                                                                contentColor = Color.White
                                                        )
                                        ) { Text(translate(AnimalList.QUICK_IMPORT)) }
                                } else {
                                        // Bouton Export Examen
                                        Button(
                                                onClick = {
                                                        isExporting = true
                                                        exportError = null
                                                        exportLink = null
                                                        coroutineScope.launch {
                                                                val result =
                                                                        viewModel.exportExamAnimalsToJsonBin(
                                                                                examSession
                                                                        )
                                                                isExporting = false
                                                                result.fold(
                                                                        onSuccess = { link ->
                                                                                exportLink = link
                                                                                exportError = null
                                                                        },
                                                                        onFailure = { error ->
                                                                                exportLink = null
                                                                                exportError =
                                                                                        error.message
                                                                                ?: translate(
                                                                                        AnimalList.EXPORT_EXAM_ERROR
                                                                                )
                                                                        }
                                                                )
                                                                showExportResultDialog = true
                                                        }
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                backgroundColor =
                                                                        VetNutriColors.Secondary,
                                                                contentColor = Color.White
                                                        )
                                        ) { Text(translate(AnimalList.EXPORT_EXAM)) }
                                }

                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Filtres de recherche
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                val isCompact = maxWidth < 560.dp
                                val placeholderText =
                                        if (isCompact) {
                                                General.SEARCH.translate()
                                        } else {
                                                "${General.SEARCH.translate()} (ID, ${Animal.NAME.translate()}, ${Animal.OWNER.translate()}, ${Animal.BREED.translate()})"
                                        }
                                val especeNullLabel =
                                        if (isCompact) Animal.SPECIES.translate()
                                        else "Toutes espèces"

                                if (isCompact) {
                                        Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                                OutlinedTextField(
                                                        value = searchQuery,
                                                        onValueChange = { viewModel.setSearchQuery(it) },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        placeholder = {
                                                                Text(
                                                                        placeholderText,
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis
                                                                )
                                                        },
                                                        leadingIcon = {
                                                                Icon(
                                                                        Icons.Default.Search,
                                                                        contentDescription = null
                                                                )
                                                        },
                                                        trailingIcon = {
                                                                if (searchQuery.isNotEmpty()) {
                                                                        IconButtonWithTooltip(
                                                                                onClick = {
                                                                                        viewModel.setSearchQuery("")
                                                                                },
                                                                                imageVector = Icons.Default.Clear,
                                                                                contentDescription = "Effacer",
                                                                                tooltip = "Effacer"
                                                                        )
                                                                }
                                                        },
                                                        singleLine = true,
                                                        colors =
                                                                TextFieldDefaults.outlinedTextFieldColors(
                                                                        focusedBorderColor = VetNutriColors.Primary,
                                                                        unfocusedBorderColor = Color.Gray
                                                                )
                                                )

                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                        EspeceDropdown(
                                                                selectedEspece = selectedEspece,
                                                                onEspeceSelected = { viewModel.setSelectedEspece(it) },
                                                                availableEspeces = viewModel.availableEspeces,
                                                                nullLabel = especeNullLabel,
                                                                modifier = Modifier.weight(1f)
                                                        )

                                                        OutlinedButton(
                                                                onClick = { showKeywordFilterDialog = true },
                                                                modifier = Modifier.weight(1f)
                                                        ) {
                                                                Text(
                                                                        translate(AnimalList.KEYWORD_FILTER_BUTTON),
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis
                                                                )
                                                                if (hasKeywordFilter) {
                                                                        Spacer(
                                                                                modifier =
                                                                                        Modifier.width(
                                                                                                AppSizes.paddingSmall
                                                                                        )
                                                                        )
                                                                        Box(
                                                                                modifier =
                                                                                        Modifier.size(8.dp)
                                                                                                .background(
                                                                                                        VetNutriColors.Primary,
                                                                                                        shape =
                                                                                                                MaterialTheme.shapes.small
                                                                                                )
                                                                        )
                                                                }
                                                        }
                                                }
                                        }
                                } else {
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                                OutlinedTextField(
                                                        value = searchQuery,
                                                        onValueChange = { viewModel.setSearchQuery(it) },
                                                        modifier = Modifier.weight(2f),
                                                        placeholder = {
                                                                Text(
                                                                        placeholderText,
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis
                                                                )
                                                        },
                                                        leadingIcon = {
                                                                Icon(
                                                                        Icons.Default.Search,
                                                                        contentDescription = null
                                                                )
                                                        },
                                                        trailingIcon = {
                                                                if (searchQuery.isNotEmpty()) {
                                                                        IconButtonWithTooltip(
                                                                                onClick = {
                                                                                        viewModel.setSearchQuery("")
                                                                                },
                                                                                imageVector = Icons.Default.Clear,
                                                                                contentDescription = "Effacer",
                                                                                tooltip = "Effacer"
                                                                        )
                                                                }
                                                        },
                                                        singleLine = true,
                                                        colors =
                                                                TextFieldDefaults.outlinedTextFieldColors(
                                                                        focusedBorderColor = VetNutriColors.Primary,
                                                                        unfocusedBorderColor = Color.Gray
                                                                )
                                                )

                                                EspeceDropdown(
                                                        selectedEspece = selectedEspece,
                                                        onEspeceSelected = { viewModel.setSelectedEspece(it) },
                                                        availableEspeces = viewModel.availableEspeces,
                                                        nullLabel = especeNullLabel,
                                                        modifier = Modifier.weight(1f)
                                                )

                                                OutlinedButton(
                                                        onClick = { showKeywordFilterDialog = true },
                                                        modifier = Modifier.weight(1f)
                                                ) {
                                                        Text(translate(AnimalList.KEYWORD_FILTER_BUTTON))
                                                        if (hasKeywordFilter) {
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.width(
                                                                                        AppSizes.paddingSmall
                                                                                )
                                                                )
                                                                Box(
                                                                        modifier =
                                                                                Modifier.size(8.dp)
                                                                                        .background(
                                                                                                VetNutriColors.Primary,
                                                                                                shape =
                                                                                                        MaterialTheme.shapes.small
                                                                                        )
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (animals.isEmpty()) {
                                Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                ) {
                                        Text(
                                                text =
                                                        if (searchQuery.isEmpty() &&
                                                                        selectedEspece == null &&
                                                                        !hasKeywordFilter
                                                        )
                                                                translate(AnimalList.NO_ANIMAL_FOUND)
                                                        else
                                                                translate(AnimalList.NO_FILTER_RESULTS),
                                                style = MaterialTheme.typography.body1
                                        )
                                }
                        } else {
                                LazyColumn(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                        items(animals) { animal ->
                                                AnimalCard(
                                                        animal = animal,
                                                        onClick = { onSelectAnimal(animal) },
                                                        onDelete = {
                                                                viewModel.deleteAnimal(animal)
                                                        },
                                                        isExamMode = isExamMode
                                                )
                                        }
                                }
                        }
                }
        }

        if (isExporting) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(translate(AnimalList.EXPORT_EXAM_TITLE)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(translate(AnimalList.EXPORT_EXAM_IN_PROGRESS))
                    }
                },
                confirmButton = {}
            )
        }

        if (pendingCopyText != null) {
            copyToClipboardComposable(pendingCopyText!!)
            LaunchedEffect(pendingCopyText) { pendingCopyText = null }
        }

        if (showExportResultDialog) {
            AlertDialog(
                onDismissRequest = { showExportResultDialog = false },
                title = { Text(translate(AnimalList.EXPORT_EXAM_TITLE)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (exportLink != null && exportError == null) {
                            Text(translate(AnimalList.EXPORT_EXAM_SUCCESS))
                            Text(
                                    text = exportLink!!.binId,
                                    style = MaterialTheme.typography.subtitle1,
                                    color = VetNutriColors.Primary
                            )
                            Text(translate(AnimalList.EXPORT_EXAM_NOTE))
                        } else {
                            Text(
                                    exportError
                                            ?: translate(AnimalList.EXPORT_EXAM_ERROR),
                                    color = MaterialTheme.colors.error
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showExportResultDialog = false }) { Text("OK") }
                },
                dismissButton = {
                    if (exportLink != null && exportError == null) {
                        OutlinedButton(
                                onClick = { pendingCopyText = exportLink!!.binId }
                        ) { Text(translate(General.COPY)) }
                    }
                }
            )
        }

        if (showQuickImportDialog) {
            AlertDialog(
                onDismissRequest = {
                    showQuickImportDialog = false
                    quickImportInput = ""
                },
                title = { Text(translate(AnimalList.QUICK_IMPORT_TITLE)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            translate(Settings.JSONBIN_MESSAGE),
                            style = MaterialTheme.typography.body2
                        )
                        OutlinedTextField(
                            value = quickImportInput,
                            onValueChange = { quickImportInput = it },
                            label = {
                                Text(translate(Settings.JSONBIN_LABEL))
                            },
                            placeholder = {
                                Text(translate(Settings.JSONBIN_PLACEHOLDER))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedButton(
                            onClick = { showQuickImportScanner = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(translate(AnimalList.SCAN_QR))
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (quickImportInput.isNotBlank()) {
                                coroutineScope.launch {
                                    val result =
                                        viewModel.importFromJsonBin(quickImportInput.trim())
                                    viewModel.setImportResult(result)
                                    if (result is AnimalListViewModel.ImportResult.Success &&
                                        result.animalIds.size == 1
                                    ) {
                                        pendingAutoOpenAnimalId = result.animalIds.first()
                                        shouldAutoOpenAfterDialog = true
                                    } else {
                                        pendingAutoOpenAnimalId = null
                                        shouldAutoOpenAfterDialog = false
                                    }
                                    viewModel.loadAnimals()
                                    showQuickImportDialog = false
                                    quickImportInput = ""
                                }
                            }
                        },
                        enabled = quickImportInput.isNotBlank(),
                        colors =
                            ButtonDefaults.buttonColors(
                                backgroundColor = VetNutriColors.Primary
                            )
                    ) {
                        Text(translate(General.IMPORT), color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showQuickImportDialog = false
                            quickImportInput = ""
                        }
                    ) { Text(translate(General.CANCEL)) }
                }
            )
        }

        if (showQuickImportScanner) {
            AlertDialog(
                onDismissRequest = { showQuickImportScanner = false },
                title = { Text(translate(AnimalList.SCAN_QR)) },
                text = {
                    Box(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                        QRCodeScannerView(
                            onCodeScanned = { code ->
                                quickImportInput = code
                                showQuickImportScanner = false
                            },
                            onClose = { showQuickImportScanner = false },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showQuickImportScanner = false }) {
                        Text(translate(General.CANCEL))
                    }
                }
            )
        }

        val apiImportResult = viewModel.importResult.collectAsState().value
        val apiImporting = viewModel.isApiImporting.collectAsState().value
        val apiProgress = viewModel.apiImportProgress.collectAsState().value
        val apiLogs = viewModel.apiImportLogs.collectAsState().value

        if (apiImporting) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(translate(Settings.IMPORT_RUNNING)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LinearProgressIndicator(progress = apiProgress.toFloat())
                        Box(
                            modifier =
                                Modifier.fillMaxWidth().height(120.dp).verticalScroll(
                                    rememberScrollState()
                                )
                        ) {
                            Column {
                                apiLogs.takeLast(10).forEach { line -> Text(line) }
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }

        var showApiImportDialog by remember { mutableStateOf(false) }
        LaunchedEffect(apiImportResult) {
            showApiImportDialog = apiImportResult != null
        }
        if (showApiImportDialog && apiImportResult != null) {
            AlertDialog(
                onDismissRequest = {
                    showApiImportDialog = false
                    viewModel.resetImportResult()
                },
                title = { Text("Résultat de l'import") },
                text = {
                    when (val r = apiImportResult) {
                        is AnimalListViewModel.ImportResult.Success -> {
                            Column {
                                Text("Total pris en compte: ${r.count}")
                                Text("Importés: ${r.importedCount}")
                                if (r.updatedCount > 0) Text("Mises à jour: ${r.updatedCount}")
                                if (r.deletedCount > 0) Text("Supprimés: ${r.deletedCount}")
                                if (r.errorCount > 0) {
                                    Text(
                                        "Erreurs: ${r.errorCount}",
                                        color = MaterialTheme.colors.error
                                    )
                                }
                                if (r.conseils > 0) Text("Conseils: ${r.conseils}")
                            }
                        }
                        is AnimalListViewModel.ImportResult.Error -> {
                            Text(
                                "Erreur: ${r.message}",
                                color = MaterialTheme.colors.error
                            )
                        }
                        else -> {}
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showApiImportDialog = false
                            val id = pendingAutoOpenAnimalId
                            val shouldOpen = shouldAutoOpenAfterDialog && id != null
                            shouldAutoOpenAfterDialog = false
                            pendingAutoOpenAnimalId = null
                            viewModel.resetImportResult()
                            if (shouldOpen) {
                                coroutineScope.launch {
                                    val animal = viewModel.getAnimalById(id!!)
                                    if (animal != null) onSelectAnimal(animal)
                                }
                            }
                        }
                    ) { Text("OK") }
                }
            )
        }

        if (showKeywordFilterDialog) {
                KeywordFilterDialog(
                        availableKeywords = availableKeywords,
                        includeIds = keywordIncludeIds,
                        excludeIds = keywordExcludeIds,
                        onUpdateFilters = { includeIds, excludeIds ->
                                viewModel.setKeywordFilters(includeIds, excludeIds)
                        },
                        onReset = { viewModel.clearKeywordFilters() },
                        onDismiss = { showKeywordFilterDialog = false }
                )
        }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun EspeceDropdown(
        selectedEspece: Espece?,
        onEspeceSelected: (Espece?) -> Unit,
        availableEspeces: List<Espece?>,
        nullLabel: String,
        modifier: Modifier = Modifier
) {
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = modifier
        ) {
                OutlinedTextField(
                        value = selectedEspece?.label ?: nullLabel,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        trailingIcon = {
                                Icon(
                                        if (expanded) Icons.Default.KeyboardArrowUp
                                        else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null
                                )
                        },
                        colors =
                                TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = VetNutriColors.Primary,
                                        unfocusedBorderColor = Color.Gray
                                ),
                        modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            if (!isIosPlatform) {
                                expanded = false 
                            }
                        },
                        modifier = Modifier.exposedDropdownSize()
                ) {
                        availableEspeces.forEach { espece ->
                                DropdownMenuItem(
                                        onClick = {
                                                onEspeceSelected(espece)
                                                expanded = false
                                        }
                                ) { Text(espece?.label ?: nullLabel) }
                        }
                }
        }
}

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterialApi::class)
@Composable
private fun AnimalCard(
        animal: AnimalEv,
        onClick: () -> Unit,
        onDelete: () -> Unit,
        isExamMode: Boolean,
        modifier: Modifier = Modifier
) {
        var showDeleteConfirmation by remember { mutableStateOf(false) }

        Card(modifier = modifier.fillMaxWidth(), elevation = 4.dp) {
                Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Column(modifier = Modifier.weight(1f)) {
                                        Text(text = animal.nom, style = MaterialTheme.typography.h6)
                                        Text(
                                                text =
                                                        "${Animal.SPECIES.translate()}: ${animal.getEspece().translateEnum()}",
                                                style = MaterialTheme.typography.body1
                                        )
                                        if (animal.race.isNotEmpty()) {
                                                Text(
                                                        text =
                                                                "${Animal.BREED.translate()}: ${animal.race}",
                                                        style = MaterialTheme.typography.body2
                                                )
                                        }
                                        if (animal.ownerName.isNotEmpty()) {
                                                Text(
                                                        text =
                                                                "${Animal.OWNER.translate()}: ${animal.ownerName}",
                                                        style = MaterialTheme.typography.body2
                                                )
                                        }
                                        if (isExamMode && !animal.examExerciseId.isNullOrBlank()) {
                                                Text(
                                                        text =
                                                                "${Animal.EXAM_EXERCISE_ID.translate()}: ${animal.examExerciseId}",
                                                        style = MaterialTheme.typography.body2,
                                                        color = VetNutriColors.Primary
                                                )
                                        }
                                }
                                Row {
                                        IconButtonWithTooltip(
                                                onClick = onClick,
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Détails",
                                                tooltip = "Voir les détails"
                                        )
                                        IconButtonWithTooltip(
                                                onClick = { showDeleteConfirmation = true },
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Supprimer",
                                                tooltip = "Supprimer",
                                                tint = Color.Red
                                        )
                                }
                        }
                }
        }

        if (showDeleteConfirmation) {
                ConfirmDialog(
                        title = Animal.DELETE_ANIMAL.translate(),
                        message = Animal.DELETE_ANIMAL_CONFIRM.translate(),
                        onConfirm = {
                                onDelete()
                                showDeleteConfirmation = false
                        },
                        onDismiss = { showDeleteConfirmation = false }
                )
        }
}

@Composable
private fun KeywordFilterDialog(
        availableKeywords: List<ConsultationKeyword>,
        includeIds: Set<String>,
        excludeIds: Set<String>,
        onUpdateFilters: (Set<String>, Set<String>) -> Unit,
        onReset: () -> Unit,
        onDismiss: () -> Unit
) {
        val sortedKeywords =
                remember(availableKeywords) {
                        availableKeywords.sortedBy { it.label.lowercase() }
                }
        val includeLabel = translate(AnimalList.KEYWORD_FILTER_INCLUDE)
        val excludeLabel = translate(AnimalList.KEYWORD_FILTER_EXCLUDE)

        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(translate(AnimalList.KEYWORD_FILTER_TITLE)) },
                text = {
                        Column(modifier = Modifier.width(520.dp).height(480.dp)) {
                                if (sortedKeywords.isEmpty()) {
                                        Text(translate(AnimalList.KEYWORD_FILTER_EMPTY))
                                } else {
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                                Box(modifier = Modifier.width(72.dp)) {
                                                        Text(includeLabel, style = MaterialTheme.typography.caption)
                                                }
                                                Box(modifier = Modifier.width(72.dp)) {
                                                        Text(excludeLabel, style = MaterialTheme.typography.caption)
                                                }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        LazyColumn(
                                                modifier = Modifier.fillMaxWidth().weight(1f)
                                        ) {
                                                items(sortedKeywords) { keyword ->
                                                        val isIncluded = includeIds.contains(keyword.uuid)
                                                        val isExcluded = excludeIds.contains(keyword.uuid)

                                                        Row(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .padding(4.dp),
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                Box(modifier = Modifier.width(72.dp)) {
                                                                        Checkbox(
                                                                                checked = isIncluded,
                                                                                onCheckedChange = { checked ->
                                                                                        val newInclude = includeIds.toMutableSet()
                                                                                        val newExclude = excludeIds.toMutableSet()
                                                                                        if (checked) {
                                                                                                newInclude.add(keyword.uuid)
                                                                                                newExclude.remove(keyword.uuid)
                                                                                        } else {
                                                                                                newInclude.remove(keyword.uuid)
                                                                                        }
                                                                                        onUpdateFilters(newInclude, newExclude)
                                                                                }
                                                                        )
                                                                }
                                                                Box(modifier = Modifier.width(72.dp)) {
                                                                        Checkbox(
                                                                                checked = isExcluded,
                                                                                onCheckedChange = { checked ->
                                                                                        val newInclude = includeIds.toMutableSet()
                                                                                        val newExclude = excludeIds.toMutableSet()
                                                                                        if (checked) {
                                                                                                newExclude.add(keyword.uuid)
                                                                                                newInclude.remove(keyword.uuid)
                                                                                        } else {
                                                                                                newExclude.remove(keyword.uuid)
                                                                                        }
                                                                                        onUpdateFilters(newInclude, newExclude)
                                                                                }
                                                                        )
                                                                }
                                                                Text(
                                                                        text = keyword.label,
                                                                        style = MaterialTheme.typography.body1
                                                                )
                                                        }
                                                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                                                }
                                        }
                                }
                        }
                },
                confirmButton = {
                        TextButton(onClick = onDismiss) {
                                Text(translate(General.CLOSE))
                        }
                },
                dismissButton = {
                        TextButton(onClick = onReset) {
                                Text(translate(General.RESET))
                        }
                }
        )
}
