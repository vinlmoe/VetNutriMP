package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

/**
 * Composant générique de liste déroulante pour tout type de données
 *
 * @param T Type d'élément pour la liste déroulante
 * @param label Libellé du champ
 * @param selectedValue Valeur actuellement sélectionnée
 * @param options Liste des options disponibles
 * @param onValueChange Callback appelé lorsqu'une valeur est sélectionnée
 * @param valueToString Fonction de conversion des valeurs en chaînes affichables
 * @param modifier Modificateur optionnel
 * @param enabled Si le champ est activé (par défaut: true)
 */
@Composable
fun <T> DropdownField(
        label: String,
        selectedValue: T?,
        options: List<T>,
        onValueChange: (T) -> Unit,
        valueToString: (T) -> String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val displayValue = selectedValue?.let { valueToString(it) } ?: "Sélectionner"

    Column(modifier = modifier) {
        Box {
            OutlinedTextField(
                    value = displayValue,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(label) },
                    trailingIcon = {
                        IconButton(
                                onClick = { if (enabled) expanded = !expanded },
                                enabled = enabled
                        ) { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                    },
                    enabled = enabled,
                    colors =
                            TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = VetNutriColors.Primary,
                                    unfocusedBorderColor = Color.Gray,
                                    disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    disabledTextColor =
                                            MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                            ),
                    modifier = Modifier.fillMaxWidth()
            )

            DropdownMenu(
                    expanded = expanded && enabled,
                    onDismissRequest = { expanded = false },
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
                                                fontSize = AppSizes.fontSizeBody1
                                        ),
                                modifier = Modifier.padding(AppSizes.paddingSmall)
                        )
                    }
                }
            }
        }
    }
}
