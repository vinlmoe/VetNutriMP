package fr.vetbrain.vetnutri_mp.Localization

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlinx.serialization.json.Json

/**
 * Cohérence entre les clés déclarées dans `LocalizationKeys.kt` et les fichiers de traduction
 * `strings_*.json` (commonMain/resources) :
 * - chaque clé déclarée existe dans `strings_fr.json` (langue de référence, repli de
 *   `LocalizationManager`) ;
 * - les langues complètes (fr, en, zh) ont exactement le même jeu de clés ;
 * - les autres langues (partielles, voir `LocalizationManager.loadStrings` qui replie sur fr) sont
 *   au moins des fichiers valides et non vides.
 *
 * Les constantes de `LocalizationKeys` ne sont pas énumérables à l'exécution sans réflexion :
 * le fichier source est lu et ses littéraux de chaîne (`= "..."`) sont extraits. Les fichiers
 * sont résolus depuis le répertoire de travail du test (module `composeApp`) ou la racine du
 * projet, comme le fait `ResourceReader` desktop.
 */
class LocalizationKeysConsistencyTest {

    private val langues = listOf("fr", "en", "zh", "de", "es", "it", "ja", "ro", "uk")
    private val languesCompletes = listOf("fr", "en", "zh")
    private val json = Json { ignoreUnknownKeys = true }

    /** Résout un chemin relatif au module composeApp, quel que soit le répertoire de travail. */
    private fun fichierProjet(cheminDansComposeApp: String): File {
        val candidats = listOf(
            File(cheminDansComposeApp),
            File("composeApp/$cheminDansComposeApp"),
            File("../composeApp/$cheminDansComposeApp")
        )
        return candidats.firstOrNull { it.isFile }
            ?: fail("Fichier introuvable : $cheminDansComposeApp (répertoire courant : ${File(".").absolutePath})")
    }

    private fun clesDeclarees(): Set<String> {
        val source = fichierProjet("src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/Localization/LocalizationKeys.kt").readText()
        val cles = Regex("=\\s*\"([^\"]+)\"").findAll(source).map { it.groupValues[1] }.toSet()
        assertTrue(cles.size > 500, "extraction des clés suspecte : ${cles.size} clés trouvées")
        return cles
    }

    private fun traductions(langue: String): Map<String, String> {
        val texte = fichierProjet("src/commonMain/resources/strings_$langue.json").readText()
        return json.decodeFromString(LocalizedStrings.serializer(), texte).translations
    }

    @Test
    fun toutesLesClesDeclareesExistentEnFrancais() {
        val fr = traductions("fr")
        val manquantes = clesDeclarees().filter { it !in fr }.sorted()
        assertTrue(manquantes.isEmpty(), "clés de LocalizationKeys absentes de strings_fr.json : $manquantes")
    }

    @Test
    fun traductionsFrancaises_nonVides() {
        val vides = traductions("fr").filterValues { it.isBlank() }.keys.sorted()
        assertTrue(vides.isEmpty(), "traductions françaises vides : $vides")
    }

    @Test
    fun languesCompletes_ontLeMemeJeuDeCles() {
        val fr = traductions("fr").keys
        for (langue in languesCompletes - "fr") {
            val cles = traductions(langue).keys
            val manquantes = (fr - cles).sorted()
            val enTrop = (cles - fr).sorted()
            assertTrue(manquantes.isEmpty(), "clés absentes de strings_$langue.json : $manquantes")
            assertTrue(enTrop.isEmpty(), "clés de strings_$langue.json inconnues en français : $enTrop")
        }
    }

    @Test
    fun toutesLesLangues_fichiersValidesEtNonVides() {
        for (langue in langues) {
            val cles = traductions(langue)
            assertTrue(cles.isNotEmpty(), "strings_$langue.json ne contient aucune traduction")
            assertTrue(cles.keys.none { it.isBlank() }, "clé vide dans strings_$langue.json")
        }
    }

    @Test
    fun placeholders_identiquesEntreLanguesCompletes() {
        // Une traduction qui attend %1$s doit l'attendre dans l'autre langue. Limité à fr/en :
        // strings_zh.json omet déjà le paramètre de six libellés d'unités (alimentItem.unit.*).
        val motif = Regex("%\\d+\\$[sd]")
        fun placeholders(texte: String) = motif.findAll(texte).map { it.value }.toSortedSet()
        val fr = traductions("fr")
        for (langue in listOf("en")) {
            val autre = traductions(langue)
            val divergentes = fr.keys.filter { placeholders(fr.getValue(it)) != placeholders(autre[it] ?: "") }.sorted()
            assertEquals(emptyList<String>(), divergentes, "paramètres %n\$s différents entre fr et $langue")
        }
    }
}
