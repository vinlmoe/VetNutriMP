package fr.vetbrain.vetnutri_mp.Utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image as SkiaImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Teste le pipeline de décodage PNG → Skia → ImageBitmap utilisé par AppLogo.ios.kt.
 * Le code iOS charge l'icône via UIKit puis passe par ce même pipeline Skia ;
 * on le teste ici sur JVM/Desktop où Skia (Skiko) est disponible sans simulateur.
 */
class AppLogoDecoderTest {

    // PNG 1×1 pixel gris — image minimale valide connue, encodée en base64
    private val validPngBytes: ByteArray = java.util.Base64.getDecoder().decode(
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAC0lEQVQI12NgAAIABQAABjE+ibYAAAAASUVORK5CYII="
    )

    // Réplique la logique de loadAppIconPainter() sans la partie UIKit
    private fun decodeSkia(bytes: ByteArray): ImageBitmap? {
        if (bytes.isEmpty()) return null
        return try {
            SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
        } catch (_: Throwable) {
            null
        }
    }

    // ── Cas d'erreur ───────────────────────────────────────────────────────────

    @Test
    fun decode_emptyBytes_returnsNull() {
        assertNull(decodeSkia(ByteArray(0)))
    }

    @Test
    fun decode_invalidBytes_returnsNull() {
        assertNull(decodeSkia(byteArrayOf(0x00, 0x01, 0x02, 0x03, 0x04)))
    }

    @Test
    fun decode_truncatedPng_returnsNull() {
        // Signature PNG valide mais données tronquées
        val truncated = validPngBytes.copyOf(8)
        assertNull(decodeSkia(truncated))
    }

    // ── Cas nominal ───────────────────────────────────────────────────────────

    @Test
    fun decode_validPng_returnsNonNull() {
        assertNotNull(decodeSkia(validPngBytes))
    }

    @Test
    fun decode_validPng_hasExpectedWidth() {
        val bitmap = decodeSkia(validPngBytes)
        assertNotNull(bitmap)
        assertEquals(1, bitmap!!.width)
    }

    @Test
    fun decode_validPng_hasExpectedHeight() {
        val bitmap = decodeSkia(validPngBytes)
        assertNotNull(bitmap)
        assertEquals(1, bitmap!!.height)
    }

    @Test
    fun decode_sameBytesCalledTwice_returnsDifferentInstances() {
        val first = decodeSkia(validPngBytes)
        val second = decodeSkia(validPngBytes)
        assertNotNull(first)
        assertNotNull(second)
        // Vérifie que le décodeur n'est pas un singleton partagé (pas de réutilisation accidentelle)
        assert(first !== second)
    }
}
