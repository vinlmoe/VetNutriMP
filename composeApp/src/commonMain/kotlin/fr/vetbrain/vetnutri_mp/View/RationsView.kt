package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.*
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.ViewModel.RationsViewModel

enum class RationTab {
    RATIONS,
    CHAMPS,
    SEGMENT1,
    SEGMENT2,
    SEGMENT3;

    @Composable
    fun getTitle(): String {
        return when (this) {
            RATIONS -> "general.rations".translate()
            CHAMPS -> "general.fields".translate()
            SEGMENT1 -> "general.segment1".translate()
            SEGMENT2 -> "general.segment2".translate()
            SEGMENT3 -> "general.segment3".translate()
        }
    }
}

@Composable
fun RationsView(
        viewModel: RationsViewModel,
        onNavigateBack: () -> Unit,
        showAddDialog: Boolean = false,
        onAddDialogDismiss: () -> Unit = {}
) {
    val rations by viewModel.rations.collectAsState()
    val selectedRation by viewModel.selectedRation.collectAsState()
    var showEditDialog by remember { mutableStateOf<Ration?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Ration?>(null) }
    var showAddRationDialog by remember { mutableStateOf(false) }
    var showDeleteError by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(RationTab.RATIONS) }

    // Détection de la taille de l'écran
    val density = LocalDensity.current
    var screenWidth by remember { mutableStateOf(0.dp) }
    val isSmallScreen = screenWidth < AppSizes.breakpointWideScreen

    Column(
            modifier =
                    Modifier.fillMaxSize().padding(AppSizes.paddingMedium).onSizeChanged { size ->
                        screenWidth = with(density) { size.width.toDp() }
                    },
            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
    ) {
        // Bande supérieure avec 5 champs de texte
        if (!isSmallScreen) {
            Row(
                    modifier = Modifier.fillMaxWidth().height(AppSizes.buttonHeight),
                    horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                repeat(5) { index ->
                    OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            modifier = Modifier.weight(1f),
                            label = { Text("Champ ${index + 1}") },
                            singleLine = true
                    )
                }
            }
        }

        // TabRow pour les petits écrans
        if (isSmallScreen) {
            TabRow(
                    selectedTabIndex = RationTab.values().indexOf(selectedTab),
                    backgroundColor = MaterialTheme.colors.surface,
                    contentColor = MaterialTheme.colors.primary
            ) {
                RationTab.values().forEach { tab ->
                    Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = { Text(tab.getTitle()) }
                    )
                }
            }
        }

        // Contenu en fonction de la taille de l'écran
        if (isSmallScreen) {
            // Vue pour petits écrans avec onglets
            when (selectedTab) {
                RationTab.RATIONS ->
                        RationsContent(
                                rations = rations,
                                selectedRation = selectedRation,
                                onRationSelected = viewModel::selectRation,
                                onEditRation = { showEditDialog = it },
                                onDeleteRation = { showDeleteDialog = it }
                        )
                RationTab.CHAMPS -> FieldsContent()
                RationTab.SEGMENT1 -> SegmentContent("Segment 1")
                RationTab.SEGMENT2 -> SegmentContent("Segment 2")
                RationTab.SEGMENT3 -> SegmentContent("Segment 3")
            }
        } else {
            // Vue normale pour grands écrans
            Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
            ) {
                // Colonne gauche (2 segments)
                Column(
                        modifier = Modifier.weight(0.2f),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                    // Segment supérieur gauche (Liste des rations)
                    Surface(
                            modifier = Modifier.weight(1f),
                            elevation = AppSizes.elevationSmall,
                            shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // En-tête intégré
                            Surface(
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    color = MaterialTheme.colors.primary,
                                    shape =
                                            MaterialTheme.shapes.medium.copy(
                                                    bottomStart = CornerSize(0.dp),
                                                    bottomEnd = CornerSize(0.dp)
                                            )
                            ) {
                                Row(
                                        modifier =
                                                Modifier.fillMaxSize()
                                                        .padding(
                                                                horizontal = AppSizes.paddingMedium
                                                        ),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                            "Liste des rations",
                                            style =
                                                    MaterialTheme.typography.subtitle1.copy(
                                                            color = MaterialTheme.colors.onPrimary
                                                    )
                                    )
                                    IconButton(
                                            onClick = { showAddRationDialog = true },
                                            modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                                AppIcons.Add,
                                                contentDescription = "general.add".translate(),
                                                tint = MaterialTheme.colors.onPrimary,
                                                modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            // Liste des rations
                            LazyColumn(
                                    modifier =
                                            Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
                                    verticalArrangement =
                                            Arrangement.spacedBy(AppSizes.paddingSmall)
                            ) {
                                items(rations) { ration ->
                                    RationListItem(
                                            ration = ration,
                                            isSelected = selectedRation?.uuid == ration.uuid,
                                            onClick = { viewModel.selectRation(ration) },
                                            onEdit = { showEditDialog = it },
                                            onDelete = { showDeleteDialog = it }
                                    )
                                }
                            }
                        }
                    }

                    // Segment inférieur gauche
                    Surface(
                            modifier = Modifier.weight(1f),
                            elevation = AppSizes.elevationSmall,
                            shape = MaterialTheme.shapes.medium
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium)) {
                            Text("Segment inférieur gauche")
                        }
                    }
                }

                // Colonne droite (2 segments)
                Column(
                        modifier = Modifier.weight(0.8f),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                    // Segment supérieur droit
                    Surface(
                            modifier = Modifier.weight(1f),
                            elevation = AppSizes.elevationSmall,
                            shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Surface(
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    color = MaterialTheme.colors.primary,
                                    shape =
                                            MaterialTheme.shapes.medium.copy(
                                                    bottomStart = CornerSize(0.dp),
                                                    bottomEnd = CornerSize(0.dp)
                                            )
                            ) {
                                Row(
                                        modifier =
                                                Modifier.fillMaxSize()
                                                        .padding(
                                                                horizontal = AppSizes.paddingMedium
                                                        ),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                            "Liste des aliments",
                                            style =
                                                    MaterialTheme.typography.subtitle1.copy(
                                                            color = MaterialTheme.colors.onPrimary
                                                    )
                                    )
                                    IconButton(
                                            onClick = { showAddDialog = true },
                                            modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                                AppIcons.Add,
                                                contentDescription = "general.add".translate(),
                                                tint = MaterialTheme.colors.onPrimary,
                                                modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                            AlimentRationList(
                                    viewModel = viewModel,
                                    modifier =
                                            Modifier.fillMaxSize().padding(AppSizes.paddingMedium)
                            )
                        }
                    }

                    // Segment inférieur droit
                    Surface(
                            modifier = Modifier.weight(1f),
                            elevation = AppSizes.elevationSmall,
                            shape = MaterialTheme.shapes.medium
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium)) {
                            Text("Segment inférieur droit")
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddRationDialog) {
        RationEditDialog(
                ration = null,
                onDismiss = { showAddRationDialog = false },
                onSave = { ration ->
                    viewModel.addRation(ration)
                    showAddRationDialog = false
                }
        )
    }

    showEditDialog?.let { ration ->
        RationEditDialog(
                ration = ration,
                onDismiss = { showEditDialog = null },
                onSave = { updatedRation ->
                    viewModel.updateRation(updatedRation)
                    showEditDialog = null
                }
        )
    }

    showDeleteDialog?.let { ration ->
        if (rations.count { it.idConsult == ration.idConsult } <= 1) {
            showDeleteError = true
            showDeleteDialog = null
        } else {
            AlertDialog(
                    onDismissRequest = { showDeleteDialog = null },
                    title = { Text("general.delete".translate()) },
                    text = { Text("general.delete_confirm".translate()) },
                    confirmButton = {
                        Button(
                                onClick = {
                                    viewModel.deleteRation(ration)
                                    showDeleteDialog = null
                                }
                        ) { Text("general.delete".translate()) }
                    },
                    dismissButton = {
                        Button(onClick = { showDeleteDialog = null }) {
                            Text("general.cancel".translate())
                        }
                    }
            )
        }
    }

    if (showDeleteError) {
        AlertDialog(
                onDismissRequest = { showDeleteError = false },
                title = { Text("error.cannot_delete".translate()) },
                text = { Text("error.last_ration".translate()) },
                confirmButton = {
                    Button(onClick = { showDeleteError = false }) { Text("general.ok".translate()) }
                }
        )
    }
}

@Composable
private fun RationsContent(
        rations: List<Ration>,
        selectedRation: Ration?,
        onRationSelected: (Ration) -> Unit,
        onEditRation: (Ration) -> Unit,
        onDeleteRation: (Ration) -> Unit
) {
    Surface(
            modifier = Modifier.fillMaxSize(),
            elevation = AppSizes.elevationSmall,
            shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    color = MaterialTheme.colors.primary,
                    shape =
                            MaterialTheme.shapes.medium.copy(
                                    bottomStart = CornerSize(0.dp),
                                    bottomEnd = CornerSize(0.dp)
                            )
            ) {
                Row(
                        modifier =
                                Modifier.fillMaxSize().padding(horizontal = AppSizes.paddingMedium),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            "Liste des rations",
                            style =
                                    MaterialTheme.typography.subtitle1.copy(
                                            color = MaterialTheme.colors.onPrimary
                                    )
                    )
                    IconButton(
                            onClick = { /* TODO: Implement add ration */},
                            modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                                AppIcons.Add,
                                contentDescription = "general.add".translate(),
                                tint = MaterialTheme.colors.onPrimary,
                                modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                items(rations) { ration ->
                    RationListItem(
                            ration = ration,
                            isSelected = selectedRation?.uuid == ration.uuid,
                            onClick = { onRationSelected(ration) },
                            onEdit = onEditRation,
                            onDelete = onDeleteRation
                    )
                }
            }
        }
    }
}

@Composable
private fun FieldsContent() {
    Surface(
            modifier = Modifier.fillMaxSize(),
            elevation = AppSizes.elevationSmall,
            shape = MaterialTheme.shapes.medium
    ) {
        Column(
                modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
        ) {
            repeat(5) { index ->
                OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Champ ${index + 1}") },
                        singleLine = true
                )
            }
        }
    }
}

@Composable
private fun SegmentContent(title: String) {
    Surface(
            modifier = Modifier.fillMaxSize(),
            elevation = AppSizes.elevationSmall,
            shape = MaterialTheme.shapes.medium
    ) { Box(modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium)) { Text(title) } }
}

@Composable
private fun RationListItem(
        ration: Ration,
        isSelected: Boolean,
        onClick: () -> Unit,
        onEdit: (Ration) -> Unit,
        onDelete: (Ration) -> Unit
) {
    var showDeleteError by remember { mutableStateOf(false) }

    Card(
            modifier =
                    Modifier.fillMaxWidth()
                            .padding(vertical = AppSizes.paddingXXSmall)
                            .clickable(onClick = onClick),
            elevation = AppSizes.elevationSmall,
            backgroundColor =
                    if (isSelected) MaterialTheme.colors.secondary else MaterialTheme.colors.surface
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSizes.paddingXXSmall)) {
                Text(
                        text = ration.name,
                        style = MaterialTheme.typography.subtitle1,
                        color =
                                if (isSelected) MaterialTheme.colors.onSecondary
                                else MaterialTheme.colors.onSurface
                )
                Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            text =
                                    "general.coefficient".translate() +
                                            ": " +
                                            ration.coef.toString(),
                            style = MaterialTheme.typography.caption,
                            color =
                                    if (isSelected) MaterialTheme.colors.onSecondary
                                    else MaterialTheme.colors.onSurface
                    )
                    if (ration.actual) {
                        Text(
                                text = "ration.actual".translate(),
                                style = MaterialTheme.typography.caption,
                                color =
                                        if (isSelected) MaterialTheme.colors.onSecondary
                                        else MaterialTheme.colors.primary
                        )
                    }
                }
            }
            Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingXXSmall),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                        onClick = { onEdit(ration) },
                        modifier = Modifier.size(AppSizes.buttonHeight * 0.75f)
                ) {
                    Icon(
                            AppIcons.Edit,
                            contentDescription = "general.edit".translate(),
                            tint =
                                    if (isSelected) MaterialTheme.colors.onSecondary
                                    else MaterialTheme.colors.onSurface,
                            modifier = Modifier.size(AppSizes.iconSizeSmall)
                    )
                }
                IconButton(
                        onClick = { onDelete(ration) },
                        modifier = Modifier.size(AppSizes.buttonHeight * 0.75f)
                ) {
                    Icon(
                            AppIcons.Delete,
                            contentDescription = "general.delete".translate(),
                            tint =
                                    if (isSelected) MaterialTheme.colors.onSecondary
                                    else MaterialTheme.colors.onSurface,
                            modifier = Modifier.size(AppSizes.iconSizeSmall)
                    )
                }
            }
        }
    }

    if (showDeleteError) {
        AlertDialog(
                onDismissRequest = { showDeleteError = false },
                title = { Text("error.cannot_delete".translate()) },
                text = { Text("error.last_ration".translate()) },
                confirmButton = {
                    Button(onClick = { showDeleteError = false }) { Text("general.ok".translate()) }
                }
        )
    }
}

@Composable
private fun RationEditDialog(ration: Ration?, onDismiss: () -> Unit, onSave: (Ration) -> Unit) {
    var name by remember { mutableStateOf(ration?.name ?: "") }
    var coef by remember { mutableStateOf(ration?.coef?.toString() ?: "") }
    var description by remember { mutableStateOf(ration?.description ?: "") }
    var actual by remember { mutableStateOf(ration?.actual ?: false) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                        if (ration == null) "general.add_ration".translate()
                        else "general.edit_ration".translate()
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("general.name".translate()) },
                            modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                    OutlinedTextField(
                            value = coef,
                            onValueChange = {
                                coef = it
                                isError = it.toFloatOrNull() == null
                            },
                            label = { Text("general.coefficient".translate()) },
                            isError = isError,
                            modifier = Modifier.fillMaxWidth()
                    )
                    if (isError) {
                        Text(
                                text = "error.invalidValue".translate(),
                                color = MaterialTheme.colors.error,
                                style = MaterialTheme.typography.caption
                        )
                    }
                    Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                    OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("general.description".translate()) },
                            modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(checked = actual, onCheckedChange = { actual = it })
                        Text("ration.actual".translate())
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = {
                            val coefValue = coef.toFloatOrNull()
                            if (coefValue != null) {
                                val newRation =
                                        (ration ?: Ration()).copy(
                                                name = name,
                                                coef = coefValue,
                                                description = description,
                                                actual = actual
                                        )
                                onSave(newRation)
                            }
                        },
                        enabled = !isError && coef.toFloatOrNull() != null
                ) { Text("general.save".translate()) }
            },
            dismissButton = { Button(onClick = onDismiss) { Text("general.cancel".translate()) } }
    )
}

@Composable
private fun AlimentRationList(viewModel: RationsViewModel, modifier: Modifier = Modifier) {
    val alimentRations by viewModel.alimentRations.collectAsState()
    val selectedRation by viewModel.selectedRation.collectAsState()
    var showEditDialog by remember { mutableStateOf<AlimentRation?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showNoRationError by remember { mutableStateOf(false) }

    LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
    ) {
        items(alimentRations) { alimentRation ->
            AlimentRationListItem(
                    alimentRation = alimentRation,
                    onEdit = { showEditDialog = it },
                    onDelete = { viewModel.deleteAlimentRation(it) }
            )
        }
    }

    // Dialogs
    if (showAddDialog) {
        if (selectedRation != null) {
            AlimentRationEditDialog(
                    alimentRation = null,
                    aliments = viewModel.aliments.value,
                    onDismiss = { showAddDialog = false },
                    onSave = { newAlimentRation ->
                        viewModel.addAlimentRation(
                                newAlimentRation.copy(refRation = selectedRation.uuid)
                        )
                        showAddDialog = false
                    }
            )
        } else {
            showNoRationError = true
            showAddDialog = false
        }
    }

    showEditDialog?.let { alimentRation ->
        AlimentRationEditDialog(
                alimentRation = alimentRation,
                aliments = viewModel.aliments.value,
                onDismiss = { showEditDialog = null },
                onSave = { updatedAlimentRation ->
                    viewModel.updateAlimentRation(updatedAlimentRation)
                    showEditDialog = null
                }
        )
    }

    if (showNoRationError) {
        AlertDialog(
                onDismissRequest = { showNoRationError = false },
                title = { Text("error.no_ration_selected".translate()) },
                text = { Text("error.select_ration_first".translate()) },
                confirmButton = {
                    Button(onClick = { showNoRationError = false }) {
                        Text("general.ok".translate())
                    }
                }
        )
    }
}

@Composable
private fun AlimentRationListItem(
        alimentRation: AlimentRation,
        onEdit: (AlimentRation) -> Unit,
        onDelete: (AlimentRation) -> Unit
) {
    Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = AppSizes.paddingXXSmall),
            elevation = AppSizes.elevationSmall
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSizes.paddingXXSmall)) {
                Text(
                        text = alimentRation.alim?.nom ?: "Aliment inconnu",
                        style = MaterialTheme.typography.subtitle1
                )
                Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            text = "Quantité: ${String.format("%.2f", alimentRation.quantity)}",
                            style = MaterialTheme.typography.caption
                    )
                }
            }
            Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingXXSmall),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                        onClick = { onEdit(alimentRation) },
                        modifier = Modifier.size(AppSizes.buttonHeight * 0.75f)
                ) {
                    Icon(
                            AppIcons.Edit,
                            contentDescription = "general.edit".translate(),
                            modifier = Modifier.size(AppSizes.iconSizeSmall)
                    )
                }
                IconButton(
                        onClick = { onDelete(alimentRation) },
                        modifier = Modifier.size(AppSizes.buttonHeight * 0.75f)
                ) {
                    Icon(
                            AppIcons.Delete,
                            contentDescription = "general.delete".translate(),
                            modifier = Modifier.size(AppSizes.iconSizeSmall)
                    )
                }
            }
        }
    }
}

@Composable
private fun AlimentRationEditDialog(
        alimentRation: AlimentRation?,
        aliments: List<AlimentEv>,
        onDismiss: () -> Unit,
        onSave: (AlimentRation) -> Unit
) {
    var selectedAliment by remember { mutableStateOf(alimentRation?.alim) }
    var quantity by remember { mutableStateOf(alimentRation?.quantity?.toString() ?: "0.0") }
    var isError by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                        if (alimentRation == null) "general.add_aliment".translate()
                        else "general.edit_aliment".translate()
                )
            },
            text = {
                Column {
                    // Sélection de l'aliment
                    OutlinedTextField(
                            value = selectedAliment?.nom ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("general.aliment".translate()) },
                            modifier = Modifier.fillMaxWidth().clickable { expanded = true }
                    )
                    DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        aliments.forEach { aliment ->
                            DropdownMenuItem(
                                    onClick = {
                                        selectedAliment = aliment
                                        expanded = false
                                    }
                            ) { Text(aliment.nom) }
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                    // Quantité
                    OutlinedTextField(
                            value = quantity,
                            onValueChange = {
                                quantity = it
                                isError = it.toDoubleOrNull() == null
                            },
                            label = { Text("general.quantity".translate()) },
                            isError = isError,
                            modifier = Modifier.fillMaxWidth()
                    )
                    if (isError) {
                        Text(
                                text = "error.invalidValue".translate(),
                                color = MaterialTheme.colors.error,
                                style = MaterialTheme.typography.caption
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = {
                            val quantityValue = quantity.toDoubleOrNull()
                            val aliment = selectedAliment
                            if (aliment != null && quantityValue != null) {
                                val newAlimentRation =
                                        (alimentRation ?: AlimentRation()).copy(
                                                refAlimUnif = aliment.uuid,
                                                quantity = quantityValue.toFloat(),
                                                alim = aliment
                                        )
                                onSave(newAlimentRation)
                            }
                        },
                        enabled =
                                !isError &&
                                        quantity.toDoubleOrNull() != null &&
                                        selectedAliment != null
                ) { Text("general.save".translate()) }
            },
            dismissButton = { Button(onClick = onDismiss) { Text("general.cancel".translate()) } }
    )
}
