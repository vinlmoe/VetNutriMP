package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailSection

/** Option d'élément de menu pour la navigation dans la vue de détail de l'animal */
data class MenuOption(val section: AnimalDetailSection, val title: String, val icon: ImageVector)

/**
 * Composant réutilisable pour afficher une ligne d'information avec une étiquette et une valeur
 *
 * @param label L'étiquette à afficher
 * @param value La valeur à afficher
 */
@Composable
fun InfoRow(label: String, value: String) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = AppSizes.paddingXSmall),
                horizontalArrangement = Arrangement.Start
        ) {
                Text(
                        text = "$label :",
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.width(AppSizes.textFieldHeight.times(2.7f))
                )
                Text(text = value, style = MaterialTheme.typography.body1)
        }
}

/**
 * Composant réutilisable pour afficher une carte de consultation
 *
 * @param consultation La consultation à afficher
 * @param isSelected Indique si la consultation est sélectionnée
 * @param onEdit Action à exécuter lors du clic sur le bouton d'édition
 * @param onDelete Action à exécuter lors du clic sur le bouton de suppression
 * @param isDeleteEnabled Indique si le bouton de suppression est activé
 * @param onClick Action à exécuter lors du clic sur la carte
 * @param modifier Modificateur optionnel pour personnaliser l'apparence
 */
@Composable
fun ConsultationCard(
        consultation: ConsultationEv,
        isSelected: Boolean,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
        isDeleteEnabled: Boolean = true,
        onClick: () -> Unit = {},
        modifier: Modifier = Modifier
) {
        Card(
                modifier =
                        modifier.fillMaxWidth()
                                .padding(
                                        vertical = AppSizes.paddingSmall,
                                        horizontal = AppSizes.paddingXXSmall
                                ),
                elevation =
                        if (isSelected) AppSizes.cardElevationSelected
                        else AppSizes.cardElevationNormal,
                backgroundColor =
                        if (isSelected) VetNutriColors.Primary.copy(alpha = 0.12f)
                        else MaterialTheme.colors.surface,
                shape = MaterialTheme.shapes.medium,
                border =
                        if (isSelected)
                                BorderStroke(
                                        AppSizes.borderWidth,
                                        VetNutriColors.Primary.copy(alpha = 0.5f)
                                )
                        else
                                BorderStroke(
                                        AppSizes.borderWidth.times(0.5f),
                                        Color.LightGray.copy(alpha = 0.5f)
                                )
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                        Column(
                                modifier =
                                        Modifier.weight(1f)
                                                .clickable(onClick = onClick)
                                                .padding(AppSizes.paddingMedium),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                // Date de la consultation
                                Row(
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingSmall),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Icon(
                                                AppIcons.DateRange,
                                                contentDescription = null,
                                                tint = VetNutriColors.Primary,
                                                modifier = Modifier.size(AppSizes.iconSizeSmall)
                                        )
                                        Text(
                                                text = consultation.date?.toString()
                                                                ?: "Date inconnue",
                                                style = MaterialTheme.typography.subtitle1,
                                                color = VetNutriColors.Primary
                                        )
                                }

                                // Afficher le poids si disponible
                                consultation.weight?.let { weight ->
                                        Row(
                                                horizontalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall),
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Icon(
                                                        AppIcons.Weight,
                                                        contentDescription = null,
                                                        tint = Color.Gray,
                                                        modifier =
                                                                Modifier.size(
                                                                        AppSizes.iconSizeXSmall
                                                                )
                                                )
                                                Text(
                                                        text = "$weight kg",
                                                        style = MaterialTheme.typography.caption,
                                                        color = Color.Gray
                                                )
                                        }
                                }

                                Divider(
                                        modifier =
                                                Modifier.padding(
                                                        vertical = AppSizes.paddingXXSmall
                                                ),
                                        color = Color.LightGray,
                                        thickness = AppSizes.dividerHeight.times(0.5f)
                                )

                                // Motif de la consultation
                                Row(
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingSmall),
                                        verticalAlignment = Alignment.Top
                                ) {
                                        Icon(
                                                AppIcons.Info,
                                                contentDescription = null,
                                                tint = VetNutriColors.Secondary,
                                                modifier = Modifier.size(AppSizes.iconSizeSmall)
                                        )
                                        Text(
                                                text = consultation.objectConsult,
                                                style = MaterialTheme.typography.body1,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                        )
                                }

                                // Afficher un aperçu des observations si disponibles
                                if (!consultation.observation.isNullOrEmpty()) {
                                        Row(
                                                horizontalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall),
                                                verticalAlignment = Alignment.Top
                                        ) {
                                                Icon(
                                                        AppIcons.Info,
                                                        contentDescription = null,
                                                        tint = Color.Gray,
                                                        modifier =
                                                                Modifier.size(
                                                                        AppSizes.iconSizeSmall
                                                                )
                                                )
                                                Text(
                                                        text = consultation.observation,
                                                        style = MaterialTheme.typography.body2,
                                                        color = Color.Gray,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                )
                                        }
                                }

                                // Afficher le nombre de rations si disponibles
                                if (consultation.rations.isNotEmpty()) {
                                        Row(
                                                horizontalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall),
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Icon(
                                                        AppIcons.Ration,
                                                        contentDescription = null,
                                                        tint = VetNutriColors.Secondary,
                                                        modifier =
                                                                Modifier.size(
                                                                        AppSizes.iconSizeXSmall
                                                                )
                                                )
                                                Text(
                                                        text =
                                                                "Rations: ${consultation.rations.size}",
                                                        style = MaterialTheme.typography.caption,
                                                        color = VetNutriColors.Secondary
                                                )
                                        }
                                }
                        }
                        // Boutons d'action dans une colonne séparée
                        Column(
                                modifier =
                                        Modifier.padding(
                                                top = AppSizes.paddingMedium,
                                                end = AppSizes.paddingMedium
                                        ),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                // Bouton d'édition
                                IconButton(
                                        onClick = onEdit,
                                        modifier = Modifier.size(AppSizes.iconSizeMedium)
                                ) {
                                        Icon(
                                                imageVector = AppIcons.Edit,
                                                contentDescription = "Modifier la consultation",
                                                tint = VetNutriColors.Primary,
                                                modifier = Modifier.size(AppSizes.iconSizeSmall)
                                        )
                                }

                                // Bouton de suppression
                                IconButton(
                                        onClick = onDelete,
                                        enabled = isDeleteEnabled,
                                        modifier = Modifier.size(AppSizes.iconSizeMedium)
                                ) {
                                        Icon(
                                                imageVector = AppIcons.Delete,
                                                contentDescription = "Supprimer la consultation",
                                                tint =
                                                        if (isDeleteEnabled) Color.Red
                                                        else Color.Gray.copy(alpha = 0.5f),
                                                modifier = Modifier.size(AppSizes.iconSizeSmall)
                                        )
                                }
                        }
                }
        }
}

/**
 * Composant réutilisable pour afficher un élément de ration dans une liste
 *
 * @param ration La ration à afficher
 * @param isSelected Indique si la ration est sélectionnée
 * @param onClick Action à exécuter lors du clic sur l'élément
 * @param onDelete Action à exécuter lors du clic sur le bouton de suppression
 * @param isDeleteEnabled Indique si le bouton de suppression est activé
 * @param onEdit Action à exécuter lors du clic sur le bouton d'édition
 * @param onDuplicate Action à exécuter lors du clic sur le bouton de duplication
 * @param modifier Modificateur optionnel pour personnaliser l'apparence
 */
@Composable
fun RationItem(
        ration: Ration,
        isSelected: Boolean,
        onClick: () -> Unit,
        onDelete: () -> Unit = {},
        isDeleteEnabled: Boolean = true,
        onEdit: () -> Unit = {},
        onDuplicate: () -> Unit = {},
        modifier: Modifier = Modifier
) {
        Row(
                modifier =
                        modifier.fillMaxWidth()
                                .clickable(onClick = onClick)
                                .background(
                                        if (isSelected) VetNutriColors.Primary.copy(alpha = 0.12f)
                                        else Color.Transparent
                                )
                                .border(
                                        width = if (isSelected) AppSizes.borderWidth else 0.dp,
                                        color =
                                                if (isSelected)
                                                        VetNutriColors.Primary.copy(alpha = 0.5f)
                                                else Color.Transparent,
                                        shape = MaterialTheme.shapes.small
                                )
                                .padding(AppSizes.paddingSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
                Column(modifier = Modifier.weight(1f)) {
                        Text(
                                text = ration.name,
                                style = MaterialTheme.typography.subtitle1,
                                color =
                                        if (isSelected) VetNutriColors.Primary
                                        else MaterialTheme.colors.onSurface
                        )
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                Text(
                                        text = if (ration.actual) "Proposée" else "Actuelle",
                                        style = MaterialTheme.typography.caption,
                                        color =
                                                if (ration.actual) VetNutriColors.Primary
                                                else Color.Gray
                                )
                                Text(
                                        text = "Coef: ${ration.coef}",
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Gray
                                )
                        }
                }

                // Boutons d'action (édition et suppression)
                Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        IconButton(
                                onClick = { onEdit() },
                                modifier = Modifier.size(AppSizes.iconSizeLarge)
                        ) {
                                Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Modifier",
                                        tint = VetNutriColors.Primary.copy(alpha = 0.8f)
                                )
                        }

                        IconButton(
                                onClick = { onDuplicate() },
                                modifier = Modifier.size(AppSizes.iconSizeLarge)
                        ) {
                                Icon(
                                        AppIcons.ContentCopy,
                                        contentDescription = "Dupliquer",
                                        tint = VetNutriColors.Primary.copy(alpha = 0.8f)
                                )
                        }

                        IconButton(
                                onClick = { onDelete() },
                                modifier = Modifier.size(AppSizes.iconSizeLarge),
                                enabled = isDeleteEnabled
                        ) {
                                Icon(
                                        AppIcons.Delete,
                                        contentDescription = "Supprimer",
                                        tint =
                                                if (isDeleteEnabled) Color.Red.copy(alpha = 0.8f)
                                                else Color.Gray.copy(alpha = 0.5f)
                                )
                        }
                }
        }
}

/**
 * Composant réutilisable pour afficher un élément d'aliment dans une liste
 *
 * @param aliment L'aliment à afficher
 * @param isEditing Indique si cet aliment est en mode édition
 * @param onStartEditing Callback appelé quand l'édition commence
 * @param onEndEditing Callback appelé quand l'édition se termine sans changement
 * @param onQuantityChange Callback appelé quand la quantité est changée et validée
 * @param onDelete Callback appelé lors du clic sur le bouton de suppression
 * @param modifier Modificateur optionnel pour personnaliser l'apparence
 */
@Composable
fun AlimentItem(
        aliment: AlimentRation,
        isEditing: Boolean = false,
        onStartEditing: () -> Unit = {},
        onEndEditing: () -> Unit = {},
        onQuantityChange: (Double) -> Unit = {},
        onDelete: () -> Unit = {},
        modifier: Modifier = Modifier
) {
        var quantityText by
                remember(isEditing) {
                        mutableStateOf(
                                if (isEditing) aliment.quantity.toString()
                                else aliment.quantity.toString()
                        )
                }

        // Fonction de validation de la quantité
        val validateQuantity = {
                val newQuantity = quantityText.toDoubleOrNull() ?: aliment.quantity
                onQuantityChange(newQuantity)
        }

        // Fonction d'annulation de l'édition
        val cancelEdit = { onEndEditing() }

        Row(
                modifier = modifier.fillMaxWidth().padding(AppSizes.paddingSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
                Column(modifier = Modifier.weight(1f)) {
                        Text(
                                text = aliment.aliment?.nom ?: aliment.uuidUnif,
                                style = MaterialTheme.typography.subtitle1
                        )
                        Text(
                                text = "Catégorie: ${aliment.category}",
                                style = MaterialTheme.typography.caption,
                                color = Color.Gray
                        )
                }

                // Affichage et édition de la quantité
                if (isEditing) {
                        // Mode édition de la quantité
                        Row(
                                modifier = Modifier.padding(horizontal = AppSizes.paddingSmall),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                OutlinedTextField(
                                        value = quantityText,
                                        onValueChange = { quantityText = it },
                                        label = { Text("Quantité") },
                                        keyboardOptions =
                                                KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier =
                                                Modifier.width(AppSizes.textFieldHeight.times(2.2f))
                                )
                                Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                                IconButton(onClick = validateQuantity) {
                                        Icon(
                                                AppIcons.Check,
                                                contentDescription = "Valider",
                                                tint = Color.Green
                                        )
                                }
                                IconButton(onClick = cancelEdit) {
                                        Icon(
                                                AppIcons.Close,
                                                contentDescription = "Annuler",
                                                tint = Color.Red.copy(alpha = 0.8f)
                                        )
                                }
                        }
                } else {
                        // Mode affichage de la quantité
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                Text(
                                        text = "${aliment.quantity} g",
                                        style = MaterialTheme.typography.body1,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable(onClick = onStartEditing)
                                )
                                IconButton(onClick = onStartEditing) {
                                        Icon(
                                                AppIcons.Edit,
                                                contentDescription = "Modifier la quantité",
                                                tint = VetNutriColors.Primary
                                        )
                                }
                                IconButton(onClick = onDelete) {
                                        Icon(
                                                AppIcons.Delete,
                                                contentDescription = "Supprimer",
                                                tint = Color.Red.copy(alpha = 0.8f)
                                        )
                                }
                        }
                }
        }
}

/**
 * Composant réutilisable pour afficher une carte générique avec un titre et un contenu
 *
 * @param title Le titre de la carte
 * @param content Le contenu de la carte
 * @param modifier Modificateur optionnel pour personnaliser l'apparence
 */
@Composable
fun ContentCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
        Card(
                modifier = modifier,
                elevation = AppSizes.cardElevationNormal,
                backgroundColor = MaterialTheme.colors.surface
        ) {
                Column(
                        modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                        Text(
                                text = title,
                                style = MaterialTheme.typography.h6,
                                color = VetNutriColors.Primary
                        )

                        Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f))

                        content()
                }
        }
}

/**
 * Composant réutilisable pour afficher un message d'information centré
 *
 * @param message Le message à afficher
 * @param icon L'icône à afficher (par défaut, icône d'information)
 * @param modifier Modificateur optionnel pour personnaliser l'apparence
 */
@Composable
fun CenteredMessage(
        message: String,
        icon: ImageVector = Icons.Default.Info,
        modifier: Modifier = Modifier
) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                        Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(AppSizes.iconSizeLarge)
                        )
                        Text(
                                text = message,
                                style = MaterialTheme.typography.body1,
                                color = Color.Gray
                        )
                }
        }
}

/**
 * Composant réutilisable pour afficher un élément de menu
 *
 * @param option L'option de menu à afficher
 * @param isSelected Indique si l'option est sélectionnée
 * @param onClick Action à exécuter lors du clic sur l'élément
 * @param modifier Modificateur optionnel pour personnaliser l'apparence
 */
@Composable
fun MenuOptionItem(
        option: MenuOption,
        isSelected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
        Row(
                modifier =
                        modifier.fillMaxWidth()
                                .clickable(onClick = onClick)
                                .padding(vertical = AppSizes.paddingSmall),
                verticalAlignment = Alignment.CenterVertically
        ) {
                Icon(
                        imageVector = option.icon,
                        contentDescription = option.title,
                        tint = if (isSelected) VetNutriColors.Primary else Color.Gray
                )
                Spacer(modifier = Modifier.width(AppSizes.paddingMedium))
                Text(
                        text = option.title,
                        style = MaterialTheme.typography.body1,
                        color = if (isSelected) VetNutriColors.Primary else Color.Gray
                )
        }
}
