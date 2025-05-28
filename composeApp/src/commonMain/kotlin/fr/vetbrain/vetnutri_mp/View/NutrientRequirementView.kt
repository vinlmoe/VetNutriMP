package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.ViewModel.ReferenceEvViewModel

/**
 * Vue des besoins nutritionnels. Cette vue affiche la liste des références évaluées permettant
 * d'accéder à l'édition des besoins nutritionnels.
 *
 * @param viewModel ViewModel des références évaluées
 * @param onNavigateBack Callback pour revenir à la vue précédente
 * @param onEditReference Callback pour éditer une référence
 * @param onCreateReference Callback pour créer une nouvelle référence
 * @param onEditNutrients Callback pour éditer les besoins nutritionnels d'une référence
 * @param modifier Modifier à appliquer à la vue
 */
@Composable
fun NutrientRequirementView(
        viewModel: ReferenceEvViewModel,
        onNavigateBack: () -> Unit,
        onEditReference: (String) -> Unit,
        onCreateReference: () -> Unit,
        onEditNutrients: (String) -> Unit,
        modifier: Modifier = Modifier
) {
    val allReferences by viewModel.allReferences.collectAsState(initial = emptyList())
    val loading by viewModel.loading.collectAsState(initial = false)

    // Charger les références au démarrage
    LaunchedEffect(Unit) { viewModel.loadAllReferences() }

    Scaffold(
            topBar = {
                TopBarSimple(title = "Besoins Nutritionnels bbb", onNavigateBack = onNavigateBack)
            }
    ) { paddingValues ->
        Column(modifier = modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            // Section d'introduction
            Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Gestion des besoins nutritionnels", style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                            "Les besoins nutritionnels sont définis pour chaque référence évaluée. " +
                                    "Sélectionnez une référence ci-dessous pour éditer ses besoins nutritionnels.",
                            style = MaterialTheme.typography.body1
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bouton pour créer une nouvelle référence
            Button(onClick = onCreateReference, modifier = Modifier.align(Alignment.End)) {
                Text("Ajouter une référence")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Liste des références
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (allReferences.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                            "Aucune référence disponible. Ajoutez une référence pour définir des besoins nutritionnels.",
                            style = MaterialTheme.typography.body1
                    )
                }
            } else {
                LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allReferences) { reference ->
                        ReferenceNutrientCard(
                                reference = reference,
                                onEdit = { onEditReference(reference.uuid) },
                                onEditNutrients = { onEditNutrients(reference.uuid) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferenceNutrientCard(
        reference: ReferenceEv,
        onEdit: () -> Unit,
        onEditNutrients: () -> Unit,
        modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth(), elevation = 4.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = reference.nom, style = MaterialTheme.typography.h6)

            Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            text = "Espèce: ${reference.espece}",
                            style = MaterialTheme.typography.body1
                    )
                    Text(
                            text = "Stade physiologique: ${reference.stadePhysio}",
                            style = MaterialTheme.typography.body1
                    )
                    if (reference.maladie) {
                        Text(
                                text = "Maladie: ${reference.nomMaladie}",
                                style = MaterialTheme.typography.body1
                        )
                    }
                }

                // Boutons d'action
                Row {
                    OutlinedButton(onClick = onEdit, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Éditer")
                    }

                    Button(onClick = onEditNutrients) { Text("Besoins nutritionnels") }
                }
            }
        }
    }
}
