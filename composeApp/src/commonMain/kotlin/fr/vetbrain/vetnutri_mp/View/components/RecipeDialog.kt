package fr.vetbrain.vetnutri_mp.View.Components

import fr.vetbrain.vetnutri_mp.ViewModel.RecipeLibrary
import fr.vetbrain.vetnutri_mp.Components.AppAlertDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import kotlinx.coroutines.launch

@Composable
fun RecipeDialog(
        library: RecipeLibrary,
        onApply: (Ration) -> Unit,
        onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var recipes by remember { mutableStateOf<List<Ration>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    suspend fun loadRecipesWithFoodDetails(): List<Ration> = library.loadRecipes()

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            recipes = loadRecipesWithFoodDetails()
        } finally {
            isLoading = false
        }
    }

    AppAlertDialog(
            onDismissRequest = onClose,
            title = { Text(translate(LocalizationKeys.Recipe.TITLE)) },
            text = {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Bouton créer une recette avec icône
                        Row(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .clickable { showCreateDialog = true }
                                                .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = translate("recipe.createNew"),
                                    tint = VetNutriColors.Primary,
                                    modifier = Modifier.size(24.dp)
                            )
                            Text(
                                    text = translate("recipe.createNew"),
                                    color = VetNutriColors.Primary,
                                    style = MaterialTheme.typography.button
                            )
                        }

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

                                            // Bouton Appliquer avec icône
                                            Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = translate(LocalizationKeys.Recipe.CONTENT_APPLY),
                                                    tint = VetNutriColors.Primary,
                                                    modifier =
                                                            Modifier.size(24.dp).clickable {
                                                                onApply(r)
                                                            }
                                            )

                                            // Bouton Cloner avec icône
                                            Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = translate(LocalizationKeys.Recipe.CONTENT_CLONE),
                                                    tint = VetNutriColors.Secondary,
                                                    modifier =
                                                            Modifier.size(24.dp).clickable {
                                                                scope.launch {
                                                                    library.cloneRecipe(r.uuid)
                                                                    recipes =
                                                                            loadRecipesWithFoodDetails()
                                                                }
                                                            }
                                            )

                                            // Bouton Supprimer avec icône
                                            Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = translate(LocalizationKeys.Recipe.CONTENT_DELETE),
                                                    tint = Color.Red,
                                                    modifier =
                                                            Modifier.size(24.dp).clickable {
                                                                scope.launch {
                                                                    library.deleteRecipe(r.uuid)
                                                                    recipes =
                                                                            loadRecipesWithFoodDetails()
                                                                }
                                                            }
                                            )
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
                                                            translate(
                                                                    "recipe.foodLine",
                                                                    nom.toString(),
                                                                    qStr
                                                            ),
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
            confirmButton = {
                Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = translate(LocalizationKeys.AnalNut.CLOSE),
                        tint = VetNutriColors.Primary,
                        modifier = Modifier.size(24.dp).clickable { onClose() }
                )
            }
    )

    if (showCreateDialog) {
        AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text(translate("recipe.createNew")) },
                text = {
                    OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text(translate(LocalizationKeys.Recipe.NAME_LABEL)) }
                    )
                },
                confirmButton = {
                    Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = translate(LocalizationKeys.Recipe.CONTENT_CREATE),
                            tint = if (newName.isNotBlank()) VetNutriColors.Primary else Color.Gray,
                            modifier =
                                    Modifier.size(24.dp)
                                            .clickable(
                                                    enabled = newName.isNotBlank(),
                                                    onClick = {
                                                        scope.launch {
                                                            library.createRecipe(newName)
                                                            recipes = loadRecipesWithFoodDetails()
                                                            newName = ""
                                                            showCreateDialog = false
                                                        }
                                                    }
                                            )
                    )
                },
                dismissButton = {
                    Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = translate(LocalizationKeys.General.CANCEL),
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp).clickable { showCreateDialog = false }
                    )
                }
        )
    }
}
