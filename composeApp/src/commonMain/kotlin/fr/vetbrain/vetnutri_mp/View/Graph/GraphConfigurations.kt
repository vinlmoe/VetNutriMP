package fr.vetbrain.vetnutri_mp.View.Graph

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.PreferencesEspece
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv

/**
 * Configurations prédéfinies des graphiques disponibles
 */
object GraphConfigurations {
    
    /**
     * Graphique : % énergie protéines vs % énergie lipides
     */
    fun createProteinLipidEnergyGraph(
        referenceEv: ReferenceEv,
        preferencesEspece: PreferencesEspece
    ): GraphConfig {
        return GraphConfig(
            title = "Répartition énergétique - Protéines vs Lipides",
            type = GraphType.SCATTER_PLOT,
            xAxis = AxisConfig(
                label = "% énergie protéines",
                unit = "%",
                minValue = 0f,
                maxValue = 100f
            ),
            yAxis = AxisConfig(
                label = "% énergie lipides", 
                unit = "%",
                minValue = 0f,
                maxValue = 100f
            ),
            calculateX = { aliment ->
                GraphCalculations.calculateProteinEnergyPercentage(
                    aliment, referenceEv, preferencesEspece
                )
            },
            calculateY = { aliment ->
                GraphCalculations.calculateLipidEnergyPercentage(
                    aliment, referenceEv, preferencesEspece
                )
            },
            showNumbers = true,
            allowSelection = true
        )
    }
    
    /**
     * Graphique : Phosphore vs Protéines pour 1000 kcal
     */
    fun createPhosphoreProteinPer1000KcalGraph(
        referenceEv: ReferenceEv,
        preferencesEspece: PreferencesEspece
    ): GraphConfig {
        return GraphConfig(
            title = "Phosphore vs Protéines pour 1000 kcal",
            type = GraphType.SCATTER_PLOT,
            xAxis = AxisConfig(
                label = "Phosphore",
                unit = "mg/1000kcal"
            ),
            yAxis = AxisConfig(
                label = "Protéines",
                unit = "g/1000kcal"
            ),
            calculateX = { aliment ->
                GraphCalculations.calculatePhosphorePer1000Kcal(
                    aliment, referenceEv, preferencesEspece
                )
            },
            calculateY = { aliment ->
                GraphCalculations.calculateProteinPer1000Kcal(
                    aliment, referenceEv, preferencesEspece
                )
            },
            showNumbers = true,
            allowSelection = true
        )
    }
    
    /**
     * Créer la liste des onglets disponibles
     */
    fun createAvailableTabs(
        referenceEv: ReferenceEv,
        preferencesEspece: PreferencesEspece
    ): List<GraphTab> {
        return listOf(
            GraphTab(
                id = "protein_lipid_energy",
                title = "Protéines/Lipides",
                config = createProteinLipidEnergyGraph(referenceEv, preferencesEspece)
            ),
            GraphTab(
                id = "phosphore_protein_1000kcal",
                title = "Phosphore/Protéines",
                config = createPhosphoreProteinPer1000KcalGraph(referenceEv, preferencesEspece)
            )
        )
    }
}
