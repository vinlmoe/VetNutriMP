package fr.vetbrain.vetnutri_mp.PlatformFile

import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import kotlin.test.*

class PlatformFileFailureTest {
    private lateinit var directory: PlatformFile
    @BeforeTest fun prepare() {
        directory = PlatformFile(NSTemporaryDirectory() + NSUUID().UUIDString)
        assertTrue(directory.mkdirs())
    }
    @AfterTest fun cleanup() { directory.delete() }

    @Test fun utf8RoundTrip() {
        val file = PlatformFile("${directory.path}/nested/file.json")
        file.writeText("{\"name\":\"Éléphant 🐘\"}")
        assertEquals("{\"name\":\"Éléphant 🐘\"}", file.readText())
    }
    @Test fun missingFileReadThrows() {
        assertFails { PlatformFile("${directory.path}/missing").readText() }
    }
    @Test fun invalidDestinationWriteThrows() {
        val parent = PlatformFile("${directory.path}/file")
        parent.writeText("keep")
        assertFails { PlatformFile("${parent.path}/child").writeText("data") }
        assertEquals("keep", parent.readText())
    }
    @Test fun missingSourceCopyThrows() {
        assertFails { PlatformFile("${directory.path}/missing").copyTo(PlatformFile("${directory.path}/target"), false) }
    }
    @Test fun failedOverwritePreservesExistingDestination() {
        val target = PlatformFile("${directory.path}/target")
        target.writeText("keep")
        assertFails { PlatformFile("${directory.path}/missing").copyTo(target, true) }
        assertEquals("keep", target.readText())
    }
    @Test fun successfulCopyHonorsOverwrite() {
        val source = PlatformFile("${directory.path}/source")
        val target = PlatformFile("${directory.path}/target")
        source.writeText("new")
        target.writeText("old")
        assertFails { source.copyTo(target, false) }
        assertEquals("old", target.readText())
        source.copyTo(target, true)
        assertEquals("new", target.readText())
        assertEquals("new", source.readText())
    }
}
