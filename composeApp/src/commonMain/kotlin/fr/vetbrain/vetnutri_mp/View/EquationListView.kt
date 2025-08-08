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
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.EquationViewModel

/**
 * Vue affichant la liste des équations disponibles
 *
 * @param viewModel ViewModel pour gérer les équations
 * @param onEditEquation Callback pour éditer une équation existante
 * @param onCreateEquation Callback pour créer une nouvelle équation
 * @param modifier Modifier à appliquer à la vue
 */
@Composable
fun EquationListView(
        viewModel: EquationViewModel,
        onEditEquation: (String) -> Unit,
        onCreateEquation: () -> Unit,
        modifier: Modifier = Modifier
) {
    val equations by viewModel.equations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()
    val searchQuery = remember { mutableStateOf("") }

    // Dialogue de confirmation pour la suppression
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var equationToDelete by remember { mutableStateOf<Equation?>(null) }

    // Effet pour charger les équations au lancement
    LaunchedEffect(Unit) { viewModel.loadEquations() }

    // Filtrage des équations en fonction de la recherche
    val filteredEquations =
            remember(equations, searchQuery.value) {
                if (searchQuery.value.isBlank()) {
                    equations
                } else {
                    val query = searchQuery.value.lowercase()
                    equations.filter { equation ->
                        equation.name.lowercase().contains(query) ||
                                equation.description.lowercase().contains(query) ||
                                equation.equationScript.lowercase().contains(query) ||
                                equation.kind.toString().lowercase().contains(query) ||
                                equation.specie.toString().lowercase().contains(query)
                    }
                }
            }

    Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                        onClick = onCreateEquation,
                        backgroundColor = VetNutriColors.Primary
                ) {
                    Icon(
                            imageVector = AppIcons.Add,
                            contentDescription = "Ajouter une équation",
                            tint = VetNutriColors.OnPrimary
                    )
                }
            }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            // Barre de recherche
            OutlinedTextField(
                    value = searchQuery.value,
                    onValueChange = { searchQuery.value = it },
                    label = { Text("Rechercher une équation") },
                    leadingIcon = {
                        Icon(imageVector = AppIcons.Search, contentDescription = "Rechercher")
                    },
                    trailingIcon = {
                        if (searchQuery.value.isNotEmpty()) {
                            IconButton(onClick = { searchQuery.value = "" }) {
                                Icon(imageVector = AppIcons.Close, contentDescription = "Effacer")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                            TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = VetNutriColors.Primary,
                                    unfocusedBorderColor = Color.Gray
                            )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Affichage des résultats
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VetNutriColors.Primary)
                }
            } else if (filteredEquations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                            text =
                                    if (searchQuery.value.isBlank()) "Aucune équation disponible"
                                    else "Aucune équation ne correspond à votre recherche",
                            style = MaterialTheme.typography.h6
                    )
                }
            } else {
                Text(
                        text = "Liste des équations (${filteredEquations.size})",
                        style = MaterialTheme.typography.h6
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredEquations) { equation ->
                        EquationCard(
                                equation = equation,
                                onEdit = { equation.uuid?.let { id -> onEditEquation(id) } },
                                onDelete = {
                                    equationToDelete = equation
                                    showDeleteConfirmation = true
                                }
                        )
                    }
                }
            }
        }
    }

    // Afficher l'erreur s'il y en a une
    val message = operationMessage
    if (message != null && message.isNotEmpty() && message.startsWith("Erreur")) {
        Snackbar(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearOperationMessage() }) { Text("Fermer") }
                }
        ) { Text(message) }
    }

    // Dialogue de confirmation de suppression
    if (showDeleteConfirmation) {
        AlertDialog(
                onDismissRequest = {
                    showDeleteConfirmation = false
                    equationToDelete = null
                },
                title = { Text("Confirmation de suppression") },
                text = {
                    Text(
                            "Êtes-vous sûr de vouloir supprimer l'équation '${equationToDelete?.name ?: ""}'?"
                    )
                },
                confirmButton = {
                    Button(
                            onClick = {
                                equationToDelete?.let { eq ->
                                    if (eq.uuid.isNotEmpty()) {
                                        viewModel.deleteEquationById(eq.uuid)
                                    }
                                }
                                showDeleteConfirmation = false
                                equationToDelete = null
                            },
                            colors =
                                    ButtonDefaults.buttonColors(
                                            backgroundColor = VetNutriColors.Error
                                    )
                    ) { Text("Supprimer", color = VetNutriColors.OnError) }
                },
                dismissButton = {
                    OutlinedButton(
                            onClick = {
                                showDeleteConfirmation = false
                                equationToDelete = null
                            }
                    ) { Text("Annuler") }
                }
        )
    }
}

/** Carte affichant une équation dans la liste */
@Composable
private fun EquationCard(
        equation: Equation,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
        modifier: Modifier = Modifier
) {
    Card(
            modifier = modifier.fillMaxWidth().clickable { onEdit() },
            elevation = 4.dp,
            backgroundColor =
                    if (equation.consistent) {
                        MaterialTheme.colors.surface
                    } else {
                        Color(0xFFFFEBEE) // Rouge très clair pour les équations non cohérentes
                    }
    ) {
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
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                                text = equation.name,
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold,
                                color =
                                        if (equation.consistent) {
                                            MaterialTheme.colors.onSurface
                                        } else {
                                            Color(
                                                    0xFFD32F2F
                                            ) // Rouge pour les équations non cohérentes
                                        }
                        )

                        // Indicateur visuel pour les équations non cohérentes
                        if (!equation.consistent) {
                            Icon(
                                    imageVector = AppIcons.Warning,
                                    contentDescription = "Équation non cohérente",
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Description
                    if (equation.description.isNotEmpty()) {
                        Text(
                                text = equation.description,
                                style = MaterialTheme.typography.body2,
                                color =
                                        if (equation.consistent) {
                                            MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                        } else {
                                            Color(0xFFD32F2F).copy(alpha = 0.7f)
                                        }
                        )
                    }

                    // Script de l'équation
                    Text(
                            text = "Script: ${equation.equationScript}",
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Medium
                    )

                    // Type et espèce
                    Text(
                            text = "Type: ${equation.kind} | Espèce: ${equation.specie}",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )

                    // Message d'avertissement pour les équations non cohérentes
                    if (!equation.consistent) {
                        Text(
                                text = "⚠️ Cette équation contient des variables non reconnues",
                                style = MaterialTheme.typography.caption,
                                color = Color(0xFFD32F2F)
                        )
                    }
                }

                // Boutons d'action
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                                imageVector = AppIcons.Edit,
                                contentDescription = "Modifier",
                                tint = VetNutriColors.Primary
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                                imageVector = AppIcons.Delete,
                                contentDescription = "Supprimer",
                                tint = VetNutriColors.Error
                        )
                    }
                }
            }
        }
    }
}
