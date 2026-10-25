package fr.vetbrain.vetnutri_mp.Utils

import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Chiffrement des partages (AES-CBC + HMAC-SHA256). Exercé sur la JVM car PlatformCrypto
 * est un expect/actual : le même contrat vaut pour Android et iOS.
 */
class CryptoUtilsTest {

    private val json = """{"animal":{"nom":"Rex","poids":12.5},"consultations":[1,2,3]}"""

    private fun String.b64(): ByteArray = decodeBase64()!!.toByteArray()
    private fun ByteArray.b64(): String = ByteString.of(*this).base64()

    @Test
    fun `aller-retour chiffrement authentifie`() {
        val r = CryptoUtils.encryptJson(json)
        assertNotNull(r.tagBase64, "un chiffrement neuf porte toujours une étiquette")
        assertEquals(32, r.keyBase64.b64().size)
        assertEquals(16, r.ivBase64.b64().size)
        assertEquals(32, r.tagBase64!!.b64().size)
        assertEquals(json, CryptoUtils.decryptJson(r.cipherTextBase64, r.keyBase64, r.ivBase64, r.tagBase64))
    }

    @Test
    fun `texte vide et unicode`() {
        for (plain in listOf("", "é à ü — 日本語 🐶")) {
            val r = CryptoUtils.encryptJson(plain)
            assertEquals(plain, CryptoUtils.decryptJson(r.cipherTextBase64, r.keyBase64, r.ivBase64, r.tagBase64))
        }
    }

    @Test
    fun `chaque envoi utilise une cle et un IV neufs`() {
        val a = CryptoUtils.encryptJson(json)
        val b = CryptoUtils.encryptJson(json)
        assertTrue(a.keyBase64 != b.keyBase64)
        assertTrue(a.ivBase64 != b.ivBase64)
        assertTrue(a.cipherTextBase64 != b.cipherTextBase64)
    }

    @Test
    fun `texte chiffre altere est refuse`() {
        val r = CryptoUtils.encryptJson(json)
        val tampered = r.cipherTextBase64.b64().also { it[0] = (it[0].toInt() xor 0x01).toByte() }.b64()
        assertFailsWith<IllegalArgumentException> {
            CryptoUtils.decryptJson(tampered, r.keyBase64, r.ivBase64, r.tagBase64)
        }
    }

    @Test
    fun `IV altere est refuse`() {
        val r = CryptoUtils.encryptJson(json)
        val tamperedIv = r.ivBase64.b64().also { it[3] = (it[3].toInt() xor 0x80).toByte() }.b64()
        assertFailsWith<IllegalArgumentException> {
            CryptoUtils.decryptJson(r.cipherTextBase64, r.keyBase64, tamperedIv, r.tagBase64)
        }
    }

    @Test
    fun `etiquette alteree ou de mauvaise taille est refusee`() {
        val r = CryptoUtils.encryptJson(json)
        val tag = r.tagBase64!!.b64()
        val flipped = tag.copyOf().also { it[31] = (it[31].toInt() xor 0x01).toByte() }.b64()
        assertFailsWith<IllegalArgumentException> {
            CryptoUtils.decryptJson(r.cipherTextBase64, r.keyBase64, r.ivBase64, flipped)
        }
        assertFailsWith<IllegalArgumentException> {
            CryptoUtils.decryptJson(r.cipherTextBase64, r.keyBase64, r.ivBase64, tag.copyOf(16).b64())
        }
    }

    @Test
    fun `mauvaise cle est refusee avant dechiffrement`() {
        val r = CryptoUtils.encryptJson(json)
        val otherKey = PlatformCrypto.secureRandomBytes(32).b64()
        assertFailsWith<IllegalArgumentException> {
            CryptoUtils.decryptJson(r.cipherTextBase64, otherKey, r.ivBase64, r.tagBase64)
        }
    }

    @Test
    fun `format herite v1 sans etiquette reste lisible`() {
        // Un bin partagé avant l'authentification : AES-CBC direct avec la clé maîtresse.
        val key = PlatformCrypto.secureRandomBytes(32)
        val iv = PlatformCrypto.secureRandomBytes(16)
        val legacyCipher = PlatformCrypto.aesCbcEncrypt(json.encodeToByteArray(), key, iv)
        assertEquals(json, CryptoUtils.decryptJson(legacyCipher.b64(), key.b64(), iv.b64(), tagBase64 = null))
    }

    @Test
    fun `hmac sha256 vecteur RFC 4231`() {
        // Cas de test n°2 de la RFC 4231 : key = "Jefe", data = "what do ya want for nothing?"
        val mac = PlatformCrypto.hmacSha256("Jefe".encodeToByteArray(), "what do ya want for nothing?".encodeToByteArray())
        assertEquals(
            "5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843",
            ByteString.of(*mac).hex()
        )
    }

    @Test
    fun `comparaison temps constant`() {
        val a = byteArrayOf(1, 2, 3)
        assertTrue(CryptoUtils.constantTimeEquals(a, byteArrayOf(1, 2, 3)))
        assertTrue(!CryptoUtils.constantTimeEquals(a, byteArrayOf(1, 2, 4)))
        assertTrue(!CryptoUtils.constantTimeEquals(a, byteArrayOf(1, 2)))
        assertTrue(CryptoUtils.constantTimeEquals(ByteArray(0), ByteArray(0)))
    }
}
