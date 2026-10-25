package fr.vetbrain.vetnutri_mp.View

import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.ParametresTypeTroupeau
import fr.vetbrain.vetnutri_mp.Data.TypeAnimalTroupeau
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TroupeauComponentsTest {

    private val brebis = TypeAnimalTroupeau(id = "b", nom = "Brebis", nombre = 30, poids = 60.0)
    private val agneaux = TypeAnimalTroupeau(id = "a", nom = "Agneaux", nombre = 40, poids = 15.0)

    @Test
    fun groupesIncomplets_referentielOuPoidsManquant() {
        val consultation =
                ConsultationEv(
                        parametresTroupeau =
                                mutableListOf(
                                        ParametresTypeTroupeau("b", nombre = 30, poids = 60.0, referenceId = "ref"),
                                        ParametresTypeTroupeau("a", nombre = 40, poids = 0.0, referenceId = "ref")
                                )
                )
        assertEquals(listOf("Agneaux"), groupesIncompletsTroupeau(listOf(brebis, agneaux), consultation))
        // Sans paramètres : valeurs du troupeau, sans référentiel → tous incomplets
        assertEquals(listOf("Brebis", "Agneaux"), groupesIncompletsTroupeau(listOf(brebis, agneaux), ConsultationEv()))
    }

    @Test
    fun groupesIncomplets_groupeSansIndividuIgnore() {
        val consultation =
                ConsultationEv(
                        parametresTroupeau =
                                mutableListOf(
                                        ParametresTypeTroupeau("b", nombre = 30, poids = 60.0, referenceId = "ref"),
                                        ParametresTypeTroupeau("a", nombre = 0)
                                )
                )
        assertTrue(groupesIncompletsTroupeau(listOf(brebis, agneaux), consultation).isEmpty())
    }

    @Test
    fun groupesIncomplets_referentielIntrouvableOuKNonPositif() {
        val consultation =
                ConsultationEv(
                        parametresTroupeau =
                                mutableListOf(
                                        ParametresTypeTroupeau("b", nombre = 30, poids = 60.0, referenceId = "supprime"),
                                        ParametresTypeTroupeau("a", nombre = 40, poids = 15.0, referenceId = "ref", k = 0.0)
                                )
                )
        assertEquals(listOf("Brebis", "Agneaux"), groupesIncompletsTroupeau(listOf(brebis, agneaux), consultation, setOf("ref")))
        // Référentiels non chargés : l'existence n'est pas vérifiée, seul K reste bloquant
        assertEquals(listOf("Agneaux"), groupesIncompletsTroupeau(listOf(brebis, agneaux), consultation))
    }

    @Test
    fun typesParDefaut_especeDeLaFiche() {
        val types = typesTroupeauParDefaut(Espece.CHEVAL)
        assertEquals(Espece.CHEVAL, types.single().especeEnum())
        assertEquals(null, typesTroupeauParDefaut().single().espece)
    }

    @Test
    fun formatage_nombresEtGrammes() {
        assertEquals("1235", formaterNombreTroupeau(1234.6))
        assertEquals("12,5", formaterNombreTroupeau(12.5))
        assertEquals("0,125", formaterNombreTroupeau(0.125))
        assertEquals("—", formaterNombreTroupeau(null))
        assertEquals("—", formaterNombreTroupeau(Double.NaN))
        assertEquals("-2,50", formaterNombreTroupeau(-2.5))
        assertEquals("250,0 g", formaterGrammesTroupeau(250.0))
        assertEquals("1,50 kg", formaterGrammesTroupeau(1500.0))
    }
}
