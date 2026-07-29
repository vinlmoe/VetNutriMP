package fr.vetbrain.vetnutri_mp.Utils

import okio.ByteString
import okio.ByteString.Companion.decodeBase64

internal data class EncryptionResult(
    val cipherTextBase64: String,
    val keyBase64: String,
    val ivBase64: String
)

internal object CryptoUtils {
    private const val KEY_SIZE_BYTES = 32
    private const val IV_SIZE_BYTES = 16

    fun encryptJson(plainText: String): EncryptionResult {
        val key = PlatformCrypto.secureRandomBytes(KEY_SIZE_BYTES)
        val iv = PlatformCrypto.secureRandomBytes(IV_SIZE_BYTES)
        val cipherText = PlatformCrypto.aesCbcEncrypt(plainText.encodeToByteArray(), key, iv)
        return EncryptionResult(
            cipherTextBase64 = cipherText.toBase64(),
            keyBase64 = key.toBase64(),
            ivBase64 = iv.toBase64()
        )
    }

    fun decryptJson(cipherTextBase64: String, keyBase64: String, ivBase64: String): String {
        val cipherText = cipherTextBase64.fromBase64()
        val key = keyBase64.fromBase64()
        val iv = ivBase64.fromBase64()
        val plainBytes = PlatformCrypto.aesCbcDecrypt(cipherText, key, iv)
        return plainBytes.decodeToString()
    }

    private fun ByteArray.toBase64(): String = ByteString.of(*this).base64()

    private fun String.fromBase64(): ByteArray =
        this.decodeBase64()?.toByteArray()
            ?: throw IllegalArgumentException("Base64 invalide")
}

internal expect object PlatformCrypto {
    fun secureRandomBytes(size: Int): ByteArray
    fun aesCbcEncrypt(plain: ByteArray, key: ByteArray, iv: ByteArray): ByteArray
    fun aesCbcDecrypt(cipher: ByteArray, key: ByteArray, iv: ByteArray): ByteArray
}
