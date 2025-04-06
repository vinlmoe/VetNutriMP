package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.AppTextField
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.EquationViewModel

/**
 * Vue pour l'édition d'une équation
 *
 * @param viewModel Le ViewModel pour la gestion des équations
 * @param equationId L'identifiant de l'équation à éditer (null pour une nouvelle équation)
 * @param onNavigateBack Callback pour revenir à la vue précédente
 * @param modifier Modifier à appliquer à la vue
 */
@Composable
fun EquationEditView(
        viewModel: EquationViewModel,
        equationId: String?,
        onNavigateBack: () -> Unit,
        modifier: Modifier = Modifier
) {
    val equation by viewModel.currentEquation.collectAsState()
    val biblioRefs by viewModel.biblioRefs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Variable pour savoir si c'est une nouvelle équation
    val isNewEquation = equationId == null

    // Effet pour charger l'équation ou initialiser une nouvelle équation
    LaunchedEffect(equationId) { viewModel.loadEquation(equationId) }

    // Effet pour charger les références bibliographiques
    LaunchedEffect(Unit) { viewModel.loadBiblioRefs() }

    Column(modifier = modifier.fillMaxSize()) {
        TopBarSimple(
                title = if (isNewEquation) "Nouvelle équation" else "Éditer l'équation",
                onNavigateBack = onNavigateBack,
                actions = {
                    // Bouton pour sauvegarder l'équation
                    IconButton(
                            onClick = {
                                viewModel.saveEquation()
                                onNavigateBack()
                            }
                    ) {
                        Icon(
                                imageVector = AppIcons.Save,
                                contentDescription = "Sauvegarder",
                                tint = VetNutriColors.OnPrimary
                        )
                    }
                }
        )

        // Afficher le chargement
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VetNutriColors.Primary)
            }
        } else if (equation == null) {
            // Si aucune équation n'est chargée
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Aucune équation trouvée", style = MaterialTheme.typography.h6)
            }
        } else {
            // Formulaire d'édition
            val scrollState = rememberScrollState()

            Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState)) {
                // Nom de l'équation
                AppTextField(
                        value = equation?.name ?: "",
                        onValueChange = { viewModel.updateEquationField("name", it) },
                        label = "Nom de l'équation",
                        modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                AppTextField(
                        value = equation?.description ?: "",
                        onValueChange = { viewModel.updateEquationField("description", it) },
                        label = "Description",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Script de l'équation
                AppTextField(
                        value = equation?.equationScript ?: "",
                        onValueChange = { viewModel.updateEquationField("equationScript", it) },
                        label = "Formule mathématique",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Type d'équation (EquationKind)
                Column {
                    Text(
                            "Type d'équation",
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Liste déroulante pour le type d'équation
                    var equationTypeExpanded by remember { mutableStateOf(false) }

                    OutlinedButton(
                            onClick = { equationTypeExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                    ) { Text(equation?.kind?.getNom() ?: "Sélectionner un type") }

                    DropdownMenu(
                            expanded = equationTypeExpanded,
                            onDismissRequest = { equationTypeExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        EquationKind.values().forEach { kind ->
                            DropdownMenuItem(
                                    onClick = {
                                        viewModel.updateEquationField("kind", kind)
                                        equationTypeExpanded = false
                                    }
                            ) { Text(kind.getNom()) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Espèce (Espece)
                Column {
                    Text(
                            "Espèce",
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Liste déroulante pour l'espèce
                    var especeExpanded by remember { mutableStateOf(false) }

                    OutlinedButton(
                            onClick = { especeExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                    ) { Text(equation?.specie?.name ?: "Sélectionner une espèce") }

                    DropdownMenu(
                            expanded = especeExpanded,
                            onDismissRequest = { especeExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Espece.values().forEach { espece ->
                            DropdownMenuItem(
                                    onClick = {
                                        viewModel.updateEquationField("specie", espece)
                                        especeExpanded = false
                                    }
                            ) { Text(espece.name) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Référence bibliographique (version simplifiée avec combobox)
                Column {
                    Text(
                            "Référence bibliographique",
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Liste déroulante simplifiée pour les références
                    var biblioRefExpanded by remember { mutableStateOf(false) }

                    OutlinedButton(
                            onClick = { biblioRefExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        val currentRef = equation?.bib
                        val displayText =
                                if (currentRef != null && currentRef.firstAuthor.isNotBlank()) {
                                    "${currentRef.firstAuthor}, ${currentRef.year}"
                                } else {
                                    "Sélectionner une référence"
                                }
                        Text(displayText)
                    }

                    DropdownMenu(
                            expanded = biblioRefExpanded,
                            onDismissRequest = { biblioRefExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        biblioRefs.forEach { ref ->
                            DropdownMenuItem(
                                    onClick = {
                                        viewModel.updateEquationField("biblioRef", ref)
                                        biblioRefExpanded = false
                                    }
                            ) { Text("${ref.firstAuthor}, ${ref.year} - ${ref.completeRef}") }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Boutons d'action en bas
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Bouton Annuler
                    OutlinedButton(onClick = onNavigateBack, modifier = Modifier.weight(1f)) {
                        Text("Annuler")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Bouton Sauvegarder
                    Button(
                            onClick = {
                                viewModel.saveEquation()
                                onNavigateBack()
                            },
                            modifier = Modifier.weight(1f),
                            colors =
                                    ButtonDefaults.buttonColors(
                                            backgroundColor = VetNutriColors.Primary
                                    )
                    ) { Text("Sauvegarder", color = VetNutriColors.OnPrimary) }
                }
            }
        }
    }

    // Dialogue d'erreur
    if (errorMessage != null) {
        AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text("Erreur") },
                text = { Text(errorMessage ?: "") },
                confirmButton = { Button(onClick = { viewModel.clearError() }) { Text("OK") } }
        )
    }
}
