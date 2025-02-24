package fr.vetbrain.vetnutri_mp.DataBase

import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun testBooleanConversion() {
        // Test conversion true/false vers 1/0
        assertEquals(1, converters.fromBoolean(true))
        assertEquals(0, converters.fromBoolean(false))

        // Test conversion 1/0 vers true/false
        assertEquals(true, converters.toBoolean(1))
        assertEquals(false, converters.toBoolean(0))

        // Test valeurs nulles
        assertNull(converters.fromBoolean(null))
        assertNull(converters.toBoolean(null))
    }

    @Test
    fun testGroupAlimConversion() {
        // Test conversion enum vers Int
        assertEquals(GroupAlim.CEREALES.ordinal, converters.fromGroupAlim(GroupAlim.CEREALES))
        assertEquals(GroupAlim.PROTEINES.ordinal, converters.fromGroupAlim(GroupAlim.PROTEINES))

        // Test conversion Int vers enum
        assertEquals(GroupAlim.CEREALES, converters.toGroupAlim(GroupAlim.CEREALES.ordinal))
        assertEquals(GroupAlim.PROTEINES, converters.toGroupAlim(GroupAlim.PROTEINES.ordinal))

        // Test valeurs nulles
        assertNull(converters.fromGroupAlim(null))
        assertNull(converters.toGroupAlim(null))
    }

    @Test
    fun testFoodKindConversion() {
        // Test conversion enum vers Int
        assertEquals(FoodKind.COMPLET.ordinal, converters.fromFoodKind(FoodKind.COMPLET))
        assertEquals(
                FoodKind.COMPLEMENTAIRE.ordinal,
                converters.fromFoodKind(FoodKind.COMPLEMENTAIRE)
        )

        // Test conversion Int vers enum
        assertEquals(FoodKind.COMPLET, converters.toFoodKind(FoodKind.COMPLET.ordinal))
        assertEquals(
                FoodKind.COMPLEMENTAIRE,
                converters.toFoodKind(FoodKind.COMPLEMENTAIRE.ordinal)
        )

        // Test valeurs nulles
        assertNull(converters.fromFoodKind(null))
        assertNull(converters.toFoodKind(null))
    }

    @Test
    fun testAlimIndicListConversion() {
        val indications = listOf(AlimIndic.PHYS, AlimIndic.SEN)

        // Test conversion liste vers String
        val jsonString = converters.fromAlimIndicList(indications)
        assertNotNull(jsonString)
        assertTrue(jsonString.contains("PHYS"))
        assertTrue(jsonString.contains("SEN"))

        // Test conversion String vers liste
        val convertedList = converters.toAlimIndicList(jsonString)
        assertNotNull(convertedList)
        assertEquals(2, convertedList.size)
        assertTrue(convertedList.contains(AlimIndic.PHYS))
        assertTrue(convertedList.contains(AlimIndic.SEN))

        // Test valeurs nulles
        assertNull(converters.fromAlimIndicList(null))
        assertNull(converters.toAlimIndicList(null))
    }

    @Test
    fun testStringListConversion() {
        val species = listOf("CHIEN", "CHAT")

        // Test conversion liste vers String
        val jsonString = converters.fromStringList(species)
        assertNotNull(jsonString)
        assertTrue(jsonString.contains("CHIEN"))
        assertTrue(jsonString.contains("CHAT"))

        // Test conversion String vers liste
        val convertedList = converters.toStringList(jsonString)
        assertNotNull(convertedList)
        assertEquals(2, convertedList.size)
        assertTrue(convertedList.contains("CHIEN"))
        assertTrue(convertedList.contains("CHAT"))

        // Test valeurs nulles
        assertNull(converters.fromStringList(null))
        assertNull(converters.toStringList(null))
    }

    @Test
    fun testInvalidInputs() {
        // Test conversion de valeurs booléennes invalides
        assertNull(converters.toBoolean(2))
        assertNull(converters.toBoolean(-1))

        // Test conversion d'ordinal invalide pour GroupAlim
        assertNull(converters.toGroupAlim(999))

        // Test conversion d'ordinal invalide pour FoodKind
        assertNull(converters.toFoodKind(999))

        // Test conversion de JSON invalide pour listes
        assertNull(converters.toAlimIndicList("{invalid json}"))
        assertNull(converters.toStringList("{invalid json}"))
    }
}


import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun testBooleanConversion() {
        // Test conversion true/false vers 1/0
        assertEquals(1, converters.fromBoolean(true))
        assertEquals(0, converters.fromBoolean(false))

        // Test conversion 1/0 vers true/false
        assertEquals(true, converters.toBoolean(1))
        assertEquals(false, converters.toBoolean(0))

        // Test valeurs nulles
        assertNull(converters.fromBoolean(null))
        assertNull(converters.toBoolean(null))
    }

    @Test
    fun testGroupAlimConversion() {
        // Test conversion enum vers Int
        assertEquals(GroupAlim.CEREALES.ordinal, converters.fromGroupAlim(GroupAlim.CEREALES))
        assertEquals(GroupAlim.PROTEINES.ordinal, converters.fromGroupAlim(GroupAlim.PROTEINES))

        // Test conversion Int vers enum
        assertEquals(GroupAlim.CEREALES, converters.toGroupAlim(GroupAlim.CEREALES.ordinal))
        assertEquals(GroupAlim.PROTEINES, converters.toGroupAlim(GroupAlim.PROTEINES.ordinal))

        // Test valeurs nulles
        assertNull(converters.fromGroupAlim(null))
        assertNull(converters.toGroupAlim(null))
    }

    @Test
    fun testFoodKindConversion() {
        // Test conversion enum vers Int
        assertEquals(FoodKind.COMPLET.ordinal, converters.fromFoodKind(FoodKind.COMPLET))
        assertEquals(
                FoodKind.COMPLEMENTAIRE.ordinal,
                converters.fromFoodKind(FoodKind.COMPLEMENTAIRE)
        )

        // Test conversion Int vers enum
        assertEquals(FoodKind.COMPLET, converters.toFoodKind(FoodKind.COMPLET.ordinal))
        assertEquals(
                FoodKind.COMPLEMENTAIRE,
                converters.toFoodKind(FoodKind.COMPLEMENTAIRE.ordinal)
        )

        // Test valeurs nulles
        assertNull(converters.fromFoodKind(null))
        assertNull(converters.toFoodKind(null))
    }

    @Test
    fun testAlimIndicListConversion() {
        val indications = listOf(AlimIndic.PHYS, AlimIndic.SEN)

        // Test conversion liste vers String
        val jsonString = converters.fromAlimIndicList(indications)
        assertNotNull(jsonString)
        assertTrue(jsonString.contains("PHYS"))
        assertTrue(jsonString.contains("SEN"))

        // Test conversion String vers liste
        val convertedList = converters.toAlimIndicList(jsonString)
        assertNotNull(convertedList)
        assertEquals(2, convertedList.size)
        assertTrue(convertedList.contains(AlimIndic.PHYS))
        assertTrue(convertedList.contains(AlimIndic.SEN))

        // Test valeurs nulles
        assertNull(converters.fromAlimIndicList(null))
        assertNull(converters.toAlimIndicList(null))
    }

    @Test
    fun testStringListConversion() {
        val species = listOf("CHIEN", "CHAT")

        // Test conversion liste vers String
        val jsonString = converters.fromStringList(species)
        assertNotNull(jsonString)
        assertTrue(jsonString.contains("CHIEN"))
        assertTrue(jsonString.contains("CHAT"))

        // Test conversion String vers liste
        val convertedList = converters.toStringList(jsonString)
        assertNotNull(convertedList)
        assertEquals(2, convertedList.size)
        assertTrue(convertedList.contains("CHIEN"))
        assertTrue(convertedList.contains("CHAT"))

        // Test valeurs nulles
        assertNull(converters.fromStringList(null))
        assertNull(converters.toStringList(null))
    }

    @Test
    fun testInvalidInputs() {
        // Test conversion de valeurs booléennes invalides
        assertNull(converters.toBoolean(2))
        assertNull(converters.toBoolean(-1))

        // Test conversion d'ordinal invalide pour GroupAlim
        assertNull(converters.toGroupAlim(999))

        // Test conversion d'ordinal invalide pour FoodKind
        assertNull(converters.toFoodKind(999))

        // Test conversion de JSON invalide pour listes
        assertNull(converters.toAlimIndicList("{invalid json}"))
        assertNull(converters.toStringList("{invalid json}"))
    }
}
