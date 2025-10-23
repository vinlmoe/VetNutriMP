package fr.vetbrain.vetnutri_mp.View.SettingsSections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Components.BasicAppTextField
import fr.vetbrain.vetnutri_mp.Components.Section
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Espece.Companion.valuesExcept
import fr.vetbrain.vetnutri_mp.Enumer.Espece.CH
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.View.SettingsSections.RecipeAddAlimentView
import fr.vetbrain.vetnutri_mp.ViewModel.RecipeEditViewModel

/**
 * Vue principale d'édition des recettes
 */
@Composable
fun RecipeEditView(
    viewModel: RecipeEditViewModel,
    modifier: Modifier = Modifier
) {
    val isEditMode = viewModel.isEditMode.value
    val editingRecipe = viewModel.editingRecipe.value
    val selectedIngredients = viewModel.selectedIngredients.toList()
    val message = viewModel.message.value
    val isLoading = viewModel.isLoading.value
    val showDeleteConfirmation = viewModel.showDeleteConfirmation.value
    val showSaveConfirmation = viewModel.showSaveConfirmation.value
    

    
    // État pour afficher la vue d'ajout d'aliment
    var showAddAlimentView by remember { mutableStateOf(false) }
    
    if (showAddAlimentView) {
        // Afficher la vue d'ajout d'aliment en pleine page
        RecipeAddAlimentView(
            recipeEditViewModel = viewModel,
            onNavigateBack = {
                showAddAlimentView = false
            },
            onAddAliment = { aliment, quantite ->
                viewModel.selectAlimentToAdd(aliment)
                viewModel.updateQuantityToAdd(quantite.toString())
                viewModel.addAlimentToRecipe()
                showAddAlimentView = false
            },
            modifier = modifier
        )
    } else {
        Scaffold(
            floatingActionButton = {
                if (isEditMode) {
                    // Bouton de validation en mode édition
                    FloatingActionButton(
                        onClick = { 
                            // Protection contre les clics multiples
                            if (!viewModel.isLoading.value) {
                                viewModel.showSaveConfirmation()
                            }
                        },
                        backgroundColor = if (viewModel.canSaveRecipe() && !viewModel.isLoading.value) VetNutriColors.Primary else Color.Gray,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Sauvegarder",
                            tint = Color.White
                        )
                    }
                } else {
                    // Bouton d'ajout en mode liste
                    FloatingActionButton(
                        onClick = { 
                            // Protection contre les clics multiples
                            if (!viewModel.isLoading.value) {
                                viewModel.startCreatingRecipe()
                            }
                        },
                        backgroundColor = VetNutriColors.Primary,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Nouvelle recette",
                            tint = Color.White
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
            ) {
                // En-tête simplifié (seulement en mode édition)
                if (isEditMode) {
                    TopAppBar(
                        title = { 
                            Text(
                                text = editingRecipe?.name ?: "Nouvelle recette",
                                style = MaterialTheme.typography.h6
                            ) 
                        },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.cancelEditing() }) {
                                Icon(
                                    Icons.AutoMirrored.Default.ArrowBack,
                                    contentDescription = "Retour"
                                )
                            }
                        },
                        backgroundColor = VetNutriColors.Surface,
                        elevation = AppSizes.elevationSmall
                    )
                }
                
                // Message d'information/erreur
                message?.let { msg ->
                    MessageCard(
                        message = msg,
                        onDismiss = { viewModel.clearMessage() }
                    )
                }
                
                if (isEditMode) {
                    // Mode édition : formulaire de recette
                    RecipeEditForm(
                        viewModel = viewModel,
                        editingRecipe = editingRecipe,
                        selectedIngredients = selectedIngredients,
                        onAddAliment = { showAddAlimentView = true },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // Mode liste : affichage des recettes existantes (sans titre)
                    RecipeListSection(
                        viewModel = viewModel,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
    
    // Dialogues de confirmation
    showDeleteConfirmation?.let { recipe ->
        DeleteRecipeDialog(
            recipe = recipe,
            onConfirm = {
                viewModel.deleteRecipe(recipe)
                viewModel.hideDeleteConfirmation()
            },
            onDismiss = { viewModel.hideDeleteConfirmation() }
        )
    }
    
    if (showSaveConfirmation) {
        SaveRecipeDialog(
            onConfirm = {
                // Protection contre les appels multiples
                if (!viewModel.isLoading.value) {
                    viewModel.saveRecipe()
                    viewModel.hideSaveConfirmation()
                }
            },
            onDismiss = { viewModel.hideSaveConfirmation() }
        )
    }
}



/**
 * Formulaire d'édition de recette
 */
@Composable
private fun RecipeEditForm(
    viewModel: RecipeEditViewModel,
    editingRecipe: Ration?,
    selectedIngredients: List<AlimentRation>,
    onAddAliment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
    ) {
        // En-tête avec nom de la recette et bouton d'ajout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicAppTextField(
                value = viewModel.newRecipeName.value,
                onValueChange = { viewModel.updateRecipeName(it) },
                placeholder = "Nom de la recette",
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(AppSizes.paddingMedium))
            
            // Bouton d'ajout d'aliment (petit + comme dans AlimentsRationSection)
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Ajouter un aliment",
                tint = VetNutriColors.Primary,
                modifier = Modifier
                    .size(AppSizes.iconSizeXSmall)
                    .clickable(
                        onClick = onAddAliment,
                        enabled = !viewModel.isLoading.value
                    )
            )
        }
        
        Divider()
        
        // Liste des aliments de la recette (scrollable)
        if (selectedIngredients.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                items(selectedIngredients) { ingredient ->
                    RecipeIngredientItem(
                        ingredient = ingredient,
                        onRemove = { viewModel.removeAlimentFromRecipe(ingredient) },
                        onUpdateQuantity = { quantity ->
                            viewModel.updateAlimentQuantity(ingredient, quantity)
                        }
                    )
                }
            }
        } else {
            // Message quand aucun aliment
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucun aliment dans cette recette\nCliquez sur le + pour en ajouter",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}



/**
 * Élément d'ingrédient de recette
 */
@Composable
private fun RecipeIngredientItem(
    ingredient: AlimentRation,
    onRemove: () -> Unit,
    onUpdateQuantity: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = AppSizes.elevationSmall
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingMedium),
            horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Informations de l'aliment
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ingredient.aliment?.nom ?: "Aliment inconnu",
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Medium
                )
                if (ingredient.aliment?.brand != null) {
                    Text(
                        text = ingredient.aliment!!.brand!!,
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                    )
                }
            }
            
            // Contrôle de quantité avec label clair
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quantité (g):",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.width(70.dp)
                )
                
                BasicAppTextField(
                    value = ingredient.quantite.toString(),
                    onValueChange = onUpdateQuantity,
                    placeholder = "100",
                    modifier = Modifier.width(80.dp)
                )
                
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colors.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Section de liste des recettes
 */
@Composable
private fun RecipeListSection(
    viewModel: RecipeEditViewModel,
    modifier: Modifier = Modifier
) {
    val recipes = viewModel.recipes.toList()
    val searchQuery = viewModel.searchQuery.value
    val isLoading = viewModel.isLoading.value
    

    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
    ) {
        // Barre de recherche
        BasicAppTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = "Rechercher une recette...",
            leadingIcon = Icons.Default.Search,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Liste des recettes
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = VetNutriColors.Primary)
            }
        } else if (recipes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucune recette trouvée",
                    style = MaterialTheme.typography.body1,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                items(viewModel.getFilteredRecipes()) { recipe ->
                    RecipeListItem(
                        recipe = recipe,
                        onEdit = { viewModel.startEditingRecipe(recipe) },
                        onDelete = { viewModel.showDeleteConfirmation(recipe) },
                        onDuplicate = { viewModel.duplicateRecipe(recipe) }
                    )
                }
            }
        }
    }
}

/**
 * Élément de liste de recette
 */
@Composable
private fun RecipeListItem(
    recipe: Ration,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = AppSizes.elevationSmall
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingMedium)
        ) {
            // En-tête avec nom et actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)
                ) {
                    IconButton(
                        onClick = onDuplicate,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Dupliquer",
                            tint = VetNutriColors.Secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Éditer",
                            tint = VetNutriColors.Primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = MaterialTheme.colors.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            // Informations de la recette
            Text(
                text = "${recipe.alimentMutableList.size} aliments",
                style = MaterialTheme.typography.caption,
                color = Color.Gray
            )
            
            // Liste des aliments (résumé)
            if (recipe.alimentMutableList.isNotEmpty()) {
                val alimentsText = recipe.alimentMutableList
                    .take(3)
                    .joinToString(", ") { 
                        "${it.aliment?.nom ?: "Inconnu"} (${it.quantite}g)"
                    }
                Text(
                    text = alimentsText,
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
                
                if (recipe.alimentMutableList.size > 3) {
                    Text(
                        text = "... et ${recipe.alimentMutableList.size - 3} autres",
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

/**
 * Carte de message
 */
@Composable
private fun MessageCard(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isError = message.startsWith("Erreur")
    val isSuccess = message.startsWith("Recette") && (message.contains("succès") || message.contains("créée") || message.contains("modifiée"))
    
    Card(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = when {
            isError -> MaterialTheme.colors.error.copy(alpha = 0.1f)
            isSuccess -> VetNutriColors.Primary.copy(alpha = 0.1f)
            else -> VetNutriColors.Secondary.copy(alpha = 0.1f)
        },
        elevation = AppSizes.elevationSmall
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.body2,
                color = when {
                    isError -> MaterialTheme.colors.error
                    isSuccess -> VetNutriColors.Primary
                    else -> VetNutriColors.Secondary
                },
                modifier = Modifier.weight(1f)
            )
            
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Fermer",
                    tint = Color.Gray
                )
            }
        }
    }
}

/**
 * Dialogue de confirmation de suppression
 */
@Composable
private fun DeleteRecipeDialog(
    recipe: Ration,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmer la suppression") },
        text = { 
            Text("Êtes-vous sûr de vouloir supprimer la recette \"${recipe.name}\" ? Cette action est irréversible.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.error
                )
            ) {
                Text("Supprimer")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

/**
 * Dialogue de confirmation de sauvegarde
 */
@Composable
private fun SaveRecipeDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmer la sauvegarde") },
        text = { 
            Text("Voulez-vous sauvegarder cette recette ?")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = VetNutriColors.Primary
                )
            ) {
                Text("Sauvegarder")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
