package fr.vetbrain.vetnutri_mp.Utils

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserErrorsTest {

    @AfterTest
    fun reset() = UserErrors.dismiss()

    @Test
    fun valeurPresenteRenvoyeeSansErreur() {
        assertEquals("ration", "ration".orUserError("aucune ration n'est sélectionnée"))
        assertNull(UserErrors.current.value)
    }

    @Test
    fun valeurAbsenteSignaleeALUtilisateur() {
        val absente: String? = null
        assertNull(absente.orUserError("aucune ration n'est sélectionnée"))
        assertEquals("Action impossible : aucune ration n'est sélectionnée.", UserErrors.current.value)
    }

    @Test
    fun fermetureEffaceLErreur() {
        UserErrors.report("Erreur de test")
        UserErrors.dismiss()
        assertNull(UserErrors.current.value)
    }
}
