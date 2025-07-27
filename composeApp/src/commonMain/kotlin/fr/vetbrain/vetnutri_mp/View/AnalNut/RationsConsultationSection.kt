package fr.vetbrain.vetnutri_mp.View.AnalNut

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.CenteredMessage
import fr.vetbrain.vetnutri_mp.Components.RationItem
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

/**
 * Section compacte pour afficher la liste des rations d'une consultation
 *
 * @param consultation La consultation sélectionnée
 * @param rationSelectionnee La ration actuellement sélectionnée
 * @param onSelectRation Callback pour sélectionner une ration
 * @param onEditRation Callback pour éditer une ration
 * @param onDuplicateRation Callback pour dupliquer une ration
 * @param onDeleteRation Callback pour supprimer une ration
 * @param onAddRation Callback pour ajouter une nouvelle ration
 * @param modifier Modificateur d'affichage
 */
@Composable
fun SectionRationsConsultation(
        consultation: ConsultationEv?,
        rationSelectionnee: Ration?,
        onSelectRation: (Ration) -> Unit,
        onEditRation: (Ration) -> Unit,
        onDuplicateRation: (Ration) -> Unit,
        onDeleteRation: (Ration) -> Unit,
        onAddRation: () -> Unit,
        modifier: Modifier = Modifier
) {
        Card(
                modifier = modifier,
                elevation = AppSizes.elevationMedium,
                backgroundColor = MaterialTheme.colors.surface
        ) {
                Column(
                        modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                        // En-tête avec titre et bouton d'ajout
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text = "Rations de la consultation",
                                        style = MaterialTheme.typography.subtitle2,
                                        color = VetNutriColors.Primary
                                )
                                IconButton(
                                        onClick = onAddRation,
                                        modifier = Modifier.size(AppSizes.iconSizeMedium)
                                ) {
                                        Icon(
                                                Icons.Filled.Add,
                                                contentDescription = "Ajouter une ration",
                                                tint = VetNutriColors.Primary
                                        )
                                }
                        }
                        Divider()
                        if (consultation?.rations.isNullOrEmpty()) {
                                CenteredMessage(
                                        message = "Aucune ration disponible",
                                        modifier = Modifier.weight(1f)
                                )
                        } else {
                                LazyColumn(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                        items(consultation?.rations ?: emptyList()) { ration ->
                                                RationItem(
                                                        ration = ration,
                                                        isSelected =
                                                                ration.uuid ==
                                                                        rationSelectionnee?.uuid,
                                                        onClick = { onSelectRation(ration) },
                                                        onEdit = { onEditRation(ration) },
                                                        onDelete = { onDeleteRation(ration) },
                                                        onDuplicate = { onDuplicateRation(ration) },
                                                        onEditCoef = null // à relier si besoin
                                                )
                                        }
                                }
                        }
                }
        }
}
