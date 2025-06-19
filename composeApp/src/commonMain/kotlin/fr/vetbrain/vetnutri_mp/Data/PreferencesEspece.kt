package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin
import kotlinx.serialization.Serializable

/** Préférences par espèce */
@Serializable
data class PreferencesEspece(
        val selectedSpecies: Espece = Espece.CHIEN,
        val availableSpecies: List<Espece> = Espece.valuesExcept(),
        val typeExpressionBesoin: String = TypeExpressionBesoin.DEFAULT.id,
        val formatAffichageDecimales: Int = 2,
        val afficherValeursAbsolues: Boolean = true,
        val afficherValeursRelatives: Boolean = true
) {
    /** Obtient le type d'expression des besoins sous forme d'enum */
    fun getTypeExpressionBesoinEnum(): TypeExpressionBesoin {
        return TypeExpressionBesoin.getById(typeExpressionBesoin) ?: TypeExpressionBesoin.DEFAULT
    }

    fun getSelectedSpeciesName(): String {
        return selectedSpecies.label
    }

    fun getAvailableSpeciesNames(): List<String> {
        return availableSpecies.map { it.label }
    }

    fun getSpeciesByName(name: String): Espece? {
        return availableSpecies.find { it.label == name }
    }

    fun updateSelectedSpecies(species: Espece) {
        // Note: This would need to be handled by the repository in a real implementation
        // For now, we'll just return a new instance
    }
}

/** Ensemble des préférences de toutes les espèces */
@Serializable
data class PreferencesApplication(
        val preferencesParEspece: Map<String, PreferencesEspece> = emptyMap(),
        val versionPreferences: Int = 1
) {
    /** Obtient les préférences pour une espèce donnée */
    fun getPreferencesEspece(espece: Espece): PreferencesEspece {
        return preferencesParEspece[espece.name] ?: PreferencesEspece(espece)
    }

    /** Met à jour les préférences pour une espèce */
    fun updatePreferencesEspece(preferences: PreferencesEspece): PreferencesApplication {
        val nouvellesPreferences = preferencesParEspece.toMutableMap()
        nouvellesPreferences[preferences.selectedSpecies.name] = preferences
        return copy(preferencesParEspece = nouvellesPreferences)
    }
}
