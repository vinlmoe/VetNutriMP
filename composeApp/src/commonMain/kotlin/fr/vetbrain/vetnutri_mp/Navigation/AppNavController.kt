package fr.vetbrain.vetnutri_mp.Navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece

/**
 * Contrôleur de navigation manuel : écran courant observable par Compose + pile de retour.
 *
 * - [navigate] empile l'écran courant puis affiche la cible (pas de doublon consécutif, pile bornée) ;
 * - [back] revient à l'écran précédent de la pile (false si elle est vide) ;
 * - [backOr] revient en arrière, ou va vers l'écran de repli si la pile est vide ;
 * - [replace] remplace l'écran courant sans l'empiler (redirections, écran suivant « à la place ») ;
 * - [navigateAndClear] vide la pile et affiche une racine (ex. retour à la liste après le démarrage).
 */
internal class AppNavController {
    var screen by mutableStateOf<Screen>(Screen.List)
        private set
    var selectedAnimal by mutableStateOf<AnimalEv?>(null)
    var isEditing by mutableStateOf(false)
    var selectedFoodUuid by mutableStateOf<String?>(null)
    var selectedBiblioRefId by mutableStateOf<String?>(null)
    var selectedReferenceEvId by mutableStateOf<String?>(null)
    var selectedEquationId by mutableStateOf<String?>(null)
    var selectedConseilId by mutableStateOf<String?>(null)
    var selectedCalculationTab by mutableStateOf(0)
    var selectedReferenceIdsForBulk by mutableStateOf<List<String>>(emptyList())
    var selectedSpecies by mutableStateOf<Espece?>(null)

    /** Pile des écrans précédents (le plus récent en dernier) ; l'écran courant n'y figure pas. */
    private val pile = ArrayDeque<Screen>()

    /** Taille de la pile, exposée comme état Compose pour dériver [canGoBack]. */
    private var profondeur by mutableStateOf(0)

    /** Vrai s'il existe un écran précédent vers lequel revenir. */
    val canGoBack: Boolean get() = profondeur > 0

    /** Empile l'écran courant et affiche [target]. Sans effet si [target] est déjà l'écran courant. */
    fun navigate(target: Screen) {
        if (target == screen) return
        pile.addLast(screen)
        while (pile.size > PROFONDEUR_MAX) pile.removeFirst()
        profondeur = pile.size
        screen = target
    }

    /** Revient à l'écran précédent. Retourne false (sans rien changer) si la pile est vide. */
    fun back(): Boolean {
        val precedent = pile.removeLastOrNull() ?: return false
        profondeur = pile.size
        screen = precedent
        return true
    }

    /** Revient en arrière, ou navigue vers [fallback] quand la pile est vide. */
    fun backOr(fallback: Screen) {
        if (!back()) navigate(fallback)
    }

    /** Affiche [target] à la place de l'écran courant, sans l'empiler. */
    fun replace(target: Screen) {
        screen = target
    }

    /** Vide la pile et affiche [target] comme nouvelle racine. */
    fun navigateAndClear(target: Screen) {
        pile.clear()
        profondeur = 0
        screen = target
    }

    fun clearFoodSelection() {
        selectedFoodUuid = null
    }

    /** Oublie toutes les sélections d'édition (animal, aliment, références, équation, conseil, espèce). */
    fun clearSelection() {
        selectedAnimal = null
        isEditing = false
        selectedFoodUuid = null
        selectedBiblioRefId = null
        selectedReferenceEvId = null
        selectedEquationId = null
        selectedConseilId = null
        selectedReferenceIdsForBulk = emptyList()
        selectedSpecies = null
    }

    private companion object {
        const val PROFONDEUR_MAX = 50
    }
}
