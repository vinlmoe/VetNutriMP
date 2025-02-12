package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.Localization.ResourceReader
import java.io.File
import kotlin.test.*

class DesktopResourceReaderTest {
    private lateinit var resourceReader: ResourceReader
    private lateinit var testResourceDir: File

    @BeforeTest
    fun setup() {
        resourceReader = ResourceReader()
        testResourceDir = File("src/commonMain/resources").apply { mkdirs() }
    }

    @Test
    fun `test read existing resource`() {
        // Créer un fichier de test
        val testFile = File(testResourceDir, "test_resource.json")
        testFile.writeText("""{"test": "value"}""")

        val content = resourceReader.readResource("test_resource.json")
        assertEquals("""{"test": "value"}""", content)

        testFile.delete()
    }

    @Test
    fun `test read non-existent resource`() {
        assertFailsWith<IllegalStateException> {
            resourceReader.readResource("non_existent_file.json")
        }
    }

    @AfterTest
    fun cleanup() {
        testResourceDir.deleteRecursively()
    }
}
