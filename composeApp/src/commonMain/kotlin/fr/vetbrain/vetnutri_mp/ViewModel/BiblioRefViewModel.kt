package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.compose.runtime.mutableStateOf
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository
import fr.vetbrain.vetnutri_mp.Utils.PlatformDispatcher
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** ViewModel pour la gestion des références bibliographiques */
class BiblioRefViewModel(private val repository: BiblioRefRepository) {
    // Utilisation du dispatcher de la plateforme
    private val dispatcher = PlatformDispatcher().provideMainDispatcher()
    private val viewModelScope = CoroutineScope(dispatcher)

    // État de toutes les références bibliographiques
    val allBiblioRefs: StateFlow<List<BiblioRef>> =
            repository
                    .getAllBiblioRefs()
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // État pour l'édition d'une référence bibliographique
    private val _currentBiblioRef = MutableStateFlow(BiblioRef())
    val currentBiblioRef: StateFlow<BiblioRef> = _currentBiblioRef

    // États pour les champs d'édition
    val firstAuthor = mutableStateOf("")
    val year = mutableStateOf("")
    val completeRef = mutableStateOf("")
    val comments = mutableStateOf("")

    // État de validation du formulaire
    val isValid = mutableStateOf(false)

    // État de l'opération en cours
    private val _actionInProgress = MutableStateFlow(false)
    val actionInProgress: StateFlow<Boolean> = _actionInProgress

    // Message d'erreur ou de succès
    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage

    /** Initialise le ViewModel avec une nouvelle référence ou une référence existante */
    fun initForEdit(biblioRef: BiblioRef? = null) {
        val refToEdit = biblioRef ?: BiblioRef()
        _currentBiblioRef.value = refToEdit

        // Initialiser les champs d'édition
        firstAuthor.value = refToEdit.firstAuthor
        year.value = if (refToEdit.year > 1800) refToEdit.year.toString() else ""
        completeRef.value = refToEdit.completeRef
        comments.value = refToEdit.comments

        // Vérifier la validité initiale
        validateForm()
    }

    /** Valide le formulaire */
    fun validateForm() {
        isValid.value =
                firstAuthor.value.isNotBlank() &&
                        year.value.isNotBlank() &&
                        completeRef.value.isNotBlank()
    }

    /** Met à jour la valeur du champ auteur */
    fun updateFirstAuthor(value: String) {
        firstAuthor.value = value
        validateForm()
    }

    /** Met à jour la valeur du champ année */
    fun updateYear(value: String) {
        // Accepter uniquement les chiffres
        if (value.isEmpty() || value.all { it.isDigit() }) {
            year.value = value
        }
        validateForm()
    }

    /** Met à jour la valeur du champ référence complète */
    fun updateCompleteRef(value: String) {
        completeRef.value = value
        validateForm()
    }

    /** Met à jour la valeur du champ commentaires */
    fun updateComments(value: String) {
        comments.value = value
    }

    /** Sauvegarde la référence bibliographique actuelle */
    fun saveBiblioRef() {
        if (!isValid.value) {
            println("DEBUG: Formulaire non valide, sauvegarde abandonnée")
            return
        }

        _actionInProgress.value = true

        // Créer une nouvelle instance de BiblioRef avec les valeurs du formulaire
        val biblioRefToSave =
                if (_currentBiblioRef.value.uuid.isBlank()) {
                    // Nouvelle référence avec un UUID généré
                    BiblioRef(
                            uuid = UUID.randomUUID().toString(),
                            firstAuthor = firstAuthor.value,
                            year = year.value.toIntOrNull() ?: 1800,
                            completeRef = completeRef.value,
                            comments = comments.value,
                            consistent = 1 // Supposé cohérent lors de la création par l'utilisateur
                    )
                } else {
                    // Mise à jour d'une référence existante
                    BiblioRef(
                            uuid = _currentBiblioRef.value.uuid,
                            firstAuthor = firstAuthor.value,
                            year = year.value.toIntOrNull() ?: 1800,
                            completeRef = completeRef.value,
                            comments = comments.value,
                            consistent =
                                    1 // Supposé cohérent lors de la mise à jour par l'utilisateur
                    )
                }

        // Log pour le débogage
        println(
                "DEBUG: Tentative de sauvegarde: ${biblioRefToSave.firstAuthor}, ${biblioRefToSave.year}, UUID: ${biblioRefToSave.uuid}"
        )

        viewModelScope.launch {
            try {
                // Déterminer s'il s'agit d'une création ou d'une mise à jour
                val isNewReference = _currentBiblioRef.value.uuid.isBlank()

                if (isNewReference) {
                    repository.insertBiblioRef(biblioRefToSave)
                    _operationMessage.value = "Référence ajoutée avec succès"
                    println("DEBUG: Référence ajoutée: ${biblioRefToSave.firstAuthor}")
                } else {
                    repository.updateBiblioRef(biblioRefToSave)
                    _operationMessage.value = "Référence mise à jour avec succès"
                    println("DEBUG: Référence mise à jour: ${biblioRefToSave.firstAuthor}")
                }

                // Réinitialiser le formulaire après sauvegarde
                initForEdit()
            } catch (e: Exception) {
                _operationMessage.value = "Erreur: ${e.message}"
                println("DEBUG: Erreur lors de la sauvegarde: ${e.message}")
                e.printStackTrace()
            } finally {
                _actionInProgress.value = false
            }
        }
    }

    /** Supprime une référence bibliographique */
    fun deleteBiblioRef(biblioRef: BiblioRef) {
        _actionInProgress.value = true

        viewModelScope.launch {
            try {
                repository.deleteBiblioRef(biblioRef)
                _operationMessage.value = "Référence supprimée avec succès"

                // Réinitialiser le formulaire si la référence supprimée était en cours d'édition
                if (_currentBiblioRef.value.uuid == biblioRef.uuid) {
                    initForEdit()
                }
            } catch (e: Exception) {
                _operationMessage.value = "Erreur: ${e.message}"
                println("DEBUG: Erreur lors de la suppression: ${e.message}")
                e.printStackTrace()
            } finally {
                _actionInProgress.value = false
            }
        }
    }

    /** Efface le message d'opération */
    fun clearOperationMessage() {
        _operationMessage.value = null
    }
}
