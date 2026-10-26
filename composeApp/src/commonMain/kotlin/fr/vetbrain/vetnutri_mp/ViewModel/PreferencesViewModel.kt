package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.PreferencesApplication
import fr.vetbrain.vetnutri_mp.Data.PreferencesEspece
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Préférences de l'application (praticien, nutriments affichés par espèce, type d'expression
 * des besoins). Utilisé par SettingsView (section Préférences), SpeciesPreferencesView et
 * PreferencesView ; les Composables n'instancient plus de PreferencesRepository.
 */
class PreferencesViewModel(
        private val preferencesRepository: PreferencesRepository
) : BaseViewModel() {

    private val _preferences = MutableStateFlow<PreferencesApplication?>(null)
    /** Dernières préférences chargées ou enregistrées (null tant que rien n'a été chargé). */
    val preferences: StateFlow<PreferencesApplication?> = _preferences.asStateFlow()

    /** Recharge les préférences depuis le stockage et les retourne. */
    suspend fun loadPreferences(): PreferencesApplication {
        preferencesRepository.loadPreferences()
        return preferencesRepository.preferences.also { _preferences.value = it }
    }

    suspend fun savePreferences(preferences: PreferencesApplication) {
        preferencesRepository.savePreferences(preferences)
        _preferences.value = preferences
    }

    suspend fun getPreferencesForSpecies(species: Espece): PreferencesEspece =
            preferencesRepository.getPreferencesForSpecies(species)

    fun getAvailableSpecies(): List<Espece> = preferencesRepository.getAvailableSpecies()
}
