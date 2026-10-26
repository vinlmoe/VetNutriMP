package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Classe de base multiplateforme des ViewModels (sans dépendance à androidx.lifecycle.ViewModel).
 *
 * - `viewModelScope` : scope sur `AppDispatchers.Main` + `SupervisorJob` (une coroutine en échec
 *   n'annule pas les autres).
 * - `clear()` : annule le scope ; appelé par `App.kt` dans son `DisposableEffect` (ou par la vue
 *   qui a créé le ViewModel) pour éviter les fuites de coroutines.
 *
 * Les sous-classes qui exposaient un `scope` conservent ce nom via un alias.
 */
abstract class BaseViewModel {
    protected val viewModelScope: CoroutineScope = CoroutineScope(AppDispatchers.Main + SupervisorJob())

    open fun clear() {
        viewModelScope.cancel()
    }
}
