package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.Data.CoefP
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import kotlin.test.*
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class ReferenceEvTest {
    private lateinit var reference: ReferenceEv

    @BeforeTest
    fun setup() {
        reference =
                ReferenceEv(
                        name = "Test Reference",
                        description = "Description test",
                        disease = false,
                        BWeqRef = "eq1",
                        SERName = "Standard Energy Requirement",
                        SERRef = "eq2",
                        DEcomRef = "eq3",
                        DErawRef = "eq4",
                        k1Name = "k1",
                        k1Ref = "coef1",
                        k2Name = "k2",
                        k2Ref = "coef2",
                        k3Name = "k3",
                        k3Ref = "coef3",
                        k4Name = "k4",
                        k4Ref = "coef4",
                        k5Name = "k5",
                        k5Ref = "coef5",
                        specie = "CHIEN",
                        consistent = 1,
                        modk1 = mutableListOf(),
                        modk2 = mutableListOf(),
                        modk3 = mutableListOf(),
                        modk4 = mutableListOf(),
                        modk5 = mutableListOf(),
                        nutEqu = mutableListOf()
                )
    }

    @Test
    fun `test reference creation with all fields`() {
        assertEquals("Test Reference", reference.name)
        assertEquals("Description test", reference.description)
        assertFalse(reference.disease ?: true)
        assertEquals("eq1", reference.BWeqRef)
        assertEquals("Standard Energy Requirement", reference.SERName)
        assertEquals("eq2", reference.SERRef)
        assertEquals("eq3", reference.DEcomRef)
        assertEquals("eq4", reference.DErawRef)
        assertEquals("CHIEN", reference.specie)
        assertEquals(1, reference.consistent)
    }

    @Test
    fun `test reference creation with minimal fields`() {
        val minimalReference =
                ReferenceEv(
                        name = "Minimal",
                        description = null,
                        disease = null,
                        BWeqRef = null,
                        SERName = null,
                        SERRef = null,
                        DEcomRef = null,
                        DErawRef = null,
                        k1Name = null,
                        k1Ref = null,
                        k2Name = null,
                        k2Ref = null,
                        k3Name = null,
                        k3Ref = null,
                        k4Name = null,
                        k4Ref = null,
                        k5Name = null,
                        k5Ref = null,
                        specie = null,
                        consistent = null,
                        modk1 = mutableListOf(),
                        modk2 = mutableListOf(),
                        modk3 = mutableListOf(),
                        modk4 = mutableListOf(),
                        modk5 = mutableListOf(),
                        nutEqu = mutableListOf()
                )

        assertEquals("Minimal", minimalReference.name)
        assertNull(minimalReference.description)
        assertNull(minimalReference.disease)
        assertNull(minimalReference.BWeqRef)
        assertNull(minimalReference.SERName)
        assertTrue(minimalReference.modk1.isEmpty())
        assertTrue(minimalReference.nutEqu.isEmpty())
    }

    @Test
    fun `test add and remove coefficients`() {
        val coef = CoefP(description = "Test coefficient", coef = 1.5f, groupUUID = 1)

        assertTrue(reference.modk1.isEmpty())
        reference.modk1.add(coef)
        assertEquals(1, reference.modk1.size)

        reference.modk1.remove(coef)
        assertTrue(reference.modk1.isEmpty())
    }

    @Test
    fun `test add and remove equations`() {
        val equation =
                Equation(
                        script = "x * 2",
                        name = "Test equation",
                        description = "Test",
                        specie = "CHIEN",
                        kind = 1,
                        consistent = true,
                        nutrient = 1,
                        refBiblio = "test-ref",
                        varMutableList = mutableListOf()
                )

        assertTrue(reference.nutEqu.isEmpty())
        reference.nutEqu.add(equation)
        assertEquals(1, reference.nutEqu.size)

        reference.nutEqu.remove(equation)
        assertTrue(reference.nutEqu.isEmpty())
    }
}
