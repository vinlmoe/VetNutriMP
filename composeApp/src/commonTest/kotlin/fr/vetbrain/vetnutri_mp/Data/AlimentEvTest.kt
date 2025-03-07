package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.BaseTest
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.ContEnum
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import kotlin.test.*

class AlimentEvTest : BaseTest() {

    @Test
    fun `test création d'un aliment avec constructeur par défaut`() {
        val aliment = AlimentEv(rationUUID = null)

        assertNotNull(aliment.uuid)
        assertNull(aliment.group)
        assertNull(aliment.typeAliment)
        assertNull(aliment.ingredients)
        assertNull(aliment.price)
        assertNull(aliment.categPrice)
        assertNull(aliment.brand)
        assertNull(aliment.gamme)
        assertNull(aliment.nom)
        assertFalse(aliment.consistent)
        assertNull(aliment.cont)
        assertNull(aliment.quantInt)
        assertFalse(aliment.deprecated)
        assertNull(aliment.dataB)
        assertTrue(aliment.especes.isEmpty())
        assertTrue(aliment.indicat.isEmpty())
        assertTrue(aliment.valMap.isEmpty())
        assertNull(aliment.rationUUID)
    }

    @Test
    fun `test création d'un aliment avec paramètres spécifiques`() {
        val aliment =
                AlimentEv(
                        uuid = "test-uuid",
                        group = GroupAlim.ABATS,
                        typeAliment = FoodKind.COMPLET,
                        ingredients = "Ingrédients test",
                        price = 10.5,
                        categPrice = "Premium",
                        brand = "Marque Test",
                        gamme = "Gamme Test",
                        nom = "Aliment Test",
                        consistent = true,
                        cont = ContEnum.CAN,
                        quantInt = 500.0f,
                        deprecated = false,
                        dataB = "DataB test",
                        especes = mutableListOf("CHIEN", "CHAT"),
                        indicat = mutableListOf(AlimIndic.PHYS, AlimIndic.OBES),
                        valMap = mutableMapOf(),
                        rationUUID = "ration-uuid"
                )

        assertEquals("test-uuid", aliment.uuid)
        assertEquals(GroupAlim.ABATS, aliment.group)
        assertEquals(FoodKind.COMPLET, aliment.typeAliment)
        assertEquals("Ingrédients test", aliment.ingredients)
        assertEquals(10.5, aliment.price)
        assertEquals("Premium", aliment.categPrice)
        assertEquals("Marque Test", aliment.brand)
        assertEquals("Gamme Test", aliment.gamme)
        assertEquals("Aliment Test", aliment.nom)
        assertTrue(aliment.consistent)
        assertEquals(ContEnum.CAN, aliment.cont)
        assertEquals(500.0f, aliment.quantInt)
        assertFalse(aliment.deprecated)
        assertEquals("DataB test", aliment.dataB)
        assertEquals(2, aliment.especes.size)
        assertTrue(aliment.especes.contains("CHIEN"))
        assertTrue(aliment.especes.contains("CHAT"))
        assertEquals(2, aliment.indicat.size)
        assertTrue(aliment.indicat.contains(AlimIndic.PHYS))
        assertTrue(aliment.indicat.contains(AlimIndic.OBES))
        assertTrue(aliment.valMap.isEmpty())
        assertEquals("ration-uuid", aliment.rationUUID)
    }

    @Test
    fun `test ajout et modification des valeurs nutritionnelles`() {
        val aliment = AlimentEv(rationUUID = null)
        val nutrient = NutrientMain.PROTEINE
        val quantite = NutrientQuantity(20.5f, nutrient.label)

        // Ajout d'une valeur nutritionnelle
        aliment.valMap[nutrient] = quantite

        assertEquals(1, aliment.valMap.size)
        assertEquals(20.5f, aliment.valMap[nutrient]?.value)

        // Modification d'une valeur nutritionnelle
        aliment.valMap[nutrient] = NutrientQuantity(25.0f, nutrient.label)

        assertEquals(1, aliment.valMap.size)
        assertEquals(25.0f, aliment.valMap[nutrient]?.value)
    }

    @Test
    fun `test modification des données variables`() {
        val aliment = AlimentEv(rationUUID = null)

        // Test modification quantité
        aliment.quantInt = 250.0f
        assertEquals(250.0f, aliment.quantInt)

        // Test modification dataB
        aliment.dataB = "Nouvelle dataB"
        assertEquals("Nouvelle dataB", aliment.dataB)

        // Test modification deprecated
        aliment.deprecated = true
        assertTrue(aliment.deprecated)

        // Test modification espèces
        aliment.especes.add("CHAT")
        assertEquals(1, aliment.especes.size)
        assertTrue(aliment.especes.contains("CHAT"))

        // Test modification indications
        aliment.indicat.add(AlimIndic.PHYS)
        assertEquals(1, aliment.indicat.size)
        assertTrue(aliment.indicat.contains(AlimIndic.PHYS))
    }

    @Test
    fun `test copie d'un aliment`() {
        val original =
                AlimentEv(
                        uuid = "test-uuid",
                        group = GroupAlim.ABATS,
                        nom = "Aliment Original",
                        rationUUID = "ration-uuid"
                )

        // Création d'une copie avec certaines modifications
        val copie = original.copy(nom = "Aliment Copié")

        // Vérification que les valeurs non modifiées sont identiques
        assertEquals(original.uuid, copie.uuid)
        assertEquals(original.group, copie.group)
        assertEquals(original.rationUUID, copie.rationUUID)

        // Vérification que la valeur modifiée est différente
        assertEquals("Aliment Copié", copie.nom)
    }
}
