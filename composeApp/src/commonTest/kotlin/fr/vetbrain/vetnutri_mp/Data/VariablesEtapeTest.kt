package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.TypeConsultation
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import fr.vetbrain.vetnutri_mp.Utils.VariablesEnergie
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Résolution du poids et des variables par étape de plan évolutif, et détection des variables à
 * saisir d'après les noms écrits dans les équations.
 */
class VariablesEtapeTest {

    private val aw25 = SupplementalvariableP(VariableKind.AdultWeight, 25.0)

    private fun consultation(type: TypeConsultation, vararg rations: Ration) =
            ConsultationEv(
                    uuid = "c",
                    weight = 5.0,
                    idealWeight = 6.0,
                    typeConsultation = type,
                    suppVarp = mutableListOf(aw25),
                    rations = rations.toMutableList()
            )

    @Test
    fun rationPrincipale_isUnchanged() {
        // Sans ration parente, ni le poids ni les variables propres ne s'appliquent
        val ration = Ration(uuid = "r", etapeEvolutive = true, poids = 9.0)
        val c = consultation(TypeConsultation.EVOLUTIVE, ration)

        assertFalse(VariablesEtape.estEtape(c, ration))
        assertEquals(6.0, VariablesEtape.poidsEtape(c, ration)) // poids idéal prioritaire
        assertSame(c, VariablesEtape.consultationPourEtape(c, ration))
    }

    @Test
    fun evolutiveStep_usesOwnWeight_andRealWeightWhenNull() {
        val etape = Ration(uuid = "e", etapeEvolutive = true, poids = 9.0, refRationParente = "p")
        val reel = Ration(uuid = "r", etapeEvolutive = true, refRationParente = "p")
        val c = consultation(TypeConsultation.EVOLUTIVE, etape, reel)

        assertEquals(9.0, VariablesEtape.poidsEtape(c, etape))
        // Étape au poids réel : jamais le poids idéal
        assertEquals(5.0, VariablesEtape.poidsEtape(c, reel))

        val vue = VariablesEtape.consultationPourEtape(c, etape)
        assertEquals(9.0, vue.effectiveWeight)
        assertNull(vue.idealWeight)
        // La consultation d'origine n'est pas modifiée
        assertEquals(5.0, c.weight)
    }

    @Test
    fun rationOutsidePlan_keepsConsultationBehaviour() {
        val horsPlan = Ration(uuid = "h", poids = 9.0)
        val c = consultation(TypeConsultation.EVOLUTIVE, horsPlan)
        assertEquals(6.0, VariablesEtape.poidsEtape(c, horsPlan))
    }

    @Test
    fun stepVariables_overrideConsultationVariables() {
        val etape =
                Ration(
                        uuid = "e",
                        etapeEvolutive = true,
                        refRationParente = "p",
                        suppVarp =
                                mutableListOf(
                                        SupplementalvariableP(VariableKind.AdultWeight, 30.0),
                                        SupplementalvariableP(VariableKind.D, 12.0)
                                )
                )
        val c = consultation(TypeConsultation.EVOLUTIVE, etape)

        val fusion = VariablesEtape.variablesFusionnees(c, etape).associate { it.variable to it.varue }
        assertEquals(mapOf<VariableKind?, Double?>(VariableKind.AdultWeight to 30.0, VariableKind.D to 12.0), fusion)
        assertEquals(listOf(aw25), c.suppVarp)
    }

    @Test
    fun signature_changesWithWeightAndVariables() {
        val etape = Ration(uuid = "e", etapeEvolutive = true, poids = 9.0, refRationParente = "p")
        val c = consultation(TypeConsultation.EVOLUTIVE, etape)
        val s1 = VariablesEtape.signature(c, etape)
        etape.poids = 10.0
        val s2 = VariablesEtape.signature(c, etape)
        etape.suppVarp = mutableListOf(SupplementalvariableP(VariableKind.D, 3.0))
        val s3 = VariablesEtape.signature(c, etape)
        assertTrue(s1 != s2 && s2 != s3)
    }

    @Test
    fun injecterVariables_usesEquationNames_andNeverOverridesComputedOnes() {
        val variables = mutableMapOf("BW" to 8.0)
        VariablesEtape.injecterVariables(
                variables,
                listOf(
                        SupplementalvariableP(VariableKind.AdultWeight, 25.0),
                        SupplementalvariableP(VariableKind.CW, 4.0),
                        SupplementalvariableP(VariableKind.BW, 99.0)
                )
        )
        assertEquals(mapOf("BW" to 8.0, "AW" to 25.0, "CW" to 4.0), variables)
    }

    @Test
    fun variablesEnergieRequises_readsEnergyAndEnercompEquationsOnly() {
        val reference =
                ReferenceEv(uuid = "ref").apply {
                    equationBW = Equation(equationScript = "BW^0.75")
                    equationBEE = Equation(equationScript = "(254-135*BW/AW)*BW^0.75")
                    equationsNut = mutableListOf(Equation(equationScript = "wG * 2"))
                }
        val maladie =
                ReferenceEv(uuid = "mal").apply {
                    equationsNut =
                            mutableListOf(
                                    Equation(equationScript = "D * BW", kind = EquationKind.ENERCOMP),
                                    Equation(equationScript = "L * 3")
                            )
                }

        assertEquals(
                listOf(VariableKind.AdultWeight, VariableKind.D),
                VariablesEnergie.variablesEnergieRequises(reference, listOf(maladie))
        )
        // Saisie niveau consultation : les équations nutritionnelles de la référence comptent aussi
        assertEquals(
                setOf(VariableKind.AdultWeight, VariableKind.D, VariableKind.WeekGestation),
                VariablesEnergie.variablesRequises(reference, listOf(maladie)).toSet()
        )
    }
}
