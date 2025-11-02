package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Composant d'autocomplétion générique et réutilisable
 *
 * @param T Type de l'élément dans les suggestions
 * @param value Valeur actuelle du champ texte
 * @param onValueChange Callback appelé quand la valeur change
 * @param suggestions Liste des suggestions disponibles
 * @param onSuggestionSelected Callback appelé quand une suggestion est sélectionnée
 * @param filterSuggestions Fonction pour filtrer les suggestions selon le texte saisi
 * @param suggestionToString Fonction pour convertir une suggestion en String affichable
 * @param label Libellé du champ
 * @param modifier Modificateur optionnel
 * @param enabled Si le champ est activé
 * @param isError Si le champ contient une erreur
 * @param errorMessage Message d'erreur à afficher
 * @param maxSuggestions Nombre maximum de suggestions à afficher (par défaut: 5)
 * @param placeholder Texte de placeholder
 */
@Composable
fun <T> AutocompleteTextField(
        value: String,
        onValueChange: (String) -> Unit,
        suggestions: List<T>,
        onSuggestionSelected: (T) -> Unit,
        suggestionToString: (T) -> String,
        label: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        isError: Boolean = false,
        errorMessage: String? = null,
        maxSuggestions: Int = 5,
        placeholder: String? = null,
        filterSuggestions: (String, List<T>, (T) -> String) -> List<T> = { query, items, toStringFn ->
                if (query.isBlank()) {
                        items
                } else {
                        items.filter { suggestion ->
                                toStringFn(suggestion).contains(query, ignoreCase = true)
                        }
                }
        }
) {
        val filteredSuggestions = remember(value, suggestions, suggestionToString) {
                if (value.isNotBlank()) {
                        filterSuggestions(value, suggestions, suggestionToString).take(maxSuggestions)
                } else {
                        emptyList()
                }
        }
        
        var expanded by remember { mutableStateOf(false) }
        var isFocused by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        
        // Mettre à jour expanded quand filteredSuggestions change et que le champ est focus
        LaunchedEffect(filteredSuggestions, value, isFocused) {
                expanded = isFocused && value.isNotBlank() && filteredSuggestions.isNotEmpty()
        }

        Column(modifier = modifier) {
                OutlinedTextField(
                        value = value,
                        onValueChange = { newValue ->
                                onValueChange(newValue)
                        },
                        label = { Text(label) },
                        placeholder = placeholder?.let { { Text(it) } },
                        modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                        val wasFocused = isFocused
                                        isFocused = focusState.isFocused
                                        if (!focusState.isFocused && wasFocused && !expanded) {
                                                coroutineScope.launch {
                                                        delay(200)
                                                        if (!isFocused && !expanded) {
                                                                expanded = false
                                                        }
                                                }
                                        }
                                },
                        enabled = enabled,
                        isError = isError,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = VetNutriColors.Primary,
                                unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                                errorBorderColor = MaterialTheme.colors.error
                        )
                )

                if (isError && errorMessage != null) {
                        Text(
                                text = errorMessage,
                                color = MaterialTheme.colors.error,
                                style = MaterialTheme.typography.caption,
                                modifier = Modifier.padding(top = AppSizes.paddingXSmall)
                        )
                }

                if (expanded && filteredSuggestions.isNotEmpty()) {
                        Card(
                                modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                elevation = 4.dp,
                                backgroundColor = MaterialTheme.colors.surface
                        ) {
                                LazyColumn(
                                        modifier = Modifier.heightIn(max = 200.dp),
                                        verticalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                        items(filteredSuggestions) { suggestion ->
                                                val selectedValue = suggestionToString(suggestion)
                                                Row(
                                                        modifier = Modifier
                                                                .fillMaxWidth()
                                                                .pointerInput(suggestion) {
                                                                        detectTapGestures {
                                                                                onValueChange(selectedValue)
                                                                                onSuggestionSelected(suggestion)
                                                                                expanded = false
                                                                        }
                                                                }
                                                                .padding(
                                                                        horizontal = AppSizes.paddingMedium,
                                                                        vertical = AppSizes.paddingSmall
                                                                ),
                                                        verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                        Text(
                                                                text = selectedValue,
                                                                style = MaterialTheme.typography.body1,
                                                                color = MaterialTheme.colors.onSurface
                                                        )
                                                }
                                                if (suggestion != filteredSuggestions.last()) {
                                                        Divider(
                                                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                                                                thickness = 1.dp
                                                        )
                                                }
                                        }
                                }
                        }
                }
        }
}

/**
 * Version spécialisée pour String (suggestions simples)
 */
@Composable
fun AutocompleteTextField(
        value: String,
        onValueChange: (String) -> Unit,
        suggestions: List<String>,
        label: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        isError: Boolean = false,
        errorMessage: String? = null,
        maxSuggestions: Int = 5,
        placeholder: String? = null,
        filterFunction: (String, List<String>) -> List<String> = { query, items ->
                if (query.isBlank()) {
                        items
                } else {
                        items.filter { it.contains(query, ignoreCase = true) }
                }
        },
        onSuggestionSelected: ((String) -> Unit)? = null
) {
        AutocompleteTextField(
                value = value,
                onValueChange = onValueChange,
                suggestions = suggestions,
                onSuggestionSelected = { selected ->
                        if (onSuggestionSelected != null) {
                                onSuggestionSelected(selected)
                        } else {
                                onValueChange(selected)
                        }
                },
                suggestionToString = { it },
                label = label,
                modifier = modifier,
                enabled = enabled,
                isError = isError,
                errorMessage = errorMessage,
                maxSuggestions = maxSuggestions,
                placeholder = placeholder,
                filterSuggestions = { query, items, toStringFn ->
                        filterFunction(query, items)
                }
        )
}

