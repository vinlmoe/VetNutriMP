package fr.vetbrain.vetnutri_mp.Utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RechercheTest {

    @Test
    fun normaliser_enleveCasseAccentsEtLigatures() {
        assertEquals("oeuf entier", Recherche.normaliser("Œuf entier"))
        assertEquals("creme fraiche", Recherche.normaliser("Crème fraîche"))
        assertEquals("caecum", Recherche.normaliser("cæcum"))
        assertEquals("boeuf", Recherche.normaliser("BŒUF"))
        assertEquals("mais", Recherche.normaliser("Maïs"))
    }

    @Test
    fun normaliser_reduitSeparateurs() {
        assertEquals("l agneau", Recherche.normaliser("l’agneau"))
        assertEquals("pomme de terre", Recherche.normaliser("  Pomme-de -terre "))
        assertEquals("100 g", Recherche.normaliser("100 g"))
    }

    @Test
    fun normaliser_texteDecompose() {
        // « é » écrit e + accent aigu combinant
        assertEquals("pate", Recherche.normaliser("pâté"))
    }

    @Test
    fun correspond_ligatureDansLesDeuxSens() {
        assertTrue(Recherche.correspond("oeuf", "Œuf dur"))
        assertTrue(Recherche.correspond("œuf", "Oeuf dur"))
        assertTrue(Recherche.correspond("coeur", "Cœur de bœuf"))
    }

    @Test
    fun correspond_accentsDansLesDeuxSens() {
        assertTrue(Recherche.correspond("creme", "Crème"))
        assertTrue(Recherche.correspond("CRÈME", "creme"))
        assertTrue(Recherche.correspond("l'agneau", "Épaule de l’agneau"))
    }

    @Test
    fun correspond_chaqueMotDansUnChampQuelconque() {
        assertTrue(Recherche.correspond("canin renal", "Renal", "Royal Canin"))
        assertFalse(Recherche.correspond("canin hepatic", "Renal", "Royal Canin"))
        assertTrue(Recherche.correspond("", "Renal"))
        assertTrue(Recherche.correspond("  ", null))
    }

    @Test
    fun correspond_unMotNeChevauchePasDeuxChamps() {
        assertFalse(Recherche.correspond("alca", "Royal", "Canin"))
        assertTrue(Recherche.correspond("al ca", "Royal", "Canin"))
    }
}
