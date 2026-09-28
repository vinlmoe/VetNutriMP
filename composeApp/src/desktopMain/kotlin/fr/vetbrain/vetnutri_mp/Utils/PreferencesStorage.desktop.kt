package fr.vetbrain.vetnutri_mp.Utils

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.AtomicMoveNotSupportedException
import java.util.Properties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Each operation reloads under a shared lock: separate screens cannot overwrite stale snapshots. */
actual class PreferencesStorage {
    private val preferencesFile = File(System.getProperty("user.home"), ".vetnutri_preferences.properties")

    private suspend fun <T> access(write: Boolean = false, action: (Properties) -> T): T =
        withContext(Dispatchers.IO) {
            preferencesMutex.withLock {
                val properties = Properties()
                if (preferencesFile.exists()) {
                    preferencesFile.inputStream().use { properties.load(it) }
                }
                val result = action(properties)
                if (write) {
                    val parent = preferencesFile.absoluteFile.parentFile
                    check(parent.isDirectory || parent.mkdirs()) { "Cannot create preferences directory" }
                    val temporary = Files.createTempFile(parent.toPath(), "vetnutri-preferences-", ".tmp")
                    try {
                        Files.newOutputStream(temporary).use { properties.store(it, "VetNutri Preferences") }
                        try {
                            Files.move(temporary, preferencesFile.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
                        } catch (_: AtomicMoveNotSupportedException) {
                            Files.move(temporary, preferencesFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                        }
                    } finally {
                        Files.deleteIfExists(temporary)
                    }
                }
                result
            }
        }

    actual suspend fun saveString(key: String, value: String) { access(true) { it.setProperty(key, value) } }
    actual suspend fun getString(key: String, defaultValue: String): String = access { it.getProperty(key, defaultValue) }
    actual suspend fun remove(key: String) { access(true) { it.remove(key) } }
    actual suspend fun contains(key: String): Boolean = access { it.containsKey(key) }
    actual suspend fun clear() { access(true) { it.clear() } }
}

private val preferencesMutex = Mutex()
actual fun createPreferencesStorage(): PreferencesStorage = PreferencesStorage()
