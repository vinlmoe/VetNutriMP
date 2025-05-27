package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.AlimentItem
import fr.vetbrain.vetnutri_mp.Components.CenteredMessage
import fr.vetbrain.vetnutri_mp.Components.RationItem
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel

/**
 * Vue pour afficher les rations d'un animal
 *
 * @param viewModel ViewModel contenant les données de l'animal
 * @param showSnackbar Action à exécuter pour afficher un message snackbar
 */
@Composable
fun RationsView(viewModel: AnimalDetailViewModel, showSnackbar: (String) -> Unit) {
        val animal by viewModel.animal.collectAsState()
        val selectedConsultation by viewModel.selectedConsultation.collectAsState()
        val selectedRation by viewModel.selectedRation.collectAsState()

        // État pour la répartition de l'espace entre les colonnes (valeur entre 0.2f et 0.8f)
        var repartitionColonnes by remember { mutableStateOf(0.5f) }

        // États pour les dialogues
        var showRationEditDialog by remember { mutableStateOf(false) }
        var showAddAlimentDialog by remember { mutableStateOf(false) }
        var rationToEdit by remember { mutableStateOf<Ration?>(null) }
        var editingAlimentId by remember { mutableStateOf<String?>(null) }

        Column(
                modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
                Text(
                        text = "Rations",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = AppSizes.paddingSmall)
                )

                if (selectedConsultation == null) {
                        Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                        ) { Text("Sélectionnez une consultation pour voir les rations") }
                } else {
                        // En-tête avec informations de consultation et ration
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = AppSizes.elevationSmall
                        ) {
                                Row(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .padding(AppSizes.paddingMedium),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                                selectedConsultation?.date?.let { date ->
                                                        Text(
                                                                text = "Consultation du $date",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .subtitle1
                                                        )
                                                }
                                                selectedRation?.let { ration ->
                                                        Text(
                                                                text =
                                                                        "${ration.name} (${if (ration.actual) "Proposée" else "Actuelle"})",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .subtitle2,
                                                                fontWeight = FontWeight.Bold
                                                        )
                                                }
                                        }

                                        // Contrôle de répartition de l'espace
                                        Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier =
                                                        Modifier.width(
                                                                AppSizes.iconSizeXLarge.times(3f)
                                                        )
                                        ) {
                                                Text(
                                                        text = "Répartition",
                                                        style = MaterialTheme.typography.caption,
                                                        color = Color.Gray
                                                )
                                                Slider(
                                                        value = repartitionColonnes,
                                                        onValueChange = {
                                                                repartitionColonnes = it
                                                        },
                                                        valueRange = 0.2f..0.8f,
                                                        steps = 5,
                                                        colors =
                                                                SliderDefaults.colors(
                                                                        thumbColor =
                                                                                VetNutriColors
                                                                                        .Primary,
                                                                        activeTrackColor =
                                                                                VetNutriColors
                                                                                        .Primary,
                                                                        inactiveTrackColor =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.3f
                                                                                        )
                                                                )
                                                )
                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement =
                                                                Arrangement.SpaceBetween
                                                ) {
                                                        Text(
                                                                text = "Listes",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption,
                                                                color = Color.Gray
                                                        )
                                                        Text(
                                                                text = "Analyse",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption,
                                                                color = Color.Gray
                                                        )
                                                }
                                        }
                                }
                        }

                        // Contenu principal - grille 2x2 de cartes
                        Box(modifier = Modifier.weight(1f)) {
                                Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingMedium)
                                ) {
                                        // Colonne gauche (listes) - poids dynamique basé sur
                                        // repartitionColonnes
                                        Column(
                                                modifier = Modifier.weight(repartitionColonnes),
                                                verticalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingMedium)
                                        ) {
                                                // Segment 2: Liste des rations de la consultation
                                                Card(
                                                        modifier =
                                                                Modifier.weight(1f).fillMaxWidth(),
                                                        elevation = AppSizes.elevationMedium,
                                                        backgroundColor =
                                                                MaterialTheme.colors.surface
                                                ) {
                                                        Column(
                                                                modifier =
                                                                        Modifier.fillMaxSize()
                                                                                .padding(
                                                                                        AppSizes.paddingMedium
                                                                                ),
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        ) {
                                                                // En-tête avec titre et bouton
                                                                // d'ajout
                                                                Row(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth(),
                                                                        horizontalArrangement =
                                                                                Arrangement
                                                                                        .SpaceBetween,
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically
                                                                ) {
                                                                        Text(
                                                                                text =
                                                                                        "Rations de la consultation",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .h6,
                                                                                color =
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        )

                                                                        // Bouton pour ajouter une
                                                                        // nouvelle ration
                                                                        IconButton(
                                                                                onClick = {
                                                                                        rationToEdit =
                                                                                                null // Nouvelle ration
                                                                                        showRationEditDialog =
                                                                                                true
                                                                                },
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                AppSizes.iconSizeMedium
                                                                                        )
                                                                        ) {
                                                                                Icon(
                                                                                        Icons.Filled
                                                                                                .Add,
                                                                                        contentDescription =
                                                                                                "Ajouter une ration",
                                                                                        tint =
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                )
                                                                        }
                                                                }

                                                                Divider()

                                                                if (selectedConsultation?.rations
                                                                                .isNullOrEmpty()
                                                                ) {
                                                                        CenteredMessage(
                                                                                message =
                                                                                        "Aucune ration disponible",
                                                                                modifier =
                                                                                        Modifier.weight(
                                                                                                1f
                                                                                        )
                                                                        )
                                                                } else {
                                                                        LazyColumn(
                                                                                modifier =
                                                                                        Modifier.weight(
                                                                                                1f
                                                                                        ),
                                                                                verticalArrangement =
                                                                                        Arrangement
                                                                                                .spacedBy(
                                                                                                        8.dp
                                                                                                )
                                                                        ) {
                                                                                items(
                                                                                        selectedConsultation
                                                                                                ?.rations
                                                                                                ?: emptyList()
                                                                                ) { ration ->
                                                                                        RationItem(
                                                                                                ration =
                                                                                                        ration,
                                                                                                isSelected =
                                                                                                        ration.uuid ==
                                                                                                                selectedRation
                                                                                                                        ?.uuid,
                                                                                                onClick = {
                                                                                                        viewModel
                                                                                                                .selectRation(
                                                                                                                        ration
                                                                                                                )
                                                                                                },
                                                                                                onEdit = {
                                                                                                        rationToEdit =
                                                                                                                ration
                                                                                                        showRationEditDialog =
                                                                                                                true
                                                                                                },
                                                                                                onDuplicate = {
                                                                                                        viewModel
                                                                                                                .duplicateRation(
                                                                                                                        ration
                                                                                                                )
                                                                                                        showSnackbar(
                                                                                                                "Ration '${ration.name}' dupliquée"
                                                                                                        )
                                                                                                },
                                                                                                onDelete = {
                                                                                                        // Temporairement commenté jusqu'à l'implémentation de cette méthode
                                                                                                        showSnackbar(
                                                                                                                "Suppression de ration non implémentée"
                                                                                                        )
                                                                                                        // viewModel.deleteRation(ration)
                                                                                                }
                                                                                        )
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }

                                                // Segment 3: Liste des aliments de la ration
                                                // sélectionnée
                                                Card(
                                                        modifier =
                                                                Modifier.weight(1f).fillMaxWidth(),
                                                        elevation = AppSizes.elevationMedium,
                                                        backgroundColor =
                                                                MaterialTheme.colors.surface
                                                ) {
                                                        Column(
                                                                modifier =
                                                                        Modifier.fillMaxSize()
                                                                                .padding(
                                                                                        AppSizes.paddingMedium
                                                                                ),
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        ) {
                                                                // En-tête avec titre et bouton
                                                                // d'ajout
                                                                Row(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth(),
                                                                        horizontalArrangement =
                                                                                Arrangement
                                                                                        .SpaceBetween,
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically
                                                                ) {
                                                                        Text(
                                                                                text =
                                                                                        "Aliments de la ration",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .h6,
                                                                                color =
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        )

                                                                        // Bouton pour ajouter un
                                                                        // aliment
                                                                        IconButton(
                                                                                onClick = {
                                                                                        // Vérifier
                                                                                        // que la
                                                                                        // ration
                                                                                        // existe
                                                                                        // avant
                                                                                        // d'afficher le dialogue
                                                                                        if (selectedRation !=
                                                                                                        null
                                                                                        ) {
                                                                                                showAddAlimentDialog =
                                                                                                        true
                                                                                        } else {
                                                                                                showSnackbar(
                                                                                                        "Sélectionnez d'abord une ration"
                                                                                                )
                                                                                        }
                                                                                },
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                AppSizes.iconSizeMedium
                                                                                        )
                                                                        ) {
                                                                                Icon(
                                                                                        imageVector =
                                                                                                Icons.Filled
                                                                                                        .Add,
                                                                                        contentDescription =
                                                                                                "Ajouter un aliment",
                                                                                        tint =
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                )
                                                                        }
                                                                }

                                                                Divider()

                                                                if (selectedRation
                                                                                ?.alimentMutableList
                                                                                .isNullOrEmpty()
                                                                ) {
                                                                        // Message plus explicite et
                                                                        // vérification que la liste
                                                                        // est vide
                                                                        CenteredMessage(
                                                                                message =
                                                                                        "Aucun aliment dans cette ration",
                                                                                modifier =
                                                                                        Modifier.weight(
                                                                                                1f
                                                                                        )
                                                                        )
                                                                } else {
                                                                        LazyColumn(
                                                                                modifier =
                                                                                        Modifier.weight(
                                                                                                1f
                                                                                        ),
                                                                                verticalArrangement =
                                                                                        Arrangement
                                                                                                .spacedBy(
                                                                                                        AppSizes.paddingSmall
                                                                                                )
                                                                        ) {
                                                                                items(
                                                                                        selectedRation
                                                                                                ?.alimentMutableList
                                                                                                ?: emptyList()
                                                                                ) { aliment ->
                                                                                        AlimentItem(
                                                                                                aliment =
                                                                                                        aliment,
                                                                                                isEditing =
                                                                                                        editingAlimentId ==
                                                                                                                aliment.uuid,
                                                                                                onStartEditing = {
                                                                                                        // Si une autre édition est en cours, valider cette édition d'abord
                                                                                                        if (editingAlimentId !=
                                                                                                                        null &&
                                                                                                                        editingAlimentId !=
                                                                                                                                aliment.uuid
                                                                                                        ) {
                                                                                                                editingAlimentId =
                                                                                                                        null
                                                                                                        }
                                                                                                        editingAlimentId =
                                                                                                                aliment.uuid
                                                                                                },
                                                                                                onQuantityChange = {
                                                                                                        newQuantity
                                                                                                        ->
                                                                                                        viewModel
                                                                                                                .updateAlimentQuantity(
                                                                                                                        aliment.uuid,
                                                                                                                        newQuantity
                                                                                                                )
                                                                                                },
                                                                                                onFinishEditing = {
                                                                                                        editingAlimentId =
                                                                                                                null
                                                                                                },
                                                                                                onDelete = {
                                                                                                        // Utilisation de l'UUID au lieu de l'objet
                                                                                                        viewModel
                                                                                                                .removeAlimentFromRation(
                                                                                                                        aliment.uuid
                                                                                                                )
                                                                                                }
                                                                                        )
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        }

                                        // Colonne droite (analyses) - poids dynamique basé sur (1 -
                                        // repartitionColonnes)
                                        Column(
                                                modifier = Modifier.weight(1 - repartitionColonnes),
                                                verticalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingMedium)
                                        ) {
                                                // TODO: Ajouter les composants d'analyse ici quand
                                                // nécessaire
                                                // Pour l'instant, ajoutons juste un placeholder
                                                Card(
                                                        modifier = Modifier.fillMaxSize(),
                                                        elevation = AppSizes.elevationMedium
                                                ) {
                                                        Box(
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentAlignment = Alignment.Center
                                                        ) { Text("Fonctionnalités d'analyse") }
                                                }
                                        }
                                }
                        }

                        // TODO: Réimplémentez les dialogues d'édition ici quand nécessaire
                        
                        // Afficher le dialogue d'édition de ration si nécessaire
                        if (showRationEditDialog) {
                            RationEditDialog(
                                ration = rationToEdit,
                                onDismiss = { 
                                    showRationEditDialog = false
                                    rationToEdit = null
                                },
                                onSave = { updatedRation ->
                                    if (rationToEdit == null) {
                                        // Création d'une nouvelle ration
                                        // Assurer que la ration est liée à la consultation
                                        val newRation = updatedRation.copy(
                                            idConsult = selectedConsultation?.uuid ?: ""
                                        )
                                        
                                        selectedConsultation?.let { consultation ->
                                            // Créer une copie de la liste des rations et y ajouter la nouvelle ration
                                            val updatedRations = consultation.rations.toMutableList()
                                            updatedRations.add(newRation)
                                            
                                            // Mettre à jour la consultation avec la nouvelle liste de rations
                                            val updatedConsultation = consultation.copy(rations = updatedRations)
                                            
                                            // Sauvegarder la consultation mise à jour
                                            viewModel.updateConsultation(updatedConsultation)
                                            
                                            // Sélectionner la nouvelle ration
                                            viewModel.selectRation(newRation)
                                            
                                            showSnackbar("Ration '${newRation.name}' créée")
                                        }
                                    } else {
                                        // Mise à jour d'une ration existante
                                        viewModel.updateRation(updatedRation)
                                        showSnackbar("Ration '${updatedRation.name}' mise à jour")
                                    }
                                    
                                    showRationEditDialog = false
                                    rationToEdit = null
                                }
                            )
                        }
                        
                        // Afficher le dialogue d'ajout d'aliment si nécessaire
                        if (showAddAlimentDialog) {
                            selectedRation?.let { ration ->
                                AddAlimentDialog(
                                    viewModel = viewModel,
                                    onDismiss = { showAddAlimentDialog = false },
                                    onAddAliment = { aliment, quantite ->
                                        // Ajouter l'aliment à la ration
                                        viewModel.addAlimentToRation(ration, aliment, quantite)
                                        showSnackbar("Aliment '${aliment.nom}' ajouté à la ration (${quantite}g)")
                                        showAddAlimentDialog = false
                                    }
                                )
                            }
                        }
                
                        // Ajouter ici d'autres dialogues si nécessaire
                }
        }
}

/**
 * Dialogue pour créer ou éditer une ration
 *
 * @param ration Ration à éditer, null pour une nouvelle ration
 * @param onDismiss Action à exécuter pour fermer le dialogue
 * @param onSave Action à exécuter pour sauvegarder la ration
 */
@Composable
fun RationEditDialog(
    ration: Ration?,
    onDismiss: () -> Unit,
    onSave: (Ration) -> Unit
) {
    val isNewRation = ration == null
    val title = if (isNewRation) "Créer une ration" else "Modifier la ration"
    
    // État éditable de la ration
    var editedRation by remember {
        mutableStateOf(
            ration?.copy() ?: Ration(
                name = "Nouvelle ration",
                actual = false,
                alimentMutableList = mutableListOf()
            )
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.h6) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = AppSizes.paddingSmall),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
            ) {
                // Nom de la ration
                OutlinedTextField(
                    value = editedRation.name,
                    onValueChange = { editedRation = editedRation.copy(name = it) },
                    label = { Text("Nom de la ration") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Type de ration (actuelle ou proposée)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Type de ration:", modifier = Modifier.weight(1f))
                    
                    Switch(
                        checked = editedRation.actual,
                        onCheckedChange = { checked ->
                            editedRation = editedRation.copy(actual = checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VetNutriColors.Primary,
                            checkedTrackColor = VetNutriColors.Primary.copy(alpha = 0.5f)
                        )
                    )
                    
                    Text(
                        text = if (editedRation.actual) "Actuelle" else "Proposée",
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(start = AppSizes.paddingSmall)
                    )
                }
                
                // Description de la ration
                OutlinedTextField(
                    value = editedRation.description,
                    onValueChange = { editedRation = editedRation.copy(description = it) },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    singleLine = false
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(editedRation) },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = VetNutriColors.Primary,
                    contentColor = VetNutriColors.OnPrimary
                )
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

/**
 * Dialogue pour ajouter un aliment à une ration
 *
 * @param viewModel ViewModel contenant les données de l'animal et des aliments
 * @param onDismiss Action à exécuter pour fermer le dialogue
 * @param onAddAliment Action à exécuter pour ajouter un aliment
 */
@Composable
fun AddAlimentDialog(
    viewModel: AnimalDetailViewModel,
    onDismiss: () -> Unit,
    onAddAliment: (AlimentEv, Float) -> Unit
) {
    // État pour la recherche
    var searchQuery by remember { mutableStateOf("") }
    // État pour la quantité
    var quantite by remember { mutableStateOf("100") }
    // État pour l'aliment sélectionné
    var selectedAliment by remember { mutableStateOf<AlimentEv?>(null) }
    // État d'erreur pour la quantité
    var quantiteError by remember { mutableStateOf(false) }
    
    // Charger les aliments au premier affichage du dialogue
    LaunchedEffect(Unit) {
        viewModel.loadAvailableFoods()
    }
    
    // Filtrer les aliments selon la recherche
    val filteredFoods = viewModel.getFilteredFoods(searchQuery)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un aliment", style = MaterialTheme.typography.h6) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = AppSizes.paddingSmall),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
            ) {
                // Champ de recherche
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Rechercher un aliment") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Liste des aliments filtrés
                if (viewModel.isLoadingFoods) {
                    Box(
                        modifier = Modifier.height(200.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (filteredFoods.isEmpty()) {
                    Box(
                        modifier = Modifier.height(200.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Aucun aliment trouvé")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.height(200.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                    ) {
                        items(filteredFoods) { aliment ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { selectedAliment = aliment },
                                elevation = AppSizes.elevationSmall,
                                backgroundColor = if (selectedAliment?.uuid == aliment.uuid) 
                                    VetNutriColors.Primary.copy(alpha = 0.1f) 
                                else 
                                    MaterialTheme.colors.surface
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingSmall),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = aliment.nom ?: "Sans nom",
                                            style = MaterialTheme.typography.subtitle1,
                                            fontWeight = FontWeight.Bold
                                        )
                                        aliment.brand?.let { brand ->
                                            Text(
                                                text = brand,
                                                style = MaterialTheme.typography.caption
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Champ de quantité
                OutlinedTextField(
                    value = quantite,
                    onValueChange = { 
                        quantite = it
                        // Vérifier que la valeur est un nombre valide
                        quantiteError = try {
                            it.toFloat() <= 0
                        } catch (e: NumberFormatException) {
                            true
                        }
                    },
                    label = { Text("Quantité (g)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = quantiteError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                if (quantiteError) {
                    Text(
                        text = "Veuillez entrer une quantité valide > 0",
                        color = Color.Red,
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    // Vérifier que tous les champs sont valides
                    val aliment = selectedAliment
                    if (aliment != null && !quantiteError) {
                        try {
                            val quantiteValue = quantite.toFloat()
                            onAddAliment(aliment, quantiteValue)
                        } catch (e: NumberFormatException) {
                            // Si la conversion échoue, ne rien faire
                        }
                    }
                },
                enabled = selectedAliment != null && !quantiteError,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = VetNutriColors.Primary,
                    contentColor = VetNutriColors.OnPrimary
                )
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
