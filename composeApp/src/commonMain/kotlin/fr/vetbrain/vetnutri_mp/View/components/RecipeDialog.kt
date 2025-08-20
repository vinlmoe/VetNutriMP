package fr.vetbrain.vetnutri_mp.View.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Repository.RecipeRepository
import kotlinx.coroutines.launch

@Composable
fun RecipeDialog(
        repository: RecipeRepository,
        foodRepository: FoodRepository,
        onApply: (Ration) -> Unit,
        onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var recipes by remember { mutableStateOf<List<Ration>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    // Fonction helper pour charger les recettes avec les détails des aliments
    suspend fun loadRecipesWithFoodDetails(): List<Ration> {
        val loadedRecipes = repository.getAllRecipes()
        return loadedRecipes.map { recipe ->
            val alimentsWithDetails =
                    recipe.alimentMutableList.map { aliment ->
                        val alimentDetails =
                                aliment.refAlimUnif?.let { foodRepository.getFoodById(it) }
                        aliment.copy(aliment = alimentDetails)
                    }
            recipe.copy(alimentMutableList = alimentsWithDetails.toMutableList())
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            recipes = loadRecipesWithFoodDetails()
        } finally {
            isLoading = false
        }
    }

    AlertDialog(
            onDismissRequest = onClose,
            title = { Text("Recettes") },
            text = {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { showCreateDialog = true }) { Text("Créer une recette") }
                        LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            items(recipes) { r ->
                                Card(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                                    Column(
                                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = r.name)
                                                val esp = r.espece ?: ""
                                                if (esp.isNotBlank())
                                                        Text(
                                                                text = esp,
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption
                                                        )
                                            }
                                            TextButton(onClick = { onApply(r) }) {
                                                Text("Appliquer")
                                            }
                                            TextButton(
                                                    onClick = {
                                                        scope.launch {
                                                            repository.cloneRecipe(r.uuid)
                                                            recipes = loadRecipesWithFoodDetails()
                                                        }
                                                    }
                                            ) { Text("Cloner") }
                                            TextButton(
                                                    onClick = {
                                                        scope.launch {
                                                            repository.deleteRecipe(r.uuid)
                                                            recipes = loadRecipesWithFoodDetails()
                                                        }
                                                    }
                                            ) { Text("Supprimer") }
                                        }
                                        if (r.alimentMutableList.isNotEmpty()) {
                                            Divider()
                                            Column(
                                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                r.alimentMutableList.forEach { a ->
                                                    val nom =
                                                            a.aliment?.nom
                                                                    ?: a.refAlimUnif ?: a.uuidUnif
                                                    val q = a.quantite
                                                    val qStr =
                                                            (kotlin.math.round(q * 10.0) / 10.0)
                                                                    .toString()
                                                    Text(
                                                            "- $nom: $qStr g",
                                                            style = MaterialTheme.typography.caption
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = onClose) { Text("Fermer") } }
    )

    if (showCreateDialog) {
        AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Créer une recette") },
                text = {
                    OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Nom de la recette") }
                    )
                },
                confirmButton = {
                    TextButton(
                            onClick = {
                                scope.launch {
                                    repository.createRecipe(newName, null, null)
                                    recipes = loadRecipesWithFoodDetails()
                                    newName = ""
                                    showCreateDialog = false
                                }
                            },
                            enabled = newName.isNotBlank()
                    ) { Text("Créer") }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) { Text("Annuler") }
                }
        )
    }
}
