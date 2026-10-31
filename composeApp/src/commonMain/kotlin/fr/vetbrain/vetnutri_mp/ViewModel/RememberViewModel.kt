package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

/**
 * ViewModel lié à la composition : créé par [create] pour les [keys] données, détruit quand une
 * clé change ou que l'appelant quitte la composition. La destruction passe par le
 * [ViewModelStore], qui appelle `onCleared()` et annule `viewModelScope` : plus de `clear()` à
 * appeler à la main.
 */
@Composable
inline fun <reified VM : ViewModel> rememberViewModel(vararg keys: Any?, crossinline create: () -> VM): VM {
    val store = remember(*keys) { ViewModelStore() }
    DisposableEffect(store) { onDispose { store.clear() } }
    return remember(store) {
        ViewModelProvider.create(store, viewModelFactory { initializer { create() } })[VM::class]
    }
}
