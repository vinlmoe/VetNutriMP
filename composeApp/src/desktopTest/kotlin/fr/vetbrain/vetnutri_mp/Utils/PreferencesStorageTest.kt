package fr.vetbrain.vetnutri_mp.Utils

import java.nio.file.Files
import java.io.File
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest

class PreferencesStorageTest {
    private lateinit var home: String
    private lateinit var directory: File

    @BeforeTest fun prepare() {
        home = System.getProperty("user.home")
        directory = Files.createTempDirectory("vetnutri-preferences-test").toFile()
        System.setProperty("user.home", directory.path)
    }
    @AfterTest fun cleanup() {
        System.setProperty("user.home", home)
        directory.deleteRecursively()
    }

    @Test fun separateInstancesPreserveEachOthersKeysAndObserveFreshReads() = runTest {
        val first = PreferencesStorage()
        val second = PreferencesStorage()
        first.saveString("selected_locale", "en")
        second.saveString("app_preferences", "custom")
        assertEquals("en", second.getString("selected_locale", "fr"))
        assertEquals("custom", first.getString("app_preferences", ""))
        assertEquals("en", PreferencesStorage().getString("selected_locale", "fr"))
    }

    @Test fun concurrentWritesPreserveAllKeys() = runTest {
        coroutineScope {
            repeat(30) { i -> launch(Dispatchers.Default) { PreferencesStorage().saveString("key$i", "$i") } }
        }
        val reader = PreferencesStorage()
        repeat(30) { i -> assertEquals("$i", reader.getString("key$i", "missing")) }
    }

    @Test fun removedKeysAreNotResurrectedByOldInstances() = runTest {
        val first = PreferencesStorage()
        first.saveString("old", "value")
        val second = PreferencesStorage()
        first.remove("old")
        second.saveString("new", "value")
        assertFalse(first.contains("old"))
        first.clear()
        assertFalse(second.contains("new"))
    }

    @Test fun ioErrorsArePropagatedInsteadOfReportedAsSaved() = runTest {
        File(directory, ".vetnutri_preferences.properties").mkdir()
        assertFails { PreferencesStorage().saveString("key", "value") }
    }
}
