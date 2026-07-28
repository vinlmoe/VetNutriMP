package fr.vetbrain.vetnutri_mp.View.Graph

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.PreferencesEspece
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro

/**
 * Utilitaires de calcul pour les graphiques nutritionnels
 */
object GraphCalculations {
    
    // Constantes de conversion énergétique (kcal/g)
    private const val ENERGIE_PROTEINES = 3.5f
    private const val ENERGIE_LIPIDES = 8.5f
    private const val ENERGIE_GLUCIDES = 3.5f
    
    /**
     * Calcule le pourcentage d'énergie provenant des protéines
     */
    suspend fun calculateProteinEnergyPercentage(
        aliment: AlimentEv,
        referenceEv: ReferenceEv,
        preferencesEspece: PreferencesEspece
    ): Float {
        val alimentRation = AlimentRation(
            aliment = aliment,
            quantite = 100.0, // 100g pour les calculs
            weight = 1.0
        )
        
        val proteines = alimentRation.getNutrientWithComplementary(
            nutrient = NutrientMain.PROTEINE,
            preferences = preferencesEspece,
            equationRepository = null, // Sera injecté plus tard
            referenceEv = referenceEv
        ) ?: 0.0
        
        val lipides = alimentRation.getNutrientWithComplementary(
            nutrient = NutrientMain.LIPIDE,
            preferences = preferencesEspece,
            equationRepository = null, // Sera injecté plus tard
            referenceEv = referenceEv
        ) ?: 0.0
        
        val glucides = alimentRation.getNutrientWithComplementary(
            nutrient = NutrientMain.GLUCIDE,
            preferences = preferencesEspece,
            equationRepository = null, // Sera injecté plus tard
            referenceEv = referenceEv
        ) ?: 0.0
        
        val energieProteines = proteines * ENERGIE_PROTEINES
        val energieLipides = lipides * ENERGIE_LIPIDES
        val energieGlucides = glucides * ENERGIE_GLUCIDES
        val energieTotale = energieProteines + energieLipides + energieGlucides
        
        return if (energieTotale > 0) {
            ((energieProteines / energieTotale) * 100).toFloat()
        } else {
            0f
        }
    }
    
    /**
     * Calcule le pourcentage d'énergie provenant des lipides
     */
    suspend fun calculateLipidEnergyPercentage(
        aliment: AlimentEv,
        referenceEv: ReferenceEv,
        preferencesEspece: PreferencesEspece
    ): Float {
        val alimentRation = AlimentRation(
            aliment = aliment,
            quantite = 100.0, // 100g pour les calculs
            weight = 1.0
        )
        
        val proteines = alimentRation.getNutrientWithComplementary(
            nutrient = NutrientMain.PROTEINE,
            preferences = preferencesEspece,
            equationRepository = null, // Sera injecté plus tard
            referenceEv = referenceEv
        ) ?: 0.0
        
        val lipides = alimentRation.getNutrientWithComplementary(
            nutrient = NutrientMain.LIPIDE,
            preferences = preferencesEspece,
            equationRepository = null, // Sera injecté plus tard
            referenceEv = referenceEv
        ) ?: 0.0
        
        val glucides = alimentRation.getNutrientWithComplementary(
            nutrient = NutrientMain.GLUCIDE,
            preferences = preferencesEspece,
            equationRepository = null, // Sera injecté plus tard
            referenceEv = referenceEv
        ) ?: 0.0
        
        val energieProteines = proteines * ENERGIE_PROTEINES
        val energieLipides = lipides * ENERGIE_LIPIDES
        val energieGlucides = glucides * ENERGIE_GLUCIDES
        val energieTotale = energieProteines + energieLipides + energieGlucides
        
        return if (energieTotale > 0) {
            ((energieLipides / energieTotale) * 100).toFloat()
        } else {
            0f
        }
    }
    
    /**
     * Calcule la quantité de protéines pour 1000 kcal d'aliment
     */
    suspend fun calculateProteinPer1000Kcal(
        aliment: AlimentEv,
        referenceEv: ReferenceEv,
        preferencesEspece: PreferencesEspece
    ): Float {
        val alimentRation = AlimentRation(
            aliment = aliment,
            quantite = 100.0, // 100g pour les calculs
            weight = 1.0
        )
        
        val proteines = alimentRation.getNutrientWithComplementary(
            nutrient = NutrientMain.PROTEINE,
            preferences = preferencesEspece,
            equationRepository = null, // Sera injecté plus tard
            referenceEv = referenceEv
        ) ?: 0.0
        
        val lipides = alimentRation.getNutrientWithComplementary(
            nutrient = NutrientMain.LIPIDE,
            preferences = preferencesEspece,
            equationRepository = null, // Sera injecté plus tard
            referenceEv = referenceEv
        ) ?: 0.0
        
        val glucides = alimentRation.getNutrientWithComplementary(
            nutrient = NutrientMain.GLUCIDE,
            preferences = preferencesEspece,
            equationRepository = null, // Sera injecté plus tard
            referenceEv = referenceEv
        ) ?: 0.0
        
        val energieProteines = proteines * ENERGIE_PROTEINES
        val energieLipides = lipides * ENERGIE_LIPIDES
        val energieGlucides = glucides * ENERGIE_GLUCIDES
        val energieTotale = energieProteines + energieLipides + energieGlucides
        
        return if (energieTotale > 0) {
            ((proteines * 1000) / energieTotale).toFloat()
        } else {
            0f
        }
    }
    
    /**
     * Calcule la quantité de phosphore pour 1000 kcal d'aliment
     */
    suspend fun calculatePhosphorePer1000Kcal(
        aliment: AlimentEv,
        referenceEv: ReferenceEv,
        preferencesEspece: PreferencesEspece
    ): Float {
        val alimentRation = AlimentRation(
            aliment = aliment,
            quantite = 100.0, // 100g pour les calculs
            weight = 1.0
        )
        
        // Récupérer le phosphore en mg/100g
        val phosphore = alimentRation.getNutrientWithComplementary(
            nutrient = NutrientMacro.PHOS,
            preferences = preferencesEspece,
            equationRepository = null, // Sera injecté plus tard
            referenceEv = referenceEv
        ) ?: 0.0
        
        // Calculer l'énergie totale
        val proteines = alimentRation.getNutrientWithComplementary(
            nutrient = NutrientMain.PROTEINE,
            preferences = preferencesEspece,
            equationRepository = null, // Sera injecté plus tard
            referenceEv = referenceEv
        ) ?: 0.0
        
        val lipides = alimentRation.getNutrientWithComplementary(
            nutrient = NutrientMain.LIPIDE,
            preferences = preferencesEspece,
            equationRepository = null, // Sera injecté plus tard
            referenceEv = referenceEv
        ) ?: 0.0
        
        val glucides = alimentRation.getNutrientWithComplementary(
            nutrient = NutrientMain.GLUCIDE,
            preferences = preferencesEspece,
            equationRepository = null, // Sera injecté plus tard
            referenceEv = referenceEv
        ) ?: 0.0
        
        val energieProteines = proteines * ENERGIE_PROTEINES
        val energieLipides = lipides * ENERGIE_LIPIDES
        val energieGlucides = glucides * ENERGIE_GLUCIDES
        val energieTotale = energieProteines + energieLipides + energieGlucides
        
        return if (energieTotale > 0) {
            ((phosphore * 1000) / energieTotale).toFloat()
        } else {
            0f
        }
    }
    
    /**
     * Crée une fonction de calcul générique avec gestion des erreurs
     */
    fun createCalculationFunction(
        calculation: suspend (AlimentEv, ReferenceEv, PreferencesEspece) -> Float
    ): suspend (AlimentEv) -> Float {
        return { aliment ->
            try {
                // Ces valeurs seront injectées par le composant parent
                // Pour l'instant, on retourne 0f - sera remplacé par l'injection de dépendances
                0f
            } catch (e: Exception) {
                
                0f
            }
        }
    }
}
