package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.isIosPlatform

/**
 * Composant générique de liste déroulante basé sur BasicTextField pour un contrôle total
 *
 * @param T Type d'élément pour la liste déroulante
 * @param label Libellé du champ
 * @param selectedValue Valeur actuellement sélectionnée
 * @param options Liste des options disponibles
 * @param onValueChange Callback appelé lorsqu'une valeur est sélectionnée
 * @param valueToString Fonction de conversion des valeurs en chaînes affichables
 * @param modifier Modificateur optionnel
 * @param enabled Si le champ est activé (par défaut: true)
 * @param height Hauteur personnalisable du champ (par défaut: 40.dp)
 * @param fontSize Taille de police (par défaut: 12.sp)
 * @param labelFontSize Taille de police du label (par défaut: 10.sp)
 * @param borderWidth Épaisseur de bordure (par défaut: 0.5.dp)
 */
@Composable
fun <T> DropdownField(
        label: String,
        selectedValue: T?,
        options: List<T>,
        onValueChange: (T) -> Unit,
        valueToString: (T) -> String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        height: Dp = 40.dp,
        fontSize: TextUnit = 12.sp,
        labelFontSize: TextUnit = 10.sp,
        borderWidth: Dp = 0.5.dp
) {
        var expanded by remember { mutableStateOf(false) }
        val displayValue = remember(selectedValue) { 
            selectedValue?.let { valueToString(it) } ?: "Sélectionner" 
        }

        Column(modifier = modifier) {
                Box {
                        BasicTextField(
                                value = displayValue,
                                onValueChange = {},
                                readOnly = true,
                                textStyle =
                                        LocalTextStyle.current.copy(
                                                fontSize = fontSize,
                                                color =
                                                        if (enabled) MaterialTheme.colors.onSurface
                                                        else
                                                                MaterialTheme.colors.onSurface.copy(
                                                                        alpha = 0.6f
                                                                )
                                        ),
                                modifier = Modifier.fillMaxWidth().height(height),
                                decorationBox = { innerTextField ->
                                        Box(
                                                modifier =
                                                        Modifier.fillMaxSize()
                                                                .clickable(enabled = enabled) {
                                                                        if (enabled)
                                                                                expanded = !expanded
                                                                }
                                                                .border(
                                                                        width = borderWidth,
                                                                        color =
                                                                                when {
                                                                                        !enabled ->
                                                                                                MaterialTheme
                                                                                                        .colors
                                                                                                        .onSurface
                                                                                                        .copy(
                                                                                                                alpha =
                                                                                                                        0.3f
                                                                                                        )
                                                                                        expanded ->
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                                        .copy(
                                                                                                                alpha =
                                                                                                                        0.6f
                                                                                                        )
                                                                                        else ->
                                                                                                MaterialTheme
                                                                                                        .colors
                                                                                                        .onSurface
                                                                                                        .copy(
                                                                                                                alpha =
                                                                                                                        0.4f
                                                                                                        )
                                                                                },
                                                                        shape =
                                                                                RoundedCornerShape(
                                                                                        4.dp
                                                                                )
                                                                )
                                                                .padding(
                                                                        horizontal = 8.dp,
                                                                        vertical = 6.dp
                                                                )
                                        ) {
                                                Row(
                                                        modifier = Modifier.fillMaxSize(),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        // Label fixe (pas d'animation flottante)
                                                        Text(
                                                                text = "$label: ",
                                                                fontSize = labelFontSize,
                                                                color =
                                                                        MaterialTheme.colors
                                                                                .onSurface.copy(
                                                                                alpha =
                                                                                        if (enabled)
                                                                                                0.7f
                                                                                        else 0.5f
                                                                        ),
                                                                modifier =
                                                                        Modifier.padding(end = 4.dp)
                                                        )

                                                        // Contenu du champ
                                                        Box(modifier = Modifier.weight(1f)) {
                                                                if (displayValue == "Sélectionner"
                                                                ) {
                                                                        Text(
                                                                                text = displayValue,
                                                                                fontSize = fontSize,
                                                                                color =
                                                                                        MaterialTheme
                                                                                                .colors
                                                                                                .onSurface
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                if (enabled
                                                                                                                )
                                                                                                                        0.5f
                                                                                                                else
                                                                                                                        0.3f
                                                                                                ),
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Normal
                                                                        )
                                                                } else {
                                                                        innerTextField()
                                                                }
                                                        }

                                                        // Icône dropdown
                                                        Icon(
                                                                Icons.Default.ArrowDropDown,
                                                                contentDescription = "Dropdown",
                                                                modifier = Modifier.size(18.dp),
                                                                tint =
                                                                        MaterialTheme.colors
                                                                                .onSurface.copy(
                                                                                alpha =
                                                                                        if (enabled)
                                                                                                0.6f
                                                                                        else 0.3f
                                                                        )
                                                        )
                                                }
                                        }
                                }
                        )

                        DropdownMenu(
                                expanded = expanded && enabled,
                                onDismissRequest = {
                                        if (!isIosPlatform) {
                                                expanded = false
                                        }
                                },
                                modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                                options.forEach { option ->
                                        DropdownMenuItem(
                                                onClick = {
                                                        onValueChange(option)
                                                        expanded = false
                                                }
                                        ) {
                                                Text(
                                                        text = valueToString(option),
                                                        style =
                                                                MaterialTheme.typography.body1.copy(
                                                                        fontSize =
                                                                                AppSizes.fontSizeBody1
                                                                ),
                                                        modifier =
                                                                Modifier.padding(
                                                                        AppSizes.paddingSmall
                                                                )
                                                )
                                        }
                                }
                        }
                }
        }
}

/**
 * Composant générique de liste déroulante multi-sélection basé sur BasicTextField
 *
 * @param T Type d'élément pour la liste déroulante
 * @param label Libellé du champ
 * @param selectedValues Valeurs actuellement sélectionnées
 * @param options Liste des options disponibles
 * @param onValuesChange Callback appelé lorsqu'une ou plusieurs valeurs sont sélectionnées
 * @param valueToString Fonction de conversion des valeurs en chaînes affichables
 * @param modifier Modificateur optionnel
 * @param enabled Si le champ est activé (par défaut: true)
 * @param height Hauteur personnalisable du champ (par défaut: 40.dp)
 * @param fontSize Taille de police (par défaut: 12.sp)
 * @param labelFontSize Taille de police du label (par défaut: 10.sp)
 * @param borderWidth Épaisseur de bordure (par défaut: 0.5.dp)
 */
@Composable
fun <T> MultiSelectDropdownField(
        label: String,
        selectedValues: Set<T>,
        options: List<T>,
        onValuesChange: (Set<T>) -> Unit,
        valueToString: (T) -> String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        height: Dp = 40.dp,
        fontSize: TextUnit = 12.sp,
        labelFontSize: TextUnit = 10.sp,
        borderWidth: Dp = 0.5.dp
) {
        var expanded by remember { mutableStateOf(false) }
        // État local pour les valeurs temporaires pendant la sélection
        var tempSelectedValues by remember(expanded, selectedValues) {
                mutableStateOf(selectedValues)
        }
        
        // Réinitialiser les valeurs temporaires quand le menu s'ouvre
        LaunchedEffect(expanded) {
                if (expanded) {
                        tempSelectedValues = selectedValues
                }
        }
        
        val displayValue =
                if (selectedValues.isEmpty()) "Sélectionner"
                else "${selectedValues.size} sélectionnée(s)"

        Column(modifier = modifier) {
                        BasicTextField(
                                value = displayValue,
                                onValueChange = {},
                                readOnly = true,
                                textStyle =
                                        LocalTextStyle.current.copy(
                                                fontSize = fontSize,
                                                color =
                                                        if (enabled) MaterialTheme.colors.onSurface
                                                        else
                                                                MaterialTheme.colors.onSurface.copy(
                                                                        alpha = 0.6f
                                                                )
                                        ),
                                modifier = Modifier.fillMaxWidth().height(height),
                                decorationBox = { innerTextField ->
                                        Box(
                                                modifier =
                                                        Modifier.fillMaxSize()
                                                                .clickable(enabled = enabled) {
                                                                        if (enabled)
                                                                                expanded = !expanded
                                                                }
                                                                .border(
                                                                        width = borderWidth,
                                                                        color =
                                                                                when {
                                                                                        !enabled ->
                                                                                                MaterialTheme
                                                                                                        .colors
                                                                                                        .onSurface
                                                                                                        .copy(
                                                                                                                alpha =
                                                                                                                        0.3f
                                                                                                        )
                                                                                        expanded ->
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                                        .copy(
                                                                                                                alpha =
                                                                                                                        0.6f
                                                                                                        )
                                                                                        else ->
                                                                                                MaterialTheme
                                                                                                        .colors
                                                                                                        .onSurface
                                                                                                        .copy(
                                                                                                                alpha =
                                                                                                                        0.4f
                                                                                                        )
                                                                                },
                                                                        shape =
                                                                                RoundedCornerShape(
                                                                                        4.dp
                                                                                )
                                                                )
                                                                .padding(
                                                                        horizontal = 8.dp,
                                                                        vertical = 6.dp
                                                                )
                                        ) {
                                                Row(
                                                        modifier = Modifier.fillMaxSize(),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Text(
                                                                text = "$label: ",
                                                                fontSize = labelFontSize,
                                                                color =
                                                                        MaterialTheme.colors
                                                                                .onSurface.copy(
                                                                                alpha =
                                                                                        if (enabled)
                                                                                                0.7f
                                                                                        else 0.5f
                                                                        ),
                                                                modifier =
                                                                        Modifier.padding(end = 4.dp)
                                                        )
                                                        Box(modifier = Modifier.weight(1f)) {
                                                                if (selectedValues.isEmpty()) {
                                                                        Text(
                                                                                text =
                                                                                        "Sélectionner...",
                                                                                fontSize = fontSize,
                                                                                color =
                                                                                        MaterialTheme
                                                                                                .colors
                                                                                                .onSurface
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                if (enabled
                                                                                                                )
                                                                                                                        0.5f
                                                                                                                else
                                                                                                                        0.3f
                                                                                                ),
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Normal
                                                                        )
                                                                } else {
                                                                        Text(
                                                                                text =
                                                                                        selectedValues
                                                                                                .joinToString(
                                                                                                        ", "
                                                                                                ) {
                                                                                                        valueToString(
                                                                                                                it
                                                                                                        )
                                                                                                },
                                                                                fontSize = fontSize,
                                                                                color =
                                                                                        MaterialTheme
                                                                                                .colors
                                                                                                .onSurface,
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Normal
                                                                        )
                                                                }
                                                        }
                                                        Icon(
                                                                Icons.Default.ArrowDropDown,
                                                                contentDescription = "Dropdown",
                                                                modifier = Modifier.size(18.dp),
                                                                tint =
                                                                        MaterialTheme.colors
                                                                                .onSurface.copy(
                                                                                alpha =
                                                                                        if (enabled)
                                                                                                0.6f
                                                                                        else 0.3f
                                                                        )
                                                        )
                                                }
                                        }
                                }
                        )
                // Dialog personnalisé pour la sélection multiple
                if (expanded && enabled) {
                        Dialog(onDismissRequest = { 
                                // Réinitialiser les valeurs temporaires si on ferme sans valider
                                tempSelectedValues = selectedValues
                                                expanded = false
                        }) {
                                Surface(
                                        shape = MaterialTheme.shapes.medium,
                                        color = MaterialTheme.colors.surface,
                                        elevation = 8.dp,
                                        modifier = Modifier
                                                .fillMaxWidth(0.9f)
                                                .heightIn(max = 500.dp)
                                ) {
                                        Column(
                                                modifier = Modifier.fillMaxSize()
                                        ) {
                                                // Liste des options avec scroll
                                                Column(
                                                        modifier = Modifier
                                                                .weight(1f)
                                                                .fillMaxWidth()
                                                                .verticalScroll(rememberScrollState())
                                                ) {
                                                        // Option "Aucune sélection"
                                                        Row(
                                                                modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .clickable {
                                                                                tempSelectedValues = emptySet()
                                                                        }
                                                                        .padding(16.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                Icon(
                                                                        if (tempSelectedValues.isEmpty())
                                                                Icons.Default.Check
                                                        else Icons.Default.Clear,
                                                        contentDescription = null,
                                                                        modifier = Modifier.size(20.dp),
                                                                        tint = if (tempSelectedValues.isEmpty())
                                                                                VetNutriColors.Primary
                                                                        else MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                                                )
                                                                Spacer(modifier = Modifier.width(12.dp))
                                                                Text(
                                                                        "Aucune sélection",
                                                                        style = MaterialTheme.typography.body1
                                                                )
                                                        }
                                                        Divider()
                                // Options individuelles
                                options.forEach { option ->
                                                                Row(
                                                                        modifier = Modifier
                                                                                .fillMaxWidth()
                                                                                .clickable {
                                                                                        tempSelectedValues = if (option in tempSelectedValues)
                                                                                                tempSelectedValues - option
                                                                                        else tempSelectedValues + option
                                                                                }
                                                                                .padding(16.dp),
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                        Icon(
                                                                                if (option in tempSelectedValues)
                                                                                        Icons.Default.Check
                                                                                else Icons.Default.Clear,
                                                                                contentDescription = null,
                                                                                modifier = Modifier.size(20.dp),
                                                                                tint = if (option in tempSelectedValues)
                                                                                        VetNutriColors.Primary
                                                                                else MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                                                        )
                                                                        Spacer(modifier = Modifier.width(12.dp))
                                                                        Text(
                                                                                valueToString(option),
                                                                                style = MaterialTheme.typography.body1
                                                                        )
                                                                }
                                                                Divider()
                                                        }
                                                }
                                                // Bouton de validation en bas
                                                Surface(
                                                        elevation = 4.dp,
                                                        modifier = Modifier.fillMaxWidth()
                                                ) {
                                                        Row(
                                                                modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .padding(16.dp),
                                                                horizontalArrangement = Arrangement.End
                                                        ) {
                                                                OutlinedButton(
                                                                        onClick = {
                                                                                tempSelectedValues = selectedValues
                                                                                expanded = false
                                                                        },
                                                                        modifier = Modifier.padding(end = 8.dp)
                                                                ) {
                                                                        Text("Annuler")
                                                                }
                                                                FloatingActionButton(
                                                                        onClick = {
                                                                                onValuesChange(tempSelectedValues)
                                                                                expanded = false
                                                                        },
                                                                        backgroundColor = VetNutriColors.Primary,
                                                                        modifier = Modifier.size(48.dp)
                                                ) {
                                                        Icon(
                                                                                Icons.Default.Check,
                                                                                contentDescription = "Valider",
                                                                                tint = MaterialTheme.colors.onPrimary
                                                                        )
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
}
