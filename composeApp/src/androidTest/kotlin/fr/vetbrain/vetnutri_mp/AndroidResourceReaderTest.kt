package fr.vetbrain.vetnutri_mp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import fr.vetbrain.vetnutri_mp.Localization.ResourceReader
import kotlin.test.assertFailsWith
import org.junit.Before
import org.junit.Test

class AndroidResourceReaderTest {
    private lateinit var resourceReader: ResourceReader
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        resourceReader = ResourceReader()
    }

    @Test
    fun testReadExistingResource() {
        val content = resourceReader.readResource("strings_fr.json")
        assert(content.contains("translations"))
    }

    @Test
    fun testReadNonExistentResource() {
        assertFailsWith<Exception> { resourceReader.readResource("non_existent_file.json") }
    }
}
