package fr.vetbrain.vetnutri_mp.View.Graph

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.PreferencesEspece
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate

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
            title = translate("auto.view.graph.graphconfigurations.repartition_energetique_proteines_vs_lipides"),
            type = GraphType.SCATTER_PLOT,
            xAxis = AxisConfig(
                label = translate("auto.view.graph.graphconfigurations.energie_proteines"),
                unit = "%",
                minValue = 0f,
                maxValue = 100f
            ),
            yAxis = AxisConfig(
                label = translate("auto.view.graph.graphconfigurations.energie_lipides"), 
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
            title = translate("auto.view.graph.graphconfigurations.phosphore_vs_proteines_pour_1000_kcal"),
            type = GraphType.SCATTER_PLOT,
            xAxis = AxisConfig(
                label = translate("enum.NutrientMacro.PHOS"),
                unit = "mg/1000kcal"
            ),
            yAxis = AxisConfig(
                label = translate("chart.label.protein"),
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
                title = translate("auto.view.graph.graphconfigurations.proteines_lipides"),
                config = createProteinLipidEnergyGraph(referenceEv, preferencesEspece)
            ),
            GraphTab(
                id = "phosphore_protein_1000kcal",
                title = translate("auto.view.analysegraphiquealimentsview.phosphore_proteines"),
                config = createPhosphoreProteinPer1000KcalGraph(referenceEv, preferencesEspece)
            )
        )
    }
}
