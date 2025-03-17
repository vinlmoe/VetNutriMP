package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

/**
 * Composant pour afficher une ration dans une liste
 *
 * @param ration La ration à afficher
 * @param isSelected Indique si la ration est sélectionnée
 * @param onClick Action à exécuter lors du clic sur la ration
 * @param onEdit Action à exécuter pour éditer la ration
 * @param onDelete Action à exécuter pour supprimer la ration
 * @param modifier Modificateur optionnel
 */
@Composable
fun RationItem(
        ration: Ration,
        isSelected: Boolean,
        onClick: () -> Unit,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
        modifier: Modifier = Modifier
) {
    Card(
            modifier =
                    modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).clickable {
                        onClick()
                    },
            elevation = if (isSelected) AppSizes.elevationMedium else AppSizes.elevationSmall,
            backgroundColor =
                    if (isSelected) VetNutriColors.Primary.copy(alpha = 0.1f)
                    else MaterialTheme.colors.surface
    ) {
        Row(
                modifier = Modifier.padding(AppSizes.paddingSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            // Informations de la ration
            Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingXXSmall)
            ) {
                Text(
                        text = ration.name,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )

                Text(
                        text = if (ration.actual) "Actuelle" else "Proposée",
                        style = MaterialTheme.typography.caption,
                        color =
                                if (ration.actual) VetNutriColors.Primary
                                else VetNutriColors.Secondary
                )
            }

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(AppSizes.iconSizeSmall)) {
                    Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Éditer",
                            tint = VetNutriColors.Secondary
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(AppSizes.iconSizeSmall)) {
                    Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Supprimer",
                            tint = VetNutriColors.Error
                    )
                }
            }
        }
    }
}
