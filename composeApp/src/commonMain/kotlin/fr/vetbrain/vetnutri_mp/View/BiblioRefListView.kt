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
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.ConfirmDialog
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.BiblioRefViewModel
import java.util.*
import kotlinx.coroutines.launch

/** Vue pour afficher et gérer la liste des références bibliographiques */
@Composable
fun BiblioRefListView(
        viewModel: BiblioRefViewModel,
        onNavigateBack: () -> Unit,
        onEditBiblioRef: (String) -> Unit = {}, // UUID de la référence à éditer
        onCreateBiblioRef: () -> Unit = {},
        modifier: Modifier = Modifier
) {
    val biblioRefs by viewModel.allBiblioRefs.collectAsState()
    val searchQuery = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // État pour la référence à supprimer
    var refToDelete by remember { mutableStateOf<BiblioRef?>(null) }

    LaunchedEffect(biblioRefs) {
        println("DEBUG BiblioRefListView: Liste mise à jour - ${biblioRefs.size} références")
    }

    LaunchedEffect(Unit) {
        // Si la liste est vide, on peut ajouter des références de test (pour développement)
        if (biblioRefs.isEmpty()) {
            println(
                    "DEBUG BiblioRefListView: Liste vide, possibilité d'ajouter des données de test"
            )
        }
    }

    // Filtrage des références en fonction de la recherche
    val filteredRefs =
            remember(biblioRefs, searchQuery.value) {
                if (searchQuery.value.isBlank()) {
                    biblioRefs
                } else {
                    val query = searchQuery.value.lowercase()
                    biblioRefs.filter { biblioRef ->
                        biblioRef.firstAuthor.lowercase().contains(query) ||
                                biblioRef.year.toString().contains(query) ||
                                biblioRef.completeRef.lowercase().contains(query) ||
                                biblioRef.comments.lowercase().contains(query)
                    }
                }
            }

    Scaffold(
            topBar = {
                TopBarSimple(
                        title = "Références bibliographiques",
                        onNavigateBack = onNavigateBack,
                        actions = {
                            IconButton(onClick = onCreateBiblioRef) {
                                Icon(
                                        imageVector = AppIcons.Add,
                                        contentDescription = "Ajouter une référence",
                                        tint = VetNutriColors.OnPrimary
                                )
                            }
                        }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                        onClick = onCreateBiblioRef,
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
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(paddingValues)
                                .padding(AppSizes.paddingMedium)
        ) {
            // Barre de recherche
            OutlinedTextField(
                    value = searchQuery.value,
                    onValueChange = { searchQuery.value = it },
                    label = { Text("Rechercher une référence") },
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

            Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

            // Affichage des résultats
            if (filteredRefs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                            text =
                                    if (searchQuery.value.isBlank())
                                            "Aucune référence bibliographique disponible"
                                    else "Aucune référence ne correspond à votre recherche",
                            style = MaterialTheme.typography.h6
                    )
                }
            } else {
                Text(
                        text = "Liste des références (${filteredRefs.size})",
                        style = MaterialTheme.typography.h6
                )

                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.cardSpacing)
                ) {
                    items(filteredRefs) { biblioRef ->
                        BiblioRefCard(
                                biblioRef = biblioRef,
                                onDelete = { refToDelete = biblioRef },
                                onEdit = { onEditBiblioRef(biblioRef.uuid) }
                        )
                    }
                }
            }
        }

        // Dialogue de confirmation de suppression
        refToDelete?.let { biblioRef ->
            ConfirmDialog(
                    title = "Confirmation de suppression",
                    message =
                            "Êtes-vous sûr de vouloir supprimer la référence '${biblioRef.firstAuthor}, ${biblioRef.year}' ?",
                    onConfirm = {
                        coroutineScope.launch { viewModel.deleteBiblioRef(biblioRef) }
                        refToDelete = null
                    },
                    onDismiss = { refToDelete = null }
            )
        }
    }
}

/** Carte affichant une référence bibliographique dans la liste */
@Composable
private fun BiblioRefCard(
        biblioRef: BiblioRef,
        onDelete: () -> Unit,
        onEdit: () -> Unit,
        modifier: Modifier = Modifier
) {
    Card(
            modifier = modifier.fillMaxWidth().clickable { onEdit() },
            elevation = AppSizes.cardElevationNormal
    ) {
        Column(
                modifier = Modifier.padding(AppSizes.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            text = "${biblioRef.firstAuthor}, ${biblioRef.year}",
                            style = MaterialTheme.typography.subtitle1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(text = biblioRef.completeRef, style = MaterialTheme.typography.body2)

                    if (biblioRef.comments.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                                text = "Commentaire: ${biblioRef.comments}",
                                style = MaterialTheme.typography.caption
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
