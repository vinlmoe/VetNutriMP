package fr.vetbrain.vetnutri_mp.ExcelPlatform

import javax.swing.SwingUtilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExcelFileOperationsDesktopTest {

    @Test
    fun ensureCsvExtension_withoutExtension_appendsCsv() {
        assertEquals("references_export.csv", ensureCsvExtension("references_export"))
    }

    @Test
    fun ensureCsvExtension_alreadyPresent_leftUnchanged() {
        assertEquals("references_export.csv", ensureCsvExtension("references_export.csv"))
    }

    @Test
    fun ensureCsvExtension_otherExtension_appendsCsvAnyway() {
        // Le nom garde son extension d'origine, ".csv" est simplement ajouté à la suite
        // (comportement existant, on ne remplace pas une extension différente).
        assertEquals("notes.xlsx.csv", ensureCsvExtension("notes.xlsx"))
    }

    @Test
    fun isCsvFileOperationsSupported_true() {
        assertTrue(isCsvFileOperationsSupported())
    }

    @Test
    fun runSwingDialog_calledFromSwingCoroutine_executesOnEdtWithoutDeadlock() = runBlocking {
        val result =
                withContext(Dispatchers.Swing) {
                    runSwingDialog {
                        assertTrue(SwingUtilities.isEventDispatchThread())
                        "dialog-result"
                    }
                }

        assertEquals("dialog-result", result)
    }
}
