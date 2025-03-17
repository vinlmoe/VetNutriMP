package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

/**
 * Composant pour afficher un aliment dans une liste
 *
 * @param aliment L'aliment à afficher
 * @param isEditing Indique si l'aliment est en cours d'édition
 * @param onStartEditing Action à exécuter pour commencer l'édition
 * @param onQuantityChange Action à exécuter lorsque la quantité change
 * @param onFinishEditing Action à exécuter lorsque l'édition est terminée
 * @param onDelete Action à exécuter pour supprimer l'aliment
 * @param modifier Modificateur optionnel
 */
@Composable
fun AlimentItem(
        aliment: AlimentRation,
        isEditing: Boolean,
        onStartEditing: () -> Unit,
        onQuantityChange: (Float) -> Unit,
        onFinishEditing: () -> Unit,
        onDelete: () -> Unit,
        modifier: Modifier = Modifier
) {
    // État local pour la quantité en cours d'édition
    var quantityText by remember(aliment.quantite) { mutableStateOf(aliment.quantite.toString()) }

    Card(
            modifier = modifier.fillMaxWidth(),
            elevation = AppSizes.elevationSmall,
            backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(modifier = Modifier.padding(AppSizes.paddingSmall)) {
            // En-tête avec nom et boutons d'action
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = aliment.aliment?.nom ?: "Aliment sans nom",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Medium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                            onClick = onStartEditing,
                            modifier = Modifier.size(AppSizes.iconSizeSmall)
                    ) {
                        Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Éditer la quantité",
                                tint = VetNutriColors.Secondary
                        )
                    }

                    IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(AppSizes.iconSizeSmall)
                    ) {
                        Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Supprimer",
                                tint = VetNutriColors.Error
                        )
                    }
                }
            }

            // Informations sur l'aliment
            Row(
                    modifier = Modifier.fillMaxWidth().padding(top = AppSizes.paddingXSmall),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Quantité: ", style = MaterialTheme.typography.body2)

                if (isEditing) {
                    // Mode édition avec champ de texte
                    OutlinedTextField(
                            value = quantityText,
                            onValueChange = { newValue ->
                                // Filtrer pour n'accepter que les nombres et décimaux
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    quantityText = newValue
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = VetNutriColors.Primary,
                                    unfocusedBorderColor = VetNutriColors.Secondary
                            )
                    )

                    Button(
                            onClick = {
                                val newQuantity = quantityText.toFloatOrNull() ?: aliment.quantite
                                onQuantityChange(newQuantity)
                                onFinishEditing()
                            },
                            colors = ButtonDefaults.buttonColors(
                                    backgroundColor = VetNutriColors.Primary,
                                    contentColor = VetNutriColors.OnPrimary
                            ),
                            modifier = Modifier.padding(start = AppSizes.paddingSmall)
                    ) { Text("OK") }
                } else {
                    // Mode affichage
                    Text(
                            text = "${aliment.quantite} g",
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
