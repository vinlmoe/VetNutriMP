package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.PreferencesApplication
import fr.vetbrain.vetnutri_mp.Data.PreferencesEspece
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Utils.PreferencesStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Repository pour la gestion des préférences de l'application */
class PreferencesRepository(private val preferencesStorage: PreferencesStorage) {

    companion object {
        private const val PREFERENCES_KEY = "app_preferences"
    }

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    // État des préférences
    private val _preferences = MutableStateFlow(PreferencesApplication())
    val preferences: StateFlow<PreferencesApplication> = _preferences.asStateFlow()

    /** Charge les préférences depuis le stockage */
    suspend fun loadPreferences() {
        try {
            val jsonString = preferencesStorage.getString(PREFERENCES_KEY, "{}")
            val loadedPreferences = json.decodeFromString<PreferencesApplication>(jsonString)
            _preferences.value = loadedPreferences
        } catch (e: Exception) {
            // En cas d'erreur, utiliser les préférences par défaut
            _preferences.value = PreferencesApplication()
        }
    }

    /** Sauvegarde les préférences dans le stockage */
    suspend fun savePreferences(preferences: PreferencesApplication) {
        try {
            val jsonString = json.encodeToString(preferences)
            preferencesStorage.saveString(PREFERENCES_KEY, jsonString)
            _preferences.value = preferences
        } catch (e: Exception) {
            // Gérer l'erreur de sauvegarde
            throw e
        }
    }

    /** Obtient les préférences pour une espèce */
    suspend fun getPreferencesForSpecies(species: Espece): PreferencesEspece {
        return _preferences.value.getPreferencesEspece(species)
    }

    /** Met à jour les préférences pour une espèce */
    suspend fun updatePreferencesForSpecies(
            species: Espece,
            preferences: PreferencesEspece
    ): PreferencesApplication {
        val updatedPreferences = _preferences.value.updatePreferencesEspece(preferences)
        savePreferences(updatedPreferences)
        return updatedPreferences
    }

    /** Remet à zéro les préférences d'une espèce */
    suspend fun updateSelectedSpecies(species: Espece) {
        val currentPreferences = _preferences.value
        val speciesPreferences = currentPreferences.getPreferencesEspece(species)
        val updatedSpeciesPreferences = speciesPreferences.copy(selectedSpecies = species)
        val updatedPreferences =
                currentPreferences.updatePreferencesEspece(updatedSpeciesPreferences)
        savePreferences(updatedPreferences)
    }

    fun getAvailableSpecies(): List<Espece> {
        return _preferences
                .value
                .preferencesParEspece
                .values
                .flatMap { it.availableSpecies }
                .distinct()
                .ifEmpty { Espece.valuesExcept() }
    }

    fun getSpeciesDisplayNames(): List<String> {
        return getAvailableSpecies().map { it.label }
    }

    suspend fun addSpeciesToAvailable(species: Espece) {
        val currentPreferences = _preferences.value
        val updatedPreferences =
                currentPreferences.preferencesParEspece.mapValues { (_, prefs) ->
                    if (species !in prefs.availableSpecies) {
                        prefs.copy(availableSpecies = prefs.availableSpecies + species)
                    } else {
                        prefs
                    }
                }
        val newPreferences = currentPreferences.copy(preferencesParEspece = updatedPreferences)
        savePreferences(newPreferences)
    }

    suspend fun removeSpeciesFromAvailable(species: Espece) {
        val currentPreferences = _preferences.value
        val updatedPreferences =
                currentPreferences.preferencesParEspece.mapValues { (_, prefs) ->
                    prefs.copy(availableSpecies = prefs.availableSpecies.filter { it != species })
                }
        val newPreferences = currentPreferences.copy(preferencesParEspece = updatedPreferences)
        savePreferences(newPreferences)
    }

    /** Exporte les préférences au format JSON */
    fun exporterPreferences(): String {
        return json.encodeToString(_preferences.value)
    }

    /** Importe les préférences depuis un JSON */
    suspend fun importerPreferences(preferencesJson: String): Boolean {
        return try {
            val nouvellesPreferences =
                    json.decodeFromString<PreferencesApplication>(preferencesJson)
            _preferences.value = nouvellesPreferences
            savePreferences(nouvellesPreferences)
            println("DEBUG: Préférences importées avec succès")
            true
        } catch (e: Exception) {
            println("ERROR: Erreur lors de l'import des préférences: ${e.message}")
            false
        }
    }
}
