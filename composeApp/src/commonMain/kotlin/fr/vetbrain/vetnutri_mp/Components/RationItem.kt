package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Functions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.RationAggregator
import fr.vetbrain.vetnutri_mp.Enumer.RationAnalysisScope
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Ration as RationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.TextUtils

/**
 * Composant pour afficher une ration dans une liste
 *
 * @param ration La ration à afficher
 * @param isSelected Indique si la ration est sélectionnée
 * @param onClick Action à exécuter lors du clic sur la ration
 * @param onEdit Action à exécuter pour éditer la ration
 * @param onDelete Action à exécuter pour supprimer la ration
 * @param onDuplicate Action à exécuter pour dupliquer la ration (optionnel)
 * @param onEditCoef Callback pour éditer le coefficient de la ration (optionnel)
 * @param modifier Modificateur optionnel
 */
@Composable
fun RationItem(
        ration: Ration,
        isSelected: Boolean,
        onClick: () -> Unit,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
        onDuplicate: (() -> Unit)? = null,
        onEditCoef: ((Double) -> Unit)? = null,
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
                                        fontWeight =
                                                if (isSelected) FontWeight.Bold
                                                else FontWeight.Normal
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                                text =
                                                        if (ration.actual) translate(RationKeys.ACTUAL)
                                                        else translate(RationKeys.PROPOSED),
                                                style = MaterialTheme.typography.caption,
                                                color =
                                                        if (ration.actual) Color(0xFFFF9800)
                                                        else VetNutriColors.Secondary
                                        )
                                        Spacer(modifier = Modifier.width(AppSizes.paddingXSmall))
                                        Text(
                                                text =
                                                        translate(
                                                                "ration.coefficientFormat",
                                                                TextUtils.formatDecimal(ration.coef.toDouble(), 2)
                                                        ),
                                                style = MaterialTheme.typography.caption,
                                                color = Color.Gray,
                                                modifier =
                                                        Modifier.clickable(
                                                                enabled = onEditCoef != null
                                                        ) { onEditCoef?.invoke(ration.coef) }
                                        )
                                }
                        }
                        // Actions
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)) {
                                if (onDuplicate != null) {
                                        IconButton(
                                                onClick = onDuplicate,
                                                modifier = Modifier.size(AppSizes.iconSizeSmall)
                                        ) {
                                                Icon(
                                                        imageVector = Icons.Filled.ContentCopy,
                                                        contentDescription = translate("ration.duplicateAction"),
                                                        tint = VetNutriColors.Secondary
                                                )
                                        }
                                }
                                IconButton(
                                        onClick = onEdit,
                                        modifier = Modifier.size(AppSizes.iconSizeSmall)
                                ) {
                                        Icon(
                                                imageVector = Icons.Filled.Edit,
                                                contentDescription = translate(General.EDIT),
                                                tint = VetNutriColors.Secondary
                                        )
                                }
                                IconButton(
                                        onClick = onDelete,
                                        modifier = Modifier.size(AppSizes.iconSizeSmall)
                                ) {
                                        Icon(
                                                imageVector = Icons.Filled.Delete,
                                                contentDescription = translate(General.DELETE),
                                                tint = VetNutriColors.Error
                                        )
                                }
                        }
                }
        }
}

/**
 * Entrée de liste représentant un groupe de rations analysées comme une ration unique.
 *
 * Sélectionner cette entrée bascule l'analyse en mode groupé : toutes les rations du groupe
 * (actuelles ou proposées) sont moyennées, chacune pesant son coefficient.
 *
 * @param scope Le périmètre d'analyse représenté (actuelles ou proposées)
 * @param rations Les rations composant le groupe
 * @param isSelected Indique si ce groupe est le périmètre d'analyse courant
 * @param onClick Action à exécuter lors du clic sur le groupe
 * @param modifier Modificateur optionnel
 */
@Composable
fun RationGroupItem(
        scope: RationAnalysisScope,
        rations: List<Ration>,
        isSelected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
        val sommeCoefficients = RationAggregator.sommeCoefficients(rations)
        val groupColor =
                if (scope.cibleRationsActuelles) Color(0xFFFF9800)
                else VetNutriColors.Secondary

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
                        Icon(
                                imageVector = Icons.Filled.Functions,
                                contentDescription = null,
                                tint = groupColor,
                                modifier = Modifier.size(AppSizes.iconSizeSmall)
                        )
                        Spacer(modifier = Modifier.width(AppSizes.paddingXSmall))
                        Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingXXSmall)
                        ) {
                                Text(
                                        text = RationAggregator.nomGroupe(scope),
                                        style = MaterialTheme.typography.subtitle1,
                                        color = groupColor,
                                        fontWeight =
                                                if (isSelected) FontWeight.Bold
                                                else FontWeight.Normal
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                                text =
                                                        translate(
                                                                RationKeys.GROUP_RATION_COUNT,
                                                                rations.size.toString()
                                                        ),
                                                style = MaterialTheme.typography.caption,
                                                color = groupColor
                                        )
                                        Spacer(modifier = Modifier.width(AppSizes.paddingXSmall))
                                        Text(
                                                text =
                                                        translate(
                                                                RationKeys.GROUP_TOTAL_COEF,
                                                                TextUtils.formatDecimal(
                                                                        sommeCoefficients,
                                                                        2
                                                                )
                                                        ),
                                                style = MaterialTheme.typography.caption,
                                                color = Color.Gray
                                        )
                                }
                        }
                }
        }
}
