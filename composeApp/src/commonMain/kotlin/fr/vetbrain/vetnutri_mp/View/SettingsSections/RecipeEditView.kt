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
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    foodRepository: FoodRepository,
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
            foodRepository = foodRepository,
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
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
            // En-tête avec actions
            RecipeEditHeader(
                viewModel = viewModel,
                isEditMode = isEditMode,
                canSave = viewModel.canSaveRecipe(),
                hasChanges = viewModel.hasRecipeChanged()
            )
            
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
                // Mode liste : affichage des recettes existantes
                RecipeListSection(
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )
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
                viewModel.saveRecipe()
                viewModel.hideSaveConfirmation()
            },
            onDismiss = { viewModel.hideSaveConfirmation() }
        )
    }
}

/**
 * En-tête de la vue d'édition avec actions
 */
@Composable
private fun RecipeEditHeader(
    viewModel: RecipeEditViewModel,
    isEditMode: Boolean,
    canSave: Boolean,
    hasChanges: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = AppSizes.elevationSmall,
        backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isEditMode) "Édition de recette" else "Gestion des recettes",
                    style = MaterialTheme.typography.h6,
                    color = VetNutriColors.Primary
                )
                if (isEditMode && hasChanges) {
                    Text(
                        text = "Modifications non sauvegardées",
                        style = MaterialTheme.typography.caption,
                        color = VetNutriColors.Secondary
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                if (isEditMode) {
                    // Boutons en mode édition
                    OutlinedButton(
                        onClick = { viewModel.cancelEditing() }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Annuler")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Annuler")
                    }
                    
                    Button(
                        onClick = { viewModel.showSaveConfirmation() },
                        enabled = canSave,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = VetNutriColors.Primary
                        )
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Sauvegarder")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sauvegarder")
                    }
                } else {
                    // Bouton en mode liste
                    Button(
                        onClick = { viewModel.startCreatingRecipe() },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = VetNutriColors.Primary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Nouvelle recette")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nouvelle recette")
                    }
                }
            }
        }
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
        // Informations de base de la recette
        Section(title = "Informations de la recette") {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                BasicAppTextField(
                    value = viewModel.newRecipeName.value,
                    onValueChange = { viewModel.updateRecipeName(it) },
                    placeholder = "Nom de la recette",
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "Aliments dans la recette: ${selectedIngredients.size}",
                    style = MaterialTheme.typography.body2,
                    color = VetNutriColors.Primary
                )
            }
        }
        
        // Section d'ajout d'aliments
        Section(title = "Ajouter des aliments") {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
            ) {
                // Configuration de l'aliment à ajouter
                AlimentAddConfiguration(
                    viewModel = viewModel,
                    onAddAliment = onAddAliment,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Liste des aliments de la recette
        if (selectedIngredients.isNotEmpty()) {
            Section(title = "Aliments de la recette") {
                RecipeIngredientsList(
                    ingredients = selectedIngredients,
                    onRemoveIngredient = { viewModel.removeAlimentFromRecipe(it) },
                    onUpdateQuantity = { aliment, quantity ->
                        viewModel.updateAlimentQuantity(aliment, quantity)
                    },
                    onUpdateTarget = { aliment, target ->
                        viewModel.updateAlimentTarget(aliment, target)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Configuration de l'aliment à ajouter
 */
@Composable
private fun AlimentAddConfiguration(
    viewModel: RecipeEditViewModel,
    onAddAliment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = AppSizes.elevationSmall
    ) {
        Column(
            modifier = Modifier.padding(AppSizes.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
        ) {
            Text(
                text = "Ajouter des aliments",
                style = MaterialTheme.typography.h6,
                color = VetNutriColors.Primary
            )
            
            Text(
                text = "Cliquez sur le bouton ci-dessous pour rechercher et ajouter des aliments à votre recette",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
            
            Button(
                onClick = onAddAliment,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = VetNutriColors.Primary,
                    contentColor = VetNutriColors.OnPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rechercher et ajouter un aliment")
            }
        }
    }
}

/**
 * Liste des ingrédients de la recette
 */
@Composable
private fun RecipeIngredientsList(
    ingredients: List<AlimentRation>,
    onRemoveIngredient: (AlimentRation) -> Unit,
    onUpdateQuantity: (AlimentRation, String) -> Unit,
    onUpdateTarget: (AlimentRation, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
    ) {
        items(ingredients) { ingredient ->
            RecipeIngredientItem(
                ingredient = ingredient,
                onRemove = { onRemoveIngredient(ingredient) },
                onUpdateQuantity = { quantity -> onUpdateQuantity(ingredient, quantity) },
                onUpdateTarget = { target -> onUpdateTarget(ingredient, target) }
            )
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
    onUpdateTarget: (Int) -> Unit,
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
            
            // Contrôles de quantité et cible
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicAppTextField(
                    value = ingredient.quantite.toString(),
                    onValueChange = onUpdateQuantity,
                    placeholder = "100",
                    modifier = Modifier.width(80.dp)
                )
                
                BasicAppTextField(
                    value = (ingredient.refTarget ?: 0).toString(),
                    onValueChange = { 
                        try {
                            onUpdateTarget(it.toInt())
                        } catch (e: NumberFormatException) {
                            // Ignore les valeurs invalides
                        }
                    },
                    placeholder = "0",
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
