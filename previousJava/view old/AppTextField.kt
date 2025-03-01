package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Theme.AppSizes

@Composable
fun AppTextField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier,
        label: String? = null,
        placeholder: String? = null,
        leadingIcon: ImageVector? = null,
        trailingIcon: ImageVector? = null,
        onTrailingIconClick: (() -> Unit)? = null,
        isError: Boolean = false,
        errorMessage: String? = null,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        singleLine: Boolean = true,
        maxLines: Int = 1,
        enabled: Boolean = true
) {
    val textFieldColors =
            TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    backgroundColor = MaterialTheme.colors.surface,
                    cursorColor = MaterialTheme.colors.primary,
                    errorCursorColor = MaterialTheme.colors.error,
                    focusedIndicatorColor = MaterialTheme.colors.primary,
                    unfocusedIndicatorColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                    errorIndicatorColor = MaterialTheme.colors.error,
                    leadingIconColor = MaterialTheme.colors.onSurface.copy(alpha = 0.54f),
                    trailingIconColor = MaterialTheme.colors.onSurface.copy(alpha = 0.54f),
                    errorTrailingIconColor = MaterialTheme.colors.error,
                    focusedLabelColor = MaterialTheme.colors.primary,
                    unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.54f),
                    errorLabelColor = MaterialTheme.colors.error,
                    placeholderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.38f)
            )

    val textStyle = MaterialTheme.typography.body1.copy(fontSize = AppSizes.fontSizeBody1)

    val labelStyle = MaterialTheme.typography.caption.copy(fontSize = AppSizes.fontSizeCaption)

    TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth().height(56.dp),
            textStyle = textStyle,
            label = label?.let { { Text(it, style = labelStyle) } },
            placeholder = placeholder?.let { { Text(it, style = textStyle) } },
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
            singleLine = singleLine,
            maxLines = maxLines,
            enabled = enabled,
            colors = textFieldColors
    )

    if (isError && !errorMessage.isNullOrBlank()) {
        Text(
                text = errorMessage,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption.copy(fontSize = AppSizes.fontSizeCaption)
        )
    }
}
