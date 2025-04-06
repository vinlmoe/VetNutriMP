package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.EquationViewModel

/**
 * Vue pour afficher la liste des équations
 *
 * @param viewModel Le ViewModel gérant les données des équations
 * @param onNavigateBack Callback appelé pour retourner à la vue précédente
 * @param onEditEquation Callback appelé lorsqu'une équation est sélectionnée pour édition
 * @param onCreateEquation Callback appelé lorsque l'utilisateur souhaite créer une nouvelle
 * équation
 * @param modifier Modifier à appliquer à la vue
 */
@Composable
fun EquationListView(
        viewModel: EquationViewModel,
        onNavigateBack: () -> Unit,
        onEditEquation: (String) -> Unit,
        onCreateEquation: () -> Unit,
        modifier: Modifier = Modifier
) {
    val equations by viewModel.equations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Dialogue de confirmation pour la suppression
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var equationToDelete by remember { mutableStateOf<Equation?>(null) }

    // Effet pour charger les équations au lancement
    LaunchedEffect(Unit) { viewModel.loadEquations() }

    Scaffold(
            topBar = {
                TopBarSimple(
                        title = "Liste des équations",
                        onNavigateBack = onNavigateBack,
                        actions = {
                            IconButton(onClick = onCreateEquation) {
                                Icon(
                                        imageVector = AppIcons.Add,
                                        contentDescription = "Ajouter une équation",
                                        tint = VetNutriColors.OnPrimary
                                )
                            }
                        }
                )
            },
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
        if (isLoading) {
            Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = VetNutriColors.Primary) }
        } else if (equations.isEmpty()) {
            Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
            ) { Text(text = "Aucune équation disponible", style = MaterialTheme.typography.h6) }
        } else {
            LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(16.dp)
            ) {
                items(equations) { equation ->
                    EquationItem(
                            equation = equation,
                            onEditClick = { onEditEquation(equation.uuid) },
                            onDeleteClick = {
                                equationToDelete = equation
                                showDeleteConfirmation = true
                            }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Afficher les erreurs éventuelles
    if (errorMessage != null) {
        AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text("Erreur") },
                text = { Text(errorMessage ?: "") },
                confirmButton = { Button(onClick = { viewModel.clearError() }) { Text("OK") } }
        )
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
                                equationToDelete?.let {
                                    viewModel.loadEquation(it.uuid)
                                    viewModel.deleteEquation()
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

/**
 * Composant affichant une équation dans la liste
 *
 * @param equation L'équation à afficher
 * @param onEditClick Callback appelé lorsque l'utilisateur souhaite éditer l'équation
 * @param onDeleteClick Callback appelé lorsque l'utilisateur souhaite supprimer l'équation
 */
@Composable
private fun EquationItem(equation: Equation, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onEditClick), elevation = 4.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = equation.name,
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                )

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                                imageVector = AppIcons.Edit,
                                contentDescription = "Modifier",
                                tint = VetNutriColors.Primary
                        )
                    }

                    IconButton(onClick = onDeleteClick) {
                        Icon(
                                imageVector = AppIcons.Delete,
                                contentDescription = "Supprimer",
                                tint = VetNutriColors.Error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Type: ${equation.kind.getNom()}", style = MaterialTheme.typography.body1)

            Text(
                    text = "Espèce: ${equation.specie?.name ?: "Non spécifié"}",
                    style = MaterialTheme.typography.body1
            )

            if (equation.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = equation.description, style = MaterialTheme.typography.body2)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .background(VetNutriColors.Surface.copy(alpha = 0.5f))
                                    .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = "Formule: ${equation.equationScript}",
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
