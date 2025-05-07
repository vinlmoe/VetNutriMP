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
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.ReferenceEvViewModel
import kotlinx.coroutines.launch

/**
 * Vue des besoins nutritionnels améliorée. Cette vue affiche la liste des références évaluées avec
 * des fonctionnalités de filtrage et recherche.
 *
 * @param viewModel ViewModel des références évaluées
 * @param onNavigateBack Callback pour revenir à la vue précédente
 * @param onEditReference Callback pour éditer une référence
 * @param onCreateReference Callback pour créer une nouvelle référence
 * @param onEditNutrients Callback pour éditer les besoins nutritionnels d'une référence
 * @param onViewTabs Callback pour voir les détails d'une référence par onglets (facultatif)
 * @param modifier Modifier à appliquer à la vue
 */
@Composable
fun NutrientRequirementView(
        viewModel: ReferenceEvViewModel,
        onNavigateBack: () -> Unit,
        onEditReference: (String) -> Unit,
        onCreateReference: () -> Unit,
        onEditNutrients: (String) -> Unit,
        onViewTabs: (String) -> Unit =
                onEditNutrients, // Par défaut, même action que onEditNutrients
        modifier: Modifier = Modifier
) {
    val references by viewModel.allReferences.collectAsState(initial = emptyList())
    val searchText by viewModel.searchText.collectAsState(initial = "")
    val selectedEspece by viewModel.selectedEspece.collectAsState(initial = null)
    val filteredReferences by viewModel.filteredReferences.collectAsState(initial = emptyList())
    val loading by viewModel.loading.collectAsState(initial = false)
    val error by viewModel.error.collectAsState(initial = "")
    val coroutineScope = rememberCoroutineScope()

    // État pour la boîte de dialogue de confirmation de suppression
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var referenceToDelete by remember { mutableStateOf<ReferenceEv?>(null) }

    // Charger les références au démarrage
    LaunchedEffect(Unit) { viewModel.loadAllReferences() }

    Scaffold(
            topBar = {
                TopBarSimple(title = "Besoins Nutritionnels", onNavigateBack = onNavigateBack)
            },
            floatingActionButton = {
                FloatingActionButton(
                        onClick = onCreateReference,
                        backgroundColor = VetNutriColors.Primary,
                        contentColor = VetNutriColors.OnPrimary
                ) { Icon(AppIcons.Add, contentDescription = "Ajouter une référence") }
            }
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Barre de recherche et filtres
                Card(
                        modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingMedium),
                        elevation = AppSizes.cardElevationSmall
                ) {
                    Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                        // Champ de recherche
                        OutlinedTextField(
                                value = searchText,
                                onValueChange = { viewModel.updateSearchText(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Rechercher par nom") },
                                singleLine = true,
                                colors =
                                        TextFieldDefaults.outlinedTextFieldColors(
                                                focusedBorderColor = VetNutriColors.Primary,
                                                unfocusedBorderColor = Color.Gray
                                        )
                        )

                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

                        // Filtre par espèce
                        Text("Filtrer par espèce:", style = MaterialTheme.typography.subtitle1)
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                            // Bouton "Toutes"
                            OutlinedButton(
                                    onClick = { viewModel.updateSelectedEspece(null) },
                                    modifier = Modifier.weight(1f),
                                    colors =
                                            ButtonDefaults.outlinedButtonColors(
                                                    backgroundColor =
                                                            if (selectedEspece == null)
                                                                    VetNutriColors.Primary.copy(
                                                                            alpha = 0.1f
                                                                    )
                                                            else Color.Transparent,
                                                    contentColor =
                                                            if (selectedEspece == null)
                                                                    VetNutriColors.Primary
                                                            else Color.Gray
                                            )
                            ) { Text("Toutes") }

                            // Boutons pour chaque espèce
                            for (espece in Espece.values()) {
                                OutlinedButton(
                                        onClick = { viewModel.updateSelectedEspece(espece) },
                                        modifier = Modifier.weight(1f),
                                        colors =
                                                ButtonDefaults.outlinedButtonColors(
                                                        backgroundColor =
                                                                if (selectedEspece == espece)
                                                                        VetNutriColors.Primary.copy(
                                                                                alpha = 0.1f
                                                                        )
                                                                else Color.Transparent,
                                                        contentColor =
                                                                if (selectedEspece == espece)
                                                                        VetNutriColors.Primary
                                                                else Color.Gray
                                                )
                                ) { Text(espece.toString()) }
                            }
                        }

                        // Texte d'aide
                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                        Text(
                                "Sélectionnez une référence pour éditer ses besoins nutritionnels.",
                                style = MaterialTheme.typography.caption,
                                color = Color.Gray
                        )
                    }
                }

                // Liste des références
                Box(modifier = Modifier.fillMaxSize()) {
                    if (loading) {
                        CircularProgressIndicator(
                                modifier = Modifier.size(50.dp).align(Alignment.Center),
                                color = VetNutriColors.Primary
                        )
                    } else if (error.isNotBlank()) {
                        Text(
                                text = error,
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.body1,
                                color = Color.Red
                        )
                    } else if (filteredReferences.isEmpty()) {
                        Text(
                                text =
                                        if (searchText.isBlank() && selectedEspece == null)
                                                "Aucune référence disponible. Ajoutez-en une nouvelle."
                                        else "Aucun résultat ne correspond à votre recherche.",
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.body1
                        )
                    } else {
                        LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding =
                                        PaddingValues(
                                                horizontal = AppSizes.paddingMedium,
                                                vertical = AppSizes.paddingMedium
                                        )
                        ) {
                            items(filteredReferences) { reference ->
                                EnhancedReferenceCard(
                                        reference = reference,
                                        onEdit = { onEditReference(reference.uuid) },
                                        onDelete = {
                                            referenceToDelete = reference
                                            showDeleteConfirmation = true
                                        },
                                        onViewTabs = { onViewTabs(reference.uuid) },
                                        onEditNutrients = { onEditNutrients(reference.uuid) }
                                )
                                Divider()
                            }
                        }
                    }
                }
            }

            // Boîte de dialogue de confirmation de suppression
            if (showDeleteConfirmation) {
                ConfirmDialog(
                        title = "Confirmer la suppression",
                        message =
                                "Êtes-vous sûr de vouloir supprimer cette référence ? Cette action est irréversible.",
                        onConfirm = {
                            coroutineScope.launch {
                                referenceToDelete?.let { viewModel.deleteReference(it.uuid) }
                                showDeleteConfirmation = false
                                referenceToDelete = null
                            }
                        },
                        onDismiss = {
                            showDeleteConfirmation = false
                            referenceToDelete = null
                        }
                )
            }
        }
    }
}

@Composable
private fun EnhancedReferenceCard(
        reference: ReferenceEv,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
        onViewTabs: () -> Unit,
        onEditNutrients: () -> Unit,
        modifier: Modifier = Modifier
) {
    Card(
            modifier =
                    modifier.fillMaxWidth()
                            .padding(vertical = AppSizes.paddingSmall)
                            .clickable(onClick = onEditNutrients),
            elevation = AppSizes.cardElevationSmall
    ) {
        Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                // Titre et informations principales
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            text = reference.nom,
                            style = MaterialTheme.typography.h6,
                            color = VetNutriColors.Primary
                    )
                    Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                                text = reference.espece.toString(),
                                style = MaterialTheme.typography.caption,
                                color = Color.Gray
                        )
                        Text(
                                text = "|",
                                style = MaterialTheme.typography.caption,
                                color = Color.Gray
                        )
                        Text(
                                text = reference.stadePhysio.toString(),
                                style = MaterialTheme.typography.caption,
                                color = Color.Gray
                        )
                    }
                    if (reference.maladie && reference.nomMaladie.isNotBlank()) {
                        Text(
                                text = "Maladie: ${reference.nomMaladie}",
                                style = MaterialTheme.typography.caption,
                                color = Color.Red
                        )
                    }
                }

                // Boutons d'action
                Row {
                    IconButton(onClick = onViewTabs) {
                        Icon(
                                imageVector = AppIcons.ViewList,
                                contentDescription = "Voir détails par onglets",
                                tint = VetNutriColors.Primary
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                                imageVector = AppIcons.Edit,
                                contentDescription = "Modifier",
                                tint = VetNutriColors.Primary
                        )
                    }
                    IconButton(onClick = onEditNutrients) {
                        Icon(
                                imageVector = AppIcons.Ration,
                                contentDescription = "Besoins nutritionnels",
                                tint = VetNutriColors.Secondary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                                imageVector = AppIcons.Delete,
                                contentDescription = "Supprimer",
                                tint = Color.Red
                        )
                    }
                }
            }

            // Afficher une description courte si elle existe
            if (reference.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                Text(
                        text =
                                reference.description.take(100) +
                                        if (reference.description.length > 100) "..." else "",
                        style = MaterialTheme.typography.body2,
                        color = Color.DarkGray
                )
            }

            // Afficher les informations sur l'énergie si disponible
            if (reference.nomEnergie.isNotBlank()) {
                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                Text(
                        text = "Énergie: ${reference.nomEnergie}",
                        style = MaterialTheme.typography.body2,
                        color = Color.DarkGray
                )
            }
        }
    }
}
