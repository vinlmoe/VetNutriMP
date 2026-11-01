package fr.vetbrain.vetnutri_mp.Export

import java.io.ByteArrayInputStream
import java.util.zip.CRC32
import java.util.zip.ZipInputStream
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ArchiveZipTest {

    @Test
    fun archive_luePar_javaUtilZip() {
        val image = ByteArray(5000) { (it * 31).toByte() }
        val fichiers = listOf(
                "graphiques.md" to "# Énergie — Σ rations\n".encodeToByteArray(),
                "images/énergie.jpg" to image
        )
        val lus = mutableMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(ArchiveZip.creer(fichiers))).use { zip ->
            while (true) {
                val entree = zip.nextEntry ?: break
                lus[entree.name] = zip.readBytes()
            }
        }
        assertEquals(setOf("graphiques.md", "images/énergie.jpg"), lus.keys)
        assertEquals("# Énergie — Σ rations\n", lus.getValue("graphiques.md").decodeToString())
        assertContentEquals(image, lus.getValue("images/énergie.jpg"))
    }

    @Test
    fun crc32_commeJava() {
        val octets = "VetNutri 123 éà".encodeToByteArray()
        val java = CRC32().apply { update(octets) }.value
        assertEquals(java, ArchiveZip.crc32(octets).toLong() and 0xFFFFFFFFL)
    }
}
