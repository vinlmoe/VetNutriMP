package fr.vetbrain.vetnutri_mp.Data

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * `src/commonMain/resources/data/vetnutri_export_init.json` est la source unique des données
 * initiales. La copie du bundle iOS (référencée par le projet Xcode) est régénérée par la
 * tâche Gradle `syncInitialDataIos` ; ce test échoue si elle a divergé, ce qui arrivait
 * quand les copies étaient tenues à la main.
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
}
