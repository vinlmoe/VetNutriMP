package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Utils.Recherche
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatalogueRacesTest {

    @Test
    fun catalogueCompletDansLesTroisLangues() {
        // Les 360 races FCI, leurs variétés courantes et quelques races hors FCI
        assertTrue(CatalogueRaces.chiens.size >= 360, "chiens : ${CatalogueRaces.chiens.size}")
        assertTrue(CatalogueRaces.chats.size >= 70, "chats : ${CatalogueRaces.chats.size}")
        (CatalogueRaces.chiens + CatalogueRaces.chats).forEach { race ->
            assertTrue(race.fr.isNotBlank() && race.en.isNotBlank() && race.zh.isNotBlank(), race.toString())
        }
    }

    @Test
    fun aucunNomEnDoublonDansUneEspece() {
        listOf(CatalogueRaces.chiens, CatalogueRaces.chats).forEach { races ->
            listOf<(RaceCatalogue) -> String>({ it.fr }, { it.en }, { it.zh }).forEach { nom ->
                val doublons = races.groupBy { Recherche.normaliser(nom(it)) }.filterValues { it.size > 1 }.keys
                assertTrue(doublons.isEmpty(), "doublons : $doublons")
            }
        }
    }

    @Test
    fun libellesDansLaLangueDeLInterface() {
        val labrador = CatalogueRaces.chiens.first { it.en == "Labrador Retriever" }
        assertEquals("Labrador retriever", labrador.nom("fr"))
        assertEquals("拉布拉多寻回犬", labrador.nom("zh"))
        // Langue sans traduction : anglais
        assertEquals("Labrador Retriever", labrador.nom("de"))
    }

    @Test
    fun rechercheParNimporteQuelNomEtSansAccent() {
        val fr = CatalogueRaces.suggestions(Espece.CHIEN, "fr")
        assertEquals("Berger allemand", CatalogueRaces.filtrer("german shepherd", fr).first().libelle)
        assertEquals("Épagneul breton", CatalogueRaces.filtrer("epagneul bre", fr).first().libelle)
        assertEquals("Labrador retriever", CatalogueRaces.filtrer("拉布拉多", fr).first().libelle)

        val zh = CatalogueRaces.suggestions(Espece.CHAT, "zh")
        assertEquals("布偶猫", CatalogueRaces.filtrer("ragdoll", zh).first().libelle)

        val en = CatalogueRaces.suggestions(Espece.CHAT, "en")
        assertEquals("Birman", CatalogueRaces.filtrer("sacre de birmanie", en).first().libelle)
    }

    @Test
    fun leDebutDuLibellePasseEnPremier() {
        val fr = CatalogueRaces.suggestions(Espece.CHIEN, "fr")
        val resultats = CatalogueRaces.filtrer("berger", fr, max = 50)
        assertTrue(resultats.size > 10)
        assertTrue(resultats.take(5).all { Recherche.normaliser(it.libelle).startsWith("berger") })
    }

    @Test
    fun racesSaisiesAjouteesSansDoublerLeCatalogue() {
        val suggestions =
                CatalogueRaces.suggestions(Espece.CHAT, "fr", listOf("maine coon", "Croisé siamois", " ", "croise siamois"))
        assertEquals(1, suggestions.count { Recherche.normaliser(it.libelle) == "maine coon" })
        assertEquals(1, suggestions.count { Recherche.normaliser(it.libelle) == "croise siamois" })
        assertEquals("Croisé siamois", CatalogueRaces.filtrer("croisé si", suggestions).first().libelle)
    }

    @Test
    fun especeSansCatalogueEtChoixFait() {
        assertTrue(CatalogueRaces.suggestions(Espece.LAPIN, "fr").isEmpty())
        val fr = CatalogueRaces.suggestions(Espece.CHIEN, "fr")
        assertTrue(CatalogueRaces.filtrer("", fr).isEmpty())
        // Race déjà choisie : plus de liste ouverte
        assertTrue(CatalogueRaces.filtrer("Boxer", fr).isEmpty())
    }
}

class TraductionRacesTest {

    @Test
    fun raceEnregistreeTraduiteDansLaLangueDeLInterface() {
        assertEquals("German Shepherd Dog", CatalogueRaces.traduire("Berger allemand", Espece.CHIEN, "en"))
        assertEquals("德国牧羊犬", CatalogueRaces.traduire("berger allemand", Espece.CHIEN, "zh"))
        assertEquals("Berger allemand", CatalogueRaces.traduire("German Shepherd Dog", Espece.CHIEN, "fr"))
        assertEquals("Persan", CatalogueRaces.traduire("波斯猫", Espece.CHAT, "fr"))
        // Synonyme propre à une race
        assertEquals("Labrador Retriever", CatalogueRaces.traduire("Labrador", Espece.CHIEN, "en"))
    }

    @Test
    fun saisieLibreOuAmbigueGardeeTelleQuelle() {
        assertEquals("Croisé labrador x husky", CatalogueRaces.traduire("Croisé labrador x husky", Espece.CHIEN, "en"))
        // « Corgi » désigne deux races : pas de traduction
        assertEquals("Corgi", CatalogueRaces.traduire("Corgi", Espece.CHIEN, "zh"))
        // Un nom principal l'emporte sur le synonyme d'une autre race
        assertEquals("Siberian", CatalogueRaces.traduire("Sibérien", Espece.CHAT, "en"))
        assertEquals("Akita", CatalogueRaces.traduire("秋田犬", Espece.CHIEN, "en"))
        // Espèce sans catalogue
        assertEquals("Bélier nain", CatalogueRaces.traduire("Bélier nain", Espece.LAPIN, "en"))
        assertEquals("", CatalogueRaces.traduire("", Espece.CHIEN, "en"))
    }

    @Test
    fun especeInconnueChercheeChezLeChienEtLeChat() {
        assertEquals("Maine Coon", CatalogueRaces.traduire("Maine coon", null, "en"))
        assertEquals("Beagle", CatalogueRaces.traduire("Beagle", null, "en"))
    }

    @Test
    fun anciensNomsDImportReconnus() {
        // Noms produits par RaceCodeMapper à l'import des anciens fichiers
        assertEquals("Norwegian Forest Cat", CatalogueRaces.traduire("Norvégien ou Skogkatt", Espece.CHAT, "en"))
        assertEquals("Italian Sighthound", CatalogueRaces.traduire("Levrette italienne", Espece.CHIEN, "en"))
        assertEquals("Belgian Shepherd Dog", CatalogueRaces.traduire("Berger Belge", Espece.CHIEN, "en"))
    }

    @Test
    fun aucunNomPrincipalPartageEntreDeuxRaces() {
        listOf(Espece.CHIEN, Espece.CHAT).forEach { espece ->
            CatalogueRaces.races(espece).forEach { race ->
                listOf(race.fr, race.en, race.zh).forEach { nom ->
                    assertEquals(race, CatalogueRaces.trouver(nom, espece), nom)
                }
            }
        }
    }
}
