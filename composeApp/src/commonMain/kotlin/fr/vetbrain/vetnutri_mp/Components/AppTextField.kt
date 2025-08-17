package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                                                        modifier =
                                                                Modifier.height(
                                                                        AppSizes.iconSizeLarge
                                                                )
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

/**
 * Champ de texte basique personnalisé avec validation et contraintes Similaire à BasicTextField
 * mais avec validation intégrée
 *
 * @param value Valeur actuelle du champ
 * @param onValueChange Callback appelé lorsque la valeur change
 * @param modifier Modificateur optionnel
 * @param placeholder Texte d'indication lorsque le champ est vide
 * @param leadingIcon Icône à afficher au début du champ
 * @param trailingIcon Icône à afficher à la fin du champ
 * @param onTrailingIconClick Callback appelé lors du clic sur l'icône de fin
 * @param isError Indique si le champ contient une erreur
 * @param errorMessage Message d'erreur à afficher
 * @param keyboardOptions Options du clavier
 * @param singleLine Indique si le champ doit être sur une seule ligne
 * @param maxLines Nombre maximum de lignes
 * @param readOnly Indique si le champ est en lecture seule
 * @param enabled Indique si le champ est activé
 * @param validationRegex Regex de validation pour filtrer les entrées
 * @param borderColor Couleur de la bordure
 * @param borderWidth Épaisseur de la bordure
 * @param borderRadius Rayon des coins de la bordure
 * @param backgroundColor Couleur d'arrière-plan
 * @param textColor Couleur du texte
 * @param fontSize Taille de la police
 * @param height Hauteur du champ
 */
@Composable
fun BasicAppTextField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier,
        placeholder: String? = null,
        leadingIcon: ImageVector? = null,
        trailingIcon: ImageVector? = null,
        onTrailingIconClick: (() -> Unit)? = null,
        isError: Boolean = false,
        errorMessage: String? = null,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        singleLine: Boolean = true,
        maxLines: Int = Int.MAX_VALUE,
        readOnly: Boolean = false,
        enabled: Boolean = true,
        validationRegex: Regex? = null
) {
        Column(modifier = modifier) {
                BasicTextField(
                        value = value,
                        onValueChange = { newValue ->
                                // Appliquer la validation si un regex est fourni
                                if (validationRegex == null ||
                                                newValue.isEmpty() ||
                                                newValue.matches(validationRegex)
                                ) {
                                        onValueChange(newValue)
                                }
                        },
                        textStyle =
                                LocalTextStyle.current.copy(
                                        fontSize = 14.sp,
                                        color =
                                                if (enabled) MaterialTheme.colors.onSurface
                                                else
                                                        MaterialTheme.colors.onSurface.copy(
                                                                alpha = 0.6f
                                                        )
                                ),
                        keyboardOptions = keyboardOptions,
                        visualTransformation = VisualTransformation.None,
                        singleLine = singleLine,
                        maxLines = maxLines,
                        readOnly = readOnly,
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        decorationBox = { innerTextField ->
                                Box(
                                        modifier =
                                                Modifier.fillMaxSize()
                                                        .border(
                                                                width = 0.5.dp,
                                                                color =
                                                                        when {
                                                                                isError ->
                                                                                        MaterialTheme
                                                                                                .colors
                                                                                                .error
                                                                                !enabled ->
                                                                                        MaterialTheme
                                                                                                .colors
                                                                                                .onSurface
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.3f
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
                                                                shape = RoundedCornerShape(4.dp)
                                                        )
                                                        .background(MaterialTheme.colors.surface)
                                                        .padding(
                                                                horizontal = 12.dp,
                                                                vertical = 8.dp
                                                        )
                                ) {
                                        Row(
                                                modifier = Modifier.fillMaxSize(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                                // Icône de début
                                                leadingIcon?.let { icon ->
                                                        Icon(
                                                                imageVector = icon,
                                                                contentDescription = null,
                                                                tint =
                                                                        if (enabled)
                                                                                MaterialTheme.colors
                                                                                        .onSurface
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.7f
                                                                                        )
                                                                        else
                                                                                MaterialTheme.colors
                                                                                        .onSurface
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.4f
                                                                                        ),
                                                                modifier = Modifier.size(16.dp)
                                                        )
                                                }

                                                // Champ de texte
                                                Box(modifier = Modifier.weight(1f)) {
                                                        if (value.isEmpty() && placeholder != null
                                                        ) {
                                                                Text(
                                                                        text = placeholder,
                                                                        fontSize = 14.sp,
                                                                        color =
                                                                                MaterialTheme.colors
                                                                                        .onSurface
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.4f
                                                                                        ),
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body2
                                                                )
                                                        }
                                                        innerTextField()
                                                }

                                                // Icône de fin
                                                trailingIcon?.let { icon ->
                                                        IconButton(
                                                                onClick = {
                                                                        onTrailingIconClick
                                                                                ?.invoke()
                                                                },
                                                                modifier = Modifier.size(20.dp),
                                                                enabled =
                                                                        enabled &&
                                                                                onTrailingIconClick !=
                                                                                        null
                                                        ) {
                                                                Icon(
                                                                        imageVector = icon,
                                                                        contentDescription = null,
                                                                        tint =
                                                                                if (enabled)
                                                                                        MaterialTheme
                                                                                                .colors
                                                                                                .onSurface
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.7f
                                                                                                )
                                                                                else
                                                                                        MaterialTheme
                                                                                                .colors
                                                                                                .onSurface
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.4f
                                                                                                ),
                                                                        modifier =
                                                                                Modifier.size(16.dp)
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }
                )

                // Message d'erreur
                if (isError && errorMessage != null) {
                        Text(
                                text = errorMessage,
                                color = MaterialTheme.colors.error,
                                style = MaterialTheme.typography.caption,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                }
        }
}

/**
 * Version numérique du BasicAppTextField avec validation automatique des nombres
 *
 * @param value Valeur actuelle du champ
 * @param onValueChange Callback appelé lorsque la valeur change
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
 * @param allowDecimals Indique si les décimaux sont autorisés
 * @param allowNegative Indique si les nombres négatifs sont autorisés
 * @param borderColor Couleur de la bordure
 * @param borderWidth Épaisseur de la bordure
 * @param borderRadius Rayon des coins de la bordure
 * @param backgroundColor Couleur d'arrière-plan
 * @param textColor Couleur du texte
 * @param fontSize Taille de la police
 * @param height Hauteur du champ
 */
@Composable
fun BasicNumberTextField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier,
        placeholder: String? = null,
        leadingIcon: ImageVector? = null,
        trailingIcon: ImageVector? = null,
        onTrailingIconClick: (() -> Unit)? = null,
        isError: Boolean = false,
        errorMessage: String? = null,
        singleLine: Boolean = true,
        readOnly: Boolean = false,
        enabled: Boolean = true,
        allowDecimals: Boolean = true,
        allowNegative: Boolean = false
) {
        val validationRegex =
                when {
                        allowDecimals && allowNegative -> Regex("^-?\\d*\\.?\\d*$")
                        allowDecimals && !allowNegative -> Regex("^\\d*\\.?\\d*$")
                        !allowDecimals && allowNegative -> Regex("^-?\\d*$")
                        else -> Regex("^\\d*$")
                }

        BasicAppTextField(
                value = value,
                onValueChange = onValueChange,
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
                enabled = enabled,
                validationRegex = validationRegex
        )
}
