package fr.vetbrain.vetnutri_mp.DataBase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun testLocalDateConversion() {
        val date = LocalDate(2024, 3, 14)
        val dateString = converters.fromLocalDate(date)
        assertEquals(date, converters.toLocalDate(dateString))

        assertNull(converters.fromLocalDate(null))
        assertNull(converters.toLocalDate(null))
    }

    @Test
    fun testBooleanConversion() {
        assertEquals(1, converters.fromBoolean(true))
        assertEquals(0, converters.fromBoolean(false))
        assertEquals(true, converters.toBoolean(1))
        assertEquals(false, converters.toBoolean(0))

        assertNull(converters.fromBoolean(null))
        assertNull(converters.toBoolean(null))
    }

    @Test
    fun testFloatConversion() {
        val float = 3.14f
        val floatString = converters.fromFloat(float)
        assertEquals(float, converters.toFloat(floatString))

        assertNull(converters.fromFloat(null))
        assertNull(converters.toFloat(null))
        assertNull(converters.toFloat("invalid"))
    }

    @Test
    fun testDoubleConversion() {
        val double = 3.14159265359
        val doubleString = converters.fromDouble(double)
        assertEquals(double, converters.toDouble(doubleString))

        assertNull(converters.fromDouble(null))
        assertNull(converters.toDouble(null))
        assertNull(converters.toDouble("invalid"))
    }
}
