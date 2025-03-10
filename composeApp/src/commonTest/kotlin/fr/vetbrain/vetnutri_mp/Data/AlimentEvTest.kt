package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.BaseTest
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.ContEnum
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import kotlin.test.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class AlimentEvTest : BaseTest() {
    private lateinit var alimentTest: AlimentEv

    @BeforeTest
    override fun setUp() {
        super.setUp()
        alimentTest = AlimentEv(rationUUID = null)
    }

    @Test
    fun `test création d'un aliment avec constructeur par défaut`() {
        assertNotNull(alimentTest.uuid)
        assertNull(alimentTest.group)
        assertNull(alimentTest.typeAliment)
        assertNull(alimentTest.ingredients)
        assertNull(alimentTest.price)
        assertNull(alimentTest.categPrice)
        assertNull(alimentTest.brand)
        assertNull(alimentTest.gamme)
        assertNull(alimentTest.nom)
        assertFalse(alimentTest.consistent)
        assertNull(alimentTest.cont)
        assertNull(alimentTest.quantInt)
        assertFalse(alimentTest.deprecated)
        assertNull(alimentTest.dataB)
        assertTrue(alimentTest.especes.isEmpty())
        assertTrue(alimentTest.indicat.isEmpty())
        assertTrue(alimentTest.valMap.isEmpty())
        assertNull(alimentTest.rationUUID)
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

        with(aliment) {
            assertEquals("test-uuid", uuid)
            assertEquals(GroupAlim.ABATS, group)
            assertEquals(FoodKind.COMPLET, typeAliment)
            assertEquals("Ingrédients test", ingredients)
            assertValeurPositive(price!!)
            assertEquals("Premium", categPrice)
            assertEquals("Marque Test", brand)
            assertEquals("Gamme Test", gamme)
            assertEquals("Aliment Test", nom)
            assertTrue(consistent)
            assertEquals(ContEnum.CAN, cont)
            assertValeurPositive(quantInt!!)
            assertEquals(500.0f, quantInt)
            assertFalse(deprecated)
            assertEquals("DataB test", dataB)
            assertEquals(2, especes.size)
            assertListeSansDoublons(especes)
            assertTrue(especes.containsAll(listOf("CHIEN", "CHAT")))
            assertEquals(2, indicat.size)
            assertListeSansDoublons(indicat)
            assertTrue(indicat.containsAll(listOf(AlimIndic.PHYS, AlimIndic.OBES)))
            assertTrue(valMap.isEmpty())
            assertEquals("ration-uuid", rationUUID)
        }
    }

    @Test
    fun `test ajout et modification des valeurs nutritionnelles`() {
        val nutrient = NutrientMain.PROTEINE
        val quantite = NutrientQuantity(20.5f, nutrient.label)

        // Ajout d'une valeur nutritionnelle
        alimentTest.valMap[nutrient] = quantite

        assertEquals(1, alimentTest.valMap.size)
        assertValeurPositive(alimentTest.valMap[nutrient]?.value!!)
        assertEquals(20.5f, alimentTest.valMap[nutrient]?.value)

        // Modification d'une valeur nutritionnelle
        alimentTest.valMap[nutrient] = NutrientQuantity(25.0f, nutrient.label)

        assertEquals(1, alimentTest.valMap.size)
        assertValeurPositive(alimentTest.valMap[nutrient]?.value!!)
        assertEquals(25.0f, alimentTest.valMap[nutrient]?.value)
    }

    @Test
    fun `test modification des données variables`() {
        // Test modification quantité
        alimentTest = alimentTest.copy(quantInt = 250.0f)
        assertValeurPositive(alimentTest.quantInt!!)
        assertEquals(250.0f, alimentTest.quantInt)

        // Test modification dataB
        alimentTest = alimentTest.copy(dataB = "Nouvelle dataB")
        assertEquals("Nouvelle dataB", alimentTest.dataB)

        // Test modification deprecated
        alimentTest = alimentTest.copy(deprecated = true)
        assertTrue(alimentTest.deprecated)

        // Test modification espèces
        val nouvellesEspeces = mutableListOf("CHAT")
        alimentTest = alimentTest.copy(especes = nouvellesEspeces)
        assertEquals(1, alimentTest.especes.size)
        assertListeSansDoublons(alimentTest.especes)
        assertTrue(alimentTest.especes.contains("CHAT"))

        // Test modification indications
        val nouvellesIndications = mutableListOf(AlimIndic.PHYS)
        alimentTest = alimentTest.copy(indicat = nouvellesIndications)
        assertEquals(1, alimentTest.indicat.size)
        assertListeSansDoublons(alimentTest.indicat)
        assertTrue(alimentTest.indicat.contains(AlimIndic.PHYS))
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

    @Test
    fun `test validation des valeurs nutritionnelles négatives`() {
        val nutrient = NutrientMain.PROTEINE
        val quantiteNegative = NutrientQuantity(-20.5f, nutrient.label)

        assertFailsWith<AssertionError> {
            alimentTest.valMap[nutrient] = quantiteNegative
            assertValeurPositive(alimentTest.valMap[nutrient]?.value!!)
        }
    }

    @Test
    fun `test gestion des espèces multiples`() {
        // Test ajout initial
        val especesInitiales = setOf("CHIEN", "CHAT", "LAPIN")
        alimentTest = alimentTest.copy(especes = especesInitiales.toMutableList())
        assertEquals(3, alimentTest.especes.size)
        assertListeSansDoublons(alimentTest.especes)
        assertEquals(especesInitiales, alimentTest.especes.toSet())

        // Test suppression
        val especesApresRetrait = especesInitiales - "CHAT"
        alimentTest = alimentTest.copy(especes = especesApresRetrait.toMutableList())
        assertEquals(2, alimentTest.especes.size)
        assertListeSansDoublons(alimentTest.especes)
        assertFalse(alimentTest.especes.contains("CHAT"))
        assertEquals(especesApresRetrait, alimentTest.especes.toSet())

        // Test ajout d'un doublon
        val especesAvecDoublon = (especesApresRetrait + "CHIEN").toMutableList()
        alimentTest = alimentTest.copy(especes = especesAvecDoublon)
        assertEquals(2, alimentTest.especes.size)
        assertListeSansDoublons(alimentTest.especes)
        assertEquals(especesApresRetrait, alimentTest.especes.toSet())
    }

    @Test
    fun `test gestion des indications multiples`() {
        // Test ajout initial
        val indicationsInitiales = setOf(AlimIndic.PHYS, AlimIndic.OBES, AlimIndic.DIAB)
        alimentTest = alimentTest.copy(indicat = indicationsInitiales.toMutableList())
        assertEquals(3, alimentTest.indicat.size)
        assertListeSansDoublons(alimentTest.indicat)
        assertEquals(indicationsInitiales, alimentTest.indicat.toSet())

        // Test suppression
        val indicationsApresRetrait = indicationsInitiales - AlimIndic.OBES
        alimentTest = alimentTest.copy(indicat = indicationsApresRetrait.toMutableList())
        assertEquals(2, alimentTest.indicat.size)
        assertListeSansDoublons(alimentTest.indicat)
        assertFalse(alimentTest.indicat.contains(AlimIndic.OBES))
        assertEquals(indicationsApresRetrait, alimentTest.indicat.toSet())

        // Test ajout d'un doublon
        val indicationsAvecDoublon = (indicationsApresRetrait + AlimIndic.PHYS).toMutableList()
        alimentTest = alimentTest.copy(indicat = indicationsAvecDoublon)
        assertEquals(2, alimentTest.indicat.size)
        assertListeSansDoublons(alimentTest.indicat)
        assertEquals(indicationsApresRetrait, alimentTest.indicat.toSet())
    }

    @Test
    fun `test gestion des valeurs limites`() {
        alimentTest =
                AlimentEv(
                        uuid = "test-uuid",
                        quantInt = Float.MAX_VALUE,
                        price = Double.MAX_VALUE,
                        rationUUID = null
                )

        // Vérification des valeurs limites
        assertValeurPositive(alimentTest.quantInt!!)
        assertValeurPositive(alimentTest.price!!)
        assertEquals(Float.MAX_VALUE, alimentTest.quantInt)
        assertEquals(Double.MAX_VALUE, alimentTest.price)

        // Test avec des valeurs minimales positives
        alimentTest = alimentTest.copy(quantInt = Float.MIN_VALUE, price = Double.MIN_VALUE)

        assertValeurPositive(alimentTest.quantInt!!)
        assertValeurPositive(alimentTest.price!!)
        assertEquals(Float.MIN_VALUE, alimentTest.quantInt)
        assertEquals(Double.MIN_VALUE, alimentTest.price)
    }

    @Test
    fun `test modification simultanée de plusieurs propriétés`() {
        // Modification simultanée de plusieurs propriétés
        alimentTest =
                alimentTest.copy(
                        nom = "Nouvel aliment",
                        price = 15.0,
                        quantInt = 300.0f,
                        especes = mutableListOf("CHIEN", "CHAT"),
                        indicat = mutableListOf(AlimIndic.PHYS)
                )

        with(alimentTest) {
            assertEquals("Nouvel aliment", nom)
            assertValeurPositive(price!!)
            assertEquals(15.0, price)
            assertValeurPositive(quantInt!!)
            assertEquals(300.0f, quantInt)
            assertEquals(2, especes.size)
            assertListeSansDoublons(especes)
            assertEquals(1, indicat.size)
            assertListeSansDoublons(indicat)
        }
    }
}
