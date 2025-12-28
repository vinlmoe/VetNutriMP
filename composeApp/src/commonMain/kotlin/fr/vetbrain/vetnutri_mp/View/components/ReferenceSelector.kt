package fr.vetbrain.vetnutri_mp.View.Components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate

/**
 * Composant pour sélectionner une référence nutritionnelle
 *
 * @param references Liste des références disponibles
 * @param selectedReferenceId ID de la référence actuellement sélectionnée
 * @param onReferenceSelected Callback appelé lors de la sélection d'une référence
 * @param onDismiss Callback appelé pour fermer le sélecteur
 * @param title Titre du sélecteur
 * @param allowNone Permet de ne sélectionner aucune référence
 */
@Composable
fun ReferenceSelector(
        references: List<ReferenceEv>,
        selectedReferenceId: String?,
        onReferenceSelected: (String?) -> Unit,
        onDismiss: () -> Unit,
        title: String = translate(LocalizationKeys.Reference.SELECT_TITLE),
        allowNone: Boolean = true,
        modifier: Modifier = Modifier
) {
    AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                        text = title,
                        style = MaterialTheme.typography.h6,
                        color = VetNutriColors.Primary
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (references.isEmpty()) {
                        Box(
                                modifier = Modifier.fillMaxWidth().height(AppSizes.cardMinHeight),
                                contentAlignment = Alignment.Center
                        ) {
                            Text(
                                    text = translate(LocalizationKeys.Reference.NONE_AVAILABLE),
                                    style = MaterialTheme.typography.body2,
                                    color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .height(AppSizes.cardMinHeight.times(3f)),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                            // Option "Aucune référence" si autorisée
                            if (allowNone) {
                                item {
                                    ReferenceItem(
                                            reference = null,
                                            isSelected = selectedReferenceId == null,
                                            onSelected = { onReferenceSelected(null) }
                                    )
                                }
                            }

                            // Liste des références
                            items(references) { reference ->
                                ReferenceItem(
                                        reference = reference,
                                        isSelected = selectedReferenceId == reference.uuid,
                                        onSelected = { onReferenceSelected(reference.uuid) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = translate(LocalizationKeys.AnalNut.CLOSE), color = VetNutriColors.Primary)
                }
            },
            modifier = modifier
    )
}

/** Composant pour afficher un élément de référence dans la liste */
@Composable
private fun ReferenceItem(
        reference: ReferenceEv?,
        isSelected: Boolean,
        onSelected: () -> Unit,
        modifier: Modifier = Modifier
) {
    Card(
            modifier = modifier.fillMaxWidth(),
            elevation = if (isSelected) AppSizes.elevationMedium else AppSizes.elevationSmall,
            backgroundColor =
                    if (isSelected) VetNutriColors.Primary.copy(alpha = 0.1f)
                    else VetNutriColors.Surface
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingMedium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = reference?.nom ?: translate(LocalizationKeys.Reference.NONE),
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color =
                                if (isSelected) VetNutriColors.Primary
                                else MaterialTheme.colors.onSurface
                )

                if (reference != null) {
                    Text(
                            text = translate(LocalizationKeys.Reference.SPECIES_PREFIX, reference.espece.label),
                            style = MaterialTheme.typography.body2,
                            color = Color.Gray
                    )

                    if (reference.description.isNotBlank()) {
                        Text(
                                text = reference.description,
                                style = MaterialTheme.typography.caption,
                                color = Color.Gray
                        )
                    }

                    if (reference.maladie && reference.nomMaladie.isNotBlank()) {
                        Text(
                                text = translate(LocalizationKeys.Reference.DISEASE_PREFIX, reference.nomMaladie),
                                style = MaterialTheme.typography.caption,
                                color = VetNutriColors.Secondary
                        )
                    }
                }
            }

            // Bouton de sélection
            IconButton(onClick = onSelected) {
                Icon(
                        imageVector =
                                if (isSelected) AppIcons.Check
                                else AppIcons.Add,
                        contentDescription = if (isSelected) translate(LocalizationKeys.Reference.SELECTED) else translate(LocalizationKeys.Reference.SELECT),
                        tint = if (isSelected) VetNutriColors.Primary else Color.Gray
                )
            }
        }
    }
}

/** Composant pour afficher un résumé des références sélectionnées */
@Composable
fun ReferencesSummary(
        referenceGeneraleId: String?,
        referencesMaladies: List<String>,
        availableReferences: List<ReferenceEv>,
        modifier: Modifier = Modifier
) {
    Card(
            modifier = modifier.fillMaxWidth(),
            elevation = AppSizes.elevationSmall,
            backgroundColor = VetNutriColors.Surface
    ) {
        Column(
                modifier = Modifier.padding(AppSizes.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
        ) {
            Text(
                    text = translate(LocalizationKeys.Reference.ACTIVE_REFS_TITLE),
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    color = VetNutriColors.Primary
            )

            Divider(color = VetNutriColors.Primary.copy(alpha = 0.3f))

            // Référence générale
            val referenceGenerale = availableReferences.find { it.uuid == referenceGeneraleId }
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = "Référence générale:",
                        style = MaterialTheme.typography.body2,
                        fontWeight = FontWeight.Medium
                )
                Text(
                        text = referenceGenerale?.nom ?: translate(LocalizationKeys.General.NONE),
                        style = MaterialTheme.typography.body2,
                        color =
                                if (referenceGenerale != null) VetNutriColors.Primary
                                else Color.Gray
                )
            }

            // Références de maladies
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
            ) {
                Text(
                        text = translate(LocalizationKeys.Reference.DISEASE_REFS_LABEL),
                        style = MaterialTheme.typography.body2,
                        fontWeight = FontWeight.Medium
                )
                Column {
                    if (referencesMaladies.isEmpty()) {
                        Text(
                                text = translate(LocalizationKeys.General.NONE),
                                style = MaterialTheme.typography.body2,
                                color = Color.Gray
                        )
                    } else {
                        referencesMaladies.forEach { referenceId ->
                            val reference = availableReferences.find { it.uuid == referenceId }
                            Text(
                                    text = "• ${reference?.nom ?: translate(LocalizationKeys.Reference.UNKNOWN)}",
                                    style = MaterialTheme.typography.body2,
                                    color = VetNutriColors.Secondary
                            )
                        }
                    }
                }
            }
        }
    }
}
