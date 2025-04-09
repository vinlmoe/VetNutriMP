package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import fr.vetbrain.vetnutri_mp.Components.ConfirmDialog
import fr.vetbrain.vetnutri_mp.Components.TopBarWithActions
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.EquationViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.ReferenceEvViewModel

/**
 * Écran de gestion des équations pour une référence nutritionnelle
 *
 * @param viewModel ViewModel pour les références nutritionnelles
 * @param equationViewModel ViewModel pour les équations
 * @param referenceId ID de la référence affichée
 * @param onNavigateBack Callback pour naviguer en arrière
 * @param onEditEquation Callback pour éditer une équation
 * @param onCreateEquation Callback pour créer une nouvelle équation
 */
@Composable
fun ReferenceEvEquationView(
        viewModel: ReferenceEvViewModel,
        equationViewModel: EquationViewModel,
        referenceId: String,
        onNavigateBack: () -> Unit,
        onEditEquation: (String) -> Unit = {},
        onCreateEquation: () -> Unit = {},
        modifier: Modifier = Modifier
) {
    val currentReferenceEv by viewModel.currentReferenceEv.collectAsState(initial = ReferenceEv())
    val equations by equationViewModel.equations.collectAsState(initial = emptyList())
    val isLoading by viewModel.loading.collectAsState(initial = false)
    val equationLoading by equationViewModel.isLoading.collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    // État pour la confirmation de suppression
    val (showDeleteConfirm, setShowDeleteConfirm) = remember { mutableStateOf(false) }
    // Équation à supprimer
    val (equationToDelete, setEquationToDelete) = remember { mutableStateOf<Equation?>(null) }

    // Charger les équations associées à la référence au chargement
    LaunchedEffect(referenceId) {
        viewModel.loadReferenceEvById(referenceId)
        equationViewModel.loadEquations()
    }

    Scaffold(
            topBar = {
                TopBarWithActions(
                        title = "Équations pour ${currentReferenceEv.nom}",
                        onNavigateBack = onNavigateBack,
                        actions = {
                            IconButton(onClick = onCreateEquation) {
                                Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Ajouter une équation",
                                        tint = MaterialTheme.colors.onPrimary
                                )
                            }
                        }
                )
            }
    ) { paddingValues ->
        Box(
                modifier =
                        modifier.fillMaxSize()
                                .padding(paddingValues)
                                .padding(horizontal = AppSizes.paddingMedium)
        ) {
            if (equations.isEmpty() && !isLoading) {
                // Affichage du message quand il n'y a pas d'équations
                Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                            "Aucune équation disponible pour cette référence",
                            style = MaterialTheme.typography.h6
                    )
                    Spacer(modifier = Modifier.height(AppSizes.paddingMedium))
                    Button(onClick = onCreateEquation) { Text("Ajouter une équation") }
                }
            } else {
                // Liste des équations
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(equations) { equation ->
                        EquationItem(
                                equation = equation,
                                onEdit = { onEditEquation(equation.uuid) },
                                onDelete = {
                                    setEquationToDelete(equation)
                                    setShowDeleteConfirm(true)
                                },
                                onAssociate = {
                                    // Associer l'équation à la référence (à implémenter)
                                    // viewModel.associateEquation(referenceId, equation.uuid)
                                },
                                isAssociated =
                                        currentReferenceEv.obtenirToutesEquations().any {
                                            it.uuid == equation.uuid
                                        }
                        )
                    }
                }
            }

            // Indicateur de chargement
            if (isLoading) {
                CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = VetNutriColors.Primary
                )
            }

            // Dialogue de confirmation de suppression
            if (showDeleteConfirm) {
                ConfirmDialog(
                        title = "Supprimer l'équation",
                        message =
                                "Êtes-vous sûr de vouloir supprimer l'équation '${equationToDelete?.name}'?",
                        onConfirm = {
                            equationToDelete?.let {
                                equationViewModel.deleteEquation()
                                // Retirer aussi les associations avec la référence
                                // viewModel.removeEquationAssociation(referenceEv.uuid, it.uuid)
                            }
                            setShowDeleteConfirm(false)
                        },
                        onDismiss = { setShowDeleteConfirm(false) }
                )
            }
        }
    }
}

/** Élément représentant une équation dans la liste */
@Composable
private fun EquationItem(
        equation: Equation,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
        onAssociate: () -> Unit,
        isAssociated: Boolean
) {
    Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = AppSizes.paddingSmall),
            elevation = AppSizes.cardElevationNormal
    ) {
        Column(
                modifier =
                        Modifier.fillMaxWidth()
                                .clickable(onClick = onEdit)
                                .padding(AppSizes.paddingMedium)
        ) {
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
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Éditer")
                }
            }

            Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

            Text(text = "Type: ${equation.kind}", style = MaterialTheme.typography.body2)

            if (equation.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                Text(text = equation.description, style = MaterialTheme.typography.body2)
            }

            Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

            // Boutons d'action
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                // Bouton pour associer/dissocier l'équation à la référence
                Button(
                        onClick = onAssociate,
                        colors =
                                ButtonDefaults.buttonColors(
                                        backgroundColor =
                                                if (isAssociated) MaterialTheme.colors.primary
                                                else MaterialTheme.colors.surface
                                )
                ) { Text(if (isAssociated) "Associée" else "Associer") }

                Spacer(modifier = Modifier.width(AppSizes.paddingSmall))

                // Bouton pour supprimer l'équation
                OutlinedButton(onClick = onDelete) { Text("Supprimer") }
            }
        }
    }
}
