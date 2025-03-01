package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.AppTypography
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = AppTypography.titleMedium, modifier = modifier)
}

@Composable
fun StandardDivider(modifier: Modifier = Modifier) {
    Divider(modifier = modifier)
}

@Composable
fun StandardButton(
        onClick: () -> Unit,
        text: String,
        modifier: Modifier = Modifier,
        leadingIcon: ImageVector? = null,
        enabled: Boolean = true
) {
    Button(
            onClick = onClick,
            colors =
                    ButtonDefaults.buttonColors(
                            backgroundColor = VetNutriColors.Primary,
                            contentColor = VetNutriColors.OnPrimary
                    ),
            modifier = modifier.height(AppSizes.buttonHeight),
            enabled = enabled
    ) {
        Row(
                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                        leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(AppSizes.iconSizeSmall)
                )
            }
            Text(text = text, style = AppTypography.bodyMedium)
        }
    }
}

@Composable
fun StandardIconButton(
        onClick: () -> Unit,
        icon: ImageVector,
        contentDescription: String?,
        modifier: Modifier = Modifier,
        tint: Color = MaterialTheme.colors.onSurface
) {
    IconButton(onClick = onClick, modifier = modifier.size(AppSizes.iconSizeMedium)) {
        Icon(
                icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(AppSizes.iconSizeSmall),
                tint = tint
        )
    }
}

@Composable
fun StandardCard(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        isSelected: Boolean = false,
        content: @Composable () -> Unit
) {
    Card(
            modifier = modifier.fillMaxWidth(),
            elevation =
                    if (isSelected) AppSizes.cardElevationSelected
                    else AppSizes.cardElevationNormal,
            backgroundColor =
                    if (isSelected) VetNutriColors.Secondary else MaterialTheme.colors.surface
    ) {
        Column(
                modifier = Modifier.clickable(onClick = onClick).padding(AppSizes.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
        ) { content() }
    }
}

@Composable
fun TextFieldNut(value: String?, label: String) {
    TextField(
            value = value ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
    )
}

@Composable
fun <T> ComboBox(
        items: List<T>,
        selectedItem: T,
        onItemSelected: (T) -> Unit,
        itemToString: (T) -> String,
        modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = itemToString(selectedItem)

    Column(modifier = modifier) {
        Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.clickable { expanded = !expanded }
        ) {
            OutlinedTextField(
                    value = selectedText,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                                if (expanded) AppIcons.ArrowDropUp else AppIcons.ArrowDropDown,
                                contentDescription = if (expanded) "Réduire" else "Développer"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
            )
        }

        DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                        }
                ) { Text(itemToString(item)) }
            }
        }
    }
}

@Composable
fun ConsultationHeader(onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        SectionTitle(text = "Consultations")
        StandardButton(
                onClick = onAddClick,
                text = "Nouvelle consultation",
                leadingIcon = AppIcons.Add
        )
    }
}

@Composable
fun ConsultationListItem(
        consultation: ConsultationEv,
        isSelected: Boolean,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
        modifier: Modifier = Modifier
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        ConfirmDialog(
                title = "Supprimer la consultation",
                message = "Êtes-vous sûr de vouloir supprimer cette consultation ?",
                onConfirm = {
                    onDelete()
                    showDeleteConfirmation = false
                },
                onDismiss = { showDeleteConfirmation = false }
        )
    }

    StandardCard(onClick = onEdit, isSelected = isSelected, modifier = modifier) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                Text(
                        text = consultation.date?.toString() ?: "Date non spécifiée",
                        style = AppTypography.titleMedium
                )
                Text(
                        text = consultation.objectConsult,
                        style = AppTypography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.padding(start = AppSizes.paddingSmall)
            ) {
                Icon(
                        imageVector = AppIcons.Delete,
                        contentDescription = "Supprimer la consultation"
                )
            }
        }
    }
}

@Composable
fun SectionSubtitle(text: String, modifier: Modifier = Modifier) {
    Text(
            text = text,
            style = AppTypography.titleSmall,
            modifier = modifier.padding(bottom = AppSizes.paddingSmall)
    )
}
