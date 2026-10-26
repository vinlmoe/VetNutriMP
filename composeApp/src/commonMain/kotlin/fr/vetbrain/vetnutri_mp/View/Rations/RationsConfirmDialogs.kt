package fr.vetbrain.vetnutri_mp.View

// Dialogues de confirmation de l'écran Rations (extraits du corps de RationsView) :
// création d'une recette à partir de la ration et suppression d'une ration.

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Ration as RationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.RecipeDialogViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Dialogue « Créer une recette » à partir de la ration sélectionnée. */
@Composable
internal fun SaveRecipeDialog(
        selectedRation: Ration?,
        newRecipeName: String,
        onNewRecipeNameChange: (String) -> Unit,
        recipeDialogViewModel: RecipeDialogViewModel,
        coroutineScope: CoroutineScope,
        showSnackbar: (String) -> Unit,
        onDismiss: () -> Unit
) {
        AlertDialog(
                onDismissRequest = { onDismiss() },
                title = { Text(translate(RationKeys.CREATE_RECIPE)) },
                text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                        value = newRecipeName,
                                        onValueChange = { onNewRecipeNameChange(it) },
                                        label = { Text(translate(RationKeys.RECIPE_NAME)) },
                                        modifier = Modifier.fillMaxWidth()
                                )
                                val especeLabel = selectedRation?.espece ?: ""
                                if (especeLabel.isNotEmpty()) {
                                        Text(
                                                "${translate(Animal.SPECIES)}: $especeLabel",
                                                style = MaterialTheme.typography.body2
                                        )
                                }
                        }
                },
                confirmButton = {
                        Button(
                                onClick = {
                                        val ration = selectedRation
                                        if (ration != null && newRecipeName.isNotBlank()) {
                                                coroutineScope.launch {
                                                        try {
                                                                recipeDialogViewModel.saveRationAsRecipe(
                                                                        name = newRecipeName,
                                                                        espece = ration.espece,
                                                                        aliments = ration.alimentMutableList
                                                                )
                                                                showSnackbar(
                                                                        "${translate(RationKeys.RECIPE_CREATED)} $newRecipeName"
                                                                )
                                                        } catch (err: Exception) {
                                                                showSnackbar(
                                                                        "${translate(General.ERROR)}: ${err.message ?: translate(RationKeys.RECIPE_CREATE_ERROR_FALLBACK)}"
                                                                )
                                                        } finally {
                                                                onDismiss()
                                                        }
                                                }
                                        }
                                }
                        ) { Text(translate(General.CREATE)) }
                },
                dismissButton = {
                        OutlinedButton(onClick = { onDismiss() }) {
                                Text(translate(General.CANCEL))
                        }
                }
        )
}

/** Dialogue de confirmation de suppression d'une ration. */
@Composable
internal fun DeleteRationDialog(
        rationCible: Ration,
        viewModel: AnimalDetailViewModel,
        showSnackbar: (String) -> Unit,
        onDismiss: () -> Unit
) {
        AlertDialog(
                onDismissRequest = {
                        onDismiss()
                },
                title = {
                        Text(
                                translate(RationKeys.DELETE_CONFIRM_TITLE),
                                style = MaterialTheme.typography.h6,
                                color = VetNutriColors.Error
                        )
                },
                text = {
                        Text(
                                translate(
                                        RationKeys.DELETE_CONFIRM_MESSAGE,
                                        rationCible.name
                                ),
                                style = MaterialTheme.typography.body1
                        )
                },
                confirmButton = {
                        Button(
                                onClick = {
                                        viewModel.removeRationFromConsultation(
                                                rationCible
                                        )
                                        showSnackbar(
                                                translate(
                                                        RationKeys.DELETED,
                                                        rationCible.name
                                                )
                                        )
                                        onDismiss()
                                },
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor =
                                                        VetNutriColors.Error,
                                                contentColor =
                                                        VetNutriColors.OnError
                                        )
                        ) { Text(translate(General.DELETE)) }
                },
                dismissButton = {
                        TextButton(
                                onClick = {
                                        onDismiss()
                                }
                        ) { Text(translate(General.CANCEL)) }
                }
        )
}
