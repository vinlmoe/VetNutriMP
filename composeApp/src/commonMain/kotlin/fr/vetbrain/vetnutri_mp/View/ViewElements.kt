package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Labelable
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateNonComposable
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
        Text(text = text, style = MaterialTheme.typography.h6, modifier = modifier)
}

@Composable
fun SectionSubtitle(text: String, modifier: Modifier = Modifier) {
        Text(text = text, style = MaterialTheme.typography.subtitle1, modifier = modifier)
}

@Composable
fun StandardDivider(modifier: Modifier = Modifier) {
        Divider(
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                thickness = 1.dp,
                modifier = modifier
        )
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
                modifier = modifier,
                enabled = enabled,
                colors =
                        ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.primary,
                                contentColor = MaterialTheme.colors.onPrimary
                        )
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
                        Text(text)
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
                        modifier =
                                Modifier.clickable(onClick = onClick)
                                        .padding(AppSizes.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) { content() }
        }
}

@Composable
fun TextFieldNut(value: Labelable?, label: String) {
        TextField(
                value = value?.label ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(label.translate()) },
                modifier = Modifier
        )
}

@Composable
private fun LabelableDropdownMenuItem(item: Labelable, onSelect: (Labelable) -> Unit) {
        DropdownMenuItem(onClick = { onSelect(item) }) {
                Text(
                        item.label?.translateNonComposable()
                                ?: General.VALIDATE.translateNonComposable()
                )
        }
}

@Preview
@Composable
fun ComboBox(
        items: List<Labelable>,
        init: Labelable?,
        modifier: Modifier = Modifier,
        label: String = "",
        onItemSelected: (String) -> Unit
) {
        var expanded by remember { mutableStateOf(false) }
        var selectedObject by remember { mutableStateOf(init) }
        var selectedText by remember {
                mutableStateOf(
                        TextFieldValue(
                                init?.label?.translateNonComposable()
                                        ?: General.VALIDATE.translateNonComposable()
                        )
                )
        }

        Column(modifier = modifier) {
                Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier =
                                Modifier.clip(RoundedCornerShape(4.dp))
                                        .border(
                                                BorderStroke(1.dp, Color.LightGray),
                                                RoundedCornerShape(4.dp)
                                        )
                                        .clickable { expanded = !expanded }
                ) {
                        OutlinedTextField(
                                value = selectedText.text,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(label.ifEmpty { General.CALCULATE.translate() }) },
                                placeholder = { Text(General.CALCULATE.translate()) },
                                trailingIcon = {
                                        Icon(
                                                if (expanded) AppIcons.ArrowDropUp
                                                else AppIcons.ArrowDropDown,
                                                contentDescription =
                                                        if (expanded) "general.reduce".translate()
                                                        else "general.develop".translate()
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
                                LabelableDropdownMenuItem(
                                        item = item,
                                        onSelect = { selectedItem ->
                                                selectedText =
                                                        TextFieldValue(
                                                                selectedItem.label
                                                                        ?.translateNonComposable()
                                                                        ?: General.VALIDATE
                                                                                .translateNonComposable()
                                                        )
                                                expanded = false
                                                onItemSelected(selectedItem.label ?: "null")
                                                selectedObject = selectedItem
                                        }
                                )
                        }
                }
        }
}

@Composable
fun SexSelector(selectedSex: Sex, onSexSelected: (Sex) -> Unit, modifier: Modifier = Modifier) {
        var expanded by remember { mutableStateOf(false) }

        Column(modifier = modifier) {
                SectionSubtitle(text = "animal.sex".translate())
                Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth()
                        ) {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(selectedSex.displayName)
                                        Icon(
                                                AppIcons.ArrowDropDown,
                                                contentDescription = "general.expand".translate(),
                                                modifier = Modifier.size(AppSizes.iconSizeMedium)
                                        )
                                }
                        }
                        DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth()
                        ) {
                                Sex.values().forEach { sex ->
                                        DropdownMenuItem(
                                                onClick = {
                                                        onSexSelected(sex)
                                                        expanded = false
                                                }
                                        ) {
                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement =
                                                                Arrangement.SpaceBetween,
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Text(sex.displayName)
                                                        Text(
                                                                "(${(sex.coef * 100).toInt()}%)",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption.copy(
                                                                                fontSize =
                                                                                        AppSizes.fontSizeCaption,
                                                                                color =
                                                                                        MaterialTheme
                                                                                                .colors
                                                                                                .primary
                                                                        )
                                                        )
                                                }
                                        }
                                }
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
                SectionTitle(text = "consultation.listTitle".translate())
                StandardButton(
                        onClick = onAddClick,
                        text = "consultation.new".translate(),
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
                        title = "consultation.delete".translate(),
                        message = "consultation.deleteConfirm".translate(),
                        onConfirm = {
                                onDelete()
                                showDeleteConfirmation = false
                        },
                        onDismiss = { showDeleteConfirmation = false }
                )
        }

        StandardCard(onClick = onEdit, isSelected = isSelected, modifier = modifier) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text = consultation.date?.toString()
                                                        ?: "consultation.unspecifiedDate".translate(),
                                        style =
                                                MaterialTheme.typography.subtitle1.copy(
                                                        fontSize = AppSizes.fontSizeSubtitle1
                                                )
                                )
                                StandardIconButton(
                                        onClick = onDelete,
                                        icon = AppIcons.Delete,
                                        contentDescription = "consultation.delete".translate(),
                                        tint = MaterialTheme.colors.error
                                )
                        }

                        Text(
                                text = consultation.objectConsult,
                                style =
                                        MaterialTheme.typography.body1.copy(
                                                fontSize = AppSizes.fontSizeBody1
                                        ),
                                maxLines = 2
                        )

                        if (consultation.rations.isNotEmpty()) {
                                Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingXSmall)
                                ) {
                                        Icon(
                                                AppIcons.List,
                                                contentDescription =
                                                        "consultation.rations".translate(),
                                                modifier = Modifier.size(AppSizes.iconSizeSmall),
                                                tint = MaterialTheme.colors.primary
                                        )
                                        Text(
                                                text =
                                                        "${consultation.rations.size} " +
                                                                "consultation.rations".translate(),
                                                style =
                                                        MaterialTheme.typography.caption.copy(
                                                                fontSize = AppSizes.fontSizeCaption
                                                        )
                                        )
                                }
                        }
                }
        }
}

@Composable
fun StandardSwitch(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        label: String,
        modifier: Modifier = Modifier
) {
        Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
                Text(
                        text = label,
                        style =
                                MaterialTheme.typography.body2.copy(
                                        fontSize = AppSizes.fontSizeBody2,
                                        color =
                                                if (checked) MaterialTheme.colors.error
                                                else MaterialTheme.colors.primary
                                )
                )
                Switch(
                        checked = checked,
                        onCheckedChange = onCheckedChange,
                        colors =
                                SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colors.error,
                                        checkedTrackColor =
                                                MaterialTheme.colors.error.copy(alpha = 0.5f),
                                        uncheckedThumbColor = MaterialTheme.colors.primary,
                                        uncheckedTrackColor =
                                                MaterialTheme.colors.primary.copy(alpha = 0.5f)
                                )
                )
        }
}

@Composable
fun ConfirmDialog(title: String, message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(title) },
                text = { Text(message) },
                confirmButton = {
                        Button(
                                onClick = onConfirm,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = MaterialTheme.colors.primary,
                                                contentColor = MaterialTheme.colors.onPrimary
                                        )
                        ) { Text("general.validate".translate()) }
                },
                dismissButton = {
                        Button(
                                onClick = onDismiss,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = MaterialTheme.colors.secondary,
                                                contentColor = MaterialTheme.colors.onSecondary
                                        )
                        ) { Text("general.cancel".translate()) }
                }
        )
}

@Composable
fun AppTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        modifier: Modifier = Modifier,
        singleLine: Boolean = true,
        maxLines: Int = 1
) {
        OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                singleLine = singleLine,
                maxLines = maxLines,
                modifier = modifier
        )
}
