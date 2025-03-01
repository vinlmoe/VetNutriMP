package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

/**
 * Champ de texte personnalisé avec gestion des erreurs et des icônes
 *
 * @param value Valeur actuelle du champ
 * @param onValueChange Callback appelé lorsque la valeur change
 * @param label Libellé du champ
 * @param modifier Modificateur optionnel
 * @param placeholder Texte d'indication lorsque le champ est vide
 * @param leadingIcon Icône à afficher au début du champ
 * @param trailingIcon Icône à afficher à la fin du champ
 * @param onTrailingIconClick Callback appelé lors du clic sur l'icône de fin
 * @param isError Indique si le champ contient une erreur
 * @param errorMessage Message d'erreur à afficher
 * @param keyboardOptions Options du clavier
 * @param visualTransformation Transformation visuelle du texte
 * @param singleLine Indique si le champ doit être sur une seule ligne
 * @param maxLines Nombre maximum de lignes
 * @param readOnly Indique si le champ est en lecture seule
 * @param enabled Indique si le champ est activé
 */
@Composable
fun AppTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        modifier: Modifier = Modifier,
        placeholder: String? = null,
        leadingIcon: ImageVector? = null,
        trailingIcon: ImageVector? = null,
        onTrailingIconClick: (() -> Unit)? = null,
        isError: Boolean = false,
        errorMessage: String? = null,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        visualTransformation: VisualTransformation = VisualTransformation.None,
        singleLine: Boolean = false,
        maxLines: Int = Int.MAX_VALUE,
        readOnly: Boolean = false,
        enabled: Boolean = true
) {
    val textFieldColors =
            TextFieldDefaults.outlinedTextFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    backgroundColor = MaterialTheme.colors.surface,
                    cursorColor = VetNutriColors.Primary,
                    errorCursorColor = MaterialTheme.colors.error,
                    focusedBorderColor = VetNutriColors.Primary,
                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                    errorBorderColor = MaterialTheme.colors.error,
                    leadingIconColor = MaterialTheme.colors.onSurface.copy(alpha = 0.54f),
                    trailingIconColor = MaterialTheme.colors.onSurface.copy(alpha = 0.54f),
                    errorTrailingIconColor = MaterialTheme.colors.error,
                    focusedLabelColor = VetNutriColors.Primary,
                    unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.54f),
                    errorLabelColor = MaterialTheme.colors.error,
                    placeholderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.38f)
            )

    Column(modifier = modifier) {
        OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                placeholder = placeholder?.let { { Text(it) } },
                leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
                trailingIcon =
                        trailingIcon?.let { icon ->
                            {
                                IconButton(
                                        onClick = { onTrailingIconClick?.invoke() },
                                        modifier = Modifier.height(AppSizes.iconSizeLarge)
                                ) { Icon(icon, contentDescription = null) }
                            }
                        },
                isError = isError,
                keyboardOptions = keyboardOptions,
                visualTransformation = visualTransformation,
                singleLine = singleLine,
                maxLines = maxLines,
                readOnly = readOnly,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
        )

        if (isError && errorMessage != null) {
            Text(
                    text = errorMessage,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption
            )
        }
    }
}

/**
 * Champ de texte numérique personnalisé
 *
 * @param value Valeur actuelle du champ
 * @param onValueChange Callback appelé lorsque la valeur change
 * @param label Libellé du champ
 * @param modifier Modificateur optionnel
 * @param placeholder Texte d'indication lorsque le champ est vide
 * @param leadingIcon Icône à afficher au début du champ
 * @param trailingIcon Icône à afficher à la fin du champ
 * @param onTrailingIconClick Callback appelé lors du clic sur l'icône de fin
 * @param isError Indique si le champ contient une erreur
 * @param errorMessage Message d'erreur à afficher
 * @param singleLine Indique si le champ doit être sur une seule ligne
 * @param readOnly Indique si le champ est en lecture seule
 * @param enabled Indique si le champ est activé
 */
@Composable
fun NumberTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        modifier: Modifier = Modifier,
        placeholder: String? = null,
        leadingIcon: ImageVector? = null,
        trailingIcon: ImageVector? = null,
        onTrailingIconClick: (() -> Unit)? = null,
        isError: Boolean = false,
        errorMessage: String? = null,
        singleLine: Boolean = true,
        readOnly: Boolean = false,
        enabled: Boolean = true
) {
    AppTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            modifier = modifier,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            onTrailingIconClick = onTrailingIconClick,
            isError = isError,
            errorMessage = errorMessage,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = singleLine,
            readOnly = readOnly,
            enabled = enabled
    )
}
