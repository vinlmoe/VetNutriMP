package fr.vetbrain.vetnutri_mp.Data

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * `src/commonMain/resources` est la source unique des données initiales
 * (`data/vetnutri_export_init.json`) et des traductions (`strings_*.json`). Les copies du bundle
 * iOS (référencées par le projet Xcode) sont régénérées par la tâche Gradle `syncInitialDataIos` ;
 * ce test échoue si elles ont divergé, ce qui arrivait quand les copies étaient tenues à la main.
 */
class InitialDataSyncTest {

    private val moduleDir: File = File(System.getProperty("user.dir")).let { dir ->
        if (File(dir, "src/commonMain").isDirectory) dir else File(dir, "composeApp")
    }

    private val source = File(moduleDir, "src/commonMain/resources/data/vetnutri_export_init.json")

    @Test
    fun laCopieIosEstIdentiqueALaSource() {
        val ios = File(moduleDir.parentFile, "iosApp/iosApp/vetnutri_export_init.json")
        assertTrue(source.isFile, "Source absente : $source")
        assertTrue(ios.isFile, "Copie iOS absente : $ios")
        assertTrue(
            source.readBytes().contentEquals(ios.readBytes()),
            "iosApp/iosApp/vetnutri_export_init.json a divergé : lancer ./gradlew :composeApp:syncInitialDataIos"
        )
    }

    @Test
    fun aucuneCopieManuelleCoteAndroid() {
        val android = File(moduleDir, "src/androidMain/assets/data/vetnutri_export_init.json")
        assertFalse(
            android.exists(),
            "Copie Android manuelle détectée : elle est générée au build (syncInitialDataAndroidAssets)"
        )
    }

    @Test
    fun lesTraductionsIosSontIdentiquesALaSource() {
        listOf("strings_fr.json", "strings_en.json", "strings_zh.json").forEach { nom ->
            val src = File(moduleDir, "src/commonMain/resources/$nom")
            val ios = File(moduleDir.parentFile, "iosApp/iosApp/$nom")
            assertTrue(ios.isFile, "Copie iOS absente : $ios")
            assertTrue(
                src.readBytes().contentEquals(ios.readBytes()),
                "iosApp/iosApp/$nom a divergé : lancer ./gradlew :composeApp:syncInitialDataIos"
            )
        }
    }

    @Test
    fun aucuneTraductionManuelleCoteAndroid() {
        val copies = File(moduleDir, "src/androidMain/assets").listFiles { f -> f.name.startsWith("strings_") }
        assertTrue(
            copies.isNullOrEmpty(),
            "Traductions Android manuelles détectées : elles sont générées au build (syncInitialDataAndroidAssets)"
        )
    }
}
