package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.BulkAddBiblioToFoodsViewModel

/**
 * Module d'ajout en masse de bibliographie aux aliments, accessible depuis Settings.
 * Réutilise le sélecteur multi-aliments existant ([AnalyseSelectionAlimentsView]) : l'utilisateur
 * choisit une ou plusieurs références bibliographiques existantes à gauche, sélectionne les
 * aliments cibles à droite, puis valide pour attacher les références à tous les aliments choisis.
 */
@Composable
fun BulkAddBiblioToFoodsView(
        viewModel: BulkAddBiblioToFoodsViewModel,
        onNavigateBack: () -> Unit,
        modifier: Modifier = Modifier
) {
    val allFoods by viewModel.allFoods.collectAsState()
    val availableBiblioRefs by viewModel.availableBiblioRefs.collectAsState()
    val selectedBiblioRefIds by viewModel.selectedBiblioRefIds.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val resultMessage by viewModel.resultMessage.collectAsState()

    var biblioFilter by remember { mutableStateOf("") }
    val filteredBiblioRefs =
            remember(availableBiblioRefs, biblioFilter) {
                if (biblioFilter.isBlank()) availableBiblioRefs
                else availableBiblioRefs.filter { ref ->
                    ref.firstAuthor.contains(biblioFilter, ignoreCase = true) ||
                            ref.completeRef.contains(biblioFilter, ignoreCase = true) ||
                            ref.year.toString().contains(biblioFilter)
                }
            }

    var localWarning by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
    Scaffold(
            topBar = {
                TopBarSimple(
                        title = "Ajout en masse de bibliographie",
                        onNavigateBack = onNavigateBack
                )
            }
    ) { paddingValues ->
        Row(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Panneau gauche : sélection des références bibliographiques à appliquer
            Column(
                    modifier = Modifier.weight(0.35f).fillMaxHeight().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Références à appliquer", style = MaterialTheme.typography.h6)
                OutlinedTextField(
                        value = biblioFilter,
                        onValueChange = { biblioFilter = it },
                        label = { Text("Rechercher (auteur, année, référence)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                )
                Text(
                        "${selectedBiblioRefIds.size} sélectionnée(s)",
                        style = MaterialTheme.typography.caption
                )
                if (filteredBiblioRefs.isEmpty()) {
                    Text(
                            "Aucune référence bibliographique disponible.",
                            style = MaterialTheme.typography.body2
                    )
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(filteredBiblioRefs, key = { it.uuid }) { ref ->
                            Row(
                                    modifier =
                                            Modifier.fillMaxWidth()
                                                    .clickable { viewModel.toggleBiblioRef(ref.uuid) }
                                                    .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                        checked = ref.uuid in selectedBiblioRefIds,
                                        onCheckedChange = { viewModel.toggleBiblioRef(ref.uuid) }
                                )
                                Column {
                                    Text(
                                            "${ref.firstAuthor} (${ref.year})",
                                            style = MaterialTheme.typography.body1
                                    )
                                    Text(
                                            ref.completeRef,
                                            style = MaterialTheme.typography.caption,
                                            maxLines = 2
                                    )
                                }
                            }
                            Divider()
                        }
                    }
                }
            }

            Divider(modifier = Modifier.fillMaxHeight().width(1.dp))

            // Panneau droit : sélection multiple des aliments cibles (composant existant)
            Box(modifier = Modifier.weight(0.65f).fillMaxHeight()) {
                if (loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = VetNutriColors.Primary)
                    }
                } else {
                    AnalyseSelectionAlimentsView(
                            aliments = allFoods,
                            onClose = onNavigateBack,
                            onPrimaryAction = { aliments ->
                                if (selectedBiblioRefIds.isEmpty()) {
                                    localWarning =
                                            "Sélectionnez au moins une référence bibliographique."
                                } else {
                                    localWarning = null
                                    viewModel.applyToFoods(aliments.map { it.uuid })
                                }
                            },
                            primaryActionLabel = "Ajouter la bibliographie sélectionnée",
                            modifier = Modifier.fillMaxSize().padding(16.dp)
                    )
                }
            }
        }
    }

    (localWarning ?: error.takeIf { it.isNotEmpty() } ?: resultMessage)?.let { message ->
        Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                action = {
                    TextButton(
                            onClick = {
                                localWarning = null
                                viewModel.clearResultMessage()
                            }
                    ) { Text("OK") }
                }
        ) { Text(message) }
    }
    }
}
