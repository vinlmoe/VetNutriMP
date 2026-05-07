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
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.ReferenceEvViewModel
import kotlinx.coroutines.launch

/**
 * Vue des besoins nutritionnels. Cette vue affiche la liste des références évaluées permettant
 * d'accéder à l'édition des besoins nutritionnels.
 *
 * @param viewModel ViewModel des références évaluées
 * @param onEditReference Callback pour éditer une référence
 * @param onCreateReference Callback pour créer une nouvelle référence
 * @param onEditNutrients Callback pour éditer les besoins nutritionnels d'une référence
 * @param modifier Modifier à appliquer à la vue
 */
@Composable
fun NutrientRequirementView(
        viewModel: ReferenceEvViewModel,
        onEditReference: (String) -> Unit,
        onCreateReference: () -> Unit,
        onEditNutrients: (String) -> Unit,
        modifier: Modifier = Modifier
) {
    val allReferences by viewModel.allReferences.collectAsState(initial = emptyList())
    val loading by viewModel.loading.collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()
    val searchQuery = remember { mutableStateOf("") }

    // État pour la référence à supprimer
    var refToDelete by remember { mutableStateOf<ReferenceEv?>(null) }

    // Charger les références au démarrage
    LaunchedEffect(Unit) { viewModel.loadAllReferences() }

    // Filtrer les références en fonction de la recherche
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
    ) { paddingValues ->
        Column(modifier = modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            // Barre de recherche
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
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
            )
            // Titre et description

            Spacer(modifier = Modifier.height(16.dp))

            // Liste des références
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
                        text = "Références disponibles (${filteredReferences.size})",
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

        // Dialogue de confirmation pour la suppression
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
        onEdit: () -> Unit,
        onDelete: () -> Unit,
        onDuplicate: () -> Unit,
        modifier: Modifier = Modifier
) {
    val missingEquationLabels = remember(reference) { reference.getMissingEquationLabels() }

    Card(modifier = modifier.fillMaxWidth().clickable { onEdit() }, elevation = 4.dp) {
        Column(
                modifier = Modifier.padding(16.dp),
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
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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

                // Boutons d'action
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
