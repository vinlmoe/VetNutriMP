package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.FoodGroup
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.TargetAdjust
import kotlin.random.Random
import kotlinx.serialization.Serializable

@Serializable
data class AlimentRation(
        val uuid: String = Random.nextInt().toString(),
        val refAlimUnif: String = "",
        val refRation: String = "",
        val quantity: Double = 0.0,
        val target: TargetAdjust = TargetAdjust.NO,
        val weight: Double = 0.0,
        val density: Double = 0.0,
        val prop: Double = 0.0,
        val alim: AlimentEv? = null
) {
    fun upUUID(): AlimentRation = copy(uuid = Random.nextInt().toString())

    fun getPresentation(): String = alim?.presentation ?: ""

    fun getNom(): String = alim?.nom ?: ""

    fun getMarque(): String = alim?.marque ?: ""

    fun getGamme(): String = alim?.gamme ?: ""

    fun getType(): FoodKind = alim?.typeAliment ?: FoodKind.MEN

    fun getGroup(): FoodGroup = alim?.group ?: FoodGroup.AUTRES

    fun getDescription(): String = alim?.ingredients ?: ""

    fun clone(): AlimentRation = copy(uuid = Random.nextInt().toString())

    fun calculateTarget(): TargetAdjust {
        if (alim == null) return TargetAdjust.NO

        val msContent = 100f - (alim.getNutrient(NutrientMain.HUMIDITE) ?: 0f)
        val nutrientValues = alim.nutrientValues

        return when (alim.typeAliment) {
            FoodKind.COMPLET -> TargetAdjust.COMP
            FoodKind.COMPLEMENTAIRE -> {
                TargetAdjust.calculateTargetFromNutrients(msContent, nutrientValues)
            }
            else -> TargetAdjust.NO
        }
    }

    companion object {
        fun fromAliment(aliment: AlimentEv): AlimentRation {
            return AlimentRation(
                    uuid = Random.nextInt().toString(),
                    refAlimUnif = aliment.uuid,
                    alim = aliment,
                    target =
                            when (aliment.typeAliment) {
                                FoodKind.COMPLET -> TargetAdjust.COMP
                                FoodKind.COMPLEMENTAIRE -> {
                                    val msContent =
                                            100f -
                                                    (aliment.getNutrient(NutrientMain.HUMIDITE)
                                                            ?: 0f)
                                    val nutrientValues = aliment.nutrientValues
                                    TargetAdjust.calculateTargetFromNutrients(
                                            msContent,
                                            nutrientValues
                                    )
                                }
                                else -> TargetAdjust.NO
                            }
            )
        }
    }
}
