package fr.vetbrain.vetnutri_mp.Utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DataBMappingTest {
    @Test
    fun vetFood2026_isAvailableWithExpectedDisplayName() {
        assertEquals(DataB.VETFOOD_2026, DataB.fromCode("VF2026"))
        assertEquals("VetFood 2026", DataB.getDisplayName("VF2026"))
        assertTrue(DataB.hasMapping("VF2026"))
        assertEquals("VetFood 2026", DataB.getAllMappings()["VF2026"])
    }
}
