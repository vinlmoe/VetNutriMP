package fr.vetbrain.vetnutri_mp.DataBase

import fr.vetbrain.vetnutri_mp.Data.NutrientQuantity
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toNutrientValueEntities
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import kotlin.test.Test
import kotlin.test.assertEquals

class NutrientValueMapperTest {

    @Test
    fun toNutrientValueEntities_conserveValeurExpliciteZero() {
        val values: Map<Nutrient, NutrientQuantity> = mapOf(
                NutrientMain.ENA to NutrientQuantity(0.0, NutrientMain.ENA.ue.label)
        )

        val entities = values.toNutrientValueEntities("aliment-test")

        assertEquals(1, entities.size)
        assertEquals(NutrientMain.ENA.label, entities.single().nutrientLabel)
        assertEquals(0.0, entities.single().value)
    }
}
