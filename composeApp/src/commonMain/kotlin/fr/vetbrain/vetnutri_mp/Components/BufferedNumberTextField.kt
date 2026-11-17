package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType

/**
 * Champ lié à un modèle numérique : conserve la saisie (vide, virgule, zéros finaux)
 * jusqu'à la perte de focus, même si le modèle renvoie un nombre reformaté.
 * Le callback reçoit un point décimal pour les conversions Kotlin.
 */
@Composable
fun BufferedNumberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    var draft by remember { mutableStateOf(NumericInputDraft(value)) }
    LaunchedEffect(value, draft.isFocused) {
        draft = draft.synchronize(value)
    }
    OutlinedTextField(
        value = draft.text,
        onValueChange = { text ->
            draft = draft.copy(text = text)
            onValueChange(text.replace(',', '.'))
        },
        label = label,
        modifier = modifier.onFocusChanged { draft = draft.copy(isFocused = it.isFocused) },
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}

internal data class NumericInputDraft(val text: String, val isFocused: Boolean = false) {
    fun synchronize(modelText: String): NumericInputDraft =
        if (isFocused) this else copy(text = modelText)
}
