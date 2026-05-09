package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Enumer.MainNutrientEnum
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.ExcelPlatform.isCsvFileOperationsSupported
import fr.vetbrain.vetnutri_mp.ExcelPlatform.saveCsvFileForExport
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.isIosPlatform
import fr.vetbrain.vetnutri_mp.ViewModel.BULK_EDITABLE_CATEGORIES
import fr.vetbrain.vetnutri_mp.ViewModel.BulkReferenceEditorViewModel

private val NUTRIENT_COL_WIDTH = 170.dp
private val REF_COL_WIDTH = 130.dp
private val LEVELS = Reflevel.values().toList()

private val COLOR_EDITED = Color(0xFFFFF9C4)    // jaune clair
private val COLOR_ERROR = Color(0xFFFFEBEE)     // rouge clair
private val COLOR_BORDER_EDITED = Color(0xFFFFB300) // ambre
private val COLOR_BORDER_ERROR = Color(0xFFB00020)  // rouge

@Composable
fun BulkReferenceEditorView(
        viewModel: BulkReferenceEditorViewModel,
        onNavigateBack: () -> Unit,
        modifier: Modifier = Modifier
) {
    val references by viewModel.references.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedLevel by viewModel.selectedLevel.collectAsState()
    val editedValues by viewModel.editedValues.collectAsState()
    val copyVersion by viewModel.copyVersion.collectAsState()
    val consistencyErrors by viewModel.consistencyErrors.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    val nutrients = remember(selectedCategory) { viewModel.getNutrientsForCategory(selectedCategory) }
    val hScroll = rememberScrollState()

    // F3 — filtre local sur le nom du nutriment
    var nutrientFilter by remember { mutableStateOf("") }
    LaunchedEffect(selectedCategory) { nutrientFilter = "" }
    val filteredNutrients = remember(nutrients, nutrientFilter) {
        if (nutrientFilter.isBlank()) nutrients
        else nutrients.filter { it.label.lowercase().contains(nutrientFilter.lowercase()) }
    }

    // F1 — UUID de la colonne source pour le dropdown de copie
    var copySourceId by remember { mutableStateOf<String?>(null) }

    // F5 — dialog confirmation sauvegarde avec erreurs
    var showSaveWithErrorsDialog by remember { mutableStateOf(false) }

    Scaffold(
            topBar = {
                TopBarSimple(
                        title = "Édition groupée (${references.size} référence${if (references.size > 1) "s" else ""})",
                        onNavigateBack = onNavigateBack
                )
            }
    ) { paddingValues ->
        if (loading) {
            Box(
                    modifier = modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = VetNutriColors.Primary)
            }
            return@Scaffold
        }

        Column(modifier = modifier.fillMaxSize().padding(paddingValues)) {

            // Erreur technique
            if (error.isNotBlank()) {
                Text(
                        text = error,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.caption
                )
            }

            // F5 — Bandeau d'incohérences
            if (consistencyErrors.isNotEmpty()) {
                Surface(
                        color = VetNutriColors.Error.copy(alpha = 0.10f),
                        modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                                imageVector = AppIcons.Warning,
                                contentDescription = null,
                                tint = VetNutriColors.Error,
                                modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                                text = "${consistencyErrors.size} incohérence${if (consistencyErrors.size > 1) "s" else ""} détectée${if (consistencyErrors.size > 1) "s" else ""}",
                                color = VetNutriColors.Error,
                                style = MaterialTheme.typography.caption,
                                modifier = Modifier.weight(1f)
                        )
                        IconButton(
                                onClick = { viewModel.clearConsistencyErrors() },
                                modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                    imageVector = AppIcons.Close,
                                    contentDescription = "Fermer",
                                    tint = VetNutriColors.Error,
                                    modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // --- Onglets catégories ---
            val categoryIndex = BULK_EDITABLE_CATEGORIES.indexOf(selectedCategory).coerceAtLeast(0)
            ScrollableTabRow(
                    selectedTabIndex = categoryIndex,
                    backgroundColor = MaterialTheme.colors.surface,
                    contentColor = VetNutriColors.Primary,
                    edgePadding = 0.dp
            ) {
                BULK_EDITABLE_CATEGORIES.forEachIndexed { idx, cat ->
                    Tab(
                            selected = idx == categoryIndex,
                            onClick = { viewModel.selectCategory(cat) },
                            text = { Text(cat.label, maxLines = 1) }
                    )
                }
            }

            // --- Onglets niveaux ---
            val levelIndex = LEVELS.indexOf(selectedLevel).coerceAtLeast(0)
            TabRow(
                    selectedTabIndex = levelIndex,
                    backgroundColor = MaterialTheme.colors.surface,
                    contentColor = VetNutriColors.Primary
            ) {
                LEVELS.forEachIndexed { idx, level ->
                    Tab(
                            selected = idx == levelIndex,
                            onClick = { viewModel.selectLevel(level) },
                            text = { Text(level.name) }
                    )
                }
            }

            // F3 — Filtre nutriments
            OutlinedTextField(
                    value = nutrientFilter,
                    onValueChange = { nutrientFilter = it },
                    placeholder = { Text("Filtrer les nutriments…", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(imageVector = AppIcons.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    trailingIcon = {
                        if (nutrientFilter.isNotEmpty()) {
                            IconButton(onClick = { nutrientFilter = "" }, modifier = Modifier.size(20.dp)) {
                                Icon(imageVector = AppIcons.Close, contentDescription = "Effacer", modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = VetNutriColors.Primary,
                            unfocusedBorderColor = Color.LightGray
                    )
            )

            // --- En-tête du tableau ---
            Row(
                    modifier = Modifier.fillMaxWidth()
                            .background(MaterialTheme.colors.surface)
                            .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = "Nutriment",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.width(NUTRIENT_COL_WIDTH).padding(start = 8.dp)
                )
                Row(modifier = Modifier.horizontalScroll(hScroll)) {
                    references.forEach { ref ->
                        // F1 — En-tête colonne avec bouton copie
                        Box(modifier = Modifier.width(REF_COL_WIDTH)) {
                            Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                        text = ref.nom,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                )
                                if (references.size > 1) {
                                    Box {
                                        IconButton(
                                                onClick = {
                                                    copySourceId =
                                                            if (copySourceId == ref.uuid) null
                                                            else ref.uuid
                                                },
                                                modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                    imageVector = AppIcons.ContentCopy,
                                                    contentDescription = "Copier colonne",
                                                    tint = VetNutriColors.Primary,
                                                    modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        DropdownMenu(
                                                expanded = copySourceId == ref.uuid,
                                                onDismissRequest = {
                                                    if (!isIosPlatform) copySourceId = null
                                                }
                                        ) {
                                            Text(
                                                    text = "Copier « ${ref.nom} » vers :",
                                                    style = MaterialTheme.typography.caption,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                                    color = Color.Gray
                                            )
                                            references.filter { it.uuid != ref.uuid }.forEach { target ->
                                                DropdownMenuItem(
                                                        onClick = {
                                                            viewModel.copyColumn(ref.uuid, target.uuid)
                                                            copySourceId = null
                                                        }
                                                ) {
                                                    Text("→ ${target.nom}")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Divider()

            // --- Lignes de données ---
            if (filteredNutrients.isEmpty()) {
                Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                ) {
                    Text(
                            if (nutrientFilter.isNotEmpty()) "Aucun nutriment correspondant à « $nutrientFilter »"
                            else "Aucun nutriment pour cette catégorie",
                            color = Color.Gray,
                            style = MaterialTheme.typography.body2
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(filteredNutrients, key = { it.label }) { nutrient ->
                        Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Colonne fixe — nom du nutriment
                            Text(
                                    text = nutrient.label,
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.width(NUTRIENT_COL_WIDTH).padding(start = 8.dp)
                            )

                            // Colonnes défilantes
                            Row(modifier = Modifier.horizontalScroll(hScroll)) {
                                references.forEach { ref ->
                                    val editKey = "${ref.uuid}||${nutrient.label}||${selectedLevel.name}"
                                    val isEdited = editKey in editedValues
                                    val errorMsg = viewModel.hasConsistencyError(ref.uuid, nutrient.label)
                                    val hasError = errorMsg != null

                                    // F2 — état local (fluidité frappe) + réinitialisation sur copie
                                    var cellValue by remember(
                                            ref.uuid,
                                            nutrient.label,
                                            selectedLevel,
                                            selectedCategory,
                                            copyVersion
                                    ) {
                                        mutableStateOf(viewModel.getCellValue(ref.uuid, nutrient.label))
                                    }

                                    Box(
                                            modifier = Modifier.width(REF_COL_WIDTH)
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    .background(
                                                            when {
                                                                hasError -> COLOR_ERROR
                                                                isEdited -> COLOR_EDITED
                                                                else -> Color.Transparent
                                                            }
                                                    )
                                    ) {
                                        OutlinedTextField(
                                                value = cellValue,
                                                onValueChange = { newVal ->
                                                    cellValue = newVal
                                                    viewModel.updateCell(ref.uuid, nutrient.label, newVal)
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true,
                                                isError = hasError,
                                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                                        focusedBorderColor = when {
                                                            hasError -> COLOR_BORDER_ERROR
                                                            else -> VetNutriColors.Primary
                                                        },
                                                        unfocusedBorderColor = when {
                                                            hasError -> COLOR_BORDER_ERROR
                                                            isEdited -> COLOR_BORDER_EDITED
                                                            else -> Color.Gray
                                                        }
                                                )
                                        )
                                    }
                                }
                            }
                        }
                        Divider(color = Color.LightGray, thickness = 0.5.dp)
                    }
                }
            }

            Divider()

            // --- Barre d'actions ---
            Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                // F5 — Bouton valider cohérence
                OutlinedButton(
                        onClick = { viewModel.validateConsistency() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VetNutriColors.Error)
                ) {
                    Icon(
                            imageVector = AppIcons.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp).padding(end = 4.dp)
                    )
                    Text("Valider", fontSize = 12.sp)
                }

                // F4 — Export CSV
                if (isCsvFileOperationsSupported()) {
                    OutlinedButton(
                            onClick = {
                                val csv = viewModel.generateCsv()
                                saveCsvFileForExport(csv, "references_export.csv")
                            }
                    ) {
                        Text("Exporter CSV", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(onClick = onNavigateBack) { Text("Annuler") }

                Button(
                        onClick = {
                            if (consistencyErrors.isNotEmpty()) {
                                showSaveWithErrorsDialog = true
                            } else {
                                viewModel.saveAll()
                                onNavigateBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                                backgroundColor = VetNutriColors.Primary,
                                contentColor = VetNutriColors.OnPrimary
                        )
                ) {
                    Text("Enregistrer tout")
                }
            }
        }

        // F5 — Dialog confirmation sauvegarde avec incohérences
        if (showSaveWithErrorsDialog) {
            AlertDialog(
                    onDismissRequest = { showSaveWithErrorsDialog = false },
                    title = { Text("Incohérences détectées") },
                    text = {
                        Text(
                                "${consistencyErrors.size} incohérence${if (consistencyErrors.size > 1) "s" else ""} ont été détectée${if (consistencyErrors.size > 1) "s" else ""}. " +
                                        "Voulez-vous enregistrer quand même ?"
                        )
                    },
                    confirmButton = {
                        Button(
                                onClick = {
                                    showSaveWithErrorsDialog = false
                                    viewModel.saveAll()
                                    onNavigateBack()
                                },
                                colors = ButtonDefaults.buttonColors(
                                        backgroundColor = VetNutriColors.Primary
                                )
                        ) { Text("Enregistrer quand même", color = VetNutriColors.OnPrimary) }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showSaveWithErrorsDialog = false }) {
                            Text("Annuler")
                        }
                    }
            )
        }
    }
}
