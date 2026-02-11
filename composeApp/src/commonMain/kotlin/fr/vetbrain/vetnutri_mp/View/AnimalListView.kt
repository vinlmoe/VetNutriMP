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
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import fr.vetbrain.vetnutri_mp.Components.ConfirmDialog
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationKeyword
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.AnimalList
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import fr.vetbrain.vetnutri_mp.View.Components.QRCodeScannerView
import fr.vetbrain.vetnutri_mp.getPlatform
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.launch
import fr.vetbrain.vetnutri_mp.Utils.isIosPlatform
import fr.vetbrain.vetnutri_mp.Utils.getClipboardTextComposable

/**
 * Liste des animaux.
 * - Observe les flux filtrés du `AnimalListViewModel` (recherche + espèce).
 * - Pilote les imports (rapide/API) et la navigation vers aliments / calculs.
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
        onShowCrossAnalysis: () -> Unit,
        modifier: Modifier = Modifier
) {
        val animals: List<AnimalEv> = viewModel.animals.collectAsState().value
        val searchQuery = viewModel.searchQuery.collectAsState().value
        val selectedEspece = viewModel.selectedEspece.collectAsState().value
        val availableKeywords = viewModel.availableKeywords.collectAsState().value
        val keywordIncludeIds = viewModel.keywordIncludeIds.collectAsState().value
        val keywordExcludeIds = viewModel.keywordExcludeIds.collectAsState().value
        val coroutineScope = rememberCoroutineScope()

        // États pour l'import rapide
        var showImportDialog by remember { mutableStateOf(false) }
        var importCode by remember { mutableStateOf("") }
        
        // États pour le suivi de l'import API
        val apiImportResult = viewModel.importResult.collectAsState().value
        val apiImporting = viewModel.isApiImporting.collectAsState().value
        val apiProgress = viewModel.apiImportProgress.collectAsState().value
        val apiLogs = viewModel.apiImportLogs.collectAsState().value
        var showApiResultDialog by remember { mutableStateOf(false) }
        val showCrossAnalysisButton = true
        var showKeywordFilterDialog by remember { mutableStateOf(false) }
        val hasKeywordFilter = keywordIncludeIds.isNotEmpty() || keywordExcludeIds.isNotEmpty()

        LaunchedEffect(apiImportResult) {
            if (apiImportResult != null && apiImportResult is AnimalListViewModel.ImportResult.Success) {
                showApiResultDialog = true
                showImportDialog = false
            } else if (apiImportResult != null && apiImportResult is AnimalListViewModel.ImportResult.Error) {
                showApiResultDialog = true
            }
        }

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

                                // Bouton Import Rapide
                                Button(
                                        onClick = { showImportDialog = true },
                                        modifier = Modifier.weight(1f),
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Secondary,
                                                        contentColor = Color.White
                                                )
                                ) { Text(translate(AnimalList.QUICK_IMPORT)) }

                                if (showCrossAnalysisButton) {
                                        Button(
                                                onClick = onShowCrossAnalysis,
                                                modifier = Modifier.weight(1f),
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                backgroundColor =
                                                                        VetNutriColors.Primary,
                                                                contentColor =
                                                                        VetNutriColors.OnPrimary
                                                        )
                                        ) { Text(translate(AnimalList.CROSS_ANALYSIS)) }
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
                                                "${General.SEARCH.translate()} (${Animal.NAME.translate()}, ${Animal.OWNER.translate()}, ${Animal.BREED.translate()})"
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
                                                        }
                                                )
                                        }
                                }
                        }
                }
        }

        if (showImportDialog) {
            val platform = getPlatform()
            val isDesktop = platform.name.contains("Java") || platform.name.contains("Windows") || platform.name.contains("Linux")
            var showScanner by remember { mutableStateOf(false) }
            var shouldPaste by remember { mutableStateOf(false) }
            val shareService = remember { fr.vetbrain.vetnutri_mp.Service.createJsonShareService() }
            val trimmedImportCode = importCode.trim()
            val qrPayload = remember(trimmedImportCode) { shareService.parseQrPayload(trimmedImportCode) }
            val isEncryptedQr = qrPayload?.key?.isNotBlank() == true && qrPayload.iv?.isNotBlank() == true
            val isQrPayload = qrPayload != null
            val isJsonBinUrl = trimmedImportCode.contains("jsonbin.io", ignoreCase = true)
            val isLikelyBinId = trimmedImportCode.matches(Regex("[A-Za-z0-9]{10,}"))
            val importTypeLabel = when {
                trimmedImportCode.isBlank() -> null
                isEncryptedQr -> "Chiffré (QR)"
                isQrPayload -> "QR sans chiffrement"
                isJsonBinUrl || isLikelyBinId -> "Non chiffré (URL/BinID)"
                else -> "Format inconnu"
            }

            if (shouldPaste) {
                val clipboardText = getClipboardTextComposable()
                if (!clipboardText.isNullOrBlank()) {
                    importCode = clipboardText.trim()
                }
                shouldPaste = false
            }

            if (showScanner) {
                Dialog(
                    onDismissRequest = { showScanner = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                        QRCodeScannerView(
                            onCodeScanned = { code ->
                                showScanner = false
                                importCode = code
                                // Optionnel: lancer l'import automatiquement
                                // coroutineScope.launch { viewModel.importFromJsonBin(code) }
                            },
                            onClose = { showScanner = false }
                        )
                        
                        // Bouton fermer le scanner
                        IconButton(
                            onClick = { showScanner = false },
                            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Fermer",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = { Text(translate(AnimalList.QUICK_IMPORT_TITLE)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Collez un BinID, une URL jsonbin.io ou un QR JSON (chiffré ou non).")
                        OutlinedTextField(
                            value = importCode,
                            onValueChange = { importCode = it },
                            label = { Text(translate(AnimalList.CODE_OR_URL)) },
                            placeholder = { Text("{\"binId\":\"...\",\"key\":\"...\",\"iv\":\"...\"}") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { shouldPaste = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Coller")
                            }
                            importTypeLabel?.let { label ->
                                Surface(
                                    color = if (isEncryptedQr) VetNutriColors.Primary else Color.LightGray,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isEncryptedQr) Color.White else Color.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.caption
                                    )
                                }
                            }
                        }

                        // Ou scanner (uniquement sur mobile)
                        if (!isDesktop) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(translate(General.OR), color = Color.Gray)
                            }

                            Button(
                                onClick = { showScanner = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray, contentColor = Color.White)
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(translate(AnimalList.SCAN_QR))
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (importCode.isNotBlank()) {
                                coroutineScope.launch {
                                    viewModel.importFromJsonBin(importCode)
                                }
                                // On ne ferme pas le dialog tout de suite, le progress va s'afficher
                            }
                        },
                        enabled = importCode.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Primary, contentColor = Color.White)
                    ) { Text("Importer") }
                },
                dismissButton = {
                    Button(onClick = { showImportDialog = false }) { Text("Annuler") }
                }
            )
        }

        if (apiImporting) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(translate(General.IMPORTING)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LinearProgressIndicator(progress = apiProgress.toFloat(), modifier = Modifier.fillMaxWidth())
                        Text("${translate("progress")}: ${(apiProgress * 100).toInt()}%") // Assuming progress is not localized or generic

                        Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(Color.LightGray.copy(alpha = 0.3f)).padding(4.dp)) {
                            LazyColumn {
                                items(apiLogs.takeLast(5)) { log ->
                                    Text(log, style = MaterialTheme.typography.caption)
                                }
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }

        if (showApiResultDialog) {
            AlertDialog(
                onDismissRequest = {
                    showApiResultDialog = false
                    viewModel.resetImportResult() // Reset result
                },
                title = {
                    Text(if (apiImportResult is AnimalListViewModel.ImportResult.Success) translate(General.SUCCESS) else translate(General.ERROR))
                },
                text = {
                    when (apiImportResult) {
                        is AnimalListViewModel.ImportResult.Success -> {
                            Column {
                                Text(translate(General.IMPORT_SUCCESS))
                                Text("${translate(General.TOTAL_ELEMENTS)} ${apiImportResult.count}")
                            }
                        }
                        is AnimalListViewModel.ImportResult.Error -> {
                            Text(apiImportResult.message)
                        }
                        null -> {}
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showApiResultDialog = false
                        viewModel.resetImportResult()
                    }) { Text("OK") }
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
