package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.StadePhysio
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.ReferenceEvViewModel
import fr.vetbrain.vetnutri_mp.Utils.isIosPlatform
import kotlinx.coroutines.launch

@Composable
fun NutrientRequirementView(
        viewModel: ReferenceEvViewModel,
        onEditReference: (String) -> Unit,
        onCreateReference: () -> Unit,
        onEditNutrients: (String) -> Unit,
        onBulkEdit: (List<String>) -> Unit = {},
        modifier: Modifier = Modifier
) {
    val allReferences by viewModel.allReferences.collectAsState(initial = emptyList())
    val loading by viewModel.loading.collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()
    val searchQuery = remember { mutableStateOf("") }

    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<String>() }
    var filterEspece by remember { mutableStateOf<Espece?>(null) }
    var filterStade by remember { mutableStateOf<StadePhysio?>(null) }
    var showEspeceDropdown by remember { mutableStateOf(false) }
    var showStadeDropdown by remember { mutableStateOf(false) }

    var refToDelete by remember { mutableStateOf<ReferenceEv?>(null) }

    LaunchedEffect(Unit) { viewModel.loadAllReferences() }

    // Auto-sélection par filtre
    LaunchedEffect(filterEspece, filterStade, isSelectionMode) {
        if (!isSelectionMode) return@LaunchedEffect
        allReferences.forEach { ref ->
            val matchEspece = filterEspece == null || ref.espece == filterEspece
            val matchStade = filterStade == null || ref.stadePhysio == filterStade
            if (matchEspece && matchStade && ref.uuid !in selectedIds) {
                selectedIds.add(ref.uuid)
            }
        }
    }

    val filteredReferences =
            remember(allReferences, searchQuery.value) {
                if (searchQuery.value.isBlank()) {
                    allReferences
                } else {
                    val query = searchQuery.value.lowercase()
                    allReferences.filter { reference ->
                        reference.nom.lowercase().contains(query) ||
                                reference.espece.toString().lowercase().contains(query) ||
                                reference.stadePhysio.toString().lowercase().contains(query) ||
                                (reference.maladie &&
                                        reference.nomMaladie.lowercase().contains(query))
                    }
                }
            }

    Scaffold(
            floatingActionButton = {
                if (isSelectionMode && selectedIds.isNotEmpty()) {
                    ExtendedFloatingActionButton(
                            text = {
                                Text(
                                        "Éditer les ${selectedIds.size} sélectionnée${if (selectedIds.size > 1) "s" else ""}"
                                )
                            },
                            onClick = { onBulkEdit(selectedIds.toList()) },
                            backgroundColor = VetNutriColors.Primary,
                            contentColor = VetNutriColors.OnPrimary
                    )
                } else {
                    FloatingActionButton(
                            onClick = onCreateReference,
                            backgroundColor = VetNutriColors.Primary
                    ) {
                        Icon(
                                imageVector = AppIcons.Add,
                                contentDescription = "Ajouter une référence",
                                tint = VetNutriColors.OnPrimary
                        )
                    }
                }
            }
    ) { paddingValues ->
        Column(modifier = modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {

            // Barre de recherche + bouton mode sélection
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                        value = searchQuery.value,
                        onValueChange = { searchQuery.value = it },
                        label = { Text("Rechercher une référence...") },
                        leadingIcon = {
                            Icon(imageVector = AppIcons.Search, contentDescription = "Rechercher")
                        },
                        trailingIcon = {
                            if (searchQuery.value.isNotEmpty()) {
                                IconButton(onClick = { searchQuery.value = "" }) {
                                    Icon(
                                            imageVector = AppIcons.Close,
                                            contentDescription = "Effacer la recherche"
                                    )
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                        onClick = {
                            isSelectionMode = !isSelectionMode
                            if (!isSelectionMode) {
                                selectedIds.clear()
                                filterEspece = null
                                filterStade = null
                            }
                        },
                        colors =
                                ButtonDefaults.outlinedButtonColors(
                                        contentColor =
                                                if (isSelectionMode) VetNutriColors.Primary
                                                else Color.Gray
                                )
                ) {
                    Text(if (isSelectionMode) "Annuler" else "Sélectionner")
                }
            }

            // Filtres espèce / stade (visibles uniquement en mode sélection)
            if (isSelectionMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filtrer :", style = MaterialTheme.typography.body2, color = Color.Gray)

                    // Filtre espèce
                    Box {
                        OutlinedButton(
                                onClick = { showEspeceDropdown = true },
                                modifier = Modifier.widthIn(min = 100.dp)
                        ) {
                            Text(filterEspece?.label ?: "Espèce", maxLines = 1)
                        }
                        DropdownMenu(
                                expanded = showEspeceDropdown,
                                onDismissRequest = {
                                    if (!isIosPlatform) showEspeceDropdown = false
                                }
                        ) {
                            DropdownMenuItem(
                                    onClick = {
                                        filterEspece = null
                                        showEspeceDropdown = false
                                    }
                            ) {
                                Text("Toutes")
                            }
                            Espece.valuesExcept(Espece.CH).forEach { espece ->
                                DropdownMenuItem(
                                        onClick = {
                                            filterEspece = espece
                                            showEspeceDropdown = false
                                        }
                                ) {
                                    Text(espece.label)
                                }
                            }
                        }
                    }

                    // Filtre stade physiologique
                    Box {
                        OutlinedButton(
                                onClick = { showStadeDropdown = true },
                                modifier = Modifier.widthIn(min = 100.dp)
                        ) {
                            Text(filterStade?.label ?: "Stade", maxLines = 1)
                        }
                        DropdownMenu(
                                expanded = showStadeDropdown,
                                onDismissRequest = {
                                    if (!isIosPlatform) showStadeDropdown = false
                                }
                        ) {
                            DropdownMenuItem(
                                    onClick = {
                                        filterStade = null
                                        showStadeDropdown = false
                                    }
                            ) {
                                Text("Tous")
                            }
                            StadePhysio.values().forEach { stade ->
                                DropdownMenuItem(
                                        onClick = {
                                            filterStade = stade
                                            showStadeDropdown = false
                                        }
                                ) {
                                    Text(stade.label)
                                }
                            }
                        }
                    }

                    // Effacer sélection
                    if (selectedIds.isNotEmpty()) {
                        TextButton(onClick = { selectedIds.clear() }) {
                            Text("Tout désélectionner")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VetNutriColors.Primary)
                }
            } else if (filteredReferences.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                            text =
                                    if (searchQuery.value.isNotEmpty())
                                            "Aucune référence trouvée pour \"${searchQuery.value}\""
                                    else "Aucune référence disponible",
                            style = MaterialTheme.typography.h6
                    )
                }
            } else {
                Text(
                        text =
                                if (isSelectionMode && selectedIds.isNotEmpty())
                                        "Références disponibles (${filteredReferences.size}) — ${selectedIds.size} sélectionnée${if (selectedIds.size > 1) "s" else ""}"
                                else "Références disponibles (${filteredReferences.size})",
                        style = MaterialTheme.typography.h6
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredReferences) { reference ->
                        ReferenceNutrientCard(
                                reference = reference,
                                isSelectionMode = isSelectionMode,
                                isSelected = reference.uuid in selectedIds,
                                onToggleSelect = {
                                    if (reference.uuid in selectedIds) {
                                        selectedIds.remove(reference.uuid)
                                    } else {
                                        selectedIds.add(reference.uuid)
                                    }
                                },
                                onEdit = { onEditReference(reference.uuid) },
                                onDelete = { refToDelete = reference },
                                onDuplicate = {
                                    coroutineScope.launch {
                                        viewModel.duplicateReference(reference)
                                    }
                                }
                        )
                    }
                }
            }
        }

        refToDelete?.let { reference ->
            AlertDialog(
                    onDismissRequest = { refToDelete = null },
                    title = { Text("Confirmer la suppression") },
                    text = {
                        Text(
                                "Êtes-vous sûr de vouloir supprimer la référence \"${reference.nom}\" ?"
                        )
                    },
                    confirmButton = {
                        Button(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.deleteReference(reference.uuid)
                                    }
                                    refToDelete = null
                                },
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = MaterialTheme.colors.error
                                        )
                        ) { Text("Supprimer", color = Color.White) }
                    },
                    dismissButton = { Button(onClick = { refToDelete = null }) { Text("Annuler") } }
            )
        }
    }
}

@Composable
private fun ReferenceNutrientCard(
        reference: ReferenceEv,
        isSelectionMode: Boolean,
        isSelected: Boolean,
        onToggleSelect: () -> Unit,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
        onDuplicate: () -> Unit,
        modifier: Modifier = Modifier
) {
    val missingEquationLabels = remember(reference) { reference.getMissingEquationLabels() }

    Card(
            modifier =
                    modifier.fillMaxWidth().clickable {
                        if (isSelectionMode) onToggleSelect() else onEdit()
                    },
            elevation = 4.dp,
            backgroundColor =
                    if (isSelected) VetNutriColors.Primary.copy(alpha = 0.08f)
                    else MaterialTheme.colors.surface
    ) {
        Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox en mode sélection
            if (isSelectionMode) {
                Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect() },
                        colors =
                                CheckboxDefaults.colors(
                                        checkedColor = VetNutriColors.Primary
                                ),
                        modifier = Modifier.padding(end = 8.dp)
                )
            }

            Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                                text = reference.nom,
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold
                        )

                        if (missingEquationLabels.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                    color = VetNutriColors.Error.copy(alpha = 0.12f),
                                    contentColor = VetNutriColors.Error,
                                    shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                        text =
                                                "${missingEquationLabels.size} ${translate(LocalizationKeys.NewReference.NOT_DEFINED)}",
                                        modifier =
                                                Modifier.padding(
                                                        horizontal = 8.dp,
                                                        vertical = 4.dp
                                                ),
                                        style = MaterialTheme.typography.caption,
                                        fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Text(
                                text = "Espèce: ${reference.espece}",
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )

                        Text(
                                text = "Stade: ${reference.stadePhysio}",
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )

                        if (reference.maladie) {
                            Text(
                                    text = "Maladie: ${reference.nomMaladie}",
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.error
                            )
                        }

                        if (missingEquationLabels.isNotEmpty()) {
                            Text(
                                    text =
                                            "${translate(LocalizationKeys.NewReference.NOT_DEFINED)}: ${missingEquationLabels.joinToString()}",
                                    style = MaterialTheme.typography.caption,
                                    color = VetNutriColors.Error
                            )
                        }
                    }

                    // Boutons d'action (masqués en mode sélection pour ne pas gêner le tap)
                    if (!isSelectionMode) {
                        Row {
                            IconButton(onClick = onEdit) {
                                Icon(
                                        imageVector = AppIcons.Edit,
                                        contentDescription = "Éditer la référence",
                                        tint = VetNutriColors.Primary
                                )
                            }

                            IconButton(onClick = onDuplicate) {
                                Icon(
                                        imageVector = AppIcons.ContentCopy,
                                        contentDescription = "Dupliquer la référence",
                                        tint = VetNutriColors.Primary
                                )
                            }

                            IconButton(onClick = onDelete) {
                                Icon(
                                        imageVector = AppIcons.Delete,
                                        contentDescription = "Supprimer la référence",
                                        tint = VetNutriColors.Error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun ReferenceEv.getMissingEquationLabels(): List<String> {
    if (maladie) return emptyList()

    return buildList {
        if (equationBW?.equationScript.isNullOrBlank()) {
            add(translate(LocalizationKeys.NewReference.EQ_METABOLIC_WEIGHT))
        }
        if (equationBEE?.equationScript.isNullOrBlank()) {
            add(translate(LocalizationKeys.NewReference.EQ_BEE))
        }
        if (equationDEcom?.equationScript.isNullOrBlank()) {
            add(translate(LocalizationKeys.NewReference.EQ_DECOM))
        }
        if (equationDEraw?.equationScript.isNullOrBlank()) {
            add(translate(LocalizationKeys.NewReference.EQ_DERAW))
        }
    }
}
