package fr.vetbrain.vetnutri_mp.View.AnalNut

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

/** Données d'ajustement pour un aliment spécifique */
data class AlimentAdjustmentData(
        val alimentRation: AlimentRation,
        val selectedNutrient: String? = null,
        val isLocked: Boolean = false
)

/** Dialog pour l'ajustement multi-nutriments de la ration */
@Composable
fun MultiNutrientAdjustmentDialog(
        ration: Ration?,
        onDismiss: () -> Unit,
        onApplyAdjustment: (List<AlimentAdjustmentData>) -> Unit
) {
    if (ration == null) return

    // État pour les données d'ajustement de chaque aliment
    var adjustmentData by remember {
        mutableStateOf(
                ration.alimentMutableList.map { alimentRation ->
                    AlimentAdjustmentData(alimentRation = alimentRation)
                }
        )
    }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                        text = "Ajustement multi-nutriments",
                        style = MaterialTheme.typography.h6,
                        color = VetNutriColors.Primary
                )
            },
            text = {
                Column(
                        modifier = Modifier.fillMaxWidth().height(400.dp),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                    Text(
                            text = "Sélectionnez le nutriment cible pour chaque aliment :",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)) {
                        items(adjustmentData) { alimentData ->
                            AlimentAdjustmentRow(
                                    alimentData = alimentData,
                                    onNutrientChange = { newNutrient ->
                                        adjustmentData =
                                                adjustmentData.map { data ->
                                                    if (data.alimentRation.uuid ==
                                                                    alimentData.alimentRation.uuid
                                                    ) {
                                                        data.copy(selectedNutrient = newNutrient)
                                                    } else {
                                                        data
                                                    }
                                                }
                                    },
                                    onLockToggle = { isLocked ->
                                        adjustmentData =
                                                adjustmentData.map { data ->
                                                    if (data.alimentRation.uuid ==
                                                                    alimentData.alimentRation.uuid
                                                    ) {
                                                        data.copy(isLocked = isLocked)
                                                    } else {
                                                        data
                                                    }
                                                }
                                    }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = { onApplyAdjustment(adjustmentData) },
                        colors =
                                ButtonDefaults.buttonColors(
                                        backgroundColor = VetNutriColors.Primary,
                                        contentColor = VetNutriColors.OnPrimary
                                )
                ) {
                    Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Appliquer",
                            modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Appliquer")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Annuler",
                            modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Annuler")
                }
            }
    )
}

/** Ligne d'ajustement pour un aliment spécifique */
@Composable
private fun AlimentAdjustmentRow(
        alimentData: AlimentAdjustmentData,
        onNutrientChange: (String?) -> Unit,
        onLockToggle: (Boolean) -> Unit
) {
    // Filtrer les nutriments disponibles pour cet aliment spécifique
    val availableNutrients =
            remember(alimentData.alimentRation.aliment) {
                val valMap = alimentData.alimentRation.aliment?.valMap
                if (valMap != null) {
                    val nutrientLabels = mutableListOf<String>()
                    for ((nutrientLabel, nutrientQuantity) in valMap.entries) {
                        if (nutrientQuantity.value > 0) {
                            nutrientLabels.add(nutrientLabel.label)
                        }
                    }
                    nutrientLabels.sorted()
                } else {
                    emptyList<String>()
                }
            }

    Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = AppSizes.elevationSmall,
            backgroundColor =
                    if (alimentData.isLocked) {
                        MaterialTheme.colors.surface.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colors.surface
                    }
    ) {
        Column(
                modifier = Modifier.padding(AppSizes.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
        ) {
            // En-tête avec nom de l'aliment et bouton de verrouillage
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = alimentData.alimentRation.aliment?.nom ?: "Aliment inconnu",
                        style = MaterialTheme.typography.subtitle2,
                        fontWeight = FontWeight.Medium,
                        color =
                                if (alimentData.isLocked) {
                                    MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                } else {
                                    VetNutriColors.Primary
                                }
                )

                IconButton(
                        onClick = { onLockToggle(!alimentData.isLocked) },
                        modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                            imageVector =
                                    if (alimentData.isLocked) {
                                        Icons.Default.Lock
                                    } else {
                                        Icons.Default.LockOpen
                                    },
                            contentDescription =
                                    if (alimentData.isLocked) {
                                        "Déverrouiller"
                                    } else {
                                        "Verrouiller"
                                    },
                            tint =
                                    if (alimentData.isLocked) {
                                        Color.Red
                                    } else {
                                        Color.Green
                                    },
                            modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Quantité actuelle
            Text(
                    text = "Quantité actuelle : ${alimentData.alimentRation.quantite}g",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )

            // Combobox pour sélectionner le nutriment cible
            if (availableNutrients.isNotEmpty()) {
                DropdownField(
                        label = "Nutriment cible",
                        selectedValue = alimentData.selectedNutrient,
                        options = availableNutrients,
                        onValueChange = { nutrient -> onNutrientChange(nutrient) },
                        valueToString = { nutrient -> nutrient },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !alimentData.isLocked
                )
            } else {
                Text(
                        text = "Aucun nutriment disponible pour cet aliment",
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            // Indicateur visuel pour les aliments verrouillés
            if (alimentData.isLocked) {
                Text(
                        text = "🔒 Cet aliment ne sera pas ajusté",
                        style = MaterialTheme.typography.caption,
                        color = Color.Red,
                        fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
