package fr.vetbrain.vetnutri_mp.View

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
 * Vue affichant la liste des équations disponibles
 *
 * @param viewModel ViewModel pour gérer les équations
 * @param onNavigateBack Callback pour revenir à l'écran précédent
 * @param onEditEquation Callback pour éditer une équation existante
 * @param onCreateEquation Callback pour créer une nouvelle équation
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
    val operationMessage by viewModel.operationMessage.collectAsState()

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
                    Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
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
                                    IconButton(onClick = { onEditEquation(equation.uuid) }) {
                                        Icon(
                                                imageVector = AppIcons.Edit,
                                                contentDescription = "Modifier",
                                                tint = VetNutriColors.Primary
                                        )
                                    }

                                    IconButton(
                                            onClick = {
                                                equationToDelete = equation
                                                showDeleteConfirmation = true
                                            }
                                    ) {
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
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Afficher les erreurs éventuelles
    if (operationMessage.isNotEmpty() && operationMessage.startsWith("Erreur")) {
        AlertDialog(
                onDismissRequest = { viewModel.clearOperationMessage() },
                title = { Text("Erreur") },
                text = { Text(operationMessage) },
                confirmButton = {
                    Button(onClick = { viewModel.clearOperationMessage() }) { Text("OK") }
                }
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
                                    viewModel.loadEquationById(it.uuid)
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
