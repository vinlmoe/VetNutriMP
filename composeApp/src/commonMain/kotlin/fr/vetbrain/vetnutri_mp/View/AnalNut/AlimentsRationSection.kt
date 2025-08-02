package fr.vetbrain.vetnutri_mp.View.AnalNut

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.AlimentItem
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

/**
 * Section réutilisable pour l'affichage et la gestion des aliments d'une ration.
 * @param ration La ration sélectionnée (ou null).
 * @param referenceUtilisee La référence nutritionnelle utilisée (ou null).
 * @param onEditAliment Callback pour éditer un aliment.
 * @param onDeleteAliment Callback pour supprimer un aliment.
 * @param onAddAliment Callback pour ajouter un aliment.
 * @param onAjusterRation Callback pour ajuster automatiquement la ration.
 * @param onMultiNutrientAdjustment Callback pour l'ajustement multi-nutriments.
 */
@Composable
fun SectionAlimentsRation(
        ration: Ration?,
        referenceUtilisee: ReferenceEv?,
        onEditAliment: (Int) -> Unit,
        onDeleteAliment: (Int) -> Unit,
        onAddAliment: () -> Unit,
        onAjusterRation: () -> Unit,
        onMultiNutrientAdjustment: () -> Unit,
        modifier: Modifier = Modifier
) {
    val (editingIndex, setEditingIndex) = remember { mutableStateOf<Int?>(null) }
    val (editingQuantity, setEditingQuantity) = remember { mutableStateOf<Float?>(null) }
    Card(modifier = modifier.fillMaxWidth().height(350.dp), elevation = AppSizes.elevationSmall) {
        Column(
                modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingSmall),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = "Aliments de la ration",
                        style = MaterialTheme.typography.subtitle2,
                        color = VetNutriColors.Primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)) {
                    Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = "Ajuster la ration",
                            tint =
                                    if (ration != null &&
                                                    referenceUtilisee != null &&
                                                    (ration?.alimentMutableList?.isNotEmpty() ==
                                                            true)
                                    )
                                            VetNutriColors.Primary
                                    else VetNutriColors.Primary.copy(alpha = 0.5f),
                            modifier =
                                    Modifier.size(AppSizes.iconSizeXSmall)
                                            .clickable(
                                                    enabled =
                                                            ration != null &&
                                                                    referenceUtilisee != null &&
                                                                    (ration?.alimentMutableList
                                                                            ?.isNotEmpty() == true),
                                                    onClick = onAjusterRation
                                            )
                    )
                    Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Ajustement multi-nutriments",
                            tint =
                                    if (ration?.alimentMutableList?.isNotEmpty() == true)
                                            VetNutriColors.Primary
                                    else VetNutriColors.Primary.copy(alpha = 0.5f),
                            modifier =
                                    Modifier.size(AppSizes.iconSizeXSmall)
                                            .clickable(
                                                    enabled =
                                                            ration?.alimentMutableList
                                                                    ?.isNotEmpty() == true,
                                                    onClick = onMultiNutrientAdjustment
                                            )
                    )
                    Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Ajouter un aliment",
                            tint = VetNutriColors.Primary,
                            modifier =
                                    Modifier.size(AppSizes.iconSizeXSmall)
                                            .clickable(onClick = onAddAliment)
                    )
                }
            }
            Divider()
            if (ration?.alimentMutableList.isNullOrEmpty()) {
                Text(
                        text = "Aucun aliment dans cette ration.",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)) {
                    ration?.alimentMutableList?.forEachIndexed { index, alimentRation ->
                        AlimentItem(
                                aliment = alimentRation,
                                isEditing = editingIndex == index,
                                onStartEditing = {
                                    setEditingIndex(index)
                                    setEditingQuantity(alimentRation.quantite)
                                },
                                onQuantityChange = { newQuantity ->
                                    setEditingQuantity(newQuantity)
                                },
                                onFinishEditing = {
                                    if (editingIndex == index && editingQuantity != null) {
                                        onEditAliment(index)
                                    }
                                    setEditingIndex(null)
                                    setEditingQuantity(null)
                                },
                                onDelete = { onDeleteAliment(index) }
                        )
                    }
                }
            }
        }
    }
}
