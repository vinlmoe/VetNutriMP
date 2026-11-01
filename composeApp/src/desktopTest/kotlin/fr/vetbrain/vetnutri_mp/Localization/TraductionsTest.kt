package fr.vetbrain.vetnutri_mp.Localization

import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

class TraductionsTest {

    private val moduleDir = File(System.getProperty("user.dir")).let { dir ->
        if (File(dir, "src/commonMain").isDirectory) dir else File(dir, "composeApp")
    }

    private fun cles(langue: String): Set<String> =
            Json.decodeFromString<LocalizedStrings>(File(moduleDir, "src/commonMain/resources/strings_$langue.json").readText())
                    .translations.keys

    @AfterTest
    fun francais() = LocalizationManager.initialize("fr")

    @Test
    fun cleAbsenteDUneLangue_afficheLeFrancaisPasLaCle() {
        // Le chinois n'a pas (encore) les textes du mode troupeau
        LocalizationManager.initialize("zh")
        assertEquals("Troupeau entier", LocalizationManager.translate("troupeau.troupeauEntier"))
        LocalizationManager.initialize("en")
        assertEquals("Whole herd", LocalizationManager.translate("troupeau.troupeauEntier"))
        assertEquals("cle.inexistante", LocalizationManager.translate("cle.inexistante"))
    }

    @Test
    fun clesUtiliseesDansLeCode_existentEnFrancaisEtEnAnglais() {
        val fr = cles("fr")
        val en = cles("en")
        val utilisees = File(moduleDir, "src/commonMain/kotlin").walkTopDown()
                .filter { it.extension == "kt" }
                .flatMap { f -> Regex("""translate\("([A-Za-z0-9_.]+)"""").findAll(f.readText()).map { it.groupValues[1] to f.name } }
                .filterNot { (cle, _) -> cle.endsWith(".") } // préfixes complétés à l'exécution
                .toList()
        val manquantes = utilisees.filter { (cle, _) -> cle !in fr || cle !in en }.distinct()
        assertTrue(manquantes.isEmpty(), "Clés absentes de strings_fr/en.json : $manquantes")
    }
}
